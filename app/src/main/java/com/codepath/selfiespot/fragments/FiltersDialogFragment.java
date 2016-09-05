package com.codepath.selfiespot.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.View;

import com.codepath.selfiespot.R;
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

    private String[] mTags;

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final View view = View.inflate(getContext(), R.layout.fragment_filters, null);
        dialog.setContentView(view);
//        mBehavior = BottomSheetBehavior.from((View) view.getParent());

        ButterKnife.bind(this, view);
        mFakeShadow.setVisibility(View.GONE);
        populateTags();

        return dialog;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "FiltersDialogFragment dismissed");
    }

    @Override
    public void chipSelected(final int i) {
        Log.d(TAG, "chip selected: " + mTags[i]);
    }

    @Override
    public void chipDeselected(final int i) {
        Log.d(TAG, "chip deselected: " + mTags[i]);
    }

    private void populateTags() {
        mTags = Tag.getAllTagsAsStringArray();
        mTagsChipCloud.addChips(mTags);
        mTagsChipCloud.setChipListener(this);
    }

//    @Override
//    public int getTheme() {
//        return android.R.style.Theme_Translucent_NoTitleBar;
//    }
}