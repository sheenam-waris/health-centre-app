package com.example.healthcentre.staff;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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
import com.example.healthcentre.appointmenttabactivity.StaffAppointmentViewFragment;
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.utils.SortByDate;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffAppointmentsActivity extends AppCompatActivity {
    enum APPOINTMENT_CATEGORY {
        APPROVED,
        PENDING
    }

    private TabLayout tabLayout;
    ArrayList<Appointment> appointmentsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_appointments);
        confirmLogin();

        tabLayout = findViewById(R.id.tab_layout_staff);
        tabLayout.addTab(tabLayout.newTab().setText("Today's"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();

                Log.d(getClass().getCanonicalName(), "Tab selected position is: " + pos);
                ArrayList<Appointment> filteredAppointments = new ArrayList<>();
                if (pos == 0) {
                    //Approved ones TODO: only today's appointment
                    filteredAppointments = filterAppointments(appointmentsArrayList, StaffAppointmentsActivity.APPOINTMENT_CATEGORY.APPROVED);

                } else if (pos == 1) {
                    //Change the Filter Category
                    filteredAppointments = filterAppointments(appointmentsArrayList, StaffAppointmentsActivity.APPOINTMENT_CATEGORY.PENDING);
                }

                StaffAppointmentViewFragment fragment = StaffAppointmentViewFragment.newInstance(filteredAppointments,
                        pos);
                //fragment.setData(appointmentsArrayList);
                removeAllFragments();
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_staff_container_view,fragment).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout.selectTab(tabLayout.getTabAt(0));

    }

    @Override
    protected void onResume() {
        super.onResume();
        appointmentsArrayList = new ArrayList<>();

        Thread apiThread = new Thread(() -> {
            try {
                fetchAllAppointments();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        apiThread.start();
    }

    private void loadDefaultTab(){
        int pos = 0;
        ArrayList<Appointment> filteredAppointments = new ArrayList<>();
        if(pos==0){
            //Approved ones
            filteredAppointments = filterAppointments(appointmentsArrayList, StaffAppointmentsActivity.APPOINTMENT_CATEGORY.APPROVED);

        }else if(pos==1){
            //Change the Filter Category
            filteredAppointments = filterAppointments(appointmentsArrayList, StaffAppointmentsActivity.APPOINTMENT_CATEGORY.PENDING);
        }

        StaffAppointmentViewFragment fragment = StaffAppointmentViewFragment.newInstance(filteredAppointments,
                pos);
        //fragment.setData(appointmentsArrayList);
        removeAllFragments();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_staff_container_view,fragment).commit();
    }


    private void removeAllFragments(){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f:fragments) {
            getSupportFragmentManager().beginTransaction().remove(f).commit();
        }
    }



    private ArrayList<Appointment> filterAppointments(ArrayList<Appointment> appointments, StaffAppointmentsActivity.APPOINTMENT_CATEGORY filter){
        ArrayList<Appointment> a = new ArrayList<>();
        for (Appointment x:appointments) {
            if(filter == StaffAppointmentsActivity.APPOINTMENT_CATEGORY.APPROVED){
                //Log.d(getClass().getCanonicalName(), "Appointment added: " + x.getReportingTime());
                if(x.getStatus().equalsIgnoreCase("approved") && !x.isVisited() && todayReportingDay(x)){
                    Log.d(getClass().getCanonicalName(), "Approved app added: " + x.getReportingTime());
                    a.add(x);
                }
            }else if(filter == StaffAppointmentsActivity.APPOINTMENT_CATEGORY.PENDING){
                if(x.getStatus().equalsIgnoreCase("pending")){
                    a.add(x);
                }
            }
        }
        return a;
    }
    private boolean todayReportingDay(Appointment x) {
        //function which return today's reporting date appointments
        String reportingDate = x.getReportingTime();
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        if(date.equalsIgnoreCase(reportingDate)){return true;}
        return false;
    }

    private void fetchAllAppointments() throws JSONException{
        JSONObject object = new JSONObject();
        //object.put("user_id", UserSession.getInstance().getCurrentUser().getUserId());
        final String requestBody = object.toString();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, StaticDataProvider.getApiBaseUrl() + "/appointments", new Response.Listener<String>() {
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
                                Log.d(getClass().getCanonicalName(), "Appointment reason : " + reason);
                                Collections.sort(appointmentsArrayList,new SortByDate());
                                appointmentsArrayList.add(a);
                            }
                            loadDefaultTab();
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
