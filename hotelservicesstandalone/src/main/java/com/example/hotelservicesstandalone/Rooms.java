package com.example.hotelservicesstandalone;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.LogPrinter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hotelservicesstandalone.TUYA.Tuya_Login;
import com.example.hotelservicesstandalone.lock.AccountInfo;
import com.example.hotelservicesstandalone.lock.ApiService;
import com.example.hotelservicesstandalone.lock.GatewayListAdapter;
import com.example.hotelservicesstandalone.lock.GatewayObj;
import com.example.hotelservicesstandalone.lock.InitGatewayActivity;
import com.example.hotelservicesstandalone.lock.LockObj;
import com.example.hotelservicesstandalone.lock.MyApplication;
import com.example.hotelservicesstandalone.lock.RetrofitAPIManager;
import com.example.hotelservicesstandalone.lock.ServerError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.reflect.TypeToken;
import com.ttlock.bl.sdk.api.ExtendedBluetoothDevice;
import com.ttlock.bl.sdk.api.TTLockClient;
import com.ttlock.bl.sdk.callback.ControlLockCallback;
import com.ttlock.bl.sdk.constant.ControlAction;
import com.ttlock.bl.sdk.entity.ControlLockResult;
import com.ttlock.bl.sdk.entity.LockError;
import com.ttlock.bl.sdk.gateway.api.GatewayClient;
import com.ttlock.bl.sdk.gateway.callback.ConnectCallback;
import com.ttlock.bl.sdk.gateway.callback.InitGatewayCallback;
import com.ttlock.bl.sdk.gateway.callback.ScanGatewayCallback;
import com.ttlock.bl.sdk.gateway.model.ConfigureGatewayInfo;
import com.ttlock.bl.sdk.gateway.model.DeviceInfo;
import com.ttlock.bl.sdk.gateway.model.GatewayError;
import com.ttlock.bl.sdk.util.DigitUtil;
import com.ttlock.bl.sdk.util.GsonUtil;
import com.ttlock.bl.sdk.util.LogUtil;
import com.tuya.smart.android.device.api.ITuyaDeviceMultiControl;
import com.tuya.smart.android.device.bean.MultiControlBean;
import com.tuya.smart.android.device.bean.MultiControlLinkBean;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.scene.PreCondition;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.SceneCondition;
import com.tuya.smart.home.sdk.bean.scene.SceneTask;
import com.tuya.smart.home.sdk.bean.scene.condition.rule.BoolRule;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IDeviceListener;
import com.tuya.smart.sdk.api.INeedLoginListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.enums.TYDevicePublishModeEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;

public class Rooms extends AppCompatActivity
{
    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    final static private String serverKey = "key=" + "AAAAQmygXvw:APA91bFt5CiONiZPDDj4_kz9hmKXlL1cjfTa_ZNGfobMPmt0gamhzEoN2NHiOxypCDr_r5yfpLvJy-bQSgrykXvaqKkThAniTr-0hpXPBrXm7qWThMmkiaN9o6qaUqfIUwStMMuNedTw";
    final static private String contentType = "application/json";
    private TextView hotelName , devicesCount ;
    private ListView devicesListView , roomsListView ;
    static List<ROOM> list ;
    private String getRoomsUrl = Login.SelectedHotel.URL+"getAllRooms.php" ;
    private String getUnregisteredRooms = Login.SelectedHotel.URL+"getAllUnregisteredRooms.php" ;
    static Activity act ;
    private ListView  RoomsRecycler ;
    static ArrayList<LockObj> Locks ;
    static List<GatewayObj> LockGateways ;
    public AccountInfo accountInfo;
    public static AccountInfo acc;
    private List<HomeBean> Homs;
    private List<DeviceBean> Devices ;
    private List<DeviceBean> UnRecognizedDevices_B ;
    private int removeParameter =0;
    private static String insertServiceOrderUrl = Login.SelectedHotel.URL+"insertServiceOrder.php";
    private static String removeServiceOrderUrl = Login.SelectedHotel.URL+"removeServiceOrder.php";
    private FirebaseDatabase database ;
    private List<DatabaseReference> FireRooms ;
    private boolean[] CLEANUP , LAUNDRY , DND , CHECKOUT ;
    private Runnable[] TempRonnableList , DoorRunnable ;
    private Handler[] Handlers ;
    private long THE_AC_INTERVAL_TIME = 1000*30 , THEDOORWARNING_INTERVAL = 1000*60*5 ;
    private boolean[] AC_SENARIO_Status , DOORSTATUS;
    private long[] start , period , DoorStart , DoorPeriod ;
    private String[] ClientTemp , TempSetPoint ;
    private Rooms_Adapter_Base adapter ;
    ITuyaDeviceMultiControl iTuyaDeviceMultiControl ;
    static int checkInModeTime = 0 ;
    static int checkOutModeTime = 0 ;
    private LockDB lockDB ;
    Button toggle , resetDevices ;
    LinearLayout logoLayout , btnsLayout ,mainLogo ;
    private String LogInUrl = Login.SelectedHotel.URL+"logInToHotel.php";
    private RequestQueue requestQueue;
    private String getServiceEmpsUrl = Login.SelectedHotel.URL+"getServiceEmps.php";
    private static List<ServiceEmps> Emps ;
    private DatabaseReference ServiceUsers ;
    static RequestQueue MessagesQueue;
    static boolean CHANGE_STATUS = false ;
    RequestQueue REQ , REQ1 , CLEANUP_QUEUE , LAUNDRY_QUEUE , CHECKOUT_QUEUE ;
    EditText searchText ;
    Button searchBtn ;
    List<ExtendedBluetoothDevice> gatewaysList = new ArrayList<ExtendedBluetoothDevice>() ;
    ListView gatewaysListView ;
    ExtendedBluetoothDevice TheFoundGateway ;
    private ConfigureGatewayInfo configureGatewayInfo;
    List<SceneBean> SCENES ;
    List<String> IMAGES ;
    DatabaseReference ServerDevice ;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rooms);
        act = this ;
        REQ = Volley.newRequestQueue(act);
        REQ1 = Volley.newRequestQueue(act);
        CLEANUP_QUEUE = Volley.newRequestQueue(act);
        LAUNDRY_QUEUE = Volley.newRequestQueue(act);
        CHECKOUT_QUEUE = Volley.newRequestQueue(act);
        setActivity();
        setTuyaApplication();
        getRooms();
        getServiceUsersFromFirebase();
