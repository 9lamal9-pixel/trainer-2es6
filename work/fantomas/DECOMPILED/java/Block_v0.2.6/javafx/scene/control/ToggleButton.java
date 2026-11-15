/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.css.StyleableProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.skin.ToggleButtonSkin;

public class ToggleButton
extends ButtonBase
implements Toggle {
    private BooleanProperty selected;
    private ObjectProperty<ToggleGroup> toggleGroup;
    private static final String DEFAULT_STYLE_CLASS = "toggle-button";
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

    public ToggleButton() {
        this.initialize();
    }

    public ToggleButton(String string) {
        this.setText(string);
        this.initialize();
    }

    public ToggleButton(String string, Node node) {
        this.setText(string);
        this.setGraphic(node);
        this.initialize();
    }

    private void initialize() {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.TOGGLE_BUTTON);
        ((StyleableProperty)((Object)this.alignmentProperty())).applyStyle(null, Pos.CENTER);
        this.setMnemonicParsing(true);
    }

    @Override
    public final void setSelected(boolean bl) {
        this.selectedProperty().set(bl);
    }

    @Override
    public final boolean isSelected() {
        return this.selected == null ? false : this.selected.get();
    }

    @Override
    public final BooleanProperty selectedProperty() {
        if (this.selected == null) {
            this.selected = new BooleanPropertyBase(){

                @Override
                protected void invalidated() {
                    boolean bl = this.get();
                    ToggleGroup toggleGroup = ToggleButton.this.getToggleGroup();
                    ToggleButton.this.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, bl);
                    ToggleButton.this.notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
                    if (toggleGroup != null) {
                        if (bl) {
                            toggleGroup.selectToggle(ToggleButton.this);
                        } else if (toggleGroup.getSelectedToggle() == ToggleButton.this) {
                            toggleGroup.clearSelectedToggle();
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return ToggleButton.this;
                }

                @Override
                public String getName() {
                    return "selected";
                }
            };
        }
        return this.selected;
    }

    @Override
    public final void setToggleGroup(ToggleGroup toggleGroup) {
        this.toggleGroupProperty().set(toggleGroup);
    }

    @Override
    public final ToggleGroup getToggleGroup() {
        return this.toggleGroup == null ? null : (ToggleGroup)this.toggleGroup.get();
    }

    @Override
    public final ObjectProperty<ToggleGroup> toggleGroupProperty() {
        if (this.toggleGroup == null) {
            this.toggleGroup = new ObjectPropertyBase<ToggleGroup>(){
                private ToggleGroup old;
                private ChangeListener<Toggle> listener = (observableValue, toggle, toggle2) -> ParentHelper.getTraversalEngine(ToggleButton.this).setOverriddenFocusTraversability(toggle2 != null ? Boolean.valueOf(ToggleButton.this.isSelected()) : null);

                @Override
                protected void invalidated() {
                    ToggleGroup toggleGroup = (ToggleGroup)this.get();
                    if (toggleGroup != null && !toggleGroup.getToggles().contains(ToggleButton.this)) {
                        if (this.old != null) {
                            this.old.getToggles().remove(ToggleButton.this);
                        }
                        toggleGroup.getToggles().add(ToggleButton.this);
                        ParentTraversalEngine parentTraversalEngine = new ParentTraversalEngine(ToggleButton.this);
                        ParentHelper.setTraversalEngine(ToggleButton.this, parentTraversalEngine);
                        parentTraversalEngine.setOverriddenFocusTraversability(toggleGroup.getSelectedToggle() != null ? Boolean.valueOf(ToggleButton.this.isSelected()) : null);
                        toggleGroup.selectedToggleProperty().addListener(this.listener);
                    } else if (toggleGroup == null) {
                        this.old.selectedToggleProperty().removeListener(this.listener);
                        this.old.getToggles().remove(ToggleButton.this);
                        ParentHelper.setTraversalEngine(ToggleButton.this, null);
                    }
                    this.old = toggleGroup;
                }

                @Override
                public Object getBean() {
                    return ToggleButton.this;
                }

                @Override
                public String getName() {
                    return "toggleGroup";
                }
            };
        }
        return this.toggleGroup;
    }

    @Override
    public void fire() {
        if (!this.isDisabled()) {
            this.setSelected(!this.isSelected());
            this.fireEvent(new ActionEvent());
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ToggleButtonSkin(this);
    }

    @Override
    protected Pos getInitialAlignment() {
        return Pos.CENTER;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case SELECTED: {
                return this.isSelected();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

