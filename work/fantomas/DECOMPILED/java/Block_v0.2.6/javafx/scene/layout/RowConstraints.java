/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.VPos;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.layout.Priority;

public class RowConstraints
extends ConstraintsBase {
    private DoubleProperty minHeight;
    private DoubleProperty prefHeight;
    private DoubleProperty maxHeight;
    private DoubleProperty percentHeight;
    private ObjectProperty<Priority> vgrow;
    private ObjectProperty<VPos> valignment;
    private BooleanProperty fillHeight;

    public RowConstraints() {
    }

    public RowConstraints(double d) {
        this();
        this.setMinHeight(Double.NEGATIVE_INFINITY);
        this.setPrefHeight(d);
        this.setMaxHeight(Double.NEGATIVE_INFINITY);
    }

    public RowConstraints(double d, double d2, double d3) {
        this();
        this.setMinHeight(d);
        this.setPrefHeight(d2);
        this.setMaxHeight(d3);
    }

    public RowConstraints(double d, double d2, double d3, Priority priority, VPos vPos, boolean bl) {
        this(d, d2, d3);
        this.setVgrow(priority);
        this.setValignment(vPos);
        this.setFillHeight(bl);
    }

    public final void setMinHeight(double d) {
        this.minHeightProperty().set(d);
    }

    public final double getMinHeight() {
        return this.minHeight == null ? -1.0 : this.minHeight.get();
    }

    public final DoubleProperty minHeightProperty() {
        if (this.minHeight == null) {
            this.minHeight = new DoublePropertyBase(-1.0){

                @Override
                protected void invalidated() {
                    RowConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return RowConstraints.this;
                }

                @Override
                public String getName() {
                    return "minHeight";
                }
            };
        }
        return this.minHeight;
    }

    public final void setPrefHeight(double d) {
        this.prefHeightProperty().set(d);
    }

    public final double getPrefHeight() {
        return this.prefHeight == null ? -1.0 : this.prefHeight.get();
    }

    public final DoubleProperty prefHeightProperty() {
        if (this.prefHeight == null) {
            this.prefHeight = new DoublePropertyBase(-1.0){

                @Override
                protected void invalidated() {
                    RowConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return RowConstraints.this;
                }

                @Override
                public String getName() {
                    return "prefHeight";
                }
            };
        }
        return this.prefHeight;
    }

    public final void setMaxHeight(double d) {
        this.maxHeightProperty().set(d);
    }

    public final double getMaxHeight() {
        return this.maxHeight == null ? -1.0 : this.maxHeight.get();
    }

    public final DoubleProperty maxHeightProperty() {
        if (this.maxHeight == null) {
            this.maxHeight = new DoublePropertyBase(-1.0){

                @Override
                protected void invalidated() {
                    RowConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return RowConstraints.this;
                }

                @Override
                public String getName() {
                    return "maxHeight";
                }
            };
        }
        return this.maxHeight;
    }

    public final void setPercentHeight(double d) {
        this.percentHeightProperty().set(d);
    }

    public final double getPercentHeight() {
        return this.percentHeight == null ? -1.0 : this.percentHeight.get();
    }

    public final DoubleProperty percentHeightProperty() {
        if (this.percentHeight == null) {
            this.percentHeight = new DoublePropertyBase(-1.0){

                @Override
                protected void invalidated() {
                    RowConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return RowConstraints.this;
                }

                @Override
                public String getName() {
                    return "percentHeight";
                }
            };
        }
        return this.percentHeight;
    }

    public final void setVgrow(Priority priority) {
        this.vgrowProperty().set(priority);
    }

    public final Priority getVgrow() {
        return this.vgrow == null ? null : (Priority)((Object)this.vgrow.get());
    }

    public final ObjectProperty<Priority> vgrowProperty() {
        if (this.vgrow == null) {
            this.vgrow = new ObjectPropertyBase<Priority>(){

                @Override
                protected void invalidated() {
                    RowConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return RowConstraints.this;
                }

                @Override
                public String getName() {
                    return "vgrow";
                }
            };
        }
        return this.vgrow;
    }

    public final void setValignment(VPos vPos) {
        this.valignmentProperty().set(vPos);
    }

    public final VPos getValignment() {
        return this.valignment == null ? null : (VPos)((Object)this.valignment.get());
    }

    public final ObjectProperty<VPos> valignmentProperty() {
        if (this.valignment == null) {
            this.valignment = new ObjectPropertyBase<VPos>(){

                @Override
                protected void invalidated() {
                    RowConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return RowConstraints.this;
                }

                @Override
                public String getName() {
                    return "valignment";
                }
            };
        }
        return this.valignment;
    }

    public final void setFillHeight(boolean bl) {
        this.fillHeightProperty().set(bl);
    }

    public final boolean isFillHeight() {
        return this.fillHeight == null ? true : this.fillHeight.get();
    }

    public final BooleanProperty fillHeightProperty() {
        if (this.fillHeight == null) {
            this.fillHeight = new BooleanPropertyBase(true){

                @Override
                protected void invalidated() {
                    RowConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return RowConstraints.this;
                }

                @Override
                public String getName() {
                    return "fillHeight";
                }
            };
        }
        return this.fillHeight;
    }

    public String toString() {
        return "RowConstraints percentHeight=" + this.getPercentHeight() + " minHeight=" + this.getMinHeight() + " prefHeight=" + this.getPrefHeight() + " maxHeight=" + this.getMaxHeight() + " vgrow=" + this.getVgrow() + " fillHeight=" + this.isFillHeight() + " valignment=" + this.getValignment();
    }
}

