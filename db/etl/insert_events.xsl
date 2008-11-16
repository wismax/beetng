<?xml version="1.0"?>

<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="java:com.mtgi.analytics.xml.XSLLibrary"
	xmlns:fn="http://www.w3.org/2005/xpath-functions">

	<!-- passed in externally from BinaryToXSLT tool -->
	<xsl:param name="position">-1</xsl:param>
	
	<!-- output a sqlldr record for each event.  the first record is preceded by a prologue that
		specifies the record format. -->
	<xsl:template match="event">
<xsl:value-of select="@id"/>,<xsl:value-of select="@parent-id"/>,<xsl:value-of select="m:quoteCsv(type/node())"/>,<xsl:value-of select="m:quoteCsv(name/node())"/>,<xsl:value-of select="fn:translate(fn:substring(start,1,19), 'T', ' ')"/>,<xsl:value-of select="m:quoteCsv(user-id/node())"/>,<xsl:value-of select="m:quoteCsv(session-id/node())"/>,<xsl:value-of select="m:quoteCsv(application/node())"/>,<xsl:value-of select="duration-ms"/>,<xsl:value-of select="m:quoteCsv(event-data)"/>,<xsl:value-of select="m:quoteCsv(error/node())"/>,
</xsl:template>

</xsl:stylesheet>