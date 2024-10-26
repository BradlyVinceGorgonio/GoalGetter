package com.example.goalgetter;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class bottomNavigation extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bottom_navigation);



        bottomNavigationView = findViewById(R.id.bottomnavView);
        frameLayout = findViewById(R.id.frameLayout);

        ImageButton notificationButton = findViewById(R.id.notificationButton);
        ImageButton profileButton = findViewById(R.id.profileButton);

        notificationButton.setOnClickListener(v -> {
            loadFragment(new NotificationFragment(), false);
        });

        profileButton.setOnClickListener(v -> {
            loadFragment(new ProfileFragment(), false);
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == R.id.navHome)
                {
                    loadFragment(new HomeFragment(),false);
                }
                else if (itemId == R.id.navCalendar)
                {
                    loadFragment(new CalendarFragment(),false);
                }
                else if (itemId == R.id.navTodo)
                {
                    loadFragment(new TodoFragment(),false);
                }
                else //group mapupunta
                {
                    loadFragment(new GroupFragment(),false);
                }

                return true;
            }
        });


        loadFragment(new HomeFragment(),true);



//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }

    private void loadFragment (Fragment fragment, boolean isAppInitialized)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        if(isAppInitialized)
        {
            fragmentTransaction.add(R.id.frameLayout,fragment);
        }
        else
        {
            fragmentTransaction.replace(R.id.frameLayout, fragment);
        }
        fragmentTransaction.commit();
    }
}