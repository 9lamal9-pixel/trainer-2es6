/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.css.StyleableProperty;
import javafx.scene.control.Labeled;

public class Cell<T>
extends Labeled {
    private ObjectProperty<T> item = new SimpleObjectProperty(this, "item");
    private ReadOnlyBooleanWrapper empty = new ReadOnlyBooleanWrapper(true){

        @Override
        protected void invalidated() {
            boolean bl = this.get();
            Cell.this.pseudoClassStateChanged(PSEUDO_CLASS_EMPTY, bl);
            Cell.this.pseudoClassStateChanged(PSEUDO_CLASS_FILLED, !bl);
        }

        @Override
        public Object getBean() {
            return Cell.this;
        }

        @Override
        public String getName() {
            return "empty";
        }
    };
    private ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper(){

        @Override
        protected void invalidated() {
            Cell.this.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, this.get());
        }

        @Override
        public Object getBean() {
            return Cell.this;
        }

        @Override
        public String getName() {
            return "selected";
        }
    };
    private ReadOnlyBooleanWrapper editing;
    private BooleanProperty editable;
    private boolean itemDirty = false;
    private static final String DEFAULT_STYLE_CLASS = "cell";
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass PSEUDO_CLASS_FOCUSED = PseudoClass.getPseudoClass("focused");
    private static final PseudoClass PSEUDO_CLASS_EMPTY = PseudoClass.getPseudoClass("empty");
    private static final PseudoClass PSEUDO_CLASS_FILLED = PseudoClass.getPseudoClass("filled");

    public Cell() {
        this.setText(null);
        ((StyleableProperty)((Object)this.focusTraversableProperty())).applyStyle(null, Boolean.FALSE);
        this.getStyleClass().addAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        super.focusedProperty().addListener(new InvalidationListener(){

            @Override
            public void invalidated(Observable observable) {
                Cell.this.pseudoClassStateChanged(PSEUDO_CLASS_FOCUSED, Cell.this.isFocused());
                if (!Cell.this.isFocused() && Cell.this.isEditing()) {
                    Cell.this.cancelEdit();
                }
            }
        });
        this.pseudoClassStateChanged(PSEUDO_CLASS_EMPTY, true);
    }

    public final ObjectProperty<T> itemProperty() {
        return this.item;
    }

    public final void setItem(T t) {
        this.item.set(t);
    }

    public final T getItem() {
        return this.item.get();
    }

    public final ReadOnlyBooleanProperty emptyProperty() {
        return this.empty.getReadOnlyProperty();
    }

    private void setEmpty(boolean bl) {
        this.empty.set(bl);
    }

    public final boolean isEmpty() {
        return this.empty.get();
    }

    public final ReadOnlyBooleanProperty selectedProperty() {
        return this.selected.getReadOnlyProperty();
    }

    void setSelected(boolean bl) {
        this.selected.set(bl);
    }

    public final boolean isSelected() {
        return this.selected.get();
    }

    private void setEditing(boolean bl) {
        this.editingPropertyImpl().set(bl);
    }

    public final boolean isEditing() {
        return this.editing == null ? false : this.editing.get();
    }

    public final ReadOnlyBooleanProperty editingProperty() {
        return this.editingPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyBooleanWrapper editingPropertyImpl() {
        if (this.editing == null) {
            this.editing = new ReadOnlyBooleanWrapper(this, "editing");
        }
        return this.editing;
    }

    public final void setEditable(boolean bl) {
        this.editableProperty().set(bl);
    }

    public final boolean isEditable() {
        return this.editable == null ? true : this.editable.get();
    }

    public final BooleanProperty editableProperty() {
        if (this.editable == null) {
            this.editable = new SimpleBooleanProperty(this, "editable", true);
        }
        return this.editable;
    }

    public void startEdit() {
        if (this.isEditable() && !this.isEditing() && !this.isEmpty()) {
            this.setEditing(true);
        }
    }

    public void cancelEdit() {
        if (this.isEditing()) {
            this.setEditing(false);
        }
    }

    public void commitEdit(T t) {
        if (this.isEditing()) {
            this.setEditing(false);
        }
    }

    @Override
    protected void layoutChildren() {
        if (this.itemDirty) {
            this.updateItem(this.getItem(), this.isEmpty());
            this.itemDirty = false;
        }
        super.layoutChildren();
    }

    protected void updateItem(T t, boolean bl) {
        this.setItem(t);
        this.setEmpty(bl);
        if (bl && this.isSelected()) {
            this.updateSelected(false);
        }
    }

    public void updateSelected(boolean bl) {
        if (bl && this.isEmpty()) {
            return;
        }
        boolean bl2 = this.isSelected();
        this.setSelected(bl);
        if (bl2 != bl) {
            this.markCellDirty();
        }
    }

    protected boolean isItemChanged(T t, T t2) {
        return t != null ? !t.equals(t2) : t2 != null;
    }

    private final void markCellDirty() {
        this.itemDirty = true;
        this.requestLayout();
    }

    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }
}

