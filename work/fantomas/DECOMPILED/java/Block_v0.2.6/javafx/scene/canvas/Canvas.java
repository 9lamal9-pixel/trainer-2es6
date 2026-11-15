/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.canvas;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.canvas.CanvasHelper;
import com.sun.javafx.sg.prism.GrowableDataBuffer;
import com.sun.javafx.sg.prism.NGCanvas;
import com.sun.javafx.sg.prism.NGNode;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;

public class Canvas
extends Node {
    static final int DEFAULT_VAL_BUF_SIZE = 1024;
    static final int DEFAULT_OBJ_BUF_SIZE = 32;
    private static final int SIZE_HISTORY = 5;
    private GrowableDataBuffer current;
    private boolean rendererBehind;
    private int[] recentvalsizes;
    private int[] recentobjsizes;
    private int lastsizeindex;
    private GraphicsContext theContext;
    private DoubleProperty width;
    private DoubleProperty height;

    public Canvas() {
        this(0.0, 0.0);
    }

    public Canvas(double d, double d2) {
        CanvasHelper.initHelper(this);
        this.recentvalsizes = new int[5];
        this.recentobjsizes = new int[5];
        this.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        this.setWidth(d);
        this.setHeight(d2);
    }

    private static int max(int[] nArray, int n) {
        for (int n2 : nArray) {
            if (n >= n2) continue;
            n = n2;
        }
        return n;
    }

    GrowableDataBuffer getBuffer() {
        NodeHelper.markDirty(this, DirtyBits.NODE_CONTENTS);
        NodeHelper.markDirty(this, DirtyBits.NODE_FORCE_SYNC);
        if (this.current == null) {
            int n = Canvas.max(this.recentvalsizes, 1024);
            int n2 = Canvas.max(this.recentobjsizes, 32);
            this.current = GrowableDataBuffer.getBuffer(n, n2);
            this.theContext.updateDimensions();
        }
        return this.current;
    }

    boolean isRendererFallingBehind() {
        return this.rendererBehind;
    }

    public GraphicsContext getGraphicsContext2D() {
        if (this.theContext == null) {
            this.theContext = new GraphicsContext(this);
        }
        return this.theContext;
    }

    public final void setWidth(double d) {
        this.widthProperty().set(d);
    }

    public final double getWidth() {
        return this.width == null ? 0.0 : this.width.get();
    }

    public final DoubleProperty widthProperty() {
        if (this.width == null) {
            this.width = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Canvas.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(Canvas.this);
                    if (Canvas.this.theContext != null) {
                        Canvas.this.theContext.updateDimensions();
                    }
                }

                @Override
                public Object getBean() {
                    return Canvas.this;
                }

                @Override
                public String getName() {
                    return "width";
                }
            };
        }
        return this.width;
    }

    public final void setHeight(double d) {
        this.heightProperty().set(d);
    }

    public final double getHeight() {
        return this.height == null ? 0.0 : this.height.get();
    }

    public final DoubleProperty heightProperty() {
        if (this.height == null) {
            this.height = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Canvas.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(Canvas.this);
                    if (Canvas.this.theContext != null) {
                        Canvas.this.theContext.updateDimensions();
                    }
                }

                @Override
                public Object getBean() {
                    return Canvas.this;
                }

                @Override
                public String getName() {
                    return "height";
                }
            };
        }
        return this.height;
    }

    private NGNode doCreatePeer() {
        return new NGCanvas();
    }

    private void doUpdatePeer() {
        NGCanvas nGCanvas;
        if (NodeHelper.isDirty(this, DirtyBits.NODE_GEOMETRY)) {
            nGCanvas = (NGCanvas)NodeHelper.getPeer(this);
            nGCanvas.updateBounds((float)this.getWidth(), (float)this.getHeight());
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_CONTENTS)) {
            nGCanvas = (NGCanvas)NodeHelper.getPeer(this);
            if (this.current != null && !this.current.isEmpty()) {
                if (--this.lastsizeindex < 0) {
                    this.lastsizeindex = 4;
                }
                this.recentvalsizes[this.lastsizeindex] = this.current.writeValuePosition();
                this.recentobjsizes[this.lastsizeindex] = this.current.writeObjectPosition();
                this.rendererBehind = nGCanvas.updateRendering(this.current);
                this.current = null;
            }
        }
    }

    private boolean doComputeContains(double d, double d2) {
        double d3 = this.getWidth();
        double d4 = this.getHeight();
        return d3 > 0.0 && d4 > 0.0 && d >= 0.0 && d2 >= 0.0 && d < d3 && d2 < d4;
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        baseBounds = new RectBounds(0.0f, 0.0f, (float)this.getWidth(), (float)this.getHeight());
        baseBounds = baseTransform.transform(baseBounds, baseBounds);
        return baseBounds;
    }

    static {
        CanvasHelper.setCanvasAccessor(new CanvasHelper.CanvasAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Canvas)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Canvas)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Canvas)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((Canvas)node).doComputeContains(d, d2);
            }
        });
    }
}

