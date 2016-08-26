package com.codepath.selfiespot.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import com.afollestad.materialcamera.MaterialCamera;
import com.codepath.selfiespot.R;
import com.codepath.selfiespot.fragments.AlertLocationPickerMapFragment;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.util.ImageUtil;
import com.codepath.selfiespot.views.DynamicHeightImageView;
import com.codepath.selfiespot.views.HideKeyboardEditTextFocusChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class EditSelfieSpotActivity extends AppCompatActivity {
    private static final String TAG = EditSelfieSpotActivity.class.getSimpleName();

    private final static int REQUEST_CODE_CAMERA = 2;
    private static final String EXTRA_SELFIE_SPOT_ID = EditSelfieSpotActivity.class.getSimpleName() + ":SELFIE_SPOT_ID";

    private static final String TAG_MAP_PICKER = "mapPicker";

    @BindView(R.id.iv_image)
    DynamicHeightImageView mImageView;

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
        if (!TextUtils.isEmpty(selfieSpotId)) {
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

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CAMERA) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Saved to: " + data.getDataString());
                final String imageUrl = data.getDataString();
                setMedia(imageUrl);
            } else {
                if (data != null) {
                    final Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                    Log.e(TAG, "Error retrieving camera", e);
                    Toast.makeText(this, "Error retrieving image", Toast.LENGTH_SHORT).show();
                }
            }
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

        if (!TextUtils.isEmpty(mSelfieSpot.getName())) {
            mNameEditText.setText(mSelfieSpot.getName());
        }

        if (!TextUtils.isEmpty(mSelfieSpot.getDescription())) {
            mDescEditText.setText(mSelfieSpot.getDescription());
        }

        if (mSelfieSpot.getLocation() != null) {
            mLocationEditText.setText(mSelfieSpot.getLocation().toString());
        }

        if (mSelfieSpot.getMediaFile() != null) {
            showImage(mSelfieSpot.getMediaFile().getUrl(), mSelfieSpot.getMediaWidth(), mSelfieSpot.getMediaHeight());
        } else {
            mImageView.setImageResource(R.drawable.ic_add_a_photo);
        }

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                EditSelfieSpotActivityPermissionsDispatcher.addPhotoWithCheck(EditSelfieSpotActivity.this);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EditSelfieSpotActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void addPhoto() {
        final File directory = ImageUtil.getStorageDir();

        new MaterialCamera(this)
                .saveDir(directory)
                .stillShot()
                .start(REQUEST_CODE_CAMERA);
    }

    private void setMedia(final String imageUrl) {
        final int[] actualDimensions = ImageUtil.getImageSize(Uri.parse(imageUrl));

        final int[] optimizedDimensions = ImageUtil.getResizedImageSize(actualDimensions);
        String optimizedFilePath = getOptimizedFilePath(imageUrl, optimizedDimensions);

        final ParseFile parseFile = new ParseFile(new File(stripFilePrefix(optimizedFilePath)));

        mSelfieSpot.setMediaFile(parseFile);
        mSelfieSpot.setMediaWidth(optimizedDimensions[0]);
        mSelfieSpot.setMediaHeight(optimizedDimensions[1]);
        showImage(imageUrl, optimizedDimensions[0], optimizedDimensions[1]);
        Log.d(TAG, String.format("%s captured, width: %d, height: %d", optimizedFilePath, optimizedDimensions[0], optimizedDimensions[1]));
    }


    private void showImage(final String url, final int width, final int height) {
        mImageView.setAdjustViewBounds(true);

        mImageView.setHeightRatio((float) height / (float) width);

        //TODO - use image width & height
        Picasso.with(this)
                .load(url)
                .placeholder(R.drawable.ic_progress_indeterminate)
                .error(R.drawable.ic_error)
                .into(mImageView);
    }

    private String getOptimizedFilePath(final String path, final int[] optimizedDimensions) {
        final Bitmap rawTakenImage = BitmapFactory.decodeFile(stripFilePrefix(path));
        final Bitmap resizedImage = Bitmap.createScaledBitmap(rawTakenImage, optimizedDimensions[0], optimizedDimensions[1], true);

        // if same, just return the given path
        if (resizedImage == rawTakenImage) {
            return path;
        }

        final Uri resizedImagePath = ImageUtil.savePicture(this, resizedImage);
        return resizedImagePath.toString();
    }

    private String stripFilePrefix(final String path) {
        final String PREFIX = "file:///";
        if (path.startsWith(PREFIX)) {
            return path.replace(PREFIX, "");
        }
        return path;
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

        if (mSelfieSpot.getLocation() != null) {
            position = new LatLng(mSelfieSpot.getLocation().getLatitude(), mSelfieSpot.getLocation().getLongitude());
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
