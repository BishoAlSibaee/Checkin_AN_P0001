package com.example.hotelservicesstandalone;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hotelservicesstandalone.lock.ApiResponse;
import com.example.hotelservicesstandalone.lock.ApiResult;
import com.example.hotelservicesstandalone.lock.ApiService;
import com.example.hotelservicesstandalone.lock.LockInitResultObj;
import com.example.hotelservicesstandalone.lock.RetrofitAPIManager;
import com.google.gson.reflect.TypeToken;
import com.ttlock.bl.sdk.api.ExtendedBluetoothDevice;
import com.ttlock.bl.sdk.api.TTLockClient;
import com.ttlock.bl.sdk.callback.InitLockCallback;
import com.ttlock.bl.sdk.callback.ScanLockCallback;
import com.ttlock.bl.sdk.callback.SetNBServerCallback;
import com.ttlock.bl.sdk.constant.FeatureValue;
import com.ttlock.bl.sdk.entity.LockError;
import com.ttlock.bl.sdk.util.FeatureValueUtil;
import com.tuya.smart.android.hardware.bean.HgwBean;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.IGwSearchListener;
import com.tuya.smart.home.sdk.api.ITuyaGwSearcher;
import com.tuya.smart.home.sdk.builder.ActivatorBuilder;
import com.tuya.smart.home.sdk.builder.TuyaGwActivatorBuilder;
import com.tuya.smart.home.sdk.builder.TuyaGwSubDevActivatorBuilder;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaActivator;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.api.ITuyaGateway;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.enums.ActivatorModelEnum;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class RoomManager extends AppCompatActivity
{
    private ROOM Room ;
    private TextView foundLockNewName,foundLock,caption , lock,power,curtain,service,door,motion,switch1,switch2,switch3,switch4,ac,gateway,selectedWifi,foundWifiDevice,power2,ac2,gateway2,foundZbeeDevice,foundwireZbGateway,wirezbGatewayNewName;
    private Button getWifi ;
    private WifiManager wifiManager;
    private WifiReceiver receiverWifi;
    private static Activity act  ;
    private ListView wifiList;
    private Spinner DeviceTypes , DeviceTypesZ ;
    private EditText wifiPass ;
    private String Token , NewName ,NewNameZ;
    private DeviceBean FOUND ;
    private ITuyaDevice FOUNDD ;
    private ITuyaGateway FOUNDG ;
    private int ID ;
    ConnectivityManager cm ;
    String ConnectedNetworkType ;
    WifiInfo info ;
    protected static final int REQUEST_PERMISSION_REQ_CODE = 11;
    private ExtendedBluetoothDevice FOUNDLOCK ;
    ITuyaActivator mTuyaActivatorAP ;
    ITuyaActivator mTuyaActivator ;
    ITuyaActivator mTuyaGWActivator ;
    ITuyaGwSearcher mTuyaGwSearcher ;
    ITuyaActivator mITuyaActivator ;
    RequestQueue REQ ;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_manager);
        act = this ;
        REQ = Volley.newRequestQueue(act);
        setActivity();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        wifiManager = (WifiManager) act.getApplicationContext().getSystemService(WIFI_SERVICE) ;
        cm = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                getTheNetworkInfo ();
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();

            }

            @Override
            public void onLosing(@NonNull Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);

            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);

            }
        } );
    }

    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        receiverWifi = new WifiReceiver(wifiManager, wifiList,selectedWifi);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        getWifi();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(receiverWifi);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(act, "permission granted", Toast.LENGTH_SHORT).show();
                    //wifiManager.startScan();
                }
                else
                {
                    Toast.makeText(act, "permission not granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case REQUEST_PERMISSION_REQ_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getScanLockCallback();
                } else {
                    if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)){

                    }
                }
                break;
            }
        }
    }

    private void getWifi()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            //Toast.makeText(act, "version> = marshmallow", Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                //Toast.makeText(act, "location turned off", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
            }
            else
            {
                //Toast.makeText(act, "location turned on", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
            }
        }
        else
        {
            //Toast.makeText(act, "scanning", Toast.LENGTH_SHORT).show();
            wifiManager.startScan();
        }
    }

    private void setActivity()
    {
        ID = getIntent().getExtras().getInt("RoomId");
        for (int i=0;i<Rooms.list.size();i++)
        {
            if (Rooms.list.get(i).id == ID )
            {
                Room = Rooms.list.get(i) ;
            }
        }
        caption = (TextView)findViewById(R.id.roomManager_caption);
        lock = (TextView)findViewById(R.id.room_Lock);
        power =(TextView)findViewById(R.id.room_Power);
        power.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if (Room.getPOWER() == null )
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("No Power Switch");
                    d.create().show();
                }
                else
                {
                    AlertDialog.Builder b = new AlertDialog.Builder(act);
                    b.setTitle("Delete Power ?");
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            setPowerSwitchStatus("0");
                            //Room.getLock().
                        }
                    });
                    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    b.create().show();
                }
                return false;
            }
        });
        power2 = (TextView)findViewById(R.id.room_Power2);
        curtain = (TextView)findViewById(R.id.room_Curtain);
        curtain.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if (Room.getCURTAIN() == null )
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("No Curtain ");
                    d.create().show();
                }
                else
                {
                    AlertDialog.Builder b = new AlertDialog.Builder(act);
                    b.setTitle("Delete Curtain ?");
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            Room.getCURTAIN().removeDevice(new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {

                                }

                                @Override
                                public void onSuccess()
                                {
                                    setCurtainSwitchStatus("0");
                                }
                            });
                        }
                    });
                    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    b.create().show();
                }
                return false;
            }
        });
        service = (TextView)findViewById(R.id.room_Service);
        service.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if (Room.getSERVICE() == null )
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("No Service Switch");
                    d.create().show();
                }
                else
                {
                    AlertDialog.Builder b = new AlertDialog.Builder(act);
                    b.setTitle("Delete Service Switch ?");
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            Room.getSERVICE().removeDevice(new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {

                                }

                                @Override
                                public void onSuccess()
                                {
                                    setServiceSwitchStatus("0");
                                    //door.setText("NO");
                                    //door.setTextColor(Color.RED);
                                }
                            });
                        }
                    });
                    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    b.create().show();
                }
                return false;
            }
        });
        door = (TextView)findViewById(R.id.room_Doorsensor);
        door.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if (Room.getDOORSENSOR() == null )
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("No Door Sensor");
                    d.create().show();
                }
                else
                {
                    AlertDialog.Builder b = new AlertDialog.Builder(act);
                    b.setTitle("Delete Door Sensor ?");
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            Room.getDOORSENSOR().removeDevice(new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {

                                }

                                @Override
                                public void onSuccess()
                                {
                                    setDoorSensorStatus("0");
                                    //door.setText("NO");
                                    //door.setTextColor(Color.RED);
                                }
                            });
                        }
                    });
                    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    b.create().show();
                }


                return false;
            }
        });
        motion = (TextView)findViewById(R.id.room_Motion);
        motion.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (Room.getMOTIONSENSOR() == null )
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("No Motion Sensor");
                    d.create().show();
                }
                else
                {
                    AlertDialog.Builder b = new AlertDialog.Builder(act);
                    b.setTitle("Delete Motion Sensor ?");
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            Room.getMOTIONSENSOR().removeDevice(new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {

                                }

                                @Override
                                public void onSuccess()
                                {
                                    setMotionSensorStatus("0");
                                    //door.setText("NO");
                                    //door.setTextColor(Color.RED);
                                }
                            });
                        }
                    });
                    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    b.create().show();
                }

                return false;
            }
        });
        switch1 = (TextView)findViewById(R.id.room_Switch1);
        switch1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if (Room.getSWITCH1() == null )
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("No Switch 1");
                    d.create().show();
                }
                else
                {
                    AlertDialog.Builder b = new AlertDialog.Builder(act);
                    b.setTitle("Delete Switch 1 ?");
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            Room.getSWITCH1().removeDevice(new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {

                                }

                                @Override
                                public void onSuccess()
                                {
                                    setSwitch1Status("0");
                                    //door.setText("NO");
                                    //door.setTextColor(Color.RED);
                                }
                            });
                        }
                    });
                    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    b.create().show();
                }
                return false;
            }
        });
        switch2 = (TextView)findViewById(R.id.room_Switch2);
        switch2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if (Room.getSWITCH2() == null )
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("No Switch 2");
                    d.create().show();
                }
                else
                {
                    AlertDialog.Builder b = new AlertDialog.Builder(act);
                    b.setTitle("Delete Switch 2 ?");
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            Room.getSWITCH2().removeDevice(new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {

                                }

                                @Override
                                public void onSuccess()
                                {
                                    setSwitch2Status("0");
                                    //door.setText("NO");
                                    //door.setTextColor(Color.RED);
                                }
                            });
                        }
                    });
                    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    b.create().show();
                }
                return false;
            }
        });
        switch3 = (TextView)findViewById(R.id.room_Switch3);
        switch3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if (Room.getSWITCH3() == null )
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("No Switch 3");
                    d.create().show();
                }
                else
                {
                    AlertDialog.Builder b = new AlertDialog.Builder(act);
                    b.setTitle("Delete Switch 3 ?");
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            Room.getSWITCH3().removeDevice(new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {

                                }

                                @Override
                                public void onSuccess()
                                {
                                    setSwitch3Status("0");
                                    //door.setText("NO");
                                    //door.setTextColor(Color.RED);
                                }
                            });
                        }
                    });
                    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    b.create().show();
                }
                return false;
            }
        });
        switch4 = (TextView)findViewById(R.id.room_Switch4);
        switch4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if (Room.getSWITCH4() == null )
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("No Switch 4");
                    d.create().show();
                }
                else
                {
                    AlertDialog.Builder b = new AlertDialog.Builder(act);
                    b.setTitle("Delete Switch 4 ?");
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            Room.getSWITCH4().removeDevice(new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {

                                }

                                @Override
                                public void onSuccess()
                                {
                                    setSwitch4Status("0");
                                    //door.setText("NO");
                                    //door.setTextColor(Color.RED);
                                }
                            });
                        }
                    });
                    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    b.create().show();
                }
                return false;
            }
        });
        selectedWifi = findViewById(R.id.selected_wifi);
        foundWifiDevice = findViewById(R.id.theFoundDevice);
        foundZbeeDevice = findViewById(R.id.theFoundDeviceZbee);
        foundwireZbGateway = findViewById(R.id.wire_zbgate_found);
        wirezbGatewayNewName = findViewById(R.id.wire_zbgateway_newstaticName);
        foundLock = findViewById(R.id.foundlock);
        foundLockNewName = findViewById(R.id.foundLockNewName);
        ac = findViewById(R.id.room_AC);
        ac.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if (Room.getAC() == null)
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("No AC Controller");
                    d.create().show();
                }
                else
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("Delete Ac Controller .. ?");
                    d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Room.getAC().removeDevice(new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {

                                }

                                @Override
                                public void onSuccess()
                                {
                                    setThermostatStatus("0");
                                }
                            });
                        }
                    });
                }
                return false;
            }
        });
        ac2 = findViewById(R.id.room_AC2);
        wifiPass = findViewById(R.id.wifi_pass);
        getWifi = findViewById(R.id.room_addWifi);
        getWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                } else
                {
                    wifiManager.startScan();
                }
            }

        });
        wifiList = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
        {
            Toast.makeText(act, "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        gateway = (TextView)findViewById(R.id.room_Gateway);
        gateway.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if (Room.getGATEWAY() == null)
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("No ZBGateway");
                    d.create().show();
                }
                else
                {
                    AlertDialog.Builder d = new AlertDialog.Builder(act);
                    d.setTitle("Delete ZBGateway .. ?");
                    d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Room.getGATEWAY().removeDevice(new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {

                                }

                                @Override
                                public void onSuccess()
                                {
                                    setZBGatewayStatus("0");
                                }
                            });
                        }
                    });
                }
                return false;
            }
        });
        gateway2 = (TextView)findViewById(R.id.room_Gateway2);
        DeviceTypes = (Spinner) findViewById(R.id.deviceNames_spinner);
        DeviceTypesZ = (Spinner) findViewById(R.id.deviceNames_spinnerZbee);
        String [] Types = new String[]{Room.RoomNumber+"Power",Room.RoomNumber+"ZGatway",Room.RoomNumber+"AC",Room.RoomNumber+"DoorSensor",Room.RoomNumber+"MotionSensor",Room.RoomNumber+"Curtain",Room.RoomNumber+"ServiceSwitch",Room.RoomNumber+"Switch1",Room.RoomNumber+"Switch2",Room.RoomNumber+"Switch3",Room.RoomNumber+"Switch4",Room.RoomNumber+"IR"};
        ArrayAdapter x =  new ArrayAdapter<String>(act ,R.layout.spinners_item ,Types);
        ArrayAdapter y =  new ArrayAdapter<String>(act ,R.layout.spinners_item ,Types);
        DeviceTypesZ.setAdapter(y);
        DeviceTypes.setAdapter(x);
        caption.setText("Manage Room : "+ Room.RoomNumber);
        if (Room.lock == 0 )
        {
            lock.setText("NO");
            lock.setTextColor(Color.RED);
        }
        else
        {
            lock.setText("YES");
            lock.setTextColor(Color.GREEN);
        }
        if (Room.Switch1 == 0)
        {
            switch1.setText("NO");
            switch1.setTextColor(Color.RED);
        }
        else
        {
            switch1.setText("YES");
            switch1.setTextColor(Color.GREEN);
        }
        if (Room.Switch2 == 0)
        {
            switch2.setText("NO");
            switch2.setTextColor(Color.RED);
        }
        else
        {
            switch2.setText("YES");
            switch2.setTextColor(Color.GREEN);
        }
        if (Room.Switch3 == 0)
        {
            switch3.setText("NO");
            switch3.setTextColor(Color.RED);
        }
        else
        {
            switch3.setText("YES");
            switch3.setTextColor(Color.GREEN);
        }
        if (Room.Switch4 == 0)
        {
            switch4.setText("NO");
            switch4.setTextColor(Color.RED);
        }
        else
        {
            switch4.setText("YES");
            switch4.setTextColor(Color.GREEN);
        }
        if (Room.CurtainSwitch == 0)
        {
            curtain.setText("NO");
            curtain.setTextColor(Color.RED);
        }
        else
        {
            curtain.setText("YES");
            curtain.setTextColor(Color.GREEN);
        }
        if (Room.MotionSensor == 0)
        {
            motion.setText("NO");
            motion.setTextColor(Color.RED);
        }
        else
        {
            motion.setText("YES");
            motion.setTextColor(Color.GREEN);
        }
        if (Room.DoorSensor == 0)
        {
            door.setText("NO");
            door.setTextColor(Color.RED);
        }
        else
        {
            door.setText("YES");
            door.setTextColor(Color.GREEN);
        }
        if (Room.PowerSwitch == 0)
        {
            power.setText("NO");
            power.setTextColor(Color.RED);
            power2.setText("NO");
            power2.setTextColor(Color.RED);
        }
        else
        {
            power.setText("YES");
            power.setTextColor(Color.GREEN);
            power2.setText("YES");
            power2.setTextColor(Color.GREEN);
        }
        if (Room.ServiceSwitch == 0)
        {
            service.setText("NO");
            service.setTextColor(Color.RED);
        }
        else
        {
            service.setText("YES");
            service.setTextColor(Color.GREEN);
        }
        if (Room.Thermostat == 0)
        {
            ac.setText("NO");
            ac.setTextColor(Color.RED);
            ac2.setText("NO");
            ac2.setTextColor(Color.RED);
        }
        else
        {
            ac.setText("YES");
            ac.setTextColor(Color.GREEN);
            ac2.setText("YES");
            ac2.setTextColor(Color.GREEN);
        }
        if ( Room.getGATEWAY_B() != null )
        {
            gateway.setText("YES");
            gateway.setTextColor(Color.GREEN);
            gateway2.setText("YES");
            gateway2.setTextColor(Color.GREEN);
        }
        else
        {
            gateway.setText("NO");
            gateway.setTextColor(Color.RED);
            gateway2.setText("NO");
            gateway2.setTextColor(Color.RED);
        }
    }

    public void searchWifiDevice(View view) {
        lodingDialog d = new lodingDialog(act);
        TuyaHomeSdk.getActivatorInstance().getActivatorToken(Login.THEHOME.getHomeId(),
                new ITuyaActivatorGetToken() {
                    @Override
                    public void onSuccess(String token)
                    {
                        Log.d("scanWifiDevice" , "token "+token+" "+selectedWifi.getText().toString()+" "+wifiPass.getText().toString());
                        Token = token ;
                        ActivatorBuilder builder = new ActivatorBuilder()
                                .setSsid(selectedWifi.getText().toString())
                                .setContext(act)
                                .setPassword(wifiPass.getText().toString())
                                .setActivatorModel(ActivatorModelEnum.TY_EZ)
                                .setTimeOut(100)
                                .setToken(Token)
                                .setListener(new ITuyaSmartActivatorListener() {

                                                 @Override
                                                 public void onError(String errorCode, String errorMsg)
                                                 {
                                                     d.stop();
                                                     mTuyaActivator.stop();
                                                     Log.d("scanWifiDevice" , "error "+errorMsg +" "+errorCode);
                                                     Toast.makeText(act, errorMsg+" "+errorCode , Toast.LENGTH_LONG).show();
                                                 }

                                                 @Override
                                                 public void onActiveSuccess(DeviceBean devResp)
                                                 {
                                                     Log.d("scanWifiDevice" , "success ");
                                                     d.stop();
                                                     Rooms.CHANGE_STATUS = true ;
                                                     foundWifiDevice.setText(devResp.getName());
                                                     FOUND = devResp ;
                                                     FOUNDD = TuyaHomeSdk.newDeviceInstance(FOUND.getDevId());
                                                     Toast.makeText(act, "Found", Toast.LENGTH_LONG).show();
                                                     mTuyaActivator.stop();
                                                 }

                                                 @Override
                                                 public void onStep(String step, Object data)
                                                 {
                                                     d.stop();
                                                     Rooms.CHANGE_STATUS = true ;
                                                     Toast.makeText(act , step , Toast.LENGTH_LONG).show();
                                                     mTuyaActivator.stop();
                                                     Log.d("scanWifiDevice" , "step "+step+" "+data.toString());
                                                 }
                                             }
                                );
                        mTuyaActivator = TuyaHomeSdk.getActivatorInstance().newMultiActivator(builder);

                        mTuyaActivator.start();
                    }

                    @Override
                    public void onFailure(String s, String s1)
                    {
                        mTuyaActivator.stop();
                        Log.d("scanWifiDevice" , s+" "+s1);
                    }
                });

    }

    public void searchApMode(View view) {

        lodingDialog d = new lodingDialog(act);
        TuyaHomeSdk.getActivatorInstance().getActivatorToken(Login.THEHOME.getHomeId(),
                new ITuyaActivatorGetToken() {
                    @Override
                    public void onSuccess(String token) {
                        Log.d("apSearchMode" , "tokrn is: "+token );
                        ActivatorBuilder builder = new ActivatorBuilder()
                                .setContext(act)
                                .setSsid(selectedWifi.getText().toString())
                                .setPassword(wifiPass.getText().toString())
                                .setActivatorModel(ActivatorModelEnum.TY_AP)
                                .setTimeOut(100)
                                .setToken(token)
                                .setListener(new ITuyaSmartActivatorListener() {

                                                 @Override
                                                 public void onError(String errorCode, String errorMsg) {
                                                     d.stop();
                                                     mTuyaActivatorAP.stop();
                                                     Log.d("apSearchMode" , "error "+errorMsg +" "+errorCode);
                                                     Toast.makeText(act, errorMsg+" "+errorCode , Toast.LENGTH_LONG).show();
                                                 }

                                                 @Override
                                                 public void onActiveSuccess(DeviceBean devResp) {
                                                     d.stop();
                                                     mTuyaActivatorAP.stop();
                                                     Log.d("apSearchMode" , "device is: "+devResp.name);
                                                     Rooms.CHANGE_STATUS = true ;
                                                     foundWifiDevice.setText(devResp.getName());
                                                     FOUND = devResp ;
                                                     FOUNDD = TuyaHomeSdk.newDeviceInstance(FOUND.getDevId());
                                                 }

                                                 @Override
                                                 public void onStep(String step, Object data) {
                                                     d.stop();
                                                     mTuyaActivatorAP.stop();
                                                     Log.d("apSearchMode" , "step is: "+step);
                                                 }
                                             }
                                );
                        mTuyaActivatorAP = TuyaHomeSdk.getActivatorInstance().newActivator(builder);
                        mTuyaActivatorAP.start();
                    }

                    @Override
                    public void onFailure(String s, String s1) {

                    }
                });

    }

    public void renameDevice(View view) {
        if (DeviceTypes.getSelectedItem().toString() != null )
        {
            lodingDialog d = new lodingDialog(act);
            if (FOUND != null ) {
                TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()).renameDevice(DeviceTypes.getSelectedItem().toString(), new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        // Renaming failed
                        d.stop();
                    }

                    @Override
                    public void onSuccess() {
                        // Renaming succeeded
                        d.stop();
                        Toast.makeText(act, "Name Changed Successfully", Toast.LENGTH_LONG).show();
                        NewName = DeviceTypes.getSelectedItem().toString();
                        foundWifiDevice.setText(NewName);
                        foundWifiDevice.setTextColor(Color.GREEN);
                    }
                });
            }
            else {
                Toast.makeText( act,"Device is null " , Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText( act,"Write New Device Name " , Toast.LENGTH_LONG).show();
        }
    }

    public void saveDevice(View view) {
        if (NewName == null)
        {
            Toast.makeText(act,"Rename Device First " , Toast.LENGTH_LONG).show();
        }
        else if (NewName.equals(Room.RoomNumber+"Power"))
        {
            setPowerSwitchStatus("1");
        }
        else if (NewName.equals(Room.RoomNumber+"ZGatway"))
        {
            setZBGatewayStatus("1");
        }
        else if (NewName.equals(Room.RoomNumber+"AC"))
        {
            setThermostatStatus("1");
        }
        else if (NewName.equals(Room.RoomNumber+"DoorSensor"))
        {
            setDoorSensorStatus("1");
        }
        else if (NewName.equals(Room.RoomNumber+"MotionSensor"))
        {
            setMotionSensorStatus("1");
        }
        else if (NewName.equals(Room.RoomNumber+"Curtain"))
        {
            setCurtainSwitchStatus("1");
        }
        else if (NewName.equals(Room.RoomNumber+"ServiceSwitch"))
        {
            setServiceSwitchStatus("1");
        }
        else if (NewName.equals(Room.RoomNumber+"Switch1"))
        {
            setSwitch1Status("1");
        }
        else if (NewName.equals(Room.RoomNumber+"Switch2"))
        {
            setSwitch2Status("1");
        }
        else if (NewName.equals(Room.RoomNumber+"Switch3"))
        {
            setSwitch3Status("1");
        }
        else if (NewName.equals(Room.RoomNumber+"Switch4"))
        {
            setSwitch4Status("1");
        }
        else
        {
            Toast.makeText(act,"Device Not Detected" , Toast.LENGTH_LONG).show();
        }
    }

    void setThermostatStatus(String status)
    {
        try
        {
            lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setThermostatStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    if (response.equals("1"))
                    {
                        //myRefThermostat.setValue(status);
                        if (status.equals("1"))
                        {
                            Room.setAC_B(FOUND);
                            Room.setAC(TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()));
                            ac.setText("YES");
                            ac.setTextColor(Color.GREEN);
                            ac2.setText("YES");
                            ac2.setTextColor(Color.GREEN);
                        }
                        else if (status.equals("0"))
                        {
                            Room.setAC_B(null);
                            Room.setAC(null);
                            ac.setText("No");
                            ac.setTextColor(Color.RED);
                            ac2.setText("NO");
                            ac2.setTextColor(Color.RED);
                        }

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setLockStatus(String status)
    {
        try
        {
            String url = Login.SelectedHotel.URL+"setLockStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    //ToastMaker.MakeToast(ID+"lock "+ status+" " + response ,act);
                    Log.e("lock" , response);
                    if (response.equals("1"))
                    {
                        //myRefLock.setValue(status);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id", String.valueOf(Room.id));
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setDoorSensorStatus(String status)
    {
        try
        {
            lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setDoorSensorStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    //ToastMaker.MakeToast("Door "+status+"   " + response , act);
                    if (response.equals("1"))
                    {
                        //myRefDoorSensor.setValue(status);
                        if (status.equals("1"))
                        {
                            Room.setDOORSENSOR_B(FOUND);
                            Room.setDOORSENSOR(TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()));
                            door.setText("YES");
                            door.setTextColor(Color.GREEN);
                        }
                        else if (status.equals("0"))
                        {
                            Room.setDOORSENSOR_B(null);
                            Room.setDOORSENSOR(null);
                            door.setText("NO");
                            door.setTextColor(Color.RED);
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setMotionSensorStatus(String status)
    {
        try
        {
            lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setMotionSensorStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    if (response.equals("1"))
                    {
                        if (status.equals("1"))
                        {
                            Room.setMOTIONSENSOR_B(FOUND);
                            Room.setMOTIONSENSOR(TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()));
                            motion.setText("YES");
                            motion.setTextColor(Color.GREEN);
                        }
                        else if (status.equals("0"))
                        {
                            Room.setMOTIONSENSOR_B(null);
                            Room.setMOTIONSENSOR(null);
                            motion.setText("NO");
                            motion.setTextColor(Color.RED);
                        }

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setCurtainSwitchStatus(String status)
    {
        try
        {
            lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setCurtainSwitchStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    if (response.equals("1"))
                    {
                        if (status.equals("1"))
                        {
                            Room.setCURTAIN_B(FOUND);
                            Room.setCURTAIN(TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()));
                            curtain.setText("YES");
                            curtain.setTextColor(Color.GREEN);
                        }
                        else
                        {
                            Room.setCURTAIN_B(null);
                            Room.setCURTAIN(null);
                            curtain.setText("NO");
                            curtain.setTextColor(Color.RED);
                        }

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setSwitch1Status(String status)
    {
        try
        {
            lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setSwitch1StatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    if (response.equals("1"))
                    {
                        if (status.equals("1"))
                        {
                            Room.setSWITCH1_B(FOUND);
                            Room.setSWITCH1(TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()));
                            switch1.setText("YES");
                            switch1.setTextColor(Color.GREEN);
                        }
                        else
                        {
                            Room.setSWITCH1_B(null);
                            Room.setSWITCH1(null);
                            switch1.setText("NO");
                            switch1.setTextColor(Color.RED);
                        }

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setSwitch2Status(String status)
    {
        try
        {
            lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setSwitch2StatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    if (response.equals("1"))
                    {
                        if (status.equals("1"))
                        {
                            Room.setSWITCH2_B(FOUND);
                            Room.setSWITCH2(TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()));
                            switch2.setText("YES");
                            switch2.setTextColor(Color.GREEN);
                        }
                        else
                        {
                            Room.setSWITCH2_B(null);
                            Room.setSWITCH2(null);
                            switch2.setText("NO");
                            switch2.setTextColor(Color.RED);
                        }

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setSwitch3Status(String status)
    {
        try
        {
            lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setSwitch3StatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    if (response.equals("1"))
                    {
                        if (status.equals("1"))
                        {
                            Room.setSWITCH3_B(FOUND);
                            Room.setSWITCH3(TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()));
                            switch3.setText("YES");
                            switch3.setTextColor(Color.GREEN);
                        }
                        else
                        {
                            Room.setSWITCH3_B(null);
                            Room.setSWITCH3(null);
                            switch3.setText("NO");
                            switch3.setTextColor(Color.RED);
                        }

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setSwitch4Status(String status)
    {
        try
        {
            lodingDialog loading =  new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setSwitch4StatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    if (response.equals("1"))
                    {
                        if (status.equals("1"))
                        {
                            Room.setSWITCH4_B(FOUND);
                            Room.setSWITCH4(TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()));
                            switch4.setText("YES");
                            switch4.setTextColor(Color.GREEN);
                        }
                        else
                        {
                            Room.setSWITCH4_B(null);
                            Room.setSWITCH4(null);
                            switch4.setText("NO");
                            switch4.setTextColor(Color.RED);
                        }

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setPowerSwitchStatus(String status)
    {
        try
        {
            lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setPowerSwitchStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    Log.e("power " , response +" " + status);
                    if (response.equals("1"))
                    {
                        if (status.equals("1"))
                        {
                            Room.setPOWER_B(FOUND);
                            Room.setPOWER(TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()));
                            power.setText("YES");
                            power.setTextColor(Color.GREEN);
                            power2.setText("YES");
                            power2.setTextColor(Color.GREEN);
                        }
                        else if (status.equals("0"))
                        {
                            Room.setPOWER_B(null);
                            Room.setPOWER(null);
                            power.setText("NO");
                            power.setTextColor(Color.RED);
                            power2.setText("NO");
                            power2.setTextColor(Color.RED);
                        }

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setServiceSwitchStatus(String status)
    {
        try
        {
            lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setServiceSwitchStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    if (response.equals("1"))
                    {
                        if (status.equals("1"))
                        {
                            Room.setSERVICE_B(FOUND);
                            Room.setSERVICE(TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()));
                            service.setText("YES");
                            service.setTextColor(Color.GREEN);
                        }
                        else if (status.equals("0"))
                        {
                            Room.setSERVICE_B(null);
                            Room.setSERVICE(null);
                            service.setText("NO");
                            service.setTextColor(Color.RED);
                        }

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setZBGatewayStatus(String status)
    {
        try
        {
            lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setGatewayStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    if (response.equals("1"))
                    {
                        if (status.equals("1"))
                        {
                            Room.setGATEWAY_B(FOUND);
                            Room.setGATEWAY(TuyaHomeSdk.newDeviceInstance(FOUND.getDevId()));
                            gateway.setText("YES");
                            gateway.setTextColor(Color.GREEN);
                            gateway2.setText("YES");
                            gateway2.setTextColor(Color.GREEN);
                        }
                        else
                        {
                            Room.setGATEWAY_B(null);
                            Room.setGATEWAY(null);
                            gateway.setText("NO");
                            gateway.setTextColor(Color.RED);
                            gateway2.setText("NO");
                            gateway2.setTextColor(Color.RED);
                        }

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setWiredZBGatewayStatus(String status)
    {
        try
        {
            lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setGatewayStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    if (response.equals("1"))
                    {
                        Room.setGATEWAY_B(FOUND);
                        Room.setWiredZBGateway(TuyaHomeSdk.newGatewayInstance(FOUND.getDevId()));
                        gateway.setText("YES");
                        gateway.setTextColor(Color.GREEN);
                        gateway2.setText("YES");
                        gateway2.setTextColor(Color.GREEN);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(Room.RoomNumber));
                    Params.put("id",String.valueOf(Room.id) );
                    Params.put("value" , status);
                    return Params;
                }
            };
            REQ.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    public void searchZbeeDevice(View view)
    {
        if (Room.getGATEWAY_B() != null )
        {
            lodingDialog d = new lodingDialog(act);
            TuyaGwSubDevActivatorBuilder builder = new TuyaGwSubDevActivatorBuilder()
                    .setDevId(Room.getGATEWAY_B().devId)
                    .setTimeOut(90)
                    .setListener(new ITuyaSmartActivatorListener() {

                                     @Override
                                     public void onError(String errorCode, String errorMsg) {
                                         d.stop();
                                         mTuyaGWActivator.stop();
                                         Log.d("ZBdeviceSearch" , "errm "+errorMsg +"errc "+errorCode);
                                         Toast.makeText(act,errorMsg,Toast.LENGTH_LONG).show();
                                     }

                                     @Override
                                     public void onActiveSuccess(DeviceBean devResp) {
                                         d.stop();
                                         Rooms.CHANGE_STATUS = true ;
                                         FOUND = devResp ;
                                         FOUNDD = TuyaHomeSdk.newDeviceInstance(devResp.getDevId());
                                         Toast.makeText(act,"Device Saved" , Toast.LENGTH_LONG).show();
                                         foundZbeeDevice.setText(devResp.getName());
                                         mTuyaGWActivator.stop();
                                         Log.d("ZBdeviceSearch" , devResp.name);
                                     }

                                     @Override
                                     public void onStep(String step, Object data) {
                                         d.stop();
                                         Rooms.CHANGE_STATUS = true ;
                                         mTuyaGWActivator.stop();
                                         Log.d("ZBdeviceSearch" , "step "+step);
                                         Toast.makeText(act,"Old Device",Toast.LENGTH_LONG).show();
                                     }
                                 }
                    );


            mTuyaGWActivator = TuyaHomeSdk.getActivatorInstance(). newGwSubDevActivator(builder);
// Start network configuration
            mTuyaGWActivator.start();

        }
        else if (Room.getWiredZBGateway() !=null )
        {
            lodingDialog d = new lodingDialog(act);
            TuyaGwSubDevActivatorBuilder builder = new TuyaGwSubDevActivatorBuilder()
                    .setDevId(Room.getGATEWAY_B().devId)
                    .setTimeOut(150)
                    .setListener(new ITuyaSmartActivatorListener() {

                                     @Override
                                     public void onError(String errorCode, String errorMsg)
                                     {
                                         d.stop();
                                         mTuyaGWActivator.stop();
                                         Log.d("ZBdeviceSearch" , "errm "+errorMsg +" errc "+errorCode);
                                     }

                                     @Override
                                     public void onActiveSuccess(DeviceBean devResp)
                                     {
                                         d.stop();
                                         Rooms.CHANGE_STATUS = true ;
                                         FOUND = devResp ;
                                         FOUNDD = TuyaHomeSdk.newDeviceInstance(devResp.getDevId());
                                         Toast.makeText(act,"Device Saved" , Toast.LENGTH_LONG).show();
                                         foundZbeeDevice.setText(devResp.getName());
                                         mTuyaGWActivator.stop();
                                         Log.d("ZBdeviceSearch" , devResp.name);
                                     }

                                     @Override
                                     public void onStep(String step, Object data)
                                     {
                                         d.stop();
                                         Rooms.CHANGE_STATUS = true ;
                                         mTuyaGWActivator.stop();
                                         Log.d("ZBdeviceSearch" , "step "+step);
                                     }
                                 }
                    );
            ITuyaActivator mTuyaGWActivator = TuyaHomeSdk.getActivatorInstance(). newGwSubDevActivator(builder);
// Start network configuration
            mTuyaGWActivator.start();
        }
        else
        {
            Toast.makeText(act,"this Room Has No ZBEE Gateway",Toast.LENGTH_LONG).show();
        }
    }

    public void renameDeviceZ(View view)
    {
        if (DeviceTypesZ.getSelectedItem().toString() != null )
        {
            lodingDialog d = new lodingDialog(act);
            if (FOUNDD != null ) {
                FOUNDD.renameDevice(DeviceTypesZ.getSelectedItem().toString(), new IResultCallback() {
                    @Override
                    public void onError(String code, String error)
                    {
                        // Renaming failed
                        d.stop();
                    }
                    @Override
                    public void onSuccess()
                    {
                        // Renaming succeeded
                        d.stop();
                        Toast.makeText(act,"Name Changed Successfully" , Toast.LENGTH_LONG).show();
                        NewNameZ = DeviceTypesZ.getSelectedItem().toString();
                        foundZbeeDevice.setText(NewNameZ);
                        foundZbeeDevice.setTextColor(Color.GREEN);
                    }
                });
            }
            else {
                Toast.makeText(act,"No Found Device",Toast.LENGTH_LONG).show();
            }

        }
        else
        {
            Toast.makeText( act,"Write New Device Name " , Toast.LENGTH_LONG).show();
        }
    }

    public void saveDeviceZ(View view)
    {
        if (NewNameZ == null)
        {
            Toast.makeText(act,"Rename Device First " , Toast.LENGTH_LONG).show();
        }
        else if (NewNameZ.equals(Room.RoomNumber+"Power"))
        {
            setPowerSwitchStatus("1");
        }
        else if (NewNameZ.equals(Room.RoomNumber+"ZGatway"))
        {
            setZBGatewayStatus("1");
        }
        else if (NewNameZ.equals(Room.RoomNumber+"AC"))
        {
            setThermostatStatus("1");
        }
        else if (NewNameZ.equals(Room.RoomNumber+"DoorSensor"))
        {
            setDoorSensorStatus("1");
        }
        else if (NewNameZ.equals(Room.RoomNumber+"MotionSensor"))
        {
            setMotionSensorStatus("1");
        }
        else if (NewNameZ.equals(Room.RoomNumber+"Curtain"))
        {
            setCurtainSwitchStatus("1");
        }
        else if (NewNameZ.equals(Room.RoomNumber+"ServiceSwitch"))
        {
            setServiceSwitchStatus("1");
        }
        else if (NewNameZ.equals(Room.RoomNumber+"Switch1"))
        {
            setSwitch1Status("1");
        }
        else if (NewNameZ.equals(Room.RoomNumber+"Switch2"))
        {
            setSwitch2Status("1");
        }
        else if (NewNameZ.equals(Room.RoomNumber+"Switch3"))
        {
            setSwitch3Status("1");
        }
        else if (NewNameZ.equals(Room.RoomNumber+"Switch4"))
        {
            setSwitch4Status("1");
        }
        else
        {
            Toast.makeText(act,"Device Not Detected" , Toast.LENGTH_LONG).show();
        }
    }

    public void searchWireZBGateway(View view)
    {
        lodingDialog d = new lodingDialog(act);
        mTuyaGwSearcher = TuyaHomeSdk.getActivatorInstance().newTuyaGwActivator().newSearcher();
        mTuyaGwSearcher.registerGwSearchListener(new IGwSearchListener()
        {
            @Override
            public void onDevFind(HgwBean hgwBean)
            {

                Log.e("wiregateway" , "id "+hgwBean.gwId + " " +Login.THEHOME.getName() );
                TuyaHomeSdk.getActivatorInstance().getActivatorToken(Login.THEHOME.getHomeId(), new ITuyaActivatorGetToken() {
                    @Override
                    public void onSuccess(String token)
                    {
                        Token = token ;
                        Log.e("wiregateway" , "token "+Token);
                                TuyaGwActivatorBuilder builder = new TuyaGwActivatorBuilder()
                                        .setToken(Token)
                                        .setTimeOut(60)
                                        .setContext(act)
                                        .setHgwBean(hgwBean)
                                        .setListener(new ITuyaSmartActivatorListener() {

                                                         @Override
                                                         public void onError(String errorCode, String errorMsg)
                                                         {
                                                             d.stop();
                                                             Log.e("wiregateway" , "error "+errorMsg+" "+errorCode);
                                                             //long time = ca.getTimeInMillis();
                                                             //ErrorRegister.rigestError(act,LogIn.room.getProjectName(),LogIn.room.getRoomNumber(),time,14,errorMsg,"error Searching Wire Zigbee Gateway");
                                                             mITuyaActivator.stop();
                                                             Toast.makeText(act,errorMsg,Toast.LENGTH_LONG).show();
                                                         }

                                                         @Override
                                                         public void onActiveSuccess(DeviceBean devResp)
                                                         {
                                                             Log.e("wiregateway" ,"name "+ devResp.name);
                                                             d.stop();
                                                             FOUND = devResp ;
                                                             FOUNDG = TuyaHomeSdk.newGatewayInstance(devResp.devId);
                                                             foundwireZbGateway.setText(FOUND.getName());
                                                             //foundwireZbGateway.setTextColor(Color.GREEN);
                                                             wirezbGatewayNewName.setText(Room.RoomNumber+"ZGatway");
                                                             mITuyaActivator.stop();
                                                             Toast.makeText(act,"Gateway Found",Toast.LENGTH_LONG).show();
                                                         }

                                                         @Override
                                                         public void onStep(String step, Object data)
                                                         {
                                                             d.stop();
                                                             Log.e("wiregateway" , "step "+step+ " "+data.toString());
                                                             mITuyaActivator.stop();
                                                             Toast.makeText(act,"old gateway",Toast.LENGTH_LONG).show();
                                                         }
                                                     });

                        mITuyaActivator = TuyaHomeSdk.getActivatorInstance().newGwActivator(builder);
                        mITuyaActivator.start() ;
                    }
                    @Override
                    public void onFailure(String errorCode, String errorMsg) {
                        Log.e("wiregateway" , errorMsg);
                    }
                });
            }
        });
    }

    public void renameWiredGateway(View view)
    {
        FOUNDD = TuyaHomeSdk.newDeviceInstance(FOUND.getDevId());
        FOUNDD.renameDevice(wirezbGatewayNewName.getText().toString(), new IResultCallback() {
            @Override
            public void onError(String code, String error) {

            }

            @Override
            public void onSuccess()
            {
                foundwireZbGateway.setText(wirezbGatewayNewName.getText().toString());
                foundwireZbGateway.setTextColor(Color.GREEN);
            }
        });
        Log.d("wiredNewName" , FOUND.name);

    }

    public void saveWiredGateway(View view)
    {
        if (foundwireZbGateway.getText().toString() !=null)
        {
            setWiredZBGatewayStatus("1");
        }
    }

    private void initBtService()
    {
        TTLockClient.getDefault().prepareBTService(getApplicationContext());
    }

    public void scanLocks(View view)
    {
        //check BT active
        boolean isBtEnable = TTLockClient.getDefault().isBLEEnabled(act);
        if (!isBtEnable)
        {
            TTLockClient.getDefault().requestBleEnable(act);
        }
        //start scan
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
            return;
        }

        getScanLockCallback();
    }

    private void getScanLockCallback()
    {
        lodingDialog l = new lodingDialog(act);
        TTLockClient.getDefault().startScanLock(new ScanLockCallback()
        {
            @Override
            public void onScanLockSuccess(ExtendedBluetoothDevice device)
            {
                l.stop();
                Rooms.CHANGE_STATUS = true ;
                FOUNDLOCK = device ;
                foundLock.setText(device.getName());
                TTLockClient.getDefault().stopScanLock();
                foundLockNewName.setText(Room.RoomNumber+"Lock");
            }

            @Override
            public void onFail(LockError error)
            {
                l.stop();
                Log.e("tttt",error.getErrorMsg());
                //ToastMaker.MakeToast(error.getErrorMsg(),act);
            }
        });
    }

    public void saveLock(View view)
    {
        if (!FOUNDLOCK.getName().equals(Room.RoomNumber+"Lock"))
        {
            Toast.makeText(act,"Please Rename Lock First ",Toast.LENGTH_LONG).show();
        }
        else
        {
            TTLockClient.getDefault().initLock(FOUNDLOCK, new InitLockCallback()
            {
                @Override
                public void onInitLockSuccess(String lockData) {
                    //this must be done after lock is initialized,call server api to post to your server
                    Rooms.CHANGE_STATUS = true ;
                    if ( FeatureValueUtil.isSupportFeature(lockData, FeatureValue.NB_LOCK) )
                    {
                        setNBServerForNBLock( lockData,FOUNDLOCK.getAddress());
                    }
                    else
                    {
                        //ToastMaker.MakeToast("--lock is initialized success--",act);
                        lodingDialog l = new lodingDialog(act);
                        String url = Login.SelectedHotel.URL+"setLockStatusValue.php";
                        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response)
                            {
                                l.stop();
                                if (response.equals("1"))
                                {
                                    Toast.makeText(act, "--lock is initialized success--", Toast.LENGTH_LONG).show();
                                    lock.setTextColor(Color.GREEN);
                                    lock.setText("YES");
                                    upload2Server(lockData);
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                l.stop();
                                Toast.makeText(act, error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError
                            {
                                Map<String,String> Params = new HashMap<String,String>();
                                Params.put("room", String.valueOf(Room.RoomNumber));
                                Params.put("id",String.valueOf(Room.id) );
                                Params.put("value" , "1");
                                return Params;
                            }
                        };
                        Volley.newRequestQueue(act).add(request);
                    }
                }

                @Override
                public void onFail(LockError error)
                {
                    Toast.makeText(act, error.getErrorMsg(), Toast.LENGTH_LONG).show();
                    //ToastMaker.MakeToast(error.getErrorMsg(),act);
                }
            });
        }
    }

    public void renameLock(View view)
    {
        if (FOUNDLOCK != null)
        {
            FOUNDLOCK.setName(Room.RoomNumber+"Lock");
            foundLock.setText(FOUNDLOCK.getName());
            foundLock.setTextColor(Color.GREEN);
        }
        else
        {
            Toast.makeText(act,"Found Lock Is Null",Toast.LENGTH_LONG).show();
        }
    }

    public static void setNBServerForNBLock(final String lockData, String lockMac)
    {
        //NB server port
        short mNBServerPort = 8011;
        String mNBServerAddress = "192.127.123.11";
        TTLockClient.getDefault().setNBServerInfo(mNBServerPort, mNBServerAddress, lockData, new SetNBServerCallback() {
            @Override
            public void onSetNBServerSuccess(int battery)
            {
                Toast.makeText(act, "--set NB server success--", Toast.LENGTH_LONG).show();
                upload2Server(lockData);
            }

            @Override
            public void onFail(LockError error)
            {
                Toast.makeText(act, error.getErrorMsg(), Toast.LENGTH_LONG).show();
                //no matter callback is success or fail,upload lockData to server.
                upload2Server(lockData);
            }
        });
    }

    public static void upload2Server(String lockData)
    {
        Calendar c = Calendar.getInstance() ;
        c.setTimeInMillis(System.currentTimeMillis());
        String lockAlias = "MyTestLock" + c.get(Calendar.DAY_OF_MONTH);//DateUtils.getMillsTimeFormat(System.currentTimeMillis());
        ApiService apiService = RetrofitAPIManager.provideClientApi();
        Call<ResponseBody> call = apiService.lockInit(ApiService.CLIENT_ID,Rooms.acc.getAccess_token() , lockData,lockAlias,System.currentTimeMillis());
        RetrofitAPIManager.enqueue(call, new TypeToken<LockInitResultObj>()
        {
        }, new ApiResponse.Listener<ApiResult<LockInitResultObj>>()
        {
            @Override
            public void onResponse(ApiResult<LockInitResultObj> result)
            {
                if (!result.success)
                {
                    Toast.makeText(act, "-init fail-to server-", Toast.LENGTH_LONG).show();
                    //if upload fail you should cache lockData and upload again until success,or you should reset lock and do init again.
                    return;
                }
                Toast.makeText(act, "--init lock success--", Toast.LENGTH_LONG).show();
                //Intent intent = new Intent(act, UserLockActivity.class);
               // act.startActivity(intent);
                //act.finish();

            }
        }, new ApiResponse.ErrorListener()
        {
            @Override
            public void onErrorResponse(Throwable requestError)
            {
                Toast.makeText(act, requestError.getMessage()+"error", Toast.LENGTH_LONG).show();
                //ToastMaker.MakeToast(requestError.getMessage()+"error", act);
            }
        });
    }

    void getTheNetworkInfo () {
        if (cm != null ) {
            if (cm.getActiveNetworkInfo() != null ) {
                if (cm.getActiveNetworkInfo().isConnected()) {
                    ConnectedNetworkType = cm.getActiveNetworkInfo().getTypeName() ;
                    if (ConnectedNetworkType != null) {
                        if (ConnectedNetworkType.equals("WIFI") || ConnectedNetworkType.equals("wifi") || ConnectedNetworkType.equals("Wifi")) {
                            if (wifiManager.getConnectionInfo() != null) {
                                info = wifiManager.getConnectionInfo();
                                if (info != null ) {
                                    Log.d("wifiName",info.getSSID());
                                    selectedWifi.setText(info.getSSID());
                                }
                                else {
                                    //MessageDialog m = new MessageDialog(act,"Wifi Name ?","Couldn't Get The Wifi Name ");
                                }
                            }
                            else {
                                //MessageDialog m = new MessageDialog(act,"Wifi Name ?","Couldn't Get The Wifi Name ");
                            }
                        }
                        else {
                            //MessageDialog m = new MessageDialog(act,"No Wifi Connection","Your Device Must Be Connected To WIFI Network");
                        }
                    }
                    else {
                        //MessageDialog m = new MessageDialog(act,"No Network Type","Couldn't get The Network Type");
                    }
                }
                else  {
                    //MessageDialog m = new MessageDialog(act,"UnConnected","This Device Is Not Connected To any network");
                }
            }
            else {
                //MessageDialog m = new MessageDialog(act,"No Network","Couldn't get network data");
            }
        }
        else {
            //MessageDialog m = new MessageDialog(act,"No Connection","This Android Device Has No Network Connection Feature");
        }
    }


}