package com.example.farmermarketapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProductAdapter extends
        RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    Context context;
    List<Product> productList;
    OnProductClickListener listener;
    FirebaseFirestore db;

    public interface OnProductClickListener {
        void onAddToCart(Product product);
    }

    public ProductAdapter(Context context,
                          List<Product> productList,
                          OnProductClickListener listener) {
        this.context     = context;
        this.productList = productList;
        this.listener    = listener;
        this.db          = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder, int position) {

        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvCategory.setText(product.getCategory());
        holder.tvPrice.setText("₹" + (int) product.getPrice() + "/kg");
        holder.tvQuantity.setText(
                "Available: " + product.getQuantity() + " kg");

        // Farmer info load karo
        if (product.getFarmerId() != null
                && !product.getFarmerId().isEmpty()) {
            db.collection("users")
                    .document(product.getFarmerId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            String city = doc.getString("city");
                            if (name != null && city != null) {
                                holder.tvFarmerInfo.setText(
                                        "👨‍🌾 " + name + " • 📍 " + city);
                            } else if (name != null) {
                                holder.tvFarmerInfo.setText(
                                        "👨‍🌾 " + name);
                            }
                        }
                    });
        }

        // Edit aur Delete buttons hide karo
        holder.btnEdit.setVisibility(View.GONE);
        holder.btnDelete.setVisibility(View.GONE);

        // Add to Cart button show karo
        holder.btnAddToCart.setVisibility(View.VISIBLE);

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(product);
            }
        });

        // Item click — Detail popup
        holder.itemView.setOnClickListener(v ->
                showDetailDialog(product));
    }

    private void showDetailDialog(Product product) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_product_detail, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextView tvName     = dialogView.findViewById(R.id.tvDetailName);
        TextView tvCategory = dialogView.findViewById(R.id.tvDetailCategory);
        TextView tvPrice    = dialogView.findViewById(R.id.tvDetailPrice);
        TextView tvQty      = dialogView.findViewById(R.id.tvDetailQuantity);
        TextView tvDesc     = dialogView.findViewById(R.id.tvDetailDesc);
        TextView tvFarmer   = dialogView.findViewById(R.id.tvDetailFarmer);
        Button btnAddCart   = dialogView.findViewById(R.id.btnDetailAddCart);

        tvName.setText(product.getName());
        tvCategory.setText(product.getCategory());
        tvPrice.setText("₹" + (int) product.getPrice() + "/kg");
        tvQty.setText(product.getQuantity() + " kg");
        tvDesc.setText(product.getDescription() != null
                && !product.getDescription().isEmpty()
                ? product.getDescription()
                : "No description available");

        // Farmer info load karo
        if (product.getFarmerId() != null
                && !product.getFarmerId().isEmpty()) {
            db.collection("users")
                    .document(product.getFarmerId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            String city = doc.getString("city");
                            String state = doc.getString("state");
                            if (name != null) {
                                String info = name;
                                if (city != null) info += ", " + city;
                                if (state != null) info += ", " + state;
                                tvFarmer.setText(info);
                            }
                        }
                    });
        }

        btnAddCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(product);
            }
            dialog.dismiss();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvCategory, tvPrice,
                tvQuantity, tvFarmerInfo;
        Button btnEdit, btnDelete, btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName       = itemView.findViewById(R.id.tvProductName);
            tvCategory   = itemView.findViewById(R.id.tvCategory);
            tvPrice      = itemView.findViewById(R.id.tvPrice);
            tvQuantity   = itemView.findViewById(R.id.tvQuantity);
            tvFarmerInfo = itemView.findViewById(R.id.tvFarmerInfo);
            btnEdit      = itemView.findViewById(R.id.btnEdit);
            btnDelete    = itemView.findViewById(R.id.btnDelete);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}