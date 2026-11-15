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
import javafx.geometry.HPos;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.layout.Priority;

public class ColumnConstraints
extends ConstraintsBase {
    private DoubleProperty minWidth;
    private DoubleProperty prefWidth;
    private DoubleProperty maxWidth;
    private DoubleProperty percentWidth;
    private ObjectProperty<Priority> hgrow;
    private ObjectProperty<HPos> halignment;
    private BooleanProperty fillWidth;

    public ColumnConstraints() {
    }

    public ColumnConstraints(double d) {
        this();
        this.setMinWidth(Double.NEGATIVE_INFINITY);
        this.setPrefWidth(d);
        this.setMaxWidth(Double.NEGATIVE_INFINITY);
    }

    public ColumnConstraints(double d, double d2, double d3) {
        this();
        this.setMinWidth(d);
        this.setPrefWidth(d2);
        this.setMaxWidth(d3);
    }

    public ColumnConstraints(double d, double d2, double d3, Priority priority, HPos hPos, boolean bl) {
        this(d, d2, d3);
        this.setHgrow(priority);
        this.setHalignment(hPos);
        this.setFillWidth(bl);
    }

    public final void setMinWidth(double d) {
        this.minWidthProperty().set(d);
    }

    public final double getMinWidth() {
        return this.minWidth == null ? -1.0 : this.minWidth.get();
    }

    public final DoubleProperty minWidthProperty() {
        if (this.minWidth == null) {
            this.minWidth = new DoublePropertyBase(-1.0){

                @Override
                protected void invalidated() {
                    ColumnConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return ColumnConstraints.this;
                }

                @Override
                public String getName() {
                    return "minWidth";
                }
            };
        }
        return this.minWidth;
    }

    public final void setPrefWidth(double d) {
        this.prefWidthProperty().set(d);
    }

    public final double getPrefWidth() {
        return this.prefWidth == null ? -1.0 : this.prefWidth.get();
    }

    public final DoubleProperty prefWidthProperty() {
        if (this.prefWidth == null) {
            this.prefWidth = new DoublePropertyBase(-1.0){

                @Override
                protected void invalidated() {
                    ColumnConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return ColumnConstraints.this;
                }

                @Override
                public String getName() {
                    return "prefWidth";
                }
            };
        }
        return this.prefWidth;
    }

    public final void setMaxWidth(double d) {
        this.maxWidthProperty().set(d);
    }

    public final double getMaxWidth() {
        return this.maxWidth == null ? -1.0 : this.maxWidth.get();
    }

    public final DoubleProperty maxWidthProperty() {
        if (this.maxWidth == null) {
            this.maxWidth = new DoublePropertyBase(-1.0){

                @Override
                protected void invalidated() {
                    ColumnConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return ColumnConstraints.this;
                }

                @Override
                public String getName() {
                    return "maxWidth";
                }
            };
        }
        return this.maxWidth;
    }

    public final void setPercentWidth(double d) {
        this.percentWidthProperty().set(d);
    }

    public final double getPercentWidth() {
        return this.percentWidth == null ? -1.0 : this.percentWidth.get();
    }

    public final DoubleProperty percentWidthProperty() {
        if (this.percentWidth == null) {
            this.percentWidth = new DoublePropertyBase(-1.0){

                @Override
                protected void invalidated() {
                    ColumnConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return ColumnConstraints.this;
                }

                @Override
                public String getName() {
                    return "percentWidth";
                }
            };
        }
        return this.percentWidth;
    }

    public final void setHgrow(Priority priority) {
        this.hgrowProperty().set(priority);
    }

    public final Priority getHgrow() {
        return this.hgrow == null ? null : (Priority)((Object)this.hgrow.get());
    }

    public final ObjectProperty<Priority> hgrowProperty() {
        if (this.hgrow == null) {
            this.hgrow = new ObjectPropertyBase<Priority>(){

                @Override
                protected void invalidated() {
                    ColumnConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return ColumnConstraints.this;
                }

                @Override
                public String getName() {
                    return "hgrow";
                }
            };
        }
        return this.hgrow;
    }

    public final void setHalignment(HPos hPos) {
        this.halignmentProperty().set(hPos);
    }

    public final HPos getHalignment() {
        return this.halignment == null ? null : (HPos)((Object)this.halignment.get());
    }

    public final ObjectProperty<HPos> halignmentProperty() {
        if (this.halignment == null) {
            this.halignment = new ObjectPropertyBase<HPos>(){

                @Override
                protected void invalidated() {
                    ColumnConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return ColumnConstraints.this;
                }

                @Override
                public String getName() {
                    return "halignment";
                }
            };
        }
        return this.halignment;
    }

    public final void setFillWidth(boolean bl) {
        this.fillWidthProperty().set(bl);
    }

    public final boolean isFillWidth() {
        return this.fillWidth == null ? true : this.fillWidth.get();
    }

    public final BooleanProperty fillWidthProperty() {
        if (this.fillWidth == null) {
            this.fillWidth = new BooleanPropertyBase(true){

                @Override
                protected void invalidated() {
                    ColumnConstraints.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return ColumnConstraints.this;
                }

                @Override
                public String getName() {
                    return "fillWidth";
                }
            };
        }
        return this.fillWidth;
    }

    public String toString() {
        return "ColumnConstraints percentWidth=" + this.getPercentWidth() + " minWidth=" + this.getMinWidth() + " prefWidth=" + this.getPrefWidth() + " maxWidth=" + this.getMaxWidth() + " hgrow=" + this.getHgrow() + " fillWidth=" + this.isFillWidth() + " halignment=" + this.getHalignment();
    }
}

