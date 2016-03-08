package org.sourceforge.eclpropfileedit.editor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.sourceforge.eclpropfileedit.core.PropertyLineWrapper;
public class PropertiesSorter extends ViewerSorter {
	private String locale;
	// Criteria that the instance uses
	private int criteria;
	public PropertiesSorter(String locale) {
		super();
		this.locale = "<DEFAULT>".equals(locale) ? "" : locale;
	}
	/*
	 * (non-Javadoc) Method declared on ViewerSorter.
	 */
	public int compare(Viewer viewer, Object o1, Object o2) {
		int result = 0;
		if ((o1 instanceof PropertyLineWrapper)
				&& (o2 instanceof PropertyLineWrapper)) {
			PropertyLineWrapper task1 = (PropertyLineWrapper) o1;
			PropertyLineWrapper task2 = (PropertyLineWrapper) o2;
			if (this.locale==null){
				result = compareKeys(task1, task2);	
			}else {
				result = compareValues(task1, task2, this.locale);
			}
		}
		return result;
	}
	protected int compareKeys(PropertyLineWrapper task1,
			PropertyLineWrapper task2) {
		return this.collator.compare(task1.getKey(), task2.getKey());
	}
	protected int compareValues(PropertyLineWrapper task1,
			PropertyLineWrapper task2,String locale) {
		return this.collator.compare(task1.getValue(locale), task2.getValue(locale));
	}
	public int getCriteria() {
		return this.criteria;
	}
}