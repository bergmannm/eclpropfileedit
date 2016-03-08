/*
 * 
 * Created on 07.09.2003
 *
 */
package ru.mipt._2ka.dzenya.util;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * 
 * @author bob
 *  
 */
public class FileSearch {

	public static final int	FILE		= 0;
	public static final int	DIRECTORY	= 1;

	private List			foundFiles	= new ArrayList();
	Pattern					pattern;
	int						fileType	= FILE;

	public void setFilePattern(String pattern) {
		this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
	}

	public String getFilePattern() {
		return this.pattern.pattern();
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	/**
	 * find recursively files which satisfy patternString and put they to
	 * foundFiles
	 * 
	 * @param fileName
	 *            start path for find
	 */
	public void doSearch(File fileName) {
		File[] fileList = fileName.listFiles(new FileFilter() {

			public boolean accept(File file) {
				//String name = file.getAbsolutePath();
				String name = file.getName();
				return (FileSearch.this.pattern.matcher(name).find())
						&& (((FileSearch.this.fileType == FILE) && (file.isFile())) || ((FileSearch.this.fileType == DIRECTORY) && (file
								.isDirectory())));

			}
		});

		for (int i = 0; i < fileList.length; i++)
			this.foundFiles.add(fileList[i]);

		File[] dirList = fileName.listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.isDirectory();
			}
		});

		for (int i = 0; i < dirList.length; i++) {
			try {
				doSearch(dirList[i]);
			} catch (NullPointerException npe) {
				// nothing
			}
		}
	}

	public List getFoundList() {
		return this.foundFiles;
	}

}