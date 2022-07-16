package com.saltapor.soporti.Models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saltapor.soporti.R;

import java.util.ArrayList;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder> {

    Context context;
    ArrayList<Category> list;

    public CategoriesAdapter(Context context, ArrayList<Category> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CategoriesAdapter.CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.new_category_item, parent, false);
        return new CategoriesAdapter.CategoriesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesAdapter.CategoriesViewHolder holder, int position) {

        Category category = list.get(position);
        holder.categoryName.setText(category.category);
        holder.subcategoryName.setText(category.subcategory);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CategoriesViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName, subcategoryName;

        public CategoriesViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryName = itemView.findViewById(R.id.tvCategoryName);
            subcategoryName = itemView.findViewById(R.id.tvSubcategoryName);

        }

    }

}
