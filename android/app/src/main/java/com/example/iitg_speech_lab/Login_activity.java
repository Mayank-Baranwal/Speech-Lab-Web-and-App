package com.example.iitg_speech_lab;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.microsoft.identity.client.*;
import java.util.List;
import com.microsoft.identity.client.exception.*;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Login_activity extends AppCompatActivity{

    /* Azure AD v2 Configs */
    final static String SCOPES [] = {"https://graph.microsoft.com/User.Read"};
    final static String MSGRAPH_URL = "https://graph.microsoft.com/v1.0/me";

    /* UI & Debugging Variables */
    private static final String TAG = Login_activity.class.getSimpleName();
    Button callGraphButton;
    Button signOutButton;

    /* Azure AD Variables */
    private PublicClientApplication sampleApp;
    private AuthenticationResult authResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);

        callGraphButton = (Button) findViewById(R.id.callGraph);
        signOutButton = (Button) findViewById(R.id.clearCache);

        callGraphButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onCallGraphClicked();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSignOutClicked();
            }
        });

        /* Configure your sample app and save state for this activity */
        sampleApp = null;
        if (sampleApp == null) {
            sampleApp = new PublicClientApplication(this.getApplicationContext(),
                    R.raw.auth_config);
        }

        /* Attempt to get a user and acquireTokenSilent
         * If this fails we do an interactive request
         */
        List<IAccount> accounts = null;

        try {
            accounts = sampleApp.getAccounts();

            if (accounts != null && accounts.size() == 1) {
                /* We have 1 account */

                sampleApp.acquireTokenSilentAsync(SCOPES, accounts.get(0), getAuthSilentCallback());
            } else {
                /* We have no account or >1 account */
            }
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "Account at this position does not exist: " + e.toString());
        }

    }

    //
    // Core Identity methods used by MSAL
    // ==================================
    // onActivityResult() - handles redirect from System browser
    // onCallGraphClicked() - attempts to get tokens for graph, if it succeeds calls graph & updates UI
    // onSignOutClicked() - Signs account out of the app & updates UI
    // callGraphAPI() - called on successful token acquisition which makes an HTTP request to graph
    //

    /* Handles the redirect from the System Browser */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        sampleApp.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }

    /* Use MSAL to acquireToken for the end-user
     * Callback will call Graph api w/ access token & update UI
     */
    private void onCallGraphClicked() {
        sampleApp.acquireToken(getActivity(), SCOPES, getAuthInteractiveCallback());
    }

    /* Clears an account's tokens from the cache.
     * Logically similar to "sign out" but only signs out of this app.
     */
    private void onSignOutClicked() {

        /* Attempt to get a account and remove their cookies from cache */
        List<IAccount> accounts = null;

        try {
            accounts = sampleApp.getAccounts();

            if (accounts == null) {
                /* We have no accounts */

            } else if (accounts.size() == 1) {
                /* We have 1 account */
                /* Remove from token cache */
                sampleApp.removeAccount(accounts.get(0));
                updateSignedOutUI();

            }
            else {
                /* We have multiple accounts */
                for (int i = 0; i < accounts.size(); i++) {
                    sampleApp.removeAccount(accounts.get(i));
                }
            }

            Toast.makeText(getBaseContext(), "Signed Out!", Toast.LENGTH_SHORT)
                    .show();

        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "User at this position does not exist: " + e.toString());
        }
    }

    /* Use Volley to make an HTTP request to the /me endpoint from MS Graph using an access token */
    private void callGraphAPI() {
        Log.d(TAG, "Starting volley request to graph");

        /* Make sure we have a token to send to graph */
        if (authResult.getAccessToken() == null) {return;}

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("key", "value");
        } catch (Exception e) {
            Log.d(TAG, "Failed to put parameters: " + e.toString());
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, MSGRAPH_URL,
                parameters,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                /* Successfully called graph, process data and send to UI */
                Log.d(TAG, "Response: " + response.toString());

                updateGraphUI(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authResult.getAccessToken());
                return headers;
            }
        };

        Log.d(TAG, "Adding HTTP GET to Queue, Request: " + request.toString());

        request.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }
    private DatabaseReference mdatabase;
    //
    // Helper methods manage UI updates
    // ================================
    // updateGraphUI() - Sets graph response in UI
    // updateSuccessUI() - Updates UI when token acquisition succeeds
    // updateSignedOutUI() - Updates UI when app sign out succeeds
    //

    /* Sets the graph response */
    private void updateGraphUI(JSONObject graphResponse) {
        TextView graphText = (TextView) findViewById(R.id.graphData);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String username = graphResponse.optString("mail").replace("@iitg.ac.in", "");
        final Map<String, Object> newUser = new HashMap<>();
        final Map<String, Object> Url = new HashMap<>();
        String des = graphResponse.optString("jobTitle").toLowerCase();
        newUser.put("About", "");
        newUser.put("Contact", "");
        newUser.put("Department", graphResponse.optString("Department").toLowerCase());

        final String designation ;
        if(des.equalsIgnoreCase("btech") || des.equalsIgnoreCase("mtech") ||
                des.equalsIgnoreCase("bdes") || des.equalsIgnoreCase("mdes")
                || des.equalsIgnoreCase("phd"))
        {
            designation = "Student";
        }
        else
            designation="Faculty";

        newUser.put("Designation", designation);
        newUser.put("Email", graphResponse.optString("mail"));
        newUser.put("FullName", graphResponse.optString("displayName").toUpperCase());
        newUser.put("ProfilePic", "");
        if (designation.equalsIgnoreCase("Student"))
            newUser.put("Program", des);
        else
            newUser.put("CollegeDesignation", des);
        newUser.put("RollNumber", graphResponse.optString("surname"));
        Url.put("Github", "");
        Url.put("Homepage", "");
        Url.put("Linkedin", "");
        newUser.put("URL", Url);
        DocumentReference docref= db.collection("Users").document(username);
        docref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot user = task.getResult();
                            if(user.exists()){
                                Log.d(TAG, "Already Added");
                            }
                            else{
                                db.collection("Users").document(username).set(newUser);
                            }
                        }
                    }
                });
        graphText.setText(graphResponse.optString("surname")+graphResponse.toString()+username);
    }

    /* Set the UI for successful token acquisition data */
    private void updateSuccessUI() {
        callGraphButton.setVisibility(View.INVISIBLE);
        signOutButton.setVisibility(View.VISIBLE);
        findViewById(R.id.welcome).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.welcome)).setText("Welcome, " +
                authResult.getAccount().getUsername());
        findViewById(R.id.graphData).setVisibility(View.VISIBLE);
    }

    /* Set the UI for signed out account */
    private void updateSignedOutUI() {
        callGraphButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.INVISIBLE);
        findViewById(R.id.welcome).setVisibility(View.INVISIBLE);
        findViewById(R.id.graphData).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.graphData)).setText("No Data");
    }

    //
    // App callbacks for MSAL
    // ======================
    // getActivity() - returns activity so we can acquireToken within a callback
    // getAuthSilentCallback() - callback defined to handle acquireTokenSilent() case
    // getAuthInteractiveCallback() - callback defined to handle acquireToken() case
    //

    public Activity getActivity() {
        return this;
    }

    /* Callback used in for silent acquireToken calls.
     * Looks if tokens are in the cache (refreshes if necessary and if we don't forceRefresh)
     * else errors that we need to do an interactive request.
     */
    private AuthenticationCallback getAuthSilentCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                /* Successfully got a token, call graph now */
                Log.d(TAG, "Successfully authenticated");

                /* Store the authResult */
                authResult = authenticationResult;

                /* call graph */
                callGraphAPI();

                /* update the UI to post call graph state */
                updateSuccessUI();
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());

                if (exception instanceof MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                } else if (exception instanceof MsalUiRequiredException) {
                    /* Tokens expired or no session, retry with interactive */
                }
            }

            @Override
            public void onCancel() {
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
            }
        };
    }

    /* Callback used for interactive request.  If succeeds we use the access
     * token to call the Microsoft Graph. Does not check cache
     */
    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                /* Successfully got a token, call graph now */
                Log.d(TAG, "Successfully authenticated");
                Log.d(TAG, "ID Token: " + authenticationResult.getIdToken());

                /* Store the auth result */
                authResult = authenticationResult;

                /* call graph */
                callGraphAPI();

                /* update the UI to post call graph state */
                updateSuccessUI();
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());

                if (exception instanceof MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                }
            }

            @Override
            public void onCancel() {
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
            }
        };
    }
}
