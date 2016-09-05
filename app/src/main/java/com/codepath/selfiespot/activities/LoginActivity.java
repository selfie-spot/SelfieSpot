package com.codepath.selfiespot.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.util.ParseUserUtil;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private Button loginButton;

    public static Intent createIntent(final Context context) {
        final Intent intent = new Intent(context, LoginActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // already logged-in, directly go to home-activity
        if (ParseUser.getCurrentUser() != null) {
            navigateToMain();
        }


        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);

        loginButton = (Button) findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<String> permissions = Arrays.asList("public_profile", "email");
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e != null) {
                            Log.d(TAG, "Uh oh. Error occurred" + e.toString());
                            Toast.makeText(LoginActivity.this, "Unable to login", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (user == null) {
                            Log.d(TAG, "The user cancelled the Facebook Login");
                            Toast.makeText(LoginActivity.this, "User cancelled login", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            getUserDetailsFromFaceBook();
                            Log.d(TAG, "User signed up and logged in through Facebook!");
                        }
                    }
                });
            }
        });
    }

    private void navigateToMain() {
        final Intent intent = SelfieSpotsMapActivity.createIntent(this);
        startActivity(intent);
        finish();
    }

    private void getUserDetailsFromFaceBook() {
        final Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(240).height(240),cover");
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", parameters, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(final GraphResponse response) {
                try {
                    final String email = response.getJSONObject().getString("email");
                    final String name = response.getJSONObject().getString("name");
                    final String profilePic = response.getJSONObject()
                            .getJSONObject("picture")
                            .getJSONObject("data")
                            .getString("url");
                    final String coverPic = response.getJSONObject()
                            .getJSONObject("cover")
                            .getString("source");
                    saveNewUser(email, name, profilePic, coverPic);
                    Toast.makeText(LoginActivity.this, "User logged-in & saved", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } catch (final JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Error retrieving facebook info", Toast.LENGTH_SHORT).show();
                }
            }
        }).executeAsync();
    }

    private void saveNewUser(final String email,
                             final String name,
                             final String profilePic,
                             final String coverPic) {
        final ParseUser parseUser = ParseUser.getCurrentUser();
        parseUser.setEmail(email);
        parseUser.setUsername(name);
        ParseUserUtil.setProfilePictureUrl(parseUser, profilePic);
        ParseUserUtil.setCoverPictureUrl(parseUser, coverPic);
        parseUser.saveEventually();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}