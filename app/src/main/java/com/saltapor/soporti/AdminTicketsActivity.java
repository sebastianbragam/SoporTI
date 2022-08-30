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
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.saltapor.soporti.Models.AdminTicketsAdapter;
import com.saltapor.soporti.Models.Ticket;
import com.saltapor.soporti.Models.User;

import java.util.ArrayList;
import java.util.Locale;

public class AdminTicketsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference ticketsReference;
    AdminTicketsAdapter adminTicketsAdapter;
    ArrayList<Ticket> list;
    TextView tvName;
    User userLogged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tickets);

        // Initialize FirebaseAuth.
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Configure toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        DatabaseReference usersReference = database.getReference("users").child(currentUser.getUid());

        // RecyclerView setup.
        recyclerView = findViewById(R.id.rvTickets);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Listener to update user data.
        usersReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {

                    // Set user name on TextView.
                    tvName = findViewById(R.id.tvName);
                    tvName.setText(user.firstName + " " + user.lastName);
                    userLogged = user;

                    setRecyclerView();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setRecyclerView() {

        // Database query.
        ticketsReference = FirebaseDatabase.getInstance().getReference("tickets");
        Query ticketsQuery = ticketsReference.orderByChild("admin/email").equalTo(userLogged.email);

        // Obtain data.
        ticketsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Obtain TextView element and set text to default.
                TextView tvTickets = findViewById(R.id.tvTickets);
                tvTickets.setText("Tickets sin asignar");

                // Counter to see if there is data.
                int count = 0;

                // RecyclerView list setup.
                list = new ArrayList<>();
                adminTicketsAdapter = new AdminTicketsAdapter(AdminTicketsActivity.this, list);
                recyclerView.setAdapter(adminTicketsAdapter);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ticket ticket = dataSnapshot.getValue(Ticket.class);
                    list.add(ticket);
                    count = count + 1;
                }

                // Change text if there is no data.
                if (count == 0) {
                    tvTickets.setText("No hay tickets pendientes.");
                }

                // Update data on recycler.
                adminTicketsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_admin_main, menu);

        // Building search bar.
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String filter) {
                filterTickets(filter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String filter) {
                filterTickets(filter);
                return false;
            }
        });

        return true;
    }

    private void filterTickets(String filter) {

        // Database query.
        ticketsReference = FirebaseDatabase.getInstance().getReference("tickets");
        Query ticketsQuery = ticketsReference.orderByChild("admin/email").equalTo(userLogged.email);

        // Obtain data.
        ticketsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // RecyclerView list setup.
                list = new ArrayList<>();
                adminTicketsAdapter = new AdminTicketsAdapter(AdminTicketsActivity.this, list);
                recyclerView.setAdapter(adminTicketsAdapter);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ticket ticket = dataSnapshot.getValue(Ticket.class);
                    if (ticket.number.toString().contains(filter) || ticket.title.toLowerCase(Locale.ROOT).contains(filter)
                            || ticket.description.toLowerCase(Locale.ROOT).contains(filter)) {
                        list.add(ticket);
                    }
                }

                adminTicketsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_users:
                startActivityNewUser();
                return true;
            case R.id.action_report:
                startActivityReport();
                return true;
            case R.id.action_finished:
                startActivityFinishedTickets();
                return true;
            case R.id.action_new_object:
                startActivityCategories();
                return true;
            case R.id.action_documentation:
                startActivityDocumentation();
                return true;
            case R.id.action_logout:
                logOutUser();
                return true;
        }
        return true;
    }

    private void startActivityDocumentation() {
        Intent intent = new Intent(this, DocumentationActivity.class);
        startActivity(intent);
    }

    private void startActivityReport() {
        Intent intent = new Intent(this, ReportActivity.class);
        startActivity(intent);
    }

    private void startActivityNewUser() {
        Intent intent = new Intent(this, UsersActivity.class);
        startActivity(intent);
    }

    private void startActivityFinishedTickets() {
        Intent intent = new Intent(this, FinishedTicketsActivity.class);
        startActivity(intent);
    }

    private void startActivityCategories() {
        Intent intent = new Intent(this, CategoriesActivity.class);
        startActivity(intent);
    }

    private void logOutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}