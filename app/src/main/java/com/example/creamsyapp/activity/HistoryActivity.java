package com.example.creamsyapp.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.creamsyapp.R;
import com.example.creamsyapp.supabase.SupabaseHelper;
import com.example.creamsyapp.product.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private List<Transaction> transactionHistory;
    private ArrayAdapter<Transaction> historyAdapter;
    private boolean isDeletingMode = false;
    private List<Transaction> selectedTransactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Ambil riwayat transaksi dari intent (sebagai fallback awal)
        transactionHistory = getIntent().getParcelableArrayListExtra("transactions");
        if (transactionHistory == null) {
            transactionHistory = new ArrayList<>();
        }

        // Setup ListView untuk riwayat transaksi
        ListView historyListView = findViewById(R.id.history_list_view);
        historyAdapter = new ArrayAdapter<Transaction>(this,
                R.layout.transaction_item, transactionHistory) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.transaction_item, parent, false);
                }

                Transaction transaction = getItem(position);
                TextView tvTransaction = (TextView) convertView.findViewById(R.id.tv_transaction);
                TextView tvSelect = (TextView) convertView.findViewById(R.id.tv_select);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date ts = transaction.getTimestamp();
                String dateString = ts != null ? sdf.format(ts) : "-";
                tvTransaction.setText(String.format("Total: Rp %.0f | %s", transaction.getTotal(), dateString));

                // Tampilkan checkbox untuk mode penghapusan
                tvSelect.setVisibility(isDeletingMode ? View.VISIBLE : View.GONE);
                tvSelect.setText(selectedTransactions.contains(transaction) ? "✓" : "○");

                return convertView;
            }
        };
        historyListView.setAdapter(historyAdapter);

        // Setup tombol kembali
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            if (isDeletingMode) {
                isDeletingMode = false;
                selectedTransactions.clear();
                historyAdapter.notifyDataSetChanged();
                updateDeleteUI();
            } else {
                finish();
            }
        });

        // Setup tombol hapus
        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            if (isDeletingMode) {
                if (selectedTransactions.isEmpty()) {
                    showDeleteConfirmationDialog();
                } else {
                    deleteSelectedTransactions();
                }
            } else {
                isDeletingMode = true;
                selectedTransactions.clear();
                historyAdapter.notifyDataSetChanged();
                updateDeleteUI();
            }
        });

        // Setup tombol Kirim ke WA
        Button btnSendWa = findViewById(R.id.btn_send_wa);
        if (btnSendWa != null) {
            btnSendWa.setOnClickListener(v -> sendHistoryToWhatsApp());
        }

        // Set initial UI state
        updateDeleteUI();

        // Setup klik item
        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            if (isDeletingMode) {
                Transaction transaction = transactionHistory.get(position);
                if (selectedTransactions.contains(transaction)) {
                    selectedTransactions.remove(transaction);
                } else {
                    selectedTransactions.add(transaction);
                }
                historyAdapter.notifyDataSetChanged();
                updateDeleteUI();
            } else {
                showTransactionDetails(transactionHistory.get(position));
            }
        });

        // Muat ulang riwayat transaksi dari Supabase agar selalu terbaru (dan memiliki ID dari DB)
        refreshTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Pastikan data terbaru saat kembali ke layar ini
        refreshTransactions();
    }

    private void sendHistoryToWhatsApp() {
        if (transactionHistory == null || transactionHistory.isEmpty()) {
            Toast.makeText(this, "Tidak ada riwayat transaksi", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = buildHistoryMessage(transactionHistory);

        // Intent umum
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);

        // Coba WhatsApp reguler
        Intent waIntent = new Intent(sendIntent);
        waIntent.setPackage("com.whatsapp");
        try {
            startActivity(waIntent);
            return;
        } catch (ActivityNotFoundException e) {
            // try WhatsApp Business
        }

        Intent waBusinessIntent = new Intent(sendIntent);
        waBusinessIntent.setPackage("com.whatsapp.w4b");
        try {
            startActivity(waBusinessIntent);
            return;
        } catch (ActivityNotFoundException e) {
            // fallback ke chooser
        }

        startActivity(Intent.createChooser(sendIntent, "Kirim riwayat via"));
    }

    private String buildHistoryMessage(List<Transaction> txs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Riwayat Transaksi\n");
        sb.append("===================\n");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        double totalAll = 0.0;
        int idx = 1;
        for (Transaction t : txs) {
            Date ts = t.getTimestamp();
            String dateString = ts != null ? sdf.format(ts) : "-";
            double total = t.getTotal();
            totalAll += total;
            sb.append(String.format(Locale.getDefault(), "%d. %s | Total: Rp %.0f | Bayar: Rp %.0f | Kembali: Rp %.0f\n",
                    idx++, dateString, total, t.getAmountPaid(), t.getChange()));
        }
        sb.append("-------------------\n");
        sb.append(String.format(Locale.getDefault(), "Jumlah transaksi: %d\n", txs.size()));
        sb.append(String.format(Locale.getDefault(), "Total keseluruhan: Rp %.0f\n", totalAll));
        return sb.toString();
    }

    private void refreshTransactions() {
        SupabaseHelper.getInstance().loadTransactions(new SupabaseHelper.TransactionsCallback() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                runOnUiThread(() -> {
                    transactionHistory.clear();
                    transactionHistory.addAll(transactions);
                    selectedTransactions.clear();
                    isDeletingMode = false;
                    historyAdapter.notifyDataSetChanged();
                    updateDeleteUI();
                });
            }

            @Override
            public void onError(String error) {
                // Biarkan tampilan memakai data intent jika ada; tampilkan error ringan opsional
            }
        });
    }

    private void updateDeleteUI() {
        Button btnDelete = findViewById(R.id.btn_delete);
        Button btnBack = findViewById(R.id.btn_back);
        if (btnDelete == null || btnBack == null) return;
        if (isDeletingMode) {
            btnBack.setText("Batalkan");
            int count = selectedTransactions.size();
            if (count > 0) {
                btnDelete.setText("Hapus (" + count + ")");
            } else {
                btnDelete.setText("Hapus Semua");
            }
        } else {
            btnBack.setText("Kembali");
            btnDelete.setText("Hapus");
        }
    }

    private void showTransactionDetails(Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detail Transaksi");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_transaction_details, null);
        builder.setView(dialogView);

        TextView tvDate = (TextView) dialogView.findViewById(R.id.tv_date);
        TextView tvTotal = (TextView) dialogView.findViewById(R.id.tv_total);
        TextView tvAmountPaid = (TextView) dialogView.findViewById(R.id.tv_amount_paid);
        TextView tvChange = (TextView) dialogView.findViewById(R.id.tv_change);
        ListView lvItems = dialogView.findViewById(R.id.lv_items);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date ts = transaction.getTimestamp();
        tvDate.setText(ts != null ? sdf.format(ts) : "-");
        tvTotal.setText(String.format("Total: Rp %.0f", transaction.getTotal()));
        tvAmountPaid.setText(String.format(Locale.getDefault(), "Rp %.0f", transaction.getAmountPaid()));
        tvChange.setText(String.format(Locale.getDefault(), "Rp %.0f", transaction.getChange()));

        // Tampilkan item dalam transaksi dengan memuat dari database
        ArrayList<String> displayItems = new ArrayList<>();
        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayItems);
        lvItems.setAdapter(itemAdapter);

        // Placeholder saat memuat
        displayItems.add("Memuat item...");
        itemAdapter.notifyDataSetChanged();

        SupabaseHelper.getInstance().loadTransactionItems(transaction.getId(), new SupabaseHelper.ItemsCallback() {
            @Override
            public void onSuccess(List<String> items) {
                runOnUiThread(() -> {
                    displayItems.clear();
                    displayItems.addAll(items);
                    if (displayItems.isEmpty()) {
                        displayItems.add("Tidak ada item");
                    }
                    itemAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    displayItems.clear();
                    displayItems.add("Gagal memuat item: " + error);
                    itemAdapter.notifyDataSetChanged();
                });
            }
        });

        builder.setPositiveButton("Tutup", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.on_surface));
            }
        });
        dialog.show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Semua Riwayat")
                .setMessage("Tidak dapat menghapus riwayat transaksi dari database online.\n\n" +
                        "Riwayat transaksi disimpan di database untuk keperluan arsip dan laporan.\n\n" +
                        "Apakah Anda yakin ingin melanjutkan?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    // Hapus semua transaksi milik user di server
                    SupabaseHelper.getInstance().deleteAllTransactionsForUser(new SupabaseHelper.DatabaseCallback() {
                        @Override
                        public void onSuccess(String id) {
                            transactionHistory.clear();
                            selectedTransactions.clear();
                            isDeletingMode = false;
                            historyAdapter.notifyDataSetChanged();
                            setResult(RESULT_OK);
                            updateDeleteUI();
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            new AlertDialog.Builder(HistoryActivity.this)
                                    .setTitle("Gagal Menghapus")
                                    .setMessage(error)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    });
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void deleteSelectedTransactions() {
        // Hapus transaksi terpilih di server
        java.util.List<String> ids = new java.util.ArrayList<>();
        for (Transaction t : selectedTransactions) ids.add(t.getId());

        SupabaseHelper.getInstance().deleteTransactionsByIds(ids, new SupabaseHelper.DatabaseCallback() {
            @Override
            public void onSuccess(String id) {
                transactionHistory.removeAll(selectedTransactions);
                selectedTransactions.clear();
                isDeletingMode = false;
                historyAdapter.notifyDataSetChanged();
                setResult(RESULT_OK);
                updateDeleteUI();
            }

            @Override
            public void onError(String error) {
                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle("Gagal Menghapus")
                        .setMessage(error)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isDeletingMode) {
            isDeletingMode = false;
            selectedTransactions.clear();
            Button btnDelete = findViewById(R.id.btn_delete);
            btnDelete.setText("Hapus");
            Button btnBack = findViewById(R.id.btn_back);
            btnBack.setText("Kembali");
            historyAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }
}