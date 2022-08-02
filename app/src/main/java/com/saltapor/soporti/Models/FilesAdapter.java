package com.saltapor.soporti.Models;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.saltapor.soporti.EditCategoryActivity;
import com.saltapor.soporti.FilesActivity;
import com.saltapor.soporti.R;

import java.io.IOException;
import java.util.ArrayList;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> {

    Context context;
    ArrayList<File> list;
    String className;

    public FilesAdapter(Context context, ArrayList<File> list, String className) {
        this.context = context;
        this.list = list;
        this.className = className;
    }

    @NonNull
    @Override
    public FilesAdapter.FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false);
        return new FilesAdapter.FilesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesAdapter.FilesViewHolder holder, int position) {

        File file = list.get(position);
        holder.tvTypeName.setText(file.fileName);

        // Download file.
        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Storage reference.
                StorageReference fileRef = FirebaseStorage.getInstance().getReference(file.filePath);

                // Download from URL.
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String url = uri.toString();
                        downloadFile(context, file.fileName, Environment.DIRECTORY_DOWNLOADS, url);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(context, "La descarga falló", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        // Delete file.
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Storage reference.
                StorageReference fileRef = FirebaseStorage.getInstance().getReference(file.filePath);

                // Build alert.
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("¿Seguro de que quieres eliminar este archivo?");
                builder.setMessage("Los archivos eliminados no pueden ser recuperados.");

                // Eliminate.
                builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Delete file.
                        fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(context, "Archivo eliminado con éxito", Toast.LENGTH_SHORT).show();
                                ((FilesActivity)context).setRecyclerView();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "La descarga falló", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });

                // Cancel.
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "Cancelado", Toast.LENGTH_SHORT).show();
                    }
                });

                // Show alert.
                builder.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class FilesViewHolder extends RecyclerView.ViewHolder {

        TextView tvTypeName;
        ImageButton btnDownload, btnDelete;

        public FilesViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTypeName = itemView.findViewById(R.id.tvTypeName);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnDelete = itemView.findViewById(R.id.btnDelete);

        }

    }

    public void downloadFile(Context context, String filename, String destinationDirectory, String url) {

        // Set uri and add to request.
        Uri uri = Uri.parse(url);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        // Set notification and download directory.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(destinationDirectory, filename.replaceAll("[^a-zA-Z0-9]", ""));

        // Start download and show toast.
        downloadManager.enqueue(request);
        Toast.makeText(context, "La descarga ha comenzado", Toast.LENGTH_SHORT).show();

    }

}
