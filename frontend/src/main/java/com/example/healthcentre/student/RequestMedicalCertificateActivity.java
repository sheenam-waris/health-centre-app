package com.example.healthcentre.student;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.healthcentre.MainActivity;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.models.StaticDataProvider;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

public class RequestMedicalCertificateActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText purposeET;
    private Button durationFromDate, durationToDate, submitMedReqBtn;
    private int selectedButton = 0,app_id; // 0 for no button selected, 1 for from date button, 2 for to date button
    private String purpose,duration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_medical_certificate);
        confirmLogin();

        Bundle bundle = getIntent().getExtras();
        app_id = bundle.getInt("appointmentId");

        purposeET = findViewById(R.id.purpose);
        durationFromDate = findViewById(R.id.date_from);
        durationToDate = findViewById(R.id.date_to);
        submitMedReqBtn = findViewById(R.id.submit_med_req_btn);

        durationFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedButton = 1;
                showDatePickerDialog();
            }
        });
        durationToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedButton = 2;
                showDatePickerDialog();
            }
        });
        submitMedReqBtn = findViewById(R.id.submit_med_req_btn);
        submitMedReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                purpose = purposeET.getText().toString();
                if (TextUtils.isEmpty(purpose)) {
                    purposeET.setError("Please enter reason");
                    return;
                }
                String dateFrom = durationFromDate.getText().toString();
                String dateTo = durationToDate.getText().toString();
                if (dateFrom.equalsIgnoreCase("DD/MM/YY") || dateTo.equalsIgnoreCase("DD/MM/YY")
                        || TextUtils.isEmpty(dateFrom) || TextUtils.isEmpty(dateTo)) {
                    durationFromDate.setError("Please select duration date!");
                    return;
                }
               duration = dateFrom + " to " + dateTo;
                Log.d("duration: ", duration);

                JSONObject object = new JSONObject();
                try {
                    object.put("patient_id", UserSession.getInstance().getCurrentUser().getUserId());
                    //TODO: get appointmnet id also
                    object.put("app_id",app_id);
                    object.put("purpose", purpose);
                    object.put("duration", duration);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                final String requestBody = object.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl() + "/med-cert/request-new",
                        response -> {
                            // Handle successful response from backend
                            try {
                                JSONObject responseObject = new JSONObject(response);
                                String status = responseObject.getString("status");
                                if (status.equals("SUCCESS")) {
                                    Toast.makeText(getApplicationContext(),"Sent Request Successfully! Wait for the Certificate...",Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), StudentAppointmentActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.e("ReqMedicalActivity", ": " + response);
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                        },
                        error -> {
                            // Handle error response from backend
                            error.printStackTrace();
                            Log.e("ReqMedicalActivity", "VolleyError: " + error.getMessage());
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
                Volley.newRequestQueue(RequestMedicalCertificateActivity.this).add(stringRequest);
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(RequestMedicalCertificateActivity.this, year, month, day);
        datePickerDialog.show(getSupportFragmentManager(), "DatePickerDialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String selectedDate = getStringRepresentation(dayOfMonth) + "/" + getStringRepresentation(monthOfYear+1) + "/" + year;
        if (selectedButton == 1) {
            durationFromDate.setText(selectedDate);
        } else if (selectedButton == 2) {
            durationToDate.setText(selectedDate);
        }
        selectedButton = 0;
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
