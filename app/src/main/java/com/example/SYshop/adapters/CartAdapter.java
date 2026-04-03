package com.example.SYshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.SYshop.models.CartItem;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.models.Product;
import com.example.SYshop.R;
import com.example.SYshop.utils.Navigator;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final Context context;
    private final List<CartItem> cartList;
    private final OnCartChangedListener listener;

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    public CartAdapter(Context context, List<CartItem> cartList, OnCartChangedListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int position) {
        CartItem cartItem = cartList.get(position);
        Product product = cartItem.getProduct();

        holder.cartItemName.setText(product.getName());
        holder.cartItemTag.setText(product.getTag());
        holder.cartItemPrice.setText(product.getPrice());
        holder.quantityText.setText(String.valueOf(cartItem.getQuantity()));
        
        Glide.with(context)
                .load(product.getImageRes())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.cartItemImage);

        holder.plusBtn.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                CartManager.increaseQuantity(adapterPosition);
                notifyDataSetChanged();

                if (listener != null) {
                    listener.onCartChanged();
                }
            }
        });

        holder.minusBtn.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                CartManager.decreaseQuantity(adapterPosition);
                notifyDataSetChanged();

                if (listener != null) {
                    listener.onCartChanged();
                }
            }
        });

        holder.itemView.setOnClickListener(v -> Navigator.openProductDetails(context, product));
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cartItemImage;
        TextView cartItemName, cartItemTag, cartItemPrice, quantityText, plusBtn, minusBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cartItemImage = itemView.findViewById(R.id.cartItemImage);
            cartItemName = itemView.findViewById(R.id.cartItemName);
            cartItemTag = itemView.findViewById(R.id.cartItemTag);
            cartItemPrice = itemView.findViewById(R.id.cartItemPrice);
            quantityText = itemView.findViewById(R.id.quantityText);
            plusBtn = itemView.findViewById(R.id.plusBtn);
            minusBtn = itemView.findViewById(R.id.minusBtn);
        }
    }
}