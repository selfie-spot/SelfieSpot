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
import android.view.View;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.fragments.AlertLocationPickerMapFragment;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.views.HideKeyboardEditTextFocusChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditSelfieSpotActivity extends AppCompatActivity implements AlertLocationPickerMapFragment.LocationPickedListener {
    private static final String TAG_MAP_PICKER = "mapPicker";

    @BindView(R.id.tie_name)
    TextInputEditText mNameEditText;

    @BindView(R.id.til_name)
    TextInputLayout mNameTextInputLayout;

    @BindView(R.id.tie_description)
    TextInputEditText mDescEditText;

    @BindView(R.id.tie_location)
    TextInputEditText mItemLocationEditText;

    @BindView(R.id.til_location)
    TextInputLayout mLocationTextInputLayout;

    @BindView(R.id.fab_save_selfie)
    FloatingActionButton mSaveButton;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private HideKeyboardEditTextFocusChangeListener mHideKeyboardEditTextFocusChangeListener;
    private SelfieSpot mSelfieSpot;

    public static Intent createIntent(final Context context) {
        final Intent intent = new Intent(context, EditSelfieSpotActivity.class);
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

        if (mSelfieSpot == null) {
            mSelfieSpot = new SelfieSpot();
        }

        initViews();
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

        mItemLocationEditText.setInputType(InputType.TYPE_NULL);

        // this is required once the focus is obtained
        mItemLocationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMap();
            }
        });

        // this is required the first time focus is obtained
        mItemLocationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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

//        mSelfieSpot.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(final ParseException e) {
//                if (e == null) {
//                    Toast.makeText(EditSelfieSpotActivity.this, "Saving SelfieSpot", Toast.LENGTH_SHORT).show();
//                    finish();
//                } else {
//                    Toast.makeText(EditSelfieSpotActivity.this, "Error saving SelfieSpot", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    private void showMap() {
        final AlertLocationPickerMapFragment locationPickerMapFragment = AlertLocationPickerMapFragment.createInstance(null);
        locationPickerMapFragment.setLocationPickedListener(this);
        locationPickerMapFragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_FullScreen);
        locationPickerMapFragment.show(getSupportFragmentManager(), TAG_MAP_PICKER);
    }

    @Override
    public void onLocationPicked(final LatLng location) {
        mItemLocationEditText.setText(location.toString());
        mSelfieSpot.setLocation(new ParseGeoPoint(location.latitude, location.longitude));
    }
}
