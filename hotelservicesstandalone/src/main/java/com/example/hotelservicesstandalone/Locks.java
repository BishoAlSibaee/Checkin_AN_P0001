package com.example.hotelservicesstandalone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hotelservicesstandalone.lock.ApiService;
import com.example.hotelservicesstandalone.lock.LockObj;
import com.example.hotelservicesstandalone.lock.RetrofitAPIManager;
import com.google.gson.reflect.TypeToken;
import com.ttlock.bl.sdk.util.GsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class Locks extends AppCompatActivity {

    static Activity act ;
    static RecyclerView LocksRecycler ;
    static ArrayList<LockObj> LocksList ;
    LinearLayoutManager Manager ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locks);
        setActivity();
        getLocks();
        getUsers();
    }

    void setActivity() {
        act = this ;
        LocksRecycler = findViewById(R.id.locksRecycler);
        LocksList = new ArrayList<>();
        Manager = new LinearLayoutManager(act,RecyclerView.VERTICAL,false);
        LocksRecycler.setLayoutManager(Manager);
    }

    static void getLocks() {
        final Dialog d = new Dialog(act);
        d.setContentView(R.layout.loading_layout);
        d.setCancelable(false);
        d.show();
        ApiService apiService = RetrofitAPIManager.provideClientApi();
        Call<String> call = apiService.getLockList(ApiService.CLIENT_ID,Rooms.acc.getAccess_token(), 1, 100, System.currentTimeMillis());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response)
            {
                Log.d("locksNum" ,response.body().toString());
                d.dismiss();
                String json = response.body();
                if (json.contains("list"))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(json);
                        JSONArray array = jsonObject.getJSONArray("list");
                        LocksList = GsonUtil.toObject(array.toString(), new TypeToken<ArrayList<LockObj>>(){});
                        Log.d("locksNum" ,String.valueOf( LocksList.size() ));
                        lockAdapter adapter = new lockAdapter(LocksList);
                        LocksRecycler.setAdapter(adapter);
                    }
                    catch (JSONException e)
                    {
                    }
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

    void getUsers() {
        StringRequest req = new StringRequest(Request.Method.POST, "https://api.ttlock.com/v3/user/list", new Response.Listener<String>() {
            @Override
            public void onResponse(String responsee) {
                Log.d("tokenResp",responsee);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Calendar ca = Calendar.getInstance(Locale.getDefault());
                Map<String,String> par = new HashMap<String, String>();
                par.put("clientId", ApiService.CLIENT_ID);
                par.put("clientSecret",ApiService.CLIENT_SECRET);
                par.put("startDate", String.valueOf(0));
                par.put("endDate",String.valueOf(ca.getTimeInMillis()));
                par.put("pageNo", String.valueOf(1));
                par.put("pageSize", String.valueOf(100));
                par.put("date", String.valueOf(ca.getTimeInMillis()));
                return par;
            }
        };
        Volley.newRequestQueue(act).add(req);
    }

}