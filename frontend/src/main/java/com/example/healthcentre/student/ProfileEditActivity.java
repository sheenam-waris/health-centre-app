package com.example.healthcentre.student;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthcentre.LoginActivity;
import com.example.healthcentre.MainActivity;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.models.StaticDataProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

public class ProfileEditActivity extends AppCompatActivity {

    private EditText rollnoET, addressET, hostelDetailsET, contactET, dobET;
    private RadioGroup genderRadioGroup;
    private Button updateButton;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        confirmLogin();

        /*nameET = findViewById(R.id.name);
        passwordET = findViewById(R.id.password);*/
        rollnoET = findViewById(R.id.edit_roll_no);
        addressET = findViewById(R.id.edit_address);
        hostelDetailsET = findViewById(R.id.edit_hostel_details);
        contactET = findViewById(R.id.edit_phone_num);
        dobET = findViewById(R.id.edit_dob);
        genderRadioGroup = findViewById(R.id.edit_gender_radio_group);
        updateButton = findViewById(R.id.update_button);

        // populate user details into the form fields
        populateUserDetails();

        dobET.setFocusable(false); // to disable manual editing of the date

        final Calendar calendar = Calendar.getInstance();
        calendar.set(2005, 0, 1); // January 1, 2005
        long maxDate = calendar.getTimeInMillis(); // get the maximum date in milliseconds

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // set the selected date on the edit text
                dobET.setText(String.format("%02d/%02d/%d", dayOfMonth, month+1, year));
            }
        };

        dobET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show the date picker dialog with the maximum date set
                DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileEditActivity.this, dateSetListener, 2005, 0, 1);
                datePickerDialog.getDatePicker().setMaxDate(maxDate);
                datePickerDialog.show();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkForAnyEmptyField()) {
                    //String name = nameET.getText().toString().trim();
                    //String password = passwordET.getText().toString().trim();
                    String rollno = rollnoET.getText().toString().trim();
                    int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
                    String gender = ((RadioButton) findViewById(selectedGenderId)).getText().toString().trim();
                    String dob = dobET.getText().toString().trim();
                    String phone = contactET.getText().toString().trim();
                    String address = addressET.getText().toString().trim();
                    String hostel_details = hostelDetailsET.getText().toString().trim();
                    int user_id = UserSession.getInstance().getCurrentUser().getUserId();

                    // create request queue
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

                    // create request body as JSON object
                    JSONObject jsonObject = new JSONObject();
                    try {
                        //jsonObject.put("name", name);
                        //jsonObject.put("password", password);
                        jsonObject.put("user_id",user_id);
                        jsonObject.put("rollno", rollno);
                        jsonObject.put("gender", gender);
                        jsonObject.put("dob", dob);
                        jsonObject.put("phone", phone);
                        jsonObject.put("address", address);
                        jsonObject.put("hostel_details", hostel_details);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // create PUT request to update user details
                    final String requestBody = jsonObject.toString();
                    JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl() + "/user/update-profile", jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String status = response.getString("status");
                                        switch (status) {
                                            case "SUCCESS":
                                                String msg = "Profile Updated Successfully";
                                                SpannableString spannableString = new SpannableString(msg);
                                                spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                // Show the toast with the modified message
                                                Toast.makeText(getApplicationContext(), spannableString, Toast.LENGTH_LONG).show();
                                                UserSession.getInstance().getCurrentUser().setDob(dob);
                                                UserSession.getInstance().getCurrentUser().setGender(gender);
                                                UserSession.getInstance().getCurrentUser().setPhone(phone);
                                                UserSession.getInstance().getCurrentUser().getPatient().setRollNum(rollno);
                                                UserSession.getInstance().getCurrentUser().getPatient().setAddress(address);
                                                UserSession.getInstance().getCurrentUser().getPatient().setHostel(hostel_details);
                                                Intent intent = new Intent(getApplicationContext(), StudentDashboardActivity.class);
                                                startActivity(intent);
                                                finish();
                                                break;
                                            case "FAILURE":
                                                String message = response.getString("message");
                                                Log.e("EditProfileActivity", message);
                                                break;
                                            default:
                                                break;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.e("EditProfileActivity", "JSONException: " + e.getMessage());
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error updating profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                    ){
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public byte[] getBody() {
                            try {
                                return requestBody == null ? null : requestBody.getBytes("utf-8");
                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                                return null;
                            }
                        }
                    };

                    // add request to request queue
                    requestQueue.add(putRequest);
                }else {
                    Toast.makeText(getApplicationContext(),"Please Fill all details",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
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

    private void populateUserDetails() {

            // Set values to corresponding EditText views
            //nameET.setText(name);
           // emailET.setText(email);
            rollnoET.setText(UserSession.getInstance().getCurrentUser().getPatient().getRollNum());
            dobET.setText(UserSession.getInstance().getCurrentUser().getDob());
            contactET.setText(UserSession.getInstance().getCurrentUser().getPhone());
            addressET.setText(UserSession.getInstance().getCurrentUser().getPatient().getAddress());
            hostelDetailsET.setText(UserSession.getInstance().getCurrentUser().getPatient().getHostel());

            // Set radio button according to gender
            String gender = UserSession.getInstance().getCurrentUser().getGender();
            if (gender.equalsIgnoreCase("Male")) {
                genderRadioGroup.findViewById(R.id.male_radio_button).setActivated(true);
            } else if (gender.equalsIgnoreCase("Female")) {
                genderRadioGroup.findViewById(R.id.female_radio_button).setActivated(true);
            }
            return;
    }

    private boolean checkForAnyEmptyField() {

        boolean b = rollnoET.getText().toString().trim().isEmpty() ||
                addressET.getText().toString().trim().isEmpty() ||
                hostelDetailsET.getText().toString().trim().isEmpty() ||
                dobET.getText().toString().trim().isEmpty() ||
                contactET.getText().toString().trim().isEmpty();
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();

        return b || (selectedId == -1);
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