package org.sourceforge.eclpropfileedit.editor;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;
import org.sourceforge.eclpropfileedit.core.PropertyLineWrapper;

public class PropertiesCellModifier implements ICellModifier {

	private PropertyFileEditor	fileEditor;

	public PropertiesCellModifier(PropertyFileEditor tableViewerExample) {
		super();
		this.fileEditor = tableViewerExample;
	}

	public boolean canModify(Object element, String property) {
		return true;
	}

	public Object getValue(Object element, String property) {
		// Find the index of the column
		int columnIndex = getIndex(property);
		Object result = null;
		PropertyLineWrapper line = (PropertyLineWrapper) element;
		switch (columnIndex) {
			case 0:
				// COLUMN_COMMENTED
				result = new Boolean(line.isCommentedProperty());
				break;
			case 1:
				// COLUMN_KEY
				result = line.getKey();
				break;
			default:
				result = line.getValue(this.fileEditor.getLocale(property));
				break;
		}
		return result;
	}

	public void modify(Object element, String property, Object value) {
		// Find the index of the column
		int columnIndex = getIndex(property);
		TableItem item = (TableItem) element;
		PropertyLineWrapper line = (PropertyLineWrapper) item.getData();
		String valueString;
		switch (columnIndex) {
			case 0:
				// COLUMN_COMMENTED
				line.setCommentedProperty(((Boolean) value).booleanValue());
				break;
			case 1:
				// COLUMN_KEY
				valueString = ((String) value).trim();
				line.setKey(valueString);
				break;
			default:
				valueString = ((String) value).trim();
				line.setValue(this.fileEditor.getLocale(property), valueString);
				break;
		}
		this.fileEditor.getPropertyLinesList().propertyChanged(line);
	}

	private int getIndex(String property) {
		int index = -1;
		String[] columnNames = this.fileEditor.getColumnNames();
		for (int i = 0; i < columnNames.length; i++) {
			if (columnNames[i].equals(property)) {
				index = i;
				break;
			}
		}
		return index;
	}
}