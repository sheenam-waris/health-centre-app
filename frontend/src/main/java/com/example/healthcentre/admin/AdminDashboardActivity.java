package com.example.healthcentre.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.healthcentre.LoginActivity;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.student.StudentDashboardActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    TextView allAppointmentTV,doctorsTV,staffTV,adminUsernameTV;
    ImageView profileView,signOutView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        confirmLogin();
        adminUsernameTV = findViewById(R.id.textview_admin_dashboard);
        adminUsernameTV.setText(UserSession.getInstance().getCurrentUser().getName());


        allAppointmentTV = findViewById(R.id.all_appointments_view);
        allAppointmentTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),AdminViewAppointmentsActivity.class);
                startActivity(intent);
            }
        });
        doctorsTV = findViewById(R.id.all_doctors_view);
        doctorsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),AdminViewDoctorsActivity.class);
                startActivity(intent);
            }
        });

        staffTV = findViewById(R.id.all_staff_view);
        staffTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),AdminViewStaffsActivity.class);
                startActivity(intent);
            }
        });

        profileView = findViewById(R.id.admin_profile_icon);
        profileView.setOnClickListener(new View.OnClickListener() {
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
                //schedule.setText(UserSession.getInstance().getCurrentUser().getSchedule());
            }
        });


        signOutView = findViewById(R.id.admin_logout_button);
        signOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear user session data
                UserSession.logout(AdminDashboardActivity.this);
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
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