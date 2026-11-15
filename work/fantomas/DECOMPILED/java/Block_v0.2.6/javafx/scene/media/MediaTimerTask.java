/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.media.MediaPlayer;

class MediaTimerTask
extends TimerTask {
    private Timer mediaTimer = null;
    static final Object timerLock = new Object();
    private WeakReference<MediaPlayer> playerRef;

    MediaTimerTask(MediaPlayer mediaPlayer) {
        this.playerRef = new WeakReference<MediaPlayer>(mediaPlayer);
    }

    void start() {
        if (this.mediaTimer == null) {
            this.mediaTimer = new Timer(true);
            this.mediaTimer.scheduleAtFixedRate((TimerTask)this, 0L, 100L);
        }
    }

    void stop() {
        if (this.mediaTimer != null) {
            this.mediaTimer.cancel();
            this.mediaTimer = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        Object object = timerLock;
        synchronized (object) {
            MediaPlayer mediaPlayer = (MediaPlayer)this.playerRef.get();
            if (mediaPlayer != null) {
                Platform.runLater(() -> {
                    Object object = timerLock;
                    synchronized (object) {
                        mediaPlayer.updateTime();
                    }
                });
            } else {
                this.cancel();
            }
        }
    }
}

