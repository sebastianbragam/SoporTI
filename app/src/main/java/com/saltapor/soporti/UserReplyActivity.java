package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.saltapor.soporti.Models.Reply;
import com.saltapor.soporti.Models.Ticket;
import com.saltapor.soporti.Models.User;
import com.saltapor.soporti.Models.Utils;

import java.text.SimpleDateFormat;
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

public class UserReplyActivity extends AppCompatActivity {

    User userLogged;
    Ticket ticket;
    Reply reply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reply);

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

        // Set text.
        TextView tvDescription = findViewById(R.id.tvDescription);
        tvDescription.setText(ticket.description);
        
    }

    private void registerReply() {

        // Obtain form data.
        EditText etReply = findViewById(R.id.etReply);
        String replyText = etReply.getText().toString();
        long date = new Date().getTime();

        // Check if there is missing data.
        if (replyText.isEmpty()) {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_LONG).show();
            return;
        }

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Obtain data.
        DatabaseReference reference = database.getReference("tickets").child(ticket.id).child("replies");

        // Obtain register ID and other data.
        String id = reference.push().getKey();

        // Create ticket object with form data.
        reply = new Reply(id, replyText, date, userLogged);

        // Upload data.
        reference.child(id).setValue(reply).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                // Update ticket state.
                database.getReference("tickets").child(ticket.id).child("state").setValue("Pendiente respuesta de soporte").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(UserReplyActivity.this, "Respuesta registrada con éxito", Toast.LENGTH_LONG).show();
                        sendMail();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserReplyActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserReplyActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
                finish();
            }
        });

    }

    private void sendMail() {

        try {

            // Create email.
            String host = "smtp.gmail.com";
            String mailToAdmin = ticket.admin.email.trim();
            String mailToUser = ticket.user.email.trim();
            String subject = "Ticket Nº" + ticket.number + " respondido.";
            String message = ("Se informa que el ticket Nº" + ticket.number + " ha sido respondido por "
                    + ticket.user.firstName + " " + ticket.user.lastName + ", y está pendiente de respuesta de soporte. \n\n" +
                    "Respuesta: \"" + reply.reply + "\"\n\n" +
                    "- Título: " + ticket.title + ":\n" +
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