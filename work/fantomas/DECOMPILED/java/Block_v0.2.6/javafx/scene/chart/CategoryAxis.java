/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.chart;

import com.sun.javafx.charts.ChartLayoutAnimator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Dimension2D;
import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.util.Duration;

public final class CategoryAxis
extends Axis<String> {
    private List<String> allDataCategories = new ArrayList<String>();
    private boolean changeIsLocal = false;
    private final DoubleProperty firstCategoryPos = new SimpleDoubleProperty(this, "firstCategoryPos", 0.0);
    private Object currentAnimationID;
    private final ChartLayoutAnimator animator = new ChartLayoutAnimator(this);
    private ListChangeListener<String> itemsListener = change -> {
        while (change.next()) {
            if (!change.getAddedSubList().isEmpty()) {
                for (String string : change.getAddedSubList()) {
                    this.checkAndRemoveDuplicates(string);
                }
            }
            if (!this.isAutoRanging()) {
                this.allDataCategories.clear();
                this.allDataCategories.addAll(this.getCategories());
                this.rangeValid = false;
            }
            this.requestAxisLayout();
        }
    };
    private DoubleProperty startMargin = new StyleableDoubleProperty(5.0){

        @Override
        protected void invalidated() {
            CategoryAxis.this.requestAxisLayout();
        }

        @Override
        public CssMetaData<CategoryAxis, Number> getCssMetaData() {
            return StyleableProperties.START_MARGIN;
        }

        @Override
        public Object getBean() {
            return CategoryAxis.this;
        }

        @Override
        public String getName() {
            return "startMargin";
        }
    };
    private DoubleProperty endMargin = new StyleableDoubleProperty(5.0){

        @Override
        protected void invalidated() {
            CategoryAxis.this.requestAxisLayout();
        }

        @Override
        public CssMetaData<CategoryAxis, Number> getCssMetaData() {
            return StyleableProperties.END_MARGIN;
        }

        @Override
        public Object getBean() {
            return CategoryAxis.this;
        }

        @Override
        public String getName() {
            return "endMargin";
        }
    };
    private BooleanProperty gapStartAndEnd = new StyleableBooleanProperty(true){

        @Override
        protected void invalidated() {
            CategoryAxis.this.requestAxisLayout();
        }

        @Override
        public CssMetaData<CategoryAxis, Boolean> getCssMetaData() {
            return StyleableProperties.GAP_START_AND_END;
        }

        @Override
        public Object getBean() {
            return CategoryAxis.this;
        }

        @Override
        public String getName() {
            return "gapStartAndEnd";
        }
    };
    private ObjectProperty<ObservableList<String>> categories = new ObjectPropertyBase<ObservableList<String>>(){
        ObservableList<String> old;

        @Override
        protected void invalidated() {
            if (CategoryAxis.this.getDuplicate() != null) {
                throw new IllegalArgumentException("Duplicate category added; " + CategoryAxis.this.getDuplicate() + " already present");
            }
            ObservableList observableList = (ObservableList)this.get();
            if (this.old != observableList) {
                if (this.old != null) {
                    this.old.removeListener(CategoryAxis.this.itemsListener);
                }
                if (observableList != null) {
                    observableList.addListener(CategoryAxis.this.itemsListener);
                }
                this.old = observableList;
            }
        }

        @Override
        public Object getBean() {
            return CategoryAxis.this;
        }

        @Override
        public String getName() {
            return "categories";
        }
    };
    private final ReadOnlyDoubleWrapper categorySpacing = new ReadOnlyDoubleWrapper(this, "categorySpacing", 1.0);

    public final double getStartMargin() {
        return this.startMargin.getValue();
    }

    public final void setStartMargin(double d) {
        this.startMargin.setValue(d);
    }

    public final DoubleProperty startMarginProperty() {
        return this.startMargin;
    }

    public final double getEndMargin() {
        return this.endMargin.getValue();
    }

    public final void setEndMargin(double d) {
        this.endMargin.setValue(d);
    }

    public final DoubleProperty endMarginProperty() {
        return this.endMargin;
    }

    public final boolean isGapStartAndEnd() {
        return this.gapStartAndEnd.getValue();
    }

    public final void setGapStartAndEnd(boolean bl) {
        this.gapStartAndEnd.setValue(bl);
    }

    public final BooleanProperty gapStartAndEndProperty() {
        return this.gapStartAndEnd;
    }

    public final void setCategories(ObservableList<String> observableList) {
        this.categories.set(observableList);
        if (!this.changeIsLocal) {
            this.setAutoRanging(false);
            this.allDataCategories.clear();
            this.allDataCategories.addAll(this.getCategories());
        }
        this.requestAxisLayout();
    }

    private void checkAndRemoveDuplicates(String string) {
        if (this.getDuplicate() != null) {
            this.getCategories().remove(string);
            throw new IllegalArgumentException("Duplicate category ; " + string + " already present");
        }
    }

    private String getDuplicate() {
        if (this.getCategories() != null) {
            for (int i = 0; i < this.getCategories().size(); ++i) {
                for (int j = 0; j < this.getCategories().size(); ++j) {
                    if (!((String)this.getCategories().get(i)).equals(this.getCategories().get(j)) || i == j) continue;
                    return (String)this.getCategories().get(i);
                }
            }
        }
        return null;
    }

    public final ObservableList<String> getCategories() {
        return (ObservableList)this.categories.get();
    }

    public final double getCategorySpacing() {
        return this.categorySpacing.get();
    }

    public final ReadOnlyDoubleProperty categorySpacingProperty() {
        return this.categorySpacing.getReadOnlyProperty();
    }

    public CategoryAxis() {
        this.changeIsLocal = true;
        this.setCategories(FXCollections.observableArrayList());
        this.changeIsLocal = false;
    }

    public CategoryAxis(ObservableList<String> observableList) {
        this.setCategories(observableList);
    }

    private double calculateNewSpacing(double d, List<String> list) {
        Side side = this.getEffectiveSide();
        double d2 = 1.0;
        if (list != null) {
            double d3 = this.isGapStartAndEnd() ? list.size() : list.size() - 1;
            double d4 = d2 = d3 == 0.0 ? 1.0 : (d - this.getStartMargin() - this.getEndMargin()) / d3;
        }
        if (!this.isAutoRanging()) {
            this.categorySpacing.set(d2);
        }
        return d2;
    }

    private double calculateNewFirstPos(double d, double d2) {
        Side side = this.getEffectiveSide();
        double d3 = 1.0;
        double d4 = this.isGapStartAndEnd() ? d2 / 2.0 : 0.0;
        d3 = side.isHorizontal() ? 0.0 + this.getStartMargin() + d4 : d - this.getStartMargin() - d4;
        if (!this.isAutoRanging()) {
            this.firstCategoryPos.set(d3);
        }
        return d3;
    }

    @Override
    protected Object getRange() {
        return new Object[]{this.getCategories(), this.categorySpacing.get(), this.firstCategoryPos.get(), this.getEffectiveTickLabelRotation()};
    }

    @Override
    protected void setRange(Object object, boolean bl) {
        Object[] objectArray = (Object[])object;
        List list = (List)objectArray[0];
        double d = (Double)objectArray[1];
        double d2 = (Double)objectArray[2];
        this.setEffectiveTickLabelRotation((Double)objectArray[3]);
        this.changeIsLocal = true;
        this.setCategories(FXCollections.observableArrayList(list));
        this.changeIsLocal = false;
        if (bl) {
            this.animator.stop(this.currentAnimationID);
            this.currentAnimationID = this.animator.animate(new KeyFrame(Duration.ZERO, new KeyValue(this.firstCategoryPos, this.firstCategoryPos.get()), new KeyValue(this.categorySpacing, this.categorySpacing.get())), new KeyFrame(Duration.millis(1000.0), new KeyValue(this.firstCategoryPos, d2), new KeyValue(this.categorySpacing, d)));
        } else {
            this.categorySpacing.set(d);
            this.firstCategoryPos.set(d2);
        }
    }

    @Override
    protected Object autoRange(double d) {
        double d2;
        Side side = this.getEffectiveSide();
        double d3 = this.calculateNewSpacing(d, this.allDataCategories);
        double d4 = this.calculateNewFirstPos(d, d3);
        double d5 = this.getTickLabelRotation();
        if (d >= 0.0 && (d2 = this.calculateRequiredSize(side.isVertical(), d5)) > d) {
            if (side.isHorizontal() && d5 != 90.0) {
                d5 = 90.0;
            }
            if (side.isVertical() && d5 != 0.0) {
                d5 = 0.0;
            }
        }
        return new Object[]{this.allDataCategories, d3, d4, d5};
    }

    private double calculateRequiredSize(boolean bl, double d) {
        double d2 = 0.0;
        double d3 = 0.0;
        boolean bl2 = true;
        for (String string : this.allDataCategories) {
            double d4;
            Dimension2D dimension2D = this.measureTickMarkSize(string, d);
            double d5 = d4 = bl || d != 0.0 ? dimension2D.getHeight() : dimension2D.getWidth();
            if (bl2) {
                bl2 = false;
                d3 = d4 / 2.0;
                continue;
            }
            d2 = Math.max(d2, d3 + 6.0 + d4 / 2.0);
        }
        return this.getStartMargin() + d2 * (double)this.allDataCategories.size() + this.getEndMargin();
    }

    @Override
    protected List<String> calculateTickValues(double d, Object object) {
        Object[] objectArray = (Object[])object;
        return (List)objectArray[0];
    }

    @Override
    protected String getTickMarkLabel(String string) {
        return string;
    }

    @Override
    protected Dimension2D measureTickMarkSize(String string, Object object) {
        Object[] objectArray = (Object[])object;
        double d = (Double)objectArray[3];
        return this.measureTickMarkSize(string, d);
    }

    @Override
    public void invalidateRange(List<String> list) {
        super.invalidateRange(list);
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.addAll(this.allDataCategories);
        for (String string : this.allDataCategories) {
            if (list.contains(string)) continue;
            arrayList.remove(string);
        }
        for (int i = 0; i < list.size(); ++i) {
            int n = arrayList.size();
            if (arrayList.contains(list.get(i))) continue;
            arrayList.add(i > n ? n : i, list.get(i));
        }
        this.allDataCategories.clear();
        this.allDataCategories.addAll(arrayList);
    }

    final List<String> getAllDataCategories() {
        return this.allDataCategories;
    }

    @Override
    public double getDisplayPosition(String string) {
        ObservableList<String> observableList = this.getCategories();
        if (!observableList.contains(string)) {
            return Double.NaN;
        }
        if (this.getEffectiveSide().isHorizontal()) {
            return this.firstCategoryPos.get() + (double)observableList.indexOf(string) * this.categorySpacing.get();
        }
        return this.firstCategoryPos.get() + (double)observableList.indexOf(string) * this.categorySpacing.get() * -1.0;
    }

    @Override
    public String getValueForDisplay(double d) {
        if (this.getEffectiveSide().isHorizontal()) {
            if (d < 0.0 || d > this.getWidth()) {
                return null;
            }
            double d2 = (d - this.firstCategoryPos.get()) / this.categorySpacing.get();
            return this.toRealValue(d2);
        }
        if (d < 0.0 || d > this.getHeight()) {
            return null;
        }
        double d3 = (d - this.firstCategoryPos.get()) / (this.categorySpacing.get() * -1.0);
        return this.toRealValue(d3);
    }

    @Override
    public boolean isValueOnAxis(String string) {
        return this.getCategories().indexOf(string) != -1;
    }

    @Override
    public double toNumericValue(String string) {
        return this.getCategories().indexOf(string);
    }

    @Override
    public String toRealValue(double d) {
        int n = (int)Math.round(d);
        ObservableList<String> observableList = this.getCategories();
        if (n >= 0 && n < observableList.size()) {
            return (String)this.getCategories().get(n);
        }
        return null;
    }

    @Override
    public double getZeroPosition() {
        return Double.NaN;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return CategoryAxis.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<CategoryAxis, Number> START_MARGIN = new CssMetaData<CategoryAxis, Number>("-fx-start-margin", SizeConverter.getInstance(), (Number)5.0){

            @Override
            public boolean isSettable(CategoryAxis categoryAxis) {
                return categoryAxis.startMargin == null || !categoryAxis.startMargin.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(CategoryAxis categoryAxis) {
                return (StyleableProperty)((Object)categoryAxis.startMarginProperty());
            }
        };
        private static final CssMetaData<CategoryAxis, Number> END_MARGIN = new CssMetaData<CategoryAxis, Number>("-fx-end-margin", SizeConverter.getInstance(), (Number)5.0){

            @Override
            public boolean isSettable(CategoryAxis categoryAxis) {
                return categoryAxis.endMargin == null || !categoryAxis.endMargin.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(CategoryAxis categoryAxis) {
                return (StyleableProperty)((Object)categoryAxis.endMarginProperty());
            }
        };
        private static final CssMetaData<CategoryAxis, Boolean> GAP_START_AND_END = new CssMetaData<CategoryAxis, Boolean>("-fx-gap-start-and-end", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(CategoryAxis categoryAxis) {
                return categoryAxis.gapStartAndEnd == null || !categoryAxis.gapStartAndEnd.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(CategoryAxis categoryAxis) {
                return (StyleableProperty)((Object)categoryAxis.gapStartAndEndProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Axis.getClassCssMetaData());
            arrayList.add(START_MARGIN);
            arrayList.add(END_MARGIN);
            arrayList.add(GAP_START_AND_END);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

