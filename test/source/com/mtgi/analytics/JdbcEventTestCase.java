package com.mtgi.analytics;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

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
	
	
	protected void runResourceScript(String resource) 
		throws IOException, SQLException 
	{
		URL location = Thread.currentThread().getContextClassLoader().getResource(resource);
		if (location == null)
			fail("Unable to find script " + resource);
	
		InputStream is = location.openStream();
		String sql = IOUtils.toString(is);
		is.close();

		//normalize oracle specific type to generic SQL92
		sql = sql.replaceAll("VARCHAR2", "VARCHAR");
		stmt.execute(sql);
	}
}
