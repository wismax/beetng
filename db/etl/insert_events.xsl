<?xml version="1.0"?>

<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="java:com.mtgi.analytics.xml.XSLLibrary"
	xmlns:fn="http://www.w3.org/2005/xpath-functions">

	<!-- passed in externally from BinaryToXSLT tool -->
	<xsl:param name="position">1</xsl:param>

	<!-- output a SQL insert statement for each event.  every 100th statement is followed by a 'COMMIT'. -->
	<xsl:template match="event">
INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_event_id, event_type,
	event_name, event_start, user_id, session_id,
	application, duration_ms, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'<xsl:value-of select="@id"/>',
	<xsl:value-of select="m:quoteSql(@parent-id)"/>,
	'<xsl:value-of select="type"/>',
	'<xsl:value-of select="name"/>',
	TO_DATE('<xsl:value-of select="fn:translate(fn:substring(start,1,19), 'T', ' ')"/>','YYYY-MM-DD HH24:MI:SS'),
	<xsl:value-of select="m:quoteSql(user-id/node())"/>,
	<xsl:value-of select="m:quoteSql(session-id/node())"/>,
	'<xsl:value-of select="application"/>',
	<xsl:value-of select="duration-ms"/>,
	<xsl:value-of select="m:quoteSql(event-data)"/>,
	<xsl:value-of select="m:quoteSql(error/node())"/>
);
<xsl:if test="($position mod 100) eq 0">COMMIT;
</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>