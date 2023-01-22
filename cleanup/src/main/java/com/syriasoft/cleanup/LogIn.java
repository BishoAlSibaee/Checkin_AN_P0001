package com.syriasoft.cleanup;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.BuildConfig;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LogIn extends AppCompatActivity {


    public static String Project = "P0001";
    public static String URL = "https://ratco-solutions.com/Checkin/"+Project+"/php/";
    private int SelectedHotel = 1 ;
    private String jobNumber ;
    private String password ;
    private Activity act = this ;
    private String loginUrl ;
    public static UserDB db ;
    public static List<Activity> actList = new ArrayList<Activity>();
    private List<RESTAURANT_UNIT> Restaurants = new ArrayList<RESTAURANT_UNIT>();
    private Spinner facilities , deps;
    private String[] RESTAURANTS ;
    private RESTAURANT_UNIT THERESTAURANT ;
    private TextInputLayout pass , job ;
    LinearLayout loginLayout ;
    TextView versionTV;
    int Version ;
    int NewVersion ;
    String NewVersionUrl;
    int REQUEST_CODE = 10 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        actList.add(act);
        loginLayout = (LinearLayout) findViewById(R.id.layout);
        loginLayout.setVisibility(View.GONE);
        job = (TextInputLayout) findViewById(R.id.Login_jobNumber);
        pass = (TextInputLayout) findViewById(R.id.Login_password);
        facilities = (Spinner) findViewById(R.id.facility_spinner);
        versionTV = (TextView) findViewById(R.id.textView10);
        Version = BuildConfig.VERSION_CODE ;
        versionTV.setText( "Version "+Version);
        LinearLayout logo = (LinearLayout)findViewById(R.id.logo_layout);
        logo.setVisibility(View.VISIBLE);
        facilities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                THERESTAURANT = Restaurants.get(position);
                Log.d("selectedfacility" , THERESTAURANT.Name+" " +THERESTAURANT.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (!isNetworkConnected())
        {
            Toast.makeText(act, "لا يوجد اتصال بالانترنت .. تاكد من الاتصال بالانترنت", Toast.LENGTH_LONG).show();
            this.finish();
        }
        else
        {
                db = new UserDB(this);
                //db.logout();
                deps = findViewById(R.id.Login_department);
                final String[] items = new String[]{"Laundry", "Cleanup", "Restaurant", "RoomService", "Gym", "Service"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
                deps.setAdapter(adapter);
                deps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                    {
                        if (items[position].equals("Restaurant"))
                        {
                            getRestaurants();
                            job.setHint("Enter User ");
                        }
                        else
                        {
                            facilities.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                Runnable r = new Runnable() {
                @Override
                public void run() {
                    Log.d("threadStarted","started");
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (db.isLogedIn())
                    {
                        Log.d("threadStarted","loged in");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginLayout.setVisibility(View.GONE);
                            }
                        });
                        if (db.getUser().department.equals("Restaurant"))
                        {
                            Intent i = new Intent(act, RestaurantOrders.class);
                            i.putExtra("id" , db.getFacility());
                            startActivity(i);
                            act.finish();
                        }
                        else
                        {
                            Intent i = new Intent(act, MainActivity.class);
                            startActivity(i);
                            act.finish();
                        }
                    }
                    else
                    {
                        Log.d("threadStarted","un loged in");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LinearLayout logo = findViewById(R.id.logo_layout);
                                loginLayout.setVisibility(View.VISIBLE);
                                logo.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
                };
                Thread t = new Thread(r);
                t.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){

        }
    }

    public void logInBtn(View view) {
        if (deps.getSelectedItem().toString().equals("Restaurant")) {
            loginUrl = URL+"logInFacilityUser.php";
        }
        else {
            loginUrl = URL+"logInEmployees.php";
        }
        final Spinner deps = findViewById(R.id.Login_department);
        jobNumber = job.getEditText().getText().toString();
        password = pass.getEditText().getText().toString();
        if (jobNumber.length() > 0 )
        {
            if (password.length() >0)
            {
                final LoadingDialog d = new LoadingDialog(act);
                StringRequest re = new StringRequest(Request.Method.POST, loginUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d("loginResp" , response);
                        if (response.equals("0"))
                        {
                            d.close();
                            new messageDialog("wrong job number or password" , "wrong entry",act);
                        }
                        else
                        {
                                d.close();
                                try {
                                    JSONObject user = new JSONObject(response);
                                    int id=0 ;
                                    String name="";
                                    int mobile=0 ;
                                    String department="" ;
                                    String token ="";
                                    int jobNumbe =0  ;
                                    if (deps.getSelectedItem().toString().equals("Restaurant"))
                                    {
                                            id = user.getInt("id");
                                            name = user.getString("Name");
                                            mobile = user.getInt("Mobile");
                                            department = "Restaurant";
                                            token = user.getString("token");
                                            jobNumbe = 0;
                                    }
                                    else
                                    {
                                         id = user.getInt("id");
                                         name = user.getString("name");
                                         mobile = user.getInt("mobile");
                                         department = user.getString("department");
                                         token = user.getString("token");
                                         jobNumbe = user.getInt("jobNumber");
                                    }
                                    int facility = 0 ;
                                    if (deps.getSelectedItem().toString().equals("Restaurant"))
                                    {
                                        facility = THERESTAURANT.id ;
                                    }
                                    if (db.insertUser(id,name,mobile,token,department,jobNumbe,facility,Version))
                                    {
                                        if (db.getUser().department.equals("Restaurant"))
                                        {
                                            Log.d("facilityid" , LogIn.db.getFacility()+"" );
                                            Intent i = new Intent(act,RestaurantOrders.class);
                                            i.putExtra("id",THERESTAURANT.id);
                                            i.putExtra("hotel" , THERESTAURANT.Hotel);
                                            i.putExtra("name" , THERESTAURANT.Name);
                                            i.putExtra("photo" , THERESTAURANT.photo);
                                            i.putExtra("control" , THERESTAURANT.Control);
                                            i.putExtra("type" , THERESTAURANT.TypeName);
                                            i.putExtra("typeId" , THERESTAURANT.TypeId);
                                            startActivity(i);
                                            act.finish();
                                        }
                                        else
                                        {
                                            Intent i = new Intent(act,MainActivity.class);
                                            startActivity(i);
                                            act.finish();
                                        }
                                    }
                                    else
                                    {
                                        Toast.makeText(act , "fdskfbjskhdf" , Toast.LENGTH_LONG).show();
                                    }


                                    } catch (JSONException e)
                                {
                                    e.printStackTrace();
                                    Toast.makeText(act , e.getMessage() , Toast.LENGTH_LONG).show();
                                }

                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        d.close();
                        Toast.makeText(act , error.getMessage() , Toast.LENGTH_LONG).show();
                        Log.e("loginError" , error.getMessage());
                    }
                })
                {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError
                    {
                        Map<String,String> params = new HashMap<String,String>();
                        if (deps.getSelectedItem().toString().equals("Restaurant"))
                        {
                            params.put("user" , jobNumber);
                            params.put("password",password);
                            params.put("facility",String.valueOf( THERESTAURANT.id ));
                            //Log.d("loginproblem" , jobNumber + password +THERESTAURANT.id +" " + loginUrl+ params.toString());
                        }
                        else
                        {
                            params.put("jobNumber" , jobNumber);
                            params.put("password",password);
                            params.put("department", deps.getSelectedItem().toString());
                            //Log.d("loginproblem" , jobNumber + password +THERESTAURANT.id +" " + loginUrl+ params.toString());
                        }

                        return params ;
                    }
                };
                Volley.newRequestQueue(act).add(re);
            }
            else
                {
                   new messageDialog("enter password" , "password",act);
                }
        }
        else
            {
                new messageDialog("enter jobnumber" , "job number",act);
            }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void getRestaurants() {
            final LoadingDialog d = new LoadingDialog(act);
            String url = URL+"getRestaurantsOrCoffeeShops.php";
            StringRequest laundryRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    Log.d("RestaurantsResponse" , response);
                    d.close();
                    if (response.equals("0"))
                    {

                    }
                    else
                    {
                        try
                        {
                            JSONArray arr = new JSONArray(response);
                            for (int i=0 ; i<arr.length();i++)
                            {
                                JSONObject row =arr.getJSONObject(i);
                                Restaurants.add(new RESTAURANT_UNIT(row.getInt("id"),row.getInt("Hotel"),row.getInt("TypeId"),row.getString("TypeName"),row.getString("Name"),row.getInt("Control"),row.getString("photo")));
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                        if (Restaurants.size() > 0)
                        {
                            RESTAURANTS = new String[Restaurants.size()];
                            for (int i=0;i<Restaurants.size();i++)
                            {
                                RESTAURANTS[i] = Restaurants.get(i).Name ;
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(act,R.layout.spinner_item,RESTAURANTS);
                            facilities.setAdapter(adapter);
                            facilities.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            Toast.makeText(act,"No Restaurants In Your Hotel" , Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    d.close();
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String,String> par = new HashMap<String, String>();
                    par.put("Hotel" , String.valueOf(SelectedHotel));
                    return par;
                }
            };

            Volley.newRequestQueue(act).add(laundryRequest);
    }

    void getLastVersion(VolleyCallback callback) {
        StringRequest req = new StringRequest(Request.Method.POST, URL+"getVersions.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("versionIs",response);
                if (response != null) {
                    try {
                        JSONArray arr = new JSONArray(response);
                        JSONObject row = arr.getJSONObject(arr.length()-1);
                        NewVersion = row.getInt("Version");
                        NewVersionUrl = row.getString("Link");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("versionIs",response+" "+NewVersion+" "+Version);
                    if (NewVersion != 0) {
                        if (NewVersion > Version) {
                            callback.onSuccess("update");
                        }
                        else {
                            callback.onSuccess("noUpdates");
                        }
                    }
                    else {
                        callback.onSuccess("noUpdates");
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailed(error.toString());
            }
        });
        Volley.newRequestQueue(act).add(req);
    }

    private void updateApp(Thread t) {
        if (NewVersionUrl != null) {
            AlertDialog.Builder B = new AlertDialog.Builder(act);
            B.setTitle("New Update Available")
                    .setMessage("New application update available .." +
                            "do you want to update")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("threadStarted","no pressed");
                            dialog.dismiss();
                            t.start();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("threadStarted","yes pressed");
                            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                Log.e("Permission error","You have permission");
                                act.requestPermissions( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                                return;
                            }
                            String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"; //Objects.requireNonNull(act.getExternalFilesDir(null)).getAbsolutePath();  //
                            String fileName = "ServiceAppV"+NewVersion+".apk";
                            destination += fileName;
                            final Uri uri = Uri.parse("file://" + destination);
                            File file = new File(destination);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(NewVersionUrl));
                            request.setDescription("Download Updates");
                            request.setTitle("Service App");
                            //set destination
                            request.setDestinationUri(uri);
                            // get download service and enqueue file
                            final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                            final long downloadId = manager.enqueue(request);
                            //set BroadcastReceiver to install app when .apk is downloaded
                            BroadcastReceiver onComplete = new BroadcastReceiver() {
                                public void onReceive(Context ctxt, Intent intent) {
                                    installApk();
                                    unregisterReceiver(this);
                                    finish();
                                }
                            };
                            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                        }
                    })
                    .create()
                    .show();
        }

    }

    private void installApk() {

            String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"; //Objects.requireNonNull(act.getExternalFilesDir(null)).getAbsolutePath();
            File file = new File(PATH + "ServiceAppV"+NewVersion+".apk");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= 24) {
                Uri downloaded_apk = FileProvider.getUriForFile(act, act.getApplicationContext().getPackageName() + ".provider", file);
                intent.setDataAndType(downloaded_apk, "application/vnd.android.package-archive");
                List<ResolveInfo> resInfoList = act.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    act.grantUriPermission(act.getApplicationContext().getPackageName() + ".provider", downloaded_apk, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else {
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);

    }
}


interface VolleyCallback {
    void onSuccess(String res);
    void onFailed(String error);
}