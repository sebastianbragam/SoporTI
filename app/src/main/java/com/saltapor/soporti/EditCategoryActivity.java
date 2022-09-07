package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.saltapor.soporti.Models.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class EditCategoryActivity extends AppCompatActivity {

    Category category;
    ArrayList<Category> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category);

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
        category = (Category) this.getIntent().getSerializableExtra("KEY_NAME");
        list = (ArrayList<Category>) this.getIntent().getSerializableExtra("KEY_LIST");

        // Set text.
        EditText etCategory = findViewById(R.id.etCategory);
        EditText etSubcategory = findViewById(R.id.etSubcategory);
        EditText etEnabled = findViewById(R.id.etEnabled);

        etCategory.setText(category.category);
        etSubcategory.setText(category.subcategory);

        // Check if it is enabled or not.
        if (category.enabled) {
            etEnabled.setText("Habilitada");
            etEnabled.setEnabled(false);
        } else {
            etEnabled.setText("Deshabilitada");
            etEnabled.setEnabled(false);
        }

    }

    private void editCategory() {

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

        // Check if category has been edited.
        if (!Objects.equals(category.category.trim(), categoryName) || !Objects.equals(category.subcategory.trim(), subcategory)) {

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

        }

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Set reference.
        DatabaseReference reference = database.getReference("categories");

        // Create update Map object.
        HashMap<String, Object> categoryUpd = new HashMap<>();
        categoryUpd.put("category", categoryName);
        categoryUpd.put("subcategory", subcategory);
        categoryUpd.put("enabled", true);
        categoryUpd.put("id", category.id);

        // Update data.
        reference.child(category.id).updateChildren(categoryUpd).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(EditCategoryActivity.this, "Categoría editada con éxito", Toast.LENGTH_LONG).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditCategoryActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                finish();
            }
        });

    }

    private void disableCategory() {

        // Build alert.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Seguro de que quieres deshabilitar esta categoría?");
        builder.setMessage("Para volver a habilitarla, puedes editarla.");

        // Eliminate.
        builder.setPositiveButton("Deshabilitar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // Connect to database.
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                // Obtain data.
                DatabaseReference reference = database.getReference("categories");

                // Create update Map object.
                HashMap<String, Object> categoryUpd = new HashMap<>();
                categoryUpd.put("category", category.category);
                categoryUpd.put("subcategory", category.subcategory);
                categoryUpd.put("enabled", false);
                categoryUpd.put("id", category.id);

                // Update enabled data.
                reference.child(category.id).updateChildren(categoryUpd).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(EditCategoryActivity.this, "Categoría deshabilitada con éxito", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditCategoryActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            }
        });

        // Cancel.
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(EditCategoryActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });

        // Show alert.
        builder.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_disable:
                disableCategory();
                return true;
            case R.id.action_edit:
                editCategory();
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