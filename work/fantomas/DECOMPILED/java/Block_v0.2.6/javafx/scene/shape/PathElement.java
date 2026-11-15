/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.scene.shape.PathElementHelper;
import com.sun.javafx.sg.prism.NGPath;
import com.sun.javafx.util.WeakReferenceQueue;
import java.util.Iterator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.scene.Node;
import javafx.scene.shape.Path;

public abstract class PathElement {
    private PathElementHelper pathElementHelper = null;
    WeakReferenceQueue nodes = new WeakReferenceQueue();
    private BooleanProperty absolute;

    void addNode(Node node) {
        this.nodes.add(node);
    }

    void removeNode(Node node) {
        this.nodes.remove(node);
    }

    void u() {
        Iterator iterator = this.nodes.iterator();
        while (iterator.hasNext()) {
            ((Path)iterator.next()).markPathDirty();
        }
    }

    abstract void addTo(NGPath var1);

    public final void setAbsolute(boolean bl) {
        this.absoluteProperty().set(bl);
    }

    public final boolean isAbsolute() {
        return this.absolute == null || this.absolute.get();
    }

    public final BooleanProperty absoluteProperty() {
        if (this.absolute == null) {
            this.absolute = new BooleanPropertyBase(true){

                @Override
                protected void invalidated() {
                    PathElement.this.u();
                }

                @Override
                public Object getBean() {
                    return PathElement.this;
                }

                @Override
                public String getName() {
                    return "absolute";
                }
            };
        }
        return this.absolute;
    }

    static {
        PathElementHelper.setPathElementAccessor(new PathElementHelper.PathElementAccessor(){

            @Override
            public PathElementHelper getHelper(PathElement pathElement) {
                return pathElement.pathElementHelper;
            }

            @Override
            public void setHelper(PathElement pathElement, PathElementHelper pathElementHelper) {
                pathElement.pathElementHelper = pathElementHelper;
            }
        });
    }
}

