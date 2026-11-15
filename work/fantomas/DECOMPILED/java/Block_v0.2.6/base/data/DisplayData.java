/*
 * Decompiled with CFR 0.152.
 */
package base.data;

import base.data.TRAFFIC_SIGNALS;

public class DisplayData {
    private boolean power;
    private String infoStr = "";
    private String valueStr = "";
    private String wayNum = "";
    private String worldTime = "";
    private int allowableSpeed;
    private int targetSpeed;
    private int cruiseControlSpeed;
    private int actualSpeed;
    private boolean visiblyActualSpeed = true;
    private String dimensionOfTheProgressBar = "";
    private String pressureTC = "";
    private String pressureTM = "";
    private String pressureUR = "";
    private float progressTC;
    private float progressTM;
    private float progressUR;
    private int reverse;
    private boolean visiblyAttention;
    private String targetType = "";
    private final String[] targetCodes = new String[]{"", "\u0421\u0412\u0415\u0422\u041e\u0424\u041e\u0420", "\u041e\u041f\u0410\u0421\u041d\u041e\u0415 \u041c\u0415\u0421\u0422\u041e", "\u041c\u041e\u0421\u0422", "\u041f\u0415\u0420\u0415\u0415\u0417\u0414", "\u041f\u041b\u0410\u0422\u0424\u041e\u0420\u041c\u0410", "\u0422\u041e\u041d\u041d\u0415\u041b\u042c", "\u041f\u0423\u0422\u0415\u041f\u0420\u041e\u0412\u041e\u0414", "\u041f\u0415\u0420\u0415\u0425\u041e\u0414", "\u0420\u0410\u0411\u041e\u0422\u0410\u042e\u0422 \u041b\u042e\u0414\u0418", "\u0421\u0418\u0413\u041d\u0410\u041b", "\u041f\u0420\u041e\u0411\u0410 \u0422\u041e\u0420\u041c\u041e\u0417\u041e\u0412", "\u0421\u0422\u0410\u041d\u0426\u0418\u042f", "\u0413\u0410\u0417\u041e\u041f\u0420\u041e\u0412\u041e\u0414", "\u041f\u041e\u041d\u0410\u0411"};
    private String distanceToTarget = "";
    private String targetName = "";
    private String coordinate = "";
    private String acceleration = "";
    private String distanceToSaut = "";
    private String brakingCoefficient = "";
    private String roadBias = "";
    private String stationName = "";
    private boolean cassetteEquipped;
    private boolean neutralInsert;
    private boolean tokorazdel;
    private String distanceTokorazdelOrNeutralInsert = "";
    private boolean visiblyReleaseBan;
    private boolean visiblyServiceBraking;
    private String channel = "";
    private final String[] frequenciesAlsn = new String[]{"25 \u0413\u0426", "50 \u0413\u0426", "75 \u0413\u0426"};
    private int currentIndexAlsn;
    private String trainMode = "";
    private boolean visiblyTskbmPower;
    private int videoCoord;
    private boolean visiblyChart;
    private TRAFFIC_SIGNALS trafficSignal;

