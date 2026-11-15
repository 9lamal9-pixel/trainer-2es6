/*
 * Decompiled with CFR 0.152.
 */
package base.logic;

import base.data.BIL_MODEL;
import base.data.BLOCK_MODE;
import base.data.BlockData;
import base.data.DisplayData;
import base.data.NetworkData;
import base.data.TRAFFIC_SIGNALS;
import base.logic.KeyboardAndCommandController;
import base.sound.SoundController;
import base.timers.TimerController;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogicController {
    private static final Logger logger = LogManager.getLogger(LogicController.class);
    private BlockData blockDataInstance;
    private int whiteSpeedReductionDistance;
    private float signalLossDistance;
    private int signalLossSpeed;
    private float distanceTravelled;
    private boolean isRollingDown = false;
    private boolean isOverSpeed = false;
    private boolean isSingleVigilanceCheck = false;
    private int rbsHoldCheck = 0;
    private int rbRbpRbsHoldCheck = 0;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private double deltaDistance;
    private static final double PASCAL_TO_SM = 10.19716212978;

    public LogicController() {
        this.timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.blockDataInstance = BlockData.getInstance();
        logger.debug("init");
    }

    public void update() {
        NetworkData networkData = (NetworkData)this.blockDataInstance.getCurrentNetworkDataQueue().poll();
        if (networkData == null) {
            if (this.blockDataInstance.getCurNetworkData().getBlockPower() == 1) {
                this.updateLogic();
            }
            if (this.blockDataInstance.getPrevNetworkData().getBlockPower() == 1) {
                TimerController.getInstance().enableLackOfNetwork();
            }
        } else {
            this.blockDataInstance.getCurNetworkData().updateDataFromClass(networkData);
            if (this.blockDataInstance.getCurNetworkData().getBlockPower() == 1) {
                if (TRAFFIC_SIGNALS.values()[this.blockDataInstance.getPrevNetworkData().getTrafficLightCode()] == TRAFFIC_SIGNALS.RED && TRAFFIC_SIGNALS.values()[this.blockDataInstance.getCurNetworkData().getTrafficLightCode()] == TRAFFIC_SIGNALS.WHITE) {
                    this.blockDataInstance.getCurNetworkData().setTrafficLightCode(this.blockDataInstance.getPrevNetworkData().getTrafficLightCode());
                }
                if (this.blockDataInstance.getPrevNetworkData().getBlockPower() == 0) {
                    this.blockDataInstance.getCommandSettingsData().invokeTrainCategory();
                    this.blockDataInstance.getCommandSettingsData().invokeWay();
                }
                this.updateLogic();
            }
            if (this.blockDataInstance.getCurNetworkData().getRoadIndex() != this.blockDataInstance.getPrevNetworkData().getRoadIndex()) {
                this.blockDataInstance.reloadRoadData(this.blockDataInstance.getCurNetworkData().getRoadIndex());
            }
            if (this.blockDataInstance.getCurNetworkData().getBlockPower() == 0 && this.blockDataInstance.getPrevNetworkData().getBlockPower() == 1) {
                this.blockDataInstance.removePower();
                TimerController.getInstance().powerFailure();
                SoundController.getInstance().stopCyclicBeep();
            } else {
                this.blockDataInstance.getPrevNetworkData().updateDataFromClass(this.blockDataInstance.getCurNetworkData());
            }
            if (TimerController.getInstance().isThereLackOfNetwork()) {
                TimerController.getInstance().disableLackOfNetwork();
            }
        }
    }

    private void updateLogic() {
        boolean rb = this.blockDataInstance.getCurNetworkData().getRbButton() == 1;
        boolean rbp = this.blockDataInstance.getCurNetworkData().getRbpButton() == 1;
        boolean rbs = this.blockDataInstance.getCurNetworkData().getRbsButton() == 1;
        boolean currentTraction = this.blockDataInstance.getCurNetworkData().getTraction() == 1;
        boolean previousTraction = this.blockDataInstance.getPrevNetworkData().getTraction() == 1;
        float actualSpeed = this.blockDataInstance.getCurNetworkData().getActualSpeed();
        float previousActualSpeed = this.blockDataInstance.getPrevNetworkData().getActualSpeed();
        boolean epkKey = this.blockDataInstance.getCurNetworkData().getEpkKey() == 1;
        boolean previousEpkKey = this.blockDataInstance.getPrevNetworkData().getEpkKey() == 1;
        TRAFFIC_SIGNALS currentTrafficLight = TRAFFIC_SIGNALS.values()[this.blockDataInstance.getCurNetworkData().getTrafficLightCode()];
        TRAFFIC_SIGNALS previousTrafficLight = TRAFFIC_SIGNALS.values()[this.blockDataInstance.getPrevNetworkData().getTrafficLightCode()];
        boolean isMovement = actualSpeed > 0.0f;
        boolean isSautEquipped = this.blockDataInstance.getCurNetworkData().getSautEquipped() == 1;
        boolean isEkEquipped = this.blockDataInstance.getCurNetworkData().getEkEquipped() == 1;
        boolean isTskbmEquipped = this.blockDataInstance.getCurNetworkData().getTskbmEquipped() == 1;
        int previousTargetSpeed = this.blockDataInstance.getLocalData().getPreviousTargetSpeed();
        if (currentTraction != previousTraction) {
            logger.debug("traction {}", (Object)currentTraction);
        }
        if (isSautEquipped != (this.blockDataInstance.getPrevNetworkData().getSautEquipped() == 1)) {
            logger.debug("saut equipped {}", (Object)isSautEquipped);
        }
        if (isEkEquipped != (this.blockDataInstance.getPrevNetworkData().getEkEquipped() == 1)) {
            logger.debug("EK equipped {}", (Object)isEkEquipped);
        }
        if (isTskbmEquipped != (this.blockDataInstance.getPrevNetworkData().getTskbmEquipped() == 1)) {
            logger.debug("Tskbm equipped {}", (Object)isTskbmEquipped);
        }
        if (actualSpeed == 0.0f) {
            this.blockDataInstance.getLocalData().setDistanceTraveledAfterStopping(0.0);
        } else {
            double t = (double)(this.blockDataInstance.getCurNetworkData().getWorldTime() - this.blockDataInstance.getPrevNetworkData().getWorldTime()) * 0.001;
            this.deltaDistance = (double)this.blockDataInstance.getCurNetworkData().getAcceleration() * Math.pow(t, 2.0) / 2.0 + (double)previousActualSpeed * 1000.0 / 3600.0 * t;
            this.blockDataInstance.getLocalData().setDistanceTraveledAfterStopping(this.blockDataInstance.getLocalData().getDistanceTraveledAfterStopping() + this.deltaDistance);
            this.distanceTravelled = (float)((double)this.distanceTravelled + this.deltaDistance);
            this.manualCalculationOfTheCurrentCoordinate();
        }
        if (this.blockDataInstance.getPrevNetworkData().getMalfunctionCode() != this.blockDataInstance.getCurNetworkData().getMalfunctionCode()) {
            SoundController.getInstance().playMalfunction(this.blockDataInstance.getCurNetworkData().getMalfunctionCode());
        }
        this.checkBrakingValveOpened();
        this.keyChangesEpk(previousEpkKey, epkKey);
        this.doubleTractionMode(rb, rbp);
        KeyboardAndCommandController.getInstance().update();
        if (this.blockDataInstance.getCurNetworkData().getEpkKey() == 1) {
            this.rollingControl(currentTraction, actualSpeed, previousActualSpeed);
            this.beginningOfTheMovement(currentTraction, previousTraction, actualSpeed);
            this.allowableSpeedCalculation(previousTrafficLight, currentTrafficLight, actualSpeed, isEkEquipped, isSautEquipped);
            float deltaSpeed = (float)this.blockDataInstance.getLocalData().getAllowableSpeed() - this.blockDataInstance.getCurNetworkData().getActualSpeed();
            this.checkingOverSpeed(currentTrafficLight, deltaSpeed, isMovement, isSautEquipped);
            int currentTargetSpeed = this.blockDataInstance.getLocalData().getTargetSpeed();
            this.periodicVigilanceCheck(currentTrafficLight, isEkEquipped, isSautEquipped, isTskbmEquipped, isMovement, actualSpeed, currentTargetSpeed);
            this.singleVigilanceCheck(previousTrafficLight, currentTrafficLight, previousTargetSpeed, currentTargetSpeed, currentTraction, previousTraction, isEkEquipped, isSautEquipped, isTskbmEquipped, isMovement);
            if (this.blockDataInstance.getPrevNetworkData().getTargetCode() != this.blockDataInstance.getCurNetworkData().getTargetCode() && this.blockDataInstance.getCurNetworkData().getEkEquipped() == 1) {
                SoundController.getInstance().playTarget(this.blockDataInstance.getCurNetworkData().getTargetCode(), currentTrafficLight.ordinal());
            }
        }
        this.checkingForALongHoldOfVigilanceHandle(rb, rbp, rbs);
        this.disableAttentionTest(rb, rbp, rbs);
        if (!TimerController.getInstance().isLongHoldOfVigilanceHandle()) {
            this.recoverEpkHoldingCoil(rb, rbp, rbs, actualSpeed, epkKey);
        }
        this.updateDisplayData();
        this.blockDataInstance.getLocalData().setPreviousTargetSpeed();
    }

    private void keyChangesEpk(boolean previousEpkKey, boolean epkKey) {
        if (!previousEpkKey && epkKey) {
            TimerController.getInstance().setEpkHoldingCoil(true);
            SoundController.getInstance().playBeep();
            if (!this.blockDataInstance.getDisplayData().getInfoStr().equals("")) {
                this.blockDataInstance.getDisplayData().setInfoStr("");
            }
        } else if (previousEpkKey && !epkKey) {
            TimerController.getInstance().disablingEpkKey();
            SoundController.getInstance().stopCyclicBeep();
            this.blockDataInstance.getDisplayData().setInfoStr("");
            this.blockDataInstance.getDisplayData().setValueStr("");
            this.blockDataInstance.removeEPK();
            this.blockOffEPK();
        }
    }

    private void checkBrakingValveOpened() {
        if (this.blockDataInstance.getCurNetworkData().getBrakingValveOpenedFromKOH() == 1) {
            this.blockDataInstance.getLocalData().setBrakingValveOpenedFromKOH(true);
            BlockData.getInstance().getDisplayData().setInfoStr("\u0421\u0420\u042b\u0412 \u041a\u041e\u041d");
            SoundController.getInstance().stopCyclicBeep();
            if (BlockData.getInstance().getLocalData().isUseServiceBrakingOfSaut()) {
                BlockData.getInstance().getLocalData().setUseServiceBrakingOfSaut(false);
                logger.debug("UseServiceBrakingOfSaut off BrakingValveOpened");
            }
        }
        if (this.blockDataInstance.getCurNetworkData().getBrakingValveOpenedFromEPK() == 1) {
            this.blockDataInstance.getLocalData().setBrakingValveOpenedFromEPK(true);
            BlockData.getInstance().getDisplayData().setInfoStr("\u0421\u0420\u042b\u0412 \u042d\u041f\u041a");
            SoundController.getInstance().stopCyclicBeep();
            if (BlockData.getInstance().getLocalData().isUseServiceBrakingOfSaut()) {
                BlockData.getInstance().getLocalData().setUseServiceBrakingOfSaut(false);
                logger.debug("UseServiceBrakingOfSaut off BrakingValveOpened");
            }
        }
    }

    private void manualCalculationOfTheCurrentCoordinate() {
        try {
            int reverseMark;
            double coordinates = Integer.valueOf(this.blockDataInstance.getCommandSettingsData().getCurrent_coordinate()).intValue();
            int coordinateChange = this.blockDataInstance.getCommandSettingsData().getValue_of_coordinate_change() == 0 ? 1 : -1;
            int parityMark = Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getWay_number()) % 2 != 0 ? 1 : -1;
            int wayMark = Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getCorrectness_of_way()) == 1 ? 1 : -1;
            switch (this.blockDataInstance.getCurNetworkData().getReverse()) {
                case 1: {
                    reverseMark = 1;
                    break;
                }
                case 2: {
                    reverseMark = -1;
                    break;
                }
                default: {
                    reverseMark = 0;
                }
            }
            coordinates += (double)(coordinateChange * parityMark * wayMark * reverseMark) * this.deltaDistance;
            if (coordinates < 0.0) {
                coordinates = 0.0;
            } else if (coordinates > 9999999.0) {
                coordinates = 9999999.0;
            }
            this.blockDataInstance.getCommandSettingsData().setCurrent_coordinate(String.format("%07d", (int)Math.floor(coordinates)));
        }
        catch (NumberFormatException e) {
            logger.error(e);
        }
    }

    private void recoverEpkHoldingCoil(boolean rb, boolean rbp, boolean rbs, float actualSpeed, boolean keyEpk) {
        if (!TimerController.getInstance().isEpkHoldingCoil() && keyEpk && !this.blockDataInstance.getLocalData().isBrakingValveOpenedFromKOH() && !this.blockDataInstance.getLocalData().isBrakingValveOpenedFromEPK()) {
            if (rbs) {
                ++this.rbsHoldCheck;
            }
            if (this.isSingleVigilanceCheck && (rb || rbp)) {
                ++this.rbRbpRbsHoldCheck;
            }
            if (this.isRollingDown) {
                if (actualSpeed == 0.0f) {
                    this.rbsHoldCheck = 0;
                    this.isRollingDown = false;
                    logger.debug("voltage recover from rolling down recoverEpkHoldingCoil()");
                    TimerController.getInstance().recoverEpkHoldingCoil();
                }
            } else if (this.isOverSpeed) {
                if ((float)this.blockDataInstance.getLocalData().getAllowableSpeed() - this.blockDataInstance.getCurNetworkData().getActualSpeed() >= 0.0f) {
                    this.rbsHoldCheck = 0;
                    this.isOverSpeed = false;
                    logger.debug("voltage recover from over speed recoverEpkHoldingCoil()");
                    TimerController.getInstance().recoverEpkHoldingCoil();
                }
            } else if (this.isSingleVigilanceCheck) {
                if (this.rbsHoldCheck == 20 || this.rbRbpRbsHoldCheck == 20) {
                    this.rbsHoldCheck = 0;
                    this.rbRbpRbsHoldCheck = 0;
                    this.isSingleVigilanceCheck = false;
                    logger.debug("voltage recover from single vigilance check recoverEpkHoldingCoil()");
                    TimerController.getInstance().recoverEpkHoldingCoil();
                }
            } else if (this.rbsHoldCheck == 20 || actualSpeed == 0.0f) {
                this.rbsHoldCheck = 0;
                logger.debug("voltage recover from rbs recoverEpkHoldingCoil()");
                TimerController.getInstance().recoverEpkHoldingCoil();
            }
        }
    }

    private void checkingForALongHoldOfVigilanceHandle(boolean rb, boolean rbp, boolean rbs) {
        if (TimerController.getInstance().isEpkHoldingCoil()) {
            if (rb || rbp || rbs) {
                TimerController.getInstance().enableLongHoldOfVigilanceHandleTimer();
            } else {
                TimerController.getInstance().disableLongHoldOfVigilanceHandleTimer();
            }
        }
    }

    private void disableAttentionTest(boolean rb, boolean rbp, boolean rbs) {
        if (TimerController.getInstance().isThereIsAttention()) {
            if (rb || rbp || rbs) {
                ++this.rbRbpRbsHoldCheck;
            }
            if (this.rbRbpRbsHoldCheck == 20) {
                this.rbRbpRbsHoldCheck = 0;
                TimerController.getInstance().disableAttentionTest();
            }
        }
    }

    private void doubleTractionMode(boolean rb, boolean rbp) {
        if (rb && rbp && this.blockDataInstance.getLocalData().getBlockMode() != BLOCK_MODE.RDT) {
            this.blockDataInstance.getLocalData().setRbAndRbpToRmp(this.blockDataInstance.getLocalData().getRbAndRbpToRmp() + 1);
            if (this.blockDataInstance.getLocalData().getRbAndRbpToRmp() > 20) {
                TimerController.getInstance().enableButtonRBRBPOffTimer();
            }
        }
    }

    private void rollingControl(boolean currentTraction, float actualSpeed, float previousActualSpeed) {
        if (!currentTraction && previousActualSpeed <= 1.0f && actualSpeed > 1.0f) {
            this.isRollingDown = true;
            TimerController.getInstance().startRollingDown30sec();
            SoundController.getInstance().play("\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u043d\u0430\u0447\u0430\u043b\u043e \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f");
        }
        if (TimerController.getInstance().isRollingDown30sec()) {
            if (currentTraction || actualSpeed == 0.0f) {
                TimerController.getInstance().stopRollingDown30sec();
                this.isRollingDown = false;
            }
            if (actualSpeed >= 2.0f) {
                logger.debug("voltage remove due to rolling down RollingControl() actualSpeed: {}", (Object)Float.valueOf(actualSpeed));
                TimerController.getInstance().stopRollingDown30sec();
                TimerController.getInstance().disableEpkHoldingCoil();
            }
        }
    }

    private void beginningOfTheMovement(boolean currentTraction, boolean previousTraction, float actualSpeed) {
        if (this.blockDataInstance.getLocalData().getBlockMode() == BLOCK_MODE.TRAIN) {
            if (!previousTraction && currentTraction) {
                TimerController.getInstance().startBeginningOfTheMovement();
            }
            if ((TimerController.getInstance().isScheduledBeginningOfTheMovementSpeedWork() || TimerController.getInstance().isScheduledBeginningOfTheMovementDistanceWork()) && !currentTraction) {
                TimerController.getInstance().stopBeginningOfTheMovement();
            }
            if (TimerController.getInstance().isDidNotAccelerateTo2KmH() && actualSpeed >= 2.0f) {
                logger.debug("voltage remove due to long set 2km beginningOfTheMovement() actualSpeed: {}", (Object)Float.valueOf(actualSpeed));
                TimerController.getInstance().disableEpkHoldingCoil();
                TimerController.getInstance().setDidNotAccelerateTo2KmH(false);
            }
        }
    }

    private void allowableSpeedCalculation(TRAFFIC_SIGNALS previousTrafficLight, TRAFFIC_SIGNALS currentTrafficLight, float actualSpeed, boolean isEkEquipped, boolean isSautEquipped) {
        PairOfSpeeds speedsOnALSN;
        int speedToGreen = Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getSpeed_to_green());
        int speedToYellow = Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getSpeed_to_yellow());
        int speedToWhite = Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getSpeed_to_white());
        int distanceToTrafficLight = this.blockDataInstance.getCurNetworkData().getDistanceToTrafficLight();
        PairOfSpeeds speedsOnEK = new PairOfSpeeds();
        PairOfSpeeds speedsOnSaut = new PairOfSpeeds();
        if (this.blockDataInstance.getLocalData().isK799() || this.blockDataInstance.getLocalData().isK809() || this.blockDataInstance.getLocalData().getBlockMode() != BLOCK_MODE.TRAIN) {
            if (this.blockDataInstance.getLocalData().isModeChangeOnCommandk799k809()) {
                speedsOnALSN = this.dataProcessingALSN(TRAFFIC_SIGNALS.WHITE, TRAFFIC_SIGNALS.WHITE, distanceToTrafficLight, speedToGreen, speedToYellow, this.blockDataInstance.getLocalData().getSpeedOnWhiteFromCommand(), actualSpeed, isEkEquipped);
            } else {
                speedsOnALSN = this.dataProcessingALSN(TRAFFIC_SIGNALS.NONE, TRAFFIC_SIGNALS.WHITE, distanceToTrafficLight, speedToGreen, speedToYellow, this.blockDataInstance.getLocalData().getSpeedOnWhiteFromCommand(), actualSpeed, isEkEquipped);
                this.blockDataInstance.getLocalData().setModeChangeOnCommandk799k809(true);
            }
            this.blockDataInstance.getLocalData().setTrafficSignal(TRAFFIC_SIGNALS.WHITE);
        } else {
            if (this.blockDataInstance.getLocalData().isModeChangeOnCommandk799k809()) {
                this.blockDataInstance.getLocalData().setModeChangeOnCommandk799k809(false);
                previousTrafficLight = TRAFFIC_SIGNALS.WHITE;
            }
            speedsOnALSN = this.dataProcessingALSN(previousTrafficLight, currentTrafficLight, distanceToTrafficLight, speedToGreen, speedToYellow, this.blockDataInstance.getLocalData().isK809() ? this.blockDataInstance.getLocalData().getSpeedOnWhiteFromCommand() : speedToWhite, actualSpeed, isEkEquipped);
            this.blockDataInstance.getLocalData().setTrafficSignal(currentTrafficLight);
        }
        if (this.blockDataInstance.getCurNetworkData().getAllowableSpeedByEK() != 0 && this.blockDataInstance.getCurNetworkData().getTargetSpeedByEK() != 0) {
            speedsOnEK = this.dataProcessingEK(this.blockDataInstance.getCurNetworkData().getTargetSpeedByEK(), this.blockDataInstance.getCurNetworkData().getAllowableSpeedByEK(), this.blockDataInstance.getCurNetworkData().getDistanceToNextSectionEK());
        }
        if (isSautEquipped) {
            speedsOnSaut = this.dataProcessingSaut(previousTrafficLight, currentTrafficLight, distanceToTrafficLight, speedToGreen, speedToYellow);
        }
        if (!isEkEquipped && !isSautEquipped || this.blockDataInstance.getLocalData().isK799() || this.blockDataInstance.getLocalData().isVk() || this.blockDataInstance.getLocalData().isOtpr() || this.blockDataInstance.getLocalData().isK20() || this.blockDataInstance.getLocalData().isPull() || this.blockDataInstance.getLocalData().getBlockMode() != BLOCK_MODE.TRAIN) {
            this.blockDataInstance.getLocalData().setTargetSpeed(speedsOnALSN.targetSpeed);
            this.blockDataInstance.getLocalData().setAllowableSpeed(speedsOnALSN.allowableSpeed);
        } else {
            if (speedsOnALSN.targetSpeed < speedsOnEK.targetSpeed) {
                this.blockDataInstance.getLocalData().setTargetSpeed(Math.min(speedsOnALSN.targetSpeed, speedsOnSaut.targetSpeed));
            } else {
                this.blockDataInstance.getLocalData().setTargetSpeed(Math.min(speedsOnEK.targetSpeed, speedsOnSaut.targetSpeed));
            }
            if (speedsOnALSN.allowableSpeed < speedsOnEK.allowableSpeed) {
                this.blockDataInstance.getLocalData().setAllowableSpeed(Math.min(speedsOnALSN.allowableSpeed, speedsOnSaut.allowableSpeed));
            } else {
                this.blockDataInstance.getLocalData().setAllowableSpeed(Math.min(speedsOnEK.allowableSpeed, speedsOnSaut.allowableSpeed));
            }
        }
    }

    private PairOfSpeeds dataProcessingALSN(TRAFFIC_SIGNALS previousTrafficLight, TRAFFIC_SIGNALS currentTrafficLight, int distanceToTrafficLight, int speedToGreen, int speedToYellow, int speedToWhite, float currentActualSpeed, boolean isEkEquipped) {
        int newAllowableSpeed;
        int targetSpeedALSN = 1000;
        int allowableSpeedALSN = 1000;
        switch (currentTrafficLight) {
            case GREEN: {
                targetSpeedALSN = speedToGreen;
                allowableSpeedALSN = speedToGreen;
                break;
            }
            case YELLOW: {
                targetSpeedALSN = speedToYellow;
                allowableSpeedALSN = speedToGreen;
                break;
            }
            case RED_YELLOW: {
                targetSpeedALSN = isEkEquipped ? 0 : 20;
                allowableSpeedALSN = speedToYellow;
                break;
            }
            case RED: {
                targetSpeedALSN = 20;
                allowableSpeedALSN = 20;
                break;
            }
            case WHITE: {
                switch (previousTrafficLight) {
                    case NONE: 
                    case GREEN: 
                    case YELLOW: {
                        targetSpeedALSN = speedToWhite;
                        if (currentActualSpeed > (float)speedToWhite) {
                            allowableSpeedALSN = (int)currentActualSpeed + 5;
                            break;
                        }
                        allowableSpeedALSN = speedToWhite;
                        break;
                    }
                    case RED_YELLOW: 
                    case RED: {
                        targetSpeedALSN = 0;
                        int n = allowableSpeedALSN = this.blockDataInstance.getLocalData().getDistanceTraveledAfterStopping() > 200.0 ? 0 : 20;
                    }
                }
                if (!this.blockDataInstance.getLocalData().isK799() && !this.blockDataInstance.getLocalData().isK809() && this.blockDataInstance.getLocalData().getBlockMode() == BLOCK_MODE.TRAIN) break;
                targetSpeedALSN = speedToWhite;
            }
        }
        if (previousTrafficLight != currentTrafficLight) {
            SoundController.getInstance().playBeep();
            switch (currentTrafficLight) {
                case GREEN: 
                case YELLOW: 
                case RED_YELLOW: 
                case RED: {
                    this.blockDataInstance.getLocalData().setVk(false);
                    break;
                }
                case WHITE: {
                    switch (previousTrafficLight) {
                        case NONE: 
                        case GREEN: 
                        case YELLOW: {
                            if (currentActualSpeed > (float)speedToWhite) {
                                this.whiteSpeedReductionDistance = (int)(currentActualSpeed + 5.0f - (float)speedToWhite) * 50;
                                this.signalLossDistance = this.distanceTravelled;
                                this.signalLossSpeed = allowableSpeedALSN;
                            } else {
                                allowableSpeedALSN = speedToWhite;
                            }
                            this.blockDataInstance.getLocalData().setVk(false);
                            break;
                        }
                        case RED_YELLOW: 
                        case RED: {
                            this.blockDataInstance.getLocalData().setVk(false);
                        }
                    }
                    if (!this.blockDataInstance.getLocalData().isK799() && !this.blockDataInstance.getLocalData().isK809() && this.blockDataInstance.getLocalData().getBlockMode() == BLOCK_MODE.TRAIN) break;
                    if (currentActualSpeed > (float)speedToWhite) {
                        this.whiteSpeedReductionDistance = (int)(currentActualSpeed + 5.0f - (float)speedToWhite) * 50;
                        this.signalLossDistance = this.distanceTravelled;
                        this.signalLossSpeed = allowableSpeedALSN;
                    } else {
                        allowableSpeedALSN = speedToWhite;
                    }
                    this.blockDataInstance.getLocalData().setVk(false);
                }
            }
            this.blockDataInstance.getLocalData().setOtpr(false);
            this.blockDataInstance.getLocalData().setK20(false);
            this.blockDataInstance.getLocalData().setPull(false);
            this.blockDataInstance.getLocalData().setPullCount(0);
            this.blockDataInstance.getLocalData().setDistanceToAimingPoint(0);
        } else {
            switch (currentTrafficLight) {
                case GREEN: {
                    break;
                }
                case YELLOW: {
                    if (!isEkEquipped || (newAllowableSpeed = this.getAllowableSpeedFromDistanceToCertainLimit(distanceToTrafficLight, Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getSpeed_to_yellow()))) >= speedToGreen) break;
                    allowableSpeedALSN = newAllowableSpeed;
                    break;
                }
                case RED_YELLOW: {
                    newAllowableSpeed = this.getAllowableSpeedFromDistanceToCertainLimit(distanceToTrafficLight, isEkEquipped ? 0 : 20);
                    if (newAllowableSpeed >= speedToYellow) break;
                    allowableSpeedALSN = newAllowableSpeed;
                    break;
                }
                case WHITE: {
                    if (this.whiteSpeedReductionDistance > 0) {
                        int deltaDistance = (int)(this.distanceTravelled - this.signalLossDistance);
                        if (deltaDistance < 50) break;
                        this.whiteSpeedReductionDistance -= 50;
                        this.signalLossDistance += 50.0f;
                        if (this.signalLossSpeed - 1 < speedToWhite) {
                            this.whiteSpeedReductionDistance = 0;
                            this.signalLossDistance = 0.0f;
                            this.signalLossSpeed = 0;
                            allowableSpeedALSN = speedToWhite;
                            break;
                        }
                        --this.signalLossSpeed;
                        allowableSpeedALSN = this.signalLossSpeed;
                        break;
                    }
                    allowableSpeedALSN = speedToWhite;
                }
            }
        }
        if (this.blockDataInstance.getLocalData().isVk()) {
            allowableSpeedALSN = 20;
        }
        if (this.blockDataInstance.getLocalData().isOtpr()) {
            if (this.blockDataInstance.getLocalData().getDistanceTraveledAfterStopping() > 1.0 && TimerController.getInstance().isThereButtonOffTimer()) {
                TimerController.getInstance().disableButtonOffTimer();
            }
            if (this.blockDataInstance.getLocalData().getDistanceTraveledAfterStopping() < 600.0) {
                this.blockDataInstance.getLocalData().setDistanceToAimingPoint(this.blockDataInstance.getLocalData().getDistanceToAimingPoint() - (int)this.deltaDistance);
                allowableSpeedALSN = 50;
                newAllowableSpeed = this.getAllowableSpeedFromDistance(this.blockDataInstance.getLocalData().getDistanceToAimingPoint(), -3);
                if (newAllowableSpeed < allowableSpeedALSN) {
                    allowableSpeedALSN = newAllowableSpeed;
                }
            } else {
                this.blockDataInstance.getLocalData().setOtpr(false);
            }
        }
        if (this.blockDataInstance.getLocalData().isK20()) {
            if (this.blockDataInstance.getLocalData().getDistanceTraveledAfterStopping() > 1.0 && TimerController.getInstance().isThereButtonOffTimer()) {
                TimerController.getInstance().disableButtonOffTimer();
            }
            if (this.blockDataInstance.getLocalData().getDistanceTraveledAfterStopping() < 300.0) {
                this.blockDataInstance.getLocalData().setDistanceToAimingPoint(this.blockDataInstance.getLocalData().getDistanceToAimingPoint() - (int)this.deltaDistance);
                allowableSpeedALSN = 22;
                newAllowableSpeed = this.getAllowableSpeedFromDistance(this.blockDataInstance.getLocalData().getDistanceToAimingPoint(), -3);
                if (newAllowableSpeed < allowableSpeedALSN) {
                    allowableSpeedALSN = newAllowableSpeed;
                }
            } else {
                this.blockDataInstance.getLocalData().setK20(false);
            }
        }
        if (this.blockDataInstance.getLocalData().isPull()) {
            if (this.blockDataInstance.getLocalData().getDistanceTraveledAfterStopping() > 1.0 && TimerController.getInstance().isThereButtonOffTimer()) {
                TimerController.getInstance().disableButtonOffTimer();
            }
            if (this.blockDataInstance.getLocalData().getDistanceTraveledAfterStopping() < 300.0 && this.blockDataInstance.getLocalData().getPullCount() == 1 || this.blockDataInstance.getLocalData().getDistanceTraveledAfterStopping() < 50.0 && this.blockDataInstance.getLocalData().getPullCount() > 1) {
                this.blockDataInstance.getLocalData().setDistanceToAimingPoint(this.blockDataInstance.getLocalData().getDistanceToAimingPoint() - (int)this.deltaDistance);
                allowableSpeedALSN = 17;
                newAllowableSpeed = this.getAllowableSpeedFromDistance(this.blockDataInstance.getLocalData().getDistanceToAimingPoint(), -3);
                if (newAllowableSpeed < allowableSpeedALSN) {
                    allowableSpeedALSN = newAllowableSpeed;
                }
            } else {
                this.blockDataInstance.getLocalData().setPull(false);
            }
        }
        return new PairOfSpeeds(targetSpeedALSN, allowableSpeedALSN);
    }

    private PairOfSpeeds dataProcessingEK(int targetSpeedByEK, int allowableSpeedByEK, int distanceToNextSectionEK) {
        int newAllowableSpeed;
        int allowableSpeedEK = allowableSpeedByEK;
        if (allowableSpeedByEK > targetSpeedByEK && (newAllowableSpeed = this.getAllowableSpeedFromDistanceToCertainLimit(distanceToNextSectionEK, targetSpeedByEK)) < allowableSpeedByEK) {
            allowableSpeedEK = newAllowableSpeed;
        }
        return new PairOfSpeeds(targetSpeedByEK, allowableSpeedEK);
    }

    private PairOfSpeeds dataProcessingSaut(TRAFFIC_SIGNALS previousTrafficLight, TRAFFIC_SIGNALS currentTrafficLight, int distanceToTrafficLight, int speedToGreen, int speedToYellow) {
        int targetSpeedSaut = 1000;
        int allowableSpeedSaut = 1000;
        if (previousTrafficLight != currentTrafficLight) {
            SoundController.getInstance().playTrafficLignt(currentTrafficLight.ordinal());
            switch (currentTrafficLight) {
                case GREEN: {
                    targetSpeedSaut = speedToGreen;
                    allowableSpeedSaut = speedToGreen;
                    break;
                }
                case YELLOW: {
                    targetSpeedSaut = speedToYellow;
                    allowableSpeedSaut = speedToGreen;
                    break;
                }
                case RED_YELLOW: {
                    targetSpeedSaut = 0;
                    allowableSpeedSaut = speedToYellow;
                }
            }
        } else {
            switch (currentTrafficLight) {
                case GREEN: {
                    break;
                }
                case YELLOW: {
                    int newAllowableSpeed = this.getAllowableSpeedFromDistanceToCertainLimit(distanceToTrafficLight, Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getSpeed_to_yellow()));
                    if (newAllowableSpeed >= speedToGreen) break;
                    allowableSpeedSaut = newAllowableSpeed;
                    break;
                }
                case RED_YELLOW: {
                    if (this.blockDataInstance.getLocalData().isOtpr() || this.blockDataInstance.getLocalData().isK20() || this.blockDataInstance.getLocalData().isPull()) break;
                    int newAllowableSpeed = this.getAllowableSpeedFromDistance(distanceToTrafficLight, 70);
                    if (distanceToTrafficLight - 70 >= 0) {
                        this.blockDataInstance.getLocalData().setDistanceToAimingPoint(distanceToTrafficLight - 70);
                    }
                    if (newAllowableSpeed >= speedToYellow) break;
                    allowableSpeedSaut = newAllowableSpeed;
                }
            }
        }
        return new PairOfSpeeds(targetSpeedSaut, allowableSpeedSaut);
    }

    private int getAllowableSpeedFromDistance(int distanceToTarget, int stopForMeters) {
        short[] distance = this.blockDataInstance.getDistanceToTheEndBlockSection();
        for (int speed = 0; speed < distance.length; ++speed) {
            if (distanceToTarget >= distance[speed] + stopForMeters) continue;
            return Math.max(speed - 1, 0);
        }
        return 1000;
    }

    private int getAllowableSpeedFromDistanceToCertainLimit(int distanceToTarget, int speedLimit) {
        short[] distance = this.blockDataInstance.getDistanceToTheEndBlockSection();
        short distanceCorrection = 0;
        if (speedLimit < distance.length) {
            distanceCorrection = distance[speedLimit];
        }
        for (int speed = 0; speed < distance.length; ++speed) {
            if (distanceToTarget + distanceCorrection >= distance[speed]) continue;
            if (speed - 1 < 0) {
                return speedLimit;
            }
            return speed - 1;
        }
        return 1000;
    }

    private void checkingOverSpeed(TRAFFIC_SIGNALS currentTrafficLight, float deltaSpeed, boolean isMovement, boolean isSautEquipped) {
        if (isMovement && this.blockDataInstance.getLocalData().getBlockMode() != BLOCK_MODE.RDT) {
            switch (currentTrafficLight) {
                case GREEN: 
                case YELLOW: {
                    if (!isSautEquipped) {
                        if (deltaSpeed <= 4.0f) {
                            if (!TimerController.getInstance().isThereBlinkActualSpeed()) {
                                TimerController.getInstance().enableBlinkActualSpeed();
                            }
                        } else if (TimerController.getInstance().isThereBlinkActualSpeed()) {
                            TimerController.getInstance().disableBlinkActualSpeed();
                        }
                        if (deltaSpeed <= 3.0f && !this.blockDataInstance.getLocalData().isBrakingValveOpenedFromKOH() && !this.blockDataInstance.getLocalData().isBrakingValveOpenedFromEPK()) {
                            SoundController.getInstance().playCyclicBeep();
                        } else {
                            SoundController.getInstance().stopCyclicBeep();
                        }
                        if (!(deltaSpeed <= -5.0f) || this.isOverSpeed) break;
                        this.isOverSpeed = true;
                        if (TimerController.getInstance().isThereBlinkActualSpeed()) {
                            TimerController.getInstance().disableBlinkActualSpeed();
                        }
                        SoundController.getInstance().stopCyclicBeep();
                        logger.debug("voltage remove, over speed {} no saut on yellow checkingOverSpeed()", (Object)Float.valueOf(deltaSpeed));
                        TimerController.getInstance().disableEpkHoldingCoil();
                        break;
                    }
                    if (TimerController.getInstance().isThereBlinkActualSpeed()) {
                        TimerController.getInstance().disableBlinkActualSpeed();
                    }
                    if (!this.blockDataInstance.getLocalData().isBrakingValveOpenedFromKOH() && !this.blockDataInstance.getLocalData().isBrakingValveOpenedFromEPK()) {
                        SoundController.getInstance().stopCyclicBeep();
                    }
                    this.checkingOverSpeedSaut(deltaSpeed);
                    break;
                }
                case RED_YELLOW: {
                    if (!isSautEquipped) {
                        if (!(deltaSpeed <= -1.0f) || this.isOverSpeed) break;
                        this.isOverSpeed = true;
                        logger.debug("voltage remove, over speed \u043d\u0430 {} on red-yellow checkingOverSpeed()", (Object)Float.valueOf(deltaSpeed));
                        TimerController.getInstance().disableEpkHoldingCoil();
                        break;
                    }
                    if (TimerController.getInstance().isThereBlinkActualSpeed()) {
                        TimerController.getInstance().disableBlinkActualSpeed();
                    }
                    if (!this.blockDataInstance.getLocalData().isBrakingValveOpenedFromKOH() && !this.blockDataInstance.getLocalData().isBrakingValveOpenedFromEPK()) {
                        SoundController.getInstance().stopCyclicBeep();
                    }
                    this.checkingOverSpeedSaut(deltaSpeed);
                    break;
                }
                case RED: 
                case WHITE: {
                    if (!(deltaSpeed <= -1.0f) || this.isOverSpeed) break;
                    this.isOverSpeed = true;
                    logger.debug("voltage remove, over speed {} on red checkingOverSpeed()", (Object)Float.valueOf(deltaSpeed));
                    TimerController.getInstance().disableEpkHoldingCoil();
                    break;
                }
            }
        } else {
            this.blockDataInstance.getLocalData().setDisassembleSoundTheTraction(false);
            this.blockDataInstance.getLocalData().setDisassembleTheTraction(false);
            this.blockDataInstance.getLocalData().setUseServiceBrakingOfSaut(false);
        }
    }

    private void checkingOverSpeedSaut(float deltaSpeed) {
        if (deltaSpeed <= 2.0f) {
            if (!this.blockDataInstance.getLocalData().isDisassembleSoundTheTraction()) {
                this.blockDataInstance.getLocalData().setDisassembleSoundTheTraction(true);
                SoundController.getInstance().play("\u041e\u0442\u043a\u043b\u044e\u0447\u0438 \u0442\u044f\u0433\u0443");
            }
        } else if (this.blockDataInstance.getLocalData().isDisassembleSoundTheTraction()) {
            this.blockDataInstance.getLocalData().setDisassembleSoundTheTraction(false);
        }
        if (deltaSpeed <= 1.0f) {
            if (!this.blockDataInstance.getLocalData().isDisassembleTheTraction()) {
                this.blockDataInstance.getLocalData().setDisassembleTheTraction(true);
                logger.debug("DisassembleTheTraction checkingOverSpeedSaut()");
            }
        } else if (this.blockDataInstance.getLocalData().isDisassembleTheTraction()) {
            this.blockDataInstance.getLocalData().setDisassembleTheTraction(false);
            logger.debug("DisassembleTheTraction off checkingOverSpeedSaut()");
        }
        if (deltaSpeed <= 0.0f) {
            if (!(this.blockDataInstance.getLocalData().isUseServiceBrakingOfSaut() || this.blockDataInstance.getLocalData().isBrakingValveOpenedFromKOH() || this.blockDataInstance.getLocalData().isBrakingValveOpenedFromEPK())) {
                this.blockDataInstance.getLocalData().setUseServiceBrakingOfSaut(true);
                logger.debug("UseServiceBrakingOfSaut checkingOverSpeedSaut()");
            }
        } else if (deltaSpeed >= 2.0f && this.blockDataInstance.getLocalData().isUseServiceBrakingOfSaut()) {
            this.blockDataInstance.getLocalData().setUseServiceBrakingOfSaut(false);
            logger.debug("UseServiceBrakingOfSaut off checkingOverSpeedSaut()");
        }
        if (deltaSpeed <= -6.0f && !this.isOverSpeed) {
            this.isOverSpeed = true;
            logger.debug("voltage remove, over speed {} with saut checkingOverSpeedSaut()", (Object)Float.valueOf(deltaSpeed));
            TimerController.getInstance().disableEpkHoldingCoil();
        }
    }

    private void periodicVigilanceCheck(TRAFFIC_SIGNALS currentTrafficLight, boolean isEkEquipped, boolean isSautEquipped, boolean isTskbmEquipped, boolean isMovement, float actualSpeed, int targetSpeed) {
        if (!isTskbmEquipped && isMovement && this.blockDataInstance.getLocalData().getBlockMode() != BLOCK_MODE.RDT) {
            if (this.blockDataInstance.getLocalData().getBlockMode() == BLOCK_MODE.TRAIN) {
                if (!isSautEquipped) {
                    if (currentTrafficLight == TRAFFIC_SIGNALS.RED_YELLOW || currentTrafficLight == TRAFFIC_SIGNALS.RED) {
                        TimerController.getInstance().enablePeriodicVigilanceCheck(32);
                    } else if (currentTrafficLight == TRAFFIC_SIGNALS.WHITE) {
                        TimerController.getInstance().enablePeriodicVigilanceCheck(60);
                    } else if (currentTrafficLight == TRAFFIC_SIGNALS.YELLOW) {
                        if (actualSpeed > (float)targetSpeed) {
                            TimerController.getInstance().enablePeriodicVigilanceCheck(32);
                        } else {
                            TimerController.getInstance().enablePeriodicVigilanceCheck(63);
                        }
                    } else if (actualSpeed > (float)targetSpeed) {
                        TimerController.getInstance().enablePeriodicVigilanceCheck(32);
                    } else if (TimerController.getInstance().isThereIsPeriodicVigilanceCheck()) {
                        TimerController.getInstance().disablePeriodicVigilanceCheck();
                    }
                } else if (currentTrafficLight == TRAFFIC_SIGNALS.WHITE && actualSpeed > (float)targetSpeed) {
                    TimerController.getInstance().enablePeriodicVigilanceCheck(32);
                } else if (TimerController.getInstance().isThereIsPeriodicVigilanceCheck()) {
                    TimerController.getInstance().disablePeriodicVigilanceCheck();
                }
            } else if (this.blockDataInstance.getLocalData().getBlockMode() == BLOCK_MODE.MANEUVERING) {
                TimerController.getInstance().enablePeriodicVigilanceCheck(60);
            }
        } else if (TimerController.getInstance().isThereIsPeriodicVigilanceCheck()) {
            TimerController.getInstance().disablePeriodicVigilanceCheck();
        }
    }

    private void singleVigilanceCheck(TRAFFIC_SIGNALS previousTrafficLight, TRAFFIC_SIGNALS currentTrafficLight, int previousTargetSpeed, int currentTargetSpeed, boolean currentTraction, boolean previousTraction, boolean isEkEquipped, boolean isSautEquipped, boolean isTskbmEquipped, boolean isMovement) {
        if (isMovement && this.blockDataInstance.getLocalData().getBlockMode() != BLOCK_MODE.RDT) {
            if (previousTargetSpeed > currentTargetSpeed) {
                if (isSautEquipped) {
                    if (currentTrafficLight == TRAFFIC_SIGNALS.RED_YELLOW && !this.isSingleVigilanceCheck) {
                        this.isSingleVigilanceCheck = true;
                        logger.debug("voltage remove, single vigilance check with saut on target speed reduction singleVigilanceCheck()");
                        TimerController.getInstance().disableEpkHoldingCoil();
                    }
                } else if (!this.isSingleVigilanceCheck) {
                    this.isSingleVigilanceCheck = true;
                    logger.debug("voltage remove, single vigilance check without saut on target speed reduction singleVigilanceCheck()");
                    TimerController.getInstance().disableEpkHoldingCoil();
                }
            }
            if (previousTrafficLight != currentTrafficLight) {
                switch (currentTrafficLight) {
                    case RED_YELLOW: 
                    case RED: 
                    case WHITE: {
                        if (this.isSingleVigilanceCheck) break;
                        this.isSingleVigilanceCheck = true;
                        logger.debug("voltage remove, single vigilance check, switch to prohibition signal singleVigilanceCheck()");
                        TimerController.getInstance().disableEpkHoldingCoil();
                    }
                }
            }
        }
        if (!previousTraction && currentTraction && this.blockDataInstance.getLocalData().getBlockMode() == BLOCK_MODE.TRAIN) {
            switch (currentTrafficLight) {
                case RED_YELLOW: 
                case RED: 
                case WHITE: {
                    if (this.isSingleVigilanceCheck) break;
                    this.isSingleVigilanceCheck = true;
                    logger.debug("voltage remove, single vigilance check on beginning of the movement R RY W singleVigilanceCheck()");
                    TimerController.getInstance().disableEpkHoldingCoil();
                }
            }
        }
    }

    private void blockOffEPK() {
        this.whiteSpeedReductionDistance = 0;
        this.signalLossDistance = 0.0f;
        this.signalLossSpeed = 0;
        this.distanceTravelled = 0.0f;
        this.isRollingDown = false;
        this.isOverSpeed = false;
        this.isSingleVigilanceCheck = false;
        this.rbsHoldCheck = 0;
        this.rbRbpRbsHoldCheck = 0;
    }

    private void updateDisplayData() {
        DisplayData displayDataInstance = this.blockDataInstance.getDisplayData();
        displayDataInstance.setPower(this.blockDataInstance.getCurNetworkData().getBlockPower() == 1);
        String FLOAT_FORMAT = "%1.2f";
        if (this.blockDataInstance.getCurNetworkData().getEpkKey() == 1) {
            displayDataInstance.setAllowableSpeed(this.blockDataInstance.getLocalData().getAllowableSpeed());
            displayDataInstance.setTargetSpeed(this.blockDataInstance.getLocalData().getTargetSpeed());
            if (this.blockDataInstance.getCurNetworkData().getTargetCode() == 0) {
                displayDataInstance.setTargetType(0);
                displayDataInstance.setDistanceToTarget("");
                displayDataInstance.setTargetName("");
            } else if (this.blockDataInstance.getCurNetworkData().getEkEquipped() == 1) {
                displayDataInstance.setTargetType(this.blockDataInstance.getCurNetworkData().getTargetCode());
                if (this.blockDataInstance.getCurNetworkData().getTargetCode() == 1) {
                    displayDataInstance.setDistanceToTarget(String.valueOf(this.blockDataInstance.getCurNetworkData().getDistanceToTrafficLight()));
                    displayDataInstance.setTargetName(this.blockDataInstance.getCurNetworkData().getTrafficLightName());
                } else {
                    displayDataInstance.setDistanceToTarget(String.valueOf(this.blockDataInstance.getCurNetworkData().getDistanceToTarget()));
                    displayDataInstance.setTargetName(this.blockDataInstance.getCurNetworkData().getTargetName());
                }
            } else {
                displayDataInstance.setTargetType(1);
                displayDataInstance.setDistanceToTarget(String.valueOf(this.blockDataInstance.getCurNetworkData().getDistanceToTrafficLight()));
                displayDataInstance.setTargetName(this.blockDataInstance.getCurNetworkData().getTrafficLightName());
            }
            if (this.blockDataInstance.getCurNetworkData().getSautEquipped() == 1) {
                displayDataInstance.setDistanceToSaut(String.valueOf(this.blockDataInstance.getLocalData().getDistanceToAimingPoint()));
                displayDataInstance.setBrakingCoefficient(String.format(Locale.ROOT, FLOAT_FORMAT, Float.valueOf(this.blockDataInstance.getCurNetworkData().getBrakingCoefficient())));
            } else {
                displayDataInstance.setDistanceToSaut("");
                displayDataInstance.setBrakingCoefficient("");
            }
            displayDataInstance.setStationName(this.blockDataInstance.getCurNetworkData().getStationName());
            displayDataInstance.setRoadBias(String.format(Locale.ROOT, FLOAT_FORMAT, Float.valueOf(this.blockDataInstance.getCurNetworkData().getRoadBias())));
            displayDataInstance.setNeutralInsert(this.blockDataInstance.getCurNetworkData().getNeutralInsert() == 1);
            displayDataInstance.setTokorazdel(this.blockDataInstance.getCurNetworkData().getTokorazdel() == 1);
            displayDataInstance.setDistanceTokorazdelOrNeutralInsert(String.valueOf(this.blockDataInstance.getCurNetworkData().getDistanceTokorazdelOrNeutralInsert()));
            displayDataInstance.setVisiblyReleaseBan(this.blockDataInstance.getLocalData().isUseServiceBrakingOfSaut());
            displayDataInstance.setVisiblyServiceBraking(this.blockDataInstance.getCurNetworkData().getServiceBraking() == 1);
        } else {
            displayDataInstance.setTargetType(0);
            displayDataInstance.setDistanceToTarget("");
            displayDataInstance.setTargetName("");
            displayDataInstance.setAllowableSpeed(0);
            displayDataInstance.setTargetSpeed(0);
            displayDataInstance.setDistanceToSaut("");
            displayDataInstance.setBrakingCoefficient("");
            displayDataInstance.setStationName("");
            displayDataInstance.setRoadBias("");
            displayDataInstance.setNeutralInsert(false);
            displayDataInstance.setTokorazdel(false);
            displayDataInstance.setDistanceTokorazdelOrNeutralInsert("");
            displayDataInstance.setVisiblyReleaseBan(false);
            displayDataInstance.setVisiblyServiceBraking(false);
        }
        if (this.blockDataInstance.getCurNetworkData().getWorldTime() != this.blockDataInstance.getPrevNetworkData().getWorldTime()) {
            Date date = new Date(this.blockDataInstance.getCurNetworkData().getWorldTime());
            String timeS = this.timeFormat.format(date.getTime());
            displayDataInstance.setWorldTime(timeS);
        }
        if (this.blockDataInstance.getCurNetworkData().getActualSpeed() != this.blockDataInstance.getPrevNetworkData().getActualSpeed()) {
            displayDataInstance.setActualSpeed((int)this.blockDataInstance.getCurNetworkData().getActualSpeed());
        }
        float displayPressureOnTmLabel = this.blockDataInstance.getCurNetworkData().getTmValue();
        if (this.blockDataInstance.getStartConfig().getBilModel() != BIL_MODEL.BIL_UT && this.blockDataInstance.getStartConfig().getBilModel() != BIL_MODEL.BIL_V) {
            if (this.blockDataInstance.getLocalData().isK80() && this.blockDataInstance.getCurNetworkData().getTmValue() != this.blockDataInstance.getPrevNetworkData().getTmValue()) {
                displayDataInstance.setValueStr(String.format(Locale.ROOT, FLOAT_FORMAT, Float.valueOf(this.blockDataInstance.getCurNetworkData().getTmValue())));
            }
            if (this.blockDataInstance.getLocalData().isK81() && this.blockDataInstance.getCurNetworkData().getTcValue() != this.blockDataInstance.getPrevNetworkData().getTcValue()) {
                displayDataInstance.setValueStr(String.format(Locale.ROOT, FLOAT_FORMAT, Float.valueOf(this.blockDataInstance.getCurNetworkData().getTcValue())));
            }
            if (this.blockDataInstance.getLocalData().isK82() && this.blockDataInstance.getCurNetworkData().getUr2Value() != this.blockDataInstance.getPrevNetworkData().getUr2Value()) {
                displayDataInstance.setValueStr(String.format(Locale.ROOT, FLOAT_FORMAT, Float.valueOf(this.blockDataInstance.getCurNetworkData().getUr2Value())));
            }
            if (this.blockDataInstance.getLocalData().isK83() && this.blockDataInstance.getCurNetworkData().getUr1Value() != this.blockDataInstance.getPrevNetworkData().getUr1Value()) {
                displayDataInstance.setValueStr(String.format(Locale.ROOT, FLOAT_FORMAT, Float.valueOf(this.blockDataInstance.getCurNetworkData().getUr1Value())));
            }
        } else {
            if (this.blockDataInstance.getLocalData().isK80()) {
                displayPressureOnTmLabel = this.blockDataInstance.getCurNetworkData().getTmValue();
            }
            if (this.blockDataInstance.getLocalData().isK81()) {
                displayPressureOnTmLabel = this.blockDataInstance.getCurNetworkData().getTcValue();
            }
            if (this.blockDataInstance.getLocalData().isK82()) {
                displayPressureOnTmLabel = this.blockDataInstance.getCurNetworkData().getUr2Value();
            }
            if (this.blockDataInstance.getLocalData().isK83()) {
                displayPressureOnTmLabel = this.blockDataInstance.getCurNetworkData().getUr1Value();
            }
        }
        if (this.blockDataInstance.getLocalData().isK4()) {
            displayDataInstance.setValueStr(displayDataInstance.getWorldTime());
        }
        if (this.blockDataInstance.getLocalData().isK123()) {
            displayDataInstance.setDimensionOfTheProgressBar("\u043a\u0433\u0441/\u0441\u043c\u00b2");
            displayDataInstance.setPressureUR(String.format(Locale.ROOT, FLOAT_FORMAT, (double)this.blockDataInstance.getCurNetworkData().getUr1Value() * 10.19716212978));
            displayDataInstance.setPressureTC(String.format(Locale.ROOT, FLOAT_FORMAT, (double)this.blockDataInstance.getCurNetworkData().getTcValue() * 10.19716212978));
            displayDataInstance.setPressureTM(String.format(Locale.ROOT, FLOAT_FORMAT, (double)displayPressureOnTmLabel * 10.19716212978));
        } else {
            displayDataInstance.setDimensionOfTheProgressBar("\u041c\u041f\u0430");
            displayDataInstance.setPressureUR(String.format(Locale.ROOT, FLOAT_FORMAT, Float.valueOf(this.blockDataInstance.getCurNetworkData().getUr1Value())));
            displayDataInstance.setPressureTC(String.format(Locale.ROOT, FLOAT_FORMAT, Float.valueOf(this.blockDataInstance.getCurNetworkData().getTcValue())));
            displayDataInstance.setPressureTM(String.format(Locale.ROOT, FLOAT_FORMAT, Float.valueOf(displayPressureOnTmLabel)));
        }
        displayDataInstance.setProgressUR(this.blockDataInstance.getCurNetworkData().getUr1Value());
        displayDataInstance.setProgressTC(this.blockDataInstance.getCurNetworkData().getTcValue());
        displayDataInstance.setProgressTM(this.blockDataInstance.getCurNetworkData().getTmValue());
        if (this.blockDataInstance.getCurNetworkData().getReverse() != this.blockDataInstance.getPrevNetworkData().getReverse()) {
            displayDataInstance.setReverse(this.blockDataInstance.getCurNetworkData().getReverse());
        }
        displayDataInstance.setCoordinate(this.getCoordinateString());
        if (this.blockDataInstance.getCurNetworkData().getAcceleration() < 0.0f && this.blockDataInstance.getCurNetworkData().getAcceleration() > -0.01f) {
            displayDataInstance.setAcceleration("0.00");
        } else {
            displayDataInstance.setAcceleration(String.format(Locale.ROOT, FLOAT_FORMAT, Float.valueOf(this.blockDataInstance.getCurNetworkData().getAcceleration())));
        }
        displayDataInstance.setCassetteEquipped(this.blockDataInstance.getCurNetworkData().getCassetteEquipped() == 1);
        if (this.blockDataInstance.getCurNetworkData().getEkEquipped() == 1) {
            displayDataInstance.setChannel("\u042d\u041a");
        } else {
            displayDataInstance.setChannel(this.blockDataInstance.getDisplayData().getFrequenciesAlsn());
        }
        displayDataInstance.setVisiblyTskbmPower(this.blockDataInstance.getCurNetworkData().getTskbmEquipped() == 1);
        displayDataInstance.setVideoCoord(this.blockDataInstance.getCurNetworkData().getVideoCoord());
        displayDataInstance.setVisiblyChart(this.blockDataInstance.getCurNetworkData().getEkEquipped() == 1);
        displayDataInstance.setTrafficSignal(this.blockDataInstance.getLocalData().getTrafficSignal());
    }

    private String getCoordinateString() {
        switch (BlockData.getInstance().getStartConfig().getBilModel()) {
            case BIL_UT: 
            case BIL_V: {
                if (this.blockDataInstance.getCurNetworkData().getEkEquipped() == 1) {
                    return String.format("%04d.%03d", this.blockDataInstance.getCurNetworkData().getCoordinateGps() / 1000, this.blockDataInstance.getCurNetworkData().getCoordinateGps() % 1000);
                }
                try {
                    return String.format("%04d.%03d", Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getCurrent_coordinate()) / 1000, Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getCurrent_coordinate()) % 1000);
                }
                catch (NumberFormatException e) {
                    logger.error(e);
                    break;
                }
            }
            default: {
                if (this.blockDataInstance.getCurNetworkData().getEkEquipped() == 1) {
                    return String.format("%04d\u043a\u043c %02d\u043f %03d\u043c", this.blockDataInstance.getCurNetworkData().getCoordinateGps() / 1000, this.blockDataInstance.getCurNetworkData().getPicket(), this.blockDataInstance.getCurNetworkData().getCoordinateGps() % 1000);
                }
                try {
                    return String.format("%04d\u043a\u043c %02d\u043f %03d\u043c", Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getCurrent_coordinate()) / 1000, Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getCurrent_coordinate()) % 1000 / 100 + 1, Integer.parseInt(this.blockDataInstance.getCommandSettingsData().getCurrent_coordinate()) % 1000);
                }
                catch (NumberFormatException e) {
                    logger.error(e);
                }
            }
        }
        return "0000.000";
    }

    class PairOfSpeeds {
        private final int targetSpeed;
        private final int allowableSpeed;

        PairOfSpeeds() {
            this.targetSpeed = 1000;
            this.allowableSpeed = 1000;
        }

        PairOfSpeeds(int targetSpeed, int allowableSpeed) {
            this.targetSpeed = targetSpeed;
            this.allowableSpeed = allowableSpeed;
        }
    }
}

