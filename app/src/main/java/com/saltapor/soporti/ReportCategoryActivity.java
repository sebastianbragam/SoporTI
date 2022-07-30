package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saltapor.soporti.Models.Category;
import com.saltapor.soporti.Models.ReportAdapter;
import com.saltapor.soporti.Models.ReportCategoryAdapter;
import com.saltapor.soporti.Models.ReportItem;
import com.saltapor.soporti.Models.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReportCategoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ReportCategoryAdapter reportCategoryAdapter;
    ArrayList<ReportItem> reportList = new ArrayList<>();
    ArrayList<Ticket> ticketsList = new ArrayList<>();
    ArrayList<Category> categoriesList = new ArrayList<>();
    String selectedType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_category);

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
        recyclerView = findViewById(R.id.rvReport);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Update type string.
        selectedType = (String) this.getIntent().getSerializableExtra("KEY_NAME");

        // Create and fill categories list.
        // Categories reference.
        DatabaseReference refCategories = FirebaseDatabase.getInstance().getReference("categories");

        // Listener to update categories data.
        refCategories.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Fill categories list.
                for (DataSnapshot categoriesListSnapshot : dataSnapshot.getChildren()) {
                    Category category = categoriesListSnapshot.getValue(Category.class);
                    categoriesList.add(category);
                }

                // Create tickets list.
                fillTicketsList();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });

    }

    private void setRecyclerView () {

        // RecyclerView list setup.
        reportCategoryAdapter = new ReportCategoryAdapter(ReportCategoryActivity.this, reportList);
        recyclerView.setAdapter(reportCategoryAdapter);

        // Iterate types list.
        for (Category categoryItem : categoriesList) {

            // Initialize variables.
            Long count = Long.valueOf(0);
            long time = 0;

            // Iterate tickets list.
            for (Ticket ticketItem : ticketsList) {
                if (Objects.equals(categoryItem.category, ticketItem.category.category) && Objects.equals(categoryItem.subcategory, ticketItem.category.subcategory)) {
                    count = count + 1;
                    time = time + (ticketItem.finishDate - ticketItem.date);
                }
            }

            if (count > 0) {
                // Create ReportItem object and add to report list.
                ReportItem reportItem = new ReportItem(categoryItem.category + ": " + categoryItem.subcategory, count, time);
                reportList.add(reportItem);
            }

        }

        // Set text if there is no items.
        if (reportList.size() == 0) {
            TextView tvTicketsByCategory = findViewById(R.id.tvTicketsByCategory);
            tvTicketsByCategory.setText("No hay tickets para el tipo seleccionado.");
        }

        // Update data on recycler.
        reportCategoryAdapter.notifyDataSetChanged();

    }

    private void fillTicketsList() {

        // Database query to fill tickets list.
        DatabaseReference ticketsReference = FirebaseDatabase.getInstance().getReference("tickets");

        // Obtain data.
        ticketsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ticket ticket = dataSnapshot.getValue(Ticket.class);
                    if ((Objects.equals(ticket.state, "Finalizado") || Objects.equals(ticket.state, "Finalizado por usuario")) && Objects.equals(ticket.type, selectedType)) {
                        ticketsList.add(ticket);
                    }
                }

                setRecyclerView();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}

