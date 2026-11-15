/*
 * Decompiled with CFR 0.152.
 */
package base.sound;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoundPlayer
extends Thread {
    private static final Logger logger = LogManager.getLogger(SoundPlayer.class);
    private boolean running;
    private BooleanProperty isCyclic = new SimpleBooleanProperty(false);
    private BlockingQueue<String> queueFileNamesToPlay;

    SoundPlayer(String name) {
        this.setName("SoundPlayer " + name);
        this.queueFileNamesToPlay = new ArrayBlockingQueue<String>(10);
        logger.debug("init {}", (Object)name);
    }

    @Override
    public void run() {
        this.running = true;
        block4: while (this.running) {
            try {
                String fileName = this.queueFileNamesToPlay.take();
                do {
                    try {
                        this.playSound(fileName);
                    }
                    catch (Exception e) {
                        logger.error(e);
                        continue block4;
                    }
                } while (this.isCyclic.get());
            }
            catch (Exception e) {
                logger.error(e);
            }
        }
    }

    void destructor() {
        this.isCyclic.set(false);
        this.running = false;
    }

    void play(String filename) {
        try {
            this.queueFileNamesToPlay.put(filename);
        }
        catch (InterruptedException e) {
            logger.error(e);
        }
    }

    void startCyclicPlay() {
        this.isCyclic.set(true);
    }

    private void playSound(String fileName) {
        try {
            int nBytesRead;
            URL strFilename = this.getClass().getResource("/audio_files/" + fileName);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(strFilename);
            AudioFormat audioFormat = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceLine = (SourceDataLine)AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
            sourceLine.start();
            int BUFFER_SIZE = 128000;
            byte[] abData = new byte[BUFFER_SIZE];
            while ((nBytesRead = audioStream.read(abData)) != -1) {
                sourceLine.write(abData, 0, nBytesRead);
            }
            audioStream.close();
            sourceLine.drain();
            sourceLine.close();
        }
        catch (Exception e) {
            logger.error(e);
        }
    }
}

