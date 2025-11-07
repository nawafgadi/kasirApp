package com.nawaf.kasirpas;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.nawaf.kasirpas.databinding.ActivityDetailMakananBinding;

public class detailMakananActivity extends AppCompatActivity {

    private ActivityDetailMakananBinding binding;
    private String pilihanRasa = "Pedas";
    private String porsi = "Sedang";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menggunakan View Binding untuk menghubungkan layout
        binding = ActivityDetailMakananBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Mengatur listener untuk tombol kembali
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Menutup activity saat ini
            }
        });

        // Mengatur listener untuk tombol pilihan rasa
        binding.btnPilihan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] rasaList = {"Tidak Pedas", "Sedang", "Pedas", "Sangat Pedas"};
                AlertDialog.Builder builder = new AlertDialog.Builder(detailMakananActivity.this);
                builder.setTitle("Pilih Tingkat Kepedasan");
                builder.setItems(rasaList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pilihanRasa = rasaList[which];
                        binding.btnPilihan.setText(pilihanRasa);
                    }
                });
                builder.show();
            }
        });

        // Mengatur listener untuk tombol pilihan porsi
        binding.btnPorsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] porsiList = {"Kecil", "Sedang", "Besar"};
                AlertDialog.Builder builder = new AlertDialog.Builder(detailMakananActivity.this);
                builder.setTitle("Pilih Porsi");
                builder.setItems(porsiList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        porsi = porsiList[which];
                        binding.btnPorsi.setText(porsi);
                    }
                });
                builder.show();
            }
        });

        // Mengatur listener untuk tombol tambahkan ke keranjang
        binding.btnTambahkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Ditambahkan: " + binding.txtNamaMakanan.getText().toString() +
                                 " (" + pilihanRasa + ", " + porsi + ")";
                Toast.makeText(detailMakananActivity.this, message, Toast.LENGTH_SHORT).show();
                // Di sini Anda bisa menambahkan logika untuk mengirim data ke activity keranjang
            }
        });

        // Mengatur listener untuk tombol lihat keranjang
        binding.btnKeranjang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Di sini Anda bisa menambahkan intent untuk pindah ke KeranjangActivity
                Toast.makeText(detailMakananActivity.this, "Membuka keranjang...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
