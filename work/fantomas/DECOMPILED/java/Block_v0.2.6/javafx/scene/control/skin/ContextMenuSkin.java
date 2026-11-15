/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.ContextMenuContent;
import com.sun.javafx.scene.control.EmbeddedTextContextMenuContent;
import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusPopupBehavior;
import com.sun.javafx.scene.control.skin.Utils;
import java.util.Collection;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

public class ContextMenuSkin
implements Skin<ContextMenu> {
    private ContextMenu popupMenu;
    private final Region root;
    private TwoLevelFocusPopupBehavior tlFocus;
    private double prefHeight;
    private double shiftY;
    private double prefWidth;
    private double shiftX;
    private final EventHandler<KeyEvent> keyListener = new EventHandler<KeyEvent>(){

        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.getEventType() != KeyEvent.KEY_PRESSED) {
                return;
            }
            if (!ContextMenuSkin.this.root.isFocused()) {
                return;
            }
            KeyCode keyCode = keyEvent.getCode();
            switch (keyCode) {
                case ENTER: 
                case SPACE: {
                    ContextMenuSkin.this.popupMenu.hide();
                    return;
                }
            }
        }
    };

    public ContextMenuSkin(ContextMenu contextMenu) {
        this.popupMenu = contextMenu;
        this.popupMenu.addEventHandler(Menu.ON_SHOWING, new EventHandler<Event>(){

            @Override
            public void handle(Event event) {
                ContextMenuSkin.this.prefHeight = ContextMenuSkin.this.root.prefHeight(-1.0);
                ContextMenuSkin.this.prefWidth = ContextMenuSkin.this.root.prefWidth(-1.0);
            }
        });
        this.popupMenu.addEventHandler(Menu.ON_SHOWN, new EventHandler<Event>(){

            @Override
            public void handle(Event event) {
                Node node = ContextMenuSkin.this.popupMenu.getSkin().getNode();
                if (node != null && node instanceof ContextMenuContent) {
                    VBox vBox = ((ContextMenuContent)node).getItemsContainer();
                    vBox.notifyAccessibleAttributeChanged(AccessibleAttribute.VISIBLE);
                }
                ContextMenuSkin.this.root.addEventHandler(KeyEvent.KEY_PRESSED, ContextMenuSkin.this.keyListener);
                ContextMenuSkin.this.performPopupShifts();
            }
        });
        this.popupMenu.addEventHandler(Menu.ON_HIDDEN, new EventHandler<Event>(){

            @Override
            public void handle(Event event) {
                Node node = ContextMenuSkin.this.popupMenu.getSkin().getNode();
                if (node != null) {
                    node.requestFocus();
                }
                ContextMenuSkin.this.root.removeEventHandler(KeyEvent.KEY_PRESSED, ContextMenuSkin.this.keyListener);
            }
        });
        this.popupMenu.addEventFilter(WindowEvent.WINDOW_HIDING, new EventHandler<Event>(){

            @Override
            public void handle(Event event) {
                Node node = ContextMenuSkin.this.popupMenu.getSkin().getNode();
                if (node instanceof ContextMenuContent) {
                    VBox vBox = ((ContextMenuContent)node).getItemsContainer();
                    vBox.notifyAccessibleAttributeChanged(AccessibleAttribute.VISIBLE);
                }
            }
        });
        this.root = Properties.IS_TOUCH_SUPPORTED && this.popupMenu.getStyleClass().contains("text-input-context-menu") ? new EmbeddedTextContextMenuContent(this.popupMenu) : new ContextMenuContent(this.popupMenu);
        this.root.idProperty().bind(this.popupMenu.idProperty());
        this.root.styleProperty().bind(this.popupMenu.styleProperty());
        this.root.getStyleClass().addAll((Collection<String>)this.popupMenu.getStyleClass());
        if (Utils.isTwoLevelFocus()) {
            this.tlFocus = new TwoLevelFocusPopupBehavior(this.popupMenu);
        }
    }

    @Override
    public ContextMenu getSkinnable() {
        return this.popupMenu;
    }

    @Override
    public Node getNode() {
        return this.root;
    }

    @Override
    public void dispose() {
        this.root.idProperty().unbind();
        this.root.styleProperty().unbind();
        if (this.tlFocus != null) {
            this.tlFocus.dispose();
        }
    }

    private void performPopupShifts() {
        ContextMenu contextMenu = this.getSkinnable();
        Node node = contextMenu.getOwnerNode();
        if (node == null) {
            return;
        }
        Bounds bounds = node.localToScreen(node.getLayoutBounds());
        if (bounds == null) {
            return;
        }
        double d = this.root.prefHeight(-1.0);
        this.shiftY = this.prefHeight - d;
        if (this.shiftY > 0.0 && contextMenu.getY() + d < bounds.getMinY()) {
            contextMenu.setY(contextMenu.getY() + this.shiftY);
        }
        double d2 = this.root.prefWidth(-1.0);
        this.shiftX = this.prefWidth - d2;
        if (this.shiftX > 0.0 && contextMenu.getX() + d2 < bounds.getMinX()) {
            contextMenu.setX(contextMenu.getX() + this.shiftX);
        }
    }
}

