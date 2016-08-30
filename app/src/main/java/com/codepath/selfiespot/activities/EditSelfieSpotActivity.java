package com.codepath.selfiespot.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
import com.codepath.selfiespot.R;
import com.codepath.selfiespot.fragments.AlertLocationPickerMapFragment;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.models.Tag;
import com.codepath.selfiespot.util.CollectionUtils;
import com.codepath.selfiespot.util.ImageUtil;
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
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class EditSelfieSpotActivity extends AppCompatActivity {
    private static final String TAG = EditSelfieSpotActivity.class.getSimpleName();

    // state "keys"
    private static final String STATE_DATA = "selfieSpotData";

    private static final int REQUEST_CODE_CAMERA = 2;
    private static final String EXTRA_SELFIE_SPOT_ID = EditSelfieSpotActivity.class.getSimpleName() + ":SELFIE_SPOT_ID";

    private static final String TAG_MAP_PICKER = "mapPicker";

    @BindView(R.id.iv_image)
    ImageView mImageView;

    @BindView(R.id.tv_name)
    EditText mNameTextView;

    @BindView(R.id.btn_create)
    Button mCreateButton;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fl_progress_holder)
    FrameLayout mProgressFrameLayout;

    @BindView(R.id.iv_location)
    ImageView mLocationImageView;

    @BindView(R.id.iv_tag)
    ImageView mTagImageView;

    private SelfieSpot mSelfieSpot;

    // "transient fields" to be persisted across configuration changes
    private SelfieSpotData mSelfieSpotData;

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

        if (savedInstanceState != null) {
            mSelfieSpotData = (SelfieSpotData) savedInstanceState.getSerializable(STATE_DATA);
        }

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
                        if (mSelfieSpotData == null) {
                            mSelfieSpotData = convert(selfieSpot);
                        }
                        initViews();
                    }
                }
            });
        } else {
            if (mSelfieSpotData == null) {
                mSelfieSpotData = new SelfieSpotData();
            }
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

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        // grab the name
        mSelfieSpotData.currentName = mNameTextView.getText().toString();

        outState.putSerializable(STATE_DATA, mSelfieSpotData);
    }

    private void initViews() {
        // tags
        if (CollectionUtils.isEmpty(mSelfieSpotData.currentTags)) {
            mTagImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_tag));
        } else {
            mTagImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_tag_populated));
        }

        // name
        if (! TextUtils.isEmpty(mSelfieSpotData.currentName)) {
            mNameTextView.setText(mSelfieSpotData.currentName);
        }

        // image
        if (! TextUtils.isEmpty(mSelfieSpotData.currentImagePath)) {
            showImage(mSelfieSpotData.currentImagePath, mSelfieSpotData.currentImageWidth,
                    mSelfieSpotData.currentImageHeight);
        } else {
            mImageView.setImageResource(R.drawable.ic_add_a_photo);
        }

        // location
        if (mSelfieSpotData.currentLocationLat != null && mSelfieSpotData.currentLocationLong != null) {
            mLocationImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_place_populated));
        } else {
            mLocationImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_place_normal));
        }

        // listeners
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                EditSelfieSpotActivityPermissionsDispatcher.addPhotoWithCheck(EditSelfieSpotActivity.this);
            }
        });

        mLocationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showMap();
            }
        });

        mTagImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showTags();
            }
        });

        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveSelfieSpot();
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

        mSelfieSpotData.currentImagePath = getOptimizedFilePath(imageUrl, optimizedDimensions);
        mSelfieSpotData.currentImageWidth = optimizedDimensions[0];
        mSelfieSpotData.currentImageHeight = optimizedDimensions[1];

        showImage(mSelfieSpotData.currentImagePath, mSelfieSpotData.currentImageWidth,
                mSelfieSpotData.currentImageHeight);
        Log.d(TAG, String.format("%s captured, width: %d, height: %d", mSelfieSpotData.currentImagePath,
                mSelfieSpotData.currentImageWidth, mSelfieSpotData.currentImageHeight));
    }


    private void showImage(final String url, final int width, final int height) {
        mImageView.setAdjustViewBounds(true);

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
        // validation
        final String updatedName = mNameTextView.getText().toString();

        if (TextUtils.isEmpty(updatedName)) {
            mNameTextView.setError(getString(R.string.error_name));
            return;
        }

        if (mSelfieSpotData.currentLocationLong == null || mSelfieSpotData.currentLocationLat == null) {
            ImageUtil.animateImageView(this, mLocationImageView, getResources().getDrawable(R.drawable.ic_place_error));
            return;
        }

        if (mSelfieSpotData.currentImagePath == null) {
            ImageUtil.animateImageView(this, mImageView, getResources().getDrawable(R.drawable.ic_add_a_photo_error));
            return;
        }

        // populate latest data
        if (mSelfieSpot == null) {
            mSelfieSpot = new SelfieSpot();
        }

        mSelfieSpot.setName(updatedName.trim());
        mSelfieSpot.setUser(ParseUser.getCurrentUser());
        mSelfieSpot.setLocation(new ParseGeoPoint(mSelfieSpotData.currentLocationLat, mSelfieSpotData.currentLocationLong));

        if (mSelfieSpot.getMediaFile() == null ||
                ! mSelfieSpotData.currentImagePath.equals(mSelfieSpot.getMediaFile().getUrl())) {
            final ParseFile parseFile = new ParseFile(new File(stripFilePrefix(mSelfieSpotData.currentImagePath)));
            mSelfieSpot.setMediaFile(parseFile);
            mSelfieSpot.setMediaWidth(mSelfieSpotData.currentImageWidth);
            mSelfieSpot.setMediaHeight(mSelfieSpotData.currentImageHeight);
        }

        mSelfieSpot.setTags(mSelfieSpotData.currentTags);

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
        mCreateButton.setVisibility(View.INVISIBLE);
    }

    private void hideBusy() {
        mProgressFrameLayout.setVisibility(View.GONE);
        mCreateButton.setVisibility(View.VISIBLE);
    }

    private void showMap() {
        LatLng position = null;

        if (mSelfieSpotData.currentLocationLat != null && mSelfieSpotData.currentLocationLong != null) {
            position = new LatLng(mSelfieSpotData.currentLocationLat, mSelfieSpotData.currentLocationLong);
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

    private void showTags() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String[] allTags = Tag.getAllTagsAsStringArray();
        final boolean[] checkedOptions = getSelectedTags(allTags);

        builder.setMultiChoiceItems(allTags, checkedOptions, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int which, final boolean isChecked) {
                final String item = allTags[which];
                updateTag(item, isChecked);
            }
        });
        builder.show();
    }

    private boolean[] getSelectedTags(final String[] allTags) {
        final boolean[] selected = new boolean[allTags.length];
        final Set<String> selectedTags = mSelfieSpotData.currentTags;

        for (int i = 0; i < allTags.length; i++) {
            selected[i] = selectedTags.contains(allTags[i]);
        }

        return selected;
    }

    private void updateTag(final String tag, final boolean selected) {
        if (selected) {
            mSelfieSpotData.currentTags.add(tag);
        } else {
            mSelfieSpotData.currentTags.remove(tag);
        }

        // update the tags icon now that a tag was added/removed..
        if (CollectionUtils.isEmpty(mSelfieSpotData.currentTags)) {
            mTagImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_tag));
        } else {
            mTagImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_tag_populated));
        }
    }

    private void setLocation(final LatLng location) {
        mLocationImageView.setImageResource(R.drawable.ic_place_populated);
        mSelfieSpotData.currentLocationLat = location.latitude;
        mSelfieSpotData.currentLocationLong = location.longitude;
    }

    private SelfieSpotData convert(final SelfieSpot selfieSpot) {
        final SelfieSpotData data = new SelfieSpotData();
        data.currentName = selfieSpot.getName();

        if (CollectionUtils.isEmpty(selfieSpot.getTags())) {
            data.currentTags = new HashSet<>();
        } else {
            data.currentTags = new HashSet<>(selfieSpot.getTags());
        }

        data.currentImagePath = selfieSpot.getMediaFile().getUrl();
        data.currentImageWidth = selfieSpot.getMediaWidth();
        data.currentImageHeight = selfieSpot.getMediaHeight();
        data.currentLocationLat = selfieSpot.getPosition().latitude;
        data.currentLocationLong = selfieSpot.getPosition().longitude;

        return data;
    }

    // "transient" instance to be persisted across configuration changes, this is required because
    // SelfieSpot (& ParseObject) do NOT implement Parcelable/Serializable
    static class SelfieSpotData implements Serializable {
        static final long serialVersionUID = 1L;

        String currentName;

        // media
        String currentImagePath;
        int currentImageWidth;
        int currentImageHeight;

        // location
        Double currentLocationLat;
        Double currentLocationLong;

        // tags
        Set<String> currentTags = new HashSet<>();
    }
}
