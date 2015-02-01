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

// TODO: i consider security still prealpha feature that needs completely refactoring
// TODO: review localization, backup

public class MainActivity extends Activity {

    Class<?> SelectedActivity = NoteView.class; //default
    final String TAG = "S_main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getPreferences(0).getBoolean("first_run", true)) {
            getPreferences(0).edit().putString("stored_key", "").commit();
            getPreferences(0).edit().putBoolean("allow_no_password", true).commit();
            getPreferences(0).edit().putBoolean("default_backup_to_external", true).commit();
            getPreferences(0).edit().putBoolean("first_run", false).commit();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //menu.findItem(R.id.auto_login).setChecked(getPreferences(0).getBoolean("auto_login", false));
        menu.findItem(R.id.allow_no_password).setChecked(getPreferences(0).getBoolean("allow_no_password", true));
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
//            case R.id.auto_login:
//                return true;
//                if (item.isChecked()) { // disable auto_login
//                    item.setChecked(false);
//                    //getPreferences(0).edit().putString("stored_key", "").commit(); //remove stored pass
//                } else {
//                    if (!getPreferences(0).getString("stored_key", "").isEmpty()){
//                        if (validateLogin(get_password())) {
//                            item.setChecked(true);
//                            //set_password(); // probably dont need to change password just set auto_login with old one.
//                        } else {
//                            Toast.makeText(getApplicationContext(), getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
//                        }
//                        Log.d(TAG, "Password previously was set, checked if same user wants to enable auto_login");
//                    }
//                    Log.d(TAG, "Password was none, so just enable auto_login - this is wrong, should be just allow no password then");
//                }
//                getPreferences(0).edit().putBoolean("auto_login", item.isChecked()).commit();
//                return true;

            case R.id.reset_password:
                getPreferences(0).edit().putString("stored_key", "").commit();
                return true;
            case R.id.set_password:
                setPasswordIfVerified();
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

            case R.id.allow_no_password:
                if (item.isChecked()) {
                    item.setChecked(false);
                    getPreferences(0).edit().putBoolean("secure", true);
                } else {
                    item.setChecked(true);
                    if(!getPreferences(0).getString("stored_key", "").isEmpty()){
                        disableSecureLogin();
                    } else {
                        getPreferences(0).edit().putBoolean("secure", false);
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// LOGIN VERIFICATION ///////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    private Boolean validateLogin(String p) {
        if (p.equals(getPreferences(0).getString("stored_key", "")))
            return true;
        else
            return false;
    }

    // TODO: probably those below could be made into some factory - usable even for other activities
    private void set_password(){
        final EditText input = new EditText(this);

        // if(numeric_only)   input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle(R.string.set_password)
                //.setMessage(message)
                .setView(input)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        try {
                            getPreferences(0).edit().putString("stored_key", value.toString()).commit();
                        } catch (NumberFormatException e) {
                            Toast.makeText(getApplicationContext(), getString(R.string.pass_too_short), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        }).show();
        //return validateLogin(getPreferences(0).getString("stored_key", ""));
    }

//    private String get_password() {
//
//        final EditText input = new EditText(this);
//        // if(numeric_only)   input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
//        new AlertDialog.Builder(this)
//                .setTitle(R.string.get_password)
//                 //.setMessage(message)
//                .setView(input)
//                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                    }
//                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                dialog.dismiss();
//            }
//        }).show();
//        return input.getText().toString();
//    }

    // due to crazy Android asynchronous everything one cannot reuse some functions here... TODO: interface? command pattern? Callable<T> func?
    public void setPasswordIfVerified() {

        final EditText input = new EditText(this);
        // if(numeric_only)   input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle(R.string.get_password)
                        //.setMessage(message)
                .setView(input)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (validateLogin(input.getText().toString())) {
                            set_password();
                            Log.d(TAG, "...validated via pass = and set new password. (Probably - unless user dissmised dialog...)");
                        } else {
                            Log.d(TAG, "...FAILED validating via password = password not changed.");
                            Toast.makeText(getApplicationContext(), getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                Log.d(TAG, "...FAILED validating via password.");
                Toast.makeText(getApplicationContext(), getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
            }
        }).show();
    }

    public void disableSecureLogin(){

        final EditText input = new EditText(this);
        // if(numeric_only)   input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle(R.string.get_password)
                        //.setMessage(message)
                .setView(input)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (validateLogin(input.getText().toString())) {
                            getPreferences(0).edit().putBoolean("secure", false);
                            Log.d(TAG, "...validated via pass = disabled security on.");
                        } else {
                            Log.d(TAG, "...FAILED validating via password = security not disabled.");
                            Toast.makeText(getApplicationContext(), getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                Log.d(TAG, "...FAILED validating via password.");
                Toast.makeText(getApplicationContext(), getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
            }
        }).show();
    }

    public void loginWithPass(){

            final EditText input = new EditText(this);
            // if(numeric_only)   input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.get_password)
                            //.setMessage(message)
                    .setView(input)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (validateLogin(input.getText().toString())) {
                                startActivity(new Intent(getApplicationContext(), SelectedActivity));
                                Log.d(TAG, "...validated via pass = logged on.");
                            } else {
                                Log.d(TAG, "...FAILED validating via password will not login.");
                                Toast.makeText(getApplicationContext(), getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                                Toast.makeText(getApplicationContext(), getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    Log.d(TAG, "...FAILED validating via password.");
                    Toast.makeText(getApplicationContext(), getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
                }
            }).show();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////// BUTTONS HANDLERS /////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    public void runAfterVerification(View view) {

        //EditText passField = (EditText) findViewById(R.id.password);

        if (!getPreferences(0).getBoolean("secure", false)){
            startActivity(new Intent(this, SelectedActivity));
            Log.d(TAG, "Security off = logged on.");
        } else {
//            Log.d(TAG, "Security on = check auto_login settings.");
//            if (getPreferences(0).getBoolean("auto_login", false)) {
//                startActivity(new Intent(this, SelectedActivity));
//                Log.d(TAG, "Security on, login not validated (auto_login on) = logged on.");
//            } else {
                Log.d(TAG, "Security on = auto_login off, verify password...(if password set at all?)");
                loginWithPass();
            }
        }
//    }

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
//            case R.id.activity_todo_selected:
//                if (checked)
//                    SelectedActivity = TodoView.class;
//                break;
//            case R.id.activity_smallitems_selected:
//                if (checked)
//                    SelectedActivity = .class;
//                break;
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

}