//        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>()
//                {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task)
//                    {
//                        if (!task.isSuccessful())
//                        {
//                            //Log.w(TAG, "Fetching FCM registration token failed", task.getException());
//                            return;
//                        }
//                        String token = task.getResult();
//                        Log.e("tokeneee" , token);
//                        sendRegistrationToServer(token);
//                        ServerDevice.child("status").setValue("1");
//                        ServerDevice.child("token").setValue(token);
//                    }
//                });
        hideSystemUI();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        hideSystemUI();
        if (CHANGE_STATUS) {
            getRooms();
            loginTTLock();
            CHANGE_STATUS = false ;
        }
    }

    private void setActivity()
    {
        lockDB = new LockDB(act);
        if (!lockDB.isLoggedIn()) {
            lockDB.removeAll();
            lockDB.insertLock("off");
        }
        SCENES = new ArrayList<>();
        configureGatewayInfo = new ConfigureGatewayInfo();
        gatewaysListView = (ListView) findViewById(R.id.scanLockGatewayList);
        searchText = (EditText) findViewById(R.id.search_text);
        searchBtn = (Button) findViewById(R.id.button16);
        devicesCount = findViewById(R.id.textView26);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!searchBtn.getText().toString().equals("X")) {
                    if (searchText.getText() == null ) {
                        Toast.makeText(act,"enter text",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (devicesListView.getVisibility() == View.VISIBLE ) {
                        String Text = searchText.getText().toString() ;
                        List<DeviceBean> Results = new ArrayList<DeviceBean>();
                        for (int i = 0 ; i < Devices.size() ; i++) {
                            if (Devices.get(i).getName().contains(Text)) {
                                Results.add(Devices.get(i));
                            }
                        }
                        if (Results.size() > 0 ) {
                            searchBtn.setText("X");
                            devicesCount.setText(String.valueOf(Results.size()));
                            Devices_Adapter adapter = new Devices_Adapter(Results,act);
                            devicesListView.setAdapter(adapter);
                        }
                        else {
                            Toast.makeText(act,"no results",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    if (devicesListView.getVisibility() == View.VISIBLE) {
                        searchBtn.setText("Search");
                        devicesCount.setText(String.valueOf(Devices.size()));
                        Devices_Adapter adapter = new Devices_Adapter(Devices,act);
                        devicesListView.setAdapter(adapter);
                    }
                }

            }
        });
        MessagesQueue = Volley.newRequestQueue(act);
        Emps = new ArrayList<ServiceEmps>();
        toggle = (Button) findViewById(R.id.button9);
        mainLogo = (LinearLayout) findViewById(R.id.logoLyout) ;
        resetDevices = (Button) findViewById(R.id.button2);
        logoLayout = (LinearLayout) findViewById(R.id.logoLyout);
        btnsLayout = (LinearLayout) findViewById(R.id.btnsLayout);
        hotelName = (TextView) findViewById(R.id.hotelName);
        hotelName.setText(Login.THEHOTELDB.getHotelName());
        list = new ArrayList<ROOM>();
        Locks = new ArrayList<LockObj>();
        UnRecognizedDevices_B = new ArrayList<DeviceBean>();
        Devices = new ArrayList<DeviceBean>();
        //RoomsRecycler = (ListView) findViewById(R.id.rooms_recycler);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(act, LinearLayoutManager.VERTICAL,false);
        //RoomsRecycler.setLayoutManager(manager);
        roomsListView = (ListView) findViewById(R.id.RoomsListView);
        devicesListView = (ListView) findViewById(R.id.DevicesListView);
        database = FirebaseDatabase.getInstance("https://hotelservices-ebe66.firebaseio.com/");
        ServerDevice = database.getReference(Login.SelectedHotel.ProjectName+"ServerDevice");
        ServiceUsers = database.getReference(Login.SelectedHotel.ProjectName+"ServiceUsers");
        FireRooms = new ArrayList<DatabaseReference>();
        iTuyaDeviceMultiControl = TuyaHomeSdk.getDeviceMultiControlInstance();
        //roomsListView.setNestedScrollingEnabled(false);
        //devicesListView.setNestedScrollingEnabled(false);
//        devicesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                view.setBackgroundColor(Color.DKGRAY);
//                Dialog d = new Dialog(act);
//                d.setContentView(R.layout.rename_device_dialog);
//                Spinner s = (Spinner) d.findViewById(R.id.devicerenamespinner);
//                Spinner rr = (Spinner) d.findViewById(R.id.roomsspinner);
//                String [] Types = new String[]{"Power","ZGatway","AC","DoorSensor","MotionSensor","Curtain","ServiceSwitch","Switch1","Switch2","Switch3","Switch4"};
//                String [] therooms = new String [list.size()];
//                for (int i=0; i<list.size() ; i++) {
//                    therooms[i] = String.valueOf( list.get(i).RoomNumber );
//                }
//                ArrayAdapter<String> a = new ArrayAdapter<String>(act,R.layout.spinners_item,Types);
//                ArrayAdapter<String> r = new ArrayAdapter<String>(act,R.layout.spinners_item,therooms);
//                s.setAdapter(a);
//                rr.setAdapter(r);
//                TextView title = (TextView) d.findViewById(R.id.RenameDialog_title);
//                for (int i=0;i<Devices.size();i++) {
//                    if (Devices.get(i).getName().equals(devicesListView.getAdapter().getItem(position).toString())) {
//                        title.setText("Modify "+Devices.get(i).getName() + " Device "+Devices.get(i).getIsOnline().toString());
//                    }
//                }
//                Button cancel = (Button) d.findViewById(R.id.cancel_diallog);
//                Button rename = (Button) d.findViewById(R.id.DoTheRename);
//                Button delete = (Button) d.findViewById(R.id.deleteDevice);
//                cancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        d.dismiss();
//                    }
//                });
//                rename.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        for (int i=0;i<Devices.size();i++) {
//                            if (Devices.get(i).getName().equals(devicesListView.getAdapter().getItem(position).toString())) {
//                                ITuyaDevice Device = TuyaHomeSdk.newDeviceInstance(Devices.get(i).getDevId());
//                                Device.renameDevice(rr.getSelectedItem().toString()+s.getSelectedItem().toString(), new IResultCallback() {
//                                    @Override
//                                    public void onError(String code, String error) {
//                                        Toast.makeText(act,"Error. "+error,Toast.LENGTH_LONG).show();
//                                    }
//
//                                    @Override
//                                    public void onSuccess() {
//                                        Rooms.CHANGE_STATUS = true ;
//                                        Toast.makeText(act,"Device Renamed .",Toast.LENGTH_LONG).show();
//                                        d.dismiss();
//                                    }
//                                });
//                            }
//                        }
//
//                    }
//                });
//                delete.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        for (int i=0;i<Devices.size();i++) {
//                            if (Devices.get(i).getName().equals(devicesListView.getAdapter().getItem(position).toString())) {
//                                ITuyaDevice Device = TuyaHomeSdk.newDeviceInstance(Devices.get(i).getDevId());
//                                Device.removeDevice(new IResultCallback() {
//                                    @Override
//                                    public void onError(String code, String error) {
//                                        Toast.makeText(act,"Error. "+error,Toast.LENGTH_LONG).show();
//                                    }
//
//                                    @Override
//                                    public void onSuccess() {
//                                        Rooms.CHANGE_STATUS = true ;
//                                        Toast.makeText(act,"Device Deleted .",Toast.LENGTH_LONG).show();
//                                        d.dismiss();
//                                    }
//                                });
//                            }
//                        }
//
//                    }
//                });
//                d.show();
//
//                d.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        view.setBackgroundColor(Color.LTGRAY);
//                    }
//                });
//                return false;
//            }
//        });
//        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                for (int i=0;i<Devices.size();i++) {
//                    if (Devices.get(i).getName().equals(devicesListView.getAdapter().getItem(position).toString())) {
//                        Log.d("SelectedDeviceInfo","name: "+Devices.get(i).getName()+" dps: "+Devices.get(position).getDps()+" category: "+Devices.get(position).getDeviceCategory());
//                            if (Devices.get(i).getIsOnline()) {
//                                Toast.makeText(act,"online",Toast.LENGTH_LONG).show();
//                            }
//                            else {
//                                Toast.makeText(act,"offline",Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    }
//
//            }
//        });
        mainLogo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Dialog  dd = new Dialog(act);
                dd.setContentView(R.layout.lock_unlock_dialog);
                Button cancel = (Button) dd.findViewById(R.id.confermationDialog_cancel);
                Button lock = (Button) dd.findViewById(R.id.messageDialog_ok);
                EditText password = (EditText) dd.findViewById(R.id.editTextTextPassword);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dd.dismiss();
                    }
                });
                lock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final lodingDialog loading = new lodingDialog(act);
                        final String pass = password.getText().toString() ;
                        StringRequest re = new StringRequest(Request.Method.POST, LogInUrl, new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response)
                            {
                                Log.d("LoginResult" , response );
                                loading.stop();
                                if (response.equals("1"))
                                {
                                    lockDB.modifyValue("off");
                                    roomsListView.setVisibility(View.VISIBLE);
                                    devicesListView.setVisibility(View.GONE);
                                    btnsLayout.setVisibility(View.VISIBLE);
                                    logoLayout.setVisibility(View.GONE);
                                    dd.dismiss();
                                }
                                else if (response.equals("0"))
                                {
                                    Toast.makeText(act,"UnLock Failed",Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    Toast.makeText(act,"No Params",Toast.LENGTH_LONG).show();
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                loading.stop();
                            }
                        })
                        {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError
                            {
                                Map<String,String> par = new HashMap<String, String>();
                                par.put( "password" , pass ) ;
                                par.put( "hotel" , "1" ) ;
                                return par;
                            }
                        };
                        Volley.newRequestQueue(act).add(re);
                    }
                });
                dd.show();
                return false;
            }
        });
        mainLogo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSystemUI();
                return false;
            }
        });
        logoLayout.setVisibility(View.GONE);
        roomsListView.setVisibility(View.VISIBLE);
        devicesListView.setVisibility(View.GONE);
        hideSystemUI();
        searchBtn.setVisibility(View.GONE);
        searchText.setVisibility(View.GONE);
        if (lockDB.getLockValue().equals("off")) {
            roomsListView.setVisibility(View.VISIBLE);
            devicesListView.setVisibility(View.GONE);
            btnsLayout.setVisibility(View.VISIBLE);
            logoLayout.setVisibility(View.GONE);
        }
        else if (lockDB.getLockValue().equals("on")) {
            roomsListView.setVisibility(View.GONE);
            devicesListView.setVisibility(View.GONE);
            btnsLayout.setVisibility(View.GONE);
            logoLayout.setVisibility(View.VISIBLE);
        }
        else {
            roomsListView.setVisibility(View.GONE);
            devicesListView.setVisibility(View.GONE);
            btnsLayout.setVisibility(View.GONE);
            logoLayout.setVisibility(View.VISIBLE);
        }
    }

    private void getRooms()
    {

        final lodingDialog loading = new lodingDialog(act);
        StringRequest re = new StringRequest(Request.Method.POST, getRoomsUrl, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("rooms" , response);
                loading.stop();
                try
                {

                    JSONArray arr = new JSONArray(response);
                    list.clear();
                    FireRooms.clear();
                    for (int i=0;i<arr.length();i++)
                    {
                        JSONObject row = arr.getJSONObject(i);
                        int id = row.getInt("id");
                        int rNum = row.getInt("RoomNumber");
                        int Status = row.getInt("Status");
                        int Hotel = row.getInt("hotel");
                        int b = row.getInt("Building");
                        int bId = row.getInt("BuildingId");
                        int f = row.getInt("Floor");
                        int fId = row.getInt("FloorId");
                        String rType = row.getString("RoomType");
                        int ss = row.getInt("SuiteStatus");
                        int sn = row.getInt("SuiteNumber");
                        int si = row.getInt("SuiteId");
                        int rn = row.getInt("ReservationNumber");
                        int rs = row.getInt("roomStatus");
                        int t = row.getInt("Tablet");
                        String dep = row.getString("dep");
                        int c = row.getInt("Cleanup");
                        int l = row.getInt("Laundry");
                        int roomS = row.getInt("RoomService");
                        int ch = row.getInt("Checkout");
                        int res = row.getInt("Restaurant");
                        int sos = row.getInt("SOS");
                        int dnd = row.getInt("DND");
                        int PowerSwitch = row.getInt("PowerSwitch");
                        int DoorSensor = row.getInt("DoorSensor");
                        int MotionSensor = row.getInt("MotionSensor");
                        int Thermostat = row.getInt("Thermostat");
                        int zbgateway = row.getInt("ZBGateway");
                        int CurtainSwitch = row.getInt("CurtainSwitch");
                        int ServiceSwitch = row.getInt("ServiceSwitch");
                        int lock = row.getInt("lock");
                        int Switch1 = row.getInt("Switch1");
                        int Switch2 = row.getInt("Switch2");
                        int Switch3 = row.getInt("Switch3");
                        int Switch4 = row.getInt("Switch4");
                        String LockGateway = row.getString("LockGateway");
                        String LockName = row.getString("LockName");
                        int po = row.getInt("powerStatus");
                        int cu = row.getInt("curtainStatus");
                        int doo = row.getInt("doorStatus");
                        int temp = row.getInt("temp");
                        String token =row.getString("token");
                        ROOM room = new ROOM(id,rNum,Status,Hotel,b,bId,f,fId,rType,ss,sn,si,rn,rs,t,dep,c,l,roomS,ch,res,sos,dnd,PowerSwitch,DoorSensor,MotionSensor,Thermostat,zbgateway,CurtainSwitch,ServiceSwitch,lock,Switch1,Switch2,Switch3,Switch4,LockGateway,LockName,po,cu,doo,temp,token);
                        room.printRoomOnLog();
                        list.add(room);
                        FireRooms.add(database.getReference(Login.SelectedHotel.ProjectName+"/B"+room.Building+"/F"+room.Floor+"/R"+room.RoomNumber));

                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                adapter = new Rooms_Adapter_Base(list,act);
                roomsListView.setAdapter(adapter);
                defineVariables();

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                loading.stop();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String,String> par = new HashMap<String, String>();
                par.put("Hotel" , String.valueOf(Login.THEHOTELDB.getHotelId()));
                return par;
            }
        };
        Volley.newRequestQueue(act).add(re);
    }

    void defineVariables() {
        AC_SENARIO_Status = new boolean[list.size()];
        DOORSTATUS = new boolean[list.size()];
        start = new long[list.size()];
        DoorStart = new long[list.size()];
        period = new long[list.size()];
        DoorPeriod = new long[list.size()];
        ClientTemp = new String[list.size()];
        TempSetPoint = new String[list.size()];
        TempRonnableList = new Runnable[list.size()];
        DoorRunnable = new Runnable[list.size()];
        CLEANUP = new boolean[list.size()];
        LAUNDRY = new boolean[list.size()];
        DND = new boolean[list.size()];
        CHECKOUT = new boolean[list.size()];
        Handlers = new Handler[list.size()];
        for (int t =0;t<list.size();t++)
        {
            CLEANUP[t] = false ;
            LAUNDRY[t] = false ;
            DND[t] = false ;
            CHECKOUT[t] = false ;
            AC_SENARIO_Status[t] = false ;
            DOORSTATUS[t] = false ;
            start[t] = 0 ;
            DoorStart[t]=0;
            period[t]=0 ;
            DoorPeriod[t]=0;
            ClientTemp[t] = "0" ;
            TempSetPoint[t] = "250" ;
            int finalT = t;
            int finalT1 = t;
            int finalT2 = t;
            DoorRunnable[t] = new Runnable() {
                @Override
                public void run()
                {
                    long millis = System.currentTimeMillis() - DoorStart[finalT] ;
                    int seconds = (int) (millis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    Handlers[finalT] = new Handler();
                    Handlers[finalT].postDelayed(this,1000) ;
                    //Handler timerDoorHandler = new Handler();
                    //timerDoorHandler.postDelayed(this,1000) ;
                    DoorPeriod[finalT] = System.currentTimeMillis() - DoorStart[finalT] ;
                    Log.d("theSTATUSDOOR"+list.get(finalT).RoomNumber , String.valueOf(DOORSTATUS[finalT])+" " +minutes+":"+seconds);
                    if ( DoorPeriod[finalT] >=  THEDOORWARNING_INTERVAL  && DOORSTATUS[finalT])
                    {
                        FireRooms.get(finalT).child("doorStatus").setValue(2);
                        Handlers[finalT].removeCallbacks(DoorRunnable[finalT]);
                    }
                    else if (DoorPeriod[finalT] >=  THEDOORWARNING_INTERVAL  && !DOORSTATUS[finalT])
                    {
                        Handlers[finalT].removeCallbacks(DoorRunnable[finalT]);
                    }

                }
            };
            TempRonnableList[t] = new Runnable()
            {
                @Override
                public void run()
                {
                    long millis = System.currentTimeMillis() - start[finalT] ;
                    int seconds = (int) (millis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    Handler hsndler = new Handler();
                    hsndler.postDelayed(this, 1000);
                    //Handlers[finalT].postDelayed(TempRonnableList[finalT], 1000);
                    period[finalT] = System.currentTimeMillis() - start[finalT] ;
                    Log.d(list.get(finalT).RoomNumber+" theSTATUS " , String.valueOf(AC_SENARIO_Status[finalT])+" "+ClientTemp[finalT]+" " +minutes+":"+seconds);
                    if ( period[finalT] >=  THE_AC_INTERVAL_TIME  && AC_SENARIO_Status[finalT])
                    {
                        if (list.get(finalT2).getAC() != null )
                        {
                            list.get(finalT2).getAC().publishDps("{\" 2\": "+TempSetPoint[finalT]+"}", new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {

                                }

                                @Override
                                public void onSuccess()
                                {
                                    //ToastMaker.MakeToast("Temp Set to Setpoint " , act);
                                    AC_SENARIO_Status[finalT1] = false ;
                                    hsndler.removeCallbacks(TempRonnableList[finalT]);
                                }
                            });
                        }

                    }
                    else if (period[finalT] >=  THE_AC_INTERVAL_TIME  && !AC_SENARIO_Status[finalT])
                    {
                        hsndler.removeCallbacks(TempRonnableList[finalT]);
                    }
                }
            };
        }
        getTuyaDevices() ;
        loginTTLock();
        getHotelTempSetpoint();
    }

    private void loginTTLock()
    {
        Log.d("TTLOCKLogin" , "started");
        ApiService apiService = RetrofitAPIManager.provideClientApi();
        String user = "basharsebai@gmail.com";
        String pass = "Freesyria579251";
        pass = DigitUtil.getMD5(pass);
        Call<String> call = apiService.auth(ApiService.CLIENT_ID, ApiService.CLIENT_SECRET, "password", user, pass, ApiService.REDIRECT_URI);
        String finalPass = pass;
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response)
            {
                String json = response.body();
                accountInfo = GsonUtil.toObject(json, AccountInfo.class);
                if (accountInfo != null)
                {
                    if (accountInfo.errcode == 0)
                    {
                        Log.d("TTLOCKLogin" , "success");
                        //Toast.makeText(act,"login success",Toast.LENGTH_LONG).show();
                        accountInfo.setMd5Pwd(finalPass);
                        acc = accountInfo;
                        Log.d("TTLOCKLogin" , accountInfo.getAccess_token());
                        getLocks();
                    }
                    else
                    {
                       // Toast.makeText(act,accountInfo.errmsg,Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    //Toast.makeText(act,"acc = null",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                //d.dismiss();
                //ToastMaker.MakeToast(t.getMessage() , act);
                Calendar x = Calendar.getInstance(Locale.getDefault());
                long time =  x.getTimeInMillis();
                //ErrorRegister.rigestError(activ , LogIn.room.getProjectName() , LogIn.room.getRoomNumber() , time ,004 ,accountInfo.errmsg , "LogIn To TTlock Account" );
            }
        });
    }

    private void getLocks()
    {
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.loading_layout);
        d.setCancelable(false);
        d.show();
        ApiService apiService = RetrofitAPIManager.provideClientApi();
        Call<String> call = apiService.getLockList(ApiService.CLIENT_ID,acc.getAccess_token(), 1, 100, System.currentTimeMillis());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response)
            {
                d.dismiss();
                String json = response.body();
                if (json.contains("list"))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(json);
                        JSONArray array = jsonObject.getJSONArray("list");
                        Locks = GsonUtil.toObject(array.toString(), new TypeToken<ArrayList<LockObj>>(){});
                        Log.d("locksNum" ,String.valueOf( Locks.size() ));
                        //Toast.makeText(act,"Locks Are "+String.valueOf( Locks.size()),Toast.LENGTH_LONG).show();
                    }
                    catch (JSONException e)
                    {
                    }
                    setLocks(Locks);
                }
                else
                {
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
            }
        });
    }

    void setTuyaApplication()
    {
        TuyaHomeSdk.setDebugMode(true);
        TuyaHomeSdk.init(getApplication());
        TuyaHomeSdk.setOnNeedLoginListener(new INeedLoginListener() {
            @Override
            public void onNeedLogin(Context context) {
                Intent intent = new Intent(context, Tuya_Login.class);
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
            }
        });

    }

    void getTuyaDevices()
    {
        UnRecognizedDevices_B.clear();
        lodingDialog loading = new lodingDialog(act);
        TuyaHomeSdk.newHomeInstance(Login.THEHOME.getHomeId()).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean)
            {
                loading.stop();
                Devices = homeBean.getDeviceList();
                //String[] dd = new String[Devices.size()];
//                for (int i=0;i<Devices.size();i++)
//                {
//                    dd[i] = Devices.get(i).name ;
//                    Log.d("deviceOnline",Devices.get(i).name +" local: "+Devices.get(i).getIsLocalOnline()+ " net: "+Devices.get(i).getIsOnline().toString()+" cloud: "+Devices.get(i).isCloudOnline());
//                }
                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(act,R.layout.spinners_item,dd);
                Devices_Adapter adapter = new Devices_Adapter(Devices,act);
                devicesListView.setAdapter(adapter);
                if (Devices.size() == 0)
                {
                    Toast.makeText(act,"no devices",Toast.LENGTH_LONG).show();
                    Log.d("devicesAre " ,"no devices" );
                }
                else
                {
                    Toast.makeText(act,String.valueOf(Devices.size()),Toast.LENGTH_LONG).show();
                    devicesCount.setText(String.valueOf(Devices.size()));
                    Log.d("devicesAre " ,String.valueOf(Devices.size()) );
                    for (int i = 0 ; i < Devices.size() ; i++ )
                    {
                        for (int j = 0 ; j < list.size() ; j++ )
                        {
                            if (Devices.get(i).getName().equals(list.get(j).RoomNumber+"Power"))
                            {
                                list.get(j).setPOWER_B(Devices.get(i));
                                list.get(j).setPOWER(TuyaHomeSdk.newDeviceInstance(list.get(j).getPOWER_B().devId));
                            }
                            else if (Devices.get(i).getName().equals(list.get(j).RoomNumber+"ZGatway"))
                            {
                                list.get(j).setGATEWAY_B(Devices.get(i));
                                try
                                {
                                    list.get(j).setGATEWAY(TuyaHomeSdk.newDeviceInstance(list.get(j).getGATEWAY_B().devId));
                                    list.get(j).setWiredZBGateway(TuyaHomeSdk.newGatewayInstance(list.get(j).getGATEWAY_B().devId));
                                    if (list.get(j).ZBGateway == 0 ) {
                                        setZBGatewayStatus(list.get(j),"1");
                                    }
                                }
                                catch (Exception e)
                                {

                                }
                            }
                            else if (Devices.get(i).getName().equals(list.get(j).RoomNumber+"AC"))
                            {
                                list.get(j).setAC_B(Devices.get(i));
                                list.get(j).setAC(TuyaHomeSdk.newDeviceInstance(list.get(j).getAC_B().devId));

                            }
                            else if (Devices.get(i).getName().equals(list.get(j).RoomNumber+"DoorSensor"))
                            {
                                list.get(j).setDOORSENSOR_B(Devices.get(i));
                                list.get(j).setDOORSENSOR(TuyaHomeSdk.newDeviceInstance(list.get(j).getDOORSENSOR_B().devId));
                            }
                            else if (Devices.get(i).getName().equals(list.get(j).RoomNumber+"MotionSensor"))
                            {
                                list.get(j).setMOTIONSENSOR_B(Devices.get(i));
                                list.get(j).setMOTIONSENSOR(TuyaHomeSdk.newDeviceInstance(list.get(j).getMOTIONSENSOR_B().devId));

                            }
                            else if (Devices.get(i).getName().equals(list.get(j).RoomNumber+"Curtain"))
                            {
                                list.get(j).setCURTAIN_B(Devices.get(i));
                                list.get(j).setCURTAIN(TuyaHomeSdk.newDeviceInstance(list.get(j).getCURTAIN_B().devId));

                            }
                            else if (Devices.get(i).getName().equals(list.get(j).RoomNumber+"ServiceSwitch"))
                            {
                                list.get(j).setSERVICE_B(Devices.get(i));
                                list.get(j).setSERVICE(TuyaHomeSdk.newDeviceInstance(list.get(j).getSERVICE_B().devId));

                            }
                            else if (Devices.get(i).getName().equals(list.get(j).RoomNumber+"Switch1"))
                            {
                                list.get(j).setSWITCH1_B(Devices.get(i));
                                list.get(j).setSWITCH1(TuyaHomeSdk.newDeviceInstance(list.get(j).getSWITCH1_B().devId));

                            }
                            else if (Devices.get(i).getName().equals(list.get(j).RoomNumber+"Switch2"))
                            {
                                list.get(j).setSWITCH2_B(Devices.get(i));
                                list.get(j).setSWITCH2(TuyaHomeSdk.newDeviceInstance(list.get(j).getSWITCH2_B().devId));

                            }
                            else if (Devices.get(i).getName().equals(list.get(j).RoomNumber+"Switch3"))
                            {
                                list.get(j).setSWITCH3_B(Devices.get(i));
                                list.get(j).setSWITCH3(TuyaHomeSdk.newDeviceInstance(list.get(j).getSWITCH3_B().devId));

                            }
                            else if (Devices.get(i).getName().equals(list.get(j).RoomNumber+"Switch4"))
                            {
                                list.get(j).setSWITCH4_B(Devices.get(i));
                                list.get(j).setSWITCH4(TuyaHomeSdk.newDeviceInstance(list.get(j).getSWITCH4_B().devId));

                            }
                            else
                            {
                                UnRecognizedDevices_B.add(Devices.get(i));
                            }

//                            if (list.get(j).getSWITCH3() == null ) {
//                                setSwitch1DB2(list.get(j));
//                            }
//                            else {
//                                bindSwitch1ToSwitch3btn1(list.get(j));
//                                bindSwitch1ToSwitch3btn2(list.get(j));
//                            }

                            //setSwitch1DB1(list.get(j));

                            //setSwitch1DB3(list.get(j));
                            //setSwitch1DB4(list.get(j));
                        }
                    }
                    if (UnRecognizedDevices_B.size() > 0 )
                    {
                        Toast.makeText(act,"Some Devices Not Recognized" , Toast.LENGTH_LONG);
                        Log.d("unrecognized " , String.valueOf(UnRecognizedDevices_B.size()+" all "+Devices.size() ));
                    }
                }
                //setDevicesListiners();
                //setFireRoomsListiner();
                getSceneBGs();
            }
            @Override
            public void onError(String errorCode, String errorMsg)
            {
                loading.stop();
                //long time = ca.getTimeInMillis();
                //ErrorRegister.rigestError(act,LogIn.room.getProjectName(),LogIn.room.getRoomNumber(),time,16,errorMsg,"error Getting Project Registered Devices");
            }
        });
    }

    void setLocks(ArrayList<LockObj> Locks)
    {
        for (int j=0;j<Locks.size();j++)
        {
            Log.d("locks" , Locks.get(j).getLockName());
            for (int i=0;i<list.size();i++)
            {
                if (Locks.get(j).getLockName().equals(list.get(i).RoomNumber+"Lock"))
                {
                    list.get(i).setLock(Locks.get(j));
                    break;
                }
            }
        }

    }

    public void resetAllDevices(View view)
    {

        Dialog  dd = new Dialog(act);
        dd.setContentView(R.layout.lock_unlock_dialog);
        Button cancel = (Button) dd.findViewById(R.id.confermationDialog_cancel);
        Button lock = (Button) dd.findViewById(R.id.messageDialog_ok);
        TextView title = (TextView) dd.findViewById(R.id.textView2);
        TextView message = (TextView) dd.findViewById(R.id.confermationDialog_Text);
        title.setText("Remove All Devices ?");
        message.setText("Are You Sure You want to remove all devices ??");
        EditText password = (EditText) dd.findViewById(R.id.editTextTextPassword);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dd.dismiss();
            }
        });
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final lodingDialog loading = new lodingDialog(act);
                final String pass = password.getText().toString() ;
                StringRequest re = new StringRequest(Request.Method.POST, LogInUrl, new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d("LoginResult" , response +" "+ LogInUrl );
                        loading.stop();
                        if (response.equals("1"))
                        {
                            dd.dismiss();
                            lodingDialog loading = new lodingDialog(act);
                            //removeParameter=0;
                            for (int i=0;i<Devices.size();i++)
                            {
                                int finalI = i;
                                TuyaHomeSdk.newDeviceInstance(Devices.get(i).getDevId()).removeDevice(new IResultCallback()
                                {

                                    @Override
                                    public void onError(String code, String error)
                                    {
                                        Toast.makeText(act,error,Toast.LENGTH_LONG).show();
                                    }
                                    @Override
                                    public void onSuccess()
                                    {
                                        //Devices.remove(finalI);
                                    }
                                });

                            }

                            String url = Login.SelectedHotel.URL+"removeAllDevices.php";
                            StringRequest re = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response)
                                {
                                    loading.stop();
                                    Toast.makeText(act,response,Toast.LENGTH_LONG).show();
                                    if (response.equals("1"))
                                    {
                                        Toast.makeText(act,"Devices Removed",Toast.LENGTH_LONG).show();
                                        getRooms();
                                    }

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error)
                                {
                                    loading.stop();
                                    Toast.makeText(act,error.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            })
                            {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError
                                {
                                    Map<String,String> Param = new HashMap<String, String>();
                                    Param.put("hotel","1");
                                    return Param;
                                }
                            };
                            Volley.newRequestQueue(act).add(re);

                        }
                        else if (response.equals("0"))
                        {
                            Toast.makeText(act,"Lock Failed",Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(act,"No Params",Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        loading.stop();
                    }
                })
                {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError
                    {
                        Map<String,String> par = new HashMap<String, String>();
                        par.put( "password" , pass ) ;
                        par.put( "hotel" , "1" ) ;
                        return par;
                    }
                };
                Volley.newRequestQueue(act).add(re);
            }
        });
        dd.show();



    }

    public void setDevicesListiners()
    {
        Toast.makeText(act,"Rooms are : "+list.size(),Toast.LENGTH_LONG).show();
        for (int i=0;i<list.size();i++)
        {
            int finalI3 = i;
                if(list.get(i).getDOORSENSOR_B() != null )
                {
                    int finalI = i;
                    list.get(i).getDOORSENSOR().registerDeviceListener(new IDeviceListener() {
                        @Override
                        public void onDpUpdate(String devId, Map<String, Object> dpStr)
                        {
                            //Toast.makeText(act,list.get(finalI).RoomNumber+"door",Toast.LENGTH_SHORT).show();
                            if (dpStr.get("doorcontact_state") != null )
                            {

                                if (dpStr.get("doorcontact_state").toString().equals("true") )
                                {
                                    FireRooms.get(finalI).child("doorStatus").setValue(1);
                                    start[finalI] = System.currentTimeMillis() ;
                                    DoorStart[finalI] = System.currentTimeMillis() ;
                                    AC_SENARIO_Status[finalI] = true ;
                                    DOORSTATUS[finalI] = true ;
                                    period[finalI] = 0;
                                    DoorPeriod[finalI]= 0;
                                    //TempRonnableList[finalI].run();
                                    DoorRunnable[finalI].run();
                                    setDoorOpenClosed(list.get(finalI3),"1");

                                }
                                else
                                {
                                    if (Handlers[finalI] != null) {
                                        Handlers[finalI].removeCallbacks(DoorRunnable[finalI]);
                                    }
                                    FireRooms.get(finalI).child("doorStatus").setValue(0);
                                    DOORSTATUS[finalI] = false ;
                                    setDoorOpenClosed(list.get(finalI3),"0");
                                }
                            }
                        }
                        @Override
                        public void onRemoved(String devId)
                        {
                            Log.d("DoorSensor" , "Removed" );
                            //setDoorSensorStatus(list.get(finalI),"0");
                        }
                        @Override
                        public void onStatusChanged(String devId, boolean online)
                        {
                            Log.d("DoorSensor" , "status changed " + online );

                        }

                        @Override
                        public void onNetworkStatusChanged(String devId, boolean status)
                        {
                            Log.d("DoorSensor" , "network status changed " + status );
                        }

                        @Override
                        public void onDevInfoUpdate(String devId)
                        {
                            Log.d("DoorSensor" , "DevInfo"  );
                        }
                    });
                }
                if (list.get(i).getSERVICE_B() != null)
                {
                    CLEANUP[i] = Boolean.getBoolean(list.get(i).getSERVICE_B().dps.get("2").toString()) ;
                    LAUNDRY[i] = Boolean.getBoolean( list.get(i).getSERVICE_B().dps.get("3").toString());
                    DND[i] = Boolean.getBoolean( list.get(i).getSERVICE_B().dps.get("1").toString());
                    CHECKOUT[i] = Boolean.getBoolean(list.get(i).getSERVICE_B().dps.get("4").toString());
                    int finalI1 = i;
                    list.get(i).getSERVICE().registerDeviceListener(new IDeviceListener() {
                        @Override
                        public void onDpUpdate(String devId, Map<String, Object> dpStr)
                        {
                            Log.d("servicePressed",list.get(finalI3).roomStatus+" "+dpStr);
                            if (list.get(finalI3).roomStatus == 2) {
                                if (dpStr.get("switch_2") != null) {
                                    if (dpStr.get("switch_2").toString().equals("true") && !CLEANUP[finalI1]) {
                                        Calendar c = Calendar.getInstance(Locale.getDefault());
                                        long time = c.getTimeInMillis();
                                        CLEANUP[finalI1] = true;
                                        FireRooms.get(finalI1).child("Cleanup").setValue(time);
                                        FireRooms.get(finalI1).child("dep").setValue("Cleanup");
                                        addCleanupOrder(list.get(finalI1), finalI1);
                                        for (ServiceEmps u : Emps) {
                                            if (u.department.equals("Cleanup") || u.department.equals("Service")) {
                                                makemessage(u.token, "Cleanup", true, list.get(finalI1).RoomNumber);
                                            }
                                        }
//                                    if (list.get(finalI1).getSERVICE_B().dps.get("1").toString().equals("true")) {
//                                        DND[finalI1] = false ;
//                                        list.get(finalI1).getSERVICE().publishDps("{\"1\": false}", new IResultCallback() {
//                                            @Override
//                                            public void onError(String code, String error) {
//
//                                            }
//
//                                            @Override
//                                            public void onSuccess() {
//
//                                            }
//                                        });
//                                        FireRooms.get(finalI1).child("DND").setValue(0);
//                                    }
                                    } else if (dpStr.get("switch_2").toString().equals("false") && CLEANUP[finalI1]) {
                                        CLEANUP[finalI1] = false;
                                        cancelCleanupOrder(list.get(finalI1), finalI1);
                                        FireRooms.get(finalI1).child("Cleanup").setValue(0);
                                        for (ServiceEmps u : Emps) {
                                            if (u.department.equals("Cleanup") || u.department.equals("Service")) {
                                                makemessage(u.token, "Cleanup", false, list.get(finalI1).RoomNumber);
                                            }
                                        }
                                        if (DND[finalI1]) {
                                            FireRooms.get(finalI1).child("dep").setValue("DND");
                                        } else if (LAUNDRY[finalI1]) {
                                            FireRooms.get(finalI1).child("dep").setValue("Laundry");
                                        } else {
                                            FireRooms.get(finalI1).child("dep").setValue(0);
                                        }
                                    }
                                }
                                if (dpStr.get("switch_3") != null) {
                                    if (dpStr.get("switch_3").toString().equals("true") && !LAUNDRY[finalI1]) {
                                        Calendar c = Calendar.getInstance(Locale.getDefault());
                                        long time = c.getTimeInMillis();
                                        LAUNDRY[finalI1] = true;
                                        addLaundryOrder(list.get(finalI1), finalI1);
                                        FireRooms.get(finalI1).child("Laundry").setValue(time);
                                        FireRooms.get(finalI1).child("dep").setValue("Laundry");
                                        for (ServiceEmps u : Emps) {
                                            if (u.department.equals("Laundry") || u.department.equals("Service")) {
                                                makemessage(u.token, "Laundry", true, list.get(finalI1).RoomNumber);
                                            }
                                        }
//                                    if (list.get(finalI1).getSERVICE_B().dps.get("1").toString().equals("true"))
//                                    {
//                                        list.get(finalI1).getSERVICE().publishDps("{\"1\": false}", new IResultCallback() {
//                                            @Override
//                                            public void onError(String code, String error) {
//
//                                            }
//
//                                            @Override
//                                            public void onSuccess() {
//
//                                            }
//                                        });
//                                        DND[finalI1] = false ;
//                                        FireRooms.get(finalI1).child("DND").setValue(0);
//                                    }
                                    } else if (dpStr.get("switch_3").toString().equals("false") && LAUNDRY[finalI1]) {
                                        LAUNDRY[finalI1] = false;
                                        cancelLaundryOrder(list.get(finalI1), finalI1);
                                        FireRooms.get(finalI1).child("Laundry").setValue(0);
                                        for (ServiceEmps u : Emps) {
                                            if (u.department.equals("Laundry") || u.department.equals("Service")) {
                                                makemessage(u.token, "Laundry", false, list.get(finalI1).RoomNumber);
                                            }
                                        }
                                        if (DND[finalI1]) {
                                            FireRooms.get(finalI1).child("dep").setValue("DND");
                                        } else if (CLEANUP[finalI1]) {
                                            FireRooms.get(finalI1).child("dep").setValue("Cleanup");
                                        } else {
                                            FireRooms.get(finalI1).child("dep").setValue(0);
                                        }
                                    }
                                }
                                if (dpStr.get("switch_1") != null) {
                                    if (dpStr.get("switch_1").toString().equals("true") && !DND[finalI1]) {
                                        Calendar c = Calendar.getInstance(Locale.getDefault());
                                        long time = c.getTimeInMillis();
                                        DND[finalI1] = true;
                                        FireRooms.get(finalI1).child("DND").setValue(time);
                                        FireRooms.get(finalI1).child("dep").setValue("DND");
                                        addDNDOrder(list.get(finalI1), finalI1);
                                        for (ServiceEmps u : Emps) {
                                            if (u.department.equals("Service") || u.department.equals("Cleanup") || u.department.equals("Laundry") || u.department.equals("RoomService")) {
                                                makemessage(u.token, "DND", true, list.get(finalI1).RoomNumber);
                                            }
                                        }
//                                    if (list.get(finalI1).getSERVICE_B().dps.get("2").toString().equals("true"))
//                                    {
//                                        list.get(finalI1).getSERVICE().publishDps("{\"2\": false}", new IResultCallback() {
//                                            @Override
//                                            public void onError(String code, String error) {
//
//                                            }
//
//                                            @Override
//                                            public void onSuccess() {
//
//                                            }
//                                        });
//                                        CLEANUP[finalI1] = false ;
//                                        FireRooms.get(finalI1).child("Cleanup").setValue(0);
//                                        cancelCleanupOrder(list.get(finalI1) , finalI1);
//                                    }
//                                    if (list.get(finalI1).getSERVICE_B().dps.get("3").toString().equals("true"))
//                                    {
//                                        list.get(finalI1).getSERVICE().publishDps("{\"3\": false}", new IResultCallback() {
//                                            @Override
//                                            public void onError(String code, String error) {
//
//                                            }
//
//                                            @Override
//                                            public void onSuccess() {
//
//                                            }
//                                        });
//                                        LAUNDRY[finalI1] = false ;
//                                        FireRooms.get(finalI1).child("Laundry").setValue(0);
//                                        cancelLaundryOrder(list.get(finalI1) , finalI1);
//                                    }
//                                    if (list.get(finalI1).getSERVICE_B().dps.get("4").toString().equals("true"))
//                                    {
//                                        list.get(finalI1).getSERVICE().publishDps("{\"4\": false}", new IResultCallback() {
//                                            @Override
//                                            public void onError(String code, String error) {
//
//                                            }
//
//                                            @Override
//                                            public void onSuccess() {
//
//                                            }
//                                        });
//                                        CHECKOUT[finalI1] = false ;
//                                        FireRooms.get(finalI1).child("Checkout").setValue(0);
//                                        cancelCheckoutOrder(list.get(finalI1) , finalI1);
//                                    }
                                    } else if (dpStr.get("switch_1").toString().equals("false") && DND[finalI1]) {
                                        DND[finalI1] = false;
                                        cancelDNDOrder(list.get(finalI1), finalI1);
                                        FireRooms.get(finalI1).child("DND").setValue(0);
                                        for (ServiceEmps u : Emps) {
                                            if (u.department.equals("Service") || u.department.equals("Cleanup") || u.department.equals("Laundry") || u.department.equals("RoomService")) {
                                                makemessage(u.token, "DND", false, list.get(finalI1).RoomNumber);
                                            }
                                        }
                                        if (LAUNDRY[finalI1]) {
                                            FireRooms.get(finalI1).child("dep").setValue("Laundry");
                                        } else if (CLEANUP[finalI1]) {
                                            FireRooms.get(finalI1).child("dep").setValue("Cleanup");
                                        } else {
                                            FireRooms.get(finalI1).child("dep").setValue(0);
                                        }
                                    }
                                }
                                if (dpStr.get("switch_4") != null) {
                                    if (dpStr.get("switch_4").toString().equals("true") && !CHECKOUT[finalI1]) {
                                        Calendar c = Calendar.getInstance(Locale.getDefault());
                                        long time = c.getTimeInMillis();
                                        CHECKOUT[finalI1] = true;
                                        FireRooms.get(finalI1).child("Checkout").setValue(time);
                                        FireRooms.get(finalI1).child("dep").setValue("Checkout");
                                        addCheckoutOrder(list.get(finalI1), finalI1);
//                                    if (list.get(finalI1).getSERVICE_B().dps.get("1").toString().equals("true"))
//                                    {
//                                        DND[finalI1] = false ;
//                                        list.get(finalI1).getSERVICE().publishDps("{\"1\": false}", new IResultCallback() {
//                                            @Override
//                                            public void onError(String code, String error) {
//
//                                            }
//
//                                            @Override
//                                            public void onSuccess() {
//
//                                            }
//                                        });
//                                        FireRooms.get(finalI1).child("DND").setValue(0);
//                                        //cancelDNDOrder(list.get(finalI1),finalI1);
//                                    }
                                    } else if (dpStr.get("switch_4").toString().equals("false") && CHECKOUT[finalI1]) {
                                        //Log.d("checkoutProblem" , " from here " + CheckoutStatus+dpStr.get("switch_4").toString() ) ;
                                        CHECKOUT[finalI1] = false;
                                        FireRooms.get(finalI1).child("Checkout").setValue(0);
                                        cancelCheckoutOrder(list.get(finalI1), finalI1);
                                    }
                                }
                            }
                        }
                        @Override
                        public void onRemoved(String devId)
                        {
                            setServiceSwitchStatus(list.get(finalI1),"0");
                        }
                        @Override
                        public void onStatusChanged(String devId, boolean online) {

                        }

                        @Override
                        public void onNetworkStatusChanged(String devId, boolean status) {

                        }

                        @Override
                        public void onDevInfoUpdate(String devId) {

                        }
                    });
                }
                if (list.get(i).getAC_B() !=null)
                {
//                    int finalI2 = i;
//                    list.get(i).getAC().registerDeviceListener(new IDeviceListener() {
//                        @Override
//                        public void onDpUpdate(String devId, Map<String, Object> dpStr)
//                        {
//                            //Toast.makeText(act,list.get(finalI2).RoomNumber+"AC",Toast.LENGTH_LONG).show();
//                            if (dpStr.get("temp_current") != null)
//                            {
//                                double temp = (Integer.parseInt(dpStr.get("temp_current").toString())*0.1);
//                                FireRooms.get(finalI2).child("temp").setValue(temp) ;
//                            }
//                            if ( dpStr.get("temp_set") != null )
//                            {
//                                if (Double.parseDouble(dpStr.get("temp_set").toString()) !=  Double.parseDouble(TempSetPoint[finalI2]))
//                                {
//                                    ClientTemp[finalI2] = dpStr.get("temp_set").toString();
//                                }
//
//                            }
//                        }
//                        @Override
//                        public void onRemoved(String devId)
//                        {
//                            setThermostatStatus(list.get(finalI2) , "0");
//                        }
//                        @Override
//                        public void onStatusChanged(String devId, boolean online) {
//
//                        }
//
//                        @Override
//                        public void onNetworkStatusChanged(String devId, boolean status) {
//
//                        }
//
//                        @Override
//                        public void onDevInfoUpdate(String devId) {
//
//                        }
//                    });
                }
                if (list.get(i).getPOWER_B() != null)
                {
                    list.get(i).getPOWER().registerDeviceListener(new IDeviceListener() {
                        @Override
                        public void onDpUpdate(String devId, Map<String, Object> dpStr)
                        {
                            String DP1 ;
                            String DP2 ;
                            String DP8 ;
                            Log.d("powerReact" , dpStr.toString());
//                            if (DP1.equals("false") && DP2.equals("false")) {
//                                Log.d("powerReact" , "all off");
//                                FireRooms.get(finalI3).child("powerStatus").setValue(0);
//                                setPowerOnOff(list.get(finalI3),"0");
//                            }
//                            else if (DP1.equals("true") && DP2.equals("false")) {
//                                Log.d("powerReact" , "one on tow off");
//                            }
//                            else if (DP1.equals("true") && DP2.equals("true")) {
//                                Log.d("powerReact" , "one on tow on");
//                                FireRooms.get(finalI3).child("powerStatus").setValue(2);
//                                setPowerOnOff(list.get(finalI3),"2");
//                            }
                            if (dpStr != null) {
                                if (dpStr.get("switch_1") != null) {
                                    DP1 = dpStr.get("switch_1").toString();
                                    if (DP1.equals("false")) {
                                        FireRooms.get(finalI3).child("powerStatus").setValue(0);
                                        setPowerOnOff(list.get(finalI3),"0");
                                    }
                                }
                                if (dpStr.get("switch_2") != null) {
                                    DP2 = dpStr.get("switch_2").toString();
                                    if (DP2.equals("true")) {
                                        FireRooms.get(finalI3).child("powerStatus").setValue(2);
                                        setPowerOnOff(list.get(finalI3),"2");
                                    }
                                    else {
                                        FireRooms.get(finalI3).child("powerStatus").setValue(1);
                                        setPowerOnOff(list.get(finalI3),"1");
                                    }

                                }
                            }
                        }

                        @Override
                        public void onRemoved(String devId)
                        {
                            setPowerSwitchStatus(list.get(finalI3),"0");
                        }
                        @Override
                        public void onStatusChanged(String devId, boolean online) {

                        }

                        @Override
                        public void onNetworkStatusChanged(String devId, boolean status) {

                        }

                        @Override
                        public void onDevInfoUpdate(String devId) {

                        }
                    });
                }
                if (list.get(i).getCURTAIN_B() != null)
                {
//                    list.get(i).getCURTAIN().registerDeviceListener(new IDeviceListener() {
//                        @Override
//                        public void onDpUpdate(String devId, Map<String, Object> dpStr)
//                        {
//                            //Toast.makeText(act,list.get(finalI3).RoomNumber+"curtain",Toast.LENGTH_LONG).show();
//                        }
//
//                        @Override
//                        public void onRemoved(String devId)
//                        {
//                            setCurtainSwitchStatus(list.get(finalI3),"0");
//                        }
//                        @Override
//                        public void onStatusChanged(String devId, boolean online) {
//
//                        }
//
//                        @Override
//                        public void onNetworkStatusChanged(String devId, boolean status) {
//
//                        }
//
//                        @Override
//                        public void onDevInfoUpdate(String devId) {
//
//                        }
//                    });
                }
                if (list.get(i).getMOTIONSENSOR_B() != null )
                {
//                    list.get(i).getMOTIONSENSOR().registerDeviceListener(new IDeviceListener() {
//                        @Override
//                        public void onDpUpdate(String devId, Map<String, Object> dpStr)
//                        {
//                            Log.d("motion" , dpStr.toString());
//                            //Toast.makeText(act,list.get(finalI3).RoomNumber+"motion",Toast.LENGTH_LONG).show();
//                            if (AC_SENARIO_Status[finalI3])
//                            {
//                                AC_SENARIO_Status[finalI3] = false ;
//                            }
//                            else
//                            {
//                                String t ="";
//                                if (ClientTemp[finalI3].equals("0"))
//                                {
//                                    t="240";
//                                }
//                                else
//                                {
//                                    t = ClientTemp[finalI3] ;
//                                }
//                                String dp = "{\" 2\": "+t+"}";
//                                if (list.get(finalI3).getAC() != null )
//                                {
//                                    list.get(finalI3).getAC().publishDps(dp, new IResultCallback() {
//                                        @Override
//                                        public void onError(String code, String error) {
//
//                                        }
//
//                                        @Override
//                                        public void onSuccess()
//                                        {
//                                            //ToastMaker.MakeToast("Temp Set to Client Temp " , act);
//
//                                        }
//                                    });
//                                }
//
//
//                            }
//                        }
//
//                        @Override
//                        public void onRemoved(String devId)
//                        {
//                            setMotionSensorStatus(list.get(finalI3),"0");
//                        }
//                        @Override
//                        public void onStatusChanged(String devId, boolean online) {
//
//                        }
//
//                        @Override
//                        public void onNetworkStatusChanged(String devId, boolean status) {
//
//                        }
//
//                        @Override
//                        public void onDevInfoUpdate(String devId) {
//
//                        }
//                    });
                }
                if (list.get(i).getSWITCH1_B() != null)
                {
//                    list.get(i).getSWITCH1().registerDeviceListener(new IDeviceListener() {
//                        @Override
//                        public void onDpUpdate(String devId, Map<String, Object> dpStr)
//                        {
//                            if (dpStr.get("switch_3") != null ){
//
//                                if (dpStr.get("switch_3").toString().equals("true")) {
//
//                                    if (list.get(finalI3).getSWITCH1_B().dps.get("2").toString().equals("true")) {
//                                        list.get(finalI3).getSWITCH1().publishDps("{\" 2\":false}", new IResultCallback() {
//                                            @Override
//                                            public void onError(String code, String error) {
//
//                                            }
//
//                                            @Override
//                                            public void onSuccess() {
//
//                                            }
//                                        });
//                                    }
//                                    if (list.get(finalI3).getSWITCH1_B().dps.get("1").toString().equals("true")) {
//                                        list.get(finalI3).getSWITCH1().publishDps("{\" 1\":false}", new IResultCallback() {
//                                            @Override
//                                            public void onError(String code, String error) {
//
//                                            }
//
//                                            @Override
//                                            public void onSuccess() {
//
//                                            }
//                                        });
//                                    }
//                                    if (list.get(finalI3).getSWITCH2_B() != null ) {
//                                        if (list.get(finalI3).getSWITCH2_B().dps.get("1").toString().equals("true")) {
//                                            list.get(finalI3).getSWITCH2().publishDps("{\" 1\":false}", new IResultCallback() {
//                                                @Override
//                                                public void onError(String code, String error) {
//
//                                                }
//
//                                                @Override
//                                                public void onSuccess() {
//
//                                                }
//                                            });
//                                        }
//                                        if (list.get(finalI3).getSWITCH2_B().dps.get("2").toString().equals("true")) {
//                                            list.get(finalI3).getSWITCH2().publishDps("{\" 2\":false}", new IResultCallback() {
//                                                @Override
//                                                public void onError(String code, String error) {
//
//                                                }
//
//                                                @Override
//                                                public void onSuccess() {
//
//                                                }
//                                            });
//                                        }
//                                    }
//                                }
//                            }
//                            if (dpStr.get("switch_2") != null ){
//                                if (dpStr.get("switch_2").toString().equals("true")){
//
//                                    if (list.get(finalI3).getSWITCH1_B().dps.get("3").toString().equals("true")) {
//                                        list.get(finalI3).getSWITCH1().publishDps("{\" 3\":false}", new IResultCallback() {
//                                            @Override
//                                            public void onError(String code, String error) {
//
//                                            }
//
//                                            @Override
//                                            public void onSuccess() {
//
//                                            }
//                                        });
//                                    }
//                                }
//                                else {
//
//                                }
//                            }
//                            if (dpStr.get("switch_1") != null ){
////                                                if (dpStr.get("switch_3").toString().equals("true")){
////                                                    Button b1 = (Button)findViewById(R.id.button17);
////                                                    b1.setBackgroundResource(R.drawable.light_on);
////                                                }
////                                                else {
////                                                    Button b1 = (Button)findViewById(R.id.button17);
////                                                    b1.setBackgroundResource(R.drawable.group_62);
////                                                }
//                                if (dpStr.get("switch_1").toString().equals("true")){
//                                    if (list.get(finalI3).getSWITCH1_B().dps.get("3").toString().equals("true")) {
//                                        list.get(finalI3).getSWITCH1().publishDps("{\" 3\":false}", new IResultCallback() {
//                                            @Override
//                                            public void onError(String code, String error) {
//
//                                            }
//
//                                            @Override
//                                            public void onSuccess() {
//
//                                            }
//                                        });
//                                    }
//                                }
//                                else {
//                                }
//
//                            }
//                        }
//
//                        @Override
//                        public void onRemoved(String devId)
//                        {
//                            setSwitch1Status(list.get(finalI3),"0");
//                        }
//                        @Override
//                        public void onStatusChanged(String devId, boolean online) {
//
//                        }
//
//                        @Override
//                        public void onNetworkStatusChanged(String devId, boolean status) {
//
//                        }
//
//                        @Override
//                        public void onDevInfoUpdate(String devId) {
//
//                        }
//                    });
                }
                if (list.get(i).getSWITCH2_B() != null)
                {
//                    list.get(i).getSWITCH2().registerDeviceListener(new IDeviceListener() {
//                        @Override
//                        public void onDpUpdate(String devId, Map<String, Object> dpStr)
//                        {
//                            Log.d("switch2Data" , dpStr.toString());
//                            if (dpStr.get("switch_1") != null ) {
//                                if (dpStr.get("switch_1").toString().equals("true")) {
//                                    if (list.get(finalI3).getSWITCH1_B() != null) {
//                                        if (list.get(finalI3).getSWITCH1_B().dps.get("3").toString().equals("true")) {
//                                            list.get(finalI3).getSWITCH1().publishDps("{\" 3\":false}", new IResultCallback() {
//                                                @Override
//                                                public void onError(String code, String error) {
//
//                                                }
//
//                                                @Override
//                                                public void onSuccess() {
//
//                                                }
//                                            });
//                                        }
//                                    }
//                                }
//                            }
//                            if (dpStr.get("switch_2") != null ) {
//                                if (dpStr.get("switch_2").toString().equals("true")) {
//                                    if (list.get(finalI3).getSWITCH1_B() != null) {
//                                        if (list.get(finalI3).getSWITCH1_B().dps.get("3").toString().equals("true")) {
//                                            list.get(finalI3).getSWITCH1().publishDps("{\" 3\":false}", new IResultCallback() {
//                                                @Override
//                                                public void onError(String code, String error) {
//
//                                                }
//
//                                                @Override
//                                                public void onSuccess() {
//
//                                                }
//                                            });
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onRemoved(String devId)
//                        {
//                            setSwitch2Status(list.get(finalI3),"0");
//                        }
//                        @Override
//                        public void onStatusChanged(String devId, boolean online) {
//
//                        }
//
//                        @Override
//                        public void onNetworkStatusChanged(String devId, boolean status) {
//
//                        }
//
//                        @Override
//                        public void onDevInfoUpdate(String devId) {
//
//                        }
//                    });
                }
                if (list.get(i).getSWITCH3_B() != null)
                {
//                    list.get(i).getSWITCH3().registerDeviceListener(new IDeviceListener() {
//                        @Override
//                        public void onDpUpdate(String devId, Map<String, Object> dpStr)
//                        {
//                            //Toast.makeText(act,list.get(finalI3).RoomNumber+"switch3",Toast.LENGTH_LONG).show();
//                        }
//
//                        @Override
//                        public void onRemoved(String devId)
//                        {
//                            setSwitch3Status(list.get(finalI3),"0");
//                        }
//                        @Override
//                        public void onStatusChanged(String devId, boolean online) {
//
//                        }
//
//                        @Override
//                        public void onNetworkStatusChanged(String devId, boolean status) {
//
//                        }
//
//                        @Override
//                        public void onDevInfoUpdate(String devId) {
//
//                        }
//                    });
                }
                if (list.get(i).getSWITCH4_B() != null)
                {
//                    list.get(i).getSWITCH4().registerDeviceListener(new IDeviceListener() {
//                        @Override
//                        public void onDpUpdate(String devId, Map<String, Object> dpStr)
//                        {
//                            //Toast.makeText(act,list.get(finalI3).RoomNumber+"switch4",Toast.LENGTH_LONG).show();
//                            Log.d("switch4" , dpStr.toString() );
//                            if ( dpStr.get("switch_1") != null )
//                            {
//                                if (dpStr.get("switch_1").toString().equals("true")) {
//                                    if (list.get(finalI3).getLock() != null) {
//                                        Log.d("switch4", list.get(finalI3).getLock().getLockName());
//                                        TTLockClient.getDefault().controlLock(ControlAction.UNLOCK, list.get(finalI3).getLock().getLockData(), list.get(finalI3).getLock().getLockMac(), new ControlLockCallback() {
//                                            @Override
//                                            public void onControlLockSuccess(ControlLockResult controlLockResult) {
//                                                //Toast.makeText(act,"lock is unlock  success!",Toast.LENGTH_LONG).show();
//                                                //d.dismiss();
//                                                //ToastMaker.MakeToast("Door Opened",act);
//                                                Log.d("switch4", "open");
//                                                list.get(finalI3).getSWITCH4().publishDps("{\" 1\":false}", new IResultCallback() {
//                                                    @Override
//                                                    public void onError(String code, String error) {
//
//                                                    }
//
//                                                    @Override
//                                                    public void onSuccess() {
//
//                                                    }
//                                                });
//                                            }
//
//                                            @Override
//                                            public void onFail(LockError error) {
//                                                // Toast.makeText(UnlockActivity.this,"unLock fail!--" + error.getDescription(),Toast.LENGTH_LONG).show();
//                                                //d.dismiss();
//                                                //ToastMaker.MakeToast("Open Fail!  "+error,act);
//                                            }
//                                        });
//                                    } else {
//                                        Log.d("switch4", "Lock in null");
//                                    }
//
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onRemoved(String devId)
//                        {
//                            setSwitch4Status(list.get(finalI3),"0");
//                        }
//                        @Override
//                        public void onStatusChanged(String devId, boolean online) {
//
//                        }
//
//                        @Override
//                        public void onNetworkStatusChanged(String devId, boolean status) {
//
//                        }
//
//                        @Override
//                        public void onDevInfoUpdate(String devId) {
//
//                        }
//                    });
                }
        }
    }

    public void setFireRoomsListiner()
    {
        //Toast.makeText(act,"rooms "+list.size()+"   fires "+FireRooms.size() ,Toast.LENGTH_LONG).show();
        //Log.d("theProblem" , "rooms "+list.size()+"   fires "+FireRooms.size());

        for (int i=0;i<FireRooms.size();i++)
        {
            int finalI = i;
                if (list.get(i).getSERVICE_B() != null) {
                    FireRooms.get(i).child("Cleanup").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            if (snapshot.getValue() != null) {
                                Log.d("turnSwitchOff",snapshot.getValue().toString());
                                if (snapshot.getValue().toString().equals("0")) {
                                    CLEANUP[finalI] = false;
//                                    list.get(finalI).getSERVICE().publishDps("{\" 2\": false}", new IResultCallback() {
//                                        @Override
//                                        public void onError(String code, String error) {
//
//                                        }
//
//                                        @Override
//                                        public void onSuccess() {
//
//                                        }
//                                    });
                                    TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\" 2\" : false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                        @Override
                                        public void onError(String code, String error) {
                                            TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\" 2\": false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                @Override
                                                public void onError(String code, String error) {
                                                    TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\" 2\": false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                        @Override
                                                        public void onError(String code, String error) {
                                                            TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\" 2\": false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                                @Override
                                                                public void onError(String code, String error) {
                                                                    TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\" 2\": false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                                        @Override
                                                                        public void onError(String code, String error) {
                                                                        }

                                                                        @Override
                                                                        public void onSuccess() {

                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onSuccess() {

                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onSuccess() {

                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onSuccess() {

                                                }
                                            });
                                        }

                                        @Override
                                        public void onSuccess() {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    FireRooms.get(i).child("Laundry").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            if (snapshot.getValue() != null) {
                                Log.d("turnSwitchOff",snapshot.getValue().toString());
                                if (snapshot.getValue().toString().equals("0")) {
                                    LAUNDRY[finalI] = false;
//                                    list.get(finalI).getSERVICE().publishDps("{\"3\" : false}", new IResultCallback() {
//                                        @Override
//                                        public void onError(String code, String error) {
//
//                                        }
//
//                                        @Override
//                                        public void onSuccess() {
//
//                                        }
//                                    });
                                    TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\"3\" : false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                        @Override
                                        public void onError(String code, String error) {
                                            TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\"3\" : false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                @Override
                                                public void onError(String code, String error) {
                                                    TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\"3\" : false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                        @Override
                                                        public void onError(String code, String error) {
                                                            TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\"3\" : false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                                @Override
                                                                public void onError(String code, String error) {
                                                                    TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\"3\" : false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                                        @Override
                                                                        public void onError(String code, String error) {
                                                                            TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\"3\" : false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                                                @Override
                                                                                public void onError(String code, String error) {

                                                                                }

                                                                                @Override
                                                                                public void onSuccess() {

                                                                                }
                                                                            });
                                                                        }

                                                                        @Override
                                                                        public void onSuccess() {

                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onSuccess() {

                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onSuccess() {

                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onSuccess() {

                                                }
                                            });
                                        }

                                        @Override
                                        public void onSuccess() {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    FireRooms.get(i).child("Checkout").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            if (snapshot.getValue() != null) {
                                if (snapshot.getValue().toString().equals("0")) {
                                    CHECKOUT[finalI] = false;
//                                    list.get(finalI).getSERVICE().publishDps("{\" 4\":false}", new IResultCallback() {
//                                        @Override
//                                        public void onError(String code, String error) {
//                                            Log.d("checkout", "not" + error);
//                                        }
//
//                                        @Override
//                                        public void onSuccess() {
//                                            Log.d("checkout", "done");
//                                        }
//                                    });
                                    TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\" 4\":false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                        @Override
                                        public void onError(String code, String error) {
                                            TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\" 4\":false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                @Override
                                                public void onError(String code, String error) {
                                                    TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\" 4\":false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                        @Override
                                                        public void onError(String code, String error) {
                                                            TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\" 4\":false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                                @Override
                                                                public void onError(String code, String error) {
                                                                    TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\" 4\":false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                                        @Override
                                                                        public void onError(String code, String error) {
                                                                            TuyaHomeSdk.newDeviceInstance(list.get(finalI).getSERVICE_B().devId).publishDps("{\" 4\":false}", TYDevicePublishModeEnum.TYDevicePublishModeAuto, new IResultCallback() {
                                                                                @Override
                                                                                public void onError(String code, String error) {

                                                                                }

                                                                                @Override
                                                                                public void onSuccess() {
                                                                                    Log.d("checkout", "done");
                                                                                }
                                                                            });
                                                                        }

                                                                        @Override
                                                                        public void onSuccess() {
                                                                            Log.d("checkout", "done");
                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onSuccess() {
                                                                    Log.d("checkout", "done");
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onSuccess() {
                                                            Log.d("checkout", "done");
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onSuccess() {
                                                    Log.d("checkout", "done");
                                                }
                                            });
                                        }

                                        @Override
                                        public void onSuccess() {
                                            Log.d("checkout", "done");
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                FireRooms.get(i).child("TempSetPoint").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.getValue() != null)
                        {
                            try
                            {
                                if (Integer.parseInt(snapshot.getValue().toString()) > 15 )
                                {
                                    String temp = snapshot.getValue().toString() ;
                                    if (temp.length()==2)
                                    {
                                        temp = temp+"0";
                                        TempSetPoint[finalI] = temp ;
                                    }
                                    else if (temp.length()>2)
                                    {
                                        TempSetPoint[finalI] = temp ;
                                    }
                                    //TempSetPoint[finalI] = snapshot.getValue().toString();
                                }

                            }
                            catch (Exception e)
                            {

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });
                FireRooms.get(i).child("SetPointInterval").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.getValue() != null)
                        {
                            try
                            {
                                THE_AC_INTERVAL_TIME = 1000*60* Integer.parseInt(snapshot.getValue().toString());
                                Log.d("intervalsetpoint" , THE_AC_INTERVAL_TIME+"" );
                            }
                            catch (Exception e)
                            {

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                FireRooms.get(i).child("DoorWarning").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.getValue() != null )
                        {
                            try {
                                THEDOORWARNING_INTERVAL = 1000 * 60 * Integer.parseInt(snapshot.getValue().toString());
                                Log.d("Doorinterval", THEDOORWARNING_INTERVAL + "");
                            } catch (Exception e) {

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                FireRooms.get(i).child("roomStatus").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null ) {
                            Log.d("roomChangedTo" ,snapshot.getValue().toString() );
                            if (snapshot.getValue().toString().equals("3")) {
                                if(list.get(finalI).getPOWER() != null ) {
                                    checkoutModeRoom(list.get(finalI));
//                                list.get(finalI).getPOWER().publishDps("{\"1\": true}", new IResultCallback() {
//                                    @Override
//                                    public void onError(String code, String error) {
//                                        //Toast.makeText(act, error, Toast.LENGTH_SHORT).show();
//                                        Log.e("light", error);
//                                    }
//
//                                    @Override
//                                    public void onSuccess() {
//                                        //Toast.makeText(act, "turn on the light success", Toast.LENGTH_SHORT).show();
//                                        //myRefPower.setValue(1);
//                                    }
//                                });
//                                list.get(finalI).getPOWER().publishDps("{\"2\": true}", new IResultCallback() {
//                                    @Override
//                                    public void onError(String code, String error) {
//                                        //Toast.makeText(act, "turn on the light failure", Toast.LENGTH_SHORT).show();
//                                    }
//
//                                    @Override
//                                    public void onSuccess() {
//                                        //Toast.makeText(act, "turn on the light success", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                                list.get(finalI).getPOWER().publishDps("{\"10\": 20}", new IResultCallback() {
//                                    @Override
//                                    public void onError(String code, String error) {
//                                        //Toast.makeText(act, "turn on the light failure", Toast.LENGTH_SHORT).show();
//                                    }
//
//                                    @Override
//                                    public void onSuccess() {
//                                        //Toast.makeText(act, "turn on the light success", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
                                }
                            }
                            list.get(finalI).roomStatus = Integer.parseInt(snapshot.getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                FireRooms.get(i).child("CheckInModeTime").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.getValue() != null )
                        {
                            if (!snapshot.getValue().toString().equals("0"))
                            {
                                checkInModeTime = Integer.parseInt( snapshot.getValue().toString());
                                //Log.d("checkinModeDuration" , "check in chenged to "+checkInModeTime);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                FireRooms.get(i).child("CheckOutModeTime").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null ){
                            if (!snapshot.getValue().toString().equals("0"))
                            {
                                checkOutModeTime = Integer.parseInt( snapshot.getValue().toString());
                                //Log.d("checkoutModeDuration" , "changed to "+checkOutModeTime+"");
                                //Toast.makeText(act,"checkout changed to "+checkOutModeTime+"",Toast.LENGTH_LONG).show();
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
    }

    public void setRoomStatusListiner(){
        for (int i=0;i<FireRooms.size();i++){
            int finalI = i;
            FireRooms.get(i).child("roomStatus").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null ){
                        if (snapshot.getValue().toString().equals("2")){
                            list.get(finalI).getPOWER().publishDps("{\"1\": true}", new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    //Toast.makeText(act, error, Toast.LENGTH_SHORT).show();
                                    Log.e("light", error);
                                }

                                @Override
                                public void onSuccess() {
                                    //Toast.makeText(act, "turn on the light success", Toast.LENGTH_SHORT).show();
                                    //myRefPower.setValue(1);
                                }
                            });
                            list.get(finalI).getPOWER().publishDps("{\"2\": true}", new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    //Toast.makeText(act, "turn on the light failure", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onSuccess() {
                                    //Toast.makeText(act, "turn on the light success", Toast.LENGTH_SHORT).show();
                                }
                            });
                            list.get(finalI).getPOWER().publishDps("{\"10\": 20}", new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    //Toast.makeText(act, "turn on the light failure", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onSuccess() {
                                    //Toast.makeText(act, "turn on the light success", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    //Add & Cancel Orders

    public void addCleanupOrder(ROOM room , int index)
    {
        try
        {
            final String dep = "Cleanup";
            Calendar x = Calendar.getInstance(Locale.getDefault());
            long timee =  x.getTimeInMillis();

            StringRequest addOrder = new StringRequest(Request.Method.POST, insertServiceOrderUrl , new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    if (Integer.parseInt(response) > 0 )
                    {
                        room.Cleanup = Integer.parseInt(response);
                    }
                    else
                    {
                        Toast.makeText(act , response,Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Toast.makeText(act , error.getMessage(),Toast.LENGTH_LONG).show();
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("roomNumber" ,String.valueOf(room.RoomNumber));
                    params.put("time" ,String.valueOf(timee));
                    params.put( "dep" , dep );
                    params.put("Hotel" ,String.valueOf( room.Hotel));
                    params.put("RorS" ,String.valueOf( room.SuiteStatus));
                    params.put("Reservation" ,String.valueOf( room.ReservationNumber));
                    return params;
                }

            };
            CLEANUP_QUEUE.add(addOrder);
        }
        catch (Exception e)
        {

        }
    }

    public void cancelCleanupOrder(ROOM room , int index)
    {
        try
        {
            final String dep = "Cleanup";
            StringRequest removOrder = new StringRequest(Request.Method.POST, removeServiceOrderUrl , new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    if (response.equals("1")  )
                    {
                        room.Cleanup = 0 ;
                    }
                    else
                    {
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("id" ,String.valueOf( room.Cleanup));
                    params.put("room" , String.valueOf( room.RoomNumber));
                    params.put("dep" , dep);
                    params.put("Hotel" , String.valueOf( room.Hotel));
                    return params;
                }
            };
            CLEANUP_QUEUE.add(removOrder);
        }
        catch (Exception e )
        {

        }
    }

    public void addLaundryOrder(ROOM room , int index)
    {
        try
        {
            final String dep = "Laundry";
            Calendar x = Calendar.getInstance(Locale.getDefault());
            long timee =  x.getTimeInMillis();

            StringRequest addOrder = new StringRequest(Request.Method.POST, insertServiceOrderUrl , new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    if (Integer.parseInt(response) > 0 )
                    {
                        room.Laundry = Integer.parseInt(response);
                    }
                    else
                    {
                        Toast.makeText(act , response,Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("roomNumber" ,String.valueOf(room.RoomNumber));
                    params.put("time" ,String.valueOf(timee));
                    params.put("dep" ,dep);
                    params.put("Hotel" ,String.valueOf( room.Hotel));
                    params.put("RorS" ,String.valueOf( room.SuiteStatus));
                    params.put("Reservation" ,String.valueOf( room.ReservationNumber));
                    return params;
                }

            };
            LAUNDRY_QUEUE.add(addOrder);
        }
        catch (Exception e)
        {

        }
    }

    public void cancelLaundryOrder(ROOM room , int index)
    {
        try
        {
            final String dep = "Laundry";
            StringRequest removOrder = new StringRequest(Request.Method.POST, removeServiceOrderUrl , new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    if (response.equals("1")  )
                    {
                        room.Laundry = 0;
                    }
                    else
                    {
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("id" , String.valueOf( room.Laundry));
                    params.put("room" , String.valueOf(room.RoomNumber));
                    params.put("dep" , dep);
                    params.put("Hotel" , String.valueOf(room.Hotel));
                    return params;
                }
            };
            LAUNDRY_QUEUE.add(removOrder);
        }
        catch (Exception e)
        {

        }
    }

    public void addDNDOrder(ROOM room , int index)
    {
        Calendar x = Calendar.getInstance(Locale.getDefault());
        long timee =  x.getTimeInMillis();
        StringRequest request = new StringRequest(Request.Method.POST, insertServiceOrderUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.e("DND", response);
                    if (Integer.parseInt(response) > 0)
                    {
                        room.DND = Integer.parseInt(response);
                    }
                } catch (Exception e) {
                    Log.e("DND", e.getMessage());
                }

            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("roomNumber", String.valueOf(room.RoomNumber));
                params.put("time", String.valueOf(timee));
                params.put("dep", "DND");
                params.put("Hotel", String.valueOf(room.Hotel));
                params.put("RorS", String.valueOf(room.SuiteStatus));
                params.put("Reservation", String.valueOf(room.ReservationNumber));
                return params;
            }
        };
        Volley.newRequestQueue(act).add(request);
    }

    public void cancelDNDOrder(ROOM room , int index)
    {
        String dep = "DND";
        StringRequest rrr = new StringRequest(Request.Method.POST, removeServiceOrderUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("1"))
                {
                    room.DND = 0 ;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", String.valueOf(room.DND));
                params.put("room", String.valueOf(room.RoomNumber));
                params.put("dep", dep);
                params.put("Hotel", String.valueOf(room.Hotel));
                return params;
            }
        };
        Volley.newRequestQueue(act).add(rrr);
    }

    public void cancelCheckoutOrder(ROOM room , int index ) {

        final String dep = "Checkout";
        StringRequest re = new StringRequest(Request.Method.POST, removeServiceOrderUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //loading.stop();
                //checkoutOff();
                //myRefCheckout.setValue(0);
                //ToastMaker.MakeToast( dep + " Order Cancelled" , act);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //d.dismiss();
                //loading.stop();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", String.valueOf(1));
                params.put("room", String.valueOf(list.get(index).RoomNumber));
                params.put("dep", dep);
                params.put("Hotel", String.valueOf(1));
                return params;
            }
        };
        CHECKOUT_QUEUE.add(re);
    }

    public void addCheckoutOrder (ROOM room , int index ) {
        final String dep = "Checkout";
        Calendar x = Calendar.getInstance(Locale.getDefault());
        long timee =  x.getTimeInMillis();

        StringRequest addOrder = new StringRequest(Request.Method.POST, insertServiceOrderUrl , new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                //loading.stop();
                if (Integer.parseInt(response) > 0 )
                {
                    room.Laundry = Integer.parseInt(response);
                    //Toast.makeText(act,room.RoomNumber+" Laundry",Toast.LENGTH_LONG).show();
                    //LAUNDRY = true ;
                    //FireRooms.get(index).child(dep).setValue(Integer.parseInt(response));
                    //FireRooms.get(index).child("dep").setValue(dep);
                }
                else
                {
                    //Toast.makeText(act , response,Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                //loading.stop();
                //Toast.makeText(act , error.getMessage(),Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String,String> params = new HashMap<String, String>();
                params.put("roomNumber" ,String.valueOf(room.RoomNumber));
                params.put("time" ,String.valueOf(timee));
                params.put("dep" ,dep);
                params.put("Hotel" ,String.valueOf( room.Hotel));
                params.put("RorS" ,String.valueOf( room.SuiteStatus));
                params.put("Reservation" ,String.valueOf( room.ReservationNumber));
                return params;
            }

        };
        //Volley volley = new Volley();
        CHECKOUT_QUEUE.add(addOrder);
    }

    private void getHotelTempSetpoint()
    {
        String url = Login.SelectedHotel.URL+"getTempSetPointAndroid.php";
        StringRequest re = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                String temp ="";
                try
                {
                    JSONObject row = new JSONObject(response);
                    if ( !row.getString("Temp").equals("0"))
                    {
                        temp = row.getString("Temp") ;
                        if (temp.length()==2)
                        {
                            temp = temp+"0";
                            for (int i=0;i<TempSetPoint.length;i++)
                            {
                                TempSetPoint[i] = temp ;
                            }

                        }
                        else if (temp.length()>2)
                        {
                            for (int i=0;i<TempSetPoint.length;i++)
                            {
                                TempSetPoint[i] = temp ;
                            }
                        }
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                //Toast.makeText(act,temp+" "+TempSetPoint,Toast.LENGTH_LONG).show();
                Log.d("tempSetPoint",temp+" "+TempSetPoint);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String,String> pars = new HashMap<String, String>();
                pars.put("Hotel",String.valueOf(Login.THEHOTELDB.getHotelId()));
                return pars;
            }
        };
        Volley.newRequestQueue(act).add(re);
    }

    public  void OpenTheDoor(LockObj lock)
    {
        if(lock == null)
        {
            Toast.makeText(act," you should get your key list first " , Toast.LENGTH_LONG).show();
            return;
        }
        final Dialog d = new Dialog(act);
        d.setContentView(R.layout.loading_layout);
        TextView t = (TextView) d.findViewById(R.id.textViewdfsdf);
        t.setText("Door Opening");
        d.setCancelable(false);
        d.show();
        //ensureBluetoothIsEnabled();
        //showConnectLockToast();
        TTLockClient.getDefault().controlLock(ControlAction.UNLOCK, lock.getLockData(), lock.getLockMac(),new ControlLockCallback()
        {
            @Override
            public void onControlLockSuccess(ControlLockResult controlLockResult)
            {
                //Toast.makeText(act,"lock is unlock  success!",Toast.LENGTH_LONG).show();
                d.dismiss();
                //ToastMaker.MakeToast("Door Opened",act);
            }
            @Override
            public void onFail(LockError error) {
                // Toast.makeText(UnlockActivity.this,"unLock fail!--" + error.getDescription(),Toast.LENGTH_LONG).show();
                d.dismiss();
                //ToastMaker.MakeToast("Open Fail!  "+error,act);
            }
        });

    }

    void setDoorSensorStatus(ROOM Room, String status)
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
                        Room.setDOORSENSOR_B(null);
                        Room.setDOORSENSOR(null);
                        Room.DoorSensor = 0 ;
                        adapter.notifyDataSetChanged();
                        AlertDialog.Builder d = new AlertDialog.Builder(act);
                        d.setTitle(Room.RoomNumber+"DoorSensor Deleted ");
                        d.setMessage("The Door Sensor Of Room "+Room.RoomNumber+" Has Deleted");
                        d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        d.create().show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    loading.stop();
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
            Volley.newRequestQueue(act).add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setServiceSwitchStatus(ROOM Room, String status)
    {
        try
        {
            //lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setServiceSwitchStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    //loading.stop();
                    if (response.equals("1"))
                    {
                        Room.setSERVICE_B(null);
                        Room.setSERVICE(null);
                        Room.ServiceSwitch = 0 ;
                        adapter.notifyDataSetChanged();
                        AlertDialog.Builder d = new AlertDialog.Builder(act);
                        d.setTitle(Room.RoomNumber+"Service Switch Deleted ");
                        d.setMessage("The Service Switch Of Room "+Room.RoomNumber+" Has Deleted");
                        d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        d.create().show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //loading.stop();
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

    void setThermostatStatus(ROOM Room, String status)
    {
        try
        {
            //lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setThermostatStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    //loading.stop();
                    if (response.equals("1"))
                    {
                        //myRefThermostat.setValue(status);
                        Room.setAC_B(null);
                        Room.setAC(null);
                        Room.Thermostat = 0 ;
                        adapter.notifyDataSetChanged();
                        AlertDialog.Builder d = new AlertDialog.Builder(act);
                        d.setTitle(Room.RoomNumber+"AC Controller Deleted ");
                        d.setMessage("The AC Controller Of Room "+Room.RoomNumber+" Has Deleted");
                        d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        d.create().show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //loading.stop();
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

    void setPowerSwitchStatus(ROOM Room, String status)
    {
        try
        {
            //lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setPowerSwitchStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    //loading.stop();
                    Log.e("power " , response +" " + status);
                    if (response.equals("1"))
                    {
                        //myRefSwitch4.setValue(status);
                        Room.setPOWER_B(null);
                        Room.setPOWER(null);
                        Room.PowerSwitch = 0 ;
                        adapter.notifyDataSetChanged();
                        AlertDialog.Builder d = new AlertDialog.Builder(act);
                        d.setTitle(Room.RoomNumber+" Power Switch Deleted ");
                        d.setMessage("The Power Switch Of Room "+Room.RoomNumber+" Has Deleted");
                        d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        d.create().show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //loading.stop();
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

    void setCurtainSwitchStatus(ROOM Room, String status)
    {
        try
        {
            //lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setCurtainSwitchStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    //loading.stop();
                    if (response.equals("1"))
                    {
                        //myRefCurtainSwitch.setValue(status);
                        Room.setCURTAIN_B(null);
                        Room.setCURTAIN(null);
                        Room.CurtainSwitch = 0 ;
                        adapter.notifyDataSetChanged();
                        AlertDialog.Builder d = new AlertDialog.Builder(act);
                        d.setTitle(Room.RoomNumber+" Curtain Switch Deleted ");
                        d.setMessage("The Curtain Switch Of Room "+Room.RoomNumber+" Has Deleted");
                        d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        d.create().show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //loading.stop();
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

    void setMotionSensorStatus(ROOM Room, String status)
    {
        try
        {
            //lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setMotionSensorStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    //loading.stop();
                    if (response.equals("1"))
                    {
                        //myRefMotionSensor.setValue(status);
                        Room.setMOTIONSENSOR_B(null);
                        Room.setMOTIONSENSOR(null);
                        Room.MotionSensor = 0 ;
                        adapter.notifyDataSetChanged();
                        AlertDialog.Builder d = new AlertDialog.Builder(act);
                        d.setTitle(Room.RoomNumber+" Motion Sensor Deleted ");
                        d.setMessage("The Motion Sensor Of Room "+Room.RoomNumber+" Has Deleted");
                        d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        d.create().show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //loading.stop();
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
            REQ1.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setSwitch1Status(ROOM Room, String status)
    {
        try
        {
            //lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setSwitch1StatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    //loading.stop();
                    if (response.equals("1"))
                    {
                        //myRefSwitch1.setValue(status);
                        Room.setSWITCH1_B(null);
                        Room.setSWITCH1(null);
                        Room.Switch1 = 0 ;
                        adapter.notifyDataSetChanged();
                        AlertDialog.Builder d = new AlertDialog.Builder(act);
                        d.setTitle(Room.RoomNumber+" Switch 1 Deleted ");
                        d.setMessage("The Switch 1 Of Room "+Room.RoomNumber+" Has Deleted");
                        d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        d.create().show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //loading.stop();
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
            REQ1.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setSwitch2Status(ROOM Room, String status)
    {
        try
        {
            //lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setSwitch2StatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    //loading.stop();
                    if (response.equals("1"))
                    {
                        //myRefSwitch2.setValue(status);
                        Room.setSWITCH2_B(null);
                        Room.setSWITCH2(null);
                        Room.Switch2=0;
                        adapter.notifyDataSetChanged();
                        AlertDialog.Builder d = new AlertDialog.Builder(act);
                        d.setTitle(Room.RoomNumber+" Switch 2 Deleted ");
                        d.setMessage("The Switch 2 Of Room "+Room.RoomNumber+" Has Deleted");
                        d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        d.create().show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //loading.stop();
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
            REQ1.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setSwitch3Status(ROOM Room, String status)
    {
        try
        {
            //lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setSwitch3StatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    //loading.stop();
                    if (response.equals("1"))
                    {
                        //myRefSwitch3.setValue(status);
                        Room.setSWITCH3_B(null);
                        Room.setSWITCH3(null);
                        Room.Switch3 = 0 ;
                        adapter.notifyDataSetChanged();
                        AlertDialog.Builder d = new AlertDialog.Builder(act);
                        d.setTitle(Room.RoomNumber+" Switch 3 Deleted ");
                        d.setMessage("The Switch 3 Of Room "+Room.RoomNumber+" Has Deleted");
                        d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        d.create().show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //loading.stop();
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
            REQ1.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setSwitch4Status(ROOM Room, String status)
    {
        try
        {
            //lodingDialog loading =  new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setSwitch4StatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    //loading.stop();
                    if (response.equals("1"))
                    {
                        //myRefSwitch4.setValue(status);
                        Room.setSWITCH4_B(null);
                        Room.setSWITCH4(null);
                        Room.Switch4 = 0 ;
                        adapter.notifyDataSetChanged();
                        AlertDialog.Builder d = new AlertDialog.Builder(act);
                        d.setTitle(Room.RoomNumber+" Switch 4 Deleted ");
                        d.setMessage("The Switch 4 Of Room "+Room.RoomNumber+" Has Deleted");
                        d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        d.create().show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //loading.stop();
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
            REQ1.add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setZBGatewayStatus(ROOM Room , String status)
    {
        try
        {
            //lodingDialog loading = new lodingDialog(act);
            String url = Login.SelectedHotel.URL+"setGatewayStatusValue.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    //loading.stop();
                    if (response.equals("1"))
                    {
                        for (int i=0; i<list.size();i++){
                             if (list.get(i).RoomNumber == Room.RoomNumber) {
                                 list.get(i).ZBGateway = 1 ;
                                 FireRooms.get(i).child("ZBGateway").setValue("1");
                             }
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //loading.stop();
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
            requestQueue = Volley.newRequestQueue(act);
            requestQueue.add(tabR);
        }
        catch (Exception e)
        {

        }
    }


    void setPowerOnOff(ROOM room,String status)
    {
        try
        {
            String url = Login.SelectedHotel.URL+"setCurrentPowerOnOff.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    Log.e("powerstatusChange" , response +" " + status);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //ToastMaker.MakeToast(error.getMessage() , act);
                    //Log.e("Tablet" , error.getMessage() );
                    Log.e("powerstatusChange" , error.toString() +" " + status);
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> Params = new HashMap<String,String>();
                    Params.put("room", String.valueOf(room.RoomNumber));
                    Params.put("id",String.valueOf(room.id) );
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

    void setDoorOpenClosed(ROOM room,String status)
    {
        try
        {
            String url = Login.SelectedHotel.URL+"setDoorOpenClosed.php";
            StringRequest tabR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    Log.e("power " , response +" " + status);
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
                    Params.put("room", String.valueOf(room.RoomNumber));
                    Params.put("id",String.valueOf(room.id));
                    Params.put("value" , status);
                    return Params;
                }
            };
            Volley.newRequestQueue(act).add(tabR);
        }
        catch (Exception e)
        {

        }
    }

    void setSwitch1DB1(ROOM THEROOM){
        if (THEROOM.getSWITCH1_B() != null && THEROOM.getSWITCH2_B() != null) {

            iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH1_B().devId, "1", new ITuyaDataCallback<MultiControlLinkBean>() {
                @Override
                public void onSuccess(MultiControlLinkBean result) {

                    if (result.getMultiGroup() == null )
                    {
                        Log.d("S1D1RES " , "null");
                        iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH2_B().devId, "1", new ITuyaDataCallback<MultiControlLinkBean>() {
                            @Override
                            public void onSuccess(MultiControlLinkBean result) {

                                if (result.getMultiGroup() == null ){
                                    Log.d("S2D1RES" , "null");
                                    Random r = new Random();
                                    int x = r.nextInt(30);
                                    JSONObject groupdetailes1 = new JSONObject(), groupdetailes2 = new JSONObject();
                                    try {
                                        groupdetailes1.put("devId", THEROOM.getSWITCH1_B().devId);
                                        groupdetailes1.put("dpId", 1);
                                        groupdetailes1.put("id", x);
                                        groupdetailes1.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    try {
                                        groupdetailes2.put("devId", THEROOM.getSWITCH2_B().devId);
                                        groupdetailes2.put("dpId", 1);
                                        groupdetailes2.put("id", x);
                                        groupdetailes2.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    JSONArray arr = new JSONArray();
                                    arr.put(groupdetailes2);
                                    arr.put(groupdetailes1);
                                    JSONObject multiControlBean = new JSONObject();
                                    try {
                                        multiControlBean.put("groupName", THEROOM.RoomNumber + "Lighting" + x);
                                        multiControlBean.put("groupType", 1);
                                        multiControlBean.put("groupDetail", arr);
                                        multiControlBean.put("id", x);
                                    } catch (JSONException e) {

                                    }
                                    iTuyaDeviceMultiControl.saveDeviceMultiControl(Login.THEHOME.getHomeId(), multiControlBean.toString(), new ITuyaResultCallback<MultiControlBean>() {
                                        @Override
                                        public void onSuccess(MultiControlBean result) {
                                            //ToastUtil.shortToast(mContext,"success");
                                            Log.d("switch1Dp1", result.getGroupName());
                                            iTuyaDeviceMultiControl.enableMultiControl(x, new ITuyaResultCallback<Boolean>() {
                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    //ToastUtil.shortToast(mContext,"success");
                                                    Log.d("switch1Dp1", result.toString());
                                                }

                                                @Override
                                                public void onError(String errorCode, String errorMessage) {
                                                    //ToastUtil.shortToast(mContext,errorMessage);
                                                    Log.d("switch1Dp1", errorMessage);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String errorCode, String errorMessage) {
                                            //ToastUtil.shortToast(mContext,errorMessage);
                                            Log.d("switch1Dp1", errorMessage + "here "+x);
                                        }
                                    });
                                }
                                else
                                {
                                    Log.d("S2D1RES " , result.getMultiGroup().getGroupName());
                                }
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {

                            }
                        });
                    }
                    else
                    {
                        Log.d("S1D1RES " , result.getMultiGroup().getGroupName());
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {

                }
            });


        }
    }
    void setSwitch1DB2(ROOM THEROOM){
        if (THEROOM.getSWITCH1_B() != null && THEROOM.getSWITCH2_B() != null) {

            iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH1_B().devId, "2", new ITuyaDataCallback<MultiControlLinkBean>() {
                @Override
                public void onSuccess(MultiControlLinkBean result) {

                    if (result.getMultiGroup() == null ){
                        Log.d("MultiControlProblem"+THEROOM.RoomNumber , "null");
                        iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH2_B().devId, "2", new ITuyaDataCallback<MultiControlLinkBean>() {
                            @Override
                            public void onSuccess(MultiControlLinkBean result) {

                                if (result.getMultiGroup() == null ){
                                    Log.d("MultiControlProblem"+THEROOM.RoomNumber , "null");
                                    Random r = new Random();
                                    int y = r.nextInt(30);
                                    JSONObject groupdetailes1 = new JSONObject(), groupdetailes2 = new JSONObject();
                                    try {
                                        groupdetailes1.put("devId", THEROOM.getSWITCH1_B().devId);
                                        groupdetailes1.put("dpId", 2);
                                        groupdetailes1.put("id", y);
                                        groupdetailes1.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    try {
                                        groupdetailes2.put("devId", THEROOM.getSWITCH2_B().devId);
                                        groupdetailes2.put("dpId", 2);
                                        groupdetailes2.put("id", y);
                                        groupdetailes2.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    JSONArray arr = new JSONArray();
                                    arr.put(groupdetailes2);
                                    arr.put(groupdetailes1);
                                    JSONObject multiControlBean = new JSONObject();
                                    try {
                                        multiControlBean.put("groupName", THEROOM.RoomNumber + "Lighting" + y);
                                        multiControlBean.put("groupType", 2);
                                        multiControlBean.put("groupDetail", arr);
                                        multiControlBean.put("id", y);
                                    } catch (JSONException e) {

                                    }
                                    iTuyaDeviceMultiControl.saveDeviceMultiControl(Login.THEHOME.getHomeId(), multiControlBean.toString(), new ITuyaResultCallback<MultiControlBean>() {
                                        @Override
                                        public void onSuccess(MultiControlBean result) {
                                            //ToastUtil.shortToast(mContext,"success");
                                            Log.d("MultiControlProblem"+THEROOM.RoomNumber, result.getGroupName());

                                            iTuyaDeviceMultiControl.enableMultiControl(y, new ITuyaResultCallback<Boolean>() {
                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    //ToastUtil.shortToast(mContext,"success");
                                                    Log.d("MultiControlProblem"+THEROOM.RoomNumber, result.toString());
                                                }

                                                @Override
                                                public void onError(String errorCode, String errorMessage) {
                                                    //ToastUtil.shortToast(mContext,errorMessage);
                                                    Log.d("MultiControlProblem"+THEROOM.RoomNumber, errorMessage);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String errorCode, String errorMessage) {
                                            //ToastUtil.shortToast(mContext,errorMessage);
                                            Log.d("MultiControlProblem"+THEROOM.RoomNumber, errorMessage+" "+y);
                                        }
                                    });
                                }
                                else
                                {
                                    Log.d("MultiControlProblem"+THEROOM.RoomNumber , result.getMultiGroup().getGroupName());
                                }
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {

                            }
                        });
                    }
                    else
                    {
                        Log.d("MultiControlProblem"+THEROOM.RoomNumber , result.getMultiGroup().getGroupName());
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {

                }
            });

        }
    }
    void setSwitch1DB3(ROOM THEROOM){
        if (THEROOM.getSWITCH1_B() != null && THEROOM.getSWITCH2_B() != null) {

            iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH1_B().devId, "3", new ITuyaDataCallback<MultiControlLinkBean>() {
                @Override
                public void onSuccess(MultiControlLinkBean result) {

                    if (result.getMultiGroup() == null ){
                        Log.d("S1D3RES " , "null");
                        iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH2_B().devId, "3", new ITuyaDataCallback<MultiControlLinkBean>() {
                            @Override
                            public void onSuccess(MultiControlLinkBean result) {

                                if (result.getMultiGroup() == null ){
                                    Log.d("S2D3RES " , "null");
                                    Random r = new Random();
                                    int x = r.nextInt(30);
                                    JSONObject groupdetailes1 = new JSONObject(), groupdetailes2 = new JSONObject();
                                    try {
                                        groupdetailes1.put("devId", THEROOM.getSWITCH1_B().devId);
                                        groupdetailes1.put("dpId", 3);
                                        groupdetailes1.put("id", x);
                                        groupdetailes1.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    try {
                                        groupdetailes2.put("devId", THEROOM.getSWITCH2_B().devId);
                                        groupdetailes2.put("dpId", 3);
                                        groupdetailes2.put("id", x);
                                        groupdetailes2.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    JSONArray arr = new JSONArray();
                                    arr.put(groupdetailes2);
                                    arr.put(groupdetailes1);
                                    JSONObject multiControlBean = new JSONObject();
                                    try {
                                        multiControlBean.put("groupName", THEROOM.RoomNumber + "Lighting" + x);
                                        multiControlBean.put("groupType", 3);
                                        multiControlBean.put("groupDetail", arr);
                                        multiControlBean.put("id", x);
                                    } catch (JSONException e) {

                                    }
                                    iTuyaDeviceMultiControl.saveDeviceMultiControl(Login.THEHOME.getHomeId(), multiControlBean.toString(), new ITuyaResultCallback<MultiControlBean>() {
                                        @Override
                                        public void onSuccess(MultiControlBean result) {
                                            //ToastUtil.shortToast(mContext,"success");
                                            Log.d("switch1DeviceDp3", result.getGroupName());
                                            iTuyaDeviceMultiControl.enableMultiControl(x, new ITuyaResultCallback<Boolean>() {
                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    //ToastUtil.shortToast(mContext,"success");
                                                    Log.d("switch1DeviceDp3", result.toString());
                                                }

                                                @Override
                                                public void onError(String errorCode, String errorMessage) {
                                                    //ToastUtil.shortToast(mContext,errorMessage);
                                                    Log.d("switch1DeviceDp3", errorMessage);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String errorCode, String errorMessage) {
                                            //ToastUtil.shortToast(mContext,errorMessage);
                                            Log.d("switch1DeviceDp3", errorMessage+" "+x);
                                        }
                                    });
                                }
                                else
                                {
                                    Log.d("S2D3RES " , result.getMultiGroup().getGroupName());
                                }
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {

                            }
                        });
                    }
                    else
                    {
                        Log.d("S1D3RES " , result.getMultiGroup().getGroupName());
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {

                }
            });

        }
    }
    void setSwitch1DB4(ROOM THEROOM){
        if (THEROOM.getSWITCH1_B() != null && THEROOM.getSWITCH2_B() != null) {
            iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH1_B().devId, "4", new ITuyaDataCallback<MultiControlLinkBean>() {
                @Override
                public void onSuccess(MultiControlLinkBean result) {

                    if (result.getMultiGroup() == null ){
                        Log.d("S1D4RES " , "null");
                        iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH2_B().devId, "4", new ITuyaDataCallback<MultiControlLinkBean>() {
                            @Override
                            public void onSuccess(MultiControlLinkBean result) {

                                if (result.getMultiGroup() == null ){
                                    Log.d("S2D4RES " , "null");
                                    Random r = new Random();
                                    int x = r.nextInt(30);
                                    JSONObject groupdetailes1 = new JSONObject(), groupdetailes2 = new JSONObject();
                                    try {
                                        groupdetailes1.put("devId", THEROOM.getSWITCH1_B().devId);
                                        groupdetailes1.put("dpId", 4);
                                        groupdetailes1.put("id", x);
                                        groupdetailes1.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    try {
                                        groupdetailes2.put("devId", THEROOM.getSWITCH2_B().devId);
                                        groupdetailes2.put("dpId", 4);
                                        groupdetailes2.put("id", x);
                                        groupdetailes2.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    JSONArray arr = new JSONArray();
                                    arr.put(groupdetailes2);
                                    arr.put(groupdetailes1);
                                    JSONObject multiControlBean = new JSONObject();
                                    try {
                                        multiControlBean.put("groupName", THEROOM.RoomNumber + "Lighting" + x);
                                        multiControlBean.put("groupType", 4);
                                        multiControlBean.put("groupDetail", arr);
                                        multiControlBean.put("id", x);
                                    } catch (JSONException e) {

                                    }
                                    iTuyaDeviceMultiControl.saveDeviceMultiControl(Login.THEHOME.getHomeId(), multiControlBean.toString(), new ITuyaResultCallback<MultiControlBean>() {
                                        @Override
                                        public void onSuccess(MultiControlBean result) {
                                            //ToastUtil.shortToast(mContext,"success");
                                            Log.d("switch1DeviceDp4", result.getGroupName());
                                            iTuyaDeviceMultiControl.enableMultiControl(x, new ITuyaResultCallback<Boolean>() {
                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    //ToastUtil.shortToast(mContext,"success");
                                                    Log.d("switch1DeviceDp4", result.toString());
                                                }

                                                @Override
                                                public void onError(String errorCode, String errorMessage) {
                                                    //ToastUtil.shortToast(mContext,errorMessage);
                                                    Log.d("switch1DeviceDp4", errorMessage);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String errorCode, String errorMessage) {
                                            //ToastUtil.shortToast(mContext,errorMessage);
                                            Log.d("switch1DeviceDp4", errorMessage+" "+x);
                                        }
                                    });
                                }
                                else
                                {
                                    Log.d("S2D4RES " , result.getMultiGroup().getGroupName());
                                }
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {

                            }
                        });
                    }
                    else
                    {
                        Log.d("S1D4RES " , result.getMultiGroup().getGroupName());
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {

                }
            });

        }
    }

    void bindSwitch1ToSwitch3btn1(ROOM THEROOM){
        if (THEROOM.getSWITCH1_B() != null && THEROOM.getSWITCH3_B() != null) {

            iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH1_B().devId, "2", new ITuyaDataCallback<MultiControlLinkBean>() {
                @Override
                public void onSuccess(MultiControlLinkBean result) {

                    if (result.getMultiGroup() == null )
                    {
                        Log.d("bindS1S3" , "null");
                        iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH3_B().devId, "1", new ITuyaDataCallback<MultiControlLinkBean>() {
                            @Override
                            public void onSuccess(MultiControlLinkBean result) {

                                if (result.getMultiGroup() == null ){
                                    Log.d("bindS1S3" , "null");
                                    Random r = new Random();
                                    int x = r.nextInt(30);
                                    JSONObject groupdetailes1 = new JSONObject(), groupdetailes2 = new JSONObject();
                                    try {
                                        groupdetailes1.put("devId", THEROOM.getSWITCH1_B().devId);
                                        groupdetailes1.put("dpId", 2);
                                        groupdetailes1.put("id", x);
                                        groupdetailes1.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    try {
                                        groupdetailes2.put("devId", THEROOM.getSWITCH3_B().devId);
                                        groupdetailes2.put("dpId", 1);
                                        groupdetailes2.put("id", x);
                                        groupdetailes2.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    JSONArray arr = new JSONArray();
                                    arr.put(groupdetailes2);
                                    arr.put(groupdetailes1);
                                    JSONObject multiControlBean = new JSONObject();
                                    try {
                                        multiControlBean.put("groupName", THEROOM.RoomNumber + "Lighting" + x);
                                        multiControlBean.put("groupType", 13);
                                        multiControlBean.put("groupDetail", arr);
                                        multiControlBean.put("id", x);
                                    } catch (JSONException e) {

                                    }
                                    iTuyaDeviceMultiControl.saveDeviceMultiControl(Login.THEHOME.getHomeId(), multiControlBean.toString(), new ITuyaResultCallback<MultiControlBean>() {
                                        @Override
                                        public void onSuccess(MultiControlBean result) {
                                            //ToastUtil.shortToast(mContext,"success");
                                            Log.d("bindS1S3", result.getGroupName());
                                            iTuyaDeviceMultiControl.enableMultiControl(x, new ITuyaResultCallback<Boolean>() {
                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    //ToastUtil.shortToast(mContext,"success");
                                                    Log.d("bindS1S3", result.toString());
                                                }

                                                @Override
                                                public void onError(String errorCode, String errorMessage) {
                                                    //ToastUtil.shortToast(mContext,errorMessage);
                                                    Log.d("bindS1S3", errorMessage);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String errorCode, String errorMessage) {
                                            //ToastUtil.shortToast(mContext,errorMessage);
                                            Log.d("bindS1S3", errorMessage + "here "+x);
                                        }
                                    });
                                }
                                else
                                {
                                    Log.d("bindS1S3" , result.getMultiGroup().getGroupName());
                                }
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {

                            }
                        });
                    }
                    else
                    {
                        Log.d("bindS1S3" , result.getMultiGroup().getGroupName());
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {

                }
            });


        }
    }
    void bindSwitch1ToSwitch3btn2(ROOM THEROOM){
        if (THEROOM.getSWITCH1_B() != null && THEROOM.getSWITCH3_B() != null) {

            iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH1_B().devId, "3", new ITuyaDataCallback<MultiControlLinkBean>() {
                @Override
                public void onSuccess(MultiControlLinkBean result) {

                    if (result.getMultiGroup() == null )
                    {
                        Log.d("bindS1S3" , "null");
                        iTuyaDeviceMultiControl.queryLinkInfoByDp(THEROOM.getSWITCH3_B().devId, "2", new ITuyaDataCallback<MultiControlLinkBean>() {
                            @Override
                            public void onSuccess(MultiControlLinkBean result) {

                                if (result.getMultiGroup() == null ){
                                    Log.d("bindS1S3" , "null");
                                    Random r = new Random();
                                    int x = r.nextInt(30);
                                    JSONObject groupdetailes1 = new JSONObject(), groupdetailes2 = new JSONObject();
                                    try {
                                        groupdetailes1.put("devId", THEROOM.getSWITCH1_B().devId);
                                        groupdetailes1.put("dpId", 3);
                                        groupdetailes1.put("id", x);
                                        groupdetailes1.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    try {
                                        groupdetailes2.put("devId", THEROOM.getSWITCH3_B().devId);
                                        groupdetailes2.put("dpId", 2);
                                        groupdetailes2.put("id", x);
                                        groupdetailes2.put("enable", true);

                                    } catch (JSONException e) {
                                    }
                                    JSONArray arr = new JSONArray();
                                    arr.put(groupdetailes2);
                                    arr.put(groupdetailes1);
                                    JSONObject multiControlBean = new JSONObject();
                                    try {
                                        multiControlBean.put("groupName", THEROOM.RoomNumber + "Lighting" + x);
                                        multiControlBean.put("groupType", 14);
                                        multiControlBean.put("groupDetail", arr);
                                        multiControlBean.put("id", x);
                                    } catch (JSONException e) {

                                    }
                                    iTuyaDeviceMultiControl.saveDeviceMultiControl(Login.THEHOME.getHomeId(), multiControlBean.toString(), new ITuyaResultCallback<MultiControlBean>() {
                                        @Override
                                        public void onSuccess(MultiControlBean result) {
                                            //ToastUtil.shortToast(mContext,"success");
                                            Log.d("bindS1S3", result.getGroupName());
                                            iTuyaDeviceMultiControl.enableMultiControl(x, new ITuyaResultCallback<Boolean>() {
                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    //ToastUtil.shortToast(mContext,"success");
                                                    Log.d("bindS1S3", result.toString());
                                                }

                                                @Override
                                                public void onError(String errorCode, String errorMessage) {
                                                    //ToastUtil.shortToast(mContext,errorMessage);
                                                    Log.d("bindS1S3", errorMessage);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String errorCode, String errorMessage) {
                                            //ToastUtil.shortToast(mContext,errorMessage);
                                            Log.d("bindS1S3", errorMessage + "here "+x);
                                        }
                                    });
                                }
                                else
                                {
                                    Log.d("bindS1S3" , result.getMultiGroup().getGroupName());
                                }
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {

                            }
                        });
                    }
                    else
                    {
                        Log.d("bindS1S3" , result.getMultiGroup().getGroupName());
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {

                }
            });


        }
    }

    void sendRegistrationToServer(String token) {
        String url = Login.SelectedHotel.URL+ "modifyTokenForNonScreenRooms.php" ;
        StringRequest re  = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                    Log.d("tokenRegister" , response) ;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tokenRegister" , error.toString()) ;
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> par = new HashMap<String, String>();
                par.put("token" , token);
                return par;
            }
        };
        Volley.newRequestQueue(act).add(re);
    }

    static void powerOffRoom(ROOM THEROOM) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (THEROOM.getPOWER() != null ) {
                    THEROOM.getPOWER().publishDps("{\"1\": false}", new IResultCallback() {
                        @Override
                        public void onError(String code, String error) {
                            Toast.makeText(act, "Turn Off the Power failure Room "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onSuccess() {
                            THEROOM.getPOWER().publishDps("{\"2\": false}", new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    Toast.makeText(act, "Turn Off the Power failure Room "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onSuccess()
                                {
                                    Toast.makeText(act, "Turn Off Power Success room "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    static void powerOnRoom(ROOM THEROOM) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (THEROOM.getPOWER() != null ) {
                    THEROOM.getPOWER().publishDps("{\"1\": true}", new IResultCallback() {
                        @Override
                        public void onError(String code, String error) {
                            Toast.makeText(act, "Turn on the Power failure Room "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onSuccess() {
                            THEROOM.getPOWER().publishDps("{\"2\": true}", new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    Toast.makeText(act, "Turn on the Power failure Room "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(act, "Turn On Power success "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
//                    THEROOM.getPOWER().publishDps("{\"2\": false}", new IResultCallback() {
//                        @Override
//                        public void onError(String code, String error) {
//                            Toast.makeText(act, "turn on the light failure", Toast.LENGTH_SHORT).show();
//                        }
//                        @Override
//                        public void onSuccess()
//                        {
//                            Toast.makeText(act, "turn Off 2 success "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
//
//                        }
//                    });
                }

            }
        });
    }

    public static void powerByCard(ROOM THEROOM) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (THEROOM.getPOWER() != null ) {
                    THEROOM.getPOWER().publishDps("{\"1\": true}", new IResultCallback() {
                        @Override
                        public void onError(String code, String error) {
                            Toast.makeText(act, "Turn on the Power failure Room "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onSuccess() {
                            THEROOM.getPOWER().publishDps("{\"2\": false}", new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    Toast.makeText(act, "Turn on the Power failure Room "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(act, "Turn On Power success "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
//                    THEROOM.getPOWER().publishDps("{\"2\": false}", new IResultCallback() {
//                        @Override
//                        public void onError(String code, String error) {
//                            Toast.makeText(act, "turn on the light failure", Toast.LENGTH_SHORT).show();
//                        }
//                        @Override
//                        public void onSuccess()
//                        {
//                            Toast.makeText(act, "turn Off 2 success "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
//
//                        }
//                    });
                }

            }
        });
    }

    static void checkInModeRoom(ROOM THEROOM) {

        String Duration = "" ;
        if (checkInModeTime != 0)
        {
            Duration = String.valueOf(checkInModeTime*60) ;
        }
        else {
            Duration = "60" ;
        }
        Log.d("checkinModeDuration" , Duration +" "+checkInModeTime);
        if (THEROOM.getPOWER() != null )
        {
            String finalDuration = Duration;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Log.d("LightWithWelcome" , THEROOM.getPOWER_B().dps.toString());
                    if (THEROOM.getPOWER() != null )
                    {
                        THEROOM.getPOWER().publishDps("{\"1\": true}", new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                //Toast.makeText(act, error, Toast.LENGTH_SHORT).show();
                                Log.e("light", error);
                            }

                            @Override
                            public void onSuccess() {
                                //Toast.makeText(act, "turn on the light success", Toast.LENGTH_SHORT).show();
                                //myRefPower.setValue(1);
                            }
                        });
                        THEROOM.getPOWER().publishDps("{\"2\": true}", new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                //Toast.makeText(act, "turn on the light failure", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess() {
                                //Toast.makeText(act, "turn on the light success", Toast.LENGTH_SHORT).show();
                            }
                        });
                        THEROOM.getPOWER().publishDps("{\"8\": "+ finalDuration +"}", new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                //Toast.makeText(act, "turn on the light failure", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess() {
                                Log.d("LightWithWelcome" , "countdoun");
                                final long[] tt = {0};
                                long xx = Integer.parseInt( finalDuration ) ;
                                Handler H = new Handler();
                                Runnable r = new Runnable() {
                                    @Override
                                    public void run() {
                                        tt[0] = tt[0] +1000 ;
                                        H.postDelayed(this,1000);
                                        Log.d("LightWithWelcome" , tt[0]+" "+(xx*1000));
                                        if (tt[0] >= (xx*1000)){
                                            THEROOM.getPOWER().publishDps("{\"2\": false}", new IResultCallback() {
                                                @Override
                                                public void onError(String code, String error) {
                                                    Log.d("LightWithWelcome" , error);
                                                }

                                                @Override
                                                public void onSuccess() {
                                                    Log.d("LightWithWelcome" , "Light is off ");
                                                }
                                            });
                                            H.removeCallbacks(this);
                                        }
                                    }
                                };
                            }
                        });
                    }
                    final long[] tt = {0};
                    long xx = Integer.parseInt( finalDuration ) ;
                    if ( THEROOM.getSWITCH1() != null ){
                        Handler H = new Handler();
                        Runnable d = new Runnable() {
                            @Override
                            public void run() {
                                tt[0] = tt[0] +1000 ;
                                H.postDelayed(this,1000);
                                Log.d("LightWithWelcome" , tt[0]+" "+(xx*1000));
                                if (tt[0] >= (xx*1000)){
                                    THEROOM.getSWITCH1().publishDps("{\"1\": false}", new IResultCallback() {
                                        @Override
                                        public void onError(String code, String error) {
                                            Log.d("LightWithWelcome" , error);
                                        }

                                        @Override
                                        public void onSuccess() {
                                            Log.d("LightWithWelcome" , "Light is off ");
                                        }
                                    });
                                    H.removeCallbacks(this);
                                }
                            }
                        } ;
                        THEROOM.getSWITCH1().publishDps("{\"1\": true}", new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                Log.d("LightWithWelcome" , error);
                            }

                            @Override
                            public void onSuccess() {
                                Log.d("LightWithWelcome" , "Light is on "+finalDuration);
                                d.run();
                            }
                        });


                    }
                }
            });
        }
    }

    static void checkoutModeRoom(ROOM THEROOM) {

        String Duration = "" ;
        if (checkOutModeTime != 0 )
        {
            Duration = String.valueOf(checkOutModeTime * 60);
        }
        else
        {
            Duration = "60" ;
        }
        Log.d("checkoutModeDuration" , Duration+" "+checkOutModeTime );

        if (THEROOM.getPOWER_B() != null) {
            THEROOM.getPOWER().publishDps("{\"1\": true}", new IResultCallback() {
                @Override
                public void onError(String code, String error) {
                    Toast.makeText(act, "Checkout failed room "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onSuccess() {
                    THEROOM.getPOWER().publishDps("{\"2\": false}", new IResultCallback() {
                        @Override
                        public void onError(String code, String error) {
                            Toast.makeText(act, "Checkout failed room "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess() {
                            Toast.makeText(act, "Checkout room success "+THEROOM.RoomNumber, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        for (ServiceEmps u : Emps) {
            if (u.department.equals("Cleanup") || u.department.equals("Service")) {
                makemessage(u.token, "Cleanup", true, THEROOM.RoomNumber);
            }
        }
    }

    static void OpenTheDoor(ROOM THEROOM) {
        if(THEROOM.getLock() == null)
        {
            Toast.makeText(act,"this room has no lock " , Toast.LENGTH_LONG).show();
            return;
        }

        //showConnectLockToast();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run()
            {
//                final Dialog d = new Dialog(act);
//                d.setContentView(R.layout.loading_layout);
//                TextView t = (TextView) d.findViewById(R.id.textViewdfsdf);
//                t.setText("Door Opening");
//                d.setCancelable(false);
//                d.show();
                ensureBluetoothIsEnabled();
                TTLockClient.getDefault().controlLock(ControlAction.UNLOCK, THEROOM.getLock().getLockData(), THEROOM.getLock().getLockMac(),new ControlLockCallback()
                {
                    @Override
                    public void onControlLockSuccess(ControlLockResult controlLockResult) {
                        //Toast.makeText(act,"lock is unlock  success!",Toast.LENGTH_LONG).show();
                        //d.dismiss();
                        //ToastMaker.MakeToast("Door Opened",act);
                    }

                    @Override
                    public void onFail(LockError error) {
                        // Toast.makeText(UnlockActivity.this,"unLock fail!--" + error.getDescription(),Toast.LENGTH_LONG).show();
                        //d.dismiss();
                        //ToastMaker.MakeToast("Open Fail!  "+error,act);
                    }
                });
            }
        });


    }

    public static void ensureBluetoothIsEnabled() {
        if(!TTLockClient.getDefault().isBLEEnabled(act)){
            TTLockClient.getDefault().requestBleEnable(act);
        }
    }

    public void toggleRoomsDevices(View view) {
        hideSystemUI();
        if (roomsListView.getVisibility() == View.VISIBLE) {
            roomsListView.setVisibility(View.GONE);
            devicesListView.setVisibility(View.VISIBLE);
            searchText.setVisibility(View.VISIBLE);
            searchBtn.setVisibility(View.VISIBLE);
            Toast.makeText(act,"devices are "+Devices.size(),Toast.LENGTH_LONG).show();
        }
        else if (roomsListView.getVisibility() == View.GONE) {
            roomsListView.setVisibility(View.VISIBLE);
            devicesListView.setVisibility(View.GONE);
            searchBtn.setVisibility(View.GONE);
            searchText.setVisibility(View.GONE);
        }

    }

    public void lockAndUnlock(View view) {
        hideSystemUI();
        if(lockDB.getLockValue().equals("off")) {

            Dialog  dd = new Dialog(act);
            dd.setContentView(R.layout.lock_unlock_dialog);
            Button cancel = (Button) dd.findViewById(R.id.confermationDialog_cancel);
            Button lock = (Button) dd.findViewById(R.id.messageDialog_ok);
            EditText password = (EditText) dd.findViewById(R.id.editTextTextPassword);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dd.dismiss();
                }
            });
            lock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final lodingDialog loading = new lodingDialog(act);
                    final String pass = password.getText().toString() ;
                    StringRequest re = new StringRequest(Request.Method.POST, LogInUrl, new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {
                            Log.d("LoginResult" , response +" "+ LogInUrl );
                            loading.stop();
                            if (response.equals("1"))
                            {
                                lockDB.modifyValue("on");
                                roomsListView.setVisibility(View.GONE);
                                devicesListView.setVisibility(View.GONE);
                                btnsLayout.setVisibility(View.GONE);
                                logoLayout.setVisibility(View.VISIBLE);
                                dd.dismiss();
                            }
                            else if (response.equals("0"))
                            {
                                Toast.makeText(act,"Lock Failed",Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Toast.makeText(act,"No Params",Toast.LENGTH_LONG).show();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            loading.stop();
                        }
                    })
                    {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError
                        {
                            Map<String,String> par = new HashMap<String, String>();
                            par.put( "password" , pass ) ;
                            par.put( "hotel" , "1" ) ;
                            return par;
                        }
                    };
                    Volley.newRequestQueue(act).add(re);
                }
            });
            dd.show();
        }
        else if (lockDB.getLockValue().equals("on")) {
            lockDB.modifyValue("off");
            roomsListView.setVisibility(View.GONE);
            devicesListView.setVisibility(View.GONE);
            btnsLayout.setVisibility(View.GONE);
            logoLayout.setVisibility(View.VISIBLE);
        }
        else {
            lockDB.modifyValue("off");
            roomsListView.setVisibility(View.GONE);
            devicesListView.setVisibility(View.GONE);
            btnsLayout.setVisibility(View.GONE);
            logoLayout.setVisibility(View.VISIBLE);
        }
    }

    private void KeepScreenFull() {
        final Calendar x = Calendar.getInstance(Locale.getDefault());
        final Handler hander = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hander.post(new Runnable() {
                    @Override
                    public void run() {
                        hideSystemUI();
                        KeepScreenFull();
                    }
                });
            }
        }).start();
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    static void sendNotification(final JSONObject notification ) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_MESSAGE_URL, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response)
                    {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MessagesQueue.add(jsonObjectRequest);

    }

    public static void makemessage(String t ,String Order , boolean addOrRemove , int RoomNumber) {

        String NOTIFICATION_TITLE = Order ;
        String NOTIFICATION_MESSAGE = "" ;
        if (Order.equals("DND")) {
            if (addOrRemove) {
                NOTIFICATION_MESSAGE = RoomNumber + " is on DND mode";
            }
            else {
                NOTIFICATION_MESSAGE = "DND mode for "+RoomNumber+ " is off";
            }
        }
        else {
            if (addOrRemove) {
                NOTIFICATION_MESSAGE = "New " + Order + " Order From Room "+RoomNumber;
            }
            else {
                NOTIFICATION_MESSAGE = "Cancelled " + Order + " Order From Room "+RoomNumber;
            }
        }



        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", NOTIFICATION_TITLE);
            notifcationBody.put("message", NOTIFICATION_MESSAGE);
            notifcationBody.put("RoomNumber", RoomNumber);
            notification.put("to", t);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {

        }
        sendNotification(notification);
    }

    void getServiceEmps() {
        StringRequest request = new StringRequest(Request.Method.POST, getServiceEmpsUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null && !response.equals("0")) {
                    try {
                        JSONArray arr = new JSONArray(response);
                        for (int i=0;i<arr.length();i++) {
                            JSONObject row = arr.getJSONObject(i);
                            ServiceEmps emp = new ServiceEmps(row.getInt("id"),row.getInt("projectId"),row.getString("name"),row.getInt("jobNumber"),row.getString("department"),row.getString("mobile"),row.getString("token"));
                            Emps.add(emp);
                        }
                        Log.d("EmpsCount" , Emps.size()+"");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(act,"No service emps",Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {

        };
        Volley.newRequestQueue(act).add(request);
    }

    void getServiceUsersFromFirebase() {
        ServiceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null ) {
                    Emps.clear();
                    //Log.d("EmpsAre ",snapshot.getValue().toString());
                    for (DataSnapshot child : snapshot.getChildren()) {
                        //Log.d("EmpsAre ",child.getValue().toString());
                        int id = 0;
                        if (child.child("id").getValue() != null ) {
                            id = Integer.parseInt( child.child("id").getValue().toString());
                        }
                        String name = "";
                        if (child.child("name").getValue() != null ) {
                            name = child.child("name").getValue().toString();
                        }
                        int jobnum = 0 ;
                        if (child.child("jobNumber").getValue() != null ) {
                            jobnum = Integer.parseInt(child.child("jobNumber").getValue().toString());
                        }
                        String department = "";
                        if (child.child("department").getValue() != null ) {
                            department = child.child("department").getValue().toString();
                        }
                        String mobile = "" ;
                        if (child.child("Mobile").getValue() != null ) {
                            mobile = child.child("Mobile").getValue().toString();
                        }
                        String token = "";
                        if (child.child("token").getValue() != null ) {
                            token = child.child("token").getValue().toString() ;
                        }
                       Emps.add(new ServiceEmps(id,1,name,jobnum,department,mobile,token));
                    }
                    //Toast.makeText(act,Emps.size()+"",Toast.LENGTH_LONG).show();
                    Log.d("EmpsAre ",Emps.size()+"");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void scanLockGateway(View view) {
        int REQUEST_PERMISSION_REQ_CODE = 18 ;
        if (act.checkSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
            return;
        }
        getScanGatewayCallback();
    }

    private void getScanGatewayCallback(){
        GatewayClient.getDefault().startScanGateway(new ScanGatewayCallback() {
            @Override
            public void onScanGatewaySuccess(ExtendedBluetoothDevice device) {
//                LogUtil.d("device:" + device);
                TheFoundGateway = device ;
                gatewaysList.add(device);
                String[] xx = new String[gatewaysList.size()];
                for (int i=0; i<gatewaysList.size();i++) {
                    xx[i] = gatewaysList.get(i).getName();
                }
                ArrayAdapter<String> ad = new ArrayAdapter<String>(act,R.layout.gateway_list_item,xx);
                gatewaysListView.setAdapter(ad);
                GatewayClient.getDefault().stopScanGateway();
//                if (mListApapter != null)
//                    mListApapter.updateData(device);
            }

            @Override
            public void onScanFailed(int errorCode) {

            }
        });
    }

    public void initLockGateway(View view) {

        if (TheFoundGateway == null ) {

        }
        else {
                GatewayClient.getDefault().connectGateway(TheFoundGateway, new ConnectCallback() {
                    @Override
                    public void onConnectSuccess(ExtendedBluetoothDevice device) {
                        //InitGatewayActivity.launch(act, TheFoundGateway);
                        //LogUtil.d("connect success");
                        Toast.makeText(act,"gateway connected",Toast.LENGTH_LONG);
                        EditText wifiName = (EditText) findViewById(R.id.wifiName);
                        EditText wifiPassword = (EditText) findViewById(R.id.wifiPassword);
                        configureGatewayInfo.uid = acc.getUid();
                        configureGatewayInfo.userPwd = acc.getMd5Pwd();
                        configureGatewayInfo.ssid = wifiName.getText().toString().trim();
                        configureGatewayInfo.wifiPwd = wifiPassword.getText().toString().trim();
                        configureGatewayInfo.plugName = device.getAddress();
                        GatewayClient.getDefault().initGateway(configureGatewayInfo, new InitGatewayCallback() {
                            @Override
                            public void onInitGatewaySuccess(DeviceInfo deviceInfo) {
                                LogUtil.d("gateway init success");
                                Toast.makeText(act,"gateway inited",Toast.LENGTH_LONG);
                                isInitSuccess(deviceInfo);
                            }

                            @Override
                            public void onFail(GatewayError error) {
                                Toast.makeText(act,error.getDescription(),Toast.LENGTH_LONG);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onDisconnected() {
                        Toast.makeText(act, "gateway_out_of_time", Toast.LENGTH_LONG).show();
                    }

                });


        }
    }

    private void isInitSuccess(DeviceInfo deviceInfo) {
        ApiService apiService = RetrofitAPIManager.provideClientApi();
        Call<String> call = apiService.gatewayIsInitSuccess(ApiService.CLIENT_ID, MyApplication.getmInstance().getAccountInfo().getAccess_token(), TheFoundGateway.getAddress(), System.currentTimeMillis());
        LogUtil.d("call server isSuccess api");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                String json = response.body();
                if (!TextUtils.isEmpty(json)) {
                    GatewayObj gatewayObj = GsonUtil.toObject(json, GatewayObj.class);
                    if (gatewayObj.errcode == 0) {
                        Toast.makeText(act, "init success", Toast.LENGTH_LONG);
                        uploadGatewayDetail(deviceInfo, gatewayObj.getGatewayId());
                    }
                    else Toast.makeText(act,gatewayObj.errmsg,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(act,t.getMessage(),Toast.LENGTH_LONG).show();
                LogUtil.d("t.getMessage():" + t.getMessage());
            }
        });
    }

    private void uploadGatewayDetail(DeviceInfo deviceInfo, int gatewayId) {
        ApiService apiService = RetrofitAPIManager.provideClientApi();
        EditText wifiName = (EditText) findViewById(R.id.wifiName);
        Call<String> call = apiService.uploadGatewayDetail(ApiService.CLIENT_ID, MyApplication.getmInstance().getAccountInfo().getAccess_token(), gatewayId, deviceInfo.getModelNum(), deviceInfo.hardwareRevision, deviceInfo.getFirmwareRevision(), wifiName.getText().toString(), System.currentTimeMillis());
        LogUtil.d("call server isSuccess api");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                String json = response.body();
                if (!TextUtils.isEmpty(json)) {
                    ServerError error = GsonUtil.toObject(json, ServerError.class);
                    if (error.errcode == 0)
                        Toast.makeText(act,"Done",Toast.LENGTH_LONG).show();
                    else Toast.makeText(act,error.errmsg,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(act,t.getMessage(),Toast.LENGTH_LONG).show();
                LogUtil.d("t.getMessage():" + t.getMessage());
            }
        });
    }

    void getScenes() {
        TuyaHomeSdk.getSceneManagerInstance().getSceneList(Login.THEHOME.getHomeId(), new ITuyaResultCallback<List<SceneBean>>() {
            @Override
            public void onSuccess(List<SceneBean> result) {
                SCENES = result ;
                Log.d("scenesAre",SCENES.size()+"");
                for (SceneBean s : SCENES) {
                    Log.d("scenesAre",s.getName());
//                    if (s.getName().contains("104")) {
//                        TuyaHomeSdk.newSceneInstance(s.getId()).deleteScene(new
//                          IResultCallback() {
//                              @Override
//                              public void onSuccess() {
//                                  //Log.d(TAG, "Delete Scene Success");
//                              }
//
//                              @Override
//                              public void onError(String errorCode, String errorMessage) {
//                              }
//                          });
//                    }
                }
                setSCENES(SCENES);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.d("scenesAre",errorCode+" "+errorMessage);
            }
        });

    }

    void setSCENES(List<SceneBean> SCENES) {
        for (int i=0;i<list.size();i++) {
            if (!searchScene(SCENES, list.get(i).RoomNumber + "ServiceSwitchDNDScene2")) {
                //Log.d("SCENE_DND1", "i am in");
                PreCondition pr = new PreCondition();
                List<PreCondition> lpr = new ArrayList<>();
                lpr.add(pr);
                BoolRule rule = BoolRule.newInstance("dp1", true);
                SceneCondition cond = SceneCondition.createDevCondition(list.get(i).getSERVICE_B(), "1", rule);
                List<SceneCondition> conds = new ArrayList<>();
                conds.add(cond);
                HashMap<String, Object> taskMap = new HashMap<>();
                taskMap.put("2", false); // Starts a device.
                SceneTask task = TuyaHomeSdk.getSceneManagerInstance().createDpTask(
                        list.get(i).getSERVICE_B().devId, // The device ID.
                        taskMap     // The action of the device.
                );
                List<SceneTask> tasks = new ArrayList<>();
                tasks.add(task);
                TuyaHomeSdk.getSceneManagerInstance().createScene(
                        Login.THEHOME.getHomeId(),
                        list.get(i).RoomNumber + "ServiceSwitchDNDScene2", // The name of the scene.
                        false,
                        IMAGES.get(0),  // Indicates whether the scene is displayed on the homepage.
                        conds, // The effective period. This parameter is optional.
                        tasks, // The conditions.
                        null,     // The tasks.
                        SceneBean.MATCH_TYPE_AND, // The type of trigger conditions to match.
                        new ITuyaResultCallback<SceneBean>() {
                            @Override
                            public void onSuccess(SceneBean sceneBean) {
                                Log.d("SCENE_DND1", "createScene Success");
                                TuyaHomeSdk.newSceneInstance(sceneBean.getId()).enableScene(sceneBean.getId(), new
                                        IResultCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d("SCENE_DND1", "enable Scene Success");
                                            }

                                            @Override
                                            public void onError(String errorCode, String errorMessage) {
                                                Log.d("SCENE_DND1", errorMessage + " " + errorCode);
                                            }
                                        });
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {
                                Log.d("SCENE_DND1", errorMessage + " " + errorCode);
                            }
                        });
            }
            if (!searchScene(SCENES, list.get(i).RoomNumber + "ServiceSwitchDNDScene3")) {
                BoolRule rule = BoolRule.newInstance("dp1", true);
                SceneCondition cond = SceneCondition.createDevCondition(list.get(i).getSERVICE_B(), "1", rule);
                List<SceneCondition> conds = new ArrayList<>();
                conds.add(cond);
                HashMap<String, Object> taskMap = new HashMap<>();
                taskMap.put("3", false); // Starts a device.
                SceneTask task = TuyaHomeSdk.getSceneManagerInstance().createDpTask(
                        list.get(i).getSERVICE_B().devId, // The device ID.
                        taskMap     // The action of the device.
                );
                List<SceneTask> tasks = new ArrayList<>();
                tasks.add(task);
                TuyaHomeSdk.getSceneManagerInstance().createScene(
                        Login.THEHOME.getHomeId(),
                        list.get(i).RoomNumber + "ServiceSwitchDNDScene3", // The name of the scene.
                        false,
                        IMAGES.get(0),  // Indicates whether the scene is displayed on the homepage.
                        conds, // The effective period. This parameter is optional.
                        tasks, // The conditions.
                        null,     // The tasks.
                        SceneBean.MATCH_TYPE_AND, // The type of trigger conditions to match.
                        new ITuyaResultCallback<SceneBean>() {
                            @Override
                            public void onSuccess(SceneBean sceneBean) {
                                Log.d("SCENE_DND2", "createScene Success");
                                TuyaHomeSdk.newSceneInstance(sceneBean.getId()).enableScene(sceneBean.getId(), new
                                        IResultCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d("SCENE_DND2", "enable Scene Success");
                                            }

                                            @Override
                                            public void onError(String errorCode, String errorMessage) {
                                                Log.d("SCENE_DND2", errorMessage);
                                            }
                                        });
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {
                                Log.d("SCENE_DND2", errorMessage);
                            }
                        });
            }
            if (!searchScene(SCENES, list.get(i).RoomNumber + "ServiceSwitchDNDScene4")) {
                BoolRule rule = BoolRule.newInstance("dp1", true);
                SceneCondition cond = SceneCondition.createDevCondition(list.get(i).getSERVICE_B(), "1", rule);
                List<SceneCondition> conds = new ArrayList<>();
                conds.add(cond);
                HashMap<String, Object> taskMap = new HashMap<>();
                taskMap.put("4", false); // Starts a device.
                SceneTask task = TuyaHomeSdk.getSceneManagerInstance().createDpTask(
                        list.get(i).getSERVICE_B().devId, // The device ID.
                        taskMap     // The action of the device.
                );
                List<SceneTask> tasks = new ArrayList<>();
                tasks.add(task);
                TuyaHomeSdk.getSceneManagerInstance().createScene(
                        Login.THEHOME.getHomeId(),
                        list.get(i).RoomNumber + "ServiceSwitchDNDScene4", // The name of the scene.
                        false,
                        IMAGES.get(0),  // Indicates whether the scene is displayed on the homepage.
                        conds, // The effective period. This parameter is optional.
                        tasks, // The conditions.
                        null,     // The tasks.
                        SceneBean.MATCH_TYPE_AND, // The type of trigger conditions to match.
                        new ITuyaResultCallback<SceneBean>() {
                            @Override
                            public void onSuccess(SceneBean sceneBean) {
                                Log.d("SCENE_DND2", "createScene Success");
                                TuyaHomeSdk.newSceneInstance(sceneBean.getId()).enableScene(sceneBean.getId(), new
                                        IResultCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d("SCENE_DND2", "enable Scene Success");
                                            }

                                            @Override
                                            public void onError(String errorCode, String errorMessage) {
                                                Log.d("SCENE_DND2", errorMessage);
                                            }
                                        });
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {
                                Log.d("SCENE_DND2", errorMessage);
                            }
                        });
            }
            if (!searchScene(SCENES, list.get(i).RoomNumber + "ServiceSwitchCleanupScene")) {
                BoolRule rule = BoolRule.newInstance("dp2", true);
                SceneCondition cond = SceneCondition.createDevCondition(list.get(i).getSERVICE_B(), "2", rule);
                List<SceneCondition> conds = new ArrayList<>();
                conds.add(cond);
                HashMap<String, Object> taskMap = new HashMap<>();
                taskMap.put("1", false); // Starts a device.
                SceneTask task = TuyaHomeSdk.getSceneManagerInstance().createDpTask(
                        list.get(i).getSERVICE_B().devId, // The device ID.
                        taskMap     // The action of the device.
                );
                List<SceneTask> tasks = new ArrayList<>();
                tasks.add(task);
                TuyaHomeSdk.getSceneManagerInstance().createScene(
                        Login.THEHOME.getHomeId(),
                        list.get(i).RoomNumber + "ServiceSwitchCleanupScene", // The name of the scene.
                        false,
                        IMAGES.get(0),  // Indicates whether the scene is displayed on the homepage.
                        conds, // The effective period. This parameter is optional.
                        tasks, // The conditions.
                        null,     // The tasks.
                        SceneBean.MATCH_TYPE_AND, // The type of trigger conditions to match.
                        new ITuyaResultCallback<SceneBean>() {
                            @Override
                            public void onSuccess(SceneBean sceneBean) {
                                Log.d("SCENE_Cleanup", "createScene Success");
                                TuyaHomeSdk.newSceneInstance(sceneBean.getId()).enableScene(sceneBean.getId(), new
                                        IResultCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d("SCENE_Cleanup", "enable Scene Success");
                                            }

                                            @Override
                                            public void onError(String errorCode, String errorMessage) {
                                                Log.d("SCENE_Cleanup", errorMessage);
                                            }
                                        });
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {
                                Log.d("SCENE_Cleanup", errorMessage);
                            }
                        });
            }
            if (!searchScene(SCENES, list.get(i).RoomNumber + "ServiceSwitchLaundryScene")) {
                BoolRule rule = BoolRule.newInstance("dp3", true);
                SceneCondition cond = SceneCondition.createDevCondition(list.get(i).getSERVICE_B(), "3", rule);
                List<SceneCondition> conds = new ArrayList<>();
                conds.add(cond);
                HashMap<String, Object> taskMap = new HashMap<>();
                taskMap.put("1", false); // Starts a device.
                SceneTask task = TuyaHomeSdk.getSceneManagerInstance().createDpTask(
                        list.get(i).getSERVICE_B().devId, // The device ID.
                        taskMap     // The action of the device.
                );
                List<SceneTask> tasks = new ArrayList<>();
                tasks.add(task);
                TuyaHomeSdk.getSceneManagerInstance().createScene(
                        Login.THEHOME.getHomeId(),
                        list.get(i).RoomNumber + "ServiceSwitchLaundryScene", // The name of the scene.
                        false,
                        IMAGES.get(0),  // Indicates whether the scene is displayed on the homepage.
                        conds, // The effective period. This parameter is optional.
                        tasks, // The conditions.
                        null,     // The tasks.
                        SceneBean.MATCH_TYPE_AND, // The type of trigger conditions to match.
                        new ITuyaResultCallback<SceneBean>() {
                            @Override
                            public void onSuccess(SceneBean sceneBean) {
                                Log.d("SCENE_Laundry", "createScene Success");
                                TuyaHomeSdk.newSceneInstance(sceneBean.getId()).enableScene(sceneBean.getId(), new
                                        IResultCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d("SCENE_Laundry", "enable Scene Success");
                                            }

                                            @Override
                                            public void onError(String errorCode, String errorMessage) {
                                                Log.d("SCENE_Laundry", errorMessage);
                                            }
                                        });
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {
                                Log.d("SCENE_Laundry", errorMessage);
                            }
                        });
            }
            if (!searchScene(SCENES, list.get(i).RoomNumber + "ServiceSwitchCheckoutScene")) {
                BoolRule rule = BoolRule.newInstance("dp4", true);
                SceneCondition cond = SceneCondition.createDevCondition(list.get(i).getSERVICE_B(), "4", rule);
                List<SceneCondition> conds = new ArrayList<>();
                conds.add(cond);
                HashMap<String, Object> taskMap = new HashMap<>();
                taskMap.put("1", false); // Starts a device.
                SceneTask task = TuyaHomeSdk.getSceneManagerInstance().createDpTask(
                        list.get(i).getSERVICE_B().devId, // The device ID.
                        taskMap     // The action of the device.
                );
                List<SceneTask> tasks = new ArrayList<>();
                tasks.add(task);
                TuyaHomeSdk.getSceneManagerInstance().createScene(
                        Login.THEHOME.getHomeId(),
                        list.get(i).RoomNumber + "ServiceSwitchCheckoutScene", // The name of the scene.
                        false,
                        IMAGES.get(0),  // Indicates whether the scene is displayed on the homepage.
                        conds, // The effective period. This parameter is optional.
                        tasks, // The conditions.
                        null,     // The tasks.
                        SceneBean.MATCH_TYPE_AND, // The type of trigger conditions to match.
                        new ITuyaResultCallback<SceneBean>() {
                            @Override
                            public void onSuccess(SceneBean sceneBean) {
                                Log.d("SCENE_Laundry", "createScene Success");
                                TuyaHomeSdk.newSceneInstance(sceneBean.getId()).enableScene(sceneBean.getId(), new
                                        IResultCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d("SCENE_Laundry", "enable Scene Success");
                                            }

                                            @Override
                                            public void onError(String errorCode, String errorMessage) {
                                                Log.d("SCENE_Laundry", errorMessage);
                                            }
                                        });
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {
                                Log.d("SCENE_Laundry", errorMessage);
                            }
                        });
            }
        }
    }

    void getSceneBGs() {
        TuyaHomeSdk.getSceneManagerInstance().getSceneBgs(new ITuyaResultCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> strings) {
                Log.d("scenesAre","images get done");
                IMAGES = strings ;
                getScenes();
            }

            @Override
            public void onError(String s, String s1) {

            }
        });
    }

    public static boolean searchScene (List<SceneBean> list , String name) {
        boolean res = false ;
        for (int i=0 ; i<list.size();i++) {
            if (list.get(i).getName().equals(name)) {
                res = true ;
                break;
            }
        }
        return res ;
    }

    public void goToLocks(View view) {
        Intent i = new Intent(act,Locks.class);
        startActivity(i);
    }
}