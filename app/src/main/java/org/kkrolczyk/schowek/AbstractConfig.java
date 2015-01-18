package org.kkrolczyk.schowek;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kkrolczyk on 15.01.15.
 */
public abstract class AbstractConfig {

    public String TABLE_DROP;
    public String TABLE_CREATE;
    protected String DBASE_NAME; // virtual - filled by subclasses

    //public List<String> DATABASE_KEYS;    // preffered but,
    public String [] DATABASE_KEYS;         // Sqlite api uses this

    public AbstractConfig(String DBASE_NAME, String TABLE_NAME, List<Pair<String, String>> configuration) {

        TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";  // todo: check this.TABLE_DROP = X VS TABLE_DROP = X
        this.DATABASE_KEYS = new String[configuration.size()];    // actually oracle docs say that you cannot instantiate abstract class...

        List<String> temp = new ArrayList<String>(configuration.size());
        for( Pair<String, String> entry : configuration ){
            temp.add(entry.first);
        }
        System.arraycopy(temp.toArray(), 0, this.DATABASE_KEYS, 0, temp.size());

        StringBuilder sb = new StringBuilder(DATABASE_KEYS.length);
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append(TABLE_NAME);
        sb.append(" (");
        for(
        Pair<String, String> entry : configuration){
            sb.append(entry.first + ' ' + entry.second + ',');
        }
        sb.deleteCharAt(sb.length()-1); // remove last ','
        sb.append(" );");
        this.TABLE_CREATE = sb.toString();
    }
}
