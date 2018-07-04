# helphero_content_converter
Rules based HelpHero Content Converter
# Help Hero Converter
### What is it?
An open-source tool used to convert content from Office formats to alternate formats typically XML or JSON. The converter leverages open source libraries to firstly transform Office documents to a common object format namely xHTML.  It then applies a set of transformation rules to filter and re-structure the content into business filtered xHTML. Lastly the content in then converted to a target format leveraging high-performance XSLT transforms. 

The result of each stage of conversion are viewable in any browser. This is useful for adding, removing or enhancing the transformation rules. Once a rules set has been defined it can then be applied to all other documents that have a high degree of conformity to the styling defined in the initial document.

The converter has been designed to descend through a directory structure and convert all files into target formats. It leverages thread pooling to provide high performance conversion of files in parallel. The number of parallel conversions is defined by the threads argument.

### What is the purpose of the converter
Many organisations already have useful authoritative content that can empower their communities. Unfortunately desktop publishing formats are not suitable for searching and targeted viewing on mobile devices. People want to be able to pin-point what they need from your content without reading all the content. Everything needs to be searchable, easily readable and rapidly digested.

Unfortunately most Content / Knowledge Management Systems require organisations to rewrite their content in formats that align with the target system. Very few organisation have the resources nor interest in re-writing their content.

The Help Hero Content Converter is an open-source initiative designed to alleviate this problem.

In addition to transforming Office documents into proprietary formats suitable for Content / Knowledge Management Systems, the converter can also be used to directly upload converted content into Search engines in due course.

### Broader Objectives of Help Hero
At Help Hero we want to assist organisations build useful helper information eco-systems that empower their communities.

We cannot do it all on our own so we would love it if other like-minded professionals worked with us to develop tooling and capabilities.

In addition the content converter needs to be tested against a broader range of Office document types. We would love it other people could help us test and get it working for other content types.

### Want to contribute?

Great! It will give participants with an opportunity to provide information architecting and consulting services to organisations and communities using tooling you have helped to create.

### Tech

The Help Hero Converter uses a number of open source projects to work properly:

* [docx4j] - Office Documents to XHtml conversion library!
* [commons-cli] - Apache Commons Command Line Interpreter
* [slf4j] - Simple logging facade for Java
* [apache-xmlgraphics] - Apache library for the conversion of XML formats to graphical output
* [xalan] - XSLT Transformer
* [commons-codec] - Apache commons encoders and decoders providing base64 encoding 

And of course Help Her itself is open source with a [public repository][helphero]
 on GitHub.

### What is on the Roadmap
#### Converter
- Automatically create overview documents containing links to all the sub-documents.
- Adding connectors to the converter to directly upload to target systems including:
-- Content and Knowledge Management Systems
-- Search Engines. At this stage we have only been focussed on Elastic Search but are open to other technologies.
- Add machine learning to the conversion process to provide intelligent meta-tagging to content. This can then be leveraged by Search technologies to improve search accuracy

#### Mobile Clients
- Release a React Native Client linked to a Search engine for viewing and consuming community helper content
- Adding a registration capability so end-users can register into Help Hero and consume content
- Adding oAuth2 authentication to the client so users have authenticated access using their preferred public identity provider
- Add chat-bot capability to allow question / answer styled interactions with authoritative content

#### Event Driven Content Dispatcher
- Build and release a CQRS based Event Driven Content Synchronizer
- Extend the Converter to publish to the Dispatcher and then the dispatcher will dispatch content to the target repository or repositories. The CQRS Event Source synchronizer decouples will content to be written in parallel to multiple repositories.

## Converter Details

#### Maven Depedencies
<groupId>com.helphero.util</groupId>
<artifactId>hhc</artifactId>
<version>2.0.0-3</version>
<packaging>jar</packaging>

### How to Build the Converter?
1.	Clone the source code from github
2.	Add as a project into Eclipse
3.	Clean and build

#### What gets built?
In the project target folder 2 items will be created after a build
1.	hhc-version-distribution.zip
a.	Contains all the libraries used by Help Hero
b.	Resources
2.	hhc-version.jar
a.	The Help Hero Converter jar file

