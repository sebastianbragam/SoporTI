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
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.database.ValueEventListener;
import com.saltapor.soporti.Models.Category;
import com.saltapor.soporti.Models.NewCategoryAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class NewCategoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    NewCategoryAdapter newCategoryAdapter;
    ArrayList<Category> list;

    boolean appOnline = false;
    boolean reconnectionCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_category);

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

        // Check if app is online.
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                appOnline = connected;
                if (reconnectionCheck && connected) {
                    Toast.makeText(NewCategoryActivity.this, "Conexión reestablecida", Toast.LENGTH_SHORT).show();
                }
                reconnectionCheck = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                appOnline = false;
            }
        });

        // RecyclerView setup.
        recyclerView = findViewById(R.id.rvNewCategory);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setRecyclerView();

    }

    private void setRecyclerView() {

        // Database reference.
        databaseReference = FirebaseDatabase.getInstance().getReference("categories");

        // Obtain data.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // RecyclerView list setup.
                list = new ArrayList<>();
                newCategoryAdapter = new NewCategoryAdapter(NewCategoryActivity.this, list);
                recyclerView.setAdapter(newCategoryAdapter);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    list.add(category);
                }

                newCategoryAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });

    }

    private void registerCategory() {

        // Check if app is online.
        if (!appOnline) {
            Toast.makeText(this, "Conexión perdida, para realizar cambios debe encontrarse en línea", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtain form data.
        EditText etCategory = findViewById(R.id.etCategory);
        EditText etSubcategory = findViewById(R.id.etSubcategory);

        String categoryName = etCategory.getText().toString().trim();
        String subcategory = etSubcategory.getText().toString().trim();

        // Check if there is missing data.
        if (categoryName.isEmpty() || subcategory.isEmpty()) {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if category already exists.
        boolean exists = false;

        for (Category categoryItem : list) {
            if (Objects.equals(categoryItem.category.trim(), categoryName) && Objects.equals(categoryItem.subcategory.trim(), subcategory)) {
                exists = true;
            }
        }

        if (exists) {
            Toast.makeText(this, "La categoría ya existe", Toast.LENGTH_SHORT).show();
            return;
        }

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Obtain data.
        DatabaseReference reference = database.getReference("categories");

        // Obtain registry ID.
        String id = reference.push().getKey();

        // Create ticket object with form data.
        Category category = new Category(categoryName, subcategory, id);

        // Upload data.
        reference.child(id).setValue(category).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(NewCategoryActivity.this, "Categoría registrada con éxito", Toast.LENGTH_LONG).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewCategoryActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
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
                registerCategory();
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