package com.java.virtualeventplatform.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.java.virtualeventplatform.JoinedEventsFragment;
import com.java.virtualeventplatform.MyEventsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MyEventsFragment();
            case 2:
                return new JoinedEventsFragment();
            default:
                // fallback to avoid crashes on unexpected index
                return new MyEventsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // We have 2 tabs: My Events & Upcoming
    }
}
