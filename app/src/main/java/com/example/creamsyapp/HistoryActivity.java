package com.example.creamsyapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

        // Ambil riwayat transaksi dari intent
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
                // Perbaikan: Tambahkan casting eksplisit ke TextView
                TextView tvTransaction = (TextView) convertView.findViewById(R.id.tv_transaction);
                TextView tvSelect = (TextView) convertView.findViewById(R.id.tv_select);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String dateString = sdf.format(transaction.getTimestamp());
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
        btnBack.setOnClickListener(v -> finish());

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
                btnDelete.setText("Batalkan");
                historyAdapter.notifyDataSetChanged();
            }
        });

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
            } else {
                showTransactionDetails(transactionHistory.get(position));
            }
        });
    }

    private void showTransactionDetails(Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detail Transaksi");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_transaction_details, null);
        builder.setView(dialogView);

        // Perbaikan: Tambahkan casting eksplisit ke TextView
        TextView tvDate = (TextView) dialogView.findViewById(R.id.tv_date);
        TextView tvTotal = (TextView) dialogView.findViewById(R.id.tv_total);
        ListView lvItems = dialogView.findViewById(R.id.lv_items);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvDate.setText(sdf.format(transaction.getTimestamp()));
        tvTotal.setText(String.format("Total: Rp %.0f", transaction.getTotal()));

        // Tampilkan item dalam transaksi
        ArrayAdapter<IceCreamProduct> itemAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, transaction.getItems()) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(
                            android.R.layout.simple_list_item_1, parent, false);
                }
                // Perbaikan: Tambahkan casting eksplisit ke TextView
                TextView text = (TextView) convertView.findViewById(android.R.id.text1);
                IceCreamProduct product = getItem(position);
                text.setText(String.format("%s - Rp %.0f", product.getName(), product.getPrice()));
                return convertView;
            }
        };
        lvItems.setAdapter(itemAdapter);

        builder.setPositiveButton("Tutup", null);
        builder.show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Semua Riwayat")
                .setMessage("Apakah Anda yakin ingin menghapus semua riwayat transaksi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    transactionHistory.clear();
                    selectedTransactions.clear();
                    isDeletingMode = false;
                    historyAdapter.notifyDataSetChanged();
                    setResult(RESULT_OK); // Beri tahu MainActivity bahwa riwayat telah dihapus
                    finish();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void deleteSelectedTransactions() {
        transactionHistory.removeAll(selectedTransactions);
        selectedTransactions.clear();
        isDeletingMode = false;
        historyAdapter.notifyDataSetChanged();
        setResult(RESULT_OK); // Beri tahu MainActivity bahwa riwayat telah dihapus
    }

    @Override
    public void onBackPressed() {
        if (isDeletingMode) {
            isDeletingMode = false;
            selectedTransactions.clear();
            Button btnDelete = findViewById(R.id.btn_delete);
            btnDelete.setText("Hapus");
            historyAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }
}