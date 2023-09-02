package com.example.healthcentre.staff;

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

public class StaffDashboardActivity extends AppCompatActivity {

    private TextView usernameTextView,appointmentsTextView,createNewAppointmentTV;
    private ImageView signOutButton,profileView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);
        confirmLogin();

        usernameTextView = findViewById(R.id.textview_staff_dashboard);
        usernameTextView.setText(UserSession.getInstance().getCurrentUser().getName());


        appointmentsTextView = findViewById(R.id.appointments_staff);
        appointmentsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StaffAppointmentsActivity.class);
                startActivity(intent);
            }
        });


        createNewAppointmentTV = findViewById(R.id.create_new_appt);

        createNewAppointmentTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),StaffCreateNewAppointmentActivity.class);
                startActivity(intent);
            }
        });

        profileView = findViewById(R.id.profile_icon);
        profileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog profileView = new Dialog(view.getContext());
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

       signOutButton = findViewById(R.id.staff_logout_button);

        //sign-out button OnClickListener
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Clear user session data
                UserSession.logout(StaffDashboardActivity.this);
                Intent intent = new Intent(StaffDashboardActivity.this, LoginActivity.class);
                startActivity(intent);

                finish();
                // Send sign-out request to the server using Volley
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