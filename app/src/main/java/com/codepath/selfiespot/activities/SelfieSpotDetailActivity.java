package com.codepath.selfiespot.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.SelfieSpot;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelfieSpotDetailActivity extends AppCompatActivity {

    private static final String TAG = SelfieSpotDetailActivity.class.getSimpleName();

    private static final String EXTRA_SELFIE_SPOT_ID = SelfieSpotDetailActivity.class.getSimpleName() + ":SELFIE_SPOT_ID";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tvName)
    TextView tvName;

    @BindView(R.id.ivImage)
    ImageView ivImage;

    @BindView(R.id.btnRateMe)
    Button btnRateMe;

    @BindView(R.id.rbRatingBar)
    RatingBar rbRatingBar;

    @BindView(R.id.tvDesc)
    TextView tvDesc;


    public static Intent createIntent(final Context context, final String selfieSpotId) {
        final Intent intent = new Intent(context, SelfieSpotDetailActivity.class);
        if (!TextUtils.isEmpty(selfieSpotId)) {
            intent.putExtra(EXTRA_SELFIE_SPOT_ID, selfieSpotId);
        }
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_spot_detail);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final String selfieSpotId = getIntent().getStringExtra(EXTRA_SELFIE_SPOT_ID);

        if (selfieSpotId != null) {

            final ParseQuery<SelfieSpot> query = SelfieSpot.getQuery();
            query.getInBackground(selfieSpotId, new GetCallback<SelfieSpot>() {
                @Override
                public void done(final SelfieSpot selfieSpot, final ParseException e) {
                    if (e != null) {
                        Toast.makeText(SelfieSpotDetailActivity.this,
                                "Unable to retrieve SelfieSpot", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Unable to retrieve SelfieSpot: " + selfieSpotId, e);
                        finish();
                    } else {

                        Toast.makeText(SelfieSpotDetailActivity.this,
                                selfieSpot.getName(), Toast.LENGTH_SHORT).show();
                        tvName.setText(selfieSpot.getName());
                        tvDesc.setText(selfieSpot.getDescription());
                        rbRatingBar.setMax(5);

                        if(selfieSpot.getMediaFile() != null) {
                            ParseFile file = selfieSpot.getParseFile(String.valueOf(selfieSpot.getMediaFile()));
                            Uri fileUri = Uri.parse(file.getUrl());
                            Picasso.with(getApplicationContext()).load(fileUri.toString()).into(ivImage);
                            /*ParseFile file = selfieSpot.getMediaFile() */
                        }
                        else{
                           /* Picasso.with(getApplicationContext()).load(imageUri).fit().centerCrop()
                                    .placeholder(R.drawable.user_placeholder)
                                    .error(R.drawable.user_placeholder_error)
                                    .into(imageView);*/
                            Toast.makeText(getApplicationContext(), "Unable to load Image", Toast.LENGTH_LONG).show();

                        }

                    }
                }
            });

        }
    }
}
