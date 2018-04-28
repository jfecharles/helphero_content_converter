<!-- To perform xhtml to xhtml or xml transformations the namespaces and attributes MUST be set correctly. See below. -->
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xpath-default-namespace= "http://www.w3.org/1999/xhtml"
        xmlns:uuid="java:java.util.UUID"
	exclude-result-prefixes="xhtml xsl xs">

  <!-- variables relevant to a specific document transformation -->
  <xsl:import href="sps.v1.0.vars.xsl" />

  <xsl:output method="xml" encoding="utf-8" indent="no" standalone="no" 
	  doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" 
	  doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" />

  <!-- Main body part -->
  <xsl:template match="/">
	  <export type='{$top_partition_type}' doc_id="{$top_partition_id}">
   <database id="{$db_id}" name='a-team' version="{$db_version}" />
    <documents>
	<xsl:apply-templates/>
    </documents>	   
    <objects>
       <!-- Take the output of the tree generation xslt and insert into the output of this document -->
       <xsl:copy-of select="document('output.tree.xml')/tree"/>
       <xsl:call-template name="images"> 
           <xsl:with-param name="explicit-call" select="'true'" />
       </xsl:call-template>
    </objects>
    </export>					 
  </xsl:template>

  <!-- Extract the folder -->
  <xsl:template name="folder" match="//body/div/div[@class='Folder']">
        <document revision="0" lang_id="{$lang_id}" type="shelf">
        <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
        </xsl:attribute>
        <xsl:attribute name="group_id">
            <xsl:value-of select="@id"/>
        </xsl:attribute>
        <properties>
        <name>
            <xsl:value-of select="@title"/>
        </name>
        <release_date>
            <xsl:value-of  select="format-dateTime(current-dateTime(), '[D01] [MNn] [Y0001]')"/>
        </release_date>
        <complete>100</complete>
        <authorised>Authorised</authorised>
        </properties>
        <body>
        </body>
        </document>
	<xsl:apply-templates select="*"/>
  </xsl:template>

  <!-- Extract the document -->
  <xsl:template name="document" match="//body/div/div/div[@class='Document']">
        <document revision="0" lang_id="{$lang_id}" type="{$doc_type}">
        <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
        </xsl:attribute>
        <xsl:attribute name="group_id">
            <xsl:value-of select="@id"/>
        </xsl:attribute>
        <properties>
        <name>
            <xsl:value-of select="@title"/>
        </name>
        <release_date>
            <xsl:value-of  select="format-dateTime(current-dateTime(), '[D01] [MNn] [Y0001]')"/>
        </release_date>
        <complete>100</complete>
        <authorised>Authorised</authorised>
        </properties>
        <body>
	<xsl:apply-templates select="*"/>
        </body>
        </document>
  </xsl:template>

  <!-- Extract the section -->
  <xsl:template name="section" match="//body/div/div/div/div[@class='Section']">
	<section>
	<section_title>
            <xsl:value-of select="@title"/>
	</section_title>
	<xsl:apply-templates select="*"/>
        </section>
  </xsl:template>

  <xsl:template name="task" match="//body/div/div/div/div/div[@class='Task']">
	<task>
	<name>
            <xsl:value-of select="@title"/>
	</name>
	<number>
            <xsl:value-of select="@id"/>
	</number>
        </task>
	<xsl:apply-templates select="*"/>
  </xsl:template>

  <!-- Top level paragraphs -->
  <xsl:template name="topParagraph" match="//body/div[matches(@class,'document')]//p">
    <xsl:call-template name="processParagraph">
       <xsl:with-param name="nestedTable" select="'false'" />
       <xsl:with-param name="padding" select="$nested_table_padding_character" />
       <xsl:with-param name="called" select="'true'" />
    </xsl:call-template>
  </xsl:template>

  <!-- Top level tables -->
  <xsl:template name="topTable" match="table">
    <xsl:text>&lt;table</xsl:text>
        <xsl:for-each select="*">
	    <xsl:if test="name() = 'colgroup'">
		<xsl:text> widths="</xsl:text>
                <xsl:call-template name="joinTableWidths">
                        <xsl:with-param name="list" select="col" />
                        <xsl:with-param name="separator" select="' '" />
                </xsl:call-template>
		<xsl:text>"&gt;</xsl:text>
            </xsl:if>
	    <xsl:if test="name() = 'tbody'">
		<xsl:call-template name="tablebody">
                        <xsl:with-param name="nestedTable" select="'false'" />
		</xsl:call-template>
            </xsl:if>
        </xsl:for-each>
    <xsl:text>&lt;/table&gt;</xsl:text>
  </xsl:template>

  <!-- Extract the table width details from the xhtml table/colgroup/col style attribute and maps it into a list of column widths palatable for SupportPoint -->
  <xsl:template name="joinTableWidths">
        <xsl:param name="list" />
        <xsl:param name="separator"/>

        <xsl:for-each select="$list">
	    <xsl:variable name="w" select="number(replace(@style,'^.*width:[ ]+(\d*\.?\d+)[^\d]+$','$1'))"/>
            <xsl:choose>
		<xsl:when test="$w le 20">
		    <xsl:value-of select="'small'" />
		</xsl:when>
		<xsl:when test="$w gt 20 and $w lt 40">
		    <xsl:value-of select="'medium'" />
		</xsl:when>
		<xsl:otherwise>
		    <xsl:value-of select="'large'" />
		</xsl:otherwise>
	    </xsl:choose>
            <xsl:if test="position() != last()">
                <xsl:value-of select="$separator" />
            </xsl:if>
        </xsl:for-each>
  </xsl:template>

  <xsl:template name="join">
        <xsl:param name="list" />
        <xsl:param name="separator"/>

        <xsl:for-each select="$list">
            <xsl:value-of select="." />
                <xsl:value-of select="$separator" />
                <xsl:value-of select="position()" />
            <xsl:if test="position() != last()">
                <xsl:value-of select="$separator" />
            </xsl:if>
        </xsl:for-each>
  </xsl:template>

  <!-- The body of a table used by top level and nested tables. -->
  <xsl:template name="tablebody" match="tbody">
    <xsl:param name="nestedTable"/>

        <xsl:for-each select="tr">
            <xsl:choose>
	        <xsl:when test="$nestedTable = 'false'">
                    <tr>
		        <xsl:call-template name="tablerow">
		           <xsl:with-param name="nestedTable" select="'false'" />
                        </xsl:call-template>
                    </tr>
	        </xsl:when>
                <xsl:otherwise>
		    <xsl:call-template name="tablerow">
		       <xsl:with-param name="nestedTable" select="'true'" />
                    </xsl:call-template>
	        </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>

  </xsl:template>

  <!-- Table row template used by tables at any level. -->
  <xsl:template name="tablerow" match="tr">
    <xsl:param name="nestedTable"/>

    <xsl:for-each select="td|th">
		<xsl:call-template name="tablecell">
		    <xsl:with-param name="nestedTable" select="$nestedTable" />
	        </xsl:call-template>
    </xsl:for-each>

  </xsl:template>

  <!-- Process the contents of a table cell. This could be a paragraph or a nested table -->
  <xsl:template name="tablecell" match="td|th">
    <xsl:param name="nestedTable"/>

        <xsl:if test="$nestedTable = 'false'">
		<xsl:text>&lt;td&gt;</xsl:text>
	</xsl:if>

	<xsl:variable name="padding" select="string-join((for $i in 1 to position() return $nested_table_padding_character),'')"/>

        <xsl:for-each select="p|table">
	    <xsl:choose>
	        <xsl:when test="name() = 'table'">
    		    <xsl:apply-templates select="tbody"/>
		    <xsl:call-template name="tablebody">
                        <xsl:with-param name="nestedTable" select="'true'" />
		    </xsl:call-template>
	        </xsl:when>
	        <xsl:otherwise>
	            <xsl:call-template name="processParagraph">
                        <xsl:with-param name="nestedTable" select="$nestedTable" />
                        <xsl:with-param name="padding" select="$padding" />
                        <xsl:with-param name="called" select="'true'" />
		    </xsl:call-template>
	        </xsl:otherwise>
	    </xsl:choose>
        </xsl:for-each>

        <xsl:if test="$nestedTable = 'false'">
		<xsl:text>&lt;/td&gt;</xsl:text>
	</xsl:if>

  </xsl:template>

  <!-- Template for handling paragraphs.
       In xhtml paragraphs are also containers for bulleted lists, numbered lists, headings and formatted text via multiple spans -->
  <xsl:template name="processParagraph" match="p">
    <xsl:param name="nestedTable"/>
    <xsl:param name="padding"/>
    <xsl:param name="called"/>

    <xsl:if test="$called = 'true'">
    <!--
    <xsl:variable name="numListIndicator" select="string(number(replace(text(),'^(\d+).*$','$1')))"/>
    -->
    <xsl:variable name="numListIndicator" select="'NaN'"/>

    <xsl:choose>
       <xsl:when test="starts-with(@class,'ListParagraph')">
          <xsl:variable name="marginleft" select="number(replace(@style,'^.*margin-left:[ ]+(\d*\.?\d+)[^\d]+;.*$','$1'))"/>
          <xsl:variable name="textindent" select="number(replace(@style,'^.*text-indent:[ ]+[-]?(\d*\.?\d+)[^\d]+;','$1'))"/>
          <xsl:variable name="indentlevel" select="$marginleft div $textindent"/>
         
          <xsl:choose>
      	    <xsl:when test="string(number($numListIndicator)) != 'NaN'">
                    <xsl:choose>
                        <xsl:when test="$indentlevel >= 2">
                            <xsl:text>&lt;number_list level=&quot;2&quot;&gt;</xsl:text>
                            <xsl:text>&lt;item&gt;</xsl:text>
      	                </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>&lt;number_list&gt;</xsl:text>
                            <xsl:text>&lt;item&gt;</xsl:text>
      		        </xsl:otherwise>
                    </xsl:choose>
      	    </xsl:when>
               <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="$indentlevel >= 2">
                            <xsl:text>&lt;bullet_list level=&quot;2&quot;&gt;</xsl:text>
                            <xsl:text>&lt;item&gt;</xsl:text>
      	                </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>&lt;bullet_list&gt;</xsl:text>
                            <xsl:text>&lt;item&gt;</xsl:text>
      		        </xsl:otherwise>
                    </xsl:choose>
               </xsl:otherwise>
          </xsl:choose>
       </xsl:when>
       <xsl:when test="starts-with(@class,'TableBullet')">
           <xsl:text>&lt;bullet_list&gt;</xsl:text>
           <xsl:text>&lt;item&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="matches(@class,'^TableBullet[2-9].*$')">
           <xsl:text>&lt;bullet_list level=&quot;2&quot;&gt;</xsl:text>
           <xsl:text>&lt;item&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="starts-with(@class,'Bullet1')">
           <xsl:text>&lt;bullet_list&gt;</xsl:text>
           <xsl:text>&lt;item&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="matches(@class,'^Bullet[2-9].*$')">
           <xsl:text>&lt;bullet_list level=&quot;2&quot;&gt;</xsl:text>
           <xsl:text>&lt;item&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="matches(@class,'^Heading1.*$')">
           <xsl:text>&lt;heading&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="matches(@class,'^Heading[2-9].*$')">
           <xsl:text>&lt;heading level="2"&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="name(parent::*) = 'td'">
           <xsl:text>&lt;p&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="name(parent::*) = 'th'">
           <xsl:text>&lt;p&gt;&lt;b&gt;</xsl:text>
       </xsl:when>
       <xsl:otherwise>
           <xsl:text>&lt;p&gt;</xsl:text>
       </xsl:otherwise>
    </xsl:choose>

    <xsl:if test="$use_nested_table_indenting = '1' and $nestedTable = 'true'">
	<xsl:value-of select="$padding"/>
    </xsl:if>

    <xsl:for-each select="span|a">
        <xsl:if test="name() = 'span'">
            <xsl:choose>
            <xsl:when test="img">
                <xsl:for-each select="img">
                    <xsl:element name="image">
        	        <xsl:attribute name="id">
			    <xsl:value-of select="replace(@id,'^rId(\d+)$','$1')"/>
                        </xsl:attribute>
                    </xsl:element>
	        </xsl:for-each>
            </xsl:when>
            <xsl:when test="contains(@style,'font-weight: bold') and contains(@style,'font-style: italic') and contains(@style,'text-decoration: underline')">
    	          <b><i><u>
        	  <xsl:value-of select="replace(.,'[ ]+',' ')"/>
    	          </u></i></b>
            </xsl:when>
            <xsl:when test="contains(@style,'font-weight: bold') and contains(@style,'font-style: italic')">
    	          <b><i>
        	  <xsl:value-of select="replace(.,'[ ]+',' ')"/>
    	          </i></b>
            </xsl:when>
            <xsl:when test="contains(@style,'font-weight: bold') and contains(@style,'text-decoration: underline')">
    	          <b><u>
        	  <xsl:value-of select="replace(.,'[ ]+',' ')"/>
    	          </u></b>
            </xsl:when>
            <xsl:when test="contains(@style,'font-style: italic') and contains(@style,'text-decoration: underline')">
    	          <i><u>
        	  <xsl:value-of select="replace(.,'[ ]+',' ')"/>
    	          </u></i>
            </xsl:when>
            <xsl:when test="contains(@style,'font-weight: bold')">
        	  <b>
        	  <xsl:value-of select="replace(.,'[ ]+',' ')"/>
                  </b>
            </xsl:when>
            <xsl:when test="contains(@style,'font-style: italic')">
        	  <i>
        	  <xsl:value-of select="replace(.,'[ ]+',' ')"/>
                  </i>
            </xsl:when>
            <xsl:when test="contains(@style,'text-decoration: underline')">
        	  <u>
        	  <xsl:value-of select="replace(.,'[ ]+',' ')"/>
                  </u>
            </xsl:when>
            <xsl:otherwise>
        	  <xsl:value-of select="replace(.,'[ ]+',' ')"/>
            </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
        <xsl:if test="name() = 'a'">
		<xsl:element name="link">
		    <xsl:attribute name="type">external</xsl:attribute>
		    <xsl:attribute name="url">
			    <xsl:value-of select="@href"/>
		    </xsl:attribute>
		    <xsl:value-of select="span"/>
		</xsl:element>
        </xsl:if>
    </xsl:for-each>

    <xsl:choose>
       <xsl:when test="starts-with(@class,'ListParagraph')">
            <xsl:choose>
        	    <xsl:when test="string(number($numListIndicator)) != 'NaN'">
			    <xsl:text>&lt;/item&gt;</xsl:text>
        	        <xsl:text>&lt;/number_list&gt;</xsl:text>
        	    </xsl:when>
                <xsl:otherwise>
			<xsl:text>&lt;/item&gt;</xsl:text>
        	        <xsl:text>&lt;/bullet_list&gt;</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
       </xsl:when>
       <xsl:when test="starts-with(@class,'TableBullet')">
	       <xsl:text>&lt;/item&gt;</xsl:text>
	       <xsl:text>&lt;/bullet_list&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="matches(@class,'^TableBullet[2-9].*$')">
	       <xsl:text>&lt;/item&gt;</xsl:text>
	       <xsl:text>&lt;/bullet_list&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="matches(@class,'^Bullet[1-9].*$')">
	       <xsl:text>&lt;/item&gt;</xsl:text>
	       <xsl:text>&lt;/bullet_list&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="matches(@class,'^Heading[1-9].*$')">
	       <xsl:text>&lt;/heading&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="name(parent::*) = 'td'">
	       <xsl:text>&lt;/p&gt;</xsl:text>
       </xsl:when>
       <xsl:when test="name(parent::*) = 'th'">
	       <xsl:text>&lt;/b&gt;&lt;/p&gt;</xsl:text>
       </xsl:when>
       <xsl:otherwise>
	    <xsl:text>&lt;/p&gt;</xsl:text>
       </xsl:otherwise>
    </xsl:choose>

    </xsl:if>

  </xsl:template>

    <!-- Dump out all folder tree -->
    <!--
  <xsl:template name="folder-tree" match="//body/div/div[@class='Folder']" priority="2">
      <xsl:param name="explicit-call" select = "'false'"/>

      <xsl:if test="$explicit-call = 'true'">
      	<tree>
                <xsl:element name="shelf">
        	    <xsl:attribute name="id">
                        <xsl:value-of select="@id"/>
        	    </xsl:attribute>
                    <xsl:for-each select="*">
                        <xsl:element name="document">
        	            <xsl:attribute name="id">
                                <xsl:value-of select="@id"/>
        	            </xsl:attribute>
	                </xsl:element>
                    </xsl:for-each>
	        </xsl:element>
	</tree>
      </xsl:if>

  </xsl:template>
  -->

    <!-- Dump out all unique images -->
  <xsl:template name="images" match="//img">
      <xsl:param name="explicit-call" select = "'false'"/>

      <xsl:if test="$explicit-call = 'true'">
          <xsl:for-each-group select="//img" group-by="@id">
              <xsl:element name="image">
        	      <xsl:attribute name="id">
                      <xsl:value-of select="replace(@id,'^rId(\d+)$','$1')"/>
        	      </xsl:attribute>
                  <xsl:attribute name="src">
                      <xsl:value-of select="@src"/>
                  </xsl:attribute>
        	      <xsl:attribute name="name">
        		  <xsl:value-of select="replace(@src,'^.*/([^/.]+).?.*$','$1')"/>
                  </xsl:attribute>
              </xsl:element>
          </xsl:for-each-group>
      </xsl:if>
  </xsl:template>

  <!-- Suppress all non-matching items -->
  <xsl:template match="text()"/>				

</xsl:stylesheet>

