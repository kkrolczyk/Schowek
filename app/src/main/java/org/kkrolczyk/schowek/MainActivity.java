package org.kkrolczyk.schowek;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.auto_login).setChecked(getPreferences(0).getBoolean("auto_login", false));
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
            case R.id.auto_login:
                if (item.isChecked()) item.setChecked(false);
                else { if (set_password(true)) item.setChecked(true); }
                getPreferences(0).edit().putBoolean("auto_login", item.isChecked()).commit();
                return true;

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
            case R.id.action_settings: //noinspection SimplifiableIfStatement
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }







    private Boolean set_password(Boolean num_only){
        final EditText input = new EditText(this);
        if (num_only)
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle(R.string.enter_password)
                //.setMessage(message)
                .setView(input)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        getPreferences(0).edit().putInt("stored_key", Integer.parseInt(value.toString())).commit();
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        }).show();
        return true;
    }


    public void validateLogin(View view) {

        Boolean allow_login = false; // TODO:super duper security...
        int i;
        if (!getPreferences(0).getBoolean("auto_login", false)) {
            i = getPreferences(0).getInt("stored_key", -546841323);
            EditText passField = (EditText) findViewById(R.id.password);
            if (Integer.parseInt(passField.getText().toString()) == i){
                allow_login = true;
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
            }
        } else {
            allow_login = true;
        }

        if (allow_login) {
            Intent intent = new Intent(this, NoteView.class);
            startActivity(intent); //intent.putExtra(EXTRA_MESSAGE, message);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.not_logged), Toast.LENGTH_LONG).show();
        }
    }


}
