package com.example.healthcentre.appointmenttabactivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.models.Patient;
import com.example.healthcentre.models.Role;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.models.User;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;


public class StaffPendingAppointmentViewAdaptor extends RecyclerView.Adapter<PendingViewHolder> implements DatePickerDialog.OnDateSetListener {

    ArrayList<Appointment> data;
    Button reportingDateSelect;
    private Context context;

   private RequestQueue requestQueue;
   private StringRequest stringRequestPatient;
   private User user;
   private  DatePickerDialog datePickerDialog;


    public StaffPendingAppointmentViewAdaptor(Context context){
        this.context
                 = context;
    }

    public ArrayList<Appointment> getData() {
        return data;
    }

    public void setData(ArrayList<Appointment> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public PendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_staff_pending_appointment_view,parent,false);
        return new PendingViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull PendingViewHolder holder, int position) {
        //position = holder.getAdapterPosition();
        holder.schedule_date.setText("Date of Scheduling : "+data.get(position).getSchedule());
        holder.reason.setText("Reason : "+data.get(position).getReason());
        //holder.name.setText("Status: "+data.get(position).getName());
        holder.approve_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog repotingDate_dialog = new Dialog(view.getContext());
                repotingDate_dialog.setContentView(R.layout.staff_reporting_date_dialog_box);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(repotingDate_dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;
                repotingDate_dialog.show();
                repotingDate_dialog.setCancelable(false);
                repotingDate_dialog.getWindow().setAttributes(lp);

                reportingDateSelect = repotingDate_dialog.findViewById(R.id.staff_give_reporting_date);
                reportingDateSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar minDateCalendar = Calendar.getInstance();
                        int year = minDateCalendar.get(Calendar.YEAR);
                        int month = minDateCalendar.get(Calendar.MONTH);
                        int dayOfMonth = minDateCalendar.get(Calendar.DAY_OF_MONTH);

                        datePickerDialog  = DatePickerDialog.newInstance(StaffPendingAppointmentViewAdaptor.this,year,month,dayOfMonth);
                        datePickerDialog.setAccentColor(Color.parseColor("#BAC9F4"));
                        datePickerDialog.setThemeDark(false);
                        datePickerDialog.showYearPickerFirst(false);
                        datePickerDialog.setTitle("Select your appointment date");
                        AppCompatActivity activity = (AppCompatActivity) context;
                        datePickerDialog.show(activity.getSupportFragmentManager(), "DatePickerDialog");
                    }
                });

                Button closeButton = repotingDate_dialog.findViewById(R.id.staff_ok_button);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String reporting_time = reportingDateSelect.getText().toString();
                        if (reporting_time.equalsIgnoreCase("DD/MM/YY")
                                || TextUtils.isEmpty(reporting_time)) {
                            reportingDateSelect.setError("Please select a date!");
                            return;
                        }
                        repotingDate_dialog.dismiss();
                            // Update the appointment with the selected reporting date


                            int pos = holder.getAdapterPosition();
                            int appt_id = data.get(pos).getAppointmentId();

                            JSONObject object = new JSONObject();
                            try {
                                object.put("user_id", UserSession.getInstance().getCurrentUser().getUserId());
                                object.put("app_id", appt_id);
                                object.put("status", "APPROVED");
                                object.put("reporting_time", reporting_time);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            final String requestBody = object.toString();

                            StringRequest stringRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl() + "/appointments/approve",
                                    response -> {
                                        // Handle successful response from backend
                                        try {
                                            JSONObject responseObject = new JSONObject(response);
                                            String status = responseObject.getString("status");
                                            if (status.equals("SUCCESS")) {
                                                data.get(pos).setStatus("Approved");
                                                data.get(pos).setReportingTime(reporting_time);
                                                data.remove(pos);
                                                notifyItemRemoved(pos);
                                                notifyDataSetChanged();

                                            } else {
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
                            Volley.newRequestQueue(view.getContext()).add(stringRequest);
                        }
                });
            }
        });

        holder.viewProfile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Context context = view.getContext();
                int pos = holder.getAdapterPosition();
               int user_id = data.get(pos).getUser_id();
               viewProfile(context,user_id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        String dateReporting = getStringRepresentation(dayOfMonth) + "/" + getStringRepresentation(monthOfYear+1) + "/" + year;
        reportingDateSelect.setText(dateReporting);
    }

    private String getStringRepresentation(int t){
        if(t<10){
            return "0"+t;
        }else{
            return ""+t;
        }
    }

    public void viewProfile(Context context,int user_id){
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

class PendingViewHolder extends RecyclerView.ViewHolder{

    TextView schedule_date,reason;
    Button approve_btn,viewProfile_btn;

    public PendingViewHolder(@NonNull View itemView) {

        super(itemView);
        schedule_date = itemView.findViewById(R.id.tv_schedule_date);
        reason = itemView.findViewById(R.id.tv_reason);
        approve_btn = itemView.findViewById(R.id.approve_appt_btn);
        viewProfile_btn = itemView.findViewById(R.id.view_profile_btn);
    }
}
