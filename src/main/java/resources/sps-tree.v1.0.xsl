<!-- To perform xhtml to xhtml or xml transformations the namespaces and attributes MUST be set correctly. See below. -->
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xpath-default-namespace= "http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="xhtml xsl xs">

  <xsl:output method="xml" encoding="utf-8" indent="no" omit-xml-declaration="yes"
	  doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" 
	  doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
  />

  <!-- Main body part -->
  <xsl:template match="/">
   <tree>
	<xsl:apply-templates/>
   </tree>
  </xsl:template>

  <!-- Extract the folder -->
  <xsl:template name="folder-tree" match="//div[@class='Folder']">
        <shelf>
        <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
        </xsl:attribute>
	<xsl:apply-templates select="*"/>
        </shelf>
  </xsl:template>

  <!-- Extract the document -->
  <xsl:template name="document-tree" match="//div[@class='Document']">
        <document>
        <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
        </xsl:attribute>
        </document>
  </xsl:template>

  <!-- Suppress all non-matching items -->
  <xsl:template match="text()"/>				

</xsl:stylesheet>
