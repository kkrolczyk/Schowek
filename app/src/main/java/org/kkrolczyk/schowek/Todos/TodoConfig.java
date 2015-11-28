package org.kkrolczyk.schowek.Todos;

import android.util.Pair;

import org.kkrolczyk.schowek._AbstractConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TodoConfig extends _AbstractConfig {

    public static final String DBASE_NAME = "todo.db";

    private static final List<Pair<String, String>> entries;
    static
    {
        entries = new ArrayList<Pair<String, String>>();
        entries.add(new Pair("item_id", "INTEGER PRIMARY KEY autoincrement"));
        entries.add(new Pair("item", "TEXT"));
    }

    private static final List<Pair<String, String>> tags;
    static
    {
        tags = new ArrayList<Pair<String, String>>();
        tags.add(new Pair("tag_id", "INTEGER PRIMARY KEY autoincrement"));
        tags.add(new Pair("item", "TEXT UNIQUE"));
    }

    private static final List<Pair<String, String>> tag_to_entry_map;
    static
    {
        tag_to_entry_map = new ArrayList<Pair<String, String>>();
        tag_to_entry_map.add(new Pair("rel_ids", "INTEGER PRIMARY KEY autoincrement"));  // NO IDEA IF I EVER NEED THIS FIELD. but according to SO "some people prefer surrogate keys, even on many-to-many tables. And some ORMs do not support compound PKs."
        // NO IDEA IF I NEED TIMESTAMPS AS WELL
        tag_to_entry_map.add(new Pair("mtag_id", "INTEGER"));
        tag_to_entry_map.add(new Pair("mitem_id", "INTEGER"));
        tag_to_entry_map.add(new Pair("FOREIGN KEY(mitem_id) REFERENCES entries(item_id)" , ""));
        tag_to_entry_map.add(new Pair("FOREIGN KEY(mtag_id) REFERENCES tags(tag_id)" , ""));
    }

    private static final Object which[] = {entries, tags, tag_to_entry_map};

    public TodoConfig(String table) {
        super(DBASE_NAME, table, myHelper(table));
        this.tb_preconfiguration = "INSERT INTO tags(item) VALUES (\"not set\")";
    }

    private static List<Pair<String, String>> myHelper(String choose_me) {
        List<String> map = Arrays.asList("entries", "tags", "tag_to_entry_map");
        return (List<Pair<String, String>>) which[map.indexOf(choose_me)];
    };

}
