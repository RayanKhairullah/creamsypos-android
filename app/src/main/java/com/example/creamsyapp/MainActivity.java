package com.example.creamsyapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private List<IceCreamProduct> products = new ArrayList<>();
    private List<IceCreamProduct> cart = new ArrayList<>();
    private List<Transaction> transactionHistory = new ArrayList<>();
    private ArrayAdapter<IceCreamProduct> cartAdapter;
    private TextView totalTextView;
    private double total = 0;

    // Konstanta untuk request code
    private static final int ADD_PRODUCT_REQUEST_CODE = 2;
    private static final int PRODUCT_MANAGEMENT_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi produk default
        initProducts();

        // Setup cart ListView
        ListView cartListView = findViewById(R.id.cart_list_view);
        totalTextView = findViewById(R.id.total_text_view);

        cartAdapter = new ArrayAdapter<>(this, R.layout.cart_item, cart) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.cart_item, parent, false);
                }
                TextView itemText = convertView.findViewById(R.id.cart_item_text);
                IceCreamProduct product = cart.get(position);
                itemText.setText(String.format("%s - Rp %.0f (Stok: %d)",
                        product.getName(), product.getPrice(), product.getStock()));
                return convertView;
            }
        };
        cartListView.setAdapter(cartAdapter);

        // Setup RecyclerView untuk produk
        RecyclerView productsRecyclerView = findViewById(R.id.products_recycler_view);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        ProductAdapter productAdapter = new ProductAdapter(products, this::addToCart);
        productsRecyclerView.setAdapter(productAdapter);

        // Setup tombol checkout
        Button btnCheckout = findViewById(R.id.btn_checkout);
        btnCheckout.setOnClickListener(v -> checkout());

        // Muat riwayat transaksi dari penyimpanan (simulasi)
        loadTransactionHistory();
    }

    private void initProducts() {
        // Produk default dengan stok dan gambar
        products.add(new IceCreamProduct(
                UUID.randomUUID().toString(), "Vanilla", 5000, 10, R.drawable.ic_vanilla));
        products.add(new IceCreamProduct(
                UUID.randomUUID().toString(), "Chocolate", 6000, 10, R.drawable.ic_chocolate));
        products.add(new IceCreamProduct(
                UUID.randomUUID().toString(), "Strawberry", 5500, 10, R.drawable.ic_strawberry));
    }

    private void loadTransactionHistory() {
        // Dalam aplikasi nyata, ini akan memuat dari database atau penyimpanan
        // Di sini kita hanya membuat beberapa transaksi contoh
        List<IceCreamProduct> sampleItems = new ArrayList<>();
        sampleItems.add(new IceCreamProduct(
                UUID.randomUUID().toString(), "Vanilla", 5000, 10, R.drawable.ic_vanilla));
        sampleItems.add(new IceCreamProduct(
                UUID.randomUUID().toString(), "Chocolate", 6000, 10, R.drawable.ic_chocolate));

        transactionHistory.add(new Transaction(
                UUID.randomUUID().toString(),
                sampleItems,
                11000,
                new Date(System.currentTimeMillis() - 86400000))); // 1 hari yang lalu

        transactionHistory.add(new Transaction(
                UUID.randomUUID().toString(),
                new ArrayList<>(sampleItems.subList(0, 1)),
                5000,
                new Date(System.currentTimeMillis() - 172800000))); // 2 hari yang lalu
    }

    private void addToCart(IceCreamProduct product) {
        // Cek stok yang tersedia (stok - jumlah di keranjang)
        int countInCart = 0;
        for (IceCreamProduct item : cart) {
            if (item.getId().equals(product.getId())) {
                countInCart++;
            }
        }

        if (product.getStock() > countInCart) {
            cart.add(product);
            total += product.getPrice();
            totalTextView.setText(String.format("Total: Rp %.0f", total));
            cartAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Stok " + product.getName() + " habis!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void checkout() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Agregasi keranjang untuk mendapatkan jumlah per produk
        Map<String, Integer> productCount = new HashMap<>();
        for (IceCreamProduct product : cart) {
            productCount.put(product.getId(),
                    productCount.getOrDefault(product.getId(), 0) + 1);
        }

        // Cek stok untuk semua produk
        boolean sufficientStock = true;
        for (Map.Entry<String, Integer> entry : productCount.entrySet()) {
            String productId = entry.getKey();
            int quantity = entry.getValue();
            IceCreamProduct product = findProductById(productId);
            if (product == null || product.getStock() < quantity) {
                sufficientStock = false;
                break;
            }
        }

        if (sufficientStock) {
            // Kurangi stok
            for (Map.Entry<String, Integer> entry : productCount.entrySet()) {
                String productId = entry.getKey();
                int quantity = entry.getValue();
                IceCreamProduct product = findProductById(productId);
                if (product != null) {
                    product.setStock(product.getStock() - quantity);
                }
            }

            // Simpan transaksi
            String transactionId = UUID.randomUUID().toString();
            Transaction transaction = new Transaction(
                    transactionId,
                    new ArrayList<>(cart),
                    total,
                    new Date());
            transactionHistory.add(transaction);

            // Bersihkan keranjang
            cart.clear();
            total = 0;
            totalTextView.setText("Total: Rp 0");
            cartAdapter.notifyDataSetChanged();

            // Perbarui tampilan produk untuk menampilkan stok terbaru
            RecyclerView productsRecyclerView = findViewById(R.id.products_recycler_view);
            ProductAdapter adapter = (ProductAdapter) productsRecyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            Toast.makeText(this, "Transaksi berhasil!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Gagal checkout: stok tidak mencukupi",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private IceCreamProduct findProductById(String id) {
        for (IceCreamProduct product : products) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_product_management) {
            Intent intent = new Intent(MainActivity.this, ProductManagementActivity.class);
            intent.putParcelableArrayListExtra("products", new ArrayList<>(products));
            startActivityForResult(intent, PRODUCT_MANAGEMENT_REQUEST_CODE);
            return true;
        }
        else if (id == R.id.action_history) {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            intent.putParcelableArrayListExtra("transactions",
                    new ArrayList<>(transactionHistory));
            startActivityForResult(intent, 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PRODUCT_MANAGEMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("updated_products")) {
                List<IceCreamProduct> updatedProducts = data.getParcelableArrayListExtra("updated_products");
                if (updatedProducts != null) {
                    products.clear();
                    products.addAll(updatedProducts);

                    // Perbarui tampilan produk
                    RecyclerView productsRecyclerView = findViewById(R.id.products_recycler_view);
                    ProductAdapter adapter = (ProductAdapter) productsRecyclerView.getAdapter();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    Toast.makeText(this, "Daftar produk berhasil diperbarui",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (requestCode == 1 && resultCode == RESULT_OK) {
            // Riwayat transaksi telah dihapus, perbarui data
            loadTransactionHistory();
        }
    }
}