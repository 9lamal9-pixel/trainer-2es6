/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.css.StyleableProperty;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.HyperlinkSkin;

public class Hyperlink
extends ButtonBase {
    private BooleanProperty visited;
    private static final String DEFAULT_STYLE_CLASS = "hyperlink";
    private static final PseudoClass PSEUDO_CLASS_VISITED = PseudoClass.getPseudoClass("visited");

    public Hyperlink() {
        this.initialize();
    }

    public Hyperlink(String string) {
        super(string);
        this.initialize();
    }

    public Hyperlink(String string, Node node) {
        super(string, node);
        this.initialize();
    }

    private void initialize() {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.HYPERLINK);
        ((StyleableProperty)((Object)this.cursorProperty())).applyStyle(null, Cursor.HAND);
    }

    public final BooleanProperty visitedProperty() {
        if (this.visited == null) {
            this.visited = new BooleanPropertyBase(){

                @Override
                protected void invalidated() {
                    Hyperlink.this.pseudoClassStateChanged(PSEUDO_CLASS_VISITED, this.get());
                }

                @Override
                public Object getBean() {
                    return Hyperlink.this;
                }

                @Override
                public String getName() {
                    return "visited";
                }
            };
        }
        return this.visited;
    }

    public final void setVisited(boolean bl) {
        this.visitedProperty().set(bl);
    }

    public final boolean isVisited() {
        return this.visited == null ? false : this.visited.get();
    }

    @Override
    public void fire() {
        if (!this.isDisabled()) {
            if (this.visited == null || !this.visited.isBound()) {
                this.setVisited(true);
            }
            this.fireEvent(new ActionEvent());
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new HyperlinkSkin(this);
    }

    @Override
    protected Cursor getInitialCursor() {
        return Cursor.HAND;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case VISITED: {
                return this.isVisited();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

