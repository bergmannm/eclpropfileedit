package org.sourceforge.eclpropfileedit.editor;

import org.sourceforge.eclpropfileedit.core.PropertyLineWrapper;


public interface IPropertyListViewer {
	
	public void addProperty(PropertyLineWrapper line);
	
	public void removeProperty(PropertyLineWrapper line);
	
	public void removeAll();
	
	public void updateProperty(PropertyLineWrapper line);	
	
}
