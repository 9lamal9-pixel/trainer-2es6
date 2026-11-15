/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class CheckBoxTreeItem<T>
extends TreeItem<T> {
    private static final EventType<? extends Event> CHECK_BOX_SELECTION_CHANGED_EVENT = new EventType<Event>(TreeModificationEvent.ANY, "checkBoxSelectionChangedEvent");
    private final ChangeListener<Boolean> stateChangeListener = (observableValue, bl, bl2) -> this.updateState();
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected", false){

        @Override
        protected void invalidated() {
            super.invalidated();
            CheckBoxTreeItem.this.fireEvent(CheckBoxTreeItem.this, true);
        }
    };
    private final BooleanProperty indeterminate = new SimpleBooleanProperty(this, "indeterminate", false){

        @Override
        protected void invalidated() {
            super.invalidated();
            CheckBoxTreeItem.this.fireEvent(CheckBoxTreeItem.this, false);
        }
    };
    private final BooleanProperty independent = new SimpleBooleanProperty(this, "independent", false);
    private static boolean updateLock = false;

    public static <T> EventType<TreeModificationEvent<T>> checkBoxSelectionChangedEvent() {
        return CHECK_BOX_SELECTION_CHANGED_EVENT;
    }

    public CheckBoxTreeItem() {
        this(null);
    }

    public CheckBoxTreeItem(T t) {
        this(t, null, false);
    }

    public CheckBoxTreeItem(T t, Node node) {
        this(t, node, false);
    }

    public CheckBoxTreeItem(T t, Node node, boolean bl) {
        this(t, node, bl, false);
    }

    public CheckBoxTreeItem(T t, Node node, boolean bl3, boolean bl4) {
        super(t, node);
        this.setSelected(bl3);
        this.setIndependent(bl4);
        this.selectedProperty().addListener(this.stateChangeListener);
        this.indeterminateProperty().addListener(this.stateChangeListener);
    }

    public final void setSelected(boolean bl) {
        this.selectedProperty().setValue(bl);
    }

    public final boolean isSelected() {
        return this.selected.getValue();
    }

    public final BooleanProperty selectedProperty() {
        return this.selected;
    }

    public final void setIndeterminate(boolean bl) {
        this.indeterminateProperty().setValue(bl);
    }

    public final boolean isIndeterminate() {
        return this.indeterminate.getValue();
    }

    public final BooleanProperty indeterminateProperty() {
        return this.indeterminate;
    }

    public final BooleanProperty independentProperty() {
        return this.independent;
    }

    public final void setIndependent(boolean bl) {
        this.independentProperty().setValue(bl);
    }

    public final boolean isIndependent() {
        return this.independent.getValue();
    }

    private void updateState() {
        if (this.isIndependent()) {
            return;
        }
        boolean bl = !updateLock;
        updateLock = true;
        this.updateUpwards();
        if (bl) {
            updateLock = false;
        }
        if (updateLock) {
            return;
        }
        this.updateDownwards();
    }

    private void updateUpwards() {
        if (!(this.getParent() instanceof CheckBoxTreeItem)) {
            return;
        }
        CheckBoxTreeItem checkBoxTreeItem = (CheckBoxTreeItem)this.getParent();
        int n = 0;
        int n2 = 0;
        for (TreeItem treeItem : checkBoxTreeItem.getChildren()) {
            if (!(treeItem instanceof CheckBoxTreeItem)) continue;
            CheckBoxTreeItem checkBoxTreeItem2 = (CheckBoxTreeItem)treeItem;
            n += checkBoxTreeItem2.isSelected() && !checkBoxTreeItem2.isIndeterminate() ? 1 : 0;
            n2 += checkBoxTreeItem2.isIndeterminate() ? 1 : 0;
        }
        if (n == checkBoxTreeItem.getChildren().size()) {
            checkBoxTreeItem.setSelected(true);
            checkBoxTreeItem.setIndeterminate(false);
        } else if (n == 0 && n2 == 0) {
            checkBoxTreeItem.setSelected(false);
            checkBoxTreeItem.setIndeterminate(false);
        } else {
            checkBoxTreeItem.setIndeterminate(true);
        }
    }

    private void updateDownwards() {
        if (!this.isLeaf()) {
            for (TreeItem treeItem : this.getChildren()) {
                if (!(treeItem instanceof CheckBoxTreeItem)) continue;
                CheckBoxTreeItem checkBoxTreeItem = (CheckBoxTreeItem)treeItem;
                checkBoxTreeItem.setSelected(this.isSelected());
            }
        }
    }

    private void fireEvent(CheckBoxTreeItem<T> checkBoxTreeItem, boolean bl) {
        TreeModificationEvent<T> treeModificationEvent = new TreeModificationEvent<T>(CHECK_BOX_SELECTION_CHANGED_EVENT, checkBoxTreeItem, bl);
        Event.fireEvent(this, treeModificationEvent);
    }

    public static class TreeModificationEvent<T>
    extends Event {
        private static final long serialVersionUID = -8445355590698862999L;
        private final transient CheckBoxTreeItem<T> treeItem;
        private final boolean selectionChanged;
        public static final EventType<Event> ANY = new EventType<Event>(Event.ANY, "TREE_MODIFICATION");

        public TreeModificationEvent(EventType<? extends Event> eventType, CheckBoxTreeItem<T> checkBoxTreeItem, boolean bl) {
            super(eventType);
            this.treeItem = checkBoxTreeItem;
            this.selectionChanged = bl;
        }

        public CheckBoxTreeItem<T> getTreeItem() {
            return this.treeItem;
        }

        public boolean wasSelectionChanged() {
            return this.selectionChanged;
        }

        public boolean wasIndeterminateChanged() {
            return !this.selectionChanged;
        }
    }
}

