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
import com.example.SYshop.models.OrderItem;
import com.example.SYshop.utils.ProductImageLoader;

import java.util.List;
import java.util.Locale;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderItem> orderItems;
    private final OnOrderItemClickListener listener;

    public interface OnOrderItemClickListener {
        void onOrderItemClick(OrderItem item);
    }

    public OrderItemsAdapter(Context context, List<OrderItem> orderItems, OnOrderItemClickListener listener) {
        this.context = context;
        this.orderItems = orderItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);

        holder.itemName.setText(item.getName());
        String localizedTag = getLocalizedTag(item.getTag());
        holder.itemTag.setText(localizedTag);
        holder.itemTag.setVisibility(localizedTag.isEmpty() ? View.GONE : View.VISIBLE);
        holder.itemPrice.setText(item.getPrice());
        holder.itemQuantity.setText(context.getString(R.string.quantity_label, item.getQuantity()));

        ProductImageLoader.loadCenterCrop(
                holder.itemImage,
                item.getImageUrl(),
                item.getPreferredLocalImageRes()
        );

        holder.viewProductText.setText(context.getString(R.string.view_product));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    private String getLocalizedTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return "";
        }

        String normalizedTag = tag.trim().toLowerCase(Locale.ROOT);
        switch (normalizedTag) {
            case "promo":
                return context.getString(R.string.product_tag_promo);
            case "new":
                return context.getString(R.string.product_tag_new);
            case "best seller":
                return context.getString(R.string.product_tag_best_seller);
            case "sale":
                return context.getString(R.string.product_tag_sale);
            case "top rated":
                return context.getString(R.string.product_tag_top_rated);
            default:
                return tag;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, itemTag, itemPrice, itemQuantity, viewProductText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemTag = itemView.findViewById(R.id.itemTag);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            viewProductText = itemView.findViewById(R.id.viewProductText);
        }
    }
}