    public void copy(DisplayData displayData) {
        this.power = displayData.power;
        this.infoStr = displayData.infoStr;
        this.valueStr = displayData.valueStr;
        this.wayNum = displayData.wayNum;
        this.worldTime = displayData.worldTime;
        this.allowableSpeed = displayData.allowableSpeed;
        this.cruiseControlSpeed = displayData.cruiseControlSpeed;
        this.actualSpeed = displayData.actualSpeed;
        this.targetSpeed = displayData.targetSpeed;
        this.visiblyActualSpeed = displayData.visiblyActualSpeed;
        this.dimensionOfTheProgressBar = displayData.dimensionOfTheProgressBar;
        this.pressureTC = displayData.pressureTC;
        this.pressureTM = displayData.pressureTM;
        this.pressureUR = displayData.pressureUR;
        this.progressTC = displayData.progressTC;
        this.progressTM = displayData.progressTM;
        this.progressUR = displayData.progressUR;
        this.reverse = displayData.reverse;
        this.targetType = displayData.targetType;
        this.distanceToTarget = displayData.distanceToTarget;
        this.targetName = displayData.targetName;
        this.coordinate = displayData.coordinate;
        this.acceleration = displayData.acceleration;
        this.distanceToSaut = displayData.distanceToSaut;
        this.brakingCoefficient = displayData.brakingCoefficient;
        this.roadBias = displayData.roadBias;
        this.stationName = displayData.stationName;
        this.cassetteEquipped = displayData.cassetteEquipped;
        this.neutralInsert = displayData.neutralInsert;
        this.tokorazdel = displayData.tokorazdel;
        this.distanceTokorazdelOrNeutralInsert = displayData.distanceTokorazdelOrNeutralInsert;
        this.visiblyAttention = displayData.visiblyAttention;
        this.visiblyReleaseBan = displayData.visiblyReleaseBan;
        this.visiblyServiceBraking = displayData.visiblyServiceBraking;
        this.channel = displayData.channel;
        this.trainMode = displayData.trainMode;
        this.visiblyTskbmPower = displayData.visiblyTskbmPower;
        this.videoCoord = displayData.videoCoord;
        this.visiblyChart = displayData.visiblyChart;
        this.trafficSignal = displayData.trafficSignal;
    }

    public boolean equalityTest(DisplayData displayData) {
        if (this.power != displayData.power) {
            return false;
        }
        if (!this.infoStr.equals(displayData.infoStr)) {
            return false;
        }
        if (!this.valueStr.equals(displayData.valueStr)) {
            return false;
        }
        if (!this.wayNum.equals(displayData.wayNum)) {
            return false;
        }
        if (!this.worldTime.equals(displayData.worldTime)) {
            return false;
        }
        if (this.allowableSpeed != displayData.allowableSpeed) {
            return false;
        }
        if (this.cruiseControlSpeed != displayData.cruiseControlSpeed) {
            return false;
        }
        if (this.actualSpeed != displayData.actualSpeed) {
            return false;
        }
        if (this.targetSpeed != displayData.targetSpeed) {
            return false;
        }
        if (this.visiblyActualSpeed != displayData.visiblyActualSpeed) {
            return false;
        }
        if (!this.dimensionOfTheProgressBar.equals(displayData.dimensionOfTheProgressBar)) {
            return false;
        }
        if (!this.pressureTC.equals(displayData.pressureTC)) {
            return false;
        }
        if (!this.pressureTM.equals(displayData.pressureTM)) {
            return false;
        }
        if (!this.pressureUR.equals(displayData.pressureUR)) {
            return false;
        }
        if (this.progressTC != displayData.progressTC) {
            return false;
        }
        if (this.progressTM != displayData.progressTM) {
            return false;
        }
        if (this.progressUR != displayData.progressUR) {
            return false;
        }
        if (this.reverse != displayData.reverse) {
            return false;
        }
        if (!this.targetType.equals(displayData.targetType)) {
            return false;
        }
        if (!this.distanceToTarget.equals(displayData.distanceToTarget)) {
            return false;
        }
        if (!this.targetName.equals(displayData.targetName)) {
            return false;
        }
        if (!this.coordinate.equals(displayData.coordinate)) {
            return false;
        }
        if (!this.acceleration.equals(displayData.acceleration)) {
            return false;
        }
        if (!this.distanceToSaut.equals(displayData.distanceToSaut)) {
            return false;
        }
        if (!this.brakingCoefficient.equals(displayData.brakingCoefficient)) {
            return false;
        }
        if (!this.roadBias.equals(displayData.roadBias)) {
            return false;
        }
        if (!this.stationName.equals(displayData.stationName)) {
            return false;
        }
        if (this.cassetteEquipped != displayData.cassetteEquipped) {
            return false;
        }
        if (this.tokorazdel != displayData.tokorazdel) {
            return false;
        }
        if (this.neutralInsert != displayData.neutralInsert) {
            return false;
        }
        if (!this.distanceTokorazdelOrNeutralInsert.equals(displayData.distanceTokorazdelOrNeutralInsert)) {
            return false;
        }
        if (this.visiblyAttention != displayData.visiblyAttention) {
            return false;
        }
        if (this.visiblyReleaseBan != displayData.visiblyReleaseBan) {
            return false;
        }
        if (this.visiblyServiceBraking != displayData.visiblyServiceBraking) {
            return false;
        }
        if (!this.channel.equals(displayData.channel)) {
            return false;
        }
        if (!this.trainMode.equals(displayData.trainMode)) {
            return false;
        }
        if (this.visiblyTskbmPower != displayData.visiblyTskbmPower) {
            return false;
        }
        if (this.videoCoord != displayData.videoCoord) {
            return false;
        }
        if (this.visiblyChart != displayData.visiblyChart) {
            return false;
        }
        return this.trafficSignal == displayData.trafficSignal;
    }

