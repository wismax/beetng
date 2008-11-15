package com.mtgi.analytics;

import static org.dbunit.dataset.filter.DefaultColumnFilter.excludedColumnsTable;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;
import org.unitils.thirdparty.org.apache.commons.io.IOUtils;

@SpringApplicationContext("applicationContext-test.xml")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public abstract class JdbcEventTestCase {

	@SpringBeanByType
	protected DataSource ds;
	
	//we keep a connection and statement open for general purpose verification during test runs
	protected Connection conn;
	protected Statement stmt;
	
	@Before
	public void createDB() throws IOException, SQLException {
		conn = ds.getConnection();
		stmt = conn.createStatement();
		runResourceScript("create/create_behavior_tracking.sql");
	}

	@After
	public void dropDB() throws IOException, SQLException {
		try {
			runResourceScript("create/drop_behavior_tracking.sql");
		} finally {
			stmt.close();
			conn.close();
		}
	}

	/**
	 * Verify that the contents of the test event database match the dataset in the given resource XML file.
	 * The comparison excludes the START and DURATION_MS columns, since these will change with each test run.
	 */
	public void assertEventDataMatches(String dataSetResource) throws SQLException, DataSetException, IOException, DatabaseUnitException  {
		IDatabaseConnection connection = new DatabaseConnection(conn);
        ITable actualTable = connection.createDataSet().getTable("BEHAVIOR_TRACKING_EVENT");
        actualTable = excludedColumnsTable(actualTable, new String[]{"EVENT_START", "DURATION_MS"});

		InputStream expectedData = getClass().getResourceAsStream(dataSetResource);
		FlatXmlDataSet expectedDataSet = new FlatXmlDataSet(expectedData, true);
		ITable expectedTable = expectedDataSet.getTable("BEHAVIOR_TRACKING_EVENT");
		
		expectedTable = excludedColumnsTable(expectedTable, new String[]{"EVENT_START", "DURATION_MS"});

		org.dbunit.Assertion.assertEquals(expectedTable, actualTable);
		
	}
	
	protected void runResourceScript(String resource) 
		throws IOException, SQLException 
	{
		URL location = Thread.currentThread().getContextClassLoader().getResource(resource);
		if (location == null)
			fail("Unable to find script " + resource);
	
		InputStream is = location.openStream();
		String sql = IOUtils.toString(is);
		is.close();

		//convert oracle types to hsql types
		sql = sql.replaceAll("VARCHAR2", "VARCHAR");
		sql = sql.replaceAll("NUMBER", "NUMERIC");
		sql = sql.replaceAll("CLOB", "LONGVARCHAR");
		stmt.execute(sql);
	}
}
