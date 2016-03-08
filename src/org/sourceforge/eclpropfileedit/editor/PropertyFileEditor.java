package org.sourceforge.eclpropfileedit.editor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.sourceforge.eclpropfileedit.MyActivator;
import org.sourceforge.eclpropfileedit.core.PropertyLineWrapper;
import org.sourceforge.eclpropfileedit.io.PropertyFileHandler;

public class PropertyFileEditor extends EditorPart {

    class PropertyContentProvider implements IStructuredContentProvider, IPropertyListViewer {

        private PropertyFileEditor propertyFileEditor;

        public PropertyContentProvider(PropertyFileEditor propertyFileEditor) {
            this.propertyFileEditor = propertyFileEditor;
        }

        public void addProperty(PropertyLineWrapper line) {
            this.propertyFileEditor.setDirty(true);
            this.propertyFileEditor.firePropertyChangeWrapper(PROP_DIRTY);
            TableViewer tableViewer = this.propertyFileEditor.tableViewer;
            tableViewer.add(line);
            Table table = tableViewer.getTable();
            for (int i = 0; i < table.getItemCount(); i++) {
                Object obj = table.getItem(i).getData();
                if (obj.equals(line)) {
                    this.propertyFileEditor.lastSelectedIndex = i;
                    table.select(this.propertyFileEditor.lastSelectedIndex);
                    this.propertyFileEditor.table.forceFocus();
                    break;
                }

            }
        }

        public void dispose() {
            this.propertyFileEditor.propertyLinesList.removeChangeListener(this);
        }

        public Object[] getElements(Object parent) {
            return this.propertyFileEditor.propertyLinesList.getProperties().toArray();
        }

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
            if (newInput != null)
                ((PropertiesList) newInput).addChangeListener(this);
            if (oldInput != null)
                ((PropertiesList) oldInput).removeChangeListener(this);
        }

        public void removeProperty(PropertyLineWrapper line) {
            this.propertyFileEditor.setDirty(true);
            this.propertyFileEditor.firePropertyChangeWrapper(PROP_DIRTY);
            this.propertyFileEditor.tableViewer.remove(line);
            List properties = this.propertyFileEditor.propertyLinesList.getProperties();
            properties.remove(line);
            if (this.propertyFileEditor.lastSelectedIndex >= properties.size())
                this.propertyFileEditor.lastSelectedIndex = properties.size() - 1;
            this.propertyFileEditor.table.select(PropertyFileEditor.this.lastSelectedIndex);
            this.propertyFileEditor.table.forceFocus();
        }

        public void removeAll() {
            List properties = this.propertyFileEditor.propertyLinesList.getProperties();
            TableViewer tableViewer = this.propertyFileEditor.tableViewer;
            for (Iterator it = properties.iterator(); it.hasNext();) {
                PropertyLineWrapper line = (PropertyLineWrapper) it.next();
                tableViewer.remove(line);
            }
            properties.clear();
        }

