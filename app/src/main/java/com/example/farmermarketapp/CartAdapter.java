package com.example.farmermarketapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    Context context;
    List<CartItem> cartList;
    OnRemoveClickListener listener;
    OnQuantityChangeListener quantityListener;
    FirebaseFirestore db;

    public interface OnRemoveClickListener {
        void onRemove(String docId);
    }

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    public CartAdapter(Context context, List<CartItem> cartList,
                       OnRemoveClickListener listener,
                       OnQuantityChangeListener quantityListener) {
        this.context          = context;
        this.cartList         = cartList;
        this.listener         = listener;
        this.quantityListener = quantityListener;
        this.db               = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {

        CartItem item = cartList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText("₹" + (int) item.getPrice() + "/kg");
        holder.tvQty.setText(String.valueOf(item.getQuantity()));
        updateSubtotal(holder, item);

        // Plus button
        holder.btnPlus.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            item.setQuantity(newQty);
            holder.tvQty.setText(String.valueOf(newQty));
            updateSubtotal(holder, item);
            updateFirestore(item);
            if (quantityListener != null) quantityListener.onQuantityChanged();
        });

        // Minus button
        holder.btnMinus.setOnClickListener(v -> {
            int currentQty = item.getQuantity();
            if (currentQty > 1) {
                int newQty = currentQty - 1;
                item.setQuantity(newQty);
                holder.tvQty.setText(String.valueOf(newQty));
                updateSubtotal(holder, item);
                updateFirestore(item);
                if (quantityListener != null) quantityListener.onQuantityChanged();
            }
        });

        // Remove button
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(item.getDocId());
            }
        });
    }

    private void updateSubtotal(CartViewHolder holder, CartItem item) {
        int subtotal = (int)(item.getPrice() * item.getQuantity());
        holder.tvSubtotal.setText("Subtotal: ₹" + subtotal);
    }

    private void updateFirestore(CartItem item) {
        Map<String, Object> update = new HashMap<>();
        update.put("quantity", item.getQuantity());
        db.collection("cart").document(item.getDocId())
                .update(update);
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPrice, tvQty, tvSubtotal, btnPlus, btnMinus;
        Button btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvCartName);
            tvPrice    = itemView.findViewById(R.id.tvCartPrice);
            tvQty      = itemView.findViewById(R.id.tvCartQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvCartSubtotal);
            btnPlus    = itemView.findViewById(R.id.btnPlus);
            btnMinus   = itemView.findViewById(R.id.btnMinus);
            btnRemove  = itemView.findViewById(R.id.btnRemoveCart);
        }
    }
}