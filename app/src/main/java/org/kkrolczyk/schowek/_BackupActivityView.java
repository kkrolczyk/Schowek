package org.kkrolczyk.schowek;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

public class _BackupActivityView extends Activity {

    private String dbname;
    private String dbpath;
    private String TAG = "Backup Acvitivy";
    private enum db_copy_direction { STORE, LOAD };
    private enum db_copy_location { external, internal };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_activity_view);

        Intent intent = getIntent();
        dbname = intent.getStringExtra("dbname");
        dbpath = intent.getStringExtra("dbpath");
        ((EditText) findViewById(R.id.backup_calling_activity)).setText(dbname);

        // set selected backup:target/source based on saved shared prefs
        // String b_location = getPreferences(0).getString("default_backup_location", "internal")
        // RadioGroup r1 = (RadioGroup) findViewById(R.id.backup_target);
        // r1. set selected (b_location == "internal" ? 0:1);
        // in runBackupOrRestore update shared prefs:
        //  getPreferences(0).edit().putString("default_backup_location", b_location).commit();
    }
    
    public void runBackupOrRestore(View view) {

        RadioGroup r1 = (RadioGroup) findViewById(R.id.backup_target);
        RadioGroup r2 = (RadioGroup) findViewById(R.id.backup_direction);

        int backup_target = r1.indexOfChild(findViewById(r1.getCheckedRadioButtonId()));
        int backup_direction = r2.indexOfChild(findViewById(r2.getCheckedRadioButtonId()));

        if (backup_direction == -1) {
            Toast.makeText(getBaseContext(), getString(R.string.backup_not_set), Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.backup_not_set));
        } else {

            try {
                File dataDir = Environment.getDataDirectory();
                File cardDir = backup_target == db_copy_location.external.ordinal() ? new File("/mnt/extSdCard/") : Environment.getExternalStorageDirectory();

                // optionally: getBaseContext().getDatabasePath(dbname).getPath();
                // appDBpath="//data//"+_MyUtils.class.getPackage().getName()+"//databases//"+dbname;
                String appDBpath = dbpath;
                String bakBDpath = dbname;
                File source_fp, target_fp;
                if (backup_direction == db_copy_direction.LOAD.ordinal()) {
                    source_fp = new File(cardDir, bakBDpath);
                    target_fp = new File(dataDir, appDBpath);
                } else {
                    source_fp = new File(dataDir, appDBpath);
                    target_fp = new File(cardDir, bakBDpath);
                }

                FileChannel src = new FileInputStream(source_fp).getChannel();
                FileChannel dst = new FileOutputStream(target_fp).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(), getString(R.string.ok), Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.e(TAG, "failed with:" + e.toString());
            }
        }
    }
}
