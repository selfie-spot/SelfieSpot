package com.codepath.selfiespot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.SelfieSpotApplication;
import com.codepath.selfiespot.repositories.PreferencesDAO;
import com.parse.ParseUser;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {

    TextView tvUserInfo;
    @Inject
    PreferencesDAO mPreferencesDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SelfieSpotApplication.from(this).getComponent().inject(this);
        tvUserInfo = (TextView) findViewById(R.id.tvUserInfo);
        tvUserInfo.setText(ParseUser.getCurrentUser().getUsername());


        findViewById(R.id.btn_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = SelfieSpotsMapActivity.createIntent(MainActivity.this);
                startActivity(intent);
            }
        });

//        testParse();
    }

    private void testParse() {
//        ParseObject testObject = new ParseObject("Test");
//        testObject.put("now", new Date().getTime());
//        testObject.saveInBackground();

    }
}
