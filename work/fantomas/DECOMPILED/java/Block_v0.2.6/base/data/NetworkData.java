/*
 * Decompiled with CFR 0.152.
 */
package base.data;

import base.data.BLOCK_MODE;
import base.data.BlockData;
import base.data.KEYBOARD_BUTTON;

public class NetworkData {
    private int blockPower = 0;
    private int epkKey = 0;
    private int rbButton = 0;
    private int rbsButton = 0;
    private int rbpButton = 0;
    private int coordinateGps = 0;
    private int picket = 0;
    private float tmValue = 0.0f;
    private float tcValue = 0.0f;
    private float ur1Value = 0.0f;
    private float ur2Value = 0.0f;
    private float pmValue = 0.0f;
    private float acceleration = 0.0f;
    private float actualSpeed = 0.0f;
    private float roadBias = 0.0f;
    private float brakingCoefficient = 0.0f;
    private int ekEquipped = 0;
    private int cassetteEquipped = 0;
    private int traction = 0;
    private int reverse = 0;
    private long worldTime = 0L;
    private int keyboardButton = 0;
    private String stationName = "";
    private int targetCode = 0;
    private int distanceToTarget = 0;
    private int distanceToSaut = 0;
    private String targetName = "";
    private int allowableSpeedByEK = 0;
    private int targetSpeedByEK = 0;
    private int distanceToNextSectionEK = 0;
    private int trafficLightCode = 5;
    private int distanceToTrafficLight = 0;
    private int cruiseControlCode = 0;
    private int cruiseControlSpeed = 0;
    private int neutralInsert = 0;
    private int tokorazdel = 0;
    private int distanceTokorazdelOrNeutralInsert = 0;
    private int serviceBraking = 0;
    private int malfunctionCode = 0;
    private int sautEquipped = 0;
    private int tskbmEquipped = 0;
    private int tskbmPower = 0;
    private int tskbmnDressed = 0;
    private int tskbmnStatus = 0;
    private String trafficLightName = "";
    private int brakingValveOpenedFromKOH = 0;
    private int brakingValveOpenedFromEPK = 0;
    private int roadIndex = -1;
    private int videoCoord = 0;

    public void updateDataFromClass(NetworkData data) {
        this.blockPower = data.blockPower;
        this.epkKey = data.epkKey;
        this.rbButton = data.rbButton;
        this.rbsButton = data.rbsButton;
        this.rbpButton = data.rbpButton;
        this.coordinateGps = data.coordinateGps;
        this.picket = data.picket;
        this.tmValue = data.tmValue;
        this.tcValue = data.tcValue;
        this.ur1Value = data.ur1Value;
        this.ur2Value = data.ur2Value;
        this.pmValue = data.pmValue;
        this.acceleration = data.acceleration;
        this.cassetteEquipped = data.cassetteEquipped;
        this.tskbmEquipped = data.tskbmEquipped;
        this.tskbmPower = data.tskbmPower;
        this.tskbmnDressed = data.tskbmnDressed;
        this.tskbmnStatus = data.tskbmnStatus;
        this.sautEquipped = data.sautEquipped;
        this.reverse = data.reverse;
        this.worldTime = data.worldTime;
        this.ekEquipped = data.ekEquipped;
        this.keyboardButton = data.keyboardButton;
        this.actualSpeed = data.actualSpeed;
        this.roadIndex = data.roadIndex;
        this.videoCoord = data.videoCoord;
        this.brakingValveOpenedFromKOH = data.brakingValveOpenedFromKOH;
        this.brakingValveOpenedFromEPK = data.brakingValveOpenedFromEPK;
        if (data.epkKey == 1) {
            this.roadBias = data.roadBias;
            this.brakingCoefficient = data.brakingCoefficient;
            this.traction = data.traction;
            this.stationName = data.stationName;
            this.targetCode = data.targetCode;
            this.distanceToTarget = data.distanceToTarget;
            this.distanceToSaut = data.distanceToSaut;
            this.targetName = data.targetName;
            this.allowableSpeedByEK = data.allowableSpeedByEK;
            this.targetSpeedByEK = data.targetSpeedByEK;
            this.distanceToNextSectionEK = data.distanceToNextSectionEK;
            this.trafficLightCode = data.trafficLightCode;
            this.distanceToTrafficLight = data.distanceToTrafficLight;
            this.cruiseControlCode = data.cruiseControlCode;
            this.cruiseControlSpeed = data.cruiseControlSpeed;
            this.neutralInsert = data.neutralInsert;
            this.tokorazdel = data.tokorazdel;
            this.distanceTokorazdelOrNeutralInsert = data.distanceTokorazdelOrNeutralInsert;
            this.serviceBraking = data.serviceBraking;
            this.malfunctionCode = data.malfunctionCode;
            this.trafficLightName = data.trafficLightName;
        } else {
            this.roadBias = 0.0f;
            this.brakingCoefficient = 0.0f;
            this.traction = 0;
            this.stationName = "";
            this.targetCode = 0;
            this.distanceToTarget = 0;
            this.distanceToSaut = 0;
            this.targetName = "";
            this.allowableSpeedByEK = 0;
            this.targetSpeedByEK = 0;
            this.distanceToNextSectionEK = 0;
            this.trafficLightCode = 5;
            this.distanceToTrafficLight = 0;
            this.cruiseControlCode = 0;
            this.cruiseControlSpeed = 0;
            this.neutralInsert = 0;
            this.tokorazdel = 0;
            this.distanceTokorazdelOrNeutralInsert = 0;
            this.serviceBraking = 0;
            this.malfunctionCode = 0;
            this.trafficLightName = "";
        }
    }

