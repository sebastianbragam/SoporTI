package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import com.saltapor.soporti.Models.RepliesAdapter;
import com.saltapor.soporti.Models.Reply;
import com.saltapor.soporti.Models.Ticket;
import com.saltapor.soporti.Models.User;
import com.saltapor.soporti.Models.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ViewTicketActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    RepliesAdapter repliesAdapter;
    ArrayList<Reply> list;

    User userLogged;
    Ticket ticket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ticket);

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

        // Reference to obtain user data.
        DatabaseReference usersReference = database.getReference("users").child(currentUser.getUid());

        // Listener to obtain user data.
        usersReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userLogged = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Obtain object data.
        ticket = (Ticket) this.getIntent().getSerializableExtra("KEY_NAME");

        // Set text.
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvTypeName = findViewById(R.id.tvTypeName);
        TextView tvPriorityName = findViewById(R.id.tvPriorityName);
        TextView tvCategoryName = findViewById(R.id.tvCategoryName);
        TextView tvStateName = findViewById(R.id.tvStateName);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvUser = findViewById(R.id.tvUser);
        TextView tvAdmin = findViewById(R.id.tvAdmin);
        TextView tvDescription = findViewById(R.id.tvDescription);

        tvTitle.setText("Nº" + ticket.number + ": " + ticket.title);
        tvTypeName.setText(ticket.type);
        tvPriorityName.setText(ticket.priority.substring(3));
        tvCategoryName.setText(ticket.category.category + ": " + ticket.category.subcategory);
        tvStateName.setText(ticket.state);
        String date = new SimpleDateFormat("dd/MM/yy hh:mm aa ").format(new Date(ticket.date));
        tvDate.setText(date);
        tvUser.setText(ticket.user.email);
        tvAdmin.setText(ticket.admin.email);
        tvDescription.setText(ticket.description);

        // RecyclerView setup.
        recyclerView = findViewById(R.id.rvViewTicket);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setRecyclerView();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Reference to update ticket data.
        DatabaseReference usersReference = database.getReference("tickets").child(ticket.id);

        // Listener to obtain user data.
        usersReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticket = snapshot.getValue(Ticket.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Update TextViews.
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvTypeName = findViewById(R.id.tvTypeName);
        TextView tvPriorityName = findViewById(R.id.tvPriorityName);
        TextView tvCategoryName = findViewById(R.id.tvCategoryName);
        TextView tvStateName = findViewById(R.id.tvStateName);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvUser = findViewById(R.id.tvUser);
        TextView tvAdmin = findViewById(R.id.tvAdmin);
        TextView tvDescription = findViewById(R.id.tvDescription);

        tvTitle.setText("Nº" + ticket.number + ": " + ticket.title);
        tvTypeName.setText(ticket.type);
        tvPriorityName.setText(ticket.priority.substring(3));
        tvCategoryName.setText(ticket.category.category + ": " + ticket.category.subcategory);
        tvStateName.setText(ticket.state);
        String date = new SimpleDateFormat("dd/MM/yy hh:mm aa ").format(new Date(ticket.date));
        tvDate.setText(date);
        tvUser.setText(ticket.user.email);
        tvAdmin.setText(ticket.admin.email);
        tvDescription.setText(ticket.description);

    }

    private void setRecyclerView() {

        // Database reference.
        databaseReference = FirebaseDatabase.getInstance().getReference("tickets").child(ticket.id).child("replies");

        // Obtain data.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Obtain TextView element and set text to default.
                TextView tvReplies = findViewById(R.id.tvReplies);
                tvReplies.setText("Respuestas");

                // RecyclerView list setup.
                list = new ArrayList<>();
                repliesAdapter = new RepliesAdapter(ViewTicketActivity.this, list);
                recyclerView.setAdapter(repliesAdapter);

                // Counter to see if there is data.
                int count = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Reply reply = dataSnapshot.getValue(Reply.class);
                    list.add(reply);
                    count = count + 1;
                }

                // Change text if there is no data.
                if (count == 0) {
                    tvReplies.setText("No hay respuestas.");
                }

                // Update data on recycler.
                repliesAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void finishTicket() {

        if (Objects.equals(userLogged.type, "Usuario")) {
            if (Objects.equals(ticket.state, "Pendiente respuesta de usuario")) {
                startActivityUserFinish();
            } else {
                Toast.makeText(ViewTicketActivity.this, "El parte no está pendiente de respuesta", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (Objects.equals(ticket.state, "Finalizado por usuario")) {
                startActivityAdminFinish();
            } else {
                Toast.makeText(ViewTicketActivity.this, "El parte aún no fue finalizado por el usuario o ya fue finalizado por soporte", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void replyCheck() {

        if (Objects.equals(userLogged.type, "Usuario")) {
            if (Objects.equals(ticket.state, "Pendiente respuesta de usuario")) {
                startActivityUserReply();
            } else {
                Toast.makeText(ViewTicketActivity.this, "El parte no está pendiente de respuesta", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (Objects.equals(ticket.state, "Pendiente respuesta de soporte") || Objects.equals(ticket.state, "Derivado a desarrollo/proveedor")) {
                startActivityAdminReply();
            } else {
                Toast.makeText(ViewTicketActivity.this, "El parte no está pendiente de respuesta", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void startActivityAdminFinish() {
        Intent intent = new Intent(this, SupportFinishActivity.class);
        intent.putExtra("KEY_NAME", ticket);
        this.startActivity(intent);
    }

    private void startActivityUserFinish() {
        Intent intent = new Intent(this, UserFinishActivity.class);
        intent.putExtra("KEY_NAME", ticket);
        this.startActivity(intent);
    }

    private void startActivityAdminReply() {
        Intent intent = new Intent(this, SupportReplyActivity.class);
        intent.putExtra("KEY_NAME", ticket);
        this.startActivity(intent);
    }

    private void startActivityUserReply() {
        Intent intent = new Intent(this, UserReplyActivity.class);
        intent.putExtra("KEY_NAME", ticket);
        this.startActivity(intent);
    }

    private void startFilesActivity() {
        Intent intent = new Intent(this, FilesActivity.class);
        intent.putExtra("KEY_NAME", ticket);
        this.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_reply_finish_files, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reply:
                replyCheck();
                return true;
            case R.id.action_finish:
                finishTicket();
                return true;
            case R.id.action_files:
                startFilesActivity();
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