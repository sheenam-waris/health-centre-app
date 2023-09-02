package com.example.healthcentre.appointmenttabactivity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.example.healthcentre.R;
import com.example.healthcentre.doctor.ViewPatientProfileActivity;
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.models.User;

import java.util.ArrayList;

public class DoctorTodaysAppointmentViewAdapter extends RecyclerView.Adapter<DoctorTodayViewHolder> {
    ArrayList<Appointment> data;

    public DoctorTodaysAppointmentViewAdapter(ArrayList<Appointment> data) {
        this.data = data;
    }

    public void setData(ArrayList<Appointment> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public DoctorTodayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_doctor_today_approved_appointment_view,parent,false);
        return new DoctorTodayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorTodayViewHolder holder, int position) {
        holder.reportingDateTV.setText("Date of reporting : "+data.get(position).getReportingTime());
        holder.scheduleDateTV.setText("Date of scheduling : "+data.get(position).getSchedule());
        holder.reasonTV.setText("Reason : "+data.get(position).getReason());
        holder.viewPatientProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                int user_id = data.get(pos).getUser_id();
                Intent intent = new Intent(v.getContext(), ViewPatientProfileActivity.class);
                intent.putExtra("user_id",user_id);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

}
class DoctorTodayViewHolder extends RecyclerView.ViewHolder{

    TextView reportingDateTV,scheduleDateTV,reasonTV;
    Button viewPatientProfile;

    public DoctorTodayViewHolder(@NonNull View itemView) {
        super(itemView);
        reportingDateTV = itemView.findViewById(R.id.tv_reporting_date);
        scheduleDateTV = itemView.findViewById(R.id.tv_scheduling_date);
        reasonTV = itemView.findViewById(R.id.tv_reason);
        viewPatientProfile = itemView.findViewById(R.id.view_profile_btn);
    }
}