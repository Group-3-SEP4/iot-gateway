package models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


public class TeracomModel {

    @SerializedName("total")
    @Expose
    public Integer total;

    @SerializedName("perPage")
    @Expose
    public Integer perPage;
    @SerializedName("cmd")
    @Expose
    public String cmd;
    @SerializedName("page")
    @Expose
    public Integer page;

    @SerializedName("rssi")
    @Expose
    public Integer rssi;
    @SerializedName("snr")
    @Expose
    public Float snr;
    @SerializedName("ant")
    @Expose
    public Integer ant;
    @SerializedName("tmms")
    @Expose
    public Integer tmms;
    @SerializedName("gweui")
    @Expose
    public String gweui;
    @SerializedName("lon")
    @Expose
    public Float lon;
    @SerializedName("time")
    @Expose
    public String time;
    @SerializedName("lat")
    @Expose
    public Float lat;
    @SerializedName("ts")
    @Expose
    public String ts; // TODO: Change this to sql TimeStamp
    @SerializedName("cache")
    @Expose
    public TeracomModel[] cache;
    @SerializedName("seqno")
    @Expose
    public Integer seqno;
    @SerializedName("data")
    @Expose
    public String data;
    @SerializedName("toa")
    @Expose
    public Integer toa;
    @SerializedName("freq")
    @Expose
    public Integer freq;
    @SerializedName("ack")
    @Expose
    public Boolean ack;
    @SerializedName("fcnt")
    @Expose
    public Integer fcnt;
    @SerializedName("dr")
    @Expose
    public String dr;
    @SerializedName("bat")
    @Expose
    public Integer bat;
    @SerializedName("port")
    @Expose
    public Integer port;
    @SerializedName("EUI")
    @Expose
    public String eUI;

    @SerializedName("sessionKeyId")
    @Expose
    public Object sessionKeyId;


    public TeracomModel() {
    }
}