/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.ReadOnlyPropertyDescriptor;
import java.lang.ref.WeakReference;

class DescriptorListenerCleaner
implements Runnable {
    private final ReadOnlyPropertyDescriptor pd;
    private final WeakReference<ReadOnlyPropertyDescriptor.ReadOnlyListener<?>> lRef;

    DescriptorListenerCleaner(ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor, ReadOnlyPropertyDescriptor.ReadOnlyListener<?> readOnlyListener) {
        this.pd = readOnlyPropertyDescriptor;
        this.lRef = new WeakReference(readOnlyListener);
    }

    @Override
    public void run() {
        ReadOnlyPropertyDescriptor.ReadOnlyListener readOnlyListener = (ReadOnlyPropertyDescriptor.ReadOnlyListener)this.lRef.get();
        if (readOnlyListener != null) {
            this.pd.removeListener(readOnlyListener);
        }
    }
}

