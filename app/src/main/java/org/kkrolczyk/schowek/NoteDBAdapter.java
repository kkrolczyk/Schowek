package org.kkrolczyk.schowek;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kkrolczyk on 18.11.14.
 */

public class NoteDBAdapter extends AbstractDBAdapter {

    private static final String TAG = "NoteDBAdapter";

    private static NoteConfig config;
    private static List<NoteConfig> Configs_preparer(){
        config = new NoteConfig();
        return new ArrayList<NoteConfig>(Arrays.asList(config));
    }

    public NoteDBAdapter(Context ctx){
            super(ctx, Configs_preparer());
            };

//private NoteDBAdapter(){this.context = null;}; //prevent empty constructor

    ////////////////////////////////////////////////////////////////////////////////////////////////

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
    }

    public Cursor getAllItems(int... sort_order) throws SQLException
    {
        String order_by = null;

        if (sort_order.length>0){
            //Log.e(TAG,"Sort order = cre asc/cre desc/mod asc/mod desc/cont asc/cont desc/ =:" + sort_order[0]);
            String order_dir = Arrays.toString(MyUtils.sort_order.values()).replaceAll("^.|.$", "").split(", ")[sort_order[0]].split("_")[1];
            //Log.e(TAG,"Sort order 7:" + config.DATABASE_KEYS[sort_order[0] % config.DATABASE_KEYS.length]);
            order_by = config.DATABASE_KEYS[sort_order[0] / 2] + " " + order_dir;
        }

        return db.query(config.TABLE_NAME, config.DATABASE_KEYS,
                null,
                null,
                null,
                null,
                order_by);
    }

    public Cursor getItem(long rowId) throws SQLException
    {
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

    public boolean updateItem(long rowId, String timestamp, String note)
    {
        ContentValues args = new ContentValues();
        args.put("timestamp", timestamp);
        args.put("note", note);
        return db.update(config.TABLE_NAME, args,
                "_id" + "=" + rowId, null) > 0;
    }

}
