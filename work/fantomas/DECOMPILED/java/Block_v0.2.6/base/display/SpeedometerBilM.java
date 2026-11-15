/*
 * Decompiled with CFR 0.152.
 */
package base.display;

import base.data.BlockData;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class SpeedometerBilM {
    private static final double SPEEDOMETER_RANGE = 270.0;
    private static final double SPEEDOMETER_ARC_ROTATE_ANGLE = 135.0;
    private static final short TICK_LENGTH = 16;
    private static final short TICK_CIRCLE_RADIUS = 190;
    private static final short TICK_CIRCLE_PIVOT = 170;
    private static final short NUMBER_CIRCLE_RADIUS = 160;
    private static final short NUMBER_CIRCLE_PIVOT_X = 162;
    private static final short NUMBER_CIRCLE_PIVOT_Y = 155;
    private final Polygon arrow;
    private final Arc redArc;
    private final Arc yellowArc;
    private final Label allowedSpeed;
    private final Label cruise;
    private final Label currentSpeed;
    private final Pane speedPane;
    private final int maxValue;
    private final int minValue;
    private final int ticks;
    private final int stepValue;
    private int previousState = 0;

    public SpeedometerBilM(Polygon arrow, Arc redArc, Arc yellowArc, Label allowedSpeed, Label cruise, Label currentSpeed, Pane speedPane) {
        this.arrow = arrow;
        this.redArc = redArc;
        this.yellowArc = yellowArc;
        this.allowedSpeed = allowedSpeed;
        this.cruise = cruise;
        this.currentSpeed = currentSpeed;
        this.speedPane = speedPane;
        this.minValue = BlockData.getInstance().getStartConfig().getSpeedometer().getMinValue();
        this.ticks = BlockData.getInstance().getStartConfig().getSpeedometer().getTicks();
        this.stepValue = BlockData.getInstance().getStartConfig().getSpeedometer().getStepValue();
        this.maxValue = this.minValue + this.stepValue * this.ticks;
        this.movePivot(0.0, 100.0);
        this.drawSpeedometerTicks();
    }

    private double positionToAngle(double position) {
        return (position - (double)this.minValue) / (double)(this.maxValue - this.minValue) * 270.0;
    }

    private void rotateArc(Arc arc, int position) {
        arc.setLength(this.positionToAngle(this.maxValue - position) - 2.0);
    }

    private void setSpeedInFormat(Label label, int speed) {
        String formattedSpeed = String.format("%03d", speed);
        label.setText(formattedSpeed);
    }

    private void rotateArrow(Polygon polygon, int speed) {
        double angle = this.positionToAngle(speed) - 135.0;
        RotateTransition rt = new RotateTransition(Duration.millis(30.0), polygon);
        rt.setToAngle(angle);
        rt.play();
    }

    private void drawSpeedometerTicks() {
        Group linesGroup = new Group();
        double degreeStep = 270.0 / (double)this.ticks;
        double i = 0.0;
        double value = this.minValue;
        while (i <= 270.0) {
            double cos = Math.cos(Math.toRadians(i + 135.0));
            double sin = Math.sin(Math.toRadians(i + 135.0));
            double x = cos * 190.0 + 170.0;
            double y = sin * 190.0 + 170.0;
            double innerX = cos * 160.0 + 162.0;
            double innerY = sin * 160.0 + 155.0;
            Line line = new Line(x, y, x + 16.0, y);
            line.setStroke(Color.WHITE);
            line.setRotate(i + 135.0);
            Label text = new Label(Integer.toString((int)value));
            text.setTextFill(Color.WHITE);
            text.setLayoutX(innerX);
            text.setLayoutY(innerY);
            linesGroup.getChildren().addAll((Node[])new Node[]{line, text});
            i += degreeStep;
            value += (double)this.stepValue;
        }
        this.speedPane.getChildren().add(linesGroup);
    }

    private void movePivot(double x, double y) {
        this.arrow.getTransforms().add(new Translate(-x, -y));
        this.arrow.setTranslateX(x);
        this.arrow.setTranslateY(y);
    }

    public void setCruiseControl(int state, int value) {
        this.setSpeedInFormat(this.cruise, value);
        if (state == this.previousState) {
            return;
        }
        this.previousState = state;
    }

    public void setActualSpeed(int speed) {
        this.setSpeedInFormat(this.currentSpeed, speed);
        this.rotateArrow(this.arrow, speed);
    }

    public void setAllowableSpeed(int speed) {
        this.setSpeedInFormat(this.allowedSpeed, speed);
        this.rotateArc(this.redArc, speed);
    }

    public void setTargetSpeed(int speed) {
        this.rotateArc(this.yellowArc, speed);
    }
}

