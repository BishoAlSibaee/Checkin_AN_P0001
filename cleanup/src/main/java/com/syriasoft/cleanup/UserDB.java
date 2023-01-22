package com.syriasoft.cleanup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;

public class UserDB extends SQLiteOpenHelper implements Serializable{
    private static  int DATABASE_VESION = 1 ;
    private static String DATABASE_NAME = "User" ;
    SQLiteDatabase db ;
    Context c ;

    public UserDB( Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VESION);
        db =getWritableDatabase();
        this.c = context ;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS user ( 'id' INTEGER PRIMARY KEY ,'name' VARCHAR , 'jobNumber' INTEGER , 'mobile' INTEGER,'token' TEXT ,'department' VARCHAR , 'Facility' INTEGER,'Version' INTEGER) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase dbd, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS 'user'");
        onCreate(db);
    }

    public boolean insertUser (int id ,String name, int mobile , String token ,String department , int jobNumber ,int Facility,int Version)
    {
        boolean result = false ;
        ContentValues values = new ContentValues();
        values.put("id",id);
        values.put("name",name);
        values.put("jobNumber",jobNumber);
        values.put("mobile",mobile);
        values.put("token",token);
        values.put("department",department);
        values.put("Facility", Facility );
        values.put("Version",Version);
        try {
            if (db.insert("user", null, values) > 0){
                result = true ;
            }
        }catch (Exception e )
        {
            Toast.makeText(c , e.getMessage() , Toast.LENGTH_LONG).show();
            Log.e("bbb" , e.getMessage());
            result = false ;
        }

        return result ;
    }

    public boolean isLogedIn()
    {
        boolean result = false ;
        Cursor c = db.query("user", new String[]{"id"}, "", null, null, null, null);
        if (c.getCount() == 1)
        {
            result = true ;
        }
        else
        {
            result = false ;
        }

        return result;
    }

    public User getUser()
    {
        User u ;
        int id  ;
        int jobNumber ;
        int mobile ;
        String token;
        String department ;
        String name ;
        //"id","name","mobile","jobNumber","token",
        //Cursor c = db.query("user", new String[]{"department" }, "", null, null, null, null);
        Cursor c = db.rawQuery("SELECT * FROM 'user' ; " , null);
        if (c.getCount() > 0 ) {
            c.moveToFirst();
            id = c.getInt(0);
            name = c.getString(1);
            jobNumber = c.getInt(2);
            mobile = c.getInt(3);
            token = c.getString(4);
            department = c.getString(5);
            u=new User(id,name,jobNumber,mobile,department,token);
            return u ;
        }
        else {
            return null;
        }

    }

    public void logout()
    {
        db.execSQL("DROP TABLE IF EXISTS 'user'");
        onCreate(db);
    }

    public void insertFacility(int fac)
    {
        //String v = String.valueOf(fac);
        if (isLogedIn())
        {
            ContentValues cv = new ContentValues();
            cv.put("Facility" , fac );
            db.update("user" , cv , "id= 1" ,null) ;
        }
    }

    public int getFacility()
    {
        int fac = 0;

        Cursor c = db.query("user", new String[]{"Facility"}, "", null, null, null, null);
        c.moveToFirst();
        fac = c.getInt(0);

        return fac ;
    }


}
