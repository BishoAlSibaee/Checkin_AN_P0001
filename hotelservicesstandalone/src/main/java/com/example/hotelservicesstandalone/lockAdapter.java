package com.example.hotelservicesstandalone;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hotelservicesstandalone.lock.ApiService;
import com.example.hotelservicesstandalone.lock.LockObj;
import com.ttlock.bl.sdk.api.ExtendedBluetoothDevice;
import com.ttlock.bl.sdk.api.TTLockClient;
import com.ttlock.bl.sdk.callback.ResetLockCallback;
import com.ttlock.bl.sdk.constant.TTLockConfigType;
import com.ttlock.bl.sdk.device.TTDevice;
import com.ttlock.bl.sdk.entity.LockError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class lockAdapter extends RecyclerView.Adapter<lockAdapter.HOLDER> {

    ArrayList<LockObj> list ;

    lockAdapter( ArrayList<LockObj> list) {
        this.list = list ;
    }

    @NonNull
    @Override
    public lockAdapter.HOLDER onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lock_unit,parent,false);
        HOLDER holder = new HOLDER(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull lockAdapter.HOLDER holder, int position) {
        holder.name.setText(list.get(position).getLockName());
        holder.data1.setText(String.valueOf(list.get(position).getLockId()));
        holder.data2.setText(list.get(position).getLockAlias());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Dialog D = new Dialog(holder.itemView.getContext());
                D.setContentView(R.layout.modify_lock_dialog);
                D.setCancelable(false);
                TextView name = D.findViewById(R.id.textView24);
                name.setText(list.get(position).getLockName());
                EditText newName = D.findViewById(R.id.editTextTextPersonName2);
                Button cancel = D.findViewById(R.id.button23);
                Button rename = D.findViewById(R.id.button24);
                Button delete = D.findViewById(R.id.button25);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        D.dismiss();
                    }
                });
                rename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (newName.getText() == null || newName.getText().toString().isEmpty()) {
                            Toast.makeText(holder.itemView.getContext(),"enter lock name",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String token = Rooms.acc.getAccess_token();  //r.getString("access_token");
                        StringRequest req = new StringRequest(Request.Method.POST, "https://api.sciener.com/v3/lock/rename", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String responsee) {
                                Log.d("tokenResp",responsee);
                                if (responsee != null) {
                                    try {
                                        JSONObject rrr = new JSONObject(responsee);
                                        String errrr = rrr.getString("errcode");
                                        if (errrr.equals("0")) {
                                            D.dismiss();
                                            Toast.makeText(holder.itemView.getContext(),"Name Changed",Toast.LENGTH_SHORT).show();
                                            Locks.getLocks();
                                        }
                                        else {
                                            Toast.makeText(holder.itemView.getContext(),errrr+" "+rrr.getString("errmsg"),Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.d("tokenResp",e.toString());
                                    }

                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("tokenResp",error.toString());
                                Toast.makeText(holder.itemView.getContext(),"error changing name "+error.toString(),Toast.LENGTH_SHORT).show();
                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Calendar ca = Calendar.getInstance(Locale.getDefault());
                                Map<String,String> par = new HashMap<String, String>();
                                par.put("clientId", ApiService.CLIENT_ID);
                                par.put("accessToken",token);
                                par.put("lockId", String.valueOf(list.get(position).getLockId()));
                                par.put("lockAlias",newName.getText().toString());
                                par.put("date", String.valueOf(ca.getTimeInMillis()));
                                return par;
                            }
                        };
                        Volley.newRequestQueue(holder.itemView.getContext()).add(req);
                                }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TTLockClient.getDefault().resetLock(list.get(position).getLockData(), list.get(position).getLockMac(),new ResetLockCallback() {
                            @Override
                            public void onResetLockSuccess() {
                                Locks.getLocks();
                            }

                            @Override
                            public void onFail(LockError error) {
                                Toast.makeText(holder.itemView.getContext(),error.getErrorMsg(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                D.show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class HOLDER extends RecyclerView.ViewHolder {
        TextView name , data1,data2 ;
        public HOLDER(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textView10);
            data1 = itemView.findViewById(R.id.textView12);
            data2 = itemView.findViewById(R.id.textView22);
        }
    }
}
