package com.example.farmermarketapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FarmerOrderAdapter extends
        RecyclerView.Adapter<FarmerOrderAdapter.OrderViewHolder> {

    Context context;
    List<FarmerOrderModel> orderList;
    String tabType;
    OnOrderActionListener listener;
    FirebaseFirestore db;

    public interface OnOrderActionListener {
        void onOrderAction(FarmerOrderModel order, String action);
    }

    public FarmerOrderAdapter(Context context,
                              List<FarmerOrderModel> orderList,
                              String tabType,
                              OnOrderActionListener listener) {
        this.context   = context;
        this.orderList = orderList;
        this.tabType   = tabType;
        this.listener  = listener;
        this.db        = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_farmer_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder,
                                 int position) {

        FarmerOrderModel order = orderList.get(position);

        holder.tvName.setText(order.getName());
        holder.tvPrice.setText("₹" + (int) order.getPrice() + "/kg");
        holder.tvQuantity.setText(
                "Qty: " + order.getQuantity() + " kg");
        holder.tvTotal.setText("Total: ₹"
                + (int)(order.getPrice() * order.getQuantity()));

        // Timestamp
        if (order.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a", Locale.getDefault());
            holder.tvTime.setText("🕐 "
                    + sdf.format(new Date(order.getTimestamp())));
        } else {
            holder.tvTime.setText("");
        }

        // Customer info load karo
        loadCustomerInfo(order, holder);

        // Customer info click — popup
        holder.layoutCustomerInfo.setOnClickListener(v ->
                showCustomerDialog(order));

        // Tab type ke hisaab se buttons
        switch (tabType) {
            case "new":
                holder.tvStatus.setText("🆕 NEW");
                holder.tvStatus.setBackgroundResource(
                        R.drawable.status_pending_bg);
                holder.btnAction1.setVisibility(View.VISIBLE);
                holder.btnAction2.setVisibility(View.VISIBLE);
                holder.btnAction1.setText("✅ Accept");
                holder.btnAction2.setText("❌ Reject");
                holder.btnAction1.setBackgroundTintList(
                        android.content.res.ColorStateList
                                .valueOf(android.graphics.Color
                                        .parseColor("#2E7D32")));
                holder.btnAction2.setBackgroundTintList(
                        android.content.res.ColorStateList
                                .valueOf(android.graphics.Color
                                        .parseColor("#B00020")));
                holder.btnAction1.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onOrderAction(order, "accepted");
                });
                holder.btnAction2.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onOrderAction(order, "rejected");
                });
                break;

            case "accepted":
                holder.tvStatus.setText("📦 ACCEPTED");
                holder.tvStatus.setBackgroundResource(
                        R.drawable.status_delivered_bg);
                holder.btnAction1.setVisibility(View.VISIBLE);
                holder.btnAction2.setVisibility(View.GONE);
                holder.btnAction1.setText("🚚 Mark Delivered");
                holder.btnAction1.setBackgroundTintList(
                        android.content.res.ColorStateList
                                .valueOf(android.graphics.Color
                                        .parseColor("#1565C0")));
                holder.btnAction1.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onOrderAction(order, "delivered");
                });
                break;

            case "history":
                holder.btnAction1.setVisibility(View.GONE);
                holder.btnAction2.setVisibility(View.GONE);
                String status = order.getStatus();
                if ("delivered".equalsIgnoreCase(status)) {
                    holder.tvStatus.setText("✅ DELIVERED");
                    holder.tvStatus.setBackgroundResource(
                            R.drawable.status_delivered_bg);
                } else {
                    holder.tvStatus.setText("❌ REJECTED");
                    holder.tvStatus.setBackgroundResource(
                            R.drawable.status_rejected_bg);
                }
                break;
        }
    }

    // Customer info Firestore se load karo
    private void loadCustomerInfo(FarmerOrderModel order,
                                  OrderViewHolder holder) {

        if (order.getCustomerName() != null
                && !order.getCustomerName().isEmpty()) {
            // Already loaded
            holder.tvCustomerName.setText(
                    order.getCustomerName());
            holder.tvCustomerCity.setText(
                    "📍 " + order.getCustomerCity());
            return;
        }

        if (order.getCustomerId() == null
                || order.getCustomerId().isEmpty()) {
            holder.tvCustomerName.setText("Unknown Customer");
            return;
        }

        db.collection("users")
                .document(order.getCustomerId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name  = doc.getString("name");
                        String city  = doc.getString("city");
                        String state = doc.getString("state");
                        String phone = doc.getString("phone");

                        order.setCustomerName(
                                name != null ? name : "Unknown");
                        order.setCustomerCity(
                                city != null ? city : "");
                        order.setCustomerPhone(
                                phone != null ? phone : "");

                        holder.tvCustomerName.setText(
                                name != null ? name : "Unknown");

                        String location = "";
                        if (city != null) location = city;
                        if (state != null && !state.isEmpty())
                            location += location.isEmpty()
                                    ? state : ", " + state;
                        holder.tvCustomerCity.setText(
                                location.isEmpty()
                                        ? "" : "📍 " + location);
                    }
                });
    }

    // Customer detail popup
    private void showCustomerDialog(FarmerOrderModel order) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_customer_detail, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextView tvInitial  =
                dialogView.findViewById(R.id.tvCustomerInitial);
        TextView tvName     =
                dialogView.findViewById(R.id.tvDialogCustomerName);
        TextView tvPhone    =
                dialogView.findViewById(R.id.tvDialogPhone);
        TextView tvLocation =
                dialogView.findViewById(R.id.tvDialogLocation);
        TextView tvDate     =
                dialogView.findViewById(R.id.tvDialogDate);
        Button btnClose     =
                dialogView.findViewById(R.id.btnCloseDialog);

        // Customer details
        String name = order.getCustomerName() != null
                ? order.getCustomerName() : "Unknown";
        String phone = order.getCustomerPhone() != null
                ? order.getCustomerPhone() : "--";
        String city = order.getCustomerCity() != null
                ? order.getCustomerCity() : "--";

        tvName.setText(name);
        tvPhone.setText(phone);
        tvLocation.setText(city);

        // Initial
        if (!name.isEmpty() && !name.equals("Unknown")) {
            tvInitial.setText(
                    String.valueOf(name.charAt(0)).toUpperCase());
        } else {
            tvInitial.setText("?");
        }

        // Date
        if (order.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a", Locale.getDefault());
            tvDate.setText(
                    sdf.format(new Date(order.getTimestamp())));
        } else {
            tvDate.setText("--");
        }

        // Agar customer info abhi tak load nahi hua
        if (order.getCustomerId() != null
                && !order.getCustomerId().isEmpty()
                && (order.getCustomerName() == null
                || order.getCustomerName().isEmpty())) {

            db.collection("users")
                    .document(order.getCustomerId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String n = doc.getString("name");
                            String p = doc.getString("phone");
                            String c = doc.getString("city");
                            String s = doc.getString("state");

                            tvName.setText(n != null ? n : "--");
                            tvPhone.setText(p != null ? p : "--");

                            String loc = "";
                            if (c != null) loc = c;
                            if (s != null && !s.isEmpty())
                                loc += loc.isEmpty()
                                        ? s : ", " + s;
                            tvLocation.setText(
                                    loc.isEmpty() ? "--" : loc);

                            if (n != null && !n.isEmpty()) {
                                tvInitial.setText(String.valueOf(
                                        n.charAt(0)).toUpperCase());
                            }
                        }
                    });
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPrice, tvQuantity, tvTotal,
                tvStatus, tvTime, tvCustomerName, tvCustomerCity;
        Button btnAction1, btnAction2;
        LinearLayout layoutCustomerInfo;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName           = itemView.findViewById(
                    R.id.tvFarmerOrderName);
            tvPrice          = itemView.findViewById(
                    R.id.tvFarmerOrderPrice);
            tvQuantity       = itemView.findViewById(
                    R.id.tvFarmerOrderQuantity);
            tvTotal          = itemView.findViewById(
                    R.id.tvFarmerOrderTotal);
            tvStatus         = itemView.findViewById(
                    R.id.tvFarmerOrderStatus);
            tvTime           = itemView.findViewById(
                    R.id.tvFarmerOrderTime);
            tvCustomerName   = itemView.findViewById(
                    R.id.tvCustomerName);
            tvCustomerCity   = itemView.findViewById(
                    R.id.tvCustomerCity);
            btnAction1       = itemView.findViewById(R.id.btnAction1);
            btnAction2       = itemView.findViewById(R.id.btnAction2);
            layoutCustomerInfo = itemView.findViewById(
                    R.id.layoutCustomerInfo);
        }
    }
}