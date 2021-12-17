package com.shashi.maxwellsmarthome.roomdb;

import java.util.List;

public class DbUtil {

    public static void insertDeviceToLocal(AppDatabase mDb, ModelConfiguredDevice modelConfiguredDevice) {
        AppExecutors.getInstance().diskIO().execute(() -> mDb.daoDevice().insertDevice(modelConfiguredDevice));
    }

    public static void updateToAdminLocal(AppDatabase mDb, ModelConfiguredDevice modelConfiguredDevice) {
        AppExecutors.getInstance().diskIO().execute(() -> mDb.daoDevice().updateDevice(modelConfiguredDevice));
    }


    public static void readAll(AppDatabase mDb, ReadLocalDb readLocalDb) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            final List<ModelConfiguredDevice> devices = mDb.daoDevice().loadAllDevice();
            readLocalDb.onReadAdmin(devices);
        });
    }


    public interface ReadLocalDb{
        void onReadAdmin(List<ModelConfiguredDevice> deviceList);
    }
}
