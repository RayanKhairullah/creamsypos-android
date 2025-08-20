package com.example.creamsyapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.bumptech.glide.Glide;
import com.example.creamsyapp.product.IceCreamProduct;
import com.example.creamsyapp.R;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<IceCreamProduct> products;
    private OnProductAddListener listener;

    public interface OnProductAddListener {
        void onAddProduct(IceCreamProduct product);
    }

    public ProductAdapter(List<IceCreamProduct> products, OnProductAddListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        IceCreamProduct product = products.get(position);

        // Pastikan produk tidak null
        if (product != null) {
            holder.tvName.setText(product.getName());
            holder.tvPrice.setText(String.format("Rp %.0f", product.getPrice()));
            holder.tvStock.setText(String.format("Stok: %d", product.getStock()));

            // Load image from URL if available, else fallback to resource/default
            String url = product.getImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .placeholder(R.drawable.ic_default_product)
                        .error(R.drawable.ic_default_product)
                        .into(holder.ivProduct);
            } else {
                try {
                    if (product.getImageResId() > 0) {
                        holder.ivProduct.setImageResource(product.getImageResId());
                    } else {
                        holder.ivProduct.setImageResource(R.drawable.ic_default_product);
                    }
                } catch (Exception e) {
                    holder.ivProduct.setImageResource(R.drawable.ic_default_product);
                }
            }

            holder.btnAdd.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddProduct(product);
                }
            });
        } else {
            // Jika produk null, tampilkan pesan error
            holder.tvName.setText("Produk tidak ditemukan");
            holder.tvPrice.setText("");
            holder.tvStock.setText("");
            holder.ivProduct.setImageResource(R.drawable.ic_default_product);
        }
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStock;
        Button btnAdd;
        ImageView ivProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvStock = itemView.findViewById(R.id.tv_product_stock);
            btnAdd = itemView.findViewById(R.id.btn_add_to_cart);
            ivProduct = itemView.findViewById(R.id.iv_product);
        }
    }
}