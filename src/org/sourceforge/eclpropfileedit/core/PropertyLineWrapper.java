/* $RCSfile: PropertyLineWrapper.java,v $
 * Created on 19.09.2002, 21:02:57 by Oliver David
 * $Source: /cvsroot/epfe/epfe/src/org/sourceforge/eclpropfileedit/core/PropertyLineWrapper.java,v $
 * $Id: PropertyLineWrapper.java,v 1.1 2004/11/05 18:00:01 bob_marlin Exp $
 * Copyright (c) 2000-2002 Oliver David. All rights reserved. */
package org.sourceforge.eclpropfileedit.core;

import java.util.*;


/**
 * @author Oliver
 * @version $Revision: 1.1 $
 */
public class PropertyLineWrapper implements Comparable, PropertyConstants {

	private String				comment				= "";
	private boolean				commentedProperty;
	private String				key;

	//private PropertyFileService service;
	private Map					values				= new HashMap();

	/**
	 * Method isCommentValid.
	 * 
	 * @param comment
	 * @return boolean
	 */
	public static boolean isCommentValid(String comment) {
		return comment != null && comment.startsWith(COMMENT_PREFIX);
	}

	/**
	 * Method isKeyValuePairValid.
	 * 
	 * @param keyValuePair
	 * @return boolean
	 */
	public static boolean isKeyValuePairValid(String keyValuePair) {
		return keyValuePair != null && keyValuePair.indexOf(KEY_VALUE_SEPARATOR) > -1;
	}

	/**
	 * Method isValid.
	 * 
	 * @param textLine
	 * @return boolean
	 */
	public static boolean isValid(String textLine) {
		if ((textLine.indexOf(KEY_VALUE_SEPARATOR) > -1 || textLine.startsWith(COMMENT_PREFIX))) { return true; }
		return false;
	}

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object arg0) {
		return 0;
		//return getKey().toLowerCase().compareTo(((PropertyLineWrapper)
		// arg0).getKeyValuePair().toLowerCase());
	}

	/**
	 * Returns the comment.
	 * 
	 * @return String
	 */
	public String getComment() {
		return this.comment;
	}

	/**
	 * Returns the key.
	 * 
	 * @return String
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Returns the value.
	 * 
	 * @return String
	 */
	public String getValue(String locale) {
		String s = (String) this.values.get(locale);
		return (s==null)?"":s;
	}

	/**
	 * Returns the isCommentedPoperty.
	 * 
	 * @return boolean
	 */
	public boolean isCommentedProperty() {
		return this.commentedProperty;
	}

	/**
	 * Sets the comment.
	 * 
	 * @param comment
	 *            The comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Sets the isCommentedPoperty.
	 * 
	 * @param isCommentedPoperty
	 *            The isCommentedPoperty to set
	 */
	public void setCommentedProperty(boolean isCommentedPoperty) {
		this.commentedProperty = isCommentedPoperty;
	}

	/**
	 * Sets the key.
	 * 
	 * @param key
	 *            The key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *            The value to set
	 */
	public void setValue(String locale, String value) {
		this.values.put(locale, value);
	}
}