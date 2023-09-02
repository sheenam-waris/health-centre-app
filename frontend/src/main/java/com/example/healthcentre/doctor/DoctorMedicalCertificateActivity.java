package com.example.healthcentre.doctor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
import com.example.healthcentre.appointmenttabactivity.DoctorMedicalCertificateViewAdapter;
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.models.MedicalCertificate;
import com.example.healthcentre.models.StaticDataProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class DoctorMedicalCertificateActivity extends AppCompatActivity {

    private RecyclerView doctorMedCertRCV;
    private ArrayList<MedicalCertificate> medicalCertificateArrayList;
    private DoctorMedicalCertificateViewAdapter doctorMedicalCertificateViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_medical_certificate);
        confirmLogin();
        doctorMedCertRCV = findViewById(R.id.recyclerview_medical_view_doctor);
        doctorMedCertRCV.setLayoutManager(new LinearLayoutManager(DoctorMedicalCertificateActivity.this));

        medicalCertificateArrayList = new ArrayList<>();

        doctorMedicalCertificateViewAdapter = new DoctorMedicalCertificateViewAdapter(medicalCertificateArrayList,this);
        doctorMedCertRCV.setAdapter(doctorMedicalCertificateViewAdapter);
        runFetchMedicalCertificatesBackground();

    }



    private void fetchAllMedicalCertificates() throws JSONException {
        JSONObject object = new JSONObject();
        //object.put("user_id", UserSession.getInstance().getCurrentUser().getUserId());
        final String requestBody = object.toString();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, StaticDataProvider.getApiBaseUrl() + "/med-cert", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseJson = new JSONObject(response);
                    String status = responseJson.getString("status");
                    switch(status){
                        case "SUCCESS":
                            JSONArray certificates = responseJson.getJSONArray("certificates");
                            for(int i=0;i<certificates.length();i++){
                                JSONObject obj = certificates.getJSONObject(i);
                                int medcert_id = obj.getInt("mc_id");
                                if(medicalCertificateArrayList.stream().filter(a -> {
                                    if(a.getMedCert_id() == medcert_id)return true;
                                    else return false;
                                }).count()>0){
                                    continue;
                                }
                                int appt_id = obj.getInt("app_id");
                                int patient_id = obj.getInt("patient_id");
                                String purpose = obj.getString("purpose");
                                String medcertStatus =  obj.getString("status");
                                String duration = obj.getString("duration");
                                String approved_at = obj.getString("approved_at");
                                String requested_at = obj.getString("requested_at");
                                MedicalCertificate a = new MedicalCertificate(medcert_id,appt_id,patient_id,purpose,duration,medcertStatus,approved_at,requested_at);

                                if(a.getStatus().equalsIgnoreCase("pending"))
                                {medicalCertificateArrayList.add(a);}
                                Log.d(getClass().getCanonicalName(), "Appointment reason : " + medicalCertificateArrayList.size());
                            }
                            doctorMedicalCertificateViewAdapter.setMedCertData(medicalCertificateArrayList);
//                            Log.d(getClass().getCanonicalName(),filteredAppointments.size()+"");
                           doctorMedicalCertificateViewAdapter.notifyDataSetChanged();
                            break;
                        case "Failed":
                            String message = responseJson.getString("message");
                            Log.e("DoctorMedicalCertificateActivity", message);
                            break;
                        default:
                            Log.e("Doctor MedicalCertificates : ", "LOL");
                            break;

                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("DoctorMedicalCertificateActivity", "VolleyError: " + error.getMessage());
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

    public void runFetchMedicalCertificatesBackground(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    fetchAllMedicalCertificates();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
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