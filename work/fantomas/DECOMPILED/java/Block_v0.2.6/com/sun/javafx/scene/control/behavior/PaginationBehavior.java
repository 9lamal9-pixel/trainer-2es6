/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;
import javafx.scene.control.Pagination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

public class PaginationBehavior
extends BehaviorBase<Pagination> {
    private final InputMap<Pagination> paginationInputMap = this.createInputMap();

    public PaginationBehavior(Pagination pagination) {
        super(pagination);
        this.addDefaultMapping(this.paginationInputMap, new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> this.rtl(pagination, this::right, this::left)), new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> this.rtl(pagination, this::left, this::right)), new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed));
    }

    @Override
    public InputMap<Pagination> getInputMap() {
        return this.paginationInputMap;
    }

    public void mousePressed(MouseEvent mouseEvent) {
        ((Pagination)this.getNode()).requestFocus();
    }

    private void left() {
        this.movePage(-1);
    }

    private void right() {
        this.movePage(1);
    }

    private void movePage(int n) {
        Pagination pagination = (Pagination)this.getNode();
        int n2 = pagination.getCurrentPageIndex();
        pagination.setCurrentPageIndex(n2 + n);
    }
}

