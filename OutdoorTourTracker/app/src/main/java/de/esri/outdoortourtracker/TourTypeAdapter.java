package de.esri.outdoortourtracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *
 */
public class TourTypeAdapter extends BaseAdapter {
    private Context context;
    private int[] typeIcons;
    private String[] typeNames;
    private LayoutInflater inflater;

    public TourTypeAdapter(Context context, int[] typeIcons, String[] typeNames){
        this.context = context;
        this.typeIcons = typeIcons;
        this.typeNames = typeNames;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return typeNames.length;
    }

    @Override
    public Object getItem(int position) {
        return typeNames[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.tour_type_item, null);
        ImageView image = (ImageView) view.findViewById(R.id.image_tour_type);
        TextView name = (TextView) view.findViewById(R.id.text_tour_type);
        image.setImageResource(typeIcons[position]);
        name.setText(typeNames[position]);
        return view;
    }
}
