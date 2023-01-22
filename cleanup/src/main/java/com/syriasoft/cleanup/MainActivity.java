package com.syriasoft.cleanup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.BuildConfig;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.reflect.TypeToken;
import com.syriasoft.cleanup.TTLOCK.AccountInfo;
import com.syriasoft.cleanup.TTLOCK.ApiService;
import com.syriasoft.cleanup.TTLOCK.LockObj;
import com.syriasoft.cleanup.TTLOCK.RetrofitAPIManager;
import com.ttlock.bl.sdk.util.DigitUtil;
import com.ttlock.bl.sdk.util.GsonUtil;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.INeedLoginListener;
import com.tuya.smart.sdk.bean.DeviceBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {
    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAQmygXvw:APA91bFt5CiONiZPDDj4_kz9hmKXlL1cjfTa_ZNGfobMPmt0gamhzEoN2NHiOxypCDr_r5yfpLvJy-bQSgrykXvaqKkThAniTr-0hpXPBrXm7qWThMmkiaN9o6qaUqfIUwStMMuNedTw";
    final private String contentType = "application/json";
    private String makeCleanOrderDone = LogIn.URL+"makeCleanOrderDone.php";
    private static String ordersUrl = LogIn.URL+"getServiceOrders.php";
    static ListView g ;
    static List<cleanOrder> list =new ArrayList<cleanOrder>();
    static List<cleanOrder> dndList =new ArrayList<cleanOrder>();
    private DND_Adapter ada ;
    private static CleanUp_Adapter adapter ;
    private static Activity act ;
    private static String makeOrderDone =LogIn.URL+"makeServiceOrderDone.php";
    private String getRoomsUrl = LogIn.URL+"getAllRooms.php" ;
    private String changePasswordUrl = LogIn.URL+"changeUserPassword.php";
    static boolean activityStatus = false ;
    private static ProgressBar p ;
    private static UserDB db ;
    private FirebaseDatabase database ;
    static List<ROOM> Rooms ;
    static public List<DatabaseReference> FireRooms ;
    private Random r = new Random();
    private Intent NotificationIntent ;
    private RecyclerView dnds ;
    private ValueEventListener[] CleanupListiner ;
    private ValueEventListener[] LaundryListiner ;
    private ValueEventListener[] RoomServiceListiner ;
    private ValueEventListener[] DNDListiner ;
    private ValueEventListener[] SOSListiner ;
    private ValueEventListener[] MiniBarCheck ;
    private static String DEP= "";
    static DatabaseReference FireRoom ;
    private AccountInfo accountInfo;
    static AccountInfo acc ;
    String password ;
    List<LockObj> lockObjs = new ArrayList<LockObj>();
    List<HomeBean> Homs ;
    HomeBean THE_HOME ;
    List<DeviceBean> Devices ;
    static OrdersDB orderDB ;
    static DNDDB dndDB ;
    static public DatabaseReference MyFireUser ;
    public static List<String> CurrentRoomsStatus ;
    ConnectivityManager connectivityManager ;
    static boolean isConnected = false ;


//--------------------------------------------------------
    //Activity Methods
    @Override
    protected void onStart()
    {
        super.onStart();
        activityStatus = true ;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        activityStatus = false ;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }


//------------------------------------------------------------------------
    //On Creat Method

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        act = this ;
        LogIn.actList.add(act);
        if (LogIn.actList.size()>2) {
            for (int i = 1 ; i<(LogIn.actList.size()-1);i++)
            {
                LogIn.actList.get(i).finish();
            }
        }
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        isConnected = connectivityManager.getActiveNetworkInfo().isConnected();
        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                isConnected = true ;
            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                isConnected = false ;
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
            }
        });
        orderDB = new OrdersDB(act);
        dndDB = new DNDDB(act);
        DEP = LogIn.db.getUser().department ;
        NotificationIntent = new Intent(this, MainActivity.class);
        Rooms = new ArrayList<ROOM>();
        FireRooms = new ArrayList<DatabaseReference>();
        CurrentRoomsStatus = new ArrayList<String>() ;
        database = FirebaseDatabase.getInstance("https://hotelservices-ebe66.firebaseio.com/");
        MyFireUser = database.getReference(LogIn.Project+"ServiceUsers/"+LogIn.db.getUser().jobNumber);
        User MYUSER = new User(LogIn.db.getUser().id,LogIn.db.getUser().name,LogIn.db.getUser().jobNumber,LogIn.db.getUser().Mobile,LogIn.db.getUser().department,LogIn.db.getUser().token);
        MyFireUser.setValue(MYUSER);
        getRooms();
        db = new UserDB(act);
        if (!db.isLogedIn()) {
            db.insertUser(LogIn.db.getUser().id, LogIn.db.getUser().name, LogIn.db.getUser().Mobile, LogIn.db.getUser().token, LogIn.db.getUser().department, LogIn.db.getUser().jobNumber,0, BuildConfig.VERSION_CODE);
        }
        g = findViewById(R.id.cleanUpGrid);
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task)
                    {
                        if (task.getResult() != null) {
                            String token = task.getResult().getToken();
                            MyFireUser.child("token").setValue(token);
                            sendRegistrationToServer(token);
                        }
                    }
                });
        TextView mainText = findViewById(R.id.mainText) ;
        mainText.setText(LogIn.db.getUser().department + " Orders");
         p = findViewById(R.id.progressBar2);
        Toast.makeText(act,LogIn.db.getUser().department , Toast.LENGTH_LONG).show();
        final GridLayoutManager manager = new GridLayoutManager(this,4);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        dnds = findViewById(R.id.dnd_recycler);
        dnds.setLayoutManager(manager);
        ada = new DND_Adapter(dndDB.getOrders());
        dnds.setAdapter(ada);
        orderDB.removeAll();
        adapter = new CleanUp_Adapter(orderDB.getOrders(),act);
        g.setAdapter(adapter);

        auth();
        setTuyaApplication();
        goLogIn();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.getItem(0).setTitle(LogIn.db.getUser().name);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {

            case R.id.button2:
                Button x =(Button)findViewById(R.id.button2);
                sgnOut(x);
                break;
            case R.id.goToRooms:
                Intent i = new Intent(act,ROOMS.class);
                startActivity(i);
                break;
            case R.id.changePassword :
                changePasswordDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//-------------------------------------------------------------------------
    //Make Order Done

    static void makeOrderDone(final int position , final int room, final  int orderNumber , final Dialog d , final int jobNumber ,final String dep )
    {

        final StringRequest re = new StringRequest(Request.Method.POST, makeOrderDone, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {

                if (response.equals("1"))
                {
                    //d.dismiss();
                    /*
                    int index = 0 ;
                    //Toast.makeText(act,response+" "+dep+" "+room,Toast.LENGTH_LONG).show();
                    for (int i=0;i<Rooms.size();i++)
                    {
                        if (Rooms.get(i).RoomNumber == Integer.parseInt( list.get(position).roomNumber))
                        {
                            index = i ;
                        }
                    }
                    FireRooms.get(index).child(dep).setValue(0);
                    if (dep.equals("Cleanup"))
                    {
                        final int finalIndex = index;
                        FireRooms.get(index).child("roomStatus").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.getValue() != null)
                                {
                                    if (dataSnapshot.getValue().toString().equals("3"))
                                    {
                                        FireRooms.get(finalIndex).child("roomStatus").setValue(1);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    */
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> p = new HashMap<String, String>();
                p.put("room" ,String.valueOf( room ));
                p.put("id" ,String.valueOf( orderNumber));
                p.put("dep" , dep);
                Calendar x = Calendar.getInstance(Locale.getDefault());
                double time =  x.getTimeInMillis();
                p.put("time" ,String.valueOf(time));
                p.put("jobNumber" ,String.valueOf( jobNumber));
                return p;
            }
        };
        Volley.newRequestQueue(act).add(re);
    }

//------------------------------------------------------------------------
    //Send Token To Server
    void sendRegistrationToServer(final String token)
    {
        String url = LogIn.URL+"registCleanupUserToken.php";
        StringRequest r = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                Log.d("TokenResp" , response) ;
                if (response.equals("1"))
                {

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
                Map<String,String> params = new HashMap<String,String>();
                params.put("token",token);
                params.put("jnum", String.valueOf(LogIn.db.getUser().jobNumber));
                return params;
            }
        };

        Volley.newRequestQueue(this ).add(r);

    }

    public void sgnOut(View view) {

        for (int i=0;i<FireRooms.size();i++)
        {
            if (DEP.equals("Cleanup"))
            {
                FireRooms.get(i).child("Cleanup").removeEventListener(CleanupListiner[i]);
                FireRooms.get(i).child("DND").removeEventListener(DNDListiner[i]);
                //FireRooms.get(i).child("Cleanup").removeEventListener(ReceivingService.CleanupListiner[i]);
                //FireRooms.get(i).child("DND").removeEventListener(ReceivingService.DNDListiner[i]);
            }
            else if (DEP.equals("Laundry"))
            {
                FireRooms.get(i).child("Laundry").removeEventListener(LaundryListiner[i]);
                FireRooms.get(i).child("DND").removeEventListener(DNDListiner[i]);
                //FireRooms.get(i).child("Laundry").removeEventListener(ReceivingService.LaundryListiner[i]);
                //FireRooms.get(i).child("DND").removeEventListener(ReceivingService.DNDListiner[i]);
            }
            else if (DEP.equals("RoomService"))
            {
                FireRooms.get(i).child("RoomService").removeEventListener(RoomServiceListiner[i]);
                FireRooms.get(i).child("DND").removeEventListener(DNDListiner[i]);
                FireRooms.get(i).child("SOS").removeEventListener(SOSListiner[i]);
                FireRooms.get(i).child("MiniBarCheck").removeEventListener(MiniBarCheck[i]);
                //FireRooms.get(i).child("RoomService").removeEventListener(ReceivingService.RoomServiceListiner[i]);
                //FireRooms.get(i).child("DND").removeEventListener(ReceivingService.DNDListiner[i]);
                //FireRooms.get(i).child("SOS").removeEventListener(ReceivingService.SOSListiner[i]);
                //FireRooms.get(i).child("MiniBarCheck").removeEventListener(ReceivingService.MiniBarCheck[i]);

            }
            else if (DEP.equals("Service"))
            {
                FireRooms.get(i).child("Cleanup").removeEventListener(CleanupListiner[i]);
                FireRooms.get(i).child("Laundry").removeEventListener(LaundryListiner[i]);
                FireRooms.get(i).child("DND").removeEventListener(DNDListiner[i]);
                FireRooms.get(i).child("RoomService").removeEventListener(RoomServiceListiner[i]);
                FireRooms.get(i).child("SOS").removeEventListener(SOSListiner[i]);
                FireRooms.get(i).child("MiniBarCheck").removeEventListener(MiniBarCheck[i]);
                //FireRooms.get(i).child("Cleanup").removeEventListener(ReceivingService.CleanupListiner[i]);
                //FireRooms.get(i).child("Laundry").removeEventListener(ReceivingService.LaundryListiner[i]);
                //FireRooms.get(i).child("DND").removeEventListener(ReceivingService.DNDListiner[i]);
               // FireRooms.get(i).child("RoomService").removeEventListener(ReceivingService.RoomServiceListiner[i]);
                //FireRooms.get(i).child("SOS").removeEventListener(ReceivingService.SOSListiner[i]);
                //FireRooms.get(i).child("MiniBarCheck").removeEventListener(ReceivingService.MiniBarCheck[i]);
            }
        }
        LogIn.db.logout();
        for (int i=0;i<LogIn.actList.size();i++)
        {
            LogIn.actList.get(i).finish();
        }
        Intent i = new Intent(act,LogIn.class);
        startActivity(i);
    }

    public static  void alertActivity() {
        for (int i =0;i<list.size();i++)
        {
            if (list.get(i).dep.equals("SOS"))
            {
                final int finalI = i;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run()
                    {
                        g.getChildAt(finalI).setBackgroundColor(Color.RED);
                    }
                });

            }
        }

    }

    private void getRooms() {
        final LoadingDialog loading = new LoadingDialog(act);
        StringRequest re = new StringRequest(Request.Method.POST, getRoomsUrl, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {
                loading.close();
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
                        ROOM room = new ROOM(id,rNum,Hotel,b,bId,f,fId,rType,ss,sn,si,rn,rs,t,dep,c,l,roomS,ch,res,sos,dnd,PowerSwitch,DoorSensor,MotionSensor,Thermostat,zbgateway,CurtainSwitch,ServiceSwitch,lock,Switch1,Switch2,Switch3,Switch4,LockGateway,LockName,po,cu,doo,temp,token);
                        room.printRoomOnLog();
                        Rooms.add(room);
                        FireRooms.add(database.getReference(LogIn.Project+"/B"+room.Building+"/F"+room.Floor+"/R"+room.RoomNumber));
                        CurrentRoomsStatus.add("1");
                    }
                    Log.d("roomCount" , "room "+Rooms.size()+" fires "+FireRooms.size());
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                CleanupListiner = new ValueEventListener[Rooms.size()];
                LaundryListiner = new ValueEventListener[Rooms.size()];
                RoomServiceListiner = new ValueEventListener[Rooms.size()];
                DNDListiner = new ValueEventListener[Rooms.size()];
                SOSListiner = new ValueEventListener[Rooms.size()];
                MiniBarCheck = new ValueEventListener[Rooms.size()];
                setRoomsListeners();
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                loading.close();
                AlertDialog.Builder b = new AlertDialog.Builder(act);
                b.setTitle("Getting data failed")
                        .setMessage("failed to get data .. try again ?? ")
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                act.finish();
                            }
                        })
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getRooms();
                            }
                        }).create().show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String,String> par = new HashMap<String, String>();
                par.put("Hotel" , String.valueOf(1));
                return par;
            }
        };
        Volley.newRequestQueue(act).add(re);
    }

    private void setRoomsListeners()
    {
        for ( int i=0; i < FireRooms.size() ; i++)
        {
            final int finalI = i;

            if (DEP.equals("Cleanup")) {
                CleanupListiner[i] = FireRooms.get(i).child("Cleanup").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (Long.parseLong(dataSnapshot.getValue().toString()) !=0  ) {
                            boolean status = false ;

                            status = orderDB.searchOrder(Rooms.get(finalI).RoomNumber,"Cleanup");
//                            for (int j=0;j<orderDB.getOrders().size();j++)
//                            {
//                                if (orderDB.getOrders().get(j).roomNumber.equals(String.valueOf(Rooms.get(finalI).RoomNumber)) && orderDB.getOrders().get(j).dep.equals("Cleanup"))
//                                {
//                                    status=true;
//                                }
//                            }
                            if (!status)
                            {
                                long timee = Long.parseLong(dataSnapshot.getValue().toString());
                                orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"Cleanup","",timee);
                                adapter = new CleanUp_Adapter(orderDB.getOrders(),act);
                                g.setAdapter(adapter);
                            }
                        }
                        else
                        {
                            for (int x=0;x<orderDB.getOrders().size();x++)
                            {
                                if (Long.parseLong(orderDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && orderDB.getOrders().get(x).dep.equals("Cleanup") )
                                {
                                    orderDB.removeRow( Long.parseLong(orderDB.getOrders().get(x).orderNumber));
                                    adapter = new CleanUp_Adapter(orderDB.getOrders(),act);
                                    g.setAdapter(adapter);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                DNDListiner[i] = FireRooms.get(i).child("DND").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        //Log.d("DNDChanged" , "_____________________") ;
                        //Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list befor "+dndDB.getOrders().size()) ;
                        if (Long.parseLong(dataSnapshot.getValue().toString()) > 0 )
                        {
                            boolean status = false ;
                            status = dndDB.searchOrder(Rooms.get(finalI).RoomNumber,"DND");
                            //Log.d("DNDChanged" , status+"") ;
                            if (!status) {

                                long time = Long.parseLong(dataSnapshot.getValue().toString());
                                dndDB.insertOrder(Rooms.get(finalI).RoomNumber,"DND","",time);
                                //Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list after "+dndDB.getOrders().size()) ;
                            }
                            ada = new DND_Adapter(dndDB.getOrders());
                            dnds.setAdapter(ada);
                        }
                        else if(Long.parseLong(dataSnapshot.getValue().toString()) == 0 )
                        {
                            for (int x=0;x<dndDB.getOrders().size();x++)
                            {
                                if (Long.parseLong(dndDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && dndDB.getOrders().get(x).dep.equals("DND"))
                                {
                                    dndDB.removeRow( Long.parseLong(dndDB.getOrders().get(x).orderNumber));
                                }
                            }
                            //Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list after "+dndDB.getOrders().size()) ;
                            ada = new DND_Adapter(dndDB.getOrders());
                            dnds.setAdapter(ada);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                SOSListiner[i] = FireRooms.get(i).child("SOS").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (!dataSnapshot.getValue().toString().equals("0"))
                        {
                            boolean status = false ;
                            status = orderDB.searchOrder(Rooms.get(finalI).RoomNumber,"SOS");
                            long time =  Long.parseLong(dataSnapshot.getValue().toString());
//                            for (int j=0;j<list.size();j++)
//                            {
//                                if (list.get(j).roomNumber.equals(String.valueOf(Rooms.get(finalI).RoomNumber)) && list.get(j).dep.equals("SOS") )
//                                {
//                                    status=true;
//                                }
//                            }
                            if (!status)
                            {
                                //Calendar x = Calendar.getInstance(Locale.getDefault());
//                                long timee = Long.parseLong(dataSnapshot.getValue().toString());
//                                list.add(new cleanOrder(String.valueOf(Rooms.get(finalI).RoomNumber), String.valueOf(1), "SOS", "", timee));
//                                adapter.notifyDataSetChanged();
                                //int reqCode = r.nextInt();
                                //showNotification(act,"SOS "+Rooms.get(finalI).RoomNumber , "SOS on room "+Rooms.get(finalI).RoomNumber,NotificationIntent,reqCode);
                                orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"SOS","",time);
                                adapter = new CleanUp_Adapter(orderDB.getOrders(),act);
                                g.setAdapter(adapter);
                            }
                        }
                        else
                        {
                            for (int x=0;x<orderDB.getOrders().size();x++)
                            {
//                                if (Integer.parseInt(list.get(x).roomNumber) == Rooms.get(finalI).RoomNumber && list.get(x).dep.equals("SOS"))
//                                {
//                                    list.remove(x);
//                                    adapter.notifyDataSetChanged();
//                                }
                                if (Long.parseLong(orderDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && orderDB.getOrders().get(x).dep.equals("SOS") )
                                {
                                    orderDB.removeRow( Long.parseLong(orderDB.getOrders().get(x).orderNumber));
                                    adapter = new CleanUp_Adapter(orderDB.getOrders(),act);
                                    g.setAdapter(adapter);
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }
            else if (DEP.equals("Laundry"))
            {
                LaundryListiner[i] = FireRooms.get(i).child("Laundry").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (Long.parseLong(dataSnapshot.getValue().toString()) != 0 )
                        {
                            boolean status = false ;
                            status = orderDB.searchOrder(Rooms.get(finalI).RoomNumber,"Laundry");
                            if (!status)
                            {
                                long timee =  Long.parseLong(dataSnapshot.getValue().toString());
                                orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"Laundry","",timee);
                                List<cleanOrder> list = orderDB.getOrders() ;
                                adapter = new CleanUp_Adapter(list,act);
                                g.setAdapter(adapter);
//                                adapter.notifyDataSetChanged();
//                                int reqCode = r.nextInt();
                                //showNotification(act,"Laundry Order "+Rooms.get(finalI).RoomNumber , "new laundry order from "+Rooms.get(finalI).RoomNumber,NotificationIntent,reqCode);
                            }

                        }
                        else
                        {
                            for (int x=0;x<orderDB.getOrders().size();x++)
                            {
//                                if (Integer.parseInt(list.get(x).roomNumber) == Rooms.get(finalI).RoomNumber && list.get(x).dep.equals("Laundry") )
//                                {
//                                    list.remove(x);
//                                    adapter.notifyDataSetChanged();
//                                }
                                if (Long.parseLong(orderDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && orderDB.getOrders().get(x).dep.equals("Laundry") )
                                {
                                    orderDB.removeRow( Long.parseLong(orderDB.getOrders().get(x).orderNumber));
                                    List<cleanOrder> list = orderDB.getOrders() ;
                                    adapter = new CleanUp_Adapter(list,act);
                                    g.setAdapter(adapter);
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                DNDListiner[i] = FireRooms.get(i).child("DND").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        //Log.d("DNDChanged" , "_____________________") ;
                        //Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list befor "+dndDB.getOrders().size()) ;
                        if (Long.parseLong(dataSnapshot.getValue().toString()) > 0 )
                        {
                            boolean status = false ;
                            status = dndDB.searchOrder(Rooms.get(finalI).RoomNumber,"DND");
                            //Log.d("DNDChanged" , status+"") ;
                            if (!status) {

                                long time = Long.parseLong(dataSnapshot.getValue().toString());
                                dndDB.insertOrder(Rooms.get(finalI).RoomNumber,"DND","",time);
                                //Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list after "+dndDB.getOrders().size()) ;
                            }
                            ada = new DND_Adapter(dndDB.getOrders());
                            dnds.setAdapter(ada);
                        }
                        else if(Long.parseLong(dataSnapshot.getValue().toString()) == 0 )
                        {
                            for (int x=0;x<dndDB.getOrders().size();x++)
                            {
                                if (Long.parseLong(dndDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && dndDB.getOrders().get(x).dep.equals("DND"))
                                {
                                    dndDB.removeRow( Long.parseLong(dndDB.getOrders().get(x).orderNumber));
                                }
                            }
                            //Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list after "+dndDB.getOrders().size()) ;
                            ada = new DND_Adapter(dndDB.getOrders());
                            dnds.setAdapter(ada);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            else if (DEP.equals("RoomService"))
            {
                RoomServiceListiner[i] = FireRooms.get(i).child("RoomService").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1)
                    {
                        if (Long.parseLong(dataSnapshot1.getValue().toString()) != 0 )
                        {
                            boolean status = false ;
                            long time = Long.parseLong(dataSnapshot1.getValue().toString());
                            status = orderDB.searchOrder(Rooms.get(finalI).RoomNumber,"RoomService");
//                            for (int j=0;j<list.size();j++)
//                            {
//                                if ( Integer.parseInt(list.get(j).roomNumber) == Rooms.get(finalI).RoomNumber && list.get(j).dep.equals("RoomService") )
//                                {
//                                    status=true;
//
//                                }
//                            }
                            //Log.d("roomserviceProblem" , status+"");
                            if (!status)
                            {
                                FireRooms.get(finalI).child("RoomServiceText").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot2)
                                    {
                                        //Calendar x = Calendar.getInstance(Locale.getDefault());
                                        //long timee = x.getTimeInMillis();
                                        if (dataSnapshot2.getValue() != null ) {
//                                            list.add(new cleanOrder(String.valueOf(Rooms.get(finalI).RoomNumber), String.valueOf(1), "RoomService", dataSnapshot2.getValue().toString(), time));
//                                            adapter.notifyDataSetChanged();
//                                            Log.d("roomserviceProblem" , "inserted");
                                            orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"RoomService",dataSnapshot2.getValue().toString(),time);
                                            List<cleanOrder> list = orderDB.getOrders() ;
                                            adapter = new CleanUp_Adapter(list,act);
                                            g.setAdapter(adapter);
                                        }
                                        else {
//                                            list.add(new cleanOrder(String.valueOf(Rooms.get(finalI).RoomNumber), String.valueOf(1), "RoomService", "", time));
//                                            adapter.notifyDataSetChanged();
                                            orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"RoomService","",time);
                                            List<cleanOrder> list = orderDB.getOrders() ;
                                            adapter = new CleanUp_Adapter(list,act);
                                            g.setAdapter(adapter);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                        else
                        {
                            for (int x=0;x<orderDB.getOrders().size();x++)
                            {
//                                if (Integer.parseInt(list.get(x).roomNumber) == Rooms.get(finalI).RoomNumber && list.get(x).dep.equals("RoomService"))
//                                {
//                                    list.remove(x);
//                                    adapter.notifyDataSetChanged();
//                                }
                                if (Integer.parseInt(orderDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && orderDB.getOrders().get(x).dep.equals("RoomService") )
                                {
                                    orderDB.removeRow( Long.parseLong(orderDB.getOrders().get(x).orderNumber));
                                    List<cleanOrder> list = orderDB.getOrders() ;
                                    adapter = new CleanUp_Adapter(list,act);
                                    g.setAdapter(adapter);
                                }
                            }
                        }
                        //past here

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                DNDListiner[i] = FireRooms.get(i).child("DND").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        Log.d("DNDChanged" , "_____________________") ;
                        Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list befor "+dndDB.getOrders().size()) ;
                        if (Long.parseLong(dataSnapshot.getValue().toString()) > 0 )
                        {
                            boolean status = false ;
                            status = dndDB.searchOrder(Rooms.get(finalI).RoomNumber,"DND");
                            Log.d("DNDChanged" , status+"") ;
                            if (!status) {

                                long time = Long.parseLong(dataSnapshot.getValue().toString());
                                dndDB.insertOrder(Rooms.get(finalI).RoomNumber,"DND","",time);
                                Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list after "+dndDB.getOrders().size()) ;
                            }
                            ada = new DND_Adapter(dndDB.getOrders());
                            dnds.setAdapter(ada);
                        }
                        else if(Long.parseLong(dataSnapshot.getValue().toString()) == 0 )
                        {
                            for (int x=0;x<dndDB.getOrders().size();x++)
                            {
                                if (Long.parseLong(dndDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && dndDB.getOrders().get(x).dep.equals("DND"))
                                {
                                    dndDB.removeRow( Long.parseLong(dndDB.getOrders().get(x).orderNumber));
                                }
                            }
                            Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list after "+dndDB.getOrders().size()) ;
                            ada = new DND_Adapter(dndDB.getOrders());
                            dnds.setAdapter(ada);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                SOSListiner[i] = FireRooms.get(i).child("SOS").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            if (!dataSnapshot.getValue().toString().equals("0"))
                            {
                                boolean status = false ;
                                status = orderDB.searchOrder(Rooms.get(finalI).RoomNumber,"SOS");
                                long time =  Long.parseLong(dataSnapshot.getValue().toString());
//                            for (int j=0;j<list.size();j++)
//                            {
//                                if (list.get(j).roomNumber.equals(String.valueOf(Rooms.get(finalI).RoomNumber)) && list.get(j).dep.equals("SOS") )
//                                {
//                                    status=true;
//                                }
//                            }
                                if (!status)
                                {
                                    orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"SOS","",time);
                                    List<cleanOrder> list = orderDB.getOrders() ;
                                    adapter = new CleanUp_Adapter(list,act);
                                    g.setAdapter(adapter);
                                }
                            }
                            else
                            {
                                for (int x=0;x<orderDB.getOrders().size();x++)
                                {
//                                if (Integer.parseInt(list.get(x).roomNumber) == Rooms.get(finalI).RoomNumber && list.get(x).dep.equals("SOS"))
//                                {
//                                    list.remove(x);
//                                    adapter.notifyDataSetChanged();
//                                }
                                    if (Long.parseLong(orderDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && orderDB.getOrders().get(x).dep.equals("SOS") )
                                    {
                                        orderDB.removeRow( Long.parseLong(orderDB.getOrders().get(x).orderNumber));
                                        List<cleanOrder> list = orderDB.getOrders() ;
                                        adapter = new CleanUp_Adapter(list,act);
                                        g.setAdapter(adapter);
                                    }
                                }
                            }
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
                MiniBarCheck[i] = FireRooms.get(i).child("MiniBarCheck").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() !=null) {
                            if (dataSnapshot.getValue().toString().equals("0"))
                            {
                                for (int x=0;x<orderDB.getOrders().size();x++)
                                {
//                                    if (Integer.parseInt(list.get(x).roomNumber) == Rooms.get(finalI).RoomNumber && list.get(x).dep.equals("MiniBarCheck") )
//                                    {
//                                        list.remove(x);
//                                        adapter.notifyDataSetChanged();
//                                    }
                                    if (Long.parseLong(orderDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && orderDB.getOrders().get(x).dep.equals("MiniBarCheck") )
                                    {
                                        orderDB.removeRow( Long.parseLong(orderDB.getOrders().get(x).orderNumber));
                                        List<cleanOrder> list = orderDB.getOrders() ;
                                        adapter = new CleanUp_Adapter(list,act);
                                        g.setAdapter(adapter);
                                    }
                                }
                            }
                            else
                            {
                                boolean status = false ;
                                status = orderDB.searchOrder(Rooms.get(finalI).RoomNumber,"MiniBarCheck");
//                                for (int j=0;j<list.size();j++)
//                                {
//                                    if (list.get(j).roomNumber.equals(String.valueOf(Rooms.get(finalI).RoomNumber)) && list.get(j).dep.equals("MiniBarCheck"))
//                                    {
//                                        status=true;
//                                    }
//                                }
                                if (!status)
                                {
//                                    Calendar x = Calendar.getInstance(Locale.getDefault());
//                                    long timee =  x.getTimeInMillis();
//                                    list.add(new cleanOrder(String.valueOf(Rooms.get(finalI).RoomNumber),String.valueOf(1),"MiniBarCheck","",timee));
//                                    g.setAdapter(adapter);
                                    long timee =  Long.parseLong(dataSnapshot.getValue().toString());
                                    orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"MiniBarCheck","",timee);
                                    List<cleanOrder> list = orderDB.getOrders() ;
                                    adapter = new CleanUp_Adapter(list,act);
                                    g.setAdapter(adapter);
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            else if (DEP.equals("Service"))
            {
                DNDListiner[i] = FireRooms.get(i).child("DND").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        Log.d("DNDChanged" , "_____________________") ;
                        Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list befor "+dndDB.getOrders().size()) ;
                        if (Long.parseLong(dataSnapshot.getValue().toString()) > 0 )
                        {
                            boolean status = false ;
                            status = dndDB.searchOrder(Rooms.get(finalI).RoomNumber,"DND");
                            Log.d("DNDChanged" , status+"") ;
                            if (!status) {

                                long time = Long.parseLong(dataSnapshot.getValue().toString());
                                dndDB.insertOrder(Rooms.get(finalI).RoomNumber,"DND","",time);
                                Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list after "+dndDB.getOrders().size()) ;
                            }
                            ada = new DND_Adapter(dndDB.getOrders());
                            dnds.setAdapter(ada);
                        }
                        else if(Long.parseLong(dataSnapshot.getValue().toString()) == 0 )
                        {
                            for (int x=0;x<dndDB.getOrders().size();x++)
                            {
                                if (Long.parseLong(dndDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && dndDB.getOrders().get(x).dep.equals("DND"))
                                {
                                    dndDB.removeRow( Long.parseLong(dndDB.getOrders().get(x).orderNumber));
                                }
                            }
                            Log.d("DNDChanged" , Rooms.get(finalI).RoomNumber+" i am changed to " + dataSnapshot.getValue().toString()+" in list after "+dndDB.getOrders().size()) ;
                            ada = new DND_Adapter(dndDB.getOrders());
                            dnds.setAdapter(ada);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                CleanupListiner[i] = FireRooms.get(i).child("Cleanup").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {

                        if (Long.parseLong(dataSnapshot.getValue().toString()) !=0  )
                        {
                            boolean status = false ;

                            status = orderDB.searchOrder(Rooms.get(finalI).RoomNumber,"Cleanup");
//                            for (int j=0;j<orderDB.getOrders().size();j++)
//                            {
//                                if (orderDB.getOrders().get(j).roomNumber.equals(String.valueOf(Rooms.get(finalI).RoomNumber)) && orderDB.getOrders().get(j).dep.equals("Cleanup"))
//                                {
//                                    status=true;
//                                }
//                            }
                            if (!status)
                            {
                                long timee = Long.parseLong(dataSnapshot.getValue().toString());
                                orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"Cleanup","",timee);
                                List<cleanOrder> list = orderDB.getOrders() ;
                                //TODO insert the sort function here to sort the list before pass it to adapter

                                adapter = new CleanUp_Adapter(list,act);
                                g.setAdapter(adapter);
                            }
                        }
                        else
                        {
                            for (int x=0;x<orderDB.getOrders().size();x++)
                            {
                                if (Long.parseLong(orderDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && orderDB.getOrders().get(x).dep.equals("Cleanup") )
                                {
                                    orderDB.removeRow( Long.parseLong(orderDB.getOrders().get(x).orderNumber));
                                    List<cleanOrder> list = orderDB.getOrders() ;
                                    //TODO insert the sort function here to sort the list before pass it to adapter

                                    adapter = new CleanUp_Adapter(list,act);
                                    g.setAdapter(adapter);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                LaundryListiner[i] = FireRooms.get(i).child("Laundry").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (Long.parseLong(dataSnapshot.getValue().toString()) != 0 )
                        {
                            boolean status = false ;
                            status = orderDB.searchOrder(Rooms.get(finalI).RoomNumber,"Laundry");
                            if (!status)
                            {
                                long timee =  Long.parseLong(dataSnapshot.getValue().toString());
                                orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"Laundry","",timee);
                                List<cleanOrder> list = orderDB.getOrders() ;
                                //TODO insert the sort function here to sort the list before pass it to adapter

                                adapter = new CleanUp_Adapter(list,act);
                                g.setAdapter(adapter);
//                                adapter.notifyDataSetChanged();
//                                int reqCode = r.nextInt();
                                //showNotification(act,"Laundry Order "+Rooms.get(finalI).RoomNumber , "new laundry order from "+Rooms.get(finalI).RoomNumber,NotificationIntent,reqCode);
                            }

                        }
                        else
                        {
                            for (int x=0;x<orderDB.getOrders().size();x++)
                            {
//                                if (Integer.parseInt(list.get(x).roomNumber) == Rooms.get(finalI).RoomNumber && list.get(x).dep.equals("Laundry") )
//                                {
//                                    list.remove(x);
//                                    adapter.notifyDataSetChanged();
//                                }
                                if (Long.parseLong(orderDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && orderDB.getOrders().get(x).dep.equals("Laundry") )
                                {
                                    orderDB.removeRow( Long.parseLong(orderDB.getOrders().get(x).orderNumber));
                                    List<cleanOrder> list = orderDB.getOrders() ;
                                    //TODO insert the sort function here to sort the list before pass it to adapter

                                    adapter = new CleanUp_Adapter(list,act);
                                    g.setAdapter(adapter);
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                RoomServiceListiner[i] = FireRooms.get(i).child("RoomService").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1)
                    {
                        FireRooms.get(finalI).child("RoomServiceText").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (Long.parseLong(dataSnapshot1.getValue().toString()) != 0 )
                                {
                                    boolean status = false ;
                                    long time = Long.parseLong(dataSnapshot1.getValue().toString());
                                    status = orderDB.searchOrder(Rooms.get(finalI).RoomNumber,"RoomService");
                                    if (!status)
                                    {
                                        if (dataSnapshot.getValue() != null ) {
                                           list.add(new cleanOrder(String.valueOf(Rooms.get(finalI).RoomNumber), String.valueOf(1), "RoomService", dataSnapshot.getValue().toString(), time));
                                            adapter.notifyDataSetChanged();
                                           orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"RoomService",dataSnapshot.getValue().toString(),time);
                                            List<cleanOrder> list = orderDB.getOrders() ;
                                            //TODO insert the sort function here to sort the list before pass it to adapter

                                            adapter = new CleanUp_Adapter(list,act);
                                           g.setAdapter(adapter);
                                        }
                                       else {
                                           list.add(new cleanOrder(String.valueOf(Rooms.get(finalI).RoomNumber), String.valueOf(1), "RoomService", "", time));
                                            adapter.notifyDataSetChanged();
                                          orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"RoomService","",time);
                                            List<cleanOrder> list = orderDB.getOrders() ;
                                            //TODO insert the sort function here to sort the list before pass it to adapter

                                            adapter = new CleanUp_Adapter(list,act);
                                            g.setAdapter(adapter);
                                       }
                                    }
                                }
                                else
                                {
                                    for (int x=0;x<orderDB.getOrders().size();x++)
                                    {
//                                if (Integer.parseInt(list.get(x).roomNumber) == Rooms.get(finalI).RoomNumber && list.get(x).dep.equals("RoomService"))
//                                {
//                                    list.remove(x);
//                                    adapter.notifyDataSetChanged();
//                                }
                                        if (Long.parseLong(orderDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && orderDB.getOrders().get(x).dep.equals("RoomService") )
                                        {
                                            orderDB.removeRow( Long.parseLong(orderDB.getOrders().get(x).orderNumber));
                                            List<cleanOrder> list = orderDB.getOrders() ;
                                            //TODO insert the sort function here to sort the list before pass it to adapter

                                            adapter = new CleanUp_Adapter(list,act);
                                            g.setAdapter(adapter);
                                        }
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                SOSListiner[i] = FireRooms.get(i).child("SOS").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (!dataSnapshot.getValue().toString().equals("0"))
                        {
                            boolean status = false ;
                            status = orderDB.searchOrder(Rooms.get(finalI).RoomNumber,"SOS");
                            long time =  Long.parseLong(dataSnapshot.getValue().toString());
//                            for (int j=0;j<list.size();j++)
//                            {
//                                if (list.get(j).roomNumber.equals(String.valueOf(Rooms.get(finalI).RoomNumber)) && list.get(j).dep.equals("SOS") )
//                                {
//                                    status=true;
//                                }
//
                            if (!status)
                            {
                                //Calendar x = Calendar.getInstance(Locale.getDefault());
//                                long timee = Long.parseLong(dataSnapshot.getValue().toString());
//                                list.add(new cleanOrder(String.valueOf(Rooms.get(finalI).RoomNumber), String.valueOf(1), "SOS", "", timee));
//                                adapter.notifyDataSetChanged();
                                //int reqCode = r.nextInt();
                                //showNotification(act,"SOS "+Rooms.get(finalI).RoomNumber , "SOS on room "+Rooms.get(finalI).RoomNumber,NotificationIntent,reqCode);
                                orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"SOS","",time);
                                List<cleanOrder> list = orderDB.getOrders() ;
                                //TODO insert the sort function here to sort the list before pass it to adapter

                                adapter = new CleanUp_Adapter(list,act);
                                g.setAdapter(adapter);
                            }
                        }
                        else
                        {
                            for (int x=0;x<orderDB.getOrders().size();x++)
                            {
//                                if (Integer.parseInt(list.get(x).roomNumber) == Rooms.get(finalI).RoomNumber && list.get(x).dep.equals("SOS"))
//                                {
//                                    list.remove(x);
//                                    adapter.notifyDataSetChanged();
//                                }
                                if (Long.parseLong(orderDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && orderDB.getOrders().get(x).dep.equals("SOS") )
                                {
                                    orderDB.removeRow( Long.parseLong(orderDB.getOrders().get(x).orderNumber));
                                    List<cleanOrder> list = orderDB.getOrders() ;
                                    //TODO insert the sort function here to sort the list before pass it to adapter

                                    adapter = new CleanUp_Adapter(list,act);
                                    g.setAdapter(adapter);
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
                MiniBarCheck[i] = FireRooms.get(i).child("MiniBarCheck").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.getValue() !=null)
                        {
                            if (dataSnapshot.getValue().toString().equals("0"))
                            {
                                for (int x=0;x<orderDB.getOrders().size();x++)
                                {
                                    if (Long.parseLong(orderDB.getOrders().get(x).roomNumber) == Rooms.get(finalI).RoomNumber && orderDB.getOrders().get(x).dep.equals("MiniBarCheck") )
                                    {
                                        orderDB.removeRow( Long.parseLong(orderDB.getOrders().get(x).orderNumber));
                                        List<cleanOrder> list = orderDB.getOrders() ;
                                        //TODO insert the sort function here to sort the list before pass it to adapter

                                        adapter = new CleanUp_Adapter(list,act);
                                        g.setAdapter(adapter);
                                    }
                                }
                            }
                            else
                            {
                                boolean status = false ;
                                status = orderDB.searchOrder(Rooms.get(finalI).RoomNumber,"MiniBarCheck");
                                if (!status)
                                {
                                    long timee =  Long.parseLong(dataSnapshot.getValue().toString());
                                    orderDB.insertOrder(Rooms.get(finalI).RoomNumber,"MiniBarCheck","",timee);
                                    List<cleanOrder> list = orderDB.getOrders() ;
                                    //TODO insert the sort function here to sort the list before pass it to adapter

                                    adapter = new CleanUp_Adapter(list,act);
                                    g.setAdapter(adapter);
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            FireRooms.get(i).child("roomStatus").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null ) {
                        CurrentRoomsStatus.set(finalI,dataSnapshot.getValue().toString());
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void auth() {
        final Dialog d = new Dialog(act) ;
        d.setContentView(R.layout.loading_dialog);
        d.setCancelable(false);
        d.show();
        ApiService apiService = RetrofitAPIManager.provideClientApi();
        String userN = "";
        String passW = "";
        String account = userN.trim();
        password = passW.trim();
        password = DigitUtil.getMD5(password);
        Call<String> call = apiService.auth(ApiService.CLIENT_ID, ApiService.CLIENT_SECRET, "password", account, password, ApiService.REDIRECT_URI);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                d.dismiss();
                String json = response.body();
                accountInfo = GsonUtil.toObject(json, AccountInfo.class);
                if (accountInfo != null)
                {
                    if (accountInfo.errcode == 0)
                    {
                        accountInfo.setMd5Pwd(password);
                        acc = accountInfo;
                        lockList();
                        //Intent i = new Intent(act ,IndexActivity.class );
                        //startActivity(i);
                        Log.d("ttlockLogin" , response.message() );
                    } else
                    {
                        //ToastMaker.MakeToast(accountInfo.errmsg,act);
                        //Calendar x = Calendar.getInstance(Locale.getDefault());
                        //long time =  x.getTimeInMillis();
                        //ErrorRegister.rigestError(activ ,LogIn.room.getProjectName(),LogIn.room.getRoomNumber() , time ,004 ,accountInfo.errmsg , "LogIn To TTlock Account" );
                    }
                } else
                {
                    //ToastMaker.MakeToast(response.message() , act);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                //d.dismiss();
                //ToastMaker.MakeToast(t.getMessage() , act);
                //Calendar x = Calendar.getInstance(Locale.getDefault());
                //long time =  x.getTimeInMillis();
                //ErrorRegister.rigestError(activ , LogIn.room.getProjectName() , LogIn.room.getRoomNumber() , time ,004 ,accountInfo.errmsg , "LogIn To TTlock Account" );
            }
        });
    }

    private void lockList() {
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.loading_dialog);
        d.setCancelable(false);
        d.show();
        ApiService apiService = RetrofitAPIManager.provideClientApi();
        Call<String> call = apiService.getLockList(ApiService.CLIENT_ID,acc.getAccess_token(), 1, 100, System.currentTimeMillis());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response)
            {
                d.dismiss();
                //mListApapter = new UserLockListAdapter(act ,lockObjs );
                String json = response.body();
                if (json.contains("list"))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(json);
                        JSONArray array = jsonObject.getJSONArray("list");
                        lockObjs = GsonUtil.toObject(array.toString(), new TypeToken<ArrayList<LockObj>>(){});
                        Toast.makeText(act,lockObjs.size()+"",Toast.LENGTH_LONG);
                        Log.d("ttlockLogin" , lockObjs.size()+"");
                        for(LockObj o : lockObjs){
                            Log.d("ttlockLogin" , o.getLockName());
                            for (ROOM r :Rooms){
                                if (o.getLockAlias().equals(r.RoomNumber+"Lock")){
                                    r.setLOCK(o);
                                }
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        Calendar c = Calendar.getInstance(Locale.getDefault());
                        long time = c.getTimeInMillis();
                        //ErrorRegister.rigestError(act ,LogIn.room.getProjectName(),LogIn.room.getRoomNumber(),time,007,e.getMessage(),"error Getting Locks List");
                    }
                }
                else
                {
                    //ToastMaker.MakeToast(json,act);
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                d.dismiss();
                //ToastMaker.MakeToast(t.getMessage(),act);
                //Calendar c = Calendar.getInstance(Locale.getDefault());
                //long time = c.getTimeInMillis();
               // ErrorRegister.rigestError(act ,LogIn.room.getProjectName(),LogIn.room.getRoomNumber(),time,007,t.getMessage(),"error Getting Locks List");
            }
        });
    }

    void setTuyaApplication() {
        TuyaHomeSdk.setDebugMode(true);
        TuyaHomeSdk.init(getApplication());
        TuyaHomeSdk.setOnNeedLoginListener(new INeedLoginListener() {
            @Override
            public void onNeedLogin(Context context) {
                Intent intent = new Intent(context, LogIn.class);
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
            }
        });

    }

    public void goLogIn() {
       final  LoadingDialog d = new LoadingDialog(act);
                    TuyaHomeSdk.getUserInstance().loginWithEmail("966", "basharsebai@gmail.com", "Freesyria579251", new ILoginCallback()
                    {
                        @Override
                        public void onSuccess(com.tuya.smart.android.user.bean.User user) {
                            d.close();
                            //Toast.makeText (act, "Login succeeded, username:" + user.getUsername(), Toast.LENGTH_SHORT).show();
                            getFamilies();
                        }

                        @Override
                        public void onError (String code, String error) {
                            d.close();
                            //Toast.makeText (act, "code:" + code + "error:" + error, Toast.LENGTH_SHORT) .show();
                           // Calendar c = Calendar.getInstance(Locale.getDefault());
                            //long time = c.getTimeInMillis();
                            //ErrorRegister.rigestError( act , LogIn.room.getProjectName() , LogIn.room.getRoomNumber() , time ,9,error,"error logging in to tuya account");
                        }
                    });


    }

    void getFamilies() {
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onError(String errorCode, String error)
            {
                getFamilies();
            }
            @Override
            public void onSuccess(List<HomeBean> homeBeans)
            {
                // do something
                Homs = homeBeans ;
                for (HomeBean h :Homs){
                    if (h.getName().equals(LogIn.Project)){
                        THE_HOME = h ;
                    }
                }
                if (THE_HOME == null ){
                    Log.d("tuyaHome" ,"No Home");
                }
                else {
                    Log.d("tuyaHome" ,THE_HOME.getName());
                    TuyaHomeSdk.newHomeInstance(THE_HOME.getHomeId()).getHomeDetail(new ITuyaHomeResultCallback() {
                        @Override
                        public void onSuccess(HomeBean homeBean)
                        {
                            //loading.stop();
                            List<DeviceBean> lis = new ArrayList<DeviceBean>();
                            lis = homeBean.getDeviceList();
                            if (lis.size() == 0)
                            {
                                Log.d("tuyaHome" ,lis.size()+"");
                            }
                            else
                            {
                                for (DeviceBean d :lis){
                                    Log.d("tuyaHome" ,d.name);
                                    for(ROOM r :Rooms){
                                        if (d.getName().equals(r.RoomNumber+"Power")){
                                            r.setPOWER(d);
                                            r.setPower(TuyaHomeSdk.newDeviceInstance(r.getPOWER().devId));
                                        }
                                    }

                                }
                                Devices = lis ;
                                Log.d("tuyaHome" ,Devices.size()+"");
                                /*for (int i=0;i<lis.size();i++)
                                {
                                    if (lis.get(i).getName().equals(LogIn.room.getRoomNumber()+"Power"))
                                    {
                                        powerBean = lis.get(i);
                                        mDevice = TuyaHomeSdk.newDeviceInstance(powerBean.devId);
                                        THEROOM.setPOWER_B(powerBean);
                                        THEROOM.setPOWER(TuyaHomeSdk.newDeviceInstance(powerBean.devId));
                                        POWER.setText("YES");
                                        POWER.setTextColor(Color.GREEN);
                                    }
                                    else if (lis.get(i).getName().equals(LogIn.room.getRoomNumber()+"ZGatway"))
                                    {
                                        zgatwayBean = lis.get(i);
                                        mgate = TuyaHomeSdk.newGatewayInstance(Tuya_Devices.zgatwayBean.devId);
                                        GATEWAY.setText("YES");
                                        GATEWAY.setTextColor(Color.GREEN);
                                        THEROOM.setWiredZBGateway(mgate);
                                        mgate.getSubDevList(new ITuyaDataCallback<List<DeviceBean>>() {
                                            @Override
                                            public void onSuccess(List<DeviceBean> result)
                                            {
                                                for (int i=0;i<result.size();i++)
                                                {
                                                    if (result.get(i).getName().equals(LogIn.room.getRoomNumber()+"DoorSensor"))
                                                    {

                                                        THEROOM.setDOORSENSOR_B(result.get(i));
                                                        THEROOM.setDOORSENSOR(TuyaHomeSdk.newDeviceInstance(THEROOM.getDOORSENSOR_B().devId));
                                                        DOORSENSOR.setText("YES");
                                                        DOORSENSOR.setTextColor(Color.GREEN);
                                                    }
                                                    else if (result.get(i).getName().equals(LogIn.room.getRoomNumber()+"MotionSensor"))
                                                    {
                                                        THEROOM.setMOTIONSENSOR_B(result.get(i));
                                                        THEROOM.setMOTIONSENSOR(TuyaHomeSdk.newDeviceInstance(THEROOM.getMOTIONSENSOR_B().getDevId()));
                                                        MOTIONSENSOR.setText("YES");
                                                        MOTIONSENSOR.setTextColor(Color.GREEN);
                                                    }
                                                    else if (result.get(i).getName().equals(LogIn.room.getRoomNumber()+"Curtain"))
                                                    {
                                                        THEROOM.setCURTAIN_B(result.get(i));
                                                        THEROOM.setCURTAIN(TuyaHomeSdk.newDeviceInstance(THEROOM.getCURTAIN_B().getDevId()));
                                                        CURTAIN.setText("YES");
                                                        CURTAIN.setTextColor(Color.GREEN);
                                                    }
                                                    else if (result.get(i).getName().equals(LogIn.room.getRoomNumber()+"ServiceSwitch"))
                                                    {
                                                        THEROOM.setSERVICE_B(result.get(i));
                                                        THEROOM.setSERVICE(TuyaHomeSdk.newDeviceInstance(THEROOM.getSERVICE_B().getDevId()));
                                                        SERVICE.setText("YES");
                                                        SERVICE.setTextColor(Color.GREEN);
                                                    }
                                                    else if (result.get(i).getName().equals(LogIn.room.getRoomNumber()+"Switch1"))
                                                    {
                                                        THEROOM.setSWITCH1_B(result.get(i));
                                                        THEROOM.setSWITCH1(TuyaHomeSdk.newDeviceInstance(THEROOM.getSWITCH1_B().getDevId()));
                                                        SWITCH1.setText("YES");
                                                        SWITCH1.setTextColor(Color.GREEN);
                                                    }
                                                    else if (result.get(i).getName().equals(LogIn.room.getRoomNumber()+"Switch2"))
                                                    {
                                                        THEROOM.setSWITCH2_B(result.get(i));
                                                        THEROOM.setSWITCH2(TuyaHomeSdk.newDeviceInstance(THEROOM.getSWITCH2_B().getDevId()));
                                                        SWITCH2.setText("YES");
                                                        SWITCH2.setTextColor(Color.GREEN);
                                                    }
                                                    else if (result.get(i).getName().equals(LogIn.room.getRoomNumber()+"Switch3"))
                                                    {

                                                        THEROOM.setSWITCH3_B(result.get(i));
                                                        THEROOM.setSWITCH3(TuyaHomeSdk.newDeviceInstance(THEROOM.getSWITCH3_B().getDevId()));
                                                        SWITCH3.setText("YES");
                                                        SWITCH3.setTextColor(Color.GREEN);
                                                    }
                                                    else if (result.get(i).getName().equals(LogIn.room.getRoomNumber()+"Switch4"))
                                                    {

                                                        THEROOM.setSWITCH4_B(result.get(i));
                                                        THEROOM.setSWITCH4(TuyaHomeSdk.newDeviceInstance(THEROOM.getSWITCH4_B().getDevId()));
                                                        SWITCH4.setText("YES");
                                                        SWITCH4.setTextColor(Color.GREEN);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onError(String errorCode, String errorMessage) {

                                            }
                                        });
                                    }
                                    else if(lis.get(i).getName().equals(LogIn.room.getRoomNumber()+"AC"))
                                    {
                                        ACbean = lis.get(i);
                                        AC = TuyaHomeSdk.newDeviceInstance(Tuya_Devices.ACbean.devId);
                                        THEROOM.setAC_B(ACbean);
                                        THEROOM.setAC(TuyaHomeSdk.newDeviceInstance(Tuya_Devices.ACbean.devId));
                                        ACTEXT.setText("YES");
                                        ACTEXT.setTextColor(Color.GREEN);
                                    }
                                }*/
                                /*
                                if (powerBean != null && mDevice != null)
                                {
                                    Intent i = new Intent(act , FullscreenActivity.class);
                                    startActivity(i);
                                }
                                else
                                {
                                    //ToastMaker.MakeToast(String.valueOf(lis.size()),act);
                                    Device_List_Adapter adapter = new Device_List_Adapter(lis);
                                    devices.setLayoutManager(ll);
                                    devices.setAdapter(adapter);
                                }*/

                            }
                        }

                        @Override
                        public void onError(String errorCode, String errorMsg)
                        {
                            getFamilies();
                        }
                    });
                }
            }
        });
    }

    void changePasswordDialog () {
        Dialog D = new Dialog(act) ;
        D.setContentView(R.layout.change_password_dialog);
        Window window = D.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        EditText oldPassword = (EditText) D.findViewById(R.id.changePassword_oldPassword);
        EditText newPassword = (EditText) D.findViewById(R.id.changePassword_NewPassword);
        EditText Conferm = (EditText) D.findViewById(R.id.changePassword_ConNewPassword);
        Button cancel = (Button) D.findViewById(R.id.changePassword_cancel) ;
        Button send = (Button) D.findViewById(R.id.changePassword_send) ;
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                D.dismiss();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldPassword.getText() == null || oldPassword.getText().toString().isEmpty()) {
                    Toast.makeText(act,"enter old password" ,Toast.LENGTH_SHORT).show();
                    oldPassword.setHint("old password");
                    oldPassword.setHintTextColor(Color.RED);
                    return;
                }
                if (newPassword.getText() == null || newPassword.getText().toString().isEmpty()) {
                    Toast.makeText(act,"enter new password" ,Toast.LENGTH_SHORT).show();
                    newPassword.setHint("new password");
                    newPassword.setHintTextColor(Color.RED);
                    return;
                }
                if (Conferm.getText() == null || Conferm.getText().toString().isEmpty()) {
                    Toast.makeText(act,"enter password confirmation" ,Toast.LENGTH_SHORT).show();
                    Conferm.setHint("password confirm");
                    Conferm.setHintTextColor(Color.RED);
                    return;
                }
                LoadingDialog l = new LoadingDialog(act);
                StringRequest request = new StringRequest(Request.Method.POST, changePasswordUrl , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("passwordResp" , response);
                        l.close();
                        if (response.equals("1")) {
                            Toast.makeText(act,"Password Changed" ,Toast.LENGTH_LONG).show();
                            D.dismiss();
                        }
                        else if (response.equals("-1")) {
                            Toast.makeText(act,"Old Password Is Wrong" ,Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("passwordResp" , error.toString());
                        l.close();
                        Toast.makeText(act,"error.. try again later" ,Toast.LENGTH_LONG).show();
                    }
                })
                {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> par = new HashMap<String, String>();
                        par.put("oldPassword" , oldPassword.getText().toString());
                        par.put("newPassword" , newPassword.getText().toString());
                        par.put("jobNumber" , String.valueOf(LogIn.db.getUser().jobNumber));
                        return par;
                    }
                };
                Volley.newRequestQueue(act).add(request);
            }
        });
        D.show();
    }

    public List<cleanOrder> sortOrdersListByTime(List<cleanOrder> list) {
        //TODO make the sort function here

        return null;
    }
}
