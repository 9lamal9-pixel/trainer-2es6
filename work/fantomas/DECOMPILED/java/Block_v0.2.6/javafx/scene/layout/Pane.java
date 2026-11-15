/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import com.sun.javafx.scene.layout.PaneHelper;
import javafx.beans.DefaultProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Region;

@DefaultProperty(value="children")
public class Pane
extends Region {
    static void setConstraint(Node node, Object object, Object object2) {
        if (object2 == null) {
            node.getProperties().remove(object);
        } else {
            node.getProperties().put(object, object2);
        }
        if (node.getParent() != null) {
            node.getParent().requestLayout();
        }
    }

    static Object getConstraint(Node node, Object object) {
        Object v;
        if (node.hasProperties() && (v = node.getProperties().get(object)) != null) {
            return v;
        }
        return null;
    }

    public Pane() {
        PaneHelper.initHelper(this);
    }

    public Pane(Node ... nodeArray) {
        PaneHelper.initHelper(this);
        this.getChildren().addAll((Node[])nodeArray);
    }

    @Override
    public ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    static {
        PaneHelper.setPaneAccessor(new PaneHelper.PaneAccessor(){});
    }
}

