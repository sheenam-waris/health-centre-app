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
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.healthcentre.models.StaticDataProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    String email, name;
    TextView dashboardNameTextview;

    EditText rollnoET, addressET, hostelDetailsET, contactET, dobET;
    RadioGroup genderRadioGroup;
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            email = bundle.getString("email");
            name = bundle.getString("name");
        }

        dashboardNameTextview = findViewById(R.id.name);
        dashboardNameTextview.setText(" "+name);

        rollnoET = findViewById(R.id.roll_no);
        addressET = findViewById(R.id.address);
        hostelDetailsET = findViewById(R.id.hostel_details);
        contactET = findViewById(R.id.phone_num);
        dobET = findViewById(R.id.dob);
        genderRadioGroup = findViewById(R.id.gender_radio_group);

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
                DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, dateSetListener, 2005, 0, 1);
                datePickerDialog.getDatePicker().setMaxDate(maxDate);
                datePickerDialog.show();
            }
        });



        registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkForAnyEmptyField()) {
                    //get all texts
                    String rollno = rollnoET.getText().toString().trim();
                    int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
                    String gender = ((RadioButton) findViewById(selectedGenderId)).getText().toString().trim();
                    String dob = dobET.getText().toString().trim();
                    String phone = contactET.getText().toString().trim();
                    String address = addressET.getText().toString().trim();
                    String hostel_details = hostelDetailsET.getText().toString().trim();
                    String password = generateFirstPassword(name,dob);
                    //Create request queue

                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

                    //Create request

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("email", email);
                        jsonObject.put("name", name);
                        jsonObject.put("password", password);
                        jsonObject.put("rollno", rollno);
                        jsonObject.put("gender", gender);
                        jsonObject.put("dob", dob);
                        jsonObject.put("phone", phone);
                        jsonObject.put("role", 3);
                        jsonObject.put("address", address);
                        jsonObject.put("hostel_details", hostel_details);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    final String requestBody = jsonObject.toString();
                    StringRequest postRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl() + "/user/create-account",
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Handle successful response from server

                                    try {
                                        JSONObject Object = new JSONObject(response);
                                        String status = Object.getString("status");
                                        switch (status){
                                            case "SUCCESS" :
                                                Toast.makeText(getApplicationContext(),"Successfully registered...",Toast.LENGTH_LONG).show();

                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                                break;
                                            case "FAILED" :
                                                String message = Object.getString("message");
                                                Log.e("RegisterActivity", message);
                                                Toast.makeText(getApplicationContext(),"Something Went wrong",Toast.LENGTH_LONG).show();
                                                break;
                                            default:
                                                break;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.e("RegisterActivity", "JSONException: " + e.getMessage());
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // Handle error response from server
                                    Log.d(getClass().getCanonicalName(),error.getLocalizedMessage());
                                }
                            }
                    ) {
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
                    requestQueue.add(postRequest);
                }else {
                    String message = "Please Fill all details";
                    SpannableString spannableString = new SpannableString(message);
                    spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // Show the toast with the modified message
                    Toast.makeText(getApplicationContext(), spannableString, Toast.LENGTH_LONG).show();
                    return;
                }

            }
        });

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
    private String generateFirstPassword(String name,String dob){
        String password ="";
        String delimeter = "_";
        password += name.split(" ")[0];
        password += delimeter;
        password += dob.split("/")[2];
        return password;
    }
    /*
    * private boolean checkForAnyEmptyField() {

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    String phonePattern = "^\\+(?:[0-9] ?){6,14}[0-9]$";
    * String phonePattern = "^[6-9]\\d{9}$";

    String password = passwordET.getText().toString().trim();
    String phone = contactET.getText().toString().trim();
    String email = emailET.getText().toString().trim();

    boolean b = password.isEmpty() || phone.isEmpty() || email.isEmpty() ||
            rollnoET.getText().toString().trim().isEmpty() ||
            addressET.getText().toString().trim().isEmpty() ||
            hostelDetailsET.getText().toString().trim().isEmpty() ||
            dobET.getText().toString().trim().isEmpty();

    int selectedId = genderRadioGroup.getCheckedRadioButtonId();

    if (!email.matches(emailPattern)) {
        emailET.setError("Invalid email address");
        return true;
    }

    if (!password.matches(passwordPattern)) {
        passwordET.setError("Password should be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character");
        return true;
    }

    if (!phone.matches(phonePattern)) {
        contactET.setError("Invalid phone number");
        return true;
    }

    return b || (selectedId == -1);
}
*/

}
