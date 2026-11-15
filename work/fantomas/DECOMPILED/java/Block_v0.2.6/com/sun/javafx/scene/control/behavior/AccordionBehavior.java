/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Accordion;
import javafx.scene.control.FocusModel;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class AccordionBehavior
extends BehaviorBase<Accordion> {
    private final InputMap<Accordion> inputMap;
    private AccordionFocusModel focusModel;

    public AccordionBehavior(Accordion accordion) {
        super(accordion);
        this.focusModel = new AccordionFocusModel(accordion);
        this.inputMap = this.createInputMap();
        this.addDefaultMapping(this.inputMap, new InputMap.KeyMapping(KeyCode.UP, keyEvent -> this.pageUp(false)), new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> this.pageDown(false)), new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> {
            if (this.isRTL(accordion)) {
                this.pageDown(false);
            } else {
                this.pageUp(false);
            }
        }), new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> {
            if (this.isRTL(accordion)) {
                this.pageUp(false);
            } else {
                this.pageDown(false);
            }
        }), new InputMap.KeyMapping(KeyCode.HOME, this::home), new InputMap.KeyMapping(KeyCode.END, this::end), new InputMap.KeyMapping(KeyCode.PAGE_UP, keyEvent -> this.pageUp(true)), new InputMap.KeyMapping(KeyCode.PAGE_DOWN, keyEvent -> this.pageDown(true)), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_UP).ctrl(), this::moveBackward), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_DOWN).ctrl(), this::moveForward), new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).ctrl(), this::moveForward), new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).ctrl().shift(), this::moveBackward), new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed));
    }

    @Override
    public void dispose() {
        this.focusModel.dispose();
        super.dispose();
    }

    @Override
    public InputMap<Accordion> getInputMap() {
        return this.inputMap;
    }

    private void pageUp(boolean bl) {
        Accordion accordion = (Accordion)this.getNode();
        if (this.focusModel.getFocusedIndex() != -1 && ((TitledPane)accordion.getPanes().get(this.focusModel.getFocusedIndex())).isFocused()) {
            this.focusModel.focusPrevious();
            int n = this.focusModel.getFocusedIndex();
            ((TitledPane)accordion.getPanes().get(n)).requestFocus();
            if (bl) {
                ((TitledPane)accordion.getPanes().get(n)).setExpanded(true);
            }
        }
    }

    private void pageDown(boolean bl) {
        Accordion accordion = (Accordion)this.getNode();
        if (this.focusModel.getFocusedIndex() != -1 && ((TitledPane)accordion.getPanes().get(this.focusModel.getFocusedIndex())).isFocused()) {
            this.focusModel.focusNext();
            int n = this.focusModel.getFocusedIndex();
            ((TitledPane)accordion.getPanes().get(n)).requestFocus();
            if (bl) {
                ((TitledPane)accordion.getPanes().get(n)).setExpanded(true);
            }
        }
    }

    private void moveBackward(KeyEvent keyEvent) {
        Accordion accordion = (Accordion)this.getNode();
        this.focusModel.focusPrevious();
        if (this.focusModel.getFocusedIndex() != -1) {
            int n = this.focusModel.getFocusedIndex();
            ((TitledPane)accordion.getPanes().get(n)).requestFocus();
            ((TitledPane)accordion.getPanes().get(n)).setExpanded(true);
        }
    }

    private void moveForward(KeyEvent keyEvent) {
        Accordion accordion = (Accordion)this.getNode();
        this.focusModel.focusNext();
        if (this.focusModel.getFocusedIndex() != -1) {
            int n = this.focusModel.getFocusedIndex();
            ((TitledPane)accordion.getPanes().get(n)).requestFocus();
            ((TitledPane)accordion.getPanes().get(n)).setExpanded(true);
        }
    }

    private void home(KeyEvent keyEvent) {
        Accordion accordion = (Accordion)this.getNode();
        if (this.focusModel.getFocusedIndex() != -1 && ((TitledPane)accordion.getPanes().get(this.focusModel.getFocusedIndex())).isFocused()) {
            TitledPane titledPane = (TitledPane)accordion.getPanes().get(0);
            titledPane.requestFocus();
            titledPane.setExpanded(!titledPane.isExpanded());
        }
    }

    private void end(KeyEvent keyEvent) {
        Accordion accordion = (Accordion)this.getNode();
        if (this.focusModel.getFocusedIndex() != -1 && ((TitledPane)accordion.getPanes().get(this.focusModel.getFocusedIndex())).isFocused()) {
            TitledPane titledPane = (TitledPane)accordion.getPanes().get(accordion.getPanes().size() - 1);
            titledPane.requestFocus();
            titledPane.setExpanded(!titledPane.isExpanded());
        }
    }

    public void mousePressed(MouseEvent mouseEvent) {
        Accordion accordion = (Accordion)this.getNode();
        if (accordion.getPanes().size() > 0) {
            TitledPane titledPane = (TitledPane)accordion.getPanes().get(accordion.getPanes().size() - 1);
            titledPane.requestFocus();
        } else {
            accordion.requestFocus();
        }
    }

    static class AccordionFocusModel
    extends FocusModel<TitledPane> {
        private final Accordion accordion;
        private final ChangeListener<Boolean> focusListener = new ChangeListener<Boolean>(){

            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean bl, Boolean bl2) {
                if (bl2.booleanValue()) {
                    if (accordion.getExpandedPane() != null) {
                        accordion.getExpandedPane().requestFocus();
                    } else if (!accordion.getPanes().isEmpty()) {
                        ((TitledPane)accordion.getPanes().get(0)).requestFocus();
                    }
                }
            }
        };
        private final ChangeListener<Boolean> paneFocusListener = new ChangeListener<Boolean>(){

            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean bl, Boolean bl2) {
                if (bl2.booleanValue()) {
                    ReadOnlyBooleanProperty readOnlyBooleanProperty = (ReadOnlyBooleanProperty)observableValue;
                    TitledPane titledPane = (TitledPane)readOnlyBooleanProperty.getBean();
                    this.focus(accordion.getPanes().indexOf(titledPane));
                }
            }
        };
        private final ListChangeListener<TitledPane> panesListener = change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (TitledPane titledPane : change.getAddedSubList()) {
                        titledPane.focusedProperty().addListener(this.paneFocusListener);
                    }
                    continue;
                }
                if (!change.wasRemoved()) continue;
                for (TitledPane titledPane : change.getAddedSubList()) {
                    titledPane.focusedProperty().removeListener(this.paneFocusListener);
                }
            }
        };

        public AccordionFocusModel(Accordion accordion) {
            if (accordion == null) {
                throw new IllegalArgumentException("Accordion can not be null");
            }
            this.accordion = accordion;
            this.accordion.focusedProperty().addListener(this.focusListener);
            this.accordion.getPanes().addListener(this.panesListener);
            for (TitledPane titledPane : this.accordion.getPanes()) {
                titledPane.focusedProperty().addListener(this.paneFocusListener);
            }
        }

        void dispose() {
            this.accordion.focusedProperty().removeListener(this.focusListener);
            this.accordion.getPanes().removeListener(this.panesListener);
            for (TitledPane titledPane : this.accordion.getPanes()) {
                titledPane.focusedProperty().removeListener(this.paneFocusListener);
            }
        }

        @Override
        protected int getItemCount() {
            ObservableList<TitledPane> observableList = this.accordion.getPanes();
            return observableList == null ? 0 : observableList.size();
        }

        @Override
        protected TitledPane getModelItem(int n) {
            ObservableList<TitledPane> observableList = this.accordion.getPanes();
            if (observableList == null) {
                return null;
            }
            if (n < 0) {
                return null;
            }
            return (TitledPane)observableList.get(n % observableList.size());
        }

        @Override
        public void focusPrevious() {
            if (this.getFocusedIndex() <= 0) {
                this.focus(this.accordion.getPanes().size() - 1);
            } else {
                this.focus((this.getFocusedIndex() - 1) % this.accordion.getPanes().size());
            }
        }

        @Override
        public void focusNext() {
            if (this.getFocusedIndex() == -1) {
                this.focus(0);
            } else {
                this.focus((this.getFocusedIndex() + 1) % this.accordion.getPanes().size());
            }
        }
    }
}

