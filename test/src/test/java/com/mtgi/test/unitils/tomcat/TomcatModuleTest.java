package com.mtgi.test.unitils.tomcat;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.mtgi.test.unitils.tomcat.annotations.DeployDescriptor;
import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;

@RunWith(UnitilsJUnit4TestClassRunner.class)
public class TomcatModuleTest {

	@EmbeddedTomcat(start=true)
	private String baseUrl;
	
	@Test @DeployDescriptor(contextRoot="/app", webXml="web.xml")
	public void testUrlInjection() throws Exception {
		String now = String.valueOf(System.currentTimeMillis());

		WebClient client = new WebClient();
		Page page = client.getPage(baseUrl + "/app/echo?hello=" + now);
		assertEquals("embedded server base url is injected into test class", now, page.getWebResponse().getContentAsString());
	}

}
