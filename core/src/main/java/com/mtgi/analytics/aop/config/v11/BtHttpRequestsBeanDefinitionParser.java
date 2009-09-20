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
 
package com.mtgi.analytics.aop.config.v11;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.mtgi.analytics.servlet.ServletRequestBehaviorTrackingAdapter;

/**
 * Parses bean definitions for {@link ServletRequestBehaviorTrackingAdapter} based on occurrances of
 * <code>bt:http-requests</code> nested in <code>bt:manager</code>.  These instances of {@link ServletRequestBehaviorTrackingAdapter}
 * are then invoked by the {@link ServletRequestBehaviorTrackingAdapter}, which will have automatically
 * registered itself within the calling web application.
 */
public class BtHttpRequestsBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final Pattern LIST_SEPARATOR = Pattern.compile("[,;]+");
	
	private static final String ATT_TRACKING_MANAGER = "tracking-manager";
	private static final String ATT_EVENT_TYPE = "event-type";
	private static final String ATT_PARAMETERS = "parameters";
	private static final String ATT_URI_PATTERN = "uri-pattern";
	private static final String ATT_NAME_PARAMETERS = "name-parameters";

	@Override
	protected Class<?> getBeanClass(Element element) {
		return ServletRequestBehaviorTrackingAdapter.class;
	}

	@Override
	protected boolean shouldGenerateId() {
		return true;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

		//compile required constructor arguments from attributes and nested elements.  first up, event type.
		builder.addConstructorArg(element.getAttribute(ATT_EVENT_TYPE));

		//manager ID from enclosing tag, or ref attribute.
		String managerId = parserContext.isNested() ? (String)parserContext.getContainingBeanDefinition().getAttribute("id")
													: element.getAttribute(ATT_TRACKING_MANAGER);
		builder.addConstructorArgReference(managerId);

		//parameter list to include in event data, if any.
		String paramList = element.getAttribute(ATT_PARAMETERS);
		builder.addConstructorArg(parseList(paramList));

		//parameter list to include in event name, if any
		String nameList = element.getAttribute(ATT_NAME_PARAMETERS);
		builder.addConstructorArg(parseList(nameList));

		//URI patterns, if any.  can be specified as attribute or nested elements.
		ArrayList<Pattern> accum = new ArrayList<Pattern>();
		if (element.hasAttribute(ATT_URI_PATTERN))
			accum.add(Pattern.compile(element.getAttribute(ATT_URI_PATTERN)));
		
		NodeList nl = element.getElementsByTagNameNS("*", ATT_URI_PATTERN);
		for (int i = 0; i < nl.getLength(); ++i) {
			Element e = (Element)nl.item(i);
			String pattern = e.getTextContent();
			if (StringUtils.hasText(pattern))
				accum.add(Pattern.compile(pattern));
		}
		
		if (accum.isEmpty())
			builder.addConstructorArg(null);
		else
			builder.addConstructorArg(accum.toArray(new Pattern[accum.size()]));

		if (parserContext.isNested())
			parserContext.getReaderContext().registerWithGeneratedName(builder.getBeanDefinition());
	}
	
	private static final String[] parseList(String paramList) {
		if (StringUtils.hasText(paramList))
			return LIST_SEPARATOR.split(paramList);
		return null;
	}
	
}
