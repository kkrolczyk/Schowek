package org.kkrolczyk.schowek.modules.Todos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.kkrolczyk.schowek.R;

import java.util.ArrayList;

public class TodoView extends Activity
{
    final String TAG = "S_TodoView";

    protected TodoDBAdapter db = new TodoDBAdapter(this);
    private TodoCustomArrayAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_view);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showAll();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////   MENU   ///////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notes_view, menu);
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
            case R.id.manage_backup:
                db.Backup();
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

    public void prepare_intent(int request_code, long invoking_id)
    {
        Intent intent = new Intent(TodoView.this, TodoAdd.class);
        if (invoking_id > 0) {
            intent.putExtra("item_id", invoking_id);
            intent.putExtra("content", GetItem(invoking_id));                       // get item from main table
            intent.putExtra("assigned_tags", db.getTags(new Long[]{invoking_id}));  // pass an array of associated tags from table 2
        } else {
            intent.putExtra("content", "");
            intent.putExtra("assigned_tags", new ArrayList<String>());
        }
        db.open();
        intent.putExtra("all_tags", db.getTags());
        db.close();
        startActivityForResult(intent, request_code);
    }

    public void tags_add_new(View v){
        Log.e(TAG, "Not implemented");
        db.open();
        db.insertTag(this.toString());
        db.close();
        //prepare_intent(0, -1);
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
                    long update_id = data.getLongExtra("item_id", -1);
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

    public void showAll()
    {
        db.open();
        Cursor c = db.getAllItems(); // first item has items, second aggregated tags related to id
        db.close();

        ArrayList<Pair<String, String>> dataAggregate = new ArrayList<Pair<String, String>>();
        if (c != null && c.moveToFirst())
        {
            do {
                Pair<String, String> dataContainer = new Pair<String, String>(c.getString(0), c.getString(1));
                dataAggregate.add(dataContainer);
            } while (c.moveToNext());
        } else {
            Log.e(TAG, "null pointer - cursor from database (show all)");
        }

        dataAdapter = new TodoCustomArrayAdapter(this, R.layout.activity_todo_view_listview, dataAggregate);

        ListView listView = (ListView) findViewById(R.id.todo_list_view);
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position_of_of_view_in_adapter, long id_clicked) {

                prepare_intent(1, id_clicked);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(final AdapterView<?> listView, final View v, int pos, final long id_clicked) {

                AlertDialog.Builder alert = new AlertDialog.Builder(TodoView.this);
                alert.setTitle(getString(R.string.delete_question));
                alert.setMessage(getString(R.string.delete_confirm) + pos);
                alert.setNegativeButton(getString(R.string.cancel), null);
                alert.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DelItem(id_clicked);
                        db.open();
                        //dataAdapter.changeCursor(db.getAllItems());
                        db.close();
                        dataAdapter.notifyDataSetChanged();
                    }
                });
                alert.show();
                return true;
            }
        });

        db.close();
    }

    public void UpdateItem(long id, String item) {
        db.open();
        if (db.updateItem(id, item))
            Toast.makeText(this, getString(R.string.update_successful),
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.update_failed),
                    Toast.LENGTH_LONG).show();
    }

    public void PutItem(String item) {
        db.open();
        db.insertItem( item );
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

