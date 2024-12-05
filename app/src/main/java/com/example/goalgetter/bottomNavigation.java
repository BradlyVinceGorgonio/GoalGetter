package com.example.goalgetter;

import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class bottomNavigation extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;
    private long lastClickTime = 0;  // Track the last click time

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
            if (System.currentTimeMillis() - lastClickTime < 2000) {
                return; // Prevent click if less than 1.5 seconds passed
            }
            lastClickTime = System.currentTimeMillis(); // Update the last click time
            loadFragment(new NotificationFragment(), false);
        });

        profileButton.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 2000) {
                return; // Prevent click if less than 1.5 seconds passed
            }
            lastClickTime = System.currentTimeMillis(); // Update the last click time

            PopupMenu popupMenu = new PopupMenu(this, profileButton);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.profile_menu, popupMenu.getMenu());

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener(this::onProfileMenuItemClick);
            popupMenu.show();
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (System.currentTimeMillis() - lastClickTime < 2000) {
                return false; // Prevent click if less than 1.5 seconds passed
            }
            lastClickTime = System.currentTimeMillis(); // Update the last click time

            if (itemId == R.id.navHome) {
                loadFragment(new HomeFragment(), false);
            } else if (itemId == R.id.navCalendar) {
                loadFragment(new CalendarFragment(), false);
            } else if (itemId == R.id.navTodo) {
                loadFragment(new TodoFragment(), false);
            } else {
                loadFragment(new GroupFragment(), false);
            }
            return true;
        });

        loadFragment(new HomeFragment(), true);
    }

    private boolean onProfileMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.editprofile) {
            loadFragment(new ProfileFragment(), false);
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.priorityModelist) {
            loadFragment(new PriorityModeFragment(), false);
            Toast.makeText(this, "Priority Mode List clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else {
            return false;
        }
    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (isAppInitialized) {
            fragmentTransaction.add(R.id.frameLayout, fragment);
        } else {
            fragmentTransaction.replace(R.id.frameLayout, fragment);
        }
        fragmentTransaction.commit();
    }
}
