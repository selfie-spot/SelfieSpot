package com.codepath.selfiespot.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.selfiespot.R;
import com.codepath.selfiespot.fragments.BookmarkedSelfiesFragment;
import com.codepath.selfiespot.fragments.MySelfiesFragment;
import com.codepath.selfiespot.util.ParseUserUtil;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ProfileActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 30;
    private static final int PROFILE_TRANSFORM_MARGIN = 0;
    private static final int PROFILE_TRANSFORM_RADIUS = 10;

    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @BindView(R.id.tl_profile)
    TabLayout mTabLayout;

    @BindView(R.id.tv_name)
    TextView mUserName;

    @BindView(R.id.iv_profile)
    ImageView mProfileImageView;

    @BindView(R.id.iv_cover_backdrop)
    ImageView mCoverImageView;

    @BindView(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.collapsing_tool_bar)
    CollapsingToolbarLayout mCollapsingToolbar;

    private int mMaxScrollSize;
    private boolean mIsAvatarShown = true;

    public static Intent createIntent(final Context context) {
        final Intent intent = new Intent(context, ProfileActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        mViewPager.setAdapter(new SelfieSpotsPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);

        mAppBarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = mAppBarLayout.getTotalScrollRange();

        populateUserDetails();
    }

    private void populateUserDetails() {
        final ParseUser parseUser = ParseUser.getCurrentUser();

        mUserName.setText(parseUser.getUsername());

        Glide.with(this)
                .load(ParseUserUtil.getCoverPictureUrl(parseUser))
                .centerCrop()
                .placeholder(R.drawable.ic_progress_indeterminate)
                .error(R.drawable.ic_error)
                .into(mCoverImageView);

        Glide.with(this)
                .load(ParseUserUtil.getProfilePictureUrl(parseUser))
                .bitmapTransform(new RoundedCornersTransformation(this, PROFILE_TRANSFORM_RADIUS, PROFILE_TRANSFORM_MARGIN))
                .placeholder(R.drawable.ic_progress_indeterminate)
                .error(R.drawable.ic_error)
                .into(mProfileImageView);
    }

    @Override
    public void onOffsetChanged(final AppBarLayout appBarLayout, final int i) {
        if (mMaxScrollSize == 0) {
            mMaxScrollSize = appBarLayout.getTotalScrollRange();
        }

        final int percentage = Math.abs(i * 100 / mMaxScrollSize);

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;
            mProfileImageView.animate().scaleY(0).scaleX(0).setDuration(200).start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;
            mProfileImageView.animate()
                    .scaleY(1).scaleX(1)
                    .start();
        }
    }

    class SelfieSpotsPagerAdapter extends FragmentPagerAdapter {
        private final int PAGE_COUNT = 2;
        private final int POSITION_MY_SELFIES = 0;
        private final int POSITION_BOOKMARK_SELFIES = 1;
        private String mTabTitles[] = new String[] {"My Selfies", "Bookmarks"};

        public SelfieSpotsPagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            switch (position) {
                case POSITION_MY_SELFIES: {
                    return MySelfiesFragment.createInstance();
                }
                case POSITION_BOOKMARK_SELFIES: {
                    return BookmarkedSelfiesFragment.createInstance();
                }
            }
            throw new IllegalStateException("Invalid position: " + position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
}