#### Running the Converter
1.	Create a working directory i.e. c:\tools\helphero
2.	Copy the hhc-*version*-distribution.zip to this folder
3.	Copy the hhc-*version*.jar file to this folder
4.	Create batch script to run the converter over your files
```
java -Xmx4096m -Xms1024m -d64 
-jar hhc-2.0.0-3.jar com.helphero.util.hhc.HelpHeroConverter 
-threads 4  
-org {yourOrganisation} 
-targettype hh 
-targetversion 1 
-spsxsl " c:\tools\helphero\resources\hh.v3.0.xsl"
-treexsl " c:\tools\helphero\resources\sps-tree.v1.0.xsl" 
-log4jprops " c:\tools\helphero\resources\log4j.properties"
-r "{pathToYourFiles}\rules.xml" 
-log_file "{pathToYourFiles}\hhc.log"
-w "*.docx" 
 "{pathToYourFiles}"
 ```
### Results
In the case of the example above, a series of json documents residing in the *{pathToYourFiles}*\documents folder.

The JSON objects can be directly uploaded to json based search engines allowing content to be searched and retrieved. In due course tools to connect the generated documents to the target repository will be developed.

### Content Converter Command Line Arguments
 Argument | Description
 -------- | -----------
 threads  | The number of parallel threads for converting documents
 org      | The name of your organisation
 targettype | Target document object model type
 targetversion | Target DOM version
 spsxsl | xsl transform file used to convert to the target format
 treexsl | xsl transform to manage tree structures within a target format
 log4jprops | Log4j properties files
 r | Transformation rules file
 w | Type of Office document (\*.docx, \*.pptx, \*.xlsx)
 
### Sample Rules xml Files
##### Example Rules File 1
The first rules file shown below partitions a Word document into 4 partition levels and then removes unnecessary content.
```
<?xml version="1.0" encoding="utf-8"?>
<!--  This is a sample rules file providing basic rules partitioning -->
<rules document_root="//body/div[starts-with(@class,'document')]">
	<rule description="Folder Partitioning" type="partition" target="folder">
		<tasks>
			<task type="src_path" src="//div/p"><match type="expr" expr="starts_with" attr="class" attr_value="Title"/></task>
			<task type="partition_options" keep_intro_nodes="no" intro_partition_title="Preliminaries"/>
		</tasks>
	</rule>
	<rule description="Document Partitioning" type="partition" target="document">
		<tasks>
			<task type="src_path" src="//div/div/p"><match type="expr" expr="starts_with" attr="class" attr_value="Heading1"/></task>
			<task type="partition_options" keep_intro_nodes="yes" intro_partition_title="Preliminaries"/>
		</tasks>
	</rule>
	<rule description="Section Partitioning" type="partition" target="section">
		<tasks>
			<task type="src_path" src="//div/div/p"><match type="expr" expr="starts_with" attr="class" attr_value="Heading2"/></task>
			<task type="partition_options" keep_intro_nodes="yes" intro_partition_title="Preliminaries"/>
		</tasks>
	</rule>
	<rule description="Task Partitioning" type="partition" target="task">
		<tasks>
			<task type="src_path" src="//div/div/p"><match type="expr" expr="starts_with" attr="class" attr_value="Heading3"/></task>
			<task type="partition_options" keep_intro_nodes="yes" intro_partition_title="Preliminaries"/>
		</tasks>
	</rule>
	<!-- Delete the Table of content and Version control register documents -->
	<rule disable="false" type="delete" target="document" description="Remove the Table of contents and the Version Control register">
		<tasks>
			<!-- Path to all non-procedure documents -->
			<task type="target_path" src="//div/div[@class='Document' and contains(@title,'Table of contents') or contains(@title,'Version control register')]"></task>
		</tasks>
	</rule>
	<!-- Delete all empty paragraphs -->
	<rule disable="false" type="delete" target="element" description="Remove empty paragraphs">
		<tasks>
			<!-- Path to all non-procedure documents -->
			<task type="target_path" src="//p[starts-with(@class,'Normal') and text()='Ã‚ ' ]"></task>
		</tasks>
	</rule>
	<!-- Delete all footer paragraphs -->
	<rule disable="false" type="delete" target="element" description="Remove all footer paragraphs">
		<tasks>
			<!-- Path to all non-procedure documents -->
			<task type="target_path" src="//p[starts-with(@class,'Normal') and starts-with(text(),'ANZCA Handbook for Training and Accreditation 20170421 v1.5')]"></task>
		</tasks>
	</rule>
	<!-- Delete all ListParagraph Styles -->
	<rule disable="false" type="delete" target="element" description="Remove all ListParagraph style paragraphs">
		<tasks>
			<!-- Path to all ListParagraph Style elements -->
			<task type="target_path" src="//p[starts-with(@class,'ListParagraph Normal DocDefaults')]"></task>
		</tasks>
	</rule>

</rules>
```

