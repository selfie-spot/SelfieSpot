package com.codepath.selfiespot.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.SearchFilter;
import com.codepath.selfiespot.models.Tag;
import com.codepath.selfiespot.util.CollectionUtils;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.fiskur.chipcloud.ChipCloud;
import eu.fiskur.chipcloud.ChipListener;

public class FiltersDialogFragment extends BottomSheetDialogFragment implements ChipListener {
    private static final String TAG = FiltersDialogFragment.class.getSimpleName();
    private static final String ARG_FILTERS = FiltersDialogFragment.class.getSimpleName() + ":FILTERS";

//    private BottomSheetBehavior mBehavior;

    @BindView(R.id.v_fake_shadow)
    View mFakeShadow;

    @BindView(R.id.cc_tags)
    ChipCloud mTagsChipCloud;

    @BindView(R.id.ss_hide_no_likes)
    Switch mHideNoLikesSwitch;

    private FiltersCallback mFiltersCallback;

    private String[] mTags;

    private SearchFilter mSearchFilter;

    public static FiltersDialogFragment createInstance(final SearchFilter searchFilter) {
        final FiltersDialogFragment fragment = new FiltersDialogFragment();
        if (searchFilter != null) {
            final Bundle args = new Bundle();
            args.putSerializable(ARG_FILTERS, searchFilter);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().getSerializable(ARG_FILTERS) != null) {
            mSearchFilter = (SearchFilter) getArguments().getSerializable(ARG_FILTERS);
        } else {
            mSearchFilter = new SearchFilter();
        }
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
        mHideNoLikesSwitch.setChecked(mSearchFilter.isHideZeroLikes());
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

        // set the selected tags
        final Set<String> currentSelectedTags = mSearchFilter.getTags();
        if (! CollectionUtils.isEmpty(currentSelectedTags)) {
            for (int i = 0; i < mTags.length; i++) {
                if (currentSelectedTags.contains(mTags[i])) {
                    mTagsChipCloud.setSelectedChip(i);
                }
            }
        }

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