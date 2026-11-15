/*
 * Decompiled with CFR 0.152.
 */
package base.display;

import base.data.BlockData;
import base.data.DisplayData;
import base.display.DisplayController;
import base.display.SpeedometerBilM;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DisplayControllerOther
implements DisplayController {
    private static final Logger logger = LogManager.getLogger(DisplayControllerOther.class);
    @FXML
    TextField coordinateGps;
    @FXML
    TextField stationName;
    @FXML
    TextField worldTime;
    @FXML
    TextField trainMode;
    @FXML
    ImageView cassetteEquipped;
    @FXML
    ImageView serviceBraking;
    @FXML
    Label ALS_EN;
    @FXML
    Polygon tskbmAttention;
    @FXML
    Polygon attention;
    @FXML
    Circle tskbmPower;
    @FXML
    TextField channel;
    @FXML
    TextField wayNum;
    @FXML
    TextField acceleration;
    @FXML
    TextField distanceToSaut;
    @FXML
    TextField brakingCoefficient;
    @FXML
    Label releaseBan;
    @FXML
    Polygon moveForward;
    @FXML
    Polygon moveBack;
    @FXML
    Label neutralInsert;
    @FXML
    Label tokorazdel;
    @FXML
    Label distanceTokorazdelOrNeutralInsert;
    @FXML
    TextField distToTarget;
    @FXML
    TextField roadBias;
    @FXML
    TextField targetCode;
    @FXML
    TextField targetName;
    @FXML
    TextField infoStr;
    @FXML
    TextField valueStr;
    @FXML
    ProgressBar progressTC;
    @FXML
    Label measureTC;
    @FXML
    Label pressureTC;
    @FXML
    ProgressBar progressTM;
    @FXML
    Label measureTM;
    @FXML
    Label pressureTM;
    @FXML
    ProgressBar progressUR;
    @FXML
    Label measureUR;
    @FXML
    Label pressureUR;
    @FXML
    Label actualSpeed;
    @FXML
    Label allowableSpeed;
    @FXML
    Label cruiseControlSpeed;
    @FXML
    Polygon arrow;
    @FXML
    Pane speedPane;
    @FXML
    Arc redArc;
    @FXML
    Arc yellowArc;
    @FXML
    Rectangle power;
    DisplayData displayData = new DisplayData();
    SpeedometerBilM speedometerBilM;

    @Override
    public void initialize() {
        this.speedometerBilM = new SpeedometerBilM(this.arrow, this.redArc, this.yellowArc, this.allowableSpeed, this.cruiseControlSpeed, this.actualSpeed, this.speedPane);
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
                this.power.setVisible(!this.displayData.isPower());
                this.infoStr.setText(this.displayData.getInfoStr());
                this.valueStr.setText(this.displayData.getValueStr());
                this.wayNum.setText(this.displayData.getWayNum());
                this.worldTime.setText(this.displayData.getWorldTime());
                this.speedometerBilM.setActualSpeed(this.displayData.getActualSpeed());
                this.speedometerBilM.setAllowableSpeed(this.displayData.getAllowableSpeed());
                this.speedometerBilM.setTargetSpeed(this.displayData.getTargetSpeed());
                this.actualSpeed.setVisible(this.displayData.isVisiblyActualSpeed());
                this.measureUR.setText(this.displayData.getDimensionOfTheProgressBar());
                this.measureTM.setText(this.displayData.getDimensionOfTheProgressBar());
                this.measureTC.setText(this.displayData.getDimensionOfTheProgressBar());
                this.pressureUR.setText(this.displayData.getPressureUR());
                this.pressureTM.setText(this.displayData.getPressureTM());
                this.pressureTC.setText(this.displayData.getPressureTC());
                this.progressUR.setProgress(this.displayData.getProgressUR());
                this.progressTM.setProgress(this.displayData.getProgressTM());
                this.progressTC.setProgress(this.displayData.getProgressTC());
                this.updateReverse(this.displayData.getReverse());
                this.targetCode.setText(this.displayData.getTargetType());
                this.distToTarget.setText(this.displayData.getDistanceToTarget() + (this.displayData.getDistanceToTarget().length() > 0 ? "" : "\u043c"));
                this.targetName.setText(this.displayData.getTargetName());
                this.coordinateGps.setText(this.displayData.getCoordinate());
                this.acceleration.setText(this.displayData.getAcceleration());
                this.distanceToSaut.setText(this.displayData.getDistanceToSaut() + (this.displayData.getDistanceToSaut().length() > 0 ? "" : "\u043c"));
                this.brakingCoefficient.setText(this.displayData.getBrakingCoefficient());
                this.roadBias.setText(this.displayData.getRoadBias() + (this.displayData.getRoadBias().length() > 0 ? "" : "\u2030"));
                this.stationName.setText(this.displayData.getStationName());
                this.cassetteEquipped.setVisible(this.displayData.isCassetteEquipped());
                this.updateTokorazdelOrNeutralInsert();
                this.attention.setFill(this.displayData.isVisiblyAttention() ? Color.RED : Color.BLACK);
                this.releaseBan.setVisible(this.displayData.isVisiblyReleaseBan());
                this.serviceBraking.setVisible(this.displayData.isVisiblyServiceBraking());
                this.channel.setText(this.displayData.getChannel());
                this.trainMode.setText(this.displayData.getTrainMode());
                this.tskbmPower.setVisible(this.displayData.isVisiblyTskbmPower());
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

    private void updateTokorazdelOrNeutralInsert() {
        if (this.displayData.isTokorazdel()) {
            if (this.neutralInsert.isVisible()) {
                this.neutralInsert.setVisible(false);
            }
            if (!this.tokorazdel.isVisible()) {
                this.tokorazdel.setVisible(true);
                this.distanceTokorazdelOrNeutralInsert.setVisible(true);
            }
        } else if (!this.displayData.isTokorazdel() && this.displayData.isNeutralInsert()) {
            if (this.tokorazdel.isVisible()) {
                this.tokorazdel.setVisible(false);
            }
            if (!this.neutralInsert.isVisible()) {
                this.neutralInsert.setVisible(true);
                this.distanceTokorazdelOrNeutralInsert.setVisible(true);
            }
        } else {
            if (this.tokorazdel.isVisible()) {
                this.tokorazdel.setVisible(false);
                this.distanceTokorazdelOrNeutralInsert.setVisible(false);
            }
            if (this.neutralInsert.isVisible()) {
                this.neutralInsert.setVisible(false);
                this.distanceTokorazdelOrNeutralInsert.setVisible(false);
            }
        }
        this.distanceTokorazdelOrNeutralInsert.setText(this.displayData.getDistanceTokorazdelOrNeutralInsert() + (this.displayData.getDistanceTokorazdelOrNeutralInsert().length() > 0 ? "" : "\u041c"));
    }
}

