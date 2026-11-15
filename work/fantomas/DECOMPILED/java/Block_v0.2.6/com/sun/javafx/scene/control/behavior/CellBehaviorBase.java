/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;
import java.util.ArrayList;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public abstract class CellBehaviorBase<T extends Cell>
extends BehaviorBase<T> {
    private static final String ANCHOR_PROPERTY_KEY = "anchor";
    private static final String IS_DEFAULT_ANCHOR_KEY = "isDefaultAnchor";
    private final InputMap<T> cellInputMap = this.createInputMap();
    private boolean latePress = false;

    public static <T> T getAnchor(Control control, T t) {
        return (T)(CellBehaviorBase.hasNonDefaultAnchor(control) ? control.getProperties().get(ANCHOR_PROPERTY_KEY) : t);
    }

    public static <T> void setAnchor(Control control, T t, boolean bl) {
        if (control == null) {
            return;
        }
        if (t == null) {
            CellBehaviorBase.removeAnchor(control);
        } else {
            control.getProperties().put(ANCHOR_PROPERTY_KEY, t);
            control.getProperties().put(IS_DEFAULT_ANCHOR_KEY, bl);
        }
    }

    public static boolean hasNonDefaultAnchor(Control control) {
        Boolean bl = (Boolean)control.getProperties().remove(IS_DEFAULT_ANCHOR_KEY);
        return (bl == null || bl == false) && CellBehaviorBase.hasAnchor(control);
    }

    public static boolean hasDefaultAnchor(Control control) {
        Boolean bl = (Boolean)control.getProperties().remove(IS_DEFAULT_ANCHOR_KEY);
        return bl != null && bl == true && CellBehaviorBase.hasAnchor(control);
    }

    private static boolean hasAnchor(Control control) {
        return control.getProperties().get(ANCHOR_PROPERTY_KEY) != null;
    }

    public static void removeAnchor(Control control) {
        control.getProperties().remove(ANCHOR_PROPERTY_KEY);
        control.getProperties().remove(IS_DEFAULT_ANCHOR_KEY);
    }

    public CellBehaviorBase(T t) {
        super(t);
        InputMap.Mapping[] mappingArray = new InputMap.Mapping[3];
        InputMap.MouseMapping mouseMapping = new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        mappingArray[0] = mouseMapping;
        InputMap.MouseMapping mouseMapping2 = new InputMap.MouseMapping(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        mappingArray[1] = mouseMapping2;
        InputMap.MouseMapping mouseMapping3 = new InputMap.MouseMapping(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
        mappingArray[2] = mouseMapping3;
        this.addDefaultMapping(mappingArray);
        mouseMapping.setAutoConsume(false);
        mouseMapping2.setAutoConsume(false);
        mouseMapping3.setAutoConsume(false);
    }

    protected abstract Control getCellContainer();

    protected abstract MultipleSelectionModel<?> getSelectionModel();

    protected abstract FocusModel<?> getFocusModel();

    protected abstract void edit(T var1);

    protected boolean handleDisclosureNode(double d, double d2) {
        return false;
    }

    protected boolean isClickPositionValid(double d, double d2) {
        return true;
    }

    @Override
    public InputMap<T> getInputMap() {
        return this.cellInputMap;
    }

    protected int getIndex() {
        return this.getNode() instanceof IndexedCell ? ((IndexedCell)this.getNode()).getIndex() : -1;
    }

    public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.isSynthesized()) {
            this.latePress = true;
        } else {
            this.latePress = this.isSelected();
            if (!this.latePress) {
                this.doSelect(mouseEvent.getX(), mouseEvent.getY(), mouseEvent.getButton(), mouseEvent.getClickCount(), mouseEvent.isShiftDown(), mouseEvent.isShortcutDown());
            }
        }
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        if (this.latePress) {
            this.latePress = false;
            this.doSelect(mouseEvent.getX(), mouseEvent.getY(), mouseEvent.getButton(), mouseEvent.getClickCount(), mouseEvent.isShiftDown(), mouseEvent.isShortcutDown());
        }
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        this.latePress = false;
    }

    protected void doSelect(double d, double d2, MouseButton mouseButton, int n, boolean bl, boolean bl2) {
        Cell cell = (Cell)this.getNode();
        Control control = this.getCellContainer();
        if (cell.isEmpty() || !cell.contains(d, d2)) {
            return;
        }
        int n2 = this.getIndex();
        boolean bl3 = cell.isSelected();
        MultipleSelectionModel<?> multipleSelectionModel = this.getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        FocusModel<?> focusModel = this.getFocusModel();
        if (focusModel == null) {
            return;
        }
        if (this.handleDisclosureNode(d, d2)) {
            return;
        }
        if (!this.isClickPositionValid(d, d2)) {
            return;
        }
        if (bl) {
            if (!CellBehaviorBase.hasNonDefaultAnchor(control)) {
                CellBehaviorBase.setAnchor(control, focusModel.getFocusedIndex(), false);
            }
        } else {
            CellBehaviorBase.removeAnchor(control);
        }
        if (mouseButton == MouseButton.PRIMARY || mouseButton == MouseButton.SECONDARY && !bl3) {
            if (multipleSelectionModel.getSelectionMode() == SelectionMode.SINGLE) {
                this.simpleSelect(mouseButton, n, bl2);
            } else if (bl2) {
                if (bl3) {
                    multipleSelectionModel.clearSelection(n2);
                    focusModel.focus(n2);
                } else {
                    multipleSelectionModel.select(n2);
                }
            } else if (bl && n == 1) {
                int n3 = CellBehaviorBase.getAnchor(control, focusModel.getFocusedIndex());
                this.selectRows(n3, n2);
                focusModel.focus(n2);
            } else {
                this.simpleSelect(mouseButton, n, bl2);
            }
        }
    }

    protected void simpleSelect(MouseButton mouseButton, int n, boolean bl) {
        int n2 = this.getIndex();
        MultipleSelectionModel<?> multipleSelectionModel = this.getSelectionModel();
        boolean bl2 = multipleSelectionModel.isSelected(n2);
        if (bl2 && bl) {
            multipleSelectionModel.clearSelection(n2);
            this.getFocusModel().focus(n2);
            bl2 = false;
        } else {
            multipleSelectionModel.clearAndSelect(n2);
        }
        this.handleClicks(mouseButton, n, bl2);
    }

    protected void handleClicks(MouseButton mouseButton, int n, boolean bl) {
        if (mouseButton == MouseButton.PRIMARY) {
            if (n == 1 && bl) {
                this.edit((Cell)this.getNode());
            } else if (n == 1) {
                this.edit(null);
            } else if (n == 2 && ((Cell)this.getNode()).isEditable()) {
                this.edit((Cell)this.getNode());
            }
        }
    }

    void selectRows(int n, int n2) {
        boolean bl = n < n2;
        int n3 = Math.min(n, n2);
        int n4 = Math.max(n, n2);
        ArrayList<Integer> arrayList = new ArrayList<Integer>(this.getSelectionModel().getSelectedIndices());
        int n5 = arrayList.size();
        for (int i = 0; i < n5; ++i) {
            int n6 = (Integer)arrayList.get(i);
            if (n6 >= n3 && n6 <= n4) continue;
            this.getSelectionModel().clearSelection(n6);
        }
        if (n3 == n4) {
            this.getSelectionModel().select(n3);
        } else if (bl) {
            this.getSelectionModel().selectRange(n3, n4 + 1);
        } else {
            this.getSelectionModel().selectRange(n4, n3 - 1);
        }
    }

    protected boolean isSelected() {
        return ((Cell)this.getNode()).isSelected();
    }
}

