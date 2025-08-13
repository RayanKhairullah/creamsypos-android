Design Database:
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    stock INTEGER NOT NULL CHECK (stock >= 0),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cashier_id UUID NOT NULL,
    total_amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE transaction_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    product_id UUID NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_per_unit NUMERIC(10,2) NOT NULL
);

CREATE TABLE cashiers (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id),
    pin_hash TEXT NOT NULL
);

Flow Aplikasi:
1. Alur Login & Autentikasi
graph TD
    A[Start App] --> B{Sudah Login?}
    B -->|Ya| C[Verifikasi Session Supabase]
    B -->|Tidak| D[Halaman Login]
    D --> E[Input Email & Password]
    E --> F[Supabase Auth Sign In]
    F -->|Gagal| E
    F -->|Sukses| G[Input PIN 6 Digit]
    G --> H[Verifikasi PIN di Tabel cashiers]
    H -->|PIN Salah| G
    H -->|PIN Benar| I[Buka Halaman Utama POS]

Detail Teknis:
Gunakan SupabaseClient.auth.signInWithPassword() untuk autentikasi email/password
Setelah session valid, ambil user.id dan verifikasi PIN
Implementasi session timeout
15 menit untuk keamanan

2. Alur Transaksi Utama
graph TD
    A[Halaman Utama POS] --> B[Tampilkan Produk dari Tabel products]
    B --> C{Kasir Pilih Produk}
    C -->|Klik Produk| D[Tambah ke Keranjang]
    D --> E[Validasi Stok Tersedia]
    E -->|Stok Cukup| F[Update Keranjang]
    E -->|Stok Tidak Cukup| G[Tampilkan Peringatan]
    F --> H{Tambah Produk Lain?}
    H -->|Ya| B
    H -->|Tidak| I[Klik Bayar]
    I --> J[Input Jumlah Uang Tunai]
    J --> K[Hitung Kembalian]
    K --> L[Konfirmasi Transaksi]
    L --> M[Simpan ke Database]
    M --> N[Cetak/ Tampilkan Receipt]

Detail Teknis:
A. Pengambilan Data Produk
B. Proses Transaksi (Critical Section)
C. Stored Procedure untuk Transaksi (Di Supabase)

3. Alur Ekspor Laporan ke Excel
graph TD
    A[Klik Menu Laporan] --> B[Pilih Rentang Tanggal]
    B --> C[Query Transaksi dengan Supabase]
    C --> D[Format Data ke CSV]
    D --> E[Generate File Excel]
    E --> F[Tampilkan Opsi: Simpan/Bagikan]

4. Alur Keamanan & Error Handling
. Penanganan Error Kritis
Stok tidak mencukupi
Tampilkan notifikasi + disable tombol "Tambah ke Keranjang"
Koneksi internet terputus
Simpan transaksi sementara di local DB (Room) + sync saat online
PIN salah 3x berturut-turut
Blokir sementara + wajibkan logout otomatis
Transaksi gagal disimpan
Rollback perubahan stok + tampilkan detail error

5. Fitur Pendukung Kritis
A. Receipt Generator
Simpan receipt sebagai PDF lokal
Struktur receipt:
[Nama Toko]
=====================
Es Krim Vanila x2   Rp20.000
Es Krim Coklat x1   Rp15.000
---------------------
TOTAL             Rp35.000
Tunai             Rp50.000
Kembalian         Rp15.000
=====================
[Timestamp Transaksi]

B. Manajemen Session
Implementasi token refresh setiap 5 menit
Gunakan SupabaseClient.auth.onAuthStateChange untuk deteksi session expired
C. Optimasi Performa
Gunakan query batch untuk transaksi multi-item
Terapkan caching produk dengan Room DB untuk akses offline sederhana

6. Alur Keluar Aplikasi
graph LR
    A[Klik Logout] --> B[Hapus Session Supabase]
    B --> C[Hapus Data Sementara]
    C --> D[Tampilkan Halaman Login]


enviroment:
Project URL=https://odoziqrjqlbgxjcxkuud.supabase.co
anon public=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9kb3ppcXJqcWxiZ3hqY3hrdXVkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ5MTUxNzUsImV4cCI6MjA3MDQ5MTE3NX0.R1rwun6dUzWAB-xjWPvw8Xs58uZCX7wNtLWojHWp_JU