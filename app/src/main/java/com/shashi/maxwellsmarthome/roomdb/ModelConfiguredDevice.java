package com.shashi.maxwellsmarthome.roomdb;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "device_config")
public class ModelConfiguredDevice {

    @PrimaryKey(autoGenerate = true) int id;
    String name;
    String ip;
    String bssid;

    @Ignore
    public ModelConfiguredDevice(int id, String name, String ip, String bssid) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.bssid = bssid;
    }

    public ModelConfiguredDevice(String name, String ip, String bssid) {
        this.name = name;
        this.ip = ip;
        this.bssid = bssid;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getBssid() {
        return bssid;
    }
}
