package org.kkrolczyk.schowek;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by kkrolczyk on 23.11.14.
 */
public class MyUtils {
    private static String TAG = MyUtils.class.getSimpleName();
    //private Context mContext;
    //public MyUtils(Context context){ mContext=context; };


    public enum db_copy_direction { STORE, LOAD };
    public enum sort_order { CREATION_ASC, CREATION_DESC, MODIFICATION_ASC, MODIFICATION_DESC, CONTENT_ASC, CONTENT_DESC };


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
            Log.e(TAG, "Backup_DB:"+e.toString());
            return false;
        }
        return true;
    }

    private static String [] getSortNames(final Context ctx){
        int [] sort_name_ids  = { R.string.creation_asc, R.string.creation_desc, R.string.modification_asc, R.string.modification_desc, R.string.content_asc, R.string.content_desc };
        String [] names = new String[sort_name_ids.length];
        for(int i = 0;i<sort_name_ids.length;++i){
            names[i] = ctx.getString(sort_name_ids[i]);
        }
        return names;
    }

    public static void set_sort_order(final Context ctx, final SortOrderCallback sortOrderCallback){

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(ctx.getString(R.string.sort_order))
               .setSingleChoiceItems(new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, getSortNames(ctx)), sort_order.CREATION_ASC.ordinal(),
                       new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               //selected_sort[0] = sort_order.values()[which];
                               ctx.getSharedPreferences(ctx.getPackageName(), 0).edit().putInt("sort_order", sort_order.values()[which].ordinal()).commit();
                               ctx.getSharedPreferences("Prefs", 0).edit().putInt("sort_order", sort_order.values()[which].ordinal()).commit();
                               ctx.getApplicationContext().getSharedPreferences(ctx.getPackageName(), 0).edit().putInt("sort_order", sort_order.values()[which].ordinal()).commit();
                               if (sortOrderCallback != null){
                                   sortOrderCallback.callback();
                               }
                               dialog.dismiss();
                           }
                       });

        builder.show();
    }

    interface SortOrderCallback{
        void callback();
    }

}
