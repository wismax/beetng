package com.mtgi.analytics;

import static org.springframework.jdbc.support.JdbcUtils.closeResultSet;
import static org.springframework.jdbc.support.JdbcUtils.closeStatement;
import static org.springframework.jdbc.support.JdbcUtils.supportsBatchUpdates;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Queue;

import javax.xml.stream.XMLOutputFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.ConnectionProxy;

import com.mtgi.analytics.sql.BehaviorTrackingConnectionProxy;

/**
 * Basic implementation of {@link BehaviorEventPersister}, which uses JDBC
 * batching to persist instances of {@link BehaviorEvent} using configurable
 * insert SQL.  An instance of {@link EventDataElementSerializer} is used
 * to convert event data to XML documents for insertion.
 */
public class JdbcBehaviorEventPersisterImpl extends JdbcDaoSupport
	implements BehaviorEventPersister {

	private static final String DEFAULT_ID_SQL = "select S_BEHAVIOR_TRACKING_EVENT.nextval from dual";
	private static final String DEFAULT_INSERT_SQL = 
		"insert into BEHAVIOR_TRACKING_EVENT " +
			"(EVENT_ID, PARENT_EVENT_ID, APPLICATION, TYPE, NAME, START, DURATION_MS, USER_ID, SESSION_ID, ERROR, DATA) values " +
			"(       ?,               ?,           ?,    ?,    ?,     ?,           ?,       ?,          ?,     ?,    ?)";
	
	private int batchSize = 25;
	private String insertSql = DEFAULT_INSERT_SQL;
	private String idSql = DEFAULT_ID_SQL;
	
	private XMLOutputFactory xmlFactory;

	/**
	 * Set the JDBC batch size for executing inserts.  Only has effect if the JDBC driver
	 * supports statement batching. Defaults to 25 if unspecified.
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	/**
	 * Set the SQL select statement used to retrieve a new primary key value for an event
	 * prior to insert.  Default is
	 * <pre>select S_BEHAVIOR_TRACKING_EVENT.nextval from dual</pre>
	 * if unspecified.
	 */
	public void setIdSql(String idSql) {
		this.idSql = idSql;
	}

	/**
	 * Set the SQL statement used to insert a new behavior event record into the database.
	 * The SQL statement must take exactly 11 parameters, which must accept the following 
	 * event values in order:
	 * <ol>
	 * <li>EVENT ID</li> 
	 * <li>PARENT EVENT ID</li> 
	 * <li>APPLICATION</li> 
	 * <li>TYPE</li> 
	 * <li>NAME</li> 
	 * <li>START</li> 
	 * <li>DURATION</li> 
	 * <li>USER ID</li> 
	 * <li>SESSION ID</li> 
	 * <li>ERROR</li> 
	 * <li>DATA</li>
	 * </ol>
	 * 
	 * Default is 
	 * <pre>insert into BEHAVIOR_TRACKING_EVENT
	 *  (EVENT_ID, PARENT_EVENT_ID, APPLICATION, TYPE, NAME, START, DURATION_MS, USER_ID, SESSION_ID, ERROR, DATA) 
	 *   values 
	 *  (       ?,               ?,           ?,    ?,    ?,     ?,           ?,       ?,          ?,     ?,    ?)
	 * </pre>
	 */
	public void setInsertSql(String insertSql) {
		this.insertSql = insertSql;
	}

	@Override
	protected void initDao() throws Exception {
		super.initDao();
		this.xmlFactory = XMLOutputFactory.newInstance();
	}

	public int persist(final Queue<BehaviorEvent> events) {

		int count = 0;
		if (!events.isEmpty()) {
		
			count = (Integer)getJdbcTemplate().execute(new ConnectionCallback() {
	
				public Object doInConnection(Connection con) throws SQLException, DataAccessException {
	
					//if this connection is behavior tracking, suspend tracking.
					//we don't generate more events while persisting.
					BehaviorTrackingConnectionProxy bt = null;
					for (Connection c = con; 
						 bt == null && c instanceof ConnectionProxy; 
						 c = ((ConnectionProxy)c).getTargetConnection()) 
					{
						if (c instanceof BehaviorTrackingConnectionProxy) {
							bt = (BehaviorTrackingConnectionProxy)c;
							bt.suspendTracking();
						}
					}
					
					try {
						int tally = 0; //return total events persisted.
						
						boolean doBatch = supportsBatchUpdates(con);
						EventDataElementSerializer dataSerializer = new EventDataElementSerializer(xmlFactory);
	
						PreparedStatement insert = con.prepareStatement(insertSql);
						try {
							
							PreparedStatement idQuery = con.prepareStatement(idSql);
							try {
								
								//keep track of statements added to the batch so that we can time our
								//flushes.
								int batchCount = 0;
								
								//go until the queue is drained.
								while (!events.isEmpty()) {
									
									//pop the next event off of the queue.  event may already have an ID assigned if any
									//of its child events has been persisted.
									BehaviorEvent next = events.remove();
									assignIds(next, idQuery);

									//populate identifying information for the event into the insert statement.
									insert.setLong(1, (Long)next.getId());
									
									BehaviorEvent parent = next.getParent();
									nullSafeSet(insert, 2, parent == null ? null : parent.getId(), Types.BIGINT);

									insert.setString(3, next.getApplication());
									insert.setString(4, next.getType());
									insert.setString(5, next.getName());
									insert.setTimestamp(6, new java.sql.Timestamp(next.getStart().getTime()));
									insert.setLong(7, next.getDuration());
	
									//set optional context information on the event.
									nullSafeSet(insert, 8, next.getUserId(), Types.VARCHAR);
									nullSafeSet(insert, 9, next.getSessionId(), Types.VARCHAR);
									nullSafeSet(insert, 10, next.getError(), Types.VARCHAR);
		
									//convert event data to XML
									String data = dataSerializer.serialize(next.getData(), true);
									nullSafeSet(insert, 11, data, Types.VARCHAR);
	
									if (doBatch) {
										insert.addBatch();
										if (++batchCount >= batchSize) {
											insert.executeBatch();
											batchCount = 0;
										}
									} else {
										insert.executeUpdate();
									}
	
									++tally;
								}
	
								//flush any lingering batch inserts through to the server.
								if (batchCount > 0)
									insert.executeBatch();
								
							} finally {
								closeStatement(idQuery);
							}
							
						} finally {
							closeStatement(insert);
						}
						
						return tally;
						
					} finally {
						if (bt != null)
							bt.resumeTracking();
					}
				}
				
			});
		}
		
		return count;
	}
	
	private void assignIds(BehaviorEvent event, PreparedStatement idQuery) throws SQLException {
		if (event.getId() != null)
			return;
		
		BehaviorEvent parent = event.getParent();
		if (parent != null && parent.getId() == null)
			assignIds(parent, idQuery);
		
		if (event.getId() == null)
			event.setId(nextId(idQuery));
	}
	
	protected Long nextId(PreparedStatement idQuery) throws SQLException {
		ResultSet rs = idQuery.executeQuery();
		try {
			rs.next();
			return rs.getLong(1);
		} finally {
			closeResultSet(rs);
		}
	}
	
	protected void nullSafeSet(PreparedStatement stmt, int index, Object value, int sqlType) 
		throws SQLException
	{
		if (value == null)
			stmt.setNull(index, sqlType);
		else
			stmt.setObject(index, value, sqlType);
	}
	
}
