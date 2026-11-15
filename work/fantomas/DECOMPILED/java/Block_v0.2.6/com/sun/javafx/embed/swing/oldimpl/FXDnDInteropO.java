/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing.oldimpl;

import com.sun.javafx.embed.swing.CachingTransferable;
import com.sun.javafx.embed.swing.FXDnD;
import com.sun.javafx.embed.swing.FXDnDInterop;
import com.sun.javafx.embed.swing.SwingDnD;
import com.sun.javafx.embed.swing.SwingEvents;
import com.sun.javafx.embed.swing.SwingNodeHelper;
import com.sun.javafx.tk.Toolkit;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.SecondaryLoop;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.dnd.peer.DropTargetContextPeer;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import sun.awt.AWTAccessor;
import sun.awt.dnd.SunDragSourceContextPeer;
import sun.swing.JLightweightFrame;

public class FXDnDInteropO
extends FXDnDInterop {
    private SwingNode node = null;
    private boolean isDragSourceListenerInstalled = false;
    private javafx.scene.input.MouseEvent pressEvent = null;
    private long pressTime = 0L;
    private volatile SecondaryLoop loop;
    private final Map<Component, FXDragGestureRecognizer> recognizers = new HashMap<Component, FXDragGestureRecognizer>();
    private final EventHandler<javafx.scene.input.MouseEvent> onMousePressHandler = mouseEvent -> {
        this.pressEvent = mouseEvent;
        this.pressTime = System.currentTimeMillis();
    };
    private volatile FXDragSourceContextPeer activeDSContextPeer;
    private final EventHandler<javafx.scene.input.MouseEvent> onDragStartHandler = mouseEvent -> {
        this.activeDSContextPeer = null;
        javafx.scene.input.MouseEvent mouseEvent2 = this.getInitialGestureEvent();
        SwingNodeHelper.runOnEDTAndWait(this, () -> this.fireEvent((int)mouseEvent2.getX(), (int)mouseEvent2.getY(), this.pressTime, SwingEvents.fxMouseModsToMouseMods(mouseEvent2)));
        if (this.activeDSContextPeer == null) {
            return;
        }
        mouseEvent.consume();
        Dragboard dragboard = this.getNode().startDragAndDrop(SwingDnD.dropActionsToTransferModes(this.activeDSContextPeer.sourceActions).toArray(new TransferMode[1]));
        HashMap<DataFormat, Object> hashMap = new HashMap<DataFormat, Object>();
        for (String string : this.activeDSContextPeer.transferable.getMimeTypes()) {
            DataFormat dataFormat = DataFormat.lookupMimeType(string);
            if (dataFormat == null) continue;
            hashMap.put(dataFormat, this.activeDSContextPeer.transferable.getData(string));
        }
        boolean bl = dragboard.setContent(hashMap);
        if (!bl && !FXDnD.fxAppThreadIsDispatchThread) {
            this.loop.exit();
        }
    };
    private final EventHandler<DragEvent> onDragDoneHandler = dragEvent -> {
        dragEvent.consume();
        if (!FXDnD.fxAppThreadIsDispatchThread) {
            this.loop.exit();
        }
        if (this.activeDSContextPeer != null) {
            TransferMode transferMode = dragEvent.getTransferMode();
            this.activeDSContextPeer.dragDone(transferMode == null ? 0 : SwingDnD.transferModeToDropAction(transferMode), (int)dragEvent.getX(), (int)dragEvent.getY());
        }
    };
    private boolean isDropTargetListenerInstalled = false;
    private volatile FXDropTargetContextPeer activeDTContextPeer = null;
    private final Map<Component, DropTarget> dropTargets = new HashMap<Component, DropTarget>();
    private final EventHandler<DragEvent> onDragEnteredHandler = dragEvent -> {
        int n;
        if (this.activeDTContextPeer == null) {
            this.activeDTContextPeer = new FXDropTargetContextPeer();
        }
        if ((n = this.activeDTContextPeer.postDropTargetEvent(dragEvent)) != 0) {
            dragEvent.consume();
        }
    };
    private final EventHandler<DragEvent> onDragExitedHandler = dragEvent -> {
        if (this.activeDTContextPeer == null) {
            this.activeDTContextPeer = new FXDropTargetContextPeer();
        }
        this.activeDTContextPeer.postDropTargetEvent(dragEvent);
        this.activeDTContextPeer = null;
    };
    private final EventHandler<DragEvent> onDragOverHandler = dragEvent -> {
        int n;
        if (this.activeDTContextPeer == null) {
            this.activeDTContextPeer = new FXDropTargetContextPeer();
        }
        if ((n = this.activeDTContextPeer.postDropTargetEvent(dragEvent)) != 0) {
            dragEvent.acceptTransferModes(SwingDnD.dropActionsToTransferModes(n).toArray(new TransferMode[1]));
            dragEvent.consume();
        }
    };
    private final EventHandler<DragEvent> onDragDroppedHandler = dragEvent -> {
        int n;
        if (this.activeDTContextPeer == null) {
            this.activeDTContextPeer = new FXDropTargetContextPeer();
        }
        if ((n = this.activeDTContextPeer.postDropTargetEvent(dragEvent)) != 0) {
            dragEvent.setDropCompleted(this.activeDTContextPeer.success);
            dragEvent.consume();
        }
        this.activeDTContextPeer = null;
    };

    @Override
    public Component findComponentAt(Object object, int n, int n2, boolean bl) {
        JLightweightFrame jLightweightFrame = (JLightweightFrame)object;
        return AWTAccessor.getContainerAccessor().findComponentAt(jLightweightFrame, n, n2, false);
    }

    @Override
    public boolean isCompEqual(Component component, Object object) {
        JLightweightFrame jLightweightFrame = (JLightweightFrame)object;
        return component != jLightweightFrame;
    }

    @Override
    public int convertModifiersToDropAction(int n, int n2) {
        return SunDragSourceContextPeer.convertModifiersToDropAction(n, n2);
    }

    @Override
    public <T extends DragGestureRecognizer> T createDragGestureRecognizer(DragSource dragSource, Component component, int n, DragGestureListener dragGestureListener) {
        return (T)new FXDragGestureRecognizer(dragSource, component, n, dragGestureListener);
    }

    @Override
    public DragSourceContextPeer createDragSourceContext(DragGestureEvent dragGestureEvent) throws InvalidDnDOperationException {
        return new FXDragSourceContextPeer(dragGestureEvent);
    }

    private void runOnFxThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    private SwingNode getNode() {
        return this.node;
    }

    @Override
    public void setNode(SwingNode swingNode) {
        this.node = swingNode;
    }

    public <T> ComponentMapper<T> mapComponent(Map<Component, T> map, int n, int n2) {
        return new ComponentMapper(map, n, n2);
    }

    private void fireEvent(int n, int n2, long l, int n3) {
        ComponentMapper<FXDragGestureRecognizer> componentMapper = this.mapComponent(this.recognizers, n, n2);
        FXDragGestureRecognizer fXDragGestureRecognizer = (FXDragGestureRecognizer)componentMapper.object;
        if (fXDragGestureRecognizer != null) {
            fXDragGestureRecognizer.fireEvent(componentMapper.x, componentMapper.y, l, n3);
        } else {
            SwingNodeHelper.leaveFXNestedLoop(this);
        }
    }

    private javafx.scene.input.MouseEvent getInitialGestureEvent() {
        return this.pressEvent;
    }

    @Override
    public void addDropTarget(DropTarget dropTarget, SwingNode swingNode) {
        this.dropTargets.put(dropTarget.getComponent(), dropTarget);
        Platform.runLater(() -> {
            if (!this.isDropTargetListenerInstalled) {
                swingNode.addEventHandler(DragEvent.DRAG_ENTERED, this.onDragEnteredHandler);
                swingNode.addEventHandler(DragEvent.DRAG_EXITED, this.onDragExitedHandler);
                swingNode.addEventHandler(DragEvent.DRAG_OVER, this.onDragOverHandler);
                swingNode.addEventHandler(DragEvent.DRAG_DROPPED, this.onDragDroppedHandler);
                this.isDropTargetListenerInstalled = true;
            }
        });
    }

    @Override
    public void removeDropTarget(DropTarget dropTarget, SwingNode swingNode) {
        this.dropTargets.remove(dropTarget.getComponent());
        Platform.runLater(() -> {
            if (this.isDropTargetListenerInstalled && this.dropTargets.isEmpty()) {
                swingNode.removeEventHandler(DragEvent.DRAG_ENTERED, this.onDragEnteredHandler);
                swingNode.removeEventHandler(DragEvent.DRAG_EXITED, this.onDragExitedHandler);
                swingNode.removeEventHandler(DragEvent.DRAG_OVER, this.onDragOverHandler);
                swingNode.removeEventHandler(DragEvent.DRAG_DROPPED, this.onDragDroppedHandler);
                this.isDropTargetListenerInstalled = false;
            }
        });
    }

    private final class FXDropTargetContextPeer
    implements DropTargetContextPeer {
        private int targetActions = 0;
        private int currentAction = 0;
        private DropTarget dt = null;
        private DropTargetContext ctx = null;
        private final CachingTransferable transferable = new CachingTransferable();
        private boolean success = false;
        private int dropAction = 0;

        private FXDropTargetContextPeer() {
        }

        @Override
        public synchronized void setTargetActions(int n) {
            this.targetActions = n;
        }

        @Override
        public synchronized int getTargetActions() {
            return this.targetActions;
        }

        @Override
        public synchronized DropTarget getDropTarget() {
            return this.dt;
        }

        @Override
        public synchronized boolean isTransferableJVMLocal() {
            return false;
        }

        @Override
        public synchronized DataFlavor[] getTransferDataFlavors() {
            return this.transferable.getTransferDataFlavors();
        }

        @Override
        public synchronized Transferable getTransferable() {
            return this.transferable;
        }

        @Override
        public synchronized void acceptDrag(int n) {
            this.currentAction = n;
        }

        @Override
        public synchronized void rejectDrag() {
            this.currentAction = 0;
        }

        @Override
        public synchronized void acceptDrop(int n) {
            this.dropAction = n;
        }

        @Override
        public synchronized void rejectDrop() {
            this.dropAction = 0;
        }

        @Override
        public synchronized void dropComplete(boolean bl) {
            this.success = bl;
        }

        private int postDropTargetEvent(DragEvent dragEvent) {
            ComponentMapper componentMapper = FXDnDInteropO.this.mapComponent(FXDnDInteropO.this.dropTargets, (int)dragEvent.getX(), (int)dragEvent.getY());
            EventType<DragEvent> eventType = dragEvent.getEventType();
            Dragboard dragboard = dragEvent.getDragboard();
            this.transferable.updateData(dragboard, DragEvent.DRAG_DROPPED.equals(eventType));
            int n = SwingDnD.transferModesToDropActions(dragboard.getTransferModes());
            int n2 = dragEvent.getTransferMode() == null ? 0 : SwingDnD.transferModeToDropAction(dragEvent.getTransferMode());
            DropTarget dropTarget = componentMapper.object != null ? (DropTarget)componentMapper.object : this.dt;
            SwingNodeHelper.runOnEDTAndWait(FXDnDInteropO.this, () -> {
                if (dropTarget != this.dt) {
                    if (this.ctx != null) {
                        AWTAccessor.getDropTargetContextAccessor().reset(this.ctx);
                    }
                    this.ctx = null;
                    this.dropAction = 0;
                    this.currentAction = 0;
                }
                if (dropTarget != null) {
                    if (this.ctx == null) {
                        this.ctx = dropTarget.getDropTargetContext();
                        AWTAccessor.getDropTargetContextAccessor().setDropTargetContextPeer(this.ctx, this);
                    }
                    DropTarget dropTarget2 = dropTarget;
                    if (DragEvent.DRAG_DROPPED.equals(eventType)) {
                        DropTargetDropEvent dropTargetDropEvent = new DropTargetDropEvent(this.ctx, new Point(componentMapper.x, componentMapper.y), n2, n);
                        dropTarget2.drop(dropTargetDropEvent);
                    } else {
                        DropTargetDragEvent dropTargetDragEvent = new DropTargetDragEvent(this.ctx, new Point(componentMapper.x, componentMapper.y), n2, n);
                        if (DragEvent.DRAG_OVER.equals(eventType)) {
                            dropTarget2.dragOver(dropTargetDragEvent);
                        } else if (DragEvent.DRAG_ENTERED.equals(eventType)) {
                            dropTarget2.dragEnter(dropTargetDragEvent);
                        } else if (DragEvent.DRAG_EXITED.equals(eventType)) {
                            dropTarget2.dragExit(dropTargetDragEvent);
                        }
                    }
                }
                this.dt = (DropTarget)componentMapper.object;
                if (this.dt == null) {
                    this.ctx = null;
                    this.dropAction = 0;
                    this.currentAction = 0;
                }
                if (DragEvent.DRAG_DROPPED.equals(eventType) || DragEvent.DRAG_EXITED.equals(eventType)) {
                    this.ctx = null;
                }
                SwingNodeHelper.leaveFXNestedLoop(FXDnDInteropO.this);
            });
            if (DragEvent.DRAG_DROPPED.equals(eventType)) {
                return this.dropAction;
            }
            return this.currentAction;
        }
    }

    private final class FXDragSourceContextPeer
    extends SunDragSourceContextPeer {
        private volatile int sourceActions;
        private final CachingTransferable transferable;

        @Override
        public void startSecondaryEventLoop() {
            Toolkit.getToolkit().enterNestedEventLoop(this);
        }

        @Override
        public void quitSecondaryEventLoop() {
            assert (!Platform.isFxApplicationThread());
            Platform.runLater(() -> Toolkit.getToolkit().exitNestedEventLoop(this, null));
        }

        @Override
        protected void setNativeCursor(long l, Cursor cursor, int n) {
        }

        private void dragDone(int n, int n2, int n3) {
            this.dragDropFinished(n != 0, n, n2, n3);
        }

        FXDragSourceContextPeer(DragGestureEvent dragGestureEvent) {
            super(dragGestureEvent);
            this.sourceActions = 0;
            this.transferable = new CachingTransferable();
        }

        protected void startDrag(Transferable transferable, long[] lArray, Map map) {
            FXDnDInteropO.this.activeDSContextPeer = this;
            this.transferable.updateData(transferable, true);
            this.sourceActions = this.getDragSourceContext().getSourceActions();
            if (!FXDnD.fxAppThreadIsDispatchThread) {
                FXDnDInteropO.this.loop = java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
                SwingNodeHelper.leaveFXNestedLoop(FXDnDInteropO.this);
                if (!FXDnDInteropO.this.loop.enter()) {
                    // empty if block
                }
            }
        }
    }

    private class FXDragGestureRecognizer
    extends MouseDragGestureRecognizer {
        FXDragGestureRecognizer(DragSource dragSource, Component component, int n, DragGestureListener dragGestureListener) {
            super(dragSource, component, n, dragGestureListener);
            if (component != null) {
                FXDnDInteropO.this.recognizers.put(component, this);
            }
        }

        @Override
        public void setComponent(Component component) {
            Component component2 = this.getComponent();
            if (component2 != null) {
                FXDnDInteropO.this.recognizers.remove(component2);
            }
            super.setComponent(component);
            if (component != null) {
                FXDnDInteropO.this.recognizers.put(component, this);
            }
        }

        @Override
        protected void registerListeners() {
            FXDnDInteropO.this.runOnFxThread(() -> {
                if (!FXDnDInteropO.this.isDragSourceListenerInstalled) {
                    FXDnDInteropO.this.node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, FXDnDInteropO.this.onMousePressHandler);
                    FXDnDInteropO.this.node.addEventHandler(javafx.scene.input.MouseEvent.DRAG_DETECTED, FXDnDInteropO.this.onDragStartHandler);
                    FXDnDInteropO.this.node.addEventHandler(DragEvent.DRAG_DONE, FXDnDInteropO.this.onDragDoneHandler);
                    FXDnDInteropO.this.isDragSourceListenerInstalled = true;
                }
            });
        }

        @Override
        protected void unregisterListeners() {
            FXDnDInteropO.this.runOnFxThread(() -> {
                if (FXDnDInteropO.this.isDragSourceListenerInstalled) {
                    FXDnDInteropO.this.node.removeEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, FXDnDInteropO.this.onMousePressHandler);
                    FXDnDInteropO.this.node.removeEventHandler(javafx.scene.input.MouseEvent.DRAG_DETECTED, FXDnDInteropO.this.onDragStartHandler);
                    FXDnDInteropO.this.node.removeEventHandler(DragEvent.DRAG_DONE, FXDnDInteropO.this.onDragDoneHandler);
                    FXDnDInteropO.this.isDragSourceListenerInstalled = false;
                }
            });
        }

        private void fireEvent(int n, int n2, long l, int n3) {
            this.appendEvent(new MouseEvent(this.getComponent(), 501, l, n3, n, n2, 0, false));
            int n4 = SunDragSourceContextPeer.convertModifiersToDropAction(n3, this.getSourceActions());
            this.fireDragGestureRecognized(n4, new Point(n, n2));
        }
    }

    private class ComponentMapper<T> {
        public int x;
        public int y;
        public T object = null;

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private ComponentMapper(Map<Component, T> map, int n, int n2) {
            this.x = n;
            this.y = n2;
            JLightweightFrame jLightweightFrame = (JLightweightFrame)SwingNodeHelper.getLightweightFrame(FXDnDInteropO.this.node);
            Component component = AWTAccessor.getContainerAccessor().findComponentAt(jLightweightFrame, this.x, this.y, false);
            if (component == null) {
                return;
            }
            Object object = component.getTreeLock();
            synchronized (object) {
                do {
                    this.object = map.get(component);
                } while (this.object == null && (component = component.getParent()) != null);
                if (this.object != null) {
                    while (component != jLightweightFrame && component != null) {
                        this.x -= component.getX();
                        this.y -= component.getY();
                        component = component.getParent();
                    }
                }
            }
        }
    }
}

