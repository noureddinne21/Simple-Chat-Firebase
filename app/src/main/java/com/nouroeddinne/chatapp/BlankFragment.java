package com.nouroeddinne.chatapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlankFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    List<String> list = new ArrayList<>();;


    public BlankFragment() {}

    public static BlankFragment newInstance(String param1, List<String> list) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putStringArrayList(ARG_PARAM2, (ArrayList<String>) list);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            list = getArguments().getStringArrayList(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = view.findViewById(R.id.textView_no_data);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        RecyclerView.Adapter adapter;

        Log.d("Firebase",  "size fragment "+(list.size()));

        if (mParam1.equals("Chat")){
            if (list.size() == 0){
                textView.setVisibility(View.VISIBLE);
                textView.setText("No frendes");
            }else {
                textView.setVisibility(View.GONE);
            }
            adapter = new UsersAdapter(list,getActivity());
        }else {
            if (list.size() == 0){
                textView.setVisibility(View.VISIBLE);
                textView.setText("No Groups");
            }else {
                textView.setVisibility(View.GONE);
            }
            adapter = new AdapterGroup(list,getActivity());
        }

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}

























