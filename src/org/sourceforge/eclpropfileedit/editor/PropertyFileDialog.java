/* $RCSfile: PropertyFileDialog.java,v $
 * Created on 22.09.2002, 01:53:47 by Oliver David
 * $Source: /cvsroot/epfe/epfe/src/org/sourceforge/eclpropfileedit/editor/PropertyFileDialog.java,v $
 * $Id: PropertyFileDialog.java,v 1.2 2004/11/12 22:16:39 bob_marlin Exp $
 * Copyright (c) 2000-2002 Oliver David. All rights reserved. */
package org.sourceforge.eclpropfileedit.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * @author Oliver David
 * @version $Revision: 1.2 $
 */
public class PropertyFileDialog extends Dialog {

	//private Composite parent;

	private Label						descriptionLabel;
	private Label						localeLabel;
	private Text						descriptionText;
	private Text						localeText;


	private boolean						descriptionedProperty;
	private PropertyFileEditor			propertiesFilesEditor;

	private boolean						isNew;


	/**
	 * Constructor for PropertyFileDialog.
	 * 
	 * @param parent
	 */
	public PropertyFileDialog(Shell parent,
			PropertyFileEditor propertiesFilesEditor) {
		super(parent);
		this.propertiesFilesEditor = propertiesFilesEditor;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 10;
		gridLayout.verticalSpacing = 10;
		gridLayout.marginWidth = 10;
		gridLayout.numColumns = 2;
		parent.setLayout(gridLayout);

		this.localeLabel = new Label(parent, SWT.NONE);
		this.localeLabel.setText("Locale:");
		GridData data = new GridData();
		data.verticalAlignment = GridData.BEGINNING;
		this.localeLabel.setLayoutData(data);

		this.localeText = new Text(parent, SWT.BORDER/* | SWT.WRAP | SWT.V_SCROLL */);
		data = new GridData();
		//        data.heightHint = 26;
		data.widthHint = 450;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		this.localeText.setLayoutData(data);

		this.descriptionLabel = new Label(parent, SWT.NONE);
		this.descriptionLabel.setText("Description:");
		data = new GridData();
		data.verticalAlignment = GridData.BEGINNING;
		this.descriptionLabel.setLayoutData(data);

		this.descriptionText = new Text(parent, SWT.BORDER/* | SWT.WRAP | SWT.V_SCROLL */);
		data = new GridData();
		//        data.heightHint = 26;
		data.widthHint = 450;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		this.descriptionText.setLayoutData(data);

		// create the top level composite for the dialog area
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);

		data = new GridData(GridData.FILL_BOTH);

		composite.setLayoutData(data);
		composite.setFont(parent.getFont());

		return composite;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		String localeString = this.localeText.getText();
		String descriptionString = this.getDescriptionString();
		if ((descriptionString==null)||(descriptionString.length()==0))
			descriptionString = localeString;
		

		if (localeString == null) {
			MessageDialog messageDialog = new MessageDialog(getShell(),
					"New Locale...", this.propertiesFilesEditor.getLogo(),
					"The locale string may not be empty",
					MessageDialog.INFORMATION, new String[] { "OK"}, 0);
			messageDialog.open();
		} else {
			this.propertiesFilesEditor.addNewLocale(localeString,descriptionString);
			super.okPressed();
		}
		this.propertiesFilesEditor.setFocusOnRow();
	}

	/**
	 * Returns the descriptionText.
	 * 
	 * @return String
	 */
	public String getDescriptionString() {
		return this.descriptionText.getText();
	}

	/**
	 * Returns the localeText.
	 * 
	 * @return String
	 */
	public String getLocaleString() {
		return this.localeText.getText();
	}

	/**
	 * Sets the descriptionText.
	 * 
	 * @param descriptionText
	 *            The descriptionText to set
	 */
	public void setDescriptionString(String descriptionString) {
		this.descriptionText.setText(descriptionString);
	}


	/**
	 * Sets the localeText.
	 * 
	 * @param localeText
	 *            The localeText to set
	 */
	public void setLocaleString(String localeString) {
		this.localeText.setText(localeString);
	}

	/**
	 * Returns the descriptionedProperty.
	 * 
	 * @return boolean
	 */
	public boolean isCommentedProperty() {
		return this.descriptionedProperty;
	}

	/**
	 * Sets the descriptionedProperty.
	 * 
	 * @param descriptionedProperty
	 *            The descriptionedProperty to set
	 */
	public void setCommentedProperty(boolean descriptionedProperty) {
		this.descriptionedProperty = descriptionedProperty;
	}

//	/**
//	 * Method commitChange.
//	 */
//	private void commitChange() {		
//		super.okPressed();
//	}

	/**
	 * Returns the isNew.
	 * 
	 * @return boolean
	 */
	public boolean isNew() {
		return this.isNew;
	}

	/**
	 * Sets the isNew.
	 * 
	 * @param isNew
	 *            The isNew to set
	 */
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		super.cancelPressed();
		this.propertiesFilesEditor.setFocusOnRow();
	}

}