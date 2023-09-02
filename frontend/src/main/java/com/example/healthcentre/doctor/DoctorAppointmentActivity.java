package com.example.healthcentre.doctor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.healthcentre.appointmenttabactivity.DoctorTodaysAppointmentViewAdapter;
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.models.StaticDataProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DoctorAppointmentActivity extends AppCompatActivity {

    private RecyclerView appointmentRecyclerView;
    private DoctorTodaysAppointmentViewAdapter doctorTodaysAppointmentViewAdapter;

    private ArrayList<Appointment> appointmentsArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_appointment);
        confirmLogin();

        appointmentRecyclerView = findViewById(R.id.recyclerview_appointment_view_doctor);
        appointmentRecyclerView.setLayoutManager(new LinearLayoutManager(DoctorAppointmentActivity.this));

        appointmentsArrayList = new ArrayList<>();

        doctorTodaysAppointmentViewAdapter = new DoctorTodaysAppointmentViewAdapter(appointmentsArrayList);
        appointmentRecyclerView.setAdapter(doctorTodaysAppointmentViewAdapter);

        runFetchAppointmentsBackground();

        //updateUI();
       /* ArrayList<Appointment> filteredAppointments = filterAppointments(appointmentsArrayList);
        doctorTodaysAppointmentViewAdapter = new DoctorTodaysAppointmentViewAdapter(filteredAppointments);
        appointmentRecyclerView.setAdapter(doctorTodaysAppointmentViewAdapter);*/

    /*       ArrayList<Appointment> filteredAppointments = filterAppointments(appointmentsArrayList);

        doctorTodaysAppointmentViewAdapter = new DoctorTodaysAppointmentViewAdapter(filteredAppointments);
        appointmentRecyclerView.setAdapter(doctorTodaysAppointmentViewAdapter);
        appointmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));*/


    }





    private ArrayList<Appointment> filterAppointments(ArrayList<Appointment> appointmentsArrayList) {
        ArrayList<Appointment> filteredAppointments = new ArrayList<>();
        for (Appointment appointment : appointmentsArrayList) {
            if (isAppointmentApproved(appointment) && !isAppointmentVisited(appointment) && todayReportingDay(appointment)) {
                filteredAppointments.add(appointment);
            }
        }
        return filteredAppointments;
    }
    private boolean isAppointmentApproved(Appointment appointment) {
        return appointment.getStatus().equalsIgnoreCase("approved");
    }

    private boolean isAppointmentVisited(Appointment appointment) {
        return appointment.isVisited();
    }


    private boolean todayReportingDay(Appointment x) {
        //function which return today's reporting date appointments
        String reportingDate = x.getReportingTime();
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        if(date.equalsIgnoreCase(reportingDate)){return true;}
        return false;
    }

    private void fetchAllAppointments() throws JSONException {
        JSONObject object = new JSONObject();
        //object.put("user_id", UserSession.getInstance().getCurrentUser().getUserId());
        final String requestBody = object.toString();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, StaticDataProvider.getApiBaseUrl() + "/appointments/approved", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseJson = new JSONObject(response);
                    String status = responseJson.getString("status");
                    switch(status){
                        case "SUCCESS":
                            //changed api
                            JSONArray appointments = responseJson.getJSONArray("approved_appointments");
                            for(int i=0;i<appointments.length();i++){
                                JSONObject obj = appointments.getJSONObject(i);
                                int appt_id = obj.getInt("app_id");
                                if(appointmentsArrayList.stream().filter(a -> {
                                    if(a.getAppointmentId() == appt_id)return true;
                                    else return false;
                                }).count()>0){
                                    continue;
                                }
                                int user_id = obj.getInt("patient_id");
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
                                a.setUser_id(user_id);

                                appointmentsArrayList.add(a);
                                Log.d(getClass().getCanonicalName(), "Appointment reason : " + appointmentsArrayList.size());
                            }
                            ArrayList<Appointment> filteredAppointments = filterAppointments(appointmentsArrayList);
                            doctorTodaysAppointmentViewAdapter.setData(filteredAppointments);
                            Log.d(getClass().getCanonicalName(),filteredAppointments.size()+"");
                            doctorTodaysAppointmentViewAdapter.notifyDataSetChanged();
                            break;
                        case "Failed":
                            String message = responseJson.getString("message");
                            Log.e("DoctorAppointmentActivity", message);
                            break;
                        default:
                            Log.e("Doctor Appointments: ", "LOL");
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

    public void runFetchAppointmentsBackground(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    fetchAllAppointments();
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