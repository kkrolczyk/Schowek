package org.kkrolczyk.schowek;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

/**
 * Created by kkrolczyk on 15.01.15.
 */
public abstract class AbstractDBAdapter <T> {

    private static final String TAG = "AbstractDBAdapter";
    private static final int DATABASE_VERSION = 1;
    private List<AbstractConfig> configs;
    private AbstractConfig config;
    protected Context context;
    protected DatabaseHelper DBHelper;
    protected SQLiteDatabase db;

    //private DatabaseHelper DBHelper;

    protected class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context){
            super(context, config.DBASE_NAME, null, DATABASE_VERSION);
//            db = this.getWritableDatabase();
        }

        @Override
        public void onCreate(SQLiteDatabase db){


            Log.e(TAG, this.toString());
            db.execSQL(config.TABLE_CREATE); //create if does not exist...

            Log.e(TAG, config.TABLE_CREATE);
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

    public AbstractDBAdapter(Context context, List<T> configs){

        this.context = context;

        // upcast, we will not use any of subclass items here. Other option? reflection
        this.configs = (List<AbstractConfig>) configs;
        this.config = (AbstractConfig) configs.get(0);
        //create new DB if there isn't one...
        DBHelper = new DatabaseHelper(context);
    }

    public AbstractDBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        DBHelper.close();
    }


    public void tables_dropper(SQLiteDatabase db){
        for (AbstractConfig conf: configs){
            db.execSQL(conf.TABLE_DROP);
        }
    }
    public void tables_creator(SQLiteDatabase db){
        for (AbstractConfig conf: configs){
            db.execSQL(conf.TABLE_CREATE); //create if does not exist...
        }
    }

    public void drop(){
        tables_dropper(db);
        tables_creator(db);
        //db = DBHelper.getWritableDatabase();
    }


    protected void BackupDB(Enum direction, boolean target_ext){
        if (direction == MyUtils.db_copy_direction.STORE)
            MyUtils.Backup_DB(MyUtils.db_copy_direction.STORE, config.DBASE_NAME, target_ext);
        else
            MyUtils.Backup_DB(MyUtils.db_copy_direction.LOAD, config.DBASE_NAME, target_ext);
    }

    //public abstract long insertItem(...args);
    public abstract boolean deleteItem(long item);
    public abstract Cursor getItem(long rowId);
    //public abstract boolean updateItem(...args);


}
