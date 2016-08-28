package com.codepath.selfiespot.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.selfiespot.R;
import com.codepath.selfiespot.fragments.MySelfiesFragment;
import com.codepath.selfiespot.fragments.SavedSelfiesFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.tabs)
    PagerSlidingTabStrip tabsStrip;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    public static Intent createIntent(final Context context) {
        final Intent intent = new Intent(context, ProfileActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        viewPager.setAdapter(new SelfieSpotsPagerAdapter(getSupportFragmentManager()));
        tabsStrip.setViewPager(viewPager);

    }


    public class SelfieSpotsPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[] {"My Selfies", "Saved Selfies"};

        public SelfieSpotsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new MySelfiesFragment();
            } else if (position == 1) {
                return new SavedSelfiesFragment();
            } else {
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }
    }

}
