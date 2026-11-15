/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.scene.control.skin.Utils;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.MultipleSelectionModelBase;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePositionBase;

class ControlUtils {
    private ControlUtils() {
    }

    public static void scrollToIndex(Control control, int n) {
        Utils.executeOnceWhenPropertyIsNonNull(control.skinProperty(), skin -> Event.fireEvent(control, new ScrollToEvent<Integer>(control, control, ScrollToEvent.scrollToTopIndex(), n)));
    }

    public static void scrollToColumn(Control control, TableColumnBase<?, ?> tableColumnBase) {
        Utils.executeOnceWhenPropertyIsNonNull(control.skinProperty(), skin -> control.fireEvent(new ScrollToEvent<TableColumnBase>(control, control, ScrollToEvent.scrollToColumn(), tableColumnBase)));
    }

    static void requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(Control control) {
        Node node;
        Scene scene = control.getScene();
        Node node2 = node = scene == null ? null : scene.getFocusOwner();
        if (node == null) {
            control.requestFocus();
        } else if (!control.equals(node)) {
            for (Parent parent = node.getParent(); parent != null; parent = parent.getParent()) {
                if (!control.equals(parent)) continue;
                control.requestFocus();
                break;
            }
        }
    }

    static <T> ListChangeListener.Change<T> buildClearAndSelectChange(ObservableList<T> observableList, final List<T> list, final int n) {
        return new ListChangeListener.Change<T>(observableList){
            private final int[] EMPTY_PERM;
            private final int removedSize;
            private final List<T> firstRemovedRange;
            private final List<T> secondRemovedRange;
            private boolean invalid;
            private boolean atFirstRange;
            private int from;
            {
                super(observableList);
                this.EMPTY_PERM = new int[0];
                this.removedSize = list.size();
                this.invalid = true;
                this.atFirstRange = true;
                this.from = -1;
                int n2 = n >= this.removedSize ? this.removedSize : (n < 0 ? 0 : n);
                this.firstRemovedRange = list.subList(0, n2);
                this.secondRemovedRange = list.subList(n2, this.removedSize);
            }

            @Override
            public int getFrom() {
                this.checkState();
                return this.from;
            }

            @Override
            public int getTo() {
                return this.getFrom();
            }

            @Override
            public List<T> getRemoved() {
                this.checkState();
                return this.atFirstRange ? this.firstRemovedRange : this.secondRemovedRange;
            }

            @Override
            public int getRemovedSize() {
                return this.atFirstRange ? this.firstRemovedRange.size() : this.secondRemovedRange.size();
            }

            @Override
            protected int[] getPermutation() {
                this.checkState();
                return this.EMPTY_PERM;
            }

            @Override
            public boolean next() {
                if (this.invalid && this.atFirstRange) {
                    this.invalid = false;
                    this.from = 0;
                    return true;
                }
                if (this.atFirstRange && !this.secondRemovedRange.isEmpty()) {
                    this.atFirstRange = false;
                    this.from = 1;
                    return true;
                }
                return false;
            }

            @Override
            public void reset() {
                this.invalid = true;
                this.atFirstRange = true;
            }

            private void checkState() {
                if (this.invalid) {
                    throw new IllegalStateException("Invalid Change state: next() must be called before inspecting the Change.");
                }
            }
        };
    }

    public static <S> void updateSelectedIndices(MultipleSelectionModelBase<S> multipleSelectionModelBase, ListChangeListener.Change<? extends TablePositionBase<?>> change) {
        multipleSelectionModelBase.selectedIndices._beginChange();
        while (change.next()) {
            multipleSelectionModelBase.startAtomic();
            List list = change.getRemoved().stream().map(TablePositionBase::getRow).distinct().peek(multipleSelectionModelBase.selectedIndices::clear).collect(Collectors.toList());
            int n = (int)change.getAddedSubList().stream().map(TablePositionBase::getRow).distinct().peek(multipleSelectionModelBase.selectedIndices::set).count();
            multipleSelectionModelBase.stopAtomic();
            int n2 = change.getFrom() + n;
            if (change.wasReplaced()) {
                multipleSelectionModelBase.selectedIndices._nextReplace(change.getFrom(), n2, list);
                continue;
            }
            if (change.wasRemoved()) {
                multipleSelectionModelBase.selectedIndices._nextRemove(change.getFrom(), list);
                continue;
            }
            if (!change.wasAdded()) continue;
            multipleSelectionModelBase.selectedIndices._nextAdd(change.getFrom(), n2);
        }
        change.reset();
        multipleSelectionModelBase.selectedIndices.reset();
        if (multipleSelectionModelBase.isAtomic()) {
            return;
        }
        if (multipleSelectionModelBase.getSelectedItems().isEmpty() && multipleSelectionModelBase.getSelectedItem() != null) {
            multipleSelectionModelBase.setSelectedItem(null);
        }
        multipleSelectionModelBase.selectedIndices._endChange();
    }

    static void reducingChange(MultipleSelectionModelBase.SelectedIndicesList selectedIndicesList, List<Integer> list) {
        if (list.isEmpty()) {
            return;
        }
        int n = 0;
        int n2 = 1;
        boolean bl = false;
        while (n2 < list.size()) {
            if (list.get(n) == list.get(n2) - 1) {
                ++n2;
                continue;
            }
            selectedIndicesList._nextRemove(selectedIndicesList.indexOf(list.get(n)), (List<? extends Integer>)list.subList(n, n2));
            n = n2;
            n2 = n + 1;
            bl = true;
        }
        if (!bl) {
            selectedIndicesList._nextRemove(selectedIndicesList.indexOf(list.get(0)), (List<? extends Integer>)list);
        }
    }
}

