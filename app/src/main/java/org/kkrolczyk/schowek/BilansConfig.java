package org.kkrolczyk.schowek;

import android.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kkrolczyk on 15.01.15.
 */
public class BilansConfig extends AbstractConfig {

    public static final String DBASE_NAME = "bilans.db";
    public String TABLE_NAME;

    private static final List<Pair<String, String>> bilans;
    static
    {
        bilans = new ArrayList<Pair<String, String>>();
        bilans.add(new Pair("_id", "INTEGER PRIMARY KEY autoincrement"));
        bilans.add(new Pair("data", "TIMESTAMP"));
        bilans.add(new Pair("kasa", "REAL"));                // FloatField peewee maps to what?
        bilans.add(new Pair("parametry", "INTEGER"));        // 0, 1, 2, 3, 4 (0-gotowka, 1-karta,2-bankomat,3-lunch, 4-dbg) - wstepnie
        bilans.add(new Pair("tytul", "TEXT"));               // CharField peewee maps to what?
        bilans.add(new Pair("szczegoly", "TEXT"));           // TextField peewee maps to what?
    }

    private static final List<Pair<String, String>> items_in_categories;
    static
    {
        items_in_categories = new ArrayList<Pair<String, String>>();
        items_in_categories.add(new Pair("_id", "INTEGER PRIMARY KEY autoincrement"));
        items_in_categories.add(new Pair("category", "TEXT"));
        items_in_categories.add(new Pair("datetime", "TIMESTAMP"));
        items_in_categories.add(new Pair("item", "TEXT"));
        items_in_categories.add(new Pair("value", "REAL"));
    }
    private static final List<Pair<String, String>> categories;
    static {
        categories = new ArrayList<Pair<String, String>>();
        categories.add(new Pair("category", "TEXT UNIQUE"));
    }


    // TODO: seems ugly
    private static final Object which[] = {bilans, items_in_categories, categories};
//    static enum Option {
//        bilans, items_in_categories, categories
//    }

    public BilansConfig(String table) {
        super(DBASE_NAME, table, myHelper(table));
        super.DBASE_NAME = DBASE_NAME;
        TABLE_NAME = table;
    }

    private static List<Pair<String, String>> myHelper(String choose_me) {
        //List<String> map = ["bilans", "items_in_categories", "categories"]; // nice try
        List<String> map = Arrays.asList("bilans", "items_in_categories", "categories");
        return (List<Pair<String, String>>) which[map.indexOf(choose_me)];
    };

}
