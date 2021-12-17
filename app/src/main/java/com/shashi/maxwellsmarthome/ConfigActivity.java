package com.shashi.maxwellsmarthome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        ConnectWifiInstructionFragment connectWifiInstructionFragment = new ConnectWifiInstructionFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fcv_config, connectWifiInstructionFragment)
                .commit();

    }

}