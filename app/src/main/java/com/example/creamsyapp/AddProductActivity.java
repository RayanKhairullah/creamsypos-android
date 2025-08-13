package com.example.creamsyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {
    private EditText etProductName, etProductPrice, etProductStock;
    private Button btnSave;
    private boolean isEditMode = false;
    private IceCreamProduct editProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Inisialisasi view
        etProductName = findViewById(R.id.et_product_name);
        etProductPrice = findViewById(R.id.et_product_price);
        etProductStock = findViewById(R.id.et_product_stock);
        btnSave = findViewById(R.id.btn_save_product);

        // Periksa apakah ini mode edit
        if (getIntent().hasExtra("edit_product")) {
            isEditMode = true;
            editProduct = getIntent().getParcelableExtra("edit_product");

            // Isi form dengan data produk yang akan diedit
            etProductName.setText(editProduct.getName());
            etProductPrice.setText(String.valueOf(editProduct.getPrice()));
            etProductStock.setText(String.valueOf(editProduct.getStock()));
            btnSave.setText("Perbarui Produk");
        }
    }

    public void saveProduct(View view) {
        String name = etProductName.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String stockStr = etProductStock.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(name)) {
            etProductName.setError("Nama produk tidak boleh kosong");
            return;
        }

        if (TextUtils.isEmpty(priceStr) || !priceStr.matches("\\d+(\\.\\d+)?")) {
            etProductPrice.setError("Harga harus berupa angka");
            return;
        }

        if (TextUtils.isEmpty(stockStr) || !stockStr.matches("\\d+")) {
            etProductStock.setError("Stok harus berupa angka");
            return;
        }

        double price = Double.parseDouble(priceStr);
        int stock = Integer.parseInt(stockStr);

        // Buat objek produk
        IceCreamProduct product;
        if (isEditMode) {
            // Update produk yang sudah ada
            product = new IceCreamProduct(
                    editProduct.getId(), name, price, stock);
        } else {
            // Buat produk baru
            product = new IceCreamProduct(
                    UUID.randomUUID().toString(), name, price, stock);
        }

        // Kembalikan produk ke activity pemanggil
        Intent resultIntent = new Intent();
        if (isEditMode) {
            resultIntent.putExtra("edit_product", product);
        } else {
            resultIntent.putExtra("new_product", product);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}