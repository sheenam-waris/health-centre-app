package com.example.healthcentre.doctor;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
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
import com.example.healthcentre.R;
import com.example.healthcentre.models.StaticDataProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ViewMedicalCertificateActivity extends AppCompatActivity {

    TextView contentView,purposeView,dateView,doctorSignView;
    private String treatmentName;
    private String doctorName;
    private String patientRollNo;
    private String patientName;
    private String duration;
    private String purpose;
    private String todayDate;

    private ProgressBar progressBar;
    private FloatingActionButton download_fab;
    private RequestQueue apiRequestQueue;
    private int app_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_medical_certificate);

        apiRequestQueue = Volley.newRequestQueue(this);

        contentView = findViewById(R.id.mid_content_view);
        purposeView = findViewById(R.id.purpose_med_cert);
        dateView = findViewById(R.id.issue_date_med_cert);
        doctorSignView = findViewById(R.id.doctor_sign_med_cert);
        progressBar = findViewById(R.id.progressBar);
        download_fab = findViewById(R.id.download_fab);

        download_fab.setEnabled(false);
        download_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStoragePermissionGranted();
            }
        });
        // Get the patient details from the intent

        Bundle bundle = getIntent().getExtras();
        purpose = bundle.getString("purpose");
        duration  = bundle.getString("duration");
        app_id = bundle.getInt("app_id");
        int patient_id = bundle.getInt("user_id");
        fetchDetails(patient_id, app_id);
        doctorName = bundle.getString("doctor_name");

        todayDate = bundle.getString("approved_at");

        // Generate the medical certificate
        String medicalCertificate = generateMedicalCertificate(doctorName, patientName, patientRollNo, treatmentName, duration, purpose, todayDate);

        // Set the medical certificate text



    }

    private void fetchDetails(int user_id,int app_id)
    {
        Log.w(getClass().getCanonicalName(),"user id :: "+user_id);
        JSONObject object = new JSONObject();
        try {
            object.put("user_id",user_id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        final String requestBody = object.toString();
        StringRequest stringRequestPatient = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl()+"/user/get-user", new Response.Listener<String>() {
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
                            patientName = userObject.getString("name");


                            JSONObject patientObject = jsonObject.getJSONObject("patient_data");
                            patientRollNo = patientObject.getString("rollno");
                            progressBar.setVisibility(View.INVISIBLE);
                            contentView.setText(generateMedicalCertificate(doctorName,patientName,patientRollNo,treatmentName,duration,purpose,todayDate));
                            purposeView.setText("Purpose : " + purpose);
                            dateView.setText("Date : " + todayDate);
                            doctorSignView.setText("Dr. " + doctorName);
                            download_fab.setEnabled(true);
                            break;
                        case "FAILED":
                            String message = jsonObject.getString("message");
                            Log.e("DoctorViewProfileActivity", message);
                            //Toast.makeText(getApplicationContext(),"Invalid credentials!",Toast.LENGTH_SHORT).show();
                        default:
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("IssueMedCert", "JSONException: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("IssueMedCert", "VolleyError: " + error.getMessage());
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
        JSONObject object2 = new JSONObject();
        try {
            object2.put("app_id",app_id);
        } catch (JSONException e) {
            Log.e(getClass().getCanonicalName(),"Failed to fetch appointment!");
           return;
        }
        final String requestBody2 = object2.toString();
        StringRequest stringGetAppointment = new StringRequest(Request.Method.POST,StaticDataProvider.getApiBaseUrl() + "/appointments/get-by-id",
                response -> {
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        String status = responseObject.getString("status");
                        if(status.equalsIgnoreCase("success")){
                            JSONObject appObject = responseObject.getJSONObject("appointment");
                            treatmentName = appObject.getString("treatment_name");
                            apiRequestQueue.add(stringRequestPatient);
                        }else{
                            Log.d(getClass().getCanonicalName(),"Failed to fetch appointment!");
                        }

                    } catch (JSONException e) {
                        Log.e(getClass().getCanonicalName(),e.getLocalizedMessage());
                    }

                },
                error -> {
                    Log.e(getClass().getCanonicalName(),"Failed to fetch appointment!");
                }
                ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody2 == null ? null : requestBody2.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
        };

        apiRequestQueue.add(stringGetAppointment);
    }
    private String generateMedicalCertificate(String doctorName, String patientName, String patientRollNo, String treatmentName, String duration, String purpose, String todayDate) {
        String medicalCertificate = "I, Dr. " + doctorName + ", after careful personal examination of the case hereby certify that " +
                patientName + ", " + patientRollNo + ", is suffering from " + treatmentName+"." + "\n\n" +
                "He/She was not in a condition to write examination/attend class during the period from " + duration + "\n\n";

        return medicalCertificate;
    }
    private void generatePdf(){

//        View view = LayoutInflater.from(this).inflate(R.layout.activity_issue_medical_certificate,null);
//        ProgressBar progressBar1 = view.findViewById(R.id.progressBar);
//        progressBar1.setVisibility(View.INVISIBLE);
        View view = this.findViewById(android.R.id.content);
        view.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.download_fab).setVisibility(View.INVISIBLE);
        //TextView contentVie
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.getDisplay().getRealMetrics(displayMetrics);
        }else{
            this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }
        view.measure(displayMetrics.widthPixels,displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        //Bitmap.createScaledBitmap(bitmap, 595, 842, true);
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(view.getMeasuredWidth(), 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        page.getCanvas().drawBitmap(bitmap, 0F, 0F, null);
        pdfDocument.finishPage(page);
        String filename = String.valueOf(app_id)+".pdf";
        File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"/Medical Certificates/");
        if(!file.exists())file.mkdirs();
        File filePath = new File(file.getAbsolutePath(),"MedCertificate"+filename);

        try {
            if(!filePath.exists())file.createNewFile();
            pdfDocument.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Toast.makeText(getApplicationContext(),"Saved PDF at "+filePath.getAbsolutePath(),Toast.LENGTH_LONG).show();
        pdfDocument.close();
    }

    public  boolean isStoragePermissionGranted() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission is granted");
            generatePdf();
            return true;
        } else {

            Log.v(TAG,"Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            generatePdf();
        }
    }
}