package com.shashi.maxwellsmarthome.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoDevice {

    @Query("SELECT * FROM device_config ORDER BY ID")
    List<ModelConfiguredDevice> loadAllDevice();

    @Insert
    void insertDevice(ModelConfiguredDevice device);

    @Update
    void updateDevice(ModelConfiguredDevice device);

    @Delete
    void deleteDevice(ModelConfiguredDevice device);

    @Query("SELECT * FROM device_config WHERE id = :id")
    ModelConfiguredDevice loadDeviceById(int id);
}
