package com.shashi.maxwellsmarthome;

import static android.content.Context.WIFI_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.InetAddresses;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch2.provision.TouchNetUtil;
import com.shashi.maxwellsmarthome.roomdb.AppDatabase;
import com.shashi.maxwellsmarthome.roomdb.DbUtil;
import com.shashi.maxwellsmarthome.roomdb.ModelConfiguredDevice;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ConnectWifiInstructionFragment extends Fragment {
    private static final String TAG = "ConnectWifi";

    Button btnStartConfig;

    private WifiManager mWifiManager;
    AddingDeviceDialogFragment addingDeviceDialogFragment;

    ModelConfiguredDevice modelConfiguredDevice;

    AppDatabase mdb;

    private String mSsid;
    private byte[] mSsidBytes;
    private String mBssid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connect_wifi_instruction, container, false);

        mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(WIFI_SERVICE);

        mdb = AppDatabase.getInstance(getContext());

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                Log.e("activityResultLauncher", ""+result.toString());
                Boolean areAllGranted = true;
                for(Boolean b : result.values()) {
                    areAllGranted = areAllGranted && b;
                }
                if(areAllGranted) {
                    onWifiChanged(view);
                }
            }
        }).launch(permissions);

        MaxwellSmartHomeApp.getInstance().observeBroadcast(this, broadcast -> {
            Log.d(TAG, "onCreate: Broadcast = " + broadcast);
            onWifiChanged(view);
        });

        btnStartConfig = view.findViewById(R.id.btn_start_config);
        EditText etPassword = view.findViewById(R.id.et_connected_password);

        btnStartConfig.setOnClickListener(view1 -> {
            String pass = etPassword.getText().toString();
            if (pass.isEmpty())
                etPassword.setError("Can't be empty.");
            else if (pass.length() < 8)
                etPassword.setError("Short password.");
            else {
                addingDeviceDialogFragment = new AddingDeviceDialogFragment();
                addingDeviceDialogFragment.show(getChildFragmentManager(), "AddingDeviceDialogFragment");
                executeEsptouch(pass);
            }
        });

        return view;
    }


    private void onWifiChanged(View view) {
        ModalWifiInfo modalWifiInfo = check();

        mSsid = modalWifiInfo.ssid;
        mBssid = modalWifiInfo.bssid;
        mSsidBytes = modalWifiInfo.ssidBytes;
        CharSequence message = modalWifiInfo.message;
        boolean confirmEnable = false;
        if (modalWifiInfo.wifiConnected) {
            confirmEnable = true;
            if (modalWifiInfo.is5G) {
                message = getString(R.string.esptouch1_wifi_5g_message);
            }
        }


        TextView tvConnectedSSID = view.findViewById(R.id.tv_connected_ssid);

        tvConnectedSSID.setText(mSsid);
    }

    private ModalWifiInfo check() {
        ModalWifiInfo result = checkPermission();
        if (!result.permissionGranted) {
            return result;
        }
        result = checkLocation();
        result.permissionGranted = true;
        if (result.locationRequirement) {
            return result;
        }
        result = checkWifi();
        result.permissionGranted = true;
        result.locationRequirement = false;
        return result;
    }

    protected ModalWifiInfo checkPermission() {
        ModalWifiInfo result = new ModalWifiInfo();
        result.permissionGranted = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean locationGranted = getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
            if (!locationGranted) {
                String[] splits = getString(R.string.esptouch_message_permission).split("\n");
                if (splits.length != 2) {
                    throw new IllegalArgumentException("Invalid String @RES esptouch_message_permission");
                }
                SpannableStringBuilder ssb = new SpannableStringBuilder(splits[0]);
                ssb.append('\n');
                SpannableString clickMsg = new SpannableString(splits[1]);
                ForegroundColorSpan clickSpan = new ForegroundColorSpan(0xFF0022FF);
                clickMsg.setSpan(clickSpan, 0, clickMsg.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                ssb.append(clickMsg);
                result.message = ssb;
                return result;
            }
        }

        result.permissionGranted = true;
        return result;
    }

    protected ModalWifiInfo checkLocation() {
        ModalWifiInfo result = new ModalWifiInfo();
        result.locationRequirement = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager manager = getContext().getSystemService(LocationManager.class);
            boolean enable = manager != null && LocationManagerCompat.isLocationEnabled(manager);
            if (!enable) {
                result.message = getString(R.string.esptouch_message_location);
                return result;
            }
        }

        result.locationRequirement = false;
        return result;
    }

    protected ModalWifiInfo checkWifi() {
        ModalWifiInfo result = new ModalWifiInfo();
        result.wifiConnected = false;
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        boolean connected = TouchNetUtil.isWifiConnected(mWifiManager);
        if (!connected) {
            result.message = getString(R.string.esptouch_message_wifi_connection);
            return result;
        }

        String ssid = TouchNetUtil.getSsidString(wifiInfo);
        int ipValue = wifiInfo.getIpAddress();
        if (ipValue != 0) {
            result.address = TouchNetUtil.getAddress(wifiInfo.getIpAddress());
        } else {
            result.address = TouchNetUtil.getIPv4Address();
            if (result.address == null) {
                result.address = TouchNetUtil.getIPv6Address();
            }
        }

        result.wifiConnected = true;
        result.message = "";
        result.is5G = TouchNetUtil.is5G(wifiInfo.getFrequency());
        if (result.is5G) {
            result.message = getString(R.string.esptouch_message_wifi_frequency);
        }
        result.ssid = ssid;
        result.ssidBytes = TouchNetUtil.getRawSsidBytesOrElse(wifiInfo, ssid.getBytes());
        result.bssid = wifiInfo.getBSSID();

        return result;
    }

    private void executeEsptouch(String pwdStr) {
        byte[] ssid = mSsidBytes == null ? ByteUtil.getBytesByString(this.mSsid)
                : mSsidBytes;
        byte[] password = pwdStr == null ? null : ByteUtil.getBytesByString(pwdStr);
        byte[] bssid = com.espressif.iot.esptouch.util.TouchNetUtil.parseBssid2bytes(this.mBssid);

        startEspConfig(ssid, bssid, password);
    }

    private void startEspConfig(byte[] ssid, byte[] bssid, byte[] password) {
        Observable.fromCallable(new Callable<List<IEsptouchResult>>() {
            @Override
            public List<IEsptouchResult> call() throws Exception {
                int expectResultCount = 1;
                Context context = getContext();
                EsptouchTask task = new EsptouchTask(ssid, bssid, password, context);
                task.setPackageBroadcast(false);

                return task.executeForResults(expectResultCount);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<IEsptouchResult>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        Log.d(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull List<IEsptouchResult> iEsptouchResults) {

                        IEsptouchResult result = iEsptouchResults.get(0);
                        Log.d(TAG, "onNext: " + result.toString());

                        if (result.isSuc()) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    InetAddress inetAddress = result.getInetAddress();

                                    String ip = inetAddress.getHostAddress();
                                    String name = inetAddress.getHostName();
                                    byte[] address = inetAddress.getAddress();
                                    String canonicalName = inetAddress.getCanonicalHostName();

                                    Log.d(TAG, "onNext: ip : " + ip);
                                    Log.d(TAG, "onNext: name : " + name);
                                    Log.d(TAG, "onNext: address : " + Arrays.toString(address));
                                    Log.d(TAG, "onNext: canonicalName : " + canonicalName);
                                    Log.d(TAG, "onNext: Bssid : " + result.getBssid());

                                    modelConfiguredDevice = new ModelConfiguredDevice(name, ip, result.getBssid());

                                    DbUtil.insertDeviceToLocal(mdb, modelConfiguredDevice);

                                    ConfigSuccessFragment configSuccessFragment = new ConfigSuccessFragment(modelConfiguredDevice);

                                    getParentFragmentManager().beginTransaction()
                                            .replace(R.id.fcv_config, configSuccessFragment)
                                            .commit();
                                }
                            }).start();


                        } else {
                            try {
                                addingDeviceDialogFragment.dismiss();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }

                            Util util = new Util();
                            util.showSnackBar(btnStartConfig.getRootView(), "Configuration Failed");
                        }

                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Log.d(TAG, "onError: ");

                        try {
                            addingDeviceDialogFragment.dismiss();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                        Util util = new Util();
                        util.showSnackBar(btnStartConfig.getRootView(), e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                        try {
                            addingDeviceDialogFragment.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}