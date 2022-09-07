package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
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
import com.saltapor.soporti.Models.Category;
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

public class NewTicketActivity extends AppCompatActivity {

    User userLogged;
    Ticket ticket;

    Category category;
    TextView tvCategory;
    boolean categoryCheck = true;

    boolean typeCheck = true;
    int typeSelectionsCount = 0;
    String type;

    boolean priorityCheck = true;
    int prioritySelectionsCount = 0;
    String priority;

    Long ticketNum = Long.valueOf(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ticket);

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

        // Get next ticket number.
        Query ticketNumQuery = database.getReference("tickets").orderByChild("number").limitToLast(1);

        ticketNumQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    ticketNum = childSnapshot.child("number").getValue(Long.class) + Long.valueOf(1);
                }

                // Check when there is no tickets.
                if (ticketNum == 0) { ticketNum = Long.valueOf(1); }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });


        /* Delete tickets.
        Query deleteTicketQuery = database.getReference("tickets").orderByChild("title");

        deleteTicketQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    ticket = childSnapshot.getValue(Ticket.class);
                    if (Objects.equals(ticket.title, "1")) {
                        database.getReference().child("tickets").child(ticket.id).removeValue();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        }); */

        // Type spinner.
        Spinner spType = findViewById(R.id.spType);

        // Create and fill list.
        final List<String> typesList = new ArrayList<>();
        typesList.add("Seleccione un elemento...");
        typesList.add("Requerimiento de servicio");
        typesList.add("Requerimiento de cambio");
        typesList.add("Incidente");
        typesList.add("Problema");
        typesList.add("Ayuda");

        // Create spinner adapter.
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(NewTicketActivity.this, android.R.layout.simple_spinner_item, typesList) {

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
                if (adapterView.getSelectedItem().toString() != "Seleccione un elemento..." && typeSelectionsCount == 0) {

                    // Check type.
                    typeCheck = false;
                    typeSelectionsCount = 1;

                }

                // Get type.
                type = typesList.get(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }

        });

        // Priority spinner.
        Spinner spPriority = findViewById(R.id.spPriority);

        // Create and fill list.
        final List<String> prioritiesList = new ArrayList<>();
        prioritiesList.add("Seleccione un elemento...");
        prioritiesList.add("3: Baja");
        prioritiesList.add("2: Media");
        prioritiesList.add("1: Alta");

        // Create spinner adapter.
        ArrayAdapter<String> prioritiesAdapter = new ArrayAdapter<String>(NewTicketActivity.this, android.R.layout.simple_spinner_item, prioritiesList) {

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
        prioritiesAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spPriority.setAdapter(prioritiesAdapter);

        // Spinner behaviour.
        spPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // To check if there is a selected item.
                if (adapterView.getSelectedItem().toString() != "Seleccione un elemento..." && prioritySelectionsCount == 0) {

                    // Check type.
                    priorityCheck = false;
                    prioritySelectionsCount = 1;

                }

                // Get type.
                priority = prioritiesList.get(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }

        });

        // Category "Spinner", actually a TextView with custom dialog.
        TextView tvCategory = findViewById(R.id.tvCategory);

        // When button pressed, create custom dialog.
        tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSpinner();
            }
        });

    }

    private void setSpinner () {

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Categories reference.
        DatabaseReference refCategories = database.getReference("categories");

        // Listener to update categories data.
        refCategories.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Create and fill categories list and IDs list.
                final List<String> categoriesList = new ArrayList<>();
                final List<String> categoriesIDList = new ArrayList<>();

                // Fill rest of list.
                for (DataSnapshot categoriesListSnapshot : dataSnapshot.getChildren()) {

                    // Check if category is enabled.
                    boolean enabled = Boolean.TRUE.equals(categoriesListSnapshot.child("enabled").getValue(boolean.class));

                    if (enabled) {

                        // Fill categories list.
                        String categoryName = categoriesListSnapshot.child("category").getValue(String.class);
                        categoryName = categoryName + ": " + categoriesListSnapshot.child("subcategory").getValue(String.class);
                        categoriesList.add(categoryName);

                        // Fill IDs list.
                        String categoryID = categoriesListSnapshot.child("id").getValue(String.class);
                        categoriesIDList.add(categoryID);

                    }

                }

                // Build dialog.
                Dialog dialog = new Dialog(NewTicketActivity.this);
                dialog.setContentView(R.layout.spinner_dialog);
                dialog.show();

                // Initialize dialog variables.
                EditText etSearch = dialog.findViewById(R.id.etSearch);
                ListView listView = dialog.findViewById(R.id.listView);
                tvCategory = findViewById(R.id.tvCategory);

                // Fill list.
                final ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(NewTicketActivity.this, android.R.layout.simple_list_item_1, categoriesList);
                listView.setAdapter(categoriesAdapter);

                // Listener for search.
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        categoriesAdapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) { }
                });

                // Item selected behaviour.
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        // Check for unselected category.
                        categoryCheck = false;

                        // Set TextView text to selected category.
                        tvCategory.setText(categoriesAdapter.getItem(i));

                        // Get ID by indexing list.
                        int idIndex = categoriesList.indexOf(categoriesAdapter.getItem(i));
                        String categoryID = categoriesIDList.get(idIndex);

                        // Query to get category with ID.
                        Query categoryQuery = refCategories.orderByChild("id").equalTo(categoryID);
                        categoryQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren())
                                    category = childSnapshot.getValue(Category.class);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }

                        });

                        // Dismiss dialog.
                        dialog.dismiss();

                    }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });

    }

    private void registerTicket() {

        // Obtain form data.
        EditText etTicketTitle = findViewById(R.id.etTicketTitle);
        EditText etDescription = findViewById(R.id.etDescription);

        String title = etTicketTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        User user = userLogged;
        long date = new Date().getTime();

        // Check if ticket number has been obtained.
        if (ticketNum == 0) {
            Toast.makeText(this, "Por favor espere unos segundos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if there is missing data.
        if (title.isEmpty() || description.isEmpty() || categoryCheck || typeCheck || priorityCheck) {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Ticket reference.
        DatabaseReference reference = database.getReference("tickets");

        // Obtain registry ID.
        String id = reference.push().getKey();

        // Create default admin.
        User admin = new User();
        admin.email = "admin@saltapor.com";

        // Create ticket object with form data.
        ticket = new Ticket(title, category, type, priority, description, date, user, admin, "Pendiente de asignación", ticketNum, id);

        // Upload data.
        reference.child(id).setValue(ticket).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(NewTicketActivity.this, "Ticket registrado con éxito", Toast.LENGTH_LONG).show();
                sendMail();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewTicketActivity.this, "El registro ha fallado", Toast.LENGTH_LONG).show();
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
            String subject = "Ticket Nº" + ticket.number + " creado. Prioridad: " + ticket.priority.substring(3);
            String message = ("Se informa que el ticket Nº" + ticket.number + " ha sido creado por "
                    + ticket.user.firstName + " " + ticket.user.lastName + ", y está pendiente de asignación. \n\n" +
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
                registerTicket();
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