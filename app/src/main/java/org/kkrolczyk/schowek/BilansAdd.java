package org.kkrolczyk.schowek;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BilansAdd extends Activity {

    Calendar calendar;
    EditText data_field;
    EditText time_field;
    Spinner categories_holder;
    ListView shopping_items_for_category_lv;
    BilansDBAdapter db = new BilansDBAdapter(this);
    BilansCustomArrayAdapter current_shopping_list_adapter;
    ArrayAdapter<String> categories_list_adapter;
    List<List<String>> current_shopping_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);             // TODO: save state+populate on update too.
        setContentView(R.layout.activity_bilans_add);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        calendar = Calendar.getInstance();

        data_field = ((EditText) findViewById(R.id.bilans_date));
        time_field = ((EditText) findViewById(R.id.bilans_time));
        categories_holder = ((Spinner) findViewById(R.id.coarse_desc_bilans));
        shopping_items_for_category_lv = ((ListView) findViewById(R.id.detailed_desc_bilans));

        // populate from intent in case we want to edit entry
        Intent intent = getIntent();
        data_field.setText(intent.getStringExtra("data"));
        time_field.setText(intent.getStringExtra("time"));
        populate_categories(intent.getStringExtra("title"));
        // TODO: should populate "old" current_shopping_list as well, and their selected counts

        populate_items_for_category(intent.getStringExtra("title"));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void bilans_finalize_this_shopping_list(View v){

        if (current_shopping_list != null) {
            Intent intent = this.getIntent();
            intent.putExtra("date", data_field.getText().toString());
            intent.putExtra("time", time_field.getText().toString());

            // for all selected items in this category, need to find out
            // a) sum of all (value * amount)
            // b) serialization to string of all items: name amount value

            String category = (String) categories_holder.getSelectedItem();
            intent.putExtra("category", category);
            intent.putExtra("shopping_sum", current_shopping_list_adapter.getSumOfElements());
            intent.putExtra("items_serialized", current_shopping_list_adapter.getSerializedElements());

            RadioGroup temporary = (RadioGroup) findViewById(R.id.bilans_selected_method);
            intent.putExtra("parametry", temporary.indexOfChild(temporary.findViewById(temporary.getCheckedRadioButtonId())));

            this.setResult(RESULT_OK, intent);
            finish();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void populate_categories(String... input){

        db.open();
        List<String> categories = db.getCategories();
        db.close();

        categories_list_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,categories);
        categories_list_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories_holder.setAdapter(categories_list_adapter);

        // set spinner to new selection if any given in arguments
        if (input != null && input.length > 0)
            categories_holder.setSelection(categories_list_adapter.getPosition(input[0]));

        // set onclick listener (reload items in listview when spinner element is changed)
        categories_holder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = (String) categories_holder.getSelectedItem();
                populate_items_for_category(category);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void populate_items_for_category(String category){

        if (category != null){
            db.open();
            current_shopping_list = db.getCategoryItems(category);
            db.close();

            current_shopping_list_adapter = new BilansCustomArrayAdapter(
                    BilansAdd.this,
                    R.layout.activity_bilans_add_single_row,
                    current_shopping_list);

            shopping_items_for_category_lv.setAdapter(current_shopping_list_adapter);

            // TODO: click - long click delete/edit item ? In case of temporary price change?
            
            // TODO: check - this is probably code that never runs
            shopping_items_for_category_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    String str = (String) shopping_items_for_category_lv.getItemAtPosition(position);
                    Toast.makeText(getBaseContext(), "item was clicked " + str, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void add_category(View view){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String category = input.getText().toString();
                if (category.length()>0) {
                    db.open();
                    db.insertCategory(category);
                    db.close();
                    populate_categories(category);
                    populate_items_for_category(category);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void add_item_to_category (View view){
        Boolean wantToAddToDB = true;
        namedItemWithValueDialog(wantToAddToDB);
    }

    public void add_item_to_shopping_list_one_time(View view){
        Boolean wantToAddToDB = false;
        namedItemWithValueDialog(wantToAddToDB);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void bilans_category_item_minus(View view){

        int pos = Integer.parseInt(view.getTag().toString());
        List<String> item = (List<String>) shopping_items_for_category_lv.getItemAtPosition(pos);
        int current_amount = Integer.parseInt(item.get(2));
        if (current_amount > 0){
            item.set(2, String.valueOf(current_amount-1));
            current_shopping_list.set(pos, item);
        }
        current_shopping_list_adapter.notifyDataSetChanged();
    }

    public void bilans_category_item_plus(View view){

        int pos = Integer.parseInt(view.getTag().toString());
        List<String> item = (List<String>) shopping_items_for_category_lv.getItemAtPosition(pos);
        int current_amount = Integer.parseInt(item.get(2));
        item.set(2, String.valueOf(current_amount+1));
        current_shopping_list.set(pos, item);
        current_shopping_list_adapter.notifyDataSetChanged();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void namedItemWithValueDialog(final Boolean addToDB) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // Set an EditText view to get user input
        final EditText name = new EditText(this);
        //name.setHint("item name");
        final EditText price = new EditText(this);
        price.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //price.setHint("item price");
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name);
        layout.addView(price);
        alert.setView(layout);
        final String category;
        if (categories_holder.getChildCount()>0) {
            category = categories_holder.getSelectedItem().toString();
        } else {
            category = "_TEMPORARY_";
        }

        alert.setMessage(category);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String itemName = name.getText().toString();
                float itemValue = Float.parseFloat(price.getText().toString());

                if (addToDB) {
                    if (name.getText().length() > 0 && price.getText().length() > 0) {
                        db.open();
                        db.insertItemIntoCategory(category,
                                itemName,
                                itemValue
                        );
                        db.close();
                        populate_items_for_category(category);
                    }
                } else {
                    populate_items_for_category(category);
                    ArrayList<String> temp = new ArrayList<String>();
                    temp.add(itemName);
                    temp.add(price.getText().toString());
                    temp.add("1");
                    current_shopping_list_adapter.add(temp);
                    current_shopping_list_adapter.notifyDataSetChanged();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void runDatePicker(View view){

        // register callback
        DatePickerDialog.OnDateSetListener mDateSetListener =
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month,
                                          int day) {

                        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        data_field.setText(date.format(new Date(year-1900,month,day)));
                        // TODO: depreciated java.date - convert to Calendar.
                    }
                };
        // show dialog
        DatePickerDialog dialog = new DatePickerDialog(this, mDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();

    }
    public void runTimePicker(View view){

        // register callback
        TimePickerDialog.OnTimeSetListener mTimeSetListener =
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minutes) {

                        SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        time_field.setText(time.format( new Time(hour, minutes, calendar.get(Calendar.SECOND)) )); // TODO: depreciated sqlite.time. Nice replacement ?
                    }
                };
        // show dialog
        TimePickerDialog dialog = new TimePickerDialog(this, mTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        dialog.show();
        }
}
