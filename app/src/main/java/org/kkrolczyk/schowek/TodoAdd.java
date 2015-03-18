package org.kkrolczyk.schowek;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class TodoAdd extends Activity {

    final static String TAG = "TodoAdderTest";
    EditText textField;
    ListView available_tags_view;
    ListView assigned_tags_view;
    ArrayList<String> all_tags;
    ArrayList<String> assigned_tags;
    ArrayAdapter all_tags_adapter;
    ArrayAdapter assigned_tags_adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_add);

        Intent intent = getIntent();
        String message = intent.getStringExtra("content");

        textField = ((EditText) findViewById(R.id.display_or_update_entry_field));
        textField.setTextSize(intent.getFloatExtra("fontsize", 10));
        textField.setText(message);
        textField.setSelection(message.length());

        available_tags_view = ((ListView) findViewById(R.id.todo_add_available_tags_listview));
        assigned_tags_view = ((ListView) findViewById(R.id.todo_add_assigned_tags_listview));


        all_tags = intent.getStringArrayListExtra("all_tags");
        Log.e(TAG, "what is passed? "+ intent.getStringArrayListExtra("all_tags").toString());
        populate_list_with_data(available_tags_view, all_tags_adapter, all_tags);
        assigned_tags = intent.getStringArrayListExtra("assigned_tags");
        populate_list_with_data(assigned_tags_view, assigned_tags_adapter, assigned_tags);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_db_view_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    public void add_new_tag(View v) {

        String new_tag = ((EditText) findViewById(R.id.add_new_tag_field)).getText().toString();
        if (new_tag != null){
//            // Dont add tag instantly to DB - add it to assigned tags, that will be returned to caller
            if (assigned_tags != null) {
                assigned_tags.add(new_tag);
                //assigned_tags_adapter.notifyDataSetChanged();
            }
        } else {
            Log.w(TAG,"add_new_tag() No tag has been entered!");
        }
    }

    public void save_clicked(View v) {

        Intent intent = this.getIntent();
        intent.putExtra("content", textField.getText().toString());
        intent.putExtra("assigned_tags", assigned_tags); //can contain newly added
        this.setResult(RESULT_OK, intent);
        finish();
    }


    public void cancel_clicked(View v) {

        Intent intent = this.getIntent();
        this.setResult(RESULT_CANCELED, intent);
        finish();
    }


//    private void serialize_listview() {
//        List<Contact> contacts =
//                (List<Contact>) savedInstanceState.getSerializable("" + R.id.contactList);
//        ContactListAdapter adapter =
//                new ContactListAdapter((Context) this, android.R.layout.simple_list_item_1, contacts);
//        contactList.setAdapter(adapter);
//    }

    private void populate_list_with_data(ListView listView_ref, ArrayAdapter<String> adapter_ref, ArrayList<String> tags){

//        db.open();
//        Cursor c = db.getAllItems(); // first item has items, second aggregated tags related to id
//        db.close();
//        ArrayList<Pair<String, String>> dataAggregate = new ArrayList<Pair<String, String>>();
//        if (c != null && c.moveToFirst()) {
//            do {
//                Pair<String, String> dataContainer = new Pair<String, String>(c.getString(0), c.getString(1));
//                dataAggregate.add(dataContainer);
//            } while (c.moveToNext());
//        } else {
//            Log.e(TAG, "null pointer - cursor from database (show all)");
//        }

        adapter_ref = new ArrayAdapter(this, android.R.layout.simple_list_item_1, tags);
        listView_ref.setAdapter(adapter_ref);
        listView_ref.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position_of_of_view_in_adapter, long id_clicked) {

                    Log.e(TAG, "clicked:" + id_clicked);
                    //swap_items_in_lists(1, id_clicked);
                }
            });


//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//
//            @Override
//            public boolean onItemLongClick(final AdapterView<?> listView, final View v, int pos, final long id_clicked) {
//
//                AlertDialog.Builder alert = new AlertDialog.Builder(TodoView.this);
//                alert.setTitle(getString(R.string.delete_question));
//                alert.setMessage(getString(R.string.delete_confirm) + pos);
//                alert.setNegativeButton(getString(R.string.cancel), null);
//                alert.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        DelItem(id_clicked);
//                        db.open();
//                        //dataAdapter.changeCursor(db.getAllItems());
//                        db.close();
//                        dataAdapter.notifyDataSetChanged();
//                    }
//                });
//                alert.show();
//                return true;
//            }
//        });

    }


}
