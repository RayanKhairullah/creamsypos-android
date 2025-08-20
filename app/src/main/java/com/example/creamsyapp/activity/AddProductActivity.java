package com.example.creamsyapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.creamsyapp.product.IceCreamProduct;
import com.example.creamsyapp.R;
import com.example.creamsyapp.supabase.SupabaseHelper;

import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {
    private EditText etProductName, etProductPrice, etProductStock;
    private Button btnSave, btnSelectImage;
    private ImageView ivProductPreview;
    private boolean isEditMode = false;
    private IceCreamProduct editProduct;
    private int selectedImageResId = R.drawable.ic_default_product;
    private byte[] selectedImageBytes = null;
    private String uploadedImageUrl = null;
    private String selectedFileName = null;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleImagePicked(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Inisialisasi view
        etProductName = findViewById(R.id.et_product_name);
        etProductPrice = findViewById(R.id.et_product_price);
        etProductStock = findViewById(R.id.et_product_stock);
        btnSave = findViewById(R.id.btn_save_product);
        btnSelectImage = findViewById(R.id.btn_select_image);
        ivProductPreview = findViewById(R.id.iv_product_preview);

        // Periksa apakah ini mode edit
        if (getIntent().hasExtra("edit_product")) {
            isEditMode = true;
            editProduct = getIntent().getParcelableExtra("edit_product");

            // Isi form dengan data produk yang akan diedit
            etProductName.setText(editProduct.getName());
            etProductPrice.setText(String.valueOf(editProduct.getPrice()));
            etProductStock.setText(String.valueOf(editProduct.getStock()));
            btnSave.setText("Perbarui Produk");
            selectedImageResId = editProduct.getImageResId();
            ivProductPreview.setImageResource(selectedImageResId);
        }

        // Setup tombol pilih gambar (Gallery)
        btnSelectImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void handleImagePicked(Uri uri) {
        try {
            // Load and compress to JPEG to keep size low
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            if (bitmap != null) {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                selectedImageBytes = baos.toByteArray();
                ivProductPreview.setImageBitmap(bitmap);
                selectedFileName = "product_" + System.currentTimeMillis() + ".jpg";
                uploadedImageUrl = null; // reset, will upload on save
            } else {
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memilih gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        // Jika ada gambar yang dipilih dari perangkat, upload dulu ke Supabase Storage
        if (selectedImageBytes != null && uploadedImageUrl == null) {
            btnSave.setEnabled(false);
            SupabaseHelper.getInstance().uploadImageToStorage(selectedImageBytes, selectedFileName, new SupabaseHelper.SimpleCallback() {
                @Override
                public void onSuccess(String url) {
                    runOnUiThread(() -> {
                        uploadedImageUrl = url;
                        proceedSaveProduct(name, price, stock);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(AddProductActivity.this, "Upload gambar gagal: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            proceedSaveProduct(name, price, stock);
        }
    }

    private void proceedSaveProduct(String name, double price, int stock) {
        // Buat objek produk
        IceCreamProduct product;
        String imageUrl = uploadedImageUrl;
        if (isEditMode) {
            product = new IceCreamProduct(
                    editProduct.getId(), name, price, stock, selectedImageResId, imageUrl);
        } else {
            String productId = UUID.randomUUID().toString();
            product = new IceCreamProduct(
                    productId, name, price, stock, selectedImageResId, imageUrl);
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