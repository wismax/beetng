package com.mtgi.analytics.aop.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.mtgi.analytics.aop.BehaviorTrackingAdvice;

public class BtAdviceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected Class getBeanClass(Element element) {
		return BehaviorTrackingAdvice.class;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		// Set the transaction manager property.
		if (element.hasAttribute(BtNamespaceUtils.TRACKING_MANAGER_ATTRIBUTE))
			builder.addPropertyReference(BtNamespaceUtils.TRACKING_MANAGER_PROPERTY, element
					.getAttribute(BtNamespaceUtils.TRACKING_MANAGER_ATTRIBUTE));
		
		if (element.hasAttribute(BtNamespaceUtils.TRACKING_MANAGER_APPLICATION_ATTRIBUTE))
			builder.addPropertyValue(BtNamespaceUtils.TRACKING_MANAGER_APPLICATION_PROPERTY, element
					.getAttribute(BtNamespaceUtils.TRACKING_MANAGER_APPLICATION_ATTRIBUTE));

		//add post-processor to check manager configuration.
		BeanDefinitionBuilder processor = BeanDefinitionBuilder.rootBeanDefinition(BehaviorTrackingBeanFactoryPostPocessor.class);
		parserContext.getRegistry().registerBeanDefinition("behaviorTrackingProcessor", processor.getBeanDefinition());

		// else {
		// // Assume annotations source.
		// Class sourceClass =
		// TxNamespaceUtils.getAnnotationTransactionAttributeSourceClass();
		// builder.addPropertyValue(TxNamespaceUtils.TRANSACTION_ATTRIBUTE_SOURCE,
		// new RootBeanDefinition(sourceClass));
		// }
	}

}
