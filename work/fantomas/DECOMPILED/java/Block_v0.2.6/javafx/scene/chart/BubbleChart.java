/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.chart;

import com.sun.javafx.charts.Legend;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import javafx.util.Duration;

public class BubbleChart<X, Y>
extends XYChart<X, Y> {
    public BubbleChart(@NamedArg(value="xAxis") Axis<X> axis, @NamedArg(value="yAxis") Axis<Y> axis2) {
        this(axis, axis2, FXCollections.observableArrayList());
    }

    public BubbleChart(@NamedArg(value="xAxis") Axis<X> axis, @NamedArg(value="yAxis") Axis<Y> axis2, @NamedArg(value="data") ObservableList<XYChart.Series<X, Y>> observableList) {
        super(axis, axis2);
        if (!(axis instanceof ValueAxis) || !(axis2 instanceof ValueAxis)) {
            throw new IllegalArgumentException("Axis type incorrect, X and Y should both be NumberAxis");
        }
        this.setData(observableList);
    }

    private static double getDoubleValue(Object object, double d) {
        return !(object instanceof Number) ? d : ((Number)object).doubleValue();
    }

    @Override
    protected void layoutPlotChildren() {
        for (int i = 0; i < this.getDataSize(); ++i) {
            XYChart.Series series = (XYChart.Series)this.getData().get(i);
            Iterator iterator = this.getDisplayedDataIterator(series);
            while (iterator.hasNext()) {
                Ellipse ellipse;
                Node node;
                XYChart.Data data = iterator.next();
                double d = this.getXAxis().getDisplayPosition(data.getCurrentX());
                double d2 = this.getYAxis().getDisplayPosition(data.getCurrentY());
                if (Double.isNaN(d) || Double.isNaN(d2) || (node = data.getNode()) == null || !(node instanceof StackPane)) continue;
                StackPane stackPane = (StackPane)data.getNode();
                if (stackPane.getShape() == null) {
                    ellipse = new Ellipse(BubbleChart.getDoubleValue(data.getExtraValue(), 1.0), BubbleChart.getDoubleValue(data.getExtraValue(), 1.0));
                } else if (stackPane.getShape() instanceof Ellipse) {
                    ellipse = (Ellipse)stackPane.getShape();
                } else {
                    return;
                }
                ellipse.setRadiusX(BubbleChart.getDoubleValue(data.getExtraValue(), 1.0) * (this.getXAxis() instanceof NumberAxis ? Math.abs(((NumberAxis)this.getXAxis()).getScale()) : 1.0));
                ellipse.setRadiusY(BubbleChart.getDoubleValue(data.getExtraValue(), 1.0) * (this.getYAxis() instanceof NumberAxis ? Math.abs(((NumberAxis)this.getYAxis()).getScale()) : 1.0));
                stackPane.setShape(null);
                stackPane.setShape(ellipse);
                stackPane.setScaleShape(false);
                stackPane.setCenterShape(false);
                stackPane.setCacheShape(false);
                node.setLayoutX(d);
                node.setLayoutY(d2);
            }
        }
    }

    @Override
    protected void dataItemAdded(XYChart.Series<X, Y> series, int n, XYChart.Data<X, Y> data) {
        Node node = this.createBubble(series, this.getData().indexOf(series), data, n);
        if (this.shouldAnimate()) {
            node.setOpacity(0.0);
            this.getPlotChildren().add(node);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500.0), node);
            fadeTransition.setToValue(1.0);
            fadeTransition.play();
        } else {
            this.getPlotChildren().add(node);
        }
    }

    @Override
    protected void dataItemRemoved(XYChart.Data<X, Y> data, XYChart.Series<X, Y> series) {
        Node node = data.getNode();
        if (this.shouldAnimate()) {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500.0), node);
            fadeTransition.setToValue(0.0);
            fadeTransition.setOnFinished(actionEvent -> {
                this.getPlotChildren().remove(node);
                this.removeDataItemFromDisplay(series, data);
                node.setOpacity(1.0);
            });
            fadeTransition.play();
        } else {
            this.getPlotChildren().remove(node);
            this.removeDataItemFromDisplay(series, data);
        }
    }

    @Override
    protected void dataItemChanged(XYChart.Data<X, Y> data) {
    }

    @Override
    protected void seriesAdded(XYChart.Series<X, Y> series, int n) {
        for (int i = 0; i < series.getData().size(); ++i) {
            XYChart.Data data = (XYChart.Data)series.getData().get(i);
            Node node = this.createBubble(series, n, data, i);
            if (this.shouldAnimate()) {
                node.setOpacity(0.0);
                this.getPlotChildren().add(node);
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(500.0), node);
                fadeTransition.setToValue(1.0);
                fadeTransition.play();
                continue;
            }
            this.getPlotChildren().add(node);
        }
    }

    @Override
    protected void seriesRemoved(XYChart.Series<X, Y> series) {
        if (this.shouldAnimate()) {
            ParallelTransition parallelTransition = new ParallelTransition();
            parallelTransition.setOnFinished(actionEvent -> this.removeSeriesFromDisplay(series));
            for (XYChart.Data data : series.getData()) {
                Node node = data.getNode();
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(500.0), node);
                fadeTransition.setToValue(0.0);
                fadeTransition.setOnFinished(actionEvent -> {
                    this.getPlotChildren().remove(node);
                    node.setOpacity(1.0);
                });
                parallelTransition.getChildren().add(fadeTransition);
            }
            parallelTransition.play();
        } else {
            for (XYChart.Data data : series.getData()) {
                Node node = data.getNode();
                this.getPlotChildren().remove(node);
            }
            this.removeSeriesFromDisplay(series);
        }
    }

    private Node createBubble(XYChart.Series<X, Y> series, int n, final XYChart.Data<X, Y> data, int n2) {
        Node node = data.getNode();
        if (node == null) {
            node = new StackPane(){

                @Override
                public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
                    switch (accessibleAttribute) {
                        case TEXT: {
                            String string = this.getAccessibleText();
                            if (data.getExtraValue() == null) {
                                return string;
                            }
                            return string + " Bubble radius is " + data.getExtraValue();
                        }
                    }
                    return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
                }
            };
            node.setAccessibleRole(AccessibleRole.TEXT);
            node.setAccessibleRoleDescription("Bubble");
            node.focusTraversableProperty().bind(Platform.accessibilityActiveProperty());
            data.setNode(node);
        }
        node.getStyleClass().setAll((String[])new String[]{"chart-bubble", "series" + n, "data" + n2, series.defaultColorStyleClass});
        return node;
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
        boolean bl = axis instanceof CategoryAxis;
        boolean bl2 = axis2 instanceof CategoryAxis;
        if (arrayList != null || arrayList2 != null) {
            for (XYChart.Series series : this.getData()) {
                for (XYChart.Data data : series.getData()) {
                    if (arrayList != null) {
                        if (bl) {
                            arrayList.add(data.getXValue());
                        } else {
                            arrayList.add(axis.toRealValue(axis.toNumericValue(data.getXValue()) + BubbleChart.getDoubleValue(data.getExtraValue(), 0.0)));
                            arrayList.add(axis.toRealValue(axis.toNumericValue(data.getXValue()) - BubbleChart.getDoubleValue(data.getExtraValue(), 0.0)));
                        }
                    }
                    if (arrayList2 == null) continue;
                    if (bl2) {
                        arrayList2.add(data.getYValue());
                        continue;
                    }
                    arrayList2.add(axis2.toRealValue(axis2.toNumericValue(data.getYValue()) + BubbleChart.getDoubleValue(data.getExtraValue(), 0.0)));
                    arrayList2.add(axis2.toRealValue(axis2.toNumericValue(data.getYValue()) - BubbleChart.getDoubleValue(data.getExtraValue(), 0.0)));
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

    @Override
    Legend.LegendItem createLegendItemForSeries(XYChart.Series<X, Y> series, int n) {
        Legend.LegendItem legendItem = new Legend.LegendItem(series.getName());
        legendItem.getSymbol().getStyleClass().addAll((String[])new String[]{"series" + n, "chart-bubble", "bubble-legend-symbol", series.defaultColorStyleClass});
        return legendItem;
    }
}

