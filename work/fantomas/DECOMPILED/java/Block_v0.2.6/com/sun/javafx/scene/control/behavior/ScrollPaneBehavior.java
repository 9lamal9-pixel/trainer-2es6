/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;
import java.util.Optional;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

public class ScrollPaneBehavior
extends BehaviorBase<ScrollPane> {
    private final InputMap<ScrollPane> inputMap = this.createInputMap();

    public ScrollPaneBehavior(ScrollPane scrollPane) {
        super(scrollPane);
        this.addDefaultMapping(this.inputMap, new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> this.rtl(scrollPane, this::horizontalUnitIncrement, this::horizontalUnitDecrement)), new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> this.rtl(scrollPane, this::horizontalUnitDecrement, this::horizontalUnitIncrement)), new InputMap.KeyMapping(KeyCode.UP, keyEvent -> this.verticalUnitDecrement()), new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> this.verticalUnitIncrement()), new InputMap.KeyMapping(KeyCode.PAGE_UP, keyEvent -> this.verticalPageDecrement()), new InputMap.KeyMapping(KeyCode.PAGE_DOWN, keyEvent -> this.verticalPageIncrement()), new InputMap.KeyMapping(KeyCode.SPACE, keyEvent -> this.verticalPageIncrement()), new InputMap.KeyMapping(KeyCode.HOME, keyEvent -> this.verticalHome()), new InputMap.KeyMapping(KeyCode.END, keyEvent -> this.verticalEnd()), new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed));
    }

    @Override
    public InputMap<ScrollPane> getInputMap() {
        return this.inputMap;
    }

    public void horizontalUnitIncrement() {
        this.getHorizontalScrollBar().ifPresent(ScrollBar::increment);
    }

    public void horizontalUnitDecrement() {
        this.getHorizontalScrollBar().ifPresent(ScrollBar::decrement);
    }

    public void verticalUnitIncrement() {
        this.getVerticalScrollBar().ifPresent(ScrollBar::increment);
    }

    void verticalUnitDecrement() {
        this.getVerticalScrollBar().ifPresent(ScrollBar::decrement);
    }

    void horizontalPageIncrement() {
        this.getHorizontalScrollBar().ifPresent(ScrollBar::increment);
    }

    void horizontalPageDecrement() {
        this.getHorizontalScrollBar().ifPresent(ScrollBar::decrement);
    }

    void verticalPageIncrement() {
        this.getVerticalScrollBar().ifPresent(ScrollBar::increment);
    }

    void verticalPageDecrement() {
        this.getVerticalScrollBar().ifPresent(ScrollBar::decrement);
    }

    void verticalHome() {
        ScrollPane scrollPane = (ScrollPane)this.getNode();
        scrollPane.setHvalue(scrollPane.getHmin());
        scrollPane.setVvalue(scrollPane.getVmin());
    }

    void verticalEnd() {
        ScrollPane scrollPane = (ScrollPane)this.getNode();
        scrollPane.setHvalue(scrollPane.getHmax());
        scrollPane.setVvalue(scrollPane.getVmax());
    }

    private Optional<ScrollBar> getVerticalScrollBar() {
        return Optional.ofNullable(((ScrollPaneSkin)((ScrollPane)this.getNode()).getSkin()).getVerticalScrollBar());
    }

    private Optional<ScrollBar> getHorizontalScrollBar() {
        return Optional.ofNullable(((ScrollPaneSkin)((ScrollPane)this.getNode()).getSkin()).getHorizontalScrollBar());
    }

    public void mousePressed(MouseEvent mouseEvent) {
        ((ScrollPane)this.getNode()).requestFocus();
    }
}

