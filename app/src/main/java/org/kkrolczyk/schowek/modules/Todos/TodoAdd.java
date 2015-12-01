package org.kkrolczyk.schowek.modules.Todos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.kkrolczyk.schowek.R;

import java.util.ArrayList;

public class TodoAdd extends Activity {

    final static String TAG = "TodoAdd";
    EditText textField;
    ListView available_tags_view;
    ListView assigned_tags_view;
    ArrayList<String> all_tags;
    ArrayList<String> assigned_tags;
    ArrayAdapter all_tags_adapter;
    ArrayAdapter assigned_tags_adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_add);

        Intent intent = getIntent();
        String message = intent.getStringExtra("content");
        all_tags = intent.getStringArrayListExtra("all_tags");

        textField = ((EditText) findViewById(R.id.display_or_update_entry_field));
        textField.setText(message);
        textField.setSelection(message.length());

        available_tags_view = ((ListView) findViewById(R.id.todo_add_available_tags_listview));
        assigned_tags_view = ((ListView) findViewById(R.id.todo_add_assigned_tags_listview));

        // Log.e(TAG, "what is passed? "+ intent.getStringArrayListExtra("all_tags").toString());
        populate_list_with_data(available_tags_view, all_tags_adapter, all_tags);
        assigned_tags = intent.getStringArrayListExtra("assigned_tags");
        populate_list_with_data(assigned_tags_view, assigned_tags_adapter, assigned_tags);
    }

    public void add_new_tag(View v)
    {
        String new_tag = ((EditText) findViewById(R.id.add_new_tag_field)).getText().toString();
        if (new_tag != null){
            // Dont add tag instantly to DB - add it to assigned tags, that will be returned to caller
            if (assigned_tags != null) {
                assigned_tags.add(new_tag);
                // assigned_tags_adapter.notifyDataSetChanged();
            }
        } else {
            Log.w(TAG,"add_new_tag() No tag has been entered!");
        }
    }

    public void save_clicked(View v)
    {
        Intent intent = this.getIntent();
        intent.putExtra("content", textField.getText().toString());
        intent.putExtra("assigned_tags", assigned_tags); //can contain newly added
        this.setResult(RESULT_OK, intent);
        finish();
    }


    public void cancel_clicked(View v)
    {
        Intent intent = this.getIntent();
        this.setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void populate_list_with_data(ListView listView_ref, ArrayAdapter<String> adapter_ref, ArrayList<String> tags)
    {
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
    }
}
