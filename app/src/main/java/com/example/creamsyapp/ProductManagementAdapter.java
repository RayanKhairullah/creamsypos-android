package com.example.creamsyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementAdapter extends ArrayAdapter<IceCreamProduct> {
    private Context context;
    private List<IceCreamProduct> products;
    private boolean isEditingMode;
    private boolean isDeletingMode;
    private List<IceCreamProduct> selectedProducts;

    public ProductManagementAdapter(Context context, List<IceCreamProduct> products) {
        super(context, 0, products);
        this.context = context;
        this.products = products;
        this.isEditingMode = false;
        this.isDeletingMode = false;
        this.selectedProducts = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.product_management_item, parent, false);
        }

        IceCreamProduct product = products.get(position);

        TextView tvName = convertView.findViewById(R.id.tv_product_name);
        TextView tvPrice = convertView.findViewById(R.id.tv_product_price);
        TextView tvStock = convertView.findViewById(R.id.tv_product_stock);
        TextView tvSelect = convertView.findViewById(R.id.tv_select);

        tvName.setText(product.getName());
        tvPrice.setText(String.format("Rp %.0f", product.getPrice()));
        tvStock.setText(String.format("Stok: %d", product.getStock()));

        // Tampilkan indikator untuk mode hapus
        tvSelect.setVisibility(isDeletingMode ? View.VISIBLE : View.GONE);
        tvSelect.setText(selectedProducts.contains(product) ? "✓" : "○");

        return convertView;
    }

    public void setEditingMode(boolean editingMode) {
        this.isEditingMode = editingMode;
        notifyDataSetChanged();
    }

    public void setDeletingMode(boolean deletingMode) {
        this.isDeletingMode = deletingMode;
        notifyDataSetChanged();
    }

    public void toggleSelection(IceCreamProduct product) {
        if (selectedProducts.contains(product)) {
            selectedProducts.remove(product);
        } else {
            selectedProducts.add(product);
        }
        notifyDataSetChanged();
    }

    public List<IceCreamProduct> getSelectedProducts() {
        return selectedProducts;
    }
}