package com.mtgi.analytics.aop.config;

import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.mtgi.analytics.aop.BehaviorTrackingAdvice;

public class BtAdviceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String ATTRIBUTES = "attributes";

	protected Class getBeanClass(Element element) {
		return BehaviorTrackingAdvice.class;
	}

	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		// Set the transaction manager property.
		if (element.hasAttribute(BtNamespaceUtils.TRACKING_MANAGER_ATTRIBUTE))
			builder.addPropertyReference(BtNamespaceUtils.TRACKING_MANAGER_PROPERTY, element
					.getAttribute(BtNamespaceUtils.TRACKING_MANAGER_ATTRIBUTE));

		List btAttributes = DomUtils.getChildElementsByTagName(element, ATTRIBUTES);
		if (btAttributes.size() > 0) {
			parserContext.getReaderContext().error("Element <attributes> is not allowed inside element <advice>",
					element);
		}

		// else {
		// // Assume annotations source.
		// Class sourceClass =
		// TxNamespaceUtils.getAnnotationTransactionAttributeSourceClass();
		// builder.addPropertyValue(TxNamespaceUtils.TRANSACTION_ATTRIBUTE_SOURCE,
		// new RootBeanDefinition(sourceClass));
		// }
	}

}
