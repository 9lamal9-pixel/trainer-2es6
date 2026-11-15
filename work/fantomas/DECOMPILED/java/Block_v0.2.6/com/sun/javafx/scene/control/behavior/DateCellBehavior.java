/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.DatePickerContent;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.traversal.Direction;
import java.time.temporal.ChronoUnit;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.DateCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class DateCellBehavior
extends BehaviorBase<DateCell> {
    private final InputMap<DateCell> inputMap = this.createInputMap();

    public DateCellBehavior(DateCell dateCell) {
        super(dateCell);
        this.addDefaultMapping(this.inputMap, new InputMap.KeyMapping(KeyCode.UP, keyEvent -> this.traverse(dateCell, Direction.UP)), new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> this.traverse(dateCell, Direction.DOWN)), new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> this.traverse(dateCell, Direction.LEFT)), new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> this.traverse(dateCell, Direction.RIGHT)), new InputMap.KeyMapping(KeyCode.ENTER, KeyEvent.KEY_RELEASED, keyEvent -> this.selectDate()), new InputMap.KeyMapping(KeyCode.SPACE, KeyEvent.KEY_RELEASED, keyEvent -> this.selectDate()));
    }

    @Override
    public InputMap<DateCell> getInputMap() {
        return this.inputMap;
    }

    private void selectDate() {
        DateCell dateCell = (DateCell)this.getNode();
        DatePickerContent datePickerContent = this.findDatePickerContent(dateCell);
        datePickerContent.selectDayCell(dateCell);
    }

    public void traverse(DateCell dateCell, Direction direction) {
        boolean bl = dateCell.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        DatePickerContent datePickerContent = this.findDatePickerContent(dateCell);
        if (datePickerContent != null) {
            switch (direction) {
                case UP: {
                    datePickerContent.goToDayCell(dateCell, -1, ChronoUnit.WEEKS, true);
                    break;
                }
                case DOWN: {
                    datePickerContent.goToDayCell(dateCell, 1, ChronoUnit.WEEKS, true);
                    break;
                }
                case LEFT: {
                    datePickerContent.goToDayCell(dateCell, bl ? 1 : -1, ChronoUnit.DAYS, true);
                    break;
                }
                case RIGHT: {
                    datePickerContent.goToDayCell(dateCell, bl ? -1 : 1, ChronoUnit.DAYS, true);
                    break;
                }
            }
        }
    }

    protected DatePickerContent findDatePickerContent(Node node) {
        Node node2 = node;
        while ((node2 = node2.getParent()) != null && !(node2 instanceof DatePickerContent)) {
        }
        return (DatePickerContent)node2;
    }
}