        public void updateProperty(PropertyLineWrapper line) {
            this.propertyFileEditor.setDirty(true);
            this.propertyFileEditor.firePropertyChangeWrapper(PROP_DIRTY);
            this.propertyFileEditor.tableViewer.update(line, null);
        }
    }

    public static final String DEFAULT_LOCALE = "<DEFAULT>";

    public static void main(String[] args) {
        Shell shell = new Shell();
        shell.setText("Task List - TableViewer Example");
        // Set layout for shell
        GridLayout layout = new GridLayout();
        shell.setLayout(layout);
        // Create a composite to hold the children
        Composite composite = new Composite(shell, SWT.NONE);
        final PropertyFileEditor propertyFileEditor = new PropertyFileEditor();
        propertyFileEditor.file = new File("/home/bob/src/java/workspace/Miscs/a_ru.properties"
        // "/home/bob/src/workspace/Miscs/src/timestamp.properties"
        // "/home/bob/src/amficom/Survey/src/com/syrus/AMFICOM/Client/General/Lang/survey_ru.properties"
        );
        propertyFileEditor.createPartControl(composite);

        propertyFileEditor.getControl().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                propertyFileEditor.dispose();
            }
        });
        // Ask the shell to display its content
        shell.open();
        propertyFileEditor.run(shell);
    }

    private Button closeButton;

    private Map columnLocale = new HashMap();

    // private String fileName;

    private Image image;

    private boolean isReadOnly;

    private PropertyFileDialog propertiesFileDialog;

    private Shell shell;

    private ICellEditorListener cellEditorListener;

    private String[] columnNames;

    boolean innerChange = false;

    Text commentText;

    boolean dirty;

    private CellEditor[] editors;

    File file;

    int lastSelectedIndex = 0;

    PropertiesList propertyLinesList;

    Table table;

    TableViewer tableViewer;

    PropertyContentProvider propertyContentProvider;

    private IResourceChangeListener listener;

    public PropertyFileEditor() {
        // nothing
    }

    public void addNewLocale(String locale, String description) {
        Map locales = this.propertyLinesList.getLocales();
        if ((description.length() == locale.length()) && (locale.length() == 0))
            description = DEFAULT_LOCALE;
        if (!locales.containsKey(locale)) {
            String[] columnNames = new String[PropertyFileEditor.this.columnNames.length + 1];
            System.arraycopy(PropertyFileEditor.this.columnNames, 0, columnNames, 0,
                    PropertyFileEditor.this.columnNames.length);
            columnNames[columnNames.length - 1] = description;
            this.columnLocale.put(columnNames[columnNames.length - 1], locale);
            PropertyFileEditor.this.columnNames = columnNames;
            TableColumn column = new TableColumn(PropertyFileEditor.this.table, SWT.LEFT,
                    PropertyFileEditor.this.columnNames.length - 1);
            column.setText(description);
            column.setWidth(100);
            PropertyFileEditor.this.tableViewer.setColumnProperties(PropertyFileEditor.this.columnNames);

            CellEditor[] editors = new CellEditor[PropertyFileEditor.this.editors.length + 1];
            System.arraycopy(PropertyFileEditor.this.editors, 0, editors, 0, PropertyFileEditor.this.editors.length);
            editors[editors.length - 1] = new TextCellEditor(PropertyFileEditor.this.table);
            editors[editors.length - 1].addListener(PropertyFileEditor.this.cellEditorListener);
            PropertyFileEditor.this.editors = editors;
            PropertyFileEditor.this.tableViewer.setCellEditors(editors);

            final String l = locale;
            column.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    PropertyFileEditor.this.tableViewer.setSorter(new PropertiesSorter(l));
                }
            });
            locales.put(locale, locale);
            this.setDirty(true);
            this.firePropertyChangeWrapper(PROP_DIRTY);
        }

    }

    /*
     * Close the window and dispose of resources
     */
    public void close() {
        Shell shell = this.table.getShell();
        if (shell != null && !shell.isDisposed())
            shell.dispose();
    }

    public void createPartControl(Composite parent) {
        this.propertyLinesList = new PropertiesList();
        this.updateLocales();
        this.addChildControls(parent);
        try {
            this.refresh();
        } catch (IOException e) {
            MyActivator.log(e);
        }
    }

    /**
     * Release resources
     */
    public void dispose() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.removeResourceChangeListener(listener);
        
        // Tell the label provider to release its ressources
        this.tableViewer.getLabelProvider().dispose();
    }

    public void doSave(IProgressMonitor progressMonitor) {
        this.innerChange = true;
        final FileEditorInput input = (FileEditorInput) getEditorInput();
        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

            public void execute(IProgressMonitor pm) throws CoreException {
                try {
                    PropertyFileEditor.this.propertyLinesList.savePropertiesList();
                } catch (IOException e) {
                    MyActivator.log(e);
                }
                input.getFile().getProject().refreshLocal(IResource.DEPTH_INFINITE, pm);
            }
        };
        try {
            new ProgressMonitorDialog(getSite().getShell()).run(false, false, operation);
            setDirty(false);
            firePropertyChange(PROP_DIRTY);
        } catch (InterruptedException x) {
            // nothing
        } catch (OperationCanceledException x) {
            // nothing
        } catch (InvocationTargetException x) {
            // nothing
        } finally {
            this.innerChange = false;
        }

    }

    /**
     * @see org.eclipse.ui.IEditorPart#doSaveAs()
     */
    public void doSaveAs() {
        SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
        saveAsDialog.create();
        saveAsDialog.open();
        IPath newPath = saveAsDialog.getResult();
        IWorkspace workspace = MyActivator.getWorkspace();
        IFile file = workspace.getRoot().getFile(newPath);
        // file.setReadOnly(isReadOnly());
        final FileEditorInput newInput = new FileEditorInput(file);
        this.file = newInput.getFile().getLocation().toFile();
        // setTitle(this.file.getName());
        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

            public void execute(IProgressMonitor pm) throws CoreException {
                PropertyFileEditor.this.propertyLinesList.setParent(PropertyFileEditor.this.file.getParentFile());
                PropertyFileEditor.this.propertyLinesList.setBaseName(PropertyFileEditor.this.file.getName());
                try {
                    PropertyFileEditor.this.propertyLinesList.savePropertiesList();
                } catch (IOException e) {
                    MyActivator.log(e);
                }
                newInput.getFile().getProject().refreshLocal(IResource.DEPTH_INFINITE, pm);// new
                // NullProgressMonitor());
            }
        };
        try {
            // operation.run(pm);
            new ProgressMonitorDialog(getSite().getShell()).run(false, false, operation);
            setDirty(false);
            firePropertyChange(PROP_DIRTY);
        } catch (InterruptedException x) {
            // do nothing so far
        } catch (OperationCanceledException x) {
            // do nothing so far
        } catch (InvocationTargetException x) {
            // do nothing so far
        }
    }

    /**
     * Return the 'close' Button
     */
    public Button getCloseButton() {
        return this.closeButton;
    }

    /**
     * Return the column names in a collection
     * 
     * @return List containing column names
     */
    public String[] getColumnNames() {
        return this.columnNames;
    }

    /**
     * Return the parent composite
     */
    public Control getControl() {
        return this.table.getParent();
    }

    public String getLocale(String column) {
        return (String) this.columnLocale.get(column);
    }

    public Image getLogo() {
        return this.image;
    }

    /**
     * Return the ExampleTaskList
     */
    public PropertiesList getPropertyLinesList() {
        return this.propertyLinesList;
    }

    /**
     * @return currently selected item
     */
    public ISelection getSelection() {
        return this.tableViewer.getSelection();
    }

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        final FileEditorInput fei = (FileEditorInput) input;
        setSite(site);
        setInput(input);
        // set the file's name
        // this.fileName = fei.getName();
        // determine whether the file is read-only
        this.isReadOnly = fei.getFile().isReadOnly();
        // set the title of the editor to the file's name
        // setTitle(this.fileName);
        // get the file as java.io.File object
        this.file = fei.getFile().getLocation().toFile();
        this.image = fei.getImageDescriptor().createImage();
        this.shell = getSite().getShell();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        listener = new IResourceChangeListener() {

            public void resourceChanged(IResourceChangeEvent event) {
                if (PropertyFileEditor.this.innerChange)
                    return;
                IPath path = fei.getFile().getParent().getFullPath();
                // we are only interested in POST_CHANGE events
                if (event.getType() != IResourceChangeEvent.POST_CHANGE)
                    return;
                IResourceDelta rootDelta = event.getDelta();
                // get the delta, if any, for the documentation directory
                IResourceDelta docDelta = rootDelta.findMember(path);
                if (docDelta == null) {
                    return;
                }
                final ArrayList changed = new ArrayList();
                IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

                    public boolean visit(IResourceDelta delta) {

                        if (delta == null)
                            return false;
                        // only interested in changed resources (not added or
                        // removed)
                        if (delta.getKind() != IResourceDelta.CHANGED && delta.getKind() != IResourceDelta.ADDED
                                && delta.getKind() != IResourceDelta.REMOVED) {
                            return false;
                        }
                        IResource resource = delta.getResource();
                        // only interested in files with the "properties"
                        // extension
                        if (resource.getType() == IResource.FILE && PropertyFileHandler.PROPERTIES_EXTENSION
                                .equalsIgnoreCase(resource.getFileExtension())) {
                            changed.add(resource);
                        }
                        return true;
                    }
                };
                try {
                    docDelta.accept(visitor);
                } catch (CoreException e) {
                    // open error dialog with syncExec or print to plugin log
                    // file
                }
                // nothing more to do if there were no changed text files
                if (changed.size() == 0) {
                    return;
                }
                // post this update to the table

                PropertyFileEditor.this.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                    public void run() {
                        if (MessageDialog.openQuestion(PropertyFileEditor.this.getSite().getShell(),
                                MyActivator.getResourceString("ContentWasChanged.Title"),
                                MyActivator.getResourceString("ContentWasChanged"))) {

                            try {
                                PropertyFileEditor.this.refresh();
                            } catch (IOException e) {
                                MyActivator.log(e);
                            }
                        }
                    }
                });

            }
        };
        workspace.addResourceChangeListener(listener);
    }

    public boolean isDirty() {
        return this.dirty;
    }

    /**
     * @return Returns the isReadOnly.
     */
    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public boolean isSaveAsAllowed() {
        return true;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setFocus() {
        setFocusOnRow();
    }

    public void setFocusOnRow() {
        this.table.select(this.lastSelectedIndex);
        this.table.setFocus();
    }

    /**
     * Create a new shell, add the widgets, open the shell
     * 
     * @return the shell that was created
     */
    private void addChildControls(Composite composite) {
        // Create a composite to hold the children
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH);
        composite.setLayoutData(gridData);
        // Set numColumns to 3 for the buttons
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 3;
        composite.setLayout(layout);
        // Create the table
        createTable(composite);
        // Create and setup the TableViewer
        createTableViewer();
        // The input for the table viewer is the instance of ExampleTaskList
        this.propertyContentProvider = new PropertyContentProvider(this);
        this.tableViewer.setContentProvider(this.propertyContentProvider);
        this.tableViewer.setLabelProvider(new PropertiesLabelProvider(this));
        this.tableViewer.setInput(this.propertyLinesList);
        // Add the buttons
        createButtons(composite);
    }

    /**
     * Add the "Add", "Delete" and "Close" buttons
     * 
     * @param parent
     *            the parent composite
     */
    private void createButtons(Composite parent) {
        new Label(parent, SWT.NONE).setText(MyActivator.getResourceString("Comment") + ":");
        // create the comment's textfield
        this.commentText = new Text(parent, SWT.BORDER);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = GridData.FILL;
        this.commentText.setLayoutData(gridData);
        this.commentText.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                PropertyFileEditor.this.dirty = true;
                PropertyFileEditor.this.firePropertyChangeWrapper(PROP_DIRTY);
                keyReleased(e);
            }

            public void keyReleased(KeyEvent e) {
                PropertyFileEditor.this.dirty = true;
                PropertyFileEditor.this.firePropertyChangeWrapper(PROP_DIRTY);
                if (e.character == SWT.CR) {
                    Text text = (Text) e.getSource();
                    int index = PropertyFileEditor.this.table.getSelectionIndex();
                    PropertyLineWrapper line = (PropertyLineWrapper) PropertyFileEditor.this.table.getItem(index)
                            .getData();
                    line.setComment(text.getText());
                }
            }
        });

        // Create and configure the "Add" button
        Button addPropertyButton = new Button(parent, SWT.PUSH | SWT.CENTER);
        addPropertyButton.setText(MyActivator.getResourceString("addProperty"));
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        addPropertyButton.setLayoutData(gridData);
        addPropertyButton.addSelectionListener(new SelectionAdapter() {

            // Add a task to the ExampleTaskList and refresh the view
            public void widgetSelected(SelectionEvent e) {
                PropertyLineWrapper line = PropertyFileEditor.this.propertyLinesList.addProperty();
                PropertyFileEditor.this.propertyContentProvider.addProperty(line);
                for (int i = 0; i < PropertyFileEditor.this.table.getItemCount(); i++) {
                    Object obj = PropertyFileEditor.this.table.getItem(i).getData();
                    if (obj.equals(line)) {
                        PropertyFileEditor.this.lastSelectedIndex = i;
                        PropertyFileEditor.this.table.select(PropertyFileEditor.this.lastSelectedIndex);
                        PropertyFileEditor.this.table.forceFocus();
                        break;
                    }

                }
            }
        });

        Button addLocaleButton = new Button(parent, SWT.PUSH | SWT.CENTER);
        addLocaleButton.setText(MyActivator.getResourceString("addLocale"));
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        addLocaleButton.setLayoutData(gridData);
        addLocaleButton.addSelectionListener(new SelectionAdapter() {

            // Add a task to the ExampleTaskList and refresh the view
            public void widgetSelected(SelectionEvent e) {
                /**
                 * @TODO
                 */
                openNewPropertyDialog();

            }
        });

        // Create and configure the "Delete" button
        Button deleteButton = new Button(parent, SWT.PUSH | SWT.CENTER);
        deleteButton.setText(MyActivator.getResourceString("Delete"));
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.widthHint = 80;
        deleteButton.setLayoutData(gridData);
        deleteButton.addSelectionListener(new SelectionAdapter() {

            // Remove the selection and refresh the view
            public void widgetSelected(SelectionEvent e) {
                PropertyLineWrapper line = (PropertyLineWrapper) ((IStructuredSelection) PropertyFileEditor.this.tableViewer
                        .getSelection()).getFirstElement();
                if (line != null) {
                    PropertyFileEditor.this.propertyContentProvider.removeProperty(line);
                }
            }
        });
        parent.pack();
    }

    /**
     * Create the Table
     */
    private void createTable(Composite parent) {
        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
        final int NUMBER_COLUMNS = this.columnNames.length;

        Group tableGroup = new Group(parent, SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        GridLayout gridLayout = new GridLayout();
        gridData.horizontalSpan = 3;
        tableGroup.setLayoutData(gridData);
        // gridLayout.numColumns = NUMBER_COLUMNS;
        this.table = new Table(tableGroup, style);
        tableGroup.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = NUMBER_COLUMNS;
        this.table.setLayoutData(gridData);
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);
        this.table.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                Table table = (Table) e.getSource();
                PropertyFileEditor.this.lastSelectedIndex = table.getSelectionIndex();
                PropertyLineWrapper line = (PropertyLineWrapper) table
                        .getItem(PropertyFileEditor.this.lastSelectedIndex).getData();
                PropertyFileEditor.this.commentText.setText(line.getComment());
            }
        });

        this.table.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                // TODO Auto-generated method stub

                int input;
                if (e.keyCode != 0) {
                    input = e.keyCode | e.stateMask;
                    int firstSelected = -1;
                    int lastSelected = -1;
                    for (int i = 0; i < PropertyFileEditor.this.table.getItemCount(); i++) {
                        PropertyLineWrapper line = (PropertyLineWrapper) PropertyFileEditor.this.table.getItem(i)
                                .getData();
                        String key = line.getKey();
                        if (key.length() > 0 && key.charAt(0) == input) {
                            if (firstSelected == -1)
                                firstSelected = i;
                            lastSelected = i;
                            PropertyFileEditor.this.table.select(i);
                            if (i > PropertyFileEditor.this.lastSelectedIndex)
                                break;
                        }

                    }

                    if (firstSelected != -1) {
                        if (PropertyFileEditor.this.lastSelectedIndex == lastSelected)
                            PropertyFileEditor.this.table.select(firstSelected);

                        PropertyFileEditor.this.lastSelectedIndex = PropertyFileEditor.this.table.getSelectionIndex();

                    }

                    PropertyFileEditor.this.table.forceFocus();

                } else {
                    input = e.character | e.stateMask;
                }
            }

        });

    }

    /**
     * Create the TableViewer
     */
    private void createTableViewer() {
        this.tableViewer = new TableViewer(this.table);
        this.tableViewer.setUseHashlookup(true);
        this.tableViewer.setColumnProperties(this.columnNames);
        if (!this.isReadOnly) {

            this.cellEditorListener = new ICellEditorListener() {

                public void applyEditorValue() {
                    PropertyFileEditor.this.dirty = true;
                    PropertyFileEditor.this.firePropertyChangeWrapper(PROP_DIRTY);
                    PropertyFileEditor.this.tableViewer.refresh();
                }

                public void cancelEditor() {
                    // nothing

                }

                public void editorValueChanged(boolean arg0, boolean arg1) {
                    // nothing
                }
            };
            this.editors = new CellEditor[this.columnNames.length];
            this.editors[0] = new CheckboxCellEditor(this.table);
            this.editors[0].addListener(this.cellEditorListener);

            for (int i = 1; i < this.editors.length; i++) {
                this.editors[i] = new TextCellEditor(this.table);
                this.editors[i].addListener(this.cellEditorListener);
            }
            // Assign the cell editors to the viewer
            // this.tableViewer.setCellEditors(this.editors);
            // Set the cell modifier for the viewer
            this.tableViewer.setCellModifier(new PropertiesCellModifier(this));
        }
        // Set the default sorter for the viewer
        this.tableViewer.setSorter(new PropertiesSorter(null));

        updateTableColumns();
    }

    /**
     * Run and wait for a close event
     * 
     * @param shell
     *            Instance of Shell
     */
    private void run(Shell shell) {
        this.shell = shell;
        // Add a listener for the close button
        if (this.closeButton != null) {
            this.closeButton.addSelectionListener(new SelectionAdapter() {

                // Close the view i.e. dispose of the composite's parent
                public void widgetSelected(SelectionEvent e) {
                    PropertyFileEditor.this.table.getParent().getParent().dispose();
                }
            });
        }
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }

    private void updateLocales() {
        Collection locales = this.propertyLinesList.getSortedLocales();
        this.columnNames = new String[2 + locales.size()];
        this.columnNames[0] = "!";
        this.columnNames[1] = MyActivator.getResourceString("Key");
        {
            int i = 2;
            for (Iterator it = locales.iterator(); it.hasNext();) {
                String locale = (String) it.next();
                if (locale.length() == 0)
                    this.columnNames[i] = DEFAULT_LOCALE;
                else
                    this.columnNames[i] = locale;
                this.columnLocale.put(this.columnNames[i], locale);
                i++;
            }
        }
    }

    void firePropertyChangeWrapper(int flag) {
        firePropertyChange(flag);
    }

    void openNewPropertyDialog() {
        this.propertiesFileDialog = new PropertyFileDialog(this.shell, this);
        this.propertiesFileDialog.create();
        this.propertiesFileDialog.getShell().setText(MyActivator.getResourceString("CreateNewLocale"));
        this.propertiesFileDialog.getShell().setImage(getLogo());
        this.propertiesFileDialog.setNew(true);
        this.propertiesFileDialog.open();
    }

    /**
     * refresh content from files, table and so on
     * @throws IOException 
     * 
     */
    void refresh() throws IOException {
        TableColumn[] columns = this.table.getColumns();
        for (int i = 0; i < columns.length; i++) {
            columns[i].dispose();
        }
        this.propertyContentProvider.removeAll();

        this.propertyLinesList.setFile(this.file);
        this.updateLocales();
        this.updateTableColumns();

        this.tableViewer.setColumnProperties(this.columnNames);

        CellEditor[] editors = new CellEditor[this.columnNames.length];
        System.arraycopy(this.editors, 0, editors, 0, this.editors.length);
        this.editors = editors;

        if (this.editors[0] == null) {
            this.editors[0] = new CheckboxCellEditor(this.table);
            this.editors[0].addListener(this.cellEditorListener);
        }
        for (int i = 1; i < this.columnNames.length; i++) {
            if (this.editors[i] == null) {
                this.editors[i] = new TextCellEditor(this.table);
                this.editors[i].addListener(this.cellEditorListener);
            }
        }
        this.tableViewer.setCellEditors(this.editors);
        this.tableViewer.setLabelProvider(new PropertiesLabelProvider(this));
        this.tableViewer.setInput(this.propertyLinesList);

        // there is no changes as we refreshed
        this.dirty = false;
        this.firePropertyChange(PROP_DIRTY);

    }

    void updateTableColumns() {
        TableColumnLayout layout = new TableColumnLayout();
        this.table.getParent().setLayout( layout ); 
        
        // 1st column with image/checkboxes - NOTE: The SWT.CENTER has no
        // effect!!
        TableColumn column = new TableColumn(this.table, SWT.CENTER, 0);
        column.setText("!");
        column.setResizable(false);
        column.setWidth(20);
        layout.setColumnData( column, new ColumnWeightData( 0 ) );
        
        
        // 2nd column with task Description
        column = new TableColumn(this.table, SWT.LEFT, 1);
        column.setText(MyActivator.getResourceString("Key"));
        column.setWidth(100);
        layout.setColumnData( column, new ColumnWeightData( 20 , 100) );

        // Add listener to column so tasks are sorted by description when
        // clicked
        column.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                PropertyFileEditor.this.tableViewer.setSorter(new PropertiesSorter(null));
            }
        });
        // 3rd column with task Owner
        for (int i = 2; i < this.columnNames.length; i++) {
            final String locale = this.columnNames[i];
            column = new TableColumn(this.table, SWT.LEFT, i);
            column.setText(locale);
            //column.setWidth(100);
            layout.setColumnData( column, new ColumnWeightData( 20, 100 ) );
            
            // Add listener to column so tasks are sorted by owner when clicked
            column.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    PropertyFileEditor.this.tableViewer.setSorter(new PropertiesSorter(locale));
                }
            });
        }
    }

}
