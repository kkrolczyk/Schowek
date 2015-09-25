package org.kkrolczyk.schowek;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class BackupActivityView extends Activity {

    private String dbname;
    private String TAG = "Backup Acvitivy";
    private enum db_copy_direction { STORE, LOAD };
    private enum db_copy_location { external, internal };
    private final String unset_error = "Backup direction NOT selected - assuming accidental click";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_activity_view);

        Intent intent = getIntent();
        dbname = intent.getStringExtra("dbname");

        ((EditText) findViewById(R.id.backup_calling_activity)).setText(dbname);

        // GET/SET radio from shared properties.
        //getPreferences(0).getString("default_backup_location", "internal")
        //getPreferences(0).edit().putString("default_backup_location", "internal").commit();
        // Should inform user that it saves its value to shared preferences ?
    }
    

    public void Backup_DB(View view) {

        RadioGroup r1 = (RadioGroup) findViewById(R.id.backup_target);
        RadioGroup r2 = (RadioGroup) findViewById(R.id.backup_direction);

        int backup_target = r1.indexOfChild(findViewById(r1.getCheckedRadioButtonId()));
        int backup_direction = r2.indexOfChild(findViewById(r2.getCheckedRadioButtonId()));

        if (backup_direction == -1) {
            Toast.makeText(getBaseContext(), unset_error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, unset_error);
        } else {

            try {
                File source = Environment.getDataDirectory();
                File target = backup_target == db_copy_location.external.ordinal() ? new File("/mnt/extSdCard/") : Environment.getExternalStorageDirectory();

                //if (sd.canWrite()) {
                String currentDBPath = "//data//" + MyUtils.class.getPackage().getName() + "//databases//" + dbname;
                String backupDBPath = dbname;
                File currentDB, backupDB;

                if (backup_direction == db_copy_direction.LOAD.ordinal()) { // TODO: swap dirs == swap(currentDB, backupDB);
                    Log.e(TAG, "will load db from:" + backupDBPath);
                    currentDB = new File(target, backupDBPath);
                    backupDB = new File(source, currentDBPath);
                } else {
                    Log.e(TAG, "will save db to:" + backupDBPath);
                    currentDB = new File(source, currentDBPath);
                    backupDB = new File(target, backupDBPath);
                }

                //if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(), getString(R.string.ok), Toast.LENGTH_SHORT).show();
                //}
            } catch (Exception e) {
                Log.e(TAG, "failed with:" + e.toString());
            }
        }
    }
}
