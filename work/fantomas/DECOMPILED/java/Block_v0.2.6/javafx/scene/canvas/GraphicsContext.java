/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.canvas;

import com.sun.javafx.geom.Arc2D;
import com.sun.javafx.geom.IllegalPathStateException;
import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.PathIterator;
import com.sun.javafx.geom.transform.Affine2D;
import com.sun.javafx.geom.transform.NoninvertibleTransformException;
import com.sun.javafx.image.BytePixelGetter;
import com.sun.javafx.image.BytePixelSetter;
import com.sun.javafx.image.ByteToBytePixelConverter;
import com.sun.javafx.image.IntPixelGetter;
import com.sun.javafx.image.IntToBytePixelConverter;
import com.sun.javafx.image.PixelConverter;
import com.sun.javafx.image.PixelGetter;
import com.sun.javafx.image.PixelUtils;
import com.sun.javafx.image.impl.ByteBgraPre;
import com.sun.javafx.scene.text.FontHelper;
import com.sun.javafx.sg.prism.GrowableDataBuffer;
import com.sun.javafx.tk.Toolkit;
import com.sun.scenario.effect.EffectHelper;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import javafx.geometry.NodeOrientation;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;

public final class GraphicsContext {
    Canvas theCanvas;
    Path2D path;
    boolean pathDirty;
    State curState;
    LinkedList<State> stateStack;
    LinkedList<Path2D> clipStack;
    private float[] coords = new float[6];
    private static final byte[] pgtype = new byte[]{41, 42, 43, 44, 45};
    private static final int[] numsegs = new int[]{2, 2, 4, 6, 0};
    private float[] polybuf = new float[512];
    private boolean txdirty;
    private PixelWriter writer;

    GraphicsContext(Canvas canvas) {
        this.theCanvas = canvas;
        this.path = new Path2D();
        this.pathDirty = true;
        this.curState = new State();
        this.stateStack = new LinkedList();
        this.clipStack = new LinkedList();
    }

    private GrowableDataBuffer getBuffer() {
        return this.theCanvas.getBuffer();
    }

    private void markPathDirty() {
        this.pathDirty = true;
    }

    private void writePath(byte by) {
        this.updateTransform();
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        if (this.pathDirty) {
            growableDataBuffer.putByte((byte)40);
            PathIterator pathIterator = this.path.getPathIterator(null);
            while (!pathIterator.isDone()) {
                int n = pathIterator.currentSegment(this.coords);
                growableDataBuffer.putByte(pgtype[n]);
                for (int i = 0; i < numsegs[n]; ++i) {
                    growableDataBuffer.putFloat(this.coords[i]);
                }
                pathIterator.next();
            }
            growableDataBuffer.putByte((byte)46);
            this.pathDirty = false;
        }
        growableDataBuffer.putByte(by);
    }

    private void writePaint(Paint paint, byte by) {
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        growableDataBuffer.putByte(by);
        growableDataBuffer.putObject(Toolkit.getPaintAccessor().getPlatformPaint(paint));
    }

    private void writeArcType(ArcType arcType) {
        byte by;
        switch (arcType) {
            case OPEN: {
                by = 0;
                break;
            }
            case CHORD: {
                by = 1;
                break;
            }
            case ROUND: {
                by = 2;
                break;
            }
            default: {
                return;
            }
        }
        this.writeParam(by, (byte)15);
    }

    private void writeRectParams(GrowableDataBuffer growableDataBuffer, double d, double d2, double d3, double d4, byte by) {
        growableDataBuffer.putByte(by);
        growableDataBuffer.putFloat((float)d);
        growableDataBuffer.putFloat((float)d2);
        growableDataBuffer.putFloat((float)d3);
        growableDataBuffer.putFloat((float)d4);
    }

    private void writeOp4(double d, double d2, double d3, double d4, byte by) {
        this.updateTransform();
        this.writeRectParams(this.getBuffer(), d, d2, d3, d4, by);
    }

    private void writeOp6(double d, double d2, double d3, double d4, double d5, double d6, byte by) {
        this.updateTransform();
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        growableDataBuffer.putByte(by);
        growableDataBuffer.putFloat((float)d);
        growableDataBuffer.putFloat((float)d2);
        growableDataBuffer.putFloat((float)d3);
        growableDataBuffer.putFloat((float)d4);
        growableDataBuffer.putFloat((float)d5);
        growableDataBuffer.putFloat((float)d6);
    }

    private void flushPolyBuf(GrowableDataBuffer growableDataBuffer, float[] fArray, int n, byte by) {
        this.curState.transform.transform(fArray, 0, fArray, 0, n / 2);
        for (int i = 0; i < n; i += 2) {
            growableDataBuffer.putByte(by);
            growableDataBuffer.putFloat(fArray[i]);
            growableDataBuffer.putFloat(fArray[i + 1]);
            by = (byte)42;
        }
    }

    private void writePoly(double[] dArray, double[] dArray2, int n, boolean bl, byte by) {
        if (dArray == null || dArray2 == null) {
            return;
        }
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        growableDataBuffer.putByte((byte)40);
        int n2 = 0;
        int n3 = 41;
        for (int i = 0; i < n; ++i) {
            if (n2 >= this.polybuf.length) {
                this.flushPolyBuf(growableDataBuffer, this.polybuf, n2, (byte)n3);
                n2 = 0;
                n3 = 42;
            }
            this.polybuf[n2++] = (float)dArray[i];
            this.polybuf[n2++] = (float)dArray2[i];
        }
        this.flushPolyBuf(growableDataBuffer, this.polybuf, n2, (byte)n3);
        if (bl) {
            growableDataBuffer.putByte((byte)45);
        }
        growableDataBuffer.putByte((byte)46);
        this.updateTransform();
        growableDataBuffer.putByte(by);
        this.markPathDirty();
    }

