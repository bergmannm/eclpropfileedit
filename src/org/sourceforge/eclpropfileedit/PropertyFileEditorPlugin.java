///* $RCSfile: PropertyFileEditorPlugin.java,v $
// * Created on 19.09.2002, 21:01:37 by Oliver David
// * $Source: /cvsroot/epfe/epfe/src/org/sourceforge/eclpropfileedit/PropertyFileEditorPlugin.java,v $
// * $Id: PropertyFileEditorPlugin.java,v 1.2 2004/11/12 22:16:38 bob_marlin Exp $
// * Copyright (c) 2000-2002 Oliver David. All rights reserved. */
//package org.sourceforge.eclpropfileedit;
//
//import java.util.MissingResourceException;
//import java.util.ResourceBundle;
//
//import org.eclipse.core.resources.IWorkspace;
//import org.eclipse.core.resources.ResourcesPlugin;
//import org.eclipse.core.runtime.IPluginDescriptor;
//import org.eclipse.ui.plugin.AbstractUIPlugin;
//
///**
// * The main plugin class to be used in the desktop.
// */
//public class PropertyFileEditorPlugin extends AbstractUIPlugin {
//
//	//The shared instance.
//	private static PropertyFileEditorPlugin	plugin;
//	//Resource bundle.
//	private ResourceBundle					resourceBundle	= null;
//
//	/**
//	 * The constructor.
//	 */
//	public PropertyFileEditorPlugin(IPluginDescriptor descriptor) {
//		plugin = this;
//		try {
//			this.resourceBundle = ResourceBundle
//					.getBundle("org.sourceforge.eclpropfileedit.pluginresources");
//		} catch (MissingResourceException x) {
//			// nothing
//		}
//	}
//
//	/**
//	 * Returns the shared instance.
//	 */
//	public static PropertyFileEditorPlugin getDefault() {
//		return plugin;
//	}
//
//	/**
//	 * Returns the workspace instance.
//	 */
//	public static IWorkspace getWorkspace() {
//		return ResourcesPlugin.getWorkspace();
//	}
//
//	/**
//	 * Returns the string from the plugin's resource bundle, or 'key' if not
//	 * found.
//	 */
//	public static String getResourceString(String key) {		
//		if (PropertyFileEditorPlugin.getDefault() == null)
//			return key;
//		ResourceBundle bundle = PropertyFileEditorPlugin.getDefault()
//				.getResourceBundle();
//		try {
//			return bundle.getString(key);
//		} catch (MissingResourceException e) {
//			return key;
//		}
//	}
//
//	/**
//	 * Returns the plugin's resource bundle,
//	 */
//	public ResourceBundle getResourceBundle() {
//		return this.resourceBundle;
//	}
//}