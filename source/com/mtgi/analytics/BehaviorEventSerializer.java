package com.mtgi.analytics;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
		if (!data.isEmpty())
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
	
	/** SimpleDateFormat doesn't give us a time zone option that meets W3C standards, so we provide our own */
	private static class XmlDateFormat extends SimpleDateFormat {

		private static final long serialVersionUID = -5310271700921914349L;

		public XmlDateFormat() {
			super("yyyy-MM-dd'T'HH:mm:ss.SSS");
		}

		@Override
		public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
			StringBuffer ret = super.format(date, toAppendTo, pos);

			//append timezone as (+|-)hh:mm to meet xsd standard.
	        int value = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / 60000;

	        //append sign indicator and convert to absolute value.
	        if (value < 0) {
	        	ret.append('-');
	        	value *= -1;
	        } else {
	        	ret.append('+');
	        }
	        
	        appendTwoDigit(toAppendTo, value / 60);
	        toAppendTo.append(':');
	        appendTwoDigit(toAppendTo, value % 60);
	        
	        return ret;
		}

		private static final void appendTwoDigit(StringBuffer buf, int value) {
			buf.append(value / 10).append(value % 10);
		}
	}
}
