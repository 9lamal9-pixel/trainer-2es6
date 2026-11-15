/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.behavior.ListCellBehavior;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusListBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import com.sun.javafx.scene.control.skin.Utils;
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class ListViewBehavior<T>
extends BehaviorBase<ListView<T>> {
    private final InputMap<ListView<T>> listViewInputMap;
    private final EventHandler<KeyEvent> keyEventListener = keyEvent -> {
        if (!keyEvent.isConsumed()) {
            this.isShiftDown = keyEvent.getEventType() == KeyEvent.KEY_PRESSED && keyEvent.isShiftDown();
            this.isShortcutDown = keyEvent.getEventType() == KeyEvent.KEY_PRESSED && keyEvent.isShortcutDown();
        }
    };
    private boolean isShiftDown = false;
    private boolean isShortcutDown = false;
    private Callback<Boolean, Integer> onScrollPageUp;
    private Callback<Boolean, Integer> onScrollPageDown;
    private Runnable onFocusPreviousRow;
    private Runnable onFocusNextRow;
    private Runnable onSelectPreviousRow;
    private Runnable onSelectNextRow;
    private Runnable onMoveToFirstCell;
    private Runnable onMoveToLastCell;
    private boolean selectionChanging = false;
    private final ListChangeListener<Integer> selectedIndicesListener = change -> {
        int n = this.getAnchor();
        while (change.next()) {
            if (change.wasReplaced() && ListCellBehavior.hasDefaultAnchor((Control)this.getNode())) {
                ListCellBehavior.removeAnchor((Control)this.getNode());
                continue;
            }
            int n2 = change.wasPermutated() ? change.getTo() - change.getFrom() : 0;
            MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
            if (!this.selectionChanging) {
                if (multipleSelectionModel.isEmpty()) {
                    n = -1;
                } else if (this.hasAnchor() && !multipleSelectionModel.isSelected(this.getAnchor() + n2)) {
                    n = -1;
                }
            }
            if (n != -1) continue;
            int n3 = change.getAddedSize();
            n = n3 > 0 ? (Integer)change.getAddedSubList().get(n3 - 1) : n;
        }
        if (n > -1) {
            this.setAnchor(n);
        }
    };
    private final ListChangeListener<T> itemsListListener = change -> {
        while (change.next()) {
            int n;
            if (!this.hasAnchor()) continue;
            int n2 = n = this.hasAnchor() ? this.getAnchor() : 0;
            if (change.wasAdded() && change.getFrom() <= n) {
                n += change.getAddedSize();
            } else if (change.wasRemoved() && change.getFrom() <= n) {
                n -= change.getRemovedSize();
            }
            this.setAnchor(n < 0 ? 0 : n);
        }
    };
    private final ChangeListener<ObservableList<T>> itemsListener = new ChangeListener<ObservableList<T>>(){

        @Override
        public void changed(ObservableValue<? extends ObservableList<T>> observableValue, ObservableList<T> observableList, ObservableList<T> observableList2) {
            if (observableList != null) {
                observableList.removeListener(ListViewBehavior.this.weakItemsListListener);
            }
            if (observableList2 != null) {
                observableList2.addListener(ListViewBehavior.this.weakItemsListListener);
            }
        }
    };
    private final ChangeListener<MultipleSelectionModel<T>> selectionModelListener = new ChangeListener<MultipleSelectionModel<T>>(){

        @Override
        public void changed(ObservableValue<? extends MultipleSelectionModel<T>> observableValue, MultipleSelectionModel<T> multipleSelectionModel, MultipleSelectionModel<T> multipleSelectionModel2) {
            if (multipleSelectionModel != null) {
                multipleSelectionModel.getSelectedIndices().removeListener(ListViewBehavior.this.weakSelectedIndicesListener);
            }
            if (multipleSelectionModel2 != null) {
                multipleSelectionModel2.getSelectedIndices().addListener(ListViewBehavior.this.weakSelectedIndicesListener);
            }
        }
    };
    private final WeakChangeListener<ObservableList<T>> weakItemsListener = new WeakChangeListener<ObservableList<ObservableList<T>>>(this.itemsListener);
    private final WeakListChangeListener<Integer> weakSelectedIndicesListener = new WeakListChangeListener<Integer>(this.selectedIndicesListener);
    private final WeakListChangeListener<T> weakItemsListListener = new WeakListChangeListener<T>(this.itemsListListener);
    private final WeakChangeListener<MultipleSelectionModel<T>> weakSelectionModelListener = new WeakChangeListener<MultipleSelectionModel<MultipleSelectionModel<T>>>(this.selectionModelListener);
    private TwoLevelFocusListBehavior tlFocus;

    public ListViewBehavior(ListView<T> listView) {
        super(listView);
        this.listViewInputMap = this.createInputMap();
        this.addDefaultMapping(this.listViewInputMap, FocusTraversalInputMap.getFocusTraversalMappings());
        this.addDefaultMapping(this.listViewInputMap, new InputMap.KeyMapping(KeyCode.HOME, keyEvent -> this.selectFirstRow()), new InputMap.KeyMapping(KeyCode.END, keyEvent -> this.selectLastRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.HOME).shift(), keyEvent -> this.selectAllToFirstRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.END).shift(), keyEvent -> this.selectAllToLastRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_UP).shift(), keyEvent -> this.selectAllPageUp()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_DOWN).shift(), keyEvent -> this.selectAllPageDown()), new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).shift(), keyEvent -> this.selectAllToFocus(false)), new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).shortcut().shift(), keyEvent -> this.selectAllToFocus(true)), new InputMap.KeyMapping(KeyCode.PAGE_UP, keyEvent -> this.scrollPageUp()), new InputMap.KeyMapping(KeyCode.PAGE_DOWN, keyEvent -> this.scrollPageDown()), new InputMap.KeyMapping(KeyCode.ENTER, keyEvent -> this.activate()), new InputMap.KeyMapping(KeyCode.SPACE, keyEvent -> this.activate()), new InputMap.KeyMapping(KeyCode.F2, keyEvent -> this.activate()), new InputMap.KeyMapping(KeyCode.ESCAPE, keyEvent -> this.cancelEdit()), new InputMap.KeyMapping(new KeyBinding(KeyCode.A).shortcut(), keyEvent -> this.selectAll()), new InputMap.KeyMapping(new KeyBinding(KeyCode.HOME).shortcut(), keyEvent -> this.focusFirstRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.END).shortcut(), keyEvent -> this.focusLastRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_UP).shortcut(), keyEvent -> this.focusPageUp()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_DOWN).shortcut(), keyEvent -> this.focusPageDown()), new InputMap.KeyMapping(new KeyBinding(KeyCode.BACK_SLASH).shortcut(), keyEvent -> this.clearSelection()), new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed));
        InputMap<ListView<T>> inputMap = new InputMap<ListView<T>>(listView);
        inputMap.setInterceptor(event -> !PlatformUtil.isMac());
        this.addDefaultMapping(inputMap, new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).shortcut().ctrl(), keyEvent -> this.toggleFocusOwnerSelection()));
        this.addDefaultChildMap(this.listViewInputMap, inputMap);
        InputMap<ListView<T>> inputMap2 = new InputMap<ListView<T>>(listView);
        inputMap2.setInterceptor(event -> PlatformUtil.isMac());
        this.addDefaultMapping(inputMap2, new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).ctrl(), keyEvent -> this.toggleFocusOwnerSelection()));
        this.addDefaultChildMap(this.listViewInputMap, inputMap2);
        InputMap<ListView<T>> inputMap3 = new InputMap<ListView<T>>(listView);
        inputMap3.setInterceptor(event -> listView.getOrientation() != Orientation.VERTICAL);
        this.addDefaultMapping(inputMap3, new InputMap.KeyMapping(KeyCode.UP, keyEvent -> this.selectPreviousRow()), new InputMap.KeyMapping(KeyCode.KP_UP, keyEvent -> this.selectPreviousRow()), new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> this.selectNextRow()), new InputMap.KeyMapping(KeyCode.KP_DOWN, keyEvent -> this.selectNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.UP).shift(), keyEvent -> this.alsoSelectPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_UP).shift(), keyEvent -> this.alsoSelectPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.DOWN).shift(), keyEvent -> this.alsoSelectNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_DOWN).shift(), keyEvent -> this.alsoSelectNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.UP).shortcut(), keyEvent -> this.focusPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.DOWN).shortcut(), keyEvent -> this.focusNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.UP).shortcut().shift(), keyEvent -> this.discontinuousSelectPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.DOWN).shortcut().shift(), keyEvent -> this.discontinuousSelectNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_UP).shortcut().shift(), keyEvent -> this.discontinuousSelectPageUp()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_DOWN).shortcut().shift(), keyEvent -> this.discontinuousSelectPageDown()), new InputMap.KeyMapping(new KeyBinding(KeyCode.HOME).shortcut().shift(), keyEvent -> this.discontinuousSelectAllToFirstRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.END).shortcut().shift(), keyEvent -> this.discontinuousSelectAllToLastRow()));
        this.addDefaultChildMap(this.listViewInputMap, inputMap3);
        InputMap<ListView<T>> inputMap4 = new InputMap<ListView<T>>(listView);
        inputMap4.setInterceptor(event -> listView.getOrientation() != Orientation.HORIZONTAL);
        this.addDefaultMapping(inputMap4, new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> this.selectPreviousRow()), new InputMap.KeyMapping(KeyCode.KP_LEFT, keyEvent -> this.selectPreviousRow()), new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> this.selectNextRow()), new InputMap.KeyMapping(KeyCode.KP_RIGHT, keyEvent -> this.selectNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.LEFT).shift(), keyEvent -> this.alsoSelectPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_LEFT).shift(), keyEvent -> this.alsoSelectPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.RIGHT).shift(), keyEvent -> this.alsoSelectNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_RIGHT).shift(), keyEvent -> this.alsoSelectNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.LEFT).shortcut(), keyEvent -> this.focusPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.RIGHT).shortcut(), keyEvent -> this.focusNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.LEFT).shortcut().shift(), keyEvent -> this.discontinuousSelectPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.RIGHT).shortcut().shift(), keyEvent -> this.discontinuousSelectNextRow()));
        this.addDefaultChildMap(this.listViewInputMap, inputMap4);
        listView.addEventFilter(KeyEvent.ANY, this.keyEventListener);
        listView.itemsProperty().addListener(this.weakItemsListener);
        if (listView.getItems() != null) {
            listView.getItems().addListener(this.weakItemsListListener);
        }
        listView.selectionModelProperty().addListener(this.weakSelectionModelListener);
        if (listView.getSelectionModel() != null) {
            listView.getSelectionModel().getSelectedIndices().addListener(this.weakSelectedIndicesListener);
        }
        if (Utils.isTwoLevelFocus()) {
            this.tlFocus = new TwoLevelFocusListBehavior(listView);
        }
    }

    @Override
    public InputMap<ListView<T>> getInputMap() {
        return this.listViewInputMap;
    }

    @Override
    public void dispose() {
        ListView listView = (ListView)this.getNode();
        ListCellBehavior.removeAnchor(listView);
        if (this.tlFocus != null) {
            this.tlFocus.dispose();
        }
        super.dispose();
        listView.removeEventHandler(KeyEvent.ANY, this.keyEventListener);
    }

    public void setOnScrollPageUp(Callback<Boolean, Integer> callback) {
        this.onScrollPageUp = callback;
    }

    public void setOnScrollPageDown(Callback<Boolean, Integer> callback) {
        this.onScrollPageDown = callback;
    }

    public void setOnFocusPreviousRow(Runnable runnable) {
        this.onFocusPreviousRow = runnable;
    }

    public void setOnFocusNextRow(Runnable runnable) {
        this.onFocusNextRow = runnable;
    }

    public void setOnSelectPreviousRow(Runnable runnable) {
        this.onSelectPreviousRow = runnable;
    }

    public void setOnSelectNextRow(Runnable runnable) {
        this.onSelectNextRow = runnable;
    }

    public void setOnMoveToFirstCell(Runnable runnable) {
        this.onMoveToFirstCell = runnable;
    }

    public void setOnMoveToLastCell(Runnable runnable) {
        this.onMoveToLastCell = runnable;
    }

    private void setAnchor(int n) {
        ListCellBehavior.setAnchor((Control)this.getNode(), n < 0 ? null : Integer.valueOf(n), false);
    }

    private int getAnchor() {
        return ListCellBehavior.getAnchor((Control)this.getNode(), ((ListView)this.getNode()).getFocusModel().getFocusedIndex());
    }

    private boolean hasAnchor() {
        return ListCellBehavior.hasNonDefaultAnchor((Control)this.getNode());
    }

    private void mousePressed(MouseEvent mouseEvent) {
        if (!mouseEvent.isShiftDown() && !mouseEvent.isSynthesized()) {
            int n = ((ListView)this.getNode()).getSelectionModel().getSelectedIndex();
            this.setAnchor(n);
        }
        if (!((ListView)this.getNode()).isFocused() && ((ListView)this.getNode()).isFocusTraversable()) {
            ((ListView)this.getNode()).requestFocus();
        }
    }

    private int getRowCount() {
        return ((ListView)this.getNode()).getItems() == null ? 0 : ((ListView)this.getNode()).getItems().size();
    }

    private void clearSelection() {
        ((ListView)this.getNode()).getSelectionModel().clearSelection();
    }

    private void scrollPageUp() {
        int n = -1;
        if (this.onScrollPageUp != null) {
            n = this.onScrollPageUp.call(false);
        }
        if (n == -1) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        multipleSelectionModel.clearAndSelect(n);
    }

    private void scrollPageDown() {
        int n = -1;
        if (this.onScrollPageDown != null) {
            n = this.onScrollPageDown.call(false);
        }
        if (n == -1) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        multipleSelectionModel.clearAndSelect(n);
    }

    private void focusFirstRow() {
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        focusModel.focus(0);
        if (this.onMoveToFirstCell != null) {
            this.onMoveToFirstCell.run();
        }
    }

    private void focusLastRow() {
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        focusModel.focus(this.getRowCount() - 1);
        if (this.onMoveToLastCell != null) {
            this.onMoveToLastCell.run();
        }
    }

    private void focusPreviousRow() {
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        focusModel.focusPrevious();
        if (!this.isShortcutDown || this.getAnchor() == -1) {
            this.setAnchor(focusModel.getFocusedIndex());
        }
        if (this.onFocusPreviousRow != null) {
            this.onFocusPreviousRow.run();
        }
    }

    private void focusNextRow() {
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        focusModel.focusNext();
        if (!this.isShortcutDown || this.getAnchor() == -1) {
            this.setAnchor(focusModel.getFocusedIndex());
        }
        if (this.onFocusNextRow != null) {
            this.onFocusNextRow.run();
        }
    }

    private void focusPageUp() {
        int n = this.onScrollPageUp.call(true);
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        focusModel.focus(n);
    }

    private void focusPageDown() {
        int n = this.onScrollPageDown.call(true);
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        focusModel.focus(n);
    }

    private void alsoSelectPreviousRow() {
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        if (this.isShiftDown && this.getAnchor() != -1) {
            int n = focusModel.getFocusedIndex() - 1;
            if (n < 0) {
                return;
            }
            int n2 = this.getAnchor();
            if (!this.hasAnchor()) {
                this.setAnchor(focusModel.getFocusedIndex());
            }
            if (multipleSelectionModel.getSelectedIndices().size() > 1) {
                this.clearSelectionOutsideRange(n2, n);
            }
            if (n2 > n) {
                multipleSelectionModel.selectRange(n2, n - 1);
            } else {
                multipleSelectionModel.selectRange(n2, n + 1);
            }
        } else {
            multipleSelectionModel.selectPrevious();
        }
        this.onSelectPreviousRow.run();
    }

    private void alsoSelectNextRow() {
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        if (this.isShiftDown && this.getAnchor() != -1) {
            int n = focusModel.getFocusedIndex() + 1;
            int n2 = this.getAnchor();
            if (!this.hasAnchor()) {
                this.setAnchor(focusModel.getFocusedIndex());
            }
            if (multipleSelectionModel.getSelectedIndices().size() > 1) {
                this.clearSelectionOutsideRange(n2, n);
            }
            if (n2 > n) {
                multipleSelectionModel.selectRange(n2, n - 1);
            } else {
                multipleSelectionModel.selectRange(n2, n + 1);
            }
        } else {
            multipleSelectionModel.selectNext();
        }
        this.onSelectNextRow.run();
    }

    private void clearSelectionOutsideRange(int n, int n2) {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        int n3 = Math.min(n, n2);
        int n4 = Math.max(n, n2);
        ArrayList<Integer> arrayList = new ArrayList<Integer>(multipleSelectionModel.getSelectedIndices());
        this.selectionChanging = true;
        for (int i = 0; i < arrayList.size(); ++i) {
            int n5 = (Integer)arrayList.get(i);
            if (n5 >= n3 && n5 <= n4) continue;
            multipleSelectionModel.clearSelection(n5);
        }
        this.selectionChanging = false;
    }

    private void selectPreviousRow() {
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        if (n <= 0) {
            return;
        }
        this.setAnchor(n - 1);
        ((ListView)this.getNode()).getSelectionModel().clearAndSelect(n - 1);
        this.onSelectPreviousRow.run();
    }

    private void selectNextRow() {
        ListView listView = (ListView)this.getNode();
        FocusModel focusModel = listView.getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        if (n == this.getRowCount() - 1) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = listView.getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        this.setAnchor(n + 1);
        multipleSelectionModel.clearAndSelect(n + 1);
        if (this.onSelectNextRow != null) {
            this.onSelectNextRow.run();
        }
    }

    private void selectFirstRow() {
        if (this.getRowCount() > 0) {
            ((ListView)this.getNode()).getSelectionModel().clearAndSelect(0);
            if (this.onMoveToFirstCell != null) {
                this.onMoveToFirstCell.run();
            }
        }
    }

    private void selectLastRow() {
        ((ListView)this.getNode()).getSelectionModel().clearAndSelect(this.getRowCount() - 1);
        if (this.onMoveToLastCell != null) {
            this.onMoveToLastCell.run();
        }
    }

    private void selectAllPageUp() {
        int n;
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n2 = focusModel.getFocusedIndex();
        if (this.isShiftDown) {
            n2 = this.getAnchor() == -1 ? n2 : this.getAnchor();
            this.setAnchor(n2);
        }
        int n3 = n2 < (n = this.onScrollPageUp.call(false).intValue()) ? 1 : -1;
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        this.selectionChanging = true;
        if (multipleSelectionModel.getSelectionMode() == SelectionMode.SINGLE) {
            multipleSelectionModel.select(n);
        } else {
            multipleSelectionModel.clearSelection();
            multipleSelectionModel.selectRange(n2, n + n3);
        }
        this.selectionChanging = false;
    }

    private void selectAllPageDown() {
        int n;
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n2 = focusModel.getFocusedIndex();
        if (this.isShiftDown) {
            n2 = this.getAnchor() == -1 ? n2 : this.getAnchor();
            this.setAnchor(n2);
        }
        int n3 = n2 < (n = this.onScrollPageDown.call(false).intValue()) ? 1 : -1;
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        this.selectionChanging = true;
        if (multipleSelectionModel.getSelectionMode() == SelectionMode.SINGLE) {
            multipleSelectionModel.select(n);
        } else {
            multipleSelectionModel.clearSelection();
            multipleSelectionModel.selectRange(n2, n + n3);
        }
        this.selectionChanging = false;
    }

    private void selectAllToFirstRow() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        if (this.isShiftDown) {
            n = this.hasAnchor() ? this.getAnchor() : n;
        }
        multipleSelectionModel.clearSelection();
        multipleSelectionModel.selectRange(n, -1);
        focusModel.focus(0);
        if (this.isShiftDown) {
            this.setAnchor(n);
        }
        if (this.onMoveToFirstCell != null) {
            this.onMoveToFirstCell.run();
        }
    }

    private void selectAllToLastRow() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        if (this.isShiftDown) {
            n = this.hasAnchor() ? this.getAnchor() : n;
        }
        multipleSelectionModel.clearSelection();
        multipleSelectionModel.selectRange(n, this.getRowCount());
        if (this.isShiftDown) {
            this.setAnchor(n);
        }
        if (this.onMoveToLastCell != null) {
            this.onMoveToLastCell.run();
        }
    }

    private void selectAll() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        multipleSelectionModel.selectAll();
    }

    private void selectAllToFocus(boolean bl) {
        ListView listView = (ListView)this.getNode();
        if (listView.getEditingIndex() >= 0) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = listView.getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = listView.getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        int n2 = this.getAnchor();
        multipleSelectionModel.clearSelection();
        int n3 = n2;
        int n4 = n2 > n ? n - 1 : n + 1;
        multipleSelectionModel.selectRange(n3, n4);
        this.setAnchor(bl ? n : n2);
    }

    private void cancelEdit() {
        ((ListView)this.getNode()).edit(-1);
    }

    private void activate() {
        int n = ((ListView)this.getNode()).getFocusModel().getFocusedIndex();
        ((ListView)this.getNode()).getSelectionModel().select(n);
        this.setAnchor(n);
        if (n >= 0) {
            ((ListView)this.getNode()).edit(n);
        }
    }

    private void toggleFocusOwnerSelection() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        if (multipleSelectionModel.isSelected(n)) {
            multipleSelectionModel.clearSelection(n);
            focusModel.focus(n);
        } else {
            multipleSelectionModel.select(n);
        }
        this.setAnchor(n);
    }

    private void discontinuousSelectPreviousRow() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        if (multipleSelectionModel.getSelectionMode() != SelectionMode.MULTIPLE) {
            this.selectPreviousRow();
            return;
        }
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        int n2 = n - 1;
        if (n2 < 0) {
            return;
        }
        int n3 = n;
        if (this.isShiftDown) {
            n3 = this.getAnchor() == -1 ? n : this.getAnchor();
        }
        multipleSelectionModel.selectRange(n2, n3 + 1);
        focusModel.focus(n2);
        if (this.onFocusPreviousRow != null) {
            this.onFocusPreviousRow.run();
        }
    }

    private void discontinuousSelectNextRow() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        if (multipleSelectionModel.getSelectionMode() != SelectionMode.MULTIPLE) {
            this.selectNextRow();
            return;
        }
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        int n2 = n + 1;
        if (n2 >= this.getRowCount()) {
            return;
        }
        int n3 = n;
        if (this.isShiftDown) {
            n3 = this.getAnchor() == -1 ? n : this.getAnchor();
        }
        multipleSelectionModel.selectRange(n3, n2 + 1);
        focusModel.focus(n2);
        if (this.onFocusNextRow != null) {
            this.onFocusNextRow.run();
        }
    }

    private void discontinuousSelectPageUp() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = this.getAnchor();
        int n2 = this.onScrollPageUp.call(false);
        multipleSelectionModel.selectRange(n, n2 - 1);
    }

    private void discontinuousSelectPageDown() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = this.getAnchor();
        int n2 = this.onScrollPageDown.call(false);
        multipleSelectionModel.selectRange(n, n2 + 1);
    }

    private void discontinuousSelectAllToFirstRow() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        multipleSelectionModel.selectRange(0, n);
        focusModel.focus(0);
        if (this.onMoveToFirstCell != null) {
            this.onMoveToFirstCell.run();
        }
    }

    private void discontinuousSelectAllToLastRow() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((ListView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex() + 1;
        multipleSelectionModel.selectRange(n, this.getRowCount());
        if (this.onMoveToLastCell != null) {
            this.onMoveToLastCell.run();
        }
    }
}

