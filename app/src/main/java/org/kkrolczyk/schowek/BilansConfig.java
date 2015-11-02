package org.kkrolczyk.schowek;

import android.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BilansConfig extends _AbstractConfig {

    public static final String DBASE_NAME = "bilans.db";

    private static final List<Pair<String, String>> bilans;
    static
    {
        bilans = new ArrayList<Pair<String, String>>();
        bilans.add(new Pair("_id", "INTEGER PRIMARY KEY autoincrement"));
        bilans.add(new Pair("data", "TIMESTAMP"));
        bilans.add(new Pair("kasa", "REAL"));
        bilans.add(new Pair("parametry", "INTEGER"));
        bilans.add(new Pair("tytul", "TEXT"));
        bilans.add(new Pair("szczegoly", "TEXT"));
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

    private static final Object which[] = {bilans, items_in_categories, categories};

    public BilansConfig(String table) {
        super(DBASE_NAME, table, myHelper(table));
    }

    private static List<Pair<String, String>> myHelper(String choose_me) {
        List<String> map = Arrays.asList("bilans", "items_in_categories", "categories");
        return (List<Pair<String, String>>) which[map.indexOf(choose_me)];
    };

}
