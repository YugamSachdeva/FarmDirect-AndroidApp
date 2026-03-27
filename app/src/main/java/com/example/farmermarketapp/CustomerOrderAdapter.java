package com.example.farmermarketapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerOrderAdapter extends
        RecyclerView.Adapter<CustomerOrderAdapter.OrderViewHolder> {

    Context context;
    List<CustomerOrderModel> orderList;

    public CustomerOrderAdapter(Context context,
                                List<CustomerOrderModel> orderList) {
        this.context   = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder,
                                 int position) {

        CustomerOrderModel order = orderList.get(position);

        holder.tvName.setText(order.getName());
        holder.tvPrice.setText("₹" + (int) order.getPrice() + "/kg");
        holder.tvQuantity.setText("Qty: " + order.getQuantity() + " kg");
        holder.tvTotal.setText("Total: ₹"
                + (int)(order.getPrice() * order.getQuantity()));

        // Timestamp
        if (order.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a", Locale.getDefault());
            String date = sdf.format(new Date(order.getTimestamp()));
            holder.tvOrderTime.setText("🕐 Ordered: " + date);
        } else {
            holder.tvOrderTime.setText("");
        }

        // Status badge
        String status = order.getStatus() != null
                ? order.getStatus() : "pending";

        switch (status.toLowerCase()) {
            case "accepted":
                holder.tvStatus.setText("✅ ACCEPTED");
                holder.tvStatus.setBackgroundResource(
                        R.drawable.status_delivered_bg);
                break;
            case "delivered":
                holder.tvStatus.setText("🚚 DELIVERED");
                holder.tvStatus.setBackgroundResource(
                        R.drawable.status_delivered_bg);
                break;
            case "rejected":
                holder.tvStatus.setText("❌ REJECTED");
                holder.tvStatus.setBackgroundResource(
                        R.drawable.status_rejected_bg);
                break;
            default:
                holder.tvStatus.setText("🕐 PENDING");
                holder.tvStatus.setBackgroundResource(
                        R.drawable.status_pending_bg);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPrice, tvQuantity,
                tvTotal, tvStatus, tvOrderTime;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName      = itemView.findViewById(R.id.tvOrderName);
            tvPrice     = itemView.findViewById(R.id.tvOrderPrice);
            tvQuantity  = itemView.findViewById(R.id.tvOrderQuantity);
            tvTotal     = itemView.findViewById(R.id.tvOrderTotal);
            tvStatus    = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
        }
    }
}