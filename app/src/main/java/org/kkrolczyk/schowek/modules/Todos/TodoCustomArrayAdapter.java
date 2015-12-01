package org.kkrolczyk.schowek.modules.Todos;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.kkrolczyk.schowek.R;

import java.util.List;

public class TodoCustomArrayAdapter extends ArrayAdapter<Pair<String, String>> implements Filterable {

    private static class ViewHolder {
        // https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView - does this even work ? :)
        // view lookup cache stored in TAG
        // Check if an existing view is being reused, otherwise inflate the view
        TextView name;
        TextView values;
    }
    private List<Pair<String, String>> todo_items;
    ViewHolder viewHolder;
    public TodoCustomArrayAdapter(Context ctx, int xml_layout, List<Pair<String, String>> texts) {
        super(ctx, R.layout.activity_todo_view_listview, texts);
        todo_items = texts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        Pair<String, String> txt =  getItem(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.activity_todo_view_listview, parent, false);
            // Populate the data into the template view using the data object
            ((TextView) convertView.findViewById(R.id.todo_item_view)).setText(txt.first);
            ((TextView) convertView.findViewById(R.id.todo_tags_per_item_view)).setText(txt.second.toString());
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public Filter getFilter()
    {

        Filter filter = new Filter() {

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count == 0) {
                    notifyDataSetInvalidated();
                } else {
                    todo_items = (List<Pair<String, String>>) results.values;
                    notifyDataSetChanged();
                }
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
		// TODO: fix-me, see previous commits
                return results;
            }
        };
        return filter;
    }
}

