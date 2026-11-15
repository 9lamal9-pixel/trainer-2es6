/*
 * Decompiled with CFR 0.152.
 */
package base.timers;

import base.data.BlockData;
import base.logic.KeyboardAndCommandController;
import base.sound.SoundController;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimerController {
    private static final Logger logger = LogManager.getLogger(TimerController.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static TimerController instance;
    private ScheduledFuture<?> scheduledEpkHoldingCoil;
    private boolean epkHoldingCoil = false;
    private boolean rollingDown30sec = false;
    private ScheduledFuture<?> scheduledRollingDown30sec;
    private final RemoveElectricalVoltageFromEpk removeElectricalVoltageFromEpkRunnable = new RemoveElectricalVoltageFromEpk();
    private boolean didNotAccelerateTo2KmH = false;
    private boolean scheduledBeginningOfTheMovementDistanceWork = false;
    private boolean scheduledBeginningOfTheMovementSpeedWork = false;
    private final BeginningOfTheMovement beginningOfTheMovementSpeedRunnable = new BeginningOfTheMovement(true);
    private final BeginningOfTheMovement beginningOfTheMovementDistanceRunnable = new BeginningOfTheMovement(false);
    private ScheduledFuture<?> scheduledBeginningOfTheMovementSpeed;
    private ScheduledFuture<?> scheduledBeginningOfTheMovementDistance;
    private ScheduledFuture<?> scheduledAttention;
    private boolean pssPassLamp = false;
    private boolean pssPassLampKey = false;
    private boolean thereIsAttention = false;
    private ScheduledFuture<?> scheduledUiBlinkAttention;
    private final BlinkAttention blinkAttention = new BlinkAttention();
    private boolean thereIsUiBlinkAttention = false;
    private ScheduledFuture<?> scheduledPeriodicVigilanceCheck;
    private final PeriodicVigilanceCheck periodicVigilanceCheckRunnable = new PeriodicVigilanceCheck();
    private boolean thereIsPeriodicVigilanceCheck = false;
    private ScheduledFuture<?> scheduledCancelRollingControl;
    private final CancelRollingControl cancelRollingControlRunnable = new CancelRollingControl();
    private boolean thereCancelRollingControl = false;
    private ScheduledFuture<?> scheduledResetInputWhenIdle;
    private boolean thereIsResetInputWhenIdle = false;
    private ScheduledFuture<?> scheduledBlinkActualSpeed;
    private final BlinkActualSpeed blinkActualSpeed = new BlinkActualSpeed();
    private boolean thereBlinkActualSpeed = false;
    private ScheduledFuture<?> scheduledButtonOffTimer;
    private final ButtonOffTimer buttonOffTimer = new ButtonOffTimer();
    private boolean thereButtonOffTimer = false;
    private ScheduledFuture<?> scheduledLackOfNetwork;
    private final LackOfNetwork lackOfNetwork = new LackOfNetwork();
    private boolean thereLackOfNetwork = false;
    private ScheduledFuture<?> scheduledBlinkRDT;
    private final BlinkRDT blinkRDT = new BlinkRDT();
    private boolean thereBlinkRDT = false;
    private ScheduledFuture<?> scheduledButtonRBRBPOffTimer;
    private final ButtonRBRBPOffTimer buttonRBRBPOffTimer = new ButtonRBRBPOffTimer();
    private boolean thereButtonRBRBPOffTimer = false;
    private ScheduledFuture<?> scheduledLongHoldOfVigilanceHandleTimer;
    private final LongHoldOfVigilanceHandleTimer longHoldOfVigilanceHandleTimer = new LongHoldOfVigilanceHandleTimer();
    private boolean thereLongHoldOfVigilanceHandleTimer = false;
    private boolean longHoldOfVigilanceHandle = false;

    public static TimerController getInstance() {
        if (instance == null) {
            instance = new TimerController();
        }
        return instance;
    }

    public void stopService() {
        this.scheduler.shutdown();
    }

    public void powerFailure() {
        logger.debug("device power off");
        if (this.scheduledEpkHoldingCoil != null) {
            this.scheduledEpkHoldingCoil.cancel(true);
            logger.debug("scheduledEpkHoldingCoil stop");
        }
        if (this.scheduledRollingDown30sec != null) {
            this.scheduledRollingDown30sec.cancel(true);
            logger.debug("scheduledRollingDown30sec stop");
        }
        if (this.scheduledBeginningOfTheMovementSpeed != null) {
            this.scheduledBeginningOfTheMovementSpeed.cancel(true);
            logger.debug("scheduledBeginningOfTheMovementSpeed stop");
        }
        if (this.scheduledBeginningOfTheMovementDistance != null) {
            this.scheduledBeginningOfTheMovementDistance.cancel(true);
            logger.debug("scheduledBeginningOfTheMovementDistance stop");
        }
        if (this.scheduledAttention != null) {
            this.scheduledAttention.cancel(true);
            logger.debug("scheduledAttention stop");
        }
        if (this.scheduledUiBlinkAttention != null) {
            this.scheduledUiBlinkAttention.cancel(true);
            logger.debug("scheduledUiBlinkAttention stop");
        }
        if (this.scheduledPeriodicVigilanceCheck != null) {
            this.scheduledPeriodicVigilanceCheck.cancel(true);
            logger.debug("scheduledPeriodicVigilanceCheck stop");
        }
        if (this.scheduledCancelRollingControl != null) {
            this.scheduledCancelRollingControl.cancel(true);
            logger.debug("scheduledCancelRollingControl stop");
        }
        if (this.scheduledResetInputWhenIdle != null) {
            this.scheduledResetInputWhenIdle.cancel(true);
            logger.debug("scheduledResetInputWhenIdle stop");
        }
        if (this.scheduledBlinkActualSpeed != null) {
            this.scheduledBlinkActualSpeed.cancel(true);
            logger.debug("scheduledBlinkActualSpeed stop");
        }
        if (this.scheduledButtonOffTimer != null) {
            this.scheduledButtonOffTimer.cancel(true);
            logger.debug("scheduledButtonOffTimer stop");
        }
        if (this.scheduledBlinkRDT != null) {
            this.scheduledBlinkRDT.cancel(true);
            logger.debug("scheduledBlinkRDT stop");
        }
        if (this.scheduledButtonRBRBPOffTimer != null) {
            this.scheduledButtonRBRBPOffTimer.cancel(true);
            logger.debug("scheduledButtonRBRBPOffTimer stop");
        }
        if (this.scheduledLongHoldOfVigilanceHandleTimer != null) {
            this.scheduledLongHoldOfVigilanceHandleTimer.cancel(true);
            logger.debug("scheduledLongHoldOfVigilanceHandleTimer stop");
        }
        this.epkHoldingCoil = false;
        this.rollingDown30sec = false;
        this.didNotAccelerateTo2KmH = false;
        this.scheduledBeginningOfTheMovementDistanceWork = false;
        this.scheduledBeginningOfTheMovementSpeedWork = false;
        this.thereIsAttention = false;
        this.thereIsUiBlinkAttention = false;
        this.thereIsPeriodicVigilanceCheck = false;
        this.thereCancelRollingControl = false;
        this.thereIsResetInputWhenIdle = false;
        this.thereBlinkActualSpeed = false;
        this.thereButtonOffTimer = false;
        this.thereBlinkRDT = false;
        this.thereButtonRBRBPOffTimer = false;
        this.pssPassLampKey = false;
        this.pssPassLamp = false;
        this.thereLongHoldOfVigilanceHandleTimer = false;
        this.longHoldOfVigilanceHandle = false;
        BlockData.getInstance().getDisplayData().setVisiblyAttention(false);
        BlockData.getInstance().getDisplayData().setVisiblyActualSpeed(true);
        BlockData.getInstance().getDisplayData().setTrainMode("\u041f");
        this.setEpkHoldingCoil(false);
    }

    public void disablingEpkKey() {
        logger.debug("disabling Epk Key");
        if (this.scheduledEpkHoldingCoil != null) {
            this.scheduledEpkHoldingCoil.cancel(true);
            logger.debug("scheduledElectricalVoltageOnEpk stop");
        }
        if (this.scheduledBeginningOfTheMovementSpeed != null) {
            this.scheduledBeginningOfTheMovementSpeed.cancel(true);
            logger.debug("scheduledBeginningOfTheMovementSpeed stop");
        }
        if (this.scheduledBeginningOfTheMovementDistance != null) {
            this.scheduledBeginningOfTheMovementDistance.cancel(true);
            logger.debug("scheduledBeginningOfTheMovementDistance stop");
        }
        if (this.scheduledAttention != null) {
            this.scheduledAttention.cancel(true);
            logger.debug("scheduledAttention stop");
        }
        if (this.scheduledUiBlinkAttention != null) {
            this.scheduledUiBlinkAttention.cancel(true);
            logger.debug("scheduledUiBlinkAttention stop");
        }
        if (this.scheduledPeriodicVigilanceCheck != null) {
            this.scheduledPeriodicVigilanceCheck.cancel(true);
            logger.debug("scheduledPeriodicVigilanceCheck stop");
        }
        if (this.scheduledCancelRollingControl != null) {
            this.scheduledCancelRollingControl.cancel(true);
            logger.debug("scheduledCancelRollingControl stop");
        }
        if (this.scheduledResetInputWhenIdle != null) {
            this.scheduledResetInputWhenIdle.cancel(true);
            logger.debug("scheduledResetInputWhenIdle stop");
        }
        if (this.scheduledBlinkActualSpeed != null) {
            this.scheduledBlinkActualSpeed.cancel(true);
            logger.debug("scheduledBlinkActualSpeed stop");
        }
        if (this.scheduledButtonOffTimer != null) {
            this.scheduledButtonOffTimer.cancel(true);
            logger.debug("scheduledButtonOffTimer stop");
        }
        if (this.scheduledBlinkRDT != null) {
            this.scheduledBlinkRDT.cancel(true);
            logger.debug("scheduledBlinkRDT stop");
        }
        if (this.scheduledButtonRBRBPOffTimer != null) {
            this.scheduledButtonRBRBPOffTimer.cancel(true);
            logger.debug("scheduledButtonRBRBPOffTimer stop");
        }
        if (this.scheduledLongHoldOfVigilanceHandleTimer != null) {
            this.scheduledLongHoldOfVigilanceHandleTimer.cancel(true);
            logger.debug("scheduledLongHoldOfVigilanceHandleTimer stop");
        }
        this.epkHoldingCoil = false;
        this.didNotAccelerateTo2KmH = false;
        this.scheduledBeginningOfTheMovementDistanceWork = false;
        this.scheduledBeginningOfTheMovementSpeedWork = false;
        this.thereIsAttention = false;
        this.thereIsUiBlinkAttention = false;
        this.thereIsPeriodicVigilanceCheck = false;
        this.thereCancelRollingControl = false;
        this.thereIsResetInputWhenIdle = false;
        this.thereBlinkActualSpeed = false;
        this.thereButtonOffTimer = false;
        this.thereBlinkRDT = false;
        this.thereButtonRBRBPOffTimer = false;
        this.pssPassLampKey = false;
        this.pssPassLamp = false;
        this.thereLongHoldOfVigilanceHandleTimer = false;
        this.longHoldOfVigilanceHandle = false;
        BlockData.getInstance().getDisplayData().setVisiblyAttention(false);
        BlockData.getInstance().getDisplayData().setVisiblyActualSpeed(true);
        BlockData.getInstance().getDisplayData().setTrainMode("\u041f");
        this.setEpkHoldingCoil(false);
    }

    public void disableEpkHoldingCoil() {
        if (this.epkHoldingCoil) {
            this.epkHoldingCoil = false;
            this.enableUiBlinkAttention();
            logger.debug("disableEpkHoldingCoil voltage removed");
        }
    }

    public void recoverEpkHoldingCoil() {
        this.epkHoldingCoil = true;
        logger.debug("recoverEpkHoldingCoil voltage recover");
        this.disableUiBlinkAttention();
        if (this.scheduledEpkHoldingCoil != null) {
            this.scheduledEpkHoldingCoil.cancel(true);
        }
    }

    public boolean isEpkHoldingCoil() {
        return this.epkHoldingCoil;
    }

    public void setEpkHoldingCoil(boolean epkHoldingCoil) {
        this.epkHoldingCoil = epkHoldingCoil;
    }

    public void startRollingDown30sec() {
        if (!this.rollingDown30sec) {
            this.scheduledRollingDown30sec = this.scheduler.schedule(this.removeElectricalVoltageFromEpkRunnable, 30L, TimeUnit.SECONDS);
            this.rollingDown30sec = true;
            logger.debug("30s on rolling is on startRollingDown30sec()");
        }
    }

    public void stopRollingDown30sec() {
        this.rollingDown30sec = false;
        if (this.scheduledRollingDown30sec != null) {
            this.scheduledRollingDown30sec.cancel(true);
            logger.debug("30s on rolling is off stopRollingDown30sec()");
        }
    }

    public boolean isRollingDown30sec() {
        return this.rollingDown30sec;
    }

    public boolean isScheduledBeginningOfTheMovementDistanceWork() {
        return this.scheduledBeginningOfTheMovementDistanceWork;
    }

    public boolean isScheduledBeginningOfTheMovementSpeedWork() {
        return this.scheduledBeginningOfTheMovementSpeedWork;
    }

    public boolean isDidNotAccelerateTo2KmH() {
        return this.didNotAccelerateTo2KmH;
    }

    public void setDidNotAccelerateTo2KmH(boolean didNotAccelerateTo2KmH) {
        this.didNotAccelerateTo2KmH = didNotAccelerateTo2KmH;
    }

    public void startBeginningOfTheMovement() {
        if (!this.scheduledBeginningOfTheMovementSpeedWork) {
            this.scheduledBeginningOfTheMovementSpeedWork = true;
            this.scheduledBeginningOfTheMovementSpeed = this.scheduler.schedule(this.beginningOfTheMovementSpeedRunnable, this.thereCancelRollingControl ? 120L : 70L, TimeUnit.SECONDS);
            logger.debug("start beginningOfTheMovementSpeedRunnable {}s", (Object)(this.thereCancelRollingControl ? 120 : 70));
        }
        if (!this.scheduledBeginningOfTheMovementDistanceWork) {
            this.scheduledBeginningOfTheMovementDistanceWork = true;
            this.scheduledBeginningOfTheMovementDistance = this.scheduler.schedule(this.beginningOfTheMovementDistanceRunnable, 76L, TimeUnit.SECONDS);
            logger.debug("start beginningOfTheMovementDistanceRunnable {}s", (Object)76);
        }
    }

    public void stopBeginningOfTheMovement() {
        if (this.scheduledBeginningOfTheMovementSpeed != null) {
            this.scheduledBeginningOfTheMovementSpeed.cancel(true);
            logger.debug("stop beginningOfTheMovementSpeedRunnable");
        }
        if (this.scheduledBeginningOfTheMovementDistance != null) {
            this.scheduledBeginningOfTheMovementDistance.cancel(true);
            logger.debug("stop beginningOfTheMovementDistanceRunnable {}s", (Object)76);
        }
        this.scheduledBeginningOfTheMovementSpeedWork = false;
        this.scheduledBeginningOfTheMovementDistanceWork = false;
        this.didNotAccelerateTo2KmH = false;
    }

    private void enableAttentionTest() {
        if (!this.thereIsAttention) {
            logger.debug("start Attention 7s enableAttentionTest()");
            this.thereIsAttention = true;
            this.enableUiBlinkAttention();
            this.scheduledAttention = this.scheduler.schedule(() -> {
                this.pssPassLampKey = true;
                this.pssPassLamp = true;
                this.removeElectricalVoltageFromEpkRunnable.run();
            }, 7L, TimeUnit.SECONDS);
        }
    }

    public void disableAttentionTest() {
        this.thereIsAttention = false;
        this.disableUiBlinkAttention();
        if (this.pssPassLamp && !this.pssPassLampKey) {
            this.pssPassLamp = false;
        } else {
            this.pssPassLampKey = false;
        }
        if (this.scheduledAttention != null) {
            logger.debug("stop Attention 7s disableAttentionTest()");
            this.scheduledAttention.cancel(true);
        }
    }

    public boolean isThereIsAttention() {
        return this.thereIsAttention;
    }

    public boolean isPssPassLamp() {
        return this.pssPassLamp;
    }

    private void enableUiBlinkAttention() {
        if (!this.thereIsUiBlinkAttention) {
            this.thereIsUiBlinkAttention = true;
            SoundController.getInstance().playCyclicBeep();
            this.scheduledUiBlinkAttention = this.scheduler.scheduleAtFixedRate(this.blinkAttention, 1L, 1L, TimeUnit.SECONDS);
        }
    }

    private void disableUiBlinkAttention() {
        this.thereIsUiBlinkAttention = false;
        SoundController.getInstance().stopCyclicBeep();
        if (this.scheduledUiBlinkAttention != null) {
            this.scheduledUiBlinkAttention.cancel(true);
        }
        BlockData.getInstance().getDisplayData().setVisiblyAttention(false);
    }

    public void enablePeriodicVigilanceCheck(int delaySeconds) {
        if (!this.thereIsPeriodicVigilanceCheck) {
            logger.debug("start PeriodicVigilanceCheck delay:{}", (Object)delaySeconds);
            this.thereIsPeriodicVigilanceCheck = true;
            this.scheduledPeriodicVigilanceCheck = this.scheduler.scheduleAtFixedRate(this.periodicVigilanceCheckRunnable, delaySeconds, delaySeconds, TimeUnit.SECONDS);
        }
    }

    public void disablePeriodicVigilanceCheck() {
        this.thereIsPeriodicVigilanceCheck = false;
        if (this.scheduledPeriodicVigilanceCheck != null) {
            logger.debug("disable PeriodicVigilanceCheck");
            this.scheduledPeriodicVigilanceCheck.cancel(true);
        }
    }

    public boolean isThereIsPeriodicVigilanceCheck() {
        return this.thereIsPeriodicVigilanceCheck;
    }

    public void enableCancelRollingControl() {
        if (this.thereCancelRollingControl) {
            this.thereCancelRollingControl = false;
            this.scheduledCancelRollingControl.cancel(true);
        }
        this.thereCancelRollingControl = true;
        this.scheduledCancelRollingControl = this.scheduler.schedule(this.cancelRollingControlRunnable, 60L, TimeUnit.SECONDS);
        logger.debug("deactivation of rolling control in 60 sec thereCancelRollingControl - {}", (Object)this.thereCancelRollingControl);
    }

    public void enableResetInputWhenIdle(int delaySeconds) {
        if (this.thereIsResetInputWhenIdle) {
            this.thereIsResetInputWhenIdle = false;
            this.scheduledResetInputWhenIdle.cancel(true);
        }
        this.thereIsResetInputWhenIdle = true;
        this.scheduledResetInputWhenIdle = this.scheduler.schedule(KeyboardAndCommandController.getInstance().getResetInputWhenIdle(), (long)delaySeconds, TimeUnit.SECONDS);
    }

    public void disableResetInputWhenIdle() {
        this.thereIsResetInputWhenIdle = false;
        if (this.scheduledResetInputWhenIdle != null) {
            this.scheduledResetInputWhenIdle.cancel(true);
        }
    }

    public void setThereIsResetInputWhenIdle(boolean thereIsResetInputWhenIdle) {
        this.thereIsResetInputWhenIdle = thereIsResetInputWhenIdle;
    }

    public boolean isResetInputWhenIdle() {
        return this.thereIsResetInputWhenIdle;
    }

    public void enableBlinkActualSpeed() {
        this.thereBlinkActualSpeed = true;
        this.scheduledBlinkActualSpeed = this.scheduler.scheduleAtFixedRate(this.blinkActualSpeed, 0L, 1L, TimeUnit.SECONDS);
    }

    public void disableBlinkActualSpeed() {
        this.thereBlinkActualSpeed = false;
        if (this.scheduledBlinkActualSpeed != null) {
            this.scheduledBlinkActualSpeed.cancel(true);
        }
        BlockData.getInstance().getDisplayData().setVisiblyActualSpeed(true);
    }

    public boolean isThereBlinkActualSpeed() {
        return this.thereBlinkActualSpeed;
    }

    public void enableButtonOffTimer() {
        if (this.thereButtonOffTimer) {
            this.thereButtonOffTimer = false;
            this.scheduledButtonOffTimer.cancel(true);
        }
        this.thereButtonOffTimer = true;
        this.scheduledButtonOffTimer = this.scheduler.schedule(this.buttonOffTimer, 60L, TimeUnit.SECONDS);
    }

    public void disableButtonOffTimer() {
        this.thereButtonOffTimer = false;
        if (this.scheduledButtonOffTimer != null) {
            this.scheduledButtonOffTimer.cancel(true);
        }
    }

    public boolean isThereButtonOffTimer() {
        return this.thereButtonOffTimer;
    }

    public void enableLackOfNetwork() {
        if (!this.thereLackOfNetwork) {
            this.thereLackOfNetwork = true;
            this.scheduledLackOfNetwork = this.scheduler.schedule(this.lackOfNetwork, BlockData.getInstance().getStartConfig().getDebug().getIsWindowed() ? 180L : 8L, TimeUnit.SECONDS);
        }
    }

    public void disableLackOfNetwork() {
        this.thereLackOfNetwork = false;
        if (this.scheduledLackOfNetwork != null) {
            this.scheduledLackOfNetwork.cancel(true);
        }
    }

    public boolean isThereLackOfNetwork() {
        return this.thereLackOfNetwork;
    }

    public void enableBlinkRDT() {
        this.thereBlinkRDT = true;
        this.scheduledBlinkRDT = this.scheduler.scheduleAtFixedRate(this.blinkRDT, 0L, 1L, TimeUnit.SECONDS);
    }

    public void disableBlinkRDT() {
        this.thereBlinkRDT = false;
        if (this.scheduledBlinkRDT != null) {
            this.scheduledBlinkRDT.cancel(true);
        }
        BlockData.getInstance().getDisplayData().setTrainMode("\u041f");
    }

    public boolean isThereBlinkRDT() {
        return this.thereBlinkRDT;
    }

    public void enableButtonRBRBPOffTimer() {
        if (this.thereButtonRBRBPOffTimer) {
            this.thereButtonRBRBPOffTimer = false;
            this.scheduledButtonRBRBPOffTimer.cancel(true);
        }
        this.thereButtonRBRBPOffTimer = true;
        this.scheduledButtonRBRBPOffTimer = this.scheduler.schedule(this.buttonRBRBPOffTimer, 30L, TimeUnit.SECONDS);
    }

    public void disableButtonRBRBPOffTimer() {
        this.thereButtonRBRBPOffTimer = false;
        if (this.scheduledButtonRBRBPOffTimer != null) {
            this.scheduledButtonRBRBPOffTimer.cancel(true);
        }
    }

    public void enableLongHoldOfVigilanceHandleTimer() {
        if (!this.thereLongHoldOfVigilanceHandleTimer) {
            this.thereLongHoldOfVigilanceHandleTimer = true;
            this.scheduledLongHoldOfVigilanceHandleTimer = this.scheduler.schedule(this.longHoldOfVigilanceHandleTimer, 7L, TimeUnit.SECONDS);
        }
    }

    public void disableLongHoldOfVigilanceHandleTimer() {
        if (this.thereLongHoldOfVigilanceHandleTimer) {
            this.thereLongHoldOfVigilanceHandleTimer = false;
            if (this.scheduledLongHoldOfVigilanceHandleTimer != null) {
                this.scheduledLongHoldOfVigilanceHandleTimer.cancel(true);
            }
        }
    }

    public boolean isLongHoldOfVigilanceHandle() {
        return this.longHoldOfVigilanceHandle;
    }

    class LongHoldOfVigilanceHandleTimer
    implements Runnable {
        LongHoldOfVigilanceHandleTimer() {
        }

        @Override
        public void run() {
            logger.debug("voltage remove from LongHoldOfVigilanceHandleTimer");
            TimerController.this.longHoldOfVigilanceHandle = true;
            TimerController.getInstance().disableEpkHoldingCoil();
        }
    }

    class ButtonRBRBPOffTimer
    implements Runnable {
        ButtonRBRBPOffTimer() {
        }

        @Override
        public void run() {
            BlockData.getInstance().getLocalData().setRbAndRbpToRmp(0);
        }
    }

    class BlinkRDT
    implements Runnable {
        BlinkRDT() {
        }

        @Override
        public void run() {
            if (BlockData.getInstance().getDisplayData().getTrainMode().equals("\u041f")) {
                BlockData.getInstance().getDisplayData().setTrainMode("");
            } else {
                BlockData.getInstance().getDisplayData().setTrainMode("\u041f");
            }
        }
    }

    class LackOfNetwork
    implements Runnable {
        LackOfNetwork() {
        }

        @Override
        public void run() {
            BlockData.getInstance().removePower();
            TimerController.this.powerFailure();
            BlockData.getInstance().getCurNetworkData().setBlockPower(0);
            SoundController.getInstance().stopCyclicBeep();
            logger.debug("BLOK disconnection due to LAN disconnection");
        }
    }

    class ButtonOffTimer
    implements Runnable {
        ButtonOffTimer() {
        }

        @Override
        public void run() {
            if (BlockData.getInstance().getLocalData().isOtpr()) {
                BlockData.getInstance().getLocalData().setOtpr(false);
            }
            if (BlockData.getInstance().getLocalData().isK20()) {
                BlockData.getInstance().getLocalData().setK20(false);
            }
            if (BlockData.getInstance().getLocalData().isPull()) {
                BlockData.getInstance().getLocalData().setPull(false);
            }
        }
    }

    class BlinkActualSpeed
    implements Runnable {
        BlinkActualSpeed() {
        }

        @Override
        public void run() {
            if (BlockData.getInstance().getDisplayData().isVisiblyActualSpeed()) {
                BlockData.getInstance().getDisplayData().setVisiblyActualSpeed(false);
            } else {
                BlockData.getInstance().getDisplayData().setVisiblyActualSpeed(true);
            }
        }
    }

    class CancelRollingControl
    implements Runnable {
        CancelRollingControl() {
        }

        @Override
        public void run() {
            TimerController.this.thereCancelRollingControl = false;
            logger.debug("deactivation of rolling control in 60 sec thereCancelRollingControl - {}", (Object)false);
        }
    }

    class PeriodicVigilanceCheck
    implements Runnable {
        PeriodicVigilanceCheck() {
        }

        @Override
        public void run() {
            logger.debug("Attention from periodic vigilance checks");
            TimerController.this.enableAttentionTest();
        }
    }

    class BlinkAttention
    implements Runnable {
        BlinkAttention() {
        }

        @Override
        public void run() {
            if (BlockData.getInstance().getDisplayData().isVisiblyAttention()) {
                BlockData.getInstance().getDisplayData().setVisiblyAttention(false);
            } else {
                BlockData.getInstance().getDisplayData().setVisiblyAttention(true);
            }
        }
    }

    class BeginningOfTheMovement
    implements Runnable {
        private final boolean bySpeed;

        BeginningOfTheMovement(boolean bySpeed) {
            this.bySpeed = bySpeed;
        }

        @Override
        public void run() {
            if (this.bySpeed) {
                if (BlockData.getInstance().getCurNetworkData().getActualSpeed() < 2.0f) {
                    TimerController.this.didNotAccelerateTo2KmH = true;
                }
                TimerController.this.scheduledBeginningOfTheMovementSpeedWork = false;
                logger.debug("stop beginningOfTheMovementSpeedRunnable");
            } else {
                TimerController.this.scheduledBeginningOfTheMovementDistanceWork = false;
                if (BlockData.getInstance().getLocalData().getDistanceTraveledAfterStopping() < 0.3) {
                    logger.debug("Voltage relief due to the 30 cm that have not been passed distance:{}", (Object)BlockData.getInstance().getLocalData().getDistanceTraveledAfterStopping());
                    TimerController.this.disableEpkHoldingCoil();
                }
                logger.debug("stop beginningOfTheMovementDistanceRunnable");
            }
        }
    }

    class RemoveElectricalVoltageFromEpk
    implements Runnable {
        RemoveElectricalVoltageFromEpk() {
        }

        @Override
        public void run() {
            logger.debug("Voltage drop from EPK due to rolling or periodic vigilance checks");
            TimerController.this.disableEpkHoldingCoil();
        }
    }
}

