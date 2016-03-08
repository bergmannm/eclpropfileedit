/*
 * $RCSfile: PropertyFileHandler.java,v $ Created on 19.09.2002, 21:01:06 by
 * Oliver David $Source:
 * /cvsroot/eclpropfileedit/eclpropfileedit/src/org/sourceforge/eclpropfileedit/io/PropertyFileHandler.java,v $
 * $Id: PropertyFileHandler.java,v 1.4 2004/11/18 19:49:02 bob_marlin Exp $
 * Copyright (c) 2000-2002 Oliver David. All rights reserved.
 */
package org.sourceforge.eclpropfileedit.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.sourceforge.eclpropfileedit.core.PropertyConstants;
import org.sourceforge.eclpropfileedit.core.PropertyLineWrapper;

import ru.mipt._2ka.dzenya.util.FileSearch;

/**
 * @author Oliver
 * @version $Revision: 1.4 $
 */
public class PropertyFileHandler implements PropertyConstants {

	private static final Pattern	COMMENT_PATTERN			= Pattern
																	.compile("^\\s*#(.*)$");

	private static final Pattern	MAIN_PATTERN			= Pattern
																	.compile("^\\s*(!|;)?\\s*([^:=]+)\\s*(=|:)?\\s*(.*)\\s*$");

	public static final String		PROPERTIES_EXTENSION	= "properties";

	private static final String		PROPERTIES_SUFFUX		= "."
																	+ PROPERTIES_EXTENSION;

	private String					baseName;

	/** A table of hex digits */
	private BufferedReader			bufferedReader;

	private BufferedWriter			bufferedWriter;

	private boolean					escapeAscii				= false;

	private File					file;

	private boolean					propertyFile			= false;

	private File					parent;

	private FileReader				fileReader;

	//private Map files = new HashMap();
	private Map<String, String>						locales					= new HashMap<String, String>();

	private FileWriter				fileWriter;

	private List<PropertyLineWrapper>					list					= new LinkedList<PropertyLineWrapper>();

	//private ArrayList propertiesResults;
	private boolean					reverse					= false;

	public PropertyFileHandler() {
		// empty
	}
	/**
	 * Constructor for PropertyFileHandler.
	 * @throws IOException 
	 */
	public PropertyFileHandler(File file) throws IOException {
		setFile(file);
	}

	public String escapeUnicodeString(String inputString) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < inputString.length(); i++) {
			if (this.reverse) {
				if (i < inputString.length() - 1) {
					if (inputString.substring(i, i + 2).equals("\\u")) {
						buffer.append((char) Integer.parseInt(inputString
								.substring(i + 2, i + 6), 0x10));
						i += 5;
					} else
						buffer.append(inputString.substring(i, i + 1));
				} else
					buffer.append(inputString.substring(i, i + 1));
			} else {
				char ch = inputString.charAt(i);
				if (!this.escapeAscii && ((ch >= 0x0020) && (ch <= 0x007e))
						&& (ch != ':') && (ch != '=')) {
					buffer.append(ch);
				} else {
					buffer.append("\\u");
					String hex = Integer
							.toHexString(inputString.charAt(i) & 0xFFFF);
					hex = hex.toLowerCase();
					for (int j = hex.length(); j < 4; j++)
						buffer.append('0');
					buffer.append(hex.toUpperCase());
				}
			}
		}
		return buffer.toString();
	}

	public String getBaseName() {
		return this.baseName;
	}

	/**
	 * Method getPropertyLineWrappers.
	 * 
	 * @return ArrayList
	 */
	public List<PropertyLineWrapper> getPropertyLineWrappers() {
		return this.list;
	}

	public void setParent(File parent) {
		this.parent = parent;
	}

	public void setFile(File file) throws IOException {
		//this.file = file;
		this.locales.clear();
		String fileName = file.getName();
		List<File> list;
		int propLength = 0;
		if (fileName.endsWith(PROPERTIES_SUFFUX)) {
			this.propertyFile = true;
			String name = fileName.substring(0, fileName.length()
					- PROPERTIES_SUFFUX.length());
			int index = name.indexOf('_');
			if (index != -1)
				this.baseName = name.substring(0, index);
			else
				this.baseName = name;
			FileSearch fileSearch = new FileSearch();
			fileSearch.setFilePattern('^' + this.baseName + "(_\\w+)?\\"
					+ PROPERTIES_SUFFUX + "$");
			fileSearch.setFileType(FileSearch.FILE);
			this.parent = file.getParentFile();
			fileSearch.doSearch(this.parent);
			list = fileSearch.getFoundList();
			propLength = PROPERTIES_SUFFUX.length();
		} else {
			this.propertyFile = false;
			list = Arrays.asList(new File[] { file});
			this.baseName = fileName;
		}
		for (Iterator<File> it = list.iterator(); it.hasNext();) {
			this.file = it.next();
			String fName = this.file.getName();
			String locale = fName.substring(0, fName.length() - propLength);
			locale = locale.substring(this.baseName.length());
			if (locale.length() > 0) locale = locale.substring(1);
			this.locales.put(locale, locale);
			initReader();
			readFile(file, locale);
		}
	}

	public Map<String, String> getLocales() {
		if (this.locales.isEmpty()) this.locales.put("", "");
		return this.locales;
	}

	public Collection<String> getSortedLocales() {
		List<String> list = new ArrayList<String>(this.getLocales().keySet());
		Collections.sort(list);
		return list;
	}

	public void savePropertiesFiles() throws IOException {
		for (Iterator<String> it = this.locales.keySet().iterator(); it.hasNext();) {
			String locale = it.next();
			String fileName = this.parent.getAbsolutePath() + File.separator
					+ this.baseName + (locale.length() == 0 ? "" : "_")
					+ locale + (this.propertyFile ? PROPERTIES_SUFFUX : "");
			File file = new File(fileName);
			writeToPropertiesFile(file, locale);
		}
	}

