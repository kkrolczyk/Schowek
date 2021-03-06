package org.kkrolczyk.schowek.modules.Bilans;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.kkrolczyk.schowek.R;
import org.kkrolczyk.schowek.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BilansView extends Activity
{
    final String TAG = "S_BilansView";
    BilansDBAdapter db = new BilansDBAdapter(this);
    SimpleCursorAdapter dataAdapter;
    BilansSharedPrefsHandler status_disp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bilans_view);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        status_disp = BilansSharedPrefsHandler.getInstance(this);

        showAll();
        refreshWealthStatus();
        refreshDebtStatus();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////   MENU   ///////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bilans_view, menu);
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
            case R.id.manage_funds:
                startActivityForResult(new Intent(BilansView.this, BilansStatusSetting.class), 1);
                break;
                                                /*
                                                    TODO: check for possibility to chain/join/combine xml menus
                                                    TODO: manage recurring funds (ie set monthly incomes/payments)
                                                */
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

    public void prepare_intent(int requestCode, long invoking_id){
        Intent intent = new Intent(BilansView.this, BilansAdd.class);

        if (invoking_id > 0) {
            HashMap<String, String> map = GetItem(invoking_id);
            intent.putExtra("item_id", invoking_id);
            String timestamp[] = map.get("data").split(" ");
            intent.putExtra("data", timestamp[0]);
            intent.putExtra("time", timestamp[1]);
            intent.putExtra("tytul", map.get("tytul"));
        } else {
            intent.putExtra("data", Utils.datenow());
            intent.putExtra("time", Utils.timenow());
            intent.putExtra("amount", "");
        }
        startActivityForResult(intent, requestCode);
    }

    public void bilans_add_new(View v){
        prepare_intent(0, -1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        Boolean activity_success = false;
        switch (resultCode) {
            case RESULT_OK:
                activity_success = true;
                break;
            default:
                Toast.makeText(getApplicationContext(),
                        getString(R.string.discarded_new_entry), Toast.LENGTH_LONG).show();
                break; // failed
        }
        if (activity_success)
            switch (requestCode) {
                case 0: // activity responsible for adding/editing bilans entry
                    String timestamp = intent.getStringExtra("date") + " " + intent.getStringExtra("time");
                    int parametry = intent.getIntExtra("parametry", 0);
                    double kwota = intent.getDoubleExtra("shopping_sum", 0.0);
                    String kwota_s = String.valueOf(kwota);
                    String tytul = intent.getStringExtra("category");
                    String szczegoly = intent.getStringExtra("items_serialized");
                    PutItem(timestamp, kwota_s, parametry, tytul, szczegoly);
                    updateSingleAccount(kwota, intent.getStringExtra("konto_nazwa"));
                    break;
                case 1:
                    // activity responsible for setting current status of wallet/account etc has returned. No action needed.
                    break;
                case 2:
                    // activity responsible for setting debt/loans used in view's status
                    Log.d(TAG, "TODO: Probably should handle 'failed' exit status."); //TODO
                    break;
                default:
                    Log.e(TAG, "WRONG REQUEST CODE TO START ACTIVITY FOR RESULT ?");
            }
        else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.empty_or_cancelled), Toast.LENGTH_LONG).show();
        }
        refreshWealthStatus(); // always
        refreshDebtStatus();
    }

    private void updateSingleAccount(double amount, String account_name) {
        // ok nasty hack here. Currently account have no idea which direction money flows.
        // so for now - hardcoded for backwards compatiblility
        if (account_name.equals("bankomat")){
            status_disp.transfer_resources("karta konto 1", "gotowka", (float) amount); // TODO: remove hardcoded account names...crashes if names dont match
        }
        else {
            status_disp.reduce_wealth(account_name, (float) amount);
            Log.e(TAG, account_name + " " + amount);
        }
    }

    private void refreshWealthStatus(){

        // TODO: use BilansStatusSetting class to reduce duplication.
        SharedPreferences prefs = getSharedPreferences("Account_Status_Prefs", 0);
        // todo, push to class item
        Set<String> account_fields = prefs.getStringSet("account_fields", new HashSet<String>());
        // to remember :
        // - getSharedPreferences("Account_Status_Prefs", 0) => "named" prefs
        // - getPreferences(0) => preferences per Activity

        TableLayout table = (TableLayout) findViewById(R.id.bilans_view_statuses_table);
        table.removeAllViews(); // TODO: For now this empties table, and generates rows from start. Optimize.
        for(String name: account_fields)
        {
            TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.activity_bilans_view_status_row, null);
            ((TextView)row.findViewById(R.id.attrib_name)).setText(name);

            TextView e = (TextView)row.findViewById(R.id.attrib_value);
            e.setText(Float.toString(prefs.getFloat(name, 0.0f)));
            //e.setTag(e); // TODO: is it right use of tags?
            table.addView(row);
        }
        table.requestLayout();
    }

    private void refreshDebtStatus() { //TODO?
        SharedPreferences prefs = getSharedPreferences("Account_Status_Prefs", 0);

        TextView give = (TextView)findViewById(R.id.bilans_status_to_return_value);
        TextView get = (TextView)findViewById(R.id.bilans_status_to_collect_value);
        give.setText(Float.toString(prefs.getFloat("bilans_status_to_return_value", 0.0f)));
        get.setText(Float.toString(prefs.getFloat("bilans_status_to_collect_value", 0.0f)));
    }

    public void startDebtsManager(View v){
        startActivityForResult(new Intent(BilansView.this, BilansDebtMgr.class), 2);
    }


