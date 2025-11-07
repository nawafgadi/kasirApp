package com.nawaf.kasirpas;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.nawaf.kasirpas.databinding.ActivityPembayaranBinding;

// Enum untuk mengelola state pembayaran dengan lebih aman. Bisa diletakkan di file yang sama.
enum PaymentMethod {
    CASH,
    TRANSFER
}

public class PembayaranActivity extends AppCompatActivity {

    private ActivityPembayaranBinding binding;
    private PaymentMethod selectedMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPembayaranBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        // Atur metode tunai sebagai pilihan default saat halaman dibuka
        selectPaymentMethod(PaymentMethod.CASH);
    }

    private void setupListeners() {
        // Listener untuk tombol kembali di toolbar
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Listener untuk memilih metode Bayar Tunai
        binding.cardCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPaymentMethod(PaymentMethod.CASH);
            }
        });

        // Listener untuk memilih metode Transfer Bank
        binding.cardTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPaymentMethod(PaymentMethod.TRANSFER);
            }
        });

        // Listener untuk tombol konfirmasi pembayaran
        binding.btnBayarSekarang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMethod == null) {
                    Toast.makeText(PembayaranActivity.this, "Silakan pilih metode pembayaran terlebih dahulu.", Toast.LENGTH_SHORT).show();
                    return;
                }

                switch (selectedMethod) {
                    case CASH:
                        // Notifikasi khusus jika metode tunai dipilih
                        Toast.makeText(PembayaranActivity.this, "Silakan segera ke kasir untuk melakukan pembayaran.", Toast.LENGTH_LONG).show();
                        break;
                    case TRANSFER:
                        Toast.makeText(PembayaranActivity.this, "Pembayaran berhasil! Silakan tunggu konfirmasi.", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    private void selectPaymentMethod(PaymentMethod method) {
        selectedMethod = method;
        updateUi();
    }

    // Fungsi untuk memperbarui tampilan berdasarkan metode yang dipilih
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