    private void writeImage(Image image, double d, double d2, double d3, double d4) {
        if (image == null || image.getProgress() < 1.0) {
            return;
        }
        Object object = Toolkit.getImageAccessor().getPlatformImage(image);
        if (object == null) {
            return;
        }
        this.updateTransform();
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        this.writeRectParams(growableDataBuffer, d, d2, d3, d4, (byte)50);
        growableDataBuffer.putObject(object);
    }

    private void writeImage(Image image, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8) {
        if (image == null || image.getProgress() < 1.0) {
            return;
        }
        Object object = Toolkit.getImageAccessor().getPlatformImage(image);
        if (object == null) {
            return;
        }
        this.updateTransform();
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        this.writeRectParams(growableDataBuffer, d, d2, d3, d4, (byte)51);
        growableDataBuffer.putFloat((float)d5);
        growableDataBuffer.putFloat((float)d6);
        growableDataBuffer.putFloat((float)d7);
        growableDataBuffer.putFloat((float)d8);
        growableDataBuffer.putObject(object);
    }

    private void writeText(String string, double d, double d2, double d3, byte by) {
        if (string == null) {
            return;
        }
        this.updateTransform();
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        growableDataBuffer.putByte(by);
        growableDataBuffer.putFloat((float)d);
        growableDataBuffer.putFloat((float)d2);
        growableDataBuffer.putFloat((float)d3);
        growableDataBuffer.putBoolean(this.theCanvas.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT);
        growableDataBuffer.putObject(string);
    }

    void writeParam(double d, byte by) {
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        growableDataBuffer.putByte(by);
        growableDataBuffer.putFloat((float)d);
    }

    private void writeParam(byte by, byte by2) {
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        growableDataBuffer.putByte(by2);
        growableDataBuffer.putByte(by);
    }

    private void updateTransform() {
        if (this.txdirty) {
            this.txdirty = false;
            GrowableDataBuffer growableDataBuffer = this.getBuffer();
            growableDataBuffer.putByte((byte)11);
            growableDataBuffer.putDouble(this.curState.transform.getMxx());
            growableDataBuffer.putDouble(this.curState.transform.getMxy());
            growableDataBuffer.putDouble(this.curState.transform.getMxt());
            growableDataBuffer.putDouble(this.curState.transform.getMyx());
            growableDataBuffer.putDouble(this.curState.transform.getMyy());
            growableDataBuffer.putDouble(this.curState.transform.getMyt());
        }
    }

    void updateDimensions() {
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        growableDataBuffer.putByte((byte)71);
        growableDataBuffer.putFloat((float)this.theCanvas.getWidth());
        growableDataBuffer.putFloat((float)this.theCanvas.getHeight());
    }

    private void reset() {
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        if (growableDataBuffer.writeValuePosition() > 1024 || this.theCanvas.isRendererFallingBehind()) {
            growableDataBuffer.reset();
            growableDataBuffer.putByte((byte)70);
            this.updateDimensions();
            this.txdirty = true;
            this.pathDirty = true;
            State state = this.curState;
            int n = this.curState.numClipPaths;
            this.curState = new State();
            for (int i = 0; i < n; ++i) {
                Path2D path2D = this.clipStack.get(i);
                growableDataBuffer.putByte((byte)13);
                growableDataBuffer.putObject(path2D);
            }
            this.curState.numClipPaths = n;
            state.restore(this);
        }
    }

    private void resetIfCovers(Paint paint, double d, double d2, double d3, double d4) {
        Affine2D affine2D = this.curState.transform;
        if (affine2D.isTranslateOrIdentity()) {
            d2 += affine2D.getMyt();
            if ((d += affine2D.getMxt()) > 0.0 || d2 > 0.0 || d + d3 < this.theCanvas.getWidth() || d2 + d4 < this.theCanvas.getHeight()) {
                return;
            }
        } else {
            return;
        }
        if (paint != null) {
            if (this.curState.blendop != BlendMode.SRC_OVER) {
                return;
            }
            if (!paint.isOpaque() || this.curState.globalAlpha < 1.0) {
                return;
            }
        }
        if (this.curState.numClipPaths > 0) {
            return;
        }
        if (this.curState.effect != null) {
            return;
        }
        this.reset();
    }

    public Canvas getCanvas() {
        return this.theCanvas;
    }

    public void save() {
        this.stateStack.push(this.curState.copy());
    }

    public void restore() {
        if (!this.stateStack.isEmpty()) {
            State state = this.stateStack.pop();
            state.restore(this);
            this.txdirty = true;
        }
    }

    public void translate(double d, double d2) {
        this.curState.transform.translate(d, d2);
        this.txdirty = true;
    }

    public void scale(double d, double d2) {
        this.curState.transform.scale(d, d2);
        this.txdirty = true;
    }

    public void rotate(double d) {
        this.curState.transform.rotate(Math.toRadians(d));
        this.txdirty = true;
    }

    public void transform(double d, double d2, double d3, double d4, double d5, double d6) {
        this.curState.transform.concatenate(d, d3, d5, d2, d4, d6);
        this.txdirty = true;
    }

    public void transform(Affine affine) {
        if (affine == null) {
            return;
        }
        this.curState.transform.concatenate(affine.getMxx(), affine.getMxy(), affine.getTx(), affine.getMyx(), affine.getMyy(), affine.getTy());
        this.txdirty = true;
    }

    public void setTransform(double d, double d2, double d3, double d4, double d5, double d6) {
        this.curState.transform.setTransform(d, d2, d3, d4, d5, d6);
        this.txdirty = true;
    }

    public void setTransform(Affine affine) {
        this.curState.transform.setTransform(affine.getMxx(), affine.getMyx(), affine.getMxy(), affine.getMyy(), affine.getTx(), affine.getTy());
        this.txdirty = true;
    }

