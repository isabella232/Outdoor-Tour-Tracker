package de.esri.outdoortourtracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by rsu on 21.11.2017.
 */
public class TrackAdapter extends BaseAdapter {
    private Context context;
    private List<HashMap<String, String>> tracks;
    private LayoutInflater inflater;

    public TrackAdapter(Context context, List<HashMap<String, String>> tracks){
        this.context = context;
        this.tracks = tracks;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Object getItem(int position) {
        return tracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = inflater.inflate(R.layout.track_list_item, null);
        }
        HashMap<String, String> track = tracks.get(position);
        String type = track.get(Const.TYPE);
        ImageView image = (ImageView) view.findViewById(R.id.tour_type);
        if(type.equals(Const.TYPE_HIKE)){
            image.setImageResource(R.drawable.ic_hike);
        }else if(type.equals(Const.TYPE_MOUNTAINEERING)){
            image.setImageResource(R.drawable.ic_mountaineering);
        }else if(type.equals(Const.TYPE_BIKE)){
            image.setImageResource(R.drawable.ic_bike);
        }else if(type.equals(Const.TYPE_RUN)){
            image.setImageResource(R.drawable.ic_run);
        }
        String name = track.get(Const.NAME);
        TextView tourName = (TextView) view.findViewById(R.id.tour_name);
        tourName.setText(name);
        String distance = track.get(Const.DISTANCE);
        TextView tourDistance = (TextView) view.findViewById(R.id.tour_distance);
        tourDistance.setText(distance);
        String duration = track.get(Const.DURATION);
        TextView tourDuration = (TextView) view.findViewById(R.id.tour_time);
        tourDuration.setText(duration);

        return view;
    }
}
