/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.scene.PointLightHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGPointLight;
import javafx.scene.LightBase;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public class PointLight
extends LightBase {
    public PointLight() {
        PointLightHelper.initHelper(this);
    }

    public PointLight(Color color) {
        super(color);
        PointLightHelper.initHelper(this);
    }

    private NGNode doCreatePeer() {
        return new NGPointLight();
    }

    static {
        PointLightHelper.setPointLightAccessor(new PointLightHelper.PointLightAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((PointLight)node).doCreatePeer();
            }
        });
    }
}