    public Affine getTransform(Affine affine) {
        if (affine == null) {
            affine = new Affine();
        }
        affine.setMxx(this.curState.transform.getMxx());
        affine.setMxy(this.curState.transform.getMxy());
        affine.setMxz(0.0);
        affine.setTx(this.curState.transform.getMxt());
        affine.setMyx(this.curState.transform.getMyx());
        affine.setMyy(this.curState.transform.getMyy());
        affine.setMyz(0.0);
        affine.setTy(this.curState.transform.getMyt());
        affine.setMzx(0.0);
        affine.setMzy(0.0);
        affine.setMzz(1.0);
        affine.setTz(0.0);
        return affine;
    }

    public Affine getTransform() {
        return this.getTransform(null);
    }

    public void setGlobalAlpha(double d) {
        if (this.curState.globalAlpha != d) {
            this.curState.globalAlpha = d;
            d = d > 1.0 ? 1.0 : (d < 0.0 ? 0.0 : d);
            this.writeParam(d, (byte)0);
        }
    }

    public double getGlobalAlpha() {
        return this.curState.globalAlpha;
    }

    public void setGlobalBlendMode(BlendMode blendMode) {
        if (blendMode != null && blendMode != this.curState.blendop) {
            GrowableDataBuffer growableDataBuffer = this.getBuffer();
            this.curState.blendop = blendMode;
            growableDataBuffer.putByte((byte)1);
            growableDataBuffer.putObject((Object)EffectHelper.getToolkitBlendMode(blendMode));
        }
    }

    public BlendMode getGlobalBlendMode() {
        return this.curState.blendop;
    }

    public void setFill(Paint paint) {
        if (paint != null && this.curState.fill != paint) {
            this.curState.fill = paint;
            this.writePaint(paint, (byte)2);
        }
    }

    public Paint getFill() {
        return this.curState.fill;
    }

    public void setStroke(Paint paint) {
        if (paint != null && this.curState.stroke != paint) {
            this.curState.stroke = paint;
            this.writePaint(paint, (byte)3);
        }
    }

    public Paint getStroke() {
        return this.curState.stroke;
    }

    public void setLineWidth(double d) {
        if (d > 0.0 && d < Double.POSITIVE_INFINITY && this.curState.linewidth != d) {
            this.curState.linewidth = d;
            this.writeParam(d, (byte)4);
        }
    }

    public double getLineWidth() {
        return this.curState.linewidth;
    }

    public void setLineCap(StrokeLineCap strokeLineCap) {
        if (strokeLineCap != null && this.curState.linecap != strokeLineCap) {
            byte by;
            switch (strokeLineCap) {
                case BUTT: {
                    by = 0;
                    break;
                }
                case ROUND: {
                    by = 1;
                    break;
                }
                case SQUARE: {
                    by = 2;
                    break;
                }
                default: {
                    return;
                }
            }
            this.curState.linecap = strokeLineCap;
            this.writeParam(by, (byte)5);
        }
    }

    public StrokeLineCap getLineCap() {
        return this.curState.linecap;
    }

    public void setLineJoin(StrokeLineJoin strokeLineJoin) {
        if (strokeLineJoin != null && this.curState.linejoin != strokeLineJoin) {
            byte by;
            switch (strokeLineJoin) {
                case MITER: {
                    by = 0;
                    break;
                }
                case BEVEL: {
                    by = 2;
                    break;
                }
                case ROUND: {
                    by = 1;
                    break;
                }
                default: {
                    return;
                }
            }
            this.curState.linejoin = strokeLineJoin;
            this.writeParam(by, (byte)6);
        }
    }

    public StrokeLineJoin getLineJoin() {
        return this.curState.linejoin;
    }

    public void setMiterLimit(double d) {
        if (d > 0.0 && d < Double.POSITIVE_INFINITY && this.curState.miterlimit != d) {
            this.curState.miterlimit = d;
            this.writeParam(d, (byte)7);
        }
    }

    public double getMiterLimit() {
        return this.curState.miterlimit;
    }

    public void setLineDashes(double ... dArray) {
        if (dArray == null || dArray.length == 0) {
            if (this.curState.dashes == null) {
                return;
            }
            this.curState.dashes = null;
        } else {
            int n;
            boolean bl = true;
            for (n = 0; n < dArray.length; ++n) {
                double d = dArray[n];
                if (d >= 0.0 && d < Double.POSITIVE_INFINITY) {
                    if (!(d > 0.0)) continue;
                    bl = false;
                    continue;
                }
                return;
            }
            if (bl) {
                if (this.curState.dashes == null) {
                    return;
                }
                this.curState.dashes = null;
            } else {
                n = dArray.length;
                if ((n & 1) == 0) {
                    this.curState.dashes = Arrays.copyOf(dArray, n);
                } else {
                    this.curState.dashes = Arrays.copyOf(dArray, n * 2);
                    System.arraycopy(dArray, 0, this.curState.dashes, n, n);
                }
            }
        }
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        growableDataBuffer.putByte((byte)17);
        growableDataBuffer.putObject(this.curState.dashes);
    }

    public double[] getLineDashes() {
        if (this.curState.dashes == null) {
            return null;
        }
        return Arrays.copyOf(this.curState.dashes, this.curState.dashes.length);
    }

    public void setLineDashOffset(double d) {
        if (d > Double.NEGATIVE_INFINITY && d < Double.POSITIVE_INFINITY) {
            this.curState.dashOffset = d;
            this.writeParam(d, (byte)18);
        }
    }

    public double getLineDashOffset() {
        return this.curState.dashOffset;
    }

    public void setFont(Font font) {
        if (font != null && this.curState.font != font) {
            this.curState.font = font;
            GrowableDataBuffer growableDataBuffer = this.getBuffer();
            growableDataBuffer.putByte((byte)8);
            growableDataBuffer.putObject(FontHelper.getNativeFont(font));
        }
    }

    public Font getFont() {
        return this.curState.font;
    }

    public void setFontSmoothingType(FontSmoothingType fontSmoothingType) {
        if (fontSmoothingType != null && fontSmoothingType != this.curState.fontsmoothing) {
            this.curState.fontsmoothing = fontSmoothingType;
            this.writeParam((byte)fontSmoothingType.ordinal(), (byte)19);
        }
    }

