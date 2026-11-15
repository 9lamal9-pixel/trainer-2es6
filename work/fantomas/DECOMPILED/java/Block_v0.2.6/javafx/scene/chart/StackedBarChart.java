/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.chart;

import com.sun.javafx.charts.Legend;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class StackedBarChart<X, Y>
extends XYChart<X, Y> {
    private Map<XYChart.Series<X, Y>, Map<String, List<XYChart.Data<X, Y>>>> seriesCategoryMap = new HashMap<XYChart.Series<X, Y>, Map<String, List<XYChart.Data<X, Y>>>>();
    private final Orientation orientation;
    private CategoryAxis categoryAxis;
    private ValueAxis valueAxis;
    private ListChangeListener<String> categoriesListener = new ListChangeListener<String>(){

        @Override
        public void onChanged(ListChangeListener.Change<? extends String> change) {
            while (change.next()) {
                for (String string : change.getRemoved()) {
                    for (XYChart.Series series : StackedBarChart.this.getData()) {
                        for (XYChart.Data data : series.getData()) {
                            Orientation orientation = StackedBarChart.this.orientation;
                            StackedBarChart.this.orientation;
                            if (!string.equals(orientation == Orientation.VERTICAL ? data.getXValue() : data.getYValue())) continue;
                            boolean bl = StackedBarChart.this.getAnimated();
                            StackedBarChart.this.setAnimated(false);
                            StackedBarChart.this.dataItemRemoved(data, series);
                            StackedBarChart.this.setAnimated(bl);
                        }
                    }
                    StackedBarChart.this.requestChartLayout();
                }
            }
        }
    };
    private DoubleProperty categoryGap = new StyleableDoubleProperty(10.0){

        @Override
        protected void invalidated() {
            this.get();
            StackedBarChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return StackedBarChart.this;
        }

        @Override
        public String getName() {
            return "categoryGap";
        }

        @Override
        public CssMetaData<StackedBarChart<?, ?>, Number> getCssMetaData() {
            return StyleableProperties.CATEGORY_GAP;
        }
    };
    private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");
    private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");

    public double getCategoryGap() {
        return this.categoryGap.getValue();
    }

    public void setCategoryGap(double d) {
        this.categoryGap.setValue(d);
    }

    public DoubleProperty categoryGapProperty() {
        return this.categoryGap;
    }

    public StackedBarChart(@NamedArg(value="xAxis") Axis<X> axis, @NamedArg(value="yAxis") Axis<Y> axis2) {
        this(axis, axis2, FXCollections.observableArrayList());
    }

    public StackedBarChart(@NamedArg(value="xAxis") Axis<X> axis, @NamedArg(value="yAxis") Axis<Y> axis2, @NamedArg(value="data") ObservableList<XYChart.Series<X, Y>> observableList) {
        super(axis, axis2);
        this.getStyleClass().add("stacked-bar-chart");
        if (!(axis instanceof ValueAxis && axis2 instanceof CategoryAxis || axis2 instanceof ValueAxis && axis instanceof CategoryAxis)) {
            throw new IllegalArgumentException("Axis type incorrect, one of X,Y should be CategoryAxis and the other NumberAxis");
        }
        if (axis instanceof CategoryAxis) {
            this.categoryAxis = (CategoryAxis)axis;
            this.valueAxis = (ValueAxis)axis2;
            this.orientation = Orientation.VERTICAL;
        } else {
            this.categoryAxis = (CategoryAxis)axis2;
            this.valueAxis = (ValueAxis)axis;
            this.orientation = Orientation.HORIZONTAL;
        }
        this.pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, this.orientation == Orientation.HORIZONTAL);
        this.pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, this.orientation == Orientation.VERTICAL);
        this.setData(observableList);
        this.categoryAxis.getCategories().addListener(this.categoriesListener);
    }

    public StackedBarChart(@NamedArg(value="xAxis") Axis<X> axis, @NamedArg(value="yAxis") Axis<Y> axis2, @NamedArg(value="data") ObservableList<XYChart.Series<X, Y>> observableList, @NamedArg(value="categoryGap") double d) {
        this(axis, axis2);
        this.setData(observableList);
        this.setCategoryGap(d);
    }

    @Override
    protected void dataItemAdded(XYChart.Series<X, Y> series, int n, XYChart.Data<X, Y> data) {
        String string = this.orientation == Orientation.VERTICAL ? (String)data.getXValue() : (String)data.getYValue();
        Map<String, List<XYChart.Data<X, Y>>> map = this.seriesCategoryMap.get(series);
        if (map == null) {
            map = new HashMap<String, List<XYChart.Data<X, Y>>>();
            this.seriesCategoryMap.put(series, map);
        }
        List<Object> list = map.get(string) != null ? map.get(string) : new ArrayList();
        list.add(data);
        map.put(string, list);
        Node node = this.createBar(series, this.getData().indexOf(series), data, n);
        if (this.shouldAnimate()) {
            this.animateDataAdd(data, node);
        } else {
            this.getPlotChildren().add(node);
        }
    }

    @Override
    protected void dataItemRemoved(XYChart.Data<X, Y> data, XYChart.Series<X, Y> series) {
        Node node = data.getNode();
        if (node != null) {
            node.focusTraversableProperty().unbind();
        }
        if (this.shouldAnimate()) {
            Timeline timeline = this.createDataRemoveTimeline(data, node, series);
            timeline.setOnFinished(actionEvent -> this.removeDataItemFromDisplay(series, data));
            timeline.play();
        } else {
            this.processDataRemove(series, data);
            this.removeDataItemFromDisplay(series, data);
        }
    }

    @Override
    protected void dataItemChanged(XYChart.Data<X, Y> data) {
        double d;
        double d2;
        if (this.orientation == Orientation.VERTICAL) {
            d2 = ((Number)data.getYValue()).doubleValue();
            d = ((Number)this.getCurrentDisplayedYValue(data)).doubleValue();
        } else {
            d2 = ((Number)data.getXValue()).doubleValue();
            d = ((Number)this.getCurrentDisplayedXValue(data)).doubleValue();
        }
        if (d > 0.0 && d2 < 0.0) {
            data.getNode().getStyleClass().add("negative");
        } else if (d < 0.0 && d2 > 0.0) {
            data.getNode().getStyleClass().remove("negative");
        }
    }

    @Override
    protected void seriesChanged(ListChangeListener.Change<? extends XYChart.Series> change) {
        for (int i = 0; i < this.getDataSize(); ++i) {
            XYChart.Series series = (XYChart.Series)this.getData().get(i);
            for (int j = 0; j < series.getData().size(); ++j) {
                XYChart.Data data = (XYChart.Data)series.getData().get(j);
                Node node = data.getNode();
                node.getStyleClass().setAll((String[])new String[]{"chart-bar", "series" + i, "data" + j, series.defaultColorStyleClass});
            }
        }
    }

    @Override
    protected void seriesAdded(XYChart.Series<X, Y> series, int n) {
        HashMap hashMap = new HashMap();
        for (int i = 0; i < series.getData().size(); ++i) {
            double d;
            XYChart.Data data = (XYChart.Data)series.getData().get(i);
            Node node = this.createBar(series, n, data, i);
            String string = this.orientation == Orientation.VERTICAL ? (String)data.getXValue() : (String)data.getYValue();
            List<XYChart.Data> list = hashMap.get(string) != null ? (List)hashMap.get(string) : new ArrayList();
            list.add(data);
            hashMap.put(string, list);
            if (this.shouldAnimate()) {
                this.animateDataAdd(data, node);
                continue;
            }
            double d2 = d = this.orientation == Orientation.VERTICAL ? ((Number)data.getYValue()).doubleValue() : ((Number)data.getXValue()).doubleValue();
            if (d < 0.0) {
                node.getStyleClass().add("negative");
            }
            this.getPlotChildren().add(node);
        }
        if (hashMap.size() > 0) {
            this.seriesCategoryMap.put(series, hashMap);
        }
    }

    @Override
    protected void seriesRemoved(XYChart.Series<X, Y> series) {
        if (this.shouldAnimate()) {
            ParallelTransition parallelTransition = new ParallelTransition();
            parallelTransition.setOnFinished(actionEvent -> {
                this.removeSeriesFromDisplay(series);
                this.requestChartLayout();
            });
            for (XYChart.Data data : series.getData()) {
                Animation animation;
                Node node = data.getNode();
                if (this.getSeriesSize() > 1) {
                    animation = this.createDataRemoveTimeline(data, node, series);
                    parallelTransition.getChildren().add(animation);
                    continue;
                }
                animation = new FadeTransition(Duration.millis(700.0), node);
                ((FadeTransition)animation).setFromValue(1.0);
                ((FadeTransition)animation).setToValue(0.0);
                animation.setOnFinished(actionEvent -> {
                    this.processDataRemove(series, data);
                    node.setOpacity(1.0);
                });
                parallelTransition.getChildren().add(animation);
            }
            parallelTransition.play();
        } else {
            for (XYChart.Data data : series.getData()) {
                this.processDataRemove(series, data);
            }
            this.removeSeriesFromDisplay(series);
            this.requestChartLayout();
        }
    }

    @Override
    protected void updateAxisRange() {
        ArrayList<String> arrayList;
        boolean bl;
        boolean bl2 = bl = this.categoryAxis == this.getXAxis();
        if (this.categoryAxis.isAutoRanging()) {
            arrayList = new ArrayList<String>();
            for (XYChart.Series object : this.getData()) {
                for (XYChart.Data data : object.getData()) {
                    if (data == null) continue;
                    arrayList.add((String)(bl ? data.getXValue() : data.getYValue()));
                }
            }
            this.categoryAxis.invalidateRange((List<String>)arrayList);
        }
        if (this.valueAxis.isAutoRanging()) {
            arrayList = new ArrayList();
            for (String string : this.categoryAxis.getAllDataCategories()) {
                double d = 0.0;
                double d2 = 0.0;
                Iterator iterator = this.getDisplayedSeriesIterator();
                while (iterator.hasNext()) {
                    XYChart.Series series = iterator.next();
                    for (XYChart.Data data : this.getDataItem(series, string)) {
                        if (data == null) continue;
                        boolean bl3 = data.getNode().getStyleClass().contains("negative");
                        Number number = (Number)(bl ? data.getYValue() : data.getXValue());
                        if (!bl3) {
                            d2 += this.valueAxis.toNumericValue(number);
                            continue;
                        }
                        d += this.valueAxis.toNumericValue(number);
                    }
                }
                arrayList.add((String)((Object)Double.valueOf(d2)));
                arrayList.add((String)((Object)Double.valueOf(d)));
            }
            this.valueAxis.invalidateRange(arrayList);
        }
    }

    @Override
    protected void layoutPlotChildren() {
        double d;
        double d2 = this.categoryAxis.getCategorySpacing();
        double d3 = d = d2 - this.getCategoryGap();
        double d4 = -((d2 - this.getCategoryGap()) / 2.0);
        for (String string : this.categoryAxis.getCategories()) {
            double d5 = 0.0;
            double d6 = 0.0;
            Iterator iterator = this.getDisplayedSeriesIterator();
            while (iterator.hasNext()) {
                XYChart.Series series = iterator.next();
                for (XYChart.Data data : this.getDataItem(series, string)) {
                    double d7;
                    double d8;
                    double d9;
                    double d10;
                    if (data == null) continue;
                    Node node = data.getNode();
                    Object x = this.getCurrentDisplayedXValue(data);
                    Object y = this.getCurrentDisplayedYValue(data);
                    if (this.orientation == Orientation.VERTICAL) {
                        d10 = this.getXAxis().getDisplayPosition(x);
                        d9 = this.getYAxis().toNumericValue(y);
                    } else {
                        d10 = this.getYAxis().getDisplayPosition(y);
                        d9 = this.getXAxis().toNumericValue(x);
                    }
                    boolean bl = node.getStyleClass().contains("negative");
                    if (!bl) {
                        d8 = this.valueAxis.getDisplayPosition(d5);
                        d7 = this.valueAxis.getDisplayPosition(d5 + d9);
                        d5 += d9;
                    } else {
                        d8 = this.valueAxis.getDisplayPosition(d6 + d9);
                        d7 = this.valueAxis.getDisplayPosition(d6);
                        d6 += d9;
                    }
                    if (this.orientation == Orientation.VERTICAL) {
                        node.resizeRelocate(d10 + d4, d7, d3, d8 - d7);
                        continue;
                    }
                    node.resizeRelocate(d8, d10 + d4, d7 - d8, d3);
                }
            }
        }
    }

    @Override
    Legend.LegendItem createLegendItemForSeries(XYChart.Series<X, Y> series, int n) {
        Legend.LegendItem legendItem = new Legend.LegendItem(series.getName());
        legendItem.getSymbol().getStyleClass().addAll((String[])new String[]{"chart-bar", "series" + n, "bar-legend-symbol", series.defaultColorStyleClass});
        return legendItem;
    }

    private void updateMap(XYChart.Series<X, Y> series, XYChart.Data<X, Y> data) {
        String string = this.orientation == Orientation.VERTICAL ? (String)data.getXValue() : (String)data.getYValue();
        Map<String, List<XYChart.Data<X, Y>>> map = this.seriesCategoryMap.get(series);
        if (map != null) {
            map.remove(string);
            if (map.isEmpty()) {
                this.seriesCategoryMap.remove(series);
            }
        }
        if (this.seriesCategoryMap.isEmpty() && this.categoryAxis.isAutoRanging()) {
            this.categoryAxis.getCategories().clear();
        }
    }

    private void processDataRemove(XYChart.Series<X, Y> series, XYChart.Data<X, Y> data) {
        Node node = data.getNode();
        this.getPlotChildren().remove(node);
        this.updateMap(series, data);
    }

    private void animateDataAdd(XYChart.Data<X, Y> data, Node node) {
        if (this.orientation == Orientation.VERTICAL) {
            double d = ((Number)data.getYValue()).doubleValue();
            if (d < 0.0) {
                node.getStyleClass().add("negative");
            }
            data.setYValue(this.getYAxis().toRealValue(this.getYAxis().getZeroPosition()));
            this.setCurrentDisplayedYValue(data, this.getYAxis().toRealValue(this.getYAxis().getZeroPosition()));
            this.getPlotChildren().add(node);
            data.setYValue(this.getYAxis().toRealValue(d));
            this.animate(new KeyFrame(Duration.ZERO, new KeyValue(this.currentDisplayedYValueProperty(data), this.getCurrentDisplayedYValue(data))), new KeyFrame(Duration.millis(700.0), new KeyValue(this.currentDisplayedYValueProperty(data), data.getYValue(), Interpolator.EASE_BOTH)));
        } else {
            double d = ((Number)data.getXValue()).doubleValue();
            if (d < 0.0) {
                node.getStyleClass().add("negative");
            }
            data.setXValue(this.getXAxis().toRealValue(this.getXAxis().getZeroPosition()));
            this.setCurrentDisplayedXValue(data, this.getXAxis().toRealValue(this.getXAxis().getZeroPosition()));
            this.getPlotChildren().add(node);
            data.setXValue(this.getXAxis().toRealValue(d));
            this.animate(new KeyFrame(Duration.ZERO, new KeyValue(this.currentDisplayedXValueProperty(data), this.getCurrentDisplayedXValue(data))), new KeyFrame(Duration.millis(700.0), new KeyValue(this.currentDisplayedXValueProperty(data), data.getXValue(), Interpolator.EASE_BOTH)));
        }
    }

    private Timeline createDataRemoveTimeline(XYChart.Data<X, Y> data, Node node, XYChart.Series<X, Y> series) {
        Timeline timeline = new Timeline();
        if (this.orientation == Orientation.VERTICAL) {
            data.setYValue(this.getYAxis().toRealValue(this.getYAxis().getZeroPosition()));
            timeline.getKeyFrames().addAll((KeyFrame[])new KeyFrame[]{new KeyFrame(Duration.ZERO, new KeyValue(this.currentDisplayedYValueProperty(data), this.getCurrentDisplayedYValue(data))), new KeyFrame(Duration.millis(700.0), actionEvent -> this.processDataRemove(series, data), new KeyValue(this.currentDisplayedYValueProperty(data), data.getYValue(), Interpolator.EASE_BOTH))});
        } else {
            data.setXValue(this.getXAxis().toRealValue(this.getXAxis().getZeroPosition()));
            timeline.getKeyFrames().addAll((KeyFrame[])new KeyFrame[]{new KeyFrame(Duration.ZERO, new KeyValue(this.currentDisplayedXValueProperty(data), this.getCurrentDisplayedXValue(data))), new KeyFrame(Duration.millis(700.0), actionEvent -> this.processDataRemove(series, data), new KeyValue(this.currentDisplayedXValueProperty(data), data.getXValue(), Interpolator.EASE_BOTH))});
        }
        return timeline;
    }

    private Node createBar(XYChart.Series<X, Y> series, int n, XYChart.Data<X, Y> data, int n2) {
        Node node = data.getNode();
        if (node == null) {
            node = new StackPane();
            node.setAccessibleRole(AccessibleRole.TEXT);
            node.setAccessibleRoleDescription("Bar");
            node.focusTraversableProperty().bind(Platform.accessibilityActiveProperty());
            data.setNode(node);
        }
        node.getStyleClass().setAll((String[])new String[]{"chart-bar", "series" + n, "data" + n2, series.defaultColorStyleClass});
        return node;
    }

    private List<XYChart.Data<X, Y>> getDataItem(XYChart.Series<X, Y> series, String string) {
        Map<String, List<XYChart.Data<X, Y>>> map = this.seriesCategoryMap.get(series);
        return map != null ? (map.get(string) != null ? map.get(string) : new ArrayList()) : new ArrayList();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return StackedBarChart.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<StackedBarChart<?, ?>, Number> CATEGORY_GAP = new CssMetaData<StackedBarChart<?, ?>, Number>("-fx-category-gap", SizeConverter.getInstance(), 10.0){

            @Override
            public boolean isSettable(StackedBarChart<?, ?> stackedBarChart) {
                return ((StackedBarChart)stackedBarChart).categoryGap == null || !((StackedBarChart)stackedBarChart).categoryGap.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(StackedBarChart<?, ?> stackedBarChart) {
                return (StyleableProperty)((Object)stackedBarChart.categoryGapProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(XYChart.getClassCssMetaData());
            arrayList.add(CATEGORY_GAP);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

