/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.scene.media.MediaException;

public final class AudioClip {
    private String sourceURL;
    private com.sun.media.jfxmedia.AudioClip audioClip;
    private DoubleProperty volume;
    private DoubleProperty balance;
    private DoubleProperty rate;
    private DoubleProperty pan;
    private IntegerProperty priority;
    public static final int INDEFINITE = -1;
    private IntegerProperty cycleCount;

    public AudioClip(@NamedArg(value="source") String string) {
        URI uRI = URI.create(string);
        this.sourceURL = string;
        try {
            this.audioClip = com.sun.media.jfxmedia.AudioClip.load(uRI);
        }
        catch (URISyntaxException uRISyntaxException) {
            throw new IllegalArgumentException(uRISyntaxException);
        }
        catch (FileNotFoundException fileNotFoundException) {
            throw new MediaException(MediaException.Type.MEDIA_UNAVAILABLE, fileNotFoundException.getMessage());
        }
        catch (IOException iOException) {
            throw new MediaException(MediaException.Type.MEDIA_INACCESSIBLE, iOException.getMessage());
        }
        catch (com.sun.media.jfxmedia.MediaException mediaException) {
            throw new MediaException(MediaException.Type.MEDIA_UNSUPPORTED, mediaException.getMessage());
        }
    }

    public String getSource() {
        return this.sourceURL;
    }

    public final void setVolume(double d) {
        this.volumeProperty().set(d);
    }

    public final double getVolume() {
        return null == this.volume ? 1.0 : this.volume.get();
    }

    public DoubleProperty volumeProperty() {
        if (this.volume == null) {
            this.volume = new DoublePropertyBase(1.0){

                @Override
                protected void invalidated() {
                    if (null != AudioClip.this.audioClip) {
                        AudioClip.this.audioClip.setVolume(AudioClip.this.volume.get());
                    }
                }

                @Override
                public Object getBean() {
                    return AudioClip.this;
                }

                @Override
                public String getName() {
                    return "volume";
                }
            };
        }
        return this.volume;
    }

    public void setBalance(double d) {
        this.balanceProperty().set(d);
    }

    public double getBalance() {
        return null != this.balance ? this.balance.get() : 0.0;
    }

    public DoubleProperty balanceProperty() {
        if (null == this.balance) {
            this.balance = new DoublePropertyBase(0.0){

                @Override
                protected void invalidated() {
                    if (null != AudioClip.this.audioClip) {
                        AudioClip.this.audioClip.setBalance(AudioClip.this.balance.get());
                    }
                }

                @Override
                public Object getBean() {
                    return AudioClip.this;
                }

                @Override
                public String getName() {
                    return "balance";
                }
            };
        }
        return this.balance;
    }

    public void setRate(double d) {
        this.rateProperty().set(d);
    }

    public double getRate() {
        return null != this.rate ? this.rate.get() : 1.0;
    }

    public DoubleProperty rateProperty() {
        if (null == this.rate) {
            this.rate = new DoublePropertyBase(1.0){

                @Override
                protected void invalidated() {
                    if (null != AudioClip.this.audioClip) {
                        AudioClip.this.audioClip.setPlaybackRate(AudioClip.this.rate.get());
                    }
                }

                @Override
                public Object getBean() {
                    return AudioClip.this;
                }

                @Override
                public String getName() {
                    return "rate";
                }
            };
        }
        return this.rate;
    }

    public void setPan(double d) {
        this.panProperty().set(d);
    }

    public double getPan() {
        return null != this.pan ? this.pan.get() : 0.0;
    }

    public DoubleProperty panProperty() {
        if (null == this.pan) {
            this.pan = new DoublePropertyBase(0.0){

                @Override
                protected void invalidated() {
                    if (null != AudioClip.this.audioClip) {
                        AudioClip.this.audioClip.setPan(AudioClip.this.pan.get());
                    }
                }

                @Override
                public Object getBean() {
                    return AudioClip.this;
                }

                @Override
                public String getName() {
                    return "pan";
                }
            };
        }
        return this.pan;
    }

    public void setPriority(int n) {
        this.priorityProperty().set(n);
    }

    public int getPriority() {
        return null != this.priority ? this.priority.get() : 0;
    }

    public IntegerProperty priorityProperty() {
        if (null == this.priority) {
            this.priority = new IntegerPropertyBase(0){

                @Override
                protected void invalidated() {
                    if (null != AudioClip.this.audioClip) {
                        AudioClip.this.audioClip.setPriority(AudioClip.this.priority.get());
                    }
                }

                @Override
                public Object getBean() {
                    return AudioClip.this;
                }

                @Override
                public String getName() {
                    return "priority";
                }
            };
        }
        return this.priority;
    }

    public void setCycleCount(int n) {
        this.cycleCountProperty().set(n);
    }

    public int getCycleCount() {
        return null != this.cycleCount ? this.cycleCount.get() : 1;
    }

    public IntegerProperty cycleCountProperty() {
        if (null == this.cycleCount) {
            this.cycleCount = new IntegerPropertyBase(1){

                @Override
                protected void invalidated() {
                    if (null != AudioClip.this.audioClip) {
                        int n = AudioClip.this.cycleCount.get();
                        if (-1 != n) {
                            n = Math.max(1, n);
                            AudioClip.this.audioClip.setLoopCount(n - 1);
                        } else {
                            AudioClip.this.audioClip.setLoopCount(n);
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return AudioClip.this;
                }

                @Override
                public String getName() {
                    return "cycleCount";
                }
            };
        }
        return this.cycleCount;
    }

    public void play() {
        if (null != this.audioClip) {
            this.audioClip.play();
        }
    }

    public void play(double d) {
        if (null != this.audioClip) {
            this.audioClip.play(d);
        }
    }

    public void play(double d, double d2, double d3, double d4, int n) {
        if (null != this.audioClip) {
            this.audioClip.play(d, d2, d3, d4, this.audioClip.loopCount(), n);
        }
    }

    public boolean isPlaying() {
        return null != this.audioClip && this.audioClip.isPlaying();
    }

    public void stop() {
        if (null != this.audioClip) {
            this.audioClip.stop();
        }
    }
}

