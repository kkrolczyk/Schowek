package org.kkrolczyk.schowek;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.List;

public abstract class AbstractDBAdapter <T> {

    private static final String TAG = "AbstractDBAdapter";
    private static final int DATABASE_VERSION = 1;
    private List<AbstractConfig> configs;
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
    }

    public AbstractDBAdapter(Context context, List<T> configs){
    /* AbstractDBAdapter receives list of configs, type T, all based on AbstractConfig */

        this.context = context;
        // upcast, we will not use any of subclass items here
        this.configs = (List<AbstractConfig>) configs;
        //create new DB if there isn't one...
        DBHelper = new DatabaseHelper(context, configs.get(0).DBASE_NAME);
    }

    public AbstractDBAdapter open() throws SQLException {
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

    private void Backup(){
        Intent intent = new Intent(context, BackupActivityView.class);
        intent.putExtra("dbname", config.DBASE_NAME);
        intent.putExtra("dbpath", db_path);
        context.startActivity(intent);
    }

    private void tables_dropper(SQLiteDatabase db) {
        for (AbstractConfig conf: configs){
            db.execSQL(conf.TABLE_DROP);
        }
    }
    private void tables_creator(SQLiteDatabase db){
        for (AbstractConfig conf: configs){
            db.execSQL(conf.TABLE_CREATE);
            if (conf.tb_preconfiguration != null)
                db.execSQL(conf.tb_preconfiguration);
        }
    }
}
