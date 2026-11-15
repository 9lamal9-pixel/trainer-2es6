/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.control.ContextMenuContent;
import com.sun.javafx.scene.control.ControlAcceleratorSupport;
import com.sun.javafx.scene.control.LabeledImpl;
import com.sun.javafx.scene.control.behavior.MenuButtonBehaviorBase;
import com.sun.javafx.scene.control.skin.Utils;
import java.util.Collection;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.ContextMenuSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class MenuButtonSkinBase<C extends MenuButton>
extends SkinBase<C> {
    final LabeledImpl label;
    final StackPane arrow;
    final StackPane arrowButton;
    ContextMenu popup;
    boolean behaveLikeButton = false;
    private ListChangeListener<MenuItem> itemsChangedListener;
    boolean requestFocusOnFirstMenuItem = false;

    public MenuButtonSkinBase(C c) {
        super(c);
        if (((Node)c).getOnMousePressed() == null) {
            ((Node)c).addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
                MenuButtonBehaviorBase<C> menuButtonBehaviorBase = this.getBehavior();
                if (menuButtonBehaviorBase != null) {
                    menuButtonBehaviorBase.mousePressed((MouseEvent)mouseEvent, this.behaveLikeButton);
                }
            });
        }
        if (((Node)c).getOnMouseReleased() == null) {
            ((Node)c).addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
                MenuButtonBehaviorBase<C> menuButtonBehaviorBase = this.getBehavior();
                if (menuButtonBehaviorBase != null) {
                    menuButtonBehaviorBase.mouseReleased((MouseEvent)mouseEvent, this.behaveLikeButton);
                }
            });
        }
        this.label = new MenuLabeledImpl((MenuButton)this.getSkinnable());
        this.label.setMnemonicParsing(((Labeled)c).isMnemonicParsing());
        this.label.setLabelFor((Node)c);
        this.arrow = new StackPane();
        this.arrow.getStyleClass().setAll((String[])new String[]{"arrow"});
        this.arrow.setMaxWidth(Double.NEGATIVE_INFINITY);
        this.arrow.setMaxHeight(Double.NEGATIVE_INFINITY);
        this.arrowButton = new StackPane();
        this.arrowButton.getStyleClass().setAll((String[])new String[]{"arrow-button"});
        this.arrowButton.getChildren().add(this.arrow);
        this.popup = new ContextMenu();
        this.popup.getItems().clear();
        this.popup.getItems().addAll((Collection<MenuItem>)((MenuButton)this.getSkinnable()).getItems());
        this.getChildren().clear();
        this.getChildren().addAll((Node[])new Node[]{this.label, this.arrowButton});
        ((MenuButton)this.getSkinnable()).requestLayout();
        this.itemsChangedListener = change -> {
            while (change.next()) {
                this.popup.getItems().removeAll(change.getRemoved());
                this.popup.getItems().addAll(change.getFrom(), change.getAddedSubList());
            }
        };
        ((MenuButton)c).getItems().addListener(this.itemsChangedListener);
        if (((MenuButton)this.getSkinnable()).getScene() != null) {
            ControlAcceleratorSupport.addAcceleratorsIntoScene(((MenuButton)this.getSkinnable()).getItems(), (Node)((Object)this.getSkinnable()));
        }
        ((Node)c).sceneProperty().addListener((observableValue, scene, scene2) -> {
            if (this.getSkinnable() != null && ((MenuButton)this.getSkinnable()).getScene() != null) {
                ControlAcceleratorSupport.addAcceleratorsIntoScene(((MenuButton)this.getSkinnable()).getItems(), (Node)((Object)this.getSkinnable()));
            }
        });
        this.registerChangeListener(((MenuButton)c).showingProperty(), observableValue -> {
            if (((MenuButton)this.getSkinnable()).isShowing()) {
                this.show();
            } else {
                this.hide();
            }
        });
        this.registerChangeListener(((Node)c).focusedProperty(), observableValue -> {
            if (!((MenuButton)this.getSkinnable()).isFocused() && ((MenuButton)this.getSkinnable()).isShowing()) {
                this.hide();
            }
            if (!((MenuButton)this.getSkinnable()).isFocused() && this.popup.isShowing()) {
                this.hide();
            }
        });
        this.registerChangeListener(((Labeled)c).mnemonicParsingProperty(), observableValue -> {
            this.label.setMnemonicParsing(((MenuButton)this.getSkinnable()).isMnemonicParsing());
            ((MenuButton)this.getSkinnable()).requestLayout();
        });
        this.registerChangeListener(this.popup.showingProperty(), observableValue -> {
            if (!this.popup.isShowing() && ((MenuButton)this.getSkinnable()).isShowing()) {
                ((MenuButton)this.getSkinnable()).hide();
            }
            if (this.popup.isShowing()) {
                Utils.addMnemonics(this.popup, ((MenuButton)this.getSkinnable()).getScene(), NodeHelper.isShowMnemonics((Node)((Object)this.getSkinnable())));
            } else {
                Platform.runLater(() -> Utils.removeMnemonics(this.popup, ((MenuButton)this.getSkinnable()).getScene()));
            }
        });
    }

    @Override
    public void dispose() {
        ((MenuButton)this.getSkinnable()).getItems().removeListener(this.itemsChangedListener);
        super.dispose();
        if (this.popup != null) {
            if (this.popup.getSkin() != null && this.popup.getSkin().getNode() != null) {
                ContextMenuContent contextMenuContent = (ContextMenuContent)this.popup.getSkin().getNode();
                contextMenuContent.dispose();
            }
            this.popup.setSkin(null);
            this.popup = null;
        }
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        return d5 + this.label.minWidth(d) + this.snapSizeX(this.arrowButton.minWidth(d)) + d3;
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        return d2 + Math.max(this.label.minHeight(d), this.snapSizeY(this.arrowButton.minHeight(-1.0))) + d4;
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        return d5 + this.label.prefWidth(d) + this.snapSizeX(this.arrowButton.prefWidth(d)) + d3;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        return d2 + Math.max(this.label.prefHeight(d), this.snapSizeY(this.arrowButton.prefHeight(-1.0))) + d4;
    }

    @Override
    protected double computeMaxWidth(double d, double d2, double d3, double d4, double d5) {
        return ((MenuButton)this.getSkinnable()).prefWidth(d);
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        return ((MenuButton)this.getSkinnable()).prefHeight(d);
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        double d5 = this.snapSizeX(this.arrowButton.prefWidth(-1.0));
        this.label.resizeRelocate(d, d2, d3 - d5, d4);
        this.arrowButton.resizeRelocate(d + (d3 - d5), d2, d5, d4);
    }

    MenuButtonBehaviorBase<C> getBehavior() {
        return null;
    }

    private void show() {
        if (!this.popup.isShowing()) {
            this.popup.show((Node)((Object)this.getSkinnable()), ((MenuButton)this.getSkinnable()).getPopupSide(), 0.0, 0.0);
        }
    }

    private void hide() {
        if (this.popup.isShowing()) {
            this.popup.hide();
        }
    }

    void requestFocusOnFirstMenuItem() {
        this.requestFocusOnFirstMenuItem = true;
    }

    void putFocusOnFirstMenuItem() {
        Node node;
        Skin<?> skin = this.popup.getSkin();
        if (skin instanceof ContextMenuSkin && (node = skin.getNode()) instanceof ContextMenuContent) {
            ((ContextMenuContent)node).requestFocusOnIndex(0);
        }
    }

    private static class MenuLabeledImpl
    extends LabeledImpl {
        MenuButton button;

        public MenuLabeledImpl(MenuButton menuButton) {
            super(menuButton);
            this.button = menuButton;
            this.addEventHandler(ActionEvent.ACTION, actionEvent -> {
                this.button.fireEvent(new ActionEvent());
                actionEvent.consume();
            });
        }
    }
}

