package org.kkrolczyk.schowek;

import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by kkrolczyk on 18.11.14.
 */

public class DBAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_NOTE = "note";
    public static final String KEY_TIMESTAMP = "timestamp";
    protected static final String DATABASE_NAME = "notki.db";


    private static final String TAG = "DBAdapter";
    private static final String DATABASE_TABLE = "notatki";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
            "CREATE TABLE IF NOT EXISTS "
             + DATABASE_TABLE
                     + " ("
            + KEY_ROWID + " INTEGER PRIMARY KEY autoincrement, "
            + KEY_TIMESTAMP + " TIMESTAMP,"
            + KEY_NOTE  + " TEXT" //  not null ?

                     + ");";
    private static final String TABLE_DROP =
            "DROP TABLE IF EXISTS "+ DATABASE_TABLE + ";";


    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
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

    public long insertItem(String timestamp, String note)
    {
        //Log.e("DB insert", timestamp + " " + note);
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TIMESTAMP, timestamp);
        initialValues.put(KEY_NOTE, note);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }


    public boolean deleteItem(long rowId)
    {
        //Log.e("DB delete", ""+rowId);
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor getAllItems() throws SQLException
    {
        return db.query(DATABASE_TABLE, new String[] {
                        KEY_ROWID,
                        KEY_TIMESTAMP,
                        KEY_NOTE
                        },
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
                db.query(true, DATABASE_TABLE, new String[] {
                                KEY_ROWID,
                                KEY_TIMESTAMP,
                                KEY_NOTE
                        },
                        KEY_ROWID + "=" + rowId,
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

    public boolean updateItem(long rowId, String timestamp, String note)
    {
        //Log.d("DB update", timestamp + " " + note);
        ContentValues args = new ContentValues();
        args.put(KEY_TIMESTAMP, timestamp);
        args.put(KEY_NOTE, note);
        return db.update(DATABASE_TABLE, args,
                KEY_ROWID + "=" + rowId, null) > 0;
    }
}