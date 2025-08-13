package com.example.creamsyapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(String.format("Rp %.0f", product.getPrice()));
        holder.tvStock.setText(String.format("Stok: %d", product.getStock()));

        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddProduct(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStock;
        Button btnAdd;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvStock = itemView.findViewById(R.id.tv_product_stock);
            btnAdd = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}