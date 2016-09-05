package com.codepath.selfiespot.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.SearchFilter;
import com.codepath.selfiespot.models.Tag;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.fiskur.chipcloud.ChipCloud;
import eu.fiskur.chipcloud.ChipListener;

public class FiltersDialogFragment extends BottomSheetDialogFragment implements ChipListener {
    private static final String TAG = FiltersDialogFragment.class.getSimpleName();

//    private BottomSheetBehavior mBehavior;

    @BindView(R.id.v_fake_shadow)
    View mFakeShadow;

    @BindView(R.id.cc_tags)
    ChipCloud mTagsChipCloud;

    @BindView(R.id.ss_hide_no_likes)
    Switch mHideNoLikesSwitch;

    private FiltersCallback mFiltersCallback;

    private String[] mTags;

    private SearchFilter mSearchFilter = new SearchFilter();

    public static FiltersDialogFragment createInstance() {
        return new FiltersDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final View view = View.inflate(getContext(), R.layout.fragment_filters, null);
        dialog.setContentView(view);
//        mBehavior = BottomSheetBehavior.from((View) view.getParent());

        ButterKnife.bind(this, view);
        mFakeShadow.setVisibility(View.GONE);
        mHideNoLikesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
                mSearchFilter.setHideZeroLikes(isChecked);
            }
        });
        populateTags();

        return dialog;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "FiltersDialogFragment dismissed");
        if (mFiltersCallback != null) {
            mFiltersCallback.setFilters(mSearchFilter);
        }
    }

    @Override
    public void chipSelected(final int position) {
        final String tag = getTag(position);
        mSearchFilter.addTag(tag);
        Log.d(TAG, "tag selected: " + tag);
    }

    @Override
    public void chipDeselected(final int position) {
        final String tag = getTag(position);
        mSearchFilter.removeTag(tag);
        Log.d(TAG, "tag deselected: " + tag);
    }

    private void populateTags() {
        mTags = Tag.getAllTagsAsStringArray();
        mTagsChipCloud.addChips(mTags);
        mTagsChipCloud.setChipListener(this);
    }

    private String getTag(final int position) {
        return mTags[position];
    }

    public void setFiltersCallback(final FiltersCallback filtersCallback) {
        mFiltersCallback = filtersCallback;
    }

//    @Override
//    public int getTheme() {
//        return android.R.style.Theme_Translucent_NoTitleBar;
//    }

    public interface FiltersCallback {
        void setFilters(SearchFilter searchFilter);
    }
}