##### Example Rules File 2
The second example is more complex. It first partitions the Office documents into 4 partition levels and then performs a sequence of rules. Rules include directives to create, move, copy and delete one or more nodes. Move and copy directives require tasks to define the source node or nodes and the target node or nodes. Nodes are identified using XPath notation. You can use free online xpath editors to define the source and target nodes and build your rule sets inserted into your rules.xml file. The rules xml file will be used to convert all files of the same type in your source directory. The converter will descend through all child directories and convert all child directory documents.
```
<?xml version="1.0" encoding="utf-8"?>
<rules document_root="//body/div[starts-with(@class,'document')]" debug="false">
	<rule type="partition" target="folder" description="Folder Partitioning">
		<tasks>
			<task type="src_path" src="//div/p"><match type="expr" expr="starts_with" attr="class" attr_value="OriginDocumentType"/></task>
			<task type="partition_options" keep_intro_nodes="no" intro_partition_title="Subfolder"/>
		</tasks>
	</rule>
	<rule type="partition" target="document" target_type="policy" description="Document Partitioning">
		<tasks>
			<task type="src_path" src="//div/p"><match type="expr" expr="starts_with" attr="class" attr_value="Title"/></task>
			<task type="partition_options" keep_intro_nodes="yes" intro_partition_title="Document Preliminaries"/>
		</tasks>
	</rule>
	<rule type="partition" target="section" description="Section Partitioning">
		<tasks>
			<task type="src_path" src="//div/div/p"><match type="expr" expr="starts_with" attr="class" attr_value="ProcedureHeading"/></task>
			<task type="partition_options" keep_intro_nodes="yes" intro_partition_title="Section Preliminaries"/>
		</tasks>
	</rule>
	<rule type="partition" target="task" description="Task Partitioning">
		<tasks>
			<task type="src_path" src="//div/div/div/p"><match type="expr" expr="starts_with" attr="class" attr_value="Heading3"/></task>
			<task type="partition_options" keep_intro_nodes="yes" intro_partition_title="Task Preliminaries"/>
		</tasks>
	</rule>
	<rule type="create" sub_type="move_images" target="document" target_type="process" description="Add Images to Sample Graphics Document">
		<tasks>
			<!-- Find the last child document in the last folder -->
			<task type="src_path" src="(//div/div[starts-with(@class,'Folder')])[last()]"></task>
			<!--  Append a child document node to the last child of the last folder node -->
			<task type="append_child" target_title="Sample Graphics"/>
		</tasks>
	</rule>	
	<!-- Copy a list of nodes to the end of a document section -->
	<rule disable="true"  type="copy" target="section" description="Copy and append all paragraph nodes from document id=3 section id=1 whose text length greater than 2 characters">
		<tasks>
			<!-- Find a range of nodes in document 3 section 1 whose text content is gt 2 characters -->
			<task type="src_path" src="//div/div[@class='Document' and @id='3']/div[@class='Section' and @id='1']/p[starts-with(@class,'CoverText')]"></task>
			<task type="target_path" src="//div/div/div[@class='Document' and @id='2']/div[@class='Section' and @id='1']"/>
			<task type="append_child"/>
		</tasks>
	</rule>
	<!-- Copy a list of nodes and insert before a paragraph with previous next sibling.-->
	<rule disable="true"  type="copy" target="section" description="Copy and append all paragraph nodes from document id=3 section id=1 whose text length greater than 2 characters">
		<tasks>
			<task type="src_path" src="//div/div[@class='Document' and @id='3']/div[@class='Section' and @id='1']/p[starts-with(@class,'CoverText')]"></task>
			<task type="target_path" src="//div/div/div[@class='Document' and @id='2']/div/p[starts-with(@class,'TableBody')]"/>
			<task type="insert_before"/>
		</tasks>
	</rule>
	<!-- Copy a list of nodes and insert after a paragraph with no next sibling. -->
	<rule disable="true"  type="copy" target="section" description="Copy and append all paragraph nodes from document id=3 section id=1 whose text length greater than 2 characters">
		<tasks>
			<task type="src_path" src="//div/div[@class='Document' and @id='3']/div[@class='Section' and @id='1']/p[starts-with(@class,'CoverText')]"></task>
			<task type="target_path" src="//div/div/div[@class='Document' and @id='2']/div/p[starts-with(@class,'TableBody')]"/>
			<task type="insert_after"/>
		</tasks>
	</rule>
	<!-- Copy a list of nodes and insert after a paragraph with a next sibling. -->
	<rule disable="true"  type="copy" target="section" description="Copy and append all paragraph nodes from document id=3 section id=1 whose text length greater than 2 characters">
		<tasks>
			<task type="src_path" src="//div/div[@class='Document' and @id='3']/div[@class='Section' and @id='1']/p[starts-with(@class,'CoverText')]"></task>
			<task type="target_path" src="//div/div/div[@class='Document' and @id='2']/div/p[starts-with(@class,'ProcedureAction')][position() = 1]"/>
			<task type="insert_after"/>
		</tasks>
	</rule>
	<!-- Copy the "Sample Graphics" document and insert after the "Document Preliminaries" document and "Section Preliminaries" section. -->
	<rule disable="true"  type="copy" target="section" description="Copy and append all paragraph nodes from document id=3 section id=1 whose text length greater than 2 characters">
		<tasks>
			<task type="src_path" src="//div/div[@class='Document' and @id='3']/div[@class='Section' and @id='1']/p[starts-with(@class,'CoverText')]"></task>
			<task type="target_path" src="//div/div/div[@class='Document' and @id='2']/div[@class='Section' and @id='1']"/>
			<task type="append_child"/>
		</tasks>
	</rule>
	<!-- Move a list of nodes to the end of a document section. -->
	<rule disable="true"  type="move" target="section" description="Copy and append all paragraph nodes from document id=3 section id=1 whose text length greater than 2 characters">
		<tasks>
			<!-- Find a range of nodes in document 3 section 1 whose text content gt 2 characters-->
			<task type="src_path" src="//div/div[@class='Document' and @id='3']/div[@class='Section' and @id='1']/p[starts-with(@class,'CoverText')]"></task>
			<task type="target_path" src="//div/div/div[@class='Document' and @id='2']/div[@class='Section' and @id='1']"/>
			<task type="append_child"/>
		</tasks>
	</rule>
	<!-- Move a list of nodes and insert before a paragraph with previous next sibling . -->
	<rule disable="true" type="move" target="section" description="Copy and append all paragraph nodes from document id=3 section id=1 whose text length greater than 2 characters">
		<tasks>
			<task type="src_path" src="//div/div[@class='Document' and @id='3']/div[@class='Section' and @id='1']/p[starts-with(@class,'CoverText')]"></task>
			<task type="target_path" src="//div/div/div[@class='Document' and @id='2']/div/p[starts-with(@class,'TableBody')]"/>
			<task type="insert_before"/>
		</tasks>
	</rule>
	<!-- Move a list of nodes and insert after a paragraph with no next sibling. -->
	<rule disable="true" type="move" target="section" description="Copy and append all paragraph nodes from document id=3 section id=1 whose text length greater than 2 characters">
		<tasks>
			<task type="src_path" src="//div/div[@class='Document' and @id='3']/div[@class='Section' and @id='1']/p[starts-with(@class,'CoverText')]"></task>
			<task type="target_path" src="//div/div/div[@class='Document' and @id='2']/div/p[starts-with(@class,'TableBody')]"/>
			<task type="insert_after"/>
		</tasks>
	</rule>
	<!-- Move a list of nodes and insert before a paragraph with no next sibling. -->
	<rule disable="true" type="move" target="section" description="Copy and append all paragraph nodes from document id=3 section id=1 whose text length greater than 2 characters">
		<tasks>
			<task type="src_path" src="//div/div[@class='Document' and @id='3']/div[@class='Section' and @id='1']/p[starts-with(@class,'CoverText')]"></task>
			<task type="target_path" src="//div/div/div[@class='Document' and @id='2']/div/p[starts-with(@class,'TableBody')]"/>
			<task type="insert_before"/>
		</tasks>
	</rule>
	<!-- Delete a list of nodes containing useless information.  -->
	<rule disable="false" type="delete" target="section" description="Delete a list of nodes from document id=3 section id=1 whose text length is 1 whitespace character">
		<tasks>
			<task type="target_path" src="//div/div[@class='Document' and @id='3']/div[@class='Section' and @id='1']/p[(text() = ' ' or text() = 'Ã‚ ') and not(string-length(span/text()) > 0)]"></task>
		</tasks>
	</rule>
	<!-- The following block of rules creates a Process Overview -->
	<rule disable="true" type="create" target="document" description="Process Overview">
		<tasks>
			<!-- Find the first folder -->
			<task type="src_path" src="(//div/div[starts-with(@class,'Folder')])[1]"></task>
			<!--  Append a child document called "Process Overview" -->
			<task type="append_child" target_title="Process Overview"/>
		</tasks>
	</rule>
	<rule disable="true" type="create" target="document" description="Process Overview">
		<tasks>
			<!-- Find the first folder -->
			<task type="src_path" src="(//div/div[starts-with(@class,'Folder')])[1]/div[@class='Document' and @title='Document Preliminaries']"></task>
			<!--  Append a child document called "Process Overview" -->
			<task type="insert_before" target_title="Process Overview"/>
		</tasks>
	</rule>
	<rule disable="true" type="create" target="document" description="Process Overview">
		<tasks>
			<!-- Find the first folder -->
			<task type="src_path" src="(//div/div[starts-with(@class,'Folder')])[1]/div[@class='Document' and @title='Document Preliminaries']"></task>
			<!--  Append a child document called "Process Overview" -->
			<task type="insert_after" target_title="Process Overview"/>
		</tasks>
	</rule>
	<rule disable="false" type="create" target="document" description="Process Overview">
		<tasks>
			<!-- Find the first folder -->
			<task type="src_path" src="(//div/div[starts-with(@class,'Folder')])[1]/div[@class='Folder' and @title='Emails']"></task>
			<!--  Append a child document called "Process Overview" -->
			<task type="insert_after" target_title="Process Overview"/>
		</tasks>
	</rule>
	<!-- Find all sections in document 3 and copy to the newly created "Process Overview" document -->
	<rule disable="false" type="copy" target="document" description="Copy all non initial and procedure sections from the policy document to the Process Overview document">
		<tasks>
			<task type="src_path" src="//div/div[@class='Document' and @id='3']/div[@class='Section' and @id &gt; 1 and @id &lt; 8]"></task>
			<task type="target_path" src="//div/div[@class='Document' and @title='Process Overview']"></task>
			<task type="append_child" target_title="Process Overview"/>
		</tasks>
	</rule>
	<rule disable="false" type="delete" target="section" description="Delete all paragraphs in sections other than the Purpose section inside the Process Overview document">
		<tasks>
			<task type="target_path" src="//div/div[@class='Document' and @title='Process Overview']/div[@class='Section' and not(@title='Purpose')]/p"></task>
		</tasks>
	</rule>
</rules>
```

### Todos

 - see the Roadmap

License
----

MIT

**Free Software, Sure is!**

