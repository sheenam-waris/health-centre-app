package com.example.healthcentre.models;

public class StaticDataProvider {

    public static String API_BASE_URL = "";//paste your Mobile IP address
    public static String EMULATOR_API_BASE_URL = "http://10.0.2.2:8080";

    public static boolean EMULATOR_MODE_ENABLED = true;


    public static String getApiBaseUrl(){
        return EMULATOR_MODE_ENABLED ? EMULATOR_API_BASE_URL : API_BASE_URL;
    }
}