    public FontSmoothingType getFontSmoothingType() {
        return this.curState.fontsmoothing;
    }

    public void setTextAlign(TextAlignment textAlignment) {
        if (textAlignment != null && this.curState.textalign != textAlignment) {
            byte by;
            switch (textAlignment) {
                case LEFT: {
                    by = 0;
                    break;
                }
                case CENTER: {
                    by = 1;
                    break;
                }
                case RIGHT: {
                    by = 2;
                    break;
                }
                case JUSTIFY: {
                    by = 3;
                    break;
                }
                default: {
                    return;
                }
            }
            this.curState.textalign = textAlignment;
            this.writeParam(by, (byte)9);
        }
    }

    public TextAlignment getTextAlign() {
        return this.curState.textalign;
    }

    public void setTextBaseline(VPos vPos) {
        if (vPos != null && this.curState.textbaseline != vPos) {
            byte by;
            switch (vPos) {
                case TOP: {
                    by = 0;
                    break;
                }
                case CENTER: {
                    by = 1;
                    break;
                }
                case BASELINE: {
                    by = 2;
                    break;
                }
                case BOTTOM: {
                    by = 3;
                    break;
                }
                default: {
                    return;
                }
            }
            this.curState.textbaseline = vPos;
            this.writeParam(by, (byte)10);
        }
    }

    public VPos getTextBaseline() {
        return this.curState.textbaseline;
    }

    public void fillText(String string, double d, double d2) {
        this.writeText(string, d, d2, 0.0, (byte)30);
    }

    public void strokeText(String string, double d, double d2) {
        this.writeText(string, d, d2, 0.0, (byte)31);
    }

    public void fillText(String string, double d, double d2, double d3) {
        if (d3 <= 0.0) {
            return;
        }
        this.writeText(string, d, d2, d3, (byte)30);
    }

    public void strokeText(String string, double d, double d2, double d3) {
        if (d3 <= 0.0) {
            return;
        }
        this.writeText(string, d, d2, d3, (byte)31);
    }

    public void setFillRule(FillRule fillRule) {
        if (fillRule != null && this.curState.fillRule != fillRule) {
            byte by = fillRule == FillRule.EVEN_ODD ? (byte)1 : 0;
            this.curState.fillRule = fillRule;
            this.writeParam(by, (byte)16);
        }
    }

    public FillRule getFillRule() {
        return this.curState.fillRule;
    }

    public void beginPath() {
        this.path.reset();
        this.markPathDirty();
    }

    public void moveTo(double d, double d2) {
        this.coords[0] = (float)d;
        this.coords[1] = (float)d2;
        this.curState.transform.transform(this.coords, 0, this.coords, 0, 1);
        this.path.moveTo(this.coords[0], this.coords[1]);
        this.markPathDirty();
    }

    public void lineTo(double d, double d2) {
        this.coords[0] = (float)d;
        this.coords[1] = (float)d2;
        this.curState.transform.transform(this.coords, 0, this.coords, 0, 1);
        if (this.path.getNumCommands() == 0) {
            this.path.moveTo(this.coords[0], this.coords[1]);
        }
        this.path.lineTo(this.coords[0], this.coords[1]);
        this.markPathDirty();
    }

    public void quadraticCurveTo(double d, double d2, double d3, double d4) {
        this.coords[0] = (float)d;
        this.coords[1] = (float)d2;
        this.coords[2] = (float)d3;
        this.coords[3] = (float)d4;
        this.curState.transform.transform(this.coords, 0, this.coords, 0, 2);
        if (this.path.getNumCommands() == 0) {
            this.path.moveTo(this.coords[0], this.coords[1]);
        }
        this.path.quadTo(this.coords[0], this.coords[1], this.coords[2], this.coords[3]);
        this.markPathDirty();
    }

    public void bezierCurveTo(double d, double d2, double d3, double d4, double d5, double d6) {
        this.coords[0] = (float)d;
        this.coords[1] = (float)d2;
        this.coords[2] = (float)d3;
        this.coords[3] = (float)d4;
        this.coords[4] = (float)d5;
        this.coords[5] = (float)d6;
        this.curState.transform.transform(this.coords, 0, this.coords, 0, 3);
        if (this.path.getNumCommands() == 0) {
            this.path.moveTo(this.coords[0], this.coords[1]);
        }
        this.path.curveTo(this.coords[0], this.coords[1], this.coords[2], this.coords[3], this.coords[4], this.coords[5]);
        this.markPathDirty();
    }

    public void arcTo(double d, double d2, double d3, double d4, double d5) {
        if (this.path.getNumCommands() == 0) {
            this.moveTo(d, d2);
            this.lineTo(d, d2);
        } else if (!this.tryArcTo((float)d, (float)d2, (float)d3, (float)d4, (float)d5)) {
            this.lineTo(d, d2);
        }
    }

    private static double lenSq(double d, double d2, double d3, double d4) {
        return (d3 -= d) * d3 + (d4 -= d2) * d4;
    }

