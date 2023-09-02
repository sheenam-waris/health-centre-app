package com.example.healthcentre.utils;


import android.util.Log;

import com.example.healthcentre.models.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public  class SortByDate implements Comparator<Appointment> {

    @Override
    public int compare(Appointment o1, Appointment o2) {
        String reporting_o1 = o1.getReportingTime();
        String reporting_o2 = o2.getReportingTime();
       // Log.e(getClass().getCanonicalName(),"reporting Date : "+reporting_o2 + "  " + reporting_o1);

        SimpleDateFormat sdf = new SimpleDateFormat("DD/MM/YYYY");
//dates to be compare
        Date date1 = null,date2 = null;
        try {
            date1 = sdf.parse(reporting_o1);
            date2 = sdf.parse(reporting_o2);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
//comparing dates
        return date1.compareTo(date2) > 0 ? 1 : -1;
    }

}