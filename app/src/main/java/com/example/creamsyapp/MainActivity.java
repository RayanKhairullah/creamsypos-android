package com.example.creamsyapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private ArrayAdapter<CartLine> cartAdapter;
    private final List<CartLine> cartLines = new ArrayList<>();
    private TextView totalTextView;
    private double total = 0;

    // Konstanta untuk request code
    private static final int PRODUCT_MANAGEMENT_REQUEST_CODE = 3;

    private SupabaseHelper supabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inisialisasi sesi (auto-login dengan refresh token bila ada)
        supabaseHelper = SupabaseHelper.getInstance();
        supabaseHelper.init(getApplicationContext());
        supabaseHelper.initializeSession(this, new SupabaseHelper.SessionInitCallback() {
            @Override
            public void onReady() { runOnUiThread(() -> setupUI()); }
            @Override
            public void onRequireLogin() {
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    private void setupUI() {
        setContentView(R.layout.activity_main);

        // Setup cart ListView
        ListView cartListView = findViewById(R.id.cart_list_view);
        totalTextView = findViewById(R.id.total_text_view);

        cartAdapter = new ArrayAdapter<CartLine>(this, R.layout.cart_item, cartLines) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.cart_item, parent, false);
                }
                TextView itemText = convertView.findViewById(R.id.cart_item_text);
                TextView tvQty = convertView.findViewById(R.id.tv_quantity);
                View btnPlus = convertView.findViewById(R.id.btn_plus);
                View btnMinus = convertView.findViewById(R.id.btn_minus);

                CartLine line = cartLines.get(position);
                IceCreamProduct product = line.product;
                int qty = line.quantity;

                itemText.setText(String.format(Locale.getDefault(), "%s - Rp %.0f", product.getName(), product.getPrice()));
                tvQty.setText(String.valueOf(qty));

                btnPlus.setOnClickListener(v -> {
                    // Add one unit if stock allows
                    int inCart = countInCart(product.getId());
                    if (product.getStock() > inCart) {
                        cart.add(product);
                        total += product.getPrice();
                        totalTextView.setText(String.format(Locale.getDefault(), "Total: Rp %.0f", total));
                        rebuildCartLines();
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "Stok " + product.getName() + " habis!", Toast.LENGTH_SHORT).show();
                    }
                });

                btnMinus.setOnClickListener(v -> {
                    // Remove one unit
                    boolean removed = removeOneFromCart(product.getId());
                    if (removed) {
                        total -= product.getPrice();
                        if (total < 0) total = 0;
                        totalTextView.setText(String.format(Locale.getDefault(), "Total: Rp %.0f", total));
                        rebuildCartLines();
                        notifyDataSetChanged();
                    }
                });
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

        // Muat data dari Supabase
        loadDataFromSupabase();
    }

    // Helper to count occurrences of a product id in cart
    private int countInCart(String productId) {
        int c = 0;
        for (IceCreamProduct p : cart) {
            if (p.getId().equals(productId)) c++;
        }
        return c;
    }

    // Helper to remove one occurrence of a product id from cart
    private boolean removeOneFromCart(String productId) {
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getId().equals(productId)) {
                cart.remove(i);
                return true;
            }
        }
        return false;
    }

    // Build aggregated cart lines for display
    private void rebuildCartLines() {
        Map<String, CartLine> map = new HashMap<>();
        for (IceCreamProduct p : cart) {
            CartLine line = map.get(p.getId());
            if (line == null) {
                line = new CartLine(p, 0);
                map.put(p.getId(), line);
            }
            line.quantity += 1;
        }
        cartLines.clear();
        cartLines.addAll(map.values());
    }

    // Aggregated cart line
    private static class CartLine {
        final IceCreamProduct product;
        int quantity;
        CartLine(IceCreamProduct product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        @Override public String toString() { return product.getName(); }
    }

    public void loadDataFromSupabase() {
        // Muat produk dari Supabase
        supabaseHelper.loadProducts(new SupabaseHelper.ProductsCallback() {
            @Override
            public void onSuccess(List<IceCreamProduct> productsList) {
                runOnUiThread(() -> {
                    Log.d("MainActivity", "Products loaded: " + productsList.size());

                    products.clear();
                    products.addAll(productsList);

                    // Tambahkan log untuk memeriksa produk
                    for (IceCreamProduct product : products) {
                        Log.d("MainActivity", "Product: " + product.getName() +
                                ", ID: " + product.getId() +
                                ", Price: " + product.getPrice() +
                                ", Stock: " + product.getStock() +
                                ", ImageResId: " + product.getImageResId());
                    }

                    // Perbarui tampilan produk
                    RecyclerView productsRecyclerView = findViewById(R.id.products_recycler_view);
                    if (productsRecyclerView != null) {
                        ProductAdapter adapter = (ProductAdapter) productsRecyclerView.getAdapter();
                        if (adapter != null) {
                            Log.d("MainActivity", "Notifying adapter with " + products.size() + " products");
                            adapter.notifyDataSetChanged();

                            // Tampilkan pesan jika tidak ada produk
                            if (productsList.isEmpty()) {
                                Toast.makeText(MainActivity.this, "Tidak ada produk ditemukan",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("MainActivity", "Adapter is null");
                        }
                    } else {
                        Log.e("MainActivity", "RecyclerView is null");
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e("MainActivity", "Error loading products: " + error);
                    Toast.makeText(MainActivity.this, "Gagal memuat produk: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });

        // Muat riwayat transaksi dari Supabase
        supabaseHelper.loadTransactions(new SupabaseHelper.TransactionsCallback() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                runOnUiThread(() -> {
                    transactionHistory.clear();
                    transactionHistory.addAll(transactions);

                    Log.d("MainActivity", "Transactions loaded: " + transactions.size());
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e("MainActivity", "Error loading transactions: " + error);
                    Toast.makeText(MainActivity.this, "Gagal memuat riwayat: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void addToCart(IceCreamProduct product) {
        // Cek stok yang tersedia (stok - jumlah di keranjang)
        int countInCart = countInCart(product.getId());

        if (product.getStock() > countInCart) {
            cart.add(product);
            total += product.getPrice();
            totalTextView.setText(String.format(Locale.getDefault(), "Total: Rp %.0f", total));
            rebuildCartLines();
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
                    // Update produk di Supabase
                    supabaseHelper.updateProduct(product, new SupabaseHelper.DatabaseCallback() {
                        @Override
                        public void onSuccess(String id) {
                            // Tidak perlu melakukan apa-apa di sini
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Gagal memperbarui stok: " + error,
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            }

            // Simpan transaksi
            String transactionId = UUID.randomUUID().toString();
            Transaction transaction = new Transaction(
                    transactionId,
                    new ArrayList<>(cart),
                    total,
                    new Date());

            // Simpan transaksi di Supabase
            supabaseHelper.addTransaction(transaction, new SupabaseHelper.DatabaseCallback() {
                @Override
                public void onSuccess(String id) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Gagal menyimpan transaksi: " + error,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });

            // Bersihkan keranjang
            cart.clear();
            total = 0;
            totalTextView.setText("Total: Rp 0");
            rebuildCartLines();
            cartAdapter.notifyDataSetChanged();

            // Perbarui tampilan produk
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
        else if (id == R.id.action_logout) {
            supabaseHelper.signOut(new SupabaseHelper.AuthCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Gagal logout: " + error,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PRODUCT_MANAGEMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            // MUAT ULANG DATA DI SINI
            loadDataFromSupabase();

            Toast.makeText(this, "Daftar produk berhasil diperbarui",
                    Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == 1 && resultCode == RESULT_OK) {
            // Riwayat transaksi telah dihapus, perbarui data
            loadDataFromSupabase();
        }
    }
}