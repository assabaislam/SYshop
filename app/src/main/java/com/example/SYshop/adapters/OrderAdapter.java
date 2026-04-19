package com.example.SYshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.R;
import com.example.SYshop.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onShareOrder(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        String safeOrderId = order.getOrderId() == null ? "" : order.getOrderId();
        String shortOrderId = safeOrderId.length() > 8 ? safeOrderId.substring(0, 8) : safeOrderId;

        holder.orderId.setText(context.getString(R.string.order_number_format, shortOrderId));
        holder.orderTotal.setText(order.getTotalPrice());
        holder.orderStatus.setText(getLocalizedStatus(order.getStatus()));
        holder.orderItems.setText(context.getString(R.string.items_count, order.getItemsCount()));
        holder.viewDetailsText.setText(context.getString(R.string.view_order_details));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        holder.orderDate.setText(sdf.format(new Date(order.getCreatedAt())));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });

        holder.shareOrderBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShareOrder(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private String getLocalizedStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return context.getString(R.string.order_status_placed);
        }

        String normalizedStatus = status.trim().toLowerCase(Locale.ROOT);
        switch (normalizedStatus) {
            case "placed":
                return context.getString(R.string.order_status_placed);
            case "pending":
                return context.getString(R.string.order_status_pending);
            case "confirmed":
                return context.getString(R.string.order_status_confirmed);
            case "shipped":
                return context.getString(R.string.order_status_shipped);
            case "delivered":
                return context.getString(R.string.order_status_delivered);
            case "cancelled":
            case "canceled":
                return context.getString(R.string.order_status_cancelled);
            default:
                return status;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderTotal, orderStatus, orderItems, orderDate, viewDetailsText;
        ImageView shareOrderBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            orderTotal = itemView.findViewById(R.id.orderTotal);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderItems = itemView.findViewById(R.id.orderItems);
            orderDate = itemView.findViewById(R.id.orderDate);
            viewDetailsText = itemView.findViewById(R.id.viewDetailsText);
            shareOrderBtn = itemView.findViewById(R.id.shareOrderBtn);
        }
    }
}
