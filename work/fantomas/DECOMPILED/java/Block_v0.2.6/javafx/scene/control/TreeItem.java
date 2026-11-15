/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.event.EventHandlerManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.TreeSortMode;

public class TreeItem<T>
implements EventTarget {
    private static final EventType<?> TREE_NOTIFICATION_EVENT = new EventType<Event>(Event.ANY, "TreeNotificationEvent");
    private static final EventType<?> EXPANDED_ITEM_COUNT_CHANGE_EVENT = new EventType(TreeItem.treeNotificationEvent(), "ExpandedItemCountChangeEvent");
    private static final EventType<?> BRANCH_EXPANDED_EVENT = new EventType(TreeItem.expandedItemCountChangeEvent(), "BranchExpandedEvent");
    private static final EventType<?> BRANCH_COLLAPSED_EVENT = new EventType(TreeItem.expandedItemCountChangeEvent(), "BranchCollapsedEvent");
    private static final EventType<?> CHILDREN_MODIFICATION_EVENT = new EventType(TreeItem.expandedItemCountChangeEvent(), "ChildrenModificationEvent");
    private static final EventType<?> VALUE_CHANGED_EVENT = new EventType(TreeItem.treeNotificationEvent(), "ValueChangedEvent");
    private static final EventType<?> GRAPHIC_CHANGED_EVENT = new EventType(TreeItem.treeNotificationEvent(), "GraphicChangedEvent");
    private final EventHandler<TreeModificationEvent<Object>> itemListener = new EventHandler<TreeModificationEvent<Object>>(){

        @Override
        public void handle(TreeModificationEvent<Object> treeModificationEvent) {
            TreeItem.this.expandedDescendentCountDirty = true;
        }
    };
    private boolean ignoreSortUpdate = false;
    private boolean expandedDescendentCountDirty = true;
    ObservableList<TreeItem<T>> children;
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
    private int expandedDescendentCount = 1;
    int previousExpandedDescendentCount = 1;
    Comparator<TreeItem<T>> lastComparator = null;
    TreeSortMode lastSortMode = null;
    private int parentLinkCount = 0;
    private ListChangeListener<TreeItem<T>> childrenListener = change -> {
        this.expandedDescendentCountDirty = true;
        this.updateChildren(change);
    };
    private ObjectProperty<T> value;
    private ObjectProperty<Node> graphic;
    private BooleanProperty expanded;
    private ReadOnlyBooleanWrapper leaf;
    private ReadOnlyObjectWrapper<TreeItem<T>> parent = new ReadOnlyObjectWrapper(this, "parent");

    public static <T> EventType<TreeModificationEvent<T>> treeNotificationEvent() {
        return TREE_NOTIFICATION_EVENT;
    }

    public static <T> EventType<TreeModificationEvent<T>> expandedItemCountChangeEvent() {
        return EXPANDED_ITEM_COUNT_CHANGE_EVENT;
    }

    public static <T> EventType<TreeModificationEvent<T>> branchExpandedEvent() {
        return BRANCH_EXPANDED_EVENT;
    }

    public static <T> EventType<TreeModificationEvent<T>> branchCollapsedEvent() {
        return BRANCH_COLLAPSED_EVENT;
    }

    public static <T> EventType<TreeModificationEvent<T>> childrenModificationEvent() {
        return CHILDREN_MODIFICATION_EVENT;
    }

    public static <T> EventType<TreeModificationEvent<T>> valueChangedEvent() {
        return VALUE_CHANGED_EVENT;
    }

    public static <T> EventType<TreeModificationEvent<T>> graphicChangedEvent() {
        return GRAPHIC_CHANGED_EVENT;
    }

    public TreeItem() {
        this(null);
    }

    public TreeItem(T t) {
        this(t, null);
    }

    public TreeItem(T t, Node node) {
        this.setValue(t);
        this.setGraphic(node);
        this.addEventHandler(TreeItem.expandedItemCountChangeEvent(), this.itemListener);
    }

    public final void setValue(T t) {
        this.valueProperty().setValue(t);
    }

    public final T getValue() {
        return this.value == null ? null : (T)this.value.getValue();
    }

    public final ObjectProperty<T> valueProperty() {
        if (this.value == null) {
            this.value = new ObjectPropertyBase<T>(){

                @Override
                protected void invalidated() {
                    TreeItem.this.fireEvent(new TreeModificationEvent((EventType<? extends Event>)VALUE_CHANGED_EVENT, TreeItem.this, this.get()));
                }

                @Override
                public Object getBean() {
                    return TreeItem.this;
                }

                @Override
                public String getName() {
                    return "value";
                }
            };
        }
        return this.value;
    }

    public final void setGraphic(Node node) {
        this.graphicProperty().setValue(node);
    }

    public final Node getGraphic() {
        return this.graphic == null ? null : (Node)this.graphic.getValue();
    }

    public final ObjectProperty<Node> graphicProperty() {
        if (this.graphic == null) {
            this.graphic = new ObjectPropertyBase<Node>(){

                @Override
                protected void invalidated() {
                    TreeItem.this.fireEvent(new TreeModificationEvent(GRAPHIC_CHANGED_EVENT, TreeItem.this));
                }

                @Override
                public Object getBean() {
                    return TreeItem.this;
                }

                @Override
                public String getName() {
                    return "graphic";
                }
            };
        }
        return this.graphic;
    }

    public final void setExpanded(boolean bl) {
        if (!bl && this.expanded == null) {
            return;
        }
        this.expandedProperty().setValue(bl);
    }

    public final boolean isExpanded() {
        return this.expanded == null ? false : this.expanded.getValue();
    }

    public final BooleanProperty expandedProperty() {
        if (this.expanded == null) {
            this.expanded = new BooleanPropertyBase(){

                @Override
                protected void invalidated() {
                    if (TreeItem.this.isLeaf()) {
                        return;
                    }
                    EventType eventType = TreeItem.this.isExpanded() ? BRANCH_EXPANDED_EVENT : BRANCH_COLLAPSED_EVENT;
                    TreeItem.this.fireEvent(new TreeModificationEvent((EventType<? extends Event>)eventType, TreeItem.this, TreeItem.this.isExpanded()));
                }

                @Override
                public Object getBean() {
                    return TreeItem.this;
                }

                @Override
                public String getName() {
                    return "expanded";
                }
            };
        }
        return this.expanded;
    }

    private void setLeaf(boolean bl) {
        if (bl && this.leaf == null) {
            return;
        }
        if (this.leaf == null) {
            this.leaf = new ReadOnlyBooleanWrapper(this, "leaf", true);
        }
        this.leaf.setValue(bl);
    }

    public boolean isLeaf() {
        return this.leaf == null ? true : this.leaf.getValue();
    }

    public final ReadOnlyBooleanProperty leafProperty() {
        if (this.leaf == null) {
            this.leaf = new ReadOnlyBooleanWrapper(this, "leaf", true);
        }
        return this.leaf.getReadOnlyProperty();
    }

    private void setParent(TreeItem<T> treeItem) {
        this.parent.setValue(treeItem);
    }

    public final TreeItem<T> getParent() {
        return this.parent == null ? null : (TreeItem)this.parent.getValue();
    }

    public final ReadOnlyObjectProperty<TreeItem<T>> parentProperty() {
        return this.parent.getReadOnlyProperty();
    }

    public ObservableList<TreeItem<T>> getChildren() {
        if (this.children == null) {
            this.children = FXCollections.observableArrayList();
            this.children.addListener(this.childrenListener);
        }
        if (this.children.isEmpty()) {
            return this.children;
        }
        if (!this.ignoreSortUpdate) {
            this.checkSortState();
        }
        return this.children;
    }

    public TreeItem<T> previousSibling() {
        return this.previousSibling(this);
    }

    public TreeItem<T> previousSibling(TreeItem<T> treeItem) {
        if (this.getParent() == null || treeItem == null) {
            return null;
        }
        ObservableList<TreeItem<T>> observableList = this.getParent().getChildren();
        int n = observableList.size();
        int n2 = -1;
        for (int i = 0; i < n; ++i) {
            if (!treeItem.equals(observableList.get(i))) continue;
            n2 = i - 1;
            return n2 < 0 ? null : (TreeItem)observableList.get(n2);
        }
        return null;
    }

    public TreeItem<T> nextSibling() {
        return this.nextSibling(this);
    }

    public TreeItem<T> nextSibling(TreeItem<T> treeItem) {
        if (this.getParent() == null || treeItem == null) {
            return null;
        }
        ObservableList<TreeItem<T>> observableList = this.getParent().getChildren();
        int n = observableList.size();
        int n2 = -1;
        for (int i = 0; i < n; ++i) {
            if (!treeItem.equals(observableList.get(i))) continue;
            n2 = i + 1;
            return n2 >= n ? null : (TreeItem)observableList.get(n2);
        }
        return null;
    }

    public String toString() {
        return "TreeItem [ value: " + this.getValue() + " ]";
    }

    private void fireEvent(TreeModificationEvent<T> treeModificationEvent) {
        Event.fireEvent(this, treeModificationEvent);
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        if (this.getParent() != null) {
            this.getParent().buildEventDispatchChain(eventDispatchChain);
        }
        return eventDispatchChain.append(this.eventHandlerManager);
    }

    public <E extends Event> void addEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        this.eventHandlerManager.addEventHandler(eventType, eventHandler);
    }

    public <E extends Event> void removeEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        this.eventHandlerManager.removeEventHandler(eventType, eventHandler);
    }

    void sort() {
        this.sort(this.children, this.lastComparator, this.lastSortMode);
    }

    private void sort(ObservableList<TreeItem<T>> observableList, Comparator<TreeItem<T>> comparator, TreeSortMode treeSortMode) {
        if (comparator == null) {
            return;
        }
        this.runSort(observableList, comparator, treeSortMode);
        if (this.getParent() == null) {
            TreeModificationEvent treeModificationEvent = new TreeModificationEvent(TreeItem.childrenModificationEvent(), this);
            treeModificationEvent.wasPermutated = true;
            this.fireEvent(treeModificationEvent);
        }
    }

    private void checkSortState() {
        TreeItem<T> treeItem = this.getRoot();
        TreeSortMode treeSortMode = treeItem.lastSortMode;
        Comparator<TreeItem<T>> comparator = treeItem.lastComparator;
        if (comparator != null && comparator != this.lastComparator) {
            this.lastComparator = comparator;
            this.runSort(this.children, comparator, treeSortMode);
        }
    }

    private void runSort(ObservableList<TreeItem<T>> observableList, Comparator<TreeItem<T>> comparator, TreeSortMode treeSortMode) {
        if (treeSortMode == TreeSortMode.ALL_DESCENDANTS) {
            this.doSort(observableList, comparator);
        } else if (treeSortMode == TreeSortMode.ONLY_FIRST_LEVEL && this.getParent() == null) {
            this.doSort(observableList, comparator);
        }
    }

    private TreeItem<T> getRoot() {
        TreeItem<T> treeItem = this.getParent();
        if (treeItem == null) {
            return this;
        }
        TreeItem<T> treeItem2;
        while ((treeItem2 = treeItem.getParent()) != null) {
            treeItem = treeItem2;
        }
        return treeItem;
    }

    private void doSort(ObservableList<TreeItem<T>> observableList, Comparator<TreeItem<T>> comparator) {
        if (!this.isLeaf() && this.isExpanded()) {
            FXCollections.sort(observableList, comparator);
        }
    }

    int getExpandedDescendentCount(boolean bl) {
        if (bl || this.expandedDescendentCountDirty) {
            this.updateExpandedDescendentCount(bl);
            this.expandedDescendentCountDirty = false;
        }
        return this.expandedDescendentCount;
    }

    private void updateExpandedDescendentCount(boolean bl) {
        this.previousExpandedDescendentCount = this.expandedDescendentCount;
        this.expandedDescendentCount = 1;
        this.ignoreSortUpdate = true;
        if (!this.isLeaf() && this.isExpanded()) {
            for (TreeItem treeItem : this.getChildren()) {
                if (treeItem == null) continue;
                this.expandedDescendentCount += treeItem.isExpanded() ? treeItem.getExpandedDescendentCount(bl) : 1;
            }
        }
        this.ignoreSortUpdate = false;
    }

    private void updateChildren(ListChangeListener.Change<? extends TreeItem<T>> change) {
        this.setLeaf(this.children.isEmpty());
        ArrayList<? extends TreeItem<T>> arrayList = new ArrayList<TreeItem<T>>();
        ArrayList<? extends TreeItem<T>> arrayList2 = new ArrayList<TreeItem<T>>();
        while (change.next()) {
            arrayList.addAll(change.getAddedSubList());
            arrayList2.addAll(change.getRemoved());
        }
        TreeItem.updateChildrenParent(arrayList2, null);
        TreeItem.updateChildrenParent(arrayList, this);
        change.reset();
        this.fireEvent(new TreeModificationEvent(CHILDREN_MODIFICATION_EVENT, this, arrayList, arrayList2, change));
    }

    private static <T> void updateChildrenParent(List<? extends TreeItem<T>> list, TreeItem<T> treeItem) {
        if (list == null) {
            return;
        }
        for (TreeItem<T> treeItem2 : list) {
            boolean bl;
            if (treeItem2 == null) continue;
            TreeItem<T> treeItem3 = treeItem2.getParent();
            if (treeItem2.parentLinkCount == 0) {
                super.setParent(treeItem);
            }
            if (!(bl = treeItem3 != null && treeItem3.equals(treeItem))) continue;
            if (treeItem == null) {
                --treeItem2.parentLinkCount;
                continue;
            }
            ++treeItem2.parentLinkCount;
        }
    }

    static /* synthetic */ EventType access$800() {
        return TREE_NOTIFICATION_EVENT;
    }

    public static class TreeModificationEvent<T>
    extends Event {
        private static final long serialVersionUID = 4741889985221719579L;
        public static final EventType<?> ANY = TreeItem.access$800();
        private final transient TreeItem<T> treeItem;
        private final T newValue;
        private final List<? extends TreeItem<T>> added;
        private final List<? extends TreeItem<T>> removed;
        private final ListChangeListener.Change<? extends TreeItem<T>> change;
        private final boolean wasExpanded;
        private final boolean wasCollapsed;
        private boolean wasPermutated;

        public TreeModificationEvent(EventType<? extends Event> eventType, TreeItem<T> treeItem) {
            this(eventType, treeItem, null);
        }

        public TreeModificationEvent(EventType<? extends Event> eventType, TreeItem<T> treeItem, T t) {
            super(eventType);
            this.treeItem = treeItem;
            this.newValue = t;
            this.added = null;
            this.removed = null;
            this.change = null;
            this.wasExpanded = false;
            this.wasCollapsed = false;
        }

        public TreeModificationEvent(EventType<? extends Event> eventType, TreeItem<T> treeItem, boolean bl) {
            super(eventType);
            this.treeItem = treeItem;
            this.newValue = null;
            this.added = null;
            this.removed = null;
            this.change = null;
            this.wasExpanded = bl;
            this.wasCollapsed = !bl;
        }

        public TreeModificationEvent(EventType<? extends Event> eventType, TreeItem<T> treeItem, List<? extends TreeItem<T>> list, List<? extends TreeItem<T>> list2) {
            this(eventType, treeItem, list, list2, null);
        }

        private TreeModificationEvent(EventType<? extends Event> eventType, TreeItem<T> treeItem, List<? extends TreeItem<T>> list, List<? extends TreeItem<T>> list2, ListChangeListener.Change<? extends TreeItem<T>> change) {
            super(eventType);
            this.treeItem = treeItem;
            this.newValue = null;
            this.added = list;
            this.removed = list2;
            this.change = change;
            this.wasExpanded = false;
            this.wasCollapsed = false;
            this.wasPermutated = list != null && list2 != null && list.size() == list2.size() && list.containsAll(list2);
        }

        @Override
        public TreeItem<T> getSource() {
            return this.treeItem;
        }

        public TreeItem<T> getTreeItem() {
            return this.treeItem;
        }

        public T getNewValue() {
            return this.newValue;
        }

        public List<? extends TreeItem<T>> getAddedChildren() {
            return this.added == null ? Collections.emptyList() : this.added;
        }

        public List<? extends TreeItem<T>> getRemovedChildren() {
            return this.removed == null ? Collections.emptyList() : this.removed;
        }

        public int getRemovedSize() {
            return this.getRemovedChildren().size();
        }

        public int getAddedSize() {
            return this.getAddedChildren().size();
        }

        public boolean wasExpanded() {
            return this.wasExpanded;
        }

        public boolean wasCollapsed() {
            return this.wasCollapsed;
        }

        public boolean wasAdded() {
            return this.getAddedSize() > 0;
        }

        public boolean wasRemoved() {
            return this.getRemovedSize() > 0;
        }

        public boolean wasPermutated() {
            return this.wasPermutated;
        }

        int getFrom() {
            return this.change == null ? -1 : this.change.getFrom();
        }

        int getTo() {
            return this.change == null ? -1 : this.change.getTo();
        }

        ListChangeListener.Change<? extends TreeItem<T>> getChange() {
            return this.change;
        }
    }
}

