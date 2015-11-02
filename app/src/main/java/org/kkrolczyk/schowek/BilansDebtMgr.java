package org.kkrolczyk.schowek;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kkrolczyk on 30.10.15.
 */
public class BilansDebtMgr extends Activity {

    // actually, we want to have: person involved in lend, value, direction and optionally date so this probably would be much better to store in db.
    Set<String> debts_serialized;
    EditText who_field;
    EditText howMuch_field;
    ArrayAdapter<String> adapter;
    ArrayList<String> debts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bilans_debts);

        debts_serialized = getSharedPreferences("Account_Status_Prefs", 0).getStringSet("debts", new HashSet<String>());
        debts = new ArrayList<String>();
        debts.addAll(debts_serialized);

        // TODO: no sane way of keeping this in Shared prefs?
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, debts);
        ListView listView = (ListView) findViewById(R.id.previous_debts);
        listView.setAdapter(adapter);
        who_field = (EditText) findViewById(R.id.new_debt_owner);
        howMuch_field = (EditText) findViewById(R.id.new_debt_amount);
    }

    public void add_debt_entry_and_finalize(View view) {
        String who = who_field.getText().toString();
        if ( who_field.length() > 0 && howMuch_field.getText().toString().length() > 0 ) {
            float howMuch = Float.parseFloat(howMuch_field.getText().toString());
            if (view.getTag().equals("out"))
                debts.add("+" + howMuch + " | " + who + " | " + _MyUtils.timestamp());
            else
                debts.add("-" + howMuch + " | " + who + " | " + _MyUtils.timestamp());
            adapter.notifyDataSetChanged();
            // TODO: + on long click delete or strike (cross out) text
            debts_serialized.addAll(debts);
            getSharedPreferences("Account_Status_Prefs", 0).edit().putStringSet("debts", debts_serialized).commit();

            this.setResult(RESULT_OK);
        } else {
            this.setResult(RESULT_CANCELED);
        }
        finish();
    }
}
