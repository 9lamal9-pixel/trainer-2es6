/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.chart;

import com.sun.javafx.charts.Legend;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.scene.AccessibleRole;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;

public class AreaChart<X, Y>
extends XYChart<X, Y> {
    private Map<XYChart.Series<X, Y>, DoubleProperty> seriesYMultiplierMap = new HashMap<XYChart.Series<X, Y>, DoubleProperty>();
    private BooleanProperty createSymbols = new StyleableBooleanProperty(true){

        @Override
        protected void invalidated() {
            for (int i = 0; i < AreaChart.this.getData().size(); ++i) {
                XYChart.Series series = (XYChart.Series)AreaChart.this.getData().get(i);
                for (int j = 0; j < series.getData().size(); ++j) {
                    XYChart.Data data = (XYChart.Data)series.getData().get(j);
                    Node node = data.getNode();
                    if (this.get() && node == null) {
                        node = AreaChart.this.createSymbol(series, AreaChart.this.getData().indexOf(series), data, j);
                        if (null == node) continue;
                        AreaChart.this.getPlotChildren().add(node);
                        continue;
                    }
                    if (this.get() || node == null) continue;
                    AreaChart.this.getPlotChildren().remove(node);
                    node = null;
                    data.setNode(null);
                }
            }
            AreaChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "createSymbols";
        }

        @Override
        public CssMetaData<AreaChart<?, ?>, Boolean> getCssMetaData() {
            return StyleableProperties.CREATE_SYMBOLS;
        }
    };

    public final boolean getCreateSymbols() {
        return this.createSymbols.getValue();
    }

    public final void setCreateSymbols(boolean bl) {
        this.createSymbols.setValue(bl);
    }

    public final BooleanProperty createSymbolsProperty() {
        return this.createSymbols;
    }

    public AreaChart(@NamedArg(value="xAxis") Axis<X> axis, @NamedArg(value="yAxis") Axis<Y> axis2) {
        this(axis, axis2, FXCollections.observableArrayList());
    }

    public AreaChart(@NamedArg(value="xAxis") Axis<X> axis, @NamedArg(value="yAxis") Axis<Y> axis2, @NamedArg(value="data") ObservableList<XYChart.Series<X, Y>> observableList) {
        super(axis, axis2);
        this.setData(observableList);
    }

    private static double doubleValue(Number number) {
        return AreaChart.doubleValue(number, 0.0);
    }

    private static double doubleValue(Number number, double d) {
        return number == null ? d : number.doubleValue();
    }

    @Override
    protected void updateAxisRange() {
        Axis axis = this.getXAxis();
        Axis axis2 = this.getYAxis();
        ArrayList arrayList = null;
        ArrayList arrayList2 = null;
        if (axis.isAutoRanging()) {
            arrayList = new ArrayList();
        }
        if (axis2.isAutoRanging()) {
            arrayList2 = new ArrayList();
        }
        if (arrayList != null || arrayList2 != null) {
            for (XYChart.Series series : this.getData()) {
                for (XYChart.Data data : series.getData()) {
                    if (arrayList != null) {
                        arrayList.add(data.getXValue());
                    }
                    if (arrayList2 == null) continue;
                    arrayList2.add(data.getYValue());
                }
            }
            if (arrayList != null && (arrayList.size() != 1 || this.getXAxis().toNumericValue(arrayList.get(0)) != 0.0)) {
                axis.invalidateRange(arrayList);
            }
            if (arrayList2 != null && (arrayList2.size() != 1 || this.getYAxis().toNumericValue(arrayList2.get(0)) != 0.0)) {
                axis2.invalidateRange(arrayList2);
            }
        }
    }

    @Override
    protected void dataItemAdded(XYChart.Series<X, Y> series, int n, XYChart.Data<X, Y> data) {
        Node node = this.createSymbol(series, this.getData().indexOf(series), data, n);
        if (this.shouldAnimate()) {
            Object object;
            boolean bl = false;
            if (n > 0 && n < series.getData().size() - 1) {
                bl = true;
                object = (XYChart.Data)series.getData().get(n - 1);
                XYChart.Data data2 = (XYChart.Data)series.getData().get(n + 1);
                double d = this.getXAxis().toNumericValue(((XYChart.Data)object).getXValue());
                double d2 = this.getYAxis().toNumericValue(((XYChart.Data)object).getYValue());
                double d3 = this.getXAxis().toNumericValue(data2.getXValue());
                double d4 = this.getYAxis().toNumericValue(data2.getYValue());
                double d5 = this.getXAxis().toNumericValue(data.getXValue());
                double d6 = this.getYAxis().toNumericValue(data.getYValue());
                double d7 = (d4 - d2) / (d3 - d) * d5 + (d3 * d2 - d4 * d) / (d3 - d);
                data.setCurrentY(this.getYAxis().toRealValue(d7));
                data.setCurrentX(this.getXAxis().toRealValue(d5));
            } else if (n == 0 && series.getData().size() > 1) {
                bl = true;
                data.setCurrentX(((XYChart.Data)series.getData().get(1)).getXValue());
                data.setCurrentY(((XYChart.Data)series.getData().get(1)).getYValue());
            } else if (n == series.getData().size() - 1 && series.getData().size() > 1) {
                bl = true;
                int n2 = series.getData().size() - 2;
                data.setCurrentX(((XYChart.Data)series.getData().get(n2)).getXValue());
                data.setCurrentY(((XYChart.Data)series.getData().get(n2)).getYValue());
            }
            if (node != null) {
                node.setOpacity(0.0);
                this.getPlotChildren().add(node);
                object = new FadeTransition(Duration.millis(500.0), node);
                ((FadeTransition)object).setToValue(1.0);
                ((Animation)object).play();
            }
            if (bl) {
                this.animate(new KeyFrame(Duration.ZERO, actionEvent -> {
                    if (node != null && !this.getPlotChildren().contains(node)) {
                        this.getPlotChildren().add(node);
                    }
                }, new KeyValue(data.currentYProperty(), data.getCurrentY()), new KeyValue(data.currentXProperty(), data.getCurrentX())), new KeyFrame(Duration.millis(800.0), new KeyValue(data.currentYProperty(), data.getYValue(), Interpolator.EASE_BOTH), new KeyValue(data.currentXProperty(), data.getXValue(), Interpolator.EASE_BOTH)));
            }
        } else if (node != null) {
            this.getPlotChildren().add(node);
        }
    }

    @Override
    protected void dataItemRemoved(XYChart.Data<X, Y> data, XYChart.Series<X, Y> series) {
        Node node = data.getNode();
        if (node != null) {
            node.focusTraversableProperty().unbind();
        }
        int n = series.getItemIndex(data);
        if (this.shouldAnimate()) {
            boolean bl = false;
            int n2 = series.getDataSize();
            int n3 = series.getData().size();
            if (n > 0 && n < n2 - 1) {
                bl = true;
                XYChart.Data<X, Y> data2 = series.getItem(n - 1);
                XYChart.Data<X, Y> data3 = series.getItem(n + 1);
                double d = this.getXAxis().toNumericValue(data2.getXValue());
                double d2 = this.getYAxis().toNumericValue(data2.getYValue());
                double d3 = this.getXAxis().toNumericValue(data3.getXValue());
                double d4 = this.getYAxis().toNumericValue(data3.getYValue());
                double d5 = this.getXAxis().toNumericValue(data.getXValue());
                double d6 = this.getYAxis().toNumericValue(data.getYValue());
                double d7 = (d4 - d2) / (d3 - d) * d5 + (d3 * d2 - d4 * d) / (d3 - d);
                data.setCurrentX(this.getXAxis().toRealValue(d5));
                data.setCurrentY(this.getYAxis().toRealValue(d6));
                data.setXValue(this.getXAxis().toRealValue(d5));
                data.setYValue(this.getYAxis().toRealValue(d7));
            } else if (n == 0 && n3 > 1) {
                bl = true;
                data.setXValue(((XYChart.Data)series.getData().get(0)).getXValue());
                data.setYValue(((XYChart.Data)series.getData().get(0)).getYValue());
            } else if (n == n2 - 1 && n3 > 1) {
                bl = true;
                int n4 = n3 - 1;
                data.setXValue(((XYChart.Data)series.getData().get(n4)).getXValue());
                data.setYValue(((XYChart.Data)series.getData().get(n4)).getYValue());
            } else if (node != null) {
                node.setOpacity(0.0);
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(500.0), node);
                fadeTransition.setToValue(0.0);
                fadeTransition.setOnFinished(actionEvent -> {
                    this.getPlotChildren().remove(node);
                    this.removeDataItemFromDisplay(series, data);
                });
                fadeTransition.play();
            } else {
                data.setSeries(null);
                this.removeDataItemFromDisplay(series, data);
            }
            if (bl) {
                this.animate(new KeyFrame(Duration.ZERO, new KeyValue(data.currentYProperty(), data.getCurrentY()), new KeyValue(data.currentXProperty(), data.getCurrentX())), new KeyFrame(Duration.millis(800.0), actionEvent -> {
                    data.setSeries(null);
                    this.getPlotChildren().remove(node);
                    this.removeDataItemFromDisplay(series, data);
                }, new KeyValue(data.currentYProperty(), data.getYValue(), Interpolator.EASE_BOTH), new KeyValue(data.currentXProperty(), data.getXValue(), Interpolator.EASE_BOTH)));
            }
        } else {
            data.setSeries(null);
            this.getPlotChildren().remove(node);
            this.removeDataItemFromDisplay(series, data);
        }
    }

    @Override
    protected void dataItemChanged(XYChart.Data<X, Y> data) {
    }

    @Override
    protected void seriesChanged(ListChangeListener.Change<? extends XYChart.Series> change) {
        for (int i = 0; i < this.getDataSize(); ++i) {
            XYChart.Series series = (XYChart.Series)this.getData().get(i);
            Path path = (Path)((Group)series.getNode()).getChildren().get(1);
            Path path2 = (Path)((Group)series.getNode()).getChildren().get(0);
            path.getStyleClass().setAll((String[])new String[]{"chart-series-area-line", "series" + i, series.defaultColorStyleClass});
            path2.getStyleClass().setAll((String[])new String[]{"chart-series-area-fill", "series" + i, series.defaultColorStyleClass});
            for (int j = 0; j < series.getData().size(); ++j) {
                XYChart.Data data = (XYChart.Data)series.getData().get(j);
                Node node = data.getNode();
                if (node == null) continue;
                node.getStyleClass().setAll((String[])new String[]{"chart-area-symbol", "series" + i, "data" + j, series.defaultColorStyleClass});
            }
        }
    }

    @Override
    protected void seriesAdded(XYChart.Series<X, Y> series, int n) {
        Path path = new Path();
        Path path2 = new Path();
        path.setStrokeLineJoin(StrokeLineJoin.BEVEL);
        Group group = new Group(path2, path);
        series.setNode(group);
        SimpleDoubleProperty simpleDoubleProperty = new SimpleDoubleProperty(this, "seriesYMultiplier");
        this.seriesYMultiplierMap.put(series, simpleDoubleProperty);
        if (this.shouldAnimate()) {
            simpleDoubleProperty.setValue(0.0);
        } else {
            simpleDoubleProperty.setValue(1.0);
        }
        this.getPlotChildren().add(group);
        ArrayList<KeyFrame> arrayList = new ArrayList<KeyFrame>();
        if (this.shouldAnimate()) {
            arrayList.add(new KeyFrame(Duration.ZERO, new KeyValue(group.opacityProperty(), 0), new KeyValue(simpleDoubleProperty, 0)));
            arrayList.add(new KeyFrame(Duration.millis(200.0), new KeyValue(group.opacityProperty(), 1)));
            arrayList.add(new KeyFrame(Duration.millis(500.0), new KeyValue(simpleDoubleProperty, 1)));
        }
        for (int i = 0; i < series.getData().size(); ++i) {
            XYChart.Data data = (XYChart.Data)series.getData().get(i);
            Node node = this.createSymbol(series, n, data, i);
            if (node == null) continue;
            if (this.shouldAnimate()) {
                node.setOpacity(0.0);
                this.getPlotChildren().add(node);
                arrayList.add(new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 0)));
                arrayList.add(new KeyFrame(Duration.millis(200.0), new KeyValue(node.opacityProperty(), 1)));
                continue;
            }
            this.getPlotChildren().add(node);
        }
        if (this.shouldAnimate()) {
            this.animate(arrayList.toArray(new KeyFrame[arrayList.size()]));
        }
    }

    @Override
    protected void seriesRemoved(XYChart.Series<X, Y> series) {
        this.seriesYMultiplierMap.remove(series);
        if (this.shouldAnimate()) {
            Timeline timeline = new Timeline(this.createSeriesRemoveTimeLine(series, 400L));
            timeline.play();
        } else {
            this.getPlotChildren().remove(series.getNode());
            for (XYChart.Data data : series.getData()) {
                this.getPlotChildren().remove(data.getNode());
            }
            this.removeSeriesFromDisplay(series);
        }
    }

    @Override
    protected void layoutPlotChildren() {
        ArrayList<LineTo> arrayList = new ArrayList<LineTo>(this.getDataSize());
        for (int i = 0; i < this.getDataSize(); ++i) {
            XYChart.Series series = (XYChart.Series)this.getData().get(i);
            DoubleProperty doubleProperty = this.seriesYMultiplierMap.get(series);
            ObservableList<Node> observableList = ((Group)series.getNode()).getChildren();
            Path path = (Path)observableList.get(0);
            Path path2 = (Path)observableList.get(1);
            AreaChart.makePaths(this, series, arrayList, path, path2, doubleProperty.get(), LineChart.SortingPolicy.X_AXIS);
        }
    }

    static <X, Y> void makePaths(XYChart<X, Y> xYChart, XYChart.Series<X, Y> series, List<LineTo> list, Path path, Path path2, double d, LineChart.SortingPolicy sortingPolicy) {
        double d2;
        Object object;
        Axis<X> axis = xYChart.getXAxis();
        Axis<Y> axis2 = xYChart.getYAxis();
        double d3 = path2.getStrokeWidth() / 2.0;
        boolean bl = sortingPolicy == LineChart.SortingPolicy.X_AXIS;
        boolean bl2 = sortingPolicy == LineChart.SortingPolicy.Y_AXIS;
        double d4 = bl ? -d3 : Double.NEGATIVE_INFINITY;
        double d5 = bl ? axis.getWidth() + d3 : Double.POSITIVE_INFINITY;
        double d6 = bl2 ? -d3 : Double.NEGATIVE_INFINITY;
        double d7 = bl2 ? axis2.getHeight() + d3 : Double.POSITIVE_INFINITY;
        LineTo lineTo3 = null;
        LineTo lineTo4 = null;
        list.clear();
        Object object2 = xYChart.getDisplayedDataIterator(series);
        while (object2.hasNext()) {
            object = object2.next();
            d2 = axis.getDisplayPosition(((XYChart.Data)object).getCurrentX());
            double d8 = axis2.getDisplayPosition(axis2.toRealValue(axis2.toNumericValue(((XYChart.Data)object).getCurrentY()) * d));
            boolean bl3 = Double.isNaN(d2) || Double.isNaN(d8);
            Node node = ((XYChart.Data)object).getNode();
            if (node != null) {
                double d9 = node.prefWidth(-1.0);
                double d10 = node.prefHeight(-1.0);
                if (bl3) {
                    node.resizeRelocate(-d9 * 2.0, -d10 * 2.0, d9, d10);
                } else {
                    node.resizeRelocate(d2 - d9 / 2.0, d8 - d10 / 2.0, d9, d10);
                }
            }
            if (bl3) continue;
            if (d2 < d4 || d8 < d6) {
                if (lineTo3 == null) {
                    lineTo3 = new LineTo(d2, d8);
                    continue;
                }
                if (!(bl && lineTo3.getX() <= d2) && (!bl2 || !(lineTo3.getY() <= d8))) continue;
                lineTo3.setX(d2);
                lineTo3.setY(d8);
                continue;
            }
            if (d2 <= d5 && d8 <= d7) {
                list.add(new LineTo(d2, d8));
                continue;
            }
            if (lineTo4 == null) {
                lineTo4 = new LineTo(d2, d8);
                continue;
            }
            if (!(bl && d2 <= lineTo4.getX()) && (!bl2 || !(d8 <= lineTo4.getY()))) continue;
            lineTo4.setX(d2);
            lineTo4.setY(d8);
        }
        if (!list.isEmpty() || lineTo3 != null || lineTo4 != null) {
            if (bl) {
                Collections.sort(list, (lineTo, lineTo2) -> Double.compare(lineTo.getX(), lineTo2.getX()));
            } else if (bl2) {
                Collections.sort(list, (lineTo, lineTo2) -> Double.compare(lineTo.getY(), lineTo2.getY()));
            }
            if (lineTo3 != null) {
                list.add(0, lineTo3);
            }
            if (lineTo4 != null) {
                list.add(lineTo4);
            }
            object2 = list.get(0);
            object = list.get(list.size() - 1);
            d2 = ((LineTo)object2).getY();
            ObservableList<PathElement> observableList = path2.getElements();
            observableList.clear();
            observableList.add(new MoveTo(((LineTo)object2).getX(), d2));
            observableList.addAll((Collection<PathElement>)list);
            if (path != null) {
                ObservableList<PathElement> observableList2 = path.getElements();
                observableList2.clear();
                double d11 = axis2.getDisplayPosition(axis2.toRealValue(0.0));
                observableList2.add(new MoveTo(((LineTo)object2).getX(), d11));
                observableList2.addAll((Collection<PathElement>)list);
                observableList2.add(new LineTo(((LineTo)object).getX(), d11));
                observableList2.add(new ClosePath());
            }
        }
    }

    private Node createSymbol(XYChart.Series<X, Y> series, int n, XYChart.Data<X, Y> data, int n2) {
        Node node = data.getNode();
        if (node == null && this.getCreateSymbols()) {
            node = new StackPane();
            node.setAccessibleRole(AccessibleRole.TEXT);
            node.setAccessibleRoleDescription("Point");
            node.focusTraversableProperty().bind(Platform.accessibilityActiveProperty());
            data.setNode(node);
        }
        if (node != null) {
            node.getStyleClass().setAll((String[])new String[]{"chart-area-symbol", "series" + n, "data" + n2, series.defaultColorStyleClass});
        }
        return node;
    }

    @Override
    Legend.LegendItem createLegendItemForSeries(XYChart.Series<X, Y> series, int n) {
        Legend.LegendItem legendItem = new Legend.LegendItem(series.getName());
        legendItem.getSymbol().getStyleClass().addAll((String[])new String[]{"chart-area-symbol", "series" + n, "area-legend-symbol", series.defaultColorStyleClass});
        return legendItem;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return AreaChart.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<AreaChart<?, ?>, Boolean> CREATE_SYMBOLS = new CssMetaData<AreaChart<?, ?>, Boolean>("-fx-create-symbols", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(AreaChart<?, ?> areaChart) {
                return ((AreaChart)areaChart).createSymbols == null || !((AreaChart)areaChart).createSymbols.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(AreaChart<?, ?> areaChart) {
                return (StyleableProperty)((Object)areaChart.createSymbolsProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(XYChart.getClassCssMetaData());
            arrayList.add(CREATE_SYMBOLS);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

