package com.helphero.util.hhc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;

/**
 * Convenience class to manage the wildcard listing of matching files in a directory and all its sub-directories. 
 * 
 * @author jcharles
 *
 */
public abstract class FileHelper {
	static Logger logger = Logger.getLogger(FileHelper.class);
	
	public FileHelper() { 
	}
	
	/**
	 * Does the specified file exists
	 * @param file File to check if it exists
	 * @return boolean
	 */
	public static boolean exists(String file)
	{
		File f = new File(file);
		
		return f.exists(); 
	}
	
	/**
	 * Get the name part of a file from its file path.
	 * 
	 * @param file Name part of a file from its path
	 * @return String
	 */
	public static String getName(String file)
	{
		File f = new File(file);
		
		return f.getName();
	}
	
	/**
	 * Expand the command line list of files or directories specified.
	 * If the command line is a single file it added to the file list as a single entry.
	 * If the command line contains a directory it expands all files matching the wildcard in the immediate directory and all sub-directories
	 * If the command line is a file wildcard, it retrieves the parent directory and extracts all files matching the *.docx wildcard 
	 * @param args Remaining command line arguments 
	 * @param wildcard Command line wildcard value 
	 * @return List of files in this folder and all child folders expanded from the wildcard
	 */
	public static List<File> getExpandedFileList(String[] args, String wildcard)
	{
		List<File> fileList = new ArrayList<File>();
		
		for (int i = 0; i < args.length; i++) {
        	File file = new File(args[i]);
        	if (file.isFile()) {
        		// Either a specific file
        		fileList.add(file);
        	}
        	else if (file.isDirectory())
        	{
        		// Or a directory and all sub-directories containing files matching the wildcard specified
        		logger.info("Directory="+file.getAbsolutePath());
        		
        		Iterator<File> iterator = FileUtils.iterateFiles(file, new WildcardFileFilter(wildcard), TrueFileFilter.TRUE);
        		
        		while (iterator.hasNext())
        		{
        			fileList.add((File)iterator.next());
        		}
    			logger.info(fileList.size() + " Files added to directory "+file.getAbsolutePath());
        	}
        	else {
        		if (args[i].contains("*.docx"))
        		{
        			// Extract the directory of the file wildcard
        			File dir = new File(args[i].substring(0, args[i].lastIndexOf('\\')));
        			
        			logger.info("Matching a wildcard for args["+i+"]="+args[i]);
        			
        			if (dir.isDirectory())
        			{
                		// Extract all file within the directory matching the wildcard
        				Iterator iterator = FileUtils.iterateFiles(dir, new WildcardFileFilter(wildcard), TrueFileFilter.INSTANCE);
                		
        			    while (iterator.hasNext())
        			    {
        			    	fileList.add((File)iterator.next());
        			    }
            		}
        		}
        	}
        }
		return fileList;
	}
	
	/**
	 * Static method to copy a source file to a destination file 
	 * @param sourceFile The source file
	 * @param destFile The destination file
	 * @throws IOException Exception thrown if there is a problem copying the source file to the destination
	 */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
}
