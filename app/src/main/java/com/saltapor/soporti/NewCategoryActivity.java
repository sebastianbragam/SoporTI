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
import com.saltapor.soporti.Models.CategoryAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class NewCategoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    CategoryAdapter categoryAdapter;
    ArrayList<Category> list;

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

        // Register button.
        TextView btnRegisterCategory = findViewById(R.id.btnRegisterCategory);
        btnRegisterCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerCategory();
            }
        });

        // RecyclerView setup.
        recyclerView = findViewById(R.id.rvCategories);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setRecyclerView();

    }

    private void setRecyclerView() {

        // RecyclerView list setup.
        list = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(NewCategoryActivity.this, list);
        recyclerView.setAdapter(categoryAdapter);


        // Database reference.
        databaseReference = FirebaseDatabase.getInstance().getReference("categories");

        // Obtain data.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    list.add(category);
                }

                categoryAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void registerCategory() {

        // Obtain form data.
        EditText etCategory = findViewById(R.id.etCategory);
        EditText etSubcategory = findViewById(R.id.etSubcategory);

        String categoryName = etCategory.getText().toString();
        String subcategory = etSubcategory.getText().toString();

        // Create ticket object with form data.
        Category category = new Category(categoryName, subcategory);

        // Check if there is missing data.
        if (categoryName.isEmpty() || subcategory.isEmpty()) {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_LONG).show();
            return;
        }

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Obtain user data.
        DatabaseReference reference = database.getReference("categories");

        // Obtain register ID.
        String uid = reference.push().getKey();

        // Upload data.
        reference.child(uid).setValue(category).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(NewCategoryActivity.this, "Categoría registrada con éxito", Toast.LENGTH_LONG).show();
                etCategory.setText(null);
                etSubcategory.setText(null);
                setRecyclerView();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewCategoryActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}