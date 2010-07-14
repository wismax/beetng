/* 
 * Copyright 2008-2009 the original author or authors.
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
 
package com.mtgi.analytics;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.custommonkey.xmlunit.Diff;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.CompositeTable;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.StringDataType;
import org.dbunit.dataset.datatype.TypeCastException;
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
	protected String databaseStyle;
	
	//we keep a connection and statement open for general purpose verification during test runs
	protected Connection conn;
	protected Statement stmt;
	
	public JdbcEventTestCase() {
		this.databaseStyle = "sequence";
	}
	
	public JdbcEventTestCase(String databaseStyle) {
		this.databaseStyle = databaseStyle;
	}
	
	@Before
	public void createDB() throws IOException, SQLException {
		conn = ds.getConnection();
		stmt = conn.createStatement();
		runResourceScript("create/create_behavior_tracking-" + this.databaseStyle + ".sql");
	}

	@After
	public void dropDB() throws IOException, SQLException {
		try {
			runResourceScript("create/drop_behavior_tracking-" + this.databaseStyle + ".sql");
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
        actualTable = normalizedEventTable(actualTable);

		InputStream expectedData = getClass().getResourceAsStream(dataSetResource);
		FlatXmlDataSet expectedDataSet = new FlatXmlDataSet(expectedData, true);
		ITable expectedTable = expectedDataSet.getTable("BEHAVIOR_TRACKING_EVENT");
		expectedTable = normalizedEventTable(expectedTable);

		org.dbunit.Assertion.assertEquals(expectedTable, actualTable);
	}
	
	/** 
	 * transform the given dbunit table into one that can be used to meaningfully compare
	 * event data, without raising false negatives due to differences in start date or minor
	 * changes in XML syntax for the event_data column.
	 * @throws DataSetException 
	 */
	public static ITable normalizedEventTable(ITable table) throws DataSetException {
		ITableMetaData metaData = table.getTableMetaData();
        ArrayList<Column> columns = new ArrayList<Column>();
        for (Column c : metaData.getColumns()) {
        	String name = c.getColumnName();
        	//strip out time columns.
        	//TODO: leave in duration, with which we can compare with a tolerance value?
        	if ("EVENT_START".equals(name) || "DURATION_NS".equals(name))
        		continue;
        	//replace string data type comparator with an xmlunit-based comparator
        	if ("EVENT_DATA".equals(name))
        		c = new Column("EVENT_DATA", new XmlStringDataType(c.getSqlTypeName(), c.getDataType().getSqlType()));
        	columns.add(c);
        }
        return new CompositeTable(
    		new DefaultTableMetaData(metaData.getTableName(), 
    				columns.toArray(new Column[columns.size()]), 
    				metaData.getPrimaryKeys()), 
    		table
        );
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

	protected void dumpDBUnitFlatXmlDataSet(File file) throws DatabaseUnitException, FileNotFoundException, IOException {
        // partial database export
		IDatabaseConnection connection = new DatabaseConnection(conn);
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("BEHAVIOR_TRACKING_EVENT");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream(file));
	}

	public static class XmlStringDataType extends StringDataType {

		public XmlStringDataType(String name, int sqlType) {
			super(name, sqlType);
		}

		@Override
		public int compare(Object o1, Object o2) throws TypeCastException {
			String s1 = asString(o1);
			String s2 = asString(o2);
			
			if (s1 != null && s2 != null) {
				Diff diff;
				try {
					diff = new Diff(s1, s2);
				} catch (Exception e) {
					throw new TypeCastException("Error parsing XML data", e);
				}
				if (diff.identical())
					return 0;
			}
			return super.compare(o1, o2);
		}
		
	}
}