    private boolean tryArcTo(float f, float f2, float f3, float f4, float f5) {
        boolean bl;
        double d;
        double d2;
        double d3;
        double d4;
        float f6;
        float f7;
        if (this.curState.transform.isTranslateOrIdentity()) {
            f7 = (float)((double)this.path.getCurrentX() - this.curState.transform.getMxt());
            f6 = (float)((double)this.path.getCurrentY() - this.curState.transform.getMyt());
        } else {
            this.coords[0] = this.path.getCurrentX();
            this.coords[1] = this.path.getCurrentY();
            try {
                this.curState.transform.inverseTransform(this.coords, 0, this.coords, 0, 1);
            }
            catch (NoninvertibleTransformException noninvertibleTransformException) {
                return false;
            }
            f7 = this.coords[0];
            f6 = this.coords[1];
        }
        double d5 = GraphicsContext.lenSq(f7, f6, f, f2);
        double d6 = GraphicsContext.lenSq(f, f2, f3, f4);
        double d7 = GraphicsContext.lenSq(f7, f6, f3, f4);
        double d8 = Math.sqrt(d5);
        double d9 = Math.sqrt(d6);
        double d10 = d5 + d6 - d7;
        double d11 = 2.0 * d8 * d9;
        if (d11 == 0.0 || f5 <= 0.0f) {
            return false;
        }
        double d12 = d10 / d11;
        double d13 = 1.0 + d12;
        if (d13 == 0.0) {
            return false;
        }
        double d14 = (1.0 - d12) / d13;
        double d15 = (double)f5 / Math.sqrt(d14);
        double d16 = (double)f + d15 / d8 * (double)(f7 - f);
        double d17 = (d16 + (d4 = (double)f + d15 / d9 * (double)(f3 - f))) / 2.0;
        double d18 = GraphicsContext.lenSq(d17, d3 = ((d2 = (double)f2 + d15 / d8 * (double)(f6 - f2)) + (d = (double)f2 + d15 / d9 * (double)(f4 - f2))) / 2.0, f, f2);
        if (d18 == 0.0) {
            return false;
        }
        double d19 = GraphicsContext.lenSq(d17, d3, d16, d2) / d18;
        double d20 = d17 + (d17 - (double)f) * d19;
        double d21 = d3 + (d3 - (double)f2) * d19;
        if (d20 != d20 || d21 != d21) {
            return false;
        }
        if (d16 != (double)f7 || d2 != (double)f6) {
            this.lineTo(d16, d2);
        }
        double d22 = Math.sqrt((1.0 - d12) / 2.0);
        boolean bl2 = bl = (d2 - d21) * (d4 - d20) > (d - d21) * (d16 - d20);
        if (d12 <= 0.0) {
            double d23 = Math.sqrt((1.0 + d12) / 2.0);
            double d24 = 1.3333333333333333 * d23 / (1.0 + d22);
            if (bl) {
                d24 = -d24;
            }
            double d25 = d16 - d24 * (d2 - d21);
            double d26 = d2 + d24 * (d16 - d20);
            double d27 = d4 + d24 * (d - d21);
            double d28 = d - d24 * (d4 - d20);
            this.bezierCurveTo(d25, d26, d27, d28, d4, d);
        } else {
            double d29 = Math.sqrt((1.0 - d22) / 2.0);
            double d30 = Math.sqrt((1.0 + d22) / 2.0);
            double d31 = 1.3333333333333333 * d29 / (1.0 + d30);
            if (bl) {
                d31 = -d31;
            }
            double d32 = (double)f5 / Math.sqrt(d18);
            double d33 = d20 + ((double)f - d17) * d32;
            double d34 = d21 + ((double)f2 - d3) * d32;
            double d35 = d16 - d31 * (d2 - d21);
            double d36 = d2 + d31 * (d16 - d20);
            double d37 = d33 + d31 * (d34 - d21);
            double d38 = d34 - d31 * (d33 - d20);
            this.bezierCurveTo(d35, d36, d37, d38, d33, d34);
            d35 = d33 - d31 * (d34 - d21);
            d36 = d34 + d31 * (d33 - d20);
            d37 = d4 + d31 * (d - d21);
            d38 = d - d31 * (d4 - d20);
            this.bezierCurveTo(d35, d36, d37, d38, d4, d);
        }
        return true;
    }

    public void arc(double d, double d2, double d3, double d4, double d5, double d6) {
        Arc2D arc2D = new Arc2D((float)(d - d3), (float)(d2 - d4), (float)(d3 * 2.0), (float)(d4 * 2.0), (float)d5, (float)d6, 0);
        this.path.append(arc2D.getPathIterator(this.curState.transform), true);
        this.markPathDirty();
    }

    public void rect(double d, double d2, double d3, double d4) {
        this.coords[0] = (float)d;
        this.coords[1] = (float)d2;
        this.coords[2] = (float)d3;
        this.coords[3] = 0.0f;
        this.coords[4] = 0.0f;
        this.coords[5] = (float)d4;
        this.curState.transform.deltaTransform(this.coords, 0, this.coords, 0, 3);
        float f = this.coords[0] + (float)this.curState.transform.getMxt();
        float f2 = this.coords[1] + (float)this.curState.transform.getMyt();
        float f3 = this.coords[2];
        float f4 = this.coords[3];
        float f5 = this.coords[4];
        float f6 = this.coords[5];
        this.path.moveTo(f, f2);
        this.path.lineTo(f + f3, f2 + f4);
        this.path.lineTo(f + f3 + f5, f2 + f4 + f6);
        this.path.lineTo(f + f5, f2 + f6);
        this.path.closePath();
        this.markPathDirty();
    }

    public void appendSVGPath(String string) {
        boolean bl;
        boolean bl2;
        block17: {
            if (string == null) {
                return;
            }
            bl2 = true;
            bl = true;
            block9: for (int i = 0; i < string.length(); ++i) {
                switch (string.charAt(i)) {
                    case '\t': 
                    case '\n': 
                    case '\r': 
                    case ' ': {
                        continue block9;
                    }
                    case 'M': {
                        bl = false;
                        bl2 = false;
                        break block17;
                    }
                    case 'm': {
                        if (this.path.getNumCommands() == 0) {
                            bl2 = false;
                        }
                        bl = false;
                    }
                }
            }
        }
        Path2D path2D = new Path2D();
        if (bl2 && this.path.getNumCommands() > 0) {
            float f;
            float f2;
            if (this.curState.transform.isTranslateOrIdentity()) {
                f2 = (float)((double)this.path.getCurrentX() - this.curState.transform.getMxt());
                f = (float)((double)this.path.getCurrentY() - this.curState.transform.getMyt());
            } else {
                this.coords[0] = this.path.getCurrentX();
                this.coords[1] = this.path.getCurrentY();
                try {
                    this.curState.transform.inverseTransform(this.coords, 0, this.coords, 0, 1);
                }
                catch (NoninvertibleTransformException noninvertibleTransformException) {
                    // empty catch block
                }
                f2 = this.coords[0];
                f = this.coords[1];
            }
            path2D.moveTo(f2, f);
        } else {
            bl = false;
        }
        try {
            path2D.appendSVGPath(string);
            PathIterator pathIterator = path2D.getPathIterator(this.curState.transform);
            if (bl) {
                pathIterator.next();
            }
            this.path.append(pathIterator, false);
        }
        catch (IllegalPathStateException | IllegalArgumentException runtimeException) {
            // empty catch block
        }
    }