//	/**
//	 * Method writeToPropertiesFile.
//	 * 
//	 * @param file
//	 */
//	private void writeToPropertiesFile(File file, String locale) {
//		this.file = file;
//		initWriter();
//		try {
//			for (Iterator it = this.list.iterator(); it.hasNext();) {
//				PropertyLineWrapper element = (PropertyLineWrapper) it.next();
//				String comment = element.getComment();
//				String key = escapeUnicodeString(element.getKey());
//				String value = element.getValue(locale);
//				this.reverse = false;
//				if (comment != null && comment.length() > 0) {
//					this.bufferedWriter.write(COMMENT_PREFIX);
//					this.bufferedWriter.write(comment);
//					this.bufferedWriter.newLine();
//				}
//				if ((value != null) && (value.trim().length() > 0)) {
//					if (element.isCommentedProperty())
//						this.bufferedWriter.write(KEY_COMMENT_PREFIX);
//					this.bufferedWriter.write(key);
//					this.bufferedWriter.write(KEY_VALUE_SEPARATOR);
//					this.bufferedWriter.write(escapeUnicodeString(value));
//					this.bufferedWriter.newLine();
//					this.bufferedWriter.newLine();
//				}
//			}
//			this.bufferedWriter.close();
//		} catch (IOException e) {
//			// implement Exception Handling later!
//		}
//		this.bufferedWriter = null;
//	}
	
    private void writeToPropertiesFile(final File file, final String locale) throws IOException {
        this.file = file;
        final CommentedProperties propertiez = new CommentedProperties();
        if (file.exists()) {
            final InputStream is = new FileInputStream(file);
            try {
                propertiez.load(is);
            }
            finally {
                is.close();
            }
            is.close();
        }
        final Set<String> trash = new HashSet<String>();
        for (final Object o : ((Hashtable<Object, Object>)propertiez).keySet()) {
            trash.add((String)o);
        }
        for (final PropertyLineWrapper element : this.list) {
            final String key = element.getKey();
            final String value = element.getValue(locale);
            this.reverse = false;
            if (value != null && value.trim().length() > 0) {
                propertiez.setProperty(key, value);
                trash.remove(key);
            }
        }
        for (final String s : trash) {
            propertiez.remove(s);
        }
        final OutputStream os = new FileOutputStream(file);
        try {
            propertiez.store(os, null);
        }
        finally {
            os.close();
        }
        os.close();
    }
	

