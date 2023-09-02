package com.example.healthcentre.appointmenttabactivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.models.Patient;
import com.example.healthcentre.models.Role;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.models.User;
import com.example.healthcentre.student.PrescriptionViewActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class AdminViewAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentViewHolder> {

    private ArrayList<Appointment> mData;
    private RequestQueue requestQueue;
    private StringRequest stringRequestPatient;
    private User user;


    public AdminViewAppointmentAdapter(ArrayList<Appointment> data) {
        this.mData = data;
    }

    @NonNull
    @Override
    public AdminAppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_admin_completed_appoitnment_view, parent, false);
        return new AdminAppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminAppointmentViewHolder holder, int position) {
        holder.appointmentIDTV.setText(mData.get(position).getAppointmentId() + "");
        holder.reportingTimeTV.setText(mData.get(position).getReportingTime());
        int pos = holder.getAdapterPosition();
        holder.openFullAppointmentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Dialog appt_dialog = new Dialog(v.getContext());
                appt_dialog.setContentView(R.layout.custom_doctor_view_appointment_dialogue_box);
                // Log.d(getClass().getCanonicalName(),"Initiated dialogbox");
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



                appt_Id.setText(mData.get(pos).getAppointmentId() + "");
                schedule_time.setText(mData.get(pos).getSchedule());
                reporting_time.setText(mData.get(pos).getReportingTime());
                reason.setText(mData.get(pos).getReason());
                treatment_name.setText(mData.get(pos).getTreatmentName());
                Button viewPrescription = appt_dialog.findViewById(R.id.view_prescription_button);
                // TODO: show prescription image
                viewPrescription.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("app_id",mData.get(pos).getAppointmentId());
                        Intent intent = new Intent(v.getContext(), PrescriptionViewActivity.class);
                        intent.putExtras(bundle);
                        v.getContext().startActivity(intent);
                    }
                });

            }
        });
        holder.viewProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int user_id = mData.get(pos).getUser_id();
                viewProfile(v.getContext(),user_id);
            }
        });
        holder.removeAppointmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: remove appointment from database

            deleteAppointment(mData.get(pos).getAppointmentId(), v.getContext(), pos);

            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(ArrayList<Appointment> filteredAppointments) {
        this.mData = filteredAppointments;
    }

    private void deleteAppointment(int app_id,Context context,int pos){

        requestQueue = Volley.newRequestQueue(context);

        JSONObject object = new JSONObject();
        try {
            object.put("app_id",app_id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        final String requestBody = object.toString();
        stringRequestPatient = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl()+"/appointments/remove-by-id", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    switch(status) {
                        case "SUCCESS":
                            String msg = "Successfully Deleted Appointment : " + app_id;
                            SpannableString spannableString = new SpannableString(msg);
                            spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            // Show the toast with the modified message
                            Toast.makeText(context, spannableString, Toast.LENGTH_LONG).show();
                            mData.remove(pos);
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
        requestQueue.add(stringRequestPatient);

    }

    private void viewProfile(Context context,int user_id){
        Dialog profileView = new Dialog(context);
        profileView.setContentView(R.layout.student_profile_dialogue_box);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(profileView.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        TextView namePatient = profileView.findViewById(R.id.tv_username);
        TextView rollPatient = profileView.findViewById(R.id.tv_roll_num);
        TextView genderPatient = profileView.findViewById(R.id.tv_gender);
        TextView dobPatient = profileView.findViewById(R.id.tv_birth_date);
        TextView phonePatient = profileView.findViewById(R.id.tv_phone_num);
        TextView addressPatient = profileView.findViewById(R.id.tv_address);
        TextView hostelDetailsPatient = profileView.findViewById(R.id.tv_hostel_details);
        ImageView editProfile = profileView.findViewById(R.id.edit_profile_icon);
        editProfile.setVisibility(View.INVISIBLE);

        //int user_id = data.get(holder.getAdapterPosition()).getUser_id();
        requestQueue = Volley.newRequestQueue(profileView.getContext());

        JSONObject object = new JSONObject();
        try {
            object.put("user_id",user_id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        //object.put("password",password);

        final String requestBody = object.toString();
        stringRequestPatient = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl()+"/user/get-user", new Response.Listener<String>() {
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
                            String name = userObject.getString("name");
                            Log.d(getClass().getCanonicalName(),"user id" + name);
                            String email = userObject.getString("email");
                            String phone = userObject.getString("phone");
                            String dob = userObject.getString("dob");
                            String gender = userObject.getString("gender");
                            Role role = Role.values()[userObject.getInt("role")];
                            user= new User(userid,name,email,dob,gender,phone,role);

                            JSONObject patientObject = jsonObject.getJSONObject("patient_data");
                            String rollnum = patientObject.getString("rollno");
                            String address = patientObject.getString("address");
                            String hostel = patientObject.getString("hostel_details");
                            Patient patient;
                            patient = new Patient(rollnum,address,hostel);
                            user.setPatient(patient);
                            //Role role = jsonObject.getInt("u")

                            namePatient.setText(user.getName());
                            //roll.setText(user.getPatient().getRollNum());
                            genderPatient.setText(user.getGender());
                            dobPatient.setText(user.getDob());
                            phonePatient.setText(user.getPhone());
                            rollPatient.setText(user.getPatient().getRollNum());
                            addressPatient.setText(user.getPatient().getAddress());
                            hostelDetailsPatient.setText(user.getPatient().getHostel());


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
        requestQueue.add(stringRequestPatient);
        profileView.show();
        profileView.getWindow().setAttributes(lp);
    }

}

class AdminAppointmentViewHolder extends RecyclerView.ViewHolder{
        TextView appointmentIDTV,reportingTimeTV;
        ImageView openFullAppointmentView;
        Button viewProfileBtn,removeAppointmentBtn;


        public AdminAppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            appointmentIDTV = itemView.findViewById(R.id.tv_appointment_id);
            reportingTimeTV = itemView.findViewById(R.id.tv_reporting_date2);
            openFullAppointmentView = itemView.findViewById(R.id.open_appointment_image);
            viewProfileBtn = itemView.findViewById(R.id.view_profile_btn);
            removeAppointmentBtn = itemView.findViewById(R.id.remove_appt_btn);

        }
}