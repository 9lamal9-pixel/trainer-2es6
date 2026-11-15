/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import com.sun.javafx.tk.Toolkit;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javafx.scene.media.MediaPlayer;

class MediaPlayerShutdownHook
implements Runnable {
    private static final List<WeakReference<MediaPlayer>> playerRefs = new ArrayList<WeakReference<MediaPlayer>>();
    private static boolean isShutdown = false;

    MediaPlayerShutdownHook() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void addMediaPlayer(MediaPlayer mediaPlayer) {
        List<WeakReference<MediaPlayer>> list = playerRefs;
        synchronized (list) {
            if (isShutdown) {
                com.sun.media.jfxmedia.MediaPlayer mediaPlayer2 = mediaPlayer.retrieveJfxPlayer();
                if (mediaPlayer2 != null) {
                    mediaPlayer2.dispose();
                }
            } else {
                ListIterator<WeakReference<MediaPlayer>> listIterator = playerRefs.listIterator();
                while (listIterator.hasNext()) {
                    MediaPlayer mediaPlayer3 = (MediaPlayer)listIterator.next().get();
                    if (mediaPlayer3 != null) continue;
                    listIterator.remove();
                }
                playerRefs.add(new WeakReference<MediaPlayer>(mediaPlayer));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        List<WeakReference<MediaPlayer>> list = playerRefs;
        synchronized (list) {
            ListIterator<WeakReference<MediaPlayer>> listIterator = playerRefs.listIterator();
            while (listIterator.hasNext()) {
                MediaPlayer mediaPlayer = (MediaPlayer)listIterator.next().get();
                if (mediaPlayer != null) {
                    mediaPlayer.destroyMediaTimer();
                    com.sun.media.jfxmedia.MediaPlayer mediaPlayer2 = mediaPlayer.retrieveJfxPlayer();
                    if (mediaPlayer2 == null) continue;
                    mediaPlayer2.dispose();
                    continue;
                }
                listIterator.remove();
            }
            isShutdown = true;
        }
    }

    static {
        Toolkit.getToolkit().addShutdownHook(new MediaPlayerShutdownHook());
    }
}

