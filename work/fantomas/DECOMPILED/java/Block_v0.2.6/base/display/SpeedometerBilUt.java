/*
 * Decompiled with CFR 0.152.
 */
package base.display;

import javafx.scene.control.Label;
import javafx.scene.shape.Arc;

public class SpeedometerBilUt {
    private static final double MAX_BLACK_LENGTH = 300.5;
    private static final double STEP_LIMITS_ANGLE = 9.5;
    private final Arc redArc;
    private final Arc yellowArc;
    private final Arc blackGArc;
    private final Arc blackRArc;
    private final Arc blackYArc;
    private final Label allowedSpeed;
    private final Label actualSpeed;

    public SpeedometerBilUt(Arc redArc, Arc yellowArc, Arc blackGArc, Arc blackRArc, Arc blackYArc, Label allowedSpeed, Label actualSpeed) {
        this.redArc = redArc;
        this.yellowArc = yellowArc;
        this.blackGArc = blackGArc;
        this.blackRArc = blackRArc;
        this.blackYArc = blackYArc;
        this.allowedSpeed = allowedSpeed;
        this.actualSpeed = actualSpeed;
    }

    private void setSpeedInFormat(Label label, int speed) {
        String formattedSpeed = String.format("%03d", speed);
        label.setText(formattedSpeed);
    }

    public void setActualSpeed(float speed) {
        this.setSpeedInFormat(this.actualSpeed, (int)speed);
        this.setLengthSpeed(speed);
    }

    public void setAllowableSpeed(float speed) {
        this.setSpeedInFormat(this.allowedSpeed, (int)speed);
        this.rotateArc(this.redArc, this.blackRArc, speed);
    }

    public void setTargetSpeed(int speed) {
        this.rotateArc(this.yellowArc, this.blackYArc, speed);
    }

    private void rotateArc(Arc arc, Arc blackArc, double speed) {
        double steps = Math.round(speed / 5.0);
        double length1 = 300.5 - steps * 9.5;
        double length2 = 300.5 - (steps - 1.0) * 9.5;
        if (length1 < 0.0) {
            length1 = 0.0;
        }
        if (length2 < 0.0) {
            length2 = 0.0;
        }
        arc.setLength(length2);
        blackArc.setLength(length1);
    }

    private void setLengthSpeed(double speed) {
        double length = 300.5 - (double)Math.round(speed / 5.0) * 9.5;
        if (length < 0.0) {
            length = 0.0;
        }
        this.blackGArc.setLength(length);
    }
}

