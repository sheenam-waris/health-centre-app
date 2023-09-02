package com.example.healthcentre.student;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthcentre.LoginActivity;
import com.example.healthcentre.MainActivity;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.appointmenttabactivity.AppointmentViewFragment;
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.utils.SortByDate;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class StudentAppointmentActivity extends AppCompatActivity {

    enum APPOINTMENT_CATEGORY {
        APPROVED,
        PENDING,
        VISITED
    }
    //tab layout and view pager create adapter object

    private TabLayout tabLayout;
    ViewPager viewPager;
    FragmentContainerView fragmentContainerView;

    ArrayList<Appointment> appointmentsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_appoitnments);
        appointmentsArrayList = new ArrayList<>();

        confirmLogin();

        Thread apiThread = new Thread(() -> {
            try {
                fetchAllAppointments();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        apiThread.start();


        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Upcoming"));
        tabLayout.addTab(tabLayout.newTab().setText("Completed"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //viewPager cancel, bolo pencil, too much work
        //pencil, some methods have changed/deprecated thats why

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                Log.d(getClass().getCanonicalName(),"Tab selected position is: "+pos);
                ArrayList<Appointment> filteredAppointments = new ArrayList<>();
                if(pos==0){
                    //Approved ones
                    filteredAppointments = filterAppointments(appointmentsArrayList,APPOINTMENT_CATEGORY.APPROVED);

                }else if(pos==1){
                    //Change the Filter Category
                    filteredAppointments = filterAppointments(appointmentsArrayList,APPOINTMENT_CATEGORY.VISITED);
                }else{
                    // TODO: CHANGE THE FILTER CATEGORY
                    filteredAppointments = filterAppointments(appointmentsArrayList,APPOINTMENT_CATEGORY.PENDING);
                }

                AppointmentViewFragment fragment = AppointmentViewFragment.newInstance(filteredAppointments,
                        pos);
                //fragment.setData(appointmentsArrayList);
                removeAllFragments();
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_view,fragment).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.selectTab(tabLayout.getTabAt(1));
    }
    private void loadDefaultTab(){
        int pos = 1;
        ArrayList<Appointment> filteredAppointments = new ArrayList<>();
        if(pos==0){
            //Approved ones
            filteredAppointments = filterAppointments(appointmentsArrayList,APPOINTMENT_CATEGORY.APPROVED);

        }else if(pos==1){
            //Change the Filter Category
            filteredAppointments = filterAppointments(appointmentsArrayList,APPOINTMENT_CATEGORY.VISITED);
        }else{
            // TODO: CHANGE THE FILTER CATEGORY
            filteredAppointments = filterAppointments(appointmentsArrayList,APPOINTMENT_CATEGORY.PENDING);
        }

        AppointmentViewFragment fragment = AppointmentViewFragment.newInstance(filteredAppointments,
                pos);
        //fragment.setData(appointmentsArrayList);
        removeAllFragments();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_view,fragment).commit();
    }

    private void removeAllFragments(){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f:fragments) {
            getSupportFragmentManager().beginTransaction().remove(f).commit();
        }
    }
    private ArrayList<Appointment> filterAppointments(ArrayList<Appointment> appointments, APPOINTMENT_CATEGORY filter){
        ArrayList<Appointment> a = new ArrayList<>();
        for (Appointment x:appointments
             ) {
            if(filter == APPOINTMENT_CATEGORY.VISITED){
                if(x.isVisited()){
                    a.add(x);
                }
            }
            else if(filter == APPOINTMENT_CATEGORY.APPROVED){
                if(x.getStatus().equalsIgnoreCase("approved") && !x.isVisited()){
                    a.add(x);
                }
            }else if(filter==APPOINTMENT_CATEGORY.PENDING){
                if(x.getStatus().equalsIgnoreCase("pending")){
                    a.add(x);
                }
            }
        }
        Collections.sort(a,new SortByDate());
        return a;
    }

    private void fetchAllAppointments() throws JSONException{
        JSONObject object = new JSONObject();
        object.put("user_id", UserSession.getInstance().getCurrentUser().getUserId());
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
                               // Collections.sort(appointmentsArrayList,new SortByDate());
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
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, StudentDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

   /* @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }*/
}