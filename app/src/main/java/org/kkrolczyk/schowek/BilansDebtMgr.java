package org.kkrolczyk.schowek;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kkrolczyk on 30.10.15.
 */
public class BilansDebtMgr extends Activity {

    // actually, we want to have: person involved in lend, value, direction and optionally date so this probably would be much better to store in db.

    EditText whoField;
    EditText howMuchField;
    EditText desciptField;
    ArrayList<String> current_debts_view;
    ArrayAdapter<String> adapter;
    Set<String> saved_debts;
    BilansSharedPrefsHandler handler;



    @Override
    protected void onResume() {
        super.onResume();
        check_proper_strike_thru();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)  {
        if(hasFocus){
            check_proper_strike_thru();
        }
    }


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bilans_debts);

        handler = BilansSharedPrefsHandler.getInstance(this);
        SharedPreferences prefs = getSharedPreferences("Account_Status_Prefs", 0);
        saved_debts = prefs.getStringSet("debts", new HashSet<String>());
        current_debts_view = new ArrayList<String>();
        current_debts_view.addAll(saved_debts);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, current_debts_view);
        final ListView listView = (ListView) findViewById(R.id.previous_debts);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> listView, final View v, int pos, final long id) {
                remove_debt_entry(listView.getItemAtPosition(pos).toString());
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toggle_debt_active_state((TextView) view, parent.getItemAtPosition(position).toString());
            }
        });

        whoField = (EditText) findViewById(R.id.new_debt_owner);
        howMuchField = (EditText) findViewById(R.id.new_debt_amount);
        desciptField = (EditText) findViewById(R.id.new_debt_descr);
    }

    public void remove_debt_entry(String entry) {

        handler.updateCumulativeIndicators(entry, false);
        current_debts_view.remove(entry);
        adapter.notifyDataSetChanged();

        update_saved_debts();
    }

    public void add_debt_entry_and_finalize(View view) {

        String who = whoField.getText().toString();
        String desc = desciptField.getText().toString();
        if (desc.length()>0) { desc = " [" + desc + "]"; }
        if ( whoField.length() > 0 && howMuchField.getText().toString().length() > 0 ) {

            float howMuch = Float.parseFloat(howMuchField.getText().toString());

            if (view.getTag().equals("out")){
                current_debts_view.add("+" + howMuch + ", " + who + desc + "\n(" + _MyUtils.timestamp() + ")");
                handler.increase_wealth("bilans_status_to_collect_value", howMuch);
            } else {
                current_debts_view.add("-" + howMuch + ", " + who + desc + "\n(" + _MyUtils.timestamp() + ")");
                handler.increase_wealth("bilans_status_to_return_value", howMuch);
            }
            adapter.notifyDataSetChanged();
            update_saved_debts();
            this.setResult(RESULT_OK);
        } else {
            this.setResult(RESULT_CANCELED);
        }
        finish();
    }
    public void display_only_unchecked(View view){ check_proper_strike_thru(); }
    private void check_proper_strike_thru(){
        ListView listView = (ListView) findViewById(R.id.previous_debts);
        for (int i = 0; i < listView.getAdapter().getCount(); i++) {
            Object v = listView.getItemAtPosition(i);
            if (v != null && v.toString().substring(0,1).equals("#")) {
                if( ((CheckBox) findViewById(R.id.display_completed)).isChecked() ) {
                    TextView tv = (TextView) listView.getAdapter().getView(i, listView.getChildAt(i), listView);
                    tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    //tv.requestLayout();
                } else {
                    adapter.remove(v.toString());
                    //adapter.notifyDataSetChanged(); // TODO: actually restore them back on second click.
                }
            }
        }

//        listView.requestLayout();
//        listView.getParent().recomputeViewAttributes(listView);
    }

    private void update_saved_debts() {  // Todo: optimize.

        saved_debts.clear();
        saved_debts.addAll(current_debts_view);
        getSharedPreferences("Account_Status_Prefs", 0).edit().putStringSet("debts", saved_debts).commit();
    }

    private boolean change_strike_thru_state(TextView view){
        view.setPaintFlags(view.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
        return (view.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) == 0;
    }

    private void toggle_debt_active_state(TextView view, String text) { // TODO: optimize

        boolean isCrossedOut = change_strike_thru_state(view);
        String sign;
        int textToModify = current_debts_view.indexOf(text);

        if (isCrossedOut) {
            current_debts_view.add(textToModify, text.substring(1));
            current_debts_view.remove(text);
            sign = text.substring(1, 2);
        } else {
            current_debts_view.add(textToModify, "#" + text);
            current_debts_view.remove(text);
            sign = text.substring(0, 1);
        }
        sign = sign.equals("+") ? "-" : "+";
        handler.updateCumulativeIndicators(sign + text.substring(1), isCrossedOut);
        update_saved_debts();
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_debts_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.clean_all:
                handler.clear_debts();
                current_debts_view.clear();
                adapter.notifyDataSetChanged();
                //_MyUtils.copy_files_in_dirs(new File(getFilesDir(), "../shared_prefs"), new File("/sdcard/test"));
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}
