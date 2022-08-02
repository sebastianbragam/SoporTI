package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.saltapor.soporti.Models.Ticket;
import com.saltapor.soporti.Models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditUserActivity extends AppCompatActivity {

    User user;
    boolean typeCheck = true;
    int selectionsCount = 0;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

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
        user = (User) this.getIntent().getSerializableExtra("KEY_NAME");

        // Recover user data.
        EditText etFirstName = findViewById(R.id.etFirstName);
        EditText etLastName = findViewById(R.id.etLastName);
        EditText etEmail = findViewById(R.id.etEmail);

        etFirstName.setText(user.firstName);
        etFirstName.setEnabled(false);
        etLastName.setText(user.lastName);
        etLastName.setEnabled(false);
        etEmail.setText(user.email);
        etEmail.setEnabled(false);

        // Type spinner.
        Spinner spType = findViewById(R.id.spType);

        // Create and fill list.
        final List<String> typesList = new ArrayList<>();
        typesList.add("Seleccione un elemento...");
        typesList.add("Admin");
        typesList.add("Soporte");
        typesList.add("Usuario");

        // Create spinner adapter.
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(EditUserActivity.this, android.R.layout.simple_spinner_item, typesList) {

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

    }

    private void editUser() {

        // Check if there is missing data.
        if (typeCheck) {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build alert.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Seguro de que quieres asignar el rol " + type + " a " + user.firstName + " " + user.lastName + "?");
        builder.setMessage("Todos los permisos del rol anterior serán revocados.");

        // Assign.
        builder.setPositiveButton("Asignar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // Connect to database.
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                // Obtain data.
                DatabaseReference reference = database.getReference("users").child(user.id).child("type");

                // Change/create value.
                reference.setValue(type).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(EditUserActivity.this, "Rol asignado con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditUserActivity.this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

            }
        });

        // Cancel.
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(EditUserActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });

        // Show alert.
        builder.show();

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
                editUser();
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