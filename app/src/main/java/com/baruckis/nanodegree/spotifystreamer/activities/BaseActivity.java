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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baruckis.nanodegree.spotifystreamer.App;
import com.baruckis.nanodegree.spotifystreamer.PlayerService;
import com.baruckis.nanodegree.spotifystreamer.R;
import com.baruckis.nanodegree.spotifystreamer.Utils;
import com.baruckis.nanodegree.spotifystreamer.fragments.PlayerDialogFragment;
import com.baruckis.nanodegree.spotifystreamer.models.CustomTrack;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;


public abstract class BaseActivity extends AppCompatActivity  {

    // TODO: Replace with your client ID
    public static final String CLIENT_ID = "yourclientid";

    // TODO: Replace with your redirect URI
    public static final String REDIRECT_URI = "yourcustomprotocol://callback";


    // Request code will be used to verify if result comes from the Spotify login activity. Can be set to any integer.
    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;

    // Activity will respond to this action string
    public static final String RECEIVE_BROADCAST_INTENT_SHARE_UPDATE = "com.baruckis.nanodegree.spotifystreamer.activities.receive_broadcast_intent_share_update";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public boolean mTwoPane;

    private MenuItem mNowPlayingActionButton;
    private MenuItem mShareActionButton;

    private ShareActionProvider mShareActionProvider;

    private Fragment mContentFragment;

    /*
     * Objects
     * */
    private AuthenticationRequest getAuthenticationRequest(AuthenticationResponse.Type type) {
        return new AuthenticationRequest.Builder(CLIENT_ID, type, REDIRECT_URI)
                .setShowDialog(true)
                .build();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(RECEIVE_BROADCAST_INTENT_SHARE_UPDATE)) {

                Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                shareIntent.setData(Uri.parse(intent.getExtras().getString(Intent.EXTRA_TEXT)));

                // Call to update the share intent
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(shareIntent);
                }
            }
        }
    };

    /*
     * Events
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register to receive messages.
        // We are registering an observer (mBroadcastReceiver) to receive Intents
        // with actions named RECEIVE_BROADCAST_INTENT_SHARE_UPDATE.
        IntentFilter intentFilter = new IntentFilter(RECEIVE_BROADCAST_INTENT_SHARE_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);

        mContentFragment = getSupportFragmentManager().findFragmentById(android.R.id.content);

        App app = ((App) getApplication());
        if (!app.getIsAuthorized()) {
            requestSpotifyApiToken();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        mNowPlayingActionButton = menu.findItem(R.id.now_playing);
        mShareActionButton = menu.findItem(R.id.share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareActionButton);

        mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
            @Override
            public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {

                // When ShareActionProvider menu item is selected it is better to pause track playback.
                // For example if official spotify app is launched this will help to avoid double playback.
                Intent pauseIntent = new Intent(PlayerService.RECEIVE_BROADCAST_INTENT_PAUSE);
                LocalBroadcastManager.getInstance(getParent()).sendBroadcast(pauseIntent);
                return false;
            }
        });

        mNowPlayingActionButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showPlayerDialogFragment(null, null);
                return true;
            }
        });

        setActionButtonsVisible();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
        App app = ((App) getApplication());

        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    app.setSpotifyApiAccessToken(response.getAccessToken());
                    app.setIsAuthorized(true);
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Toast.makeText(this, response.getError(), Toast.LENGTH_LONG).show();

                    Toast.makeText(getApplicationContext(), getString(R.string.toast_spotify_authorization_error_msg_error), Toast.LENGTH_SHORT).show();
                    Log.e(BaseActivity.class.getSimpleName(), response.getError());

                    app.setIsAuthorized(false);

                    showSpotifyAuthenticateSnackbar();
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases

                    app.setIsAuthorized(false);

                    showSpotifyAuthenticateSnackbar();
            }

        }
    }


    /*
     * Methods
     * */
    public void requestSpotifyApiToken() {
        final AuthenticationRequest request = getAuthenticationRequest(AuthenticationResponse.Type.TOKEN);
        AuthenticationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
    }

    public void showSpotifyAuthenticateSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.artists_list), R.string.snackbar_spotify_authenticate_msg_error, Snackbar.LENGTH_INDEFINITE);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(3);
        snackbar.setAction(R.string.snackbar_spotify_authenticate_msg_action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSpotifyApiToken();
            }
        });
        snackbar.show();
    }

    public void showPlayerDialogFragment(ArrayList<CustomTrack> tracksList, Integer index) {

        PlayerDialogFragment playerDialogFragment = PlayerDialogFragment.newInstance(tracksList, index);

        if (mTwoPane) {
            playerDialogFragment.show(getSupportFragmentManager(), "dialog");
        } else {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, playerDialogFragment).addToBackStack(null).commit();
            mContentFragment = playerDialogFragment;
        }
    }

    public void setActionButtonsVisible() {
        App app = ((App) getApplication());
        if (app.getIsNowPlaying()) {
            if (mNowPlayingActionButton != null) mNowPlayingActionButton.setVisible(true);
            if (mShareActionButton != null) mShareActionButton.setVisible(true);
        }
    }

    public void restoreActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        Utils.setActionBar(this, getString(R.string.app_name), null);
    }

    public void setContentFragment(Fragment fragment) {
      mContentFragment = fragment;
    }

    public boolean isPlayerDialogFragmentShown() {
        return mContentFragment != null && mContentFragment instanceof PlayerDialogFragment && mContentFragment.isAdded();
    }
}
