package com.codepath.selfiespot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.SelfieSpotApplication;
import com.codepath.selfiespot.repositories.PreferencesDAO;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {

    @Inject
    PreferencesDAO mPreferencesDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SelfieSpotApplication.from(this).getComponent().inject(this);

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
