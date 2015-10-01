package com.baruckis.nanodegree.spotifystreamer.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.content.LocalBroadcastManager;

import com.baruckis.nanodegree.spotifystreamer.PlayerService;
import com.baruckis.nanodegree.spotifystreamer.R;
import com.baruckis.nanodegree.spotifystreamer.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Andrius-Baruckis on 2015.
 * http://www.baruckis.com/
 */
public class SettingsFragment extends PreferenceFragment {

    private ListPreference mCountryListPreference;
    private SwitchPreference mNotificationSwitchPreference;


    /*
     * Events
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // Getting preferences from the preference resource.
        mCountryListPreference = (ListPreference) getPreferenceManager().findPreference(getString(R.string.list_preference_country_code_key));
        mNotificationSwitchPreference = (SwitchPreference) getPreferenceManager().findPreference(getString(R.string.switch_preference_notification_key));
        Preference sourcePreference = (Preference) getPreferenceManager().findPreference(getString(R.string.preference_source_key));
        Preference authorPreference = (Preference) getPreferenceManager().findPreference(getString(R.string.preference_author_key));
        Preference licencePreference = (Preference) getPreferenceManager().findPreference(getString(R.string.preference_licence_key));
        Preference versionPreference = (Preference) getPreferenceManager().findPreference(getString(R.string.preference_version_key));

        // Setting up to display a list of countries
        List<String> countriesCodesList = new ArrayList<String>();
        List<String> countriesNamesList = new ArrayList<String>();

        String[] countries = Locale.getISOCountries(); // will return a list of all 2-letter country codes defined in ISO 3166

        for (String countryCode : countries) {
            Locale locale = new Locale("", countryCode);
            countriesCodesList.add(locale.getCountry());
            countriesNamesList.add(locale.getDisplayCountry());
        }

        final CharSequence[] countriesCodes = countriesCodesList.toArray(new CharSequence[countriesCodesList.size()]);
        final CharSequence[] countriesNames = countriesNamesList.toArray(new CharSequence[countriesNamesList.size()]);

        mCountryListPreference.setEntryValues(countriesCodes);
        mCountryListPreference.setEntries(countriesNames);

        mCountryListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateCountryListPreferenceSummary((String)newValue);
                return true;
            }
        });

        // set initial default country
        if (mCountryListPreference.getValue() == null) {
            mCountryListPreference.setValue(Locale.getDefault().getCountry());
        }
        updateCountryListPreferenceSummary(mCountryListPreference.getValue());

        mNotificationSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ((SwitchPreference) preference).setChecked((boolean) newValue);
                updateNotificationSwitchPreferenceIcon();

                // When notification visibility is changed, inform service immediately to show or stop showing notification.
                Intent notificationIntent = new Intent(PlayerService.RECEIVE_BROADCAST_INTENT_NOTIFICATION);
                notificationIntent.putExtra(PlayerService.RECEIVE_BROADCAST_INTENT_NOTIFICATION, (boolean) newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(notificationIntent);

                return true;
            }
        });
        updateNotificationSwitchPreferenceIcon();

        // on source preference click open web browser with the url link
        sourcePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = getString(R.string.preference_source_url);
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setData(Uri.parse(url));
                startActivity(viewIntent);
                return true;
            }
        });

        // on author preference click open web browser with the url link
        authorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = getString(R.string.preference_author_url);
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setData(Uri.parse(url));
                startActivity(viewIntent);
                return true;
            }
        });

        // on licence preference click open fragment dialog with the message
        licencePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                MessageDialogFragment.newInstance(
                        R.drawable.ic_certificate_grey600_36dp,
                        getString(R.string.licence_title),
                        getString(R.string.licence_body,
                                getString(R.string.app_name),
                                Utils.getAppVersionNumber(getActivity())))
                                .show(getFragmentManager(), MessageDialogFragment.TAG);

                return true;
            }
        });

        // setting app version number
        String version = getString(R.string.preference_version_summary) + Utils.getAppVersionNumber(getActivity());
        versionPreference.setSummary(version);
    }

    /*
     * Methods
     * */
    private void updateNotificationSwitchPreferenceIcon(){
        if (mNotificationSwitchPreference.isChecked()) {
            mNotificationSwitchPreference.setTitle(R.string.title_switch_preference_on);
            mNotificationSwitchPreference.setIcon(R.drawable.ic_bell_grey600_24dp);
        } else {
            mNotificationSwitchPreference.setTitle(R.string.title_switch_preference_off);
            mNotificationSwitchPreference.setIcon(R.drawable.ic_bell_off_grey600_24dp);
        }
    }

    private void updateCountryListPreferenceSummary(String country){
        Locale locale = new Locale("", country);
        mCountryListPreference.setSummary(locale.getDisplayCountry());
    }
}