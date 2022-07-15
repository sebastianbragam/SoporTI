package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class NewTicketActivity extends AppCompatActivity {

    User userLogged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ticket);

        // Configure toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Up navigation.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        // Obtain user data.
        DatabaseReference reference = database.getReference("users").child(currentUser.getUid());

        // Listener to update user data shown.
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    userLogged = user;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Register button.
        TextView btnRegisterTicket = findViewById(R.id.btnRegisterTicket);
        btnRegisterTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerTicket();
            }
        });

        // Cancel button.
        TextView btnCancelTicket = findViewById(R.id.btnCancelTicket);
        btnCancelTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMainActivity();
            }
        });

    }

    private void registerTicket() {

        // Obtain form data.
        EditText etTicketTitle = findViewById(R.id.etTicketTitle);
        EditText etDescription = findViewById(R.id.etDescription);
        Spinner spCategory = findViewById(R.id.spCategory);

        String title = etTicketTitle.getText().toString();
        String description = etDescription.getText().toString();
        String category = "Remoto"; // Change!!!!!!!!!!!!!!!!!!!!!!!!!
        User user = userLogged;
        long date = new Date().getTime();

        // Create ticket object with form data.
        Ticket ticket = new Ticket(title, category, description, date, user);

        // Check if there is missing data.
        if (title.isEmpty() || description.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_LONG).show();
            return;
        }

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Obtain user data.
        DatabaseReference reference = database.getReference("tickets");

        // Obtain register ID.
        String uid = reference.push().getKey();

        // Upload data.
        reference.child(uid).setValue(ticket).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(NewTicketActivity.this, "Ticket registrado con éxito", Toast.LENGTH_LONG).show();
                showMainActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewTicketActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                showMainActivity();
            }
        });

    }

    private void showMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}