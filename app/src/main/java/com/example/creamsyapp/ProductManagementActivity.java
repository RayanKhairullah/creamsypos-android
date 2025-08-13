package com.example.creamsyapp;

import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity {
    private List<IceCreamProduct> products;
    private ProductManagementAdapter productAdapter;
    private boolean isEditingMode = false;
    private boolean isDeletingMode = false;
    private List<IceCreamProduct> selectedProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

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
                if (isEditingMode) {
                    // Edit produk
                    IceCreamProduct product = products.get(position);
                    Intent intent = new Intent(ProductManagementActivity.this, AddProductActivity.class);

                    // Jika ini mode edit, kirim data produk yang akan diedit
                    intent.putExtra("edit_product", product);
                    startActivityForResult(intent, 1);
                } else if (isDeletingMode) {
                    // Toggle produk yang dipilih untuk dihapus
                    IceCreamProduct product = products.get(position);
                    if (selectedProducts.contains(product)) {
                        selectedProducts.remove(product);
                    } else {
                        selectedProducts.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                }
            }
        });

        // Setup klik lama untuk masuk ke mode edit atau hapus
        productsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showSelectionModeDialog();
                return true;
            }
        });
    }

    private void showSelectionModeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Pilih Mode")
                .setMessage("Apa yang ingin Anda lakukan?")
                .setPositiveButton("Edit Produk", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isEditingMode = true;
                        isDeletingMode = false;
                        selectedProducts.clear();
                        productAdapter.notifyDataSetChanged();
                        Toast.makeText(ProductManagementActivity.this, "Klik produk untuk mengedit", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hapus Produk", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isEditingMode = false;
                        isDeletingMode = true;
                        selectedProducts.clear();
                        productAdapter.notifyDataSetChanged();
                        Toast.makeText(ProductManagementActivity.this, "Pilih produk yang akan dihapus", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Batal", null)
                .show();
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
            if (isEditingMode || isDeletingMode) {
                isEditingMode = false;
                isDeletingMode = false;
                selectedProducts.clear();
                productAdapter.notifyDataSetChanged();
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
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteSelectedProducts() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus " + selectedProducts.size() + " produk terpilih?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        products.removeAll(selectedProducts);
                        selectedProducts.clear();
                        isDeletingMode = false;
                        productAdapter.notifyDataSetChanged();

                        // Kembalikan daftar produk yang diperbarui ke MainActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putParcelableArrayListExtra("updated_products", new ArrayList<>(products));
                        setResult(RESULT_OK, resultIntent);

                        Toast.makeText(ProductManagementActivity.this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show();
                    }
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
                        products.add(newProduct);
                        productAdapter.notifyDataSetChanged();

                        // Kembalikan daftar produk yang diperbarui ke MainActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putParcelableArrayListExtra("updated_products", new ArrayList<>(products));
                        setResult(RESULT_OK, resultIntent);

                        Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (data.hasExtra("edit_product")) {
                    // Update produk yang diedit
                    IceCreamProduct editedProduct = data.getParcelableExtra("edit_product");
                    if (editedProduct != null) {
                        for (int i = 0; i < products.size(); i++) {
                            if (products.get(i).getId().equals(editedProduct.getId())) {
                                products.set(i, editedProduct);
                                break;
                            }
                        }
                        productAdapter.notifyDataSetChanged();

                        // Kembalikan daftar produk yang diperbarui ke MainActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putParcelableArrayListExtra("updated_products", new ArrayList<>(products));
                        setResult(RESULT_OK, resultIntent);

                        Toast.makeText(this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isEditingMode || isDeletingMode) {
            isEditingMode = false;
            isDeletingMode = false;
            selectedProducts.clear();
            productAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }
}