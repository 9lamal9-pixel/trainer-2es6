/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.NodeHelper;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.animation.Transition;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.SizeConverter;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.ProgressIndicatorSkin;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ProgressBarSkin
extends ProgressIndicatorSkin {
    private StackPane bar;
    private StackPane track;
    private Region clipRegion;
    private double barWidth;
    private DoubleProperty indeterminateBarLength = null;
    private BooleanProperty indeterminateBarEscape = null;
    private BooleanProperty indeterminateBarFlip = null;
    private DoubleProperty indeterminateBarAnimationTime = null;
    boolean wasIndeterminate = false;

    public ProgressBarSkin(ProgressBar progressBar) {
        super(progressBar);
        this.barWidth = (double)((int)(progressBar.getWidth() - this.snappedLeftInset() - this.snappedRightInset()) * 2) * Math.min(1.0, Math.max(0.0, progressBar.getProgress())) / 2.0;
        progressBar.widthProperty().addListener(observable -> this.updateProgress());
        this.initialize();
        ((ProgressIndicator)this.getSkinnable()).requestLayout();
    }

    private DoubleProperty indeterminateBarLengthProperty() {
        if (this.indeterminateBarLength == null) {
            this.indeterminateBarLength = new StyleableDoubleProperty(60.0){

                @Override
                public Object getBean() {
                    return ProgressBarSkin.this;
                }

                @Override
                public String getName() {
                    return "indeterminateBarLength";
                }

                @Override
                public CssMetaData<ProgressBar, Number> getCssMetaData() {
                    return StyleableProperties.INDETERMINATE_BAR_LENGTH;
                }
            };
        }
        return this.indeterminateBarLength;
    }

    private Double getIndeterminateBarLength() {
        return this.indeterminateBarLength == null ? 60.0 : this.indeterminateBarLength.get();
    }

    private BooleanProperty indeterminateBarEscapeProperty() {
        if (this.indeterminateBarEscape == null) {
            this.indeterminateBarEscape = new StyleableBooleanProperty(true){

                @Override
                public Object getBean() {
                    return ProgressBarSkin.this;
                }

                @Override
                public String getName() {
                    return "indeterminateBarEscape";
                }

                @Override
                public CssMetaData<ProgressBar, Boolean> getCssMetaData() {
                    return StyleableProperties.INDETERMINATE_BAR_ESCAPE;
                }
            };
        }
        return this.indeterminateBarEscape;
    }

    private Boolean getIndeterminateBarEscape() {
        return this.indeterminateBarEscape == null ? true : this.indeterminateBarEscape.get();
    }

    private BooleanProperty indeterminateBarFlipProperty() {
        if (this.indeterminateBarFlip == null) {
            this.indeterminateBarFlip = new StyleableBooleanProperty(true){

                @Override
                public Object getBean() {
                    return ProgressBarSkin.this;
                }

                @Override
                public String getName() {
                    return "indeterminateBarFlip";
                }

                @Override
                public CssMetaData<ProgressBar, Boolean> getCssMetaData() {
                    return StyleableProperties.INDETERMINATE_BAR_FLIP;
                }
            };
        }
        return this.indeterminateBarFlip;
    }

    private Boolean getIndeterminateBarFlip() {
        return this.indeterminateBarFlip == null ? true : this.indeterminateBarFlip.get();
    }

    private DoubleProperty indeterminateBarAnimationTimeProperty() {
        if (this.indeterminateBarAnimationTime == null) {
            this.indeterminateBarAnimationTime = new StyleableDoubleProperty(2.0){

                @Override
                public Object getBean() {
                    return ProgressBarSkin.this;
                }

                @Override
                public String getName() {
                    return "indeterminateBarAnimationTime";
                }

                @Override
                public CssMetaData<ProgressBar, Number> getCssMetaData() {
                    return StyleableProperties.INDETERMINATE_BAR_ANIMATION_TIME;
                }
            };
        }
        return this.indeterminateBarAnimationTime;
    }

    private double getIndeterminateBarAnimationTime() {
        return this.indeterminateBarAnimationTime == null ? 2.0 : this.indeterminateBarAnimationTime.get();
    }

    @Override
    public double computeBaselineOffset(double d, double d2, double d3, double d4) {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        return Math.max(100.0, d5 + this.bar.prefWidth(((ProgressIndicator)this.getSkinnable()).getWidth()) + d3);
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        return d2 + this.bar.prefHeight(d) + d4;
    }

    @Override
    protected double computeMaxWidth(double d, double d2, double d3, double d4, double d5) {
        return ((ProgressIndicator)this.getSkinnable()).prefWidth(d);
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        return ((ProgressIndicator)this.getSkinnable()).prefHeight(d);
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        ProgressIndicator progressIndicator = (ProgressIndicator)this.getSkinnable();
        boolean bl = progressIndicator.isIndeterminate();
        this.clipRegion.resizeRelocate(0.0, 0.0, d3, d4);
        this.track.resizeRelocate(d, d2, d3, d4);
        this.bar.resizeRelocate(d, d2, bl ? this.getIndeterminateBarLength() : this.barWidth, d4);
        this.track.setVisible(true);
        if (bl) {
            this.createIndeterminateTimeline();
            if (NodeHelper.isTreeShowing((Node)((Object)this.getSkinnable()))) {
                this.indeterminateTransition.play();
            }
            this.bar.setClip(this.clipRegion);
        } else if (this.indeterminateTransition != null) {
            this.indeterminateTransition.stop();
            this.indeterminateTransition = null;
            this.bar.setClip(null);
            this.bar.setScaleX(1.0);
            this.bar.setTranslateX(0.0);
            this.clipRegion.translateXProperty().unbind();
        }
    }

    @Override
    void initialize() {
        this.track = new StackPane();
        this.track.getStyleClass().setAll((String[])new String[]{"track"});
        this.bar = new StackPane();
        this.bar.getStyleClass().setAll((String[])new String[]{"bar"});
        this.getChildren().setAll((Node[])new Node[]{this.track, this.bar});
        this.clipRegion = new Region();
        this.bar.backgroundProperty().addListener((observableValue, background, background2) -> {
            if (background2 != null && !background2.getFills().isEmpty()) {
                BackgroundFill[] backgroundFillArray = new BackgroundFill[background2.getFills().size()];
                for (int i = 0; i < background2.getFills().size(); ++i) {
                    BackgroundFill backgroundFill = background2.getFills().get(i);
                    backgroundFillArray[i] = new BackgroundFill(Color.BLACK, backgroundFill.getRadii(), backgroundFill.getInsets());
                }
                this.clipRegion.setBackground(new Background(backgroundFillArray));
            }
        });
    }

    @Override
    void createIndeterminateTimeline() {
        if (this.indeterminateTransition != null) {
            this.indeterminateTransition.stop();
        }
        ProgressIndicator progressIndicator = (ProgressIndicator)this.getSkinnable();
        double d = progressIndicator.getWidth() - (this.snappedLeftInset() + this.snappedRightInset());
        double d2 = this.getIndeterminateBarEscape() != false ? -this.getIndeterminateBarLength().doubleValue() : 0.0;
        double d3 = this.getIndeterminateBarEscape() != false ? d : d - this.getIndeterminateBarLength();
        this.indeterminateTransition = new IndeterminateTransition(d2, d3, this);
        this.indeterminateTransition.setCycleCount(-1);
        this.clipRegion.translateXProperty().bind(new When(this.bar.scaleXProperty().isEqualTo(-1.0, 1.0E-100)).then(this.bar.translateXProperty().subtract(d).add(this.indeterminateBarLengthProperty())).otherwise(this.bar.translateXProperty().negate()));
    }

    @Override
    void updateProgress() {
        ProgressIndicator progressIndicator = (ProgressIndicator)this.getSkinnable();
        boolean bl = progressIndicator.isIndeterminate();
        if (!bl || !this.wasIndeterminate) {
            this.barWidth = (double)((int)(progressIndicator.getWidth() - this.snappedLeftInset() - this.snappedRightInset()) * 2) * Math.min(1.0, Math.max(0.0, progressIndicator.getProgress())) / 2.0;
            ((ProgressIndicator)this.getSkinnable()).requestLayout();
        }
        this.wasIndeterminate = bl;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return ProgressBarSkin.getClassCssMetaData();
    }

    private static class IndeterminateTransition
    extends Transition {
        private final WeakReference<ProgressBarSkin> skin;
        private final double startX;
        private final double endX;
        private final boolean flip;

        public IndeterminateTransition(double d, double d2, ProgressBarSkin progressBarSkin) {
            this.startX = d;
            this.endX = d2;
            this.skin = new WeakReference<ProgressBarSkin>(progressBarSkin);
            this.flip = progressBarSkin.getIndeterminateBarFlip();
            progressBarSkin.getIndeterminateBarEscape();
            this.setCycleDuration(Duration.seconds(progressBarSkin.getIndeterminateBarAnimationTime() * (double)(this.flip ? 2 : 1)));
        }

        @Override
        protected void interpolate(double d) {
            ProgressBarSkin progressBarSkin = (ProgressBarSkin)this.skin.get();
            if (progressBarSkin == null) {
                this.stop();
            } else if (d <= 0.5 || !this.flip) {
                progressBarSkin.bar.setScaleX(-1.0);
                progressBarSkin.bar.setTranslateX(this.startX + (double)(this.flip ? 2 : 1) * d * (this.endX - this.startX));
            } else {
                progressBarSkin.bar.setScaleX(1.0);
                progressBarSkin.bar.setTranslateX(this.startX + 2.0 * (1.0 - d) * (this.endX - this.startX));
            }
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<ProgressBar, Number> INDETERMINATE_BAR_LENGTH = new CssMetaData<ProgressBar, Number>("-fx-indeterminate-bar-length", SizeConverter.getInstance(), (Number)60.0){

            @Override
            public boolean isSettable(ProgressBar progressBar) {
                ProgressBarSkin progressBarSkin = (ProgressBarSkin)progressBar.getSkin();
                return progressBarSkin.indeterminateBarLength == null || !progressBarSkin.indeterminateBarLength.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(ProgressBar progressBar) {
                ProgressBarSkin progressBarSkin = (ProgressBarSkin)progressBar.getSkin();
                return (StyleableProperty)((Object)progressBarSkin.indeterminateBarLengthProperty());
            }
        };
        private static final CssMetaData<ProgressBar, Boolean> INDETERMINATE_BAR_ESCAPE = new CssMetaData<ProgressBar, Boolean>("-fx-indeterminate-bar-escape", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(ProgressBar progressBar) {
                ProgressBarSkin progressBarSkin = (ProgressBarSkin)progressBar.getSkin();
                return progressBarSkin.indeterminateBarEscape == null || !progressBarSkin.indeterminateBarEscape.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(ProgressBar progressBar) {
                ProgressBarSkin progressBarSkin = (ProgressBarSkin)progressBar.getSkin();
                return (StyleableProperty)((Object)progressBarSkin.indeterminateBarEscapeProperty());
            }
        };
        private static final CssMetaData<ProgressBar, Boolean> INDETERMINATE_BAR_FLIP = new CssMetaData<ProgressBar, Boolean>("-fx-indeterminate-bar-flip", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(ProgressBar progressBar) {
                ProgressBarSkin progressBarSkin = (ProgressBarSkin)progressBar.getSkin();
                return progressBarSkin.indeterminateBarFlip == null || !progressBarSkin.indeterminateBarFlip.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(ProgressBar progressBar) {
                ProgressBarSkin progressBarSkin = (ProgressBarSkin)progressBar.getSkin();
                return (StyleableProperty)((Object)progressBarSkin.indeterminateBarFlipProperty());
            }
        };
        private static final CssMetaData<ProgressBar, Number> INDETERMINATE_BAR_ANIMATION_TIME = new CssMetaData<ProgressBar, Number>("-fx-indeterminate-bar-animation-time", SizeConverter.getInstance(), (Number)2.0){

            @Override
            public boolean isSettable(ProgressBar progressBar) {
                ProgressBarSkin progressBarSkin = (ProgressBarSkin)progressBar.getSkin();
                return progressBarSkin.indeterminateBarAnimationTime == null || !progressBarSkin.indeterminateBarAnimationTime.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(ProgressBar progressBar) {
                ProgressBarSkin progressBarSkin = (ProgressBarSkin)progressBar.getSkin();
                return (StyleableProperty)((Object)progressBarSkin.indeterminateBarAnimationTimeProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(SkinBase.getClassCssMetaData());
            arrayList.add(INDETERMINATE_BAR_LENGTH);
            arrayList.add(INDETERMINATE_BAR_ESCAPE);
            arrayList.add(INDETERMINATE_BAR_FLIP);
            arrayList.add(INDETERMINATE_BAR_ANIMATION_TIME);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

