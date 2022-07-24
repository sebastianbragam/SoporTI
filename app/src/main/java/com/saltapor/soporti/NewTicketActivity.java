package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.saltapor.soporti.Models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class NewTicketActivity extends AppCompatActivity {

    User userLogged;
    Category category;
    TextView tvCategory;
    boolean categoryCheck = true;
    boolean typeCheck = true;
    int selectionsCount = 0;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ticket);

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

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // User data reference.
        DatabaseReference refUsers = database.getReference("users").child(currentUser.getUid());

        // Listener to update user data.
        refUsers.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    userLogged = user;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        // Type spinner.
        Spinner spType = findViewById(R.id.spType);

        // Create and fill list.
        final List<String> typesList = new ArrayList<>();
        typesList.add("Seleccione un elemento...");
        typesList.add("Requerimiento de servicio");
        typesList.add("Cambio");
        typesList.add("Incidente");
        typesList.add("Problema");
        typesList.add("Ayuda");
        typesList.add("Prevención");

        // Create spinner adapter.
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(NewTicketActivity.this, android.R.layout.simple_spinner_item, typesList) {

            // Disable first element.
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) return false;
                else return true;
            }

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

                // To check if there is a selected item.
                if (adapterView.getSelectedItem().toString() != "Seleccione un elemento..." && selectionsCount == 0) {

                    // Check category
                    typeCheck = false;
                    selectionsCount = 1;

                }

                // Get category object with it's ID.
                type = typesList.get(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }

        });

        // Category "Spinner", actually a TextView with custom dialog.
        TextView tvCategory = findViewById(R.id.tvCategory);

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
                Dialog dialog = new Dialog(NewTicketActivity.this);
                dialog.setContentView(R.layout.spinner_dialog);
                dialog.show();

                // Initialize dialog variables.
                EditText etSearch = dialog.findViewById(R.id.etSearch);
                ListView listView = dialog.findViewById(R.id.listView);
                tvCategory = findViewById(R.id.tvCategory);

                // Fill list.
                final ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(NewTicketActivity.this, android.R.layout.simple_list_item_1, categoriesList);
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

                        // Check for unselected category.
                        categoryCheck = false;

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
                                    category = childSnapshot.getValue(Category.class);

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

    private void registerTicket() {

        // Obtain form data.
        EditText etTicketTitle = findViewById(R.id.etTicketTitle);
        EditText etDescription = findViewById(R.id.etDescription);

        String title = etTicketTitle.getText().toString();
        String description = etDescription.getText().toString();
        User user = userLogged;
        long date = new Date().getTime();

        // Check if there is missing data.
        if (title.isEmpty() || description.isEmpty() || categoryCheck || typeCheck) {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Ticket reference.
        DatabaseReference reference = database.getReference("tickets");

        // Obtain registry ID.
        String id = reference.push().getKey();

        // Create default admin.
        User admin = new User();
        admin.email = "admin@saltapor.com";

        // Create ticket object with form data.
        Ticket ticket = new Ticket(title, category, type, description, date, user, admin, "Pendiente de asignación", id);

        // Upload data.
        reference.child(id).setValue(ticket).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(NewTicketActivity.this, "Ticket registrado con éxito", Toast.LENGTH_LONG).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewTicketActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                finish();
            }
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
                registerTicket();
                return true;
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