    public void setPower(boolean power) {
        this.power = power;
    }

    public boolean isPower() {
        return this.power;
    }

    public void setInfoStr(String infoStr) {
        this.infoStr = infoStr;
    }

    public String getInfoStr() {
        return this.infoStr;
    }

    public void setValueStr(String valueStr) {
        this.valueStr = valueStr;
    }

    public String getValueStr() {
        return this.valueStr;
    }

    public void setWayNum(String wayNum) {
        this.wayNum = wayNum;
    }

    public String getWayNum() {
        return this.wayNum;
    }

    public void setWorldTime(String worldTime) {
        this.worldTime = worldTime;
    }

    public String getWorldTime() {
        return this.worldTime;
    }

    public void setActualSpeed(int actualSpeed) {
        this.actualSpeed = actualSpeed;
    }

    public int getActualSpeed() {
        return this.actualSpeed;
    }

    public void setAllowableSpeed(int allowableSpeed) {
        this.allowableSpeed = allowableSpeed;
    }

    public int getAllowableSpeed() {
        return this.allowableSpeed;
    }

    public void setCruiseControlSpeed(int cruiseControlSpeed) {
        this.cruiseControlSpeed = cruiseControlSpeed;
    }

    public int getCruiseControlSpeed() {
        return this.cruiseControlSpeed;
    }

    public void setTargetSpeed(int targetSpeed) {
        this.targetSpeed = targetSpeed;
    }

    public int getTargetSpeed() {
        return this.targetSpeed;
    }

    public void setVisiblyActualSpeed(boolean visiblyActualSpeed) {
        this.visiblyActualSpeed = visiblyActualSpeed;
    }

    public boolean isVisiblyActualSpeed() {
        return this.visiblyActualSpeed;
    }

    public void setDimensionOfTheProgressBar(String dimensionOfTheProgressBar) {
        this.dimensionOfTheProgressBar = dimensionOfTheProgressBar;
    }

    public String getDimensionOfTheProgressBar() {
        return this.dimensionOfTheProgressBar;
    }

    public void setPressureUR(String pressureUR) {
        this.pressureUR = pressureUR;
    }

    public String getPressureUR() {
        return this.pressureUR;
    }

    public void setPressureTC(String pressureTC) {
        this.pressureTC = pressureTC;
    }

    public String getPressureTC() {
        return this.pressureTC;
    }

    public void setPressureTM(String pressureTM) {
        this.pressureTM = pressureTM;
    }

    public String getPressureTM() {
        return this.pressureTM;
    }

    public void setProgressTM(float progressTM) {
        this.progressTM = progressTM;
    }

    public float getProgressUR() {
        return this.progressUR;
    }

    public void setProgressTC(float progressTC) {
        this.progressTC = progressTC;
    }

    public float getProgressTC() {
        return this.progressTC;
    }

    public void setProgressUR(float progressUR) {
        this.progressUR = progressUR;
    }

    public float getProgressTM() {
        return this.progressTM;
    }

    public void setReverse(int reverse) {
        this.reverse = reverse;
    }

    public int getReverse() {
        return this.reverse;
    }

    public void setTargetType(int value) {
        this.targetType = value < this.targetCodes.length ? this.targetCodes[value] : this.targetCodes[0];
    }

