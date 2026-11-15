/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.LabeledText;
import com.sun.javafx.scene.control.behavior.TextBinding;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.HPos;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Skinnable;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.Mnemonic;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public abstract class LabeledSkinBase<C extends Labeled>
extends SkinBase<C> {
    LabeledText text;
    boolean invalidText = true;
    Node graphic;
    double textWidth = Double.NEGATIVE_INFINITY;
    double ellipsisWidth = Double.NEGATIVE_INFINITY;
    final InvalidationListener graphicPropertyChangedListener = observable -> {
        this.invalidText = true;
        if (this.getSkinnable() != null) {
            ((Labeled)this.getSkinnable()).requestLayout();
        }
    };
    private Rectangle textClip;
    private double wrapWidth;
    private double wrapHeight;
    private TextBinding bindings;
    private Line mnemonic_underscore;
    private boolean containsMnemonic = false;
    private Scene mnemonicScene = null;
    private KeyCombination mnemonicCode;
    private Node labeledNode = null;

    public LabeledSkinBase(C c) {
        super(c);
        this.text = new LabeledText((Labeled)c);
        this.updateChildren();
        this.registerChangeListener(((Labeled)c).ellipsisStringProperty(), observableValue -> {
            this.textMetricsChanged();
            this.invalidateWidths();
            this.ellipsisWidth = Double.NEGATIVE_INFINITY;
        });
        this.registerChangeListener(((Region)c).widthProperty(), observableValue -> {
            this.updateWrappingWidth();
            this.invalidText = true;
        });
        this.registerChangeListener(((Region)c).heightProperty(), observableValue -> {
            this.invalidText = true;
        });
        this.registerChangeListener(((Labeled)c).fontProperty(), observableValue -> {
            this.textMetricsChanged();
            this.invalidateWidths();
            this.ellipsisWidth = Double.NEGATIVE_INFINITY;
        });
        this.registerChangeListener(((Labeled)c).graphicProperty(), observableValue -> {
            this.updateChildren();
            this.textMetricsChanged();
        });
        this.registerChangeListener(((Labeled)c).contentDisplayProperty(), observableValue -> {
            this.updateChildren();
            this.textMetricsChanged();
        });
        this.registerChangeListener(((Labeled)c).labelPaddingProperty(), observableValue -> this.textMetricsChanged());
        this.registerChangeListener(((Labeled)c).graphicTextGapProperty(), observableValue -> this.textMetricsChanged());
        this.registerChangeListener(((Labeled)c).alignmentProperty(), observableValue -> ((Labeled)this.getSkinnable()).requestLayout());
        this.registerChangeListener(((Labeled)c).mnemonicParsingProperty(), observableValue -> {
            this.containsMnemonic = false;
            this.textMetricsChanged();
        });
        this.registerChangeListener(((Labeled)c).textProperty(), observableValue -> {
            this.updateChildren();
            this.textMetricsChanged();
            this.invalidateWidths();
        });
        this.registerChangeListener(((Labeled)c).textAlignmentProperty(), observableValue -> {});
        this.registerChangeListener(((Labeled)c).textOverrunProperty(), observableValue -> this.textMetricsChanged());
        this.registerChangeListener(((Labeled)c).wrapTextProperty(), observableValue -> {
            this.updateWrappingWidth();
            this.textMetricsChanged();
        });
        this.registerChangeListener(((Labeled)c).underlineProperty(), observableValue -> this.textMetricsChanged());
        this.registerChangeListener(((Labeled)c).lineSpacingProperty(), observableValue -> this.textMetricsChanged());
        this.registerChangeListener(((Node)c).sceneProperty(), observableValue -> this.sceneChanged());
    }

    protected void updateChildren() {
        Labeled labeled = (Labeled)this.getSkinnable();
        if (this.graphic != null) {
            this.graphic.layoutBoundsProperty().removeListener(this.graphicPropertyChangedListener);
        }
        this.graphic = labeled.getGraphic();
        if (this.graphic instanceof ImageView) {
            this.graphic.setMouseTransparent(true);
        }
        if (this.isIgnoreGraphic()) {
            if (labeled.getContentDisplay() == ContentDisplay.GRAPHIC_ONLY) {
                this.getChildren().clear();
            } else {
                this.getChildren().setAll((Node[])new Node[]{this.text});
            }
        } else {
            this.graphic.layoutBoundsProperty().addListener(this.graphicPropertyChangedListener);
            if (this.isIgnoreText()) {
                this.getChildren().setAll((Node[])new Node[]{this.graphic});
            } else {
                this.getChildren().setAll((Node[])new Node[]{this.graphic, this.text});
            }
        }
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        return this.computeMinLabeledPartWidth(d, d2, d3, d4, d5);
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        return this.computeMinLabeledPartHeight(d, d2, d3, d4, d5);
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6;
        Labeled labeled = (Labeled)this.getSkinnable();
        Font font = this.text.getFont();
        String string = labeled.getText();
        boolean bl = string == null || string.isEmpty();
        double d7 = d5 + d3;
        if (!this.isIgnoreText()) {
            d7 += this.leftLabelPadding() + this.rightLabelPadding();
        }
        double d8 = 0.0;
        if (!bl) {
            if (labeled.isMnemonicParsing() && string.contains("_") && string.indexOf("_") != string.length() - 1) {
                string = string.replaceFirst("_", "");
            }
            d8 = Utils.computeTextWidth(font, string, 0.0);
        }
        double d9 = d6 = this.graphic == null ? 0.0 : Utils.boundedSize(this.graphic.prefWidth(-1.0), this.graphic.minWidth(-1.0), this.graphic.maxWidth(-1.0));
        if (this.isIgnoreGraphic()) {
            return d8 + d7;
        }
        if (this.isIgnoreText()) {
            return d6 + d7;
        }
        if (labeled.getContentDisplay() == ContentDisplay.LEFT || labeled.getContentDisplay() == ContentDisplay.RIGHT) {
            return d8 + labeled.getGraphicTextGap() + d6 + d7;
        }
        return Math.max(d8, d6) + d7;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        double d6;
        String string;
        Labeled labeled = (Labeled)this.getSkinnable();
        Font font = this.text.getFont();
        ContentDisplay contentDisplay = labeled.getContentDisplay();
        double d7 = labeled.getGraphicTextGap();
        d -= d5 + d3;
        if (!this.isIgnoreText()) {
            d -= this.leftLabelPadding() + this.rightLabelPadding();
        }
        if ((string = labeled.getText()) != null && string.endsWith("\n")) {
            string = string.substring(0, string.length() - 1);
        }
        double d8 = d;
        if (!(this.isIgnoreGraphic() || contentDisplay != ContentDisplay.LEFT && contentDisplay != ContentDisplay.RIGHT)) {
            d8 -= this.graphic.prefWidth(-1.0) + d7;
        }
        double d9 = d6 = Utils.computeTextHeight(font, string, labeled.isWrapText() ? d8 : 0.0, labeled.getLineSpacing(), this.text.getBoundsType());
        if (!this.isIgnoreGraphic()) {
            Node node = labeled.getGraphic();
            d9 = contentDisplay == ContentDisplay.TOP || contentDisplay == ContentDisplay.BOTTOM ? node.prefHeight(d) + d7 + d6 : Math.max(d6, node.prefHeight(d));
        }
        double d10 = d2 + d4;
        if (!this.isIgnoreText()) {
            d10 += this.topLabelPadding() + this.bottomLabelPadding();
        }
        return d9 + d10;
    }

    @Override
    protected double computeMaxWidth(double d, double d2, double d3, double d4, double d5) {
        return ((Labeled)this.getSkinnable()).prefWidth(d);
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        return ((Labeled)this.getSkinnable()).prefHeight(d);
    }

    @Override
    public double computeBaselineOffset(double d, double d2, double d3, double d4) {
        double d5;
        double d6 = d5 = this.text.getBaselineOffset();
        Labeled labeled = (Labeled)this.getSkinnable();
        Node node = labeled.getGraphic();
        if (!this.isIgnoreGraphic()) {
            ContentDisplay contentDisplay = labeled.getContentDisplay();
            if (contentDisplay == ContentDisplay.TOP) {
                d6 = node.prefHeight(-1.0) + labeled.getGraphicTextGap() + d5;
            } else if (contentDisplay == ContentDisplay.LEFT || contentDisplay == ContentDisplay.RIGHT) {
                d6 = d5 + (node.prefHeight(-1.0) - this.text.prefHeight(-1.0)) / 2.0;
            }
        }
        double d7 = d + d6;
        if (!this.isIgnoreText()) {
            d7 += this.topLabelPadding();
        }
        return d7;
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        this.layoutLabelInArea(d, d2, d3, d4);
    }

    protected void layoutLabelInArea(double d, double d2, double d3, double d4) {
        this.layoutLabelInArea(d, d2, d3, d4, null);
    }

    protected void layoutLabelInArea(double d, double d2, double d3, double d4, Pos pos) {
        double d5;
        double d6;
        double d7;
        double d8;
        Labeled labeled = (Labeled)this.getSkinnable();
        ContentDisplay contentDisplay = labeled.getContentDisplay();
        if (pos == null) {
            pos = labeled.getAlignment();
        }
        HPos hPos = pos == null ? HPos.LEFT : pos.getHpos();
        VPos vPos = pos == null ? VPos.CENTER : pos.getVpos();
        boolean bl = this.isIgnoreGraphic();
        boolean bl2 = this.isIgnoreText();
        if (!bl2) {
            d += this.leftLabelPadding();
            d2 += this.topLabelPadding();
            d3 -= this.leftLabelPadding() + this.rightLabelPadding();
            d4 -= this.topLabelPadding() + this.bottomLabelPadding();
        }
        if (bl) {
            d8 = 0.0;
            d7 = 0.0;
        } else if (bl2) {
            if (this.graphic.isResizable()) {
                Orientation orientation = this.graphic.getContentBias();
                if (orientation == Orientation.HORIZONTAL) {
                    d7 = Utils.boundedSize(d3, this.graphic.minWidth(-1.0), this.graphic.maxWidth(-1.0));
                    d8 = Utils.boundedSize(d4, this.graphic.minHeight(d7), this.graphic.maxHeight(d7));
                } else if (orientation == Orientation.VERTICAL) {
                    d8 = Utils.boundedSize(d4, this.graphic.minHeight(-1.0), this.graphic.maxHeight(-1.0));
                    d7 = Utils.boundedSize(d3, this.graphic.minWidth(d8), this.graphic.maxWidth(d8));
                } else {
                    d7 = Utils.boundedSize(d3, this.graphic.minWidth(-1.0), this.graphic.maxWidth(-1.0));
                    d8 = Utils.boundedSize(d4, this.graphic.minHeight(-1.0), this.graphic.maxHeight(-1.0));
                }
                this.graphic.resize(d7, d8);
            } else {
                d7 = this.graphic.getLayoutBounds().getWidth();
                d8 = this.graphic.getLayoutBounds().getHeight();
            }
        } else {
            this.graphic.autosize();
            d7 = this.graphic.getLayoutBounds().getWidth();
            d8 = this.graphic.getLayoutBounds().getHeight();
        }
        if (bl2) {
            d6 = 0.0;
            d5 = 0.0;
            this.text.setText("");
        } else {
            this.updateDisplayedText(d3, d4);
            d5 = this.snapSizeX(Math.min(this.text.getLayoutBounds().getWidth(), this.wrapWidth));
            d6 = this.snapSizeY(Math.min(this.text.getLayoutBounds().getHeight(), this.wrapHeight));
        }
        double d9 = bl2 || bl ? 0.0 : labeled.getGraphicTextGap();
        double d10 = Math.max(d7, d5);
        double d11 = Math.max(d8, d6);
        if (contentDisplay == ContentDisplay.TOP || contentDisplay == ContentDisplay.BOTTOM) {
            d11 = d8 + d9 + d6;
        } else if (contentDisplay == ContentDisplay.LEFT || contentDisplay == ContentDisplay.RIGHT) {
            d10 = d7 + d9 + d5;
        }
        double d12 = hPos == HPos.LEFT ? d : (hPos == HPos.RIGHT ? d + (d3 - d10) : d + (d3 - d10) / 2.0);
        double d13 = vPos == VPos.TOP ? d2 : (vPos == VPos.BOTTOM ? d2 + (d4 - d11) : d2 + (d4 - d11) / 2.0);
        Point2D point2D = null;
        double d14 = 0.0;
        double d15 = 0.0;
        if (this.containsMnemonic) {
            Font font = this.text.getFont();
            String string = this.bindings.getText();
            point2D = Utils.computeMnemonicPosition(font, string, this.bindings.getMnemonicIndex(), this.wrapWidth, labeled.getLineSpacing());
            d14 = Utils.computeTextWidth(font, string.substring(this.bindings.getMnemonicIndex(), this.bindings.getMnemonicIndex() + 1), 0.0);
            d15 = Utils.computeTextHeight(font, "_", 0.0, this.text.getBoundsType());
        }
        if (!(bl && bl2 || this.text.isManaged())) {
            this.text.setManaged(true);
        }
        if (bl && bl2) {
            if (this.text.isManaged()) {
                this.text.setManaged(false);
            }
            this.text.relocate(this.snapPositionX(d12), this.snapPositionY(d13));
        } else if (bl) {
            this.text.relocate(this.snapPositionX(d12), this.snapPositionY(d13));
            if (this.containsMnemonic && point2D != null) {
                this.mnemonic_underscore.setEndX(d14 - 2.0);
                this.mnemonic_underscore.relocate(this.snapPositionX(d12 + point2D.getX()), this.snapPositionY(d13 + point2D.getY()));
            }
        } else if (bl2) {
            this.text.relocate(this.snapPositionX(d12), this.snapPositionY(d13));
            this.graphic.relocate(this.snapPositionX(d12), this.snapPositionY(d13));
            if (this.containsMnemonic && point2D != null) {
                this.mnemonic_underscore.setEndX(d14);
                this.mnemonic_underscore.setStrokeWidth(d15 / 10.0);
                this.mnemonic_underscore.relocate(this.snapPositionX(d12 + point2D.getX()), this.snapPositionY(d13 + point2D.getY()));
            }
        } else {
            double d16 = 0.0;
            double d17 = 0.0;
            double d18 = 0.0;
            double d19 = 0.0;
            if (contentDisplay == ContentDisplay.TOP) {
                d16 = d12 + (d10 - d7) / 2.0;
                d18 = d12 + (d10 - d5) / 2.0;
                d17 = d13;
                d19 = d17 + d8 + d9;
            } else if (contentDisplay == ContentDisplay.RIGHT) {
                d18 = d12;
                d16 = d18 + d5 + d9;
                d17 = d13 + (d11 - d8) / 2.0;
                d19 = d13 + (d11 - d6) / 2.0;
            } else if (contentDisplay == ContentDisplay.BOTTOM) {
                d16 = d12 + (d10 - d7) / 2.0;
                d18 = d12 + (d10 - d5) / 2.0;
                d19 = d13;
                d17 = d19 + d6 + d9;
            } else if (contentDisplay == ContentDisplay.LEFT) {
                d16 = d12;
                d18 = d16 + d7 + d9;
                d17 = d13 + (d11 - d8) / 2.0;
                d19 = d13 + (d11 - d6) / 2.0;
            } else if (contentDisplay == ContentDisplay.CENTER) {
                d16 = d12 + (d10 - d7) / 2.0;
                d18 = d12 + (d10 - d5) / 2.0;
                d17 = d13 + (d11 - d8) / 2.0;
                d19 = d13 + (d11 - d6) / 2.0;
            }
            this.text.relocate(this.snapPositionX(d18), this.snapPositionY(d19));
            if (this.containsMnemonic && point2D != null) {
                this.mnemonic_underscore.setEndX(d14);
                this.mnemonic_underscore.setStrokeWidth(d15 / 10.0);
                this.mnemonic_underscore.relocate(this.snapPositionX(d18 + point2D.getX()), this.snapPositionY(d19 + point2D.getY()));
            }
            this.graphic.relocate(this.snapPositionX(d16), this.snapPositionY(d17));
        }
        if (this.text != null && (this.text.getLayoutBounds().getHeight() > this.wrapHeight || this.text.getLayoutBounds().getWidth() > this.wrapWidth)) {
            if (this.textClip == null) {
                this.textClip = new Rectangle();
            }
            if (labeled.getEffectiveNodeOrientation() == NodeOrientation.LEFT_TO_RIGHT) {
                this.textClip.setX(this.text.getLayoutBounds().getMinX());
            } else {
                this.textClip.setX(this.text.getLayoutBounds().getMaxX() - this.wrapWidth);
            }
            this.textClip.setY(this.text.getLayoutBounds().getMinY());
            this.textClip.setWidth(this.wrapWidth);
            this.textClip.setHeight(this.wrapHeight);
            if (this.text.getClip() == null) {
                this.text.setClip(this.textClip);
            }
        } else if (this.text.getClip() != null) {
            this.text.setClip(null);
        }
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case TEXT: {
                Object object;
                String string;
                Labeled labeled = (Labeled)this.getSkinnable();
                String string2 = labeled.getAccessibleText();
                if (string2 != null && !string2.isEmpty()) {
                    return string2;
                }
                if (this.bindings != null && (string = this.bindings.getText()) != null && !string.isEmpty()) {
                    return string;
                }
                string = labeled.getText();
                if (string != null && !string.isEmpty()) {
                    return string;
                }
                if (this.graphic != null && (object = this.graphic.queryAccessibleAttribute(AccessibleAttribute.TEXT, new Object[0])) != null) {
                    return object;
                }
                return null;
            }
            case MNEMONIC: {
                if (this.bindings != null) {
                    return this.bindings.getMnemonic();
                }
                return null;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    private double computeMinLabeledPartWidth(double d, double d2, double d3, double d4, double d5) {
        boolean bl;
        Labeled labeled = (Labeled)this.getSkinnable();
        ContentDisplay contentDisplay = labeled.getContentDisplay();
        double d6 = labeled.getGraphicTextGap();
        double d7 = 0.0;
        Font font = this.text.getFont();
        OverrunStyle overrunStyle = labeled.getTextOverrun();
        String string = labeled.getEllipsisString();
        String string2 = labeled.getText();
        boolean bl2 = bl = string2 == null || string2.isEmpty();
        if (!bl) {
            if (overrunStyle == OverrunStyle.CLIP) {
                if (this.textWidth == Double.NEGATIVE_INFINITY) {
                    this.textWidth = Utils.computeTextWidth(font, string2.substring(0, 1), 0.0);
                }
                d7 = this.textWidth;
            } else {
                if (this.textWidth == Double.NEGATIVE_INFINITY) {
                    this.textWidth = Utils.computeTextWidth(font, string2, 0.0);
                }
                if (this.ellipsisWidth == Double.NEGATIVE_INFINITY) {
                    this.ellipsisWidth = Utils.computeTextWidth(font, string, 0.0);
                }
                d7 = Math.min(this.textWidth, this.ellipsisWidth);
            }
        }
        Node node = labeled.getGraphic();
        double d8 = this.isIgnoreGraphic() ? d7 : (this.isIgnoreText() ? node.minWidth(-1.0) : (contentDisplay == ContentDisplay.LEFT || contentDisplay == ContentDisplay.RIGHT ? d7 + node.minWidth(-1.0) + d6 : Math.max(d7, node.minWidth(-1.0))));
        double d9 = d5 + d3;
        if (!this.isIgnoreText()) {
            d9 += this.leftLabelPadding() + this.rightLabelPadding();
        }
        return d8 + d9;
    }

    private double computeMinLabeledPartHeight(double d, double d2, double d3, double d4, double d5) {
        double d6;
        int n;
        Labeled labeled = (Labeled)this.getSkinnable();
        Font font = this.text.getFont();
        String string = labeled.getText();
        if (string != null && string.length() > 0 && (n = string.indexOf(10)) >= 0) {
            string = string.substring(0, n);
        }
        double d7 = labeled.getLineSpacing();
        double d8 = d6 = Utils.computeTextHeight(font, string, 0.0, d7, this.text.getBoundsType());
        if (!this.isIgnoreGraphic()) {
            Node node = labeled.getGraphic();
            d8 = labeled.getContentDisplay() == ContentDisplay.TOP || labeled.getContentDisplay() == ContentDisplay.BOTTOM ? node.minHeight(d) + labeled.getGraphicTextGap() + d6 : Math.max(d6, node.minHeight(d));
        }
        double d9 = d2 + d4;
        if (!this.isIgnoreText()) {
            d9 += this.topLabelPadding() - this.bottomLabelPadding();
        }
        return d8 + d9;
    }

    double topLabelPadding() {
        return this.snapSizeY(((Labeled)this.getSkinnable()).getLabelPadding().getTop());
    }

    double bottomLabelPadding() {
        return this.snapSizeY(((Labeled)this.getSkinnable()).getLabelPadding().getBottom());
    }

    double leftLabelPadding() {
        return this.snapSizeX(((Labeled)this.getSkinnable()).getLabelPadding().getLeft());
    }

    double rightLabelPadding() {
        return this.snapSizeX(((Labeled)this.getSkinnable()).getLabelPadding().getRight());
    }

    private void textMetricsChanged() {
        this.invalidText = true;
        ((Labeled)this.getSkinnable()).requestLayout();
    }

    void mnemonicTargetChanged() {
        if (this.containsMnemonic) {
            this.removeMnemonic();
            Skinnable skinnable = this.getSkinnable();
            if (skinnable instanceof Label) {
                this.labeledNode = ((Label)skinnable).getLabelFor();
                this.addMnemonic();
            } else {
                this.labeledNode = null;
            }
        }
    }

    private void sceneChanged() {
        Labeled labeled = (Labeled)this.getSkinnable();
        Scene scene = labeled.getScene();
        if (scene != null && this.containsMnemonic) {
            this.addMnemonic();
        }
    }

    private void invalidateWidths() {
        this.textWidth = Double.NEGATIVE_INFINITY;
    }

    void updateDisplayedText() {
        this.updateDisplayedText(-1.0, -1.0);
    }

    private void updateDisplayedText(double d, double d2) {
        if (this.invalidText) {
            String string;
            int n;
            Labeled labeled = (Labeled)this.getSkinnable();
            String string2 = labeled.getText();
            int n2 = -1;
            if (string2 != null && string2.length() > 0) {
                this.bindings = new TextBinding(string2);
                if (!PlatformUtil.isMac() && ((Labeled)this.getSkinnable()).isMnemonicParsing()) {
                    this.labeledNode = labeled instanceof Label ? ((Label)labeled).getLabelFor() : labeled;
                    if (this.labeledNode == null) {
                        this.labeledNode = labeled;
                    }
                    n2 = this.bindings.getMnemonicIndex();
                }
            }
            if (this.containsMnemonic) {
                if (this.mnemonicScene != null && (n2 == -1 || this.bindings != null && !this.bindings.getMnemonicKeyCombination().equals(this.mnemonicCode))) {
                    this.removeMnemonic();
                    this.containsMnemonic = false;
                }
            } else {
                this.removeMnemonic();
            }
            if (string2 != null && string2.length() > 0 && n2 >= 0 && !this.containsMnemonic) {
                this.containsMnemonic = true;
                this.mnemonicCode = this.bindings.getMnemonicKeyCombination();
                this.addMnemonic();
            }
            if (this.containsMnemonic) {
                string2 = this.bindings.getText();
                if (this.mnemonic_underscore == null) {
                    this.mnemonic_underscore = new Line();
                    this.mnemonic_underscore.setStartX(0.0);
                    this.mnemonic_underscore.setStartY(0.0);
                    this.mnemonic_underscore.setEndY(0.0);
                    this.mnemonic_underscore.getStyleClass().clear();
                    this.mnemonic_underscore.getStyleClass().setAll((String[])new String[]{"mnemonic-underline"});
                }
                if (!this.getChildren().contains(this.mnemonic_underscore)) {
                    this.getChildren().add(this.mnemonic_underscore);
                }
            } else {
                string2 = ((Labeled)this.getSkinnable()).isMnemonicParsing() && PlatformUtil.isMac() && this.bindings != null ? this.bindings.getText() : labeled.getText();
                if (this.mnemonic_underscore != null && this.getChildren().contains(this.mnemonic_underscore)) {
                    Platform.runLater(() -> {
                        this.getChildren().remove(this.mnemonic_underscore);
                        this.mnemonic_underscore = null;
                    });
                }
            }
            int n3 = string2 != null ? string2.length() : 0;
            boolean bl = false;
            if (string2 != null && n3 > 0 && (n = string2.indexOf(10)) > -1 && n < n3 - 1) {
                bl = true;
            }
            boolean bl2 = labeled.getContentDisplay() == ContentDisplay.LEFT || labeled.getContentDisplay() == ContentDisplay.RIGHT;
            double d3 = labeled.getWidth() - this.snappedLeftInset() - this.snappedRightInset();
            if (!this.isIgnoreText()) {
                d3 -= this.leftLabelPadding() + this.rightLabelPadding();
            }
            d3 = Math.max(d3, 0.0);
            if (d == -1.0) {
                d = d3;
            }
            double d4 = Math.min(this.computeMinLabeledPartWidth(-1.0, this.snappedTopInset(), this.snappedRightInset(), this.snappedBottomInset(), this.snappedLeftInset()), d3);
            if (bl2 && !this.isIgnoreGraphic()) {
                double d5 = labeled.getGraphic().getLayoutBounds().getWidth() + labeled.getGraphicTextGap();
                d -= d5;
                d4 -= d5;
            }
            this.wrapWidth = Math.max(d4, d);
            boolean bl3 = labeled.getContentDisplay() == ContentDisplay.TOP || labeled.getContentDisplay() == ContentDisplay.BOTTOM;
            double d6 = labeled.getHeight() - this.snappedTopInset() - this.snappedBottomInset();
            if (!this.isIgnoreText()) {
                d6 -= this.topLabelPadding() + this.bottomLabelPadding();
            }
            d6 = Math.max(d6, 0.0);
            if (d2 == -1.0) {
                d2 = d6;
            }
            double d7 = Math.min(this.computeMinLabeledPartHeight(this.wrapWidth, this.snappedTopInset(), this.snappedRightInset(), this.snappedBottomInset(), this.snappedLeftInset()), d6);
            if (bl3 && labeled.getGraphic() != null) {
                double d8 = labeled.getGraphic().getLayoutBounds().getHeight() + labeled.getGraphicTextGap();
                d2 -= d8;
                d7 -= d8;
            }
            this.wrapHeight = Math.max(d7, d2);
            this.updateWrappingWidth();
            Font font = this.text.getFont();
            OverrunStyle overrunStyle = labeled.getTextOverrun();
            String string3 = labeled.getEllipsisString();
            if (labeled.isWrapText()) {
                string = Utils.computeClippedWrappedText(font, string2, this.wrapWidth, this.wrapHeight, overrunStyle, string3, this.text.getBoundsType());
            } else if (bl) {
                StringBuilder stringBuilder = new StringBuilder();
                String[] stringArray = string2.split("\n");
                for (int i = 0; i < stringArray.length; ++i) {
                    stringBuilder.append(Utils.computeClippedText(font, stringArray[i], this.wrapWidth, overrunStyle, string3));
                    if (i >= stringArray.length - 1) continue;
                    stringBuilder.append('\n');
                }
                string = stringBuilder.toString();
            } else {
                string = Utils.computeClippedText(font, string2, this.wrapWidth, overrunStyle, string3);
            }
            if (string != null && string.endsWith("\n")) {
                string = string.substring(0, string.length() - 1);
            }
            this.text.setText(string);
            this.updateWrappingWidth();
            this.invalidText = false;
        }
    }

    private void addMnemonic() {
        if (this.labeledNode != null) {
            this.mnemonicScene = this.labeledNode.getScene();
            if (this.mnemonicScene != null) {
                this.mnemonicScene.addMnemonic(new Mnemonic(this.labeledNode, this.mnemonicCode));
            }
        }
    }

    private void removeMnemonic() {
        if (this.mnemonicScene != null && this.labeledNode != null) {
            this.mnemonicScene.removeMnemonic(new Mnemonic(this.labeledNode, this.mnemonicCode));
            this.mnemonicScene = null;
        }
    }

    private void updateWrappingWidth() {
        Labeled labeled = (Labeled)this.getSkinnable();
        this.text.setWrappingWidth(0.0);
        if (labeled.isWrapText()) {
            double d = Math.min(this.text.prefWidth(-1.0), this.wrapWidth);
            this.text.setWrappingWidth(d);
        }
    }

    boolean isIgnoreGraphic() {
        return this.graphic == null || !this.graphic.isManaged() || ((Labeled)this.getSkinnable()).getContentDisplay() == ContentDisplay.TEXT_ONLY;
    }

    boolean isIgnoreText() {
        Labeled labeled = (Labeled)this.getSkinnable();
        String string = labeled.getText();
        return string == null || string.equals("") || labeled.getContentDisplay() == ContentDisplay.GRAPHIC_ONLY;
    }
}

