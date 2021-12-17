package com.shashi.maxwellsmarthome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shashi.maxwellsmarthome.roomdb.AppDatabase;
import com.shashi.maxwellsmarthome.roomdb.DbUtil;
import com.shashi.maxwellsmarthome.roomdb.ModelConfiguredDevice;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AppDatabase mdb;
    private LinearLayout llAddDevice;
    private CardView cardViewDevice;
    private TextView tvIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAddDevice = findViewById(R.id.btn_add_device);
        btnAddDevice.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ConfigActivity.class));
        });

        mdb = AppDatabase.getInstance(this);

        llAddDevice = findViewById(R.id.ll_add_device);
        cardViewDevice = findViewById(R.id.cv_main_device);
        tvIp = findViewById(R.id.tv_ip_main);



    }

    @Override
    protected void onResume() {
        super.onResume();

        DbUtil.readAll(mdb, deviceList -> {
            if (deviceList.size() != 0) {
                ModelConfiguredDevice modelConfiguredDevice = null;
                for (ModelConfiguredDevice device: deviceList) {
                    modelConfiguredDevice = device;
//                    Log.d(TAG, "onResume: " + modelConfiguredDevice.getIp());
                }

                final ModelConfiguredDevice device = modelConfiguredDevice;

                runOnUiThread(() -> {
                    llAddDevice.setVisibility(View.GONE);
                    cardViewDevice.setVisibility(View.VISIBLE);
//                    tvIp.setText(device.getId());
                });
            }
        });

        tvIp.setText("192.168.0.0");
    }
}