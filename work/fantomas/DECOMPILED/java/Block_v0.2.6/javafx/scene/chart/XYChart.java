/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.chart;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.collections.NonIterableChange;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.Chart;
import javafx.scene.layout.Region;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public abstract class XYChart<X, Y>
extends Chart {
    private final BitSet colorBits = new BitSet(8);
    static String DEFAULT_COLOR = "default-color";
    final Map<Series<X, Y>, Integer> seriesColorMap = new HashMap<Series<X, Y>, Integer>();
    private boolean rangeValid = false;
    private final Line verticalZeroLine = new Line();
    private final Line horizontalZeroLine = new Line();
    private final Path verticalGridLines = new Path();
    private final Path horizontalGridLines = new Path();
    private final Path horizontalRowFill = new Path();
    private final Path verticalRowFill = new Path();
    private final Region plotBackground = new Region();
    private final Group plotArea = new Group(){

        @Override
        public void requestLayout() {
        }
    };
    private final Group plotContent = new Group();
    private final Rectangle plotAreaClip = new Rectangle();
    private final List<Series<X, Y>> displayedSeries = new ArrayList<Series<X, Y>>();
    private Legend legend = new Legend();
    private final ListChangeListener<Series<X, Y>> seriesChanged = change -> {
        ObservableList observableList = change.getList();
        while (change.next()) {
            if (change.wasPermutated()) {
                this.displayedSeries.sort((series, series2) -> observableList.indexOf(series2) - observableList.indexOf(series));
            }
            if (change.getRemoved().size() > 0) {
                this.updateLegend();
            }
            HashSet<Series<X, Y>> hashSet = new HashSet<Series<X, Y>>(this.displayedSeries);
            hashSet.removeAll(change.getRemoved());
            for (Series series3 : change.getAddedSubList()) {
                if (hashSet.add(series3)) continue;
                throw new IllegalArgumentException("Duplicate series added");
            }
            for (Series series3 : change.getRemoved()) {
                series3.setToRemove = true;
                this.seriesRemoved(series3);
            }
            for (int i = change.getFrom(); i < change.getTo() && !change.wasPermutated(); ++i) {
                Series series3;
                series3 = (Series)change.getList().get(i);
                series3.setChart(this);
                if (series3.setToRemove) {
                    series3.setToRemove = false;
                    series3.getChart().seriesBeingRemovedIsAdded(series3);
                }
                this.displayedSeries.add(series3);
                int n = this.colorBits.nextClearBit(0);
                this.colorBits.set(n, true);
                series3.defaultColorStyleClass = DEFAULT_COLOR + n % 8;
                this.seriesColorMap.put(series3, n % 8);
                this.seriesAdded(series3, i);
            }
            if (change.getFrom() < change.getTo()) {
                this.updateLegend();
            }
            this.seriesChanged(change);
        }
        this.invalidateRange();
        this.requestChartLayout();
    };
    private final Axis<X> xAxis;
    private final Axis<Y> yAxis;
    private ObjectProperty<ObservableList<Series<X, Y>>> data = new ObjectPropertyBase<ObservableList<Series<X, Y>>>(){
        private ObservableList<Series<X, Y>> old;

        @Override
        protected void invalidated() {
            ObservableList observableList = (ObservableList)this.getValue();
            if (observableList == this.old) {
                return;
            }
            int n = -1;
            if (this.old != null) {
                this.old.removeListener(XYChart.this.seriesChanged);
                if (observableList != null && this.old.size() > 0) {
                    n = ((Series)this.old.get(0)).getChart().getAnimated() ? 1 : 2;
                    ((Series)this.old.get(0)).getChart().setAnimated(false);
                }
            }
            if (observableList != null) {
                observableList.addListener(XYChart.this.seriesChanged);
            }
            if (this.old != null || observableList != null) {
                int n2;
                final List list = this.old != null ? this.old : Collections.emptyList();
                int n3 = n2 = observableList != null ? observableList.size() : 0;
                if (n2 > 0 || !list.isEmpty()) {
                    XYChart.this.seriesChanged.onChanged(new NonIterableChange<Series<X, Y>>(0, n2, observableList){

                        @Override
                        public List<Series<X, Y>> getRemoved() {
                            return list;
                        }

                        @Override
                        protected int[] getPermutation() {
                            return new int[0];
                        }
                    });
                }
            } else if (this.old != null && this.old.size() > 0) {
                XYChart.this.seriesChanged.onChanged(new NonIterableChange<Series<X, Y>>(0, 0, observableList){

                    @Override
                    public List<Series<X, Y>> getRemoved() {
                        return old;
                    }

                    @Override
                    protected int[] getPermutation() {
                        return new int[0];
                    }
                });
            }
            if (observableList != null && observableList.size() > 0 && n != -1) {
                ((Series)observableList.get(0)).getChart().setAnimated(n == 1);
            }
            this.old = observableList;
        }

        @Override
        public Object getBean() {
            return XYChart.this;
        }

        @Override
        public String getName() {
            return "data";
        }
    };
    private BooleanProperty verticalGridLinesVisible = new StyleableBooleanProperty(true){

        @Override
        protected void invalidated() {
            XYChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return XYChart.this;
        }

        @Override
        public String getName() {
            return "verticalGridLinesVisible";
        }

        @Override
        public CssMetaData<XYChart<?, ?>, Boolean> getCssMetaData() {
            return StyleableProperties.VERTICAL_GRID_LINE_VISIBLE;
        }
    };
    private BooleanProperty horizontalGridLinesVisible = new StyleableBooleanProperty(true){

        @Override
        protected void invalidated() {
            XYChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return XYChart.this;
        }

        @Override
        public String getName() {
            return "horizontalGridLinesVisible";
        }

        @Override
        public CssMetaData<XYChart<?, ?>, Boolean> getCssMetaData() {
            return StyleableProperties.HORIZONTAL_GRID_LINE_VISIBLE;
        }
    };
    private BooleanProperty alternativeColumnFillVisible = new StyleableBooleanProperty(false){

        @Override
        protected void invalidated() {
            XYChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return XYChart.this;
        }

        @Override
        public String getName() {
            return "alternativeColumnFillVisible";
        }

        @Override
        public CssMetaData<XYChart<?, ?>, Boolean> getCssMetaData() {
            return StyleableProperties.ALTERNATIVE_COLUMN_FILL_VISIBLE;
        }
    };
    private BooleanProperty alternativeRowFillVisible = new StyleableBooleanProperty(true){

        @Override
        protected void invalidated() {
            XYChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return XYChart.this;
        }

        @Override
        public String getName() {
            return "alternativeRowFillVisible";
        }

        @Override
        public CssMetaData<XYChart<?, ?>, Boolean> getCssMetaData() {
            return StyleableProperties.ALTERNATIVE_ROW_FILL_VISIBLE;
        }
    };
    private BooleanProperty verticalZeroLineVisible = new StyleableBooleanProperty(true){

        @Override
        protected void invalidated() {
            XYChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return XYChart.this;
        }

        @Override
        public String getName() {
            return "verticalZeroLineVisible";
        }

        @Override
        public CssMetaData<XYChart<?, ?>, Boolean> getCssMetaData() {
            return StyleableProperties.VERTICAL_ZERO_LINE_VISIBLE;
        }
    };
    private BooleanProperty horizontalZeroLineVisible = new StyleableBooleanProperty(true){

        @Override
        protected void invalidated() {
            XYChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return XYChart.this;
        }

        @Override
        public String getName() {
            return "horizontalZeroLineVisible";
        }

        @Override
        public CssMetaData<XYChart<?, ?>, Boolean> getCssMetaData() {
            return StyleableProperties.HORIZONTAL_ZERO_LINE_VISIBLE;
        }
    };

    public Axis<X> getXAxis() {
        return this.xAxis;
    }

    public Axis<Y> getYAxis() {
        return this.yAxis;
    }

    public final ObservableList<Series<X, Y>> getData() {
        return (ObservableList)this.data.getValue();
    }

    public final void setData(ObservableList<Series<X, Y>> observableList) {
        this.data.setValue(observableList);
    }

    public final ObjectProperty<ObservableList<Series<X, Y>>> dataProperty() {
        return this.data;
    }

    public final boolean getVerticalGridLinesVisible() {
        return this.verticalGridLinesVisible.get();
    }

    public final void setVerticalGridLinesVisible(boolean bl) {
        this.verticalGridLinesVisible.set(bl);
    }

    public final BooleanProperty verticalGridLinesVisibleProperty() {
        return this.verticalGridLinesVisible;
    }

    public final boolean isHorizontalGridLinesVisible() {
        return this.horizontalGridLinesVisible.get();
    }

    public final void setHorizontalGridLinesVisible(boolean bl) {
        this.horizontalGridLinesVisible.set(bl);
    }

    public final BooleanProperty horizontalGridLinesVisibleProperty() {
        return this.horizontalGridLinesVisible;
    }

    public final boolean isAlternativeColumnFillVisible() {
        return this.alternativeColumnFillVisible.getValue();
    }

    public final void setAlternativeColumnFillVisible(boolean bl) {
        this.alternativeColumnFillVisible.setValue(bl);
    }

    public final BooleanProperty alternativeColumnFillVisibleProperty() {
        return this.alternativeColumnFillVisible;
    }

    public final boolean isAlternativeRowFillVisible() {
        return this.alternativeRowFillVisible.getValue();
    }

    public final void setAlternativeRowFillVisible(boolean bl) {
        this.alternativeRowFillVisible.setValue(bl);
    }

    public final BooleanProperty alternativeRowFillVisibleProperty() {
        return this.alternativeRowFillVisible;
    }

    public final boolean isVerticalZeroLineVisible() {
        return this.verticalZeroLineVisible.get();
    }

    public final void setVerticalZeroLineVisible(boolean bl) {
        this.verticalZeroLineVisible.set(bl);
    }

    public final BooleanProperty verticalZeroLineVisibleProperty() {
        return this.verticalZeroLineVisible;
    }

    public final boolean isHorizontalZeroLineVisible() {
        return this.horizontalZeroLineVisible.get();
    }

    public final void setHorizontalZeroLineVisible(boolean bl) {
        this.horizontalZeroLineVisible.set(bl);
    }

    public final BooleanProperty horizontalZeroLineVisibleProperty() {
        return this.horizontalZeroLineVisible;
    }

    protected ObservableList<Node> getPlotChildren() {
        return this.plotContent.getChildren();
    }

    public XYChart(Axis<X> axis, Axis<Y> axis2) {
        this.xAxis = axis;
        if (axis.getSide() == null) {
            axis.setSide(Side.BOTTOM);
        }
        axis.setEffectiveOrientation(Orientation.HORIZONTAL);
        this.yAxis = axis2;
        if (axis2.getSide() == null) {
            axis2.setSide(Side.LEFT);
        }
        axis2.setEffectiveOrientation(Orientation.VERTICAL);
        axis.autoRangingProperty().addListener((observableValue, bl, bl2) -> this.updateAxisRange());
        axis2.autoRangingProperty().addListener((observableValue, bl, bl2) -> this.updateAxisRange());
        this.getChartChildren().addAll((Node[])new Node[]{this.plotBackground, this.plotArea, axis, axis2});
        this.plotArea.setAutoSizeChildren(false);
        this.plotContent.setAutoSizeChildren(false);
        this.plotAreaClip.setSmooth(false);
        this.plotArea.setClip(this.plotAreaClip);
        this.plotArea.getChildren().addAll((Node[])new Node[]{this.verticalRowFill, this.horizontalRowFill, this.verticalGridLines, this.horizontalGridLines, this.verticalZeroLine, this.horizontalZeroLine, this.plotContent});
        this.plotContent.getStyleClass().setAll((String[])new String[]{"plot-content"});
        this.plotBackground.getStyleClass().setAll((String[])new String[]{"chart-plot-background"});
        this.verticalRowFill.getStyleClass().setAll((String[])new String[]{"chart-alternative-column-fill"});
        this.horizontalRowFill.getStyleClass().setAll((String[])new String[]{"chart-alternative-row-fill"});
        this.verticalGridLines.getStyleClass().setAll((String[])new String[]{"chart-vertical-grid-lines"});
        this.horizontalGridLines.getStyleClass().setAll((String[])new String[]{"chart-horizontal-grid-lines"});
        this.verticalZeroLine.getStyleClass().setAll((String[])new String[]{"chart-vertical-zero-line"});
        this.horizontalZeroLine.getStyleClass().setAll((String[])new String[]{"chart-horizontal-zero-line"});
        this.plotContent.setManaged(false);
        this.plotArea.setManaged(false);
        this.animatedProperty().addListener((observableValue, bl, bl2) -> {
            if (this.getXAxis() != null) {
                this.getXAxis().setAnimated((boolean)bl2);
            }
            if (this.getYAxis() != null) {
                this.getYAxis().setAnimated((boolean)bl2);
            }
        });
        this.setLegend(this.legend);
    }

    final int getDataSize() {
        ObservableList<Series<X, Y>> observableList = this.getData();
        return observableList != null ? observableList.size() : 0;
    }

    private void seriesNameChanged() {
        this.updateLegend();
        this.requestChartLayout();
    }

    private void dataItemsChanged(Series<X, Y> series, List<Data<X, Y>> list, int n, int n2, boolean bl) {
        for (Data<X, Y> data : list) {
            this.dataItemRemoved(data, series);
        }
        for (int i = n; i < n2; ++i) {
            Data data = (Data)series.getData().get(i);
            this.dataItemAdded(series, i, data);
        }
        this.invalidateRange();
        this.requestChartLayout();
    }

    private <T> void dataValueChanged(Data<X, Y> data, T t, ObjectProperty<T> objectProperty) {
        if (objectProperty.get() != t) {
            this.invalidateRange();
        }
        this.dataItemChanged(data);
        if (this.shouldAnimate()) {
            this.animate(new KeyFrame(Duration.ZERO, new KeyValue(objectProperty, objectProperty.get())), new KeyFrame(Duration.millis(700.0), new KeyValue(objectProperty, t, Interpolator.EASE_BOTH)));
        } else {
            objectProperty.set(t);
            this.requestChartLayout();
        }
    }

    protected void updateLegend() {
        ArrayList<Legend.LegendItem> arrayList = new ArrayList<Legend.LegendItem>();
        if (this.getData() != null) {
            for (int i = 0; i < this.getData().size(); ++i) {
                Series series = (Series)this.getData().get(i);
                arrayList.add(this.createLegendItemForSeries(series, i));
            }
        }
        this.legend.getItems().setAll((Collection<Legend.LegendItem>)arrayList);
        if (arrayList.size() > 0) {
            if (this.getLegend() == null) {
                this.setLegend(this.legend);
            }
        } else {
            this.setLegend(null);
        }
    }

    Legend.LegendItem createLegendItemForSeries(Series<X, Y> series, int n) {
        return new Legend.LegendItem(series.getName());
    }

    void seriesBeingRemovedIsAdded(Series<X, Y> series) {
    }

    void dataBeingRemovedIsAdded(Data<X, Y> data, Series<X, Y> series) {
    }

    protected abstract void dataItemAdded(Series<X, Y> var1, int var2, Data<X, Y> var3);

    protected abstract void dataItemRemoved(Data<X, Y> var1, Series<X, Y> var2);

    protected abstract void dataItemChanged(Data<X, Y> var1);

    protected abstract void seriesAdded(Series<X, Y> var1, int var2);

    protected abstract void seriesRemoved(Series<X, Y> var1);

    protected void seriesChanged(ListChangeListener.Change<? extends Series> change) {
    }

    private void invalidateRange() {
        this.rangeValid = false;
    }

    protected void updateAxisRange() {
        Axis<X> axis = this.getXAxis();
        Axis<Y> axis2 = this.getYAxis();
        ArrayList arrayList = null;
        ArrayList arrayList2 = null;
        if (axis.isAutoRanging()) {
            arrayList = new ArrayList();
        }
        if (axis2.isAutoRanging()) {
            arrayList2 = new ArrayList();
        }
        if (arrayList != null || arrayList2 != null) {
            for (Series series : this.getData()) {
                for (Data data : series.getData()) {
                    if (arrayList != null) {
                        arrayList.add(data.getXValue());
                    }
                    if (arrayList2 == null) continue;
                    arrayList2.add(data.getYValue());
                }
            }
            if (arrayList != null) {
                axis.invalidateRange(arrayList);
            }
            if (arrayList2 != null) {
                axis2.invalidateRange(arrayList2);
            }
        }
    }

    protected abstract void layoutPlotChildren();

    @Override
    protected final void layoutChartChildren(double d, double d2, double d3, double d4) {
        double d5;
        double d6;
        double d7;
        Object object;
        int n;
        if (this.getData() == null) {
            return;
        }
        if (!this.rangeValid) {
            this.rangeValid = true;
            if (this.getData() != null) {
                this.updateAxisRange();
            }
        }
        d = this.snapPositionY(d);
        d2 = this.snapPositionX(d2);
        Axis<X> axis = this.getXAxis();
        ObservableList<Axis.TickMark<X>> observableList = axis.getTickMarks();
        Axis<Y> axis2 = this.getYAxis();
        ObservableList<Axis.TickMark<Y>> observableList2 = axis2.getTickMarks();
        if (axis == null || axis2 == null) {
            return;
        }
        double d8 = 0.0;
        double d9 = 30.0;
        double d10 = 0.0;
        double d11 = 0.0;
        for (int i = 0; i < 5; ++i) {
            double d12;
            d11 = this.snapSizeY(d4 - d9);
            if (d11 < 0.0) {
                d11 = 0.0;
            }
            if ((d8 = this.snapSizeX(d3 - (d10 = axis2.prefWidth(d11)))) < 0.0) {
                d8 = 0.0;
            }
            if ((d12 = axis.prefHeight(d8)) == d9) break;
            d9 = d12;
        }
        d8 = Math.ceil(d8);
        d9 = Math.ceil(d9);
        d10 = Math.ceil(d10);
        d11 = Math.ceil(d11);
        double d13 = 0.0;
        switch (axis.getEffectiveSide()) {
            case TOP: {
                axis.setVisible(true);
                d13 = d + 1.0;
                d += d9;
                break;
            }
            case BOTTOM: {
                axis.setVisible(true);
                d13 = d + d11;
            }
        }
        double d14 = 0.0;
        switch (axis2.getEffectiveSide()) {
            case LEFT: {
                axis2.setVisible(true);
                d14 = d2 + 1.0;
                d2 += d10;
                break;
            }
            case RIGHT: {
                axis2.setVisible(true);
                d14 = d2 + d8;
            }
        }
        axis.resizeRelocate(d2, d13, d8, d9);
        axis2.resizeRelocate(d14, d, d10, d11);
        axis.requestAxisLayout();
        axis.layout();
        axis2.requestAxisLayout();
        axis2.layout();
        this.layoutPlotChildren();
        double d15 = axis.getZeroPosition();
        double d16 = axis2.getZeroPosition();
        if (Double.isNaN(d15) || !this.isVerticalZeroLineVisible()) {
            this.verticalZeroLine.setVisible(false);
        } else {
            this.verticalZeroLine.setStartX(d2 + d15 + 0.5);
            this.verticalZeroLine.setStartY(d);
            this.verticalZeroLine.setEndX(d2 + d15 + 0.5);
            this.verticalZeroLine.setEndY(d + d11);
            this.verticalZeroLine.setVisible(true);
        }
        if (Double.isNaN(d16) || !this.isHorizontalZeroLineVisible()) {
            this.horizontalZeroLine.setVisible(false);
        } else {
            this.horizontalZeroLine.setStartX(d2);
            this.horizontalZeroLine.setStartY(d + d16 + 0.5);
            this.horizontalZeroLine.setEndX(d2 + d8);
            this.horizontalZeroLine.setEndY(d + d16 + 0.5);
            this.horizontalZeroLine.setVisible(true);
        }
        this.plotBackground.resizeRelocate(d2, d, d8, d11);
        this.plotAreaClip.setX(d2);
        this.plotAreaClip.setY(d);
        this.plotAreaClip.setWidth(d8 + 1.0);
        this.plotAreaClip.setHeight(d11 + 1.0);
        this.plotContent.setLayoutX(d2);
        this.plotContent.setLayoutY(d);
        this.plotContent.requestLayout();
        this.verticalGridLines.getElements().clear();
        if (this.getVerticalGridLinesVisible()) {
            for (n = 0; n < observableList.size(); ++n) {
                object = (Axis.TickMark)observableList.get(n);
                d7 = axis.getDisplayPosition(((Axis.TickMark)object).getValue());
                if (d7 == d15 && this.isVerticalZeroLineVisible() || !(d7 > 0.0) || !(d7 <= d8)) continue;
                this.verticalGridLines.getElements().add(new MoveTo(d2 + d7 + 0.5, d));
                this.verticalGridLines.getElements().add(new LineTo(d2 + d7 + 0.5, d + d11));
            }
        }
        this.horizontalGridLines.getElements().clear();
        if (this.isHorizontalGridLinesVisible()) {
            for (n = 0; n < observableList2.size(); ++n) {
                object = (Axis.TickMark)observableList2.get(n);
                d7 = axis2.getDisplayPosition(((Axis.TickMark)object).getValue());
                if (d7 == d16 && this.isHorizontalZeroLineVisible() || !(d7 >= 0.0) || !(d7 < d11)) continue;
                this.horizontalGridLines.getElements().add(new MoveTo(d2, d + d7 + 0.5));
                this.horizontalGridLines.getElements().add(new LineTo(d2 + d8, d + d7 + 0.5));
            }
        }
        this.verticalRowFill.getElements().clear();
        if (this.isAlternativeColumnFillVisible()) {
            int n2;
            ArrayList<Double> arrayList = new ArrayList<Double>();
            object = new ArrayList();
            for (n2 = 0; n2 < observableList.size(); ++n2) {
                d6 = axis.getDisplayPosition(((Axis.TickMark)observableList.get(n2)).getValue());
                if (d6 == d15) {
                    arrayList.add(d6);
                    object.add(d6);
                    continue;
                }
                if (d6 < d15) {
                    arrayList.add(d6);
                    continue;
                }
                object.add(d6);
            }
            Collections.sort(arrayList);
            Collections.sort(object);
            for (n2 = 1; n2 < arrayList.size(); n2 += 2) {
                if (n2 + 1 >= arrayList.size()) continue;
                d6 = (Double)arrayList.get(n2);
                d5 = (Double)arrayList.get(n2 + 1);
                this.verticalRowFill.getElements().addAll((PathElement[])new PathElement[]{new MoveTo(d2 + d6, d), new LineTo(d2 + d6, d + d11), new LineTo(d2 + d5, d + d11), new LineTo(d2 + d5, d), new ClosePath()});
            }
            for (n2 = 0; n2 < object.size(); n2 += 2) {
                if (n2 + 1 >= object.size()) continue;
                d6 = (Double)object.get(n2);
                d5 = (Double)object.get(n2 + 1);
                this.verticalRowFill.getElements().addAll((PathElement[])new PathElement[]{new MoveTo(d2 + d6, d), new LineTo(d2 + d6, d + d11), new LineTo(d2 + d5, d + d11), new LineTo(d2 + d5, d), new ClosePath()});
            }
        }
        this.horizontalRowFill.getElements().clear();
        if (this.isAlternativeRowFillVisible()) {
            int n3;
            ArrayList<Double> arrayList = new ArrayList<Double>();
            object = new ArrayList<Double>();
            for (n3 = 0; n3 < observableList2.size(); ++n3) {
                d6 = axis2.getDisplayPosition(((Axis.TickMark)observableList2.get(n3)).getValue());
                if (d6 == d16) {
                    arrayList.add(d6);
                    object.add(d6);
                    continue;
                }
                if (d6 < d16) {
                    arrayList.add(d6);
                    continue;
                }
                object.add(d6);
            }
            Collections.sort(arrayList);
            Collections.sort(object);
            for (n3 = 1; n3 < arrayList.size(); n3 += 2) {
                if (n3 + 1 >= arrayList.size()) continue;
                d6 = (Double)arrayList.get(n3);
                d5 = (Double)arrayList.get(n3 + 1);
                this.horizontalRowFill.getElements().addAll((PathElement[])new PathElement[]{new MoveTo(d2, d + d6), new LineTo(d2 + d8, d + d6), new LineTo(d2 + d8, d + d5), new LineTo(d2, d + d5), new ClosePath()});
            }
            for (n3 = 0; n3 < object.size(); n3 += 2) {
                if (n3 + 1 >= object.size()) continue;
                d6 = (Double)object.get(n3);
                d5 = (Double)object.get(n3 + 1);
                this.horizontalRowFill.getElements().addAll((PathElement[])new PathElement[]{new MoveTo(d2, d + d6), new LineTo(d2 + d8, d + d6), new LineTo(d2 + d8, d + d5), new LineTo(d2, d + d5), new ClosePath()});
            }
        }
    }

    int getSeriesIndex(Series<X, Y> series) {
        return this.displayedSeries.indexOf(series);
    }

    int getSeriesSize() {
        return this.displayedSeries.size();
    }

    protected final void removeSeriesFromDisplay(Series<X, Y> series) {
        if (series != null) {
            series.setToRemove = false;
        }
        ((Series)series).setChart(null);
        this.displayedSeries.remove(series);
        int n = this.seriesColorMap.remove(series);
        this.colorBits.clear(n);
    }

    protected final Iterator<Series<X, Y>> getDisplayedSeriesIterator() {
        return Collections.unmodifiableList(this.displayedSeries).iterator();
    }

    final KeyFrame[] createSeriesRemoveTimeLine(Series<X, Y> series, long l) {
        ArrayList<Node> arrayList = new ArrayList<Node>();
        arrayList.add(series.getNode());
        for (Data keyValueArray2 : series.getData()) {
            if (keyValueArray2.getNode() == null) continue;
            arrayList.add(keyValueArray2.getNode());
        }
        KeyValue[] keyValueArray3 = new KeyValue[arrayList.size()];
        KeyValue[] keyValueArray = new KeyValue[arrayList.size()];
        for (int i = 0; i < arrayList.size(); ++i) {
            keyValueArray3[i] = new KeyValue(((Node)arrayList.get(i)).opacityProperty(), 1);
            keyValueArray[i] = new KeyValue(((Node)arrayList.get(i)).opacityProperty(), 0);
        }
        return new KeyFrame[]{new KeyFrame(Duration.ZERO, keyValueArray3), new KeyFrame(Duration.millis(l), actionEvent -> {
            this.getPlotChildren().removeAll((Collection<?>)arrayList);
            this.removeSeriesFromDisplay(series);
        }, keyValueArray)};
    }

    protected final X getCurrentDisplayedXValue(Data<X, Y> data) {
        return data.getCurrentX();
    }

    protected final void setCurrentDisplayedXValue(Data<X, Y> data, X x) {
        data.setCurrentX(x);
    }

    protected final ObjectProperty<X> currentDisplayedXValueProperty(Data<X, Y> data) {
        return data.currentXProperty();
    }

    protected final Y getCurrentDisplayedYValue(Data<X, Y> data) {
        return data.getCurrentY();
    }

    protected final void setCurrentDisplayedYValue(Data<X, Y> data, Y y) {
        data.setCurrentY(y);
    }

    protected final ObjectProperty<Y> currentDisplayedYValueProperty(Data<X, Y> data) {
        return data.currentYProperty();
    }

    protected final Object getCurrentDisplayedExtraValue(Data<X, Y> data) {
        return data.getCurrentExtraValue();
    }

    protected final void setCurrentDisplayedExtraValue(Data<X, Y> data, Object object) {
        data.setCurrentExtraValue(object);
    }

    protected final ObjectProperty<Object> currentDisplayedExtraValueProperty(Data<X, Y> data) {
        return data.currentExtraValueProperty();
    }

    protected final Iterator<Data<X, Y>> getDisplayedDataIterator(Series<X, Y> series) {
        return Collections.unmodifiableList(((Series)series).displayedData).iterator();
    }

    protected final void removeDataItemFromDisplay(Series<X, Y> series, Data<X, Y> data) {
        ((Series)series).removeDataItemRef(data);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return XYChart.getClassCssMetaData();
    }

    public static final class Series<X, Y> {
        String defaultColorStyleClass;
        boolean setToRemove = false;
        private List<Data<X, Y>> displayedData = new ArrayList<Data<X, Y>>();
        private final ListChangeListener<Data<X, Y>> dataChangeListener = new ListChangeListener<Data<X, Y>>(){

            @Override
            public void onChanged(ListChangeListener.Change<? extends Data<X, Y>> change) {
                ObservableList observableList = change.getList();
                XYChart xYChart = this.getChart();
                while (change.next()) {
                    HashSet<Data> hashSet;
                    if (xYChart != null) {
                        if (change.wasPermutated()) {
                            displayedData.sort((data, data2) -> observableList.indexOf(data2) - observableList.indexOf(data));
                            return;
                        }
                        hashSet = new HashSet(displayedData);
                        hashSet.removeAll(change.getRemoved());
                        for (Data data3 : change.getAddedSubList()) {
                            if (hashSet.add(data3)) continue;
                            throw new IllegalArgumentException("Duplicate data added");
                        }
                        for (Data data4 : change.getRemoved()) {
                            data4.setToRemove = true;
                        }
                        if (change.getAddedSize() > 0) {
                            for (Data data5 : change.getAddedSubList()) {
                                if (!data5.setToRemove) continue;
                                if (xYChart != null) {
                                    xYChart.dataBeingRemovedIsAdded(data5, this);
                                }
                                data5.setToRemove = false;
                            }
                            for (Data data6 : change.getAddedSubList()) {
                                data6.setSeries(this);
                            }
                            if (change.getFrom() == 0) {
                                displayedData.addAll(0, change.getAddedSubList());
                            } else {
                                displayedData.addAll(displayedData.indexOf(observableList.get(change.getFrom() - 1)) + 1, change.getAddedSubList());
                            }
                        }
                        xYChart.dataItemsChanged(this, change.getRemoved(), change.getFrom(), change.getTo(), change.wasPermutated());
                        continue;
                    }
                    hashSet = new HashSet<Data>();
                    for (Data data7 : observableList) {
                        if (hashSet.add(data7)) continue;
                        throw new IllegalArgumentException("Duplicate data added");
                    }
                    for (Data data8 : change.getAddedSubList()) {
                        data8.setSeries(this);
                    }
                }
            }
        };
        private final ReadOnlyObjectWrapper<XYChart<X, Y>> chart = new ReadOnlyObjectWrapper<XYChart<X, Y>>(this, "chart"){

            @Override
            protected void invalidated() {
                if (this.get() == null) {
                    displayedData.clear();
                } else {
                    displayedData.addAll(this.getData());
                }
            }
        };
        private final StringProperty name = new StringPropertyBase(){

            @Override
            protected void invalidated() {
                this.get();
                if (this.getChart() != null) {
                    this.getChart().seriesNameChanged();
                }
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "name";
            }
        };
        private ObjectProperty<Node> node = new SimpleObjectProperty<Node>(this, "node");
        private final ObjectProperty<ObservableList<Data<X, Y>>> data = new ObjectPropertyBase<ObservableList<Data<X, Y>>>(){
            private ObservableList<Data<X, Y>> old;

            @Override
            protected void invalidated() {
                ObservableList observableList = (ObservableList)this.getValue();
                if (this.old != null) {
                    this.old.removeListener(dataChangeListener);
                }
                if (observableList != null) {
                    observableList.addListener(dataChangeListener);
                }
                if (this.old != null || observableList != null) {
                    int n;
                    final List list = this.old != null ? this.old : Collections.emptyList();
                    int n2 = n = observableList != null ? observableList.size() : 0;
                    if (n > 0 || !list.isEmpty()) {
                        dataChangeListener.onChanged(new NonIterableChange<Data<X, Y>>(0, n, observableList){

                            @Override
                            public List<Data<X, Y>> getRemoved() {
                                return list;
                            }

                            @Override
                            protected int[] getPermutation() {
                                return new int[0];
                            }
                        });
                    }
                } else if (this.old != null && this.old.size() > 0) {
                    dataChangeListener.onChanged(new NonIterableChange<Data<X, Y>>(0, 0, observableList){

                        @Override
                        public List<Data<X, Y>> getRemoved() {
                            return old;
                        }

                        @Override
                        protected int[] getPermutation() {
                            return new int[0];
                        }
                    });
                }
                this.old = observableList;
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "data";
            }
        };

        public final XYChart<X, Y> getChart() {
            return (XYChart)this.chart.get();
        }

        private void setChart(XYChart<X, Y> xYChart) {
            this.chart.set(xYChart);
        }

        public final ReadOnlyObjectProperty<XYChart<X, Y>> chartProperty() {
            return this.chart.getReadOnlyProperty();
        }

        public final String getName() {
            return (String)this.name.get();
        }

        public final void setName(String string) {
            this.name.set(string);
        }

        public final StringProperty nameProperty() {
            return this.name;
        }

        public final Node getNode() {
            return (Node)this.node.get();
        }

        public final void setNode(Node node) {
            this.node.set(node);
        }

        public final ObjectProperty<Node> nodeProperty() {
            return this.node;
        }

        public final ObservableList<Data<X, Y>> getData() {
            return (ObservableList)this.data.getValue();
        }

        public final void setData(ObservableList<Data<X, Y>> observableList) {
            this.data.setValue(observableList);
        }

        public final ObjectProperty<ObservableList<Data<X, Y>>> dataProperty() {
            return this.data;
        }

        public Series() {
            this(FXCollections.observableArrayList());
        }

        public Series(ObservableList<Data<X, Y>> observableList) {
            this.setData(observableList);
            for (Data data : observableList) {
                data.setSeries(this);
            }
        }

        public Series(String string, ObservableList<Data<X, Y>> observableList) {
            this(observableList);
            this.setName(string);
        }

        public String toString() {
            return "Series[" + this.getName() + "]";
        }

        private void removeDataItemRef(Data<X, Y> data) {
            if (data != null) {
                ((Data)data).setToRemove = false;
            }
            this.displayedData.remove(data);
        }

        int getItemIndex(Data<X, Y> data) {
            return this.displayedData.indexOf(data);
        }

        Data<X, Y> getItem(int n) {
            return this.displayedData.get(n);
        }

        int getDataSize() {
            return this.displayedData.size();
        }
    }

    public static final class Data<X, Y> {
        private boolean setToRemove = false;
        private Series<X, Y> series;
        private ObjectProperty<X> xValue = new SimpleObjectProperty<X>(this, this, "XValue"){
            final /* synthetic */ Data this$0;
            {
                this.this$0 = data;
                super(object, string);
            }

            @Override
            protected void invalidated() {
                if (this.this$0.series != null) {
                    XYChart xYChart = this.this$0.series.getChart();
                    if (xYChart != null) {
                        xYChart.dataValueChanged(this.this$0, this.get(), this.this$0.currentXProperty());
                    }
                } else {
                    this.this$0.setCurrentX(this.get());
                }
            }
        };
        private ObjectProperty<Y> yValue = new SimpleObjectProperty<Y>(this, this, "YValue"){
            final /* synthetic */ Data this$0;
            {
                this.this$0 = data;
                super(object, string);
            }

            @Override
            protected void invalidated() {
                if (this.this$0.series != null) {
                    XYChart xYChart = this.this$0.series.getChart();
                    if (xYChart != null) {
                        xYChart.dataValueChanged(this.this$0, this.get(), this.this$0.currentYProperty());
                    }
                } else {
                    this.this$0.setCurrentY(this.get());
                }
            }
        };
        private ObjectProperty<Object> extraValue = new SimpleObjectProperty<Object>(this, (Object)this, "extraValue"){
            final /* synthetic */ Data this$0;
            {
                this.this$0 = data;
                super(object, string);
            }

            @Override
            protected void invalidated() {
                XYChart xYChart;
                if (this.this$0.series != null && (xYChart = this.this$0.series.getChart()) != null) {
                    xYChart.dataValueChanged(this.this$0, this.get(), this.this$0.currentExtraValueProperty());
                }
            }
        };
        private ObjectProperty<Node> node = new SimpleObjectProperty<Node>(this, (Object)this, "node"){
            final /* synthetic */ Data this$0;
            {
                this.this$0 = data;
                super(object, string);
            }

            @Override
            protected void invalidated() {
                Node node = (Node)this.get();
                if (node != null) {
                    node.accessibleTextProperty().unbind();
                    node.accessibleTextProperty().bind(new StringBinding(){
                        {
                            this.bind(this$0.currentXProperty(), this$0.currentYProperty());
                        }

                        @Override
                        protected String computeValue() {
                            String string = this$0.series != null ? this$0.series.getName() : "";
                            return string + " X Axis is " + this$0.getCurrentX() + " Y Axis is " + this$0.getCurrentY();
                        }
                    });
                }
            }
        };
        private ObjectProperty<X> currentX = new SimpleObjectProperty<X>(this, "currentX");
        private ObjectProperty<Y> currentY = new SimpleObjectProperty<Y>(this, "currentY");
        private ObjectProperty<Object> currentExtraValue = new SimpleObjectProperty<Object>(this, "currentExtraValue");

        void setSeries(Series<X, Y> series) {
            this.series = series;
        }

        public final X getXValue() {
            return (X)this.xValue.get();
        }

        public final void setXValue(X x) {
            this.xValue.set(x);
            if (this.currentX.get() == null || this.series != null && this.series.getChart() == null) {
                this.currentX.setValue(x);
            }
        }

        public final ObjectProperty<X> XValueProperty() {
            return this.xValue;
        }

        public final Y getYValue() {
            return (Y)this.yValue.get();
        }

        public final void setYValue(Y y) {
            this.yValue.set(y);
            if (this.currentY.get() == null || this.series != null && this.series.getChart() == null) {
                this.currentY.setValue(y);
            }
        }

        public final ObjectProperty<Y> YValueProperty() {
            return this.yValue;
        }

        public final Object getExtraValue() {
            return this.extraValue.get();
        }

        public final void setExtraValue(Object object) {
            this.extraValue.set(object);
        }

        public final ObjectProperty<Object> extraValueProperty() {
            return this.extraValue;
        }

        public final Node getNode() {
            return (Node)this.node.get();
        }

        public final void setNode(Node node) {
            this.node.set(node);
        }

        public final ObjectProperty<Node> nodeProperty() {
            return this.node;
        }

        final X getCurrentX() {
            return (X)this.currentX.get();
        }

        final void setCurrentX(X x) {
            this.currentX.set(x);
        }

        final ObjectProperty<X> currentXProperty() {
            return this.currentX;
        }

        final Y getCurrentY() {
            return (Y)this.currentY.get();
        }

        final void setCurrentY(Y y) {
            this.currentY.set(y);
        }

        final ObjectProperty<Y> currentYProperty() {
            return this.currentY;
        }

        final Object getCurrentExtraValue() {
            return this.currentExtraValue.getValue();
        }

        final void setCurrentExtraValue(Object object) {
            this.currentExtraValue.setValue(object);
        }

        final ObjectProperty<Object> currentExtraValueProperty() {
            return this.currentExtraValue;
        }

        public Data() {
        }

        public Data(X x, Y y) {
            this.setXValue(x);
            this.setYValue(y);
            this.setCurrentX(x);
            this.setCurrentY(y);
        }

        public Data(X x, Y y, Object object) {
            this.setXValue(x);
            this.setYValue(y);
            this.setExtraValue(object);
            this.setCurrentX(x);
            this.setCurrentY(y);
            this.setCurrentExtraValue(object);
        }

        public String toString() {
            return "Data[" + this.getXValue() + "," + this.getYValue() + "," + this.getExtraValue() + "]";
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<XYChart<?, ?>, Boolean> HORIZONTAL_GRID_LINE_VISIBLE = new CssMetaData<XYChart<?, ?>, Boolean>("-fx-horizontal-grid-lines-visible", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(XYChart<?, ?> xYChart) {
                return ((XYChart)xYChart).horizontalGridLinesVisible == null || !((XYChart)xYChart).horizontalGridLinesVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(XYChart<?, ?> xYChart) {
                return (StyleableProperty)((Object)xYChart.horizontalGridLinesVisibleProperty());
            }
        };
        private static final CssMetaData<XYChart<?, ?>, Boolean> HORIZONTAL_ZERO_LINE_VISIBLE = new CssMetaData<XYChart<?, ?>, Boolean>("-fx-horizontal-zero-line-visible", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(XYChart<?, ?> xYChart) {
                return ((XYChart)xYChart).horizontalZeroLineVisible == null || !((XYChart)xYChart).horizontalZeroLineVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(XYChart<?, ?> xYChart) {
                return (StyleableProperty)((Object)xYChart.horizontalZeroLineVisibleProperty());
            }
        };
        private static final CssMetaData<XYChart<?, ?>, Boolean> ALTERNATIVE_ROW_FILL_VISIBLE = new CssMetaData<XYChart<?, ?>, Boolean>("-fx-alternative-row-fill-visible", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(XYChart<?, ?> xYChart) {
                return ((XYChart)xYChart).alternativeRowFillVisible == null || !((XYChart)xYChart).alternativeRowFillVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(XYChart<?, ?> xYChart) {
                return (StyleableProperty)((Object)xYChart.alternativeRowFillVisibleProperty());
            }
        };
        private static final CssMetaData<XYChart<?, ?>, Boolean> VERTICAL_GRID_LINE_VISIBLE = new CssMetaData<XYChart<?, ?>, Boolean>("-fx-vertical-grid-lines-visible", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(XYChart<?, ?> xYChart) {
                return ((XYChart)xYChart).verticalGridLinesVisible == null || !((XYChart)xYChart).verticalGridLinesVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(XYChart<?, ?> xYChart) {
                return (StyleableProperty)((Object)xYChart.verticalGridLinesVisibleProperty());
            }
        };
        private static final CssMetaData<XYChart<?, ?>, Boolean> VERTICAL_ZERO_LINE_VISIBLE = new CssMetaData<XYChart<?, ?>, Boolean>("-fx-vertical-zero-line-visible", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(XYChart<?, ?> xYChart) {
                return ((XYChart)xYChart).verticalZeroLineVisible == null || !((XYChart)xYChart).verticalZeroLineVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(XYChart<?, ?> xYChart) {
                return (StyleableProperty)((Object)xYChart.verticalZeroLineVisibleProperty());
            }
        };
        private static final CssMetaData<XYChart<?, ?>, Boolean> ALTERNATIVE_COLUMN_FILL_VISIBLE = new CssMetaData<XYChart<?, ?>, Boolean>("-fx-alternative-column-fill-visible", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(XYChart<?, ?> xYChart) {
                return ((XYChart)xYChart).alternativeColumnFillVisible == null || !((XYChart)xYChart).alternativeColumnFillVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(XYChart<?, ?> xYChart) {
                return (StyleableProperty)((Object)xYChart.alternativeColumnFillVisibleProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Chart.getClassCssMetaData());
            arrayList.add(HORIZONTAL_GRID_LINE_VISIBLE);
            arrayList.add(HORIZONTAL_ZERO_LINE_VISIBLE);
            arrayList.add(ALTERNATIVE_ROW_FILL_VISIBLE);
            arrayList.add(VERTICAL_GRID_LINE_VISIBLE);
            arrayList.add(VERTICAL_ZERO_LINE_VISIBLE);
            arrayList.add(ALTERNATIVE_COLUMN_FILL_VISIBLE);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

