package com.baruckis.nanodegree.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.baruckis.nanodegree.spotifystreamer.R;
import com.baruckis.nanodegree.spotifystreamer.fragments.ArtistTracksListFragment;
import com.baruckis.nanodegree.spotifystreamer.fragments.ArtistsListFragment;

/**
 * Created by Andrius-Baruckis on 2015-07-10.
 * http://www.baruckis.com/
 */

/**
 * An activity representing a list of Items (Artists). This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ArtistTracksListActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ArtistsListFragment} and the item details
 * (if present) is a {@link ArtistTracksListFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ArtistsListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MainArtistsListActivity extends AppCompatActivity
        implements ArtistsListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private ArtistsListFragment mArtistsListFragment = null;
    private ArtistTracksListFragment mArtistTracksListFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artists_list);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.artists_list);
        if (fragment != null && fragment instanceof ArtistsListFragment) {
            mArtistsListFragment = (ArtistsListFragment) fragment;
        }
        if (mArtistsListFragment == null) return;


        if (findViewById(R.id.artist_tracks_list) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            mArtistsListFragment.setActivateOnItemClick(true);

            fragment = getSupportFragmentManager().findFragmentById(R.id.artist_tracks_list);
            if (fragment != null && fragment instanceof ArtistTracksListFragment) {
                mArtistTracksListFragment = (ArtistTracksListFragment) fragment;
            }

        } else {
            String searchText = getIntent().getStringExtra(ArtistTracksListActivity.ARG_SEARCH_TEXT);
            if (searchText != null) {
                mArtistsListFragment.restoreSearchEditText(searchText);
            }
        }
    }

    /**
     * Callback method from {@link ArtistsListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String searchText, String artistId, String artistName) {
        if (mTwoPane) {
            // In two-pane mode, show the top tracks list
            if (mArtistTracksListFragment != null) {
                mArtistTracksListFragment.showSelectedArtistTopTracks(artistId, artistName);
            }
        } else {
            // In single-pane mode, simply start the top tracks activity
            // for the selected artist ID.
            Intent artistTracksListActivityIntent = new Intent(this, ArtistTracksListActivity.class);
            artistTracksListActivityIntent.putExtra(ArtistTracksListFragment.ARG_ACTIVATED_ARTIST_ID, artistId);
            // Pass artist name to make subtitle
            artistTracksListActivityIntent.putExtra(ArtistTracksListFragment.ARG_ACTIVATED_ARTIST_NAME, artistName);
            artistTracksListActivityIntent.putExtra(ArtistTracksListActivity.ARG_SEARCH_TEXT, searchText);
            startActivity(artistTracksListActivityIntent);
        }
    }

    @Override
    public void onSearchTextChanged() {
        if (mArtistTracksListFragment != null) {
            mArtistTracksListFragment.onSearchTextChanged();
        }
    }
}
