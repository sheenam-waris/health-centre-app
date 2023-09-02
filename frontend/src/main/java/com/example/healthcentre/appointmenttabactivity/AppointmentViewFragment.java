package com.example.healthcentre.appointmenttabactivity;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.healthcentre.R;
import com.example.healthcentre.models.Appointment;

import java.util.ArrayList;


public class AppointmentViewFragment extends Fragment {

    RecyclerView rcv;

    private static final String ARG_PARAM1 = "appointmentsList";
    private static final String ARG_PARAM2 = "TAB_INDEX";




    private ArrayList<Appointment> appointmentsList;
    private int tabIndex;


    public AppointmentViewFragment() {
        // Required empty public constructor
    }

    public static AppointmentViewFragment newInstance(ArrayList<Appointment> appointments,int tabIndex) {
        AppointmentViewFragment fragment = new AppointmentViewFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1,appointments);
        args.putInt(ARG_PARAM2,tabIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            appointmentsList = getArguments().getParcelableArrayList(ARG_PARAM1);
            tabIndex = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_appointment_view, container, false);
        rcv = view.findViewById(R.id.recyclerview_appointment_fragment);
        switch(tabIndex){
            case 0:
                ApprovedAppointmentRecyclerViewAdaptor aarva = new ApprovedAppointmentRecyclerViewAdaptor();
                aarva.setData(appointmentsList);
                rcv.setAdapter(aarva);
                rcv.setLayoutManager(new LinearLayoutManager(getContext()));
                break;
            case 1:
                CompletedAppointmentRecyclerViewAdaptor carva = new CompletedAppointmentRecyclerViewAdaptor();
                carva.setData(appointmentsList);
                rcv.setAdapter(carva);
                rcv.setLayoutManager(new LinearLayoutManager(getContext()));
                break;
            case 2:

                RequestAppointmentRecyclerViewAdaptor rarv = new RequestAppointmentRecyclerViewAdaptor(appointmentsList);
                rcv.setAdapter(rarv);
                rcv.setLayoutManager(new LinearLayoutManager(getContext()));
                break;
        }
        /*
        * RecyclerView = view.findViewById(ID)
        * switch tabIndex{
        * case 0,1,2
        * Create specific view pager adaptor class extends fragmentpageradp based on Tab selected
        *
        * }
        *
        * */

        return view;
    }
    public void setData(ArrayList<Appointment> appointments){
        this.appointmentsList = appointments;
    }

}