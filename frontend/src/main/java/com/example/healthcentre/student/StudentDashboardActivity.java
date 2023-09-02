package com.example.healthcentre.student;

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
import com.example.healthcentre.MainActivity;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.models.Role;
import com.example.healthcentre.models.User;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView usernameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
        confirmLogin();
        TextView newAppointmentRequestTextView = findViewById(R.id.new_requested_appt);
        usernameTextView = findViewById(R.id.username_textview_student_dashboard);
        usernameTextView.setText(UserSession.getInstance().getCurrentUser().getName());
        newAppointmentRequestTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentDashboardActivity.this, RequestNewAppointmentActivity.class);
                startActivity(intent);
            }
        });
        TextView myAppointmentsTextView = findViewById(R.id.my_appointment);
        myAppointmentsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StudentAppointmentActivity.class);
                startActivity(intent);
            }
        });

        ImageView profileIcon = (ImageView) findViewById(R.id.profile_icon);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*User currentUser = UserSession.getInstance().getCurrentUser();
                 showUserProfile(context, currentUser);*/

                Dialog profileView = new Dialog(v.getContext());
                profileView.setContentView(R.layout.student_profile_dialogue_box);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(profileView.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;

                profileView.show();
                profileView.getWindow().setAttributes(lp);

                TextView name = profileView.findViewById(R.id.tv_username);
                TextView roll = profileView.findViewById(R.id.tv_roll_num);
                TextView gender = profileView.findViewById(R.id.tv_gender);
                TextView dob = profileView.findViewById(R.id.tv_birth_date);
                TextView phone = profileView.findViewById(R.id.tv_phone_num);
                TextView address = profileView.findViewById(R.id.tv_address);
                TextView hostelDetails = profileView.findViewById(R.id.tv_hostel_details);

                name.setText(UserSession.getInstance().getCurrentUser().getName());
                //roll.setText(UserSession.getInstance().getCurrentUser().getPatient().getRollNum());
                gender.setText(UserSession.getInstance().getCurrentUser().getGender());
                dob.setText(UserSession.getInstance().getCurrentUser().getDob());
                phone.setText(UserSession.getInstance().getCurrentUser().getPhone());
                if(UserSession.getInstance().getCurrentUser().getRole()== Role.STUDENT){
                    if(UserSession.getInstance().getCurrentUser().getPatient()!=null){
                        roll.setText(UserSession.getInstance().getCurrentUser().getPatient().getRollNum());
                        address.setText(UserSession.getInstance().getCurrentUser().getPatient().getAddress());
                        hostelDetails.setText(UserSession.getInstance().getCurrentUser().getPatient().getHostel());
                    }
                }
               // address.setText(UserSession.getInstance().getCurrentUser().getPatient().getAddress());
                //hostelDetails.setText(UserSession.getInstance().getCurrentUser().getPatient().getHostel());

                ImageView editProfile = profileView.findViewById(R.id.edit_profile_icon);
                editProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ProfileEditActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
        // sign-out button from layout
        ImageView signOutButton = (ImageView) findViewById(R.id.logout_button);

        //sign-out button OnClickListener
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Clear user session data

                UserSession.logout(StudentDashboardActivity.this);
                Intent intent = new Intent(StudentDashboardActivity.this, MainActivity.class);
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
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}