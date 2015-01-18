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


/*

public class BilansDBAdapter extends AbstractDBAdapter{

    //private static final String TAG = "BilansDBAdapter";

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
        this.context = context;
    }
    //private BilansDBAdapter(){this.context = null;}; //prevent empty constructor

    // TODO: moved to AbstractDBAdapter  AND SILENTLY FAILS...(no error)
 */


public class BilansDBAdapter {

    private static final String TAG = "BilansDBAdapter";
    //private final Context context;

    // TODO: move to AbstractDBAdapter
    //////////////////////////////////
    private static final int DATABASE_VERSION = 1;
    private BilansConfig config_main = new BilansConfig("bilans");
    private BilansConfig config_help1 = new BilansConfig("items_in_categories");
    private BilansConfig config_help2 = new BilansConfig("categories");
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
    // TODO: move to AbstractDBAdapter

    public BilansDBAdapter(Context context)
    {
        //this.context = context;
        DBHelper = new DatabaseHelper(context);
    }
    //private BilansDBAdapter(){this.context = null;}; //prevent empty constructor

    private class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context){
            super(context, config_main.DBASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            tables_creator(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            Log.w(TAG, "Upgrading database from version " + oldVersion
                    + " to "
                    + newVersion + ", which will destroy all old data");
            tables_dropper(db);
            onCreate(db);
        }
    }

    public BilansDBAdapter open() throws SQLException{
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        DBHelper.close();
    }


    public void tables_dropper(SQLiteDatabase db){
        db.execSQL(config_main.TABLE_DROP);
        db.execSQL(config_help1.TABLE_DROP);
        db.execSQL(config_help2.TABLE_DROP);

    }
    public void tables_creator(SQLiteDatabase db){
        db.execSQL(config_main.TABLE_CREATE); //create if does not exist...
        db.execSQL(config_help1.TABLE_CREATE); //create if does not exist...
        db.execSQL(config_help2.TABLE_CREATE); //create if does not exist...
    }

    public void drop(){
        tables_dropper(db);
        tables_creator(db);
        //db = DBHelper.getWritableDatabase();
    }

    protected void BackupDB(Enum direction, boolean target_ext){
        // todo = context used only here, solve this better and remove context
        // boolean ext = context.getSharedPreferences("FILE",0).getBoolean("default_backup_to_external", true);
        if (direction == MyUtils.db_copy_direction.STORE)
            MyUtils.Backup_DB(MyUtils.db_copy_direction.STORE, config_main.DBASE_NAME, target_ext);
        else
            MyUtils.Backup_DB(MyUtils.db_copy_direction.LOAD, config_main.DBASE_NAME, target_ext);
    }



    ////////////////////////////// end of things (above) that could be transferred to AbstractDBAdapter



    public long insertItem(String timestamp, String kasa, int parametry, String tytul, String szczegoly)
    {
        ContentValues args = new ContentValues();
        args.put("data", timestamp);
        args.put("kasa", kasa);
        args.put("parametry", parametry);
        args.put("tytul", tytul);
        args.put("szczegoly", szczegoly);
        return db.insert(config_main.TABLE_NAME, null, args);
    }

    public boolean deleteItem(long rowId)
    {
        return db.delete(config_main.TABLE_NAME, "_id = " + rowId, null) > 0;
    }

    public Cursor getAllItems() throws SQLException
    {
        return db.query(config_main.TABLE_NAME, config_main.DATABASE_KEYS,
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
                db.query(true, config_main.TABLE_NAME, config_main.DATABASE_KEYS,
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
        return db.update(config_main.TABLE_NAME, args,
                "_id" + "=" + rowId, null) > 0;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // HELPERS table and its functions.
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void insertCategory(String category){

        ContentValues args = new ContentValues();
        args.put("category", category);
        try {
            db.insertOrThrow(config_help2.TABLE_NAME, null, args);
        } catch (android.database.sqlite.SQLiteConstraintException e) {}
        // skip unique
    }

    public List<String> getCategories(){
       List<String> categories = new ArrayList<String>();
        Cursor c = db.query(config_help2.TABLE_NAME, new String[]{"category"},
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

    public void insertItemIntoCategory(String category, String item, Float value){

        ContentValues args = new ContentValues();
        args.put("category", category);
        args.put("item", item);
        args.put("value", value);
        args.put("datetime", MyUtils.timestamp());
        db.insert(config_help1.TABLE_NAME, null, args);
    }

    public List<List<String>> getCategoryItems2(String category){

        List<List<String>> items = new ArrayList<List<String>>();
        Cursor c = db.query(config_help1.TABLE_NAME, new String[]{"item", "value"},
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