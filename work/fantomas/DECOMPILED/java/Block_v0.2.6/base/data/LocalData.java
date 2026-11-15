/*
 * Decompiled with CFR 0.152.
 */
package base.data;

import base.data.BLOCK_MODE;
import base.data.TRAFFIC_SIGNALS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocalData {
    private static final Logger logger = LogManager.getLogger(LocalData.class);
    private int targetSpeed = 0;
    private int allowableSpeed = 0;
    private int previousTargetSpeed = 0;
    private double distanceTraveledAfterStopping = 0.0;
    private int distanceToAimingPoint = 0;
    private boolean brakingValveOpenedFromKOH = false;
    private boolean brakingValveOpenedFromEPK = false;
    private boolean disassembleTheTraction = false;
    private boolean useServiceBrakingOfSaut = false;
    private boolean disassembleSoundTheTraction = false;
    private boolean k799 = false;
    private boolean k809 = false;
    private int SpeedOnWhiteFromCommand = 40;
    private boolean modeChangeOnCommandk799k809 = false;
    private boolean k4 = false;
    private boolean K80 = false;
    private boolean K81 = false;
    private boolean K82 = false;
    private boolean K83 = false;
    private boolean K123 = false;
    private boolean vk = false;
    private boolean otpr = false;
    private boolean k20 = false;
    private boolean pull = false;
    private int pullCount = 0;
    private int rbAndRbpToRmp = 0;
    private BLOCK_MODE blockMode = BLOCK_MODE.TRAIN;
    private TRAFFIC_SIGNALS trafficSignal = TRAFFIC_SIGNALS.NONE;

    public int getTargetSpeed() {
        return this.targetSpeed;
    }

    public void setTargetSpeed(int targetSpeed) {
        if (targetSpeed != 1000) {
            this.targetSpeed = targetSpeed;
        }
    }

    public int getAllowableSpeed() {
        return this.allowableSpeed;
    }

    public void setAllowableSpeed(int allowableSpeed) {
        if (allowableSpeed != 1000) {
            this.allowableSpeed = allowableSpeed;
        }
    }

    public int getPreviousTargetSpeed() {
        return this.previousTargetSpeed;
    }

    public void setPreviousTargetSpeed() {
        this.previousTargetSpeed = this.targetSpeed;
    }

    public int getSpeedOnWhiteFromCommand() {
        return this.SpeedOnWhiteFromCommand;
    }

    public void setSpeedOnWhiteFromCommand(int speedOnWhiteFromCommand) {
        this.SpeedOnWhiteFromCommand = speedOnWhiteFromCommand;
    }

    public void setK799(boolean k799) {
        this.k799 = k799;
    }

    public void setK809(boolean k809) {
        this.k809 = k809;
    }

    public boolean isK799() {
        return this.k799;
    }

    public boolean isK809() {
        return this.k809;
    }

    public void setK80(boolean k80) {
        this.K80 = k80;
    }

    public boolean isK80() {
        return this.K80;
    }

    public void setK81(boolean k81) {
        this.K81 = k81;
    }

    public boolean isK81() {
        return this.K81;
    }

    public void setK82(boolean k82) {
        this.K82 = k82;
    }

    public boolean isK82() {
        return this.K82;
    }

    public void setK83(boolean k83) {
        this.K83 = k83;
    }

    public boolean isK83() {
        return this.K83;
    }

    public void setK4(boolean k4) {
        this.k4 = k4;
    }

    public boolean isK4() {
        return this.k4;
    }

    public void resetK80_83() {
        this.k4 = false;
        this.K80 = false;
        this.K81 = false;
        this.K82 = false;
        this.K83 = false;
    }

    public void setK123(boolean k123) {
        this.K123 = k123;
    }

    public boolean isK123() {
        return this.K123;
    }

    public void setVk(boolean vk) {
        this.vk = vk;
    }

    public boolean isVk() {
        return this.vk;
    }

    public void setOtpr(boolean otpr) {
        this.otpr = otpr;
    }

    public boolean isOtpr() {
        return this.otpr;
    }

    public void setDistanceTraveledAfterStopping(double distanceTraveledAfterStopping) {
        this.distanceTraveledAfterStopping = distanceTraveledAfterStopping;
    }

    public double getDistanceTraveledAfterStopping() {
        return this.distanceTraveledAfterStopping;
    }

    public boolean isK20() {
        return this.k20;
    }

    public void setK20(boolean k20) {
        this.k20 = k20;
    }

    public boolean isPull() {
        return this.pull;
    }

    public void setPull(boolean pull) {
        this.pull = pull;
    }

    public void setPullCount(int pullCount) {
        this.pullCount = pullCount;
    }

    public int getPullCount() {
        return this.pullCount;
    }

    public void setDistanceToAimingPoint(int distanceToAimingPoint) {
        this.distanceToAimingPoint = distanceToAimingPoint;
    }

    public int getDistanceToAimingPoint() {
        return this.distanceToAimingPoint;
    }

    public void setModeChangeOnCommandk799k809(boolean modeChangeOnCommandk799k809) {
        this.modeChangeOnCommandk799k809 = modeChangeOnCommandk799k809;
    }

    public boolean isModeChangeOnCommandk799k809() {
        return this.modeChangeOnCommandk799k809;
    }

    public void setDisassembleTheTraction(boolean disassembleTheTraction) {
        this.disassembleTheTraction = disassembleTheTraction;
    }

    public boolean isDisassembleTheTraction() {
        return this.disassembleTheTraction;
    }

    public void setUseServiceBrakingOfSaut(boolean useServiceBrakingOfSaut) {
        this.useServiceBrakingOfSaut = useServiceBrakingOfSaut;
    }

    public boolean isUseServiceBrakingOfSaut() {
        return this.useServiceBrakingOfSaut;
    }

    public void setDisassembleSoundTheTraction(boolean disassembleSoundTheTraction) {
        this.disassembleSoundTheTraction = disassembleSoundTheTraction;
    }

    public boolean isDisassembleSoundTheTraction() {
        return this.disassembleSoundTheTraction;
    }

    public void setRbAndRbpToRmp(int rbAndRbpToRmp) {
        this.rbAndRbpToRmp = rbAndRbpToRmp;
    }

    public int getRbAndRbpToRmp() {
        return this.rbAndRbpToRmp;
    }

    public BLOCK_MODE getBlockMode() {
        return this.blockMode;
    }

    public void setBlockMode(BLOCK_MODE blockMode) {
        this.blockMode = blockMode;
    }

    public void setTrafficSignal(TRAFFIC_SIGNALS trafficSignal) {
        this.trafficSignal = trafficSignal;
    }

    public TRAFFIC_SIGNALS getTrafficSignal() {
        return this.trafficSignal;
    }

    public boolean isBrakingValveOpenedFromKOH() {
        return this.brakingValveOpenedFromKOH;
    }

    public void setBrakingValveOpenedFromKOH(boolean brakingValveOpenedFromKOH) {
        this.brakingValveOpenedFromKOH = brakingValveOpenedFromKOH;
    }

    public void setBrakingValveOpenedFromEPK(boolean brakingValveOpenedFromEPK) {
        this.brakingValveOpenedFromEPK = brakingValveOpenedFromEPK;
    }

    public boolean isBrakingValveOpenedFromEPK() {
        return this.brakingValveOpenedFromEPK;
    }
}

