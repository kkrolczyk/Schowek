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
import java.util.Arrays;

public class _BackupActivityView extends Activity {

    private String dbname;
    private String dbpath;
    private String TAG = "Backup Acvitivy";
    private enum db_copy_direction { STORE, LOAD };
    private enum db_copy_location { INTERNAL, EXTERNAL };

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

    private String search_for_external_dir(){
        File file; String extPath = "";
        for( String path : Arrays.asList("ext_card", "external_sd", "ext_sd", "external", "extSdCard", "externalSdCard", "sdcard1"))
        {
            for( String root: new String[]{"/mnt/","/storage/", "/sdcard/"}){
                file = new File(root, path);
                if( file.isDirectory() && file.canWrite() ) {
                    extPath = file.getAbsolutePath();
                    return extPath;
                }
            }
        }
        return "";
    }

    public void runBackupOrRestore(View view) {

        RadioGroup r1 = (RadioGroup) findViewById(R.id.backup_target);
        RadioGroup r2 = (RadioGroup) findViewById(R.id.backup_direction);

        int backup_target = r1.indexOfChild(findViewById(r1.getCheckedRadioButtonId())); // TODO: NO!
        int backup_direction = r2.indexOfChild(findViewById(r2.getCheckedRadioButtonId())); // TODO: NO!

        if (backup_direction == -1) {
            Toast.makeText(getBaseContext(), getString(R.string.backup_not_set), Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.backup_not_set));
        } else {

           // try {
                String extPath = search_for_external_dir();
                // usually 3 types of memory: 2x internal, 1xRemovable.
                File fileLocations[] = {Environment.getDataDirectory(), Environment.getExternalStorageDirectory(), new File(extPath) }; // "data","sdcard","extSD"
                String curDBlocations[] = { dbpath,
                                            dbpath + "//" + dbname,
                                            getBaseContext().getDatabasePath(dbname).getPath(),
                                            "//data//"+_MyUtils.class.getPackage().getName()+"//databases//" };

                String backupDir;
                if (backup_target == db_copy_location.EXTERNAL.ordinal()){ // TODO: NO!
                    backupDir = new File(fileLocations[2], dbname).getAbsolutePath();
                } else {
                    backupDir = new File(fileLocations[1], dbname).getAbsolutePath();
                }

                String appDBpath = null;
                for (String s: curDBlocations){
                    File path = new File(s);
                    if(path.isFile()){
                        appDBpath = path.getAbsolutePath();
                    }
                }
                if (null == appDBpath)
                    Toast.makeText(getBaseContext(), getString(R.string.backup_db_not_found), Toast.LENGTH_SHORT).show();
                // throw new Exception(getString(R.string.backup_db_not_found);

                File source_fp, target_fp;
                if (backup_direction == db_copy_direction.LOAD.ordinal()) {
                    source_fp = new File(backupDir);
                    target_fp = new File(appDBpath);
                } else {
                    source_fp = new File(appDBpath);
                    target_fp = new File(backupDir);
                }
                if(_MyUtils.copy_files(source_fp, target_fp))
                    Toast.makeText(getBaseContext(), getString(R.string.ok), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getBaseContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();

           // } catch (Exception e) {
           //     Log.e(TAG, "failed with:" + e.toString());
           // }
        }
    }
}
