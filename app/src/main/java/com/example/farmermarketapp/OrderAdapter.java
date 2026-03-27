package com.example.farmermarketapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    Context context;
    List<Map<String, Object>> orderList;

    public OrderAdapter(Context context, List<Map<String, Object>> orderList) {
        this.context   = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

        Map<String, Object> order = orderList.get(position);

        String name   = order.get("name") != null
                ? order.get("name").toString() : "";
        String status = order.get("status") != null
                ? order.get("status").toString() : "pending";

        double price = 0;
        Object pObj = order.get("price");
        if (pObj instanceof Double) price = (Double) pObj;
        else if (pObj instanceof Long) price = ((Long) pObj).doubleValue();

        int quantity = 0;
        Object qObj = order.get("quantity");
        if (qObj instanceof Long) quantity = ((Long) qObj).intValue();
        else if (qObj instanceof String) {
            try { quantity = Integer.parseInt((String) qObj); }
            catch (NumberFormatException e) { quantity = 0; }
        }

        holder.tvName.setText(name);
        holder.tvPrice.setText("₹" + (int) price + "/kg");
        holder.tvQuantity.setText("Qty: " + quantity + " kg");
        holder.tvTotal.setText("Total: ₹" + (int)(price * quantity));
        holder.tvStatus.setText(status.toUpperCase());

        if ("delivered".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_delivered_bg);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_pending_bg);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPrice, tvQuantity, tvTotal, tvStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvOrderName);
            tvPrice    = itemView.findViewById(R.id.tvOrderPrice);
            tvQuantity = itemView.findViewById(R.id.tvOrderQuantity);
            tvTotal    = itemView.findViewById(R.id.tvOrderTotal);
            tvStatus   = itemView.findViewById(R.id.tvOrderStatus);
        }
    }
}