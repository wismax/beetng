package com.mtgi.analytics.example.service;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.mtgi.analytics.example.model.HelloData;

/**
 * CRUD methods for our one persistent data type, {@link HelloData}.
 * Method calls on this class will be recorded by beet, according
 * to configuration found in <code>WEB-INF/beet-hello-servlet.xml</code>.
 */
public class HelloService {

	private static final String[] DICTIONARY = {
		"Lorem", "ipsum", "dolor", "sit", "amet", "consectetuer", "adipiscing", "elit",
		"Praesent", "dapibus", "eros", "eget", "massa", "Nulla", "rutrum", "dui", "et",
		"velit", "venenatis", "sodales", "Mauris", "tempor", "nisi", "a", "dui", "Sed",
		"euismod", "lectus", "eget", "dui", "Ut", "vitae", "massa", "et", "metus", "volutpat",
		"facilisis", "Vestibulum", "vitae", "mauris", "nec", "purus", "fringilla", "fermentum",
	};
	
	private HibernateTemplate dao;
	
	@Required
	public void setDao(HibernateTemplate dao) {
		this.dao = dao;
	}

	/** list all persistent instances of {@link HelloData}, sorted ascending by the given property */
	@SuppressWarnings("unchecked")
	public List<HelloData> list(String sort) {
		return (List<HelloData>)dao.find("from HelloData order by " + sort);
	}
	
	/** add a new test record to the database */
	public void save(HelloData data) {
		dao.saveOrUpdate(data);
	}
	
	/** delete a single test record */
	public void delete(HelloData data) {
		dao.delete(data);
	}

	/** delete all test data */
	public void clear() {
		dao.bulkUpdate("delete from HelloData");
	}
	
	/** generate and persist a random data set */
	public void generate() {
		Random rand = new Random();
		for (int i = 0; i < 10; ++i) {
			HelloData data = new HelloData();
			data.setAField(DICTIONARY[rand.nextInt(DICTIONARY.length)]);
			data.setAnotherField(DICTIONARY[rand.nextInt(DICTIONARY.length)]);
			dao.saveOrUpdate(data);
		}
	}

}
