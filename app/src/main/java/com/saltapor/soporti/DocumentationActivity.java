package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.saltapor.soporti.Models.File;
import com.saltapor.soporti.Models.FinishedFilesAdapter;
import com.saltapor.soporti.Models.Ticket;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

public class DocumentationActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<File> filesList;
    String fileName, filePath;
    File fileItem;
    FinishedFilesAdapter finishedFilesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation);

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

        // Notice text.
        TextView tvAdminNotice = findViewById(R.id.tvAdminNotice);
        if (Objects.equals(currentUser.getEmail(), "admin@saltapor.com")) {
            tvAdminNotice.setText("Para agregar documentación, por favor diríjase a la consola de Firebase.");
        }

        // RecyclerView setup.
        recyclerView = findViewById(R.id.rvFiles);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        setRecyclerView();
    }

    public void setRecyclerView() {

        // Create list.
        filesList = new ArrayList<>();

        // Set adapter.
        finishedFilesAdapter = new FinishedFilesAdapter(DocumentationActivity.this, filesList, FilesFinishedActivity.class.getName());
        recyclerView.setAdapter(finishedFilesAdapter);

        // Storage reference.
        StorageReference filesRef = FirebaseStorage.getInstance().getReference("documentation");

        // Obtain data.
        filesRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {

                for (StorageReference item : listResult.getItems()) {

                    // Add file to list.
                    fileName = item.getName();
                    filePath = item.getPath();
                    fileItem = new File(fileName, filePath);
                    filesList.add(fileItem);

                    // Update adapter data.
                    finishedFilesAdapter.notifyDataSetChanged();

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DocumentationActivity.this, "La sincronización falló", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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