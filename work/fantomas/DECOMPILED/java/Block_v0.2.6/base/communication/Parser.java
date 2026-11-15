/*
 * Decompiled with CFR 0.152.
 */
package base.communication;

import base.data.BlockData;
import base.data.NetworkData;
import base.timers.TimerController;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class Parser {
    private static final Logger logger = LogManager.getLogger(Parser.class);

    Parser() {
    }

    static void parseBuffer(byte[] buffer) {
        int i;
        NetworkData networkDataFromBuffer = new NetworkData();
        networkDataFromBuffer.setBlockPower(Parser.bytesToInt8(buffer[0]));
        networkDataFromBuffer.setEpkKey(Parser.bytesToInt8(buffer[1]));
        networkDataFromBuffer.setRbButton(Parser.bytesToInt8(buffer[2]));
        networkDataFromBuffer.setRbsButton(Parser.bytesToInt8(buffer[3]));
        networkDataFromBuffer.setRbpButton(Parser.bytesToInt8(buffer[4]));
        networkDataFromBuffer.setCoordinateGps(Parser.bytesToInt32(Arrays.copyOfRange(buffer, 5, 9)));
        networkDataFromBuffer.setPicket(Parser.bytesToInt8(buffer[9]));
        networkDataFromBuffer.setTmValue(Parser.bytesToFloat(Arrays.copyOfRange(buffer, 10, 14)));
        networkDataFromBuffer.setTcValue(Parser.bytesToFloat(Arrays.copyOfRange(buffer, 14, 18)));
        networkDataFromBuffer.setUr1Value(Parser.bytesToFloat(Arrays.copyOfRange(buffer, 18, 22)));
        networkDataFromBuffer.setUr2Value(Parser.bytesToFloat(Arrays.copyOfRange(buffer, 22, 26)));
        networkDataFromBuffer.setPmValue(Parser.bytesToFloat(Arrays.copyOfRange(buffer, 26, 30)));
        networkDataFromBuffer.setAcceleration(Parser.bytesToFloat(Arrays.copyOfRange(buffer, 30, 34)));
        networkDataFromBuffer.setActualSpeed(Math.abs(Parser.bytesToFloat(Arrays.copyOfRange(buffer, 34, 38))));
        networkDataFromBuffer.setRoadBias(Parser.bytesToFloat(Arrays.copyOfRange(buffer, 38, 42)));
        networkDataFromBuffer.setBrakingCoefficient(Parser.bytesToFloat(Arrays.copyOfRange(buffer, 42, 46)));
        networkDataFromBuffer.setEkEquipped(Parser.bytesToInt8(buffer[46]));
        networkDataFromBuffer.setCassetteEquipped(Parser.bytesToInt8(buffer[47]));
        networkDataFromBuffer.setTraction(Parser.bytesToInt8(buffer[48]));
        networkDataFromBuffer.setReverse(Parser.bytesToInt8(buffer[49]));
        networkDataFromBuffer.setWorldTime(Parser.bytesToLong(Arrays.copyOfRange(buffer, 50, 58)));
        networkDataFromBuffer.setKeyboardButton(Parser.bytesToInt8(buffer[58]));
        StringBuilder name = new StringBuilder();
        for (i = 59; i < 75; i += 2) {
            name.append(Parser.bytesToChar(Arrays.copyOfRange(buffer, i, i + 2)));
        }
        networkDataFromBuffer.setStationName(name.toString());
        networkDataFromBuffer.setTargetCode(Parser.bytesToInt8(buffer[75]));
        networkDataFromBuffer.setDistanceToTarget(Parser.bytesToShort(Arrays.copyOfRange(buffer, 76, 78)));
        networkDataFromBuffer.setDistanceToSaut(Parser.bytesToShort(Arrays.copyOfRange(buffer, 78, 80)));
        name = new StringBuilder();
        for (i = 80; i < 98; i += 2) {
            name.append(Parser.bytesToChar(Arrays.copyOfRange(buffer, i, i + 2)));
        }
        networkDataFromBuffer.setTargetName(name.toString());
        networkDataFromBuffer.setAllowableSpeedByEK(Parser.bytesToShort(Arrays.copyOfRange(buffer, 98, 100)));
        networkDataFromBuffer.setTargetSpeedByEK(Parser.bytesToShort(Arrays.copyOfRange(buffer, 100, 102)));
        networkDataFromBuffer.setDistanceToNextSectionEK(Parser.bytesToShort(Arrays.copyOfRange(buffer, 102, 104)));
        networkDataFromBuffer.setTrafficLightCode(Parser.bytesToInt8(buffer[104]));
        networkDataFromBuffer.setDistanceToTrafficLight(Parser.bytesToShort(Arrays.copyOfRange(buffer, 105, 107)));
        networkDataFromBuffer.setCruiseControlCode(Parser.bytesToInt8(buffer[107]));
        networkDataFromBuffer.setCruiseControlSpeed(Parser.bytesToShort(Arrays.copyOfRange(buffer, 108, 110)));
        networkDataFromBuffer.setNeutralInsert(Parser.bytesToInt8(buffer[110]));
        networkDataFromBuffer.setTokorazdel(Parser.bytesToInt8(buffer[111]));
        networkDataFromBuffer.setDistanceTokorazdelOrNeutralInsert(Parser.bytesToShort(Arrays.copyOfRange(buffer, 112, 114)));
        networkDataFromBuffer.setServiceBraking(Parser.bytesToInt8(buffer[114]));
        networkDataFromBuffer.setMalfunctionCode(Parser.bytesToInt8(buffer[115]));
        networkDataFromBuffer.setSautEquipped(Parser.bytesToInt8(buffer[116]));
        networkDataFromBuffer.setTskbmEquipped(Parser.bytesToInt8(buffer[117]));
        networkDataFromBuffer.setTskbmPower(Parser.bytesToInt8(buffer[118]));
        networkDataFromBuffer.setTskbmnDressed(Parser.bytesToInt8(buffer[119]));
        networkDataFromBuffer.setTskbmnStatus(Parser.bytesToInt8(buffer[120]));
        name = new StringBuilder();
        for (i = 121; i < 139; i += 2) {
            name.append(Parser.bytesToChar(Arrays.copyOfRange(buffer, i, i + 2)));
        }
        networkDataFromBuffer.setTrafficLightName(name.toString());
        networkDataFromBuffer.setBrakingValveOpenedFromKOH(Parser.bytesToInt8(buffer[139]));
        networkDataFromBuffer.setBrakingValveOpenedFromEPK(Parser.bytesToInt8(buffer[140]));
        networkDataFromBuffer.setRoadIndex(Parser.bytesToInt8(buffer[141]));
        networkDataFromBuffer.setVideoCoord(Parser.bytesToInt32(Arrays.copyOfRange(buffer, 142, 146)));
        try {
            BlockData.getInstance().getCurrentNetworkDataQueue().put(networkDataFromBuffer);
        }
        catch (InterruptedException e) {
            logger.error(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static byte[] getBytesToSend() {
        byte[] buf = new byte[10];
        BlockingQueue<NetworkData> blockingQueue = BlockData.getInstance().getCurrentNetworkDataQueue();
        synchronized (blockingQueue) {
            buf[0] = (byte)((TimerController.getInstance().isEpkHoldingCoil() ? 1 : 0) & 0xFF);
            buf[1] = (byte)(BlockData.getInstance().getLocalData().getAllowableSpeed() >> 8 & 0xFF);
            buf[2] = (byte)(BlockData.getInstance().getLocalData().getAllowableSpeed() & 0xFF);
            buf[3] = (byte)(BlockData.getInstance().getLocalData().getDistanceToAimingPoint() >> 8 & 0xFF);
            buf[4] = (byte)(BlockData.getInstance().getLocalData().getDistanceToAimingPoint() & 0xFF);
            buf[5] = (byte)(BlockData.getInstance().getCurNetworkData().getTrafficLightCode() & 0xFF);
            buf[6] = (byte)((BlockData.getInstance().getLocalData().isDisassembleTheTraction() ? 1 : 0) & 0xFF);
            buf[7] = (byte)((BlockData.getInstance().getLocalData().isUseServiceBrakingOfSaut() ? 1 : 0) & 0xFF);
            buf[8] = (byte)((BlockData.getInstance().getDisplayData().isVisiblyAttention() ? 1 : 0) & 0xFF);
            buf[9] = (byte)((TimerController.getInstance().isPssPassLamp() ? 1 : 0) & 0xFF);
        }
        return buf;
    }

    private static int bytesToInt32(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getInt();
    }

    private static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    private static char bytesToChar(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getChar();
    }

    private static short bytesToShort(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getShort();
    }

    private static float bytesToFloat(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getFloat();
    }

    private static int bytesToInt8(byte b) {
        return Byte.toUnsignedInt(b);
    }
}

