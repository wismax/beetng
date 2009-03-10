package com.mtgi.analytics;

import java.text.SimpleDateFormat;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.mtgi.xml.XmlDateFormat;

public class BehaviorEventSerializer {

	private static final EventDataElementSerializer dataSerializer = new EventDataElementSerializer(null);
	
	/** date output format that conforms with XSD dateTime formatting conventions */
	private SimpleDateFormat XS_DATE_FORMAT = new XmlDateFormat();
	
	public void serialize(XMLStreamWriter writer, BehaviorEvent event) throws XMLStreamException {
		writer.writeStartElement("event");
		writer.writeAttribute("id", event.getId().toString());
		
		BehaviorEvent parent = event.getParent();
		if (parent != null)
			writer.writeAttribute("parent-id", parent.getId().toString());
		
		writeEventAttribute(writer, "type", event.getType());
		writeEventAttribute(writer, "name", event.getName());
		writeEventAttribute(writer, "application", event.getApplication());
		writeEventAttribute(writer, "start", XS_DATE_FORMAT.format(event.getStart()));
		writeEventAttribute(writer, "duration-ms", event.getDuration().toString());
		writeEventAttribute(writer, "user-id", event.getUserId());
		writeEventAttribute(writer, "session-id", event.getSessionId());
		writeEventAttribute(writer, "error", event.getError());
		
		EventDataElement data = event.getData();
		dataSerializer.serializeElement(writer, data);
		
		writer.writeEndElement();
	}
	
	private static final void writeEventAttribute(XMLStreamWriter writer, String name, String value) throws XMLStreamException {
		if (value != null) {
			writer.writeStartElement(name);
			writer.writeCharacters(value);
			writer.writeEndElement();
		}
	}
}
