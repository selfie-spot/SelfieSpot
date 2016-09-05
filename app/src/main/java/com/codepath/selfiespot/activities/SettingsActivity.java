package com.codepath.selfiespot.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.SelfieSpotApplication;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// for now this will suffice instead of PreferenceActivity & PreferenceFragment etc
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    @Inject
    SharedPreferences mSharedPreferences;

    @BindView(R.id.tv_account_name)
    TextView mAccountNameTextView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    public static Intent createIntent(final Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SelfieSpotApplication.from(this).getComponent().inject(this);

        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_settings);

        final ParseUser currentUser = ParseUser.getCurrentUser();
        mAccountNameTextView.setText(currentUser.getUsername());
    }

    @OnClick(R.id.ll_user_container)
    void logout() {
        new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.sign_out)
                .setMessage(R.string.sign_out_warning)
                .setNegativeButton(R.string.sign_out_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        // no-op
                    }
                })
                .setPositiveButton(R.string.sign_out_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        doLogout();
                    }
                })
                .show();
    }

    private void doLogout() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(final ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Unable to logout", e);
                    Toast.makeText(SettingsActivity.this, "Unable to Logout", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Logout successful");
                    mSharedPreferences.edit().clear().commit();
                    startActivity(LoginActivity.createIntent(SettingsActivity.this));
                    finish();
                }
            }
        });
    }
}
