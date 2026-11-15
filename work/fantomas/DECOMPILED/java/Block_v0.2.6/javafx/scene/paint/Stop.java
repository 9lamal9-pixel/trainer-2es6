/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.paint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.scene.paint.Color;

public final class Stop {
    static final List<Stop> NO_STOPS = Collections.unmodifiableList(Arrays.asList(new Stop(0.0, Color.TRANSPARENT), new Stop(1.0, Color.TRANSPARENT)));
    private double offset;
    private Color color;
    private int hash = 0;

    static List<Stop> normalize(Stop[] stopArray) {
        List<Stop> list = stopArray == null ? null : Arrays.asList(stopArray);
        return Stop.normalize(list);
    }

    static List<Stop> normalize(List<Stop> list) {
        if (list == null) {
            return NO_STOPS;
        }
        Stop stop = null;
        Stop stop2 = null;
        ArrayList<Stop> arrayList = new ArrayList<Stop>(list.size());
        for (Stop stop3 : list) {
            if (stop3 == null || stop3.getColor() == null) continue;
            double d = stop3.getOffset();
            if (d <= 0.0) {
                if (stop != null && !(d >= stop.getOffset())) continue;
                stop = stop3;
                continue;
            }
            if (d >= 1.0) {
                if (stop2 != null && !(d < stop2.getOffset())) continue;
                stop2 = stop3;
                continue;
            }
            if (d != d) continue;
            for (int i = arrayList.size() - 1; i >= 0; --i) {
                Stop stop4 = (Stop)arrayList.get(i);
                if (!(stop4.getOffset() <= d)) continue;
                if (stop4.getOffset() == d) {
                    if (i > 0 && ((Stop)arrayList.get(i - 1)).getOffset() == d) {
                        arrayList.set(i, stop3);
                    } else {
                        arrayList.add(i + 1, stop3);
                    }
                } else {
                    arrayList.add(i + 1, stop3);
                }
                stop3 = null;
                break;
            }
            if (stop3 == null) continue;
            arrayList.add(0, stop3);
        }
        if (stop == null) {
            Object object;
            if (arrayList.isEmpty()) {
                if (stop2 == null) {
                    return NO_STOPS;
                }
                object = stop2.getColor();
            } else {
                object = ((Stop)arrayList.get(0)).getColor();
                if (stop2 == null && arrayList.size() == 1) {
                    arrayList.clear();
                }
            }
            stop = new Stop(0.0, (Color)object);
        } else if (stop.getOffset() < 0.0) {
            stop = new Stop(0.0, stop.getColor());
        }
        arrayList.add(0, stop);
        if (stop2 == null) {
            stop2 = new Stop(1.0, ((Stop)arrayList.get(arrayList.size() - 1)).getColor());
        } else if (stop2.getOffset() > 1.0) {
            stop2 = new Stop(1.0, stop2.getColor());
        }
        arrayList.add(stop2);
        return Collections.unmodifiableList(arrayList);
    }

    public final double getOffset() {
        return this.offset;
    }

    public final Color getColor() {
        return this.color;
    }

    public Stop(@NamedArg(value="offset") double d, @NamedArg(value="color", defaultValue="BLACK") Color color) {
        this.offset = d;
        this.color = color;
    }

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object instanceof Stop) {
            Stop stop = (Stop)object;
            return this.offset == stop.offset && (this.color == null ? stop.color == null : this.color.equals(stop.color));
        }
        return false;
    }

    public int hashCode() {
        if (this.hash == 0) {
            long l = 17L;
            l = 37L * l + Double.doubleToLongBits(this.offset);
            l = 37L * l + (long)this.color.hashCode();
            this.hash = (int)(l ^ l >> 32);
        }
        return this.hash;
    }

    public String toString() {
        return this.color + " " + this.offset * 100.0 + "%";
    }
}

