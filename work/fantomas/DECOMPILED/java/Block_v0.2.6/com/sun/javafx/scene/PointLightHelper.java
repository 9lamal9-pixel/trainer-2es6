/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene;

import com.sun.javafx.scene.LightBaseHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.util.Utils;
import javafx.scene.Node;
import javafx.scene.PointLight;

public class PointLightHelper
extends LightBaseHelper {
    private static final PointLightHelper theInstance = new PointLightHelper();
    private static PointLightAccessor pointLightAccessor;

    private static PointLightHelper getInstance() {
        return theInstance;
    }

    public static void initHelper(PointLight pointLight) {
        PointLightHelper.setHelper(pointLight, PointLightHelper.getInstance());
    }

    @Override
    protected NGNode createPeerImpl(Node node) {
        return pointLightAccessor.doCreatePeer(node);
    }

    public static void setPointLightAccessor(PointLightAccessor pointLightAccessor) {
        if (PointLightHelper.pointLightAccessor != null) {
            throw new IllegalStateException();
        }
        PointLightHelper.pointLightAccessor = pointLightAccessor;
    }

    static {
        Utils.forceInit(PointLight.class);
    }

    public static interface PointLightAccessor {
        public NGNode doCreatePeer(Node var1);
    }
}

