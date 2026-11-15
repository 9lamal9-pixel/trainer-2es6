/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

public class CustomMenuItem
extends MenuItem {
    private ObjectProperty<Node> content;
    private BooleanProperty hideOnClick;
    private static final String DEFAULT_STYLE_CLASS = "custom-menu-item";

    public CustomMenuItem() {
        this(null, true);
    }

    public CustomMenuItem(Node node) {
        this(node, true);
    }

    public CustomMenuItem(Node node, boolean bl) {
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.setContent(node);
        this.setHideOnClick(bl);
    }

    public final void setContent(Node node) {
        this.contentProperty().set(node);
    }

    public final Node getContent() {
        return this.content == null ? null : (Node)this.content.get();
    }

    public final ObjectProperty<Node> contentProperty() {
        if (this.content == null) {
            this.content = new SimpleObjectProperty<Node>(this, "content");
        }
        return this.content;
    }

    public final void setHideOnClick(boolean bl) {
        this.hideOnClickProperty().set(bl);
    }

    public final boolean isHideOnClick() {
        return this.hideOnClick == null ? true : this.hideOnClick.get();
    }

    public final BooleanProperty hideOnClickProperty() {
        if (this.hideOnClick == null) {
            this.hideOnClick = new SimpleBooleanProperty(this, "hideOnClick", true);
        }
        return this.hideOnClick;
    }
}

