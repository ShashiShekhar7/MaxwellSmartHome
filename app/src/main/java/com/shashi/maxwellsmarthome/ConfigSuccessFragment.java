package com.shashi.maxwellsmarthome;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.shashi.maxwellsmarthome.roomdb.AdapterConfigureSuccess;
import com.shashi.maxwellsmarthome.roomdb.ModelConfiguredDevice;

import java.util.ArrayList;
import java.util.List;

public class ConfigSuccessFragment extends Fragment {

    private static final String TAG = "ConfigSuccessFrag";

    private final ModelConfiguredDevice modelConfiguredDevice;

    public ConfigSuccessFragment(ModelConfiguredDevice modelConfiguredDevice) {
        this.modelConfiguredDevice = modelConfiguredDevice;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_config_success, container, false);

        List<ModelConfiguredDevice> modelConfiguredDevices = new ArrayList<>();
        if (modelConfiguredDevices != null)
            modelConfiguredDevices.add(modelConfiguredDevice);

        Log.d(TAG, "onCreateView: name : " + modelConfiguredDevice.getName());
        Log.d(TAG, "onCreateView: bssid : " + modelConfiguredDevice.getBssid());
        Log.d(TAG, "onCreateView: ip : " + modelConfiguredDevice.getIp());

        AdapterConfigureSuccess adapterConfigureSuccess = new AdapterConfigureSuccess(modelConfiguredDevices);
        RecyclerView recyclerView = view.findViewById(R.id.rv_config_success);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapterConfigureSuccess);

        Button button = view.findViewById(R.id.btn_config_success);
        button.setOnClickListener(view1 -> {
            getActivity().finish();
        });

        return view;
    }
}