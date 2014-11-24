package org.kkrolczyk.schowek;

// todo: szukaj notki
// todo: filtruj tagi


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class NoteView extends Activity
{
    enum copy_direction { TOEXTSD, FROMEXTSD };

    private static final String DBNAME = "notki.db";
    private static final String TBNAME = "notatki";
    private static final HashMap<String, String> configuration;
    static
    {
        configuration = new HashMap<String, String>();
        configuration.put("_id", "INTEGER PRIMARY KEY autoincrement");
        configuration.put("timestamp", "TIMESTAMP");
        configuration.put("note", "TEXT");
    }
    private static final Bundle db_adapter_data;
    static
    {
        db_adapter_data = new Bundle();
        db_adapter_data.putString("table_name", TBNAME);
        db_adapter_data.putString("dbase_name", DBNAME);
        db_adapter_data.putSerializable("dbase_kv", configuration);
    }


    DBAdapter db = new DBAdapter(this, db_adapter_data);
    SimpleCursorAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
    }

    protected void onStart() {
        super.onStart();
        //Log.i("SCHOWEK", "NoteView onStart");
        showAll();
    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        Log.i("SCHOWEK","DB on RE start"); //after pause and after activity for result has returned...
//    }


////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////   MENU   ///////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_db_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.clean_all:
                db.open();
                db.drop();
                db.close();
                break;
            case R.id.copy_to_ext_SD:
                //Log.d ("SCHOWEK", "COPYING DB to sdcard");
                Backup_DB(copy_direction.TOEXTSD);
                break;
            case R.id.copy_from_ext_SD:
                //Log.d ("SCHOWEK", "COPYING DB from sdcard");
                Backup_DB(copy_direction.FROMEXTSD);
                break;
            default:
                Log.e ("SCHOWEK", "MENU = WTF?");
        }
        return super.onOptionsItemSelected(item);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////   MENU END   ///////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////  DELEGATE WORK   ///////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    public void add_or_update_note(View v){

        int request_code = 0;
        Intent add_note_intent = new Intent(NoteView.this, NoteAdd.class);
        add_note_intent.putExtra("content", "");
        startActivityForResult(add_note_intent, request_code);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Boolean activity_success = false;
        switch (resultCode) {
            case -1:
                //Log.d("SCHOWEK", "RESULT OK in onActivityResult()");
                activity_success = data.getStringExtra("content").length() > 0;
                break;
            case 0:
                //Log.d("SCHOWEK", "FAILED in onActivityResult()");
                activity_success = false;
                break;
        }
        if (activity_success)
            switch (requestCode) {
                case 0:
                    //Log.d("SCHOWEK", "Adding new item");
                    PutItem(data.getStringExtra("content"));
                    break;
                case 1:
                    long update_id = data.getLongExtra("item_id",-1);
                    //Log.d("SCHOWEK", "Updating item " + update_id);
                    if (update_id > 0)
                        UpdateItem(update_id, data.getStringExtra("content"));
                    else
                        Log.e("SCHOWEK", "WRONG item id?");
                    break;
                default:
                    Log.e("SCHOWEK", "WRONG REQUEST CODE TO START ACTIVITY FOR RESULT ?");

            }
        else
            Toast.makeText(getApplicationContext(),
                    getString(R.string.empty_or_cancelled), Toast.LENGTH_LONG).show();

    }

    protected Boolean Backup_DB(Enum direction){

        try {
            File sd = new File("/mnt/extSdCard/");
            File data = Environment.getDataDirectory();
            //File ext_sd = new File("/mnt/extSdCard/");
            //File internal_sd = Environment.getExternalStorageDirectory();

            //if (sd.canWrite()) {
            String currentDBPath = "//data//" + this.getPackageName() + "//databases//" + DBNAME;
            String backupDBPath = DBNAME;
            File currentDB, backupDB;
            if (direction == copy_direction.FROMEXTSD) { // swap dirs == swap(currentDB, backupDB);
                currentDB = new File(sd, backupDBPath);
                backupDB = new File(data, currentDBPath);
            } else {
                currentDB = new File(data, currentDBPath);
                backupDB = new File(sd, backupDBPath);
            }

                //if (currentDB.exists()) {
            FileChannel src = new FileInputStream(currentDB).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();

            //}
        } catch (Exception e) {
            Log.e("SCHOWEK", e.toString());
            return false;
        }
        return true;
    }

    private String timestamp(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////   DELEGATE WORK END   ///////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////   DB ITEMS   ///////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

public void showAll(){

    String[] columns = new String[] {  // The desired columns to be bound
            "timestamp",
            "note",
    };

    // the XML defined views which the data will be bound to
    int[] to = new int[] {
            android.R.id.text1,
            android.R.id.text2,
    };

    db.open();

    // create the adapter using the cursor pointing to the desired data
    //as well as the layout information
    dataAdapter = new SimpleCursorAdapter(
            this, R.layout.notes_table,
            db.getAllItems(),
            columns,
            to,
            0); //flags

    ListView listView = (ListView) findViewById(R.id.my_list_view);
    listView.setAdapter(dataAdapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1,
                                int position_of_of_view_in_adapter, long id_clicked) {

            int request_code = 1;
            // inside inner anonymous class so "ACTIVITY.this" below.
            Intent note_edit_intent = new Intent(NoteView.this, NoteAdd.class);
            note_edit_intent.putExtra("content", GetItem(id_clicked));
            note_edit_intent.putExtra("item_id", id_clicked);
            startActivityForResult(note_edit_intent, request_code);
        }
    });
    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(final AdapterView<?> listView, final View v, int pos, final long  id_clicked ){

                AlertDialog.Builder alert = new AlertDialog.Builder(NoteView.this);
                alert.setTitle(getString(R.string.delete));
                alert.setMessage(getString(R.string.delete_confirm) + pos);
                alert.setNegativeButton(getString(R.string.cancel), null);
                alert.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DelItem(id_clicked);
                        db.open();
                        dataAdapter.changeCursor(db.getAllItems());
                        db.close();
                        dataAdapter.notifyDataSetChanged();
                    }});
                alert.show();
                return true;
            }
        });

    db.close();

}

    public void UpdateItem(long id, String note) {
        db.open();
        HashMap<String,String> hm = new HashMap<String,String>();
        hm.put ("timestamp",timestamp());
        hm.put ("note", note);

        if (db.updateItem(id, hm))
            Toast.makeText(this, getString(R.string.update_successful),
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.update_failed),
                    Toast.LENGTH_LONG).show();
    }

    public void PutItem(String note) {
        db.open();
        //long id =
        HashMap<String,String> hm = new HashMap<String,String>();
        hm.put ("timestamp",timestamp());
        hm.put ("note", note);
        db.insertItem(hm);
        db.close();
    }


    public void DelItem(long id)
    {
        db.open();
        if (db.deleteItem(id))
            Toast.makeText(this, getString(R.string.delete_successful),
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.delete_failed),
                    Toast.LENGTH_LONG).show();
        db.close();
    }


    public String GetItem(long id)
    {
        db.open();
        Cursor c = db.getItem(id);
        if (c.moveToFirst())
            return c.getString(2); // GET 2 column (note)
        else
            return "DB Error?";
    }


    public void ItemsProvider(int... id)
    {
        db.open();
        Cursor c;
        if (id.length == 0) {
            c = db.getAllItems();
        } else {
            c = db.getItem(id[0]);
        }

        if (c.moveToFirst())
        {
            do {
                String item = c.getString(0);
                Toast.makeText(getApplicationContext(), item, Toast.LENGTH_LONG).show();
            } while (c.moveToNext());
        } else {
            Toast.makeText(this, getString(R.string.not_found), Toast.LENGTH_LONG).show();
        }
        db.close();

    }

////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////   DB ITEMS END   ///////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////


}