//	/*
//	 * Returns true if the given line is a line that must be appended to the
//	 * next line
//	 */
//	private boolean continueLine(String line) {
//		int slashCount = 0;
//		int index = line.length() - 1;
//		while ((index >= 0) && (line.charAt(index--) == '\\'))
//			slashCount++;
//		return (slashCount % 2 == 1);
//	}

	private PropertyLineWrapper getPropertyLine(String key) {
		PropertyLineWrapper line = null;
		for (Iterator<PropertyLineWrapper> it = this.list.iterator(); it.hasNext();) {
			PropertyLineWrapper prop = it.next();
			if (prop.getKey().equals(key)) {
				line = prop;
				break;
			}
		}
		return line;
	}

	/**
	 * Method initReader.
	 */
	private void initReader() {
		try {
			this.fileReader = new FileReader(this.file);
			this.bufferedReader = new BufferedReader(this.fileReader);
		} catch (FileNotFoundException e) {
			// implement Exception Handling later!
		}
	}

	/**
	 * Method initWriter.
	 */
	private void initWriter() {
		try {
			this.fileWriter = new FileWriter(this.file, false);
			this.bufferedWriter = new BufferedWriter(this.fileWriter);
		} catch (IOException e) {
			// implement Exception Handling later!
		}
	}

//	private void readFile(String locale) {
//		String line = null;
//		String comment = null;
//		while ((line = readLine()) != null) {
//			Matcher m = COMMENT_PATTERN.matcher(line);
//			if (m.find()) {
//				comment = line.substring(m.start(1), m.end(1));
//			} else {
//				m = MAIN_PATTERN.matcher(line);
//				if (m.find()) {
//					//PropertyLineWrapper plw = new PropertyLineWrapper();
//					boolean commented = false;
//					String key = null;
//					String value = null;
//					for (int i = 0; i <= m.groupCount(); i++) {
//						int start = m.start(i);
//						int end = m.end(i);
//						if ((start > -1) && (end > -1)) {
//							String str = line.substring(m.start(i), m.end(i));
//							switch (i) {
//								case 1:
//									this.reverse = true;
//									commented = true;
//									break;
//								case 2:
//									this.reverse = true;
//									key = str.trim();
//									break;
//								case 4:
//									this.reverse = true;
//									value = escapeUnicodeString(str);
//									break;
//							}
//						}
//					}
//					PropertyLineWrapper plw = getPropertyLine(key);
//					if (plw == null) {
//						plw = new PropertyLineWrapper();
//						plw.setKey(key);
//						this.list.add(plw);
//					}
//					if (comment != null) {
//						this.reverse = true;
//						plw.setComment(escapeUnicodeString(comment));
//						comment = null;
//					}
//					plw.setValue(locale, value);
//					plw.setCommentedProperty(commented);
//				}
//			}
//		}
//	}
	
    private void readFile(final File f, final String locale) throws IOException {
        final CommentedProperties properties = new CommentedProperties();
        final InputStream inStream = new BufferedInputStream(new FileInputStream(this.file));
        try {
            properties.load(inStream);
        }
        finally {
            inStream.close();
        }
        inStream.close();
        for (final Object o : ((Hashtable<Object, Object>)properties).keySet()) {
            final String key = (String)o;
            PropertyLineWrapper plw = this.getPropertyLine(key);
            if (plw == null) {
                plw = new PropertyLineWrapper();
                plw.setKey(key);
                this.list.add(plw);
            }
            plw.setValue(locale, properties.getProperty(key));
        }
    }
	

//	/**
//	 * Method readLine.
//	 * 
//	 * @return String
//	 */
//	private String readLine() {
//		String result = null;
//		try {
//			if (this.bufferedReader != null)
//				result = this.bufferedReader.readLine();
//		} catch (IOException e) {
//			// implement Exception Handling later!
//		}
//		return result;
//	}

	/**
	 * @param baseName
	 *            The baseName to set.
	 */
	public void setBaseName(String baseName) {
		if (baseName.endsWith(PROPERTIES_SUFFUX)) {
			this.propertyFile = true;
			String name = baseName.substring(0, baseName.length()
					- PROPERTIES_SUFFUX.length());
			int index = name.indexOf('_');
			if (index != -1)
				this.baseName = name.substring(0, index);
			else
				this.baseName = name;
		} else {
			this.baseName = baseName;
		}
	}
}