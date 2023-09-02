package com.example.healthcentre.student;


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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthcentre.LoginActivity;
import com.example.healthcentre.MainActivity;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.models.StaticDataProvider;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;


public class RequestNewAppointmentActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText reasonInput;
    private DatePicker datePicker;
    private Button submitButton;
    private Button datePickET;
    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_new_appointment);
        confirmLogin();

        //Calendar calendar = Calendar.getInstance();
        reasonInput = findViewById(R.id.visit_reason);
        //datePicker = findViewById(R.id.date_pick);
        datePickET = findViewById(R.id.date_pick);



        Calendar minDateCalendar = Calendar.getInstance();
        while(isWeekend(minDateCalendar.get(Calendar.YEAR),minDateCalendar.get(Calendar.MONTH),minDateCalendar.get(Calendar.DATE))){
            minDateCalendar.set(Calendar.DATE,minDateCalendar.get(Calendar.DATE)+1);
        }
        Calendar maxDateCalendar = Calendar.getInstance();
        maxDateCalendar.set(minDateCalendar.get(Calendar.YEAR),minDateCalendar.get(Calendar.MONTH),minDateCalendar.get(Calendar.DATE));
        ArrayList<Calendar> selectableDates = new ArrayList<>();
        selectableDates.add(minDateCalendar);

        int year = minDateCalendar.get(Calendar.YEAR);
        int month = minDateCalendar.get(Calendar.MONTH);
        int _date = minDateCalendar.get(Calendar.DATE);

        int DAYS = 7;
        while(DAYS>1){
            maxDateCalendar.set(Calendar.DATE,maxDateCalendar.get(Calendar.DATE)+1);

            if(isWeekend(maxDateCalendar.get(Calendar.YEAR),maxDateCalendar.get(Calendar.MONTH),maxDateCalendar.get(Calendar.DATE))){

            }else {
                Calendar c = Calendar.getInstance();
                c.set(maxDateCalendar.get(Calendar.YEAR),maxDateCalendar.get(Calendar.MONTH),maxDateCalendar.get(Calendar.DATE));
                selectableDates.add(c);
                DAYS--;
            }
        }
        datePickET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog  = DatePickerDialog.newInstance(RequestNewAppointmentActivity.this,year,month,_date);
                datePickerDialog.setAccentColor(Color.parseColor("#BAC9F4"));
                datePickerDialog.setThemeDark(false);
                datePickerDialog.showYearPickerFirst(false);
                datePickerDialog.setTitle("Select your appointment date");

                Calendar[] selectableDays = new Calendar[selectableDates.size()];
                selectableDates.toArray(selectableDays);
                datePickerDialog.setSelectableDays(selectableDays);
                //datePickerDialog.setMinDate(minDateCalendar);
                datePickerDialog.show(getSupportFragmentManager(),"DatePickerDialog");
            }
        });

        submitButton = findViewById(R.id.submit_button);

        submitButton.setOnClickListener(v -> {

            String reason = reasonInput.getText().toString();


            if (TextUtils.isEmpty(reason)) {
                reasonInput.setError("Please enter reason");
                return;
            }

            String schedule_time = datePickET.getText().toString();
            if (schedule_time.equalsIgnoreCase("DD/MM/YY")
                ||TextUtils.isEmpty(schedule_time)) {
                datePickET.setError("Please select a date!");
                return;
            }
            JSONObject object = new JSONObject();
            try {
                object.put("user_id", UserSession.getInstance().getCurrentUser().getUserId());
                object.put("reason",reason);
                object.put("schedule_time",schedule_time);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            final String requestBody = object.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl()+"/appointments",
                    response -> {
                        // Handle successful response from backend
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            String status = responseObject.getString("status");
                            if(status.equals("SUCCESS")){
                                String msg = "Request Send, Please wait for Approval";
                                SpannableString spannableString = new SpannableString(msg);
                                spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                // Show the toast with the modified message
                                Toast.makeText(getApplicationContext(), spannableString, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), StudentAppointmentActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Log.e("ReqAppointmentActivity", ": " + response);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    },
                    error -> {
                        // Handle error response from backend
                        error.printStackTrace();
                        Log.e("ReqAppointmentActivity", "VolleyError: " + error.getMessage());
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
            Volley.newRequestQueue(RequestNewAppointmentActivity.this).add(stringRequest);
        });


    }
    public boolean isWeekend(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
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
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