    public void closePath() {
        if (this.path.getNumCommands() > 0) {
            this.path.closePath();
            this.markPathDirty();
        }
    }

    public void fill() {
        this.writePath((byte)47);
    }

    public void stroke() {
        this.writePath((byte)48);
    }

    public void clip() {
        Path2D path2D = new Path2D(this.path);
        this.clipStack.addLast(path2D);
        ++this.curState.numClipPaths;
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        growableDataBuffer.putByte((byte)13);
        growableDataBuffer.putObject(path2D);
    }

    public boolean isPointInPath(double d, double d2) {
        return this.path.contains((float)d, (float)d2);
    }

    public void clearRect(double d, double d2, double d3, double d4) {
        if (d3 != 0.0 && d4 != 0.0) {
            this.resetIfCovers(null, d, d2, d3, d4);
            this.writeOp4(d, d2, d3, d4, (byte)22);
        }
    }

    public void fillRect(double d, double d2, double d3, double d4) {
        if (d3 != 0.0 && d4 != 0.0) {
            this.resetIfCovers(this.curState.fill, d, d2, d3, d4);
            this.writeOp4(d, d2, d3, d4, (byte)20);
        }
    }

    public void strokeRect(double d, double d2, double d3, double d4) {
        if (d3 != 0.0 || d4 != 0.0) {
            this.writeOp4(d, d2, d3, d4, (byte)21);
        }
    }

    public void fillOval(double d, double d2, double d3, double d4) {
        if (d3 != 0.0 && d4 != 0.0) {
            this.writeOp4(d, d2, d3, d4, (byte)24);
        }
    }

    public void strokeOval(double d, double d2, double d3, double d4) {
        if (d3 != 0.0 || d4 != 0.0) {
            this.writeOp4(d, d2, d3, d4, (byte)25);
        }
    }

    public void fillArc(double d, double d2, double d3, double d4, double d5, double d6, ArcType arcType) {
        if (d3 != 0.0 && d4 != 0.0 && arcType != null) {
            this.writeArcType(arcType);
            this.writeOp6(d, d2, d3, d4, d5, d6, (byte)28);
        }
    }

    public void strokeArc(double d, double d2, double d3, double d4, double d5, double d6, ArcType arcType) {
        if (d3 != 0.0 && d4 != 0.0 && arcType != null) {
            this.writeArcType(arcType);
            this.writeOp6(d, d2, d3, d4, d5, d6, (byte)29);
        }
    }

    public void fillRoundRect(double d, double d2, double d3, double d4, double d5, double d6) {
        if (d3 != 0.0 && d4 != 0.0) {
            this.writeOp6(d, d2, d3, d4, d5, d6, (byte)26);
        }
    }

    public void strokeRoundRect(double d, double d2, double d3, double d4, double d5, double d6) {
        if (d3 != 0.0 && d4 != 0.0) {
            this.writeOp6(d, d2, d3, d4, d5, d6, (byte)27);
        }
    }

    public void strokeLine(double d, double d2, double d3, double d4) {
        this.writeOp4(d, d2, d3, d4, (byte)23);
    }

    public void fillPolygon(double[] dArray, double[] dArray2, int n) {
        if (n >= 3) {
            this.writePoly(dArray, dArray2, n, true, (byte)47);
        }
    }

    public void strokePolygon(double[] dArray, double[] dArray2, int n) {
        if (n >= 2) {
            this.writePoly(dArray, dArray2, n, true, (byte)48);
        }
    }

    public void strokePolyline(double[] dArray, double[] dArray2, int n) {
        if (n >= 2) {
            this.writePoly(dArray, dArray2, n, false, (byte)48);
        }
    }

    public void drawImage(Image image, double d, double d2) {
        if (image == null) {
            return;
        }
        double d3 = image.getWidth();
        double d4 = image.getHeight();
        this.writeImage(image, d, d2, d3, d4);
    }

    public void drawImage(Image image, double d, double d2, double d3, double d4) {
        this.writeImage(image, d, d2, d3, d4);
    }

    public void drawImage(Image image, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8) {
        this.writeImage(image, d5, d6, d7, d8, d, d2, d3, d4);
    }

