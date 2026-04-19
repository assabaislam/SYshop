package com.example.SYshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.SYshop.models.CartItem;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.models.Product;
import com.example.SYshop.database.CartSyncRepository;
import com.example.SYshop.R;
import com.example.SYshop.utils.Navigator;
import com.example.SYshop.utils.ProductImageLoader;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final Context context;
    private final List<CartItem> cartList;
    private final OnCartChangedListener listener;
    private final CartSyncRepository cartSyncRepository;

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    public CartAdapter(Context context, List<CartItem> cartList, OnCartChangedListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
        this.cartSyncRepository = new CartSyncRepository(context);
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
        
        ProductImageLoader.loadCenterCrop(
                holder.cartItemImage,
                product.getImageUrl(),
                product.getPreferredLocalImageRes()
        );

        holder.plusBtn.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                CartManager.increaseQuantity(adapterPosition);
                CartItem item = cartList.get(adapterPosition);
                cartSyncRepository.updateQuantity(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        null
                );
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onCartChanged();
                }
            }
        });

        holder.minusBtn.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                CartItem item = cartList.get(adapterPosition);
                CartManager.decreaseQuantity(adapterPosition);
                if (item.getQuantity() <= 1) {
                    cartSyncRepository.removeItem(item.getProduct().getId(), null);
                } else {
                    cartSyncRepository.updateQuantity(
                            item.getProduct().getId(),
                            item.getQuantity(),
                            null
                    );
                }
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
