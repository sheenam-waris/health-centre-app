package com.example.healthcentre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
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

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String token = prefs.getString("token","none");
                if(token.equalsIgnoreCase("none")){
                    gotoLogin();
                }else{
                    loginWithToken(token);
                }
            }
        },3000);
    }

    private void loginWithToken(String token){
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject object = new JSONObject();
        try {
            object.put("token",token);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        final String requestBody = object.toString();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl()+"/login/alive", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(status.equalsIgnoreCase("SUCCESS")){
                        JSONObject userObject = jsonObject.getJSONObject("user_data");
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
                            Intent intent = new Intent(SplashScreen.this, StudentDashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }else if(role == Role.STAFF){
                            Intent intent = new Intent(SplashScreen.this, StaffDashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }else if(role == Role.ADMIN){
                            Intent intent = new Intent(SplashScreen.this, AdminDashboardActivity.class);
                            startActivity(intent);
                            finish();

                        }else if(role == Role.DOCTOR){
                            Intent intent = new Intent(SplashScreen.this, DoctorDashboardActivity.class);
                            startActivity(intent);
                            finish();

                        }else{
                            Toast.makeText(getApplicationContext(),"Invalid operation!",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        //Toast.makeText(getApplicationContext(),"Session has expired, please login again",Toast.LENGTH_LONG).show();
                        gotoLogin();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(getClass().getCanonicalName(), "JSONException: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("StaffPendingAppointmentActivity", "VolleyError: " + error.getMessage());
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

    private void gotoLogin(){
        Intent intent = new Intent(SplashScreen.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}