    public String getTargetType() {
        return this.targetType;
    }

    public void setDistanceToTarget(String distanceToTarget) {
        this.distanceToTarget = distanceToTarget;
    }

    public String getDistanceToTarget() {
        return this.distanceToTarget;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public String getCoordinate() {
        return this.coordinate;
    }

    public void setAcceleration(String acceleration) {
        this.acceleration = acceleration;
    }

    public String getAcceleration() {
        return this.acceleration;
    }

    public void setDistanceToSaut(String distanceToSaut) {
        this.distanceToSaut = distanceToSaut;
    }

    public String getDistanceToSaut() {
        return this.distanceToSaut;
    }

    public void setBrakingCoefficient(String brakingCoefficient) {
        this.brakingCoefficient = brakingCoefficient;
    }

    public String getBrakingCoefficient() {
        return this.brakingCoefficient;
    }

    public void setRoadBias(String roadBias) {
        this.roadBias = roadBias;
    }

    public String getRoadBias() {
        return this.roadBias;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationName() {
        return this.stationName;
    }

    public void setCassetteEquipped(boolean cassetteEquipped) {
        this.cassetteEquipped = cassetteEquipped;
    }

    public boolean isCassetteEquipped() {
        return this.cassetteEquipped;
    }

    public void setNeutralInsert(boolean neutralInsert) {
        this.neutralInsert = neutralInsert;
    }

    public boolean isNeutralInsert() {
        return this.neutralInsert;
    }

    public void setTokorazdel(boolean tokorazdel) {
        this.tokorazdel = tokorazdel;
    }

    public boolean isTokorazdel() {
        return this.tokorazdel;
    }

    public void setDistanceTokorazdelOrNeutralInsert(String distanceTokorazdelOrNeutralInsert) {
        this.distanceTokorazdelOrNeutralInsert = distanceTokorazdelOrNeutralInsert;
    }

    public String getDistanceTokorazdelOrNeutralInsert() {
        return this.distanceTokorazdelOrNeutralInsert;
    }

    public void setVisiblyAttention(boolean visiblyAttention) {
        this.visiblyAttention = visiblyAttention;
    }

    public boolean isVisiblyAttention() {
        return this.visiblyAttention;
    }

    public void setVisiblyReleaseBan(boolean visiblyReleaseBan) {
        this.visiblyReleaseBan = visiblyReleaseBan;
    }

    public boolean isVisiblyReleaseBan() {
        return this.visiblyReleaseBan;
    }

    public void setVisiblyServiceBraking(boolean visiblyServiceBraking) {
        this.visiblyServiceBraking = visiblyServiceBraking;
    }

    public boolean isVisiblyServiceBraking() {
        return this.visiblyServiceBraking;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setTrainMode(String trainMode) {
        this.trainMode = trainMode;
    }

    public String getTrainMode() {
        return this.trainMode;
    }

    public void setVisiblyTskbmPower(boolean visiblyTskbmPower) {
        this.visiblyTskbmPower = visiblyTskbmPower;
    }

    public boolean isVisiblyTskbmPower() {
        return this.visiblyTskbmPower;
    }

    public void setCurrentIndexAlsn(int currentIndexAlsn) {
        this.currentIndexAlsn = currentIndexAlsn;
    }

    public int getCurrentIndexAlsn() {
        return this.currentIndexAlsn;
    }

    public String getFrequenciesAlsn() {
        return this.frequenciesAlsn[this.currentIndexAlsn];
    }

    public void setVideoCoord(int videoCoord) {
        this.videoCoord = videoCoord;
    }

    public int getVideoCoord() {
        return this.videoCoord;
    }

    public void setVisiblyChart(boolean visiblyChart) {
        this.visiblyChart = visiblyChart;
    }

    public boolean isVisiblyChart() {
        return this.visiblyChart;
    }

    public void setTrafficSignal(TRAFFIC_SIGNALS trafficSignal) {
        this.trafficSignal = trafficSignal;
    }

    public TRAFFIC_SIGNALS getTrafficSignal() {
        return this.trafficSignal;
    }
}

