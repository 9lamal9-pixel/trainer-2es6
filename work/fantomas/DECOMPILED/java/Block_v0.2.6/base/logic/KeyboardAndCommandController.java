/*
 * Decompiled with CFR 0.152.
 */
package base.logic;

import base.data.BIL_MODEL;
import base.data.BLOCK_MODE;
import base.data.BlockData;
import base.data.KEYBOARD_BUTTON;
import base.data.TRAFFIC_SIGNALS;
import base.data.configs.CommandSettingsData;
import base.sound.SoundController;
import base.timers.TimerController;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KeyboardAndCommandController {
    private static final Logger logger = LogManager.getLogger(KeyboardAndCommandController.class);
    private static KeyboardAndCommandController instance;
    private String pushedButtons = "";
    private COMMANDS currentCommand = COMMANDS.NONE;
    private int inputCount;
    private final ResetInputWhenIdle resetInputWhenIdle = new ResetInputWhenIdle();
    private PData pData;
    private K5Data k5Data;
    private K6Data k6Data;
    private K7Data k7Data;
    private K799809Data k799809Data;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public static KeyboardAndCommandController getInstance() {
        if (instance == null) {
            instance = new KeyboardAndCommandController();
        }
        return instance;
    }

    private KeyboardAndCommandController() {
        this.timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        logger.debug("init");
    }

    public ResetInputWhenIdle getResetInputWhenIdle() {
        return this.resetInputWhenIdle;
    }

    void update() {
        this.keyboardClickHandler(KEYBOARD_BUTTON.values()[BlockData.getInstance().getPrevNetworkData().getKeyboardButton()], KEYBOARD_BUTTON.values()[BlockData.getInstance().getCurNetworkData().getKeyboardButton()], BlockData.getInstance().getCurNetworkData().getEkEquipped() == 1, BlockData.getInstance().getCurNetworkData().getActualSpeed() > 0.0f);
    }

    private void keyboardClickHandler(KEYBOARD_BUTTON previousKeyboardButton, KEYBOARD_BUTTON currentKeyboardButton, boolean isEkEquipped, boolean isMovement) {
        if (previousKeyboardButton != currentKeyboardButton) {
            switch (this.currentCommand) {
                case NONE: {
                    if (this.pushedButtons.contains(KEYBOARD_BUTTON.K.toString())) {
                        if (this.pushedButtons.length() < 5) {
                            this.numericKeyboardClickHandler(currentKeyboardButton);
                            BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                        }
                        switch (currentKeyboardButton) {
                            case INPUT: {
                                try {
                                    this.currentCommand = COMMANDS.valueOf(this.pushedButtons);
                                    TimerController.getInstance().disableResetInputWhenIdle();
                                    switch (this.currentCommand) {
                                        case K263: {
                                            if (!isMovement) {
                                                TimerController.getInstance().enableCancelRollingControl();
                                            }
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("");
                                            BlockData.getInstance().getDisplayData().setValueStr("");
                                            break;
                                        }
                                        case K5: {
                                            if (!isMovement) {
                                                this.k5Data = new K5Data();
                                                BlockData.getInstance().getDisplayData().setInfoStr(this.k5Data.messages[0]);
                                                BlockData.getInstance().getDisplayData().setValueStr(String.valueOf(this.k5Data.values[0]));
                                                TimerController.getInstance().enableResetInputWhenIdle(10);
                                                break;
                                            }
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("");
                                            BlockData.getInstance().getDisplayData().setValueStr("");
                                            break;
                                        }
                                        case K6: {
                                            if (!isEkEquipped) {
                                                this.k6Data = new K6Data();
                                                BlockData.getInstance().getDisplayData().setInfoStr(this.k6Data.messages[0]);
                                                BlockData.getInstance().getDisplayData().setValueStr(String.valueOf(this.k6Data.values[0]));
                                                TimerController.getInstance().enableResetInputWhenIdle(10);
                                                break;
                                            }
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("");
                                            BlockData.getInstance().getDisplayData().setValueStr("");
                                            break;
                                        }
                                        case K7: {
                                            this.startK7(isMovement);
                                            break;
                                        }
                                        case K799: 
                                        case K809: {
                                            this.k799809Data = new K799809Data();
                                            BlockData.getInstance().getDisplayData().setInfoStr(this.k799809Data.messages[0]);
                                            BlockData.getInstance().getDisplayData().setValueStr(String.valueOf(this.k799809Data.values[0]));
                                            TimerController.getInstance().enableResetInputWhenIdle(10);
                                            break;
                                        }
                                        case K800: {
                                            BlockData.getInstance().getLocalData().setK799(false);
                                            BlockData.getInstance().getLocalData().setK809(false);
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("");
                                            BlockData.getInstance().getDisplayData().setValueStr("");
                                            break;
                                        }
                                        case K45: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("\u0410\u041a\u0422\u0418\u0412\u041d\u0410\u042f \u041a\u0410\u0411\u0418\u041d\u0410");
                                            BlockData.getInstance().getDisplayData().setValueStr(BlockData.getInstance().getCommandSettingsData().getCurrent_cabin());
                                            TimerController.getInstance().enableResetInputWhenIdle(30);
                                            break;
                                        }
                                        case K46: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr(BlockData.getInstance().getCommandSettingsData().getCurrent_bloc());
                                            BlockData.getInstance().getDisplayData().setValueStr("");
                                            TimerController.getInstance().enableResetInputWhenIdle(4);
                                            break;
                                        }
                                        case K91: {
                                            this.currentCommand = COMMANDS.NONE;
                                            if (BlockData.getInstance().getCommandSettingsData().getCurrent_bloc().equals("I")) {
                                                BlockData.getInstance().getDisplayData().setInfoStr("\u0420\u0435\u0441\u0442\u0430\u0440\u0442 1");
                                                BlockData.getInstance().getDisplayData().setValueStr("");
                                                TimerController.getInstance().enableResetInputWhenIdle(3);
                                                BlockData.getInstance().getCommandSettingsData().setCurrent_bloc("II");
                                                SoundController.getInstance().playBeep();
                                                break;
                                            }
                                            BlockData.getInstance().getDisplayData().setInfoStr("");
                                            BlockData.getInstance().getDisplayData().setValueStr("");
                                            break;
                                        }
                                        case K92: {
                                            this.currentCommand = COMMANDS.NONE;
                                            if (BlockData.getInstance().getCommandSettingsData().getCurrent_bloc().equals("II")) {
                                                BlockData.getInstance().getDisplayData().setInfoStr("\u0420\u0435\u0441\u0442\u0430\u0440\u0442 2");
                                                BlockData.getInstance().getDisplayData().setValueStr("");
                                                TimerController.getInstance().enableResetInputWhenIdle(3);
                                                BlockData.getInstance().getCommandSettingsData().setCurrent_bloc("I");
                                                SoundController.getInstance().playBeep();
                                                break;
                                            }
                                            BlockData.getInstance().getDisplayData().setInfoStr("");
                                            BlockData.getInstance().getDisplayData().setValueStr("");
                                            break;
                                        }
                                        case K47: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("");
                                            BlockData.getInstance().getDisplayData().setValueStr(BlockData.getInstance().getCommandSettingsData().getCurrent_dps());
                                            TimerController.getInstance().enableResetInputWhenIdle(30);
                                            break;
                                        }
                                        case K80: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("\u0414\u0410\u0412\u041b\u0415\u041d\u0418\u0415 \u0412 \u0422\u041c");
                                            if (BlockData.getInstance().getStartConfig().getBilModel() != BIL_MODEL.BIL_UT && BlockData.getInstance().getStartConfig().getBilModel() != BIL_MODEL.BIL_V) {
                                                BlockData.getInstance().getDisplayData().setValueStr(String.format("%1.2f", Float.valueOf(BlockData.getInstance().getCurNetworkData().getTmValue())));
                                            } else {
                                                BlockData.getInstance().getDisplayData().setValueStr("");
                                            }
                                            BlockData.getInstance().getLocalData().setK80(true);
                                            TimerController.getInstance().enableResetInputWhenIdle(30);
                                            break;
                                        }
                                        case K81: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("\u0414\u0410\u0412\u041b\u0415\u041d\u0418\u0415 \u0412 \u0422\u0426");
                                            if (BlockData.getInstance().getStartConfig().getBilModel() != BIL_MODEL.BIL_UT && BlockData.getInstance().getStartConfig().getBilModel() != BIL_MODEL.BIL_V) {
                                                BlockData.getInstance().getDisplayData().setValueStr(String.format("%1.2f", Float.valueOf(BlockData.getInstance().getCurNetworkData().getTcValue())));
                                            } else {
                                                BlockData.getInstance().getDisplayData().setValueStr("");
                                            }
                                            BlockData.getInstance().getLocalData().setK81(true);
                                            TimerController.getInstance().enableResetInputWhenIdle(30);
                                            break;
                                        }
                                        case K82: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("\u0414\u0410\u0412\u041b\u0415\u041d\u0418\u0415 \u0412 \u0423\u04202");
                                            if (BlockData.getInstance().getStartConfig().getBilModel() != BIL_MODEL.BIL_UT && BlockData.getInstance().getStartConfig().getBilModel() != BIL_MODEL.BIL_V) {
                                                BlockData.getInstance().getDisplayData().setValueStr(String.format("%1.2f", Float.valueOf(BlockData.getInstance().getCurNetworkData().getUr2Value())));
                                            } else {
                                                BlockData.getInstance().getDisplayData().setValueStr("");
                                            }
                                            BlockData.getInstance().getLocalData().setK82(true);
                                            TimerController.getInstance().enableResetInputWhenIdle(30);
                                            break;
                                        }
                                        case K83: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("\u0414\u0410\u0412\u041b\u0415\u041d\u0418\u0415 \u0412 \u0423\u04201");
                                            if (BlockData.getInstance().getStartConfig().getBilModel() != BIL_MODEL.BIL_UT && BlockData.getInstance().getStartConfig().getBilModel() != BIL_MODEL.BIL_V) {
                                                BlockData.getInstance().getDisplayData().setValueStr(String.format("%1.2f", Float.valueOf(BlockData.getInstance().getCurNetworkData().getUr1Value())));
                                            } else {
                                                BlockData.getInstance().getDisplayData().setValueStr("");
                                            }
                                            BlockData.getInstance().getLocalData().setK83(true);
                                            TimerController.getInstance().enableResetInputWhenIdle(30);
                                            break;
                                        }
                                        case K522: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("\u042d\u041a");
                                            BlockData.getInstance().getDisplayData().setValueStr(BlockData.getInstance().getCommandSettingsData().getCurrent_ek_number());
                                            TimerController.getInstance().enableResetInputWhenIdle(30);
                                            break;
                                        }
                                        case K4: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("\u0422\u0415\u041a\u0423\u0429\u0415\u0415 \u0412\u0420\u0415\u041c\u042f");
                                            Date date = new Date(BlockData.getInstance().getCurNetworkData().getWorldTime());
                                            String timeS = this.timeFormat.format(date.getTime());
                                            BlockData.getInstance().getDisplayData().setValueStr(timeS);
                                            BlockData.getInstance().getLocalData().setK4(true);
                                            TimerController.getInstance().enableResetInputWhenIdle(30);
                                            break;
                                        }
                                        case K3: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("\u0422\u0415\u041a\u0423\u0429\u0410\u042f \u0414\u0410\u0422\u0410");
                                            Date date = new Date(BlockData.getInstance().getCurNetworkData().getWorldTime());
                                            String dateS = this.dateFormat.format(date.getTime());
                                            BlockData.getInstance().getDisplayData().setValueStr(dateS);
                                            TimerController.getInstance().enableResetInputWhenIdle(30);
                                            break;
                                        }
                                        case K123: {
                                            this.keyOTM();
                                            BlockData.getInstance().getLocalData().setK123(true);
                                            break;
                                        }
                                        case K122: {
                                            this.keyOTM();
                                            BlockData.getInstance().getLocalData().setK123(false);
                                            break;
                                        }
                                        case K71: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("- 2 3 4 " + (BlockData.getInstance().getCurNetworkData().getEkEquipped() == 1 ? "5" : "-") + " 6 7 " + (BlockData.getInstance().getCurNetworkData().getSautEquipped() == 1 ? "8" : "-") + " 9 - " + (BlockData.getInstance().getCurNetworkData().getTskbmEquipped() == 1 ? "B" : "-") + " C");
                                            BlockData.getInstance().getDisplayData().setValueStr("");
                                            TimerController.getInstance().enableResetInputWhenIdle(30);
                                            break;
                                        }
                                        default: {
                                            this.currentCommand = COMMANDS.NONE;
                                            BlockData.getInstance().getDisplayData().setInfoStr("");
                                            BlockData.getInstance().getDisplayData().setValueStr("");
                                            break;
                                        }
                                    }
                                }
                                catch (IllegalArgumentException e) {
                                    logger.debug("Invalid value for enum {}", (Object)this.pushedButtons);
                                    this.currentCommand = COMMANDS.NONE;
                                    BlockData.getInstance().getDisplayData().setInfoStr("");
                                    BlockData.getInstance().getDisplayData().setValueStr("");
                                }
                                this.pushedButtons = "";
                                break;
                            }
                            case STR: {
                                if (this.pushedButtons.length() > 1) {
                                    this.pushedButtons = this.pushedButtons.substring(0, this.pushedButtons.length() - 1);
                                }
                                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                                break;
                            }
                            case OTM: {
                                this.pushedButtons = "";
                                BlockData.getInstance().getDisplayData().setInfoStr("");
                                BlockData.getInstance().getDisplayData().setValueStr("");
                            }
                        }
                        break;
                    }
                    switch (currentKeyboardButton) {
                        case K: {
                            BlockData.getInstance().getLocalData().resetK80_83();
                            this.pushedButtons = this.pushedButtons + KEYBOARD_BUTTON.K.toString();
                            BlockData.getInstance().getDisplayData().setInfoStr("\u0412\u0412\u041e\u0414 \u041a\u041e\u041c\u0410\u041d\u0414\u042b");
                            BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                            TimerController.getInstance().enableResetInputWhenIdle(30);
                            break;
                        }
                        case P: {
                            this.keyOTM();
                            this.currentCommand = COMMANDS.P;
                            this.pData = new PData();
                            BlockData.getInstance().getDisplayData().setInfoStr(this.pData.messages[0]);
                            BlockData.getInstance().getDisplayData().setValueStr(String.valueOf(this.pData.values[0]));
                            TimerController.getInstance().enableResetInputWhenIdle(10);
                            break;
                        }
                        case VK: {
                            if (BlockData.getInstance().getCurNetworkData().getRbButton() != 1 && BlockData.getInstance().getCurNetworkData().getRbpButton() != 1 || BlockData.getInstance().getLocalData().isVk() || BlockData.getInstance().getCurNetworkData().getActualSpeed() != 0.0f || TRAFFIC_SIGNALS.values()[BlockData.getInstance().getCurNetworkData().getTrafficLightCode()] != TRAFFIC_SIGNALS.RED_YELLOW && TRAFFIC_SIGNALS.values()[BlockData.getInstance().getCurNetworkData().getTrafficLightCode()] != TRAFFIC_SIGNALS.RED) break;
                            if (BlockData.getInstance().getCurNetworkData().getEkEquipped() == 1) {
                                if (BlockData.getInstance().getLocalData().getAllowableSpeed() >= 20) break;
                                BlockData.getInstance().getLocalData().setVk(true);
                                break;
                            }
                            if (BlockData.getInstance().getCurNetworkData().getEkEquipped() != 0) break;
                            BlockData.getInstance().getLocalData().setVk(true);
                            break;
                        }
                        case F: {
                            if (isEkEquipped) break;
                            int tempIndex = BlockData.getInstance().getDisplayData().getCurrentIndexAlsn();
                            if (++tempIndex >= 3) {
                                tempIndex = 0;
                            }
                            BlockData.getInstance().getDisplayData().setCurrentIndexAlsn(tempIndex);
                            BlockData.getInstance().getDisplayData().setChannel(BlockData.getInstance().getDisplayData().getFrequenciesAlsn());
                            break;
                        }
                        case RMP: {
                            if (BlockData.getInstance().getCurNetworkData().getActualSpeed() != 0.0f) break;
                            BlockData.getInstance().getLocalData().setK799(false);
                            BlockData.getInstance().getLocalData().setK809(false);
                            if (BlockData.getInstance().getLocalData().getRbAndRbpToRmp() > 20 && BlockData.getInstance().getLocalData().getBlockMode() != BLOCK_MODE.RDT) {
                                BlockData.getInstance().getCommandSettingsData().setCurrent_mode("\u041f");
                                BlockData.getInstance().getLocalData().setBlockMode(BLOCK_MODE.RDT);
                                BlockData.getInstance().getLocalData().setModeChangeOnCommandk799k809(false);
                                BlockData.getInstance().getLocalData().setRbAndRbpToRmp(0);
                                if (TimerController.getInstance().isThereBlinkRDT()) break;
                                TimerController.getInstance().enableBlinkRDT();
                                break;
                            }
                            if (BlockData.getInstance().getLocalData().getBlockMode() == BLOCK_MODE.RDT) {
                                BlockData.getInstance().getLocalData().setBlockMode(BLOCK_MODE.TRAIN);
                                if (!TimerController.getInstance().isThereBlinkRDT()) break;
                                TimerController.getInstance().disableBlinkRDT();
                                break;
                            }
                            switch (BlockData.getInstance().getLocalData().getBlockMode()) {
                                case TRAIN: {
                                    BlockData.getInstance().getLocalData().setBlockMode(BLOCK_MODE.MANEUVERING);
                                    BlockData.getInstance().getCommandSettingsData().setCurrent_mode("\u041c");
                                    BlockData.getInstance().writeCommandSettingsDataFile();
                                    break;
                                }
                                case MANEUVERING: {
                                    BlockData.getInstance().getLocalData().setBlockMode(BLOCK_MODE.TRAIN);
                                    BlockData.getInstance().getLocalData().setModeChangeOnCommandk799k809(false);
                                    BlockData.getInstance().getCommandSettingsData().setCurrent_mode("\u041f");
                                    BlockData.getInstance().writeCommandSettingsDataFile();
                                }
                            }
                            break;
                        }
                        case PULL: {
                            if (BlockData.getInstance().getCurNetworkData().getSautEquipped() != 1 || BlockData.getInstance().getCurNetworkData().getDistanceToTrafficLight() > 560 || TRAFFIC_SIGNALS.values()[BlockData.getInstance().getCurNetworkData().getTrafficLightCode()] != TRAFFIC_SIGNALS.RED_YELLOW && TRAFFIC_SIGNALS.values()[BlockData.getInstance().getCurNetworkData().getTrafficLightCode()] != TRAFFIC_SIGNALS.WHITE || BlockData.getInstance().getCurNetworkData().getActualSpeed() != 0.0f || BlockData.getInstance().getLocalData().isPull()) break;
                            BlockData.getInstance().getLocalData().setPull(true);
                            BlockData.getInstance().getLocalData().setPullCount(BlockData.getInstance().getLocalData().getPullCount() + 1);
                            TimerController.getInstance().enableButtonOffTimer();
                            if (BlockData.getInstance().getLocalData().getPullCount() == 1) {
                                BlockData.getInstance().getLocalData().setDistanceToAimingPoint(300);
                                break;
                            }
                            if (BlockData.getInstance().getLocalData().getPullCount() <= 1) break;
                            BlockData.getInstance().getLocalData().setDistanceToAimingPoint(50);
                            break;
                        }
                        case OTPR: {
                            if (BlockData.getInstance().getCurNetworkData().getSautEquipped() != 1 || BlockData.getInstance().getCurNetworkData().getDistanceToTrafficLight() > 560 || TRAFFIC_SIGNALS.values()[BlockData.getInstance().getCurNetworkData().getTrafficLightCode()] != TRAFFIC_SIGNALS.WHITE || BlockData.getInstance().getCurNetworkData().getActualSpeed() != 0.0f || BlockData.getInstance().getLocalData().isOtpr()) break;
                            BlockData.getInstance().getLocalData().setOtpr(true);
                            TimerController.getInstance().enableButtonOffTimer();
                            BlockData.getInstance().getLocalData().setDistanceToAimingPoint(600);
                            break;
                        }
                        case K20: {
                            if (BlockData.getInstance().getCurNetworkData().getSautEquipped() != 1 || BlockData.getInstance().getCurNetworkData().getDistanceToTrafficLight() > 300 || TRAFFIC_SIGNALS.values()[BlockData.getInstance().getCurNetworkData().getTrafficLightCode()] != TRAFFIC_SIGNALS.RED_YELLOW && TRAFFIC_SIGNALS.values()[BlockData.getInstance().getCurNetworkData().getTrafficLightCode()] != TRAFFIC_SIGNALS.RED || !(BlockData.getInstance().getCurNetworkData().getActualSpeed() <= 20.0f) || BlockData.getInstance().getLocalData().isK20()) break;
                            BlockData.getInstance().getLocalData().setK20(true);
                            TimerController.getInstance().enableButtonOffTimer();
                            BlockData.getInstance().getLocalData().setDistanceToAimingPoint(300);
                            break;
                        }
                        case OS: {
                            break;
                        }
                        case L: {
                            this.startK7(isMovement);
                        }
                    }
                    break;
                }
                case P: {
                    TimerController.getInstance().enableResetInputWhenIdle(10);
                    this.pCommand(currentKeyboardButton);
                    break;
                }
                case K5: {
                    TimerController.getInstance().enableResetInputWhenIdle(10);
                    this.k5Command(currentKeyboardButton);
                    break;
                }
                case K6: {
                    TimerController.getInstance().enableResetInputWhenIdle(10);
                    this.k6Command(currentKeyboardButton);
                    break;
                }
                case K7: {
                    TimerController.getInstance().enableResetInputWhenIdle(10);
                    this.k7Command(currentKeyboardButton);
                    break;
                }
                case K799: {
                    TimerController.getInstance().enableResetInputWhenIdle(10);
                    this.k799Command(currentKeyboardButton);
                    break;
                }
                case K809: {
                    TimerController.getInstance().enableResetInputWhenIdle(10);
                    this.k809Command(currentKeyboardButton);
                }
            }
            if (currentKeyboardButton != KEYBOARD_BUTTON.NONE) {
                SoundController.getInstance().playClick();
                logger.debug("{} button push {}", (Object)currentKeyboardButton.name(), (Object)this.pushedButtons);
            }
        }
    }

    private void startK7(boolean isMovement) {
        if (!isMovement) {
            this.currentCommand = COMMANDS.K7;
            this.k7Data = new K7Data();
            BlockData.getInstance().getDisplayData().setInfoStr(this.k7Data.messages[0]);
            BlockData.getInstance().getDisplayData().setValueStr(String.valueOf(this.k7Data.values[0]));
            TimerController.getInstance().enableResetInputWhenIdle(10);
        } else {
            this.currentCommand = COMMANDS.NONE;
            BlockData.getInstance().getDisplayData().setInfoStr("");
            BlockData.getInstance().getDisplayData().setValueStr("");
        }
    }

    private String getNumberName(KEYBOARD_BUTTON key) {
        return key.toString().replace("_", "");
    }

    private void numericKeyboardClickHandler(KEYBOARD_BUTTON currentKeyboardButton) {
        switch (currentKeyboardButton) {
            case _1: {
                this.pushedButtons = this.pushedButtons + this.getNumberName(KEYBOARD_BUTTON._1);
                break;
            }
            case _2: {
                this.pushedButtons = this.pushedButtons + this.getNumberName(KEYBOARD_BUTTON._2);
                break;
            }
            case _3: {
                this.pushedButtons = this.pushedButtons + this.getNumberName(KEYBOARD_BUTTON._3);
                break;
            }
            case _4: {
                this.pushedButtons = this.pushedButtons + this.getNumberName(KEYBOARD_BUTTON._4);
                break;
            }
            case _5: {
                this.pushedButtons = this.pushedButtons + this.getNumberName(KEYBOARD_BUTTON._5);
                break;
            }
            case _6: {
                this.pushedButtons = this.pushedButtons + this.getNumberName(KEYBOARD_BUTTON._6);
                break;
            }
            case _7: {
                this.pushedButtons = this.pushedButtons + this.getNumberName(KEYBOARD_BUTTON._7);
                break;
            }
            case _8: {
                this.pushedButtons = this.pushedButtons + this.getNumberName(KEYBOARD_BUTTON._8);
                break;
            }
            case _9: {
                this.pushedButtons = this.pushedButtons + this.getNumberName(KEYBOARD_BUTTON._9);
                break;
            }
            case _0: {
                this.pushedButtons = this.pushedButtons + this.getNumberName(KEYBOARD_BUTTON._0);
            }
        }
    }

    private void pCommand(KEYBOARD_BUTTON currentKeyboardButton) {
        switch (currentKeyboardButton) {
            case _1: 
            case _2: 
            case _3: 
            case _4: 
            case _5: 
            case _6: 
            case _7: 
            case _8: 
            case _9: 
            case _0: {
                switch (this.inputCount) {
                    case 0: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 15) break;
                        this.pushedButtons = "15";
                        break;
                    }
                    case 1: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 1) break;
                        this.pushedButtons = "1";
                    }
                }
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case STR: {
                if (this.pushedButtons.length() == 0) break;
                this.pushedButtons = this.pushedButtons.substring(0, this.pushedButtons.length() - 1);
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case INPUT: {
                if (this.pushedButtons.length() == 0) {
                    logger.debug("{} number is written", (Object)this.pData.values[this.inputCount]);
                    this.pushedButtons = String.valueOf(this.pData.values[this.inputCount]);
                }
                try {
                    this.pData.values[this.inputCount] = Integer.parseInt(this.pushedButtons);
                }
                catch (NumberFormatException e) {
                    logger.error(e);
                }
                if (this.inputCount == 1) {
                    this.keyOTM();
                    this.pData.saveToBlockData();
                    BlockData.getInstance().writeCommandSettingsDataFile();
                    break;
                }
                ++this.inputCount;
                this.pushedButtons = "";
                BlockData.getInstance().getDisplayData().setInfoStr(this.pData.messages[this.inputCount]);
                BlockData.getInstance().getDisplayData().setValueStr(String.valueOf(this.pData.values[this.inputCount]));
                TimerController.getInstance().enableResetInputWhenIdle(10);
                break;
            }
            case OTM: {
                this.keyOTM();
            }
        }
    }

    private void k7Command(KEYBOARD_BUTTON currentKeyboardButton) {
        switch (currentKeyboardButton) {
            case _1: 
            case _2: 
            case _3: 
            case _4: 
            case _5: 
            case _6: 
            case _7: 
            case _8: 
            case _9: 
            case _0: {
                switch (this.inputCount) {
                    case 0: 
                    case 1: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 99999) break;
                        this.pushedButtons = "99999";
                        break;
                    }
                    case 2: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 500) break;
                        this.pushedButtons = "500";
                        break;
                    }
                    case 3: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 150) break;
                        this.pushedButtons = "150";
                        break;
                    }
                    case 4: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 10000) break;
                        this.pushedButtons = "10000";
                        break;
                    }
                    case 5: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 1) break;
                        this.pushedButtons = "1";
                    }
                }
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case STR: {
                if (this.pushedButtons.length() == 0) break;
                this.pushedButtons = this.pushedButtons.substring(0, this.pushedButtons.length() - 1);
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case INPUT: {
                if (this.pushedButtons.length() == 0) {
                    logger.debug("{} number is written", (Object)this.k7Data.values[this.inputCount]);
                    this.pushedButtons = String.valueOf(this.k7Data.values[this.inputCount]);
                }
                try {
                    this.k7Data.values[this.inputCount] = Integer.parseInt(this.pushedButtons);
                }
                catch (NumberFormatException e) {
                    logger.error(e);
                }
                if (this.inputCount == 5) {
                    this.keyOTM();
                    this.k7Data.saveToBlockData();
                    BlockData.getInstance().writeCommandSettingsDataFile();
                    break;
                }
                ++this.inputCount;
                this.pushedButtons = "";
                BlockData.getInstance().getDisplayData().setInfoStr(this.k7Data.messages[this.inputCount]);
                BlockData.getInstance().getDisplayData().setValueStr(String.valueOf(this.k7Data.values[this.inputCount]));
                TimerController.getInstance().enableResetInputWhenIdle(10);
                break;
            }
            case OTM: {
                this.keyOTM();
            }
        }
    }

    private void k6Command(KEYBOARD_BUTTON currentKeyboardButton) {
        switch (currentKeyboardButton) {
            case _1: 
            case _2: 
            case _3: 
            case _4: 
            case _5: 
            case _6: 
            case _7: 
            case _8: 
            case _9: 
            case _0: {
                switch (this.inputCount) {
                    case 0: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 9999999) break;
                        this.pushedButtons = "9999999";
                        break;
                    }
                    case 1: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 1) break;
                        this.pushedButtons = "1";
                    }
                }
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case STR: {
                if (this.pushedButtons.length() == 0) break;
                this.pushedButtons = this.pushedButtons.substring(0, this.pushedButtons.length() - 1);
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case INPUT: {
                if (this.pushedButtons.length() == 0) {
                    logger.debug("{} number is written", (Object)this.k6Data.values[this.inputCount]);
                    this.pushedButtons = String.valueOf(this.k6Data.values[this.inputCount]);
                }
                try {
                    this.k6Data.values[this.inputCount] = Integer.parseInt(this.pushedButtons);
                }
                catch (NumberFormatException e) {
                    logger.error(e);
                }
                if (this.inputCount == 1) {
                    this.keyOTM();
                    BlockData.getInstance().getCommandSettingsData().setCurrent_coordinate(String.valueOf(this.k6Data.values[0]));
                    BlockData.getInstance().getCommandSettingsData().setValue_of_coordinate_change(this.k6Data.values[1]);
                    BlockData.getInstance().writeCommandSettingsDataFile();
                    break;
                }
                ++this.inputCount;
                this.pushedButtons = "";
                BlockData.getInstance().getDisplayData().setInfoStr(this.k6Data.messages[this.inputCount]);
                BlockData.getInstance().getDisplayData().setValueStr(String.valueOf(this.k6Data.values[this.inputCount]));
                TimerController.getInstance().enableResetInputWhenIdle(10);
                break;
            }
            case OTM: {
                this.keyOTM();
            }
        }
    }

    private void k5Command(KEYBOARD_BUTTON currentKeyboardButton) {
        switch (currentKeyboardButton) {
            case _1: 
            case _2: 
            case _3: 
            case _4: 
            case _5: 
            case _6: 
            case _7: 
            case _8: 
            case _9: 
            case _0: {
                switch (this.inputCount) {
                    case 0: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 8) break;
                        this.pushedButtons = "8";
                        break;
                    }
                    case 1: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 255) break;
                        this.pushedButtons = "255";
                        break;
                    }
                    case 2: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 99999) break;
                        this.pushedButtons = "99999";
                        break;
                    }
                    case 3: 
                    case 4: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 1290) break;
                        this.pushedButtons = "1290";
                        break;
                    }
                    case 5: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 54) break;
                        this.pushedButtons = "54";
                        break;
                    }
                    case 6: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 65535) break;
                        this.pushedButtons = "65535";
                        break;
                    }
                    case 7: 
                    case 8: 
                    case 9: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 300) break;
                        this.pushedButtons = "300";
                        break;
                    }
                    case 10: {
                        this.numericKeyboardClickHandler(currentKeyboardButton);
                        if (this.pushedButtons.length() == 0 || Integer.parseInt(this.pushedButtons) <= 3200) break;
                        this.pushedButtons = "3200";
                    }
                }
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case STR: {
                if (this.pushedButtons.length() == 0) break;
                this.pushedButtons = this.pushedButtons.substring(0, this.pushedButtons.length() - 1);
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case INPUT: {
                if (this.pushedButtons.length() != 0) {
                    int value = 0;
                    try {
                        value = Integer.parseInt(this.pushedButtons);
                    }
                    catch (NumberFormatException e) {
                        logger.error(e);
                    }
                    if (this.inputCount == 0 && value == 0) {
                        logger.debug("number is written 1");
                        this.pushedButtons = "1";
                    } else if ((this.inputCount == 3 || this.inputCount == 4) && value < 850) {
                        logger.debug("number is written 850");
                        this.pushedButtons = "850";
                    } else if (this.inputCount == 5 && value < 32) {
                        logger.debug("number is written 32");
                        this.pushedButtons = "32";
                    } else if (this.inputCount == 10 && value < 500) {
                        logger.debug("number is written 500");
                        this.pushedButtons = "500";
                    }
                } else {
                    logger.debug("{} number is written", (Object)this.k5Data.values[this.inputCount]);
                    this.pushedButtons = String.valueOf(this.k5Data.values[this.inputCount]);
                }
                try {
                    this.k5Data.values[this.inputCount] = Integer.parseInt(this.pushedButtons);
                }
                catch (NumberFormatException e) {
                    logger.error(e);
                }
                if (this.inputCount == 10) {
                    this.keyOTM();
                    this.k5Data.saveToBlockData();
                    BlockData.getInstance().writeCommandSettingsDataFile();
                    break;
                }
                ++this.inputCount;
                this.pushedButtons = "";
                BlockData.getInstance().getDisplayData().setInfoStr(this.k5Data.messages[this.inputCount]);
                BlockData.getInstance().getDisplayData().setValueStr(String.valueOf(this.k5Data.values[this.inputCount]));
                TimerController.getInstance().enableResetInputWhenIdle(10);
                break;
            }
            case OTM: {
                this.keyOTM();
            }
        }
    }

    private void k799Command(KEYBOARD_BUTTON currentKeyboardButton) {
        switch (currentKeyboardButton) {
            case _1: 
            case _2: 
            case _3: 
            case _4: 
            case _5: 
            case _6: 
            case _7: 
            case _8: 
            case _9: 
            case _0: {
                if (this.inputCount == 0) {
                    this.numericKeyboardClickHandler(currentKeyboardButton);
                    if (this.pushedButtons.length() != 0 && Integer.parseInt(this.pushedButtons) > 250) {
                        this.pushedButtons = "250";
                    }
                }
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case STR: {
                if (this.pushedButtons.length() == 0) break;
                this.pushedButtons = this.pushedButtons.substring(0, this.pushedButtons.length() - 1);
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case INPUT: {
                if (this.pushedButtons.length() == 0) {
                    logger.debug("{} number is written", (Object)this.k799809Data.values[this.inputCount]);
                    this.pushedButtons = String.valueOf(this.k799809Data.values[this.inputCount]);
                } else {
                    logger.debug("{} number is written", (Object)this.pushedButtons);
                    BlockData.getInstance().getLocalData().setSpeedOnWhiteFromCommand(Integer.parseInt(this.pushedButtons));
                }
                BlockData.getInstance().getLocalData().setK799(true);
                BlockData.getInstance().getLocalData().setModeChangeOnCommandk799k809(false);
                this.keyOTM();
                break;
            }
            case OTM: {
                this.keyOTM();
            }
        }
    }

    private void k809Command(KEYBOARD_BUTTON currentKeyboardButton) {
        switch (currentKeyboardButton) {
            case _1: 
            case _2: 
            case _3: 
            case _4: 
            case _5: 
            case _6: 
            case _7: 
            case _8: 
            case _9: 
            case _0: {
                if (this.inputCount == 0) {
                    this.numericKeyboardClickHandler(currentKeyboardButton);
                    if (this.pushedButtons.length() != 0 && Integer.parseInt(this.pushedButtons) > 250) {
                        this.pushedButtons = "250";
                    }
                }
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case STR: {
                if (this.pushedButtons.length() == 0) break;
                this.pushedButtons = this.pushedButtons.substring(0, this.pushedButtons.length() - 1);
                BlockData.getInstance().getDisplayData().setValueStr(this.pushedButtons);
                break;
            }
            case INPUT: {
                if (BlockData.getInstance().getCurNetworkData().getEkEquipped() == 0) {
                    if (this.pushedButtons.length() == 0) {
                        logger.debug("{} number is written", (Object)this.k799809Data.values[this.inputCount]);
                        this.pushedButtons = String.valueOf(this.k799809Data.values[this.inputCount]);
                    } else {
                        logger.debug("{} number is written", (Object)this.pushedButtons);
                        BlockData.getInstance().getLocalData().setSpeedOnWhiteFromCommand(Integer.parseInt(this.pushedButtons));
                    }
                    BlockData.getInstance().getLocalData().setK809(true);
                    BlockData.getInstance().getLocalData().setModeChangeOnCommandk799k809(false);
                }
                this.keyOTM();
                break;
            }
            case OTM: {
                this.keyOTM();
            }
        }
    }

    private void keyOTM() {
        this.currentCommand = COMMANDS.NONE;
        this.pushedButtons = "";
        this.inputCount = 0;
        BlockData.getInstance().getLocalData().resetK80_83();
        BlockData.getInstance().getDisplayData().setInfoStr("");
        BlockData.getInstance().getDisplayData().setValueStr("");
        if (TimerController.getInstance().isResetInputWhenIdle()) {
            TimerController.getInstance().disableResetInputWhenIdle();
        }
    }

    private class K799809Data {
        String[] messages = new String[]{"\u0421\u041a\u041e\u0420 \u041d\u0410 \u0411\u0415\u041b\u042b\u0419"};
        int[] values = new int[1];

        K799809Data() {
            this.values[0] = BlockData.getInstance().getLocalData().getSpeedOnWhiteFromCommand();
        }
    }

    private class K6Data {
        String[] messages = new String[]{"\u041a\u041e\u041e\u0420\u0414\u0418\u041d\u0410\u0422\u0410, \u043c", "\u0418\u0417\u041c. \u041a\u041e\u041e\u0420\u0414\u0418\u041d\u0410\u0422\u042b"};
        int[] values = new int[2];
        CommandSettingsData commandSettingsData = BlockData.getInstance().getCommandSettingsData();

        K6Data() {
            this.values[0] = Integer.parseInt(this.commandSettingsData.getCurrent_coordinate());
            this.values[1] = this.commandSettingsData.getValue_of_coordinate_change();
        }
    }

    private class K7Data {
        String[] messages = new String[]{"\u0422\u0410\u0411\u0415\u041b\u042c\u041d\u042b\u0419 \u041d\u041e\u041c\u0415\u0420", "\u041d\u041e\u041c\u0415\u0420 \u041f\u041e\u0415\u0417\u0414\u0410", "\u0414\u041b\u0418\u041d\u0410 \u041f\u041e\u0415\u0417\u0414\u0410 \u0412 \u041e\u0421\u042f\u0425", "\u0414\u041b\u0418\u041d\u0410 \u041f\u041e\u0415\u0417\u0414\u0410 \u0412 \u0412\u0410\u0413\u041e\u041d\u0410\u0425", "\u041c\u0410\u0421\u0421\u0410 \u041f\u041e\u0415\u0417\u0414\u0410, \u0442", "\u0412\u0420\u0415\u041c\u042f \u0417\u0438\u043c\u0430/\u041b\u0435\u0442\u043e"};
        int[] values = new int[6];
        CommandSettingsData commandSettingsData = BlockData.getInstance().getCommandSettingsData();

        K7Data() {
            this.values[0] = Integer.parseInt(this.commandSettingsData.getTable_number());
            this.values[1] = Integer.parseInt(this.commandSettingsData.getTrain_number());
            this.values[2] = Integer.parseInt(this.commandSettingsData.getTrain_length_axis());
            this.values[3] = Integer.parseInt(this.commandSettingsData.getTrain_length_box());
            this.values[4] = Integer.parseInt(this.commandSettingsData.getTrain_mass());
            this.values[5] = Integer.parseInt(this.commandSettingsData.getWinter_summer());
        }

        void saveToBlockData() {
            this.commandSettingsData.setTable_number(String.valueOf(this.values[0]));
            this.commandSettingsData.setTrain_number(String.valueOf(this.values[1]));
            this.commandSettingsData.setTrain_length_axis(String.valueOf(this.values[2]));
            this.commandSettingsData.setTrain_length_box(String.valueOf(this.values[3]));
            this.commandSettingsData.setTrain_mass(String.valueOf(this.values[4]));
            this.commandSettingsData.setWinter_summer(String.valueOf(this.values[5]));
        }
    }

    private class PData {
        String[] messages = new String[]{"\u041d\u041e\u041c\u0415\u0420 \u041f\u0423\u0422\u0418", "\u041f\u0420\u0418\u0417\u041d. \u041f\u0420\u0410\u0412\u0418\u041b\u042c\u041d"};
        int[] values = new int[2];
        CommandSettingsData commandSettingsData = BlockData.getInstance().getCommandSettingsData();

        PData() {
            this.values[0] = Integer.parseInt(this.commandSettingsData.getWay_number());
            this.values[1] = Integer.parseInt(this.commandSettingsData.getCorrectness_of_way());
        }

        void saveToBlockData() {
            this.commandSettingsData.setWay_number(String.valueOf(this.values[0]));
            this.commandSettingsData.setCorrectness_of_way(String.valueOf(this.values[1]));
        }
    }

    private class K5Data {
        String[] messages = new String[]{"\u041a\u0410\u0422\u0415\u0413\u041e\u0420\u0418\u042f \u041f\u041e\u0415\u0417\u0414\u0410", "\u0422\u0418\u041f \u041b\u041e\u041a\u041e\u041c\u041e\u0422\u0418\u0412\u0410", "\u041d\u041e\u041c\u0415\u0420 \u041b\u041e\u041a\u041e\u041c\u041e\u0422\u0418\u0412\u0410", "\u0414\u0418\u0410\u041c\u0415\u0422\u0420 1", "\u0414\u0418\u0410\u041c\u0415\u0422\u0420 2", "\u0427\u0418\u0421\u041b\u041e \u0417\u0423\u0411\u042c\u0415\u0412 \u0414\u0421", "\u041d\u041e\u041c\u0415\u0420 \u041a\u041e\u041d\u0424\u0418\u0413\u0423\u0420\u0410\u0426\u0418\u0418", "\u0421\u041a\u041e\u0420 \u041d\u0410 \u0411\u0415\u041b\u042b\u0419", "\u0421\u041a\u041e\u0420 \u041d\u0410 \u0417\u0415\u041b\u0415\u041d\u042b\u0419", "\u0421\u041a\u041e\u0420 \u041d\u0410 \u0416\u0415\u041b\u0422\u042b\u0419", "\u0414\u041b\u0418\u041d\u0410 \u0411\u041b\u041e\u041a\u0423\u0427\u0410\u0421\u0422\u041a\u0410"};
        int[] values = new int[11];
        CommandSettingsData commandSettingsData = BlockData.getInstance().getCommandSettingsData();

        K5Data() {
            this.values[0] = Integer.parseInt(this.commandSettingsData.getTrain_category());
            this.values[1] = Integer.parseInt(this.commandSettingsData.getTrain_type());
            this.values[2] = Integer.parseInt(this.commandSettingsData.getLoco_number());
            this.values[3] = Integer.parseInt(this.commandSettingsData.getDiameter_1());
            this.values[4] = Integer.parseInt(this.commandSettingsData.getDiameter_2());
            this.values[5] = Integer.parseInt(this.commandSettingsData.getTeeth_count());
            this.values[6] = Integer.parseInt(this.commandSettingsData.getConfig_number());
            this.values[7] = Integer.parseInt(this.commandSettingsData.getSpeed_to_white());
            this.values[8] = Integer.parseInt(this.commandSettingsData.getSpeed_to_green());
            this.values[9] = Integer.parseInt(this.commandSettingsData.getSpeed_to_yellow());
            this.values[10] = Integer.parseInt(this.commandSettingsData.getLength_area());
        }

        void saveToBlockData() {
            this.commandSettingsData.setTrain_category(String.valueOf(this.values[0]));
            this.commandSettingsData.setTrain_type(String.valueOf(this.values[1]));
            this.commandSettingsData.setLoco_number(String.valueOf(this.values[2]));
            this.commandSettingsData.setDiameter_1(String.valueOf(this.values[3]));
            this.commandSettingsData.setDiameter_2(String.valueOf(this.values[4]));
            this.commandSettingsData.setTeeth_count(String.valueOf(this.values[5]));
            this.commandSettingsData.setConfig_number(String.valueOf(this.values[6]));
            this.commandSettingsData.setSpeed_to_white(String.valueOf(this.values[7]));
            this.commandSettingsData.setSpeed_to_green(String.valueOf(this.values[8]));
            this.commandSettingsData.setSpeed_to_yellow(String.valueOf(this.values[9]));
            this.commandSettingsData.setLength_area(String.valueOf(this.values[10]));
        }
    }

    class ResetInputWhenIdle
    implements Runnable {
        ResetInputWhenIdle() {
        }

        @Override
        public void run() {
            KeyboardAndCommandController.this.keyOTM();
            logger.debug("10c input - skipped");
        }
    }

    private static enum COMMANDS {
        NONE,
        P,
        K2,
        K3,
        K4,
        K5,
        K6,
        K7,
        K45,
        K46,
        K47,
        K70,
        K71,
        K80,
        K81,
        K82,
        K83,
        K91,
        K92,
        K122,
        K123,
        K262,
        K263,
        K522,
        K799,
        K809,
        K800,
        K1031,
        K1037;

    }
}

