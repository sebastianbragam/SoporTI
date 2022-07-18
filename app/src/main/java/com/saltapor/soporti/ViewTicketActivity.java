package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.saltapor.soporti.Models.Category;
import com.saltapor.soporti.Models.Ticket;
import com.saltapor.soporti.Models.TicketsAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ViewTicketActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    TicketsAdapter ticketsAdapter;
    ArrayList<Ticket> list;
    Ticket ticket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ticket);

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

        // Obtain object data.
        ticket = (Ticket) this.getIntent().getSerializableExtra("KEY_NAME");

        // Set text.
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvCategoryName = findViewById(R.id.tvCategoryName);
        TextView tvStateName = findViewById(R.id.tvStateName);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvUser = findViewById(R.id.tvUser);
        TextView tvDescription = findViewById(R.id.tvDescription);

        tvTitle.setText(ticket.title);
        tvCategoryName.setText(ticket.category.category + ": " + ticket.category.subcategory);
        tvStateName.setText(ticket.state);
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date(ticket.date));
        tvDate.setText(date);
        tvUser.setText(ticket.user.email);
        tvDescription.setText(ticket.description);

        /* RecyclerView setup.
        recyclerView = findViewById(R.id.rvTickets);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setRecyclerView(); */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_reply_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reply:
                startActivityReply();
                return true;
            case R.id.action_manage:
                startActivityManage();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }

    private void startActivityReply() {
    }

    private void startActivityManage() {
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}