    public int getBlockPower() {
        return this.blockPower;
    }

    public void setBlockPower(int blockPower) {
        this.blockPower = blockPower;
    }

    public int getEpkKey() {
        return this.epkKey;
    }

    public void setEpkKey(int epkKey) {
        this.epkKey = epkKey;
    }

    public int getRbButton() {
        return this.rbButton;
    }

    public void setRbButton(int rbButton) {
        this.rbButton = rbButton;
    }

    public int getRbsButton() {
        return this.rbsButton;
    }

    public void setRbsButton(int rbsButton) {
        this.rbsButton = rbsButton;
    }

    public int getRbpButton() {
        return this.rbpButton;
    }

    public void setRbpButton(int rbpButton) {
        this.rbpButton = rbpButton;
    }

    public int getCoordinateGps() {
        return this.coordinateGps;
    }

    public void setCoordinateGps(int coordinateGps) {
        this.coordinateGps = coordinateGps;
    }

    public int getPicket() {
        return this.picket;
    }

    public void setPicket(int picket) {
        this.picket = picket;
    }

    public float getTmValue() {
        return this.tmValue;
    }

    public void setTmValue(float tmValue) {
        this.tmValue = tmValue;
    }

    public float getTcValue() {
        return this.tcValue;
    }

    public void setTcValue(float tcValue) {
        this.tcValue = tcValue;
    }

    public float getUr1Value() {
        return this.ur1Value;
    }

    public void setUr1Value(float ur1Value) {
        this.ur1Value = ur1Value;
    }

    public float getUr2Value() {
        return this.ur2Value;
    }

    public void setUr2Value(float ur2Value) {
        this.ur2Value = ur2Value;
    }

    public float getPmValue() {
        return this.pmValue;
    }

    public void setPmValue(float pmValue) {
        this.pmValue = pmValue;
    }

