package org.kkrolczyk.schowek;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.List;

public abstract class _AbstractDBAdapter<T> {

    private static final String TAG = "_AbstractDBAdapter";
    private static final int DATABASE_VERSION = 1;
    private List<_AbstractConfig> configs;
    protected Context context;
    protected DatabaseHelper DBHelper;
    protected SQLiteDatabase db = null;
    protected String db_path;

    protected class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context, String DatabaseName){
            super(context, DatabaseName, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            tables_creator(db);
            db_path = db.getPath();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database, which will destroy all old data. " +
                       "Ver_old: " + oldVersion + ", Ver_new: " + newVersion);
            tables_dropper(db);
            onCreate(db);
        }

    }

    public _AbstractDBAdapter(Context context, List<T> configs){
    /* _AbstractDBAdapter receives list of configs, type T, all based on AbstractConfig */

        this.context = context;
        // upcast, we will not use any of subclass items here
        this.configs = (List<_AbstractConfig>) configs;
        //create new DB if there isn't one...
        DBHelper = new DatabaseHelper(context, ((_AbstractConfig) configs.get(0)).DBASE_NAME);
    }

    public _AbstractDBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this; // TODO: ?
    }

    public void close() {
        DBHelper.close();
    }

    public void drop(){
        tables_dropper(db);
        tables_creator(db);
    }

    protected void Backup(){
        Intent intent = new Intent(context, _BackupActivityView.class);
        intent.putExtra("dbname", (configs.get(0)).DBASE_NAME);
        intent.putExtra("dbpath", db_path);
        context.startActivity(intent);
    }

    private void tables_dropper(SQLiteDatabase db) {
        for (_AbstractConfig conf: configs){
            db.execSQL(conf.TABLE_DROP);
        }
    }
    private void tables_creator(SQLiteDatabase db){
        for (_AbstractConfig conf: configs){
            db.execSQL(conf.TABLE_CREATE);
            if (conf.tb_preconfiguration != null)
                db.execSQL(conf.tb_preconfiguration);
        }
    }
}
