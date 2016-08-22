package com.codepath.selfiespot.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.fragments.AlertLocationPickerMapFragment;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.views.HideKeyboardEditTextFocusChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditSelfieSpotActivity extends AppCompatActivity {
    private static final String TAG = EditSelfieSpotActivity.class.getSimpleName();
    private static final String EXTRA_SELFIE_SPOT_ID = EditSelfieSpotActivity.class.getSimpleName() + ":SELFIE_SPOT_ID";

    private static final String TAG_MAP_PICKER = "mapPicker";

    @BindView(R.id.tie_name)
    TextInputEditText mNameEditText;

    @BindView(R.id.til_name)
    TextInputLayout mNameTextInputLayout;

    @BindView(R.id.tie_description)
    TextInputEditText mDescEditText;

    @BindView(R.id.tie_location)
    TextInputEditText mLocationEditText;

    @BindView(R.id.til_location)
    TextInputLayout mLocationTextInputLayout;

    @BindView(R.id.fab_save_selfie)
    FloatingActionButton mSaveButton;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fl_progress_holder)
    FrameLayout mProgressFrameLayout;

    private HideKeyboardEditTextFocusChangeListener mHideKeyboardEditTextFocusChangeListener;
    private SelfieSpot mSelfieSpot;

    public static Intent createIntent(final Context context, final String selfieSpotId) {
        final Intent intent = new Intent(context, EditSelfieSpotActivity.class);
        if (! TextUtils.isEmpty(selfieSpotId)) {
            intent.putExtra(EXTRA_SELFIE_SPOT_ID, selfieSpotId);
        }
        return intent;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_selfie_spot);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mHideKeyboardEditTextFocusChangeListener = new HideKeyboardEditTextFocusChangeListener();

        final String selfieSpotId = getIntent().getStringExtra(EXTRA_SELFIE_SPOT_ID);

        if (selfieSpotId != null) {
            showBusy();
            // retrieve SelfieSpot before initializing views
            final ParseQuery<SelfieSpot> query = SelfieSpot.getQuery();
            query.getInBackground(selfieSpotId, new GetCallback<SelfieSpot>() {
                @Override
                public void done(final SelfieSpot selfieSpot, final ParseException e) {
                    if (e != null) {
                        Toast.makeText(EditSelfieSpotActivity.this,
                                "Unable to retrieve SelfieSpot", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Unable to retrieve SelfieSpot: " + selfieSpotId, e);
                        finish();
                    } else {
                        hideBusy();
                        mSelfieSpot = selfieSpot;
                        initViews();
                    }
                }
            });
        } else {
            // a new selfiespot, initialize views immediately
            mSelfieSpot = new SelfieSpot();
            initViews();
        }
    }

    private void initViews() {
        mNameEditText.setOnFocusChangeListener(mHideKeyboardEditTextFocusChangeListener);
        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                // no-op
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                // no-op
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (mNameTextInputLayout.isErrorEnabled() && !TextUtils.isEmpty(s.toString())) {
                    mNameTextInputLayout.setErrorEnabled(false);
                }
            }
        });

        mDescEditText.setOnFocusChangeListener(mHideKeyboardEditTextFocusChangeListener);

        mLocationEditText.setInputType(InputType.TYPE_NULL);

        // this is required once the focus is obtained
        mLocationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMap();
            }
        });

        // this is required the first time focus is obtained
        mLocationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                if (hasFocus) {
                    showMap();
                }
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveSelfieSpot();
            }
        });

        if (mSelfieSpot != null) {
            mNameEditText.setText(mSelfieSpot.getName());

            if (! TextUtils.isEmpty(mSelfieSpot.getDescription())) {
                mDescEditText.setText(mSelfieSpot.getDescription());
            }

            mLocationEditText.setText(mSelfieSpot.getLocation().toString());
        }
    }

    private void onSaveSelfieSpot() {
        final String updatedName = mNameEditText.getText().toString();

        if (TextUtils.isEmpty(updatedName)) {
            mNameTextInputLayout.setError(getString(R.string.error_name));
            return;
        }

        if (mSelfieSpot.getLocation() == null) {
            mLocationTextInputLayout.setError(getString(R.string.error_location));
            return;
        }

        mSelfieSpot.setName(updatedName.trim());

        final String updatedDescription = mDescEditText.getText().toString();
        mSelfieSpot.setDescription(updatedDescription);
        mSelfieSpot.setUser(ParseUser.getCurrentUser());
        showBusy();

        mSelfieSpot.saveInBackground(new SaveCallback() {
            @Override
            public void done(final ParseException e) {
                if (e == null) {
                    Toast.makeText(EditSelfieSpotActivity.this, "Saved SelfieSpot", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Error saving SelfieSpot", e);
                    hideBusy();
                    Toast.makeText(EditSelfieSpotActivity.this, "Error saving SelfieSpot", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showBusy() {
        mProgressFrameLayout.setVisibility(View.VISIBLE);
        mSaveButton.hide();
    }

    private void hideBusy() {
        mProgressFrameLayout.setVisibility(View.GONE);
        mSaveButton.show();
    }

    private void showMap() {
        LatLng position = null;

        if (mSelfieSpot != null) {
            position = new LatLng(position.latitude, position.longitude);
        }

        final AlertLocationPickerMapFragment locationPickerMapFragment = AlertLocationPickerMapFragment.createInstance(position);
        locationPickerMapFragment.setLocationPickedListener(new AlertLocationPickerMapFragment.LocationPickedListener() {
            @Override
            public void onLocationPicked(final LatLng location) {
                setLocation(location);
                locationPickerMapFragment.dismiss();
            }
        });
        locationPickerMapFragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_FullScreen);
        locationPickerMapFragment.show(getSupportFragmentManager(), TAG_MAP_PICKER);
    }

    private void setLocation(final LatLng location) {
        mLocationEditText.setText(location.toString());
        mLocationTextInputLayout.setErrorEnabled(false);
        mSelfieSpot.setLocation(new ParseGeoPoint(location.latitude, location.longitude));
    }
}
