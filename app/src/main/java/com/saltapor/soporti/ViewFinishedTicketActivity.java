package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import com.saltapor.soporti.Models.RepliesAdapter;
import com.saltapor.soporti.Models.Reply;
import com.saltapor.soporti.Models.Ticket;
import com.saltapor.soporti.Models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ViewFinishedTicketActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    RepliesAdapter repliesAdapter;
    ArrayList<Reply> list;

    User userLogged;
    Ticket ticket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_finished_ticket);

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

        // Reference to obtain user data.
        DatabaseReference usersReference = database.getReference("users").child(currentUser.getUid());

        // Listener to obtain user data.
        usersReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userLogged = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Obtain object data.
        ticket = (Ticket) this.getIntent().getSerializableExtra("KEY_NAME");

        // Set text.
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvTypeName = findViewById(R.id.tvTypeName);
        TextView tvCategoryName = findViewById(R.id.tvCategoryName);
        TextView tvStateName = findViewById(R.id.tvStateName);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvFinishDate = findViewById(R.id.tvFinishDate);
        TextView tvUser = findViewById(R.id.tvUser);
        TextView tvAdmin = findViewById(R.id.tvAdmin);
        TextView tvDescription = findViewById(R.id.tvDescription);

        tvTitle.setText(ticket.title);
        tvTypeName.setText(ticket.type);
        tvCategoryName.setText(ticket.category.category + ": " + ticket.category.subcategory);
        tvStateName.setText(ticket.state);
        String date = new SimpleDateFormat("dd/MM/yy hh:mm aa ").format(new Date(ticket.date));
        tvDate.setText(date);
        String finishDate = new SimpleDateFormat("dd/MM/yy hh:mm aa ").format(new Date(ticket.finishDate));
        tvFinishDate.setText(finishDate);
        tvUser.setText(ticket.user.email);
        tvAdmin.setText(ticket.admin.email);
        tvDescription.setText(ticket.description);

        // RecyclerView setup.
        recyclerView = findViewById(R.id.rvViewTicket);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setRecyclerView();

    }

    private void setRecyclerView() {

        // Database reference.
        databaseReference = FirebaseDatabase.getInstance().getReference("tickets").child(ticket.id).child("replies");

        // Obtain data.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Obtain TextView element and set text to default.
                TextView tvReplies = findViewById(R.id.tvReplies);
                tvReplies.setText("Respuestas");

                // RecyclerView list setup.
                list = new ArrayList<>();
                repliesAdapter = new RepliesAdapter(ViewFinishedTicketActivity.this, list);
                recyclerView.setAdapter(repliesAdapter);

                // Counter to see if there is data.
                int count = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Reply reply = dataSnapshot.getValue(Reply.class);
                    list.add(reply);
                    count = count + 1;
                }

                // Change text if there is no data.
                if (count == 0) {
                    tvReplies.setText("No hay respuestas.");
                }

                // Update data on recycler.
                repliesAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Reference to update ticket data.
        DatabaseReference usersReference = database.getReference("tickets").child(ticket.id);

        // Listener to obtain user data.
        usersReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticket = snapshot.getValue(Ticket.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Update TextViews.
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvTypeName = findViewById(R.id.tvTypeName);
        TextView tvCategoryName = findViewById(R.id.tvCategoryName);
        TextView tvStateName = findViewById(R.id.tvStateName);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvUser = findViewById(R.id.tvUser);
        TextView tvAdmin = findViewById(R.id.tvAdmin);
        TextView tvDescription = findViewById(R.id.tvDescription);

        tvTitle.setText("Nº" + ticket.number + ": " + ticket.title);
        tvTypeName.setText(ticket.type);
        tvCategoryName.setText(ticket.category.category + ": " + ticket.category.subcategory);
        tvStateName.setText(ticket.state);
        String date = new SimpleDateFormat("dd/MM/yy hh:mm aa ").format(new Date(ticket.date));
        tvDate.setText(date);
        tvUser.setText(ticket.user.email);
        tvAdmin.setText(ticket.admin.email);
        tvDescription.setText(ticket.description);

    }

    private void startFilesFinishedActivity() {
        Intent intent = new Intent(this, FilesFinishedActivity.class);
        intent.putExtra("KEY_NAME", ticket);
        this.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_files, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_files:
                startFilesFinishedActivity();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}