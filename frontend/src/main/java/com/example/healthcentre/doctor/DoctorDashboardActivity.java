package com.example.healthcentre.doctor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.healthcentre.LoginActivity;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.student.StudentDashboardActivity;

public class DoctorDashboardActivity extends AppCompatActivity {

    private TextView doctorNameTV,appointmentDoctorTV,medicalCertificateDoctorTV;
    private ImageView doctorProfileView,doctorLogoutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);
        confirmLogin();
        doctorNameTV = findViewById(R.id.textview_doctor_name_dashboard);
        appointmentDoctorTV = findViewById(R.id.appointments_doctor);
        medicalCertificateDoctorTV = findViewById(R.id.medical_certificate_doctor);
        doctorProfileView = findViewById(R.id.profile_icon);
        doctorLogoutView = findViewById(R.id.doctor_logout_button);

        doctorNameTV.setText("Dr. "+UserSession.getInstance().getCurrentUser().getName());

        appointmentDoctorTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorDashboardActivity.this,DoctorAppointmentActivity.class);
                startActivity(intent);
            }
        });

        medicalCertificateDoctorTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorDashboardActivity.this,DoctorMedicalCertificateActivity.class);
                startActivity(intent);
            }
        });

        doctorProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog profileView = new Dialog(v.getContext());
                profileView.setContentView(R.layout.staff_dr_admin_profile_dialogue_box);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(profileView.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;

                profileView.show();
                profileView.getWindow().setAttributes(lp);

                TextView name = profileView.findViewById(R.id.tv_username);
                TextView email = profileView.findViewById(R.id.tv_email_id);
                TextView gender = profileView.findViewById(R.id.tv_gender);
                TextView dob = profileView.findViewById(R.id.tv_birth_date);
                TextView phone = profileView.findViewById(R.id.tv_phone_num);
                //TextView schedule = profileView.findViewById(R.id.tv_schedule);


                name.setText(UserSession.getInstance().getCurrentUser().getName());
                gender.setText(UserSession.getInstance().getCurrentUser().getGender());
                dob.setText(UserSession.getInstance().getCurrentUser().getDob());
                phone.setText(UserSession.getInstance().getCurrentUser().getPhone());
                email.setText(UserSession.getInstance().getCurrentUser().getEmail());

            }
        });

        doctorLogoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSession.logout(DoctorDashboardActivity.this);
                Intent intent = new Intent(DoctorDashboardActivity.this, LoginActivity.class);
                startActivity(intent);

                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
    private void confirmLogin(){
        if(UserSession.getInstance().isLoggedIn()){
            return;
        }else{
            //Intent for login
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}