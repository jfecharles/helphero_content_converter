<!-- To perform xhtml to xhtml or xml transformations the namespaces and attributes MUST be set correctly. See below. -->
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns='http://www.w3.org/1999/xhtml'
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xpath-default-namespace= "http://www.w3.org/1999/xhtml"
        xmlns:uuid='java:java.util.UUID'
	exclude-result-prefixes="xhtml xsl xs">

  <!-- variables relevant to a specific document transformation -->
  <xsl:import href="hh.v1.0.vars.xsl" />

  <!--
  <xsl:output method="xml" encoding="utf-8" indent="no" standalone="no" 
	  doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" 
	  doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" />
  -->

  <xsl:output method="html" encoding="UTF-8" media-type="text/plain" indent="yes"/>

  <xsl:variable name="runTimeMillis"  select="( current-dateTime() -
                          xs:dateTime('1970-01-01T00:00:00') )
                        div xs:dayTimeDuration('PT1S') * 1000"/>

		<xsl:variable name="MetadataFile"  select="string-join(($orig_input_file,'meta.xml'),'.')" />

  <!-- Main body part -->
  <xsl:template match="/">
	<xsl:result-document href="{$MetadataFile}" >
	<documents>
	<xsl:apply-templates/>
        </documents>
	</xsl:result-document>
  </xsl:template>

  <!-- Remove unwanted elements -->
  <xsl:template name="header" match="//body/div[@class='header']" />
  <xsl:template name="docx4j_msgs" match="//body/div[@style='color:red']" />
  <xsl:template name="footer" match="//body/div[@class='footer']" />


  <!-- Extract the document -->
  <xsl:template name="document" match="//body/div[@class='document']/div[@class='Folder']//div[@class='Document']">
	<xsl:variable name="InternalId" select="string-join((string($runTimeMillis),generate-id(),@id),'_')"></xsl:variable>
	<xsl:variable name="docId" select="string-join((string($InternalId),$lang,upper-case($country)),'.')"></xsl:variable>
	<xsl:variable name="fileName" select="string-join(($docId,'json'),'.')"></xsl:variable>
	<document>
		<xsl:value-of select="string-join(($org,'documents',$fileName),'\')" />
	</document>
	<xsl:result-document href="{$org}\\documents\\{$fileName}">
<xsl:text>{</xsl:text>
		"Id" : "<xsl:value-of select="$docId"/>",
		"Type" : "Document",
		"Metadata": {
			"Published" : "true",
			"Hidden" : "false",
			"Version" : "1",
			"AuthorId" : "<xsl:value-of select="$author_id"/>",
			"Language" : "<xsl:value-of select="$lang"/>",
			"Country" : "<xsl:value-of select="upper-case($country)"/>",
			"Name" : "<xsl:value-of select="normalize-space(@title)"/>",
			"InternalId" : "<xsl:value-of select="$InternalId"/>",
			"OrigDocumentName" : "<xsl:value-of select="$orig_input_file"/>",
			"Org" : "<xsl:value-of select="$org"/>",
			"ReleaseDate" : "<xsl:value-of  select="format-dateTime(current-dateTime(), '[D01] [MNn] [Y0001]')"/>",
			"DocumentPath": "<xsl:call-template name="listancestors" />",
			"FolderPath": "",
			"Percentage" : "100",
		<xsl:choose>
			<xsl:when test="@target_type = 'external'">
			"MsgId" : "<xsl:value-of select="@msgid"/>",
			"DocumentType" : "<xsl:value-of select="@target_type"/>"
		},
				<xsl:variable name="exdataPath" select="replace(@src,'^.*_files[/\\](.*)$','/exdata/$1.b64')"></xsl:variable>
		"Body": {
			"relationships": {
				"data": {
					"links": {
						"self": "<xsl:value-of select="string-join(('/',$org,$exdataPath),'')"/>"
					}
				}
			}
		}
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="@target_type">
			"DocumentType" : "<xsl:value-of select="@target_type" />"
					</xsl:when>
					<xsl:otherwise>
			"DocumentType" : "procedure"
					</xsl:otherwise>
				</xsl:choose>
		},
		"Body": {
				<xsl:apply-templates select="*"/><xsl:text>,</xsl:text>
			"relationships": {
				"peers": {
					"links": {
						"self": "<xsl:value-of select="string-join(('/',$org,'/documents/',$fileName),'')" />"
					}
				},
				"ancestors": {
					"links": {
					}
				}
			}
		}
			</xsl:otherwise>
		</xsl:choose>
