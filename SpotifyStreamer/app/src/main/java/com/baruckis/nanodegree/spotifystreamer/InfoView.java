package com.baruckis.nanodegree.spotifystreamer;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Andrius-Baruckis on 2015-07-10.
 * http://www.baruckis.com/
 */

/*
* This is custom view component which allows
* to show messages for initial screen or for
* empty list. Also it can show error message
* with icon and button to retry action.
* */
public class InfoView extends LinearLayout {

    private ImageView iconImageView;
    private TextView messageTextView;
    private Button button;

    public InfoView(Context context) {
        super(context);
        init();
    }

    public InfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(11)
    public InfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public InfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /*
   *   Methods
   * */
    private void init() {
        hide();
        inflate(getContext(), R.layout.info_view, this);
        iconImageView = (ImageView) findViewById(R.id.icon);
        messageTextView = (TextView) findViewById(R.id.message);
        button = (Button) findViewById(R.id.button);
    }

    public void showEmpty(String message) {
        this.setVisibility(View.VISIBLE);
        messageTextView.setText(message);
        iconImageView.setImageDrawable(null);
        button.setVisibility(View.GONE);
    }

    public void showError(String message, OnClickListener onClickListener) {
        this.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        messageTextView.setText(message);
        iconImageView.setImageResource(R.drawable.ic_error_outline_black_48dp);
        button.setOnClickListener(onClickListener);
    }

    public void hide() {
        this.setVisibility(View.GONE);
    }
}
