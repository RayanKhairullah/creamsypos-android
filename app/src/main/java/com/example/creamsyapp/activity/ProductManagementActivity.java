package com.example.creamsyapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.creamsyapp.product.IceCreamProduct;
import com.example.creamsyapp.R;
import com.example.creamsyapp.supabase.SupabaseHelper;
import com.example.creamsyapp.adapter.ProductManagementAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity {
    private List<IceCreamProduct> products;
    private ProductManagementAdapter productAdapter;
    private boolean isDeletingMode = false;
    private List<IceCreamProduct> selectedProducts = new ArrayList<>();
    private SupabaseHelper supabaseHelper;

    // Konstanta untuk request code
    public static final int PRODUCT_ADDED_REQUEST_CODE = 1001;
    public static final int PRODUCT_UPDATED_REQUEST_CODE = 1002;
    public static final int PRODUCT_DELETED_REQUEST_CODE = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        // Inisialisasi SupabaseHelper
        supabaseHelper = SupabaseHelper.getInstance();

        // Ambil daftar produk dari intent
        products = getIntent().getParcelableArrayListExtra("products");
        if (products == null) {
            products = new ArrayList<>();
        }

        // Setup ListView untuk daftar produk
        ListView productsListView = findViewById(R.id.products_list_view);
        productAdapter = new ProductManagementAdapter(this, products);
        productsListView.setAdapter(productAdapter);

        // Setup klik item
        productsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isDeletingMode) {
                    // Toggle produk yang dipilih untuk dihapus
                    IceCreamProduct product = products.get(position);
                    if (selectedProducts.contains(product)) {
                        selectedProducts.remove(product);
                    } else {
                        selectedProducts.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                } else {
                    // Edit produk
                    IceCreamProduct product = products.get(position);
                    Intent intent = new Intent(ProductManagementActivity.this, AddProductActivity.class);

                    // Jika ini mode edit, kirim data produk yang akan diedit
                    intent.putExtra("edit_product", product);
                    startActivityForResult(intent, 1);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_management, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_done) {
            if (isDeletingMode) {
                isDeletingMode = false;
                selectedProducts.clear();
                productAdapter.setDeletingMode(false);
                productAdapter.notifyDataSetChanged();
                return true;
            } else {
                // Selesai dari layar manajemen produk
                finish();
                return true;
            }
        }
        else if (id == R.id.action_add) {
            Intent intent = new Intent(this, AddProductActivity.class);
            startActivityForResult(intent, 1);
            return true;
        }
        else if (id == R.id.action_delete_selected) {
            if (isDeletingMode && !selectedProducts.isEmpty()) {
                deleteSelectedProducts();
                return true;
            } else if (!isDeletingMode) {
                isDeletingMode = true;
                productAdapter.setDeletingMode(true);
                productAdapter.notifyDataSetChanged();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteSelectedProducts() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus " + selectedProducts.size() + " produk terpilih?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    for (IceCreamProduct product : selectedProducts) {
                        // Hapus dari Supabase
                        supabaseHelper.deleteProduct(product.getId(), new SupabaseHelper.DatabaseCallback() {
                            @Override
                            public void onSuccess(String id) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ProductManagementActivity.this,
                                            "Produk berhasil dihapus", Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ProductManagementActivity.this,
                                            "Gagal menghapus produk: " + error, Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }

                    // Beri tahu MainActivity untuk memperbarui daftar produk
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("product_deleted", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.hasExtra("new_product")) {
                    // Tambah produk baru
                    IceCreamProduct newProduct = data.getParcelableExtra("new_product");
                    if (newProduct != null) {
                        // Simpan ke Supabase
                        supabaseHelper.addProduct(newProduct, new SupabaseHelper.DatabaseCallback() {
                            @Override
                            public void onSuccess(String id) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ProductManagementActivity.this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();

                                    // Beri tahu MainActivity untuk memperbarui daftar produk
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("product_added", true);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ProductManagementActivity.this, "Gagal menambahkan produk: " + error,
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }
                }
                else if (data.hasExtra("edit_product")) {
                    // Update produk yang diedit
                    IceCreamProduct editedProduct = data.getParcelableExtra("edit_product");
                    if (editedProduct != null) {
                        // Update di Supabase
                        supabaseHelper.updateProduct(editedProduct, new SupabaseHelper.DatabaseCallback() {
                            @Override
                            public void onSuccess(String id) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ProductManagementActivity.this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show();

                                    // Beri tahu MainActivity untuk memperbarui daftar produk
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("product_updated", true);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ProductManagementActivity.this, "Gagal memperbarui produk: " + error,
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isDeletingMode) {
            isDeletingMode = false;
            selectedProducts.clear();
            productAdapter.setDeletingMode(false);
            productAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }
}