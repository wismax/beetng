package com.mtgi.analytics.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class AbstractSpringTestCase<T> implements TestCase {

	private static final long serialVersionUID = 3385396966475922871L;
	
	private Class<T> beanType;
	private String beanName;
	private String[] configFiles;
	
	protected transient ClassPathXmlApplicationContext context;
	protected transient T bean;

	protected AbstractSpringTestCase(String beanName, Class<T> beanType, String[] configFiles) {
		this.beanName = beanName;
		this.beanType = beanType;
		this.configFiles = configFiles;
	}
	
	public void setUp() throws Throwable {
		this.context = new ClassPathXmlApplicationContext(configFiles);
		this.bean = beanType.cast(context.getBean(beanName, beanType));
	}

	public void tearDown() throws Throwable {
		bean = null;
		context.destroy();
		context = null;
	}

}
