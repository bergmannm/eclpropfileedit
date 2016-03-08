/**
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Apr 2, 2003 by lgauthier@opnworks.com
 */

package org.sourceforge.eclpropfileedit.editor;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.sourceforge.eclpropfileedit.core.PropertyLineWrapper;


/**
 * Label provider for the PropertyFileEditor
 * 
 * @see org.eclipse.jface.viewers.LabelProvider 
 */
public class PropertiesLabelProvider 
	extends LabelProvider
	implements ITableLabelProvider {

	// Names of images used to represent checkboxes
	public static final String CHECKED_IMAGE 	= "checked";
	public static final String UNCHECKED_IMAGE  = "unchecked";
	
	private PropertyFileEditor propertyFileEditor;

	
	public PropertiesLabelProvider(PropertyFileEditor propertyFileEditor){
		this.propertyFileEditor = propertyFileEditor;		
	}

	// For the checkbox images
	private static ImageRegistry imageRegistry = new ImageRegistry();

	/**
	 * Note: An image registry owns all of the image objects registered with it,
	 * and automatically disposes of them the SWT Display is disposed.
	 */ 
	static {
		String iconPath = "icons/"; 
		imageRegistry.put(CHECKED_IMAGE, ImageDescriptor.createFromFile(
				PropertyFileEditor.class, 
				iconPath + CHECKED_IMAGE + ".gif"
				)
			);
		imageRegistry.put(UNCHECKED_IMAGE, ImageDescriptor.createFromFile(
				PropertyFileEditor.class, 
				iconPath + UNCHECKED_IMAGE + ".gif"
				)
			);	
	}
	
	/**
	 * Returns the image with the given key, or <code>null</code> if not found.
	 */
	private Image getImage(boolean isSelected) {
		String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
		return  imageRegistry.get(key);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		String result = "";		
		PropertyLineWrapper line = (PropertyLineWrapper) element;
		switch (columnIndex) {
			case 0:  
				break;
			case 1 :
				result = line.getKey();
				break;
			default :
				String[] columnNames = this.propertyFileEditor.getColumnNames();
				if (columnIndex<columnNames.length)
				result = line.getValue(this.propertyFileEditor.getLocale(columnNames[columnIndex]));
				break; 	
		}
		return result;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return (columnIndex == 0) ?   // COMPLETED_COLUMN?
			getImage(((PropertyLineWrapper) element).isCommentedProperty()) :
			null;
	}

}
