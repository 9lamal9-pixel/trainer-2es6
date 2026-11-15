/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import com.sun.javafx.collections.VetoableListDecorator;
import com.sun.media.jfxmedia.logging.Logger;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.EqualizerBand;

public final class AudioEqualizer {
    public static final int MAX_NUM_BANDS = 64;
    private com.sun.media.jfxmedia.effects.AudioEqualizer jfxEqualizer = null;
    private final ObservableList<EqualizerBand> bands;
    private final Object disposeLock = new Object();
    private BooleanProperty enabled;

    public final ObservableList<EqualizerBand> getBands() {
        return this.bands;
    }

    AudioEqualizer() {
        this.bands = new Bands();
        this.bands.addAll((EqualizerBand[])new EqualizerBand[]{new EqualizerBand(32.0, 19.0, 0.0), new EqualizerBand(64.0, 39.0, 0.0), new EqualizerBand(125.0, 78.0, 0.0), new EqualizerBand(250.0, 156.0, 0.0), new EqualizerBand(500.0, 312.0, 0.0), new EqualizerBand(1000.0, 625.0, 0.0), new EqualizerBand(2000.0, 1250.0, 0.0), new EqualizerBand(4000.0, 2500.0, 0.0), new EqualizerBand(8000.0, 5000.0, 0.0), new EqualizerBand(16000.0, 10000.0, 0.0)});
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void setAudioEqualizer(com.sun.media.jfxmedia.effects.AudioEqualizer audioEqualizer) {
        Object object = this.disposeLock;
        synchronized (object) {
            if (this.jfxEqualizer == audioEqualizer) {
                return;
            }
            if (this.jfxEqualizer != null && audioEqualizer == null) {
                this.jfxEqualizer.setEnabled(false);
                for (EqualizerBand equalizerBand : this.bands) {
                    equalizerBand.setJfxBand(null);
                }
                this.jfxEqualizer = null;
                return;
            }
            this.jfxEqualizer = audioEqualizer;
            audioEqualizer.setEnabled(this.isEnabled());
            for (EqualizerBand equalizerBand : this.bands) {
                if (equalizerBand.getCenterFrequency() > 0.0 && equalizerBand.getBandwidth() > 0.0) {
                    com.sun.media.jfxmedia.effects.EqualizerBand equalizerBand2 = audioEqualizer.addBand(equalizerBand.getCenterFrequency(), equalizerBand.getBandwidth(), equalizerBand.getGain());
                    equalizerBand.setJfxBand(equalizerBand2);
                    continue;
                }
                Logger.logMsg(4, "Center frequency [" + equalizerBand.getCenterFrequency() + "] and bandwidth [" + equalizerBand.getBandwidth() + "] must be greater than 0.");
            }
        }
    }

    public final void setEnabled(boolean bl) {
        this.enabledProperty().set(bl);
    }

    public final boolean isEnabled() {
        return this.enabled == null ? false : this.enabled.get();
    }

    public BooleanProperty enabledProperty() {
        if (this.enabled == null) {
            this.enabled = new BooleanPropertyBase(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = AudioEqualizer.this.disposeLock;
                    synchronized (object) {
                        if (AudioEqualizer.this.jfxEqualizer != null) {
                            AudioEqualizer.this.jfxEqualizer.setEnabled(AudioEqualizer.this.enabled.get());
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return AudioEqualizer.this;
                }

                @Override
                public String getName() {
                    return "enabled";
                }
            };
        }
        return this.enabled;
    }

    private class Bands
    extends VetoableListDecorator<EqualizerBand> {
        public Bands() {
            super(FXCollections.observableArrayList());
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected void onProposedChange(List<EqualizerBand> list, int[] nArray) {
            Object object = AudioEqualizer.this.disposeLock;
            synchronized (object) {
                if (AudioEqualizer.this.jfxEqualizer != null) {
                    for (int i = 0; i < nArray.length; i += 2) {
                        for (Object object2 : this.subList(nArray[i], nArray[i + 1])) {
                            AudioEqualizer.this.jfxEqualizer.removeBand(((EqualizerBand)object2).getCenterFrequency());
                        }
                    }
                    for (EqualizerBand equalizerBand : list) {
                        if (equalizerBand.getCenterFrequency() > 0.0 && equalizerBand.getBandwidth() > 0.0) {
                            Object object2;
                            object2 = AudioEqualizer.this.jfxEqualizer.addBand(equalizerBand.getCenterFrequency(), equalizerBand.getBandwidth(), equalizerBand.getGain());
                            equalizerBand.setJfxBand((com.sun.media.jfxmedia.effects.EqualizerBand)object2);
                            continue;
                        }
                        Logger.logMsg(4, "Center frequency [" + equalizerBand.getCenterFrequency() + "] and bandwidth [" + equalizerBand.getBandwidth() + "] must be greater than 0.");
                    }
                }
            }
        }
    }
}

