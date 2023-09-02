package com.example.healthcentre;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthcentre.models.Patient;
import com.example.healthcentre.models.Role;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.models.User;
import com.example.healthcentre.student.RegisterActivity;
import com.example.healthcentre.student.StudentDashboardActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
//Singleton Class
public class UserSession {

    public User user;
    private static UserSession _instance;
    public static UserSession getInstance(){
        if(_instance==null){
            _instance =  new UserSession();
            return _instance;
        }else{
            return _instance;
        }
    }

    private UserSession(){

    }

    public User getCurrentUser() {
        return user;
    }

    public void setCurrentUser(User user) {
        this.user = user;
    }

    public boolean isLoggedIn(){
        return !(user == null);
    }

    public static void logout( Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String token = prefs.getString("token","none");
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        try {
            object.put("token",token);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        final String requestBody = object.toString();
        StringRequest request = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl() + "/logout", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equalsIgnoreCase("SUCCESS")) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("token","none");
                        editor.apply();
                        UserSession.getInstance().setCurrentUser(null);
                    } else {

                    }
                }catch (JSONException exception){
                    Log.d(getClass().getCanonicalName(),exception.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(getClass().getCanonicalName(),"ho jao yaar");
            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
        };
        requestQueue.add(request);
    }
}
