package org.kkrolczyk.schowek;

import android.util.Pair;
import java.util.ArrayList;
import java.util.List;

public abstract class _AbstractConfig {

    public String TABLE_DROP;
    public String TABLE_CREATE;
    protected String tb_preconfiguration;   // virtual - filled by subclasses
    protected String TABLE_NAME;            // virtual - filled by subclasses
    protected String DBASE_NAME;            // virtual - filled by subclasses
    public String [] DATABASE_KEYS;         // Sqlite api requires String[]

    public _AbstractConfig(String DBASE_NAME, String TABLE_NAME,
                           List<Pair<String, String>> configuration) {

        this.TABLE_NAME = TABLE_NAME;
        this.DBASE_NAME = DBASE_NAME;

        this.DATABASE_KEYS = new String[configuration.size()];
        List<String> temp = new ArrayList<String>(configuration.size());
        for( Pair<String, String> entry : configuration ){
            temp.add(entry.first);
        }
        System.arraycopy(temp.toArray(), 0, this.DATABASE_KEYS, 0, temp.size());

        StringBuilder sb = new StringBuilder(DATABASE_KEYS.length);
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append(TABLE_NAME);
        sb.append(" (");
        for(Pair<String, String> entry : configuration){
            sb.append(entry.first + ' ' + entry.second + ',');
        }
        sb.deleteCharAt(sb.length()-1); // remove last ','
        sb.append(" );");
        this.TABLE_CREATE = sb.toString();
        this.TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    }
}
