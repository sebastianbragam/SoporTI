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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.saltapor.soporti.Models.Reply;
import com.saltapor.soporti.Models.Ticket;
import com.saltapor.soporti.Models.User;
import com.saltapor.soporti.Models.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

public class SupportReplyActivity extends AppCompatActivity {

    User userLogged;
    Ticket ticket;
    Reply reply;
    boolean stateCheck = true;
    int selectionsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_reply);

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
            public void onCancelled(@NonNull DatabaseError error) { }

        });

        // Obtain object data.
        ticket = (Ticket) this.getIntent().getSerializableExtra("KEY_NAME");

        // Get TextViews
        TextView tvLastReply = findViewById(R.id.tvLastReply);
        TextView tvDescription = findViewById(R.id.tvDescription);

        // Set text.
        tvDescription.setText(ticket.description);

        // Reply data reference.
        Query queryReplies = database.getReference("tickets").child(ticket.id).child("replies").orderByChild("date").limitToLast(1);

        // Listener to update user data.
        queryReplies.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Reply reply = dataSnapshot.getValue(Reply.class);
                    if (reply != null) {
                        tvLastReply.setText(reply.reply);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });

        // Type spinner.
        Spinner spState = findViewById(R.id.spState);

        // Create and fill list.
        final List<String> statesList = new ArrayList<>();
        statesList.add("Seleccione un elemento...");
        statesList.add("Pendiente respuesta de usuario");
        statesList.add("Derivado a desarrollo/proveedor");

        // Create spinner adapter.
        ArrayAdapter<String> statesAdapter = new ArrayAdapter<String>(SupportReplyActivity.this, android.R.layout.simple_spinner_item, statesList) {

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
        statesAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spState.setAdapter(statesAdapter);

        // Spinner behaviour.
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // To check if there is a selected item.
                if (adapterView.getSelectedItem().toString() != "Seleccione un elemento..." && selectionsCount == 0) {

                    // Check category
                    stateCheck = false;
                    selectionsCount = 1;

                }

                // Get category object with it's ID.
                ticket.state = statesList.get(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }

        });

    }

    private void registerReply() {

        // Obtain form data.
        EditText etReply = findViewById(R.id.etReply);
        String replyText = etReply.getText().toString();
        long date = new Date().getTime();

        // Check if there is missing data.
        if (replyText.isEmpty() || stateCheck) {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_LONG).show();
            return;
        }

        // Build alert.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Seguro de que quieres responder este ticket?");
        builder.setMessage("Revise el estado seleccionado: " + ticket.state + ".");

        // Assign.
        builder.setPositiveButton("Responder", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // Connect to database.
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                // Replies reference.
                DatabaseReference refReplies = database.getReference("tickets").child(ticket.id).child("replies");

                // Obtain register ID and other data.
                String id = refReplies.push().getKey();

                // Create ticket object with form data.
                reply = new Reply(id, replyText, date, userLogged);

                // Upload data.
                refReplies.child(id).setValue(reply).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        // Update ticket state.
                        database.getReference("tickets").child(ticket.id).child("state").setValue(ticket.state).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(SupportReplyActivity.this, "Respuesta registrada con éxito", Toast.LENGTH_LONG).show();
                                sendMail();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SupportReplyActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SupportReplyActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            }
        });


        // Cancel.
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(SupportReplyActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });

        // Show alert.
        builder.show();

    }

    private void sendMail() {

        try {

            // Create email.
            String host = "smtp.gmail.com";
            String mailToAdmin = ticket.admin.email.trim();
            String mailToUser = ticket.user.email.trim();
            String subject = "Ticket Nº" + ticket.number + " respondido. Prioridad: " + ticket.priority.substring(3);
            String message = ("Se informa que el ticket Nº" + ticket.number + " ha sido respondido por "
                    + ticket.admin.firstName + " " + ticket.admin.lastName + ", y está pendiente de respuesta de usuario. \n\n" +
                    "Respuesta: \"" + reply.reply + "\"\n\n" +
                    "- Título: " + ticket.title + ".\n" +
                    "- Fecha: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date(ticket.date)) + ". \n" +
                    "- Tipo: " + ticket.type + ". \n" +
                    "- Categoría: " + ticket.category.category + ": " + ticket.category.subcategory + ". \n" +
                    "- Descripción: " + ticket.description + ". \n" +
                    "- Usuario: " + ticket.user.firstName + " " + ticket.user.lastName + ". \n\n" +
                    "Saludos!");

            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", 465);
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(Utils.email, Utils.password);
                }
            });

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(mailToAdmin));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(mailToUser));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();

        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

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
                registerReply();
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