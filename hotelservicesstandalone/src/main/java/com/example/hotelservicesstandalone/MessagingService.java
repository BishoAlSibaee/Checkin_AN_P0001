package com.example.hotelservicesstandalone;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ttlock.bl.sdk.api.TTLockClient;
import com.ttlock.bl.sdk.callback.ControlLockCallback;
import com.ttlock.bl.sdk.constant.ControlAction;
import com.ttlock.bl.sdk.entity.ControlLockResult;
import com.ttlock.bl.sdk.entity.LockError;

import java.util.HashMap;
import java.util.Map;

public class MessagingService extends FirebaseMessagingService {

    private FirebaseDatabase database ;



    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        Log.d("MessageRecieved" , "message recieved");
//        if (remoteMessage.getData() != null ) {
//            String title =  remoteMessage.getData().get("title");
//            if (title.equals("poweroff"))
//            {
//                //Log.d("MessageRecieved" , title+" "+remoteMessage.getData().get("room"));
//                String room = remoteMessage.getData().get("room") ;
//                for (int i=0;i<Rooms.list.size();i++) {
//                    if (Rooms.list.get(i).RoomNumber == Integer.parseInt(room)) {
//                        Rooms.powerOffRoom(Rooms.list.get(i));
//                    }
//                }
//            }
//            else if (title.equals("checkin"))
//            {
//                String room = remoteMessage.getData().get("room") ;
//
//                for (int i=0;i<Rooms.list.size();i++) {
//                    if (room.equals(String.valueOf(Rooms.list.get(i).RoomNumber))) {
//                        Rooms.checkInModeRoom(Rooms.list.get(i));
//                    }
//                }
//            }
//            else if(title.equals("opendoor"))
//            {
//                //Log.d("MessageRecieved" , title+" "+remoteMessage.getData().get("room"));
//                String room = remoteMessage.getData().get("room") ;
//                for (int i=0;i<Rooms.list.size();i++) {
//                    if (Rooms.list.get(i).RoomNumber == Integer.parseInt(room)) {
//                        Rooms.OpenTheDoor(Rooms.list.get(i));
//                    }
//                }
//            }
//            else if (title.equals("poweron"))
//            {
//                String room = remoteMessage.getData().get("room") ;
//                for (int i=0;i<Rooms.list.size();i++) {
//                    if (Rooms.list.get(i).RoomNumber == Integer.parseInt(room)) {
//                        Rooms.powerOnRoom(Rooms.list.get(i));
//                        //Log.d("MessageRecieved" , "equal");
//                    }
//                }
//            }
//            else if (title.equals("bycard"))
//            {
//                String room = remoteMessage.getData().get("room") ;
//                for (int i=0;i<Rooms.list.size();i++) {
//                    if (Rooms.list.get(i).RoomNumber == Integer.parseInt(room)) {
//                        Rooms.powerByCard(Rooms.list.get(i));
//                    }
//                }
//            }
//        }
    }

    @Override
    public void onNewToken(String token) {
        //Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        database = FirebaseDatabase.getInstance("https://hotelservices-ebe66.firebaseio.com/");
        DatabaseReference ServerDevice = database.getReference(Login.SelectedHotel.ProjectName+"ServerDevice");
        //ServerDevice.child("status").setValue("1");
        //ServerDevice.child("token").setValue(token);
        //sendRegistrationToServer(token);
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
                Log.d("tokenRegister" , error.getMessage()) ;
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
        Volley.newRequestQueue(getApplicationContext()).add(re);
    }
}
