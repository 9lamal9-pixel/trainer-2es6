/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.behavior.TitledPaneBehavior;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Labeled;
import javafx.scene.control.TitledPane;
import javafx.scene.control.skin.LabeledSkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class TitledPaneSkin
extends LabeledSkinBase<TitledPane> {
    private static final Duration TRANSITION_DURATION = new Duration(350.0);
    private static final boolean CACHE_ANIMATION = PlatformUtil.isEmbedded();
    private final TitledPaneBehavior behavior;
    private final TitleRegion titleRegion;
    private final StackPane contentContainer;
    private Node content;
    private Timeline timeline;
    private double transitionStartValue;
    private Rectangle clipRect;
    private Pos pos;
    private HPos hpos;
    private VPos vpos;
    private DoubleProperty transition;
    private double prefHeightFromAccordion = 0.0;

    public TitledPaneSkin(TitledPane titledPane) {
        super(titledPane);
        this.behavior = new TitledPaneBehavior(titledPane);
        this.clipRect = new Rectangle();
        this.transitionStartValue = 0.0;
        this.titleRegion = new TitleRegion();
        this.content = ((TitledPane)this.getSkinnable()).getContent();
        this.contentContainer = new StackPane(){
            {
                this.getStyleClass().setAll((String[])new String[]{"content"});
                if (TitledPaneSkin.this.content != null) {
                    this.getChildren().setAll((Node[])new Node[]{TitledPaneSkin.this.content});
                }
            }
        };
        this.contentContainer.setClip(this.clipRect);
        this.updateClip();
        if (titledPane.isExpanded()) {
            this.setTransition(1.0);
            this.setExpanded(titledPane.isExpanded());
        } else {
            this.setTransition(0.0);
            if (this.content != null) {
                this.content.setVisible(false);
            }
        }
        this.getChildren().setAll((Node[])new Node[]{this.contentContainer, this.titleRegion});
        this.registerChangeListener(titledPane.contentProperty(), observableValue -> {
            this.content = ((TitledPane)this.getSkinnable()).getContent();
            if (this.content == null) {
                this.contentContainer.getChildren().clear();
            } else {
                this.contentContainer.getChildren().setAll((Node[])new Node[]{this.content});
            }
        });
        this.registerChangeListener(titledPane.expandedProperty(), observableValue -> this.setExpanded(((TitledPane)this.getSkinnable()).isExpanded()));
        this.registerChangeListener(titledPane.collapsibleProperty(), observableValue -> this.titleRegion.update());
        this.registerChangeListener(titledPane.alignmentProperty(), observableValue -> {
            this.pos = ((TitledPane)this.getSkinnable()).getAlignment();
            this.hpos = this.pos.getHpos();
            this.vpos = this.pos.getVpos();
        });
        this.registerChangeListener(titledPane.widthProperty(), observableValue -> this.updateClip());
        this.registerChangeListener(titledPane.heightProperty(), observableValue -> this.updateClip());
        this.registerChangeListener(this.titleRegion.alignmentProperty(), observableValue -> {
            this.pos = this.titleRegion.getAlignment();
            this.hpos = this.pos.getHpos();
            this.vpos = this.pos.getVpos();
        });
        this.pos = titledPane.getAlignment();
        this.hpos = this.pos == null ? HPos.LEFT : this.pos.getHpos();
        this.vpos = this.pos == null ? VPos.CENTER : this.pos.getVpos();
    }

    private final void setTransition(double d) {
        this.transitionProperty().set(d);
    }

    private final double getTransition() {
        return this.transition == null ? 0.0 : this.transition.get();
    }

    private final DoubleProperty transitionProperty() {
        if (this.transition == null) {
            this.transition = new SimpleDoubleProperty(this, "transition", 0.0){

                @Override
                protected void invalidated() {
                    TitledPaneSkin.this.contentContainer.requestLayout();
                }
            };
        }
        return this.transition;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected void updateChildren() {
        if (this.titleRegion != null) {
            this.titleRegion.update();
        }
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        double d5 = this.snapSizeY(this.titleRegion.prefHeight(-1.0));
        this.titleRegion.resize(d3, d5);
        this.positionInArea(this.titleRegion, d, d2, d3, d5, 0.0, HPos.LEFT, VPos.CENTER);
        this.titleRegion.requestLayout();
        double d6 = (d4 - d5) * this.getTransition();
        if (this.isInsideAccordion() && this.prefHeightFromAccordion != 0.0) {
            d6 = (this.prefHeightFromAccordion - d5) * this.getTransition();
        }
        d6 = this.snapSizeY(d6);
        this.contentContainer.resize(d3, d6);
        this.clipRect.setHeight(d6);
        this.positionInArea(this.contentContainer, d, d2 += d5, d3, d6, 0.0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = this.snapSizeX(this.titleRegion.prefWidth(d));
        double d7 = this.snapSizeX(this.contentContainer.minWidth(d));
        return Math.max(d6, d7) + d5 + d3;
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        double d6 = this.snapSizeY(this.titleRegion.prefHeight(d));
        double d7 = this.contentContainer.minHeight(d) * this.getTransition();
        return d6 + this.snapSizeY(d7) + d2 + d4;
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = this.snapSizeX(this.titleRegion.prefWidth(d));
        double d7 = this.snapSizeX(this.contentContainer.prefWidth(d));
        return Math.max(d6, d7) + d5 + d3;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        double d6 = this.snapSizeY(this.titleRegion.prefHeight(d));
        double d7 = this.contentContainer.prefHeight(d) * this.getTransition();
        return d6 + this.snapSizeY(d7) + d2 + d4;
    }

    @Override
    protected double computeMaxWidth(double d, double d2, double d3, double d4, double d5) {
        return Double.MAX_VALUE;
    }

    private void updateClip() {
        this.clipRect.setWidth(((TitledPane)this.getSkinnable()).getWidth());
        this.clipRect.setHeight(this.contentContainer.getHeight());
    }

    private void setExpanded(boolean bl) {
        if (!((TitledPane)this.getSkinnable()).isCollapsible()) {
            this.setTransition(1.0);
            return;
        }
        if (((TitledPane)this.getSkinnable()).isAnimated()) {
            this.transitionStartValue = this.getTransition();
            this.doAnimationTransition();
        } else {
            if (bl) {
                this.setTransition(1.0);
            } else {
                this.setTransition(0.0);
            }
            if (this.content != null) {
                this.content.setVisible(bl);
            }
            ((TitledPane)this.getSkinnable()).requestLayout();
        }
    }

    private boolean isInsideAccordion() {
        return ((TitledPane)this.getSkinnable()).getParent() != null && ((TitledPane)this.getSkinnable()).getParent() instanceof Accordion;
    }

    double getTitleRegionSize(double d) {
        return this.snapSizeY(this.titleRegion.prefHeight(d)) + this.snappedTopInset() + this.snappedBottomInset();
    }

    void setMaxTitledPaneHeightForAccordion(double d) {
        this.prefHeightFromAccordion = d;
    }

    double getTitledPaneHeightForAccordion() {
        double d = this.snapSizeY(this.titleRegion.prefHeight(-1.0));
        double d2 = (this.prefHeightFromAccordion - d) * this.getTransition();
        return d + this.snapSizeY(d2) + this.snappedTopInset() + this.snappedBottomInset();
    }

    private void doAnimationTransition() {
        KeyFrame keyFrame;
        KeyFrame keyFrame2;
        Duration duration;
        if (this.content == null) {
            return;
        }
        if (this.timeline != null && this.timeline.getStatus() != Animation.Status.STOPPED) {
            duration = this.timeline.getCurrentTime();
            this.timeline.stop();
        } else {
            duration = TRANSITION_DURATION;
        }
        this.timeline = new Timeline();
        this.timeline.setCycleCount(1);
        if (((TitledPane)this.getSkinnable()).isExpanded()) {
            keyFrame2 = new KeyFrame(Duration.ZERO, actionEvent -> {
                if (CACHE_ANIMATION) {
                    this.content.setCache(true);
                }
                this.content.setVisible(true);
            }, new KeyValue(this.transitionProperty(), this.transitionStartValue));
            keyFrame = new KeyFrame(duration, actionEvent -> {
                if (CACHE_ANIMATION) {
                    this.content.setCache(false);
                }
            }, new KeyValue(this.transitionProperty(), 1, Interpolator.LINEAR));
        } else {
            keyFrame2 = new KeyFrame(Duration.ZERO, actionEvent -> {
                if (CACHE_ANIMATION) {
                    this.content.setCache(true);
                }
            }, new KeyValue(this.transitionProperty(), this.transitionStartValue));
            keyFrame = new KeyFrame(duration, actionEvent -> {
                this.content.setVisible(false);
                if (CACHE_ANIMATION) {
                    this.content.setCache(false);
                }
            }, new KeyValue(this.transitionProperty(), 0, Interpolator.LINEAR));
        }
        this.timeline.getKeyFrames().setAll((KeyFrame[])new KeyFrame[]{keyFrame2, keyFrame});
        this.timeline.play();
    }

    class TitleRegion
    extends StackPane {
        private final StackPane arrowRegion;

        public TitleRegion() {
            this.getStyleClass().setAll((String[])new String[]{"title"});
            this.arrowRegion = new StackPane();
            this.arrowRegion.setId("arrowRegion");
            this.arrowRegion.getStyleClass().setAll((String[])new String[]{"arrow-button"});
            StackPane stackPane = new StackPane();
            stackPane.setId("arrow");
            stackPane.getStyleClass().setAll((String[])new String[]{"arrow"});
            this.arrowRegion.getChildren().setAll((Node[])new Node[]{stackPane});
            stackPane.rotateProperty().bind(new DoubleBinding(){
                {
                    this.bind(TitledPaneSkin.this.transitionProperty());
                }

                @Override
                protected double computeValue() {
                    return -90.0 * (1.0 - TitledPaneSkin.this.getTransition());
                }
            });
            this.setAlignment(Pos.CENTER_LEFT);
            this.setOnMouseReleased(mouseEvent -> {
                if (mouseEvent.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                ContextMenu contextMenu = ((TitledPane)TitledPaneSkin.this.getSkinnable()).getContextMenu();
                if (contextMenu != null) {
                    contextMenu.hide();
                }
                if (((TitledPane)TitledPaneSkin.this.getSkinnable()).isCollapsible() && ((TitledPane)TitledPaneSkin.this.getSkinnable()).isFocused()) {
                    TitledPaneSkin.this.behavior.toggle();
                }
            });
            this.update();
        }

        private void update() {
            this.getChildren().clear();
            TitledPane titledPane = (TitledPane)TitledPaneSkin.this.getSkinnable();
            if (titledPane.isCollapsible()) {
                this.getChildren().add(this.arrowRegion);
            }
            if (TitledPaneSkin.this.graphic != null) {
                TitledPaneSkin.this.graphic.layoutBoundsProperty().removeListener(TitledPaneSkin.this.graphicPropertyChangedListener);
            }
            TitledPaneSkin.this.graphic = titledPane.getGraphic();
            if (TitledPaneSkin.this.isIgnoreGraphic()) {
                if (titledPane.getContentDisplay() == ContentDisplay.GRAPHIC_ONLY) {
                    this.getChildren().clear();
                    this.getChildren().add(this.arrowRegion);
                } else {
                    this.getChildren().add(TitledPaneSkin.this.text);
                }
            } else {
                TitledPaneSkin.this.graphic.layoutBoundsProperty().addListener(TitledPaneSkin.this.graphicPropertyChangedListener);
                if (TitledPaneSkin.this.isIgnoreText()) {
                    this.getChildren().add(TitledPaneSkin.this.graphic);
                } else {
                    this.getChildren().addAll((Node[])new Node[]{TitledPaneSkin.this.graphic, TitledPaneSkin.this.text});
                }
            }
            this.setCursor(((TitledPane)TitledPaneSkin.this.getSkinnable()).isCollapsible() ? Cursor.HAND : Cursor.DEFAULT);
        }

        @Override
        protected double computePrefWidth(double d) {
            double d2 = this.snappedLeftInset();
            double d3 = this.snappedRightInset();
            double d4 = 0.0;
            double d5 = this.labelPrefWidth(d);
            if (this.arrowRegion != null) {
                d4 = this.snapSize(this.arrowRegion.prefWidth(d));
            }
            return d2 + d4 + d5 + d3;
        }

        @Override
        protected double computePrefHeight(double d) {
            double d2 = this.snappedTopInset();
            double d3 = this.snappedBottomInset();
            double d4 = 0.0;
            double d5 = this.labelPrefHeight(d);
            if (this.arrowRegion != null) {
                d4 = this.snapSize(this.arrowRegion.prefHeight(d));
            }
            return d2 + Math.max(d4, d5) + d3;
        }

        @Override
        protected void layoutChildren() {
            double d = this.snappedTopInset();
            double d2 = this.snappedBottomInset();
            double d3 = this.snappedLeftInset();
            double d4 = this.snappedRightInset();
            double d5 = this.getWidth() - (d3 + d4);
            double d6 = this.getHeight() - (d + d2);
            double d7 = this.snapSize(this.arrowRegion.prefWidth(-1.0));
            double d8 = this.snapSize(this.arrowRegion.prefHeight(-1.0));
            double d9 = this.snapSize(Math.min(d5 - d7 / 2.0, this.labelPrefWidth(-1.0)));
            double d10 = this.snapSize(this.labelPrefHeight(-1.0));
            double d11 = d3 + d7 + Utils.computeXOffset(d5 - d7, d9, TitledPaneSkin.this.hpos);
            if (HPos.CENTER == TitledPaneSkin.this.hpos) {
                d11 = d3 + Utils.computeXOffset(d5, d9, TitledPaneSkin.this.hpos);
            }
            double d12 = d + Utils.computeYOffset(d6, Math.max(d8, d10), TitledPaneSkin.this.vpos);
            this.arrowRegion.resize(d7, d8);
            this.positionInArea(this.arrowRegion, d3, d, d7, d6, 0.0, HPos.CENTER, VPos.CENTER);
            TitledPaneSkin.this.layoutLabelInArea(d11, d12, d9, d6, TitledPaneSkin.this.pos);
        }

        private double labelPrefWidth(double d) {
            Labeled labeled = (Labeled)TitledPaneSkin.this.getSkinnable();
            Font font = TitledPaneSkin.this.text.getFont();
            String string = labeled.getText();
            boolean bl = string == null || string.isEmpty();
            Insets insets = labeled.getLabelPadding();
            double d2 = insets.getLeft() + insets.getRight();
            double d3 = bl ? 0.0 : Utils.computeTextWidth(font, string, 0.0);
            Node node = labeled.getGraphic();
            if (TitledPaneSkin.this.isIgnoreGraphic()) {
                return d3 + d2;
            }
            if (TitledPaneSkin.this.isIgnoreText()) {
                return node.prefWidth(-1.0) + d2;
            }
            if (labeled.getContentDisplay() == ContentDisplay.LEFT || labeled.getContentDisplay() == ContentDisplay.RIGHT) {
                return d3 + labeled.getGraphicTextGap() + node.prefWidth(-1.0) + d2;
            }
            return Math.max(d3, node.prefWidth(-1.0)) + d2;
        }

        private double labelPrefHeight(double d) {
            double d2;
            Labeled labeled = (Labeled)TitledPaneSkin.this.getSkinnable();
            Font font = TitledPaneSkin.this.text.getFont();
            ContentDisplay contentDisplay = labeled.getContentDisplay();
            double d3 = labeled.getGraphicTextGap();
            Insets insets = labeled.getLabelPadding();
            double d4 = this.snappedLeftInset() + this.snappedRightInset() + insets.getLeft() + insets.getRight();
            String string = labeled.getText();
            if (string != null && string.endsWith("\n")) {
                string = string.substring(0, string.length() - 1);
            }
            if (!(TitledPaneSkin.this.isIgnoreGraphic() || contentDisplay != ContentDisplay.LEFT && contentDisplay != ContentDisplay.RIGHT)) {
                d -= TitledPaneSkin.this.graphic.prefWidth(-1.0) + d3;
            }
            double d5 = d2 = Utils.computeTextHeight(font, string, labeled.isWrapText() ? (d -= d4) : 0.0, TitledPaneSkin.this.text.getBoundsType());
            if (!TitledPaneSkin.this.isIgnoreGraphic()) {
                Node node = labeled.getGraphic();
                d5 = contentDisplay == ContentDisplay.TOP || contentDisplay == ContentDisplay.BOTTOM ? node.prefHeight(-1.0) + d3 + d2 : Math.max(d2, node.prefHeight(-1.0));
            }
            return d5 + insets.getTop() + insets.getBottom();
        }
    }
}

