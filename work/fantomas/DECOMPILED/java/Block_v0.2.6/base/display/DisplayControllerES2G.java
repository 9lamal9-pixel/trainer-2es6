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
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DisplayControllerES2G
implements DisplayController {
    private static final Logger logger = LogManager.getLogger(DisplayControllerES2G.class);
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
    Rectangle moveStop;
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
    Label measureTC;
    @FXML
    Label pressureTC;
    @FXML
    Label measureTM;
    @FXML
    Label pressureTM;
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
    @FXML
    Pane paneChart;
    DisplayData displayData = new DisplayData();
    SpeedometerBilM speedometerBilM;
    NumberAxis xAxis = new NumberAxis(0.0, 6000.0, 1000.0);
    NumberAxis yAxis = new NumberAxis(0.0, 50.0, 10.0);
    private final XYChart.Series<Number, Number> trainCoord = new XYChart.Series();
    private final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(this.xAxis, this.yAxis);

    @Override
    public void initialize() {
        this.speedometerBilM = new SpeedometerBilM(this.arrow, this.redArc, this.yellowArc, this.allowableSpeed, this.cruiseControlSpeed, this.actualSpeed, this.speedPane);
        this.xAxis.setTickLabelsVisible(false);
        this.yAxis.setTickLabelsVisible(false);
        this.lineChart.setLayoutX(-16.0);
        this.lineChart.setLayoutY(29.0);
        this.lineChart.setPrefSize(this.paneChart.getPrefWidth() + 29.0, this.paneChart.getPrefHeight());
        this.lineChart.setLegendVisible(false);
        this.lineChart.setAnimated(false);
        this.paneChart.getChildren().add(this.lineChart);
        logger.debug("init");
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
                this.updateTrainPos(this.displayData.getVideoCoord());
                this.lineChart.setVisible(this.displayData.isVisiblyChart());
            });
        }
    }

    @Override
    public void reinitializeCharts() {
        Platform.runLater(() -> {
            XYChart.Data<Integer, Integer> data;
            Label label;
            int i;
            this.lineChart.getData().clear();
            XYChart.Series series = new XYChart.Series();
            for (int i2 = 0; i2 < BlockData.getInstance().getRoadData().lights.length; ++i2) {
                Text icon = new Text("\u042e");
                icon.setFont(Font.font("Arial", FontWeight.BOLD, 28.0));
                icon.setFill(Color.WHITE);
                Text text = new Text(BlockData.getInstance().getRoadData().lights[i2].name);
                text.setFont(Font.font("Arial", FontWeight.BOLD, 18.0));
                text.setFill(Color.WHITE);
                VBox pane = new VBox();
                pane.setStyle("-fx-background-color: transparent;");
                pane.getChildren().addAll((Node[])new Node[]{text, icon});
                pane.setTranslateX(10.0);
                XYChart.Data<Integer, Double> data2 = new XYChart.Data<Integer, Double>(BlockData.getInstance().getRoadData().lights[i2].coord, 16.5);
                data2.setNode(pane);
                series.getData().add(data2);
            }
            this.lineChart.getData().add(series);
            series = new XYChart.Series();
            int km = -1;
            for (i = 0; i < BlockData.getInstance().getRoadData().pikets.length; ++i) {
                if (km == BlockData.getInstance().getRoadData().pikets[i].km) continue;
                km = BlockData.getInstance().getRoadData().pikets[i].km;
                label = new Label(BlockData.getInstance().getRoadData().pikets[i].km + " \u043a\u043c");
                label.setFont(Font.font("Arial", FontWeight.BOLD, 16.0));
                label.setTextFill(Color.WHITE);
                label.setPrefWidth(169.0);
                label.setTranslateX(label.getPrefWidth() / 2.0);
                label.getStyleClass().add("registration");
                label.setStyle("-fx-background-color: transparent;");
                data = new XYChart.Data<Integer, Integer>(BlockData.getInstance().getRoadData().pikets[i].coord, 7);
                data.setNode(label);
                series.getData().add(data);
            }
            this.lineChart.getData().add(series);
            series = new XYChart.Series();
            for (i = 0; i < BlockData.getInstance().getRoadData().stations.length; ++i) {
                label = new Label(BlockData.getInstance().getRoadData().stations[i].name);
                label.setFont(Font.font("Arial", FontWeight.BOLD, 16.0));
                label.setTextFill(Color.WHITE);
                label.setPrefWidth(169.0);
                label.setTranslateX(label.getPrefWidth() / 2.0);
                label.getStyleClass().add("registration");
                label.setStyle("-fx-background-color: transparent;");
                data = new XYChart.Data<Integer, Integer>(BlockData.getInstance().getRoadData().stations[i].coord, 45);
                data.setNode(label);
                series.getData().add(data);
                data = new XYChart.Data<Integer, Integer>(BlockData.getInstance().getRoadData().stations[i].coord, 45);
                Line line = new Line();
                line.setEndX(0.0);
                line.setEndY(340.0);
                line.getStyleClass().add("station-line");
                data.setNode(line);
                series.getData().add(data);
            }
            this.lineChart.getData().add(series);
            this.lineChart.getData().add(this.trainCoord);
        });
    }

    private void updateTrainPos(int value) {
        this.trainCoord.getData().clear();
        XYChart.Data<Integer, Integer> data = new XYChart.Data<Integer, Integer>(value, 16);
        Line line = new Line();
        line.setEndX(0.0);
        line.setEndY(100.0);
        line.getStyleClass().add("train-line");
        data.setNode(line);
        this.trainCoord.getData().add(data);
        this.xAxis.setLowerBound(value - 127);
        this.xAxis.setUpperBound(value - 127 + 6000);
    }

    private void updateReverse(int value) {
        switch (value) {
            case 1: {
                if (this.moveStop.isVisible()) {
                    this.moveStop.setVisible(false);
                }
                if (!this.moveForward.isVisible()) {
                    this.moveForward.setVisible(true);
                }
                if (!this.moveBack.isVisible()) break;
                this.moveBack.setVisible(false);
                break;
            }
            case 2: {
                if (this.moveStop.isVisible()) {
                    this.moveStop.setVisible(false);
                }
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
                if (this.moveBack.isVisible()) {
                    this.moveBack.setVisible(false);
                }
                if (this.moveStop.isVisible()) break;
                this.moveStop.setVisible(true);
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

