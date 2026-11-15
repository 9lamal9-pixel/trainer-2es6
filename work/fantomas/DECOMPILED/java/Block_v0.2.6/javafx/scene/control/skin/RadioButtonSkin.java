/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ToggleButtonBehavior;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.skin.LabeledSkinBase;
import javafx.scene.layout.StackPane;

public class RadioButtonSkin
extends LabeledSkinBase<RadioButton> {
    private StackPane radio;
    private final BehaviorBase<RadioButton> behavior;

    public RadioButtonSkin(RadioButton radioButton) {
        super(radioButton);
        this.behavior = new ToggleButtonBehavior<RadioButton>(radioButton);
        this.radio = RadioButtonSkin.createRadio();
        this.updateChildren();
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
        super.updateChildren();
        if (this.radio != null) {
            this.getChildren().add(this.radio);
        }
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        return super.computeMinWidth(d, d2, d3, d4, d5) + this.snapSizeX(this.radio.minWidth(-1.0));
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        return Math.max(this.snapSizeY(super.computeMinHeight(d - this.radio.minWidth(-1.0), d2, d3, d4, d5)), d2 + this.radio.minHeight(-1.0) + d4);
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        return super.computePrefWidth(d, d2, d3, d4, d5) + this.snapSizeX(this.radio.prefWidth(-1.0));
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        return Math.max(this.snapSizeY(super.computePrefHeight(d - this.radio.prefWidth(-1.0), d2, d3, d4, d5)), d2 + this.radio.prefHeight(-1.0) + d4);
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        RadioButton radioButton = (RadioButton)this.getSkinnable();
        double d5 = this.radio.prefWidth(-1.0);
        double d6 = this.radio.prefHeight(-1.0);
        double d7 = Math.max(radioButton.prefWidth(-1.0), radioButton.minWidth(-1.0));
        double d8 = Math.min(d7 - d5, d3 - this.snapSizeX(d5));
        double d9 = Math.min(radioButton.prefHeight(d8), d4);
        double d10 = Math.max(d6, d9);
        double d11 = Utils.computeXOffset(d3, d8 + d5, radioButton.getAlignment().getHpos()) + d;
        double d12 = Utils.computeYOffset(d4, d10, radioButton.getAlignment().getVpos()) + d2;
        this.layoutLabelInArea(d11 + d5, d12, d8, d10, radioButton.getAlignment());
        this.radio.resize(this.snapSizeX(d5), this.snapSizeY(d6));
        this.positionInArea(this.radio, d11, d12, d5, d10, 0.0, radioButton.getAlignment().getHpos(), radioButton.getAlignment().getVpos());
    }

    private static StackPane createRadio() {
        StackPane stackPane = new StackPane();
        stackPane.getStyleClass().setAll((String[])new String[]{"radio"});
        stackPane.setSnapToPixel(false);
        StackPane stackPane2 = new StackPane();
        stackPane2.getStyleClass().setAll((String[])new String[]{"dot"});
        stackPane.getChildren().clear();
        stackPane.getChildren().addAll((Node[])new Node[]{stackPane2});
        return stackPane;
    }
}

