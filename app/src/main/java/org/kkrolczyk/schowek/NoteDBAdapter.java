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



/*

public class NoteDBAdapter extends AbstractDBAdapter {

//private static final String TAG = "NoteDBAdapter";
//private final Context context;
private static NoteConfig config;
private static List<NoteConfig> Configs_preparer(){
        config = new NoteConfig();
        return new ArrayList<NoteConfig>(Arrays.asList(config)); }
public NoteDBAdapter(Context ctx){
        super(ctx, Configs_preparer());
        this.context = ctx;
        };
//private NoteDBAdapter(){this.context = null;}; //prevent empty constructor

    // TODO: moved to AbstractDBAdapter  AND SILENTLY FAILS...(no error)

*/



public class NoteDBAdapter {  //TODO: extends AbstractDBAdapter

    private static final String TAG = "NoteDBAdapter";
    //private final Context context;

    // TODO: move to AbstractDBAdapter
    //////////////////////////////////
    private static final int DATABASE_VERSION = 1;
    private NoteConfig config = new NoteConfig();
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
    // TODO: move to AbstractDBAdapter
    //////////////////////////////////
    private class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context){
            super(context, config.DBASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(config.TABLE_CREATE); //create if does not exist...
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL(config.TABLE_DROP);
            onCreate(db);
        }
    }

    public NoteDBAdapter(Context context){
        //this.context = context;
        DBHelper = new DatabaseHelper(context);

    };
    //private NoteDBAdapter(){this.context = null;}; //prevent empty constructor


    public NoteDBAdapter open() throws SQLException
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
        db.execSQL(config.TABLE_DROP);
        db.execSQL(config.TABLE_CREATE);
        //db = DBHelper.getWritableDatabase();
    }

    protected void BackupDB(Enum direction, boolean target_ext){

        // boolean ext = context.getSharedPreferences("FILE",0).getBoolean("default_backup_to_external", true);
        if (direction == MyUtils.db_copy_direction.STORE)
            MyUtils.Backup_DB(MyUtils.db_copy_direction.STORE, config.DBASE_NAME, target_ext);
        else
            MyUtils.Backup_DB(MyUtils.db_copy_direction.LOAD, config.DBASE_NAME, target_ext);
    }






    public long insertItem(String timestamp, String note)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("timestamp", timestamp);
        initialValues.put("note", note);
        return db.insert(config.TABLE_NAME, null, initialValues);
    }
    public boolean deleteItem(long rowId)
    {
        return db.delete(config.TABLE_NAME, "_id = " + rowId, null) > 0;
        // TODO: _id should be somehow abstracted away
    }

//    public long insertItem(HashMap<String, String> items)
//    {
//        ContentValues initialValues = new ContentValues();
//        for (HashMap.Entry<String, String> entry : items.entrySet())
//            initialValues.put(entry.getKey(), entry.getValue());
//        return db.insert(config.TABLE_NAME, null, initialValues);
//    }

    public Cursor getAllItems() throws SQLException
    {
        return db.query(config.TABLE_NAME, config.DATABASE_KEYS,
                null,
                null,
                null,
                null,
                null);
    }

    public Cursor getItem(long rowId) throws SQLException
    {
        // TODO: _id should be somehow abstracted away
        Cursor mCursor =
                db.query(true, config.TABLE_NAME, config.DATABASE_KEYS,
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

    // TODO: AbstractDBAdapter should provide and abstraction!!
    // TODO: _id should be somehow abstracted away
    // ideas : args.get | @Override ContentValues | reflexion & cv.getMethod(String("get") + getClass(item)) (item) | downcast from Object ?
    // for now => each activity has it's own DB adapter implementation

    public boolean updateItem(long rowId, String timestamp, String note)
    {
        //Log.d("DB update", timestamp + " " + note);
        ContentValues args = new ContentValues();
        args.put("timestamp", timestamp);
        args.put("note", note);
        return db.update(config.TABLE_NAME, args,
                "_id" + "=" + rowId, null) > 0;
    }

}