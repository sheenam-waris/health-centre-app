package com.example.healthcentre.doctor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.healthcentre.R;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.utils.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadMedicalCertificateActivity extends AppCompatActivity {
    public static UploadMedicalCertificateActivity instance;

    private ImageView uploadFileImageView;
    private TextView fileNameTextView;
    private Button uploadMedCertBtn,editMedCertBtn;

    private static final String ROOT_URL = StaticDataProvider.getApiBaseUrl() +"/med-cert/upload";
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int PICK_IMAGE_REQUEST =1 ;
    private Bitmap bitmap = null;
    private String filePath;
    private int mc_id,app_id,positionToRemove;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_medical_certificate);

        instance = this;
        Bundle bundle = getIntent().getExtras();
        mc_id = bundle.getInt("mc_id");
        app_id = bundle.getInt("app_id");
        positionToRemove = bundle.getInt("position");
        fileName = app_id + ".png";

        uploadFileImageView = findViewById(R.id.upload_file_image_view_mc);
        fileNameTextView = findViewById(R.id.textView_upload_file_name);
        uploadMedCertBtn = findViewById(R.id.upload_med_cert_btn);
        editMedCertBtn = findViewById(R.id.remove_upload_file_button_mc);

        uploadFileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });

        uploadMedCertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadBitmap(bitmap);

                Intent intent = new Intent(v.getContext(),DoctorDashboardActivity.class);
                startActivity(intent);
                finish();
               /* if (positionToRemove != -1) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("position", positionToRemove);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }*/
            }
        });

        editMedCertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmap!=null){
                    uploadFileImageView.setImageResource(R.drawable.baseline_upload_file_24);
                    fileNameTextView.setText("Please select a file");
                }
            }
        });

    }
    private void upload(){
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(UploadMedicalCertificateActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(UploadMedicalCertificateActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE))) {

            } else {
                ActivityCompat.requestPermissions(UploadMedicalCertificateActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            }
        } else {
            Log.e("Else", "Else");
            showFileChooser();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri picUri = data.getData();
            filePath = getPath(picUri);
            if (filePath != null) {
                try {

                    //textView.setText("File Selected");
                    Log.d("filePath", String.valueOf(filePath));
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), picUri);
                    //uploadBitmap(bitmap);
                    uploadFileImageView.setImageBitmap(bitmap);
                    fileNameTextView.setText(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(
                        UploadMedicalCertificateActivity.this,"no image selected",
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        int x = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        String path = cursor.getString(x >= 0 ? x : 0);
        cursor.close();

        return path;
    }
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    private void uploadBitmap(final Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(UploadMedicalCertificateActivity.this,"Failed to upload! Maybe no image selected!",Toast.LENGTH_LONG).show();
            return;
        }
        String urlWithQueryParams = ROOT_URL + "?mc_id="+mc_id;

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, urlWithQueryParams,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("GotError",""+error.getMessage());
                    }
                }) {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                params.put("file", new DataPart(fileName, getFileDataFromDrawable(bitmap)));
                return params;
            }
        };
        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    public static UploadMedicalCertificateActivity getInstance(){return instance;}

}