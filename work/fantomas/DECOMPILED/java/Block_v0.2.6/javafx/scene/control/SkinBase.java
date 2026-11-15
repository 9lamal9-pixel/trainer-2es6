/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.scene.control.LambdaMultiplePropertyChangeListenerHandler;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public abstract class SkinBase<C extends Control>
implements Skin<C> {
    private C control;
    private ObservableList<Node> children;
    private LambdaMultiplePropertyChangeListenerHandler lambdaChangeListenerHandler;
    private static final EventHandler<MouseEvent> mouseEventConsumer = mouseEvent -> mouseEvent.consume();

    protected SkinBase(C c) {
        if (c == null) {
            throw new IllegalArgumentException("Cannot pass null for control");
        }
        this.control = c;
        this.children = ((Control)c).getControlChildren();
        this.consumeMouseEvents(true);
    }

    @Override
    public final C getSkinnable() {
        return this.control;
    }

    @Override
    public final Node getNode() {
        return this.control;
    }

    @Override
    public void dispose() {
        if (this.lambdaChangeListenerHandler != null) {
            this.lambdaChangeListenerHandler.dispose();
        }
        this.control = null;
    }

    public final ObservableList<Node> getChildren() {
        return this.children;
    }

    protected void layoutChildren(double d, double d2, double d3, double d4) {
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            Node node = (Node)this.children.get(i);
            if (!node.isManaged()) continue;
            this.layoutInArea(node, d, d2, d3, d4, -1.0, HPos.CENTER, VPos.CENTER);
        }
    }

    protected final void consumeMouseEvents(boolean bl) {
        if (bl) {
            ((Node)this.control).addEventHandler(MouseEvent.ANY, mouseEventConsumer);
        } else {
            ((Node)this.control).removeEventHandler(MouseEvent.ANY, mouseEventConsumer);
        }
    }

    protected final void registerChangeListener(ObservableValue<?> observableValue, Consumer<ObservableValue<?>> consumer) {
        if (this.lambdaChangeListenerHandler == null) {
            this.lambdaChangeListenerHandler = new LambdaMultiplePropertyChangeListenerHandler();
        }
        this.lambdaChangeListenerHandler.registerChangeListener(observableValue, consumer);
    }

    protected final Consumer<ObservableValue<?>> unregisterChangeListeners(ObservableValue<?> observableValue) {
        if (this.lambdaChangeListenerHandler == null) {
            return null;
        }
        return this.lambdaChangeListenerHandler.unregisterChangeListeners(observableValue);
    }

    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        double d7 = 0.0;
        boolean bl = true;
        for (int i = 0; i < this.children.size(); ++i) {
            Node node = (Node)this.children.get(i);
            if (!node.isManaged()) continue;
            double d8 = node.getLayoutBounds().getMinX() + node.getLayoutX();
            if (!bl) {
                d6 = Math.min(d6, d8);
                d7 = Math.max(d7, d8 + node.minWidth(-1.0));
                continue;
            }
            d6 = d8;
            d7 = d8 + node.minWidth(-1.0);
            bl = false;
        }
        double d9 = d7 - d6;
        return d5 + d9 + d3;
    }

    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        double d7 = 0.0;
        boolean bl = true;
        for (int i = 0; i < this.children.size(); ++i) {
            Node node = (Node)this.children.get(i);
            if (!node.isManaged()) continue;
            double d8 = node.getLayoutBounds().getMinY() + node.getLayoutY();
            if (!bl) {
                d6 = Math.min(d6, d8);
                d7 = Math.max(d7, d8 + node.minHeight(-1.0));
                continue;
            }
            d6 = d8;
            d7 = d8 + node.minHeight(-1.0);
            bl = false;
        }
        double d9 = d7 - d6;
        return d2 + d9 + d4;
    }

    protected double computeMaxWidth(double d, double d2, double d3, double d4, double d5) {
        return Double.MAX_VALUE;
    }

    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        return Double.MAX_VALUE;
    }

    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        double d7 = 0.0;
        boolean bl = true;
        for (int i = 0; i < this.children.size(); ++i) {
            Node node = (Node)this.children.get(i);
            if (!node.isManaged()) continue;
            double d8 = node.getLayoutBounds().getMinX() + node.getLayoutX();
            if (!bl) {
                d6 = Math.min(d6, d8);
                d7 = Math.max(d7, d8 + node.prefWidth(-1.0));
                continue;
            }
            d6 = d8;
            d7 = d8 + node.prefWidth(-1.0);
            bl = false;
        }
        return d7 - d6;
    }

    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        double d7 = 0.0;
        boolean bl = true;
        for (int i = 0; i < this.children.size(); ++i) {
            Node node = (Node)this.children.get(i);
            if (!node.isManaged()) continue;
            double d8 = node.getLayoutBounds().getMinY() + node.getLayoutY();
            if (!bl) {
                d6 = Math.min(d6, d8);
                d7 = Math.max(d7, d8 + node.prefHeight(-1.0));
                continue;
            }
            d6 = d8;
            d7 = d8 + node.prefHeight(-1.0);
            bl = false;
        }
        return d7 - d6;
    }

    protected double computeBaselineOffset(double d, double d2, double d3, double d4) {
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            double d5;
            Node node = (Node)this.children.get(i);
            if (!node.isManaged() || (d5 = node.getBaselineOffset()) == Double.NEGATIVE_INFINITY) continue;
            return node.getLayoutBounds().getMinY() + node.getLayoutY() + d5;
        }
        return Double.NEGATIVE_INFINITY;
    }

    protected double snappedTopInset() {
        return ((Region)this.control).snappedTopInset();
    }

    protected double snappedBottomInset() {
        return ((Region)this.control).snappedBottomInset();
    }

    protected double snappedLeftInset() {
        return ((Region)this.control).snappedLeftInset();
    }

    protected double snappedRightInset() {
        return ((Region)this.control).snappedRightInset();
    }

    @Deprecated(since="9")
    protected double snapSpace(double d) {
        return ((Region)this.control).snapSpaceX(d);
    }

    protected double snapSpaceX(double d) {
        return ((Region)this.control).snapSpaceX(d);
    }

    protected double snapSpaceY(double d) {
        return ((Region)this.control).snapSpaceY(d);
    }

    @Deprecated(since="9")
    protected double snapSize(double d) {
        return ((Region)this.control).snapSizeX(d);
    }

    protected double snapSizeX(double d) {
        return ((Region)this.control).snapSizeX(d);
    }

    protected double snapSizeY(double d) {
        return ((Region)this.control).snapSizeY(d);
    }

    @Deprecated(since="9")
    protected double snapPosition(double d) {
        return ((Region)this.control).snapPositionX(d);
    }

    protected double snapPositionX(double d) {
        return ((Region)this.control).snapPositionX(d);
    }

    protected double snapPositionY(double d) {
        return ((Region)this.control).snapPositionY(d);
    }

    protected void positionInArea(Node node, double d, double d2, double d3, double d4, double d5, HPos hPos, VPos vPos) {
        this.positionInArea(node, d, d2, d3, d4, d5, Insets.EMPTY, hPos, vPos);
    }

    protected void positionInArea(Node node, double d, double d2, double d3, double d4, double d5, Insets insets, HPos hPos, VPos vPos) {
        Region.positionInArea(node, d, d2, d3, d4, d5, insets, hPos, vPos, ((Region)this.control).isSnapToPixel());
    }

    protected void layoutInArea(Node node, double d, double d2, double d3, double d4, double d5, HPos hPos, VPos vPos) {
        this.layoutInArea(node, d, d2, d3, d4, d5, Insets.EMPTY, true, true, hPos, vPos);
    }

    protected void layoutInArea(Node node, double d, double d2, double d3, double d4, double d5, Insets insets, HPos hPos, VPos vPos) {
        this.layoutInArea(node, d, d2, d3, d4, d5, insets, true, true, hPos, vPos);
    }

    protected void layoutInArea(Node node, double d, double d2, double d3, double d4, double d5, Insets insets, boolean bl, boolean bl2, HPos hPos, VPos vPos) {
        Region.layoutInArea(node, d, d2, d3, d4, d5, insets, bl, bl2, hPos, vPos, ((Region)this.control).isSnapToPixel());
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return SkinBase.getClassCssMetaData();
    }

    public final void pseudoClassStateChanged(PseudoClass pseudoClass, boolean bl) {
        Skinnable skinnable = this.getSkinnable();
        if (skinnable != null) {
            ((Node)((Object)skinnable)).pseudoClassStateChanged(pseudoClass, bl);
        }
    }

    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        return null;
    }

    protected void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
    }

    private static class StyleableProperties {
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES = Collections.unmodifiableList(Control.getClassCssMetaData());

        private StyleableProperties() {
        }
    }
}

