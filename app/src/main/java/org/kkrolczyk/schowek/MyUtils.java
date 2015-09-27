package org.kkrolczyk.schowek;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class MyUtils {

    private static String TAG = "MyUtils";

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
            data[i] = string.getBytes(Charset.defaultCharset());
        }
        return data;
    }

    public void debug_bundle(Bundle bundle){
    //        Log.e(TAG, "all ?:"+intent.getExtras().keySet());
    //        Log.e(TAG, "all ?:"+intent.getExtras().toString());
    //  great for debugging bundles
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Log.d(TAG, String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }
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


    // TODO: if this is to be generic, enum should be provided by database, from keys(?)...
    public enum sort_order { CREATION_ASC, CREATION_DESC, MODIFICATION_ASC, MODIFICATION_DESC, CONTENT_ASC, CONTENT_DESC };

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
