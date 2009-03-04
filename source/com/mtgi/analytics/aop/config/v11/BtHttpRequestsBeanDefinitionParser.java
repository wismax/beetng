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

	private static final String ATT_EVENT_TYPE = "event-type";
	private static final String ATT_PARAMETERS = "parameters";
	private static final String ATT_URI_PATTERN = "uri-pattern";

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

		//manager ID from enclosing tag.
		String managerId = (String)parserContext.getContainingBeanDefinition().getAttribute("id");
		builder.addConstructorArgReference(managerId);

		//parameter list for logging, if any.
		String paramList = element.getAttribute(ATT_PARAMETERS);
		if (StringUtils.hasText(paramList))
			builder.addConstructorArg(paramList.split("[,;]+"));
		else
			builder.addConstructorArg(null);

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

		parserContext.getReaderContext().registerWithGeneratedName(builder.getBeanDefinition());
	}

	
	
}
