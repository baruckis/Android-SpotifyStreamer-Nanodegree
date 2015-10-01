package com.baruckis.nanodegree.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baruckis.nanodegree.spotifystreamer.models.CustomArtist;
import com.baruckis.nanodegree.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Andrius-Baruckis on 2015.
 * http://www.baruckis.com/
 */

public class ArtistArrayAdapter extends ArrayAdapter<CustomArtist> {

    private static class ViewHolder {
        ImageView imageView;
        TextView nameTextView;
    }

    /*
     * Constructors
     * */
    public ArtistArrayAdapter(Context context, ArrayList<CustomArtist> artists) {
        super(context, R.layout.list_item_activated_artist, artists);
    }

    /*
     * Events
     * */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CustomArtist artist = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_activated_artist, parent, false);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
            viewHolder.nameTextView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Passing null as a path will not trigger any request but will set a placeholder, if one is specified.
        // IllegalArgumentException - if path is empty or blank string.
        try {
            Picasso.with(getContext()).load(artist.getImageUrl()).into(viewHolder.imageView);
        } catch (IllegalArgumentException e){
            viewHolder.imageView.setImageDrawable(null);
        }

        viewHolder.nameTextView.setText(artist.getName());

        return convertView;
    }
}
