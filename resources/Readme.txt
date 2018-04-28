External Dependencies:
======================

1. jmsg-1.0.jar from Independent Soft
+++++++++++++++++++++++++++++++++++++

This project is dependent upon the use of an external commercial library from IndependentSoft called jmsg-1.0.jar.

The jmsg-1.0.jar library allows the program to create binary email outlook msg files that are referenced as external documents by SupportPoint.

This library is a non-maven library. To reference this as a maven library it needs to be installed into the local maven repository located at:
{your-user-home-dir}/.m2 on Windows.

To install jmsg-1.0.jar as a maven library run the following command replacing the source directly with the location where you have placed the downloaded library.

mvn install:install-file -Dfile={path-to-jmsg-jar}\jmsg-1.0.jar -DgroupId=com.independentsoft.msg -DartifactId=jmsg -Dversion=1.0 -Dpackaging=jar

2. ImageMagick
++++++++++++++

Download and install the latest ImageMagick binary for windows from the following url:
	http://www.imagemagick.org/script/binary-releases.php
	
Scroll down the page and look for the heading "Windows Binary Release". Select the latest release, download and install.  

As of the writing of this REadme.txt the latest version is ImageMagick-7.0.3-0-Q16-x64-dll.exe

Warning: If ImageMagick is not installed vector graphics format images in the Word document (like .emf) will not be converted to png and will have no content.

Installing and Running the Generic Converter:
=============================================

1. Build the maven artifacts for the Generic Converter

   a. cd base directory for the project
   b. type - mvn clean install
   c. cd target directory
   d. Copy the files pgc-{major}.{minor}.{patch}-{build#}.jar and pgc-{major}.{minor}.{patch}-{build#}-distribution.zip to the target conversion directory
 
2. Setup and run the Generic Converter

   a. unzip the pgc-1.0.0-1-distribution.zip file to the current directory
   b. Observe a resources and lib folder with entries exists now in the current directory.
   c. Run the GenericConverter preferably inside a batch script  
   
   java -Xmx1024m -Xms512m -d64 -jar pgc-1.0.0-1.jar com.panviva.util.pgc.PanvivaGenericConverter \
   		-authorid 9732 -langid 32 -dbversion "10.0.2" -folderid 10001 -r "{path-to-your-document-top-folder}\rules.xml" \
   		-w "*.docx" "{path-to-your-document-top-folder}"
    
   NOTE-1: The final argument will cause the Converter to pick up all docx files in the document folder specified by {path-to-your-document-top-folder} 
           including all sub-folders.
   NOTE-2: The final argument can also specify a single word document to convert.
   

  
    
