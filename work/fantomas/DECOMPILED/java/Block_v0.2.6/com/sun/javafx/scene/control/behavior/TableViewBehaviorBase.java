/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.SizeLimitedList;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.behavior.TableCellBehaviorBase;
import com.sun.javafx.scene.control.behavior.TreeTableCellBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public abstract class TableViewBehaviorBase<C extends Control, T, TC extends TableColumnBase<T, ?>>
extends BehaviorBase<C> {
    private final InputMap<C> tableViewInputMap;
    protected boolean isShortcutDown = false;
    protected boolean isShiftDown = false;
    private boolean selectionPathDeviated = false;
    protected boolean selectionChanging = false;
    private final EventHandler<KeyEvent> keyEventListener = keyEvent -> {
        if (!keyEvent.isConsumed()) {
            this.isShiftDown = keyEvent.getEventType() == KeyEvent.KEY_PRESSED && keyEvent.isShiftDown();
            this.isShortcutDown = keyEvent.getEventType() == KeyEvent.KEY_PRESSED && keyEvent.isShortcutDown();
        }
    };
    private final SizeLimitedList<TablePositionBase> selectionHistory = new SizeLimitedList(10);
    protected final ListChangeListener<TablePositionBase> selectedCellsListener = change -> {
        block0: while (change.next()) {
            if (change.wasReplaced() && TreeTableCellBehavior.hasDefaultAnchor((Control)this.getNode())) {
                TreeTableCellBehavior.removeAnchor((Control)this.getNode());
            }
            if (!change.wasAdded()) continue;
            TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
            if (tableSelectionModel == null) {
                return;
            }
            TablePositionBase tablePositionBase = this.getAnchor();
            boolean bl = tableSelectionModel.isCellSelectionEnabled();
            int n = change.getAddedSize();
            List list = change.getAddedSubList();
            for (TablePositionBase tablePositionBase2 : list) {
                if (this.selectionHistory.contains(tablePositionBase2)) continue;
                this.selectionHistory.add(tablePositionBase2);
            }
            if (n > 0 && !this.hasAnchor()) {
                TablePositionBase tablePositionBase3 = (TablePositionBase)list.get(n - 1);
                this.setAnchor(tablePositionBase3);
            }
            if (tablePositionBase == null || !bl || this.selectionPathDeviated) continue;
            for (int i = 0; i < n; ++i) {
                TablePositionBase tablePositionBase2;
                tablePositionBase2 = (TablePositionBase)list.get(i);
                if (tablePositionBase.getRow() == -1 || tablePositionBase2.getRow() == tablePositionBase.getRow() || tablePositionBase2.getColumn() == tablePositionBase.getColumn()) continue;
                this.setSelectionPathDeviated(true);
                continue block0;
            }
        }
    };
    protected final WeakListChangeListener<TablePositionBase> weakSelectedCellsListener = new WeakListChangeListener<TablePositionBase>(this.selectedCellsListener);
    private Callback<Boolean, Integer> onScrollPageUp;
    private Callback<Boolean, Integer> onScrollPageDown;
    private Runnable onFocusPreviousRow;
    private Runnable onFocusNextRow;
    private Runnable onSelectPreviousRow;
    private Runnable onSelectNextRow;
    private Runnable onMoveToFirstCell;
    private Runnable onMoveToLastCell;
    private Runnable onSelectRightCell;
    private Runnable onSelectLeftCell;

    public TableViewBehaviorBase(C c) {
        super(c);
        this.tableViewInputMap = this.createInputMap();
        InputMap.Mapping[] mappingArray = new InputMap.Mapping[60];
        mappingArray[0] = new InputMap.KeyMapping(KeyCode.TAB, FocusTraversalInputMap::traverseNext);
        mappingArray[1] = new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).shift(), FocusTraversalInputMap::traversePrevious);
        mappingArray[2] = new InputMap.KeyMapping(KeyCode.HOME, keyEvent -> this.selectFirstRow());
        mappingArray[3] = new InputMap.KeyMapping(KeyCode.END, keyEvent -> this.selectLastRow());
        mappingArray[4] = new InputMap.KeyMapping(KeyCode.PAGE_UP, keyEvent -> this.scrollUp());
        mappingArray[5] = new InputMap.KeyMapping(KeyCode.PAGE_DOWN, keyEvent -> this.scrollDown());
        mappingArray[6] = new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> this.selectLeftCell());
        mappingArray[7] = new InputMap.KeyMapping(KeyCode.KP_LEFT, keyEvent -> this.selectLeftCell());
        mappingArray[8] = new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> this.selectRightCell());
        mappingArray[9] = new InputMap.KeyMapping(KeyCode.KP_RIGHT, keyEvent -> this.selectRightCell());
        mappingArray[10] = new InputMap.KeyMapping(KeyCode.UP, keyEvent -> this.selectPreviousRow());
        mappingArray[11] = new InputMap.KeyMapping(KeyCode.KP_UP, keyEvent -> this.selectPreviousRow());
        mappingArray[12] = new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> this.selectNextRow());
        mappingArray[13] = new InputMap.KeyMapping(KeyCode.KP_DOWN, keyEvent -> this.selectNextRow());
        mappingArray[14] = new InputMap.KeyMapping(KeyCode.LEFT, FocusTraversalInputMap::traverseLeft);
        mappingArray[15] = new InputMap.KeyMapping(KeyCode.KP_LEFT, FocusTraversalInputMap::traverseLeft);
        mappingArray[16] = new InputMap.KeyMapping(KeyCode.RIGHT, FocusTraversalInputMap::traverseRight);
        mappingArray[17] = new InputMap.KeyMapping(KeyCode.KP_RIGHT, FocusTraversalInputMap::traverseRight);
        mappingArray[18] = new InputMap.KeyMapping(KeyCode.UP, FocusTraversalInputMap::traverseUp);
        mappingArray[19] = new InputMap.KeyMapping(KeyCode.KP_UP, FocusTraversalInputMap::traverseUp);
        mappingArray[20] = new InputMap.KeyMapping(KeyCode.DOWN, FocusTraversalInputMap::traverseDown);
        mappingArray[21] = new InputMap.KeyMapping(KeyCode.KP_DOWN, FocusTraversalInputMap::traverseDown);
        mappingArray[22] = new InputMap.KeyMapping(new KeyBinding(KeyCode.HOME).shift(), keyEvent -> this.selectAllToFirstRow());
        mappingArray[23] = new InputMap.KeyMapping(new KeyBinding(KeyCode.END).shift(), keyEvent -> this.selectAllToLastRow());
        mappingArray[24] = new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_UP).shift(), keyEvent -> this.selectAllPageUp());
        mappingArray[25] = new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_DOWN).shift(), keyEvent -> this.selectAllPageDown());
        mappingArray[26] = new InputMap.KeyMapping(new KeyBinding(KeyCode.UP).shift(), keyEvent -> this.alsoSelectPrevious());
        mappingArray[27] = new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_UP).shift(), keyEvent -> this.alsoSelectPrevious());
        mappingArray[28] = new InputMap.KeyMapping(new KeyBinding(KeyCode.DOWN).shift(), keyEvent -> this.alsoSelectNext());
        mappingArray[29] = new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_DOWN).shift(), keyEvent -> this.alsoSelectNext());
        mappingArray[30] = new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).shift(), keyEvent -> this.selectAllToFocus(false));
        mappingArray[31] = new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).shortcut().shift(), keyEvent -> this.selectAllToFocus(true));
        mappingArray[32] = new InputMap.KeyMapping(new KeyBinding(KeyCode.LEFT).shift(), keyEvent -> this.alsoSelectLeftCell());
        mappingArray[33] = new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_LEFT).shift(), keyEvent -> this.alsoSelectLeftCell());
        mappingArray[34] = new InputMap.KeyMapping(new KeyBinding(KeyCode.RIGHT).shift(), keyEvent -> this.alsoSelectRightCell());
        mappingArray[35] = new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_RIGHT).shift(), keyEvent -> this.alsoSelectRightCell());
        mappingArray[36] = new InputMap.KeyMapping(new KeyBinding(KeyCode.UP).shortcut(), keyEvent -> this.focusPreviousRow());
        mappingArray[37] = new InputMap.KeyMapping(new KeyBinding(KeyCode.DOWN).shortcut(), keyEvent -> this.focusNextRow());
        mappingArray[38] = new InputMap.KeyMapping(new KeyBinding(KeyCode.RIGHT).shortcut(), keyEvent -> this.focusRightCell());
        mappingArray[39] = new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_RIGHT).shortcut(), keyEvent -> this.focusRightCell());
        mappingArray[40] = new InputMap.KeyMapping(new KeyBinding(KeyCode.LEFT).shortcut(), keyEvent -> this.focusLeftCell());
        mappingArray[41] = new InputMap.KeyMapping(new KeyBinding(KeyCode.KP_LEFT).shortcut(), keyEvent -> this.focusLeftCell());
        mappingArray[42] = new InputMap.KeyMapping(new KeyBinding(KeyCode.A).shortcut(), keyEvent -> this.selectAll());
        mappingArray[43] = new InputMap.KeyMapping(new KeyBinding(KeyCode.HOME).shortcut(), keyEvent -> this.focusFirstRow());
        mappingArray[44] = new InputMap.KeyMapping(new KeyBinding(KeyCode.END).shortcut(), keyEvent -> this.focusLastRow());
        mappingArray[45] = new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_UP).shortcut(), keyEvent -> this.focusPageUp());
        mappingArray[46] = new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_DOWN).shortcut(), keyEvent -> this.focusPageDown());
        mappingArray[47] = new InputMap.KeyMapping(new KeyBinding(KeyCode.UP).shortcut().shift(), keyEvent -> this.discontinuousSelectPreviousRow());
        mappingArray[48] = new InputMap.KeyMapping(new KeyBinding(KeyCode.DOWN).shortcut().shift(), keyEvent -> this.discontinuousSelectNextRow());
        mappingArray[49] = new InputMap.KeyMapping(new KeyBinding(KeyCode.LEFT).shortcut().shift(), keyEvent -> this.discontinuousSelectPreviousColumn());
        mappingArray[50] = new InputMap.KeyMapping(new KeyBinding(KeyCode.RIGHT).shortcut().shift(), keyEvent -> this.discontinuousSelectNextColumn());
        mappingArray[51] = new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_UP).shortcut().shift(), keyEvent -> this.discontinuousSelectPageUp());
        mappingArray[52] = new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_DOWN).shortcut().shift(), keyEvent -> this.discontinuousSelectPageDown());
        mappingArray[53] = new InputMap.KeyMapping(new KeyBinding(KeyCode.HOME).shortcut().shift(), keyEvent -> this.discontinuousSelectAllToFirstRow());
        mappingArray[54] = new InputMap.KeyMapping(new KeyBinding(KeyCode.END).shortcut().shift(), keyEvent -> this.discontinuousSelectAllToLastRow());
        InputMap.KeyMapping keyMapping = new InputMap.KeyMapping(KeyCode.ENTER, this::activate);
        mappingArray[55] = keyMapping;
        mappingArray[56] = new InputMap.KeyMapping(KeyCode.SPACE, this::activate);
        mappingArray[57] = new InputMap.KeyMapping(KeyCode.F2, this::activate);
        InputMap.KeyMapping keyMapping2 = new InputMap.KeyMapping(KeyCode.ESCAPE, this::cancelEdit);
        mappingArray[58] = keyMapping2;
        mappingArray[59] = new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        this.addDefaultMapping(this.tableViewInputMap, mappingArray);
        keyMapping.setAutoConsume(false);
        keyMapping2.setAutoConsume(false);
        InputMap<C> inputMap = new InputMap<C>(c);
        inputMap.setInterceptor(event -> !PlatformUtil.isMac());
        this.addDefaultMapping(inputMap, new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).shortcut().ctrl(), keyEvent -> this.toggleFocusOwnerSelection()));
        this.addDefaultChildMap(this.tableViewInputMap, inputMap);
        InputMap<C> inputMap2 = new InputMap<C>(c);
        inputMap2.setInterceptor(event -> PlatformUtil.isMac());
        this.addDefaultMapping(inputMap2, new InputMap.KeyMapping(new KeyBinding(KeyCode.SPACE).ctrl(), keyEvent -> this.toggleFocusOwnerSelection()));
        this.addDefaultChildMap(this.tableViewInputMap, inputMap2);
        ((Node)c).addEventFilter(KeyEvent.ANY, this.keyEventListener);
    }

    @Override
    public InputMap<C> getInputMap() {
        return this.tableViewInputMap;
    }

    protected void setAnchor(TablePositionBase tablePositionBase) {
        TableCellBehaviorBase.setAnchor((Control)this.getNode(), tablePositionBase, false);
        this.setSelectionPathDeviated(false);
    }

    protected TablePositionBase getAnchor() {
        return TableCellBehaviorBase.getAnchor((Control)this.getNode(), this.getFocusedCell());
    }

    protected boolean hasAnchor() {
        return TableCellBehaviorBase.hasNonDefaultAnchor((Control)this.getNode());
    }

    protected abstract int getItemCount();

    protected abstract TableFocusModel getFocusModel();

    protected abstract TableSelectionModel<T> getSelectionModel();

    protected abstract ObservableList<? extends TablePositionBase> getSelectedCells();

    protected abstract TablePositionBase getFocusedCell();

    protected abstract int getVisibleLeafIndex(TableColumnBase var1);

    protected abstract TableColumnBase getVisibleLeafColumn(int var1);

    protected abstract boolean isControlEditable();

    protected abstract void editCell(int var1, TableColumnBase var2);

    protected abstract ObservableList<? extends TableColumnBase> getVisibleLeafColumns();

    protected abstract TablePositionBase<TC> getTablePosition(int var1, TableColumnBase<T, ?> var2);

    protected void setAnchor(int n, TableColumnBase tableColumnBase) {
        this.setAnchor(n == -1 && tableColumnBase == null ? null : this.getTablePosition(n, tableColumnBase));
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

    public void setOnSelectRightCell(Runnable runnable) {
        this.onSelectRightCell = runnable;
    }

    public void setOnSelectLeftCell(Runnable runnable) {
        this.onSelectLeftCell = runnable;
    }

    public void mousePressed(MouseEvent mouseEvent) {
        if (!((Control)this.getNode()).isFocused() && ((Control)this.getNode()).isFocusTraversable()) {
            ((Control)this.getNode()).requestFocus();
        }
    }

    protected boolean isRTL() {
        return ((Control)this.getNode()).getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
    }

    private void setSelectionPathDeviated(boolean bl) {
        this.selectionPathDeviated = bl;
    }

    protected void scrollUp() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null || this.getSelectedCells().isEmpty()) {
            return;
        }
        TablePositionBase tablePositionBase = (TablePositionBase)this.getSelectedCells().get(0);
        int n = -1;
        if (this.onScrollPageUp != null) {
            n = this.onScrollPageUp.call(false);
        }
        if (n == -1) {
            return;
        }
        tableSelectionModel.clearAndSelect(n, (TableColumnBase<T, ?>)tablePositionBase.getTableColumn());
    }

    protected void scrollDown() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null || this.getSelectedCells().isEmpty()) {
            return;
        }
        TablePositionBase tablePositionBase = (TablePositionBase)this.getSelectedCells().get(0);
        int n = -1;
        if (this.onScrollPageDown != null) {
            n = this.onScrollPageDown.call(false);
        }
        if (n == -1) {
            return;
        }
        tableSelectionModel.clearAndSelect(n, (TableColumnBase<T, ?>)tablePositionBase.getTableColumn());
    }

    protected void focusFirstRow() {
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        Object TC = this.getFocusedCell() == null ? null : (Object)this.getFocusedCell().getTableColumn();
        tableFocusModel.focus(0, TC);
        if (this.onMoveToFirstCell != null) {
            this.onMoveToFirstCell.run();
        }
    }

    protected void focusLastRow() {
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        Object TC = this.getFocusedCell() == null ? null : (Object)this.getFocusedCell().getTableColumn();
        tableFocusModel.focus(this.getItemCount() - 1, TC);
        if (this.onMoveToLastCell != null) {
            this.onMoveToLastCell.run();
        }
    }

    protected void focusPreviousRow() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        if (tableSelectionModel.isCellSelectionEnabled()) {
            tableFocusModel.focusAboveCell();
        } else {
            tableFocusModel.focusPrevious();
        }
        if (!this.isShortcutDown || this.getAnchor() == null) {
            this.setAnchor(tableFocusModel.getFocusedIndex(), null);
        }
        if (this.onFocusPreviousRow != null) {
            this.onFocusPreviousRow.run();
        }
    }

    protected void focusNextRow() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        if (tableSelectionModel.isCellSelectionEnabled()) {
            tableFocusModel.focusBelowCell();
        } else {
            tableFocusModel.focusNext();
        }
        if (!this.isShortcutDown || this.getAnchor() == null) {
            this.setAnchor(tableFocusModel.getFocusedIndex(), null);
        }
        if (this.onFocusNextRow != null) {
            this.onFocusNextRow.run();
        }
    }

    protected void focusLeftCell() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        tableFocusModel.focusLeftCell();
        if (this.onFocusPreviousRow != null) {
            this.onFocusPreviousRow.run();
        }
    }

    protected void focusRightCell() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        tableFocusModel.focusRightCell();
        if (this.onFocusNextRow != null) {
            this.onFocusNextRow.run();
        }
    }

    protected void focusPageUp() {
        int n = this.onScrollPageUp.call(true);
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        Object TC = this.getFocusedCell() == null ? null : (Object)this.getFocusedCell().getTableColumn();
        tableFocusModel.focus(n, TC);
    }

    protected void focusPageDown() {
        int n = this.onScrollPageDown.call(true);
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        Object TC = this.getFocusedCell() == null ? null : (Object)this.getFocusedCell().getTableColumn();
        tableFocusModel.focus(n, TC);
    }

    protected void clearSelection() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        tableSelectionModel.clearSelection();
    }

    protected void clearSelectionOutsideRange(int n, int n2, TableColumnBase<T, ?> tableColumnBase) {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        int n3 = Math.min(n, n2);
        int n4 = Math.max(n, n2);
        ArrayList arrayList = new ArrayList(tableSelectionModel.getSelectedIndices());
        this.selectionChanging = true;
        for (int i = 0; i < arrayList.size(); ++i) {
            int n5 = (Integer)arrayList.get(i);
            if (n5 >= n3 && n5 <= n4) continue;
            tableSelectionModel.clearSelection(n5, tableColumnBase);
        }
        this.selectionChanging = false;
    }

    protected void alsoSelectPrevious() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        if (tableSelectionModel.getSelectionMode() == SelectionMode.SINGLE) {
            this.selectPreviousRow();
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        if (tableSelectionModel.isCellSelectionEnabled()) {
            this.updateCellVerticalSelection(-1, () -> this.getSelectionModel().selectAboveCell());
        } else if (this.isShiftDown && this.hasAnchor()) {
            this.updateRowSelection(-1);
        } else {
            tableSelectionModel.selectPrevious();
        }
        this.onSelectPreviousRow.run();
    }

    protected void alsoSelectNext() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        if (tableSelectionModel.getSelectionMode() == SelectionMode.SINGLE) {
            this.selectNextRow();
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        if (tableSelectionModel.isCellSelectionEnabled()) {
            this.updateCellVerticalSelection(1, () -> this.getSelectionModel().selectBelowCell());
        } else if (this.isShiftDown && this.hasAnchor()) {
            this.updateRowSelection(1);
        } else {
            tableSelectionModel.selectNext();
        }
        this.onSelectNextRow.run();
    }

    protected void alsoSelectLeftCell() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null || !tableSelectionModel.isCellSelectionEnabled()) {
            return;
        }
        this.updateCellHorizontalSelection(-1, () -> this.getSelectionModel().selectLeftCell());
        this.onSelectLeftCell.run();
    }

    protected void alsoSelectRightCell() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null || !tableSelectionModel.isCellSelectionEnabled()) {
            return;
        }
        this.updateCellHorizontalSelection(1, () -> this.getSelectionModel().selectRightCell());
        this.onSelectRightCell.run();
    }

    protected void updateRowSelection(int n) {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null || tableSelectionModel.getSelectionMode() == SelectionMode.SINGLE) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        int n2 = tableFocusModel.getFocusedIndex() + n;
        TablePositionBase tablePositionBase = this.getAnchor();
        if (!this.hasAnchor()) {
            this.setAnchor(this.getFocusedCell());
        }
        if (tableSelectionModel.getSelectedIndices().size() > 1) {
            this.clearSelectionOutsideRange(tablePositionBase.getRow(), n2, null);
        }
        if (tablePositionBase.getRow() > n2) {
            tableSelectionModel.selectRange(tablePositionBase.getRow(), n2 - 1);
        } else {
            tableSelectionModel.selectRange(tablePositionBase.getRow(), n2 + 1);
        }
    }

    protected void updateCellVerticalSelection(int n, Runnable runnable) {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null || tableSelectionModel.getSelectionMode() == SelectionMode.SINGLE) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        TablePositionBase tablePositionBase = this.getFocusedCell();
        int n2 = tablePositionBase.getRow();
        if (this.isShiftDown && tableSelectionModel.isSelected(n2 + n, (TableColumnBase<T, ?>)tablePositionBase.getTableColumn())) {
            int n3 = n2 + n;
            boolean bl = false;
            if (this.selectionHistory.size() >= 2) {
                TablePositionBase tablePositionBase2 = this.selectionHistory.get(1);
                boolean bl2 = bl = tablePositionBase2.getRow() == n3 && tablePositionBase2.getColumn() == tablePositionBase.getColumn();
            }
            int n4 = this.selectionPathDeviated ? (bl ? n2 : n3) : n2;
            tableSelectionModel.clearSelection(n4, (TableColumnBase<T, ?>)tablePositionBase.getTableColumn());
            tableFocusModel.focus(n3, tablePositionBase.getTableColumn());
        } else if (this.isShiftDown && this.getAnchor() != null && !this.selectionPathDeviated) {
            int n5 = tableFocusModel.getFocusedIndex() + n;
            n5 = Math.max(Math.min(this.getItemCount() - 1, n5), 0);
            int n6 = Math.min(this.getAnchor().getRow(), n5);
            int n7 = Math.max(this.getAnchor().getRow(), n5);
            if (tableSelectionModel.getSelectedIndices().size() > 1) {
                this.clearSelectionOutsideRange(n6, n7, (TableColumnBase<T, ?>)tablePositionBase.getTableColumn());
            }
            for (int i = n6; i <= n7; ++i) {
                if (tableSelectionModel.isSelected(i, (TableColumnBase<T, ?>)tablePositionBase.getTableColumn())) continue;
                tableSelectionModel.select(i, (TableColumnBase<T, ?>)tablePositionBase.getTableColumn());
            }
            tableFocusModel.focus(n5, tablePositionBase.getTableColumn());
        } else {
            int n8 = tableFocusModel.getFocusedIndex();
            if (!tableSelectionModel.isSelected(n8, (TableColumnBase<T, ?>)tablePositionBase.getTableColumn())) {
                tableSelectionModel.select(n8, (TableColumnBase<T, ?>)tablePositionBase.getTableColumn());
            }
            runnable.run();
        }
    }

    protected void updateCellHorizontalSelection(int n, Runnable runnable) {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null || tableSelectionModel.getSelectionMode() == SelectionMode.SINGLE) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        TablePositionBase tablePositionBase = this.getFocusedCell();
        if (tablePositionBase == null || tablePositionBase.getTableColumn() == null) {
            return;
        }
        boolean bl = false;
        TableColumnBase tableColumnBase = this.getColumn((TableColumnBase)tablePositionBase.getTableColumn(), n);
        if (tableColumnBase == null) {
            tableColumnBase = tablePositionBase.getTableColumn();
            bl = true;
        }
        int n2 = tablePositionBase.getRow();
        if (this.isShiftDown && tableSelectionModel.isSelected(n2, tableColumnBase)) {
            Object object;
            if (bl) {
                return;
            }
            boolean bl2 = false;
            ObservableList<TablePositionBase> observableList = this.getSelectedCells();
            if (observableList.size() >= 2) {
                object = (TablePositionBase)observableList.get(observableList.size() - 2);
                boolean bl3 = bl2 = ((TablePositionBase)object).getRow() == n2 && ((TablePositionBase)object).getTableColumn().equals(tableColumnBase);
            }
            object = this.selectionPathDeviated ? (bl2 ? tablePositionBase.getTableColumn() : tableColumnBase) : tablePositionBase.getTableColumn();
            tableSelectionModel.clearSelection(n2, (TableColumnBase<T, ?>)object);
            tableFocusModel.focus(n2, tableColumnBase);
        } else if (this.isShiftDown && this.getAnchor() != null && !this.selectionPathDeviated) {
            int n3 = this.getAnchor().getColumn();
            int n4 = this.getVisibleLeafIndex((TableColumnBase)tablePositionBase.getTableColumn()) + n;
            n4 = Math.max(Math.min(this.getVisibleLeafColumns().size() - 1, n4), 0);
            int n5 = Math.min(n3, n4);
            int n6 = Math.max(n3, n4);
            for (int i = n5; i <= n6; ++i) {
                tableSelectionModel.select(tablePositionBase.getRow(), this.getColumn(i));
            }
            tableFocusModel.focus(tablePositionBase.getRow(), this.getColumn(n4));
        } else {
            runnable.run();
        }
    }

    protected TableColumnBase getColumn(int n) {
        return this.getVisibleLeafColumn(n);
    }

    protected TableColumnBase getColumn(TableColumnBase tableColumnBase, int n) {
        return this.getVisibleLeafColumn(this.getVisibleLeafIndex(tableColumnBase) + n);
    }

    protected void selectFirstRow() {
        TableSelectionModel tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        ObservableList<TablePositionBase> observableList = this.getSelectedCells();
        TableColumnBase<?, ?> tableColumnBase = observableList.size() == 0 ? null : (TableColumnBase<?, ?>)((TablePositionBase)observableList.get(0)).getTableColumn();
        tableSelectionModel.clearAndSelect(0, tableColumnBase);
        if (this.onMoveToFirstCell != null) {
            this.onMoveToFirstCell.run();
        }
    }

    protected void selectLastRow() {
        TableSelectionModel tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        ObservableList<TablePositionBase> observableList = this.getSelectedCells();
        TableColumnBase<?, ?> tableColumnBase = observableList.size() == 0 ? null : (TableColumnBase<?, ?>)((TablePositionBase)observableList.get(0)).getTableColumn();
        tableSelectionModel.clearAndSelect(this.getItemCount() - 1, tableColumnBase);
        if (this.onMoveToLastCell != null) {
            this.onMoveToLastCell.run();
        }
    }

    protected void selectPreviousRow() {
        this.selectCell(-1, 0);
        if (this.onSelectPreviousRow != null) {
            this.onSelectPreviousRow.run();
        }
    }

    protected void selectNextRow() {
        this.selectCell(1, 0);
        if (this.onSelectNextRow != null) {
            this.onSelectNextRow.run();
        }
    }

    protected void selectLeftCell() {
        this.selectCell(0, -1);
        if (this.onSelectLeftCell != null) {
            this.onSelectLeftCell.run();
        }
    }

    protected void selectRightCell() {
        this.selectCell(0, 1);
        if (this.onSelectRightCell != null) {
            this.onSelectRightCell.run();
        }
    }

    protected void selectCell(int n, int n2) {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        TablePositionBase tablePositionBase = this.getFocusedCell();
        int n3 = tablePositionBase.getRow();
        int n4 = this.getVisibleLeafIndex((TableColumnBase)tablePositionBase.getTableColumn());
        if (n < 0 && n3 <= 0) {
            return;
        }
        if (n > 0 && n3 >= this.getItemCount() - 1) {
            return;
        }
        if (n2 < 0 && n4 <= 0) {
            return;
        }
        if (n2 > 0 && n4 >= this.getVisibleLeafColumns().size() - 1) {
            return;
        }
        if (n2 > 0 && n4 == -1) {
            return;
        }
        Object object = tablePositionBase.getTableColumn();
        object = this.getColumn((TableColumnBase)object, n2);
        int n5 = tablePositionBase.getRow() + n;
        tableSelectionModel.clearAndSelect(n5, (TableColumnBase<T, ?>)object);
        this.setAnchor(n5, (TableColumnBase)object);
    }

    protected void cancelEdit(KeyEvent keyEvent) {
        if (this.isControlEditable()) {
            this.editCell(-1, null);
            keyEvent.consume();
        }
    }

    protected void activate(KeyEvent keyEvent) {
        boolean bl;
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        TablePositionBase tablePositionBase = this.getFocusedCell();
        tableSelectionModel.select(tablePositionBase.getRow(), (TableColumnBase<T, ?>)tablePositionBase.getTableColumn());
        this.setAnchor(tablePositionBase);
        boolean bl2 = bl = this.isControlEditable() && ((TableColumnBase)tablePositionBase.getTableColumn()).isEditable();
        if (bl && tablePositionBase.getRow() >= 0) {
            this.editCell(tablePositionBase.getRow(), (TableColumnBase)tablePositionBase.getTableColumn());
            keyEvent.consume();
        }
    }

    protected void selectAllToFocus(boolean bl) {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        TablePositionBase tablePositionBase = this.getFocusedCell();
        int n = tablePositionBase.getRow();
        TablePositionBase tablePositionBase2 = this.getAnchor();
        int n2 = tablePositionBase2.getRow();
        tableSelectionModel.clearSelection();
        if (!tableSelectionModel.isCellSelectionEnabled()) {
            int n3 = n2;
            int n4 = n2 > n ? n - 1 : n + 1;
            tableSelectionModel.selectRange(n3, n4);
        } else {
            tableSelectionModel.selectRange(tablePositionBase2.getRow(), (TableColumnBase<T, ?>)tablePositionBase2.getTableColumn(), tablePositionBase.getRow(), (TableColumnBase<T, ?>)tablePositionBase.getTableColumn());
        }
        this.setAnchor(bl ? tablePositionBase : tablePositionBase2);
    }

    protected void selectAll() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        tableSelectionModel.selectAll();
    }

    protected void selectAllToFirstRow() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        boolean bl = tableSelectionModel.getSelectionMode() == SelectionMode.SINGLE;
        TablePositionBase tablePositionBase = this.getFocusedCell();
        Object TC = this.getFocusedCell().getTableColumn();
        int n = tablePositionBase.getRow();
        if (this.isShiftDown) {
            n = this.getAnchor() == null ? n : this.getAnchor().getRow();
        }
        tableSelectionModel.clearSelection();
        if (!tableSelectionModel.isCellSelectionEnabled()) {
            if (bl) {
                tableSelectionModel.select(0);
            } else {
                tableSelectionModel.selectRange(n, -1);
            }
            tableFocusModel.focus(0);
        } else {
            if (bl) {
                tableSelectionModel.select(0, (TableColumnBase<T, ?>)TC);
            } else {
                tableSelectionModel.selectRange(n, (TableColumnBase<T, ?>)TC, -1, (TableColumnBase<T, ?>)TC);
            }
            tableFocusModel.focus(0, TC);
        }
        if (this.isShiftDown) {
            this.setAnchor(n, (TableColumnBase)TC);
        }
        if (this.onMoveToFirstCell != null) {
            this.onMoveToFirstCell.run();
        }
    }

    protected void selectAllToLastRow() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        int n = this.getItemCount();
        TablePositionBase tablePositionBase = this.getFocusedCell();
        Object TC = this.getFocusedCell().getTableColumn();
        int n2 = tablePositionBase.getRow();
        if (this.isShiftDown) {
            n2 = this.getAnchor() == null ? n2 : this.getAnchor().getRow();
        }
        tableSelectionModel.clearSelection();
        if (!tableSelectionModel.isCellSelectionEnabled()) {
            tableSelectionModel.selectRange(n2, n);
        } else {
            tableSelectionModel.selectRange(n2, (TableColumnBase<T, ?>)TC, n - 1, (TableColumnBase<T, ?>)TC);
        }
        if (this.isShiftDown) {
            this.setAnchor(n2, (TableColumnBase)TC);
        }
        if (this.onMoveToLastCell != null) {
            this.onMoveToLastCell.run();
        }
    }

    protected void selectAllPageUp() {
        TableColumnBase tableColumnBase;
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        int n = tableFocusModel.getFocusedIndex();
        TableColumnBase tableColumnBase2 = tableColumnBase = tableSelectionModel.isCellSelectionEnabled() ? (TableColumnBase)this.getFocusedCell().getTableColumn() : null;
        if (this.isShiftDown) {
            n = this.getAnchor() == null ? n : this.getAnchor().getRow();
            this.setAnchor(n, tableColumnBase);
        }
        int n2 = this.onScrollPageUp.call(false);
        this.selectionChanging = true;
        if (tableSelectionModel.getSelectionMode() == null || tableSelectionModel.getSelectionMode() == SelectionMode.SINGLE) {
            if (tableSelectionModel.isCellSelectionEnabled()) {
                tableSelectionModel.select(n2, tableColumnBase);
            } else {
                tableSelectionModel.select(n2);
            }
        } else {
            tableSelectionModel.clearSelection();
            if (tableSelectionModel.isCellSelectionEnabled()) {
                tableSelectionModel.selectRange(n, tableColumnBase, n2, tableColumnBase);
            } else {
                int n3 = n < n2 ? 1 : -1;
                tableSelectionModel.selectRange(n, n2 + n3);
            }
        }
        this.selectionChanging = false;
    }

    protected void selectAllPageDown() {
        TableColumnBase tableColumnBase;
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        int n = tableFocusModel.getFocusedIndex();
        TableColumnBase tableColumnBase2 = tableColumnBase = tableSelectionModel.isCellSelectionEnabled() ? (TableColumnBase)this.getFocusedCell().getTableColumn() : null;
        if (this.isShiftDown) {
            n = this.getAnchor() == null ? n : this.getAnchor().getRow();
            this.setAnchor(n, tableColumnBase);
        }
        int n2 = this.onScrollPageDown.call(false);
        this.selectionChanging = true;
        if (tableSelectionModel.getSelectionMode() == null || tableSelectionModel.getSelectionMode() == SelectionMode.SINGLE) {
            if (tableSelectionModel.isCellSelectionEnabled()) {
                tableSelectionModel.select(n2, tableColumnBase);
            } else {
                tableSelectionModel.select(n2);
            }
        } else {
            tableSelectionModel.clearSelection();
            if (tableSelectionModel.isCellSelectionEnabled()) {
                tableSelectionModel.selectRange(n, tableColumnBase, n2, tableColumnBase);
            } else {
                int n3 = n < n2 ? 1 : -1;
                tableSelectionModel.selectRange(n, n2 + n3);
            }
        }
        this.selectionChanging = false;
    }

    protected void toggleFocusOwnerSelection() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        TablePositionBase tablePositionBase = this.getFocusedCell();
        if (tableSelectionModel.isSelected(tablePositionBase.getRow(), (TableColumnBase<T, ?>)tablePositionBase.getTableColumn())) {
            tableSelectionModel.clearSelection(tablePositionBase.getRow(), (TableColumnBase<T, ?>)tablePositionBase.getTableColumn());
            tableFocusModel.focus(tablePositionBase.getRow(), tablePositionBase.getTableColumn());
        } else {
            tableSelectionModel.select(tablePositionBase.getRow(), (TableColumnBase<T, ?>)tablePositionBase.getTableColumn());
        }
        this.setAnchor(tablePositionBase.getRow(), (TableColumnBase)tablePositionBase.getTableColumn());
    }

    protected void discontinuousSelectPreviousRow() {
        TableColumnBase<?, ?> tableColumnBase;
        TableSelectionModel tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        if (tableSelectionModel.getSelectionMode() != SelectionMode.MULTIPLE) {
            this.selectPreviousRow();
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        int n = tableFocusModel.getFocusedIndex();
        int n2 = n - 1;
        if (n2 < 0) {
            return;
        }
        int n3 = n;
        TableColumnBase<?, ?> tableColumnBase2 = tableColumnBase = tableSelectionModel.isCellSelectionEnabled() ? (TableColumnBase<?, ?>)this.getFocusedCell().getTableColumn() : null;
        if (this.isShiftDown) {
            int n4 = n3 = this.getAnchor() == null ? n : this.getAnchor().getRow();
        }
        if (!tableSelectionModel.isCellSelectionEnabled()) {
            tableSelectionModel.selectRange(n2, n3 + 1);
            tableFocusModel.focus(n2);
        } else {
            for (int i = n2; i < n3 + 1; ++i) {
                tableSelectionModel.select(i, tableColumnBase);
            }
            tableFocusModel.focus(n2, tableColumnBase);
        }
        if (this.onFocusPreviousRow != null) {
            this.onFocusPreviousRow.run();
        }
    }

    protected void discontinuousSelectNextRow() {
        TableColumnBase<?, ?> tableColumnBase;
        TableSelectionModel tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        if (tableSelectionModel.getSelectionMode() != SelectionMode.MULTIPLE) {
            this.selectNextRow();
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        int n = tableFocusModel.getFocusedIndex();
        int n2 = n + 1;
        if (n2 >= this.getItemCount()) {
            return;
        }
        int n3 = n;
        TableColumnBase<?, ?> tableColumnBase2 = tableColumnBase = tableSelectionModel.isCellSelectionEnabled() ? (TableColumnBase<?, ?>)this.getFocusedCell().getTableColumn() : null;
        if (this.isShiftDown) {
            int n4 = n3 = this.getAnchor() == null ? n : this.getAnchor().getRow();
        }
        if (!tableSelectionModel.isCellSelectionEnabled()) {
            tableSelectionModel.selectRange(n3, n2 + 1);
            tableFocusModel.focus(n2);
        } else {
            for (int i = n3; i < n2 + 1; ++i) {
                tableSelectionModel.select(i, tableColumnBase);
            }
            tableFocusModel.focus(n2, tableColumnBase);
        }
        if (this.onFocusNextRow != null) {
            this.onFocusNextRow.run();
        }
    }

    protected void discontinuousSelectPreviousColumn() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null || !tableSelectionModel.isCellSelectionEnabled()) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        TableColumnBase tableColumnBase = this.getColumn((TableColumnBase)this.getFocusedCell().getTableColumn(), -1);
        tableSelectionModel.select(tableFocusModel.getFocusedIndex(), tableColumnBase);
    }

    protected void discontinuousSelectNextColumn() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null || !tableSelectionModel.isCellSelectionEnabled()) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        TableColumnBase tableColumnBase = this.getColumn((TableColumnBase)this.getFocusedCell().getTableColumn(), 1);
        tableSelectionModel.select(tableFocusModel.getFocusedIndex(), tableColumnBase);
    }

    protected void discontinuousSelectPageUp() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        int n = this.hasAnchor() ? this.getAnchor().getRow() : tableFocusModel.getFocusedIndex();
        int n2 = this.onScrollPageUp.call(false);
        if (!tableSelectionModel.isCellSelectionEnabled()) {
            tableSelectionModel.selectRange(n, n2 - 1);
        }
    }

    protected void discontinuousSelectPageDown() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        int n = this.hasAnchor() ? this.getAnchor().getRow() : tableFocusModel.getFocusedIndex();
        int n2 = this.onScrollPageDown.call(false);
        if (!tableSelectionModel.isCellSelectionEnabled()) {
            tableSelectionModel.selectRange(n, n2 + 1);
        }
    }

    protected void discontinuousSelectAllToFirstRow() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        int n = tableFocusModel.getFocusedIndex();
        if (!tableSelectionModel.isCellSelectionEnabled()) {
            tableSelectionModel.selectRange(0, n);
            tableFocusModel.focus(0);
        } else {
            for (int i = 0; i < n; ++i) {
                tableSelectionModel.select(i, (TableColumnBase<T, ?>)this.getFocusedCell().getTableColumn());
            }
            tableFocusModel.focus(0, this.getFocusedCell().getTableColumn());
        }
        if (this.onMoveToFirstCell != null) {
            this.onMoveToFirstCell.run();
        }
    }

    protected void discontinuousSelectAllToLastRow() {
        TableSelectionModel<T> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        TableFocusModel tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        int n = tableFocusModel.getFocusedIndex() + 1;
        if (!tableSelectionModel.isCellSelectionEnabled()) {
            tableSelectionModel.selectRange(n, this.getItemCount());
        } else {
            for (int i = n; i < this.getItemCount(); ++i) {
                tableSelectionModel.select(i, (TableColumnBase<T, ?>)this.getFocusedCell().getTableColumn());
            }
        }
        if (this.onMoveToLastCell != null) {
            this.onMoveToLastCell.run();
        }
    }
}

