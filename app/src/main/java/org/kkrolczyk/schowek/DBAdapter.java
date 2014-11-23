package org.kkrolczyk.schowek;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by kkrolczyk on 18.11.14.
 */

public class DBAdapter {

    private static final String TAG = "DBAdapter";
    private static final int DATABASE_VERSION = 1;


    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
    private String DATABASE_NAME; // backup
    private String DATABASE_TABLE;
    private String[] DATABASE_KEYS;
    private String DATABASE_CREATE;
    private String TABLE_DROP;
    private HashMap<String, String> DATABASE_KV;

    private final Context context;

    public DBAdapter(Context ctx, Bundle config)
    {
        this.context = ctx;
        this.DATABASE_TABLE = config.getString("table_name");
        this.TABLE_DROP = "DROP TABLE IF EXISTS "+ DATABASE_TABLE + ";";
        this.DATABASE_NAME = config.getString("dbase_name");
        this.DATABASE_KV = (HashMap<String, String>) config.getSerializable("dbase_kv");
        //this.DATABASE_KEYS = (String[]) DATABASE_KV.keySet().toArray();
        this.DATABASE_KEYS = DATABASE_KV.keySet().toArray(new String[this.DATABASE_KV.size()]);

        StringBuilder sb = new StringBuilder(this.DATABASE_KV.keySet().size());
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append(DATABASE_TABLE);
        sb.append(" (");
        for (HashMap.Entry<String, String> entry:this.DATABASE_KV.entrySet()){
            sb.append(entry.getKey() + ' ' + entry.getValue() + ',');
        }
        sb.deleteCharAt(sb.length()-1); // remove last ','
        sb.append(" );");
        this.DATABASE_CREATE = sb.toString();


        DBHelper = new DatabaseHelper(context);
    }
    private DBAdapter(){this.context = null;}; //prevent empty constructor
    private DBAdapter(Context ctx){this.context = null;}; // prevent also constructor without configuration.

    private class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(DATABASE_CREATE); //create if does not exist...
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL(TABLE_DROP);
            onCreate(db);
        }
    }

    public DBAdapter open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        DBHelper.close();
    }


    public void drop()
    {
        db.execSQL(TABLE_DROP);
        db.execSQL(DATABASE_CREATE);
    }

//    public long insertItem(String timestamp, String note)
//    {
//        //Log.e("DB insert", timestamp + " " + note);
//        ContentValues initialValues = new ContentValues();
//        initialValues.put(KEY_TIMESTAMP, timestamp);
//        initialValues.put(KEY_NOTE, note);
//        return db.insert(DATABASE_TABLE, null, initialValues);
//    }
    public long insertItem(HashMap<String, String> items)
    {
        ContentValues initialValues = new ContentValues();
        for (HashMap.Entry<String, String> entry : items.entrySet())
            initialValues.put(entry.getKey(), entry.getValue());
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

//    public boolean deleteItem(long rowId)
//    {
//        //Log.e("DB delete", ""+rowId);
//        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
//    }
    public boolean deleteItem(long rowId)
    {
        return db.delete(DATABASE_TABLE, "_id = " + rowId, null) > 0;
    }

    public Cursor getAllItems() throws SQLException
    {



        return db.query(DATABASE_TABLE, DATABASE_KEYS,
//        return db.query(DATABASE_TABLE, new String[] {
//                        KEY_ROWID,
//                        KEY_TIMESTAMP,
//                        KEY_NOTE
//                        },
                null,
                null,
                null,
                null,
                null);
    }


    public Cursor getItem(long rowId) throws SQLException
    {
        //Log.e("DB get", ""+rowId);
        Cursor mCursor =
                db.query(true, DATABASE_TABLE, DATABASE_KEYS,
//                        new String[] {
//                                KEY_ROWID,
//                                KEY_TIMESTAMP,
//                                KEY_NOTE
//                        },
                        //KEY_ROWID + "=" + rowId,
                        "_id =" + rowId,
                        null,
                        null,
                        null,
                        null,
                        null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

//    public boolean updateItem(long rowId, String timestamp, String note)
//    {
//        //Log.d("DB update", timestamp + " " + note);
//        ContentValues args = new ContentValues();
//        args.put(KEY_TIMESTAMP, timestamp);
//        args.put(KEY_NOTE, note);
//        return db.update(DATABASE_TABLE, args,
//                KEY_ROWID + "=" + rowId, null) > 0;
//    }
    public boolean updateItem(long rowId, HashMap<String, String> items)
    {
        ContentValues args = new ContentValues();
        for (HashMap.Entry<String, String> entry : items.entrySet())
            args.put(entry.getKey(), entry.getValue());
        return db.update(DATABASE_TABLE, args,
                "_id =" + rowId, null) > 0;
    }

}