    public PixelWriter getPixelWriter() {
        if (this.writer == null) {
            this.writer = new PixelWriter(){

                @Override
                public PixelFormat<ByteBuffer> getPixelFormat() {
                    return PixelFormat.getByteBgraPreInstance();
                }

                private BytePixelSetter getSetter() {
                    return ByteBgraPre.setter;
                }

                @Override
                public void setArgb(int n, int n2, int n3) {
                    GrowableDataBuffer growableDataBuffer = GraphicsContext.this.getBuffer();
                    growableDataBuffer.putByte((byte)52);
                    growableDataBuffer.putInt(n);
                    growableDataBuffer.putInt(n2);
                    growableDataBuffer.putInt(n3);
                }

                @Override
                public void setColor(int n, int n2, Color color) {
                    if (color == null) {
                        throw new NullPointerException("Color cannot be null");
                    }
                    int n3 = (int)Math.round(color.getOpacity() * 255.0);
                    int n4 = (int)Math.round(color.getRed() * 255.0);
                    int n5 = (int)Math.round(color.getGreen() * 255.0);
                    int n6 = (int)Math.round(color.getBlue() * 255.0);
                    this.setArgb(n, n2, n3 << 24 | n4 << 16 | n5 << 8 | n6);
                }

                private void writePixelBuffer(int n, int n2, int n3, int n4, byte[] byArray) {
                    GrowableDataBuffer growableDataBuffer = GraphicsContext.this.getBuffer();
                    growableDataBuffer.putByte((byte)53);
                    growableDataBuffer.putInt(n);
                    growableDataBuffer.putInt(n2);
                    growableDataBuffer.putInt(n3);
                    growableDataBuffer.putInt(n4);
                    growableDataBuffer.putObject(byArray);
                }

                private int[] checkBounds(int n, int n2, int n3, int n4, PixelFormat<? extends Buffer> pixelFormat, int n5) {
                    int n6 = (int)Math.ceil(GraphicsContext.this.theCanvas.getWidth());
                    int n7 = (int)Math.ceil(GraphicsContext.this.theCanvas.getHeight());
                    if (n >= 0 && n2 >= 0 && n + n3 <= n6 && n2 + n4 <= n7) {
                        return null;
                    }
                    int n8 = 0;
                    if (n < 0) {
                        if ((n3 += n) < 0) {
                            return null;
                        }
                        if (pixelFormat != null) {
                            switch (pixelFormat.getType()) {
                                case BYTE_BGRA: 
                                case BYTE_BGRA_PRE: {
                                    n8 -= n * 4;
                                    break;
                                }
                                case BYTE_RGB: {
                                    n8 -= n * 3;
                                    break;
                                }
                                case BYTE_INDEXED: 
                                case INT_ARGB: 
                                case INT_ARGB_PRE: {
                                    n8 -= n;
                                    break;
                                }
                                default: {
                                    throw new InternalError("unknown Pixel Format");
                                }
                            }
                        }
                        n = 0;
                    }
                    if (n2 < 0) {
                        if ((n4 += n2) < 0) {
                            return null;
                        }
                        n8 -= n2 * n5;
                        n2 = 0;
                    }
                    if (n + n3 > n6 && (n3 = n6 - n) < 0) {
                        return null;
                    }
                    if (n2 + n4 > n7 && (n4 = n7 - n2) < 0) {
                        return null;
                    }
                    return new int[]{n, n2, n3, n4, n8};
                }

                @Override
                public <T extends Buffer> void setPixels(int n, int n2, int n3, int n4, PixelFormat<T> pixelFormat, T t, int n5) {
                    if (pixelFormat == null) {
                        throw new NullPointerException("PixelFormat cannot be null");
                    }
                    if (t == null) {
                        throw new NullPointerException("Buffer cannot be null");
                    }
                    if (n3 <= 0 || n4 <= 0) {
                        return;
                    }
                    int n6 = t.position();
                    int[] nArray = this.checkBounds(n, n2, n3, n4, pixelFormat, n5);
                    if (nArray != null) {
                        n = nArray[0];
                        n2 = nArray[1];
                        n3 = nArray[2];
                        n4 = nArray[3];
                        n6 += nArray[4];
                    }
                    byte[] byArray = new byte[n3 * n4 * 4];
                    ByteBuffer byteBuffer = ByteBuffer.wrap(byArray);
                    PixelGetter<T> pixelGetter = PixelUtils.getGetter(pixelFormat);
                    PixelConverter<T, ByteBuffer> pixelConverter = PixelUtils.getConverter(pixelGetter, this.getSetter());
                    pixelConverter.convert(t, n6, n5, byteBuffer, 0, n3 * 4, n3, n4);
                    this.writePixelBuffer(n, n2, n3, n4, byArray);
                }

                @Override
                public void setPixels(int n, int n2, int n3, int n4, PixelFormat<ByteBuffer> pixelFormat, byte[] byArray, int n5, int n6) {
                    if (pixelFormat == null) {
                        throw new NullPointerException("PixelFormat cannot be null");
                    }
                    if (byArray == null) {
                        throw new NullPointerException("Buffer cannot be null");
                    }
                    if (n3 <= 0 || n4 <= 0) {
                        return;
                    }
                    int[] nArray = this.checkBounds(n, n2, n3, n4, pixelFormat, n6);
                    if (nArray != null) {
                        n = nArray[0];
                        n2 = nArray[1];
                        n3 = nArray[2];
                        n4 = nArray[3];
                        n5 += nArray[4];
                    }
                    byte[] byArray2 = new byte[n3 * n4 * 4];
                    BytePixelGetter bytePixelGetter = PixelUtils.getByteGetter(pixelFormat);
                    ByteToBytePixelConverter byteToBytePixelConverter = PixelUtils.getB2BConverter(bytePixelGetter, this.getSetter());
                    byteToBytePixelConverter.convert(byArray, n5, n6, byArray2, 0, n3 * 4, n3, n4);
                    this.writePixelBuffer(n, n2, n3, n4, byArray2);
                }

                @Override
                public void setPixels(int n, int n2, int n3, int n4, PixelFormat<IntBuffer> pixelFormat, int[] nArray, int n5, int n6) {
                    if (pixelFormat == null) {
                        throw new NullPointerException("PixelFormat cannot be null");
                    }
                    if (nArray == null) {
                        throw new NullPointerException("Buffer cannot be null");
                    }
                    if (n3 <= 0 || n4 <= 0) {
                        return;
                    }
                    int[] nArray2 = this.checkBounds(n, n2, n3, n4, pixelFormat, n6);
                    if (nArray2 != null) {
                        n = nArray2[0];
                        n2 = nArray2[1];
                        n3 = nArray2[2];
                        n4 = nArray2[3];
                        n5 += nArray2[4];
                    }
                    byte[] byArray = new byte[n3 * n4 * 4];
                    IntPixelGetter intPixelGetter = PixelUtils.getIntGetter(pixelFormat);
                    IntToBytePixelConverter intToBytePixelConverter = PixelUtils.getI2BConverter(intPixelGetter, this.getSetter());
                    intToBytePixelConverter.convert(nArray, n5, n6, byArray, 0, n3 * 4, n3, n4);
                    this.writePixelBuffer(n, n2, n3, n4, byArray);
                }

                @Override
                public void setPixels(int n, int n2, int n3, int n4, PixelReader pixelReader, int n5, int n6) {
                    if (pixelReader == null) {
                        throw new NullPointerException("Reader cannot be null");
                    }
                    if (n3 <= 0 || n4 <= 0) {
                        return;
                    }
                    int[] nArray = this.checkBounds(n, n2, n3, n4, null, 0);
                    if (nArray != null) {
                        int n7 = nArray[0];
                        int n8 = nArray[1];
                        n5 += n7 - n;
                        n6 += n8 - n2;
                        n = n7;
                        n2 = n8;
                        n3 = nArray[2];
                        n4 = nArray[3];
                    }
                    byte[] byArray = new byte[n3 * n4 * 4];
                    pixelReader.getPixels(n5, n6, n3, n4, PixelFormat.getByteBgraPreInstance(), byArray, 0, n3 * 4);
                    this.writePixelBuffer(n, n2, n3, n4, byArray);
                }
            };
        }
        return this.writer;
    }

