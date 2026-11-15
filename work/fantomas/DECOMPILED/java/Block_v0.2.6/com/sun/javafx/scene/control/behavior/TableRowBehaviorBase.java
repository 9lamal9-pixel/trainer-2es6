/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;
import javafx.collections.ObservableList;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public abstract class TableRowBehaviorBase<T extends Cell>
extends CellBehaviorBase<T> {
    public TableRowBehaviorBase(T t) {
        super(t);
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (!this.isClickPositionValid(mouseEvent.getX(), mouseEvent.getY())) {
            return;
        }
        super.mousePressed(mouseEvent);
    }

    @Override
    protected abstract TableSelectionModel<?> getSelectionModel();

    protected abstract TablePositionBase<?> getFocusedCell();

    protected abstract ObservableList getVisibleLeafColumns();

    @Override
    protected void doSelect(double d, double d2, MouseButton mouseButton, int n, boolean bl, boolean bl2) {
        Control control = this.getCellContainer();
        if (control == null) {
            return;
        }
        if (this.handleDisclosureNode(d, d2)) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = this.getSelectionModel();
        if (multipleSelectionModel == null || ((TableSelectionModel)multipleSelectionModel).isCellSelectionEnabled()) {
            return;
        }
        int n2 = this.getIndex();
        boolean bl3 = ((TableSelectionModel)multipleSelectionModel).isSelected(n2);
        if (n == 1) {
            if (!this.isClickPositionValid(d, d2)) {
                return;
            }
            if (bl3 && bl2) {
                ((TableSelectionModel)multipleSelectionModel).clearSelection(n2);
            } else if (bl2) {
                ((TableSelectionModel)multipleSelectionModel).select(this.getIndex());
            } else if (bl) {
                TablePositionBase<?> tablePositionBase = TableRowBehaviorBase.getAnchor(control, this.getFocusedCell());
                int n3 = tablePositionBase.getRow();
                this.selectRows(n3, n2);
            } else {
                this.simpleSelect(mouseButton, n, bl2);
            }
        } else {
            this.simpleSelect(mouseButton, n, bl2);
        }
    }

    @Override
    protected boolean isClickPositionValid(double d, double d2) {
        ObservableList observableList = this.getVisibleLeafColumns();
        double d3 = 0.0;
        for (int i = 0; i < observableList.size(); ++i) {
            d3 += ((TableColumnBase)observableList.get(i)).getWidth();
        }
        return d > d3;
    }
}

