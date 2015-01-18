package org.kkrolczyk.schowek;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kkrolczyk on 02.01.15.
 */


public class BilansCustomArrayAdapter extends ArrayAdapter<List<String>> {

    private static class ViewHolder {
        // https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView - does this even work ? :)
        // view lookup cache stored in TAG
        // Check if an existing view is being reused, otherwise inflate the view
        TextView name;
        TextView value;
        TextView bilans_row_current_amount;
    }
    ViewHolder viewHolder;
    public BilansCustomArrayAdapter(Context ctx, int xml_layout, List<List<String>> texts ) {
        super(ctx, R.layout.bilans_item_add_row, texts);
    }


    public Double getSumOfElements(){
        Double sum = 0.0;
        for(int i=0 ; i < this.getCount() ; i++){
            double price = Double.parseDouble(this.getItem(i).get(1));
            int amount   = Integer.parseInt(this.getItem(i).get(2));
            sum += amount*price;
        }
        return sum;
    }
    public String getSerializedElements(){
        StringBuilder items_details = new StringBuilder();
        for(int i=0 ; i < this.getCount() ; i++){
            //double price = Double.parseDouble(this.getItem(i).get(1));
            //int amount   = Integer.parseInt(this.getItem(i).get(2));
            if (! this.getItem(i).get(2).equals("0") ) {
                items_details.append(this.getItem(i).get(0));
                items_details.append(" ");
                items_details.append(this.getItem(i).get(2));
                items_details.append("x");
                items_details.append(this.getItem(i).get(1));
                items_details.append(" ");
            }
        }
        return items_details.toString();
    };

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        List<String> txt =  getItem(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.bilans_item_add_row, parent, false);
            // Populate the data into the template view using the data object
            ((TextView) convertView.findViewById(R.id.bilans_row_item)).setText(txt.get(0));
            ((TextView) convertView.findViewById(R.id.bilans_row_value)).setText(txt.get(1));
            ((TextView) convertView.findViewById(R.id.bilans_row_current_amount)).setText(txt.get(2));
            convertView.setTag(viewHolder);
            // set tags for '+' and '-' buttons
            convertView.findViewById(R.id.bilans_row_item_plus).setTag(position);
            convertView.findViewById(R.id.bilans_row_item_minus).setTag(position);
        } else {
            // ALWAYS update current amount TODO: why notifyDataSetChange didn't fire ? idk.
            ((TextView) convertView.findViewById(R.id.bilans_row_current_amount)).setText(txt.get(2));
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Return the completed view to render on screen
        return convertView;
    }
}

