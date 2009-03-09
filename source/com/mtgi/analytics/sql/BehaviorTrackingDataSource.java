package com.mtgi.analytics.sql;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

	private static Class<?>[] PROXY_TYPE = { BehaviorTrackingConnectionProxy.class };
	
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
		if (args != null && args.length > 0 && args[0] instanceof String)
			return (String)args[0];
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
		protected final Object invokeIdentity(Object proxy, Method method, Object[] args) throws Throwable {
			String op = method.getName();
			if (op.equals("equals")) {
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			} else if (op.equals("hashCode")) {
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
		protected final Object invokeTarget(Method method, Object[] args) 
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
			Class<?> deClass = method.getDeclaringClass();
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
			Class<?> type = method.getReturnType();
			if (PreparedStatement.class.isAssignableFrom(type)) {
				//for prepared statement, the SQL is provided when the statement is created.
				String sql = findSqlArg(args);
				//for other statements we get the exact SQL when the statement is executed.
				ret = Proxy.newProxyInstance(BehaviorTrackingDataSource.class.getClassLoader(), 
						new Class[]{ type }, new PreparedStatementHandler(this, ret, sql));
			} else if (Statement.class.isAssignableFrom(type)) {
				//for other statements we get the exact SQL when the statement is executed.
				ret = Proxy.newProxyInstance(BehaviorTrackingDataSource.class.getClassLoader(), 
						new Class[]{ type }, new DynamicStatementHandler(this, ret));
			}
			
			return ret;
		}
		
	}

	/** Base invocation handler for instrumenting Statement objects with behavior tracking events. */
	protected abstract class StatementHandler extends HandlerStub {
	
		private ConnectionHandler parent;
		private EventDataElement batch;
		
		public StatementHandler(ConnectionHandler parent, Object target) {
			super(target);
			this.parent = parent;
		}
		
		/** 
		 * notification that a statement has been added to the current batch.  Subclasses must implement this method
		 * to add any useful parameter info to <code>batchData</code>.
		 */
		protected abstract void addBatch(EventDataElement batchData, Object[] args);
		/**
		 * notification that a non-batch statement has been executed.  Subclasses
		 * must implement this method to add any useful parameter data to <code>event</code>.
		 */
		protected abstract void addExecuteParameters(BehaviorEvent event, Object[] args);

		/**
		 * Intercept an event call on the underlying statement object.
		 * If the method represents a statement execution, a behavior tracking event will
		 * be recorded, including any event data gathered from preceding calls to
		 * {@link #addBatch(EventDataElement, Object[])}, {@link #addExecuteParameters(BehaviorEvent, Object[])},
		 * and {@link #addOperationData(String, Object[])}.
		 */
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object stub = invokeIdentity(proxy, method, args);
			if (stub != null)
				return stub;
			
			//only bother with the event if tracking is enabled on the parent connection
			if (!parent.isSuspended()) {
				
				String op = method.getName();
				if (op.startsWith("execute")) {
					//query or batch is being executed -- start the event timer.
					BehaviorEvent event = start(op, args);
					try {
						return invokeTarget(method, args);
					} catch (Throwable t) {
						event.setError(t);
						throw t;
					} finally {
						trackingManager.stop(event);
					}
					
				} else if (op.equals("addBatch")) {
					//statement is being rolled up into a batch for execution,
					//add parameter and sql info to event data.
					if (batch == null)
						batch = new EventDataElement("batch");
					addBatch(batch, args);
				} else {
					addOperationData(op, args);
				}
			}
			
			return invokeTarget(method, args);
		}
		
		/**
		 * Hook for subclasses to extract any data from a Statement method call that is
		 * not an execution.  E.g. prepared statements receive parameter data from
		 * various setXX() calls.  Default behavior does nothing.
		 */
		protected void addOperationData(String op, Object[] args) {
		}
		
		/** 
		 * Create and start a tracking event, corresponding to an executeXX() method call on
		 * a statement object.
		 * @param name the name of the execute method (e.g. execute, executeUpdate, executeBatch, etc).  {@link #createEvent(String)} will be called with this argument.
		 * @param args the arguments passed to the execute method.  {@link #addExecuteParameters(BehaviorEvent, Object[])} will be called for non-batch executions.
		 */
		private BehaviorEvent start(String name, Object[] args) {
			BehaviorEvent event = createEvent(name);
			if (name.endsWith("Batch")) {
				//consolidate batch call execution data into root data element.
				if (batch != null) {
					event.addData().addElement(batch);
					batch = null;
				}
			} else {
				addExecuteParameters(event, args);
			}

			trackingManager.start(event);
			return event;
		}
		
		/**
		 * Create, but do not start, a new behavior tracking event for the given execute method name.
		 * Simply calls {@link BehaviorTrackingManager#createEvent(String, String)}.
		 */
		protected BehaviorEvent createEvent(String name) {
			//initialize statement event with the accumulated parameter and SQL information.
			return trackingManager.createEvent(eventType, name);
		}
	}
	
	/**
	 * Behavior tracking logic for prepared and callable statements.
	 */
	protected class PreparedStatementHandler extends StatementHandler {

		private String sql;
		private EventDataElement parameters = new EventDataElement("parameters");
		
		public PreparedStatementHandler(ConnectionHandler parent, Object target, String sql) {
			super(parent, target);
			this.sql = sql;
		}

		/** overridden to append the prepared statement SQL to the newly created event */
		@Override
		protected BehaviorEvent createEvent(String name) {
			BehaviorEvent ret = trackingManager.createEvent(eventType, name);
			ret.addData().addElement("sql").setText(sql);
			return ret;
		}

		/** overridden to add prepared statement parameter data to the batch data element */
		@Override
		protected void addBatch(EventDataElement batchData, Object[] args) {
			//prepared statement batch.  add any parameters to 
			//event info and reset for next statement.
			batchData.addElement(parameters);
			parameters = new EventDataElement("parameters");
		}

		/** overridden to add prepared statement parameter data to the execute event */
		@Override
		protected void addExecuteParameters(BehaviorEvent event, Object[] args) {
			if (!parameters.isEmpty()) {
				//transfer parameters from buffer into event object.
				event.addData().addElement(parameters);
				//clear out the parameter buffer for the next execute event.
				parameters = new EventDataElement("parameters");
			}
		}

		/**
		 * Overridden to read any prepared statement parameter info out of the given method call data,
		 * for inclusion in the next {@link #addBatch(EventDataElement, Object[])} or {@link #addExecuteParameters(BehaviorEvent, Object[])}
		 * call.
		 */
		@Override
		protected void addOperationData(String op, Object[] args) {
			//maybe parameters being set?  store up parameters in 'parameters' element until we start another execute event or batch statement.
			//we have to support multiple execute() calls on the same statement object to support prepared / callable API
			if (op.startsWith("set")) {
				Object key = null;
				Object value = null;
				if (op.equals("setNull")) {
					key = args[0];
				} else if (args.length == 2) {
					if (!(op.endsWith("Stream") || op.endsWith("lob"))) {
						key = args[0];
						value = args[1];
					}
				} else if (args.length == 3 && ("setObject".equals(op) || "setDate".equals(op))) {
					key = args[0];
					value = args[1];
				}
				
				if (key != null) {
					EventDataElement v = parameters.addElement("param");
					if (value != null)
						v.setText(value.toString());
				}
			}
		}
	}
	
	/**
	 * Behavior tracking logic for dynamic (not prepared or callable) sql statements.
	 */
	protected class DynamicStatementHandler extends StatementHandler {

		public DynamicStatementHandler(ConnectionHandler parent, Object target) {
			super(parent, target);
		}
		
		/** overridden to add the static sql from <code>args</code> to the event data */
		@Override
		protected void addExecuteParameters(BehaviorEvent event, Object[] args) {
			event.addData().addElement("sql").setText(findSqlArg(args));
		}

		/** overridden to add the static sql from <code>args</code> to the event data */
		@Override
		protected void addBatch(EventDataElement batchData, Object[] args) {
			//static SQL batch. add an element to contain SQL.
			batchData.addElement("sql").setText(findSqlArg(args));
		}
	}
	
}
