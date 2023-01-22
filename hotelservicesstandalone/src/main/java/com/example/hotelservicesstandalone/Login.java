package com.example.hotelservicesstandalone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hotelservicesstandalone.TUYA.Tuya_Login;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.sdk.api.INeedLoginListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Login extends AppCompatActivity
{
    private Spinner Hotels , homes;
    private String[] list ;
    private List<HOTEL> HotelsList ;
    private int [] ids ;
    private Activity act = this ;
    static HOTEL SelectedHotel = new HOTEL(1,"P0001","Riyadh","https://ratco-solutions.com/Checkin/P0001/php/");
    private String getProjectsUrl = SelectedHotel.URL+"getProjects.php";
    private String LogInUrl = SelectedHotel.URL+"logInToHotel.php";
    private EditText password ;
    static HotelDB THEHOTELDB ;
    private List<HomeBean> Homs;
    public static HomeBean THEHOME ;
    private String COUNTRY_CODE = "966" , EMAIL ="basharsebai@gmail.com" , PASS="Freesyria579251" ;
    private TextView ProjectName ;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ProjectName = (TextView) findViewById(R.id.Project_Name);
        ProjectName.setText(SelectedHotel.ProjectName);
        THEHOTELDB = new HotelDB(act);
        //THEHOTELDB.Logout();
        if (Login.THEHOTELDB.isLoggedIn())
        {
            Log.d("loginorocess" , "logged");
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setTuyaApplication();
                                LoggedInFunction();
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            Thread t = new Thread(r);
            t.start();

        }
        else
        {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setTuyaApplication();
                                LinearLayout loginLayout = (LinearLayout) findViewById(R.id.login_layout);
                                LinearLayout loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
                                loadingLayout.setVisibility(View.GONE);
                                loginLayout.setVisibility(View.VISIBLE);
                                Hotels = (Spinner)findViewById(R.id.spinner);
                                homes = (Spinner)findViewById(R.id.spinner2);
                                password = (EditText) findViewById(R.id.editTextTextPersonName);
                                HotelsList = new ArrayList<HOTEL>();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            Log.d("loginorocess" , "not logged");
            Thread t = new Thread(r);
            t.start();
            //getHotels();
            /*Hotels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    SelectedHotel = HotelsList.get(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {

                }
            });*/

        }

    }

    private void getHotels()
    {
        final lodingDialog loading = new lodingDialog(act);
        StringRequest re = new StringRequest(Request.Method.POST, getProjectsUrl , new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                loading.stop();
                if (response != null )
                {
                    try
                    {
                        JSONArray arr = new JSONArray(response);
                        list = new String [arr.length()];
                        ids = new int [arr.length()];
                        for(int i=0;i<arr.length();i++)
                        {
                            JSONObject row = arr.getJSONObject(i);
                            list[i] = row.getString("projectName");
                            ids[i] = row.getInt("id");
                            //HotelsList.add(new HOTEL(row.getInt("id"),row.getString("projectName"),row.getString("city")));
                            setTuyaApplication();
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(act,R.layout.spinners_item,list);
                    Hotels.setAdapter(adapter);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                loading.stop();
            }
        });
        Volley.newRequestQueue(act).add(re);
    }

    public void LogIn(View view)
    {
        if (SelectedHotel != null )
        {
            final lodingDialog loading = new lodingDialog(act);
            final String pass = password.getText().toString() ;
            StringRequest re = new StringRequest(Request.Method.POST, LogInUrl, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    loading.stop();
                    if (response.equals("1"))
                    {
                        Toast.makeText(act,"Login Success",Toast.LENGTH_LONG).show();
                        THEHOTELDB.insertHotel(SelectedHotel.id,SelectedHotel.ProjectName,SelectedHotel.city);
                        onlyLogInToTuya();
                    }
                    else if (response.equals("0"))
                    {
                        Toast.makeText(act,"Login Failed",Toast.LENGTH_LONG).show();
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
                    par.put( "hotel" , String.valueOf( SelectedHotel.id) ) ;
                    return par;
                }
            };
            Volley.newRequestQueue(act).add(re);
        }
    }

    private void onlyLogInToTuya()
    {
        TuyaHomeSdk.getUserInstance().loginWithEmail(COUNTRY_CODE,EMAIL ,PASS , new ILoginCallback()
        {
            @Override
            public void onSuccess (User user)
            {
                //Toast.makeText (act, "Login succeeded, username:" + user.getUsername(), Toast.LENGTH_SHORT).show();
                TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
                    @Override
                    public void onError(String errorCode, String error)
                    {
                    }
                    @Override
                    public void onSuccess(List<HomeBean> homeBeans)
                    {
                        // do something
                        Homs = homeBeans ;
                        LinearLayout l = (LinearLayout) findViewById(R.id.homes_layout);
                        l.setVisibility(View.VISIBLE);
                        String [] hh = new String[Homs.size()];
                        for(int i=0;i<Homs.size();i++)
                        {
                            hh[i] = Homs.get(i).getName() ;
                        }
                        if (hh.length>0)
                        {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(act,R.layout.spinners_item,hh);
                            homes.setAdapter(adapter);
                            homes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                                {
                                    THEHOME = Homs.get(position) ;
                                    THEHOTELDB.insertTuyaProject(Homs.get(position).getName());
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> parent)
                                {

                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onError (String code, String error)
            {

            }
        });
    }

    void setTuyaApplication()
    {
        TuyaHomeSdk.setDebugMode(true);
        try {
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
        catch (Exception e ) {
            Log.d("TuyaError" , e.getMessage());
        }


    }

    void LoggedInFunction()
    {
        TuyaHomeSdk.getUserInstance().loginWithEmail(COUNTRY_CODE,EMAIL ,PASS , new ILoginCallback()
        {
            @Override
            public void onSuccess (User user)
            {

                //Toast.makeText (act, "Login succeeded, username:" + user.getUsername(), Toast.LENGTH_SHORT).show();
                TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
                    @Override
                    public void onError(String errorCode, String error)
                    {
                    }
                    @Override
                    public void onSuccess(List<HomeBean> homeBeans)
                    {
                        // do something
                        Homs = homeBeans ;
                        for(int i=0;i<Homs.size();i++)
                        {
                            if (THEHOTELDB.getTuyaProject().equals(Homs.get(i).getName()))
                            {
                                THEHOME = Homs.get(i) ;
                            }
                        }
                        if (THEHOME != null )
                        {
                            Intent i = new Intent(act , Rooms.class);
                            act.startActivity(i);
                        }
                        else
                        {
                            LinearLayout l = (LinearLayout) findViewById(R.id.homes_layout);
                            l.setVisibility(View.VISIBLE);
                            String [] hh = new String[Homs.size()];
                            for(int i=0;i<Homs.size();i++)
                            {
                                hh[i] = Homs.get(i).getName() ;
                            }
                            if (hh.length>0)
                            {
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(act,R.layout.spinners_item,hh);
                                homes.setAdapter(adapter);
                            }
                        }
                    }
                });
            }

            @Override
            public void onError (String code, String error)
            {

            }
        });
    }

    public void Continue(View view)
    {


        Intent i = new Intent(act , Rooms.class);
        act.startActivity(i);
    }
}