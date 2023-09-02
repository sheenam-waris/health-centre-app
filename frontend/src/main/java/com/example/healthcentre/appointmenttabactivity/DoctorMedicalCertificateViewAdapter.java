package com.example.healthcentre.appointmenttabactivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.healthcentre.doctor.ViewMedicalCertificateActivity;
import com.example.healthcentre.doctor.ViewPatientProfileActivity;
import com.example.healthcentre.models.MedicalCertificate;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.student.RequestMedicalCertificateActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class DoctorMedicalCertificateViewAdapter extends RecyclerView.Adapter<MedCertificateViewHolder> {


    private ArrayList<MedicalCertificate> medCertData;
    private Context context;

    public DoctorMedicalCertificateViewAdapter(ArrayList<MedicalCertificate> medicalCertificateArrayList,Context context) {
        this.medCertData = medicalCertificateArrayList;
        this.context = context;
    }

    public void setMedCertData(ArrayList<MedicalCertificate> medCertData) {
        this.medCertData = medCertData;
    }

    @NonNull
    @Override
    public MedCertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_doctor_medical_certificate_request_view,parent,false);
        return new MedCertificateViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MedCertificateViewHolder holder, int position) {
        holder.purposeTV.setText("Purpose : "+medCertData.get(position).getPurpose());
        holder.durationTV.setText("Duration : "+medCertData.get(position).getDuration());
        holder.requestDateTV.setText("Request Date: " +medCertData.get(position).getRequestedDate());
        holder.appointmentIDTV.setText("Appointment Id : " + medCertData.get(position).getAppointmentId());
        int pos = holder.getAdapterPosition();
        holder.viewProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int user_id = medCertData.get(pos).getPatient_id();
                Intent intent = new Intent(v.getContext(), ViewPatientProfileActivity.class);
                intent.putExtra("user_id",user_id);
                v.getContext().startActivity(intent);
            }
        });

        holder.rejectRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reject request
                deleteMedCert(medCertData.get(pos).getMedCert_id(), v.getContext(), pos);
            }
        });
        holder.issueMedicalCertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setTitle("Generate Medical Certificate");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("YES", null); // Set null click listener for now

                builder.setNegativeButton("NO", null); // Set null click listener for now

                AlertDialog alert = builder.create();
                alert.show();

                int colorRed = Color.RED;
                int colorGreen = Color.GREEN;


                Button positiveButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);

                positiveButton.setTextColor(colorGreen);
                negativeButton.setTextColor(colorRed);

                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Call API
                        approveMedicalRequest(medCertData.get(pos).getMedCert_id(), v.getContext());
                        medCertData.remove(pos);
                        notifyItemRemoved(pos);
                        notifyDataSetChanged();
                        alert.dismiss();
                    }
                });

                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Do nothing
                        alert.dismiss();
                    }
                });
                Window window = alert.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.width = 700;
                    layoutParams.height = 500;
                    window.setAttributes(layoutParams);
                }


                /*AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setTitle("Generate Medical Certificate");
                builder.setMessage("Are you sure?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        //Call api
                        approveMedicalRequest(medCertData.get(pos).getMedCert_id(),v.getContext());
                        notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });



                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Do nothing
                        dialog.dismiss();
                    }
                });


                AlertDialog alert = builder.create();
                alert.getWindow().setLayout(600, 400);
                alert.show();*/
            }
        });
        //getStatusForMedCert(medCertData.get(pos).getAppointmentId(),medCertData.get(pos).getMedCert_id(),holder.issueMedicalCertBtn,context,medCertData.get(pos).getPatient_id() );
    }


    @Override
    public int getItemCount() {
        return medCertData.size();
    }

    private void deleteMedCert(int cert_id, Context context, int pos){

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();

        try {
            object.put("cert_id",cert_id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        final String requestBody = object.toString();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl()+"/med-cert/remove-by-id", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    switch(status) {
                        case "SUCCESS":
                            String msg = "Successfully Deleted Medical Certificate Request : " + cert_id;
                            SpannableString spannableString = new SpannableString(msg);
                            spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            // Show the toast with the modified message
                            Toast.makeText(context, spannableString, Toast.LENGTH_LONG).show();
                            medCertData.remove(pos);
                            notifyItemRemoved(pos);
                            notifyDataSetChanged();
                            break;
                        case "FAILED":
                            String message = jsonObject.getString("message");
                            Log.e("StaffPendingAppointmentActivity", message);
                            //Toast.makeText(getApplicationContext(),"Invalid credentials!",Toast.LENGTH_SHORT).show();
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("StaffPendingAppointmentActivity", "JSONException: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("StaffPendingAppointmentActivity", "VolleyError: " + error.getMessage());
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

    private void approveMedicalRequest(int mc_id,Context context){
        JSONObject object2 = new JSONObject();
        try {
            object2.put("mc_id",mc_id);
            object2.put("dr_name", UserSession.getInstance().getCurrentUser().getName());
        } catch (JSONException e) {
            Log.e(getClass().getCanonicalName(),e.getLocalizedMessage());
            return;
        }
        final String requestBody2 = object2.toString();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,StaticDataProvider.getApiBaseUrl() + "/med-cert/approve",
                response -> {
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        String status = responseObject.getString("status");
                        if(status.equalsIgnoreCase("success")){
                            Toast.makeText(context,"Successfully generated medical certificate",Toast.LENGTH_LONG).show();

                        }else{
                            Toast.makeText(context,"Failed to generate medical certificate",Toast.LENGTH_LONG).show();
                            Log.d(getClass().getCanonicalName(),responseObject.getString("message"));
                        }

                    } catch (JSONException e) {
                        Log.e(getClass().getCanonicalName(),e.getLocalizedMessage());
                    }

                },
                error -> {
                    Log.e(getClass().getCanonicalName(),"Failed to approve med-cert!");
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
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody2, "utf-8");
                    return null;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    private void getStatusForMedCert(int appointmentId,int mc_id, Button btn, Context context,int patient_id){
        JSONObject object = new JSONObject();
        try {
            object.put("app_id",appointmentId);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        final String requestBody = object.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl() + "/med-cert/get-status",
                response -> {
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        String reqStatus = responseObject.getString("status");

                        if(reqStatus.equalsIgnoreCase("success")){
                            String cert_stat = responseObject.getString("certificate_status");
                            String purpose = responseObject.getString("purpose");
                            String duration = responseObject.getString("duration");
                            String approvedBy = responseObject.getString("approved_by");
                            String approvedAt = responseObject.getString("approved_at");
                            if(cert_stat.equalsIgnoreCase("pending")){
                                btn.setEnabled(true);
                                btn.setText("Issue Medical Certificate");
                                btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                                        builder.setTitle("Generate Medical Certificate");
                                        builder.setMessage("Are you sure?");
                                        //builder.getWindow().setLayout(600, 400);
                                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int which) {

                                                //Call api
                                                approveMedicalRequest(mc_id,v.getContext());
                                                dialog.dismiss();
                                            }
                                        });

                                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                // Do nothing
                                                dialog.dismiss();
                                            }
                                        });

                                        AlertDialog alert = builder.create();
                                        alert.show();
                                    }
                                });

                            }else if(cert_stat.equalsIgnoreCase("approved")){
                                btn.setEnabled(true);
                                btn.setText("View Medical Certificate");
                                btn.setOnClickListener(null);
                                btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.d(getClass().getCanonicalName()+"/MC Download BTN","Initiate Download");

                                        Bundle bundle = new Bundle();
                                        bundle.putInt("app_id",appointmentId);
                                        bundle.putString("purpose",purpose);
                                        bundle.putString("duration",duration);
                                        bundle.putString("doctor_name",approvedBy);
                                        bundle.putString("approved_at",approvedAt);
                                        bundle.putInt("user_id", patient_id);
                                        Intent intent = new Intent(v.getContext(), ViewMedicalCertificateActivity.class);
                                        intent.putExtras(bundle);
                                        v.getContext().startActivity(intent);
                                    }
                                });
                            }
                        }else if(reqStatus.equalsIgnoreCase("FAILED")){
                            btn.setEnabled(false);
                            btn.setVisibility(View.INVISIBLE);

                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    // Handle error response from backend
                    error.printStackTrace();
                    Log.e("ReqMedicalActivity", "VolleyError: " + error.getMessage());
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
        Volley.newRequestQueue(context).add(stringRequest);
    }
}

class MedCertificateViewHolder extends RecyclerView.ViewHolder{
    TextView purposeTV,durationTV,requestDateTV,appointmentIDTV;
    Button viewProfileBtn,rejectRequestBtn,issueMedicalCertBtn;

    public MedCertificateViewHolder(@NonNull View itemView) {
        super(itemView);
        purposeTV = itemView.findViewById(R.id.tv_purpose);
        durationTV = itemView.findViewById(R.id.tv_duration);
        requestDateTV = itemView.findViewById(R.id.tv_request_date);
        appointmentIDTV = itemView.findViewById(R.id.tv_appointment_id);
        viewProfileBtn = itemView.findViewById(R.id.view_profile_btn);
        rejectRequestBtn = itemView.findViewById(R.id.reject_btn);
        issueMedicalCertBtn = itemView.findViewById(R.id.issue_medical_certificate);
    }
}
