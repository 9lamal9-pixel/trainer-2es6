/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ControlUtils;
import javafx.scene.control.FocusModel;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ListCellSkin;

public class ListCell<T>
extends IndexedCell<T> {
    private final InvalidationListener editingListener = observable -> this.updateEditing();
    private boolean updateEditingIndex = true;
    private final ListChangeListener<Integer> selectedListener = change -> this.updateSelection();
    private final ChangeListener<MultipleSelectionModel<T>> selectionModelPropertyListener = new ChangeListener<MultipleSelectionModel<T>>(){

        @Override
        public void changed(ObservableValue<? extends MultipleSelectionModel<T>> observableValue, MultipleSelectionModel<T> multipleSelectionModel, MultipleSelectionModel<T> multipleSelectionModel2) {
            if (multipleSelectionModel != null) {
                multipleSelectionModel.getSelectedIndices().removeListener(ListCell.this.weakSelectedListener);
            }
            if (multipleSelectionModel2 != null) {
                multipleSelectionModel2.getSelectedIndices().addListener(ListCell.this.weakSelectedListener);
            }
            ListCell.this.updateSelection();
        }
    };
    private final ListChangeListener<T> itemsListener = change -> {
        boolean bl = false;
        while (change.next()) {
            int n = this.getIndex();
            ListView<T> listView = this.getListView();
            ObservableList<T> observableList = listView == null ? null : listView.getItems();
            int n2 = observableList == null ? 0 : observableList.size();
            boolean bl2 = n >= change.getFrom();
            boolean bl3 = n < change.getTo() || n == n2;
            boolean bl4 = bl2 && bl3;
            bl = bl4 || bl2 && !change.wasReplaced() && (change.wasRemoved() || change.wasAdded());
        }
        if (bl) {
            this.updateItem(-1);
        }
    };
    private final InvalidationListener itemsPropertyListener = new InvalidationListener(){
        private WeakReference<ObservableList<T>> weakItemsRef = new WeakReference<Object>(null);

        @Override
        public void invalidated(Observable observable) {
            ListView listView;
            ObservableList observableList = (ObservableList)this.weakItemsRef.get();
            if (observableList != null) {
                observableList.removeListener(ListCell.this.weakItemsListener);
            }
            ObservableList observableList2 = (listView = ListCell.this.getListView()) == null ? null : listView.getItems();
            this.weakItemsRef = new WeakReference(observableList2);
            if (observableList2 != null) {
                observableList2.addListener(ListCell.this.weakItemsListener);
            }
            ListCell.this.updateItem(-1);
        }
    };
    private final InvalidationListener focusedListener = observable -> this.updateFocus();
    private final ChangeListener<FocusModel<T>> focusModelPropertyListener = new ChangeListener<FocusModel<T>>(){

        @Override
        public void changed(ObservableValue<? extends FocusModel<T>> observableValue, FocusModel<T> focusModel, FocusModel<T> focusModel2) {
            if (focusModel != null) {
                focusModel.focusedIndexProperty().removeListener(ListCell.this.weakFocusedListener);
            }
            if (focusModel2 != null) {
                focusModel2.focusedIndexProperty().addListener(ListCell.this.weakFocusedListener);
            }
            ListCell.this.updateFocus();
        }
    };
    private final WeakInvalidationListener weakEditingListener = new WeakInvalidationListener(this.editingListener);
    private final WeakListChangeListener<Integer> weakSelectedListener = new WeakListChangeListener<Integer>(this.selectedListener);
    private final WeakChangeListener<MultipleSelectionModel<T>> weakSelectionModelPropertyListener = new WeakChangeListener<MultipleSelectionModel<MultipleSelectionModel<T>>>(this.selectionModelPropertyListener);
    private final WeakListChangeListener<T> weakItemsListener = new WeakListChangeListener<T>(this.itemsListener);
    private final WeakInvalidationListener weakItemsPropertyListener = new WeakInvalidationListener(this.itemsPropertyListener);
    private final WeakInvalidationListener weakFocusedListener = new WeakInvalidationListener(this.focusedListener);
    private final WeakChangeListener<FocusModel<T>> weakFocusModelPropertyListener = new WeakChangeListener<FocusModel<FocusModel<T>>>(this.focusModelPropertyListener);
    private ReadOnlyObjectWrapper<ListView<T>> listView = new ReadOnlyObjectWrapper<ListView<T>>(this, "listView"){
        private WeakReference<ListView<T>> weakListViewRef;
        {
            this.weakListViewRef = new WeakReference<Object>(null);
        }

        @Override
        protected void invalidated() {
            ObservableList observableList;
            FocusModel focusModel;
            MultipleSelectionModel multipleSelectionModel;
            ListView listView;
            ListView listView2 = (ListView)this.get();
            if (listView2 == (listView = (ListView)this.weakListViewRef.get())) {
                return;
            }
            if (listView != null) {
                multipleSelectionModel = listView.getSelectionModel();
                if (multipleSelectionModel != null) {
                    multipleSelectionModel.getSelectedIndices().removeListener(ListCell.this.weakSelectedListener);
                }
                if ((focusModel = listView.getFocusModel()) != null) {
                    focusModel.focusedIndexProperty().removeListener(ListCell.this.weakFocusedListener);
                }
                if ((observableList = listView.getItems()) != null) {
                    observableList.removeListener(ListCell.this.weakItemsListener);
                }
                listView.editingIndexProperty().removeListener(ListCell.this.weakEditingListener);
                listView.itemsProperty().removeListener(ListCell.this.weakItemsPropertyListener);
                listView.focusModelProperty().removeListener(ListCell.this.weakFocusModelPropertyListener);
                listView.selectionModelProperty().removeListener(ListCell.this.weakSelectionModelPropertyListener);
            }
            if (listView2 != null) {
                multipleSelectionModel = listView2.getSelectionModel();
                if (multipleSelectionModel != null) {
                    multipleSelectionModel.getSelectedIndices().addListener(ListCell.this.weakSelectedListener);
                }
                if ((focusModel = listView2.getFocusModel()) != null) {
                    focusModel.focusedIndexProperty().addListener(ListCell.this.weakFocusedListener);
                }
                if ((observableList = listView2.getItems()) != null) {
                    observableList.addListener(ListCell.this.weakItemsListener);
                }
                listView2.editingIndexProperty().addListener(ListCell.this.weakEditingListener);
                listView2.itemsProperty().addListener(ListCell.this.weakItemsPropertyListener);
                listView2.focusModelProperty().addListener(ListCell.this.weakFocusModelPropertyListener);
                listView2.selectionModelProperty().addListener(ListCell.this.weakSelectionModelPropertyListener);
                this.weakListViewRef = new WeakReference<ListView>(listView2);
            }
            ListCell.this.updateItem(-1);
            ListCell.this.updateSelection();
            ListCell.this.updateFocus();
            ListCell.this.requestLayout();
        }
    };
    private boolean firstRun = true;
    private static final String DEFAULT_STYLE_CLASS = "list-cell";

    public ListCell() {
        this.getStyleClass().addAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.LIST_ITEM);
    }

    private void setListView(ListView<T> listView) {
        this.listView.set(listView);
    }

    public final ListView<T> getListView() {
        return (ListView)this.listView.get();
    }

    public final ReadOnlyObjectProperty<ListView<T>> listViewProperty() {
        return this.listView.getReadOnlyProperty();
    }

    @Override
    void indexChanged(int n, int n2) {
        super.indexChanged(n, n2);
        if (!this.isEditing() || n2 != n) {
            this.updateItem(n);
            this.updateSelection();
            this.updateFocus();
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ListCellSkin(this);
    }

    @Override
    public void startEdit() {
        ListView<T> listView = this.getListView();
        if (!this.isEditable() || listView != null && !listView.isEditable()) {
            return;
        }
        super.startEdit();
        if (listView != null) {
            listView.fireEvent(new ListView.EditEvent<Object>(listView, ListView.editStartEvent(), null, this.getIndex()));
            listView.edit(this.getIndex());
            listView.requestFocus();
        }
    }

    @Override
    public void commitEdit(T t) {
        if (!this.isEditing()) {
            return;
        }
        ListView<T> listView = this.getListView();
        if (listView != null) {
            listView.fireEvent(new ListView.EditEvent<T>(listView, ListView.editCommitEvent(), t, listView.getEditingIndex()));
        }
        super.commitEdit(t);
        this.updateItem(t, false);
        if (listView != null) {
            listView.edit(-1);
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(listView);
        }
    }

    @Override
    public void cancelEdit() {
        if (!this.isEditing()) {
            return;
        }
        ListView<T> listView = this.getListView();
        super.cancelEdit();
        if (listView != null) {
            int n = listView.getEditingIndex();
            if (this.updateEditingIndex) {
                listView.edit(-1);
            }
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(listView);
            listView.fireEvent(new ListView.EditEvent<Object>(listView, ListView.editCancelEvent(), null, n));
        }
    }

    private void updateItem(int n) {
        ListView<T> listView = this.getListView();
        ObservableList<T> observableList = listView == null ? null : listView.getItems();
        int n2 = this.getIndex();
        int n3 = observableList == null ? -1 : observableList.size();
        boolean bl = observableList != null && n2 >= 0 && n2 < n3;
        Object t = this.getItem();
        boolean bl2 = this.isEmpty();
        if (bl) {
            Object e = observableList.get(n2);
            if (n != n2 || this.isItemChanged(t, e)) {
                this.updateItem(e, false);
            }
        } else if (!bl2 && t != null || this.firstRun) {
            this.updateItem(null, true);
            this.firstRun = false;
        }
    }

    public final void updateListView(ListView<T> listView) {
        this.setListView(listView);
    }

    private void updateSelection() {
        if (this.isEmpty()) {
            return;
        }
        int n = this.getIndex();
        ListView<T> listView = this.getListView();
        if (n == -1 || listView == null) {
            return;
        }
        MultipleSelectionModel<T> multipleSelectionModel = listView.getSelectionModel();
        if (multipleSelectionModel == null) {
            this.updateSelected(false);
            return;
        }
        boolean bl = multipleSelectionModel.isSelected(n);
        if (this.isSelected() == bl) {
            return;
        }
        this.updateSelected(bl);
    }

    private void updateFocus() {
        int n = this.getIndex();
        ListView<T> listView = this.getListView();
        if (n == -1 || listView == null) {
            return;
        }
        FocusModel<T> focusModel = listView.getFocusModel();
        if (focusModel == null) {
            this.setFocused(false);
            return;
        }
        this.setFocused(focusModel.isFocused(n));
    }

    private void updateEditing() {
        int n = this.getIndex();
        ListView<T> listView = this.getListView();
        int n2 = listView == null ? -1 : listView.getEditingIndex();
        boolean bl = this.isEditing();
        if (n != -1 && listView != null) {
            if (n == n2 && !bl) {
                this.startEdit();
            } else if (n != n2 && bl) {
                this.updateEditingIndex = false;
                this.cancelEdit();
                this.updateEditingIndex = true;
            }
        }
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case INDEX: {
                return this.getIndex();
            }
            case SELECTED: {
                return this.isSelected();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case REQUEST_FOCUS: {
                FocusModel<T> focusModel;
                ListView<T> listView = this.getListView();
                if (listView == null || (focusModel = listView.getFocusModel()) == null) break;
                focusModel.focus(this.getIndex());
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, objectArray);
            }
        }
    }
}

