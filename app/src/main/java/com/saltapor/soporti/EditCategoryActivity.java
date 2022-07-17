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

import java.util.HashMap;
import java.util.Objects;

public class EditCategoryActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    Category category;

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

        // Set text.
        EditText etCategory = findViewById(R.id.etCategory);
        EditText etSubcategory = findViewById(R.id.etSubcategory);

        etCategory.setText(category.category.toString());
        etSubcategory.setText(category.subcategory.toString());

    }

    private void editCategory() {

        // Obtain form data.
        EditText etCategory = findViewById(R.id.etCategory);
        EditText etSubcategory = findViewById(R.id.etSubcategory);

        String categoryName = etCategory.getText().toString();
        String subcategory = etSubcategory.getText().toString();

        // Check if there is missing data.
        if (categoryName.isEmpty() || subcategory.isEmpty()) {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_LONG).show();
            return;
        }

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Obtain data.
        DatabaseReference reference = database.getReference("categories");

        // Create update Map object.
        HashMap<String, Object> categoryUpd = new HashMap<>();
        categoryUpd.put("category", categoryName);
        categoryUpd.put("subcategory", subcategory);
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

    private void deleteCategory() {

        // Build alert.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Seguro de que quieres eliminar esta categoría?");
        builder.setMessage("Los registros eliminados no pueden ser recuperados.");

        // Eliminate.
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // Connect to database.
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                // Obtain data.
                DatabaseReference reference = database.getReference("categories");

                // Delete object.
                reference.child(category.id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(EditCategoryActivity.this, "Categoría eliminada con éxito", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditCategoryActivity.this, "La eliminación ha fallado", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            }
        });

        // Cancel.
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(EditCategoryActivity.this, "Cancelado", Toast.LENGTH_LONG).show();
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
            case R.id.action_delete:
                deleteCategory();
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