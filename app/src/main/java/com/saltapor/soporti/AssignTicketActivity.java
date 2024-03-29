package com.saltapor.soporti;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class AssignTicketActivity extends AppCompatActivity {

    Ticket ticket;
    Ticket ticketOld;
    TextView tvCategory;

    boolean emailCheck = true;
    int emailSelectionsCount = 0;

    boolean appOnline = false;
    boolean reconnectionCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_ticket);

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
        ticket = (Ticket) this.getIntent().getSerializableExtra("KEY_NAME");

        // Set ticket data.
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
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date(ticket.date));
        tvDate.setText(date);
        tvUser.setText(ticket.user.email);
        tvAdmin.setText(ticket.admin.email);
        tvDescription.setText(ticket.description);

        // Type spinner.
        Spinner spType = findViewById(R.id.spType);

        // Create and fill list.
        final List<String> typesList = new ArrayList<>();
        typesList.add("Requerimiento de servicio");
        typesList.add("Requerimiento de cambio");
        typesList.add("Incidente");
        typesList.add("Problema");
        typesList.add("Ayuda");

        // Create spinner adapter.
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(AssignTicketActivity.this, android.R.layout.simple_spinner_item, typesList) { };

        // Populate spinner with list.
        typesAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spType.setAdapter(typesAdapter);

        // Select spinner item based on data.
        spType.setSelection(typesList.indexOf(ticket.type));

        // Spinner behaviour.
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // Get category object with it's ID.
                ticket.type = typesList.get(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }

        });

        // Priority spinner.
        Spinner spPriority = findViewById(R.id.spPriority);

        // Create and fill list.
        final List<String> prioritiesList = new ArrayList<>();
        prioritiesList.add("3: Baja");
        prioritiesList.add("2: Media");
        prioritiesList.add("1: Alta");

        // Create spinner adapter.
        ArrayAdapter<String> prioritiesAdapter = new ArrayAdapter<String>(AssignTicketActivity.this, android.R.layout.simple_spinner_item, prioritiesList) { };

        // Populate spinner with list.
        prioritiesAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spPriority.setAdapter(prioritiesAdapter);

        // Select spinner item based on data.
        spPriority.setSelection(prioritiesList.indexOf(ticket.priority));

        // Spinner behaviour.
        spPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // Get category object with it's ID.
                ticket.priority = prioritiesList.get(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }

        });

        // Category "Spinner", actually a TextView with custom dialog.
        tvCategory = findViewById(R.id.tvCategory);

        // Select category item based on data.
        tvCategory.setText(ticket.category.category + ": " + ticket.category.subcategory);

        // When button pressed, create custom dialog.
        tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSpinner();
            }
        });

        // Email spinner.
        Spinner spEmail = findViewById(R.id.spEmail);

        // Connect to database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Categories reference.
        DatabaseReference refUsers = database.getReference("users");

        // Listener to update users data.
        refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Create and fill users list and IDs list.
                final List<String> usersList = new ArrayList<>();
                final List<String> usersIDList = new ArrayList<>();

                // Create first hint item.
                usersList.add("Seleccione un elemento...");
                usersIDList.add(" ");

                // Fill rest of list.
                for (DataSnapshot usersListSnapshot : dataSnapshot.getChildren()) {

                    // Check if user type is support.
                    if (Objects.equals(usersListSnapshot.child("type").getValue(String.class), "Soporte")) {

                        // Fill list.
                        String email = usersListSnapshot.child("email").getValue(String.class);
                        usersList.add(email);

                        // Fill IDs list.
                        String categoryID = usersListSnapshot.child("id").getValue(String.class);
                        usersIDList.add(categoryID);

                    }

                }

                // Create spinner adapter.
                ArrayAdapter<String> usersAdapter = new ArrayAdapter<String>(AssignTicketActivity.this, android.R.layout.simple_spinner_item, usersList) {

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
                usersAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                spEmail.setAdapter(usersAdapter);

                // Spinner behaviour.
                spEmail.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                        // To check if there is a selected item.
                        if (adapterView.getSelectedItem().toString() != "Seleccione un elemento..." && emailSelectionsCount == 0) {

                            // Check category
                            emailCheck = false;
                            emailSelectionsCount = 1;

                        }

                        // Get category object with it's ID.
                        String userID = usersIDList.get(i);
                        Query userQuery = refUsers.orderByChild("id").equalTo(userID);
                        userQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren())

                                    ticket.admin = childSnapshot.getValue(User.class);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }

                        });

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) { }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });

        // Check if app is online.
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                appOnline = connected;
                if (reconnectionCheck && connected) {
                    Toast.makeText(AssignTicketActivity.this, "Conexión reestablecida", Toast.LENGTH_SHORT).show();
                }
                reconnectionCheck = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                appOnline = false;
            }
        });

    }

    private void setSpinner() {

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
                Dialog dialog = new Dialog(AssignTicketActivity.this);
                dialog.setContentView(R.layout.spinner_dialog);
                dialog.show();

                // Initialize dialog variables.
                EditText etSearch = dialog.findViewById(R.id.etSearch);
                ListView listView = dialog.findViewById(R.id.listView);
                tvCategory = findViewById(R.id.tvCategory);

                // Fill list.
                final ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(AssignTicketActivity.this, android.R.layout.simple_list_item_1, categoriesList);
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
                                    ticket.category = childSnapshot.getValue(Category.class);

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

    private void assignTicket() {

        // Check if app is online.
        if (!appOnline) {
            Toast.makeText(this, "Conexión perdida, para realizar cambios debe encontrarse en línea", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if there is missing data.
        if (emailCheck) {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build alert.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Seguro de que quieres asignar este ticket a " + ticket.admin.firstName + " " + ticket.admin.lastName + "?");
        builder.setMessage("Revise la categoría y tipo de ticket seleccionados.");

        // Assign.
        builder.setPositiveButton("Asignar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // Connect to database.
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                // Obtain data.
                DatabaseReference reference = database.getReference("tickets");

                // Copy object to old.
                ticketOld = ticket;

                // Delete object and re-upload.
                reference.child(ticket.id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        // Connect to database.
                        FirebaseDatabase database = FirebaseDatabase.getInstance();

                        // Ticket reference.
                        DatabaseReference reference = database.getReference("tickets");

                        // Create ticket object with form data.
                        Ticket ticket = new Ticket(ticketOld.title, ticketOld.category, ticketOld.type, ticketOld.priority, ticketOld.description, ticketOld.date, ticketOld.user, ticketOld.admin, "Pendiente respuesta de soporte", ticketOld.number, ticketOld.id);

                        // Upload data.
                        reference.child(ticketOld.id).setValue(ticket).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(AssignTicketActivity.this, "Ticket asignado con éxito", Toast.LENGTH_LONG).show();
                                sendMail();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AssignTicketActivity.this, "La asignación ha fallado", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AssignTicketActivity.this, "La asignación ha fallado", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            }
        });

        // Cancel.
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(AssignTicketActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });

        // Show alert.
        builder.show();

    }

    private void sendMail() {

        try {

            // Create email.
            String host = "smtp.gmail.com";
            String mailToAdmin = ticketOld.admin.email.trim();
            String mailToUser = ticketOld.user.email.trim();
            String subject = "Ticket Nº" + ticketOld.number + " asignado. Prioridad: " + ticketOld.priority.substring(3);
            String message = ("Se informa que el ticket Nº" + ticketOld.number + " ha sido asignado a "
                    + ticketOld.admin.firstName + " " + ticketOld.admin.lastName + ". \n\n" +
                    "- Título: " + ticketOld.title + ".\n" +
                    "- Fecha: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date(ticket.date)) + ". \n" +
                    "- Tipo: " + ticketOld.type + ". \n" +
                    "- Categoría: " + ticketOld.category.category + ": " + ticketOld.category.subcategory + ". \n" +
                    "- Descripción: " + ticketOld.description + ". \n" +
                    "- Usuario: " + ticketOld.user.firstName + " " + ticketOld.user.lastName + ". \n\n" +
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
                assignTicket();
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