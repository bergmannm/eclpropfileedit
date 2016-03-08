// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MyActivator.java

package org.sourceforge.eclpropfileedit;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class MyActivator extends AbstractUIPlugin
{

    public MyActivator()
    {
    }

    public void start(BundleContext context)
        throws Exception
    {
        super.start(context);
        plugin = this;
    }

    public static ResourceBundle getResourceBundle()
    {
        if(resourceBundle == null)
            resourceBundle = ResourceBundle.getBundle("org.sourceforge.eclpropfileedit.pluginresources");
        return resourceBundle;
    }

    public void stop(BundleContext context)
        throws Exception
    {
        plugin = null;
        super.stop(context);
    }

    public static String getResourceString(String key)
    {
        try
        {
            return getResourceBundle().getString(key);
        }
        catch(MissingResourceException _ex)
        {
            return key;
        }
    }

    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    public static MyActivator getDefault()
    {
        return plugin;
    }

    public static void log(IOException e)
    {
        getDefault().getLog().log(new Status(4, "eclpropfileedit", e.getMessage(), e));
    }

    public static final String PLUGIN_ID = "eclpropfileedit";
    private static MyActivator plugin;
    private static ResourceBundle resourceBundle = null;

}
