/*
 * Decompiled with CFR 0.152.
 */
package base.display;

import base.data.BlockData;
import base.data.DisplayData;
import base.data.TRAFFIC_SIGNALS;
import base.display.DisplayController;
import base.display.SpeedometerBilUt;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DisplayControllerBilUt
implements DisplayController {
    private static final Logger logger = LogManager.getLogger(DisplayControllerBilUt.class);
    @FXML
    TextField coordinateGps;
    @FXML
    TextField stationName;
    @FXML
    TextField worldTime;
    @FXML
    TextField trainMode;
    @FXML
    TextField trainMode1;
    @FXML
    ImageView cassetteEquipped;
    @FXML
    Polygon attention;
    @FXML
    TextField channel;
    @FXML
    TextField wayNum;
    @FXML
    TextField acceleration;
    @FXML
    TextField brakingCoefficient;
    @FXML
    Label releaseBan;
    @FXML
    Circle moveForward;
    @FXML
    Circle moveBack;
    @FXML
    TextField distToTarget;
    @FXML
    TextField infoStr;
    @FXML
    TextField valueStr;
    @FXML
    TextField pressureTM;
    @FXML
    TextField pressureUR;
    @FXML
    Label actualSpeed;
    @FXML
    Label allowableSpeed;
    @FXML
    Arc blackGArc;
    @FXML
    Arc redArc;
    @FXML
    Arc yellowArc;
    @FXML
    Arc blackRArc;
    @FXML
    Arc blackYArc;
    @FXML
    Rectangle g4;
    @FXML
    Rectangle g3;
    @FXML
    Rectangle g2;
    @FXML
    Rectangle g1;
    @FXML
    Rectangle y;
    @FXML
    Rectangle r_y;
    @FXML
    Rectangle r;
    @FXML
    Rectangle w;
    DisplayData displayData = new DisplayData();
    SpeedometerBilUt speedometerBilUt;

    @Override
    public void initialize() {
        this.speedometerBilUt = new SpeedometerBilUt(this.redArc, this.yellowArc, this.blackGArc, this.blackRArc, this.blackYArc, this.allowableSpeed, this.actualSpeed);
        logger.debug("init");
    }

    @Override
    public void reinitializeCharts() {
        logger.debug("empty reinitializeCharts");
    }

    @Override
    public void update() {
        if (!this.displayData.equalityTest(BlockData.getInstance().getDisplayData())) {
            this.displayData.copy(BlockData.getInstance().getDisplayData());
            Platform.runLater(() -> {
                if (!this.displayData.isPower()) {
                    this.wayNum.setText("");
                    this.worldTime.setText("");
                    this.blackGArc.setLength(310.0);
                    this.blackRArc.setLength(310.0);
                    this.blackYArc.setLength(310.0);
                    this.actualSpeed.setVisible(false);
                    this.allowableSpeed.setVisible(false);
                    this.pressureUR.setText("");
                    this.pressureTM.setText("");
                    this.moveBack.setVisible(false);
                    this.moveForward.setVisible(false);
                    this.coordinateGps.setText("");
                    this.acceleration.setText("");
                    this.brakingCoefficient.setText("");
                    this.stationName.setText("");
                    this.cassetteEquipped.setVisible(false);
                    this.attention.setFill(Color.web("#241b1b"));
                    this.releaseBan.setOpacity(0.1);
                    this.channel.setText("");
                    this.trainMode.setText("");
                    this.trainMode1.setText("");
                    this.distToTarget.setText("");
                    this.infoStr.setText("");
                    this.valueStr.setText("");
                    this.hideSignals();
                } else {
                    this.wayNum.setText(this.displayData.getWayNum());
                    this.worldTime.setText(this.displayData.getWorldTime());
                    this.speedometerBilUt.setActualSpeed(this.displayData.getActualSpeed());
                    this.speedometerBilUt.setAllowableSpeed(this.displayData.getAllowableSpeed());
                    this.speedometerBilUt.setTargetSpeed(this.displayData.getTargetSpeed());
                    this.actualSpeed.setVisible(this.displayData.isVisiblyActualSpeed());
                    this.allowableSpeed.setVisible(true);
                    this.pressureUR.setText(this.displayData.getPressureUR());
                    this.pressureTM.setText(this.displayData.getPressureTM());
                    this.updateReverse(this.displayData.getReverse());
                    this.coordinateGps.setText(this.displayData.getCoordinate());
                    this.acceleration.setText(this.displayData.getAcceleration());
                    this.brakingCoefficient.setText(this.displayData.getBrakingCoefficient());
                    this.stationName.setText(this.displayData.getStationName());
                    this.cassetteEquipped.setVisible(this.displayData.isCassetteEquipped());
                    this.attention.setFill(this.displayData.isVisiblyAttention() ? Color.RED : Color.web("#241b1b"));
                    this.releaseBan.setOpacity(this.displayData.isVisiblyReleaseBan() ? 1.0 : 0.1);
                    this.channel.setText(this.displayData.getChannel());
                    this.setSignal(this.displayData.getTrafficSignal());
                    if (this.displayData.getTrainMode().equals("\u041f")) {
                        this.trainMode.setText("\u041f");
                        this.trainMode1.setText("");
                    } else if (this.displayData.getTrainMode().equals("\u041c")) {
                        this.trainMode.setText("");
                        this.trainMode1.setText("\u041c");
                    } else {
                        this.trainMode.setText("");
                        this.trainMode1.setText("");
                    }
                    if (this.displayData.isNeutralInsert()) {
                        this.distToTarget.setText(this.displayData.getDistanceTokorazdelOrNeutralInsert());
                    } else if (this.displayData.getDistanceToSaut().equals("") || this.displayData.getDistanceToSaut().equals("0")) {
                        this.distToTarget.setText(this.displayData.getDistanceToTarget());
                    } else {
                        this.distToTarget.setText(this.displayData.getDistanceToSaut());
                    }
                    if (this.displayData.getInfoStr().equals("") && this.displayData.getValueStr().equals("")) {
                        if (this.displayData.isNeutralInsert()) {
                            this.infoStr.setText("\u041d\u0415\u0419\u0422\u0420\u0410\u041b\u042c\u041d\u0410\u042f \u0412\u0421\u0422\u0410\u0412\u041a\u0410");
                            this.valueStr.setText("");
                        } else {
                            this.infoStr.setText(this.displayData.getTargetType());
                            this.valueStr.setText(this.displayData.getTargetName());
                        }
                    } else {
                        this.infoStr.setText(this.displayData.getInfoStr());
                        this.valueStr.setText(this.displayData.getValueStr());
                    }
                }
            });
        }
    }

    private void updateReverse(int value) {
        switch (value) {
            case 1: {
                if (!this.moveForward.isVisible()) {
                    this.moveForward.setVisible(true);
                }
                if (!this.moveBack.isVisible()) break;
                this.moveBack.setVisible(false);
                break;
            }
            case 2: {
                if (this.moveForward.isVisible()) {
                    this.moveForward.setVisible(false);
                }
                if (this.moveBack.isVisible()) break;
                this.moveBack.setVisible(true);
                break;
            }
            default: {
                if (this.moveForward.isVisible()) {
                    this.moveForward.setVisible(false);
                }
                if (!this.moveBack.isVisible()) break;
                this.moveBack.setVisible(false);
            }
        }
    }

    private void hideSignals() {
        this.g4.setOpacity(0.1);
        this.g3.setOpacity(0.1);
        this.g2.setOpacity(0.1);
        this.g1.setOpacity(0.1);
        this.r.setOpacity(0.1);
        this.r_y.setOpacity(0.1);
        this.y.setOpacity(0.1);
        this.w.setOpacity(0.1);
    }

    private void setSignal(TRAFFIC_SIGNALS trafficSignals) {
        this.hideSignals();
        switch (trafficSignals) {
            case WHITE: {
                this.w.setOpacity(1.0);
                break;
            }
            case GREEN: {
                this.g1.setOpacity(1.0);
                break;
            }
            case YELLOW: {
                this.y.setOpacity(1.0);
                break;
            }
            case RED_YELLOW: {
                this.r_y.setOpacity(1.0);
                break;
            }
            case RED: {
                this.r.setOpacity(1.0);
                break;
            }
        }
    }
}

