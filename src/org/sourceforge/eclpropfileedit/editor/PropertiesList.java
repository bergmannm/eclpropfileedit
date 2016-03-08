package org.sourceforge.eclpropfileedit.editor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sourceforge.eclpropfileedit.core.PropertyLineWrapper;
import org.sourceforge.eclpropfileedit.io.PropertyFileHandler;

public class PropertiesList {

	private List				list;
	private Set					changeListeners		= new HashSet();
	private PropertyFileHandler handler;

	/**
	 * Constructor
	 */
	public PropertiesList() {
		this.handler = new PropertyFileHandler();		
		this.initData();
	}
	
	public void savePropertiesList() throws IOException{
		this.handler.savePropertiesFiles();
	}

	private void initData() {
		this.list = this.handler.getPropertyLineWrappers();
	}
	
	public List getProperties(){
		return this.list;
	}
	
	public String getBaseName(){
		return this.handler.getBaseName();
	}
	
	public void setBaseName(String baseName){
		this.handler.setBaseName(baseName);
	}
	
	public void setFile(File file) throws IOException{
		this.list.clear();
		this.handler.setFile(file);
		this.initData();
	}
	
	public void setParent(File parent){
		this.handler.setParent(parent);
	}
	
	public Collection getSortedLocales(){
		return this.handler.getSortedLocales();
	}
	
	public Map getLocales(){
		return this.handler.getLocales();
	}


	/**
	 * Add a new task to the collection of tasks
	 */
	public PropertyLineWrapper addProperty() {
		PropertyLineWrapper line = new PropertyLineWrapper();
		line.setKey("");
		for(Iterator it=this.handler.getLocales().keySet().iterator();it.hasNext();){
			String locale = (String)it.next();
			line.setValue(locale,"");
		}		
		line.setCommentedProperty(false);
		line.setComment("");
		
		Iterator iterator = this.changeListeners.iterator();
		while (iterator.hasNext())
			((IPropertyListViewer) iterator.next()).addProperty(line);
		this.list.add(line);
		return line;
	}

	/**
	 * @param task
	 */
	public void removeProperty(PropertyLineWrapper line) {
		this.list.remove(line);
		Iterator iterator = this.changeListeners.iterator();
		while (iterator.hasNext())
			((IPropertyListViewer) iterator.next()).removeProperty(line);
	}

	/**
	 * @param line
	 */
	public void propertyChanged(PropertyLineWrapper line) {
		Iterator iterator = this.changeListeners.iterator();
		while (iterator.hasNext())
			((IPropertyListViewer) iterator.next()).updateProperty(line);
	}

	/**
	 * @param viewer
	 */
	public void removeChangeListener(IPropertyListViewer viewer) {
		this.changeListeners.remove(viewer);
	}

	/**
	 * @param viewer
	 */
	public void addChangeListener(IPropertyListViewer viewer) {
		this.changeListeners.add(viewer);
	}
}