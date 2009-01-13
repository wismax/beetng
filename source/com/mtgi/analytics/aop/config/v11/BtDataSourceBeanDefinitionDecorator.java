package com.mtgi.analytics.aop.config.v11;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Node;

import com.mtgi.analytics.sql.BehaviorTrackingDataSource;

/** decorates the annotated datasource bean definition with behavior tracking */
public class BtDataSourceBeanDefinitionDecorator implements BeanDefinitionDecorator {

	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
		BeanDefinitionBuilder wrapper = BeanDefinitionBuilder.rootBeanDefinition(BehaviorTrackingDataSource.class);
		wrapper.addPropertyReference("trackingManager", node.getNodeValue());
		wrapper.addPropertyValue("targetDataSource", definition.getBeanDefinition());
		return new BeanDefinitionHolder(wrapper.getRawBeanDefinition(), definition.getBeanName());
	}

}
