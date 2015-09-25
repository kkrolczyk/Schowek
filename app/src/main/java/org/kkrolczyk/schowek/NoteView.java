package org.kkrolczyk.schowek;

// todo: szukaj notki
// todo: filtruj tagi
// todo: opcja sortuj wg daty a nie id

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class NoteView extends Activity
{
    final String TAG = "S_NoteView";
    float FontSize = 12.0f;

    NoteDBAdapter db = new NoteDBAdapter(this);
    SimpleCursorAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
        showAll();
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshViewAndDataAdapter();
    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        Log.i(TAG,"DB on RE start"); //after pause and after activity for result has returned...
//    }


////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////// handle volume keys as font size changer //////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////


    private void SizeChanger(){

        ListView listView = (ListView) findViewById(R.id.my_list_view);
        for (int i = 0; i <= listView.getCount(); i++) {
            if (listView.getChildAt(i) != null) {
                ViewGroup ll = (ViewGroup) listView.getChildAt(i);
                for (int j = 0; j< ll.getChildCount(); ++j) {
                    ((TextView) ll.getChildAt(j)).setTextSize(FontSize);
                }
            }
        }
        /* listview with 'wrap_content' is unable to properly resize itself. */
        //listView.getParent().recomputeViewAttributes(listView);
        //refreshViewAndDataAdapter();


    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0)
                    return true;
                FontSize += 1;
                SizeChanger();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0)
                    return true;
                FontSize -= 1;
                SizeChanger();
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

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
                refreshViewAndDataAdapter();
                break;
            case R.id.copy_to_ext_SD:
                db.BackupDB(MyUtils.db_copy_direction.STORE, getPreferences(0).getBoolean("default_backup_to_external", true));
                break;
            case R.id.copy_from_ext_SD:
                db.BackupDB(MyUtils.db_copy_direction.LOAD, getPreferences(0).getBoolean("default_backup_to_external", true));
                break;
            case R.id.default_backup_to_ext:
                getPreferences(0).edit().putBoolean("default_backup_to_external", !getPreferences(0).getBoolean("default_backup_to_external", true)).commit();
                //Log.d(TAG," BACKUPS TO ext? "+getPreferences(0).getBoolean("default_backup_to_external", true) );
                break;
            case R.id.sort_order:
                MyUtils.set_sort_order(this);
                db.open();
                dataAdapter.changeCursor(db.getAllItems(getSharedPreferences(this.getPackageName(),0).getInt("sort_order", MyUtils.sort_order.CREATION_ASC.ordinal())));
                db.close();
                break;
            default:
                Log.e (TAG, "MENU = WTF?");
        }
        return super.onOptionsItemSelected(item);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////   MENU END   ///////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////  DELEGATE WORK   ///////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    public void prepare_intent(int request_code, long invoking_id){
        Intent intent = new Intent(NoteView.this, NoteAdd.class);
        if (invoking_id > 0) {
            intent.putExtra("item_id", invoking_id);
            intent.putExtra("content", GetItem(invoking_id));
        } else {
            intent.putExtra("content", "");
        }
        intent.putExtra("fontsize", FontSize);
        startActivityForResult(intent, request_code);
    }

    public void note_add_new(View v){
        prepare_intent(0, -1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Boolean activity_success = false;
        switch (resultCode) {
            case -1: //success
                activity_success = data.getStringExtra("content").length() > 0;
                break;
            default:
                break;// failed
        }
        if (activity_success)
            switch (requestCode) {
                case 0:
                    PutItem(data.getStringExtra("content"));
                    break;
                case 1:
                    long update_id = data.getLongExtra("item_id",-1);
                    if (update_id > 0)
                        UpdateItem(update_id, data.getStringExtra("content"));
                    else
                        Log.e(TAG, "WRONG item id?");
                    break;
                default:
                    Log.e(TAG, "WRONG REQUEST CODE TO START ACTIVITY FOR RESULT ?");
            }
        else
            Toast.makeText(getApplicationContext(),
                    getString(R.string.empty_or_cancelled), Toast.LENGTH_LONG).show();

    }

////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////   DELEGATE WORK END   ///////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////   DB ITEMS   ///////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

public void showAll() {


    String[] columns = new String[]{  // The desired columns to be bound
            "timestamp",
            "note",
    };

    // the XML defined views which the data will be bound to
    int[] to = new int[]{
            R.id.note_field_1,
            R.id.note_field_2,
    };

    db.open();

    // create the adapter using the cursor pointing to the desired data
    //as well as the layout information
    dataAdapter = new SimpleCursorAdapter(
            this, R.layout.activity_note_view_listview,
            db.getAllItems(),
            columns,
            to,
            0); //flags

    ListView listView = (ListView) findViewById(R.id.my_list_view);
    listView.setAdapter(dataAdapter);
    // update view to display current amount of notes
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1,
                                int position_of_of_view_in_adapter, long id_clicked) {

            prepare_intent(1, id_clicked);
//            int request_code = 1;
//            // inside inner anonymous class so "ACTIVITY.this" below.
//            Intent note_edit_intent = new Intent(NoteView.this, NoteAdd.class);
//            note_edit_intent.putExtra("content", GetItem(id_clicked));
//            note_edit_intent.putExtra("item_id", id_clicked);
//            startActivityForResult(note_edit_intent, request_code);
        }
    });
    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(final AdapterView<?> listView, final View v, int pos, final long id_clicked) {

            AlertDialog.Builder alert = new AlertDialog.Builder(NoteView.this);
            alert.setTitle(getString(R.string.delete_question));
            alert.setMessage(getString(R.string.delete_confirm) + pos);
            alert.setNegativeButton(getString(R.string.cancel), null);
            alert.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    DelItem(id_clicked);
                    refreshViewAndDataAdapter();
                }
            });
            alert.show();
            return true;
        }
    });

    db.close();
    recountActionBar();
}

    private void refreshViewAndDataAdapter(){
        db.open();
        dataAdapter.changeCursor(db.getAllItems());
        db.close();
        dataAdapter.notifyDataSetChanged();
        recountActionBar();
    }
    private void recountActionBar() {
        this.getActionBar().setTitle(getString(R.string.add) + ", " + getString(R.string.right_now) + dataAdapter.getCount());
    }




    public void UpdateItem(long id, String note) {
        db.open();
        if (db.updateItem(id, MyUtils.timestamp(), note))
            Toast.makeText(this, getString(R.string.update_successful),
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.update_failed),
                    Toast.LENGTH_LONG).show();
    }

    public void PutItem(String note) {
        db.open();
        //HashMap<String,String> hm = new HashMap<String,String>();
        //hm.put ("timestamp",MyUtils.timestamp());
        //hm.put ("note", note);
        db.insertItem( MyUtils.timestamp(), note );
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
