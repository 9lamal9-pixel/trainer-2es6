/*
 * Decompiled with CFR 0.152.
 */
package base.data.configs;

import base.data.IAction;

public class CommandSettingsData {
    private String current_coordinate = "0000000";
    private String table_number = "0";
    private String train_number = "0";
    private String train_length_axis = "0";
    private String train_length_box = "0";
    private String train_mass = "0";
    private String winter_summer = "0";
    private String train_category = "6";
    private String train_type = "0";
    private String loco_number = "0";
    private String diameter_1 = "1050";
    private String diameter_2 = "1050";
    private String teeth_count = "32";
    private String config_number = "0";
    private String speed_to_white = "40";
    private String speed_to_green = "80";
    private String speed_to_yellow = "60";
    private String length_area = "1000";
    private String rasp_sns_1 = "0";
    private String rasp_sns_2 = "0";
    private String mileage = "0";
    private String is_coordinate_change = "0";
    private Boolean increment_chanel = false;
    private Integer value_of_coordinate_change = 0;
    private Integer brightness = 0;
    private String way_number = "1";
    private String correctness_of_way = "0";
    private String serviceable_modules = "- 2 3 4 5 6 7 8 9 - B C";
    private String current_cabin = "\u041a\u0410\u0411\u0418\u041d\u0410 \u0410";
    private String current_bloc = "I";
    private String current_dps = "\u0412512";
    private String current_ek_number = "1234";
    private String current_mode = "\u041f";
    private String alsn_value = "4";
    private transient IAction wayNumberChange = null;
    private transient IAction correctnessOfWayChange = null;
    private transient IAction changeCurrentMode = null;
    private transient IAction trainCategoryChange = null;

    public void setCorrectnessOfWayChange(IAction correctnessOfWayChange) {
        this.correctnessOfWayChange = correctnessOfWayChange;
    }

    public void setWayNumberChange(IAction wayNumberChange) {
        this.wayNumberChange = wayNumberChange;
    }

    public void setChangeCurrentMode(IAction changeCurrentMode) {
        this.changeCurrentMode = changeCurrentMode;
    }

    public void setTrainCategoryChange(IAction trainCategoryChange) {
        this.trainCategoryChange = trainCategoryChange;
    }

    public void invokeTrainCategory() {
        if (this.trainCategoryChange != null) {
            this.trainCategoryChange.Invoke(this.train_category);
        }
    }

    public void invokeWay() {
        if (this.wayNumberChange != null) {
            this.wayNumberChange.Invoke(this.way_number);
        }
        if (this.correctnessOfWayChange != null) {
            this.correctnessOfWayChange.Invoke(this.correctness_of_way);
        }
        if (this.changeCurrentMode != null) {
            this.changeCurrentMode.Invoke(this.current_mode);
        }
    }

    public void setCurrent_coordinate(String current_coordinate) {
        this.current_coordinate = current_coordinate;
    }

    public String getCurrent_coordinate() {
        return this.current_coordinate;
    }

    public void setTable_number(String table_number) {
        this.table_number = table_number;
    }

    public String getTable_number() {
        return this.table_number;
    }

    public void setTrain_number(String train_number) {
        this.train_number = train_number;
    }

    public String getTrain_number() {
        return this.train_number;
    }

    public void setTrain_length_axis(String train_length_axis) {
        this.train_length_axis = train_length_axis;
    }

    public String getTrain_length_axis() {
        return this.train_length_axis;
    }

    public void setTrain_length_box(String train_length_box) {
        this.train_length_box = train_length_box;
    }

    public String getTrain_length_box() {
        return this.train_length_box;
    }

    public void setTrain_mass(String train_mass) {
        this.train_mass = train_mass;
    }

    public String getTrain_mass() {
        return this.train_mass;
    }

    public void setWinter_summer(String winter_summer) {
        this.winter_summer = winter_summer;
    }

    public String getWinter_summer() {
        return this.winter_summer;
    }

    public void setTrain_category(String train_category) {
        this.train_category = train_category;
        if (this.trainCategoryChange != null) {
            this.trainCategoryChange.Invoke(train_category);
        }
    }

    public String getTrain_category() {
        return this.train_category;
    }

    public void setTrain_type(String train_type) {
        this.train_type = train_type;
    }

    public String getTrain_type() {
        return this.train_type;
    }

    public void setLoco_number(String loco_number) {
        this.loco_number = loco_number;
    }

    public String getLoco_number() {
        return this.loco_number;
    }