    public void setEffect(Effect effect) {
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        growableDataBuffer.putByte((byte)12);
        if (effect == null) {
            this.curState.effect = null;
            growableDataBuffer.putObject(null);
        } else {
            this.curState.effect = EffectHelper.copy(effect);
            EffectHelper.sync(this.curState.effect);
            growableDataBuffer.putObject(EffectHelper.getPeer(this.curState.effect));
        }
    }

    public Effect getEffect(Effect effect) {
        return this.curState.effect == null ? null : EffectHelper.copy(this.curState.effect);
    }

    public void applyEffect(Effect effect) {
        if (effect == null) {
            return;
        }
        GrowableDataBuffer growableDataBuffer = this.getBuffer();
        growableDataBuffer.putByte((byte)60);
        Effect effect2 = EffectHelper.copy(effect);
        EffectHelper.sync(effect2);
        growableDataBuffer.putObject(EffectHelper.getPeer(effect2));
    }

    static class State {
        double globalAlpha;
        BlendMode blendop;
        Affine2D transform;
        Paint fill;
        Paint stroke;
        double linewidth;
        StrokeLineCap linecap;
        StrokeLineJoin linejoin;
        double miterlimit;
        double[] dashes;
        double dashOffset;
        int numClipPaths;
        Font font;
        FontSmoothingType fontsmoothing;
        TextAlignment textalign;
        VPos textbaseline;
        Effect effect;
        FillRule fillRule;

        State() {
            this.init();
        }

        final void init() {
            this.set(1.0, BlendMode.SRC_OVER, new Affine2D(), Color.BLACK, Color.BLACK, 1.0, StrokeLineCap.SQUARE, StrokeLineJoin.MITER, 10.0, null, 0.0, 0, Font.getDefault(), FontSmoothingType.GRAY, TextAlignment.LEFT, VPos.BASELINE, null, FillRule.NON_ZERO);
        }

        State(State state) {
            this.set(state.globalAlpha, state.blendop, new Affine2D(state.transform), state.fill, state.stroke, state.linewidth, state.linecap, state.linejoin, state.miterlimit, state.dashes, state.dashOffset, state.numClipPaths, state.font, state.fontsmoothing, state.textalign, state.textbaseline, state.effect, state.fillRule);
        }

        final void set(double d, BlendMode blendMode, Affine2D affine2D, Paint paint, Paint paint2, double d2, StrokeLineCap strokeLineCap, StrokeLineJoin strokeLineJoin, double d3, double[] dArray, double d4, int n, Font font, FontSmoothingType fontSmoothingType, TextAlignment textAlignment, VPos vPos, Effect effect, FillRule fillRule) {
            this.globalAlpha = d;
            this.blendop = blendMode;
            this.transform = affine2D;
            this.fill = paint;
            this.stroke = paint2;
            this.linewidth = d2;
            this.linecap = strokeLineCap;
            this.linejoin = strokeLineJoin;
            this.miterlimit = d3;
            this.dashes = dArray;
            this.dashOffset = d4;
            this.numClipPaths = n;
            this.font = font;
            this.fontsmoothing = fontSmoothingType;
            this.textalign = textAlignment;
            this.textbaseline = vPos;
            this.effect = effect;
            this.fillRule = fillRule;
        }

        State copy() {
            return new State(this);
        }

        void restore(GraphicsContext graphicsContext) {
            graphicsContext.setGlobalAlpha(this.globalAlpha);
            graphicsContext.setGlobalBlendMode(this.blendop);
            graphicsContext.setTransform(this.transform.getMxx(), this.transform.getMyx(), this.transform.getMxy(), this.transform.getMyy(), this.transform.getMxt(), this.transform.getMyt());
            graphicsContext.setFill(this.fill);
            graphicsContext.setStroke(this.stroke);
            graphicsContext.setLineWidth(this.linewidth);
            graphicsContext.setLineCap(this.linecap);
            graphicsContext.setLineJoin(this.linejoin);
            graphicsContext.setMiterLimit(this.miterlimit);
            graphicsContext.setLineDashes(this.dashes);
            graphicsContext.setLineDashOffset(this.dashOffset);
            GrowableDataBuffer growableDataBuffer = graphicsContext.getBuffer();
            while (graphicsContext.curState.numClipPaths > this.numClipPaths) {
                --graphicsContext.curState.numClipPaths;
                graphicsContext.clipStack.removeLast();
                growableDataBuffer.putByte((byte)14);
            }
            graphicsContext.setFillRule(this.fillRule);
            graphicsContext.setFont(this.font);
            graphicsContext.setFontSmoothingType(this.fontsmoothing);
            graphicsContext.setTextAlign(this.textalign);
            graphicsContext.setTextBaseline(this.textbaseline);
            graphicsContext.setEffect(this.effect);
        }
    }
}

