/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.paint;

import com.sun.javafx.beans.event.AbstractNotifyListener;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;

public abstract class Paint {
    Paint() {
    }

    boolean acc_isMutable() {
        return false;
    }

    abstract Object acc_getPlatformPaint();

    void acc_addListener(AbstractNotifyListener abstractNotifyListener) {
        throw new UnsupportedOperationException("Not Supported.");
    }

    void acc_removeListener(AbstractNotifyListener abstractNotifyListener) {
        throw new UnsupportedOperationException("Not Supported.");
    }

    public abstract boolean isOpaque();

    public static Paint valueOf(String string) {
        if (string == null) {
            throw new NullPointerException("paint must be specified");
        }
        if (string.startsWith("linear-gradient(")) {
            return LinearGradient.valueOf(string);
        }
        if (string.startsWith("radial-gradient(")) {
            return RadialGradient.valueOf(string);
        }
        return Color.valueOf(string);
    }

    static {
        Toolkit.setPaintAccessor(new Toolkit.PaintAccessor(){

            @Override
            public boolean isMutable(Paint paint) {
                return paint.acc_isMutable();
            }

            @Override
            public Object getPlatformPaint(Paint paint) {
                return paint.acc_getPlatformPaint();
            }

            @Override
            public void addListener(Paint paint, AbstractNotifyListener abstractNotifyListener) {
                paint.acc_addListener(abstractNotifyListener);
            }

            @Override
            public void removeListener(Paint paint, AbstractNotifyListener abstractNotifyListener) {
                paint.acc_removeListener(abstractNotifyListener);
            }
        });
    }
}