    public float getAcceleration() {
        return this.acceleration;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public float getActualSpeed() {
        return this.actualSpeed;
    }

    public void setActualSpeed(float actualSpeed) {
        this.actualSpeed = actualSpeed;
    }

    public float getRoadBias() {
        return this.roadBias;
    }

    public void setRoadBias(float roadBias) {
        this.roadBias = roadBias;
    }

    public float getBrakingCoefficient() {
        return this.brakingCoefficient;
    }

    public void setBrakingCoefficient(float brakingCoefficient) {
        this.brakingCoefficient = brakingCoefficient;
    }

    public int getEkEquipped() {
        return this.ekEquipped;
    }

    public void setEkEquipped(int ekEquipped) {
        this.ekEquipped = ekEquipped;
    }

    public int getCassetteEquipped() {
        return this.cassetteEquipped;
    }

    public void setCassetteEquipped(int cassetteEquipped) {
        this.cassetteEquipped = cassetteEquipped;
    }

    public int getTraction() {
        return this.traction;
    }

    public void setTraction(int traction) {
        this.traction = traction;
    }

    public int getReverse() {
        return this.reverse;
    }

    public void setReverse(int reverse) {
        this.reverse = reverse;
    }

    public long getWorldTime() {
        return this.worldTime;
    }

    public void setWorldTime(long worldTime) {
        this.worldTime = worldTime;
    }

    public int getKeyboardButton() {
        return this.keyboardButton;
    }

    public void setKeyboardButton(int keyboardButton) {
        this.keyboardButton = keyboardButton >= 0 && keyboardButton < KEYBOARD_BUTTON.values().length ? keyboardButton : KEYBOARD_BUTTON.NONE.ordinal();
    }

    public String getStationName() {
        return this.stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public int getTargetCode() {
        return this.targetCode;
    }

    public void setTargetCode(int targetCode) {
        this.targetCode = targetCode;
    }

    public int getAllowableSpeedByEK() {
        return this.allowableSpeedByEK;
    }

    public void setAllowableSpeedByEK(int allowableSpeedByEK) {
        this.allowableSpeedByEK = allowableSpeedByEK;
    }

    public int getTargetSpeedByEK() {
        return this.targetSpeedByEK;
    }

    public void setTargetSpeedByEK(int targetSpeedByEK) {
        this.targetSpeedByEK = targetSpeedByEK;
    }

    public int getDistanceToNextSectionEK() {
        return this.distanceToNextSectionEK;
    }

    public void setDistanceToNextSectionEK(int distanceToNextSectionEK) {
        this.distanceToNextSectionEK = distanceToNextSectionEK;
    }

    public int getDistanceToTarget() {
        return this.distanceToTarget;
    }

    public void setDistanceToTarget(int distanceToTarget) {
        this.distanceToTarget = distanceToTarget;
    }

    public int getDistanceToSaut() {
        return this.distanceToSaut;
    }

    public void setDistanceToSaut(int distanceToSaut) {
        this.distanceToSaut = distanceToSaut;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public int getTrafficLightCode() {
        return this.trafficLightCode;
    }

    public void setTrafficLightCode(int trafficLightCode) {
        this.trafficLightCode = BlockData.getInstance().getLocalData().isK799() || BlockData.getInstance().getLocalData().isK809() || BlockData.getInstance().getLocalData().getBlockMode() != BLOCK_MODE.TRAIN ? 0 : trafficLightCode;
    }

    public int getDistanceToTrafficLight() {
        return this.distanceToTrafficLight;
    }

    public void setDistanceToTrafficLight(int distanceToTrafficLight) {
        this.distanceToTrafficLight = distanceToTrafficLight;
    }

    public int getCruiseControlCode() {
        return this.cruiseControlCode;
    }

    public void setCruiseControlCode(int cruiseControlCode) {
        this.cruiseControlCode = cruiseControlCode;
    }

    public int getCruiseControlSpeed() {
        return this.cruiseControlSpeed;
    }

    public void setCruiseControlSpeed(int cruiseControlSpeed) {
        this.cruiseControlSpeed = cruiseControlSpeed;
    }

    public int getNeutralInsert() {
        return this.neutralInsert;
    }

    public void setNeutralInsert(int neutralInsert) {
        this.neutralInsert = neutralInsert;
    }

    public int getTokorazdel() {
        return this.tokorazdel;
    }

    public void setTokorazdel(int tokorazdel) {
        this.tokorazdel = tokorazdel;
    }

    public int getDistanceTokorazdelOrNeutralInsert() {
        return this.distanceTokorazdelOrNeutralInsert;
    }

    public void setDistanceTokorazdelOrNeutralInsert(int distanceTokorazdelOrNeutralInsert) {
        this.distanceTokorazdelOrNeutralInsert = distanceTokorazdelOrNeutralInsert;
    }

    public int getServiceBraking() {
        return this.serviceBraking;
    }

    public void setServiceBraking(int serviceBraking) {
        this.serviceBraking = serviceBraking;
    }

    public int getMalfunctionCode() {
        return this.malfunctionCode;
    }

    public void setMalfunctionCode(int malfunctionCode) {
        this.malfunctionCode = malfunctionCode;
    }

    public int getSautEquipped() {
        return this.sautEquipped;
    }

    public void setSautEquipped(int sautEquipped) {
        this.sautEquipped = sautEquipped;
    }

    public int getTskbmEquipped() {
        return this.tskbmEquipped;
    }

    public void setTskbmEquipped(int tskbmEquipped) {
        this.tskbmEquipped = tskbmEquipped;
    }

    public int getTskbmPower() {
        return this.tskbmPower;
    }

    public void setTskbmPower(int tskbmPower) {
        this.tskbmPower = tskbmPower;
    }

    public int getTskbmnDressed() {
        return this.tskbmnDressed;
    }

    public void setTskbmnDressed(int tskbmnDressed) {
        this.tskbmnDressed = tskbmnDressed;
    }

    public int getTskbmnStatus() {
        return this.tskbmnStatus;
    }

    public void setTskbmnStatus(int tskbmnStatus) {
        this.tskbmnStatus = tskbmnStatus;
    }

    public String getTrafficLightName() {
        return this.trafficLightName;
    }

    public void setTrafficLightName(String trafficLightName) {
        this.trafficLightName = trafficLightName;
    }

    public int getRoadIndex() {
        return this.roadIndex;
    }

    public void setRoadIndex(int roadIndex) {
        this.roadIndex = roadIndex;
    }

    public int getVideoCoord() {
        return this.videoCoord;
    }

    public void setVideoCoord(int videoCoord) {
        this.videoCoord = videoCoord;
    }

    public void setBrakingValveOpenedFromKOH(int brakingValveOpenedFromKOH) {
        this.brakingValveOpenedFromKOH = brakingValveOpenedFromKOH;
    }

    public int getBrakingValveOpenedFromKOH() {
        return this.brakingValveOpenedFromKOH;
    }

    public void setBrakingValveOpenedFromEPK(int brakingValveOpenedFromEPK) {
        this.brakingValveOpenedFromEPK = brakingValveOpenedFromEPK;
    }

    public int getBrakingValveOpenedFromEPK() {
        return this.brakingValveOpenedFromEPK;
    }
}

