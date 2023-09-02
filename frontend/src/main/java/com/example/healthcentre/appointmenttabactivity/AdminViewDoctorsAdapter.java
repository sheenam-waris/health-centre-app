package com.example.healthcentre.appointmenttabactivity;

import android.content.Context;
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
import com.example.healthcentre.admin.AdminEditUserActivity;
import com.example.healthcentre.doctor.ViewPatientProfileActivity;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class AdminViewDoctorsAdapter extends RecyclerView.Adapter<AdminDoctorsViewHolder> {


    private ArrayList<User> userData;

    public AdminViewDoctorsAdapter(ArrayList<User> userData) {
        this.userData = userData;
    }

    public void setUserData(ArrayList<User> userData) {
        this.userData = userData;
    }

    @NonNull
    @Override
    public AdminDoctorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_admin_view_doctor_staff_card,parent,false);
        return new AdminDoctorsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminDoctorsViewHolder holder, int position) {
        holder.nameTV.setText(userData.get(position).getName());
        holder.emailTV.setText(userData.get(position).getEmail());
        holder.phoneTV.setText(userData.get(position).getPhone());
        holder.dobTV.setText(userData.get(position).getDob());
        holder.genderTV.setText(userData.get(position).getGender());

        int pos = holder.getAdapterPosition();
        holder.editDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //int user_id = data.get(pos).getUser_id();
                Bundle bundle = new Bundle();
                bundle.putString("name",userData.get(pos).getName());
                bundle.putString("email",userData.get(pos).getEmail());
                bundle.putString("dob",userData.get(pos).getDob());
                bundle.putString("phone",userData.get(pos).getPhone());
                bundle.putString("gender",userData.get(pos).getGender());
                bundle.putInt("user_id",userData.get(pos).getUserId());
                Intent intent = new Intent(v.getContext(), AdminEditUserActivity.class);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDoctor(userData.get(pos).getUserId(), v.getContext(), pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userData.size();
    }

    private void deleteDoctor(int user_id, Context context, int pos){

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();

        try {
            object.put("user_id",user_id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        final String requestBody = object.toString();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl()+"/user/remove-by-id", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    switch(status) {
                        case "SUCCESS":
                            String msg = "Successfully Deleted Doctor : " + user_id;
                            SpannableString spannableString = new SpannableString(msg);
                            spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            // Show the toast with the modified message
                            Toast.makeText(context, spannableString, Toast.LENGTH_LONG).show();
                            userData.remove(pos);
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
}

class AdminDoctorsViewHolder extends RecyclerView.ViewHolder{

    TextView nameTV,emailTV,dobTV,genderTV,phoneTV;
    Button editDetailsBtn,deleteBtn;
    public AdminDoctorsViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTV = itemView.findViewById(R.id.tv_doctor_staff_name);
        emailTV = itemView.findViewById(R.id.tv_email_id);
        dobTV = itemView.findViewById(R.id.tv_birth_date);
        genderTV = itemView.findViewById(R.id.tv_gender);
        phoneTV = itemView.findViewById(R.id.tv_phone_num);
        editDetailsBtn = itemView.findViewById(R.id.edit_details_btn);
        deleteBtn = itemView.findViewById(R.id.delete_btn);
    }
}
