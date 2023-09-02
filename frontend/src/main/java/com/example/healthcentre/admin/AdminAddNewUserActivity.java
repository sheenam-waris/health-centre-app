package com.example.healthcentre.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.healthcentre.LoginActivity;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.models.StaticDataProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class AdminAddNewUserActivity extends AppCompatActivity {

    private TextView titleTextview;
    private EditText nameET,emailET,genderET,phoneET,dobET;
    private Button addnewButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_user);
        confirmLogin();
        //get text from previous activity
        Bundle bundle = getIntent().getExtras();
        int role = bundle.getInt("role");
        String titleText = "";
        if(role==2){
            titleText = "Add New Doctor";
        } else if (role == 1) {
            titleText = "Add New Staff";
        }

        titleTextview = findViewById(R.id.add_user_title);
        titleTextview.setText(titleText);

        nameET = findViewById(R.id.name);
        emailET = findViewById(R.id.email_id);
        genderET = findViewById(R.id.gender_select);
        phoneET = findViewById(R.id.phone_num);
        dobET = findViewById(R.id.dob);



        addnewButton = findViewById(R.id.add_new_button);
        addnewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkForEmptyField()){
                       addUser(role);
                }else{
                    Toast.makeText(getApplicationContext(),"Please Fill all details",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

    }

    public void addUser(int role){
        String name = nameET.getText().toString().trim();
        String gender = genderET.getText().toString().trim();
        String dob = dobET.getText().toString().trim();
        String phone = phoneET.getText().toString().trim();
        String email = emailET.getText().toString().trim();

        // TODO: create one password Pattern
        // Password = FirstName_YYYY
        String password = generateFirstPassword(name,dob);

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("name", name);
            jsonObject.put("password", password);
            jsonObject.put("gender", gender);
            jsonObject.put("dob", dob);
            jsonObject.put("phone", phone);
            jsonObject.put("role", role);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String requestBody = jsonObject.toString();
        StringRequest postRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl() + "/user/create-account",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle successful response from server

                        try {
                            JSONObject Object = new JSONObject(response);
                            String status = Object.getString("status");
                            switch (status){
                                case "SUCCESS" :
                                    String messageS = Object.getString("message");
                                    Toast.makeText(getApplicationContext(),messageS,Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(AdminAddNewUserActivity.this, AdminDashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                                case "FAILURE" :
                                    String messageF = Object.getString("message");
                                    Log.e("AdminAddNewUserActivity", messageF);
                                    Toast.makeText(getApplicationContext(),"Something Went wrong",Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("RegisterActivity", "JSONException: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response from server
                    }
                }
        ) {
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
        requestQueue.add(postRequest);
    }

    private String generateFirstPassword(String name,String dob){
        String password ="";
        String delimeter = "_";
        password += name.split(" ")[0];
        password += delimeter;
        password += dob.split("/")[2];
        return password;
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