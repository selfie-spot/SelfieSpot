package com.codepath.selfiespot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codepath.selfiespot.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    LoginButton loginButton;
    CallbackManager callbackManager;
    ParseUser parseUser;
    String name = null;
    String email = null;

    public static final List<String> mPermissions = new ArrayList<String>() {{
        add("public_profile");
        add("email");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        ParseFacebookUtils.initialize(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton)findViewById(R.id.login_button);
        //loginButton.setReadPermissions("user_status");
        loginButton.setReadPermissions("public_profile");

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, mPermissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e != null) {
                            Log.d("MyApp", "Uh oh. Error occurred" + e.toString());
                        }
                        else if(user == null){
                            Log.d("SelfiSpot", "The user cancelled the Facebook Login");
                        }
                        else if(user.isNew()){
                            Log.d("SelfiSpot", "User signed up and logged in through Facebook!");
                            getUserDetailsFromFaceBook();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else{
                            Log.d("SelfiSpot", "User signed up and logged in through Facebook!");
                                Log.d("MyApp", "User logged in through Facebook!");
                            getUserDetailsFromParse();


                        }

                    }
                });
            }
        });

       /* loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(LoginActivity.this, "SUCCESS", Toast.LENGTH_LONG).show();

                    if(AccessToken.getCurrentAccessToken() != null){
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
*/


    }

    private void getUserDetailsFromParse() {
        parseUser = ParseUser.getCurrentUser();
        email = parseUser.getEmail();
        name = parseUser.getUsername();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        //intent.putExtra("email", email);
        intent.putExtra("name", name);
        startActivity(intent);

    }

    private void getUserDetailsFromFaceBook() {

        Bundle params = new Bundle();
        params.putString("params", name);

       new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", params, HttpMethod.GET, new GraphRequest.Callback() {
           @Override
           public void onCompleted(GraphResponse response) {

               try {
                   email = response.getJSONObject().getString("email");
                   name = response.getJSONObject().getString("name");

               } catch (JSONException e) {
                   e.printStackTrace();
               }

               saveNewUser();
           }
       });

    }

    private void saveNewUser() {

        parseUser = ParseUser.getCurrentUser();
        parseUser.setEmail(email);
        parseUser.setUsername(name);
        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(LoginActivity.this, "New User " + name + "", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}