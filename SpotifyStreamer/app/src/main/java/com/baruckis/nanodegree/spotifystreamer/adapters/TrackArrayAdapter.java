package com.baruckis.nanodegree.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baruckis.nanodegree.spotifystreamer.R;
import com.baruckis.nanodegree.spotifystreamer.models.CustomTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Andrius-Baruckis on 2015-07-10.
 * http://www.baruckis.com/
 */

public class TrackArrayAdapter extends ArrayAdapter<CustomTrack> {

    private static class ViewHolder {
        ImageView imageView;
        TextView trackNameTextView;
        TextView albumNameTextView;
    }

    public TrackArrayAdapter(Context context, ArrayList<CustomTrack> tracks) {
        super(context, R.layout.list_item_track, tracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CustomTrack track = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_track, parent, false);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
            viewHolder.trackNameTextView = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.albumNameTextView = (TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Picasso.with(getContext()).load(track.getAlbumImageUrl()).into(viewHolder.imageView);
        viewHolder.trackNameTextView.setText(track.getTrackName());
        viewHolder.albumNameTextView.setText(track.getAlbumName());

        return convertView;
    }
}
