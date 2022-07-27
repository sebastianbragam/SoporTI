package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FinishedTicketsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference ticketsReference;
    TicketsAdapter ticketsAdapter;
    ArrayList<Ticket> list;
    User userLogged;

    // Variables for filters.
    TextView tvCategory;
    Category category = new Category();
    String type;
    String filterText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_tickets);

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

                    userLogged = user;
                    setRecyclerView();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Type spinner.
        Spinner spType = findViewById(R.id.spType);

        // Create and fill list.
        final List<String> typesList = new ArrayList<>();
        typesList.add("Filtrar por tipo");
        typesList.add("Requerimiento de servicio");
        typesList.add("Cambio");
        typesList.add("Incidente");
        typesList.add("Problema");
        typesList.add("Ayuda");
        typesList.add("Prevención");

        // Create spinner adapter.
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(FinishedTicketsActivity.this, android.R.layout.simple_spinner_item, typesList) {

            // Set color to gray.
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.DKGRAY);
                }
                return view;
            }

        };

        // Populate spinner with list.
        typesAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spType.setAdapter(typesAdapter);

        // Spinner behaviour.
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // Get type and re-create recycler.
                type = typesList.get(i);
                setRecyclerView();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }

        });

        // Category "Spinner", actually a TextView with custom dialog.
        TextView tvCategory = findViewById(R.id.tvCategory);

        // Set default category ID.
        category.id = "Filtrar por categoría";

        // When button pressed, create custom dialog.
        tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSpinner();
            }
        });

    }

    private void setSpinner() {

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Categories reference.
        DatabaseReference refCategories = database.getReference("categories");

        // Listener to update categories data.
        refCategories.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Create and fill categories list and IDs list.
                final List<String> categoriesList = new ArrayList<>();
                final List<String> categoriesIDList = new ArrayList<>();

                // Add first item.
                categoriesList.add("Filtrar por categoría");
                categoriesIDList.add("Filtrar por categoría");

                // Fill rest of list.
                for (DataSnapshot categoriesListSnapshot : dataSnapshot.getChildren()) {

                    // Fill categories list.
                    String categoryName = categoriesListSnapshot.child("category").getValue(String.class);
                    categoryName = categoryName + ": " + categoriesListSnapshot.child("subcategory").getValue(String.class);
                    categoriesList.add(categoryName);

                    // Fill IDs list.
                    String categoryID = categoriesListSnapshot.child("id").getValue(String.class);
                    categoriesIDList.add(categoryID);

                }

                // Build dialog.
                Dialog dialog = new Dialog(FinishedTicketsActivity.this);
                dialog.setContentView(R.layout.spinner_dialog);
                dialog.show();

                // Initialize dialog variables.
                EditText etSearch = dialog.findViewById(R.id.etSearch);
                ListView listView = dialog.findViewById(R.id.listView);
                tvCategory = findViewById(R.id.tvCategory);

                // Fill list.
                final ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(FinishedTicketsActivity.this, android.R.layout.simple_list_item_1, categoriesList);
                listView.setAdapter(categoriesAdapter);

                // Listener for search.
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        categoriesAdapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) { }
                });

                // Item selected behaviour.
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        // Set TextView text to selected category.
                        tvCategory.setText(categoriesAdapter.getItem(i));

                        // Get ID by indexing list.
                        int idIndex = categoriesList.indexOf(categoriesAdapter.getItem(i));
                        String categoryID = categoriesIDList.get(idIndex);

                        // Set default category ID if user does not want to filter category..
                        category.id = "Filtrar por categoría";

                        // Query to get category with ID.
                        Query categoryQuery = refCategories.orderByChild("id").equalTo(categoryID);
                        categoryQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                                    // Get category and re-create recycler.
                                    category = childSnapshot.getValue(Category.class);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }

                        });

                        // Re-create recycler.
                        setRecyclerView();

                        // Dismiss dialog.
                        dialog.dismiss();

                    }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });

    }

    private void setRecyclerView() {

        // Database query.
        ticketsReference = FirebaseDatabase.getInstance().getReference("tickets");
        Query ticketsQuery = ticketsReference.orderByChild("state").equalTo("Finalizado");

        // Obtain data.
        ticketsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // RecyclerView list setup.
                list = new ArrayList<>();
                ticketsAdapter = new TicketsAdapter(FinishedTicketsActivity.this, list, FinishedTicketsActivity.class.getName());
                recyclerView.setAdapter(ticketsAdapter);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ticket ticket = dataSnapshot.getValue(Ticket.class);

                    // Conditions for text filter.
                    if (ticket.title.toLowerCase(Locale.ROOT).contains(filterText) || ticket.description.toLowerCase(Locale.ROOT).contains(filterText)) {

                        // Conditions for type and category.
                        if (type == "Filtrar por tipo" && category.id == "Filtrar por categoría") {
                            list.add(ticket);
                        }
                        if (type == "Filtrar por tipo" && category.id != "Filtrar por categoría") {
                            if (Objects.equals(ticket.category.id, category.id)) {
                                list.add(ticket);
                            }
                        }
                        if (type != "Filtrar por tipo" && category.id == "Filtrar por categoría") {
                            if (Objects.equals(ticket.type, type)) {
                                list.add(ticket);
                            }
                        }
                        if (type != "Filtrar por tipo" && category.id != "Filtrar por categoría") {
                            if (Objects.equals(ticket.category.id, category.id) && Objects.equals(ticket.type, type)) {
                                list.add(ticket);
                            }
                        }

                    }

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
        getMenuInflater().inflate(R.menu.toolbar_menu_search, menu);

        // Building search bar.
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String filter) {

                // Assign text to filterText and re-create recycler.
                filterText = filter;
                setRecyclerView();
                return false;

            }

            @Override
            public boolean onQueryTextChange(String filter) {

                // Assign text to filterText and re-create recycler.
                filterText = filter;
                setRecyclerView();
                return false;

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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