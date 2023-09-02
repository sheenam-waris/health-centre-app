package com.example.healthcentre.appointmenttabactivity;

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
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.models.User;

import java.util.ArrayList;

public class StaffTodaysAppointmentViewAdapter extends RecyclerView.Adapter<TodayViewHolder> {

    ArrayList<Appointment> data;

    private RequestQueue requestQueue;
    private StringRequest stringRequestPatient;
    private User user;

    private static final int REQUEST_CODE_UPLOAD_PRESCRIPTION = 1;

    private OnAppointmentItemClickListener itemClickListener;

    public StaffTodaysAppointmentViewAdapter(OnAppointmentItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public ArrayList<Appointment> getData() {
        return data;
    }

    public void setData(ArrayList<Appointment> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public TodayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_staff_today_approved_appointment_view,parent,false);
        return new TodayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayViewHolder holder, int position) {
        holder.schedule_date.setText("Date of scheduling : "+data.get(position).getSchedule());
        holder.reporting_date.setText("Date of reporting : "+data.get(position).getReportingTime());
        holder.reason.setText("Reason : "+data.get(position).getReason());
        //holder.name.setText("Status: "+data.get(position).getName());
        holder.uploadPrescription_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();

                if (itemClickListener != null) {
                    itemClickListener.onUploadPrescriptionClick(pos);
                }

             /*   Intent intent = new Intent(view.getContext(), UploadPrescriptionActivity.class);
                intent.putExtra("app_id", data.get(pos).getAppointmentId());
                ((Activity) view.getContext()).startActivityForResult(intent, REQUEST_CODE_UPLOAD_PRESCRIPTION);*/            }
        });

        holder.viewProfile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                if(itemClickListener!=null){
                    itemClickListener.onViewProfileClick(pos);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }


}

class TodayViewHolder extends RecyclerView.ViewHolder{

    TextView reporting_date,reason,schedule_date;
    Button uploadPrescription_btn,viewProfile_btn;

    public TodayViewHolder(@NonNull View itemView) {
        super(itemView);
        schedule_date = itemView.findViewById(R.id.tv_scheduling_date);
        reporting_date = itemView.findViewById(R.id.tv_reporting_date);
        reason = itemView.findViewById(R.id.tv_reason);
        uploadPrescription_btn = itemView.findViewById(R.id.upload_prescription_btn);
        viewProfile_btn  = itemView.findViewById(R.id.view_profile_btn);
    }
}
