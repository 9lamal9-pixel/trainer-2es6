/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.collections.VetoableListDecorator;
import com.sun.javafx.css.StyleManager;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.Point2D;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.geom.transform.NoninvertibleTransformException;
import com.sun.javafx.scene.CssFlags;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.LayoutFlags;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.sg.prism.NGGroup;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.TempState;
import com.sun.javafx.util.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.Selector;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.PropertyHelper;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.stage.Window;

public abstract class Parent
extends Node {
    static final int DIRTY_CHILDREN_THRESHOLD = 10;
    private static final boolean warnOnAutoMove = PropertyHelper.getBooleanProperty("javafx.sg.warn");
    private static final int REMOVED_CHILDREN_THRESHOLD = 20;
    private boolean removedChildrenOptimizationDisabled = false;
    private final Set<Node> childSet = new HashSet<Node>();
    private int startIdx = 0;
    private int pgChildrenSize = 0;
    private final List<Node> viewOrderChildren = new ArrayList<Node>(1);
    private boolean childrenTriggerPermutation = false;
    private List<Node> removed;
    private boolean geomChanged;
    private boolean childSetModified;
    private final ObservableList<Node> children = new VetoableListDecorator<Node>((ObservableList)new TrackableObservableList<Node>(){

        @Override
        protected void onChanged(ListChangeListener.Change<Node> change) {
            boolean bl;
            boolean bl2;
            block20: {
                Parent.this.unmodifiableManagedChildren = null;
                bl2 = false;
                bl = false;
                if (Parent.this.childSetModified) {
                    Object object;
                    int n;
                    int n2;
                    while (change.next()) {
                        Node node;
                        int n3;
                        n2 = change.getFrom();
                        n = change.getTo();
                        for (int i = n2; i < n; ++i) {
                            Node node2 = (Node)Parent.this.children.get(i);
                            if (node2.getParent() == null || node2.getParent() == Parent.this) continue;
                            if (warnOnAutoMove) {
                                System.err.println("WARNING added to a new parent without first removing it from its current");
                                System.err.println("    parent. It will be automatically removed from its current parent.");
                                System.err.println("    node=" + node2 + " oldparent= " + node2.getParent() + " newparent=" + this);
                            }
                            node2.getParent().children.remove(node2);
                            if (!warnOnAutoMove) continue;
                            Thread.dumpStack();
                        }
                        object = change.getRemoved();
                        int n4 = object.size();
                        for (n3 = 0; n3 < n4; ++n3) {
                            node = object.get(n3);
                            if (!node.isManaged()) continue;
                            bl2 = true;
                        }
                        if (!(n4 <= 0 && n - n2 <= 0 || Parent.this.viewOrderChildren.isEmpty())) {
                            bl = true;
                        }
                        for (n3 = n2; n3 < n; ++n3) {
                            node = (Node)Parent.this.children.get(n3);
                            if (node.getViewOrder() != 0.0) {
                                bl = true;
                            }
                            if (node.isManaged() || node instanceof Parent && ((Parent)node).layoutFlag != LayoutFlags.CLEAN) {
                                bl2 = true;
                            }
                            node.setParent(Parent.this);
                            node.setScenes(Parent.this.getScene(), Parent.this.getSubScene());
                            if (!node.isVisible()) continue;
                            Parent.this.geomChanged = true;
                            Parent.this.childIncluded(node);
                        }
                    }
                    if (Parent.this.dirtyChildren == null && Parent.this.children.size() > 10) {
                        Parent.this.dirtyChildren = new ArrayList(20);
                        if (Parent.this.dirtyChildrenCount > 0) {
                            n2 = Parent.this.children.size();
                            for (n = 0; n < n2; ++n) {
                                object = (Node)Parent.this.children.get(n);
                                if (!((Node)object).isVisible() || !((Node)object).boundsChanged) continue;
                                Parent.this.dirtyChildren.add(object);
                            }
                        }
                    }
                } else {
                    while (change.next()) {
                        int n;
                        List<Node> list = change.getRemoved();
                        int n5 = list.size();
                        for (n = 0; n < n5; ++n) {
                            if (!list.get(n).isManaged()) continue;
                            bl2 = true;
                            break block20;
                        }
                        n5 = change.getTo();
                        for (n = change.getFrom(); n < n5; ++n) {
                            if (!((Node)Parent.this.children.get(n)).isManaged()) continue;
                            bl2 = true;
                            break block20;
                        }
                    }
                }
            }
            if (bl2) {
                Parent.this.requestLayout();
            }
            if (Parent.this.geomChanged) {
                NodeHelper.geomChanged(Parent.this);
            }
            change.reset();
            change.next();
            if (Parent.this.startIdx > change.getFrom()) {
                Parent.this.startIdx = change.getFrom();
            }
            NodeHelper.markDirty(Parent.this, DirtyBits.PARENT_CHILDREN);
            NodeHelper.markDirty(Parent.this, DirtyBits.NODE_FORCE_SYNC);
            if (bl) {
                NodeHelper.markDirty(Parent.this, DirtyBits.PARENT_CHILDREN_VIEW_ORDER);
            }
        }
    }){

        @Override
        protected void onProposedChange(List<Node> list, int[] nArray) {
            Node node;
            int n;
            Window window;
            Scene scene = Parent.this.getScene();
            if (scene != null && (window = scene.getWindow()) != null && WindowHelper.getPeer(window) != null) {
                Toolkit.getToolkit().checkFxUserThread();
            }
            Parent.this.geomChanged = false;
            long l = Parent.this.children.size() + list.size();
            int n2 = 0;
            for (n = 0; n < nArray.length; n += 2) {
                n2 += nArray[n + 1] - nArray[n];
            }
            l -= (long)n2;
            if (Parent.this.childrenTriggerPermutation) {
                Parent.this.childSetModified = false;
                return;
            }
            Parent.this.childSetModified = true;
            if (l == (long)Parent.this.childSet.size()) {
                Parent.this.childSetModified = false;
                for (n = list.size() - 1; n >= 0; --n) {
                    node = list.get(n);
                    if (Parent.this.childSet.contains(node)) continue;
                    Parent.this.childSetModified = true;
                    break;
                }
            }
            for (n = 0; n < nArray.length; n += 2) {
                for (int i = nArray[n]; i < nArray[n + 1]; ++i) {
                    Parent.this.childSet.remove(Parent.this.children.get(i));
                }
            }
            try {
                if (Parent.this.childSetModified) {
                    for (n = list.size() - 1; n >= 0; --n) {
                        node = list.get(n);
                        if (node == null) {
                            throw new NullPointerException(this.constructExceptionMessage("child node is null", null));
                        }
                        if (node.getClipParent() != null) {
                            throw new IllegalArgumentException(this.constructExceptionMessage("node already used as a clip", node));
                        }
                        if (!Parent.this.wouldCreateCycle(Parent.this, node)) continue;
                        throw new IllegalArgumentException(this.constructExceptionMessage("cycle detected", node));
                    }
                }
                Parent.this.childSet.addAll(list);
                if ((long)Parent.this.childSet.size() != l) {
                    throw new IllegalArgumentException(this.constructExceptionMessage("duplicate children added", null));
                }
            }
            catch (RuntimeException runtimeException) {
                Parent.this.childSet.clear();
                Parent.this.childSet.addAll(Parent.this.children);
                throw runtimeException;
            }
            if (!Parent.this.childSetModified) {
                return;
            }
            if (Parent.this.removed == null) {
                Parent.this.removed = new ArrayList();
            }
            if (Parent.this.removed.size() + n2 > 20 || !Parent.this.isTreeVisible()) {
                Parent.this.removedChildrenOptimizationDisabled = true;
            }
            for (n = 0; n < nArray.length; n += 2) {
                for (int i = nArray[n]; i < nArray[n + 1]; ++i) {
                    Node node2 = (Node)Parent.this.children.get(i);
                    Scene scene2 = node2.getScene();
                    if (scene2 != null) {
                        scene2.generateMouseExited(node2);
                    }
                    if (Parent.this.dirtyChildren != null) {
                        Parent.this.dirtyChildren.remove(node2);
                    }
                    if (node2.isVisible()) {
                        Parent.this.geomChanged = true;
                        Parent.this.childExcluded(node2);
                    }
                    if (node2.getParent() == Parent.this) {
                        node2.setParent(null);
                        node2.setScenes(null, null);
                    }
                    if (scene == null || Parent.this.removedChildrenOptimizationDisabled) continue;
                    Parent.this.removed.add(node2);
                }
            }
        }

        private String constructExceptionMessage(String string, Node node) {
            StringBuilder stringBuilder = new StringBuilder("Children: ");
            stringBuilder.append(string);
            stringBuilder.append(": parent = ").append(Parent.this);
            if (node != null) {
                stringBuilder.append(", node = ").append(node);
            }
            return stringBuilder.toString();
        }
    };
    private final ObservableList<Node> unmodifiableChildren = FXCollections.unmodifiableObservableList(this.children);
    private List<Node> unmodifiableManagedChildren = null;
    private ParentTraversalEngine traversalEngine;
    private ReadOnlyBooleanWrapper needsLayout;
    LayoutFlags layoutFlag = LayoutFlags.CLEAN;
    private boolean performingLayout = false;
    private boolean sizeCacheClear = true;
    private double prefWidthCache = -1.0;
    private double prefHeightCache = -1.0;
    private double minWidthCache = -1.0;
    private double minHeightCache = -1.0;
    private boolean forceParentLayout = false;
    private Node currentLayoutChild = null;
    private boolean sceneRoot = false;
    boolean layoutRoot = false;
    private final ObservableList<String> stylesheets = new TrackableObservableList<String>(){

        @Override
        protected void onChanged(ListChangeListener.Change<String> change) {
            Scene scene = Parent.this.getScene();
            if (scene != null) {
                StyleManager.getInstance().stylesheetsChanged(Parent.this, change);
                change.reset();
                while (change.next() && !change.wasRemoved()) {
                }
                Parent.this.reapplyCSS();
            }
        }
    };
    private BaseBounds tmp;
    private BaseBounds cachedBounds;
    private boolean cachedBoundsInvalid;
    private int dirtyChildrenCount;
    private ArrayList<Node> dirtyChildren;
    private Node top;
    private Node left;
    private Node bottom;
    private Node right;
    private Node near;
    private Node far;
    private final int LEFT_INVALID = 1;
    private final int TOP_INVALID = 2;
    private final int NEAR_INVALID = 4;
    private final int RIGHT_INVALID = 8;
    private final int BOTTOM_INVALID = 16;
    private final int FAR_INVALID = 32;
    private Node currentlyProcessedChild;

    private void doUpdatePeer() {
        List<NGNode> list;
        NGGroup nGGroup = (NGGroup)this.getPeer();
        if (Utils.assertionEnabled() && (list = nGGroup.getChildren()).size() != this.pgChildrenSize) {
            System.err.println("*** pgnodes.size() [" + list.size() + "] != pgChildrenSize [" + this.pgChildrenSize + "]");
        }
        if (this.isDirty(DirtyBits.PARENT_CHILDREN)) {
            int n;
            nGGroup.clearFrom(this.startIdx);
            for (n = this.startIdx; n < this.children.size(); ++n) {
                nGGroup.add(n, (NGNode)((Node)this.children.get(n)).getPeer());
            }
            if (this.removedChildrenOptimizationDisabled) {
                nGGroup.markDirty();
                this.removedChildrenOptimizationDisabled = false;
            } else if (this.removed != null && !this.removed.isEmpty()) {
                for (n = 0; n < this.removed.size(); ++n) {
                    nGGroup.addToRemoved((NGNode)this.removed.get(n).getPeer());
                }
            }
            if (this.removed != null) {
                this.removed.clear();
            }
            this.startIdx = this.pgChildrenSize = this.children.size();
        }
        if (this.isDirty(DirtyBits.PARENT_CHILDREN_VIEW_ORDER)) {
            this.computeViewOrderChidrenAndUpdatePeer();
        }
        if (Utils.assertionEnabled()) {
            this.validatePG();
        }
    }

    void validatePG() {
        boolean bl = false;
        NGGroup nGGroup = (NGGroup)this.getPeer();
        List<NGNode> list = nGGroup.getChildren();
        if (list.size() != this.children.size()) {
            System.err.println("*** pgnodes.size validatePG() [" + list.size() + "] != children.size() [" + this.children.size() + "]");
            bl = true;
        } else {
            for (int i = 0; i < this.children.size(); ++i) {
                Node node = (Node)this.children.get(i);
                if (node.getParent() != this) {
                    System.err.println("*** this=" + this + " validatePG children[" + i + "].parent= " + node.getParent());
                    bl = true;
                }
                if (node.getPeer() == list.get(i)) continue;
                System.err.println("*** pgnodes[" + i + "] validatePG != children[" + i + "]");
                bl = true;
            }
        }
        if (bl) {
            throw new AssertionError((Object)"validation of PGGroup children failed");
        }
    }

    void printSeq(String string, List<Node> list) {
        Object object = string;
        for (Node node : list) {
            object = (String)object + node + " ";
        }
        System.out.println((String)object);
    }

    void markViewOrderChildrenDirty() {
        NodeHelper.markDirty(this, DirtyBits.PARENT_CHILDREN_VIEW_ORDER);
    }

    private void computeViewOrderChidrenAndUpdatePeer() {
        boolean bl = false;
        for (Node node3 : this.children) {
            double d = node3.getViewOrder();
            if (bl || d == 0.0) continue;
            bl = true;
        }
        this.viewOrderChildren.clear();
        if (bl) {
            this.viewOrderChildren.addAll(this.children);
            Collections.sort(this.viewOrderChildren, (node, node2) -> node.getViewOrder() < node2.getViewOrder() ? 1 : (node.getViewOrder() == node2.getViewOrder() ? 0 : -1));
        }
        NGGroup nGGroup = (NGGroup)this.getPeer();
        nGGroup.setViewOrderChildren(this.viewOrderChildren);
    }

    private List<Node> getOrderedChildren() {
        if (!this.viewOrderChildren.isEmpty()) {
            return this.viewOrderChildren;
        }
        return this.children;
    }

    protected ObservableList<Node> getChildren() {
        return this.children;
    }

    public ObservableList<Node> getChildrenUnmodifiable() {
        return this.unmodifiableChildren;
    }

    protected <E extends Node> List<E> getManagedChildren() {
        if (this.unmodifiableManagedChildren == null) {
            this.unmodifiableManagedChildren = new ArrayList<Node>();
            int n = this.children.size();
            for (int i = 0; i < n; ++i) {
                Node node = (Node)this.children.get(i);
                if (!node.isManaged()) continue;
                this.unmodifiableManagedChildren.add(node);
            }
        }
        return this.unmodifiableManagedChildren;
    }

    final void managedChildChanged() {
        this.requestLayout();
        this.unmodifiableManagedChildren = null;
    }

    final void toFront(Node node) {
        if (Utils.assertionEnabled() && !this.childSet.contains(node)) {
            throw new AssertionError((Object)"specified node is not in the list of children");
        }
        if (this.children.get(this.children.size() - 1) != node) {
            this.childrenTriggerPermutation = true;
            try {
                this.children.remove(node);
                this.children.add(node);
            }
            finally {
                this.childrenTriggerPermutation = false;
            }
        }
    }

    final void toBack(Node node) {
        if (Utils.assertionEnabled() && !this.childSet.contains(node)) {
            throw new AssertionError((Object)"specified node is not in the list of children");
        }
        if (this.children.get(0) != node) {
            this.childrenTriggerPermutation = true;
            try {
                this.children.remove(node);
                this.children.add(0, node);
            }
            finally {
                this.childrenTriggerPermutation = false;
            }
        }
    }

    @Override
    void scenesChanged(Scene scene, SubScene subScene, Scene scene2, SubScene subScene2) {
        int n;
        if (scene2 != null && scene == null) {
            StyleManager.getInstance().forget(this);
            if (this.removed != null) {
                this.removed.clear();
            }
        }
        for (n = 0; n < this.children.size(); ++n) {
            ((Node)this.children.get(n)).setScenes(scene, subScene);
        }
        n = this.layoutFlag != LayoutFlags.CLEAN ? 1 : 0;
        this.sceneRoot = subScene != null && subScene.getRoot() == this || scene != null && scene.getRoot() == this;
        boolean bl = this.layoutRoot = !this.isManaged() || this.sceneRoot;
        if (n != 0 && scene != null && this.layoutRoot && subScene != null) {
            subScene.setDirtyLayout(this);
        }
    }

    @Override
    void setDerivedDepthTest(boolean bl) {
        super.setDerivedDepthTest(bl);
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            Node node = (Node)this.children.get(i);
            node.computeDerivedDepthTest();
        }
    }

    boolean pickChildrenNode(PickRay pickRay, PickResultChooser pickResultChooser) {
        List<Node> list = this.getOrderedChildren();
        for (int i = list.size() - 1; i >= 0; --i) {
            list.get(i).pickNode(pickRay, pickResultChooser);
            if (!pickResultChooser.isClosed()) continue;
            return false;
        }
        return true;
    }

    private void doPickNodeLocal(PickRay pickRay, PickResultChooser pickResultChooser) {
        double d = this.intersectsBounds(pickRay);
        if (!Double.isNaN(d) && this.pickChildrenNode(pickRay, pickResultChooser) && this.isPickOnBounds()) {
            pickResultChooser.offer(this, d, PickResultChooser.computePoint(pickRay, d));
        }
    }

    @Override
    boolean isConnected() {
        return super.isConnected() || this.sceneRoot;
    }

    @Override
    public Node lookup(String string) {
        Node node = super.lookup(string);
        if (node == null) {
            int n = this.children.size();
            for (int i = 0; i < n; ++i) {
                Node node2 = (Node)this.children.get(i);
                node = node2.lookup(string);
                if (node == null) continue;
                return node;
            }
        }
        return node;
    }

    @Override
    List<Node> lookupAll(Selector selector, List<Node> list) {
        list = super.lookupAll(selector, list);
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            Node node = (Node)this.children.get(i);
            list = node.lookupAll(selector, list);
        }
        return list;
    }

    private final void setTraversalEngine(ParentTraversalEngine parentTraversalEngine) {
        this.traversalEngine = parentTraversalEngine;
    }

    private final ParentTraversalEngine getTraversalEngine() {
        return this.traversalEngine;
    }

    protected final void setNeedsLayout(boolean bl) {
        if (bl) {
            this.markDirtyLayout(true, false);
        } else if (this.layoutFlag == LayoutFlags.NEEDS_LAYOUT) {
            boolean bl2 = false;
            int n = this.children.size();
            for (int i = 0; i < n; ++i) {
                Node node = (Node)this.children.get(i);
                if (!(node instanceof Parent) || ((Parent)node).layoutFlag == LayoutFlags.CLEAN) continue;
                bl2 = true;
                break;
            }
            this.setLayoutFlag(bl2 ? LayoutFlags.DIRTY_BRANCH : LayoutFlags.CLEAN);
        }
    }

    public final boolean isNeedsLayout() {
        return this.layoutFlag == LayoutFlags.NEEDS_LAYOUT;
    }

    public final ReadOnlyBooleanProperty needsLayoutProperty() {
        if (this.needsLayout == null) {
            this.needsLayout = new ReadOnlyBooleanWrapper(this, "needsLayout", this.layoutFlag == LayoutFlags.NEEDS_LAYOUT);
        }
        return this.needsLayout;
    }

    boolean isPerformingLayout() {
        return this.performingLayout;
    }

    void setLayoutFlag(LayoutFlags layoutFlags) {
        if (this.needsLayout != null) {
            this.needsLayout.set(layoutFlags == LayoutFlags.NEEDS_LAYOUT);
        }
        this.layoutFlag = layoutFlags;
    }

    private void markDirtyLayout(boolean bl, boolean bl2) {
        this.setLayoutFlag(LayoutFlags.NEEDS_LAYOUT);
        if (bl || this.layoutRoot) {
            if (this.sceneRoot) {
                Toolkit.getToolkit().requestNextPulse();
                if (this.getSubScene() != null) {
                    this.getSubScene().setDirtyLayout(this);
                }
            } else {
                this.markDirtyLayoutBranch();
            }
        } else {
            this.requestParentLayout(bl2);
        }
    }

    public void requestLayout() {
        this.clearSizeCache();
        this.markDirtyLayout(false, this.forceParentLayout);
    }

    void requestLayout(boolean bl) {
        boolean bl2 = this.forceParentLayout;
        this.forceParentLayout = bl;
        this.requestLayout();
        this.forceParentLayout = bl2;
    }

    protected final void requestParentLayout() {
        this.requestParentLayout(false);
    }

    void requestParentLayout(boolean bl) {
        Parent parent;
        if (!(this.layoutRoot || (parent = this.getParent()) == null || parent.performingLayout && !bl)) {
            parent.requestLayout();
        }
    }

    void clearSizeCache() {
        if (this.sizeCacheClear) {
            return;
        }
        this.sizeCacheClear = true;
        this.prefWidthCache = -1.0;
        this.prefHeightCache = -1.0;
        this.minWidthCache = -1.0;
        this.minHeightCache = -1.0;
    }

    @Override
    public double prefWidth(double d) {
        if (d == -1.0) {
            if (this.prefWidthCache == -1.0) {
                this.prefWidthCache = this.computePrefWidth(-1.0);
                if (Double.isNaN(this.prefWidthCache) || this.prefWidthCache < 0.0) {
                    this.prefWidthCache = 0.0;
                }
                this.sizeCacheClear = false;
            }
            return this.prefWidthCache;
        }
        double d2 = this.computePrefWidth(d);
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public double prefHeight(double d) {
        if (d == -1.0) {
            if (this.prefHeightCache == -1.0) {
                this.prefHeightCache = this.computePrefHeight(-1.0);
                if (Double.isNaN(this.prefHeightCache) || this.prefHeightCache < 0.0) {
                    this.prefHeightCache = 0.0;
                }
                this.sizeCacheClear = false;
            }
            return this.prefHeightCache;
        }
        double d2 = this.computePrefHeight(d);
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public double minWidth(double d) {
        if (d == -1.0) {
            if (this.minWidthCache == -1.0) {
                this.minWidthCache = this.computeMinWidth(-1.0);
                if (Double.isNaN(this.minWidthCache) || this.minWidthCache < 0.0) {
                    this.minWidthCache = 0.0;
                }
                this.sizeCacheClear = false;
            }
            return this.minWidthCache;
        }
        double d2 = this.computeMinWidth(d);
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public double minHeight(double d) {
        if (d == -1.0) {
            if (this.minHeightCache == -1.0) {
                this.minHeightCache = this.computeMinHeight(-1.0);
                if (Double.isNaN(this.minHeightCache) || this.minHeightCache < 0.0) {
                    this.minHeightCache = 0.0;
                }
                this.sizeCacheClear = false;
            }
            return this.minHeightCache;
        }
        double d2 = this.computeMinHeight(d);
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    protected double computePrefWidth(double d) {
        double d2 = 0.0;
        double d3 = 0.0;
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            Node node = (Node)this.children.get(i);
            if (!node.isManaged()) continue;
            double d4 = node.getLayoutBounds().getMinX() + node.getLayoutX();
            d2 = Math.min(d2, d4);
            d3 = Math.max(d3, d4 + this.boundedSize(node.prefWidth(-1.0), node.minWidth(-1.0), node.maxWidth(-1.0)));
        }
        return d3 - d2;
    }

    protected double computePrefHeight(double d) {
        double d2 = 0.0;
        double d3 = 0.0;
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            Node node = (Node)this.children.get(i);
            if (!node.isManaged()) continue;
            double d4 = node.getLayoutBounds().getMinY() + node.getLayoutY();
            d2 = Math.min(d2, d4);
            d3 = Math.max(d3, d4 + this.boundedSize(node.prefHeight(-1.0), node.minHeight(-1.0), node.maxHeight(-1.0)));
        }
        return d3 - d2;
    }

    protected double computeMinWidth(double d) {
        return this.prefWidth(d);
    }

    protected double computeMinHeight(double d) {
        return this.prefHeight(d);
    }

    @Override
    public double getBaselineOffset() {
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            double d;
            Node node = (Node)this.children.get(i);
            if (!node.isManaged() || (d = node.getBaselineOffset()) == Double.NEGATIVE_INFINITY) continue;
            return node.getLayoutBounds().getMinY() + node.getLayoutY() + d;
        }
        return super.getBaselineOffset();
    }

    boolean isCurrentLayoutChild(Node node) {
        return node == this.currentLayoutChild;
    }

    public final void layout() {
        LayoutFlags layoutFlags = this.layoutFlag;
        this.setLayoutFlag(LayoutFlags.CLEAN);
        switch (layoutFlags) {
            case CLEAN: {
                break;
            }
            case NEEDS_LAYOUT: {
                if (this.performingLayout) break;
                this.performingLayout = true;
                this.layoutChildren();
            }
            case DIRTY_BRANCH: {
                int n = this.children.size();
                for (int i = 0; i < n; ++i) {
                    Node node;
                    this.currentLayoutChild = node = (Node)this.children.get(i);
                    if (node instanceof Parent) {
                        ((Parent)node).layout();
                        continue;
                    }
                    if (!(node instanceof SubScene)) continue;
                    ((SubScene)node).layoutPass();
                }
                this.currentLayoutChild = null;
                this.performingLayout = false;
            }
        }
    }

    protected void layoutChildren() {
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            Node node;
            this.currentLayoutChild = node = (Node)this.children.get(i);
            if (!node.isResizable() || !node.isManaged()) continue;
            node.autosize();
        }
        this.currentLayoutChild = null;
    }

    @Override
    final void notifyManagedChanged() {
        this.layoutRoot = !this.isManaged() || this.sceneRoot;
    }

    final boolean isSceneRoot() {
        return this.sceneRoot;
    }

    public final ObservableList<String> getStylesheets() {
        return this.stylesheets;
    }

    private List<String> doGetAllParentStylesheets() {
        List<String> list = null;
        Parent parent = this.getParent();
        if (parent != null) {
            list = ParentHelper.getAllParentStylesheets(parent);
        }
        if (this.stylesheets != null && !this.stylesheets.isEmpty()) {
            if (list == null) {
                list = new ArrayList<String>(this.stylesheets.size());
            }
            int n = this.stylesheets.size();
            for (int i = 0; i < n; ++i) {
                list.add((String)this.stylesheets.get(i));
            }
        }
        return list;
    }

    private void doProcessCSS() {
        if (this.cssFlag == CssFlags.CLEAN) {
            return;
        }
        if (this.cssFlag == CssFlags.DIRTY_BRANCH) {
            super.processCSS();
            return;
        }
        ParentHelper.superProcessCSS(this);
        if (this.children.isEmpty()) {
            return;
        }
        Node[] nodeArray = this.children.toArray(new Node[this.children.size()]);
        for (int i = 0; i < nodeArray.length; ++i) {
            Node node = nodeArray[i];
            Parent parent = node.getParent();
            if (parent == null || parent != this) continue;
            if (CssFlags.UPDATE.compareTo(node.cssFlag) > 0) {
                node.cssFlag = CssFlags.UPDATE;
            }
            NodeHelper.processCSS(node);
        }
    }

    protected Parent() {
        ParentHelper.initHelper(this);
        this.tmp = new RectBounds();
        this.cachedBounds = new RectBounds();
        this.LEFT_INVALID = 1;
        this.TOP_INVALID = 2;
        this.NEAR_INVALID = 4;
        this.RIGHT_INVALID = 8;
        this.BOTTOM_INVALID = 16;
        this.FAR_INVALID = 32;
        this.layoutFlag = LayoutFlags.NEEDS_LAYOUT;
        this.setAccessibleRole(AccessibleRole.PARENT);
    }

    private NGNode doCreatePeer() {
        return new NGGroup();
    }

    @Override
    void nodeResolvedOrientationChanged() {
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            ((Node)this.children.get(i)).parentResolvedOrientationInvalidated();
        }
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        if (this.children.isEmpty()) {
            return baseBounds.makeEmpty();
        }
        if (baseTransform.isTranslateOrIdentity()) {
            if (this.cachedBoundsInvalid) {
                this.recomputeBounds();
                if (this.dirtyChildren != null) {
                    this.dirtyChildren.clear();
                }
                this.cachedBoundsInvalid = false;
                this.dirtyChildrenCount = 0;
            }
            baseBounds = !baseTransform.isIdentity() ? baseBounds.deriveWithNewBounds((float)((double)this.cachedBounds.getMinX() + baseTransform.getMxt()), (float)((double)this.cachedBounds.getMinY() + baseTransform.getMyt()), (float)((double)this.cachedBounds.getMinZ() + baseTransform.getMzt()), (float)((double)this.cachedBounds.getMaxX() + baseTransform.getMxt()), (float)((double)this.cachedBounds.getMaxY() + baseTransform.getMyt()), (float)((double)this.cachedBounds.getMaxZ() + baseTransform.getMzt())) : baseBounds.deriveWithNewBounds(this.cachedBounds);
            return baseBounds;
        }
        double d = Double.MAX_VALUE;
        double d2 = Double.MAX_VALUE;
        double d3 = Double.MAX_VALUE;
        double d4 = Double.MIN_VALUE;
        double d5 = Double.MIN_VALUE;
        double d6 = Double.MIN_VALUE;
        boolean bl = true;
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            Node node = (Node)this.children.get(i);
            if (!node.isVisible() || (baseBounds = this.getChildTransformedBounds(node, baseTransform, baseBounds)).isEmpty()) continue;
            if (bl) {
                d = baseBounds.getMinX();
                d2 = baseBounds.getMinY();
                d3 = baseBounds.getMinZ();
                d4 = baseBounds.getMaxX();
                d5 = baseBounds.getMaxY();
                d6 = baseBounds.getMaxZ();
                bl = false;
                continue;
            }
            d = Math.min((double)baseBounds.getMinX(), d);
            d2 = Math.min((double)baseBounds.getMinY(), d2);
            d3 = Math.min((double)baseBounds.getMinZ(), d3);
            d4 = Math.max((double)baseBounds.getMaxX(), d4);
            d5 = Math.max((double)baseBounds.getMaxY(), d5);
            d6 = Math.max((double)baseBounds.getMaxZ(), d6);
        }
        if (bl) {
            baseBounds.makeEmpty();
        } else {
            baseBounds = baseBounds.deriveWithNewBounds((float)d, (float)d2, (float)d3, (float)d4, (float)d5, (float)d6);
        }
        return baseBounds;
    }

    private void setChildDirty(Node node, boolean bl) {
        if (node.boundsChanged == bl) {
            return;
        }
        node.boundsChanged = bl;
        if (bl) {
            if (this.dirtyChildren != null) {
                this.dirtyChildren.add(node);
            }
            ++this.dirtyChildrenCount;
        } else {
            if (this.dirtyChildren != null) {
                this.dirtyChildren.remove(node);
            }
            --this.dirtyChildrenCount;
        }
    }

    private void childIncluded(Node node) {
        this.cachedBoundsInvalid = true;
        this.setChildDirty(node, true);
    }

    private void childExcluded(Node node) {
        if (node == this.left) {
            this.left = null;
            this.cachedBoundsInvalid = true;
        }
        if (node == this.top) {
            this.top = null;
            this.cachedBoundsInvalid = true;
        }
        if (node == this.near) {
            this.near = null;
            this.cachedBoundsInvalid = true;
        }
        if (node == this.right) {
            this.right = null;
            this.cachedBoundsInvalid = true;
        }
        if (node == this.bottom) {
            this.bottom = null;
            this.cachedBoundsInvalid = true;
        }
        if (node == this.far) {
            this.far = null;
            this.cachedBoundsInvalid = true;
        }
        this.setChildDirty(node, false);
    }

    private void recomputeBounds() {
        if (this.children.isEmpty()) {
            this.cachedBounds.makeEmpty();
            return;
        }
        if (this.children.size() == 1) {
            Node node = (Node)this.children.get(0);
            node.boundsChanged = false;
            if (node.isVisible()) {
                this.cachedBounds = this.getChildTransformedBounds(node, BaseTransform.IDENTITY_TRANSFORM, this.cachedBounds);
                this.near = this.far = node;
                this.right = this.far;
                this.bottom = this.far;
                this.left = this.far;
                this.top = this.far;
            } else {
                this.cachedBounds.makeEmpty();
            }
            return;
        }
        if (this.dirtyChildrenCount == 0 || !this.updateCachedBounds(this.dirtyChildren != null ? this.dirtyChildren : this.children, this.dirtyChildrenCount)) {
            this.createCachedBounds(this.children);
        }
    }

    private boolean updateCachedBounds(List<Node> list, int n) {
        if (this.cachedBounds.isEmpty()) {
            this.createCachedBounds(list);
            return true;
        }
        int n2 = 0;
        if (this.left == null || this.left.boundsChanged) {
            n2 |= 1;
        }
        if (this.top == null || this.top.boundsChanged) {
            n2 |= 2;
        }
        if (this.near == null || this.near.boundsChanged) {
            n2 |= 4;
        }
        if (this.right == null || this.right.boundsChanged) {
            n2 |= 8;
        }
        if (this.bottom == null || this.bottom.boundsChanged) {
            n2 |= 0x10;
        }
        if (this.far == null || this.far.boundsChanged) {
            n2 |= 0x20;
        }
        float f = this.cachedBounds.getMinX();
        float f2 = this.cachedBounds.getMinY();
        float f3 = this.cachedBounds.getMinZ();
        float f4 = this.cachedBounds.getMaxX();
        float f5 = this.cachedBounds.getMaxY();
        float f6 = this.cachedBounds.getMaxZ();
        int n3 = list.size() - 1;
        while (n > 0) {
            Node node = list.get(n3);
            if (node.boundsChanged) {
                node.boundsChanged = false;
                --n;
                this.tmp = this.getChildTransformedBounds(node, BaseTransform.IDENTITY_TRANSFORM, this.tmp);
                if (!this.tmp.isEmpty()) {
                    float f7 = this.tmp.getMinX();
                    float f8 = this.tmp.getMinY();
                    float f9 = this.tmp.getMinZ();
                    float f10 = this.tmp.getMaxX();
                    float f11 = this.tmp.getMaxY();
                    float f12 = this.tmp.getMaxZ();
                    if (f7 <= f) {
                        f = f7;
                        this.left = node;
                        n2 &= 0xFFFFFFFE;
                    }
                    if (f8 <= f2) {
                        f2 = f8;
                        this.top = node;
                        n2 &= 0xFFFFFFFD;
                    }
                    if (f9 <= f3) {
                        f3 = f9;
                        this.near = node;
                        n2 &= 0xFFFFFFFB;
                    }
                    if (f10 >= f4) {
                        f4 = f10;
                        this.right = node;
                        n2 &= 0xFFFFFFF7;
                    }
                    if (f11 >= f5) {
                        f5 = f11;
                        this.bottom = node;
                        n2 &= 0xFFFFFFEF;
                    }
                    if (f12 >= f6) {
                        f6 = f12;
                        this.far = node;
                        n2 &= 0xFFFFFFDF;
                    }
                }
            }
            --n3;
        }
        if (n2 != 0) {
            return false;
        }
        this.cachedBounds = this.cachedBounds.deriveWithNewBounds(f, f2, f3, f4, f5, f6);
        return true;
    }

    private void createCachedBounds(List<Node> list) {
        Node node;
        int n;
        int n2 = list.size();
        for (n = 0; n < n2; ++n) {
            node = list.get(n);
            node.boundsChanged = false;
            if (!node.isVisible()) continue;
            this.tmp = node.getTransformedBounds(this.tmp, BaseTransform.IDENTITY_TRANSFORM);
            if (this.tmp.isEmpty()) continue;
            this.bottom = this.far = node;
            this.right = this.far;
            this.near = this.far;
            this.top = this.far;
            this.left = this.far;
            break;
        }
        if (n == n2) {
            this.far = null;
            this.bottom = null;
            this.right = null;
            this.near = null;
            this.top = null;
            this.left = null;
            this.cachedBounds.makeEmpty();
            return;
        }
        float f = this.tmp.getMinX();
        float f2 = this.tmp.getMinY();
        float f3 = this.tmp.getMinZ();
        float f4 = this.tmp.getMaxX();
        float f5 = this.tmp.getMaxY();
        float f6 = this.tmp.getMaxZ();
        ++n;
        while (n < n2) {
            node = list.get(n);
            node.boundsChanged = false;
            if (node.isVisible()) {
                this.tmp = node.getTransformedBounds(this.tmp, BaseTransform.IDENTITY_TRANSFORM);
                if (!this.tmp.isEmpty()) {
                    float f7 = this.tmp.getMinX();
                    float f8 = this.tmp.getMinY();
                    float f9 = this.tmp.getMinZ();
                    float f10 = this.tmp.getMaxX();
                    float f11 = this.tmp.getMaxY();
                    float f12 = this.tmp.getMaxZ();
                    if (f7 < f) {
                        f = f7;
                        this.left = node;
                    }
                    if (f8 < f2) {
                        f2 = f8;
                        this.top = node;
                    }
                    if (f9 < f3) {
                        f3 = f9;
                        this.near = node;
                    }
                    if (f10 > f4) {
                        f4 = f10;
                        this.right = node;
                    }
                    if (f11 > f5) {
                        f5 = f11;
                        this.bottom = node;
                    }
                    if (f12 > f6) {
                        f6 = f12;
                        this.far = node;
                    }
                }
            }
            ++n;
        }
        this.cachedBounds = this.cachedBounds.deriveWithNewBounds(f, f2, f3, f4, f5, f6);
    }

    @Override
    protected void updateBounds() {
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            ((Node)this.children.get(i)).updateBounds();
        }
        super.updateBounds();
    }

    private BaseBounds getChildTransformedBounds(Node node, BaseTransform baseTransform, BaseBounds baseBounds) {
        this.currentlyProcessedChild = node;
        baseBounds = node.getTransformedBounds(baseBounds, baseTransform);
        this.currentlyProcessedChild = null;
        return baseBounds;
    }

    void childBoundsChanged(Node node) {
        if (node == this.currentlyProcessedChild) {
            return;
        }
        this.cachedBoundsInvalid = true;
        this.setChildDirty(node, true);
        NodeHelper.geomChanged(this);
    }

    void childVisibilityChanged(Node node) {
        if (node.isVisible()) {
            this.childIncluded(node);
        } else {
            this.childExcluded(node);
        }
        NodeHelper.geomChanged(this);
    }

    private boolean doComputeContains(double d, double d2) {
        Point2D point2D = TempState.getInstance().point;
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            Node node = (Node)this.children.get(i);
            point2D.x = (float)d;
            point2D.y = (float)d2;
            try {
                node.parentToLocal(point2D);
            }
            catch (NoninvertibleTransformException noninvertibleTransformException) {
                continue;
            }
            if (!node.contains(point2D.x, point2D.y)) continue;
            return true;
        }
        return false;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case CHILDREN: {
                return this.getChildrenUnmodifiable();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    void releaseAccessible() {
        int n = this.children.size();
        for (int i = 0; i < n; ++i) {
            Node node = (Node)this.children.get(i);
            node.releaseAccessible();
        }
        super.releaseAccessible();
    }

    List<Node> test_getRemoved() {
        return this.removed;
    }

    List<Node> test_getViewOrderChildren() {
        return this.viewOrderChildren;
    }

    static {
        ParentHelper.setParentAccessor(new ParentHelper.ParentAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Parent)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Parent)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Parent)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((Parent)node).doComputeContains(d, d2);
            }

            @Override
            public void doProcessCSS(Node node) {
                ((Parent)node).doProcessCSS();
            }

            @Override
            public void doPickNodeLocal(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                ((Parent)node).doPickNodeLocal(pickRay, pickResultChooser);
            }

            @Override
            public boolean pickChildrenNode(Parent parent, PickRay pickRay, PickResultChooser pickResultChooser) {
                return parent.pickChildrenNode(pickRay, pickResultChooser);
            }

            @Override
            public void setTraversalEngine(Parent parent, ParentTraversalEngine parentTraversalEngine) {
                parent.setTraversalEngine(parentTraversalEngine);
            }

            @Override
            public ParentTraversalEngine getTraversalEngine(Parent parent) {
                return parent.getTraversalEngine();
            }

            @Override
            public List<String> doGetAllParentStylesheets(Parent parent) {
                return parent.doGetAllParentStylesheets();
            }
        });
    }
}

