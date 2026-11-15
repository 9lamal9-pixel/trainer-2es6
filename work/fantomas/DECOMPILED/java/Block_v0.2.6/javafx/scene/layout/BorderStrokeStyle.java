/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

public final class BorderStrokeStyle {
    private static final List<Double> DOTTED_LIST = Collections.unmodifiableList(BorderStrokeStyle.asList(0.0, 2.0));
    private static final List<Double> DASHED_LIST = Collections.unmodifiableList(BorderStrokeStyle.asList(2.0, 1.4));
    public static final BorderStrokeStyle NONE = new BorderStrokeStyle(StrokeType.INSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 0.0, 0.0, null);
    public static final BorderStrokeStyle DOTTED = new BorderStrokeStyle(StrokeType.INSIDE, StrokeLineJoin.MITER, StrokeLineCap.ROUND, 10.0, 0.0, DOTTED_LIST);
    public static final BorderStrokeStyle DASHED = new BorderStrokeStyle(StrokeType.INSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 10.0, 0.0, DASHED_LIST);
    public static final BorderStrokeStyle SOLID = new BorderStrokeStyle(StrokeType.INSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 10.0, 0.0, null);
    private final StrokeType type;
    private final StrokeLineJoin lineJoin;
    private final StrokeLineCap lineCap;
    private final double miterLimit;
    private final double dashOffset;
    private final List<Double> dashArray;
    private final int hash;

    public final StrokeType getType() {
        return this.type;
    }

    public final StrokeLineJoin getLineJoin() {
        return this.lineJoin;
    }

    public final StrokeLineCap getLineCap() {
        return this.lineCap;
    }

    public final double getMiterLimit() {
        return this.miterLimit;
    }

    public final double getDashOffset() {
        return this.dashOffset;
    }

    public final List<Double> getDashArray() {
        return this.dashArray;
    }

    public BorderStrokeStyle(@NamedArg(value="type") StrokeType strokeType, @NamedArg(value="lineJoin") StrokeLineJoin strokeLineJoin, @NamedArg(value="lineCap") StrokeLineCap strokeLineCap, @NamedArg(value="miterLimit") double d, @NamedArg(value="dashOffset") double d2, @NamedArg(value="dashArray") List<Double> list) {
        this.type = strokeType != null ? strokeType : StrokeType.CENTERED;
        this.lineJoin = strokeLineJoin != null ? strokeLineJoin : StrokeLineJoin.MITER;
        this.lineCap = strokeLineCap != null ? strokeLineCap : StrokeLineCap.BUTT;
        this.miterLimit = d;
        this.dashOffset = d2;
        if (list == null) {
            this.dashArray = Collections.emptyList();
        } else if (list == DASHED_LIST || list == DOTTED_LIST) {
            this.dashArray = list;
        } else {
            ArrayList<Double> arrayList = new ArrayList<Double>(list);
            this.dashArray = Collections.unmodifiableList(arrayList);
        }
        int n = this.type.hashCode();
        n = 31 * n + this.lineJoin.hashCode();
        n = 31 * n + this.lineCap.hashCode();
        long l = this.miterLimit != 0.0 ? Double.doubleToLongBits(this.miterLimit) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        l = this.dashOffset != 0.0 ? Double.doubleToLongBits(this.dashOffset) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        this.hash = n = 31 * n + this.dashArray.hashCode();
    }

    public String toString() {
        if (this == NONE) {
            return "BorderStyle.NONE";
        }
        if (this == DASHED) {
            return "BorderStyle.DASHED";
        }
        if (this == DOTTED) {
            return "BorderStyle.DOTTED";
        }
        if (this == SOLID) {
            return "BorderStyle.SOLID";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("BorderStyle: ");
        stringBuilder.append((Object)this.type);
        stringBuilder.append(", ");
        stringBuilder.append((Object)this.lineJoin);
        stringBuilder.append(", ");
        stringBuilder.append((Object)this.lineCap);
        stringBuilder.append(", ");
        stringBuilder.append(this.miterLimit);
        stringBuilder.append(", ");
        stringBuilder.append(this.dashOffset);
        stringBuilder.append(", [");
        if (this.dashArray != null) {
            stringBuilder.append(this.dashArray);
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (this == NONE && object != NONE || object == NONE && this != NONE) {
            return false;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        BorderStrokeStyle borderStrokeStyle = (BorderStrokeStyle)object;
        if (this.hash != borderStrokeStyle.hash) {
            return false;
        }
        if (Double.compare(borderStrokeStyle.dashOffset, this.dashOffset) != 0) {
            return false;
        }
        if (Double.compare(borderStrokeStyle.miterLimit, this.miterLimit) != 0) {
            return false;
        }
        if (!this.dashArray.equals(borderStrokeStyle.dashArray)) {
            return false;
        }
        if (this.lineCap != borderStrokeStyle.lineCap) {
            return false;
        }
        if (this.lineJoin != borderStrokeStyle.lineJoin) {
            return false;
        }
        return this.type == borderStrokeStyle.type;
    }

    public int hashCode() {
        return this.hash;
    }

    private static List<Double> asList(double ... dArray) {
        ArrayList<Double> arrayList = new ArrayList<Double>(dArray.length);
        for (int i = 0; i < dArray.length; ++i) {
            arrayList.add(dArray[i]);
        }
        return arrayList;
    }
}

