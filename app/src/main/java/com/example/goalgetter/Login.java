package com.example.goalgetter;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private FirebaseAuth auth;

    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        checkIfUserLoggedIn();


        Button loginButton = findViewById(R.id.loginButton);
        EditText loginEmailEditText = findViewById(R.id.loginEmailEditText);
        EditText loginPasswordEditText = findViewById(R.id.loginPasswordEditText);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String loginEmail = loginEmailEditText.getText().toString();
                String loginPassword = loginPasswordEditText.getText().toString();
                if(loginEmail.isEmpty() || loginPassword.isEmpty()){
                    Toast.makeText(Login.this, "Please fill the fields", Toast.LENGTH_SHORT).show();
                }
                else{
                    loginUser(loginEmail, loginPassword);
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        permissions();

    }
    public void permissions(){


        // Check and request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (!isGranted) {
                            // Handle the case where the user denied the notification permission
                            Toast.makeText(this, "Notification permission is required for task reminders.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // Request the POST_NOTIFICATIONS permission
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Check and request exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        // Open "Manage Notifications" settings if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8.0 (Oreo) or higher
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (!notificationManager.areNotificationsEnabled()) {
                // Navigate to the app's notification settings
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }
        }

    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // Redirect to the main activity
                        Intent intent = new Intent(Login.this, bottomNavigation.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(Login.this, "Authentication failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkIfUserLoggedIn() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, redirect to main activity
            Intent intent = new Intent(Login.this, bottomNavigation.class);
            startActivity(intent);
            finish();
        }
    }

    }
