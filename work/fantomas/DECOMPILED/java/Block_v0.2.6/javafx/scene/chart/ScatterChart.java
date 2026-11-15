/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.chart;

import com.sun.javafx.charts.Legend;
import java.util.Collection;
import java.util.Iterator;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ScatterChart<X, Y>
extends XYChart<X, Y> {
    public ScatterChart(@NamedArg(value="xAxis") Axis<X> axis, @NamedArg(value="yAxis") Axis<Y> axis2) {
        this(axis, axis2, FXCollections.observableArrayList());
    }

    public ScatterChart(@NamedArg(value="xAxis") Axis<X> axis, @NamedArg(value="yAxis") Axis<Y> axis2, @NamedArg(value="data") ObservableList<XYChart.Series<X, Y>> observableList) {
        super(axis, axis2);
        this.setData(observableList);
    }

    @Override
    protected void dataItemAdded(XYChart.Series<X, Y> series, int n, XYChart.Data<X, Y> data) {
        Node node = data.getNode();
        if (node == null) {
            node = new StackPane();
            node.setAccessibleRole(AccessibleRole.TEXT);
            node.setAccessibleRoleDescription("Point");
            node.focusTraversableProperty().bind(Platform.accessibilityActiveProperty());
            data.setNode(node);
        }
        node.getStyleClass().setAll((String[])new String[]{"chart-symbol", "series" + this.getData().indexOf(series), "data" + n, series.defaultColorStyleClass});
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
        if (node != null) {
            node.focusTraversableProperty().unbind();
        }
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
            this.dataItemAdded(series, i, (XYChart.Data)series.getData().get(i));
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

    @Override
    protected void layoutPlotChildren() {
        for (int i = 0; i < this.getDataSize(); ++i) {
            XYChart.Series series = (XYChart.Series)this.getData().get(i);
            Iterator iterator = this.getDisplayedDataIterator(series);
            while (iterator.hasNext()) {
                Node node;
                XYChart.Data data = iterator.next();
                double d = this.getXAxis().getDisplayPosition(data.getCurrentX());
                double d2 = this.getYAxis().getDisplayPosition(data.getCurrentY());
                if (Double.isNaN(d) || Double.isNaN(d2) || (node = data.getNode()) == null) continue;
                double d3 = node.prefWidth(-1.0);
                double d4 = node.prefHeight(-1.0);
                node.resizeRelocate(d - d3 / 2.0, d2 - d4 / 2.0, d3, d4);
            }
        }
    }

    @Override
    Legend.LegendItem createLegendItemForSeries(XYChart.Series<X, Y> series, int n) {
        Node node;
        Legend.LegendItem legendItem = new Legend.LegendItem(series.getName());
        Node node2 = node = series.getData().isEmpty() ? null : ((XYChart.Data)series.getData().get(0)).getNode();
        if (node != null) {
            legendItem.getSymbol().getStyleClass().addAll((Collection<String>)node.getStyleClass());
        }
        return legendItem;
    }
}

