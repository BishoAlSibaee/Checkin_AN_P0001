package com.syriasoft.cleanup;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class messagingService extends FirebaseMessagingService {

    private NotificationManager notificationManager ;

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        super.onCreate();
    }

    public void onMessageReceived(RemoteMessage remoteMessage)
    {
//        Random r = new Random();
//        int reqCode = r.nextInt();
//        final Alarm alarm = new Alarm() ;
//        //Intent q = new Intent(this , MainActivity.class );
//        //showNotification(this , remoteMessage.getData().get("title") , remoteMessage.getData().get("message") ,q,reqCode);
//
//        if (remoteMessage.getData().get("title").equals("Restaurant"))
//        {
//            if (remoteMessage.getData().get("message").equals("Order Done"))
//            {
//                Intent i = new Intent(this , RestaurantOrders.class );
//                showNotification(this , remoteMessage.getData().get("title") ,"Room "+remoteMessage.getData().get("room")+" "+ remoteMessage.getData().get("message") ,i,reqCode);
//
//                //RestaurantOrders.getRestaurantOrders();
//            }
//            else
//                {
//                    Intent i = new Intent(this , RestaurantOrders.class );
//                    showNotification(this , remoteMessage.getData().get("title") ,"New Order From " + remoteMessage.getData().get("room") ,i,reqCode);
//                    //RestaurantOrders.getRestaurantOrders();
//                }
//
//        }
//        else if (MainActivity.activityStatus)
//            {
//            if (remoteMessage.getData().get("title").equals("New Order"))
//            {
//                if (remoteMessage.getData().get("dep").equals("SOS"))
//                {
//                    MainActivity.getOrders();
//                    if (remoteMessage.getData().get("orderAction").equals("true"))
//                    {
//
//                         //Toast.makeText(getBaseContext(), remoteMessage.getData().get("dep"), Toast.LENGTH_LONG).show();
//                        Intent i = new Intent(this, SOSService.class);
//                        Intent j = new Intent(this, MainActivity.class);
//                        showNotification(getApplicationContext(),remoteMessage.getData().get("dep"), remoteMessage.getData().get("message"), j, reqCode);
//                        startService(i);
//                        //sosActivity();
//                    }
//                    else {
//                        Intent j = new Intent(this, MainActivity.class);
//                        showNotification(getApplicationContext(), "Order Cancelled", "Order From Room " + remoteMessage.getData().get("RoomNumber") + " Cancelled", j, reqCode);
//                        Intent i = new Intent(this, SOSService.class);
//                        if (SOSService.mediaPlayer != null) {
//                            if (SOSService.mediaPlayer.isPlaying()) {
//                                SOSService.mediaPlayer.stop();
//                            }
//                            stopService(i);
//                        }
//                    }
//                }
//                else
//                {
//                        MainActivity.getOrders();
//                        if (remoteMessage.getData().get("orderAction").equals("true")) {
//                            Intent i = new Intent(this, MainActivity.class);
//                            showNotification(getApplicationContext(), remoteMessage.getData().get("title"), remoteMessage.getData().get("message"), i, reqCode);
//                        } else {
//                            Intent i = new Intent(this, MainActivity.class);
//                            showNotification(getApplicationContext(), "Order Cancelled", "Order From Room " + remoteMessage.getData().get("RoomNumber") + " Cancelled", i, reqCode);
//                        }
//                }
//            }
//            else if (remoteMessage.getData().get("title").equals("labor"))
//                {
//                    MainActivity.getOrders();
//                    Intent i = new Intent(this , MainActivity.class);
//                    showNotification(getApplicationContext(),"Order Done","Order From Room "+remoteMessage.getData().get("room") + " Done By " + remoteMessage.getData().get("emp") ,i,reqCode);
//                    if (remoteMessage.getData().get("dep").equals("SOS")) {
//                        Intent j = new Intent(this, SOSService.class);
//                        if (SOSService.mediaPlayer != null) {
//                            if (SOSService.mediaPlayer.isPlaying()) {
//                                SOSService.mediaPlayer.stop();
//                            }
//                            stopService(j);
//                        }
//                    }
//                }
//        }
//        else
//            {
//                if (remoteMessage.getData().get("title").equals("New Order")) {
//                    if (remoteMessage.getData().get("dep").equals("SOS"))
//                    {
//                        if (remoteMessage.getData().get("orderAction").equals("true")) {
//                            // Toast.makeText(this, "hi ", Toast.LENGTH_LONG).show();
//                            Intent i = new Intent(this, SOSService.class);
//                            Intent j = new Intent(this, MainActivity.class);
//                            showNotification(getApplicationContext(),remoteMessage.getData().get("dep"), remoteMessage.getData().get("message"), j, reqCode);
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                startForegroundService(i);
//                            }
//                            else
//                            {
//                                startService(i);
//                            }
//                            sosActivity();
//                        }
//                        else {
//                            Intent i = new Intent(this, SOSService.class);
//                            if (SOSService.mediaPlayer != null) {
//                                if (SOSService.mediaPlayer.isPlaying()) {
//                                    SOSService.mediaPlayer.stop();
//                                }
//                            }
//                                stopService(i);
//
//                        }
//                    } else
//                   {
//                        MainActivity.getOrders();
//                    if (remoteMessage.getData().get("orderAction").equals("true")) {
//                        //MainActivity.getOrders();
//                        Intent i = new Intent(this, MainActivity.class);
//                        showNotification(getApplicationContext(), remoteMessage.getData().get("title"), remoteMessage.getData().get("message"), i, reqCode);
//                    } else {
//                        //MainActivity.getOrders();
//                        Intent i = new Intent(this, MainActivity.class);
//                        showNotification(getApplicationContext(), "Order Cancelled", "Order From Room " + remoteMessage.getData().get("RoomNumber") + " Cancelled", i, reqCode);
//                    }
//                }
//                }
//                else if (remoteMessage.getData().get("title").equals("labor"))
//                {
//                    Intent i = new Intent(this , MainActivity.class);
//                    showNotification(getApplicationContext(),"Order Done","Order From Room "+remoteMessage.getData().get("room") + " Done By " + remoteMessage.getData().get("emp") ,i,reqCode);
//                    if (remoteMessage.getData().get("dep").equals("SOS")) {
//                        Intent j = new Intent(this, SOSService.class);
//                        if (SOSService.mediaPlayer != null) {
//                            if (SOSService.mediaPlayer.isPlaying()) {
//                                SOSService.mediaPlayer.stop();
//                            }
//                        }
//                        stopService(j);
//                    }
//                }
//
//            }        // ...
        Random r = new Random();
        int reqCode = r.nextInt();
        Log.d("IncomingMessage" ,remoteMessage.getData().get("title")+remoteMessage.getData().get("message") );
        if (remoteMessage.getData().get("title") != null && remoteMessage.getData().get("message")!= null ) {
            if (remoteMessage.getData().get("title").equals("SOS")) {
                Intent i = new Intent(getApplicationContext(),SOSService.class);
                startService(i);
                Intent ii = new Intent(getApplicationContext(),MainActivity.class);
                showNotification(getApplicationContext(),remoteMessage.getData().get("title"),remoteMessage.getData().get("message"),ii,reqCode);
            }
            else {
                Intent i = new Intent(getApplicationContext(),LogIn.class);
                showNotification(getApplicationContext(),remoteMessage.getData().get("title"),remoteMessage.getData().get("message"),i,reqCode);
            }

        }
    }

    @Override
    public void onNewToken(String token) {
        //Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        if (LogIn.db.isLogedIn()) {
            sendRegistrationToServer(token);
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://hotelservices-ebe66.firebaseio.com/");
            MainActivity.MyFireUser.child("token").setValue(token);
        }
    }

    void sendRegistrationToServer(final String token)
    {
        String url = LogIn.URL+"registCleanupUserToken.php";
        StringRequest r = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
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
                params.put("jnum",String.valueOf(LogIn.db.getUser().jobNumber));
                return params;
            }
        };

        Volley.newRequestQueue(this ).add(r);

    }

    public void showNotification(Context context, String title, String message, Intent intent, int reqCode)
    {

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent p = stackBuilder.getPendingIntent(reqCode,PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent i = new Intent(this , MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        String CHANNEL_ID = "channel_name";// The id of the channel.
        Uri soundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.notification_sound);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(p)
                .setColor(Color.parseColor("#0E223B"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setSound(soundUri,null);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build()); // 0 is the request code, it should be unique id
    }


    public  void sosActivity()
    {
        Intent dialogIntent = new Intent(this, MainActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);

    }

    void SOSnotification()
    {
        Looper.prepare();
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("Are you sure?")
                .setView(R.layout.message_dialog)
                .create();
        alertDialog.getWindow().setType(params.flags);
        alertDialog.create();




        //Log.d("showNotification", "showNotification: " + reqCode);

    }


}
