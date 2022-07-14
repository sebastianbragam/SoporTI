package com.saltapor.soporti;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Objects;

public class NewTicketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ticket);

        // Configure toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Up navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Cancel button
        TextView btnCancelTicket = findViewById(R.id.btnCancelTicket);
        btnCancelTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMainActivity();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}