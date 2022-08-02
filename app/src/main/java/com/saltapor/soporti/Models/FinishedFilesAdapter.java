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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.saltapor.soporti.R;

import java.util.ArrayList;

public class FinishedFilesAdapter extends RecyclerView.Adapter<FinishedFilesAdapter.FinishedFilesViewHolder> {

    Context context;
    ArrayList<File> list;
    String className;

    public FinishedFilesAdapter(Context context, ArrayList<File> list, String className) {
        this.context = context;
        this.list = list;
        this.className = className;
    }

    @NonNull
    @Override
    public FinishedFilesAdapter.FinishedFilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.finished_file_item, parent, false);
        return new FinishedFilesAdapter.FinishedFilesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FinishedFilesAdapter.FinishedFilesViewHolder holder, int position) {

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

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class FinishedFilesViewHolder extends RecyclerView.ViewHolder {

        TextView tvTypeName;
        ImageButton btnDownload;

        public FinishedFilesViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTypeName = itemView.findViewById(R.id.tvTypeName);
            btnDownload = itemView.findViewById(R.id.btnDownload);

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
