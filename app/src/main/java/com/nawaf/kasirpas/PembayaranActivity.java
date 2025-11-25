package com.nawaf.kasirpas;

import android.content.Intent; // PERBAIKAN 1: Import kelas Intent
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.nawaf.kasirpas.databinding.ActivityPembayaranBinding;

enum PaymentMethod {
    CASH,
    TRANSFER
}

public class PembayaranActivity extends AppCompatActivity {

    private ActivityPembayaranBinding binding;
    private PaymentMethod selectedMethod;
    private int totalHarga = 0; // Tambahkan variabel untuk menampung total harga

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPembayaranBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ambil total harga yang dikirim dari PesananActivity
        totalHarga = getIntent().getIntExtra("TOTAL_HARGA", 0);

        setupListeners();
        selectPaymentMethod(PaymentMethod.CASH);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.cardCash.setOnClickListener(v -> selectPaymentMethod(PaymentMethod.CASH));

        binding.cardTransfer.setOnClickListener(v -> selectPaymentMethod(PaymentMethod.TRANSFER));

        // --- INI BAGIAN YANG DIPERBAIKI ---
        binding.btnBayarSekarang.setOnClickListener(v -> {
            if (selectedMethod == null) {
                Toast.makeText(PembayaranActivity.this, "Silakan pilih metode pembayaran terlebih dahulu.", Toast.LENGTH_SHORT).show();
                return;
            }

            // PERBAIKAN 2: Buat Intent untuk pindah ke KonfirmasiPembayaranActivity
            Intent intent = new Intent(PembayaranActivity.this, KonfirmasiPembayaranActivity.class);

            // (Opsional tapi penting) Kirim data yang relevan ke halaman konfirmasi
            intent.putExtra("TOTAL_HARGA", totalHarga);
            intent.putExtra("METODE_PEMBAYARAN", selectedMethod.name()); // Mengirim "CASH" atau "TRANSFER"

            // PERBAIKAN 3: Jalankan perintah pindah halaman
            startActivity(intent);

            // Tampilkan Toast setelah pindah halaman (opsional)
            Toast.makeText(PembayaranActivity.this, "Pembayaran dikonfirmasi!", Toast.LENGTH_SHORT).show();
        });
    }

    private void selectPaymentMethod(PaymentMethod method) {
        selectedMethod = method;
        updateUi();
    }

    private void updateUi() {
        if (selectedMethod == null) {
            binding.checkCash.setVisibility(View.GONE);
            binding.checkTransfer.setVisibility(View.GONE);
            binding.cardDetailTransfer.setVisibility(View.GONE);
            return;
        }

        switch (selectedMethod) {
            case CASH:
                binding.checkCash.setVisibility(View.VISIBLE);
                binding.checkTransfer.setVisibility(View.GONE);
                binding.cardDetailTransfer.setVisibility(View.GONE);
                break;
            case TRANSFER:
                binding.checkCash.setVisibility(View.GONE);
                binding.checkTransfer.setVisibility(View.VISIBLE);
                binding.cardDetailTransfer.setVisibility(View.VISIBLE);
                break;
        }
    }
}
