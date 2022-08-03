package com.saltapor.soporti;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.saltapor.soporti.Models.Category;
import com.saltapor.soporti.Models.Ticket;
import com.saltapor.soporti.Models.TicketsAdapter;
import com.saltapor.soporti.Models.User;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class SupportTicketsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference ticketsReference;
    TicketsAdapter ticketsAdapter;
    ArrayList<Ticket> list;
    TextView tvName;
    User userLogged;

    // Filters.
    String type;
    Category category = new Category();

    // Activity for result.
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == 1) {
                        Intent resultIntent = result.getData();
                        if (resultIntent != null) {
                            category = (Category) resultIntent.getSerializableExtra("CATEGORY");
                            type = (String) resultIntent.getSerializableExtra("TYPE");
                        }
                        setRecyclerView();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_tickets);

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
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Set default type and category id.
        type = "Filtrar por tipo";
        category.id = "Filtrar por categoría";

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
                tvTickets.setText("Tus tickets");

                // Counter to see if there is data.
                int count = 0;

                // RecyclerView list setup.
                list = new ArrayList<>();
                ticketsAdapter = new TicketsAdapter(SupportTicketsActivity.this, list, SupportTicketsActivity.class.getName());
                recyclerView.setAdapter(ticketsAdapter);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ticket ticket = dataSnapshot.getValue(Ticket.class);
                    if (!Objects.equals(ticket.state, "Finalizado")) {

                        // Conditions for type and category.
                        if (Objects.equals(type, "Filtrar por tipo") && Objects.equals(category.id, "Filtrar por categoría")) {
                            list.add(ticket);
                            count = count + 1;
                        }
                        if (Objects.equals(type, "Filtrar por tipo") && !Objects.equals(category.id, "Filtrar por categoría")) {
                            if (Objects.equals(ticket.category.id, category.id)) {
                                list.add(ticket);
                                count = count + 1;
                            }
                        }
                        if (!Objects.equals(type, "Filtrar por tipo") && Objects.equals(category.id, "Filtrar por categoría")) {
                            if (Objects.equals(ticket.type, type)) {
                                list.add(ticket);
                                count = count + 1;
                            }
                        }
                        if (!Objects.equals(type, "Filtrar por tipo") && !Objects.equals(category.id, "Filtrar por categoría")) {
                            if (Objects.equals(ticket.category.id, category.id) && Objects.equals(ticket.type, type)) {
                                list.add(ticket);
                                count = count + 1;
                            }
                        }

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
        getMenuInflater().inflate(R.menu.toolbar_menu_support_main, menu);

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

                // Obtain TextView element and set text to default.
                TextView tvTickets = findViewById(R.id.tvTickets);
                tvTickets.setText("Tus tickets");

                // RecyclerView list setup.
                list = new ArrayList<>();
                ticketsAdapter = new TicketsAdapter(SupportTicketsActivity.this, list, SupportTicketsActivity.class.getName());
                recyclerView.setAdapter(ticketsAdapter);

                // Counter to see if there is data.
                int count = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ticket ticket = dataSnapshot.getValue(Ticket.class);
                    if (!Objects.equals(ticket.state, "Finalizado")) {
                        if (ticket.title.toLowerCase(Locale.ROOT).contains(filter)
                                || ticket.description.toLowerCase(Locale.ROOT).contains(filter)
                                || ticket.state.toLowerCase(Locale.ROOT).contains(filter)) {

                            // Conditions for type and category.
                            if (Objects.equals(type, "Filtrar por tipo") && Objects.equals(category.id, "Filtrar por categoría")) {
                                list.add(ticket);
                                count = count + 1;
                            }
                            if (Objects.equals(type, "Filtrar por tipo") && !Objects.equals(category.id, "Filtrar por categoría")) {
                                if (Objects.equals(ticket.category.id, category.id)) {
                                    list.add(ticket);
                                    count = count + 1;
                                }
                            }
                            if (!Objects.equals(type, "Filtrar por tipo") && Objects.equals(category.id, "Filtrar por categoría")) {
                                if (Objects.equals(ticket.type, type)) {
                                    list.add(ticket);
                                    count = count + 1;
                                }
                            }
                            if (!Objects.equals(type, "Filtrar por tipo") && !Objects.equals(category.id, "Filtrar por categoría")) {
                                if (Objects.equals(ticket.category.id, category.id) && Objects.equals(ticket.type, type)) {
                                    list.add(ticket);
                                    count = count + 1;
                                }
                            }

                        }
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
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_object:
                startActivityCategories();
                return true;
            case R.id.action_filter:
                startActivityFilter();
                return true;
            case R.id.action_finished:
                startActivityFinishedTickets();
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

    private void startActivityFilter() {
        Intent intent = new Intent(SupportTicketsActivity.this, FilterActivity.class);
        intent.putExtra("TYPE", type);
        intent.putExtra("CATEGORY", category);
        activityResultLauncher.launch(intent);
    }

    private void startActivityDocumentation() {
        Intent intent = new Intent(this, DocumentationActivity.class);
        startActivity(intent);
    }

    private void startActivityCategories() {
        Intent intent = new Intent(this, CategoriesActivity.class);
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