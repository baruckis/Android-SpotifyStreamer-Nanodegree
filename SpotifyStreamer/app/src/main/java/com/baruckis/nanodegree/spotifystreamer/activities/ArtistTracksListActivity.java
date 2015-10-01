package com.baruckis.nanodegree.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.baruckis.nanodegree.spotifystreamer.R;
import com.baruckis.nanodegree.spotifystreamer.fragments.ArtistTracksListFragment;
import com.baruckis.nanodegree.spotifystreamer.fragments.PlayerDialogFragment;
import com.baruckis.nanodegree.spotifystreamer.models.CustomTrack;

import java.util.ArrayList;

/**
 * Created by Andrius-Baruckis on 2015.
 * http://www.baruckis.com/
 */

/**
 * An activity representing a single Item detail screen (Artist Top tracks).
 * This activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainArtistsListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ArtistTracksListFragment}.
 */
public class ArtistTracksListActivity extends BaseActivity implements ArtistTracksListFragment.Callbacks {

    public static final String ARG_SEARCH_TEXT = "search_text";

    private ArtistTracksListFragment mArtistTracksListFragment;

    /*
     * Events
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_tracks_list);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        mArtistTracksListFragment = (ArtistTracksListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_artist_tracks_list);

        if (savedInstanceState == null) {
            if (mArtistTracksListFragment != null) {
                String id = getIntent().getStringExtra(ArtistTracksListFragment.ARG_ACTIVATED_ARTIST_ID);
                String artistName = getIntent().getStringExtra(ArtistTracksListFragment.ARG_ACTIVATED_ARTIST_NAME);

                if (id == null || artistName == null) return;

                mArtistTracksListFragment.showSelectedArtistTopTracks(id, artistName);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            if (isPlayerDialogFragmentShown()) {
                mArtistTracksListFragment.restoreActionBar();
                super.onBackPressed();
                return true;
            } else {
                Intent newIntent = new Intent(this, MainArtistsListActivity.class);

                String searchText = getIntent().getStringExtra(ARG_SEARCH_TEXT);
                if (searchText != null) {
                    newIntent.putExtra(ARG_SEARCH_TEXT, searchText);
                }

                NavUtils.navigateUpTo(this, newIntent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isPlayerDialogFragmentShown()) {
            mArtistTracksListFragment.restoreActionBar();
        }
        super.onBackPressed();
    }

    @Override
    public void onItemSelected(ArrayList<CustomTrack> tracksList, Integer index) {
        // The device is smaller, so show the fragment fullscreen
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        PlayerDialogFragment playerDialogFragment = PlayerDialogFragment.newInstance(tracksList, index);
        transaction.add(android.R.id.content, playerDialogFragment).addToBackStack(null).commit();

        setContentFragment(playerDialogFragment);
    }
}
