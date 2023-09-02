package com.example.healthcentre.staff;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthcentre.LoginActivity;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.models.StaticDataProvider;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

public class StaffCreateNewAppointmentActivity extends AppCompatActivity implements  DatePickerDialog.OnDateSetListener{

    private EditText reasonInput,emailIdPatient;
    private Button submitButton;
    private Button datePickET;
    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_create_new_appointment);
        confirmLogin();
        reasonInput = findViewById(R.id.visit_reason);
        emailIdPatient = findViewById(R.id.patient_email);
        datePickET = findViewById(R.id.patient_reporting_date);
        submitButton = findViewById(R.id.submit_button);

        Calendar minDateCalendar = Calendar.getInstance();
        int year = minDateCalendar.get(Calendar.YEAR);
        int month = minDateCalendar.get(Calendar.MONTH);
        int _date = minDateCalendar.get(Calendar.DATE);

        datePickET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog  = DatePickerDialog.newInstance(StaffCreateNewAppointmentActivity.this,year,month,_date);
                datePickerDialog.setAccentColor(Color.parseColor("#BAC9F4"));
                datePickerDialog.setThemeDark(false);
                datePickerDialog.showYearPickerFirst(false);
                datePickerDialog.setTitle("Select Reporting date for appointment");
                //datePickerDialog.setMinDate(minDateCalendar);
                datePickerDialog.show(getSupportFragmentManager(),"DatePickerDialog");
            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String reason = reasonInput.getText().toString();
                String email = emailIdPatient.getText().toString();


                if (TextUtils.isEmpty(reason)) {
                    reasonInput.setError("Please enter reason");
                    return;
                }
                if (TextUtils.isEmpty(email) || !(email.contains("@nitc.ac.in"))) {
                    emailIdPatient.setError("Please enter patient's NITC email");
                    return;
                }


                String reporting_time = datePickET.getText().toString();
                if (reporting_time.equalsIgnoreCase("DD/MM/YY")
                        ||TextUtils.isEmpty(reporting_time)) {
                    datePickET.setError("Please select a date!");
                    return;
                }

                JSONObject object = new JSONObject();
                try {
                    object.put("email", email );
                    object.put("reason",reason);
                    //object.put("schedule_time",reporting_time);
                    object.put("reporting_time",reporting_time);
                    //object.put("status","APPROVED");
                } catch (JSONException e) {
                    throw new RuntimeException(e); //url change smart, ho gya, ab? ek upload ab prescription, ye hoga kaha pe?
                }
                final String requestBody = object.toString();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl()+"/staff/create-appointment",
                        response -> {
                            // Handle successful response from backend
                            try {
                                JSONObject responseObject = new JSONObject(response);
                                String status = responseObject.getString("status");
                                if(status.equals("SUCCESS")){
                                    String msg = "New Appointment Created Successfully";
                                    SpannableString spannableString = new SpannableString(msg);
                                    spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    // Show the toast with the modified message
                                    Toast.makeText(getApplicationContext(), spannableString, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), StaffAppointmentsActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Log.e("StaffCreateNewAppointmentActivity", ": " + response);
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                        },
                        error -> {
                            // Handle error response from backend
                            error.printStackTrace();
                            Log.e("StaffCreateNewAppointmentActivity", "VolleyError: " + error.getMessage());
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
                Volley.newRequestQueue(StaffCreateNewAppointmentActivity.this).add(stringRequest);
            }
        });
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        String dateReporting = getStringRepresentation(dayOfMonth) + "/" + getStringRepresentation(monthOfYear+1) + "/" + year;
        datePickET.setText(dateReporting);
    }

    private String getStringRepresentation(int t){
        if(t<10){
            return "0"+t;
        }else{
            return ""+t;
        }
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