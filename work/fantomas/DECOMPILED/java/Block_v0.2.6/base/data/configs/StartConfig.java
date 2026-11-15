/*
 * Decompiled with CFR 0.152.
 */
package base.data.configs;

import base.data.BIL_MODEL;
import base.data.configs.DebugData;
import base.data.configs.SpeedometerData;
import base.data.configs.WindowPositionResolutionData;
import com.google.gson.annotations.SerializedName;

public class StartConfig {
    @SerializedName(value="server_port")
    private final int serverPort;
    @SerializedName(value="window_position")
    private final WindowPositionResolutionData windowPositionResolutionData = new WindowPositionResolutionData();
    private final SpeedometerData speedometer = new SpeedometerData();
    private final DebugData debug = new DebugData();
    @SerializedName(value="bil_model")
    private int bilModel = 0;

    public StartConfig() {
        this.serverPort = 2002;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public WindowPositionResolutionData getWindowPositionResolutionData() {
        return this.windowPositionResolutionData;
    }

    public SpeedometerData getSpeedometer() {
        return this.speedometer;
    }

    public DebugData getDebug() {
        return this.debug;
    }

    public BIL_MODEL getBilModel() {
        if (this.bilModel < 0 || this.bilModel > BIL_MODEL.values().length) {
            this.bilModel = 0;
            return BIL_MODEL.BIL_M;
        }
        return BIL_MODEL.values()[this.bilModel];
    }
}

