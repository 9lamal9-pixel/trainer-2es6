/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import com.sun.javafx.scene.layout.RegionHelper;
import com.sun.javafx.util.Utils;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;

public class ControlHelper
extends RegionHelper {
    private static final ControlHelper theInstance = new ControlHelper();
    private static ControlAccessor controlAccessor;

    private static ControlHelper getInstance() {
        return theInstance;
    }

    public static void initHelper(Control control) {
        ControlHelper.setHelper(control, ControlHelper.getInstance());
    }

    public static void superProcessCSS(Node node) {
        ((ControlHelper)ControlHelper.getHelper(node)).superProcessCSSImpl(node);
    }

    public static StringProperty skinClassNameProperty(Control control) {
        return controlAccessor.skinClassNameProperty(control);
    }

    void superProcessCSSImpl(Node node) {
        super.processCSSImpl(node);
    }

    @Override
    protected void processCSSImpl(Node node) {
        controlAccessor.doProcessCSS(node);
    }

    public static void setControlAccessor(ControlAccessor controlAccessor) {
        if (ControlHelper.controlAccessor != null) {
            throw new IllegalStateException();
        }
        ControlHelper.controlAccessor = controlAccessor;
    }

    static {
        Utils.forceInit(Control.class);
    }

    public static interface ControlAccessor {
        public void doProcessCSS(Node var1);

        public StringProperty skinClassNameProperty(Control var1);
    }
}

