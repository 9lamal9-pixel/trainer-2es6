/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.scene.AmbientLightHelper;
import com.sun.javafx.sg.prism.NGAmbientLight;
import com.sun.javafx.sg.prism.NGNode;
import javafx.scene.LightBase;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public class AmbientLight
extends LightBase {
    public AmbientLight() {
        AmbientLightHelper.initHelper(this);
    }

    public AmbientLight(Color color) {
        super(color);
        AmbientLightHelper.initHelper(this);
    }

    private NGNode doCreatePeer() {
        return new NGAmbientLight();
    }

    static {
        AmbientLightHelper.setAmbientLightAccessor(new AmbientLightHelper.AmbientLightAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((AmbientLight)node).doCreatePeer();
            }
        });
    }
}

