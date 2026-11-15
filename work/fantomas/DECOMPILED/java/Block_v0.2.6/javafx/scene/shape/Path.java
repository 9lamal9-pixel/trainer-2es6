/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.geom.Path2D;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.shape.PathElementHelper;
import com.sun.javafx.scene.shape.PathHelper;
import com.sun.javafx.scene.shape.PathUtils;
import com.sun.javafx.scene.shape.ShapeHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGPath;
import java.util.Collection;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.StyleableProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Shape;

public class Path
extends Shape {
    private Path2D path2d = null;
    private ObjectProperty<FillRule> fillRule;
    private boolean isPathValid;
    private final ObservableList<PathElement> elements;

    public Path() {
        PathHelper.initHelper(this);
        ((StyleableProperty)((Object)this.fillProperty())).applyStyle(null, null);
        ((StyleableProperty)((Object)this.strokeProperty())).applyStyle(null, Color.BLACK);
        this.elements = new TrackableObservableList<PathElement>(){

            @Override
            protected void onChanged(ListChangeListener.Change<PathElement> change) {
                ObservableList<PathElement> observableList = change.getList();
                boolean bl = false;
                while (change.next()) {
                    int n;
                    List<PathElement> list = change.getRemoved();
                    for (n = 0; n < change.getRemovedSize(); ++n) {
                        list.get(n).removeNode(Path.this);
                    }
                    for (n = change.getFrom(); n < change.getTo(); ++n) {
                        ((PathElement)observableList.get(n)).addNode(Path.this);
                    }
                    bl |= change.getFrom() == 0;
                }
                if (Path.this.path2d != null) {
                    change.reset();
                    change.next();
                    if (change.getFrom() == change.getList().size() && !change.wasRemoved() && change.wasAdded()) {
                        for (int i = change.getFrom(); i < change.getTo(); ++i) {
                            PathElementHelper.addTo((PathElement)observableList.get(i), Path.this.path2d);
                        }
                    } else {
                        Path.this.path2d = null;
                    }
                }
                if (bl) {
                    Path.this.isPathValid = Path.this.isFirstPathElementValid();
                }
                NodeHelper.markDirty(Path.this, DirtyBits.NODE_CONTENTS);
                NodeHelper.geomChanged(Path.this);
            }
        };
    }

    public Path(PathElement ... pathElementArray) {
        PathHelper.initHelper(this);
        ((StyleableProperty)((Object)this.fillProperty())).applyStyle(null, null);
        ((StyleableProperty)((Object)this.strokeProperty())).applyStyle(null, Color.BLACK);
        this.elements = new /* invalid duplicate definition of identical inner class */;
        if (pathElementArray != null) {
            this.elements.addAll((PathElement[])pathElementArray);
        }
    }

    public Path(Collection<? extends PathElement> collection) {
        PathHelper.initHelper(this);
        ((StyleableProperty)((Object)this.fillProperty())).applyStyle(null, null);
        ((StyleableProperty)((Object)this.strokeProperty())).applyStyle(null, Color.BLACK);
        this.elements = new /* invalid duplicate definition of identical inner class */;
        if (collection != null) {
            this.elements.addAll(collection);
        }
    }

    void markPathDirty() {
        this.path2d = null;
        NodeHelper.markDirty(this, DirtyBits.NODE_CONTENTS);
        NodeHelper.geomChanged(this);
    }

    public final void setFillRule(FillRule fillRule) {
        if (this.fillRule != null || fillRule != FillRule.NON_ZERO) {
            this.fillRuleProperty().set(fillRule);
        }
    }

    public final FillRule getFillRule() {
        return this.fillRule == null ? FillRule.NON_ZERO : (FillRule)((Object)this.fillRule.get());
    }

    public final ObjectProperty<FillRule> fillRuleProperty() {
        if (this.fillRule == null) {
            this.fillRule = new ObjectPropertyBase<FillRule>(FillRule.NON_ZERO){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Path.this, DirtyBits.NODE_CONTENTS);
                    NodeHelper.geomChanged(Path.this);
                }

                @Override
                public Object getBean() {
                    return Path.this;
                }

                @Override
                public String getName() {
                    return "fillRule";
                }
            };
        }
        return this.fillRule;
    }

    public final ObservableList<PathElement> getElements() {
        return this.elements;
    }

    private NGNode doCreatePeer() {
        return new NGPath();
    }

    private Path2D doConfigShape() {
        if (this.isPathValid) {
            if (this.path2d == null) {
                this.path2d = PathUtils.configShape(this.getElements(), this.getFillRule() == FillRule.EVEN_ODD);
            } else {
                this.path2d.setWindingRule(this.getFillRule() == FillRule.NON_ZERO ? 1 : 0);
            }
            return this.path2d;
        }
        return new Path2D();
    }

    private Bounds doComputeLayoutBounds() {
        if (this.isPathValid) {
            return null;
        }
        return new BoundingBox(0.0, 0.0, -1.0, -1.0);
    }

    private boolean isFirstPathElementValid() {
        ObservableList<PathElement> observableList = this.getElements();
        if (observableList != null && observableList.size() > 0) {
            PathElement pathElement = (PathElement)observableList.get(0);
            if (!pathElement.isAbsolute()) {
                System.err.printf("First element of the path can not be relative. Path: %s\n", this);
                return false;
            }
            if (pathElement instanceof MoveTo) {
                return true;
            }
            System.err.printf("Missing initial moveto in path definition. Path: %s\n", this);
            return false;
        }
        return true;
    }

    private void doUpdatePeer() {
        if (NodeHelper.isDirty(this, DirtyBits.NODE_CONTENTS)) {
            NGPath nGPath = (NGPath)NodeHelper.getPeer(this);
            if (nGPath.acceptsPath2dOnUpdate()) {
                nGPath.updateWithPath2d((Path2D)ShapeHelper.configShape(this));
            } else {
                nGPath.reset();
                if (this.isPathValid) {
                    nGPath.setFillRule(this.getFillRule());
                    for (PathElement pathElement : this.getElements()) {
                        pathElement.addTo(nGPath);
                    }
                    nGPath.update();
                }
            }
        }
    }

    private Paint doCssGetFillInitialValue() {
        return null;
    }

    private Paint doCssGetStrokeInitialValue() {
        return Color.BLACK;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Path[");
        String string = this.getId();
        if (string != null) {
            stringBuilder.append("id=").append(string).append(", ");
        }
        stringBuilder.append("elements=").append(this.getElements());
        stringBuilder.append(", fill=").append(this.getFill());
        stringBuilder.append(", fillRule=").append((Object)this.getFillRule());
        Paint paint = this.getStroke();
        if (paint != null) {
            stringBuilder.append(", stroke=").append(paint);
            stringBuilder.append(", strokeWidth=").append(this.getStrokeWidth());
        }
        return stringBuilder.append("]").toString();
    }

    static {
        PathHelper.setPathAccessor(new PathHelper.PathAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Path)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Path)node).doUpdatePeer();
            }

            @Override
            public Bounds doComputeLayoutBounds(Node node) {
                return ((Path)node).doComputeLayoutBounds();
            }

            @Override
            public Paint doCssGetFillInitialValue(Shape shape) {
                return ((Path)shape).doCssGetFillInitialValue();
            }

            @Override
            public Paint doCssGetStrokeInitialValue(Shape shape) {
                return ((Path)shape).doCssGetStrokeInitialValue();
            }

            @Override
            public com.sun.javafx.geom.Shape doConfigShape(Shape shape) {
                return ((Path)shape).doConfigShape();
            }
        });
    }
}

