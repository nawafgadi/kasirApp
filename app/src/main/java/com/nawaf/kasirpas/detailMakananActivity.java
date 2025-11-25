package com.nawaf.kasirpas;

import android.content.DialogInterface;
import android.content.Intent; // <-- PERBAIKAN 1: Pastikan Intent di-import
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.NumberFormat;
import java.util.Locale;

public class detailMakananActivity extends AppCompatActivity {

    private TextView txtNamaMakanan, txtHarga;
    private Button btnPilihan, btnPorsi, btnTambahkan;
    private ImageButton btnBack, btnKeranjang;

    private String pilihanMerek = "Sania";
    private String pilihanBerat = "5 Kg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_makanan);
        initViews();
        loadDataFromIntent();
        setupClickListeners();
    }

    private void initViews() {
        txtNamaMakanan = findViewById(R.id.txtNamaMakanan);
        txtHarga = findViewById(R.id.txtHarga);
        btnPilihan = findViewById(R.id.btnPilihan);
        btnPorsi = findViewById(R.id.btnPorsi);
        btnTambahkan = findViewById(R.id.btnTambahkan);
        btnBack = findViewById(R.id.btnBack);
        btnKeranjang = findViewById(R.id.btnKeranjang);
    }

    private void loadDataFromIntent() {
        String namaMakanan = getIntent().getStringExtra("extra_nama_makanan");
        int hargaMakanan = getIntent().getIntExtra("extra_harga_makanan", 0);

        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        String hargaRupiah = formatRupiah.format(hargaMakanan).replace(",00", "");

        if (namaMakanan != null) {
            txtNamaMakanan.setText(namaMakanan);
        }
        txtHarga.setText(hargaRupiah);

        btnPilihan.setText(pilihanMerek);
        btnPorsi.setText(pilihanBerat);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPilihan.setOnClickListener(v -> {
            final String[] merekList = {"Sania", "Cap Bunga", "Maknyus", "Raja"};
            new AlertDialog.Builder(this)
                    .setTitle("Pilih Merek/Jenis")
                    .setItems(merekList, (dialog, which) -> {
                        pilihanMerek = merekList[which];
                        btnPilihan.setText(pilihanMerek);
                    })
                    .show();
        });

        btnPorsi.setOnClickListener(v -> {
            final String[] beratList = {"1 Kg", "2 Kg", "5 Kg", "10 Kg"};
            new AlertDialog.Builder(this)
                    .setTitle("Pilih Kemasan/Berat")
                    .setItems(beratList, (dialog, which) -> {
                        pilihanBerat = beratList[which];
                        btnPorsi.setText(pilihanBerat);
                    })
                    .show();
        });

        btnTambahkan.setOnClickListener(v -> {
            String message = "Ditambahkan: " + txtNamaMakanan.getText().toString() +
                    " (" + pilihanMerek + ", " + pilihanBerat + ")";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // --- PERUBAHAN DI SINI ---
        btnKeranjang.setOnClickListener(v -> {
            // Hapus Toast yang lama
            // Toast.makeText(this, "Membuka keranjang...", Toast.LENGTH_SHORT).show();

            // PERBAIKAN 2: Buat Intent untuk pindah ke KeranjangActivity
            Intent intent = new Intent(this, KeranjangActivity.class);
            startActivity(intent);
        });
    }
}
