package com.codepath.selfiespot.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.Tag;
import com.codepath.selfiespot.util.CollectionUtils;

import java.io.Serializable;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.fiskur.chipcloud.ChipCloud;
import eu.fiskur.chipcloud.ChipListener;

public class TagsDialogFragment extends DialogFragment implements ChipListener {
    private static final String ARG_TAGS = TagsDialogFragment.class.getSimpleName() + ":TAGS";
    private static final String TAG = TagsDialogFragment.class.getSimpleName();

    @BindView(R.id.v_fake_shadow)
    View mFakeShadow;

    @BindView(R.id.cc_tags)
    ChipCloud mTagsChipCloud;

    private String[] mTags;
    private Set<String> mSelectedTags;

    private TagsCallback mTagsCallback;

    public static TagsDialogFragment createInstance(final Set<String> selectedTags) {
        final TagsDialogFragment fragment = new TagsDialogFragment();
        if (selectedTags != null) {
            final Bundle args = new Bundle();
            args.putSerializable(ARG_TAGS, (Serializable) selectedTags);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().getSerializable(ARG_TAGS) != null) {
            mSelectedTags = (Set<String>) getArguments().getSerializable(ARG_TAGS);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tags, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFakeShadow.setVisibility(View.GONE);
        populateTags();
    }

    private void populateTags() {
        mTags = Tag.getAllTagsAsStringArray();
        mTagsChipCloud.addChips(mTags);

        // set the selected tags
        if (! CollectionUtils.isEmpty(mSelectedTags)) {
            for (int i = 0; i < mTags.length; i++) {
                if (mSelectedTags.contains(mTags[i])) {
                    mTagsChipCloud.setSelectedChip(i);
                }
            }
        }

        mTagsChipCloud.setChipListener(this);
    }

    public void setTagsCallback(final TagsCallback tagsCallback) {
        mTagsCallback = tagsCallback;
    }

    @Override
    public void chipSelected(final int position) {
        final String tag = getTag(position);
        if (mTagsCallback != null) {
            mTagsCallback.onTagSelected(tag);
        }
        Log.d(TAG, "tag selected: " + tag);
    }

    @Override
    public void chipDeselected(final int position) {
        final String tag = getTag(position);
        if (mTagsCallback != null) {
            mTagsCallback.onTagDeselected(tag);
        }
        Log.d(TAG, "tag deselected: " + tag);
    }

    private String getTag(final int position) {
        return mTags[position];
    }

    public interface TagsCallback {
        void onTagSelected(final String tag);

        void onTagDeselected(final String tag);
    }
}