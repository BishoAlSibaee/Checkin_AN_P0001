package com.syriasoft.cleanup;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CleanUp_Adapter extends BaseAdapter {

    List<cleanOrder> list;
    LayoutInflater inflter;
    Context co ;
    String setRoomStatus = LogIn.URL+"setRoomStatus.php";

    CleanUp_Adapter(List<cleanOrder> list , Context c) {
        this.list = sortList(list); ;
        inflter = (LayoutInflater.from(c));
        this.co = c ;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        convertView = inflter.inflate(R.layout.order_unit, null);
        final LinearLayout l = (LinearLayout) convertView.findViewById(R.id.all);
        TextView room = convertView.findViewById(R.id.cleanOrder_room);
        TextView dep = convertView.findViewById(R.id.cleanOrder_orderType);
        TextView date = convertView.findViewById(R.id.cleanOrder_orderDate);
        TextView vv = convertView.findViewById(R.id.textView3);
        ImageView img = convertView.findViewById(R.id.imageView2);
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(list.get(position).date);
        int month = c.get(Calendar.MONTH)+1 ;
        date.setText(c.get(Calendar.DAY_OF_MONTH)+"/"+month+"/"+c.get(Calendar.YEAR)+" " + c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE));
        room.setText(list.get(position).roomNumber);
        for (int i=0;i<MainActivity.Rooms.size();i++)
        {
            if (MainActivity.Rooms.get(i).RoomNumber == Integer.parseInt(list.get(position).roomNumber ))
            {
                if (MainActivity.CurrentRoomsStatus.get(i).equals("1")) {
                    room.setTextColor(Color.WHITE);
                    room.setBackgroundColor(co.getColor(R.color.greenRoom));
                    vv.setBackgroundColor(co.getColor(R.color.greenRoom));
                    img.setBackgroundColor(co.getColor(R.color.greenRoom));
                }
                else if (MainActivity.CurrentRoomsStatus.get(i).equals("2")) {
                    room.setTextColor(Color.WHITE);
                    room.setBackgroundColor(co.getColor(R.color.redRoom));
                    vv.setBackgroundColor(co.getColor(R.color.redRoom));
                    img.setBackgroundColor(co.getColor(R.color.redRoom));
                }
                else if (MainActivity.CurrentRoomsStatus.get(i).equals("3")) {
                    room.setTextColor(Color.WHITE);
                    room.setBackgroundColor(co.getColor(R.color.blueRoom));
                    vv.setBackgroundColor(co.getColor(R.color.blueRoom));
                    img.setBackgroundColor(co.getColor(R.color.blueRoom));
                }
                else if ( MainActivity.CurrentRoomsStatus.get(i).equals("4")) {
                    room.setTextColor(Color.WHITE);
                    room.setBackgroundColor(Color.GRAY);
                    vv.setBackgroundColor(Color.GRAY);
                    img.setBackgroundColor(Color.GRAY);
                }

                MainActivity.FireRooms.get(i).child("SuiteStatus").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null )
                        {
                            Log.d("suite " , dataSnapshot.getValue().toString());
                            if (dataSnapshot.getValue().toString().equals("2"))
                            {
                                vv.setVisibility(View.VISIBLE);
                                vv.setText("S");
                                vv.setTextColor(Color.WHITE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

        if (list.get(position).dep.equals("Cleanup"))
        {
            img.setImageResource(R.drawable.cleanup_btn);
            dep.setText(list.get(position).dep );
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v)
                {
                    int roomNumber = 0;
                    for (int i=0;i<MainActivity.Rooms.size();i++)
                    {
                        if (MainActivity.Rooms.get(i).RoomNumber == Integer.parseInt(MainActivity.orderDB.getOrders().get(position).roomNumber))
                        {
                            MainActivity.FireRoom = MainActivity.FireRooms.get(i) ;
                            roomNumber = MainActivity.Rooms.get(i).RoomNumber ;
                        }
                    }
                    dep.setBackgroundColor(co.getColor(R.color.transparentGray));
                    date.setBackgroundColor(co.getColor(R.color.transparentGray));
                    final Dialog d  = new Dialog(co);
                    d.setContentView(R.layout.confermation_dialog);
                    d.setCancelable(false);
                    TextView message = (TextView) d.findViewById(R.id.confermationDialog_Text);
                    message.setText("Are You Sure ..? ");
                    Button cancel = (Button)d.findViewById(R.id.confermationDialog_cancel);
                    cancel.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            d.dismiss();
                            //l.setBackgroundColor(co.getColor(R.color.white));
                            dep.setBackgroundColor(co.getColor(R.color.white));
                            date.setBackgroundColor(co.getColor(R.color.white));
                        }
                    });
                    Button ok = (Button)d.findViewById(R.id.messageDialog_ok);
                    final int finalRoomNumber = roomNumber;
                    ok.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            MainActivity.makeOrderDone(position,Integer.parseInt(list.get(position).roomNumber ),Integer.parseInt( list.get(position).orderNumber) , d , LogIn.db.getUser().jobNumber , list.get(position).dep);
                            Log.d("deleteProblem" , MainActivity.FireRoom.toString());
                            MainActivity.FireRoom.child("Cleanup").setValue(0);
                            d.dismiss();
                            MainActivity.FireRoom.child("roomStatus").addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if (dataSnapshot.getValue() != null)
                                    {
                                        if ( dataSnapshot.getValue().toString().equals("3"))
                                        {

                                            StringRequest request = new StringRequest(Request.Method.POST, setRoomStatus, new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response)
                                                {
                                                    Log.d("roomStatusRes" , response);
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error)
                                                {
                                                    Log.e("roomStatusRes" , error.getMessage());
                                                }
                                            })
                                            {
                                                @Override
                                                protected Map<String, String> getParams() throws AuthFailureError {
                                                    Map<String,String> par = new HashMap<String,String>();
                                                    par.put("room" ,String.valueOf(finalRoomNumber));
                                                    return par;
                                                }
                                            };
                                            Volley.newRequestQueue(co).add(request);
                                            MainActivity.FireRoom.child("roomStatus").setValue(1);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                    d.show();
                    return false;
                }
            });
        }
        else if (list.get(position).dep.equals("Laundry"))
        {
            img.setImageResource(R.drawable.laundry_btn);
            dep.setText(list.get(position).dep );
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v)
                {
                    int roomNumber = 0;
                    for (int i=0;i<MainActivity.Rooms.size();i++)
                    {
                        if (MainActivity.Rooms.get(i).RoomNumber == Integer.parseInt(MainActivity.orderDB.getOrders().get(position).roomNumber))
                        {
                            MainActivity.FireRoom = MainActivity.FireRooms.get(i) ;
                            roomNumber = MainActivity.Rooms.get(i).RoomNumber ;
                        }
                    }
                    //l.setBackgroundColor(co.getColor(R.color.transparentGray));
                    dep.setBackgroundColor(co.getColor(R.color.transparentGray));
                    date.setBackgroundColor(co.getColor(R.color.transparentGray));
                    final Dialog d  = new Dialog(co);
                    d.setContentView(R.layout.confermation_dialog);
                    d.setCancelable(false);
                    TextView message = (TextView) d.findViewById(R.id.confermationDialog_Text);
                    message.setText("Are You Sure ..? ");
                    Button cancel = (Button)d.findViewById(R.id.confermationDialog_cancel);
                    cancel.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            d.dismiss();
                            //l.setBackgroundColor(co.getColor(R.color.white));
                            dep.setBackgroundColor(co.getColor(R.color.white));
                            date.setBackgroundColor(co.getColor(R.color.white));
                        }
                    });
                    Button ok = (Button)d.findViewById(R.id.messageDialog_ok);
                    ok.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            MainActivity.makeOrderDone(position,Integer.parseInt(list.get(position).roomNumber ),Integer.parseInt( list.get(position).orderNumber) , d , LogIn.db.getUser().jobNumber , list.get(position).dep);
                            Log.d("deleteProblem" , "rooms "+MainActivity.Rooms.size()+" fires "+MainActivity.FireRooms.size()+" orders "+list.size());
                            MainActivity.FireRoom.child("Laundry").setValue(0);
                            d.dismiss();
                        }
                    });

                    d.show();
                    return false;
                }
            });
        }
        else if (list.get(position).dep.equals("RoomService"))
        {
            img.setImageResource(R.drawable.roomservice);
            dep.setText( list.get(position).roomServiceText);
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v)
                {
                    int roomNumber = 0;
                    for (int i=0;i<MainActivity.Rooms.size();i++)
                    {
                        if (MainActivity.Rooms.get(i).RoomNumber == Integer.parseInt(MainActivity.orderDB.getOrders().get(position).roomNumber))
                        {
                            MainActivity.FireRoom = MainActivity.FireRooms.get(i) ;
                            roomNumber = MainActivity.Rooms.get(i).RoomNumber ;
                        }
                    }
                    //l.setBackgroundColor(co.getColor(R.color.transparentGray));
                    dep.setBackgroundColor(co.getColor(R.color.transparentGray));
                    date.setBackgroundColor(co.getColor(R.color.transparentGray));
                    final Dialog d  = new Dialog(co);
                    d.setContentView(R.layout.confermation_dialog);
                    d.setCancelable(false);
                    TextView message = (TextView) d.findViewById(R.id.confermationDialog_Text);
                    message.setText("Are You Sure ..? ");
                    Button cancel = (Button)d.findViewById(R.id.confermationDialog_cancel);
                    cancel.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            d.dismiss();
                            //l.setBackgroundColor(co.getColor(R.color.white));
                            dep.setBackgroundColor(co.getColor(R.color.white));
                            date.setBackgroundColor(co.getColor(R.color.white));
                        }
                    });
                    Button ok = (Button)d.findViewById(R.id.messageDialog_ok);
                    ok.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            MainActivity.makeOrderDone(position,Integer.parseInt(list.get(position).roomNumber ),Integer.parseInt( list.get(position).orderNumber) , d , LogIn.db.getUser().jobNumber , list.get(position).dep);
                            Log.d("deleteProblem" , "rooms "+list.size()+" fires "+MainActivity.FireRooms.size());
                            MainActivity.FireRoom.child("RoomService").setValue(0);
                            d.dismiss();
                        }
                    });

                    d.show();
                    return false;
                }
            });
        }
        else if (list.get(position).dep.equals("SOS"))
        {
            img.setImageResource(R.drawable.sos_btn);
            dep.setText(list.get(position).dep );
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v)
                {
                    int roomNumber = 0;
                    for (int i=0;i<MainActivity.Rooms.size();i++)
                    {
                        if (MainActivity.Rooms.get(i).RoomNumber == Integer.parseInt(MainActivity.orderDB.getOrders().get(position).roomNumber))
                        {
                            MainActivity.FireRoom = MainActivity.FireRooms.get(i) ;
                            roomNumber = MainActivity.Rooms.get(i).RoomNumber ;
                        }
                    }
                    //l.setBackgroundColor(co.getColor(R.color.transparentGray));
                    dep.setBackgroundColor(co.getColor(R.color.transparentGray));
                    date.setBackgroundColor(co.getColor(R.color.transparentGray));
                    final Dialog d  = new Dialog(co);
                    d.setContentView(R.layout.confermation_dialog);
                    d.setCancelable(false);
                    TextView message = (TextView) d.findViewById(R.id.confermationDialog_Text);
                    message.setText("Are You Sure ..? ");
                    Button cancel = (Button)d.findViewById(R.id.confermationDialog_cancel);
                    cancel.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            d.dismiss();
                            //l.setBackgroundColor(co.getColor(R.color.white));
                            dep.setBackgroundColor(co.getColor(R.color.white));
                            date.setBackgroundColor(co.getColor(R.color.white));
                        }
                    });
                    Button ok = (Button)d.findViewById(R.id.messageDialog_ok);
                    ok.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            MainActivity.makeOrderDone(position,Integer.parseInt(list.get(position).roomNumber ),Integer.parseInt( list.get(position).orderNumber) , d , LogIn.db.getUser().jobNumber , list.get(position).dep);
                            Log.d("deleteProblem" , "rooms "+list.size()+" fires "+MainActivity.FireRooms.size());
                            MainActivity.FireRoom.child("SOS").setValue(0);
                            d.dismiss();
                        }
                    });

                    d.show();
                    return false;
                }
            });
        }
        else if (list.get(position).dep.equals("MiniBarCheck"))
        {
            img.setImageResource(R.drawable.minibar);
            img.setPadding(10,10,10,10);
            dep.setText(list.get(position).dep );
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v)
                {
                    int roomNumber = 0;
                    for (int i=0;i<MainActivity.Rooms.size();i++)
                    {
                        if (MainActivity.Rooms.get(i).RoomNumber == Integer.parseInt(MainActivity.orderDB.getOrders().get(position).roomNumber))
                        {
                            MainActivity.FireRoom = MainActivity.FireRooms.get(i) ;
                            roomNumber = MainActivity.Rooms.get(i).RoomNumber ;
                        }
                    }
                    //l.setBackgroundColor(co.getColor(R.color.transparentGray));
                    dep.setBackgroundColor(co.getColor(R.color.transparentGray));
                    date.setBackgroundColor(co.getColor(R.color.transparentGray));
                    final Dialog d  = new Dialog(co);
                    d.setContentView(R.layout.confermation_dialog);
                    d.setCancelable(false);
                    TextView message = (TextView) d.findViewById(R.id.confermationDialog_Text);
                    message.setText("Are You Sure ..? ");
                    Button cancel = (Button)d.findViewById(R.id.confermationDialog_cancel);
                    cancel.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            d.dismiss();
                            //l.setBackgroundColor(co.getColor(R.color.white));
                            dep.setBackgroundColor(co.getColor(R.color.white));
                            date.setBackgroundColor(co.getColor(R.color.white));
                        }
                    });
                    Button ok = (Button)d.findViewById(R.id.messageDialog_ok);
                    ok.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                                Intent i = new Intent(co, com.syriasoft.cleanup.MiniBarCheck.class);
                                i.putExtra("Room" , MainActivity.orderDB.getOrders().get(position).roomNumber);
                                i.putExtra("OrderId" , MainActivity.orderDB.getOrders().get(position).orderNumber );
                                Toast.makeText(co,"room "+MainActivity.orderDB.getOrders().get(position).roomNumber+" ordr "+MainActivity.orderDB.getOrders().get(position).orderNumber,Toast.LENGTH_LONG).show();
                                d.dismiss();
                                co.startActivity(i);
                        }
                    });

                    d.show();
                    return false;
                }
            });
        }
        else
        {
            img.setVisibility(View.GONE);
        }
//        if (MainActivity.orderDB.getOrders().get(position).dep.equals("SOS"))
//        {
//            dep.setBackgroundColor(Color.RED);
//            date.setBackgroundColor(Color.RED);
//            room.setTextColor(Color.WHITE);
//            dep.setTextColor(Color.RED);
//            date.setTextColor(Color.WHITE);
//            vv.setTextColor(Color.WHITE);
//        }

        return convertView;
    }

    List<cleanOrder> sortList(List<cleanOrder> list) {
        for (int i = 0;i<list.size()-1;i++) {
            for (int j =0;j<list.size()-i-1;j++) {
                if (list.get(j+1).date < list.get(j).date) {
                    Collections.swap(list,j,j+1);
                }
            }
        }
        return list ;
    }


}
