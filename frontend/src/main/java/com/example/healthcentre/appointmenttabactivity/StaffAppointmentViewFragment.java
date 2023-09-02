package com.example.healthcentre.appointmenttabactivity;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.example.healthcentre.staff.UploadPrescriptionActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class StaffAppointmentViewFragment extends Fragment {

    RecyclerView rcv;
    private RequestQueue requestQueue;
    private StringRequest stringRequestPatient;
    private User user;

    private static final String ARG_PARAM1 = "appointmentsList";
    private static final String ARG_PARAM2 = "TAB_INDEX";

    private static final int REQUEST_CODE_UPLOAD_PRESCRIPTION = 1;



    private ArrayList<Appointment> appointmentsList;
    private int tabIndex;

    public StaffAppointmentViewFragment() {
    }

    @NonNull
    public static StaffAppointmentViewFragment newInstance(ArrayList<Appointment> appointments, int tabIndex) {
        StaffAppointmentViewFragment fragment = new StaffAppointmentViewFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1,appointments);
        args.putInt(ARG_PARAM2,tabIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            appointmentsList = getArguments().getParcelableArrayList(ARG_PARAM1);
            tabIndex = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_staff_appointment_view, container, false);
        rcv = view.findViewById(R.id.recyclerview_staff_appointment_fragment);

        switch(tabIndex) {
            case 0:
                StaffTodaysAppointmentViewAdapter stavh = new StaffTodaysAppointmentViewAdapter(new OnAppointmentItemClickListener() {
                    @Override
                    public void onUploadPrescriptionClick(int position) {
                        Intent intent = new Intent(view.getContext(), UploadPrescriptionActivity.class);
                        intent.putExtra("app_id", appointmentsList.get(position).getAppointmentId());
                        ((Activity) view.getContext()).startActivityForResult(intent, REQUEST_CODE_UPLOAD_PRESCRIPTION);
                    }
                    @Override
                    public void onViewProfileClick(int position) {

                        int user_id = appointmentsList.get(position).getUser_id();
                        Context context = getContext();
                        viewProfile(context,user_id);
                    }
                });
                stavh.setData(appointmentsList);
                rcv.setAdapter(stavh);
                rcv.setLayoutManager(new LinearLayoutManager(getContext()));
                break;
            case 1:
                StaffPendingAppointmentViewAdaptor spavh = new StaffPendingAppointmentViewAdaptor(getContext());
                spavh.setData(appointmentsList);
                rcv.setAdapter(spavh);
                rcv.setLayoutManager(new LinearLayoutManager(getContext()));
                break;
        }

        return view;
    }

    public void setData(ArrayList<Appointment> appointments){
        this.appointmentsList = appointments;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_UPLOAD_PRESCRIPTION && resultCode == RESULT_OK) {
            // Refresh the RecyclerView
            switch(tabIndex) {
                case 0:
                    StaffTodaysAppointmentViewAdapter stavh = (StaffTodaysAppointmentViewAdapter) rcv.getAdapter();
                    stavh.notifyDataSetChanged();
                    break;
                case 1:
                    StaffPendingAppointmentViewAdaptor spavh = (StaffPendingAppointmentViewAdaptor) rcv.getAdapter();
                    spavh.notifyDataSetChanged();
                    break;
            }
            // Show a success message
            Toast.makeText(getContext(), "Prescription uploaded successfully", Toast.LENGTH_SHORT).show();
            //UploadPrescriptionActivity.getInstance().finish();
        }
    }

}

