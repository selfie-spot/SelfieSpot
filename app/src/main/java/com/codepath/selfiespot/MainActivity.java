package com.codepath.selfiespot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.parse.ParseObject;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseObject testObject = new ParseObject("Test");
        testObject.put("now", new Date().getTime());
        testObject.saveInBackground();
    }
}
