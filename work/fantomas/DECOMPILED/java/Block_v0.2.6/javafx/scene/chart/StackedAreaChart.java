/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.chart;

import com.sun.javafx.charts.Legend;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.binding.DoubleExpression;
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
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;

public class StackedAreaChart<X, Y>
extends XYChart<X, Y> {
    private Map<XYChart.Series<X, Y>, DoubleProperty> seriesYMultiplierMap = new HashMap<XYChart.Series<X, Y>, DoubleProperty>();
    private BooleanProperty createSymbols = new StyleableBooleanProperty(true){

        @Override
        protected void invalidated() {
            for (int i = 0; i < StackedAreaChart.this.getData().size(); ++i) {
                XYChart.Series series = (XYChart.Series)StackedAreaChart.this.getData().get(i);
                for (int j = 0; j < series.getData().size(); ++j) {
                    XYChart.Data data = (XYChart.Data)series.getData().get(j);
                    Node node = data.getNode();
                    if (this.get() && node == null) {
                        node = StackedAreaChart.this.createSymbol(series, StackedAreaChart.this.getData().indexOf(series), data, j);
                        if (null == node) continue;
                        StackedAreaChart.this.getPlotChildren().add(node);
                        continue;
                    }
                    if (this.get() || node == null) continue;
                    StackedAreaChart.this.getPlotChildren().remove(node);
                    node = null;
                    data.setNode(null);
                }
            }
            StackedAreaChart.this.requestChartLayout();
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
        public CssMetaData<StackedAreaChart<?, ?>, Boolean> getCssMetaData() {
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

    public StackedAreaChart(@NamedArg(value="xAxis") Axis<X> axis, @NamedArg(value="yAxis") Axis<Y> axis2) {
        this(axis, axis2, FXCollections.observableArrayList());
    }

    public StackedAreaChart(@NamedArg(value="xAxis") Axis<X> axis, @NamedArg(value="yAxis") Axis<Y> axis2, @NamedArg(value="data") ObservableList<XYChart.Series<X, Y>> observableList) {
        super(axis, axis2);
        if (!(axis2 instanceof ValueAxis)) {
            throw new IllegalArgumentException("Axis type incorrect, yAxis must be of ValueAxis type.");
        }
        this.setData(observableList);
    }

    private static double doubleValue(Number number) {
        return StackedAreaChart.doubleValue(number, 0.0);
    }

    private static double doubleValue(Number number, double d) {
        return number == null ? d : number.doubleValue();
    }

    @Override
    protected void dataItemAdded(XYChart.Series<X, Y> series, int n, XYChart.Data<X, Y> data) {
        Node node = this.createSymbol(series, this.getData().indexOf(series), data, n);
        if (this.shouldAnimate()) {
            boolean bl = false;
            if (n > 0 && n < series.getData().size() - 1) {
                bl = true;
                XYChart.Data data2 = (XYChart.Data)series.getData().get(n - 1);
                XYChart.Data data3 = (XYChart.Data)series.getData().get(n + 1);
                double d = this.getXAxis().toNumericValue(data2.getXValue());
                double d2 = this.getYAxis().toNumericValue(data2.getYValue());
                double d3 = this.getXAxis().toNumericValue(data3.getXValue());
                double d4 = this.getYAxis().toNumericValue(data3.getYValue());
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
            } else if (node != null) {
                node.setOpacity(0.0);
                this.getPlotChildren().add(node);
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(500.0), node);
                fadeTransition.setToValue(1.0);
                fadeTransition.play();
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
                    node.setOpacity(1.0);
                });
                fadeTransition.play();
            } else {
                data.setSeries(null);
                this.removeDataItemFromDisplay(series, data);
            }
            if (bl) {
                this.animate(new KeyFrame(Duration.ZERO, new KeyValue(data.currentYProperty(), data.getCurrentY()), new KeyValue(data.currentXProperty(), data.getCurrentX())), new KeyFrame(Duration.millis(800.0), actionEvent -> {
                    this.getPlotChildren().remove(node);
                    this.removeDataItemFromDisplay(series, data);
                }, new KeyValue(data.currentYProperty(), data.getYValue(), Interpolator.EASE_BOTH), new KeyValue(data.currentXProperty(), data.getXValue(), Interpolator.EASE_BOTH)));
            }
        } else {
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
        path2.setStrokeLineJoin(StrokeLineJoin.BEVEL);
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
            }
            this.getPlotChildren().add(node);
            if (!this.shouldAnimate()) continue;
            arrayList.add(new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 0)));
            arrayList.add(new KeyFrame(Duration.millis(200.0), new KeyValue(node.opacityProperty(), 1)));
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
    protected void updateAxisRange() {
        Axis axis = this.getXAxis();
        Axis axis2 = this.getYAxis();
        if (axis.isAutoRanging()) {
            ArrayList arrayList = new ArrayList();
            for (XYChart.Series object : this.getData()) {
                for (XYChart.Data data : object.getData()) {
                    arrayList.add(data.getXValue());
                }
            }
            axis.invalidateRange(arrayList);
        }
        if (axis2.isAutoRanging()) {
            double d = Double.MAX_VALUE;
            Iterator iterator = this.getDisplayedSeriesIterator();
            boolean bl = true;
            TreeMap<Double, Double> treeMap = new TreeMap<Double, Double>();
            TreeMap<Double, Double> treeMap2 = new TreeMap<Double, Double>();
            TreeMap<Double, Double> treeMap3 = new TreeMap<Double, Double>();
            while (iterator.hasNext()) {
                treeMap3.clear();
                XYChart.Series series = iterator.next();
                for (XYChart.Data data : series.getData()) {
                    if (data == null) continue;
                    double d2 = axis.toNumericValue(data.getXValue());
                    double d3 = axis2.toNumericValue(data.getYValue());
                    treeMap3.put(d2, d3);
                    if (bl) {
                        treeMap.put(d2, d3);
                        d = Math.min(d, d3);
                        continue;
                    }
                    if (treeMap2.containsKey(d2)) {
                        treeMap.put(d2, (Double)treeMap2.get(d2) + d3);
                        continue;
                    }
                    Map.Entry entry = treeMap2.higherEntry(d2);
                    Map.Entry entry2 = treeMap2.lowerEntry(d2);
                    if (entry != null && entry2 != null) {
                        treeMap.put(d2, (d2 - entry2.getKey()) / (entry.getKey() - entry2.getKey()) * ((Double)entry2.getValue() + (Double)entry.getValue()) + d3);
                        continue;
                    }
                    if (entry != null) {
                        treeMap.put(d2, (Double)entry.getValue() + d3);
                        continue;
                    }
                    if (entry2 != null) {
                        treeMap.put(d2, (Double)entry2.getValue() + d3);
                        continue;
                    }
                    treeMap.put(d2, d3);
                }
                for (Map.Entry entry : treeMap2.entrySet()) {
                    if (treeMap.keySet().contains(entry.getKey())) continue;
                    Double d4 = (Double)entry.getKey();
                    Double d5 = (Double)entry.getValue();
                    Map.Entry entry3 = treeMap3.higherEntry(d4);
                    Map.Entry entry4 = treeMap3.lowerEntry(d4);
                    if (entry3 != null && entry4 != null) {
                        treeMap.put(d4, (d4 - entry4.getKey()) / (entry3.getKey() - entry4.getKey()) * ((Double)entry4.getValue() + (Double)entry3.getValue()) + d5);
                        continue;
                    }
                    if (entry3 != null) {
                        treeMap.put(d4, (Double)entry3.getValue() + d5);
                        continue;
                    }
                    if (entry4 != null) {
                        treeMap.put(d4, (Double)entry4.getValue() + d5);
                        continue;
                    }
                    treeMap.put(d4, d5);
                }
                treeMap2.clear();
                treeMap2.putAll(treeMap);
                treeMap.clear();
                bl = d == Double.MAX_VALUE;
            }
            if (d != Double.MAX_VALUE) {
                axis2.invalidateRange(Arrays.asList(axis2.toRealValue(d), axis2.toRealValue((Double)Collections.max(treeMap2.values()))));
            }
        }
    }

    @Override
    protected void layoutPlotChildren() {
        ArrayList<DataPointInfo<X, Y>> arrayList = new ArrayList<DataPointInfo<X, Y>>();
        ArrayList<DataPointInfo<X, Y>> arrayList2 = new ArrayList<DataPointInfo<X, Y>>();
        for (int i = 0; i < this.getDataSize(); ++i) {
            Object object;
            Object object2;
            XYChart.Series series = (XYChart.Series)this.getData().get(i);
            arrayList2.clear();
            for (DataPointInfo dataPointInfo : arrayList) {
                dataPointInfo.partOf = PartOf.PREVIOUS;
                arrayList2.add(dataPointInfo);
            }
            arrayList.clear();
            Object object4 = this.getDisplayedDataIterator(series);
            while (object4.hasNext()) {
                XYChart.Data data = (XYChart.Data)object4.next();
                object2 = new DataPointInfo(data, data.getXValue(), data.getYValue(), PartOf.CURRENT);
                arrayList2.add((DataPointInfo<X, Y>)object2);
            }
            object4 = this.seriesYMultiplierMap.get(series);
            Path object32 = (Path)((Group)series.getNode()).getChildren().get(1);
            object2 = (Path)((Group)series.getNode()).getChildren().get(0);
            object32.getElements().clear();
            ((Path)object2).getElements().clear();
            int n = 0;
            this.sortAggregateList(arrayList2);
            Axis axis = this.getYAxis();
            Axis axis2 = this.getXAxis();
            boolean bl = false;
            boolean bl2 = false;
            int n2 = this.findNextCurrent(arrayList2, -1);
            int n3 = this.findPreviousCurrent(arrayList2, arrayList2.size());
            double d = axis.getZeroPosition();
            if (Double.isNaN(d)) {
                ValueAxis valueAxis = (ValueAxis)axis;
                d = valueAxis.getLowerBound() > 0.0 ? valueAxis.getDisplayPosition(valueAxis.getLowerBound()) : valueAxis.getDisplayPosition(valueAxis.getUpperBound());
            }
            for (DataPointInfo<X, Y> dataPointInfo : arrayList2) {
                double d2;
                DataPointInfo<X, Y> dataPointInfo2;
                DataPointInfo<X, Y> dataPointInfo3;
                double d3;
                double d4;
                int n4;
                int n5;
                if (n == n3) {
                    bl2 = true;
                }
                if (n == n2) {
                    bl = true;
                }
                object = dataPointInfo.dataItem;
                if (dataPointInfo.partOf.equals((Object)PartOf.CURRENT)) {
                    n5 = this.findPreviousPrevious(arrayList2, n);
                    n4 = this.findNextPrevious(arrayList2, n);
                    if (n5 == -1 || n4 == -1 && !arrayList2.get((int)n5).x.equals(dataPointInfo.x)) {
                        if (bl) {
                            XYChart.Data data = new XYChart.Data(dataPointInfo.x, 0);
                            this.addDropDown(arrayList, data, data.getXValue(), data.getYValue(), axis2.getDisplayPosition(data.getCurrentX()), d);
                        }
                        d4 = axis2.getDisplayPosition(((XYChart.Data)object).getCurrentX());
                        d3 = axis.getDisplayPosition(axis.toRealValue(axis.toNumericValue(((XYChart.Data)object).getCurrentY()) * ((DoubleExpression)object4).getValue()));
                        this.addPoint(arrayList, (XYChart.Data<X, Y>)object, ((XYChart.Data)object).getXValue(), ((XYChart.Data)object).getYValue(), d4, d3, PartOf.CURRENT, false, !bl);
                        if (n == n3) {
                            XYChart.Data data = new XYChart.Data(dataPointInfo.x, 0);
                            this.addDropDown(arrayList, data, data.getXValue(), data.getYValue(), axis2.getDisplayPosition(data.getCurrentX()), d);
                        }
                    } else {
                        dataPointInfo3 = arrayList2.get(n5);
                        if (dataPointInfo3.x.equals(dataPointInfo.x)) {
                            if (dataPointInfo3.dropDown) {
                                n5 = this.findPreviousPrevious(arrayList2, n5);
                                dataPointInfo3 = arrayList2.get(n5);
                            }
                            if (dataPointInfo3.x.equals(dataPointInfo.x)) {
                                d4 = axis2.getDisplayPosition(((XYChart.Data)object).getCurrentX());
                                d3 = axis.toNumericValue(((XYChart.Data)object).getCurrentY()) + axis.toNumericValue(dataPointInfo3.y);
                                double d5 = axis.getDisplayPosition(axis.toRealValue(d3 * ((DoubleExpression)object4).getValue()));
                                this.addPoint(arrayList, (XYChart.Data<X, Y>)object, dataPointInfo.x, axis.toRealValue(d3), d4, d5, PartOf.CURRENT, false, !bl);
                            }
                            if (bl2) {
                                this.addDropDown(arrayList, (XYChart.Data<X, Y>)object, dataPointInfo3.x, dataPointInfo3.y, dataPointInfo3.displayX, dataPointInfo3.displayY);
                            }
                        } else {
                            dataPointInfo2 = n4 == -1 ? null : arrayList2.get(n4);
                            dataPointInfo3 = n5 == -1 ? null : arrayList2.get(n5);
                            d4 = axis.toNumericValue(((XYChart.Data)object).getCurrentY());
                            if (dataPointInfo3 != null && dataPointInfo2 != null) {
                                d3 = axis2.getDisplayPosition(((XYChart.Data)object).getCurrentX());
                                double d6 = this.interpolate(dataPointInfo3.displayX, dataPointInfo3.displayY, dataPointInfo2.displayX, dataPointInfo2.displayY, d3);
                                d2 = this.interpolate(axis2.toNumericValue(dataPointInfo3.x), axis.toNumericValue(dataPointInfo3.y), axis2.toNumericValue(dataPointInfo2.x), axis.toNumericValue(dataPointInfo2.y), axis2.toNumericValue(dataPointInfo.x));
                                if (bl) {
                                    XYChart.Data data = new XYChart.Data(dataPointInfo.x, d2);
                                    this.addDropDown(arrayList, data, dataPointInfo.x, axis.toRealValue(d2), d3, d6);
                                }
                                double d7 = axis.getDisplayPosition(axis.toRealValue((d4 + d2) * ((DoubleExpression)object4).getValue()));
                                this.addPoint(arrayList, (XYChart.Data<X, Y>)object, dataPointInfo.x, axis.toRealValue(d4 + d2), d3, d7, PartOf.CURRENT, false, !bl);
                                if (n == n3) {
                                    XYChart.Data data = new XYChart.Data(dataPointInfo.x, d2);
                                    this.addDropDown(arrayList, data, dataPointInfo.x, axis.toRealValue(d2), d3, d6);
                                }
                            }
                        }
                    }
                } else {
                    n5 = this.findPreviousCurrent(arrayList2, n);
                    n4 = this.findNextCurrent(arrayList2, n);
                    if (dataPointInfo.dropDown) {
                        if (axis2.toNumericValue(dataPointInfo.x) <= axis2.toNumericValue(arrayList2.get((int)n2).x) || axis2.toNumericValue(dataPointInfo.x) > axis2.toNumericValue(arrayList2.get((int)n3).x)) {
                            this.addDropDown(arrayList, (XYChart.Data<X, Y>)object, dataPointInfo.x, dataPointInfo.y, dataPointInfo.displayX, dataPointInfo.displayY);
                        }
                    } else if (n5 == -1 || n4 == -1) {
                        this.addPoint(arrayList, (XYChart.Data<X, Y>)object, dataPointInfo.x, dataPointInfo.y, dataPointInfo.displayX, dataPointInfo.displayY, PartOf.CURRENT, true, false);
                    } else {
                        dataPointInfo2 = arrayList2.get(n4);
                        if (!dataPointInfo2.x.equals(dataPointInfo.x)) {
                            dataPointInfo3 = arrayList2.get(n5);
                            d4 = axis2.getDisplayPosition(((XYChart.Data)object).getCurrentX());
                            d3 = this.interpolate(axis2.toNumericValue(dataPointInfo3.x), axis.toNumericValue(dataPointInfo3.y), axis2.toNumericValue(dataPointInfo2.x), axis.toNumericValue(dataPointInfo2.y), axis2.toNumericValue(dataPointInfo.x));
                            double d8 = axis.toNumericValue(dataPointInfo.y) + d3;
                            d2 = axis.getDisplayPosition(axis.toRealValue(d8 * ((DoubleExpression)object4).getValue()));
                            this.addPoint(arrayList, new XYChart.Data(dataPointInfo.x, d3), dataPointInfo.x, axis.toRealValue(d8), d4, d2, PartOf.CURRENT, true, true);
                        }
                    }
                }
                ++n;
                if (bl) {
                    bl = false;
                }
                if (!bl2) continue;
                bl2 = false;
            }
            if (!arrayList.isEmpty()) {
                object32.getElements().add(new MoveTo(((DataPointInfo)arrayList.get((int)0)).displayX, ((DataPointInfo)arrayList.get((int)0)).displayY));
                ((Path)object2).getElements().add(new MoveTo(((DataPointInfo)arrayList.get((int)0)).displayX, ((DataPointInfo)arrayList.get((int)0)).displayY));
            }
            for (DataPointInfo<X, Y> dataPointInfo : arrayList) {
                if (dataPointInfo.lineTo) {
                    object32.getElements().add(new LineTo(dataPointInfo.displayX, dataPointInfo.displayY));
                } else {
                    object32.getElements().add(new MoveTo(dataPointInfo.displayX, dataPointInfo.displayY));
                }
                ((Path)object2).getElements().add(new LineTo(dataPointInfo.displayX, dataPointInfo.displayY));
                if (dataPointInfo.skipSymbol || (object = dataPointInfo.dataItem.getNode()) == null) continue;
                double d9 = ((Node)object).prefWidth(-1.0);
                double d10 = ((Node)object).prefHeight(-1.0);
                ((Node)object).resizeRelocate(dataPointInfo.displayX - d9 / 2.0, dataPointInfo.displayY - d10 / 2.0, d9, d10);
            }
            for (int j = arrayList2.size() - 1; j > 0; --j) {
                DataPointInfo<X, Y> dataPointInfo;
                dataPointInfo = arrayList2.get(j);
                if (!PartOf.PREVIOUS.equals((Object)dataPointInfo.partOf)) continue;
                ((Path)object2).getElements().add(new LineTo(dataPointInfo.displayX, dataPointInfo.displayY));
            }
            if (((Path)object2).getElements().isEmpty()) continue;
            ((Path)object2).getElements().add(new ClosePath());
        }
    }

    private void addDropDown(ArrayList<DataPointInfo<X, Y>> arrayList, XYChart.Data<X, Y> data, X x, Y y, double d, double d2) {
        DataPointInfo<X, Y> dataPointInfo = new DataPointInfo<X, Y>(true);
        dataPointInfo.setValues(data, x, y, d, d2, PartOf.CURRENT, true, false);
        arrayList.add(dataPointInfo);
    }

    private void addPoint(ArrayList<DataPointInfo<X, Y>> arrayList, XYChart.Data<X, Y> data, X x, Y y, double d, double d2, PartOf partOf, boolean bl, boolean bl2) {
        DataPointInfo<X, Y> dataPointInfo = new DataPointInfo<X, Y>();
        dataPointInfo.setValues(data, x, y, d, d2, partOf, bl, bl2);
        arrayList.add(dataPointInfo);
    }

    private int findNextCurrent(ArrayList<DataPointInfo<X, Y>> arrayList, int n) {
        for (int i = n + 1; i < arrayList.size(); ++i) {
            if (!arrayList.get((int)i).partOf.equals((Object)PartOf.CURRENT)) continue;
            return i;
        }
        return -1;
    }

    private int findPreviousCurrent(ArrayList<DataPointInfo<X, Y>> arrayList, int n) {
        for (int i = n - 1; i >= 0; --i) {
            if (!arrayList.get((int)i).partOf.equals((Object)PartOf.CURRENT)) continue;
            return i;
        }
        return -1;
    }

    private int findPreviousPrevious(ArrayList<DataPointInfo<X, Y>> arrayList, int n) {
        for (int i = n - 1; i >= 0; --i) {
            if (!arrayList.get((int)i).partOf.equals((Object)PartOf.PREVIOUS)) continue;
            return i;
        }
        return -1;
    }

    private int findNextPrevious(ArrayList<DataPointInfo<X, Y>> arrayList, int n) {
        for (int i = n + 1; i < arrayList.size(); ++i) {
            if (!arrayList.get((int)i).partOf.equals((Object)PartOf.PREVIOUS)) continue;
            return i;
        }
        return -1;
    }

    private void sortAggregateList(ArrayList<DataPointInfo<X, Y>> arrayList) {
        Collections.sort(arrayList, (dataPointInfo, dataPointInfo2) -> {
            double d;
            XYChart.Data data = dataPointInfo.dataItem;
            XYChart.Data data2 = dataPointInfo2.dataItem;
            double d2 = this.getXAxis().toNumericValue(data.getXValue());
            return d2 < (d = this.getXAxis().toNumericValue(data2.getXValue())) ? -1 : (d2 == d ? 0 : 1);
        });
    }

    private double interpolate(double d, double d2, double d3, double d4, double d5) {
        return (d4 - d2) / (d3 - d) * (d5 - d) + d2;
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
        return StackedAreaChart.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<StackedAreaChart<?, ?>, Boolean> CREATE_SYMBOLS = new CssMetaData<StackedAreaChart<?, ?>, Boolean>("-fx-create-symbols", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(StackedAreaChart<?, ?> stackedAreaChart) {
                return ((StackedAreaChart)stackedAreaChart).createSymbols == null || !((StackedAreaChart)stackedAreaChart).createSymbols.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(StackedAreaChart<?, ?> stackedAreaChart) {
                return (StyleableProperty)((Object)stackedAreaChart.createSymbolsProperty());
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

    private static enum PartOf {
        CURRENT,
        PREVIOUS;

    }

    static final class DataPointInfo<X, Y> {
        X x;
        Y y;
        double displayX;
        double displayY;
        XYChart.Data<X, Y> dataItem;
        PartOf partOf;
        boolean skipSymbol = false;
        boolean lineTo = false;
        boolean dropDown = false;

        DataPointInfo() {
        }

        DataPointInfo(XYChart.Data<X, Y> data, X x, Y y, PartOf partOf) {
            this.dataItem = data;
            this.x = x;
            this.y = y;
            this.partOf = partOf;
        }

        DataPointInfo(boolean bl) {
            this.dropDown = bl;
        }

        void setValues(XYChart.Data<X, Y> data, X x, Y y, double d, double d2, PartOf partOf, boolean bl, boolean bl2) {
            this.dataItem = data;
            this.x = x;
            this.y = y;
            this.displayX = d;
            this.displayY = d2;
            this.partOf = partOf;
            this.skipSymbol = bl;
            this.lineTo = bl2;
        }

        public final X getX() {
            return this.x;
        }

        public final Y getY() {
            return this.y;
        }
    }
}

