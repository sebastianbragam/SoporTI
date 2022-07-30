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
import com.saltapor.soporti.Models.ReportAdapter;
import com.saltapor.soporti.Models.ReportItem;
import com.saltapor.soporti.Models.Ticket;
import com.saltapor.soporti.Models.TicketsAdapter;
import com.saltapor.soporti.Models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ReportActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ReportAdapter reportAdapter;
    ArrayList<ReportItem> reportList = new ArrayList<>();
    ArrayList<Ticket> ticketsList = new ArrayList<>();
    ArrayList<String> typesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

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

        // Create and fill types list.
        typesList.add("Requerimiento de servicio");
        typesList.add("Cambio");
        typesList.add("Incidente");
        typesList.add("Problema");
        typesList.add("Ayuda");
        typesList.add("Prevención");

        // RecyclerView setup.
        recyclerView = findViewById(R.id.rvReport);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create tickets list.
        fillTicketsList();

    }

    private void setRecyclerView () {

        // RecyclerView list setup.
        reportAdapter = new ReportAdapter(ReportActivity.this, reportList);
        recyclerView.setAdapter(reportAdapter);

        // Iterate types list.
        for (String typeItem : typesList) {

            // Initialize variables.
            Long count = Long.valueOf(0);
            long time = 0;

            // Iterate tickets list.
            for (Ticket ticketItem : ticketsList) {
                if (Objects.equals(typeItem, ticketItem.type)) {
                    count = count + 1;
                    time = time + (ticketItem.finishDate - ticketItem.date);
                }
            }

            // Create ReportItem object and add to report list.
            ReportItem reportItem = new ReportItem(typeItem, count, time);
            reportList.add(reportItem);

        }

        // Update data on recycler.
        reportAdapter.notifyDataSetChanged();

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
                    if (Objects.equals(ticket.state, "Finalizado") || Objects.equals(ticket.state, "Finalizado por usuario")) {
                        ticketsList.add(ticket);
                    }
                }

                setRecyclerView();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}