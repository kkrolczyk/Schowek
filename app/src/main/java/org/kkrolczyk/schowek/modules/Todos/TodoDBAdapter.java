package org.kkrolczyk.schowek.modules.Todos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import org.kkrolczyk.schowek._AbstractDBAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TodoDBAdapter extends _AbstractDBAdapter {

    private static final String TAG = "TodoDBAdapter";

    private static List<TodoConfig> configs;
    private static List<TodoConfig> Configs_preparer()
    {
        configs = new ArrayList<TodoConfig>();
        configs.add(new TodoConfig("entries"));
        configs.add(new TodoConfig("tags"));
        configs.add(new TodoConfig("tag_to_entry_map"));
        return configs;
    }

    public TodoDBAdapter(Context context)
    {
        super(context, Configs_preparer());
    }
    //private TodoDBAdapter(){this.context = null;}; //prevent empty constructor

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public long insertItem(String item)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("item", item);
        long lastrowid = db.insert(configs.get(0).TABLE_NAME, null, initialValues);                 // using autoincrement so no need to worry for wrong id
        insertRelation(lastrowid, 1);                                                               // TODO: think about this - forcing tag "unassigned" to every item (then remove it if actual tags are added?)
        return lastrowid;
    }
    public boolean deleteItem(long rowId)
    {
        return db.delete(configs.get(0).TABLE_NAME, "item_id = " + rowId, null) > 0;
        // TODO: _id should be somehow abstracted away
    }

    public Cursor getAllItems() throws SQLException
    {
        final String MY_QUERY = "SELECT  e.item, group_concat(t.item) as tags       " +
                                "FROM    tag_to_entry_map m                         " +
                                "INNER JOIN entries e                               " +
                                "ON  m.mitem_id=e.item_id                           " +
                                "INNER JOIN tags t                                  " +
                                "ON m.mtag_id=t.tag_id                              " +
                                "GROUP BY e.item OR t.item is NULL";

        Cursor mCursor = db.rawQuery(MY_QUERY, null); //new String[]{String.valueOf(where...)}
        return mCursor.getCount() > 0 ? mCursor : null;
    }

    public Cursor getItem(long rowId) throws SQLException
    {
        Cursor mCursor =
                db.query(true, configs.get(0).TABLE_NAME, configs.get(0).DATABASE_KEYS,
                        "item_id =" + rowId,
                        null,
                        null,
                        null,
                        null,
                        null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor.getCount() > 0 ? mCursor: null;
    }

    public boolean updateItem(long rowId, String item)
    {
        ContentValues args = new ContentValues();
        args.put("item", item);
        return db.update(configs.get(0).TABLE_NAME, args,
                "item_id = " + rowId, null) > 0;
    }

    // probably suboptimal to query for single unique item? + probable SQLITE injection possible by entry
    private long getEntryId(String entry)
    {
        Cursor c = db.query(configs.get(2).TABLE_NAME, new String[]{"item_id"},
                "item = " + "'" + entry + "'",
                null,
                null,
                null,
                null);

        if (c.moveToFirst())
        {
            assert (c.getCount()==1);
            return c.getInt(0);
        } else {
            return -1;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // HELPERS table and its functions.
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void insertTag(String tag)
    {
        ContentValues args = new ContentValues();
        args.put("item", tag);
        try {
            db.insertOrThrow(configs.get(1).TABLE_NAME, null, args);
        } catch (android.database.sqlite.SQLiteConstraintException e) {}
        // skip unique
    }

    public boolean removeTag(String tag){
        return db.delete(configs.get(1).TABLE_NAME, "item = " + tag, null) > 0;
    }

    public ArrayList<String> getTags(Long[] ids_list){
        String where_clause;
        if (ids_list != null && ids_list.length > 0) {
            where_clause = " WHERE tag_id in (" + Arrays.toString(ids_list).substring(1);  // drop '['
            where_clause = where_clause.substring(0, where_clause.length() - 1) + ")";     // drope ']'
        } else {
            where_clause = null;
        }
        ArrayList<String> tags = new ArrayList<String>();
        Cursor c = db.query(configs.get(1).TABLE_NAME, new String[]{"item"},
                where_clause,
                null,
                null,
                null,
                null);
        Log.d(TAG, "getTags:" + configs.get(1).TABLE_NAME);
        if ((c != null) && c.moveToFirst())
        {
            do {
                tags.add(c.getString(0));
            } while (c.moveToNext());
        }
        return tags.size() > 0 ? tags : new ArrayList<String>();
    }
    public ArrayList<String> getTags(){
        return getTags(null);
    }

    // probably suboptimal to query for single unique item?
    private long getTagId(String tag){
        Cursor c = db.query(configs.get(1).TABLE_NAME, new String[]{"tag_id"},
                "item = " + "'" + tag + "'",
                null,
                null,
                null,
                null);

        if (c.moveToFirst())
        {
           assert (c.getCount()==1);
           return c.getInt(0);
        } else {
            return -1;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // HELPERS for mapping items to tags and vice versa

    public void insertRelation(long item_id, long tag_id)
    {
        ContentValues args = new ContentValues();
        args.put("mitem_id",item_id);
        args.put("mtag_id", tag_id);
        db.insert(configs.get(2).TABLE_NAME, null, args);
    }

    public void insertRelation(String entry, String tag)
    {
        insertRelation(getEntryId(entry), getTagId(tag));
    }

    public boolean removeRelation(long item_id, long tag_id)
    {
        return db.delete(configs.get(2).TABLE_NAME, "mtag_id = "
                                                    + tag_id + " AND mitem_id = "
                                                    + item_id, null
                        ) > 0;
    }

    public List<Long> getEntriesForTag(long tag_id){

        List<Long> items = new ArrayList<Long>();
        Cursor c = db.query(configs.get(2).TABLE_NAME, new String[]{"item_id"},
                "tag_id = " + "'" + tag_id + "'",
                null,
                null,
                null,
                null);

        if (c.moveToFirst())
        {
            do {
                items.add(c.getLong(0));
            } while (c.moveToNext());
        }
        return items;
    }
    public  List<Long> getEntriesForTag(String tag){
        return getEntriesForTag(getTagId(tag));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
}
