/*
 * Copyright 2017 Andrius Baruckis www.baruckis.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baruckis.nanodegree.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.baruckis.nanodegree.spotifystreamer.PlayerService;
import com.baruckis.nanodegree.spotifystreamer.R;
import com.baruckis.nanodegree.spotifystreamer.fragments.ArtistTracksListFragment;
import com.baruckis.nanodegree.spotifystreamer.fragments.ArtistsListFragment;
import com.baruckis.nanodegree.spotifystreamer.models.CustomTrack;

import java.util.ArrayList;


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
public class MainArtistsListActivity extends BaseActivity
        implements ArtistsListFragment.Callbacks, ArtistTracksListFragment.Callbacks {

    private ArtistsListFragment mArtistsListFragment = null;
    private ArtistTracksListFragment mArtistTracksListFragment = null;


    /*
     * Events
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artists_list);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.artists_list);
        if (fragment != null && fragment instanceof ArtistsListFragment) {
            mArtistsListFragment = (ArtistsListFragment) fragment;
        }
        if (mArtistsListFragment == null) return;

        if (isPlayerDialogFragmentShown()) {
            getSupportActionBar().setDisplayUseLogoEnabled(false);
        } else {
            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }

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

        if (isPlayerDialogFragmentShown()) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    @Override
    public void onItemSelected(ArrayList<CustomTrack> tracksList, Integer index) {
        showPlayerDialogFragment(tracksList, index);
    }


    @Override
    public void onDestroy() {
        if (!isChangingConfigurations()) {
            Intent playIntent = new Intent(this, PlayerService.class);
            stopService(playIntent);
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isPlayerDialogFragmentShown()) {
            restoreActionBar();
            super.onBackPressed();
        } else {
            // Not to destroy current activity on back button is pressed.
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        invalidateOptionsMenu();
    }
}
