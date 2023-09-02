package com.example.healthcentre;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthcentre.models.Patient;
import com.example.healthcentre.models.Role;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.models.User;
import com.example.healthcentre.student.RegisterActivity;
import com.example.healthcentre.student.StudentDashboardActivity;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    SignInClient oneTapClient;
    BeginSignInRequest signInRequest;
    RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button login =  findViewById(R.id.main_login_button);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        ActivityResultLauncher<IntentSenderRequest> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK){
                    try {
                        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                        String idToken = credential.getGoogleIdToken();
                        if (idToken !=  null) {
                            // Got an ID token from Google. Use it to authenticate
                            String email = credential.getId();
                            String Name = credential.getDisplayName();
                            Bundle bundle = new Bundle();
                            bundle.putString("email",email);
                            bundle.putString("name",Name);
                            if(email.contains("@nitc.ac.in"))
                            {
                                try{
                                    checkForAccountAndProceed(email,Name);

                                }catch (JSONException exception){
                                    Log.d(getClass().getCanonicalName(),exception.getMessage());
                                }
                            }
                            else{
                                String message = "Please Login Using NITC Email ID ";
                                SpannableString spannableString = new SpannableString(message);
                                spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                // Show the toast with the modified message
                                Toast.makeText(getApplicationContext(), spannableString, Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (ApiException e) {
                        // ...
                        e.printStackTrace();
                    }
                }
            }
        });



        Button register = findViewById(R.id.main_register_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<BeginSignInResult>() {
                            @Override
                            public void onSuccess(BeginSignInResult result) {
                                IntentSenderRequest intentSenderRequest =
                                        new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();

                                activityResultLauncher.launch(intentSenderRequest);

                            }
                        })
                        .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // No saved credentials found. Launch the One Tap sign-up flow, or
                                // do nothing and continue presenting the signed-out UI.
                                Log.d(TAG, e.getLocalizedMessage());
                            }
                        });
            }
        });
    }

    private void checkForAccountAndProceed(String email,String Name) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("email",email);
        final String requestBody = object.toString();
        StringRequest request = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl() + "/user/account-exists", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    Log.d(getClass().getCanonicalName(),"Status for account-check "+status + " for "+email);
                    if (status.equalsIgnoreCase("SUCCESS")) {
                        //Toast.makeText(getApplicationContext(), "You have already registered! Please login!", Toast.LENGTH_LONG).show();
                        JSONObject userObject = jsonObject.getJSONObject("user");
                        String token = jsonObject.getString("token");
                        int user_id = userObject.getInt("user_id");
                        String name = userObject.getString("name");
                        String email = userObject.getString("email");
                        String phone = userObject.getString("phone");
                        String dob = userObject.getString("dob");
                        String gender = userObject.getString("gender");
                        Role role = Role.values()[userObject.getInt("role")];
                        User user = new User(user_id,name,email,dob,gender,phone,role);
                        if(role == Role.STUDENT)
                        {
                            JSONObject patientObject = jsonObject.getJSONObject("patient");
                            String rollnum = patientObject.getString("rollno");
                            String address = patientObject.getString("address");
                            String hostel = patientObject.getString("hostel_details");
                            Patient patient;
                            patient = new Patient(rollnum,address,hostel);
                            user.setPatient(patient);
                        }
                        UserSession session = UserSession.getInstance();
                        session.setCurrentUser(user);

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("token",token);
                        editor.commit();
                        Intent intent = new Intent(MainActivity.this, StudentDashboardActivity.class);
                        startActivity(intent);
                        finish();
                        //Log.d(getClass().getCanonicalName(),)
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString("email",email);
                        bundle.putString("name",Name);
                        String message = "Hey, " + Name + "\nEmail Authenticate : " + email;
                        SpannableString spannableString = new SpannableString(message);
                        spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        // Show the toast with the modified message
                        Toast.makeText(getApplicationContext(), spannableString, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                }catch (JSONException exception){
                    Log.d(getClass().getCanonicalName(),exception.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(getClass().getCanonicalName(),"ho jao yaar");
            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
        };
        requestQueue.add(request);
    }
}