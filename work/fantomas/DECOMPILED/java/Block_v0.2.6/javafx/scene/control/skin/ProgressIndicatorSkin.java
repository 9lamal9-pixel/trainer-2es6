/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.control.skin.Utils;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.NodeOrientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.util.Duration;

public class ProgressIndicatorSkin
extends SkinBase<ProgressIndicator> {
    private final String DONE = ControlResources.getString("ProgressIndicator.doneString");
    final Duration CLIPPED_DELAY = new Duration(300.0);
    final Duration UNCLIPPED_DELAY = new Duration(0.0);
    private IndeterminateSpinner spinner;
    private DeterminateIndicator determinateIndicator;
    private ProgressIndicator control;
    Animation indeterminateTransition;
    private ObjectProperty<Paint> progressColor = new StyleableObjectProperty<Paint>(null){

        @Override
        protected void invalidated() {
            Paint paint = (Paint)this.get();
            if (paint != null && !(paint instanceof Color)) {
                if (this.isBound()) {
                    this.unbind();
                }
                this.set(null);
                throw new IllegalArgumentException("Only Color objects are supported");
            }
            if (ProgressIndicatorSkin.this.spinner != null) {
                ProgressIndicatorSkin.this.spinner.setFillOverride(paint);
            }
            if (ProgressIndicatorSkin.this.determinateIndicator != null) {
                ProgressIndicatorSkin.this.determinateIndicator.setFillOverride(paint);
            }
        }

        @Override
        public Object getBean() {
            return ProgressIndicatorSkin.this;
        }

        @Override
        public String getName() {
            return "progressColorProperty";
        }

        @Override
        public CssMetaData<ProgressIndicator, Paint> getCssMetaData() {
            return PROGRESS_COLOR;
        }
    };
    private IntegerProperty indeterminateSegmentCount = new StyleableIntegerProperty(8){

        @Override
        protected void invalidated() {
            if (ProgressIndicatorSkin.this.spinner != null) {
                ProgressIndicatorSkin.this.spinner.rebuild();
            }
        }

        @Override
        public Object getBean() {
            return ProgressIndicatorSkin.this;
        }

        @Override
        public String getName() {
            return "indeterminateSegmentCount";
        }

        @Override
        public CssMetaData<ProgressIndicator, Number> getCssMetaData() {
            return INDETERMINATE_SEGMENT_COUNT;
        }
    };
    private final BooleanProperty spinEnabled = new StyleableBooleanProperty(false){

        @Override
        protected void invalidated() {
            if (ProgressIndicatorSkin.this.spinner != null) {
                ProgressIndicatorSkin.this.spinner.setSpinEnabled(this.get());
            }
        }

        @Override
        public CssMetaData<ProgressIndicator, Boolean> getCssMetaData() {
            return SPIN_ENABLED;
        }

        @Override
        public Object getBean() {
            return ProgressIndicatorSkin.this;
        }

        @Override
        public String getName() {
            return "spinEnabled";
        }
    };
    private static final CssMetaData<ProgressIndicator, Paint> PROGRESS_COLOR = new CssMetaData<ProgressIndicator, Paint>("-fx-progress-color", PaintConverter.getInstance(), null){

        @Override
        public boolean isSettable(ProgressIndicator progressIndicator) {
            ProgressIndicatorSkin progressIndicatorSkin = (ProgressIndicatorSkin)progressIndicator.getSkin();
            return progressIndicatorSkin.progressColor == null || !progressIndicatorSkin.progressColor.isBound();
        }

        @Override
        public StyleableProperty<Paint> getStyleableProperty(ProgressIndicator progressIndicator) {
            ProgressIndicatorSkin progressIndicatorSkin = (ProgressIndicatorSkin)progressIndicator.getSkin();
            return (StyleableProperty)((Object)progressIndicatorSkin.progressColor);
        }
    };
    private static final CssMetaData<ProgressIndicator, Number> INDETERMINATE_SEGMENT_COUNT = new CssMetaData<ProgressIndicator, Number>("-fx-indeterminate-segment-count", SizeConverter.getInstance(), (Number)8){

        @Override
        public boolean isSettable(ProgressIndicator progressIndicator) {
            ProgressIndicatorSkin progressIndicatorSkin = (ProgressIndicatorSkin)progressIndicator.getSkin();
            return progressIndicatorSkin.indeterminateSegmentCount == null || !progressIndicatorSkin.indeterminateSegmentCount.isBound();
        }

        @Override
        public StyleableProperty<Number> getStyleableProperty(ProgressIndicator progressIndicator) {
            ProgressIndicatorSkin progressIndicatorSkin = (ProgressIndicatorSkin)progressIndicator.getSkin();
            return (StyleableProperty)((Object)progressIndicatorSkin.indeterminateSegmentCount);
        }
    };
    private static final CssMetaData<ProgressIndicator, Boolean> SPIN_ENABLED = new CssMetaData<ProgressIndicator, Boolean>("-fx-spin-enabled", BooleanConverter.getInstance(), Boolean.FALSE){

        @Override
        public boolean isSettable(ProgressIndicator progressIndicator) {
            ProgressIndicatorSkin progressIndicatorSkin = (ProgressIndicatorSkin)progressIndicator.getSkin();
            return progressIndicatorSkin.spinEnabled == null || !progressIndicatorSkin.spinEnabled.isBound();
        }

        @Override
        public StyleableProperty<Boolean> getStyleableProperty(ProgressIndicator progressIndicator) {
            ProgressIndicatorSkin progressIndicatorSkin = (ProgressIndicatorSkin)progressIndicator.getSkin();
            return (StyleableProperty)((Object)progressIndicatorSkin.spinEnabled);
        }
    };
    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

    public ProgressIndicatorSkin(ProgressIndicator progressIndicator) {
        super(progressIndicator);
        this.control = progressIndicator;
        this.registerChangeListener(progressIndicator.indeterminateProperty(), observableValue -> this.initialize());
        this.registerChangeListener(progressIndicator.progressProperty(), observableValue -> this.updateProgress());
        this.registerChangeListener(NodeHelper.treeShowingProperty(progressIndicator), observableValue -> this.updateAnimation());
        this.registerChangeListener(progressIndicator.sceneProperty(), observableValue -> this.updateAnimation());
        this.initialize();
        this.updateAnimation();
    }

    Paint getProgressColor() {
        return (Paint)this.progressColor.get();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.indeterminateTransition != null) {
            this.indeterminateTransition.stop();
            this.indeterminateTransition = null;
        }
        if (this.spinner != null) {
            this.spinner = null;
        }
        this.control = null;
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        if (this.spinner != null && this.control.isIndeterminate()) {
            this.spinner.layoutChildren();
            this.spinner.resizeRelocate(0.0, 0.0, d3, d4);
        } else if (this.determinateIndicator != null) {
            this.determinateIndicator.layoutChildren();
            this.determinateIndicator.resizeRelocate(0.0, 0.0, d3, d4);
        }
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        if (this.spinner != null && this.control.isIndeterminate()) {
            d6 = this.spinner.minWidth(-1.0);
        } else if (this.determinateIndicator != null) {
            d6 = this.determinateIndicator.minWidth(-1.0);
        }
        return d6;
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        if (this.spinner != null && this.control.isIndeterminate()) {
            d6 = this.spinner.minHeight(-1.0);
        } else if (this.determinateIndicator != null) {
            d6 = this.determinateIndicator.minHeight(-1.0);
        }
        return d6;
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        if (this.spinner != null && this.control.isIndeterminate()) {
            d6 = this.spinner.prefWidth(d);
        } else if (this.determinateIndicator != null) {
            d6 = this.determinateIndicator.prefWidth(d);
        }
        return d6;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        if (this.spinner != null && this.control.isIndeterminate()) {
            d6 = this.spinner.prefHeight(d);
        } else if (this.determinateIndicator != null) {
            d6 = this.determinateIndicator.prefHeight(d);
        }
        return d6;
    }

    @Override
    protected double computeMaxWidth(double d, double d2, double d3, double d4, double d5) {
        return this.computePrefWidth(d, d2, d3, d4, d5);
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        return this.computePrefHeight(d, d2, d3, d4, d5);
    }

    void initialize() {
        boolean bl = this.control.isIndeterminate();
        if (bl) {
            this.determinateIndicator = null;
            this.spinner = new IndeterminateSpinner(this.spinEnabled.get(), (Paint)this.progressColor.get());
            this.getChildren().setAll((Node[])new Node[]{this.spinner});
            if (NodeHelper.isTreeShowing(this.control) && this.indeterminateTransition != null) {
                this.indeterminateTransition.play();
            }
        } else {
            if (this.spinner != null) {
                if (this.indeterminateTransition != null) {
                    this.indeterminateTransition.stop();
                }
                this.spinner = null;
            }
            this.determinateIndicator = new DeterminateIndicator(this.control, this, (Paint)this.progressColor.get());
            this.getChildren().setAll((Node[])new Node[]{this.determinateIndicator});
        }
    }

    void updateProgress() {
        if (this.determinateIndicator != null) {
            this.determinateIndicator.updateProgress(this.control.getProgress());
        }
    }

    void createIndeterminateTimeline() {
        if (this.spinner != null) {
            this.spinner.rebuildTimeline();
        }
    }

    void pauseTimeline(boolean bl) {
        if (((ProgressIndicator)this.getSkinnable()).isIndeterminate()) {
            if (this.indeterminateTransition == null) {
                this.createIndeterminateTimeline();
            }
            if (bl) {
                this.indeterminateTransition.pause();
            } else {
                this.indeterminateTransition.play();
            }
        }
    }

    void updateAnimation() {
        boolean bl;
        ProgressIndicator progressIndicator = (ProgressIndicator)this.getSkinnable();
        boolean bl2 = bl = NodeHelper.isTreeShowing(progressIndicator) && progressIndicator.getScene() != null;
        if (this.indeterminateTransition != null) {
            this.pauseTimeline(!bl);
        } else if (bl) {
            this.createIndeterminateTimeline();
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return ProgressIndicatorSkin.getClassCssMetaData();
    }

    static {
        ArrayList arrayList = new ArrayList(SkinBase.getClassCssMetaData());
        arrayList.add(PROGRESS_COLOR);
        arrayList.add(INDETERMINATE_SEGMENT_COUNT);
        arrayList.add(SPIN_ENABLED);
        STYLEABLES = Collections.unmodifiableList(arrayList);
    }

    private final class IndeterminateSpinner
    extends Region {
        private IndicatorPaths pathsG;
        private final List<Double> opacities = new ArrayList<Double>();
        private boolean spinEnabled = false;
        private Paint fillOverride = null;

        private IndeterminateSpinner(boolean bl, Paint paint) {
            this.spinEnabled = bl;
            this.fillOverride = paint;
            this.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            this.getStyleClass().setAll((String[])new String[]{"spinner"});
            this.pathsG = new IndicatorPaths();
            this.getChildren().add(this.pathsG);
            this.rebuild();
            this.rebuildTimeline();
        }

        public void setFillOverride(Paint paint) {
            this.fillOverride = paint;
            this.rebuild();
        }

        public void setSpinEnabled(boolean bl) {
            this.spinEnabled = bl;
            this.rebuildTimeline();
        }

        private void rebuildTimeline() {
            if (this.spinEnabled) {
                if (ProgressIndicatorSkin.this.indeterminateTransition == null) {
                    ProgressIndicatorSkin.this.indeterminateTransition = new Timeline();
                    ProgressIndicatorSkin.this.indeterminateTransition.setCycleCount(-1);
                    ProgressIndicatorSkin.this.indeterminateTransition.setDelay(ProgressIndicatorSkin.this.UNCLIPPED_DELAY);
                } else {
                    ProgressIndicatorSkin.this.indeterminateTransition.stop();
                    ((Timeline)ProgressIndicatorSkin.this.indeterminateTransition).getKeyFrames().clear();
                }
                ObservableList<KeyFrame> observableList = FXCollections.observableArrayList();
                observableList.add(new KeyFrame(Duration.millis(1.0), new KeyValue(this.pathsG.rotateProperty(), 360)));
                observableList.add(new KeyFrame(Duration.millis(3900.0), new KeyValue(this.pathsG.rotateProperty(), 0)));
                for (int i = 100; i <= 3900; i += 100) {
                    observableList.add(new KeyFrame(Duration.millis(i), actionEvent -> this.shiftColors(), new KeyValue[0]));
                }
                ((Timeline)ProgressIndicatorSkin.this.indeterminateTransition).getKeyFrames().setAll((Collection<KeyFrame>)observableList);
                ProgressIndicatorSkin.this.indeterminateTransition.playFromStart();
            } else if (ProgressIndicatorSkin.this.indeterminateTransition != null) {
                ProgressIndicatorSkin.this.indeterminateTransition.stop();
                ((Timeline)ProgressIndicatorSkin.this.indeterminateTransition).getKeyFrames().clear();
                ProgressIndicatorSkin.this.indeterminateTransition = null;
            }
        }

        @Override
        protected void layoutChildren() {
            double d;
            double d2 = ProgressIndicatorSkin.this.control.getWidth() - ProgressIndicatorSkin.this.control.snappedLeftInset() - ProgressIndicatorSkin.this.control.snappedRightInset();
            double d3 = ProgressIndicatorSkin.this.control.getHeight() - ProgressIndicatorSkin.this.control.snappedTopInset() - ProgressIndicatorSkin.this.control.snappedBottomInset();
            double d4 = this.pathsG.prefWidth(-1.0);
            double d5 = this.pathsG.prefHeight(-1.0);
            double d6 = d = d2 / d4;
            if (d * d5 > d3) {
                d6 = d3 / d5;
            }
            double d7 = d4 * d6;
            double d8 = d5 * d6;
            this.pathsG.resizeRelocate((d2 - d7) / 2.0, (d3 - d8) / 2.0, d7, d8);
        }

        private void rebuild() {
            int n = ProgressIndicatorSkin.this.indeterminateSegmentCount.get();
            this.opacities.clear();
            this.pathsG.getChildren().clear();
            double d = 0.8 / (double)(n - 1);
            for (int i = 0; i < n; ++i) {
                Region region = new Region();
                region.setScaleShape(false);
                region.setCenterShape(false);
                region.getStyleClass().addAll((String[])new String[]{"segment", "segment" + i});
                if (this.fillOverride instanceof Color) {
                    Color color = (Color)this.fillOverride;
                    region.setStyle("-fx-background-color: rgba(" + (int)(255.0 * color.getRed()) + "," + (int)(255.0 * color.getGreen()) + "," + (int)(255.0 * color.getBlue()) + "," + color.getOpacity() + ");");
                } else {
                    region.setStyle(null);
                }
                this.pathsG.getChildren().add(region);
                this.opacities.add(Math.max(0.1, 1.0 - d * (double)i));
            }
        }

        private void shiftColors() {
            if (this.opacities.size() <= 0) {
                return;
            }
            int n = ProgressIndicatorSkin.this.indeterminateSegmentCount.get();
            Collections.rotate(this.opacities, -1);
            for (int i = 0; i < n; ++i) {
                ((Node)this.pathsG.getChildren().get(i)).setOpacity(this.opacities.get(i));
            }
        }

        private class IndicatorPaths
        extends Pane {
            private IndicatorPaths() {
            }

            @Override
            protected double computePrefWidth(double d) {
                double d2 = 0.0;
                for (Node node : this.getChildren()) {
                    if (!(node instanceof Region)) continue;
                    Region region = (Region)node;
                    if (region.getShape() != null) {
                        d2 = Math.max(d2, region.getShape().getLayoutBounds().getMaxX());
                        continue;
                    }
                    d2 = Math.max(d2, region.prefWidth(d));
                }
                return d2;
            }

            @Override
            protected double computePrefHeight(double d) {
                double d2 = 0.0;
                for (Node node : this.getChildren()) {
                    if (!(node instanceof Region)) continue;
                    Region region = (Region)node;
                    if (region.getShape() != null) {
                        d2 = Math.max(d2, region.getShape().getLayoutBounds().getMaxY());
                        continue;
                    }
                    d2 = Math.max(d2, region.prefHeight(d));
                }
                return d2;
            }

            @Override
            protected void layoutChildren() {
                double d = this.getWidth() / this.computePrefWidth(-1.0);
                for (Node node : this.getChildren()) {
                    if (!(node instanceof Region)) continue;
                    Region region = (Region)node;
                    if (region.getShape() != null) {
                        region.resize(region.getShape().getLayoutBounds().getMaxX(), region.getShape().getLayoutBounds().getMaxY());
                        region.getTransforms().setAll((Transform[])new Transform[]{new Scale(d, d, 0.0, 0.0)});
                        continue;
                    }
                    region.autosize();
                }
            }
        }
    }

    private final class DeterminateIndicator
    extends Region {
        private double textGap = 2.0;
        private int intProgress;
        private int degProgress;
        private Text text;
        private StackPane indicator;
        private StackPane progress;
        private StackPane tick;
        private Arc arcShape;
        private Circle indicatorCircle;
        private double doneTextWidth;
        private double doneTextHeight;

        public DeterminateIndicator(ProgressIndicator progressIndicator, ProgressIndicatorSkin progressIndicatorSkin2, Paint paint) {
            this.getStyleClass().add("determinate-indicator");
            this.intProgress = (int)Math.round(progressIndicator.getProgress() * 100.0);
            this.degProgress = (int)(360.0 * progressIndicator.getProgress());
            this.getChildren().clear();
            this.text = new Text((String)(progressIndicator.getProgress() >= 1.0 ? ProgressIndicatorSkin.this.DONE : this.intProgress + "%"));
            this.text.setTextOrigin(VPos.TOP);
            this.text.getStyleClass().setAll((String[])new String[]{"text", "percentage"});
            ProgressIndicatorSkin.this.registerChangeListener(this.text.fontProperty(), observableValue -> {
                this.doneTextWidth = Utils.computeTextWidth(this.text.getFont(), ProgressIndicatorSkin.this.DONE, 0.0);
                this.doneTextHeight = Utils.computeTextHeight(this.text.getFont(), ProgressIndicatorSkin.this.DONE, 0.0, TextBoundsType.LOGICAL_VERTICAL_CENTER);
            });
            this.indicator = new StackPane();
            this.indicator.setScaleShape(false);
            this.indicator.setCenterShape(false);
            this.indicator.getStyleClass().setAll((String[])new String[]{"indicator"});
            this.indicatorCircle = new Circle();
            this.indicator.setShape(this.indicatorCircle);
            this.arcShape = new Arc();
            this.arcShape.setType(ArcType.ROUND);
            this.arcShape.setStartAngle(90.0);
            this.progress = new StackPane();
            this.progress.getStyleClass().setAll((String[])new String[]{"progress"});
            this.progress.setScaleShape(false);
            this.progress.setCenterShape(false);
            this.progress.setShape(this.arcShape);
            this.progress.getChildren().clear();
            this.setFillOverride(paint);
            this.tick = new StackPane();
            this.tick.getStyleClass().setAll((String[])new String[]{"tick"});
            this.getChildren().setAll((Node[])new Node[]{this.indicator, this.progress, this.text, this.tick});
            this.updateProgress(progressIndicator.getProgress());
        }

        private void setFillOverride(Paint paint) {
            if (paint instanceof Color) {
                Color color = (Color)paint;
                this.progress.setStyle("-fx-background-color: rgba(" + (int)(255.0 * color.getRed()) + "," + (int)(255.0 * color.getGreen()) + "," + (int)(255.0 * color.getBlue()) + "," + color.getOpacity() + ");");
            } else {
                this.progress.setStyle(null);
            }
        }

        @Override
        public boolean usesMirroring() {
            return false;
        }

        private void updateProgress(double d) {
            this.intProgress = (int)Math.round(d * 100.0);
            this.text.setText((String)(d >= 1.0 ? ProgressIndicatorSkin.this.DONE : this.intProgress + "%"));
            this.degProgress = (int)(360.0 * d);
            this.arcShape.setLength(-this.degProgress);
            this.requestLayout();
        }

        @Override
        protected void layoutChildren() {
            double d = ProgressIndicatorSkin.this.control.snappedLeftInset();
            double d2 = ProgressIndicatorSkin.this.control.snappedRightInset();
            double d3 = ProgressIndicatorSkin.this.control.snappedTopInset();
            double d4 = ProgressIndicatorSkin.this.control.snappedBottomInset();
            double d5 = ProgressIndicatorSkin.this.control.getWidth() - d - d2;
            double d6 = ProgressIndicatorSkin.this.control.getHeight() - d3 - d4 - this.textGap - this.doneTextHeight;
            double d7 = d5 / 2.0;
            double d8 = d6 / 2.0;
            double d9 = Math.floor(Math.min(d7, d8));
            double d10 = this.snapPosition(d + d7);
            double d11 = this.snapPosition(d3 + d9);
            double d12 = this.indicator.snappedLeftInset();
            double d13 = this.indicator.snappedRightInset();
            double d14 = this.indicator.snappedTopInset();
            double d15 = this.indicator.snappedBottomInset();
            double d16 = this.snapSize(Math.min(Math.min(d9 - d12, d9 - d13), Math.min(d9 - d14, d9 - d15)));
            this.indicatorCircle.setRadius(d9);
            this.indicator.setLayoutX(d10);
            this.indicator.setLayoutY(d11);
            this.arcShape.setRadiusX(d16);
            this.arcShape.setRadiusY(d16);
            this.progress.setLayoutX(d10);
            this.progress.setLayoutY(d11);
            double d17 = this.progress.snappedLeftInset();
            double d18 = this.progress.snappedRightInset();
            double d19 = this.progress.snappedTopInset();
            double d20 = this.progress.snappedBottomInset();
            double d21 = this.snapSize(Math.min(Math.min(d16 - d17, d16 - d18), Math.min(d16 - d19, d16 - d20)));
            double d22 = Math.ceil(Math.sqrt(d21 * d21 / 2.0));
            this.tick.setLayoutX(d10 - d22);
            this.tick.setLayoutY(d11 - d22);
            this.tick.resize(d22 + d22, d22 + d22);
            this.tick.setVisible(ProgressIndicatorSkin.this.control.getProgress() >= 1.0);
            double d23 = this.text.getLayoutBounds().getWidth();
            double d24 = this.text.getLayoutBounds().getHeight();
            if (ProgressIndicatorSkin.this.control.getWidth() >= d23 && ProgressIndicatorSkin.this.control.getHeight() >= d24) {
                if (!this.text.isVisible()) {
                    this.text.setVisible(true);
                }
                this.text.setLayoutY(this.snapPosition(d11 + d9 + this.textGap));
                this.text.setLayoutX(this.snapPosition(d10 - d23 / 2.0));
            } else if (this.text.isVisible()) {
                this.text.setVisible(false);
            }
        }

        @Override
        protected double computePrefWidth(double d) {
            double d2 = ProgressIndicatorSkin.this.control.snappedLeftInset();
            double d3 = ProgressIndicatorSkin.this.control.snappedRightInset();
            double d4 = this.indicator.snappedLeftInset();
            double d5 = this.indicator.snappedRightInset();
            double d6 = this.indicator.snappedTopInset();
            double d7 = this.indicator.snappedBottomInset();
            double d8 = this.snapSize(Math.max(Math.max(d4, d5), Math.max(d6, d7)));
            double d9 = this.progress.snappedLeftInset();
            double d10 = this.progress.snappedRightInset();
            double d11 = this.progress.snappedTopInset();
            double d12 = this.progress.snappedBottomInset();
            double d13 = this.snapSize(Math.max(Math.max(d9, d10), Math.max(d11, d12)));
            double d14 = this.tick.snappedLeftInset();
            double d15 = this.tick.snappedRightInset();
            double d16 = d8 + d13 + d14 + d15 + d13 + d8;
            return d2 + Math.max(d16, this.doneTextWidth) + d3;
        }

        @Override
        protected double computePrefHeight(double d) {
            double d2 = ProgressIndicatorSkin.this.control.snappedTopInset();
            double d3 = ProgressIndicatorSkin.this.control.snappedBottomInset();
            double d4 = this.indicator.snappedLeftInset();
            double d5 = this.indicator.snappedRightInset();
            double d6 = this.indicator.snappedTopInset();
            double d7 = this.indicator.snappedBottomInset();
            double d8 = this.snapSize(Math.max(Math.max(d4, d5), Math.max(d6, d7)));
            double d9 = this.progress.snappedLeftInset();
            double d10 = this.progress.snappedRightInset();
            double d11 = this.progress.snappedTopInset();
            double d12 = this.progress.snappedBottomInset();
            double d13 = this.snapSize(Math.max(Math.max(d9, d10), Math.max(d11, d12)));
            double d14 = this.tick.snappedTopInset();
            double d15 = this.tick.snappedBottomInset();
            double d16 = d8 + d13 + d14 + d15 + d13 + d8;
            return d2 + d16 + this.textGap + this.doneTextHeight + d3;
        }

        @Override
        protected double computeMaxWidth(double d) {
            return this.computePrefWidth(d);
        }

        @Override
        protected double computeMaxHeight(double d) {
            return this.computePrefHeight(d);
        }
    }
}

