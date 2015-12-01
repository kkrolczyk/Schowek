package org.kkrolczyk.schowek.modules.Bilans;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;

/**
 * Created by kkrolczyk on 25.11.15.
 */
public class BilansSharedPrefsHandler {

    Context ctx;
    SharedPreferences prefs ;
    private float toGet;
    private float toGive;
    static BilansSharedPrefsHandler self;

    public static BilansSharedPrefsHandler getInstance(Context c){ if (null != self) return self; else return new BilansSharedPrefsHandler(c); }
    BilansSharedPrefsHandler(Context c){

        ctx = c;
        prefs = c.getSharedPreferences("Account_Status_Prefs", 0);
        toGet = prefs.getFloat("bilans_status_to_return_value", 0.0f);
        toGive = prefs.getFloat("bilans_status_to_collect_value", 0.0f);
    }

    public void reduce_wealth(String source, float amount){
        float value = prefs.getFloat(source, 0.0f);
        prefs.edit().putFloat(source, value - amount).commit();
    }
    public void increase_wealth(String source, float amount) {
        float value = prefs.getFloat(source, 0.0f);
        prefs.edit().putFloat(source, value + amount).commit();
    }

    public void transfer_resources(String source, String target, float amount){
        reduce_wealth(source, amount);
        increase_wealth(target, amount);
    }

    public void clear_debts(){
        prefs.edit().putStringSet("debts", new HashSet<String>()).apply();
        prefs.edit().putFloat("bilans_status_to_collect_value", 0f).apply();
        prefs.edit().putFloat("bilans_status_to_return_value", 0f).apply();
        prefs.edit().commit();
    }

    public void updateCumulativeIndicators(String text, boolean state) {  // TODO: optimize

        float value = Float.parseFloat(text.substring(1, text.indexOf(',')));
        if (state) {
            if (text.startsWith("#")) {} // SKIP, "disabled"
            if (text.startsWith("-")) {
                // BORROW UNCROSSED
                increase_wealth("bilans_status_to_collect_value", value);
                //toGive += Float.parseFloat(text.substring(1, text.indexOf(',')));
                //prefs.edit().putFloat("bilans_status_to_return_value", toGive).commit();
            } else {
                // LEND UNCROSSED
                increase_wealth("bilans_status_to_return_value", value);
            }
        } else {
            if (text.startsWith("#")) {} // SKIP, "disabled"
            if (text.startsWith("-")) {
                // BORROW CROSSED OUT
                reduce_wealth("bilans_status_to_collect_value", value);
                //toGive -= Float.parseFloat(text.substring(1, text.indexOf(',')));
                //prefs.edit().putFloat("bilans_status_to_return_value", toGive).commit();
            } else {
                // LEND CROSSED OUT
                reduce_wealth("bilans_status_to_return_value", value);
            }
        }
    }
}
