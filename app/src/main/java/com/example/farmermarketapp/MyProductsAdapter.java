package com.example.farmermarketapp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyProductsAdapter extends
        RecyclerView.Adapter<MyProductsAdapter.ProductViewHolder> {

    Context context;
    List<Product> productList;
    FirebaseFirestore db;

    public MyProductsAdapter(Context context, List<Product> productList) {
        this.context     = context;
        this.productList = productList;
        this.db          = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_my_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvCategory.setText(product.getCategory());
        holder.tvPrice.setText("₹" + (int) product.getPrice() + "/kg");
        holder.tvQuantity.setText("Available: " + product.getQuantity() + " kg");

        holder.btnEdit.setOnClickListener(v ->
                showEditDialog(product, position));

        holder.btnDelete.setOnClickListener(v ->
                showDeleteDialog(product, position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private void showEditDialog(Product product, int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_edit_product, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        EditText etName     = dialogView.findViewById(R.id.etEditName);
        EditText etPrice    = dialogView.findViewById(R.id.etEditPrice);
        EditText etQuantity = dialogView.findViewById(R.id.etEditQuantity);
        Button btnSave      = dialogView.findViewById(R.id.btnSaveEdit);

        etName.setText(product.getName());
        etPrice.setText(String.valueOf((int) product.getPrice()));
        etQuantity.setText(String.valueOf(product.getQuantity()));

        btnSave.setOnClickListener(v -> {

            String newName     = etName.getText().toString().trim();
            String newPrice    = etPrice.getText().toString().trim();
            String newQuantity = etQuantity.getText().toString().trim();

            if (newName.isEmpty() || newPrice.isEmpty() || newQuantity.isEmpty()) {
                Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newName);
            updates.put("price", Double.parseDouble(newPrice));
            updates.put("quantity", Integer.parseInt(newQuantity));

            db.collection("products").document(product.getId())
                    .update(updates)
                    .addOnSuccessListener(unused -> {
                        product.setName(newName);
                        product.setPrice(Double.parseDouble(newPrice));
                        product.setQuantity(Integer.parseInt(newQuantity));
                        notifyItemChanged(position);
                        Toast.makeText(context,
                                "Product updated! ✅", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context,
                                    "Update failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });
    }

    private void showDeleteDialog(Product product, int position) {

        new AlertDialog.Builder(context)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \""
                        + product.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    db.collection("products").document(product.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                productList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, productList.size());
                                Toast.makeText(context,
                                        "Product deleted! 🗑️", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context,
                                            "Delete failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvCategory, tvPrice, tvQuantity;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice    = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnEdit    = itemView.findViewById(R.id.btnEdit);
            btnDelete  = itemView.findViewById(R.id.btnDelete);
        }
    }
}