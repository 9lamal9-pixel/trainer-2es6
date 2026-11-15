/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.chart;

import com.sun.javafx.scene.NodeHelper;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.FontCssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public abstract class Axis<T>
extends Region {
    Text measure = new Text();
    private Orientation effectiveOrientation;
    private double effectiveTickLabelRotation = Double.NaN;
    private Label axisLabel = new Label();
    private final Path tickMarkPath = new Path();
    private double oldLength = 0.0;
    boolean rangeValid = false;
    boolean measureInvalid = false;
    boolean tickLabelsVisibleInvalid = false;
    private BitSet labelsToSkip = new BitSet();
    private final ObservableList<TickMark<T>> tickMarks = FXCollections.observableArrayList();
    private final ObservableList<TickMark<T>> unmodifiableTickMarks = FXCollections.unmodifiableObservableList(this.tickMarks);
    private ObjectProperty<Side> side = new StyleableObjectProperty<Side>(){

        @Override
        protected void invalidated() {
            Side side = (Side)((Object)this.get());
            Axis.this.pseudoClassStateChanged(TOP_PSEUDOCLASS_STATE, side == Side.TOP);
            Axis.this.pseudoClassStateChanged(RIGHT_PSEUDOCLASS_STATE, side == Side.RIGHT);
            Axis.this.pseudoClassStateChanged(BOTTOM_PSEUDOCLASS_STATE, side == Side.BOTTOM);
            Axis.this.pseudoClassStateChanged(LEFT_PSEUDOCLASS_STATE, side == Side.LEFT);
            Axis.this.requestAxisLayout();
        }

        @Override
        public CssMetaData<Axis<?>, Side> getCssMetaData() {
            return StyleableProperties.SIDE;
        }

        @Override
        public Object getBean() {
            return Axis.this;
        }

        @Override
        public String getName() {
            return "side";
        }
    };
    private ObjectProperty<String> label = new ObjectPropertyBase<String>(){

        @Override
        protected void invalidated() {
            Axis.this.axisLabel.setText((String)this.get());
            Axis.this.requestAxisLayout();
        }

        @Override
        public Object getBean() {
            return Axis.this;
        }

        @Override
        public String getName() {
            return "label";
        }
    };
    private BooleanProperty tickMarkVisible = new StyleableBooleanProperty(true){

        @Override
        protected void invalidated() {
            Axis.this.tickMarkPath.setVisible(this.get());
            Axis.this.requestAxisLayout();
        }

        @Override
        public CssMetaData<Axis<?>, Boolean> getCssMetaData() {
            return StyleableProperties.TICK_MARK_VISIBLE;
        }

        @Override
        public Object getBean() {
            return Axis.this;
        }

        @Override
        public String getName() {
            return "tickMarkVisible";
        }
    };
    private BooleanProperty tickLabelsVisible = new StyleableBooleanProperty(true){

        @Override
        protected void invalidated() {
            for (TickMark tickMark : Axis.this.tickMarks) {
                tickMark.setTextVisible(this.get());
            }
            Axis.this.tickLabelsVisibleInvalid = true;
            Axis.this.requestAxisLayout();
        }

        @Override
        public CssMetaData<Axis<?>, Boolean> getCssMetaData() {
            return StyleableProperties.TICK_LABELS_VISIBLE;
        }

        @Override
        public Object getBean() {
            return Axis.this;
        }

        @Override
        public String getName() {
            return "tickLabelsVisible";
        }
    };
    private DoubleProperty tickLength = new StyleableDoubleProperty(8.0){

        @Override
        protected void invalidated() {
            if (Axis.this.tickLength.get() < 0.0 && !Axis.this.tickLength.isBound()) {
                Axis.this.tickLength.set(0.0);
            }
            Axis.this.requestAxisLayout();
        }

        @Override
        public CssMetaData<Axis<?>, Number> getCssMetaData() {
            return StyleableProperties.TICK_LENGTH;
        }

        @Override
        public Object getBean() {
            return Axis.this;
        }

        @Override
        public String getName() {
            return "tickLength";
        }
    };
    private BooleanProperty autoRanging = new BooleanPropertyBase(true){

        @Override
        protected void invalidated() {
            if (this.get()) {
                Axis.this.requestAxisLayout();
            }
        }

        @Override
        public Object getBean() {
            return Axis.this;
        }

        @Override
        public String getName() {
            return "autoRanging";
        }
    };
    private ObjectProperty<Font> tickLabelFont = new StyleableObjectProperty<Font>(Font.font("System", 8.0)){

        @Override
        protected void invalidated() {
            Font font = (Font)this.get();
            Axis.this.measure.setFont(font);
            for (TickMark tickMark : Axis.this.getTickMarks()) {
                tickMark.textNode.setFont(font);
            }
            Axis.this.measureInvalid = true;
            Axis.this.requestAxisLayout();
        }

        @Override
        public CssMetaData<Axis<?>, Font> getCssMetaData() {
            return StyleableProperties.TICK_LABEL_FONT;
        }

        @Override
        public Object getBean() {
            return Axis.this;
        }

        @Override
        public String getName() {
            return "tickLabelFont";
        }
    };
    private ObjectProperty<Paint> tickLabelFill = new StyleableObjectProperty<Paint>((Paint)Color.BLACK){

        @Override
        protected void invalidated() {
            for (TickMark tickMark : Axis.this.tickMarks) {
                tickMark.textNode.setFill(Axis.this.getTickLabelFill());
            }
        }

        @Override
        public CssMetaData<Axis<?>, Paint> getCssMetaData() {
            return StyleableProperties.TICK_LABEL_FILL;
        }

        @Override
        public Object getBean() {
            return Axis.this;
        }

        @Override
        public String getName() {
            return "tickLabelFill";
        }
    };
    private DoubleProperty tickLabelGap = new StyleableDoubleProperty(3.0){

        @Override
        protected void invalidated() {
            Axis.this.requestAxisLayout();
        }

        @Override
        public CssMetaData<Axis<?>, Number> getCssMetaData() {
            return StyleableProperties.TICK_LABEL_TICK_GAP;
        }

        @Override
        public Object getBean() {
            return Axis.this;
        }

        @Override
        public String getName() {
            return "tickLabelGap";
        }
    };
    private BooleanProperty animated = new SimpleBooleanProperty(this, "animated", true);
    private DoubleProperty tickLabelRotation = new DoublePropertyBase(0.0){

        @Override
        protected void invalidated() {
            if (Axis.this.isAutoRanging()) {
                Axis.this.invalidateRange();
            }
            Axis.this.requestAxisLayout();
        }

        @Override
        public Object getBean() {
            return Axis.this;
        }

        @Override
        public String getName() {
            return "tickLabelRotation";
        }
    };
    private static final PseudoClass TOP_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("top");
    private static final PseudoClass BOTTOM_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("bottom");
    private static final PseudoClass LEFT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("left");
    private static final PseudoClass RIGHT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("right");

    public ObservableList<TickMark<T>> getTickMarks() {
        return this.unmodifiableTickMarks;
    }

    public final Side getSide() {
        return (Side)((Object)this.side.get());
    }

    public final void setSide(Side side) {
        this.side.set(side);
    }

    public final ObjectProperty<Side> sideProperty() {
        return this.side;
    }

    final void setEffectiveOrientation(Orientation orientation) {
        this.effectiveOrientation = orientation;
    }

    final Side getEffectiveSide() {
        Side side = this.getSide();
        if (side == null || side.isVertical() && this.effectiveOrientation == Orientation.HORIZONTAL || side.isHorizontal() && this.effectiveOrientation == Orientation.VERTICAL) {
            return this.effectiveOrientation == Orientation.VERTICAL ? Side.LEFT : Side.BOTTOM;
        }
        return side;
    }

    public final String getLabel() {
        return (String)this.label.get();
    }

    public final void setLabel(String string) {
        this.label.set(string);
    }

    public final ObjectProperty<String> labelProperty() {
        return this.label;
    }

    public final boolean isTickMarkVisible() {
        return this.tickMarkVisible.get();
    }

    public final void setTickMarkVisible(boolean bl) {
        this.tickMarkVisible.set(bl);
    }

    public final BooleanProperty tickMarkVisibleProperty() {
        return this.tickMarkVisible;
    }

    public final boolean isTickLabelsVisible() {
        return this.tickLabelsVisible.get();
    }

    public final void setTickLabelsVisible(boolean bl) {
        this.tickLabelsVisible.set(bl);
    }

    public final BooleanProperty tickLabelsVisibleProperty() {
        return this.tickLabelsVisible;
    }

    public final double getTickLength() {
        return this.tickLength.get();
    }

    public final void setTickLength(double d) {
        this.tickLength.set(d);
    }

    public final DoubleProperty tickLengthProperty() {
        return this.tickLength;
    }

    public final boolean isAutoRanging() {
        return this.autoRanging.get();
    }

    public final void setAutoRanging(boolean bl) {
        this.autoRanging.set(bl);
    }

    public final BooleanProperty autoRangingProperty() {
        return this.autoRanging;
    }

    public final Font getTickLabelFont() {
        return (Font)this.tickLabelFont.get();
    }

    public final void setTickLabelFont(Font font) {
        this.tickLabelFont.set(font);
    }

    public final ObjectProperty<Font> tickLabelFontProperty() {
        return this.tickLabelFont;
    }

    public final Paint getTickLabelFill() {
        return (Paint)this.tickLabelFill.get();
    }

    public final void setTickLabelFill(Paint paint) {
        this.tickLabelFill.set(paint);
    }

    public final ObjectProperty<Paint> tickLabelFillProperty() {
        return this.tickLabelFill;
    }

    public final double getTickLabelGap() {
        return this.tickLabelGap.get();
    }

    public final void setTickLabelGap(double d) {
        this.tickLabelGap.set(d);
    }

    public final DoubleProperty tickLabelGapProperty() {
        return this.tickLabelGap;
    }

    public final boolean getAnimated() {
        return this.animated.get();
    }

    public final void setAnimated(boolean bl) {
        this.animated.set(bl);
    }

    public final BooleanProperty animatedProperty() {
        return this.animated;
    }

    public final double getTickLabelRotation() {
        return this.tickLabelRotation.getValue();
    }

    public final void setTickLabelRotation(double d) {
        this.tickLabelRotation.setValue(d);
    }

    public final DoubleProperty tickLabelRotationProperty() {
        return this.tickLabelRotation;
    }

    public Axis() {
        this.getStyleClass().setAll((String[])new String[]{"axis"});
        this.axisLabel.getStyleClass().add("axis-label");
        this.axisLabel.setAlignment(Pos.CENTER);
        this.tickMarkPath.getStyleClass().add("axis-tick-mark");
        this.getChildren().addAll((Node[])new Node[]{this.axisLabel, this.tickMarkPath});
    }

    protected final boolean isRangeValid() {
        return this.rangeValid;
    }

    protected final void invalidateRange() {
        this.rangeValid = false;
    }

    protected final boolean shouldAnimate() {
        return this.getAnimated() && NodeHelper.isTreeShowing(this);
    }

    @Override
    public void requestLayout() {
    }

    public void requestAxisLayout() {
        super.requestLayout();
    }

    public void invalidateRange(List<T> list) {
        this.invalidateRange();
        this.requestAxisLayout();
    }

    protected abstract Object autoRange(double var1);

    protected abstract void setRange(Object var1, boolean var2);

    protected abstract Object getRange();

    public abstract double getZeroPosition();

    public abstract double getDisplayPosition(T var1);

    public abstract T getValueForDisplay(double var1);

    public abstract boolean isValueOnAxis(T var1);

    public abstract double toNumericValue(T var1);

    public abstract T toRealValue(double var1);

    protected abstract List<T> calculateTickValues(double var1, Object var3);

    @Override
    protected double computePrefHeight(double d) {
        Side side = this.getEffectiveSide();
        if (side.isVertical()) {
            return 100.0;
        }
        Object object = this.autoRange(d);
        double d2 = 0.0;
        if (this.isTickLabelsVisible()) {
            List<T> list = this.calculateTickValues(d, object);
            for (T t : list) {
                d2 = Math.max(d2, this.measureTickMarkSize(t, object).getHeight());
            }
        }
        double d3 = this.isTickMarkVisible() ? (this.getTickLength() > 0.0 ? this.getTickLength() : 0.0) : 0.0;
        double d4 = this.axisLabel.getText() == null || this.axisLabel.getText().length() == 0 ? 0.0 : this.axisLabel.prefHeight(-1.0);
        return d2 + this.getTickLabelGap() + d3 + d4;
    }

    @Override
    protected double computePrefWidth(double d) {
        Side side = this.getEffectiveSide();
        if (side.isVertical()) {
            Object object = this.autoRange(d);
            double d2 = 0.0;
            if (this.isTickLabelsVisible()) {
                List<T> list = this.calculateTickValues(d, object);
                for (T t : list) {
                    d2 = Math.max(d2, this.measureTickMarkSize(t, object).getWidth());
                }
            }
            double d3 = this.isTickMarkVisible() ? (this.getTickLength() > 0.0 ? this.getTickLength() : 0.0) : 0.0;
            double d4 = this.axisLabel.getText() == null || this.axisLabel.getText().length() == 0 ? 0.0 : this.axisLabel.prefHeight(-1.0);
            return d2 + this.getTickLabelGap() + d3 + d4;
        }
        return 100.0;
    }

    protected void tickMarksUpdated() {
    }

    @Override
    protected void layoutChildren() {
        boolean bl;
        boolean bl2 = this.oldLength == 0.0;
        Side side = this.getEffectiveSide();
        double d = side.isVertical() ? this.getHeight() : this.getWidth();
        boolean bl3 = !this.isRangeValid();
        boolean bl4 = bl = this.oldLength != d;
        if (bl || bl3) {
            Object object2;
            Object object3;
            if (this.isAutoRanging()) {
                object3 = this.autoRange(d);
                this.setRange(object3, this.getAnimated() && !bl2 && NodeHelper.isTreeShowing(this) && bl3);
            } else {
                object3 = this.getRange();
            }
            List<T> list = this.calculateTickValues(d, object3);
            Iterator iterator = this.tickMarks.iterator();
            while (iterator.hasNext()) {
                Object object4;
                Object object5 = object4 = (TickMark)iterator.next();
                if (this.shouldAnimate()) {
                    object2 = new FadeTransition(Duration.millis(250.0), ((TickMark)object4).textNode);
                    ((FadeTransition)object2).setToValue(0.0);
                    ((Animation)object2).setOnFinished(arg_0 -> this.lambda$layoutChildren$0((TickMark)object5, arg_0));
                    ((Animation)object2).play();
                } else {
                    this.getChildren().remove(((TickMark)object5).textNode);
                }
                iterator.remove();
            }
            for (Object object5 : list) {
                object2 = new TickMark();
                ((TickMark)object2).setValue(object5);
                ((TickMark)object2).textNode.setText(this.getTickMarkLabel(object5));
                ((TickMark)object2).textNode.setFont(this.getTickLabelFont());
                ((TickMark)object2).textNode.setFill(this.getTickLabelFill());
                ((TickMark)object2).setTextVisible(this.isTickLabelsVisible());
                if (this.shouldAnimate()) {
                    ((TickMark)object2).textNode.setOpacity(0.0);
                }
                this.getChildren().add(((TickMark)object2).textNode);
                this.tickMarks.add((TickMark<T>)object2);
                if (!this.shouldAnimate()) continue;
                FadeTransition object6 = new FadeTransition(Duration.millis(750.0), ((TickMark)object2).textNode);
                object6.setFromValue(0.0);
                object6.setToValue(1.0);
                object6.play();
            }
            this.tickMarksUpdated();
            this.oldLength = d;
            this.rangeValid = true;
        }
        if (bl || bl3 || this.measureInvalid || this.tickLabelsVisibleInvalid) {
            this.measureInvalid = false;
            this.tickLabelsVisibleInvalid = false;
            this.labelsToSkip.clear();
            int n = 0;
            double d2 = 0.0;
            double d3 = 0.0;
            for (TickMark tickMark : this.tickMarks) {
                tickMark.setPosition(this.getDisplayPosition(tickMark.getValue()));
                if (!tickMark.isTextVisible()) continue;
                double d4 = this.measureTickMarkSize(tickMark.getValue(), side);
                d2 += d4;
                d3 = Math.round(Math.max(d3, d4));
            }
            if (d3 > 0.0 && d < d2) {
                n = (int)((double)this.tickMarks.size() * d3 / d) + 1;
            }
            if (n > 0) {
                int n2 = 0;
                for (TickMark tickMark : this.tickMarks) {
                    if (!tickMark.isTextVisible()) continue;
                    tickMark.setTextVisible(n2++ % n == 0);
                }
            }
            if (this.tickMarks.size() > 2) {
                TickMark tickMark;
                TickMark tickMark2;
                TickMark tickMark3 = (TickMark)this.tickMarks.get(0);
                if (this.isTickLabelsOverlap(side, tickMark3, tickMark2 = (TickMark)this.tickMarks.get(1), this.getTickLabelGap())) {
                    tickMark2.setTextVisible(false);
                }
                if (this.isTickLabelsOverlap(side, tickMark3 = (TickMark)this.tickMarks.get(this.tickMarks.size() - 2), tickMark = (TickMark)this.tickMarks.get(this.tickMarks.size() - 1), this.getTickLabelGap())) {
                    tickMark3.setTextVisible(false);
                }
            }
            this.updateTickMarks(side, d);
        }
    }

    private void updateTickMarks(Side side, double d) {
        this.tickMarkPath.getElements().clear();
        double d2 = this.getWidth();
        double d3 = this.getHeight();
        double d4 = this.isTickMarkVisible() && this.getTickLength() > 0.0 ? this.getTickLength() : 0.0;
        double d5 = this.getEffectiveTickLabelRotation();
        if (Side.LEFT.equals((Object)side)) {
            this.tickMarkPath.setLayoutX(-0.5);
            this.tickMarkPath.setLayoutY(0.5);
            if (this.getLabel() != null) {
                this.axisLabel.getTransforms().setAll((Transform[])new Transform[]{new Translate(0.0, d3), new Rotate(-90.0, 0.0, 0.0)});
                this.axisLabel.setLayoutX(0.0);
                this.axisLabel.setLayoutY(0.0);
                this.axisLabel.resize(d3, Math.ceil(this.axisLabel.prefHeight(d2)));
            }
            for (TickMark tickMark : this.tickMarks) {
                this.positionTextNode(tickMark.textNode, d2 - this.getTickLabelGap() - d4, tickMark.getPosition(), d5, side);
                this.updateTickMark(tickMark, d, d2 - d4, tickMark.getPosition(), d2, tickMark.getPosition());
            }
        } else if (Side.RIGHT.equals((Object)side)) {
            this.tickMarkPath.setLayoutX(0.5);
            this.tickMarkPath.setLayoutY(0.5);
            if (this.getLabel() != null) {
                double d6 = Math.ceil(this.axisLabel.prefHeight(d2));
                this.axisLabel.getTransforms().setAll((Transform[])new Transform[]{new Translate(0.0, d3), new Rotate(-90.0, 0.0, 0.0)});
                this.axisLabel.setLayoutX(d2 - d6);
                this.axisLabel.setLayoutY(0.0);
                this.axisLabel.resize(d3, d6);
            }
            for (TickMark tickMark : this.tickMarks) {
                this.positionTextNode(tickMark.textNode, this.getTickLabelGap() + d4, tickMark.getPosition(), d5, side);
                this.updateTickMark(tickMark, d, 0.0, tickMark.getPosition(), d4, tickMark.getPosition());
            }
        } else if (Side.TOP.equals((Object)side)) {
            this.tickMarkPath.setLayoutX(0.5);
            this.tickMarkPath.setLayoutY(-0.5);
            if (this.getLabel() != null) {
                this.axisLabel.getTransforms().clear();
                this.axisLabel.setLayoutX(0.0);
                this.axisLabel.setLayoutY(0.0);
                this.axisLabel.resize(d2, Math.ceil(this.axisLabel.prefHeight(d2)));
            }
            for (TickMark tickMark : this.tickMarks) {
                this.positionTextNode(tickMark.textNode, tickMark.getPosition(), d3 - d4 - this.getTickLabelGap(), d5, side);
                this.updateTickMark(tickMark, d, tickMark.getPosition(), d3, tickMark.getPosition(), d3 - d4);
            }
        } else {
            this.tickMarkPath.setLayoutX(0.5);
            this.tickMarkPath.setLayoutY(0.5);
            if (this.getLabel() != null) {
                this.axisLabel.getTransforms().clear();
                double d7 = Math.ceil(this.axisLabel.prefHeight(d2));
                this.axisLabel.setLayoutX(0.0);
                this.axisLabel.setLayoutY(d3 - d7);
                this.axisLabel.resize(d2, d7);
            }
            for (TickMark tickMark : this.tickMarks) {
                this.positionTextNode(tickMark.textNode, tickMark.getPosition(), d4 + this.getTickLabelGap(), d5, side);
                this.updateTickMark(tickMark, d, tickMark.getPosition(), 0.0, tickMark.getPosition(), d4);
            }
        }
    }

    private boolean isTickLabelsOverlap(Side side, TickMark<T> tickMark, TickMark<T> tickMark2, double d) {
        if (!tickMark.isTextVisible() || !tickMark2.isTextVisible()) {
            return false;
        }
        double d2 = this.measureTickMarkSize(tickMark.getValue(), side);
        double d3 = this.measureTickMarkSize(tickMark2.getValue(), side);
        double d4 = tickMark.getPosition() - d2 / 2.0;
        double d5 = tickMark.getPosition() + d2 / 2.0;
        double d6 = tickMark2.getPosition() - d3 / 2.0;
        double d7 = tickMark2.getPosition() + d3 / 2.0;
        return side.isVertical() ? d4 - d7 <= d : d6 - d5 <= d;
    }

    private void positionTextNode(Text text, double d, double d2, double d3, Side side) {
        text.setLayoutX(0.0);
        text.setLayoutY(0.0);
        text.setRotate(d3);
        Bounds bounds = text.getBoundsInParent();
        if (Side.LEFT.equals((Object)side)) {
            text.setLayoutX(d - bounds.getWidth() - bounds.getMinX());
            text.setLayoutY(d2 - bounds.getHeight() / 2.0 - bounds.getMinY());
        } else if (Side.RIGHT.equals((Object)side)) {
            text.setLayoutX(d - bounds.getMinX());
            text.setLayoutY(d2 - bounds.getHeight() / 2.0 - bounds.getMinY());
        } else if (Side.TOP.equals((Object)side)) {
            text.setLayoutX(d - bounds.getWidth() / 2.0 - bounds.getMinX());
            text.setLayoutY(d2 - bounds.getHeight() - bounds.getMinY());
        } else {
            text.setLayoutX(d - bounds.getWidth() / 2.0 - bounds.getMinX());
            text.setLayoutY(d2 - bounds.getMinY());
        }
    }

    private void updateTickMark(TickMark<T> tickMark, double d, double d2, double d3, double d4, double d5) {
        if (tickMark.getPosition() >= 0.0 && tickMark.getPosition() <= Math.ceil(d)) {
            tickMark.textNode.setVisible(tickMark.isTextVisible());
            this.tickMarkPath.getElements().addAll((PathElement[])new PathElement[]{new MoveTo(d2, d3), new LineTo(d4, d5)});
        } else {
            tickMark.textNode.setVisible(false);
        }
    }

    protected abstract String getTickMarkLabel(T var1);

    protected final Dimension2D measureTickMarkLabelSize(String string, double d) {
        this.measure.setRotate(d);
        this.measure.setText(string);
        Bounds bounds = this.measure.getBoundsInParent();
        return new Dimension2D(bounds.getWidth(), bounds.getHeight());
    }

    protected final Dimension2D measureTickMarkSize(T t, double d) {
        return this.measureTickMarkLabelSize(this.getTickMarkLabel(t), d);
    }

    protected Dimension2D measureTickMarkSize(T t, Object object) {
        return this.measureTickMarkSize(t, this.getEffectiveTickLabelRotation());
    }

    private double measureTickMarkSize(T t, Side side) {
        Dimension2D dimension2D = this.measureTickMarkSize(t, this.getEffectiveTickLabelRotation());
        return side.isVertical() ? dimension2D.getHeight() : dimension2D.getWidth();
    }

    final double getEffectiveTickLabelRotation() {
        return !this.isAutoRanging() || Double.isNaN(this.effectiveTickLabelRotation) ? this.getTickLabelRotation() : this.effectiveTickLabelRotation;
    }

    final void setEffectiveTickLabelRotation(double d) {
        this.effectiveTickLabelRotation = d;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return Axis.getClassCssMetaData();
    }

    private /* synthetic */ void lambda$layoutChildren$0(TickMark tickMark, ActionEvent actionEvent) {
        this.getChildren().remove(tickMark.textNode);
    }

    private static class StyleableProperties {
        private static final CssMetaData<Axis<?>, Side> SIDE = new CssMetaData<Axis<?>, Side>("-fx-side", new EnumConverter<Side>(Side.class)){

            @Override
            public boolean isSettable(Axis<?> axis) {
                return ((Axis)axis).side == null || !((Axis)axis).side.isBound();
            }

            @Override
            public StyleableProperty<Side> getStyleableProperty(Axis<?> axis) {
                return (StyleableProperty)((Object)axis.sideProperty());
            }
        };
        private static final CssMetaData<Axis<?>, Number> TICK_LENGTH = new CssMetaData<Axis<?>, Number>("-fx-tick-length", SizeConverter.getInstance(), 8.0){

            @Override
            public boolean isSettable(Axis<?> axis) {
                return ((Axis)axis).tickLength == null || !((Axis)axis).tickLength.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Axis<?> axis) {
                return (StyleableProperty)((Object)axis.tickLengthProperty());
            }
        };
        private static final CssMetaData<Axis<?>, Font> TICK_LABEL_FONT = new FontCssMetaData<Axis<?>>("-fx-tick-label-font", Font.font("system", 8.0)){

            @Override
            public boolean isSettable(Axis<?> axis) {
                return ((Axis)axis).tickLabelFont == null || !((Axis)axis).tickLabelFont.isBound();
            }

            @Override
            public StyleableProperty<Font> getStyleableProperty(Axis<?> axis) {
                return (StyleableProperty)((Object)axis.tickLabelFontProperty());
            }
        };
        private static final CssMetaData<Axis<?>, Paint> TICK_LABEL_FILL = new CssMetaData<Axis<?>, Paint>("-fx-tick-label-fill", PaintConverter.getInstance(), Color.BLACK){

            @Override
            public boolean isSettable(Axis<?> axis) {
                return ((Axis)axis).tickLabelFill == null | !((Axis)axis).tickLabelFill.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(Axis<?> axis) {
                return (StyleableProperty)((Object)axis.tickLabelFillProperty());
            }
        };
        private static final CssMetaData<Axis<?>, Number> TICK_LABEL_TICK_GAP = new CssMetaData<Axis<?>, Number>("-fx-tick-label-gap", SizeConverter.getInstance(), 3.0){

            @Override
            public boolean isSettable(Axis<?> axis) {
                return ((Axis)axis).tickLabelGap == null || !((Axis)axis).tickLabelGap.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Axis<?> axis) {
                return (StyleableProperty)((Object)axis.tickLabelGapProperty());
            }
        };
        private static final CssMetaData<Axis<?>, Boolean> TICK_MARK_VISIBLE = new CssMetaData<Axis<?>, Boolean>("-fx-tick-mark-visible", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(Axis<?> axis) {
                return ((Axis)axis).tickMarkVisible == null || !((Axis)axis).tickMarkVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Axis<?> axis) {
                return (StyleableProperty)((Object)axis.tickMarkVisibleProperty());
            }
        };
        private static final CssMetaData<Axis<?>, Boolean> TICK_LABELS_VISIBLE = new CssMetaData<Axis<?>, Boolean>("-fx-tick-labels-visible", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(Axis<?> axis) {
                return ((Axis)axis).tickLabelsVisible == null || !((Axis)axis).tickLabelsVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Axis<?> axis) {
                return (StyleableProperty)((Object)axis.tickLabelsVisibleProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Region.getClassCssMetaData());
            arrayList.add(SIDE);
            arrayList.add(TICK_LENGTH);
            arrayList.add(TICK_LABEL_FONT);
            arrayList.add(TICK_LABEL_FILL);
            arrayList.add(TICK_LABEL_TICK_GAP);
            arrayList.add(TICK_MARK_VISIBLE);
            arrayList.add(TICK_LABELS_VISIBLE);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }

    public static final class TickMark<T> {
        private StringProperty label = new StringPropertyBase(){

            @Override
            protected void invalidated() {
                textNode.setText(this.getValue());
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "label";
            }
        };
        private ObjectProperty<T> value = new SimpleObjectProperty(this, "value");
        private DoubleProperty position = new SimpleDoubleProperty(this, "position");
        Text textNode = new Text();
        private BooleanProperty textVisible = new BooleanPropertyBase(true){

            @Override
            protected void invalidated() {
                if (!this.get()) {
                    textNode.setVisible(false);
                }
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "textVisible";
            }
        };

        public final String getLabel() {
            return (String)this.label.get();
        }

        public final void setLabel(String string) {
            this.label.set(string);
        }

        public final StringExpression labelProperty() {
            return this.label;
        }

        public final T getValue() {
            return this.value.get();
        }

        public final void setValue(T t) {
            this.value.set(t);
        }

        public final ObjectExpression<T> valueProperty() {
            return this.value;
        }

        public final double getPosition() {
            return this.position.get();
        }

        public final void setPosition(double d) {
            this.position.set(d);
        }

        public final DoubleExpression positionProperty() {
            return this.position;
        }

        public final boolean isTextVisible() {
            return this.textVisible.get();
        }

        public final void setTextVisible(boolean bl) {
            this.textVisible.set(bl);
        }

        public String toString() {
            return this.value.get().toString();
        }
    }
}

