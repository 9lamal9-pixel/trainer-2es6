/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TreeCellBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class TreeViewBehavior<T>
extends BehaviorBase<TreeView<T>> {
    private final InputMap<TreeView<T>> treeViewInputMap;
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
    private Runnable onSelectPreviousRow;
    private Runnable onSelectNextRow;
    private Runnable onMoveToFirstCell;
    private Runnable onMoveToLastCell;
    private Runnable onFocusPreviousRow;
    private Runnable onFocusNextRow;
    private boolean selectionChanging = false;
    private final ListChangeListener<Integer> selectedIndicesListener = change -> {
        int n = this.getAnchor();
        while (change.next()) {
            if (change.wasReplaced() && TreeCellBehavior.hasDefaultAnchor((Control)this.getNode())) {
                TreeCellBehavior.removeAnchor((Control)this.getNode());
                continue;
            }
            int n2 = change.wasPermutated() ? change.getTo() - change.getFrom() : 0;
            MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
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
    private final ChangeListener<MultipleSelectionModel<TreeItem<T>>> selectionModelListener = new ChangeListener<MultipleSelectionModel<TreeItem<T>>>(){

        @Override
        public void changed(ObservableValue<? extends MultipleSelectionModel<TreeItem<T>>> observableValue, MultipleSelectionModel<TreeItem<T>> multipleSelectionModel, MultipleSelectionModel<TreeItem<T>> multipleSelectionModel2) {
            if (multipleSelectionModel != null) {
                multipleSelectionModel.getSelectedIndices().removeListener(TreeViewBehavior.this.weakSelectedIndicesListener);
            }
            if (multipleSelectionModel2 != null) {
                multipleSelectionModel2.getSelectedIndices().addListener(TreeViewBehavior.this.weakSelectedIndicesListener);
            }
        }
    };
    private final WeakListChangeListener<Integer> weakSelectedIndicesListener = new WeakListChangeListener<Integer>(this.selectedIndicesListener);
    private final WeakChangeListener<MultipleSelectionModel<TreeItem<T>>> weakSelectionModelListener = new WeakChangeListener<MultipleSelectionModel<TreeItem<MultipleSelectionModel<TreeItem<T>>>>>(this.selectionModelListener);

    public void setOnScrollPageUp(Callback<Boolean, Integer> callback) {
        this.onScrollPageUp = callback;
    }

    public void setOnScrollPageDown(Callback<Boolean, Integer> callback) {
        this.onScrollPageDown = callback;
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

    public void setOnFocusPreviousRow(Runnable runnable) {
        this.onFocusPreviousRow = runnable;
    }

    public void setOnFocusNextRow(Runnable runnable) {
        this.onFocusNextRow = runnable;
    }

    public TreeViewBehavior(TreeView<T> treeView) {
        super(treeView);
        this.treeViewInputMap = this.createInputMap();
        this.addDefaultMapping(this.treeViewInputMap, new InputMap.KeyMapping(KeyCode.HOME, keyEvent -> this.selectFirstRow()), new InputMap.KeyMapping(KeyCode.END, keyEvent -> this.selectLastRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.HOME).shift(), keyEvent -> this.selectAllToFirstRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.END).shift(), keyEvent -> this.selectAllToLastRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_UP).shift(), keyEvent -> this.selectAllPageUp()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_DOWN).shift(), keyEvent -> this.selectAllPageDown()), new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).shift(), keyEvent -> this.selectAllToFocus(false)), new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).shortcut().shift(), keyEvent -> this.selectAllToFocus(true)), new InputMap.KeyMapping(new KeyBinding(KeyCode.HOME).shortcut(), keyEvent -> this.focusFirstRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.END).shortcut(), keyEvent -> this.focusLastRow()), new InputMap.KeyMapping(KeyCode.PAGE_UP, keyEvent -> this.scrollUp()), new InputMap.KeyMapping(KeyCode.PAGE_DOWN, keyEvent -> this.scrollDown()), new InputMap.KeyMapping(KeyCode.SPACE, keyEvent -> this.toggleFocusOwnerSelection()), new InputMap.KeyMapping(new KeyBinding(KeyCode.A).shortcut(), keyEvent -> this.selectAll()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_UP).shortcut(), keyEvent -> this.focusPageUp()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_DOWN).shortcut(), keyEvent -> this.focusPageDown()), new InputMap.KeyMapping(new KeyBinding(KeyCode.UP).shortcut(), keyEvent -> this.focusPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.DOWN).shortcut(), keyEvent -> this.focusNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.UP).shortcut().shift(), keyEvent -> this.discontinuousSelectPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.DOWN).shortcut().shift(), keyEvent -> this.discontinuousSelectNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_UP).shortcut().shift(), keyEvent -> this.discontinuousSelectPageUp()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_DOWN).shortcut().shift(), keyEvent -> this.discontinuousSelectPageDown()), new InputMap.KeyMapping(new KeyBinding(KeyCode.HOME).shortcut().shift(), keyEvent -> this.discontinuousSelectAllToFirstRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.END).shortcut().shift(), keyEvent -> this.discontinuousSelectAllToLastRow()), new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> this.rtl(treeView, this::expandRow, this::collapseRow)), new InputMap.KeyMapping(KeyCode.KP_LEFT, keyEvent -> this.rtl(treeView, this::expandRow, this::collapseRow)), new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> this.rtl(treeView, this::collapseRow, this::expandRow)), new InputMap.KeyMapping(KeyCode.KP_RIGHT, keyEvent -> this.rtl(treeView, this::collapseRow, this::expandRow)), new InputMap.KeyMapping(KeyCode.MULTIPLY, keyEvent -> this.expandAll()), new InputMap.KeyMapping(KeyCode.ADD, keyEvent -> this.expandRow()), new InputMap.KeyMapping(KeyCode.SUBTRACT, keyEvent -> this.collapseRow()), new InputMap.KeyMapping(KeyCode.UP, keyEvent -> this.selectPreviousRow()), new InputMap.KeyMapping(KeyCode.KP_UP, keyEvent -> this.selectPreviousRow()), new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> this.selectNextRow()), new InputMap.KeyMapping(KeyCode.KP_DOWN, keyEvent -> this.selectNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.UP).shift(), keyEvent -> this.alsoSelectPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_UP).shift(), keyEvent -> this.alsoSelectPreviousRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.DOWN).shift(), keyEvent -> this.alsoSelectNextRow()), new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_DOWN).shift(), keyEvent -> this.alsoSelectNextRow()), new InputMap.KeyMapping(KeyCode.ENTER, keyEvent -> this.edit()), new InputMap.KeyMapping(KeyCode.F2, keyEvent -> this.edit()), new InputMap.KeyMapping(KeyCode.ESCAPE, keyEvent -> this.cancelEdit()), new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed));
        InputMap<TreeView<T>> inputMap = new InputMap<TreeView<T>>(treeView);
        inputMap.setInterceptor(event -> !PlatformUtil.isMac());
        this.addDefaultMapping(inputMap, new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).shortcut().ctrl(), keyEvent -> this.toggleFocusOwnerSelection()));
        this.addDefaultChildMap(this.treeViewInputMap, inputMap);
        InputMap<TreeView<T>> inputMap2 = new InputMap<TreeView<T>>(treeView);
        inputMap2.setInterceptor(event -> PlatformUtil.isMac());
        this.addDefaultMapping(inputMap2, new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).ctrl(), keyEvent -> this.toggleFocusOwnerSelection()));
        this.addDefaultChildMap(this.treeViewInputMap, inputMap2);
        treeView.addEventFilter(KeyEvent.ANY, this.keyEventListener);
        treeView.selectionModelProperty().addListener(this.weakSelectionModelListener);
        if (treeView.getSelectionModel() != null) {
            treeView.getSelectionModel().getSelectedIndices().addListener(this.weakSelectedIndicesListener);
        }
    }

    @Override
    public InputMap<TreeView<T>> getInputMap() {
        return this.treeViewInputMap;
    }

    @Override
    public void dispose() {
        TreeCellBehavior.removeAnchor((Control)this.getNode());
        super.dispose();
    }

    private void setAnchor(int n) {
        TreeCellBehavior.setAnchor((Control)this.getNode(), n < 0 ? null : Integer.valueOf(n), false);
    }

    private int getAnchor() {
        return TreeCellBehavior.getAnchor((Control)this.getNode(), ((TreeView)this.getNode()).getFocusModel().getFocusedIndex());
    }

    private boolean hasAnchor() {
        return TreeCellBehavior.hasNonDefaultAnchor((Control)this.getNode());
    }

    public void mousePressed(MouseEvent mouseEvent) {
        if (!mouseEvent.isShiftDown()) {
            int n = ((TreeView)this.getNode()).getSelectionModel().getSelectedIndex();
            this.setAnchor(n);
        }
        if (!((TreeView)this.getNode()).isFocused() && ((TreeView)this.getNode()).isFocusTraversable()) {
            ((TreeView)this.getNode()).requestFocus();
        }
    }

    private void clearSelection() {
        ((TreeView)this.getNode()).getSelectionModel().clearSelection();
    }

    private void scrollUp() {
        int n = -1;
        if (this.onScrollPageUp != null) {
            n = this.onScrollPageUp.call(false);
        }
        if (n == -1) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        multipleSelectionModel.clearAndSelect(n);
    }

    private void scrollDown() {
        int n = -1;
        if (this.onScrollPageDown != null) {
            n = this.onScrollPageDown.call(false);
        }
        if (n == -1) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        multipleSelectionModel.clearAndSelect(n);
    }

    private void focusFirstRow() {
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        focusModel.focus(0);
        if (this.onMoveToFirstCell != null) {
            this.onMoveToFirstCell.run();
        }
    }

    private void focusLastRow() {
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        focusModel.focus(((TreeView)this.getNode()).getExpandedItemCount() - 1);
        if (this.onMoveToLastCell != null) {
            this.onMoveToLastCell.run();
        }
    }

    private void focusPreviousRow() {
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
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
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
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
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        focusModel.focus(n);
    }

    private void focusPageDown() {
        int n = this.onScrollPageDown.call(true);
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        focusModel.focus(n);
    }

    private void alsoSelectPreviousRow() {
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
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
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
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
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
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
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        if (n <= 0) {
            return;
        }
        this.setAnchor(n - 1);
        ((TreeView)this.getNode()).getSelectionModel().clearAndSelect(n - 1);
        this.onSelectPreviousRow.run();
    }

    private void selectNextRow() {
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        if (n == ((TreeView)this.getNode()).getExpandedItemCount() - 1) {
            return;
        }
        this.setAnchor(n + 1);
        ((TreeView)this.getNode()).getSelectionModel().clearAndSelect(n + 1);
        this.onSelectNextRow.run();
    }

    private void selectFirstRow() {
        if (((TreeView)this.getNode()).getExpandedItemCount() > 0) {
            ((TreeView)this.getNode()).getSelectionModel().clearAndSelect(0);
            if (this.onMoveToFirstCell != null) {
                this.onMoveToFirstCell.run();
            }
        }
    }

    private void selectLastRow() {
        ((TreeView)this.getNode()).getSelectionModel().clearAndSelect(((TreeView)this.getNode()).getExpandedItemCount() - 1);
        this.onMoveToLastCell.run();
    }

    private void selectAllToFirstRow() {
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
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
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        if (this.isShiftDown) {
            n = this.hasAnchor() ? this.getAnchor() : n;
        }
        multipleSelectionModel.clearSelection();
        multipleSelectionModel.selectRange(n, ((TreeView)this.getNode()).getExpandedItemCount());
        if (this.isShiftDown) {
            this.setAnchor(n);
        }
        if (this.onMoveToLastCell != null) {
            this.onMoveToLastCell.run();
        }
    }

    private void selectAll() {
        ((TreeView)this.getNode()).getSelectionModel().selectAll();
    }

    private void selectAllPageUp() {
        int n;
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n2 = focusModel.getFocusedIndex();
        if (this.isShiftDown) {
            n2 = this.getAnchor() == -1 ? n2 : this.getAnchor();
            this.setAnchor(n2);
        }
        int n3 = n2 < (n = this.onScrollPageUp.call(false).intValue()) ? 1 : -1;
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
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
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n2 = focusModel.getFocusedIndex();
        if (this.isShiftDown) {
            n2 = this.getAnchor() == -1 ? n2 : this.getAnchor();
            this.setAnchor(n2);
        }
        int n3 = n2 < (n = this.onScrollPageDown.call(false).intValue()) ? 1 : -1;
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
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

    private void selectAllToFocus(boolean bl) {
        TreeView treeView = (TreeView)this.getNode();
        if (treeView.getEditingItem() != null) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = treeView.getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = treeView.getFocusModel();
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

    private void expandRow() {
        Callback<TreeItem<T>, Integer> callback = treeItem -> ((TreeView)this.getNode()).getRow(treeItem);
        TreeViewBehavior.expandRow(((TreeView)this.getNode()).getSelectionModel(), callback);
    }

    private void expandAll() {
        TreeViewBehavior.expandAll(((TreeView)this.getNode()).getRoot());
    }

    private void collapseRow() {
        TreeView treeView = (TreeView)this.getNode();
        TreeViewBehavior.collapseRow(treeView.getSelectionModel(), treeView.getRoot(), treeView.isShowRoot());
    }

    static <T> void expandRow(MultipleSelectionModel<TreeItem<T>> multipleSelectionModel, Callback<TreeItem<T>, Integer> callback) {
        if (multipleSelectionModel == null) {
            return;
        }
        TreeItem treeItem = (TreeItem)multipleSelectionModel.getSelectedItem();
        if (treeItem == null || treeItem.isLeaf()) {
            return;
        }
        if (treeItem.isExpanded()) {
            ObservableList observableList = treeItem.getChildren();
            if (!observableList.isEmpty()) {
                multipleSelectionModel.clearAndSelect(callback.call((TreeItem)observableList.get(0)));
            }
        } else {
            treeItem.setExpanded(true);
        }
    }

    static <T> void expandAll(TreeItem<T> treeItem) {
        if (treeItem == null) {
            return;
        }
        treeItem.setExpanded(true);
        TreeViewBehavior.expandChildren(treeItem);
    }

    private static <T> void expandChildren(TreeItem<T> treeItem) {
        if (treeItem == null) {
            return;
        }
        ObservableList<TreeItem<T>> observableList = treeItem.getChildren();
        if (observableList == null) {
            return;
        }
        for (int i = 0; i < observableList.size(); ++i) {
            TreeItem treeItem2 = (TreeItem)observableList.get(i);
            if (treeItem2 == null || treeItem2.isLeaf()) continue;
            treeItem2.setExpanded(true);
            TreeViewBehavior.expandChildren(treeItem2);
        }
    }

    static <T> void collapseRow(MultipleSelectionModel<TreeItem<T>> multipleSelectionModel, TreeItem<T> treeItem, boolean bl) {
        if (multipleSelectionModel == null) {
            return;
        }
        TreeItem treeItem2 = (TreeItem)multipleSelectionModel.getSelectedItem();
        if (treeItem2 == null) {
            return;
        }
        if (treeItem == null) {
            return;
        }
        if (!bl && (!treeItem2.isExpanded() || treeItem2.isLeaf()) && treeItem.equals(treeItem2.getParent())) {
            return;
        }
        if (treeItem.equals(treeItem2) && (!treeItem.isExpanded() || treeItem.getChildren().isEmpty())) {
            return;
        }
        if (treeItem2.isLeaf() || !treeItem2.isExpanded()) {
            multipleSelectionModel.clearSelection();
            multipleSelectionModel.select(treeItem2.getParent());
        } else {
            treeItem2.setExpanded(false);
        }
    }

    private void cancelEdit() {
        ((TreeView)this.getNode()).edit(null);
    }

    private void edit() {
        TreeItem treeItem = (TreeItem)((TreeView)this.getNode()).getSelectionModel().getSelectedItem();
        if (treeItem == null) {
            return;
        }
        ((TreeView)this.getNode()).edit(treeItem);
    }

    private void toggleFocusOwnerSelection() {
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
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
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        if (multipleSelectionModel.getSelectionMode() != SelectionMode.MULTIPLE) {
            this.selectPreviousRow();
            return;
        }
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
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
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        if (multipleSelectionModel.getSelectionMode() != SelectionMode.MULTIPLE) {
            this.selectNextRow();
            return;
        }
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex();
        int n2 = n + 1;
        if (n2 >= ((TreeView)this.getNode()).getExpandedItemCount()) {
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
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = this.getAnchor();
        int n2 = this.onScrollPageUp.call(false);
        multipleSelectionModel.selectRange(n, n2 - 1);
    }

    private void discontinuousSelectPageDown() {
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = this.getAnchor();
        int n2 = this.onScrollPageDown.call(false);
        multipleSelectionModel.selectRange(n, n2 + 1);
    }

    private void discontinuousSelectAllToFirstRow() {
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
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
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getNode()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel focusModel = ((TreeView)this.getNode()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        int n = focusModel.getFocusedIndex() + 1;
        multipleSelectionModel.selectRange(n, ((TreeView)this.getNode()).getExpandedItemCount());
        if (this.onMoveToLastCell != null) {
            this.onMoveToLastCell.run();
        }
    }
}

