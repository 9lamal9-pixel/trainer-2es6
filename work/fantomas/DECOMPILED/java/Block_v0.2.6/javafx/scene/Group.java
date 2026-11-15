/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.scene.GroupHelper;
import java.util.Collection;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;

@DefaultProperty(value="children")
public class Group
extends Parent {
    private BooleanProperty autoSizeChildren;

    public Group() {
        GroupHelper.initHelper(this);
    }

    public Group(Node ... nodeArray) {
        GroupHelper.initHelper(this);
        this.getChildren().addAll((Node[])nodeArray);
    }

    public Group(Collection<Node> collection) {
        GroupHelper.initHelper(this);
        this.getChildren().addAll(collection);
    }

    public final void setAutoSizeChildren(boolean bl) {
        this.autoSizeChildrenProperty().set(bl);
    }

    public final boolean isAutoSizeChildren() {
        return this.autoSizeChildren == null ? true : this.autoSizeChildren.get();
    }

    public final BooleanProperty autoSizeChildrenProperty() {
        if (this.autoSizeChildren == null) {
            this.autoSizeChildren = new BooleanPropertyBase(true){

                @Override
                protected void invalidated() {
                    Group.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return Group.this;
                }

                @Override
                public String getName() {
                    return "autoSizeChildren";
                }
            };
        }
        return this.autoSizeChildren;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    private Bounds doComputeLayoutBounds() {
        this.layout();
        return null;
    }

    @Override
    public double prefWidth(double d) {
        double d2;
        if (this.isAutoSizeChildren()) {
            this.layout();
        }
        return Double.isNaN(d2 = this.getLayoutBounds().getWidth()) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public double prefHeight(double d) {
        double d2;
        if (this.isAutoSizeChildren()) {
            this.layout();
        }
        return Double.isNaN(d2 = this.getLayoutBounds().getHeight()) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public double minHeight(double d) {
        return this.prefHeight(d);
    }

    @Override
    public double minWidth(double d) {
        return this.prefWidth(d);
    }

    @Override
    protected void layoutChildren() {
        if (this.isAutoSizeChildren()) {
            super.layoutChildren();
        }
    }

    static {
        GroupHelper.setGroupAccessor(new GroupHelper.GroupAccessor(){

            @Override
            public Bounds doComputeLayoutBounds(Node node) {
                return ((Group)node).doComputeLayoutBounds();
            }
        });
    }
}

