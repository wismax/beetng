<?xml version="1.0"?>

<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://www.mantis-tgi.com/bt/etl/1.0"
	xmlns:fn="http://www.w3.org/2005/xpath-functions">

	<!-- 
	parameter that identifies the position of the document fragment being processed relative to
	other document fragments that have been encountered in the stream.  Passed in from BinaryToXSLT.
	-->
	<xsl:param name="position">1</xsl:param>

	<!-- writes an escaped SQL string based on argument 'value', or null if value is null -->
	<xsl:function name="m:write-value">
		<xsl:param name="value"/>
		<xsl:value-of select="if ($value) 
			then fn:concat(&quot;'&quot;, 
						   fn:translate($value, &quot;'&quot;, &quot;''&quot;), 
						   &quot;'&quot;) 
			else 'null'"/>
	</xsl:function>	

	<!-- output a SQL insert statement for each event.  every 100th statement is followed by a 'COMMIT'. -->
	<xsl:template match="event">
INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'<xsl:value-of select="@id"/>',
	<xsl:value-of select="m:write-value(@parent-id)"/>,
	'<xsl:value-of select="type"/>',
	'<xsl:value-of select="name"/>',
	TO_DATE('<xsl:value-of select="fn:translate(fn:substring(start,1,19), 'T', ' ')"/>','YYYY-MM-DD HH24:MI:SS'),
	<xsl:value-of select="m:write-value(user-id)"/>,
	<xsl:value-of select="m:write-value(session-id)"/>,
	'<xsl:value-of select="application"/>',
	<xsl:value-of select="duration-ms"/>,
	<xsl:choose>
		<xsl:when test="event-data">'<xsl:copy-of select="event-data"/>'</xsl:when>
		<xsl:otherwise>null</xsl:otherwise>
	</xsl:choose>,
	<xsl:value-of select="m:write-value(error)"/>
);
<xsl:if test="($position mod 7) eq 0">COMMIT;
</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>