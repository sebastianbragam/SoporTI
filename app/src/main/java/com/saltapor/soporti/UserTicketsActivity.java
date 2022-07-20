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

public class UserTicketsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    TicketsAdapter ticketsAdapter;
    ArrayList<Ticket> list;
    TextView tvName;
    User userLogged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tickets);

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
        DatabaseReference reference = database.getReference("users").child(currentUser.getUid());

        // RecyclerView setup.
        recyclerView = findViewById(R.id.rvTickets);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Listener to update user data.
        reference.addValueEventListener(new ValueEventListener() {

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

        // Database reference.
        databaseReference = FirebaseDatabase.getInstance().getReference("tickets");
        Query databaseQuery = databaseReference.orderByChild("user/email").equalTo(userLogged.email);

        // Obtain data.
        databaseQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // RecyclerView list setup.
                list = new ArrayList<>();
                ticketsAdapter = new TicketsAdapter(UserTicketsActivity.this, list);
                recyclerView.setAdapter(ticketsAdapter);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ticket ticket = dataSnapshot.getValue(Ticket.class);
                    list.add(ticket);
                }

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
            public boolean onQueryTextSubmit(String query) {
                txtSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                txtSearch(query);
                return false;
            }
        });

        return true;
    }

    private void txtSearch(String query) {



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                startActivityNewTicket();
                return true;
            case R.id.action_users:
                startActivityNewUser();
                return true;
            case R.id.action_categories:
                startActivityCategories();
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

    private void startActivityCategories() {
        Intent intent = new Intent(this, CategoriesActivity.class);
        startActivity(intent);
    }

    private void startActivityNewUser() {
        Intent intent = new Intent(this, NewUserActivity.class);
        startActivity(intent);
    }

    private void logOutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}