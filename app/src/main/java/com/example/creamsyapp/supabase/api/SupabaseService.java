package com.example.creamsyapp.supabase.api;

import com.example.creamsyapp.product.IceCreamProduct;
import com.example.creamsyapp.product.Transaction;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseService {
    // Autentikasi
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> signIn(@Header("apikey") String apiKey, @Body AuthRequest authRequest);

    // Refresh token
    @POST("auth/v1/token?grant_type=refresh_token")
    Call<AuthResponse> refreshToken(@Header("apikey") String apiKey, @Body RefreshRequest request);

    // Produk
    @GET("rest/v1/products")
    Call<List<IceCreamProduct>> getProducts(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("user_id") String userId,
            @Query("select") String select,
            @Query("order") String order
    );

    @POST("rest/v1/products")
    Call<Void> addProduct(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Prefer") String prefer,
            @Body Map<String, Object> productData
    );

    // PERBAIKAN: Gunakan @Query untuk id
    @PATCH("rest/v1/products")
    Call<Void> updateProduct(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Prefer") String prefer,
            @Query("id") String id,
            @Body Map<String, Object> productData
    );

    // PERBAIKAN: Gunakan @Query untuk id
    @DELETE("rest/v1/products")
    Call<Void> deleteProduct(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("id") String id
    );

    // Transaksi
    @GET("rest/v1/transactions")
    Call<List<Transaction>> getTransactions(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("user_id") String userId,
            @Query("select") String select,
            @Query("order") String order
    );

    @POST("rest/v1/transactions")
    Call<Transaction> addTransaction(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Prefer") String prefer,
            @Header("Accept") String accept,
            @Body Map<String, Object> transactionData
    );

    @POST("rest/v1/transaction_items")
    Call<Void> addTransactionItems(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Prefer") String prefer,
            @Body List<Map<String, Object>> items
    );

    // Transaction items fetch with embedded product
    @GET("rest/v1/transaction_items")
    Call<List<Map<String, Object>>> getTransactionItems(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("transaction_id") String transactionId,
            @Query("select") String select,
            @Query("order") String order
    );

    // Hapus transaction_items berdasarkan transaction_id filter (eq./in.)
    @DELETE("rest/v1/transaction_items")
    Call<Void> deleteTransactionItems(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("transaction_id") String transactionIdFilter
    );

    // Hapus transactions berdasarkan id filter (eq./in.) atau user_id (untuk hapus semua user)
    @DELETE("rest/v1/transactions")
    Call<Void> deleteTransactions(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Query("id") String idFilter,
            @Query("user_id") String userIdFilter
    );

    // Storage: upload object (bucket should be public for direct access)
    @PUT("storage/v1/object/{bucket}/{path}")
    Call<Void> uploadObject(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType,
            @Path("bucket") String bucket,
            @Path(value = "path", encoded = true) String objectPath,
            @Body RequestBody body
    );

    // Inner classes untuk autentikasi
    class AuthRequest {
        private String email;
        private String password;

        public AuthRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    class RefreshRequest {
        private String refresh_token;

        public RefreshRequest(String refresh_token) {
            this.refresh_token = refresh_token;
        }
    }

    class AuthResponse {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private int expires_in;
        private User user;

        // Getters
        public String getAccessToken() { return access_token; }
        public String getTokenType() { return token_type; }
        public String getRefreshToken() { return refresh_token; }
        public int getExpiresIn() { return expires_in; }
        public User getUser() { return user; }

        // Inner class untuk user
        public static class User {
            private String id;
            private String email;

            public String getId() { return id; }
            public String getEmail() { return email; }
        }
    }
}