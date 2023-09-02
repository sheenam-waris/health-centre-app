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

public class RequestAppointmentRecyclerViewAdaptor extends RecyclerView.Adapter<RequestViewHolder> {

    ArrayList<Appointment> data;

    public RequestAppointmentRecyclerViewAdaptor(ArrayList<Appointment> appointments){
        data = appointments;
    }

    public ArrayList<Appointment> getData() {
        return data;
    }

    public void setData(ArrayList<Appointment> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_request_appointment_view,parent,false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {

        holder.schedule_date.setText("Time of booking: "+data.get(position).getSchedule());
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

class RequestViewHolder extends RecyclerView.ViewHolder {

    TextView schedule_date,reason,status;

    public RequestViewHolder(@NonNull View itemView) {
        super(itemView);
        schedule_date = itemView.findViewById(R.id.tv_schedule_date);
        reason = itemView.findViewById(R.id.tv_reason);
        status = itemView.findViewById(R.id.tv_status);

    }
}

