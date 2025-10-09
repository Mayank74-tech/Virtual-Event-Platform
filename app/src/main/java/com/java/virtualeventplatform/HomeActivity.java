package com.java.virtualeventplatform;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.java.virtualeventplatform.adapters.ViewPagerAdapter;

public class HomeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Handle window insets for fullscreen layouts
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        ExtendedFloatingActionButton fabAddEvent = findViewById(R.id.fabAddEvent);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Setup ViewPager
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Add page animation
        viewPager.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            page.setScaleY(0.85f + (1 - absPos) * 0.15f);
            page.setAlpha(0.5f + (1 - absPos) * 0.5f);
            page.setTranslationX(-position * page.getWidth() / 3);
        });

        // Setup Tabs
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("My Events");
            } else {
                tab.setText("Joined");
            }
        }).attach();

        // Animate FAB
        fabAddEvent.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(300)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .start();

        // FAB click -> open CreateEventActivity
        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateEventActivity.class);
            startActivity(intent);
        });

        // ✅ Bottom Navigation click handling
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                // Already on HomeActivity — do nothing
                return true;

            } else if (id == R.id.nav_profile) {
                // Navigate to ProfileActivity
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;

            } else if (id == R.id.nav_explore) {
                // Example: Navigate to EventsActivity
               Intent intent = new Intent(HomeActivity.this, UpcomingEventsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            return false;
        });
    }
}
