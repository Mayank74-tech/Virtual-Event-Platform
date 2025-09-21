package com.java.virtualeventplatform;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);


        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);

            // Scale down pages as they move away
            page.setScaleY(0.85f + (1 - absPos) * 0.15f);

            // Fade out pages as they move away
            page.setAlpha(0.5f + (1 - absPos) * 0.5f);

            // Optional: slight translation for depth effect
            page.setTranslationX(-position * page.getWidth() / 3);
        });


        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("My Events");
            } else if (position == 1) {
                tab.setText("Upcoming");
            } else {
                tab.setText("Joined");
            }
        }).attach();


        ExtendedFloatingActionButton fabAddEvent = findViewById(R.id.fabAddEvent);
        fabAddEvent.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(300)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .start();


        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateEventActivity.class);
            startActivity(intent);
        });

    }
}
