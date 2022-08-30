package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SupportFinishActivity extends AppCompatActivity {

    Ticket ticket;
    TextView tvCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_finish);

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

        // Obtain object data.
        ticket = (Ticket) this.getIntent().getSerializableExtra("KEY_NAME");

        // Set ticket data.
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvTypeName = findViewById(R.id.tvTypeName);
        TextView tvCategoryName = findViewById(R.id.tvCategoryName);
        TextView tvStateName = findViewById(R.id.tvStateName);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvUser = findViewById(R.id.tvUser);
        TextView tvAdmin = findViewById(R.id.tvAdmin);
        TextView tvDescription = findViewById(R.id.tvDescription);

        tvTitle.setText("Nº" + ticket.number + ": " + ticket.title);
        tvTypeName.setText(ticket.type);
        tvCategoryName.setText(ticket.category.category + ": " + ticket.category.subcategory);
        tvStateName.setText(ticket.state);
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date(ticket.date));
        tvDate.setText(date);
        tvUser.setText(ticket.user.email);
        tvAdmin.setText(ticket.admin.email);
        tvDescription.setText(ticket.description);

        // Type spinner.
        Spinner spType = findViewById(R.id.spType);

        // Create and fill list.
        final List<String> typesList = new ArrayList<>();
        typesList.add("Requerimiento de servicio");
        typesList.add("Requerimiento de cambio");
        typesList.add("Incidente");
        typesList.add("Problema");
        typesList.add("Ayuda");

        // Create spinner adapter.
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(SupportFinishActivity.this, android.R.layout.simple_spinner_item, typesList) { };

        // Populate spinner with list.
        typesAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spType.setAdapter(typesAdapter);

        // Select spinner item based on data.
        spType.setSelection(typesList.indexOf(ticket.type));

        // Spinner behaviour.
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // Get category object with it's ID.
                ticket.type = typesList.get(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }

        });

        // Category "Spinner", actually a TextView with custom dialog.
        tvCategory = findViewById(R.id.tvCategory);

        // Select category item based on data.
        tvCategory.setText(ticket.category.category + ": " + ticket.category.subcategory);

        // When button pressed, create custom dialog.
        tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSpinner();
            }
        });

    }

    private void setSpinner () {

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
                Dialog dialog = new Dialog(SupportFinishActivity.this);
                dialog.setContentView(R.layout.spinner_dialog);
                dialog.show();

                // Initialize dialog variables.
                EditText etSearch = dialog.findViewById(R.id.etSearch);
                ListView listView = dialog.findViewById(R.id.listView);
                tvCategory = findViewById(R.id.tvCategory);

                // Fill list.
                final ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(SupportFinishActivity.this, android.R.layout.simple_list_item_1, categoriesList);
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

                        // Query to get category with ID.
                        Query categoryQuery = refCategories.orderByChild("id").equalTo(categoryID);
                        categoryQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren())
                                    ticket.category = childSnapshot.getValue(Category.class);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }

                        });

                        // Dismiss dialog.
                        dialog.dismiss();

                    }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                finishTicket();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }

    private void finishTicket() {

        // Build alert.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Seguro de que quieres finalizar este ticket?");
        builder.setMessage("Revise la categoría y tipo de ticket seleccionados. Una vez finalizado, no se pueden cambiar.");

        // Assign.
        builder.setPositiveButton("Finalizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // Connect to database.
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                // Update ticket category.
                database.getReference("tickets").child(ticket.id).child("category").setValue(ticket.category).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(SupportFinishActivity.this, "Ticket finalizado con éxito", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SupportFinishActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

                // Update ticket type.
                database.getReference("tickets").child(ticket.id).child("type").setValue(ticket.type).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(SupportFinishActivity.this, "Ticket finalizado con éxito", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SupportFinishActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

                // Update ticket state.
                database.getReference("tickets").child(ticket.id).child("state").setValue("Finalizado").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(SupportFinishActivity.this, "Ticket finalizado con éxito", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SupportFinishActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            }
        });

        // Cancel.
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(SupportFinishActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });

        // Show alert.
        builder.show();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}