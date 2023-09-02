package com.example.healthcentre.appointmenttabactivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthcentre.R;
import com.example.healthcentre.models.Appointment;

import java.util.ArrayList;

public class ApprovedAppointmentRecyclerViewAdaptor extends RecyclerView.Adapter<ApproveViewHolder> {

    ArrayList<Appointment> data;

    public ApprovedAppointmentRecyclerViewAdaptor(){
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
    public ApproveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_approved_appointment_view,parent,false);
        return new ApproveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApproveViewHolder holder, int position) {
        holder.reporting_date.setText("Time of Reporting: "+data.get(position).getReportingTime());
        holder.reason.setText("Reason: "+data.get(position).getReason());
        holder.status.setText("Status: "+data.get(position).getStatus());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent
                //Add data.get(position) to bundle

                //send with intent
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


}

class ApproveViewHolder extends RecyclerView.ViewHolder {

    TextView reporting_date,reason,status;

    public ApproveViewHolder(@NonNull View itemView) {
        super(itemView);
        reporting_date = itemView.findViewById(R.id.tv_reporting_date);
        reason = itemView.findViewById(R.id.tv_reason);
        status = itemView.findViewById(R.id.tv_status);

    }
}

