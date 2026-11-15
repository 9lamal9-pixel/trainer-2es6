/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.StringConverter;

public abstract class ValueAxis<T extends Number>
extends Axis<T> {
    private final Path minorTickPath = new Path();
    private double offset;
    double dataMinValue;
    double dataMaxValue;
    private List<T> minorTickMarkValues = null;
    private boolean minorTickMarksDirty = true;
    protected final DoubleProperty currentLowerBound = new SimpleDoubleProperty(this, "currentLowerBound");
    private BooleanProperty minorTickVisible = new StyleableBooleanProperty(true){

        @Override
        protected void invalidated() {
            ValueAxis.this.minorTickPath.setVisible(this.get());
            ValueAxis.this.requestAxisLayout();
        }

        @Override
        public Object getBean() {
            return ValueAxis.this;
        }

        @Override
        public String getName() {
            return "minorTickVisible";
        }

        @Override
        public CssMetaData<ValueAxis<? extends Number>, Boolean> getCssMetaData() {
            return StyleableProperties.MINOR_TICK_VISIBLE;
        }
    };
    private ReadOnlyDoubleWrapper scale = new ReadOnlyDoubleWrapper(this, "scale", 0.0){

        @Override
        protected void invalidated() {
            ValueAxis.this.requestAxisLayout();
            ValueAxis.this.measureInvalid = true;
        }
    };
    private DoubleProperty upperBound = new DoublePropertyBase(100.0){

        @Override
        protected void invalidated() {
            if (!ValueAxis.this.isAutoRanging()) {
                ValueAxis.this.invalidateRange();
                ValueAxis.this.requestAxisLayout();
            }
        }

        @Override
        public Object getBean() {
            return ValueAxis.this;
        }

        @Override
        public String getName() {
            return "upperBound";
        }
    };
    private DoubleProperty lowerBound = new DoublePropertyBase(0.0){

        @Override
        protected void invalidated() {
            if (!ValueAxis.this.isAutoRanging()) {
                ValueAxis.this.invalidateRange();
                ValueAxis.this.requestAxisLayout();
            }
        }

        @Override
        public Object getBean() {
            return ValueAxis.this;
        }

        @Override
        public String getName() {
            return "lowerBound";
        }
    };
    private final ObjectProperty<StringConverter<T>> tickLabelFormatter = new ObjectPropertyBase<StringConverter<T>>(null){

        @Override
        protected void invalidated() {
            ValueAxis.this.invalidateRange();
            ValueAxis.this.requestAxisLayout();
        }

        @Override
        public Object getBean() {
            return ValueAxis.this;
        }

        @Override
        public String getName() {
            return "tickLabelFormatter";
        }
    };
    private DoubleProperty minorTickLength = new StyleableDoubleProperty(5.0){

        @Override
        protected void invalidated() {
            ValueAxis.this.requestAxisLayout();
        }

        @Override
        public Object getBean() {
            return ValueAxis.this;
        }

        @Override
        public String getName() {
            return "minorTickLength";
        }

        @Override
        public CssMetaData<ValueAxis<? extends Number>, Number> getCssMetaData() {
            return StyleableProperties.MINOR_TICK_LENGTH;
        }
    };
    private IntegerProperty minorTickCount = new StyleableIntegerProperty(5){

        @Override
        protected void invalidated() {
            ValueAxis.this.invalidateRange();
            ValueAxis.this.requestAxisLayout();
        }

        @Override
        public Object getBean() {
            return ValueAxis.this;
        }

        @Override
        public String getName() {
            return "minorTickCount";
        }

        @Override
        public CssMetaData<ValueAxis<? extends Number>, Number> getCssMetaData() {
            return StyleableProperties.MINOR_TICK_COUNT;
        }
    };

    public final boolean isMinorTickVisible() {
        return this.minorTickVisible.get();
    }

    public final void setMinorTickVisible(boolean bl) {
        this.minorTickVisible.set(bl);
    }

    public final BooleanProperty minorTickVisibleProperty() {
        return this.minorTickVisible;
    }

    public final double getScale() {
        return this.scale.get();
    }

    protected final void setScale(double d) {
        this.scale.set(d);
    }

    public final ReadOnlyDoubleProperty scaleProperty() {
        return this.scale.getReadOnlyProperty();
    }

    ReadOnlyDoubleWrapper scalePropertyImpl() {
        return this.scale;
    }

    public final double getUpperBound() {
        return this.upperBound.get();
    }

    public final void setUpperBound(double d) {
        this.upperBound.set(d);
    }

    public final DoubleProperty upperBoundProperty() {
        return this.upperBound;
    }

    public final double getLowerBound() {
        return this.lowerBound.get();
    }

    public final void setLowerBound(double d) {
        this.lowerBound.set(d);
    }

    public final DoubleProperty lowerBoundProperty() {
        return this.lowerBound;
    }

    public final StringConverter<T> getTickLabelFormatter() {
        return (StringConverter)this.tickLabelFormatter.getValue();
    }

    public final void setTickLabelFormatter(StringConverter<T> stringConverter) {
        this.tickLabelFormatter.setValue(stringConverter);
    }

    public final ObjectProperty<StringConverter<T>> tickLabelFormatterProperty() {
        return this.tickLabelFormatter;
    }

    public final double getMinorTickLength() {
        return this.minorTickLength.get();
    }

    public final void setMinorTickLength(double d) {
        this.minorTickLength.set(d);
    }

    public final DoubleProperty minorTickLengthProperty() {
        return this.minorTickLength;
    }

    public final int getMinorTickCount() {
        return this.minorTickCount.get();
    }

    public final void setMinorTickCount(int n) {
        this.minorTickCount.set(n);
    }

    public final IntegerProperty minorTickCountProperty() {
        return this.minorTickCount;
    }

    public ValueAxis() {
        this.minorTickPath.getStyleClass().add("axis-minor-tick-mark");
        this.getChildren().add(this.minorTickPath);
    }

    public ValueAxis(double d, double d2) {
        this();
        this.setAutoRanging(false);
        this.setLowerBound(d);
        this.setUpperBound(d2);
    }

    @Override
    protected final Object autoRange(double d) {
        if (this.isAutoRanging()) {
            double d2 = this.getTickLabelFont().getSize() * 2.0;
            return this.autoRange(this.dataMinValue, this.dataMaxValue, d, d2);
        }
        return this.getRange();
    }

    protected final double calculateNewScale(double d, double d2, double d3) {
        double d4 = 1.0;
        Side side = this.getEffectiveSide();
        if (side.isVertical()) {
            this.offset = d;
            d4 = d3 - d2 == 0.0 ? -d : -(d / (d3 - d2));
        } else {
            this.offset = 0.0;
            d4 = d3 - d2 == 0.0 ? d : d / (d3 - d2);
        }
        return d4;
    }

    protected Object autoRange(double d, double d2, double d3, double d4) {
        return null;
    }

    protected abstract List<T> calculateMinorTickMarks();

    @Override
    protected void tickMarksUpdated() {
        super.tickMarksUpdated();
        this.minorTickMarkValues = this.calculateMinorTickMarks();
        this.minorTickMarksDirty = true;
    }

    @Override
    protected void layoutChildren() {
        double d;
        Side side = this.getEffectiveSide();
        double d2 = d = side.isVertical() ? this.getHeight() : this.getWidth();
        if (!this.isAutoRanging()) {
            this.setScale(this.calculateNewScale(d, this.getLowerBound(), this.getUpperBound()));
            this.currentLowerBound.set(this.getLowerBound());
        }
        super.layoutChildren();
        if (this.minorTickMarksDirty) {
            this.minorTickMarksDirty = false;
            this.updateMinorTickPath(side, d);
        }
    }

    private void updateMinorTickPath(Side side, double d) {
        block10: {
            int n = (this.getTickMarks().size() - 1) * (Math.max(1, this.getMinorTickCount()) - 1);
            double d2 = (this.getTickMarks().size() + n) * 2;
            this.minorTickPath.getElements().clear();
            double d3 = Math.max(0.0, this.getMinorTickLength());
            if (!(d3 > 0.0) || !(d > d2)) break block10;
            if (Side.LEFT.equals((Object)side)) {
                this.minorTickPath.setLayoutX(-0.5);
                this.minorTickPath.setLayoutY(0.5);
                for (Number number : this.minorTickMarkValues) {
                    double d4 = this.getDisplayPosition((T)number);
                    if (!(d4 >= 0.0) || !(d4 <= d)) continue;
                    this.minorTickPath.getElements().addAll((PathElement[])new PathElement[]{new MoveTo(this.getWidth() - d3, d4), new LineTo(this.getWidth() - 1.0, d4)});
                }
            } else if (Side.RIGHT.equals((Object)side)) {
                this.minorTickPath.setLayoutX(0.5);
                this.minorTickPath.setLayoutY(0.5);
                for (Number number : this.minorTickMarkValues) {
                    double d5 = this.getDisplayPosition((T)number);
                    if (!(d5 >= 0.0) || !(d5 <= d)) continue;
                    this.minorTickPath.getElements().addAll((PathElement[])new PathElement[]{new MoveTo(1.0, d5), new LineTo(d3, d5)});
                }
            } else if (Side.TOP.equals((Object)side)) {
                this.minorTickPath.setLayoutX(0.5);
                this.minorTickPath.setLayoutY(-0.5);
                for (Number number : this.minorTickMarkValues) {
                    double d6 = this.getDisplayPosition((T)number);
                    if (!(d6 >= 0.0) || !(d6 <= d)) continue;
                    this.minorTickPath.getElements().addAll((PathElement[])new PathElement[]{new MoveTo(d6, this.getHeight() - 1.0), new LineTo(d6, this.getHeight() - d3)});
                }
            } else {
                this.minorTickPath.setLayoutX(0.5);
                this.minorTickPath.setLayoutY(0.5);
                for (Number number : this.minorTickMarkValues) {
                    double d7 = this.getDisplayPosition((T)number);
                    if (!(d7 >= 0.0) || !(d7 <= d)) continue;
                    this.minorTickPath.getElements().addAll((PathElement[])new PathElement[]{new MoveTo(d7, 1.0), new LineTo(d7, d3)});
                }
            }
        }
    }

    @Override
    public void invalidateRange(List<T> list) {
        if (list.isEmpty()) {
            this.dataMaxValue = this.getUpperBound();
            this.dataMinValue = this.getLowerBound();
        } else {
            this.dataMinValue = Double.MAX_VALUE;
            this.dataMaxValue = -1.7976931348623157E308;
        }
        for (Number number : list) {
            this.dataMinValue = Math.min(this.dataMinValue, number.doubleValue());
            this.dataMaxValue = Math.max(this.dataMaxValue, number.doubleValue());
        }
        super.invalidateRange(list);
    }

    @Override
    public double getDisplayPosition(T t) {
        return this.offset + (((Number)t).doubleValue() - this.currentLowerBound.get()) * this.getScale();
    }

    @Override
    public T getValueForDisplay(double d) {
        return (T)this.toRealValue((d - this.offset) / this.getScale() + this.currentLowerBound.get());
    }

    @Override
    public double getZeroPosition() {
        if (0.0 < this.getLowerBound() || 0.0 > this.getUpperBound()) {
            return Double.NaN;
        }
        return this.getDisplayPosition(0.0);
    }

    @Override
    public boolean isValueOnAxis(T t) {
        double d = ((Number)t).doubleValue();
        return d >= this.getLowerBound() && d <= this.getUpperBound();
    }

    @Override
    public double toNumericValue(T t) {
        return t == null ? Double.NaN : ((Number)t).doubleValue();
    }

    @Override
    public T toRealValue(double d) {
        return (T)new Double(d);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return ValueAxis.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<ValueAxis<? extends Number>, Number> MINOR_TICK_LENGTH = new CssMetaData<ValueAxis<? extends Number>, Number>("-fx-minor-tick-length", SizeConverter.getInstance(), (Number)5.0){

            @Override
            public boolean isSettable(ValueAxis<? extends Number> valueAxis) {
                return ((ValueAxis)valueAxis).minorTickLength == null || !((ValueAxis)valueAxis).minorTickLength.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(ValueAxis<? extends Number> valueAxis) {
                return (StyleableProperty)((Object)valueAxis.minorTickLengthProperty());
            }
        };
        private static final CssMetaData<ValueAxis<? extends Number>, Number> MINOR_TICK_COUNT = new CssMetaData<ValueAxis<? extends Number>, Number>("-fx-minor-tick-count", SizeConverter.getInstance(), (Number)5){

            @Override
            public boolean isSettable(ValueAxis<? extends Number> valueAxis) {
                return ((ValueAxis)valueAxis).minorTickCount == null || !((ValueAxis)valueAxis).minorTickCount.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(ValueAxis<? extends Number> valueAxis) {
                return (StyleableProperty)((Object)valueAxis.minorTickCountProperty());
            }
        };
        private static final CssMetaData<ValueAxis<? extends Number>, Boolean> MINOR_TICK_VISIBLE = new CssMetaData<ValueAxis<? extends Number>, Boolean>("-fx-minor-tick-visible", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(ValueAxis<? extends Number> valueAxis) {
                return ((ValueAxis)valueAxis).minorTickVisible == null || !((ValueAxis)valueAxis).minorTickVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(ValueAxis<? extends Number> valueAxis) {
                return (StyleableProperty)((Object)valueAxis.minorTickVisibleProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Axis.getClassCssMetaData());
            arrayList.add(MINOR_TICK_COUNT);
            arrayList.add(MINOR_TICK_LENGTH);
            arrayList.add(MINOR_TICK_COUNT);
            arrayList.add(MINOR_TICK_VISIBLE);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

