package org.kkrolczyk.schowek;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by kkrolczyk on 23.11.14.
 */
public class MyUtils {

    public enum db_copy_direction { STORE, LOAD };
    public static String[] convertToStrings(byte[][] byteStrings) {
        String[] data = new String[byteStrings.length];
        for (int i = 0; i < byteStrings.length; i++) {
            data[i] = new String(byteStrings[i], Charset.defaultCharset());

        }
        return data;
    }

    public static byte[][] convertToBytes(String[] strings) {
        byte[][] data = new byte[strings.length][];
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            data[i] = string.getBytes(Charset.defaultCharset()); // you can chose charset
        }
        return data;
    }


    public static String timestamp(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
    }
    public static String datenow(){
        return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
    }
    public static String timenow(){
        return new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
    }



    public static Boolean Backup_DB(Enum direction, String dbname, boolean target_ext){

        try {
            File source = Environment.getDataDirectory();
            File target = target_ext ? new File("/mnt/extSdCard/") : Environment.getExternalStorageDirectory();

            //if (sd.canWrite()) {
            String currentDBPath = "//data//" + MyUtils.class.getPackage().getName() + "//databases//" + dbname;
            String backupDBPath = dbname;
            File currentDB, backupDB;
            if (direction == db_copy_direction.LOAD) { // TODO: swap dirs == swap(currentDB, backupDB);
                currentDB = new File(target, backupDBPath);
                backupDB = new File(source, currentDBPath);
            } else {
                currentDB = new File(source, currentDBPath);
                backupDB = new File(target, backupDBPath);
            }

            //if (currentDB.exists()) {
            FileChannel src = new FileInputStream(currentDB).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            //}
        } catch (Exception e) {
            Log.e("SCHOWEK", e.toString());
            return false;
        }
        return true;
    }

}
