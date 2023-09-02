package com.example.healthcentre.doctor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.appointmenttabactivity.ViewPatientProfileViewAdapter;
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.models.Patient;
import com.example.healthcentre.models.Role;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.models.User;
import com.example.healthcentre.utils.SortByDate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

public class ViewPatientProfileActivity extends AppCompatActivity {

    private  TextView namePatient,rollPatient,genderPatient,dobPatient,phonePatient,addressPatient,hostelDetailsPatient;
    private ImageView profileView;
    private RequestQueue requestQueue;
    private StringRequest stringRequestPatient;
    private User user;
    ArrayList<Appointment> appointmentsArrayList;
    RecyclerView patientAppointmentRCV;
    ViewPatientProfileViewAdapter viewPatientProfileViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient_profile);

        patientAppointmentRCV = findViewById(R.id.recyclerview_previous_appointment_patient_doctor);
        patientAppointmentRCV.setLayoutManager(new LinearLayoutManager(this));

         namePatient = findViewById(R.id.tv_username);
         profileView = findViewById(R.id.profile_icon);
        /* rollPatient = findViewById(R.id.tv_roll_num);
         genderPatient = findViewById(R.id.tv_gender);
         dobPatient = findViewById(R.id.tv_birth_date);
         phonePatient =findViewById(R.id.tv_phone_num);
         addressPatient = findViewById(R.id.tv_address);
        hostelDetailsPatient = findViewById(R.id.tv_hostel_details);*/

        Bundle bundle = getIntent().getExtras();
        int user_id = bundle.getInt("user_id");
        setProfileDetail(user_id);

        profileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog profileView = new Dialog(v.getContext());
                profileView.setContentView(R.layout.student_profile_dialogue_box);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(profileView.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;

                profileView.show();
                profileView.getWindow().setAttributes(lp);

                TextView name = profileView.findViewById(R.id.tv_username);
                rollPatient = profileView.findViewById(R.id.tv_roll_num);
                genderPatient = profileView.findViewById(R.id.tv_gender);
                dobPatient = profileView.findViewById(R.id.tv_birth_date);
                phonePatient = profileView.findViewById(R.id.tv_phone_num);
               addressPatient = profileView.findViewById(R.id.tv_address);
                hostelDetailsPatient = profileView.findViewById(R.id.tv_hostel_details);

                name.setText(user.getName());
                genderPatient.setText(user.getGender());
                dobPatient.setText(user.getDob());
                phonePatient.setText(user.getPhone());
                rollPatient.setText(user.getPatient().getRollNum());
                addressPatient.setText(user.getPatient().getAddress());
                hostelDetailsPatient.setText(user.getPatient().getHostel());
                // address.setText(UserSession.getInstance().getCurrentUser().getPatient().getAddress());
                //hostelDetails.setText(UserSession.getInstance().getCurrentUser().getPatient().getHostel());

                ImageView editProfile = profileView.findViewById(R.id.edit_profile_icon);
                editProfile.setVisibility(View.GONE);
            }
        });
        appointmentsArrayList = new ArrayList<>();
        Thread apiThread = new Thread(() -> {
            try {
                fetchAllAppointments(user_id);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        apiThread.start();
        viewPatientProfileViewAdapter = new ViewPatientProfileViewAdapter(appointmentsArrayList);
        patientAppointmentRCV.setAdapter(viewPatientProfileViewAdapter);

    }

    public ArrayList<Appointment> filterAppointments(ArrayList<Appointment> appointments){
        ArrayList<Appointment> a = new ArrayList<>();
        for (Appointment x: appointments) {
            if(x.getStatus().equalsIgnoreCase("approved") && x.isVisited()){
                a.add(x);
            }
        }
        Collections.sort(a,new SortByDate());
        return a;
    }

    public void setProfileDetail(int user_id){
        //int user_id = data.get(holder.getAdapterPosition()).getUser_id();
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        JSONObject object = new JSONObject();
        try {
            object.put("user_id",user_id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        //object.put("password",password);

        final String requestBody = object.toString();
        stringRequestPatient = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl()+"/user/get-user", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    switch(status) {
                        case "SUCCESS":
                            JSONObject userObject = jsonObject.getJSONObject("user");
                            int userid = userObject.getInt("user_id");
                            Log.d(getClass().getCanonicalName(),"data back" + userid);
                            String name = userObject.getString("name");
                            Log.d(getClass().getCanonicalName(),"user id" + name);
                            String email = userObject.getString("email");
                            String phone = userObject.getString("phone");
                            String dob = userObject.getString("dob");
                            String gender = userObject.getString("gender");
                            Role role = Role.values()[userObject.getInt("role")];
                            user= new User(userid,name,email,dob,gender,phone,role);

                            JSONObject patientObject = jsonObject.getJSONObject("patient_data");
                            String rollnum = patientObject.getString("rollno");
                            String address = patientObject.getString("address");
                            String hostel = patientObject.getString("hostel_details");
                            Patient patient;
                            patient = new Patient(rollnum,address,hostel);
                            user.setPatient(patient);
                            //Role role = jsonObject.getInt("u")

                            namePatient.setText(user.getName());
                            //roll.setText(user.getPatient().getRollNum());

                            break;
                        case "FAILED":
                            String message = jsonObject.getString("message");
                            Log.e("DoctorViewProfileActivity", message);
                            //Toast.makeText(getApplicationContext(),"Invalid credentials!",Toast.LENGTH_SHORT).show();
                        default:
                            break;
                    }
                    ArrayList<Appointment> filteredAppointment = filterAppointments(appointmentsArrayList);
                    viewPatientProfileViewAdapter.setData(filteredAppointment);
                    viewPatientProfileViewAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("DoctorViewProfileActivity", "JSONException: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("DoctorViewProfileActivity", "VolleyError: " + error.getMessage());
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
        requestQueue.add(stringRequestPatient);
    }

    private void fetchAllAppointments(int user_id) throws JSONException{
        JSONObject object = new JSONObject();
        object.put("user_id", user_id);
        final String requestBody = object.toString();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl() + "/user/appointments", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseJson = new JSONObject(response);
                    String status = responseJson.getString("status");
                    switch(status){
                        case "SUCCESS":
                            JSONArray appointments = responseJson.getJSONArray("appointments");
                            for(int i=0;i<appointments.length();i++){
                                JSONObject obj = appointments.getJSONObject(i);
                                int appt_id = obj.getInt("app_id");
                                String sch_time = obj.getString("schedule_time");
                                String approvalStatus =  obj.getString("status");
                                String reason = obj.getString("reason");
                                String reportingTime = obj.getString("reporting_time");
                                String treatmentName = obj.getString("treatment_name");
                                boolean visited = obj.getBoolean("visited");
                                Log.d(getClass().getCanonicalName(),reason+" :: "+visited);
                                Appointment a = new Appointment(appt_id,sch_time,reason,approvalStatus,visited);
                                a.setReportingTime(reportingTime);
                                a.setTreatmentName(treatmentName);
                                appointmentsArrayList.add(a);
                            }
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
                Log.e("DoctorViewProfileActivity", "VolleyError: " + error.getMessage());
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



/*
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }*/
}