<xsl:text>}</xsl:text>
	</xsl:result-document>
  </xsl:template>

  <!-- Extract the folder path to the document -->
  <xsl:template name="listancestors">
	<xsl:variable name="test" select="./ancestor::div[@title]"></xsl:variable>

	<xsl:for-each select="$test">
		<xsl:if test="string-length(@title) > 0">
			<xsl:value-of select="@title"></xsl:value-of><xsl:text>.</xsl:text>
		</xsl:if>
	</xsl:for-each>
  </xsl:template>


  <!-- Extract the sections -->
  <xsl:template name="section" match="//body//div[@class='document']/div[@class='Folder']/div[@class='Document']/div[@class='Section']">
   	     <xsl:if test="number(position()) = 1">
						"Container" : [
		</xsl:if>
							{
								"Name" : "<xsl:value-of select="normalize-space(@title)"/>",
								"Id" : "<xsl:value-of select="@id"/>",
								"Type" : "Section",
		<xsl:if test="not(./div[@class='Task'])">
								"Element": [
		</xsl:if>
		<xsl:apply-templates select="*"/>
		<xsl:if test="not(./div[@class='Task'])">
								]
		</xsl:if>
		<xsl:if test="position() != last()">
							},
		</xsl:if>
		<xsl:if test="position() = last()">
							}
						]
		</xsl:if>
  </xsl:template>

  <!-- Extract the tasks -->
  <xsl:template name="task" match="//body//div[@class='document']/div[@class='Folder']/div[@class='Document']/div[@class='Section']/div[@class='Task']">
		<xsl:if test="number(position()) = 1">
								"Container" : [
		</xsl:if>
									{
										"Name" : "<xsl:value-of select="normalize-space(@title)"/>",
										"Id" : "<xsl:value-of select="@id"/>",
										"Type" : "Task",
										"Element": [
										<xsl:apply-templates select="*"/>
										]
		<xsl:if test="position() != last()">
									},
		</xsl:if>
					<xsl:if test="position() = last()">
									}
							]
					</xsl:if>
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
  <xsl:template name="topTable" match="//body/div[@class='document']/div[@class='Folder']/div[@class='Document']/div[@class='Section']//table">
		  								{
												"tag": "table",
												"Id" : "<xsl:value-of select="@id"/>",
        <xsl:for-each select="*">
		<xsl:if test="name() = 'colgroup'"> 
		<xsl:text>												"widths": "</xsl:text>
        <xsl:call-template name="joinTableWidths">
				<xsl:with-param name="list" select="col" />
                        <xsl:with-param name="separator" select="' '" />
        </xsl:call-template>
		<xsl:text>",</xsl:text>
        </xsl:if>
	    <xsl:if test="name() = 'tbody'">
			<xsl:call-template name="tablebody">
            	<xsl:with-param name="nestedTable" select="'false'" />
			</xsl:call-template>
        </xsl:if>
        </xsl:for-each>
		<xsl:if test="position() != last()">
											},
        </xsl:if>
		<xsl:if test="position() = last()">
											}
        </xsl:if>
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

												"Element" : [
        <xsl:for-each select="tr">
            <xsl:choose>
	        <xsl:when test="$nestedTable = 'false'">
												{
													"tag": "tr",
													"Element" : [
		        <xsl:call-template name="tablerow">
		           <xsl:with-param name="nestedTable" select="'false'" />
                </xsl:call-template>
													]
            	<xsl:if test="position() != last()">
												},
            	</xsl:if>
            	<xsl:if test="position() = last()">
												}
            	</xsl:if>
	        </xsl:when>
            <xsl:otherwise>
		    	<xsl:call-template name="tablerow">
		    		<xsl:with-param name="nestedTable" select="'true'" />
                </xsl:call-template>
	        </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
												]

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
														{
															"tag": "td",
															"Element": [
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
															]
            	<xsl:if test="position() != last()">
														},
            	</xsl:if>
            	<xsl:if test="position() = last()">
														}
            	</xsl:if>
		</xsl:if>

  </xsl:template>

  <!-- Template for handling paragraphs.
       In xhtml paragraphs are also containers for bulleted lists, numbered lists, headings and formatted text via multiple spans -->
  <xsl:template name="processParagraph" match="p">
    <xsl:param name="nestedTable"/>
    <xsl:param name="padding"/>
    <xsl:param name="called"/>

    <xsl:choose>
	    <xsl:when test="$called = 'true'">
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
										{
											"tag" : "ul",
											"Element" : [
												{
													"tag": "ul",
													"Element": [
														{
															"tag": "li",
															<xsl:text>"htmltext": "</xsl:text> 
		      	                </xsl:when>
		                        <xsl:otherwise>
										{
											"tag" : "ul",
											"Element" : [
												{
													"tag": "li",
													<xsl:text>"htmltext": "</xsl:text>
		      		        </xsl:otherwise>
		                    </xsl:choose>
		      	    </xsl:when>
		               <xsl:otherwise>
		                    <xsl:choose>
		                        <xsl:when test="$indentlevel >= 2">
										{
											"tag" : "ol",
											"Element" : [
												{
													"tag": "ol",
													"Element": [
														{
															"tag": "li",
															<xsl:text>"htmltext": "</xsl:text> 
		      	                </xsl:when>
		                        <xsl:otherwise>
										{
											"tag" : "ol",
											"Element" : [
												{
													"tag": "li",
													<xsl:text>"htmltext": "</xsl:text>
		      		        </xsl:otherwise>
		                    </xsl:choose>
		               </xsl:otherwise>
		          </xsl:choose>
		       </xsl:when>
		       <xsl:when test="starts-with(@class,'TableBullet')">
										{
											"tag" : "ol", 
											"Element" : [
												{
													"tag": "li",
													<xsl:text>"htmltext": "</xsl:text>
		       </xsl:when>
		       <xsl:when test="matches(@class,'^TableBullet[2-9].*$')">
										{
											"tag" : "ol",
											"Element" : [
												{
													"tag": "ol",
													"Element": [
														{
															"tag": "li",
															<xsl:text>"htmltext": "</xsl:text>
		       </xsl:when>
		       <xsl:when test="starts-with(@class,'Bullet1')">
								   		{
											"tag" : "ol",
											"Element" : [
												{
													"tag": "li",
													<xsl:text>"htmltext": "</xsl:text>
			       </xsl:when>
		       <xsl:when test="matches(@class,'^Bullet[2-9].*$')">
										{
											"tag" : "ol",
											"Element" : [
												{
													"tag": "ol",
													"Element": [
														{
															"tag": "li",
															<xsl:text>"htmltext": "</xsl:text>
		       </xsl:when>
		       <xsl:when test="matches(@class,'^Heading1.*$')">
										{
											"tag": "h1",
											<xsl:text>"htmltext": "</xsl:text>
		       </xsl:when>
		       <xsl:when test="matches(@class,'^Heading[2-9].*$')">
										{
											"tag": "h2",
											<xsl:text>"htmltext": "</xsl:text>
		       </xsl:when>
		       <xsl:when test="name(parent::*) = 'td'">
										{
											"tag": "p",
											<xsl:text>"htmltext": "</xsl:text>
		       </xsl:when>
		       <xsl:when test="name(parent::*) = 'th'">
										{
											"tag": "p",
											<xsl:text>"htmltext": "</xsl:text>
		       </xsl:when>
		       <xsl:otherwise>
										{
											"tag": "p",
											<xsl:text>"htmltext": "</xsl:text>
		       </xsl:otherwise>
		    </xsl:choose>
		
		    <xsl:if test="$use_nested_table_indenting = '1' and $nestedTable = 'true'">
			<xsl:value-of select="$padding"/>
		    </xsl:if>

		    <xsl:variable name="spantext" select="normalize-space(string-join(span,' '))"/>


		    <xsl:for-each select="span|a">
		        <xsl:if test="name() = 'span'">
		            <xsl:choose>
		            <xsl:when test="img">
		                <xsl:for-each select="img">
					<!-- Images are assumed to be idempotent -->
					<xsl:variable name="imgPath" select="replace(@src,'^.*_files/(.*)$','/images/$1.b64')"></xsl:variable>
					<xsl:variable name="imgId" select="replace(@src,'^.*_files/(.*)$','$1.b64')"></xsl:variable>
					[img width='<xsl:value-of select='@width'/>' height='<xsl:value-of select='@height'/>' id='<xsl:value-of select="$imgId"/>' src='<xsl:value-of select="string-join(('/',$org,$imgPath),'')"/>'][/img]
			        </xsl:for-each>
		            </xsl:when>
		            <xsl:when test="contains(@style,'font-weight: bold') and contains(@style,'font-style: italic') and contains(@style,'text-decoration: underline')">
<b><i><button><xsl:value-of select="normalize-space(.)"/></button></i></b>
		            </xsl:when>
		            <xsl:when test="contains(@style,'font-weight: bold') and contains(@style,'font-style: italic')">
<b><i><xsl:value-of select="normalize-space(.)"/></i></b> 
		            </xsl:when>
		            <xsl:when test="contains(@style,'font-weight: bold') and contains(@style,'text-decoration: underline')">
<b><button><xsl:value-of select="normalize-space(.)"/></button></b>
		            </xsl:when>
		            <xsl:when test="contains(@style,'font-style: italic') and contains(@style,'text-decoration: underline')">
<i><button><xsl:value-of select="normalize-space(.)"/></button></i>
		            </xsl:when>
		            <xsl:when test="contains(@style,'font-weight: bold')">
<b><xsl:value-of select="normalize-space(.)"/></b>
		            </xsl:when>
		            <xsl:when test="contains(@style,'font-style: italic')">
<i><xsl:value-of select="normalize-space(.)"/></i>
		            </xsl:when>
		            <xsl:when test="contains(@style,'text-decoration: underline')">
<button><xsl:value-of select="normalize-space(.)"/></button>
		            </xsl:when>
		            <xsl:otherwise>
<xsl:value-of select="replace(normalize-space(.),'\\','\\\\')"/>
		            </xsl:otherwise>
		            </xsl:choose>
		        </xsl:if>
		        <xsl:if test="name() = 'a'">
				<xsl:if test="string-length(@href) > 0">
 [link url='<xsl:value-of select='@href'/>' type='external']<xsl:value-of select="span"/>[/link]
				</xsl:if>
		        </xsl:if>

			<xsl:if test="position() != last()">
<xsl:text> </xsl:text>
			</xsl:if>
		    </xsl:for-each>
<xsl:text>"</xsl:text>

		
		    <xsl:choose>
		       <xsl:when test="starts-with(@class,'TableBullet')">
												}
											]
	            	<xsl:if test="position() = last()">
										}
									]
	            	</xsl:if>
		       </xsl:when>
		       <xsl:when test="matches(@class,'^TableBullet[2-9].*$')">
														}
													]
												}
											]
											
	            	<xsl:if test="position() = last()">
										}
									]
	            	</xsl:if>
		       </xsl:when>
		       <xsl:when test="matches(@class,'^Bullet1.*$')">
								   				}
											]
	            	<xsl:if test="position() = last()">
										}
									]
	            	</xsl:if>
		       </xsl:when>
		       <xsl:when test="matches(@class,'^Bullet[2-9].*$')">
														}
													]
												}
											]
	            	<xsl:if test="position() = last()">
										}
									]
	            	</xsl:if>
		       </xsl:when>
		       <xsl:otherwise>
	            <xsl:if test="position() != last()">
											},
	            </xsl:if>
	            <xsl:if test="position() = last()">
											}
	            </xsl:if>
		       </xsl:otherwise>
		    </xsl:choose>

	    </xsl:when> 
    </xsl:choose>

  </xsl:template>

  	<!--
	<xsl:template match="node/@TEXT | text()" name="removeBreaks">
	-->
	<xsl:template name="removeParagraphBreaks">
		<xsl:param name="pText" select="normalize-space(.)"/>
		<xsl:choose>
			<xsl:when test="not(contains($pText, '&#xA;'))"><xsl:copy-of select="$pText"/></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat(substring-before($pText, '&#xD;&#xA;'), ' ')"/>
				<xsl:call-template name="removeParagraphBreaks">
					<xsl:with-param name="pText" select="substring-after($pText, '&#xD;&#xA;')"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


    <!-- Dump out all unique images -->
	<!--
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
-->

  <!-- Suppress all non-matching items -->
  <xsl:template match="text()"/>				

</xsl:stylesheet>
