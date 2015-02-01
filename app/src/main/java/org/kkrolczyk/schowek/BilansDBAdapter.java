package org.kkrolczyk.schowek;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kkrolczyk on 22.11.14.
 */

public class BilansDBAdapter extends AbstractDBAdapter{

    private static final String TAG = "BilansDBAdapter";

    private static List<BilansConfig> configs;
    private static List<BilansConfig> Configs_preparer(){
        configs = new ArrayList<BilansConfig>();
        configs.add(new BilansConfig("bilans"));
        configs.add(new BilansConfig("items_in_categories"));
        configs.add(new BilansConfig("categories"));
        return configs;
    }

    public BilansDBAdapter(Context context)
    {
        super(context, Configs_preparer());
        //this.context = context;
    }
    //private BilansDBAdapter(){this.context = null;}; //prevent empty constructor

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public long insertItem(String timestamp, String kasa, int parametry, String tytul, String szczegoly)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("data", timestamp);
        initialValues.put("kasa", kasa);
        initialValues.put("parametry", parametry);
        initialValues.put("tytul", tytul);
        initialValues.put("szczegoly", szczegoly);
        return db.insert(configs.get(0).TABLE_NAME, null, initialValues);
    }

    public boolean deleteItem(long rowId)
    {
        return db.delete(configs.get(0).TABLE_NAME, "_id = " + rowId, null) > 0;
    }

    public Cursor getAllItems() throws SQLException
    {
        return db.query(configs.get(0).TABLE_NAME, configs.get(0).DATABASE_KEYS,
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
                db.query(true, configs.get(0).TABLE_NAME, configs.get(0).DATABASE_KEYS,
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

    public boolean updateItem(long rowId, String timestamp, String kasa, int parametry, String tytul, String szczegoly)
    {
        ContentValues args = new ContentValues();
        args.put("data", timestamp);
        args.put("kasa", kasa);
        args.put("parametry", parametry);
        args.put("tytul", tytul);
        args.put("szczegoly", szczegoly);
        return db.update(configs.get(0).TABLE_NAME, args,
                "_id = " + rowId, null) > 0;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // HELPERS table and its functions.
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void insertCategory(String category){

        ContentValues args = new ContentValues();
        args.put("category", category);
        try {
            db.insertOrThrow(configs.get(2).TABLE_NAME, null, args);
        } catch (android.database.sqlite.SQLiteConstraintException e) {}
        // skip unique
    }

    public List<String> getCategories(){
       List<String> categories = new ArrayList<String>();
        Log.e(TAG, "BilansDBAdapter:" + configs.get(2).TABLE_NAME);
        Cursor c = db.query(configs.get(2).TABLE_NAME, new String[]{"category"},
                null,
                null,
                null,
                null,
                null);

        if ((c != null) && c.moveToFirst())
        {
            do {
                categories.add(c.getString(0));
            } while (c.moveToNext());
        }
        return categories;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // HELPERS for categories items
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void insertItemIntoCategory(String category, String item, Float value){

        ContentValues args = new ContentValues();
        args.put("category", category);
        args.put("item", item);
        args.put("value", value);
        args.put("datetime", MyUtils.timestamp());
        db.insert(configs.get(1).TABLE_NAME, null, args);
    }

    public List<List<String>> getCategoryItems(String category){

        List<List<String>> items = new ArrayList<List<String>>();
        Cursor c = db.query(configs.get(1).TABLE_NAME, new String[]{"item", "value"},
                "category = " + "'" + category + "'",
                null,
                null,
                null,
                null);

        if (c.moveToFirst())
        {
            do {
                ArrayList<String> list = new ArrayList<String>();
                list.add(c.getString(0));
                list.add(c.getString(1));
                list.add("0"); // placeholder for "items count"
                items.add(list);

            } while (c.moveToNext());
        }

        return items;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

}