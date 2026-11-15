/*
 * Decompiled with CFR 0.152.
 */
package base.communication;

import base.communication.Parser;
import base.data.BlockData;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LanServer
extends Thread {
    private static final Logger logger = LogManager.getLogger(LanServer.class);
    private DatagramSocket socket;
    private boolean running;
    private final byte[] buf = new byte[146];

    public LanServer() {
        this.setName("LanServer");
        try {
            this.socket = new DatagramSocket(BlockData.getInstance().getStartConfig().getServerPort());
        }
        catch (SocketException e) {
            logger.error(e);
        }
        logger.debug("init");
    }

    @Override
    public void run() {
        this.running = true;
        while (this.running) {
            DatagramPacket packet = new DatagramPacket(this.buf, this.buf.length);
            try {
                this.socket.receive(packet);
            }
            catch (IOException e) {
                logger.error(e);
            }
            Parser.parseBuffer(packet.getData());
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            byte[] out_buf = Parser.getBytesToSend();
            packet = new DatagramPacket(out_buf, out_buf.length, address, port);
            try {
                this.socket.send(packet);
            }
            catch (IOException e) {
                logger.error(e);
            }
        }
        this.socket.close();
        logger.debug("lan server stop; socket close");
    }

    public void stop_close() {
        this.running = false;
    }
}

