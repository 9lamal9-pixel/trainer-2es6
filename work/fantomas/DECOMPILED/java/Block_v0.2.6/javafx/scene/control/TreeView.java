/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.scene.control.behavior.TreeCellBehavior;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.event.WeakEventHandler;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.ControlUtils;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.MultipleSelectionModelBase;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeUtil;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.util.Callback;

@DefaultProperty(value="root")
public class TreeView<T>
extends Control {
    private static final EventType<?> EDIT_ANY_EVENT = new EventType<Event>(Event.ANY, "TREE_VIEW_EDIT");
    private static final EventType<?> EDIT_START_EVENT = new EventType(TreeView.editAnyEvent(), "EDIT_START");
    private static final EventType<?> EDIT_CANCEL_EVENT = new EventType(TreeView.editAnyEvent(), "EDIT_CANCEL");
    private static final EventType<?> EDIT_COMMIT_EVENT = new EventType(TreeView.editAnyEvent(), "EDIT_COMMIT");
    private boolean expandedItemCountDirty = true;
    private Map<Integer, SoftReference<TreeItem<T>>> treeItemCacheMap = new HashMap<Integer, SoftReference<TreeItem<T>>>();
    private final EventHandler<TreeItem.TreeModificationEvent<T>> rootEvent = treeModificationEvent -> {
        boolean bl = false;
        for (EventType<? extends Event> eventType = treeModificationEvent.getEventType(); eventType != null; eventType = eventType.getSuperType()) {
            if (!eventType.equals(TreeItem.expandedItemCountChangeEvent())) continue;
            bl = true;
            break;
        }
        if (bl) {
            this.expandedItemCountDirty = true;
            this.requestLayout();
        }
    };
    private WeakEventHandler<TreeItem.TreeModificationEvent<T>> weakRootEventListener;
    private ObjectProperty<Callback<TreeView<T>, TreeCell<T>>> cellFactory;
    private ObjectProperty<TreeItem<T>> root = new SimpleObjectProperty<TreeItem<T>>(this, "root"){
        private WeakReference<TreeItem<T>> weakOldItem;

        @Override
        protected void invalidated() {
            TreeItem treeItem;
            TreeItem treeItem2;
            TreeItem treeItem3 = treeItem2 = this.weakOldItem == null ? null : (TreeItem)this.weakOldItem.get();
            if (treeItem2 != null && TreeView.this.weakRootEventListener != null) {
                treeItem2.removeEventHandler(TreeItem.treeNotificationEvent(), TreeView.this.weakRootEventListener);
            }
            if ((treeItem = TreeView.this.getRoot()) != null) {
                TreeView.this.weakRootEventListener = new WeakEventHandler(TreeView.this.rootEvent);
                TreeView.this.getRoot().addEventHandler(TreeItem.treeNotificationEvent(), TreeView.this.weakRootEventListener);
                this.weakOldItem = new WeakReference(treeItem);
            }
            TreeView.this.edit(null);
            TreeView.this.expandedItemCountDirty = true;
            TreeView.this.updateRootExpanded();
        }
    };
    private BooleanProperty showRoot;
    private ObjectProperty<MultipleSelectionModel<TreeItem<T>>> selectionModel;
    private ObjectProperty<FocusModel<TreeItem<T>>> focusModel;
    private ReadOnlyIntegerWrapper expandedItemCount = new ReadOnlyIntegerWrapper(this, "expandedItemCount", 0);
    private DoubleProperty fixedCellSize;
    private BooleanProperty editable;
    private ReadOnlyObjectWrapper<TreeItem<T>> editingItem;
    private ObjectProperty<EventHandler<EditEvent<T>>> onEditStart;
    private ObjectProperty<EventHandler<EditEvent<T>>> onEditCommit;
    private ObjectProperty<EventHandler<EditEvent<T>>> onEditCancel;
    private ObjectProperty<EventHandler<ScrollToEvent<Integer>>> onScrollTo;
    private static final String DEFAULT_STYLE_CLASS = "tree-view";

    public static <T> EventType<EditEvent<T>> editAnyEvent() {
        return EDIT_ANY_EVENT;
    }

    public static <T> EventType<EditEvent<T>> editStartEvent() {
        return EDIT_START_EVENT;
    }

    public static <T> EventType<EditEvent<T>> editCancelEvent() {
        return EDIT_CANCEL_EVENT;
    }

    public static <T> EventType<EditEvent<T>> editCommitEvent() {
        return EDIT_COMMIT_EVENT;
    }

    @Deprecated(since="8u20")
    public static int getNodeLevel(TreeItem<?> treeItem) {
        if (treeItem == null) {
            return -1;
        }
        int n = 0;
        for (TreeItem<?> treeItem2 = treeItem.getParent(); treeItem2 != null; treeItem2 = treeItem2.getParent()) {
            ++n;
        }
        return n;
    }

    public TreeView() {
        this(null);
    }

    public TreeView(TreeItem<T> treeItem) {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.TREE_VIEW);
        this.setRoot(treeItem);
        this.updateExpandedItemCount(treeItem);
        TreeViewBitSetSelectionModel treeViewBitSetSelectionModel = new TreeViewBitSetSelectionModel(this);
        this.setSelectionModel(treeViewBitSetSelectionModel);
        this.setFocusModel(new TreeViewFocusModel(this));
    }

    public final void setCellFactory(Callback<TreeView<T>, TreeCell<T>> callback) {
        this.cellFactoryProperty().set(callback);
    }

    public final Callback<TreeView<T>, TreeCell<T>> getCellFactory() {
        return this.cellFactory == null ? null : (Callback)this.cellFactory.get();
    }

    public final ObjectProperty<Callback<TreeView<T>, TreeCell<T>>> cellFactoryProperty() {
        if (this.cellFactory == null) {
            this.cellFactory = new SimpleObjectProperty<Callback<TreeView<T>, TreeCell<T>>>(this, "cellFactory");
        }
        return this.cellFactory;
    }

    public final void setRoot(TreeItem<T> treeItem) {
        this.rootProperty().set(treeItem);
    }

    public final TreeItem<T> getRoot() {
        return this.root == null ? null : (TreeItem)this.root.get();
    }

    public final ObjectProperty<TreeItem<T>> rootProperty() {
        return this.root;
    }

    public final void setShowRoot(boolean bl) {
        this.showRootProperty().set(bl);
    }

    public final boolean isShowRoot() {
        return this.showRoot == null ? true : this.showRoot.get();
    }

    public final BooleanProperty showRootProperty() {
        if (this.showRoot == null) {
            this.showRoot = new SimpleBooleanProperty(this, "showRoot", true){

                @Override
                protected void invalidated() {
                    TreeView.this.updateRootExpanded();
                    TreeView.this.updateExpandedItemCount(TreeView.this.getRoot());
                }
            };
        }
        return this.showRoot;
    }

    public final void setSelectionModel(MultipleSelectionModel<TreeItem<T>> multipleSelectionModel) {
        this.selectionModelProperty().set(multipleSelectionModel);
    }

    public final MultipleSelectionModel<TreeItem<T>> getSelectionModel() {
        return this.selectionModel == null ? null : (MultipleSelectionModel)this.selectionModel.get();
    }

    public final ObjectProperty<MultipleSelectionModel<TreeItem<T>>> selectionModelProperty() {
        if (this.selectionModel == null) {
            this.selectionModel = new SimpleObjectProperty<MultipleSelectionModel<TreeItem<T>>>(this, "selectionModel");
        }
        return this.selectionModel;
    }

    public final void setFocusModel(FocusModel<TreeItem<T>> focusModel) {
        this.focusModelProperty().set(focusModel);
    }

    public final FocusModel<TreeItem<T>> getFocusModel() {
        return this.focusModel == null ? null : (FocusModel)this.focusModel.get();
    }

    public final ObjectProperty<FocusModel<TreeItem<T>>> focusModelProperty() {
        if (this.focusModel == null) {
            this.focusModel = new SimpleObjectProperty<FocusModel<TreeItem<T>>>(this, "focusModel");
        }
        return this.focusModel;
    }

    public final ReadOnlyIntegerProperty expandedItemCountProperty() {
        return this.expandedItemCount.getReadOnlyProperty();
    }

    private void setExpandedItemCount(int n) {
        this.expandedItemCount.set(n);
    }

    public final int getExpandedItemCount() {
        if (this.expandedItemCountDirty) {
            this.updateExpandedItemCount(this.getRoot());
        }
        return this.expandedItemCount.get();
    }

    public final void setFixedCellSize(double d) {
        this.fixedCellSizeProperty().set(d);
    }

    public final double getFixedCellSize() {
        return this.fixedCellSize == null ? -1.0 : this.fixedCellSize.get();
    }

    public final DoubleProperty fixedCellSizeProperty() {
        if (this.fixedCellSize == null) {
            this.fixedCellSize = new StyleableDoubleProperty(-1.0){

                @Override
                public CssMetaData<TreeView<?>, Number> getCssMetaData() {
                    return StyleableProperties.FIXED_CELL_SIZE;
                }

                @Override
                public Object getBean() {
                    return TreeView.this;
                }

                @Override
                public String getName() {
                    return "fixedCellSize";
                }
            };
        }
        return this.fixedCellSize;
    }

    public final void setEditable(boolean bl) {
        this.editableProperty().set(bl);
    }

    public final boolean isEditable() {
        return this.editable == null ? false : this.editable.get();
    }

    public final BooleanProperty editableProperty() {
        if (this.editable == null) {
            this.editable = new SimpleBooleanProperty(this, "editable", false);
        }
        return this.editable;
    }

    private void setEditingItem(TreeItem<T> treeItem) {
        this.editingItemPropertyImpl().set(treeItem);
    }

    public final TreeItem<T> getEditingItem() {
        return this.editingItem == null ? null : (TreeItem)this.editingItem.get();
    }

    public final ReadOnlyObjectProperty<TreeItem<T>> editingItemProperty() {
        return this.editingItemPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<TreeItem<T>> editingItemPropertyImpl() {
        if (this.editingItem == null) {
            this.editingItem = new ReadOnlyObjectWrapper(this, "editingItem");
        }
        return this.editingItem;
    }

    public final void setOnEditStart(EventHandler<EditEvent<T>> eventHandler) {
        this.onEditStartProperty().set(eventHandler);
    }

    public final EventHandler<EditEvent<T>> getOnEditStart() {
        return this.onEditStart == null ? null : (EventHandler)this.onEditStart.get();
    }

    public final ObjectProperty<EventHandler<EditEvent<T>>> onEditStartProperty() {
        if (this.onEditStart == null) {
            this.onEditStart = new SimpleObjectProperty<EventHandler<EditEvent<T>>>(this, "onEditStart"){

                @Override
                protected void invalidated() {
                    TreeView.this.setEventHandler(TreeView.editStartEvent(), (EventHandler)this.get());
                }
            };
        }
        return this.onEditStart;
    }

    public final void setOnEditCommit(EventHandler<EditEvent<T>> eventHandler) {
        this.onEditCommitProperty().set(eventHandler);
    }

    public final EventHandler<EditEvent<T>> getOnEditCommit() {
        return this.onEditCommit == null ? null : (EventHandler)this.onEditCommit.get();
    }

    public final ObjectProperty<EventHandler<EditEvent<T>>> onEditCommitProperty() {
        if (this.onEditCommit == null) {
            this.onEditCommit = new SimpleObjectProperty<EventHandler<EditEvent<T>>>(this, "onEditCommit"){

                @Override
                protected void invalidated() {
                    TreeView.this.setEventHandler(TreeView.editCommitEvent(), (EventHandler)this.get());
                }
            };
        }
        return this.onEditCommit;
    }

    public final void setOnEditCancel(EventHandler<EditEvent<T>> eventHandler) {
        this.onEditCancelProperty().set(eventHandler);
    }

    public final EventHandler<EditEvent<T>> getOnEditCancel() {
        return this.onEditCancel == null ? null : (EventHandler)this.onEditCancel.get();
    }

    public final ObjectProperty<EventHandler<EditEvent<T>>> onEditCancelProperty() {
        if (this.onEditCancel == null) {
            this.onEditCancel = new SimpleObjectProperty<EventHandler<EditEvent<T>>>(this, "onEditCancel"){

                @Override
                protected void invalidated() {
                    TreeView.this.setEventHandler(TreeView.editCancelEvent(), (EventHandler)this.get());
                }
            };
        }
        return this.onEditCancel;
    }

    @Override
    protected void layoutChildren() {
        if (this.expandedItemCountDirty) {
            this.updateExpandedItemCount(this.getRoot());
        }
        super.layoutChildren();
    }

    public void edit(TreeItem<T> treeItem) {
        if (!this.isEditable()) {
            return;
        }
        this.setEditingItem(treeItem);
    }

    public void scrollTo(int n) {
        ControlUtils.scrollToIndex(this, n);
    }

    public void setOnScrollTo(EventHandler<ScrollToEvent<Integer>> eventHandler) {
        this.onScrollToProperty().set(eventHandler);
    }

    public EventHandler<ScrollToEvent<Integer>> getOnScrollTo() {
        if (this.onScrollTo != null) {
            return (EventHandler)this.onScrollTo.get();
        }
        return null;
    }

    public ObjectProperty<EventHandler<ScrollToEvent<Integer>>> onScrollToProperty() {
        if (this.onScrollTo == null) {
            this.onScrollTo = new ObjectPropertyBase<EventHandler<ScrollToEvent<Integer>>>(){

                @Override
                protected void invalidated() {
                    TreeView.this.setEventHandler(ScrollToEvent.scrollToTopIndex(), (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return TreeView.this;
                }

                @Override
                public String getName() {
                    return "onScrollTo";
                }
            };
        }
        return this.onScrollTo;
    }

    public int getRow(TreeItem<T> treeItem) {
        return TreeUtil.getRow(treeItem, this.getRoot(), this.expandedItemCountDirty, this.isShowRoot());
    }

    public TreeItem<T> getTreeItem(int n) {
        Object object;
        TreeItem treeItem;
        int n2;
        if (n < 0) {
            return null;
        }
        int n3 = n2 = this.isShowRoot() ? n : n + 1;
        if (this.expandedItemCountDirty) {
            this.updateExpandedItemCount(this.getRoot());
        } else if (this.treeItemCacheMap.containsKey(n2) && (treeItem = (TreeItem)((SoftReference)(object = this.treeItemCacheMap.get(n2))).get()) != null) {
            return treeItem;
        }
        object = TreeUtil.getItem(this.getRoot(), n2, this.expandedItemCountDirty);
        this.treeItemCacheMap.put(n2, new SoftReference<Object>(object));
        return object;
    }

    public int getTreeItemLevel(TreeItem<?> treeItem) {
        TreeItem<T> treeItem2 = this.getRoot();
        if (treeItem == null) {
            return -1;
        }
        if (treeItem == treeItem2) {
            return 0;
        }
        int n = 0;
        for (TreeItem<?> treeItem3 = treeItem.getParent(); treeItem3 != null; treeItem3 = treeItem3.getParent()) {
            ++n;
            if (treeItem3 == treeItem2) break;
        }
        return n;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TreeViewSkin(this);
    }

    public void refresh() {
        this.getProperties().put("recreateKey", Boolean.TRUE);
    }

    private void updateExpandedItemCount(TreeItem<T> treeItem) {
        this.setExpandedItemCount(TreeUtil.updateExpandedItemCount(treeItem, this.expandedItemCountDirty, this.isShowRoot()));
        if (this.expandedItemCountDirty) {
            this.treeItemCacheMap.clear();
        }
        this.expandedItemCountDirty = false;
    }

    private void updateRootExpanded() {
        if (!this.isShowRoot() && this.getRoot() != null && !this.getRoot().isExpanded()) {
            this.getRoot().setExpanded(true);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return TreeView.getClassCssMetaData();
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case MULTIPLE_SELECTION: {
                MultipleSelectionModel<TreeItem<T>> multipleSelectionModel = this.getSelectionModel();
                return multipleSelectionModel != null && multipleSelectionModel.getSelectionMode() == SelectionMode.MULTIPLE;
            }
            case ROW_COUNT: {
                return this.getExpandedItemCount();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    static /* synthetic */ EventType access$1200() {
        return EDIT_ANY_EVENT;
    }

    static class TreeViewFocusModel<T>
    extends FocusModel<TreeItem<T>> {
        private final TreeView<T> treeView;
        private final ChangeListener<TreeItem<T>> rootPropertyListener = (observableValue, treeItem, treeItem2) -> this.updateTreeEventListener((TreeItem<T>)treeItem, (TreeItem<T>)treeItem2);
        private final WeakChangeListener<TreeItem<T>> weakRootPropertyListener = new WeakChangeListener<TreeItem<TreeItem<T>>>(this.rootPropertyListener);
        private EventHandler<TreeItem.TreeModificationEvent<T>> treeItemListener = new EventHandler<TreeItem.TreeModificationEvent<T>>(){

            @Override
            public void handle(TreeItem.TreeModificationEvent<T> treeModificationEvent) {
                int n;
                if (this.getFocusedIndex() == -1) {
                    return;
                }
                int n2 = treeView.getRow(treeModificationEvent.getTreeItem());
                int n3 = 0;
                if (treeModificationEvent.getChange() != null) {
                    treeModificationEvent.getChange().next();
                }
                do {
                    if (treeModificationEvent.wasExpanded()) {
                        if (n2 >= this.getFocusedIndex()) continue;
                        n3 += treeModificationEvent.getTreeItem().getExpandedDescendentCount(false) - 1;
                        continue;
                    }
                    if (treeModificationEvent.wasCollapsed()) {
                        if (n2 >= this.getFocusedIndex()) continue;
                        n3 += -treeModificationEvent.getTreeItem().previousExpandedDescendentCount + 1;
                        continue;
                    }
                    if (treeModificationEvent.wasAdded()) {
                        TreeItem treeItem = treeModificationEvent.getTreeItem();
                        if (!treeItem.isExpanded()) continue;
                        for (int i = 0; i < treeModificationEvent.getAddedChildren().size(); ++i) {
                            TreeItem treeItem2 = treeModificationEvent.getAddedChildren().get(i);
                            n2 = treeView.getRow(treeItem2);
                            if (treeItem2 == null || n2 > n3 + this.getFocusedIndex()) continue;
                            n3 += treeItem2.getExpandedDescendentCount(false);
                        }
                    } else {
                        if (!treeModificationEvent.wasRemoved()) continue;
                        n2 += treeModificationEvent.getFrom() + 1;
                        for (int i = 0; i < treeModificationEvent.getRemovedChildren().size(); ++i) {
                            TreeItem treeItem = treeModificationEvent.getRemovedChildren().get(i);
                            if (treeItem == null || !treeItem.equals(this.getFocusedItem())) continue;
                            this.focus(Math.max(0, this.getFocusedIndex() - 1));
                            return;
                        }
                        if (n2 > this.getFocusedIndex()) continue;
                        n3 += treeModificationEvent.getTreeItem().isExpanded() ? -treeModificationEvent.getRemovedSize() : 0;
                    }
                } while (treeModificationEvent.getChange() != null && treeModificationEvent.getChange().next());
                if (n3 != 0 && (n = this.getFocusedIndex() + n3) >= 0) {
                    Platform.runLater(() -> this.focus(n));
                }
            }
        };
        private WeakEventHandler<TreeItem.TreeModificationEvent<T>> weakTreeItemListener;

        public TreeViewFocusModel(TreeView<T> treeView) {
            this.treeView = treeView;
            this.treeView.rootProperty().addListener(this.weakRootPropertyListener);
            this.updateTreeEventListener(null, treeView.getRoot());
            if (treeView.getExpandedItemCount() > 0) {
                this.focus(0);
            }
            treeView.showRootProperty().addListener(observable -> {
                if (this.isFocused(0)) {
                    this.focus(-1);
                    this.focus(0);
                }
            });
            this.focusedIndexProperty().addListener(observable -> treeView.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_ITEM));
        }

        private void updateTreeEventListener(TreeItem<T> treeItem, TreeItem<T> treeItem2) {
            if (treeItem != null && this.weakTreeItemListener != null) {
                treeItem.removeEventHandler(TreeItem.expandedItemCountChangeEvent(), this.weakTreeItemListener);
            }
            if (treeItem2 != null) {
                this.weakTreeItemListener = new WeakEventHandler<TreeItem.TreeModificationEvent<TreeItem.TreeModificationEvent<T>>>(this.treeItemListener);
                treeItem2.addEventHandler(TreeItem.expandedItemCountChangeEvent(), this.weakTreeItemListener);
            }
        }

        @Override
        protected int getItemCount() {
            return this.treeView == null ? -1 : this.treeView.getExpandedItemCount();
        }

        @Override
        protected TreeItem<T> getModelItem(int n) {
            if (this.treeView == null) {
                return null;
            }
            if (n < 0 || n >= this.treeView.getExpandedItemCount()) {
                return null;
            }
            return this.treeView.getTreeItem(n);
        }

        @Override
        public void focus(int n) {
            if (((TreeView)this.treeView).expandedItemCountDirty) {
                ((TreeView)this.treeView).updateExpandedItemCount(this.treeView.getRoot());
            }
            super.focus(n);
        }
    }

    static class TreeViewBitSetSelectionModel<T>
    extends MultipleSelectionModelBase<TreeItem<T>> {
        private TreeView<T> treeView = null;
        private ChangeListener<TreeItem<T>> rootPropertyListener = (observableValue, treeItem, treeItem2) -> {
            this.updateDefaultSelection();
            this.updateTreeEventListener((TreeItem<T>)treeItem, (TreeItem<T>)treeItem2);
        };
        private EventHandler<TreeItem.TreeModificationEvent<T>> treeItemListener = treeModificationEvent -> {
            Integer n;
            if (this.getSelectedIndex() == -1 && this.getSelectedItem() == null) {
                return;
            }
            TreeItem treeItem = treeModificationEvent.getTreeItem();
            if (treeItem == null) {
                return;
            }
            ((TreeView)this.treeView).expandedItemCountDirty = true;
            int n2 = this.treeView.getRow(treeItem);
            int n3 = 0;
            ListChangeListener.Change change = treeModificationEvent.getChange();
            if (change != null) {
                change.next();
            }
            do {
                int n4;
                int n5;
                int n6;
                int n7;
                int n8 = change == null ? 0 : change.getAddedSize();
                int n9 = n7 = change == null ? 0 : change.getRemovedSize();
                if (treeModificationEvent.wasExpanded()) {
                    n3 += treeItem.getExpandedDescendentCount(false) - 1;
                    ++n2;
                    continue;
                }
                if (treeModificationEvent.wasCollapsed()) {
                    treeItem.getExpandedDescendentCount(false);
                    int n10 = treeItem.previousExpandedDescendentCount;
                    n6 = this.getSelectedIndex();
                    boolean bl = n6 >= n2 + 1 && n6 < n2 + n10;
                    boolean bl2 = false;
                    this.selectedIndices._beginChange();
                    int n11 = n2 + 1;
                    n5 = n2 + n10;
                    ArrayList<Integer> arrayList = new ArrayList<Integer>();
                    for (int i = n11; i < n5; ++i) {
                        if (!this.isSelected(i)) continue;
                        bl2 = true;
                        arrayList.add(i);
                    }
                    ControlUtils.reducingChange(this.selectedIndices, arrayList);
                    Iterator iterator = arrayList.iterator();
                    while (iterator.hasNext()) {
                        int n12 = (Integer)iterator.next();
                        this.startAtomic();
                        this.clearSelection(n12);
                        this.stopAtomic();
                    }
                    this.selectedIndices._endChange();
                    if (bl && bl2) {
                        this.select(n2);
                    }
                    n3 += -n10 + 1;
                    ++n2;
                    continue;
                }
                if (treeModificationEvent.wasPermutated()) continue;
                if (treeModificationEvent.wasAdded()) {
                    n3 += treeItem.isExpanded() ? n8 : 0;
                    n2 = this.treeView.getRow(treeModificationEvent.getChange().getAddedSubList().get(0));
                    continue;
                }
                if (!treeModificationEvent.wasRemoved()) continue;
                n3 += treeItem.isExpanded() ? -n7 : 0;
                n2 += treeModificationEvent.getFrom() + 1;
                ObservableList<Integer> observableList = this.getSelectedIndices();
                n6 = this.getSelectedIndex();
                ObservableList observableList2 = this.getSelectedItems();
                TreeItem treeItem2 = (TreeItem)this.getSelectedItem();
                List list = treeModificationEvent.getChange().getRemoved();
                for (n5 = 0; n5 < observableList.size() && !observableList2.isEmpty() && (n4 = ((Integer)observableList.get(n5)).intValue()) <= observableList2.size(); ++n5) {
                    int n13;
                    Object object;
                    if (list.size() != 1 || observableList2.size() != 1 || treeItem2 == null || !treeItem2.equals(list.get(0)) || n6 >= this.getItemCount() || treeItem2.equals(object = this.getModelItem(n13 = n6 == 0 ? 0 : n6 - 1))) continue;
                    this.select((TreeItem<T>)object);
                }
            } while (treeModificationEvent.getChange() != null && treeModificationEvent.getChange().next());
            this.shiftSelection(n2, n3, null);
            if ((treeModificationEvent.wasAdded() || treeModificationEvent.wasRemoved()) && (n = (Integer)TreeCellBehavior.getAnchor(this.treeView, null)) != null && this.isSelected(n + n3)) {
                TreeCellBehavior.setAnchor(this.treeView, n + n3, false);
            }
        };
        private WeakChangeListener<TreeItem<T>> weakRootPropertyListener = new WeakChangeListener<TreeItem<TreeItem<T>>>(this.rootPropertyListener);
        private WeakEventHandler<TreeItem.TreeModificationEvent<T>> weakTreeItemListener;

        public TreeViewBitSetSelectionModel(TreeView<T> treeView) {
            if (treeView == null) {
                throw new IllegalArgumentException("TreeView can not be null");
            }
            this.treeView = treeView;
            this.treeView.rootProperty().addListener(this.weakRootPropertyListener);
            this.treeView.showRootProperty().addListener(observable -> this.shiftSelection(0, treeView.isShowRoot() ? 1 : -1, null));
            this.updateTreeEventListener(null, treeView.getRoot());
            this.updateDefaultSelection();
        }

        private void updateTreeEventListener(TreeItem<T> treeItem, TreeItem<T> treeItem2) {
            if (treeItem != null && this.weakTreeItemListener != null) {
                treeItem.removeEventHandler(TreeItem.expandedItemCountChangeEvent(), this.weakTreeItemListener);
            }
            if (treeItem2 != null) {
                this.weakTreeItemListener = new WeakEventHandler<TreeItem.TreeModificationEvent<TreeItem.TreeModificationEvent<T>>>(this.treeItemListener);
                treeItem2.addEventHandler(TreeItem.expandedItemCountChangeEvent(), this.weakTreeItemListener);
            }
        }

        @Override
        public void selectAll() {
            int n = TreeCellBehavior.getAnchor(this.treeView, -1);
            super.selectAll();
            TreeCellBehavior.setAnchor(this.treeView, n, false);
        }

        @Override
        public void select(TreeItem<T> treeItem) {
            if (treeItem == null && this.getSelectionMode() == SelectionMode.SINGLE) {
                this.clearSelection();
                return;
            }
            if (treeItem != null) {
                for (TreeItem<T> treeItem2 = treeItem.getParent(); treeItem2 != null; treeItem2 = treeItem2.getParent()) {
                    treeItem2.setExpanded(true);
                }
            }
            ((TreeView)this.treeView).updateExpandedItemCount(this.treeView.getRoot());
            int n = this.treeView.getRow(treeItem);
            if (n == -1) {
                this.setSelectedIndex(-1);
                this.setSelectedItem(treeItem);
            } else {
                this.select(n);
            }
        }

        @Override
        public void clearAndSelect(int n) {
            TreeCellBehavior.setAnchor(this.treeView, n, false);
            super.clearAndSelect(n);
        }

        @Override
        protected void focus(int n) {
            if (this.treeView.getFocusModel() != null) {
                this.treeView.getFocusModel().focus(n);
            }
            this.treeView.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_ITEM);
        }

        @Override
        protected int getFocusedIndex() {
            if (this.treeView.getFocusModel() == null) {
                return -1;
            }
            return this.treeView.getFocusModel().getFocusedIndex();
        }

        @Override
        protected int getItemCount() {
            return this.treeView == null ? 0 : this.treeView.getExpandedItemCount();
        }

        @Override
        public TreeItem<T> getModelItem(int n) {
            if (this.treeView == null) {
                return null;
            }
            if (n < 0 || n >= this.treeView.getExpandedItemCount()) {
                return null;
            }
            return this.treeView.getTreeItem(n);
        }

        private void updateDefaultSelection() {
            this.clearSelection();
            this.focus(this.getItemCount() > 0 ? 0 : -1);
        }
    }

    public static class EditEvent<T>
    extends Event {
        private static final long serialVersionUID = -4437033058917528976L;
        public static final EventType<?> ANY = TreeView.access$1200();
        private final TreeView<T> source;
        private final T oldValue;
        private final T newValue;
        private final transient TreeItem<T> treeItem;

        public EditEvent(TreeView<T> treeView, EventType<? extends EditEvent> eventType, TreeItem<T> treeItem, T t, T t2) {
            super(treeView, Event.NULL_SOURCE_TARGET, eventType);
            this.source = treeView;
            this.oldValue = t;
            this.newValue = t2;
            this.treeItem = treeItem;
        }

        @Override
        public TreeView<T> getSource() {
            return this.source;
        }

        public TreeItem<T> getTreeItem() {
            return this.treeItem;
        }

        public T getNewValue() {
            return this.newValue;
        }

        public T getOldValue() {
            return this.oldValue;
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<TreeView<?>, Number> FIXED_CELL_SIZE = new CssMetaData<TreeView<?>, Number>("-fx-fixed-cell-size", SizeConverter.getInstance(), -1.0){

            @Override
            public Double getInitialValue(TreeView<?> treeView) {
                return treeView.getFixedCellSize();
            }

            @Override
            public boolean isSettable(TreeView<?> treeView) {
                return ((TreeView)treeView).fixedCellSize == null || !((TreeView)treeView).fixedCellSize.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(TreeView<?> treeView) {
                return (StyleableProperty)((Object)treeView.fixedCellSizeProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
            arrayList.add(FIXED_CELL_SIZE);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

