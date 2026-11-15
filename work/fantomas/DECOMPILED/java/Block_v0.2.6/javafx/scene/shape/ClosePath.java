/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.scene.shape.ClosePathHelper;
import com.sun.javafx.sg.prism.NGPath;
import javafx.scene.shape.PathElement;

public class ClosePath
extends PathElement {
    public ClosePath() {
        ClosePathHelper.initHelper(this);
    }

    @Override
    void addTo(NGPath nGPath) {
        nGPath.addClosePath();
    }

    private void doAddTo(Path2D path2D) {
        path2D.closePath();
    }

    public String toString() {
        return "ClosePath";
    }

    static {
        ClosePathHelper.setClosePathAccessor(new ClosePathHelper.ClosePathAccessor(){

            @Override
            public void doAddTo(PathElement pathElement, Path2D path2D) {
                ((ClosePath)pathElement).doAddTo(path2D);
            }
        });
    }
}

