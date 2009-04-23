package com.mtgi.analytics.example.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class HelloData {

	private Long id;
	private String aField;
	private String anotherField;
	
	@Id @GeneratedValue(generator="helloSeq")
	@SequenceGenerator(name="helloSeq", sequenceName="helloSeq")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAField() {
		return aField;
	}
	public void setAField(String field) {
		aField = field;
	}
	public String getAnotherField() {
		return anotherField;
	}
	public void setAnotherField(String anotherField) {
		this.anotherField = anotherField;
	}
	
}
