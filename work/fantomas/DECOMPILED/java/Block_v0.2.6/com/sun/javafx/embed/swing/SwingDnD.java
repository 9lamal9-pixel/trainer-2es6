/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import com.sun.javafx.embed.EmbeddedSceneDSInterface;
import com.sun.javafx.embed.EmbeddedSceneDTInterface;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.HostDragStartListener;
import com.sun.javafx.embed.swing.DataFlavorUtils;
import com.sun.javafx.embed.swing.SwingDragSource;
import com.sun.javafx.tk.Toolkit;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.scene.input.TransferMode;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public final class SwingDnD {
    private final Transferable dndTransferable = new DnDTransferable();
    private final DragSource dragSource;
    private final DragSourceListener dragSourceListener;
    private SwingDragSource swingDragSource;
    private EmbeddedSceneDTInterface fxDropTarget;
    private EmbeddedSceneDSInterface fxDragSource;
    private MouseEvent me;

    public SwingDnD(final JComponent jComponent, final EmbeddedSceneInterface embeddedSceneInterface) {
        jComponent.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                SwingDnD.this.storeMouseEvent(mouseEvent);
            }

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                SwingDnD.this.storeMouseEvent(mouseEvent);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                SwingDnD.this.storeMouseEvent(mouseEvent);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                SwingDnD.this.storeMouseEvent(mouseEvent);
            }
        });
        this.dragSource = new DragSource();
        this.dragSourceListener = new DragSourceAdapter(){

            @Override
            public void dragDropEnd(DragSourceDropEvent dragSourceDropEvent) {
                assert (SwingDnD.this.fxDragSource != null);
                try {
                    SwingDnD.this.fxDragSource.dragDropEnd(SwingDnD.dropActionToTransferMode(dragSourceDropEvent.getDropAction()));
                }
                finally {
                    SwingDnD.this.fxDragSource = null;
                }
            }
        };
        DropTargetAdapter dropTargetAdapter = new DropTargetAdapter(){
            private TransferMode lastTransferMode;

            @Override
            public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
                if (SwingDnD.this.swingDragSource != null || SwingDnD.this.fxDropTarget != null) {
                    return;
                }
                assert (SwingDnD.this.swingDragSource == null);
                SwingDnD.this.swingDragSource = new SwingDragSource();
                SwingDnD.this.swingDragSource.updateContents(dropTargetDragEvent, false);
                assert (SwingDnD.this.fxDropTarget == null);
                SwingDnD.this.fxDropTarget = embeddedSceneInterface.createDropTarget();
                Point point = dropTargetDragEvent.getLocation();
                Point point2 = new Point(point);
                SwingUtilities.convertPointToScreen(point2, jComponent);
                this.lastTransferMode = SwingDnD.this.fxDropTarget.handleDragEnter(point.x, point.y, point2.x, point2.y, SwingDnD.dropActionToTransferMode(dropTargetDragEvent.getDropAction()), SwingDnD.this.swingDragSource);
                SwingDnD.this.applyDragResult(this.lastTransferMode, dropTargetDragEvent);
            }

            @Override
            public void dragExit(DropTargetEvent dropTargetEvent) {
                assert (SwingDnD.this.swingDragSource != null);
                assert (SwingDnD.this.fxDropTarget != null);
                try {
                    SwingDnD.this.fxDropTarget.handleDragLeave();
                }
                finally {
                    SwingDnD.this.endDnD();
                    this.lastTransferMode = null;
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
                assert (SwingDnD.this.swingDragSource != null);
                SwingDnD.this.swingDragSource.updateContents(dropTargetDragEvent, false);
                assert (SwingDnD.this.fxDropTarget != null);
                Point point = dropTargetDragEvent.getLocation();
                Point point2 = new Point(point);
                SwingUtilities.convertPointToScreen(point2, jComponent);
                this.lastTransferMode = SwingDnD.this.fxDropTarget.handleDragOver(point.x, point.y, point2.x, point2.y, SwingDnD.dropActionToTransferMode(dropTargetDragEvent.getDropAction()));
                SwingDnD.this.applyDragResult(this.lastTransferMode, dropTargetDragEvent);
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void drop(DropTargetDropEvent dropTargetDropEvent) {
                assert (SwingDnD.this.swingDragSource != null);
                SwingDnD.this.applyDropResult(this.lastTransferMode, dropTargetDropEvent);
                SwingDnD.this.swingDragSource.updateContents(dropTargetDropEvent, true);
                Point point = dropTargetDropEvent.getLocation();
                Point point2 = new Point(point);
                SwingUtilities.convertPointToScreen(point2, jComponent);
                assert (SwingDnD.this.fxDropTarget != null);
                try {
                    this.lastTransferMode = SwingDnD.this.fxDropTarget.handleDragDrop(point.x, point.y, point2.x, point2.y, SwingDnD.dropActionToTransferMode(dropTargetDropEvent.getDropAction()));
                    try {
                        SwingDnD.this.applyDropResult(this.lastTransferMode, dropTargetDropEvent);
                    }
                    catch (InvalidDnDOperationException invalidDnDOperationException) {
                        // empty catch block
                    }
                    dropTargetDropEvent.dropComplete(this.lastTransferMode != null);
                }
                catch (Throwable throwable) {
                    dropTargetDropEvent.dropComplete(this.lastTransferMode != null);
                    SwingDnD.this.endDnD();
                    this.lastTransferMode = null;
                    throw throwable;
                }
                SwingDnD.this.endDnD();
                this.lastTransferMode = null;
            }
        };
        jComponent.setDropTarget(new DropTarget(jComponent, 0x40000003, dropTargetAdapter));
    }

    public void addNotify() {
        this.dragSource.addDragSourceListener(this.dragSourceListener);
    }

    public void removeNotify() {
        this.dragSource.removeDragSourceListener(this.dragSourceListener);
    }

    public HostDragStartListener getDragStartListener() {
        return (embeddedSceneDSInterface, transferMode) -> {
            assert (Toolkit.getToolkit().isFxUserThread());
            assert (embeddedSceneDSInterface != null);
            SwingUtilities.invokeLater(() -> {
                assert (this.fxDragSource == null);
                assert (this.swingDragSource == null);
                assert (this.fxDropTarget == null);
                this.fxDragSource = embeddedSceneDSInterface;
                this.startDrag(this.me, this.dndTransferable, embeddedSceneDSInterface.getSupportedActions(), transferMode);
            });
        };
    }

    private void startDrag(final MouseEvent mouseEvent, Transferable transferable, final Set<TransferMode> set, TransferMode transferMode) {
        assert (set.contains((Object)transferMode));
        Point point = new Point(mouseEvent.getX(), mouseEvent.getY());
        int n = SwingDnD.transferModeToDropAction(transferMode);
        final class StubDragGestureRecognizer
        extends DragGestureRecognizer {
            StubDragGestureRecognizer(DragSource dragSource) {
                super(dragSource, mouseEvent2.getComponent());
                this.setSourceActions(SwingDnD.transferModesToDropActions(set));
                this.appendEvent(mouseEvent);
            }

            @Override
            protected void registerListeners() {
            }

            @Override
            protected void unregisterListeners() {
            }
        }
        StubDragGestureRecognizer stubDragGestureRecognizer = new StubDragGestureRecognizer(this.dragSource);
        List<InputEvent> list = Arrays.asList(stubDragGestureRecognizer.getTriggerEvent());
        DragGestureEvent dragGestureEvent = new DragGestureEvent(stubDragGestureRecognizer, n, point, list);
        dragGestureEvent.startDrag(null, transferable);
    }

    private void endDnD() {
        assert (this.swingDragSource != null);
        assert (this.fxDropTarget != null);
        this.fxDropTarget = null;
        this.swingDragSource = null;
    }

    private void storeMouseEvent(MouseEvent mouseEvent) {
        this.me = mouseEvent;
    }

    private void applyDragResult(TransferMode transferMode, DropTargetDragEvent dropTargetDragEvent) {
        if (transferMode == null) {
            dropTargetDragEvent.rejectDrag();
        } else {
            dropTargetDragEvent.acceptDrag(SwingDnD.transferModeToDropAction(transferMode));
        }
    }

    private void applyDropResult(TransferMode transferMode, DropTargetDropEvent dropTargetDropEvent) {
        if (transferMode == null) {
            dropTargetDropEvent.rejectDrop();
        } else {
            dropTargetDropEvent.acceptDrop(SwingDnD.transferModeToDropAction(transferMode));
        }
    }

    public static TransferMode dropActionToTransferMode(int n) {
        switch (n) {
            case 1: {
                return TransferMode.COPY;
            }
            case 2: {
                return TransferMode.MOVE;
            }
            case 0x40000000: {
                return TransferMode.LINK;
            }
            case 0: {
                return null;
            }
        }
        throw new IllegalArgumentException();
    }

    public static int transferModeToDropAction(TransferMode transferMode) {
        switch (transferMode) {
            case COPY: {
                return 1;
            }
            case MOVE: {
                return 2;
            }
            case LINK: {
                return 0x40000000;
            }
        }
        throw new IllegalArgumentException();
    }

    public static Set<TransferMode> dropActionsToTransferModes(int n) {
        EnumSet<TransferMode> enumSet = EnumSet.noneOf(TransferMode.class);
        if ((n & 1) != 0) {
            enumSet.add(TransferMode.COPY);
        }
        if ((n & 2) != 0) {
            enumSet.add(TransferMode.MOVE);
        }
        if ((n & 0x40000000) != 0) {
            enumSet.add(TransferMode.LINK);
        }
        return Collections.unmodifiableSet(enumSet);
    }

    public static int transferModesToDropActions(Set<TransferMode> set) {
        int n = 0;
        for (TransferMode transferMode : set) {
            n |= SwingDnD.transferModeToDropAction(transferMode);
        }
        return n;
    }

    private final class DnDTransferable
    implements Transferable {
        private DnDTransferable() {
        }

        @Override
        public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedEncodingException {
            assert (SwingDnD.this.fxDragSource != null);
            assert (SwingUtilities.isEventDispatchThread());
            String string = DataFlavorUtils.getFxMimeType(dataFlavor);
            return DataFlavorUtils.adjustFxData(dataFlavor, SwingDnD.this.fxDragSource.getData(string));
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            assert (SwingDnD.this.fxDragSource != null);
            assert (SwingUtilities.isEventDispatchThread());
            String[] stringArray = SwingDnD.this.fxDragSource.getMimeTypes();
            ArrayList<DataFlavor> arrayList = new ArrayList<DataFlavor>(stringArray.length);
            for (String string : stringArray) {
                DataFlavor dataFlavor = null;
                try {
                    dataFlavor = new DataFlavor(string);
                }
                catch (ClassNotFoundException classNotFoundException) {
                    continue;
                }
                arrayList.add(dataFlavor);
            }
            return arrayList.toArray(new DataFlavor[0]);
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
            assert (SwingDnD.this.fxDragSource != null);
            assert (SwingUtilities.isEventDispatchThread());
            return SwingDnD.this.fxDragSource.isMimeTypeAvailable(DataFlavorUtils.getFxMimeType(dataFlavor));
        }
    }
}

