package com.mtgi.analytics.sql;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorTrackingManager;
import com.mtgi.analytics.EventDataElement;

/**
 * A datasource which adds SQL event logging to the behavior tracking database.  Events are persisted
 * to the required {@link #setTrackingManager(BehaviorTrackingManager) BehaviorTrackingManager}.  Events
 * are of type "jdbc" unless overridden with a call to {@link #setEventType(String)}.  Event names are the
 * Statement API call that executed the SQL (e.g. "execute", "executeQuery", "executeUpdate"), with event
 * data containing the exact SQL and parameter values logged.
 */
public class BehaviorTrackingDataSource extends DelegatingDataSource {

	private static Class[] PROXY_TYPE = { BehaviorTrackingConnectionProxy.class };
	
	private String eventType = "jdbc";
	private BehaviorTrackingManager trackingManager;

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	@Required
	public void setTrackingManager(BehaviorTrackingManager trackingManager) {
		this.trackingManager = trackingManager;
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection target = getTargetDataSource().getConnection();
		return (Connection)Proxy.newProxyInstance(
				BehaviorTrackingDataSource.class.getClassLoader(), 
				PROXY_TYPE, 
				new ConnectionHandler(target));
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		Connection target = getTargetDataSource().getConnection(username, password);
		return (Connection)Proxy.newProxyInstance(
				BehaviorTrackingDataSource.class.getClassLoader(), 
				PROXY_TYPE, 
				new ConnectionHandler(target));
	}
	
	private static final String findSqlArg(Object[] args) {
		if (args != null)
			for (Object o : args)
				if (o instanceof String)
					return (String)o;
		return null;
	}

	/** base class for proxy invocation handlers, which provides a typical implementation for "equals" and "hashcode" */
	protected static abstract class HandlerStub implements InvocationHandler {

		protected Object target;
		
		public HandlerStub(Object target) {
			this.target = target;
		}
		
		/**
		 * Standard implementation of equals / hashCode for proxy handlers.  Returns a non-null result if
		 * <code>method</code> is an identity check that can be handled here; null otherwise.
		 */
		protected Object invokeIdentity(Object proxy, Method method, Object[] args) throws Throwable {
			String op = method.getName();
			if (op.equals("equals")) {
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (op.equals("hashCode")) {
				return new Integer(hashCode());
			}
			return null;
		}

		/**
		 * Invoke <code>method</code> with <code>args</code> on the delegate
		 * object for this proxy.  If the method invocation throws an InvocationTargetException,
		 * throws the original application exception instead (generally more desirable
		 * for a proxy).
		 * @return the value returned by the delegate
		 * @throws any exception thrown trying to invoke the method.
		 */
		protected Object invokeTarget(Method method, Object[] args) 
			throws Throwable
		{
			try {
				return method.invoke(target, args);
			} catch (InvocationTargetException ite) {
				throw ite.getTargetException();
			} catch (Throwable t) {
				throw t;
			}
		}
	}
	
	/**
	 * Delegates all method calls to a target connection, wrapping returned Statement instances
	 * with behavior tracking instrumentation.
	 */
	protected class ConnectionHandler extends HandlerStub {

		private boolean suspended;
		
		protected ConnectionHandler(Connection target) {
			super(target);
		}
		
		public boolean isSuspended() {
			return suspended;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			//equals & hashCode handling.
			Object stub = invokeIdentity(proxy, method, args);
			if (stub != null)
				return stub;
			
			//handle ConnectionProxy.getTargetConnection() for use by Spring.
			String op = method.getName();
			Class deClass = method.getDeclaringClass();
			if (ConnectionProxy.class == deClass && "getTargetConnection".equals(op))
				return target;
			
			//handle suspend / resume of event generation, for the benefit of jdbc persister
			//implementations that don't want to add a bunch of noise to the event log.
			if (BehaviorTrackingConnectionProxy.class == deClass) {
				if ("suspendTracking".equals(op)) {
					suspended = true;
				} else if ("resumeTracking".equals(op)) {
					suspended = false;
				}
				return null;
			}

			//all other calls are delegated to the target connection.
			Object ret = invokeTarget(method, args);

			//if the return value is a statement, wrap the statement for behavior tracking.
			Class type = method.getReturnType();
			if (Statement.class.isAssignableFrom(type)) {
				//for prepared statement, the SQL is provided when the statement is created.
				//for other statements we get the exact SQL when the statement is executed.
				String sql = findSqlArg(args);
				ret = Proxy.newProxyInstance(BehaviorTrackingDataSource.class.getClassLoader(), 
						new Class[]{ type }, new StatementHandler(this, ret, sql));
			}
			
			return ret;
		}
		
	}

	/** Causes a target Statement object to generate behavior tracking events when execute*() methods are called */
	protected class StatementHandler extends HandlerStub {
	
		private ConnectionHandler parent;
		private String sql;
		private EventDataElement parameters = new EventDataElement("parameters");
		
		public StatementHandler(ConnectionHandler parent, Object target, String sql) {
			super(target);
			this.sql = sql; //might be null.
			this.parent = parent;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object stub = invokeIdentity(proxy, method, args);
			if (stub != null)
				return stub;
			
			//only bother with the event if tracking is enabled on the parent connection
			if (!parent.isSuspended()) {
				
				String op = method.getName();
				if (op.startsWith("execute")) {
	
					//query is being executed -- start the event timer.
					BehaviorEvent event = start(op, findSqlArg(args));
					try {
						return invokeTarget(method, args);
					} catch (Throwable t) {
						event.setError(t);
						throw t;
					} finally {
						trackingManager.stop(event);
					}
					
				} else if (op.startsWith("set")) {
					//maybe parameters being set?  store up parameters in hash table until we start another execute event.
					//we have to support multiple execute() calls on the same statement object to support prepared / callable API
					String key = null;
					Object value = null;
					if (op.equals("setNull")) {
						key = args[0].toString();
					} else if (args.length == 2) {
						if (!(op.endsWith("Stream") || op.endsWith("lob"))) {
							key = args[0].toString();
							value = args[1];
						}
					} else if (args.length == 3 && ("setObject".equals(op) || "setDate".equals(op))) {
						key = args[0].toString();
						value = args[1];
					}
					
					if (key != null) {
						EventDataElement p = parameters.addElement("parameter");
						p.put("key", key);
						p.put("value", value);
					}
				}
			}
			
			return invokeTarget(method, args);
		}

		private BehaviorEvent start(String name, String sql) {
			
			//initialize statement event with the accumulated parameter and SQL information.
			BehaviorEvent event = trackingManager.createEvent(eventType, name);
			EventDataElement data = event.addData();

			//SQL could either be provided as parameter, or when statement was created.
			String actualSql = sql == null ? this.sql : sql;
			data.put("sql", actualSql);

			if (!parameters.isEmpty()) {
				//transfer parameters from buffer into event object.
				data.addElement(parameters);
				//clear out the parameter buffer for the next execute event.
				parameters = new EventDataElement("parameters");
			}
			
			trackingManager.start(event);
			return event;
		}
	}
}
