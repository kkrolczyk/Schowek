package org.kkrolczyk.schowek;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {

    Class<?> SelectedActivity = NoteView.class; //default
    final String TAG = "S_main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getPreferences(0).getBoolean("first_run", true)) {
            getPreferences(0).edit().putString("default_backup_location", "internal").commit();
            getPreferences(0).edit().putBoolean("first_run", false).commit();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //menu.findItem(R.id.auto_login).setChecked(getPreferences(0).getBoolean("auto_login", false));
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Locale locale; Configuration config;
        switch (item.getItemId()) {
            case R.id.language_pl:
                locale = new Locale("pl_PL");
                Locale.setDefault(locale);
                config = new Configuration();
                config.locale = locale;
                getBaseContext().getApplicationContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                return true;
            case R.id.language_en:
                // TODO: reload application after locale change?
                locale = new Locale("en_EN");
                Locale.setDefault(locale);
                config = new Configuration();
                config.locale = locale;
                getBaseContext().getApplicationContext().getResources().updateConfiguration(config, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////// BUTTONS HANDLERS /////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    public void runAfterVerification(View view) {

	// Security was terrible thus removed
	startActivity(new Intent(this, SelectedActivity));
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    public void updateSelectedActivity(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.activity_note_selected:
                if (checked)
                    SelectedActivity = NoteView.class;
                break;
            case R.id.activity_bilans_selected:
                if (checked)
                    SelectedActivity = BilansView.class;
                break;
            case R.id.activity_todo_selected:
                if (checked)
                    SelectedActivity = TodoView.class;
                break;
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

}
