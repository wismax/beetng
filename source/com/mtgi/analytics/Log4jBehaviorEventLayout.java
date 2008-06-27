package com.mtgi.analytics;

import java.io.StringWriter;
import java.text.SimpleDateFormat;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Formats instances of {@link BehaviorEvent} as XML messages.
 */
public class Log4jBehaviorEventLayout extends Layout {

	/** date output format that conforms with XSD dateTime formatting conventions */
	public static final SimpleDateFormat XS_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS(ZZZZZZ)");
	
	private StringWriter buffer;
	private XMLStreamWriter writer;
	private EventDataElementSerializer dataSerializer;
	
	public Log4jBehaviorEventLayout() throws XMLStreamException {
		buffer = new StringWriter();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		writer = factory.createXMLStreamWriter(buffer);
		dataSerializer = new EventDataElementSerializer(factory);
	}
	
	@Override
	public void activateOptions() {
	}

	@Override
	public String format(LoggingEvent event) {
		
		Object msg = event.getMessage();
		if (!(msg instanceof BehaviorEvent))
			return event.getRenderedMessage();
		
		BehaviorEvent evt = (BehaviorEvent)msg;
		try {
			writer.writeStartElement("event");
			writer.writeAttribute("id", evt.getId().toString());
			
			BehaviorEvent parent = evt.getParent();
			if (parent != null)
				writer.writeAttribute("parent-id", parent.getId().toString());
			
			writeEventAttribute("type", evt.getType());
			writeEventAttribute("name", evt.getName());
			writeEventAttribute("application", evt.getApplication());
			writeEventAttribute("start", XS_DATE_FORMAT.format(evt.getStart()));
			writeEventAttribute("duration-ms", evt.getDuration().toString());
			writeEventAttribute("user-id", evt.getUserId());
			writeEventAttribute("session-id", evt.getSessionId());
			writeEventAttribute("error", evt.getError());
			
			EventDataElement data = evt.getData();
			if (data != null)
				dataSerializer.serializeElement(writer, data);
			
			writer.writeEndElement();
			writer.writeCharacters("\n");
			writer.flush();
			
		} catch (Exception e) {
			LogLog.error("Error writing event " + evt.getId(), e);
		}

		//rotate the internal buffer, return results.
		String ret = buffer.toString();
		buffer.getBuffer().setLength(0);
		return ret;
	}

	@Override
	public boolean ignoresThrowable() {
		return false;
	}

	private final void writeEventAttribute(String name, String value) throws XMLStreamException {
		if (value != null) {
			writer.writeStartElement(name);
			writer.writeCharacters(value);
			writer.writeEndElement();
		}
	}
}
