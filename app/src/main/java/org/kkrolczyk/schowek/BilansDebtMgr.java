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
    EditText whoField;
    EditText howMuchField;
    ArrayAdapter<String> adapter;
    ArrayList<String> debts;
    private float toGet;
    private float toGive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bilans_debts);

        debts_serialized = getSharedPreferences("Account_Status_Prefs", 0).getStringSet("debts", new HashSet<String>());
        debts = new ArrayList<String>();
        debts.addAll(debts_serialized);

        toGet = getSharedPreferences("Account_Status_Prefs", 0).getFloat("bilans_status_to_return_value", 0.0f);
        toGive = getSharedPreferences("Account_Status_Prefs", 0).getFloat("bilans_status_to_collect_value", 0.0f);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, debts);
        ListView listView = (ListView) findViewById(R.id.previous_debts);
        listView.setAdapter(adapter);
        whoField = (EditText) findViewById(R.id.new_debt_owner);
        howMuchField = (EditText) findViewById(R.id.new_debt_amount);
    }

    public void add_debt_entry_and_finalize(View view) {
        String who = whoField.getText().toString();
        if ( whoField.length() > 0 && howMuchField.getText().toString().length() > 0 ) {
            float howMuch = Float.parseFloat(howMuchField.getText().toString());
            if (view.getTag().equals("out")){
                debts.add("+" + howMuch + ", " + who + " (" + _MyUtils.timestamp() + ")");
                toGet += howMuch;
                getSharedPreferences("Account_Status_Prefs", 0).edit().putFloat("bilans_status_to_collect_value", toGet).commit();
            } else {
                debts.add("-" + howMuch + ", " + who + " (" + _MyUtils.timestamp() + ")");
                toGive += howMuch;
                getSharedPreferences("Account_Status_Prefs", 0).edit().putFloat("bilans_status_to_return_value", toGive).commit();
            }
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
