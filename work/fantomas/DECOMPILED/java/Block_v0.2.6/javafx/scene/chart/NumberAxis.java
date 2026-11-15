/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.chart;

import com.sun.javafx.charts.ChartLayoutAnimator;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Dimension2D;
import javafx.geometry.Side;
import javafx.scene.chart.ValueAxis;
import javafx.util.Duration;
import javafx.util.StringConverter;

public final class NumberAxis
extends ValueAxis<Number> {
    private Object currentAnimationID;
    private final ChartLayoutAnimator animator = new ChartLayoutAnimator(this);
    private final StringProperty currentFormatterProperty = new SimpleStringProperty(this, "currentFormatter", "");
    private final DefaultFormatter defaultFormatter = new DefaultFormatter(this);
    private BooleanProperty forceZeroInRange = new BooleanPropertyBase(true){

        @Override
        protected void invalidated() {
            if (NumberAxis.this.isAutoRanging()) {
                NumberAxis.this.requestAxisLayout();
                NumberAxis.this.invalidateRange();
            }
        }

        @Override
        public Object getBean() {
            return NumberAxis.this;
        }

        @Override
        public String getName() {
            return "forceZeroInRange";
        }
    };
    private DoubleProperty tickUnit = new StyleableDoubleProperty(5.0){

        @Override
        protected void invalidated() {
            if (!NumberAxis.this.isAutoRanging()) {
                NumberAxis.this.invalidateRange();
                NumberAxis.this.requestAxisLayout();
            }
        }

        @Override
        public CssMetaData<NumberAxis, Number> getCssMetaData() {
            return StyleableProperties.TICK_UNIT;
        }

        @Override
        public Object getBean() {
            return NumberAxis.this;
        }

        @Override
        public String getName() {
            return "tickUnit";
        }
    };

    public final boolean isForceZeroInRange() {
        return this.forceZeroInRange.getValue();
    }

    public final void setForceZeroInRange(boolean bl) {
        this.forceZeroInRange.setValue(bl);
    }

    public final BooleanProperty forceZeroInRangeProperty() {
        return this.forceZeroInRange;
    }

    public final double getTickUnit() {
        return this.tickUnit.get();
    }

    public final void setTickUnit(double d) {
        this.tickUnit.set(d);
    }

    public final DoubleProperty tickUnitProperty() {
        return this.tickUnit;
    }

    public NumberAxis() {
    }

    public NumberAxis(double d, double d2, double d3) {
        super(d, d2);
        this.setTickUnit(d3);
    }

    public NumberAxis(String string, double d, double d2, double d3) {
        super(d, d2);
        this.setTickUnit(d3);
        this.setLabel(string);
    }

    @Override
    protected String getTickMarkLabel(Number number) {
        DefaultFormatter defaultFormatter = this.getTickLabelFormatter();
        if (defaultFormatter == null) {
            defaultFormatter = this.defaultFormatter;
        }
        return ((StringConverter)defaultFormatter).toString(number);
    }

    @Override
    protected Object getRange() {
        return new Object[]{this.getLowerBound(), this.getUpperBound(), this.getTickUnit(), this.getScale(), this.currentFormatterProperty.get()};
    }

    @Override
    protected void setRange(Object object, boolean bl) {
        Object[] objectArray = (Object[])object;
        double d = (Double)objectArray[0];
        double d2 = (Double)objectArray[1];
        double d3 = (Double)objectArray[2];
        double d4 = (Double)objectArray[3];
        String string = (String)objectArray[4];
        this.currentFormatterProperty.set(string);
        double d5 = this.getLowerBound();
        this.setLowerBound(d);
        this.setUpperBound(d2);
        this.setTickUnit(d3);
        if (bl) {
            this.animator.stop(this.currentAnimationID);
            this.currentAnimationID = this.animator.animate(new KeyFrame(Duration.ZERO, new KeyValue(this.currentLowerBound, d5), new KeyValue(this.scalePropertyImpl(), this.getScale())), new KeyFrame(Duration.millis(700.0), new KeyValue(this.currentLowerBound, d), new KeyValue(this.scalePropertyImpl(), d4)));
        } else {
            this.currentLowerBound.set(d);
            this.setScale(d4);
        }
    }

    @Override
    protected List<Number> calculateTickValues(double d, Object object) {
        Object[] objectArray = (Object[])object;
        double d2 = (Double)objectArray[0];
        double d3 = (Double)objectArray[1];
        double d4 = (Double)objectArray[2];
        ArrayList<Number> arrayList = new ArrayList<Number>();
        if (d2 == d3) {
            arrayList.add(d2);
        } else if (d4 <= 0.0) {
            arrayList.add(d2);
            arrayList.add(d3);
        } else if (d4 > 0.0) {
            arrayList.add(d2);
            if ((d3 - d2) / d4 > 2000.0) {
                System.err.println("Warning we tried to create more than 2000 major tick marks on a NumberAxis. Lower Bound=" + d2 + ", Upper Bound=" + d3 + ", Tick Unit=" + d4);
            } else if (d2 + d4 < d3) {
                double d5 = Math.rint(d4) == d4 ? Math.ceil(d2) : d2 + d4;
                int n = (int)Math.ceil((d3 - d5) / d4);
                for (int i = 0; d5 < d3 && i < n; d5 += d4, ++i) {
                    if (arrayList.contains(d5)) continue;
                    arrayList.add(d5);
                }
            }
            arrayList.add(d3);
        }
        return arrayList;
    }

    @Override
    protected List<Number> calculateMinorTickMarks() {
        ArrayList<Number> arrayList = new ArrayList<Number>();
        double d = this.getLowerBound();
        double d2 = this.getUpperBound();
        double d3 = this.getTickUnit();
        double d4 = d3 / (double)Math.max(1, this.getMinorTickCount());
        if (d3 > 0.0) {
            int n;
            int n2;
            double d5;
            boolean bl;
            if ((d2 - d) / d4 > 10000.0) {
                System.err.println("Warning we tried to create more than 10000 minor tick marks on a NumberAxis. Lower Bound=" + this.getLowerBound() + ", Upper Bound=" + this.getUpperBound() + ", Tick Unit=" + d3);
                return arrayList;
            }
            boolean bl2 = bl = Math.rint(d3) == d3;
            if (bl) {
                d5 = Math.floor(d) + d4;
                n2 = (int)Math.ceil((Math.ceil(d) - d5) / d4);
                for (n = 0; d5 < Math.ceil(d) && n < n2; d5 += d4, ++n) {
                    if (!(d5 > d)) continue;
                    arrayList.add(d5);
                }
            }
            d5 = bl ? Math.ceil(d) : d;
            n2 = (int)Math.ceil((d2 - d5) / d3);
            for (n = 0; d5 < d2 && n < n2; d5 += d3, ++n) {
                double d6 = Math.min(d5 + d3, d2);
                double d7 = d5 + d4;
                int n3 = (int)Math.ceil((d6 - d7) / d4);
                for (int i = 0; d7 < d6 && i < n3; d7 += d4, ++i) {
                    arrayList.add(d7);
                }
            }
        }
        return arrayList;
    }

    @Override
    protected Dimension2D measureTickMarkSize(Number number, Object object) {
        Object[] objectArray = (Object[])object;
        String string = (String)objectArray[4];
        return this.measureTickMarkSize(number, this.getTickLabelRotation(), string);
    }

    private Dimension2D measureTickMarkSize(Number number, double d, String string) {
        DefaultFormatter defaultFormatter = this.getTickLabelFormatter();
        if (defaultFormatter == null) {
            defaultFormatter = this.defaultFormatter;
        }
        String string2 = defaultFormatter instanceof DefaultFormatter ? defaultFormatter.toString(number, string) : ((StringConverter)defaultFormatter).toString(number);
        return this.measureTickMarkLabelSize(string2, d);
    }

    @Override
    protected Object autoRange(double d, double d2, double d3, double d4) {
        Side side = this.getEffectiveSide();
        if (this.isForceZeroInRange()) {
            if (d2 < 0.0) {
                d2 = 0.0;
            } else if (d > 0.0) {
                d = 0.0;
            }
        }
        int n = (int)Math.floor(d3 / d4);
        n = Math.max(n, 2);
        int n2 = Math.max(this.getMinorTickCount(), 1);
        double d5 = d2 - d;
        if (d5 != 0.0 && d5 / (double)(n * n2) <= Math.ulp(d)) {
            d5 = 0.0;
        }
        double d6 = d5 == 0.0 ? (d == 0.0 ? 2.0 : Math.abs(d) * 0.02) : Math.abs(d5) * 1.02;
        double d7 = (d6 - d5) / 2.0;
        double d8 = d - d7;
        double d9 = d2 + d7;
        if (d8 < 0.0 && d >= 0.0 || d8 > 0.0 && d <= 0.0) {
            d8 = 0.0;
        }
        if (d9 < 0.0 && d2 >= 0.0 || d9 > 0.0 && d2 <= 0.0) {
            d9 = 0.0;
        }
        double d10 = d6 / (double)n;
        double d11 = 0.0;
        double d12 = 0.0;
        double d13 = 0.0;
        int n3 = 0;
        double d14 = Double.MAX_VALUE;
        String string = "0.00000000";
        while (d14 > d3 || n3 > 20) {
            double d15;
            int n4 = (int)Math.floor(Math.log10(d10));
            double d16 = d15 = d10 / Math.pow(10.0, n4);
            if (d15 > 5.0) {
                ++n4;
                d16 = 1.0;
            } else if (d15 > 1.0) {
                double d17 = d16 = d15 > 2.5 ? 5.0 : 2.5;
            }
            if (n4 > 1) {
                string = "#,##0";
            } else if (n4 == 1) {
                string = "0";
            } else {
                int n5;
                boolean bl = Math.rint(d16) != d16;
                StringBuilder stringBuilder = new StringBuilder("0");
                int n6 = n5 = bl ? Math.abs(n4) + 1 : Math.abs(n4);
                if (n5 > 0) {
                    stringBuilder.append(".");
                }
                for (int i = 0; i < n5; ++i) {
                    stringBuilder.append("0");
                }
                string = stringBuilder.toString();
            }
            d11 = d16 * Math.pow(10.0, n4);
            d12 = Math.floor(d8 / d11) * d11;
            d13 = Math.ceil(d9 / d11) * d11;
            double d18 = 0.0;
            double d19 = 0.0;
            n3 = (int)Math.ceil((d13 - d12) / d11);
            double d20 = d12;
            for (int i = 0; d20 <= d13 && i < n3; d20 += d11, ++i) {
                double d21;
                Dimension2D dimension2D = this.measureTickMarkSize(d20, this.getTickLabelRotation(), string);
                double d22 = d21 = side.isVertical() ? dimension2D.getHeight() : dimension2D.getWidth();
                if (i == 0) {
                    d19 = d21 / 2.0;
                    continue;
                }
                d18 = Math.max(d18, d19 + 6.0 + d21 / 2.0);
            }
            d14 = (double)(n3 - 1) * d18;
            d10 = d11;
            if (n == 2 && d14 > d3) break;
            if (!(d14 > d3) && n3 <= 20) continue;
            d10 *= 2.0;
        }
        double d23 = this.calculateNewScale(d3, d12, d13);
        return new Object[]{d12, d13, d11, d23, string};
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return NumberAxis.getClassCssMetaData();
    }

    public static class DefaultFormatter
    extends StringConverter<Number> {
        private DecimalFormat formatter;
        private String prefix = null;
        private String suffix = null;

        public DefaultFormatter(NumberAxis numberAxis) {
            this.formatter = numberAxis.isAutoRanging() ? new DecimalFormat((String)numberAxis.currentFormatterProperty.get()) : new DecimalFormat();
            ChangeListener<Object> changeListener = (observableValue, object, object2) -> {
                this.formatter = numberAxis.isAutoRanging() ? new DecimalFormat((String)numberAxis.currentFormatterProperty.get()) : new DecimalFormat();
            };
            numberAxis.currentFormatterProperty.addListener(changeListener);
            numberAxis.autoRangingProperty().addListener(changeListener);
        }

        public DefaultFormatter(NumberAxis numberAxis, String string, String string2) {
            this(numberAxis);
            this.prefix = string;
            this.suffix = string2;
        }

        @Override
        public String toString(Number number) {
            return this.toString(number, this.formatter);
        }

        private String toString(Number number, String string) {
            if (string == null || string.isEmpty()) {
                return this.toString(number, this.formatter);
            }
            return this.toString(number, new DecimalFormat(string));
        }

        private String toString(Number number, DecimalFormat decimalFormat) {
            if (this.prefix != null && this.suffix != null) {
                return this.prefix + decimalFormat.format(number) + this.suffix;
            }
            if (this.prefix != null) {
                return this.prefix + decimalFormat.format(number);
            }
            if (this.suffix != null) {
                return decimalFormat.format(number) + this.suffix;
            }
            return decimalFormat.format(number);
        }

        @Override
        public Number fromString(String string) {
            try {
                int n = this.prefix == null ? 0 : this.prefix.length();
                int n2 = this.suffix == null ? 0 : this.suffix.length();
                return this.formatter.parse(string.substring(n, string.length() - n2));
            }
            catch (ParseException parseException) {
                return null;
            }
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<NumberAxis, Number> TICK_UNIT = new CssMetaData<NumberAxis, Number>("-fx-tick-unit", SizeConverter.getInstance(), (Number)5.0){

            @Override
            public boolean isSettable(NumberAxis numberAxis) {
                return numberAxis.tickUnit == null || !numberAxis.tickUnit.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(NumberAxis numberAxis) {
                return (StyleableProperty)((Object)numberAxis.tickUnitProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(ValueAxis.getClassCssMetaData());
            arrayList.add(TICK_UNIT);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

