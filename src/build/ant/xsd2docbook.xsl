<?xml version="1.0"?>

<!DOCTYPE stylesheet [
	<!ENTITY cr "<xsl:text>
</xsl:text>">
]>

<!-- 
	Stylesheet to generate DocBook documentation from an XSD file.
	Functionality is fairly limited; the focus is on public and nested
	elements, and not so much on public or private types.
	
	This stylesheet requires an XSLT/Xpath 2.0 compatible processor.  Saxon
	is known to work well.
 -->
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema"
	xmlns:mtgi="http://www.mantis-tgi.com/xsl"
	exclude-result-prefixes="#all">

	<!-- the top-level docbook element of the output document, defaults to 'chapter'. -->
	<xsl:param name="rootElement">chapter</xsl:param>

	<!-- prefix appended to section titles; e.g. a value 'ns:' will result in section titles like 'ns:element' -->
	<xsl:param name="namespacePrefix"></xsl:param>

	<!-- prefix appended to section identifiers, for cross-referencing -->
	<xsl:param name="sectionPrefix">elt_</xsl:param>

	<!-- id attribute of root element -->
	<xsl:param name="rootElementId"></xsl:param>

	<!-- relative path to JavaDoc API documentation -->	
	<xsl:param name="apiBase">../../api/</xsl:param>

	<!-- we omit the XML declaration so that our output can easily be included as a fragment in another DocBook file. -->
	<xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

	<!-- set of all named elements in the schema, used for cross-referencing -->
	<xsl:variable name="schemaElements" select="//xsd:element[@name]"/>
		
	<!-- Generate a DocBook <section> for an element type in the XSD. 
	     Nested element types generate nested <sections>. -->
	<xsl:template match="xsd:element[xsd:complexType|@type]">

		<xsl:variable name="currentElement" select="current()"/>
		<xsl:variable name="name" select="@name"/>
		<xsl:variable name="sectionId" select="mtgi:section-id($name)"/>
		<xsl:variable name="sectionTitle" select="concat('&lt;', $namespacePrefix, $name, '&gt;')"/>
		<xsl:variable name="docNode" select="(.|xsd:complexType)/xsd:annotation/xsd:documentation"/>

		<!-- generate section element, with a global ID that we can use for cross-referencing from other elements -->
		<section>
			<xsl:attribute name="id"><xsl:value-of select="$sectionId"/></xsl:attribute>
			<title>
				<xsl:attribute name="id" select="concat($sectionId, '_title')"/>
				<xsl:value-of select="$sectionTitle"/>
			</title>
			
			<xsl:call-template name="output-documentation">
				<xsl:with-param name="element" select="$docNode"/>
			</xsl:call-template>
			
			<!-- generate attributes documentation table as a <segmentedlist> -->
			<xsl:if test="xsd:complexType/(.|(xsd:complexContent|xsd:simpleContent)/(xsd:extension|xsd:restriction))/xsd:attribute">
			<segmentedlist>
				<xsl:processing-instruction name="dbhtml">list-presentation="table"</xsl:processing-instruction>
				<title><xsl:value-of select="$sectionTitle"/> Attributes</title>

				<segtitle>Attribute</segtitle>
				<segtitle>Use</segtitle>
				<segtitle>Description</segtitle>
				
				<xsl:for-each select="xsd:complexType/(.|(xsd:complexContent|xsd:simpleContent)/(xsd:extension|xsd:restriction))/xsd:attribute">
					<seglistitem>
						<seg><xsl:value-of select="@name"/></seg>
						<seg>
							<xsl:choose>
								<xsl:when test="@use='required'">
									<emphasis role="bold">required</emphasis>
								</xsl:when>
								<xsl:otherwise>optional</xsl:otherwise>
							</xsl:choose>
						</seg>
						<seg>
							<xsl:if test="xsd:annotation/xsd:documentation">
							<xsl:call-template name="output-documentation">
								<xsl:with-param name="element" select="xsd:annotation/xsd:documentation"/>
							</xsl:call-template>
							</xsl:if>
						</seg>
					</seglistitem>
				</xsl:for-each>
			</segmentedlist>
			</xsl:if>
			
			<!-- generate child elements cross-reference table as a <variablelist> -->
			<xsl:if test="xsd:complexType//xsd:element">
				<variablelist>  
					<title><xsl:value-of select="$sectionTitle"/> Nested Elements</title>
				<xsl:for-each select="xsd:complexType//xsd:element except xsd:complexType//xsd:element//xsd:element">
					<xsl:variable name="lname" select="replace(@ref|@name, '\w+:', '')"/>
					<xsl:variable name="linkId" select="mtgi:section-id($lname)"/>
					<varlistentry>
						<term>
							<xsl:choose>
								<xsl:when test="//xsd:element[@name=$lname]">
									<xref>
										<xsl:attribute name="linkend">
											<xsl:value-of select="$linkId"/>
										</xsl:attribute>
										<xsl:attribute name="endterm">
											<xsl:value-of select="concat($linkId, '_title')"/>
										</xsl:attribute>
									</xref>
								</xsl:when>
								<xsl:otherwise><xsl:value-of select="@ref"/></xsl:otherwise>
							</xsl:choose>
						</term>
						<listitem>
							<xsl:call-template name="output-documentation">
								<xsl:with-param name="element" select="xsd:annotation/xsd:documentation"/>
							</xsl:call-template>
						</listitem>
					</varlistentry>
				</xsl:for-each>
				</variablelist>	
			</xsl:if>

	  		<xsl:apply-templates/>
	  		
		</section>
	</xsl:template>

	<!-- generate section for public attribute definitions -->
	<xsl:template match="xsd:schema/xsd:attribute[@name]">
		<xsl:variable name="currentElement" select="current()"/>
		<xsl:variable name="name" select="@name"/>
		<xsl:variable name="sectionId" select="mtgi:section-id($name)"/>
		<xsl:variable name="sectionTitle" select="concat('Attribute ', $namespacePrefix, $name)"/>

		<!-- generate section element, with a global ID that we can use for cross-referencing from other elements -->
		<section>
			<xsl:attribute name="id"><xsl:value-of select="$sectionId"/></xsl:attribute>
			<title>
				<xsl:attribute name="id" select="concat($sectionId, '_title')"/>
				<xsl:value-of select="$sectionTitle"/>
			</title>

			<xsl:call-template name="output-documentation">
				<xsl:with-param name="element" select="xsd:annotation/xsd:documentation"/>
			</xsl:call-template>
		</section>
		
	</xsl:template>

	<!-- generate document root and a useful header comment when we encounter the root element of the XSD. -->
	<xsl:template match="xsd:schema">
		<xsl:comment> 
	XML Schema Documentation auto-generated by xsd2docbook.xsl with parameters
	
		namespacePrefix = <xsl:value-of select="$namespacePrefix"/>
		rootElement     = <xsl:value-of select="$rootElement"/>
		rootElementId	= <xsl:value-of select="$rootElementId"/>
		sectionPrefix   = <xsl:value-of select="$sectionPrefix"/>&cr;
		
		</xsl:comment>&cr;

		<xsl:element name="{$rootElement}">
			<xsl:if test="$rootElementId">
			<xsl:attribute name="id" select="$rootElementId"/>
			</xsl:if>
			<title>XML Schema Documentation for <xsl:value-of select="@targetNamespace"/></title>

			<xsl:call-template name="output-documentation">
				<xsl:with-param name="element" select="xsd:annotation/xsd:documentation"/>
			</xsl:call-template>
			
	  		<xsl:apply-templates/>
		</xsl:element>
		
	</xsl:template>
	
	<!-- strip out any elements that we don't explicitly match -->
	<xsl:template match="@*|node()">
		<xsl:apply-templates/>
	</xsl:template>
	
	<!-- 
		transform a given flat text <xsd:documentation> element into DocBook-friendly <para></para> elements
	 	with embedded cross-references. 
	 -->
	<xsl:template name="output-documentation">
		<xsl:param name="element"/>
		<xsl:choose>
			<xsl:when test="$element">
				<!-- output a cross-reference to API doc if appropriate -->
				<xsl:call-template name="source-reference">
					<xsl:with-param name="uri" select="$element/@source"/>
				</xsl:call-template>

				<!-- interpret double spaces as paragraph boundaries. -->
				<xsl:for-each select="tokenize(string($element), '[\r\n]\s*[\r\n]')">
					<xsl:variable name="escaped" select="mtgi:escape(normalize-space())"/>
					<xsl:if test="$escaped != ''">
						<para><xsl:value-of select="mtgi:cross-reference($escaped, $schemaElements)" disable-output-escaping="yes"/></para>
					</xsl:if>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise><para></para></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- generate a link to the Java source associated with a schema element -->
	<xsl:template name="source-reference">
		<xsl:param name="uri"/>
		<xsl:if test="starts-with($uri, 'java:')">
			<emphasis role="bold">Java Bean Type:</emphasis>
			<ulink>
				<xsl:attribute name="url" select="concat($apiBase, translate(substring($uri, 6), '.', '/'), '.html')"/>
				<xsl:value-of select="substring($uri, 6)"/>
			</ulink>
		</xsl:if>
	</xsl:template>

	<!-- recursively replace textual references to other schema elements with links to the corresponding section of the doc -->
	<xsl:function name="mtgi:cross-reference">
		<xsl:param name="text"/>
		<xsl:param name="nodeSet"/>
		<xsl:choose>
			<xsl:when test="$nodeSet">
				<xsl:variable name="name" select="$nodeSet[1]/@name"/>
				<xsl:variable name="linkId" select="mtgi:section-id($name)"/>
				<xsl:variable name="linkedText" 
							  select="replace($text, 
											  concat('&amp;lt;', $namespacePrefix, $name, '&amp;gt;'), 
											  concat('&lt;xref linkend=&quot;', $linkId, '&quot; endterm=&quot;', $linkId, '_title&quot; /&gt;')
											  )"/>
				<xsl:value-of select="mtgi:cross-reference($linkedText, $nodeSet[position() > 1])"/>
			</xsl:when>
			<xsl:otherwise><xsl:value-of select="$text"/></xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	<!-- TODO: this is hideous.  we have to resort to manual escaping because some elements in the
	     plain text need to be escaped, and some transformed and inserted into the output document
	     via cross-reference.  a robust solution will probably demand much more complex XSL (e.g.
	     use one or more recursive templates to build the output fragment). -->
	<xsl:function name="mtgi:escape">
		<xsl:param name="text"/>
		<xsl:variable name="e0" select="replace($text, '&amp;', '&amp;amp;')"/>
		<xsl:variable name="e1" select="replace($e0, '&gt;', '&amp;gt;')"/>
		<xsl:value-of select="replace($e1, '&lt;', '&amp;lt;')"/>
	</xsl:function>
	
	<xsl:function name="mtgi:section-id">
		<xsl:param name="name"/>
		<xsl:value-of select="concat($sectionPrefix, replace($name, '\W+', '_'))"/>
	</xsl:function>
	
</xsl:stylesheet>
