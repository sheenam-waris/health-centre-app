package com.example.healthcentre.appointmenttabactivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.doctor.ViewMedicalCertificateActivity;
import com.example.healthcentre.student.PrescriptionViewActivity;
import com.example.healthcentre.student.RequestMedicalCertificateActivity;
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.student.StudentAppointmentActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class CompletedAppointmentRecyclerViewAdaptor extends RecyclerView.Adapter<CompleteViewHolder>{
    enum MED_CERT_STATUS{
        APPROVED,
        APPLIED,
        NOT_APPLIED,
    }
    ArrayList<Appointment> data;

    public CompletedAppointmentRecyclerViewAdaptor(){
        data = new ArrayList<>();
    }

    public ArrayList<Appointment> getData() {
        return data;
    }

    public void setData(ArrayList<Appointment> data) {
        this.data = data;
    }


    @NonNull
    @Override
    public CompleteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_complete_appointment_view_,parent,false);
        return new CompleteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompleteViewHolder holder, int position) {

        holder.reporting_date.setText("Time of Reporting: "+data.get(position).getReportingTime());
        holder.reason.setText("Reason: "+data.get(position).getReason());
        holder.status.setText("Status: "+data.get(position).getStatus());
        holder.openAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog appt_dialog = new Dialog(view.getContext());
                appt_dialog.setContentView(R.layout.custom_appointment_dialogue_box);
                Log.d(getClass().getCanonicalName(),"Initiated dialogbox");
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(appt_dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;
                appt_dialog.show();
                appt_dialog.getWindow().setAttributes(lp);
                TextView appt_Id = appt_dialog.findViewById(R.id.appointment_id);
                TextView schedule_time = appt_dialog.findViewById(R.id.tv_schedule_date);
                TextView reporting_time = appt_dialog.findViewById(R.id.tv_reporting_date2);
                TextView reason = appt_dialog.findViewById(R.id.tv_reason);
                TextView treatment_name = appt_dialog.findViewById(R.id.tv_treatment_name);
               // TextView status = appt_dialog.findViewById(R.id.tv_status);
                int pos = holder.getAdapterPosition();
                appt_Id.setText(data.get(pos).getAppointmentId()+"");
                schedule_time.setText(data.get(pos).getSchedule());
                reporting_time.setText(data.get(pos).getReportingTime());
                reason.setText(data.get(pos).getReason());
                treatment_name.setText(data.get(pos).getTreatmentName());
                //status.setText(data.get(pos).getStatus());
                Button viewPrescriptionBtn = appt_dialog.findViewById(R.id.prescription_download_button);
                viewPrescriptionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("app_id",data.get(pos).getAppointmentId());
                        Intent intent = new Intent(view.getContext(), PrescriptionViewActivity.class);
                        intent.putExtras(bundle);
                        view.getContext().startActivity(intent);
                    }
                });
                Button medCertReqButton = appt_dialog.findViewById(R.id.medical_certificate_request_button);

                // TODO:Call API to get status of medical certificate
                getStatusForMedCert(data.get(pos).getAppointmentId(),medCertReqButton,view.getContext());
                /*      MED_CERT_STATUS status1 = getStatusForMedCert(data.get(pos).getAppointmentId());

                if(status1 == MED_CERT_STATUS.NOT_APPLIED){
                    medCertReqButton.setText("Request Med Certificate");
                }
                else if(status1 == MED_CERT_STATUS.APPLIED){
                    medCertReqButton.setText("Already Requested");
                    medCertReqButton.setEnabled(false);
                }else if(status1 == MED_CERT_STATUS.APPROVED){
                    medCertReqButton.setText("Download Med Certificate");
                } */

            //if med cert not present then set button text as "request med certificate"
            //if present:
            //but not approved, then disable button and set text as "Already requested"
            //and approved, then set button text as "Download med cert"
            //request -> POST ('/medical-certificates)



            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();

    }
    private void getStatusForMedCert(int appointmentId, Button btn, Context context){
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
                                btn.setEnabled(false);
                                btn.setClickable(false);
                                btn.setText("Applied for Medical Certificate");
                            }else if(cert_stat.equalsIgnoreCase("approved")){
                                btn.setEnabled(true);
                                btn.setText("Medical Certificate Download");
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
                                        bundle.putInt("user_id", UserSession.getInstance().getCurrentUser().getUserId());
                                        Intent intent = new Intent(v.getContext(), ViewMedicalCertificateActivity.class);
                                        intent.putExtras(bundle);
                                        v.getContext().startActivity(intent);
                                    }
                                });
                            }
                        }else if(reqStatus.equalsIgnoreCase("FAILED")){
                            btn.setEnabled(true);
                            btn.setText("Request Med Certificate");
                            btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //switch on status of medical cert
                                    Intent intent = new Intent(view.getContext(), RequestMedicalCertificateActivity.class);
                                    intent.putExtra("appointmentId", appointmentId);
                                    view.getContext().startActivity(intent);
                                }
                            });
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
class CompleteViewHolder extends RecyclerView.ViewHolder{

    TextView reporting_date,reason,status;
    Button view_appointment,apply_medical_certificate;
    ImageView openAppointment;
    public CompleteViewHolder(@NonNull View itemView) {
        super(itemView);
        reporting_date = itemView.findViewById(R.id.tv_reporting_date);
        reason = itemView.findViewById(R.id.tv_reason);
        status = itemView.findViewById(R.id.tv_status);
        //apply_medical_certificate = findViewById(R.id.medical_certificate_request_button);
        openAppointment = itemView.findViewById(R.id.open_appointment_image);

    }
}
