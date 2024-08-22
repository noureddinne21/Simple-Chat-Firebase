package com.nouroeddinne.chatapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class PagerAdapter extends FragmentStateAdapter {

    ArrayList<MyTab> list = new ArrayList<MyTab>();

    public PagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public void addTab(MyTab tab) {
        list.add(tab);
        notifyDataSetChanged();
    }

    public String getTabName(int position){
        return list.get(position).getTabName();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return list.get(position).getFragment();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
























}
