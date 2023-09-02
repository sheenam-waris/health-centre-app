package com.example.healthcentre.appointmenttabactivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthcentre.R;
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.student.PrescriptionViewActivity;

import java.util.ArrayList;

public class ViewPatientProfileViewAdapter extends RecyclerView.Adapter<PatientProfileViewHolder> {

    ArrayList<Appointment> data;

    public ViewPatientProfileViewAdapter(ArrayList<Appointment> data) {
        this.data = data;
    }

    public void setData(ArrayList<Appointment> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public PatientProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_doctor_patients_completed_appointment_view,parent,false);
        return new PatientProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientProfileViewHolder holder, int position) {
        holder.appointmentTV.setText(data.get(position).getAppointmentId()+"");
        holder.openFullApptView.setOnClickListener(new View.OnClickListener() {
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

                int pos = holder.getAdapterPosition();

                appt_Id.setText(data.get(pos).getAppointmentId()+"");
                schedule_time.setText(data.get(pos).getSchedule());
                reporting_time.setText(data.get(pos).getReportingTime());
                reason.setText(data.get(pos).getReason());
                treatment_name.setText(data.get(pos).getTreatmentName());

                Button viewPrescription = appt_dialog.findViewById(R.id.view_prescription_button);
                // TODO: show prescription image
                viewPrescription.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("app_id",data.get(pos).getAppointmentId());
                        Intent intent = new Intent(v.getContext(), PrescriptionViewActivity.class);
                        intent.putExtras(bundle);
                        v.getContext().startActivity(intent);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}

class PatientProfileViewHolder extends RecyclerView.ViewHolder{

    TextView appointmentTV;
    ImageView openFullApptView;
    public PatientProfileViewHolder(@NonNull View itemView) {
        super(itemView);
        appointmentTV = itemView.findViewById(R.id.tv_appointment_id);
        openFullApptView = itemView.findViewById(R.id.open_appointment_image);
    }
}