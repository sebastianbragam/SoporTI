package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saltapor.soporti.Models.User;

import java.util.Objects;

public class UserCheckActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_check);

        // Configure toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize FirebaseAuth.
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Check if user is logged in.
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);

        // Reference all database to keep updated.
        DatabaseReference usersReference = database.getReference("users");
        DatabaseReference ticketsReference = database.getReference("tickets");
        DatabaseReference categoriesReference = database.getReference("categories");

        // Keep reference always synced.
        usersReference.keepSynced(true);
        ticketsReference.keepSynced(true);
        categoriesReference.keepSynced(true);

        // Listener to update user data.
        usersReference.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {

                    // Check user type and switch to corresponding activity.
                    if (Objects.equals(user.type, "Admin")) { showAdminActivity(); };
                    if (Objects.equals(user.type, "Soporte")) { showSupportActivity(); };
                    if (Objects.equals(user.type, "Usuario")) { showUserActivity(); };
                    if (user.type == null) {

                        // Tell user he has no role.
                        Toast.makeText(UserCheckActivity.this, "Solicitar al administrador que le asigne un rol", Toast.LENGTH_SHORT).show();

                        // Sign out.
                        auth.signOut();
                        Intent intent = new Intent(UserCheckActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                } else {

                    // Tell user he has no role.
                    Toast.makeText(UserCheckActivity.this, "Usuario inexistente, hablar con el administrador", Toast.LENGTH_SHORT).show();

                    // Sign out.
                    auth.signOut();
                    Intent intent = new Intent(UserCheckActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showUserActivity() {
        Intent intent = new Intent(this, UserTicketsActivity.class);
        startActivity(intent);
        finish();
    }

    private void showSupportActivity() {
        Intent intent = new Intent(this, SupportTicketsActivity.class);
        startActivity(intent);
        finish();
    }

    private void showAdminActivity() {
        Intent intent = new Intent(this, AdminTicketsActivity.class);
        startActivity(intent);
        finish();
    }

}