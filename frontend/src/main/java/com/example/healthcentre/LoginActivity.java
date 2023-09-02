package com.example.healthcentre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.example.healthcentre.admin.AdminDashboardActivity;
import com.example.healthcentre.doctor.DoctorDashboardActivity;
import com.example.healthcentre.models.Patient;
import com.example.healthcentre.models.Role;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.models.User;
import com.example.healthcentre.staff.StaffDashboardActivity;
import com.example.healthcentre.student.StudentDashboardActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class LoginActivity extends AppCompatActivity {


    private EditText usernameEditText, passwordEditText;
    private CheckBox showPasswordCheckBox;
    private Button loginButton;


    private RequestQueue requestQueue;
    private StringRequest stringRequest;

    //private String loginUrl = "http://localhost:80/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        showPasswordCheckBox = findViewById(R.id.show_password_checkbox);

        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Show password
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // Hide password
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        //add and manage queue request
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                try {
                    login(username,password);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void login(String email,String password) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("email",email);
        object.put("password",password);
        final String requestBody = object.toString();
        stringRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl()+"/login", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    switch(status) {
                        case "SUCCESS":
                            JSONObject userObject = jsonObject.getJSONObject("user_data");
                            int user_id = userObject.getInt("user_id");
                            String name = userObject.getString("name");
                            String email = userObject.getString("email");
                            String phone = userObject.getString("phone");
                            String dob = userObject.getString("dob");
                            String gender = userObject.getString("gender");
                            String token = jsonObject.getString("token");
                            Role role = Role.values()[userObject.getInt("role")];
                            User user = new User(user_id,name,email,dob,gender,phone,role);
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("token",token);
                            editor.commit();
                            if(role == Role.STUDENT)
                            {
                                JSONObject patientObject = jsonObject.getJSONObject("patient_data");
                                String rollnum = patientObject.getString("rollno");
                                String address = patientObject.getString("address");
                                String hostel = patientObject.getString("hostel_details");
                                Patient patient;
                                patient = new Patient(rollnum,address,hostel);
                                user.setPatient(patient);
                            }
                            UserSession session = UserSession.getInstance();
                            session.setCurrentUser(user);
                            if(role == Role.STUDENT){
                                String message = "Successfully Login";
                                SpannableString spannableString = new SpannableString(message);
                                spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                // Show the toast with the modified message
                                Toast.makeText(getApplicationContext(), spannableString, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                                startActivity(intent);
                                finish();
                            }else if(role == Role.STAFF){
                                Intent intent = new Intent(LoginActivity.this, StaffDashboardActivity.class);
                                startActivity(intent);
                                finish();
                            }else if(role == Role.ADMIN){
                                Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                startActivity(intent);
                                finish();

                            }else if(role == Role.DOCTOR){
                                Intent intent = new Intent(LoginActivity.this, DoctorDashboardActivity.class);
                                startActivity(intent);
                                finish();

                            }else{
                                Toast.makeText(getApplicationContext(),"Invalid operation!",Toast.LENGTH_SHORT).show();
                            }
                            //Role role = jsonObject.getInt("u")
                            break;
                        case "FAILED":
                            String message = jsonObject.getString("message");
                           // Log.e("LoginActivity", message);
                            SpannableString spannableString = new SpannableString(message);
                            spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            // Show the toast with the modified message
                            Toast.makeText(getApplicationContext(), spannableString, Toast.LENGTH_LONG).show();

                            //Toast.makeText(getApplicationContext(),"Invalid credentials!",Toast.LENGTH_LONG).show();
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("LoginActivity", "JSONException: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("LoginActivity", "VolleyError: " + error.getMessage());
            }
        }) {
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

        requestQueue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}



        /*EditText edusername = findViewById(R.id.username);
        EditText edpassword = findViewById(R.id.password);
        Button btn = findViewById(R.id.loginButton);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edusername.getText().toString();
                String password = edpassword.getText().toString();
                DatabaseReference db = new DatabaseReference();
                if (username.length() == 0 || password.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please fill all details", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "LoginActivity Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                    startActivity(intent);
                }
            }
        });*/


           /*
        ***************************************************************************************
*/


// Authenticate the Patient's login credentials

// Check if the Patient's login credentials are valid


// Redirect the Patient to their account dashboard

// Display an error message to the Patient


        // Instantiate the RequestQueue
       /* RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://yourserver.com/login";

// Define the POST parameters
        Map<String, String> params = new HashMap<>();
        params.put("email", "example@email.com");
        params.put("password", "password123");

// Create the POST request
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url,
                new JSONObject(params), response -> {
            // Handle successful response
            try {
                String token = response.getString("token");
                redirectToDashboard(token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            // Handle error response
            if (error.networkResponse != null) {
                int statusCode = error.networkResponse.statusCode;
                String message = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                displayErrorMessage("Error " + statusCode + ": " + message);
            } else {
                displayErrorMessage("Network error");
            }
        });

// Add the request to the RequestQueue
        queue.add(postRequest);


        /*btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edusername.getText().toString();
                String password = edpassword.getText().toString();
                DatabaseReference db = new DatabaseReference();
                if(username.length()==0 || password.length()==0)
                {
                    Toast.makeText(getApplicationContext(), "Please fill all details",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "LoginActivity Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                    startActivity(intent);
                }
            }
        });*/
