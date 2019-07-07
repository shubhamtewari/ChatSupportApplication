package com.example.chatsupportapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicommons.json.GsonUtils;
import com.example.chatsupportapplication.structure.ApiKey;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import io.kommunicate.KmChatBuilder;
import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KMLoginHandler;
import io.kommunicate.callbacks.KMLogoutHandler;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.users.KMUser;

public class MainActivity extends AppCompatActivity {

    Button buttonSignIn;
    TextView textViewUser;

    KMUser kmUser;
    GoogleSignInAccount account;
    GoogleSignInClient mGoogleSignInClient;

    final static int RC_SIGN_IN = 3434;

    void updateUI(GoogleSignInAccount account) {
        if(account == null) {
            textViewUser.setText("Login with your google account.");
        } else {
            textViewUser.setText("Logged in using "+account.getEmail());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        setSupportActionBar(toolbar);

        buttonSignIn = findViewById(R.id.idButtonSignIn);
        textViewUser = findViewById(R.id.idTextVewLogin);

        //update ui according to if user is signed in or not
        updateUI(account);

        //api key
        String json = "{\n" +
                "  \"apiKey\" : \"hoTMHdBvZmxqxWf2SZ15x5ZG17rgBXRs\",\n" +
                "  \"appid\" : \"1b00b8bced4c8763a20c6f5740dc62fc0\"\n" +
                "}";

        //initialize the kommunicate sdk after getting api key
        final ApiKey apiKeyobj = (ApiKey) GsonUtils.getObjectFromJson(json, ApiKey.class);
        Kommunicate.init(MainActivity.this, apiKeyobj.getApiKey());
        Log.d("MainActivityLog", "Kommunicate SDK initialized with Api key: "+apiKeyobj.getApiKey()+" for App id: "+apiKeyobj.getAppid()+".");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //google sign-in
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        //to open support chat
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> arrayListAgents = new ArrayList();
                arrayListAgents.add("shbmtewari@gmail.com");
                if (!Kommunicate.isLoggedIn(getApplicationContext())) {
                    new KmChatBuilder(MainActivity.this).setAgentIds(arrayListAgents).setWithPreChat(true).setApplicationId(apiKeyobj.getAppid()).setChatName("Customer Support").launchChat(new KmCallback() {
                        @Override
                        public void onSuccess(Object message) {
                            Log.d("MainActivityLog", "Sucessfully created a visitor chat and launched it.");
                        }

                        @Override
                        public void onFailure(Object error) {
                            Log.d("MainActivityLog", "Error creating visitor chat.\n" + error.toString());
                        }
                    });
                } else {
                    kmUser = KMUser.getLoggedInUser(getApplicationContext());
                    new KmChatBuilder(MainActivity.this).setAgentIds(arrayListAgents).setKmUser(kmUser).setApplicationId(apiKeyobj.getAppid()).setChatName("Customer Support").launchChat(new KmCallback() {
                        @Override
                        public void onSuccess(Object message) {
                            Log.d("MainActivityLog", "Sucessfully created a user chat and launched it.");
                        }

                        @Override
                        public void onFailure(Object error) {
                            Log.d("MainActivityLog", "Error creating user chat.\n" + error.toString());
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                kmUser = new KMUser();
                kmUser.setUserId(account.getId());
                kmUser.setUserName(account.getDisplayName());
                kmUser.setEmail(account.getEmail());
                if(account.getPhotoUrl()!=null) {
                    kmUser.setImageLink(account.getPhotoUrl().toString());
                }
                Kommunicate.login(getApplicationContext(), kmUser, new KMLoginHandler() {
                    @Override
                    public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                        updateUI(account);
                        Toast.makeText(getApplicationContext(), "Signed in successfully.", Toast.LENGTH_LONG).show();
                        Log.w("MainActivityLog", "User email: "+kmUser.getEmail());
                        Log.w("MainActivityLog", "User display name: "+kmUser.getUserName());
                    }

                    @Override
                    public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                        mGoogleSignInClient.signOut();
                    }
                });

            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Google sign-in failed.", Toast.LENGTH_LONG).show();
                Log.w("MainActivityLog", "Google sign in failed", e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            if(mGoogleSignInClient == null && account == null) {
                //already logged out, do nothing
                Toast.makeText(getApplicationContext(), "Please login.", Toast.LENGTH_LONG).show();
            }else {
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Kommunicate.logout(getApplicationContext(), new KMLogoutHandler() {
                                    @Override
                                    public void onSuccess(Context context) {
                                        Toast.makeText(getApplicationContext(), "Successfully logged out.", Toast.LENGTH_LONG).show();
                                        updateUI(account);
                                        kmUser = null;
                                    }

                                    @Override
                                    public void onFailure(Exception exception) {
                                        Toast.makeText(getApplicationContext(), "Logout failed.", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
