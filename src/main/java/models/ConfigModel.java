package models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.sql.Date;


public class ConfigModel {

    @SerializedName("settingId")
    @Expose
    public int id;

    @SerializedName("temperatureSetpoint")
    @Expose
    public int tempSetpoint;

    @SerializedName("ppmMin")
    @Expose
    public int co2Min;

    @SerializedName("ppmMax")
    @Expose
    public int co2Max;

    @SerializedName("deviceEUI")
    @Expose
    public String eui;

    public ConfigModel(){}
}
