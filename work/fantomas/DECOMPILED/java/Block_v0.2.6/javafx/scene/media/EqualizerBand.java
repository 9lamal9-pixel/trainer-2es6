/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;

public final class EqualizerBand {
    public static final double MIN_GAIN = -24.0;
    public static final double MAX_GAIN = 12.0;
    private final Object disposeLock = new Object();
    private com.sun.media.jfxmedia.effects.EqualizerBand jfxBand;
    private DoubleProperty centerFrequency;
    private DoubleProperty bandwidth;
    private DoubleProperty gain;

    public EqualizerBand() {
    }

    public EqualizerBand(double d, double d2, double d3) {
        this.setCenterFrequency(d);
        this.setBandwidth(d2);
        this.setGain(d3);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void setJfxBand(com.sun.media.jfxmedia.effects.EqualizerBand equalizerBand) {
        Object object = this.disposeLock;
        synchronized (object) {
            this.jfxBand = equalizerBand;
        }
    }

    public final void setCenterFrequency(double d) {
        this.centerFrequencyProperty().set(d);
    }

    public final double getCenterFrequency() {
        return this.centerFrequency == null ? 0.0 : this.centerFrequency.get();
    }

    public DoubleProperty centerFrequencyProperty() {
        if (this.centerFrequency == null) {
            this.centerFrequency = new DoublePropertyBase(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = EqualizerBand.this.disposeLock;
                    synchronized (object) {
                        double d = EqualizerBand.this.centerFrequency.get();
                        if (EqualizerBand.this.jfxBand != null && d > 0.0) {
                            EqualizerBand.this.jfxBand.setCenterFrequency(d);
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return EqualizerBand.this;
                }

                @Override
                public String getName() {
                    return "centerFrequency";
                }
            };
        }
        return this.centerFrequency;
    }

    public final void setBandwidth(double d) {
        this.bandwidthProperty().set(d);
    }

    public final double getBandwidth() {
        return this.bandwidth == null ? 0.0 : this.bandwidth.get();
    }

    public DoubleProperty bandwidthProperty() {
        if (this.bandwidth == null) {
            this.bandwidth = new DoublePropertyBase(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = EqualizerBand.this.disposeLock;
                    synchronized (object) {
                        double d = EqualizerBand.this.bandwidth.get();
                        if (EqualizerBand.this.jfxBand != null && d > 0.0) {
                            EqualizerBand.this.jfxBand.setBandwidth(d);
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return EqualizerBand.this;
                }

                @Override
                public String getName() {
                    return "bandwidth";
                }
            };
        }
        return this.bandwidth;
    }

    public final void setGain(double d) {
        this.gainProperty().set(d);
    }

    public final double getGain() {
        return this.gain == null ? 0.0 : this.gain.get();
    }

    public DoubleProperty gainProperty() {
        if (this.gain == null) {
            this.gain = new DoublePropertyBase(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = EqualizerBand.this.disposeLock;
                    synchronized (object) {
                        if (EqualizerBand.this.jfxBand != null) {
                            EqualizerBand.this.jfxBand.setGain(EqualizerBand.this.gain.get());
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return EqualizerBand.this;
                }

                @Override
                public String getName() {
                    return "gain";
                }
            };
        }
        return this.gain;
    }
}