////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////   DELEGATE WORK END   ///////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////   DB ITEMS   ///////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    public void showAll()
    {
        String[] columns = new String[] {  // The desired columns to be bound
                "data",
                "tytul",
                "kasa",
                "szczegoly",
                //"parametry"
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[] {
                R.id.data,
                R.id.tytul,
                R.id.kwota,
                R.id.szczegoly,
        };

        db.open();

        // create the adapter using the cursor pointing to the desired data
        // as well as the layout information
        dataAdapter = new SimpleCursorAdapter(
                this, R.layout.activity_bilans_add_listview,
                db.getAllItems(),
                columns,
                to,
                0); //flags

        ListView listView = (ListView) findViewById(R.id.bilans_list_view);
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position_of_of_view_in_adapter, long id_clicked) {

                prepare_intent(0, id_clicked);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(final AdapterView<?> listView, final View v, int pos, final long id_clicked) {

                AlertDialog.Builder alert = new AlertDialog.Builder(BilansView.this);
                alert.setTitle(getString(R.string.delete_question));
                alert.setMessage(getString(R.string.delete_confirm) + pos);
                alert.setNegativeButton(getString(R.string.cancel), null);
                alert.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DelItem(id_clicked);
                        db.open();
                        dataAdapter.changeCursor(db.getAllItems());
                        db.close();
                        dataAdapter.notifyDataSetChanged();
                        refreshWealthStatus();
                    }
                });
                alert.show();
                return true;
            }
        });

        db.close();
        refreshWealthStatus();
    }


    public void UpdateItem(long id, String timestamp, String kasa,
                           int parametry, String tytul, String szczegoly)
    {
        db.open();

        if (db.updateItem(id, timestamp, kasa, parametry, tytul, szczegoly))
            Toast.makeText(this, getString(R.string.update_successful),
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.update_failed),
                    Toast.LENGTH_LONG).show();
    }

    public void PutItem(String timestamp, String kasa, int parametry, String tytul, String szczegoly)
    {
        db.open();
        //Log.d(TAG," " + timestamp + " " + kasa + " " + parametry + " " + tytul + " " + szczegoly);
        db.insertItem(timestamp, kasa, parametry, tytul, szczegoly);
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

    public HashMap<String, String> GetItem(long id)
    {
        db.open();
        Cursor c = db.getItem(id);
        HashMap<String, String> map = new HashMap<String, String>();
        for(int i=0; i<c.getColumnCount();i++)
        {
            map.put(c.getColumnName(i), c.getString(i));
        }
        return map;
        // c.moveToFirst() is assured by DBAdapter
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
