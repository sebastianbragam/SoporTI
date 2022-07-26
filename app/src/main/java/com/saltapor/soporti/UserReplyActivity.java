package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.saltapor.soporti.Models.Reply;
import com.saltapor.soporti.Models.Ticket;
import com.saltapor.soporti.Models.User;

import java.util.Date;
import java.util.Objects;

public class UserReplyActivity extends AppCompatActivity {

    User userLogged;
    Ticket ticket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reply);

        // Configure toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Up navigation.
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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

        // User data reference.
        DatabaseReference refUsers = database.getReference("users").child(currentUser.getUid());

        // Listener to update user data.
        refUsers.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    userLogged = user;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });

        // Obtain object data.
        ticket = (Ticket) this.getIntent().getSerializableExtra("KEY_NAME");

        // Get TextViews
        TextView tvLastReply = findViewById(R.id.tvLastReply);

        // Reply data reference.
        Query queryReplies = database.getReference("tickets").child(ticket.id).child("replies").orderByChild("date").limitToLast(1);

        // Listener to update user data.
        queryReplies.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Reply reply = dataSnapshot.getValue(Reply.class);
                    if (reply != null) {
                        tvLastReply.setText(reply.reply);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });

        // Set text.
        TextView tvDescription = findViewById(R.id.tvDescription);
        tvDescription.setText(ticket.description);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                registerReply();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }

    private void registerReply() {

        // Obtain form data.
        EditText etReply = findViewById(R.id.etReply);
        String replyText = etReply.getText().toString();
        long date = new Date().getTime();

        // Check if there is missing data.
        if (replyText.isEmpty()) {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_LONG).show();
            return;
        }

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Obtain data.
        DatabaseReference reference = database.getReference("tickets").child(ticket.id).child("replies");

        // Obtain register ID and other data.
        String id = reference.push().getKey();

        // Create ticket object with form data.
        Reply reply = new Reply(id, replyText, date, userLogged);

        // Upload data.
        reference.child(id).setValue(reply).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                // Update ticket state.
                database.getReference("tickets").child(ticket.id).child("state").setValue("Pendiente respuesta de soporte").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(UserReplyActivity.this, "Respuesta registrada con éxito", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserReplyActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserReplyActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                finish();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
}