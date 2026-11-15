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
import java.util.List;
import java.util.Objects;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Side;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.layout.Region;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

public class PieChart
extends Chart {
    private static final int MIN_PIE_RADIUS = 25;
    private static final double LABEL_TICK_GAP = 6.0;
    private static final double LABEL_BALL_RADIUS = 2.0;
    private BitSet colorBits = new BitSet(8);
    private double pieRadius;
    private Data begin = null;
    private final Path labelLinePath = new Path(){

        @Override
        public boolean usesMirroring() {
            return false;
        }
    };
    private List<LabelLayoutInfo> labelLayoutInfos = null;
    private Legend legend = new Legend();
    private Data dataItemBeingRemoved = null;
    private Timeline dataRemoveTimeline = null;
    private final ListChangeListener<Data> dataChangeListener = change -> {
        while (change.next()) {
            Data data4;
            int n;
            Data data2;
            if (change.wasPermutated()) {
                Data data3 = this.begin;
                for (int i = 0; i < this.getData().size(); ++i) {
                    data2 = (Data)this.getData().get(i);
                    this.updateDataItemStyleClass(data2, i);
                    if (i == 0) {
                        data3 = this.begin = data2;
                        this.begin.next = null;
                        continue;
                    }
                    data3.next = data2;
                    data2.next = null;
                    data3 = data2;
                }
                this.updateLegend();
                this.requestChartLayout();
                return;
            }
            for (n = change.getFrom(); n < change.getTo(); ++n) {
                data4 = (Data)this.getData().get(n);
                data4.setChart(this);
                if (this.begin == null) {
                    this.begin = data4;
                    this.begin.next = null;
                    continue;
                }
                if (n == 0) {
                    data4.next = this.begin;
                    this.begin = data4;
                    continue;
                }
                data2 = this.begin;
                for (int i = 0; i < n - 1; ++i) {
                    data2 = data2.next;
                }
                data4.next = data2.next;
                data2.next = data4;
            }
            for (Data data4 : change.getRemoved()) {
                this.dataItemRemoved(data4);
            }
            for (n = change.getFrom(); n < change.getTo(); ++n) {
                data4 = (Data)this.getData().get(n);
                data4.defaultColorIndex = this.colorBits.nextClearBit(0);
                this.colorBits.set(data4.defaultColorIndex);
                this.dataItemAdded(data4, n);
            }
            if (!change.wasRemoved() && !change.wasAdded()) continue;
            for (n = 0; n < this.getData().size(); ++n) {
                data4 = (Data)this.getData().get(n);
                this.updateDataItemStyleClass(data4, n);
            }
            this.updateLegend();
        }
        this.requestChartLayout();
    };
    private ObjectProperty<ObservableList<Data>> data = new ObjectPropertyBase<ObservableList<Data>>(){
        private ObservableList<Data> old;

        @Override
        protected void invalidated() {
            ObservableList observableList = (ObservableList)this.getValue();
            if (this.old != null) {
                this.old.removeListener(PieChart.this.dataChangeListener);
            }
            if (observableList != null) {
                observableList.addListener(PieChart.this.dataChangeListener);
            }
            if (this.old != null || observableList != null) {
                int n;
                final List list = this.old != null ? this.old : Collections.emptyList();
                int n2 = n = observableList != null ? observableList.size() : 0;
                if (n > 0 || !list.isEmpty()) {
                    PieChart.this.dataChangeListener.onChanged(new NonIterableChange<Data>(0, n, observableList){

                        @Override
                        public List<Data> getRemoved() {
                            return list;
                        }

                        @Override
                        public boolean wasPermutated() {
                            return false;
                        }

                        @Override
                        protected int[] getPermutation() {
                            return new int[0];
                        }
                    });
                }
            } else if (this.old != null && this.old.size() > 0) {
                PieChart.this.dataChangeListener.onChanged(new NonIterableChange<Data>(0, 0, observableList){

                    @Override
                    public List<Data> getRemoved() {
                        return old;
                    }

                    @Override
                    public boolean wasPermutated() {
                        return false;
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
            return PieChart.this;
        }

        @Override
        public String getName() {
            return "data";
        }
    };
    private DoubleProperty startAngle = new StyleableDoubleProperty(0.0){

        @Override
        public void invalidated() {
            this.get();
            PieChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return PieChart.this;
        }

        @Override
        public String getName() {
            return "startAngle";
        }

        @Override
        public CssMetaData<PieChart, Number> getCssMetaData() {
            return StyleableProperties.START_ANGLE;
        }
    };
    private BooleanProperty clockwise = new StyleableBooleanProperty(true){

        @Override
        public void invalidated() {
            this.get();
            PieChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return PieChart.this;
        }

        @Override
        public String getName() {
            return "clockwise";
        }

        @Override
        public CssMetaData<PieChart, Boolean> getCssMetaData() {
            return StyleableProperties.CLOCKWISE;
        }
    };
    private DoubleProperty labelLineLength = new StyleableDoubleProperty(20.0){

        @Override
        public void invalidated() {
            this.get();
            PieChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return PieChart.this;
        }

        @Override
        public String getName() {
            return "labelLineLength";
        }

        @Override
        public CssMetaData<PieChart, Number> getCssMetaData() {
            return StyleableProperties.LABEL_LINE_LENGTH;
        }
    };
    private BooleanProperty labelsVisible = new StyleableBooleanProperty(true){

        @Override
        public void invalidated() {
            this.get();
            PieChart.this.requestChartLayout();
        }

        @Override
        public Object getBean() {
            return PieChart.this;
        }

        @Override
        public String getName() {
            return "labelsVisible";
        }

        @Override
        public CssMetaData<PieChart, Boolean> getCssMetaData() {
            return StyleableProperties.LABELS_VISIBLE;
        }
    };

    public final ObservableList<Data> getData() {
        return (ObservableList)this.data.getValue();
    }

    public final void setData(ObservableList<Data> observableList) {
        this.data.setValue(observableList);
    }

    public final ObjectProperty<ObservableList<Data>> dataProperty() {
        return this.data;
    }

    public final double getStartAngle() {
        return this.startAngle.getValue();
    }

    public final void setStartAngle(double d) {
        this.startAngle.setValue(d);
    }

    public final DoubleProperty startAngleProperty() {
        return this.startAngle;
    }

    public final void setClockwise(boolean bl) {
        this.clockwise.setValue(bl);
    }

    public final boolean isClockwise() {
        return this.clockwise.getValue();
    }

    public final BooleanProperty clockwiseProperty() {
        return this.clockwise;
    }

    public final double getLabelLineLength() {
        return this.labelLineLength.getValue();
    }

    public final void setLabelLineLength(double d) {
        this.labelLineLength.setValue(d);
    }

    public final DoubleProperty labelLineLengthProperty() {
        return this.labelLineLength;
    }

    public final void setLabelsVisible(boolean bl) {
        this.labelsVisible.setValue(bl);
    }

    public final boolean getLabelsVisible() {
        return this.labelsVisible.getValue();
    }

    public final BooleanProperty labelsVisibleProperty() {
        return this.labelsVisible;
    }

    public PieChart() {
        this(FXCollections.observableArrayList());
    }

    public PieChart(ObservableList<Data> observableList) {
        this.getChartChildren().add(this.labelLinePath);
        this.labelLinePath.getStyleClass().add("chart-pie-label-line");
        this.setLegend(this.legend);
        this.setData(observableList);
        this.useChartContentMirroring = false;
    }

    private void dataNameChanged(Data data) {
        data.textNode.setText(data.getName());
        this.requestChartLayout();
        this.updateLegend();
    }

    private void dataPieValueChanged(Data data) {
        if (this.shouldAnimate()) {
            this.animate(new KeyFrame(Duration.ZERO, new KeyValue(data.currentPieValueProperty(), data.getCurrentPieValue())), new KeyFrame(Duration.millis(500.0), new KeyValue(data.currentPieValueProperty(), data.getPieValue(), Interpolator.EASE_BOTH)));
        } else {
            data.setCurrentPieValue(data.getPieValue());
            this.requestChartLayout();
        }
    }

    private Node createArcRegion(Data data) {
        Node node = data.getNode();
        if (node == null) {
            node = new Region();
            node.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            node.setPickOnBounds(false);
            data.setNode(node);
        }
        return node;
    }

    private Text createPieLabel(Data data) {
        Text text = data.textNode;
        text.setText(data.getName());
        return text;
    }

    private void updateDataItemStyleClass(Data data, int n) {
        Node node = data.getNode();
        if (node != null) {
            node.getStyleClass().setAll((String[])new String[]{"chart-pie", "data" + n, "default-color" + data.defaultColorIndex % 8});
            if (data.getPieValue() < 0.0) {
                node.getStyleClass().add("negative");
            }
        }
    }

    private void dataItemAdded(Data data, int n) {
        Node node = this.createArcRegion(data);
        Text text = this.createPieLabel(data);
        data.getChart().getChartChildren().add(node);
        if (this.shouldAnimate()) {
            if (this.dataRemoveTimeline != null && this.dataRemoveTimeline.getStatus().equals((Object)Animation.Status.RUNNING) && this.dataItemBeingRemoved == data) {
                this.dataRemoveTimeline.stop();
                this.dataRemoveTimeline = null;
                this.getChartChildren().remove(data.textNode);
                this.getChartChildren().remove(node);
                this.removeDataItemRef(data);
            }
            this.animate(new KeyFrame(Duration.ZERO, new KeyValue(data.currentPieValueProperty(), data.getCurrentPieValue()), new KeyValue(data.radiusMultiplierProperty(), data.getRadiusMultiplier())), new KeyFrame(Duration.millis(500.0), actionEvent -> {
                text.setOpacity(0.0);
                if (data.getChart() == null) {
                    data.setChart(this);
                }
                data.getChart().getChartChildren().add(text);
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(150.0), text);
                fadeTransition.setToValue(1.0);
                fadeTransition.play();
            }, new KeyValue(data.currentPieValueProperty(), data.getPieValue(), Interpolator.EASE_BOTH), new KeyValue(data.radiusMultiplierProperty(), 1, Interpolator.EASE_BOTH)));
        } else {
            this.getChartChildren().add(text);
            data.setRadiusMultiplier(1.0);
            data.setCurrentPieValue(data.getPieValue());
        }
        for (int i = 0; i < this.getChartChildren().size(); ++i) {
            Node node2 = (Node)this.getChartChildren().get(i);
            if (!(node2 instanceof Text)) continue;
            node2.toFront();
        }
    }

    private void removeDataItemRef(Data data) {
        if (this.begin == data) {
            this.begin = data.next;
        } else {
            Data data2 = this.begin;
            while (data2 != null && data2.next != data) {
                data2 = data2.next;
            }
            if (data2 != null) {
                data2.next = data.next;
            }
        }
    }

    private Timeline createDataRemoveTimeline(final Data data) {
        Node node = data.getNode();
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll((KeyFrame[])new KeyFrame[]{new KeyFrame(Duration.ZERO, new KeyValue(data.currentPieValueProperty(), data.getCurrentPieValue()), new KeyValue(data.radiusMultiplierProperty(), data.getRadiusMultiplier())), new KeyFrame(Duration.millis(500.0), actionEvent -> {
            this.colorBits.clear(data.defaultColorIndex);
            this.getChartChildren().remove(node);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(150.0), data.textNode);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            fadeTransition.setOnFinished(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent actionEvent) {
                    PieChart.this.getChartChildren().remove(data.textNode);
                    data.setChart(null);
                    PieChart.this.removeDataItemRef(data);
                    data.textNode.setOpacity(1.0);
                }
            });
            fadeTransition.play();
        }, new KeyValue(data.currentPieValueProperty(), 0, Interpolator.EASE_BOTH), new KeyValue(data.radiusMultiplierProperty(), 0))});
        return timeline;
    }

    private void dataItemRemoved(Data data) {
        Node node = data.getNode();
        if (this.shouldAnimate()) {
            this.dataRemoveTimeline = this.createDataRemoveTimeline(data);
            this.dataItemBeingRemoved = data;
            this.animate(this.dataRemoveTimeline);
        } else {
            this.colorBits.clear(data.defaultColorIndex);
            this.getChartChildren().remove(data.textNode);
            this.getChartChildren().remove(node);
            data.setChart(null);
            this.removeDataItemRef(data);
        }
    }

    @Override
    protected void layoutChartChildren(double d, double d2, double d3, double d4) {
        double d5;
        double d6;
        Object object;
        int n;
        double d7;
        double d8;
        double d9 = 0.0;
        Data data = this.begin;
        while (data != null) {
            d9 += Math.abs(data.getCurrentPieValue());
            data = data.next;
        }
        double d10 = d9 != 0.0 ? 360.0 / d9 : 0.0;
        double[] dArray = null;
        double[] dArray2 = null;
        double[] dArray3 = null;
        double d11 = 1.0;
        ArrayList<LabelLayoutInfo> arrayList = null;
        boolean bl = this.getLabelsVisible();
        if (bl) {
            d8 = 0.0;
            d7 = 0.0;
            dArray = new double[this.getDataSize()];
            dArray2 = new double[this.getDataSize()];
            dArray3 = new double[this.getDataSize()];
            arrayList = new ArrayList<LabelLayoutInfo>();
            n = 0;
            double d12 = this.getStartAngle();
            object = this.begin;
            while (object != null) {
                ((Data)object).textNode.getTransforms().clear();
                double d13 = this.isClockwise() ? -d10 * Math.abs(((Data)object).getCurrentPieValue()) : d10 * Math.abs(((Data)object).getCurrentPieValue());
                dArray3[n] = PieChart.normalizeAngle(d12 + d13 / 2.0);
                d6 = PieChart.calcX(dArray3[n], this.getLabelLineLength(), 0.0);
                double d14 = PieChart.calcY(dArray3[n], this.getLabelLineLength(), 0.0);
                dArray[n] = d6;
                dArray2[n] = d14;
                d8 = Math.max(d8, 2.0 * (((Data)object).textNode.getLayoutBounds().getWidth() + 6.0 + Math.abs(d6)));
                d7 = d14 > 0.0 ? Math.max(d7, 2.0 * Math.abs(d14 + ((Data)object).textNode.getLayoutBounds().getMaxY())) : Math.max(d7, 2.0 * Math.abs(d14 + ((Data)object).textNode.getLayoutBounds().getMinY()));
                d12 += d13;
                ++n;
                object = ((Data)object).next;
            }
            this.pieRadius = Math.min(d3 - d8, d4 - d7) / 2.0;
            if (this.pieRadius < 25.0) {
                double d15 = d3 - 25.0 - 25.0;
                d5 = d4 - 25.0 - 25.0;
                d11 = Math.min(d15 / d8, d5 / d7);
                if (this.begin == null && d11 < 0.7 || this.begin.textNode.getFont().getSize() * d11 < 9.0) {
                    bl = false;
                    d11 = 1.0;
                } else {
                    this.pieRadius = 25.0;
                    for (int i = 0; i < dArray.length; ++i) {
                        dArray[i] = dArray[i] * d11;
                        dArray2[i] = dArray2[i] * d11;
                    }
                }
            }
        }
        if (!bl) {
            this.pieRadius = Math.min(d3, d4) / 2.0;
            this.labelLinePath.getElements().clear();
        }
        if (this.getChartChildren().size() > 0) {
            d8 = d3 / 2.0 + d2;
            d7 = d4 / 2.0 + d;
            n = 0;
            Data data2 = this.begin;
            while (data2 != null) {
                data2.textNode.setVisible(bl);
                if (bl) {
                    double d16 = this.isClockwise() ? -d10 * Math.abs(data2.getCurrentPieValue()) : d10 * Math.abs(data2.getCurrentPieValue());
                    boolean bl2 = !(dArray3[n] > -90.0) || !(dArray3[n] < 90.0);
                    d5 = PieChart.calcX(dArray3[n], this.pieRadius, d8);
                    double d17 = PieChart.calcY(dArray3[n], this.pieRadius, d7);
                    double d18 = bl2 ? dArray[n] + d5 - data2.textNode.getLayoutBounds().getMaxX() - 6.0 : dArray[n] + d5 - data2.textNode.getLayoutBounds().getMinX() + 6.0;
                    double d19 = dArray2[n] + d17 - data2.textNode.getLayoutBounds().getMinY() / 2.0 - 2.0;
                    double d20 = d5 + dArray[n];
                    double d21 = d17 + dArray2[n];
                    LabelLayoutInfo labelLayoutInfo = new LabelLayoutInfo(d5, d17, d20, d21, d18, d19, data2.textNode, Math.abs(d16));
                    arrayList.add(labelLayoutInfo);
                    if (d11 < 1.0) {
                        data2.textNode.getTransforms().add(new Scale(d11, d11, bl2 ? data2.textNode.getLayoutBounds().getWidth() : 0.0, 0.0));
                    }
                }
                ++n;
                data2 = data2.next;
            }
            double d22 = this.getStartAngle();
            object = this.begin;
            while (object != null) {
                Node node = ((Data)object).getNode();
                Arc arc = null;
                if (node != null && node instanceof Region) {
                    Region region = (Region)node;
                    if (region.getShape() == null) {
                        arc = new Arc();
                        region.setShape(arc);
                    } else {
                        arc = (Arc)region.getShape();
                    }
                    region.setScaleShape(false);
                    region.setCenterShape(false);
                    region.setCacheShape(false);
                }
                d6 = this.isClockwise() ? -d10 * Math.abs(((Data)object).getCurrentPieValue()) : d10 * Math.abs(((Data)object).getCurrentPieValue());
                arc.setStartAngle(d22);
                arc.setLength(d6);
                arc.setType(ArcType.ROUND);
                arc.setRadiusX(this.pieRadius * ((Data)object).getRadiusMultiplier());
                arc.setRadiusY(this.pieRadius * ((Data)object).getRadiusMultiplier());
                node.setLayoutX(d8);
                node.setLayoutY(d7);
                d22 += d6;
                object = ((Data)object).next;
            }
            if (arrayList != null) {
                this.resolveCollision(arrayList);
                if (!arrayList.equals(this.labelLayoutInfos)) {
                    this.labelLinePath.getElements().clear();
                    for (LabelLayoutInfo labelLayoutInfo : arrayList) {
                        if (!labelLayoutInfo.text.isVisible()) continue;
                        this.drawLabelLinePath(labelLayoutInfo);
                    }
                    this.labelLayoutInfos = arrayList;
                }
            }
        }
    }

    private void resolveCollision(List<LabelLayoutInfo> list) {
        int n = this.begin != null ? (int)this.begin.textNode.getLayoutBounds().getHeight() : 0;
        for (int i = 0; i < list.size(); ++i) {
            for (int j = i + 1; j < list.size(); ++j) {
                LabelLayoutInfo labelLayoutInfo = list.get(i);
                LabelLayoutInfo labelLayoutInfo2 = list.get(j);
                if (!labelLayoutInfo.text.isVisible() || !labelLayoutInfo2.text.isVisible() || !(this.fuzzyGT(labelLayoutInfo2.textY, labelLayoutInfo.textY) ? this.fuzzyLT(labelLayoutInfo2.textY - (double)n - labelLayoutInfo.textY, 2.0) : this.fuzzyLT(labelLayoutInfo.textY - (double)n - labelLayoutInfo2.textY, 2.0)) || !(this.fuzzyGT(labelLayoutInfo.textX, labelLayoutInfo2.textX) ? this.fuzzyLT(labelLayoutInfo.textX - labelLayoutInfo2.textX, labelLayoutInfo2.text.prefWidth(-1.0)) : this.fuzzyLT(labelLayoutInfo2.textX - labelLayoutInfo.textX, labelLayoutInfo.text.prefWidth(-1.0)))) continue;
                if (this.fuzzyLT(labelLayoutInfo.size, labelLayoutInfo2.size)) {
                    labelLayoutInfo.text.setVisible(false);
                    continue;
                }
                labelLayoutInfo2.text.setVisible(false);
            }
        }
    }

    private int fuzzyCompare(double d, double d2) {
        double d3 = 1.0E-5;
        return Math.abs(d - d2) < d3 ? 0 : (d < d2 ? -1 : 1);
    }

    private boolean fuzzyGT(double d, double d2) {
        return this.fuzzyCompare(d, d2) == 1;
    }

    private boolean fuzzyLT(double d, double d2) {
        return this.fuzzyCompare(d, d2) == -1;
    }

    private void drawLabelLinePath(LabelLayoutInfo labelLayoutInfo) {
        labelLayoutInfo.text.setLayoutX(labelLayoutInfo.textX);
        labelLayoutInfo.text.setLayoutY(labelLayoutInfo.textY);
        this.labelLinePath.getElements().add(new MoveTo(labelLayoutInfo.startX, labelLayoutInfo.startY));
        this.labelLinePath.getElements().add(new LineTo(labelLayoutInfo.endX, labelLayoutInfo.endY));
        this.labelLinePath.getElements().add(new MoveTo(labelLayoutInfo.endX - 2.0, labelLayoutInfo.endY));
        this.labelLinePath.getElements().add(new ArcTo(2.0, 2.0, 90.0, labelLayoutInfo.endX, labelLayoutInfo.endY - 2.0, false, true));
        this.labelLinePath.getElements().add(new ArcTo(2.0, 2.0, 90.0, labelLayoutInfo.endX + 2.0, labelLayoutInfo.endY, false, true));
        this.labelLinePath.getElements().add(new ArcTo(2.0, 2.0, 90.0, labelLayoutInfo.endX, labelLayoutInfo.endY + 2.0, false, true));
        this.labelLinePath.getElements().add(new ArcTo(2.0, 2.0, 90.0, labelLayoutInfo.endX - 2.0, labelLayoutInfo.endY, false, true));
        this.labelLinePath.getElements().add(new ClosePath());
    }

    private void updateLegend() {
        Node node = this.getLegend();
        if (node != null && node != this.legend) {
            return;
        }
        this.legend.setVertical(this.getLegendSide().equals((Object)Side.LEFT) || this.getLegendSide().equals((Object)Side.RIGHT));
        ArrayList<Legend.LegendItem> arrayList = new ArrayList<Legend.LegendItem>();
        if (this.getData() != null) {
            for (Data data : this.getData()) {
                Legend.LegendItem legendItem = new Legend.LegendItem(data.getName());
                legendItem.getSymbol().getStyleClass().addAll((Collection<String>)data.getNode().getStyleClass());
                legendItem.getSymbol().getStyleClass().add("pie-legend-symbol");
                arrayList.add(legendItem);
            }
        }
        this.legend.getItems().setAll((Collection<Legend.LegendItem>)arrayList);
        if (arrayList.size() > 0) {
            if (node == null) {
                this.setLegend(this.legend);
            }
        } else {
            this.setLegend(null);
        }
    }

    private int getDataSize() {
        int n = 0;
        Data data = this.begin;
        while (data != null) {
            ++n;
            data = data.next;
        }
        return n;
    }

    private static double calcX(double d, double d2, double d3) {
        return d3 + d2 * Math.cos(Math.toRadians(-d));
    }

    private static double calcY(double d, double d2, double d3) {
        return d3 + d2 * Math.sin(Math.toRadians(-d));
    }

    private static double normalizeAngle(double d) {
        double d2 = d % 360.0;
        if (d2 <= -180.0) {
            d2 += 360.0;
        }
        if (d2 > 180.0) {
            d2 -= 360.0;
        }
        return d2;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return PieChart.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<PieChart, Boolean> CLOCKWISE = new CssMetaData<PieChart, Boolean>("-fx-clockwise", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(PieChart pieChart) {
                return pieChart.clockwise == null || !pieChart.clockwise.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(PieChart pieChart) {
                return (StyleableProperty)((Object)pieChart.clockwiseProperty());
            }
        };
        private static final CssMetaData<PieChart, Boolean> LABELS_VISIBLE = new CssMetaData<PieChart, Boolean>("-fx-pie-label-visible", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(PieChart pieChart) {
                return pieChart.labelsVisible == null || !pieChart.labelsVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(PieChart pieChart) {
                return (StyleableProperty)((Object)pieChart.labelsVisibleProperty());
            }
        };
        private static final CssMetaData<PieChart, Number> LABEL_LINE_LENGTH = new CssMetaData<PieChart, Number>("-fx-label-line-length", SizeConverter.getInstance(), (Number)20.0){

            @Override
            public boolean isSettable(PieChart pieChart) {
                return pieChart.labelLineLength == null || !pieChart.labelLineLength.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(PieChart pieChart) {
                return (StyleableProperty)((Object)pieChart.labelLineLengthProperty());
            }
        };
        private static final CssMetaData<PieChart, Number> START_ANGLE = new CssMetaData<PieChart, Number>("-fx-start-angle", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(PieChart pieChart) {
                return pieChart.startAngle == null || !pieChart.startAngle.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(PieChart pieChart) {
                return (StyleableProperty)((Object)pieChart.startAngleProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Chart.getClassCssMetaData());
            arrayList.add(CLOCKWISE);
            arrayList.add(LABELS_VISIBLE);
            arrayList.add(LABEL_LINE_LENGTH);
            arrayList.add(START_ANGLE);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }

    public static final class Data {
        private Text textNode = new Text();
        private Data next = null;
        private int defaultColorIndex;
        private ReadOnlyObjectWrapper<PieChart> chart = new ReadOnlyObjectWrapper(this, "chart");
        private StringProperty name = new StringPropertyBase(){

            @Override
            protected void invalidated() {
                if (this.getChart() != null) {
                    this.getChart().dataNameChanged(this);
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
        private DoubleProperty pieValue = new DoublePropertyBase(){

            @Override
            protected void invalidated() {
                if (this.getChart() != null) {
                    this.getChart().dataPieValueChanged(this);
                }
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "pieValue";
            }
        };
        private DoubleProperty currentPieValue = new SimpleDoubleProperty(this, "currentPieValue");
        private DoubleProperty radiusMultiplier = new SimpleDoubleProperty(this, "radiusMultiplier");
        private ReadOnlyObjectWrapper<Node> node = new ReadOnlyObjectWrapper(this, "node");

        public final PieChart getChart() {
            return (PieChart)this.chart.getValue();
        }

        private void setChart(PieChart pieChart) {
            this.chart.setValue(pieChart);
        }

        public final ReadOnlyObjectProperty<PieChart> chartProperty() {
            return this.chart.getReadOnlyProperty();
        }

        public final void setName(String string) {
            this.name.setValue(string);
        }

        public final String getName() {
            return this.name.getValue();
        }

        public final StringProperty nameProperty() {
            return this.name;
        }

        public final double getPieValue() {
            return this.pieValue.getValue();
        }

        public final void setPieValue(double d) {
            this.pieValue.setValue(d);
        }

        public final DoubleProperty pieValueProperty() {
            return this.pieValue;
        }

        private double getCurrentPieValue() {
            return this.currentPieValue.getValue();
        }

        private void setCurrentPieValue(double d) {
            this.currentPieValue.setValue(d);
        }

        private DoubleProperty currentPieValueProperty() {
            return this.currentPieValue;
        }

        private double getRadiusMultiplier() {
            return this.radiusMultiplier.getValue();
        }

        private void setRadiusMultiplier(double d) {
            this.radiusMultiplier.setValue(d);
        }

        private DoubleProperty radiusMultiplierProperty() {
            return this.radiusMultiplier;
        }

        public Node getNode() {
            return (Node)this.node.getValue();
        }

        private void setNode(Node node) {
            this.node.setValue(node);
        }

        public ReadOnlyObjectProperty<Node> nodeProperty() {
            return this.node.getReadOnlyProperty();
        }

        public Data(String string, double d) {
            this.setName(string);
            this.setPieValue(d);
            this.textNode.getStyleClass().addAll((String[])new String[]{"text", "chart-pie-label"});
            this.textNode.setAccessibleRole(AccessibleRole.TEXT);
            this.textNode.setAccessibleRoleDescription("slice");
            this.textNode.focusTraversableProperty().bind(Platform.accessibilityActiveProperty());
            this.textNode.accessibleTextProperty().bind(new StringBinding(){
                {
                    this.bind(this.nameProperty(), this.currentPieValueProperty());
                }

                @Override
                protected String computeValue() {
                    return this.getName() + " represents " + this.getCurrentPieValue() + " percent";
                }
            });
        }

        public String toString() {
            return "Data[" + this.getName() + "," + this.getPieValue() + "]";
        }
    }

    private static final class LabelLayoutInfo {
        double startX;
        double startY;
        double endX;
        double endY;
        double textX;
        double textY;
        Text text;
        double size;

        LabelLayoutInfo(double d, double d2, double d3, double d4, double d5, double d6, Text text, double d7) {
            this.startX = d;
            this.startY = d2;
            this.endX = d3;
            this.endY = d4;
            this.textX = d5;
            this.textY = d6;
            this.text = text;
            this.size = d7;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            LabelLayoutInfo labelLayoutInfo = (LabelLayoutInfo)object;
            return Double.compare(labelLayoutInfo.startX, this.startX) == 0 && Double.compare(labelLayoutInfo.startY, this.startY) == 0 && Double.compare(labelLayoutInfo.endX, this.endX) == 0 && Double.compare(labelLayoutInfo.endY, this.endY) == 0 && Double.compare(labelLayoutInfo.textX, this.textX) == 0 && Double.compare(labelLayoutInfo.textY, this.textY) == 0 && Double.compare(labelLayoutInfo.size, this.size) == 0;
        }

        public int hashCode() {
            return Objects.hash(this.startX, this.startY, this.endX, this.endY, this.textX, this.textY, this.size);
        }
    }
}

