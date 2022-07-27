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
import com.saltapor.soporti.Models.Ticket;
import com.saltapor.soporti.Models.TicketsAdapter;
import com.saltapor.soporti.Models.User;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class UserTicketsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference ticketsReference;
    TicketsAdapter ticketsAdapter;
    ArrayList<Ticket> list;
    TextView tvName;
    User userLogged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tickets);

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

        // RecyclerView setup.
        recyclerView = findViewById(R.id.rvTickets);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Reference to obtain user data.
        DatabaseReference usersReference = database.getReference("users").child(currentUser.getUid());

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
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    private void setRecyclerView() {

        // Database query.
        ticketsReference = FirebaseDatabase.getInstance().getReference("tickets");
        Query ticketsQuery = ticketsReference.orderByChild("user/email").equalTo(userLogged.email);

        // Obtain data.
        ticketsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Obtain TextView element and set text to default.
                TextView tvTickets = findViewById(R.id.tvTickets);
                tvTickets.setText("Tus tickets");

                // Counter to see if there is data.
                int count = 0;

                // RecyclerView list setup.
                list = new ArrayList<>();
                ticketsAdapter = new TicketsAdapter(UserTicketsActivity.this, list, UserTicketsActivity.class.getName());
                recyclerView.setAdapter(ticketsAdapter);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ticket ticket = dataSnapshot.getValue(Ticket.class);
                    if (!Objects.equals(ticket.state, "Finalizado") && !Objects.equals(ticket.state, "Finalizado por usuario")) {
                        list.add(ticket);
                        count = count + 1;
                    }
                }

                // Change text if there is no data.
                if (count == 0) {
                    tvTickets.setText("No hay tickets pendientes.");
                }

                // Update data on recycler.
                ticketsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_user_main, menu);

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
        Query ticketsQuery = ticketsReference.orderByChild("user/email").equalTo(userLogged.email);

        // Obtain data.
        ticketsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // RecyclerView list setup.
                list = new ArrayList<>();
                ticketsAdapter = new TicketsAdapter(UserTicketsActivity.this, list, UserTicketsActivity.class.getName());
                recyclerView.setAdapter(ticketsAdapter);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ticket ticket = dataSnapshot.getValue(Ticket.class);
                    if (!Objects.equals(ticket.state, "Finalizado") && !Objects.equals(ticket.state, "Finalizado por usuario")) {
                        if (ticket.title.toLowerCase(Locale.ROOT).contains(filter) || ticket.description.toLowerCase(Locale.ROOT).contains(filter)) {
                            list.add(ticket);
                        }
                    }
                }

                ticketsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                startActivityNewTicket();
                return true;
            case R.id.action_finished:
                startActivityFinishedTickets();
                return true;
            case R.id.action_logout:
                logOutUser();
                return true;
        }
        return true;
    }

    private void startActivityNewTicket() {
        Intent intent = new Intent(this, NewTicketActivity.class);
        startActivity(intent);
    }

    private void startActivityFinishedTickets() {
        Intent intent = new Intent(this, FinishedTicketsActivity.class);
        startActivity(intent);
    }

    private void logOutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}