package org.kkrolczyk.schowek.modules.Notes;

import android.util.Pair;

import org.kkrolczyk.schowek._AbstractConfig;

import java.util.ArrayList;
import java.util.List;

public class NoteConfig extends _AbstractConfig {

    public static final String DBASE_NAME = "notki.db";
    public static final String TABLE_NAME = "notatki";
    private static final List<Pair<String, String>> configuration;
    static
    {
        configuration = new ArrayList<Pair<String, String>>();
        configuration.add(new Pair("_id", "INTEGER PRIMARY KEY autoincrement"));
        configuration.add(new Pair("timestamp", "TIMESTAMP"));
        configuration.add(new Pair("note", "TEXT"));
    }

    NoteConfig(){ super(DBASE_NAME, TABLE_NAME, configuration); };

}
