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

package com.baruckis.nanodegree.spotifystreamer.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.baruckis.nanodegree.spotifystreamer.R;
import com.baruckis.nanodegree.spotifystreamer.Utils;


public class MessageDialogFragment extends DialogFragment {

    public static final String TAG = MessageDialogFragment.class.getSimpleName();
    public static final String ARG_ICON = "icon";
    public static final String ARG_TITLE = "title";
    public static final String ARG_MESSAGE = "message";

    /*
     * Constructors
     * */
    public MessageDialogFragment() {
    }

    public static MessageDialogFragment newInstance(int icon, String title, String message) {
        return newInstance(icon, title, message, null);
    }

    public static MessageDialogFragment newInstance(int icon, String title, String message, Fragment fragment) {
        MessageDialogFragment confirmDialog = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ICON, icon);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        confirmDialog.setArguments(args);
        if (fragment != null) confirmDialog.setTargetFragment(fragment, 0);
        return confirmDialog;
    }


	/*
     * Events
     * */

    @SuppressLint("InflateParams")
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_dialog_message, null);

        TextView messageView = (TextView) dialogView.findViewById(R.id.message);
        messageView.setMovementMethod(LinkMovementMethod.getInstance());
        messageView.setText(Html.fromHtml(getArguments().getString(ARG_MESSAGE)));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString(ARG_TITLE))
                .setIcon(getArguments().getInt(ARG_ICON))
                .setView(dialogView)
                .setPositiveButton(R.string.OK, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Utils.hideSoftKeyboard(getActivity());
            }
        });

        return dialog;
    }
}