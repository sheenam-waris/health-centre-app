package com.example.healthcentre.student;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.healthcentre.R;
import com.example.healthcentre.models.StaticDataProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PrescriptionViewActivity extends AppCompatActivity {

    private ImageView prescriptionImageView;
    private String filename;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_view);
        filename = String.valueOf(getIntent().getExtras().getInt("app_id")) + ".png";

        prescriptionImageView = findViewById(R.id.prescription_image_view);
        url = StaticDataProvider.getApiBaseUrl() + "/appointments/download?filename="+ filename;
        Glide.with(this).load(url).into(prescriptionImageView);

        FloatingActionButton downloadBtn = findViewById(R.id.fab_download);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStoragePermissionGranted();
                downloadFile();
            }
        });
    }
    public  boolean isStoragePermissionGranted() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission is granted");
            return true;
        } else {

            Log.v(TAG,"Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
        }
    }
    private void downloadFile(){
        BitmapDrawable bd = (BitmapDrawable) prescriptionImageView.getDrawable();
        Bitmap bitmap = bd.getBitmap();
        File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"/Prescriptions/");
        if(!file.exists())file.mkdirs();
        File imageFile = new File(file.getAbsolutePath(),"prescription_"+filename);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            Log.d(getClass().getCanonicalName(),"File Not Found!");
            return;
        } catch (IOException e) {
            Log.d(getClass().getCanonicalName(),"Failed to write!");
            return;
        }
        Toast.makeText(getApplicationContext(),"Saved file here: "+imageFile.getAbsolutePath(),Toast.LENGTH_LONG).show();
    }
}