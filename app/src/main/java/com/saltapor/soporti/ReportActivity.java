package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
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
import android.widget.Toast;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ReportActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ReportAdapter reportAdapter;
    ArrayList<ReportItem> reportList;
    ArrayList<Ticket> ticketsList;
    ArrayList<String> typesList = new ArrayList<>();

    String dateFrom, dateTo;
    TextView tvDateFrom;
    TextView tvDateTo;

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
        typesList.add("Requerimiento de cambio");
        typesList.add("Incidente");
        typesList.add("Problema");
        typesList.add("Ayuda");

        // RecyclerView setup.
        recyclerView = findViewById(R.id.rvReport);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create tickets list.
        fillTicketsList();

        // "Buttons" and it's listeners.
        tvDateFrom = findViewById(R.id.tvDateFrom);
        tvDateTo = findViewById(R.id.tvDateTo);

        tvDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("DATE", 1);

                DialogFragment newFragment = new DatePickerFragment();
                newFragment.setArguments(bundle);

                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        tvDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("DATE", 2);

                DialogFragment newFragment = new DatePickerFragment();
                newFragment.setArguments(bundle);

                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

    }

    public void processDateFromPickerResult(int year, int month, int day) {

        // Get integers to string.
        month = month + 1;
        String month_string = Integer.toString(month);
        String day_string = Integer.toString(day);
        String year_string = Integer.toString(year);

        // To format date.
        if (month < 10) {
            month_string = "0" + month;
        }
        if (day < 10) {
            day_string  = "0" + day ;
        }

        // Set TextView and variable.
        dateFrom = (day_string + "/" + month_string + "/" + year_string);
        tvDateFrom.setText(dateFrom);

        // Re create list.
        fillTicketsList();

    }

    public void processDateToPickerResult(int year, int month, int day) {

        // Get integers to string.
        month = month + 1;
        String month_string = Integer.toString(month);
        String day_string = Integer.toString(day);
        String year_string = Integer.toString(year);

        // To format date.
        if (month < 10) {
            month_string = "0" + month;
        }
        if (day < 10) {
            day_string  = "0" + day ;
        }

        // Set TextView and variable.
        dateTo = (day_string + "/" + month_string + "/" + year_string);
        tvDateTo.setText(dateTo);

        // Re create list.
        fillTicketsList();

    }

    private static long parseDate(String text) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.parse(text).getTime();
    }

    private void setRecyclerView () throws ParseException {

        // RecyclerView list setup.
        reportAdapter = new ReportAdapter(ReportActivity.this, reportList, dateFrom, dateTo);
        recyclerView.setAdapter(reportAdapter);

        // See if dates are null and replace with distant dates..
        if (dateFrom == null) { dateFrom = "01/01/2000"; }
        if (dateTo == null) { dateTo = "31/12/2099"; }

        // Convert dates to long (correcting date to datetime by adding a full day).
        long dateFromLong = parseDate(dateFrom);
        long dateToLong = parseDate(dateTo) + (1000 * 60 * 60 * 24);

        // Iterate types list.
        for (String typeItem : typesList) {

            // Initialize variables.
            Long count = Long.valueOf(0);
            long time = 0;
            Long responseCount = Long.valueOf(0);
            long responseTime = 0;
            Long rating = Long.valueOf(0);

            // Iterate tickets list.
            for (Ticket ticketItem : ticketsList) {

                if (Objects.equals(typeItem, ticketItem.type) &&
                        ticketItem.date > dateFromLong && ticketItem.date < dateToLong) {
                    count = count + 1;
                    time = time + (ticketItem.finishDate - ticketItem.date);
                    rating = rating + ticketItem.rate;

                    // Retrieve replies from this ticket here and calculate quantity and mean.

                }

            }

            // Create ReportItem object and add to report list.
            ReportItem reportItem = new ReportItem(typeItem, count, time, responseCount, responseTime, rating);
            reportList.add(reportItem);

        }

        // Update data on recycler.
        reportAdapter.notifyDataSetChanged();

    }

    private void fillTicketsList() {

        // Create lists.
        reportList = new ArrayList<>();
        ticketsList = new ArrayList<>();

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

                try {
                    setRecyclerView();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

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