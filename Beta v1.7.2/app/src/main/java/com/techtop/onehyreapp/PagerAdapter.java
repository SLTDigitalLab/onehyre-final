package com.techtop.onehyreapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PagerAdapter extends FragmentStateAdapter {
    public PagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new TabPersonalInfo();
            case 1: return new TabEnterpriseInfo();
            case 2: return new TabStats();
            default: return new TabPersonalInfo();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
