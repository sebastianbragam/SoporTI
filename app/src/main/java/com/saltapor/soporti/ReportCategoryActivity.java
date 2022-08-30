package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ReportCategoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ReportCategoryAdapter reportCategoryAdapter;
    ArrayList<ReportItem> reportList;
    ArrayList<Ticket> ticketsList;
    ArrayList<Category> categoriesList = new ArrayList<>();
    String selectedType;

    String dateFrom, dateTo;
    TextView tvDateFrom;
    TextView tvDateTo;

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

        // "Buttons".
        tvDateFrom = findViewById(R.id.tvDateFrom);
        tvDateTo = findViewById(R.id.tvDateTo);

        // Set dates from other report.
        dateFrom = (String) this.getIntent().getSerializableExtra("DATE_FROM");
        dateTo = (String) this.getIntent().getSerializableExtra("DATE_TO");
        if (dateFrom != null && !dateFrom.equals("01/01/2000")) { tvDateFrom.setText(dateFrom); }
        if (dateTo != null && !dateTo.equals("31/12/2099")) { tvDateTo.setText(dateTo); }

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

        // "Buttons" listeners.
        tvDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("DATE", 1);

                DialogFragment newFragment = new DatePickerCategoriesFragment();
                newFragment.setArguments(bundle);

                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        tvDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("DATE", 2);

                DialogFragment newFragment = new DatePickerCategoriesFragment();
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
        reportCategoryAdapter = new ReportCategoryAdapter(ReportCategoryActivity.this, reportList);
        recyclerView.setAdapter(reportCategoryAdapter);

        // See if dates are null and replace with distant dates..
        if (dateFrom == null) { dateFrom = "01/01/2000"; }
        if (dateTo == null) { dateTo = "31/12/2099"; }

        // Convert dates to long (correcting date to datetime by adding a full day).
        long dateFromLong = parseDate(dateFrom);
        long dateToLong = parseDate(dateTo) + (1000 * 60 * 60 * 24);

        // Iterate types list.
        for (Category categoryItem : categoriesList) {

            // Initialize variables.
            Long count = Long.valueOf(0);
            long time = 0;
            Long responseCount = Long.valueOf(0);
            long responseTime = 0;
            Long rating = Long.valueOf(0);

            // Iterate tickets list.
            for (Ticket ticketItem : ticketsList) {
                if (Objects.equals(categoryItem.category, ticketItem.category.category)
                        && Objects.equals(categoryItem.subcategory, ticketItem.category.subcategory)
                        && ticketItem.date > dateFromLong && ticketItem.date < dateToLong) {
                    count = count + 1;
                    time = time + (ticketItem.finishDate - ticketItem.date);
                }
            }

            if (count > 0) {
                // Create ReportItem object and add to report list.
                ReportItem reportItem = new ReportItem(categoryItem.category + ": " + categoryItem.subcategory, count, time, responseCount, responseTime, rating);
                reportList.add(reportItem);
            }

        }

        // Set text if there is no items.
        if (reportList.size() == 0) {
            TextView tvTicketsByCategory = findViewById(R.id.tvTicketsByCategory);
            tvTicketsByCategory.setText("No hay tickets para el tipo seleccionado.");
        } else {
            TextView tvTicketsByCategory = findViewById(R.id.tvTicketsByCategory);
            tvTicketsByCategory.setText("Tickets de tipo '" + selectedType.toLowerCase(Locale.ROOT) + "' por categoría");
        }

        // Update data on recycler.
        reportCategoryAdapter.notifyDataSetChanged();

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
                    if ((Objects.equals(ticket.state, "Finalizado") || Objects.equals(ticket.state, "Finalizado por usuario")) && Objects.equals(ticket.type, selectedType)) {
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