    public void setDiameter_1(String diameter_1) {
        this.diameter_1 = diameter_1;
    }

    public String getDiameter_1() {
        return this.diameter_1;
    }

    public void setDiameter_2(String diameter_2) {
        this.diameter_2 = diameter_2;
    }

    public String getDiameter_2() {
        return this.diameter_2;
    }

    public void setTeeth_count(String teeth_count) {
        this.teeth_count = teeth_count;
    }

    public String getTeeth_count() {
        return this.teeth_count;
    }

    public void setConfig_number(String config_number) {
        this.config_number = config_number;
    }

    public String getConfig_number() {
        return this.config_number;
    }

    public void setSpeed_to_white(String speed_to_white) {
        this.speed_to_white = speed_to_white;
    }

    public String getSpeed_to_white() {
        return this.speed_to_white;
    }

    public void setSpeed_to_green(String speed_to_green) {
        this.speed_to_green = speed_to_green;
    }

    public String getSpeed_to_green() {
        return this.speed_to_green;
    }

    public void setSpeed_to_yellow(String speed_to_yellow) {
        this.speed_to_yellow = speed_to_yellow;
    }

    public String getSpeed_to_yellow() {
        return this.speed_to_yellow;
    }

    public void setLength_area(String length_area) {
        this.length_area = length_area;
    }

    public String getLength_area() {
        return this.length_area;
    }

    public void setRasp_sns_1(String rasp_sns_1) {
        this.rasp_sns_1 = rasp_sns_1;
    }

    public String getRasp_sns_1() {
        return this.rasp_sns_1;
    }

    public void setRasp_sns_2(String rasp_sns_2) {
        this.rasp_sns_2 = rasp_sns_2;
    }

    public String getRasp_sns_2() {
        return this.rasp_sns_2;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getMileage() {
        return this.mileage;
    }

    public void setIs_coordinate_change(String is_coordinate_change) {
        this.is_coordinate_change = is_coordinate_change;
    }

    public String getIs_coordinate_change() {
        return this.is_coordinate_change;
    }

    public void setIncrement_chanel(Boolean increment_chanel) {
        this.increment_chanel = increment_chanel;
    }

    public Boolean getIncrement_chanel() {
        return this.increment_chanel;
    }

    public void setValue_of_coordinate_change(Integer value_of_coordinate_change) {
        this.value_of_coordinate_change = value_of_coordinate_change;
    }

    public Integer getValue_of_coordinate_change() {
        return this.value_of_coordinate_change;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public Integer getBrightness() {
        return this.brightness;
    }

    public void setWay_number(String way_number) {
        this.way_number = way_number;
        if (this.wayNumberChange != null) {
            this.wayNumberChange.Invoke(way_number);
        }
    }

    public String getWay_number() {
        return this.way_number;
    }

    public void setCorrectness_of_way(String correctness_of_way) {
        this.correctness_of_way = correctness_of_way;
        if (this.correctnessOfWayChange != null) {
            this.correctnessOfWayChange.Invoke(correctness_of_way);
        }
    }

    public String getCorrectness_of_way() {
        return this.correctness_of_way;
    }

    public void setServiceable_modules(String serviceable_modules) {
        this.serviceable_modules = serviceable_modules;
    }

    public String getServiceable_modules() {
        return this.serviceable_modules;
    }

    public void setCurrent_cabin(String current_cabin) {
        this.current_cabin = current_cabin;
    }

    public String getCurrent_cabin() {
        return this.current_cabin;
    }

    public void setCurrent_bloc(String current_bloc) {
        this.current_bloc = current_bloc;
    }

    public String getCurrent_bloc() {
        return this.current_bloc;
    }

    public void setCurrent_dps(String current_dps) {
        this.current_dps = current_dps;
    }

    public String getCurrent_dps() {
        return this.current_dps;
    }

    public void setCurrent_ek_number(String current_ek_number) {
        this.current_ek_number = current_ek_number;
    }

    public String getCurrent_ek_number() {
        return this.current_ek_number;
    }

    public void setCurrent_mode(String current_mode) {
        this.current_mode = current_mode;
        if (this.changeCurrentMode != null) {
            this.changeCurrentMode.Invoke(current_mode);
        }
    }

    public String getCurrent_mode() {
        return this.current_mode;
    }

    public void setAlsn_value(String alsn_value) {
        this.alsn_value = alsn_value;
    }

    public String getAlsn_value() {
        return this.alsn_value;
    }
}

