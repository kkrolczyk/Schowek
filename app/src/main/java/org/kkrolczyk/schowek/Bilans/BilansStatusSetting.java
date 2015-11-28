package org.kkrolczyk.schowek.Bilans;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.kkrolczyk.schowek.R;

import java.util.HashSet;
import java.util.Set;

public class BilansStatusSetting extends Activity {

    // TODO: on first run it should show "no accounts defined", later = for now = accounts lists will be held in shared prefs
    Set<String> account_fields;
    //SharedPreferences prefs ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bilans_status_edit_view);
        account_fields = getSharedPreferences("Account_Status_Prefs", 0).getStringSet("account_fields", new HashSet<String>());
        if(account_fields.isEmpty()){  // first time run or all accounts deleted.
        // I will prepopulate it just to maintan backwards compatibility FOR THE TIME BEING
            account_fields.add("gotowka");
            account_fields.add("karta konto 1");
            account_fields.add("karta lunchowa");
            account_fields.add("bankomat");
        }
        populate_accounts_view();
    }

    private void save_accounts_values(){
        TableLayout table = (TableLayout) findViewById(R.id.bilans_status_table);

        for(int i = 0, j = table.getChildCount(); i < j; i++) {
            View view = table.getChildAt(i);
            TableRow row = (TableRow) view;
            String name = ((TextView) row.findViewById(R.id.attrib_name)).getText().toString();
            float value = Float.parseFloat(((TextView) row.findViewById(R.id.attrib_value)).getText().toString());
            //float value = Float.parseFloat(((TextView) table.getTag(R.id.attrib_value)).getText().toString());
            getSharedPreferences("Account_Status_Prefs", 0).edit().putFloat(name, value).commit();
        }
    }

    public void save_bilans_status(View v){
        save_accounts_values();
        this.setResult(RESULT_OK);
        finish();
    }
    public void bilans_add_account(View v){
        EditText e = (EditText)findViewById(R.id.bilans_new_account_name);
        String new_account = e.getText().toString();
        if (new_account != "" && new_account.length()>0 && !account_fields.contains(new_account)) {
            account_fields.add(new_account);
            getSharedPreferences("Account_Status_Prefs", 0).edit().putStringSet("account_fields", account_fields).commit();
            populate_accounts_view(new_account);
            e.setText("");
        }
    }
    public void bilans_del_account(View v){
        String account = ((TextView)((View)v.getParent()).findViewById(R.id.attrib_name)).getText().toString();
        account_fields.remove(account);
        getSharedPreferences("Account_Status_Prefs", 0).edit().putStringSet("account_fields", account_fields).commit();
        populate_accounts_view();
        save_accounts_values();
    }

    //TODO, optimize those two functions. Also make it somewhat accessible to BilansView
    // mayble pass args: TableLayout...table, Context...context ?
    private void populate_accounts_view(){

        TableLayout table = (TableLayout) findViewById(R.id.bilans_status_table);
        // clean table, populate from zero, TODO: optimize?
        table.removeAllViews();
        for(String account_name: account_fields)
        {
            TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.activity_bilans_status_edit_row, null);
            ((TextView)row.findViewById(R.id.attrib_name)).setText(account_name);

            EditText e = (EditText)row.findViewById(R.id.attrib_value);
            e.setText(Float.toString(getSharedPreferences("Account_Status_Prefs", 0).getFloat(account_name, 0.0f)));
            //e.setTag(e); // TODO: is it right use of tags?
            table.addView(row);
        }
        table.requestLayout();
    }
    private void populate_accounts_view(String account_name){

        TableLayout table = (TableLayout) findViewById(R.id.bilans_status_table);
        TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.activity_bilans_status_edit_row, null);
        ((TextView)row.findViewById(R.id.attrib_name)).setText(account_name);
        EditText e = (EditText)row.findViewById(R.id.attrib_value);
        e.setText(Float.toString(getSharedPreferences("Account_Status_Prefs", 0).getFloat(account_name, 0.0f)));
        table.addView(row);
        table.requestLayout();
    }
}
