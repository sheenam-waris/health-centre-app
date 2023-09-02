package com.example.healthcentre.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthcentre.LoginActivity;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.models.StaticDataProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class AdminEditUserActivity extends AppCompatActivity {

    private EditText nameET,emailET,genderET,phoneET,dobET;
    private Button editUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_user);
        confirmLogin();

        nameET = findViewById(R.id.name);
        emailET = findViewById(R.id.email_id);
        genderET = findViewById(R.id.gender_select);
        phoneET = findViewById(R.id.phone_num);
        dobET = findViewById(R.id.dob);

        // TODO: get user details as bundle populateDetails(user);
        Bundle bundle = new Bundle();
        bundle = getIntent().getExtras();
        String name = bundle.getString("name");
        String email = bundle.getString("email");
        String phone = bundle.getString("phone");
        String dob = bundle.getString("dob");
        String gender = bundle.getString("gender");
        int user_id = bundle.getInt("user_id");

        nameET.setText(name);
        emailET.setText(email);
        genderET.setText(gender);
        phoneET.setText(phone);
        dobET.setText(dob);

        editUserButton = findViewById(R.id.edit_details_admin_button);
        editUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkForEmptyField()){
                    updateUserProfile(user_id);
                }else{
                    Toast.makeText(getApplicationContext(),"Please Fill all details",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }

    public void updateUserProfile(int user_id){
        String gender = genderET.getText().toString().trim();
        String dob = dobET.getText().toString().trim();
        String phone = phoneET.getText().toString().trim();
        String name = nameET.getText().toString().trim();
        String email = emailET.getText().toString().trim();



        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("user_id",user_id);
            jsonObject.put("gender", gender);
            jsonObject.put("dob", dob);
            jsonObject.put("phone", phone);
            jsonObject.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // create PUT request to update user details


        final String requestBody = jsonObject.toString();
        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl() + "/user/admin-update-profile", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            switch (status) {
                                case "SUCCESS":
                                    Toast.makeText(getApplicationContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), AdminDashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                                case "FAILURE":
                                    String message = response.getString("message");
                                    Log.e("AdminEditUserActivity", message);
                                    Toast.makeText(getApplicationContext(), "Something Went wrong", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("AdminEditUserActivity", "JSONException: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error updating profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
        };
        // add request to request queue
        requestQueue.add(putRequest);
    }
    public boolean checkForEmptyField(){
        boolean x = nameET.getText().toString().trim().isEmpty() ||
                emailET.getText().toString().trim().isEmpty() ||
                phoneET.getText().toString().trim().isEmpty()||
                genderET.getText().toString().trim().isEmpty()||
                dobET.getText().toString().trim().isEmpty();
        return x;
    }
    private void confirmLogin(){
        if(UserSession.getInstance().isLoggedIn()){
            return;
        }else{
            //Intent for login
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}