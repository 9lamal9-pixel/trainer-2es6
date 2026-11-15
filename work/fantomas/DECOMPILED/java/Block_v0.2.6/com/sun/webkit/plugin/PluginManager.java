/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.plugin;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.webkit.plugin.DefaultPlugin;
import com.sun.webkit.plugin.Plugin;
import com.sun.webkit.plugin.PluginHandler;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.Vector;

public final class PluginManager {
    private static final PlatformLogger log = PlatformLogger.getLogger("com.sun.browser.plugin.PluginManager");
    private static final ServiceLoader<PluginHandler> pHandlers = ServiceLoader.load(PluginHandler.class);
    private static final TreeMap<String, PluginHandler> hndMap = new TreeMap();
    private static PluginHandler[] hndArray;
    private static final HashSet<String> disabledPluginHandlers;

    private static void updatePluginHandlers() {
        log.fine("Update plugin handlers");
        hndMap.clear();
        for (PluginHandler object2 : pHandlers) {
            String[] stringArray;
            if (!object2.isSupportedPlatform() || PluginManager.isDisabledPlugin(object2)) continue;
            for (String string : stringArray = object2.supportedMIMETypes()) {
                hndMap.put(string, object2);
                log.fine(string);
            }
        }
        Collection<PluginHandler> collection = hndMap.values();
        hndArray = collection.toArray(new PluginHandler[collection.size()]);
    }

    public static Plugin createPlugin(URL uRL, String string, String[] stringArray, String[] stringArray2) {
        try {
            PluginHandler pluginHandler = hndMap.get(string);
            if (pluginHandler == null) {
                return new DefaultPlugin(uRL, string, stringArray, stringArray2);
            }
            Plugin plugin = pluginHandler.createPlugin(uRL, string, stringArray, stringArray2);
            if (plugin == null) {
                return new DefaultPlugin(uRL, string, stringArray, stringArray2);
            }
            return plugin;
        }
        catch (Throwable throwable) {
            log.fine("Cannot create plugin", throwable);
            return new DefaultPlugin(uRL, string, stringArray, stringArray2);
        }
    }

    private static List<PluginHandler> getAvailablePlugins() {
        Vector<PluginHandler> vector = new Vector<PluginHandler>();
        for (PluginHandler pluginHandler : pHandlers) {
            if (!pluginHandler.isSupportedPlatform()) continue;
            vector.add(pluginHandler);
        }
        return vector;
    }

    private static PluginHandler getEnabledPlugin(int n) {
        if (n < 0 || n >= hndArray.length) {
            return null;
        }
        return hndArray[n];
    }

    private static int getEnabledPluginCount() {
        return hndArray.length;
    }

    private static void disablePlugin(PluginHandler pluginHandler) {
        disabledPluginHandlers.add(pluginHandler.getClass().getCanonicalName());
        PluginManager.updatePluginHandlers();
    }

    private static void enablePlugin(PluginHandler pluginHandler) {
        disabledPluginHandlers.remove(pluginHandler.getClass().getCanonicalName());
        PluginManager.updatePluginHandlers();
    }

    private static boolean isDisabledPlugin(PluginHandler pluginHandler) {
        return disabledPluginHandlers.contains(pluginHandler.getClass().getCanonicalName());
    }

    private static boolean supportsMIMEType(String string) {
        return hndMap.containsKey(string);
    }

    private static String getPluginNameForMIMEType(String string) {
        PluginHandler pluginHandler = hndMap.get(string);
        if (pluginHandler != null) {
            return pluginHandler.getName();
        }
        return "";
    }

    static {
        disabledPluginHandlers = new HashSet();
        if ("false".equalsIgnoreCase(System.getProperty("com.sun.browser.plugin"))) {
            for (PluginHandler pluginHandler : PluginManager.getAvailablePlugins()) {
                disabledPluginHandlers.add(pluginHandler.getClass().getCanonicalName());
            }
        }
        PluginManager.updatePluginHandlers();
    }
}

