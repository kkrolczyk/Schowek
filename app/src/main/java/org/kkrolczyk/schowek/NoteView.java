package org.kkrolczyk.schowek;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class NoteView extends Activity {

    final String TAG = "NoteView";
    float fontSize = 12.0f;

    NoteDBAdapter db = new NoteDBAdapter(this);
    SimpleCursorAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
        EditText etext=(EditText) findViewById(R.id.edit_text_for_filter);
        etext.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void afterTextChanged(Editable s) {
                if (s.length()>0)
                    dataAdapter.getFilter().filter(s.toString());
            }
        });
        showAll();
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshViewAndDataAdapter();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////// handle volume keys as font size changer //////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////


    private void SizeChanger() {

        ListView listView = (ListView) findViewById(R.id.note_list_view);
        for (int i = 0; i <= listView.getCount(); i++) {
            if (listView.getChildAt(i) != null) {
                ViewGroup ll = (ViewGroup) listView.getChildAt(i);
                for (int j = 0; j< ll.getChildCount(); ++j) {
                    ((TextView) ll.getChildAt(j)).setTextSize(fontSize);
                }
            }
        }
        /* listview with 'wrap_content' is unable to properly resize itself. Changed to match_parent
           // listView.getParent().recomputeViewAttributes(listView);
           // refreshViewAndDataAdapter();
        */
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        Boolean isRepeated = event.getRepeatCount() == 0;
        Boolean isPressed = event.getAction() == KeyEvent.ACTION_DOWN;

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (isPressed && isRepeated) return true;
                fontSize += 1;
                SizeChanger();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (isPressed && isRepeated) return true;
                fontSize -= 1;
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
                refreshViewAndDataAdapter();
                break;
            case R.id.manage_backup:
                db.Backup();
                break;
            case R.id.sort_order:
                _MyUtils.setSortOrder(this,
                        new _MyUtils.SortOrderCallback() {
                            @Override
                            public void callback() {
                                refreshViewAndDataAdapter();
                            }
                        }
                );
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

    public void prepareIntent(int requestCode)
    {
        Intent intent = new Intent(NoteView.this, NoteAdd.class);
        intent.putExtra("content", "");
        intent.putExtra("fontSize", fontSize);
        if( ((CheckBox) findViewById(R.id.want_encryption)).isChecked() )
            intent.putExtra(getString(R.string.content_encrypted), true);
        startActivityForResult(intent, requestCode);
    }

    public void prepareIntent(int requestCode, long invokingId)
    {
        Intent intent = new Intent(NoteView.this, NoteAdd.class);
        intent.putExtra("noteId", invokingId);
        intent.putExtra("content", GetItem(invokingId));
        intent.putExtra("fontSize", fontSize);
        if( ((CheckBox) findViewById(R.id.want_encryption)).isChecked() )
            intent.putExtra(getString(R.string.content_encrypted), true);
        startActivityForResult(intent, requestCode);
    }

    public void noteAddNew(View v)
    {
        prepareIntent(0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Boolean activitySuccess = false;
        switch (resultCode) {
            case RESULT_OK:
                activitySuccess = data.getStringExtra("content").length() > 0;
                break;
            default:
                break;// failed
        }
        if (activitySuccess)
            switch (requestCode) {
                case 0:
                    PutItem(data.getStringExtra("content"));
                    break;
                case 1:
                    long noteId = data.getLongExtra("noteId", -1);
                    if (noteId > 0)
                        UpdateItem(noteId, data.getStringExtra("content"));
                    else
                        Log.e(TAG, "WRONG edit noteId");
                    break;
                default:
                    Log.e(TAG, "WRONG REQUEST CODE TO START ACTIVITY FOR RESULT");
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
        String[] columnsNames = new String[]{
                "timestamp",
                "note",
        };
        int[] columnsMapedTo = new int[]{
                R.id.note_field_1,
                R.id.note_field_2,
        };

        db.open();
        dataAdapter = new SimpleCursorAdapter(
                this, R.layout.activity_note_view_listview,
                db.getAllItems(),
                columnsNames,
                columnsMapedTo,
                0); //flags

        ListView listView = (ListView) findViewById(R.id.note_list_view);
        listView.setAdapter(dataAdapter);
        // update view to display current amount of notes
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position_of_of_view_in_adapter, long id_clicked) {
                prepareIntent(1, id_clicked);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(final AdapterView<?> listView, final View v, int pos, final long noteId) {

                AlertDialog.Builder alert = new AlertDialog.Builder(NoteView.this);
                alert.setTitle(getString(R.string.delete_question));
                alert.setMessage(getString(R.string.delete_confirm) + pos);
                alert.setNegativeButton(getString(R.string.cancel), null);
                alert.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DelItem(noteId);
                        refreshViewAndDataAdapter();
                    }
                });
                alert.show();
                return true;
            }
        });

        dataAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                db.open();
                return db.getAllItemsLike(constraint.toString());
            }
            });
        db.close();
        recountActionBar();
    }

    public void refreshViewAndDataAdapter()
    {
        db.open();
        int defSortOrder = _MyUtils.SORT_ORDER.CREATION_ASC.ordinal();
        dataAdapter.changeCursor(db.getAllItems(getSharedPreferences(this.getPackageName(), 0).getInt("sort_order", defSortOrder)));
        db.close();
        dataAdapter.notifyDataSetChanged();
        recountActionBar();
    }

    private void recountActionBar()
    {
        String countDescription = getString(R.string.add) + ", " + getString(R.string.right_now) + dataAdapter.getCount();
        this.getActionBar().setTitle(countDescription);
    }

    public void UpdateItem(long noteId, String note)
    {
        db.open();
        if (db.updateItem(noteId, _MyUtils.timestamp(), note))
            Toast.makeText(this, getString(R.string.update_successful),
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.update_failed),
                    Toast.LENGTH_LONG).show();
    }

    public void PutItem(String note)
    {
        db.open();
        db.insertItem( _MyUtils.timestamp(), note );
        db.close();
    }

    public void DelItem(long noteId)
    {
        db.open();
        if (db.deleteItem(noteId))
            Toast.makeText(this, getString(R.string.delete_successful),
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.delete_failed),
                    Toast.LENGTH_LONG).show();
        db.close();
    }

    public String GetItem(long noteId)
    {
        db.open();
        Cursor c = db.getItem(noteId);
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
