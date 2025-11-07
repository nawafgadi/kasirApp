package com.nawaf.kasirpas;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityMakanan extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makanan);

        final Button pilihanButton = findViewById(R.id.pilihanButton);

        pilihanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] pilihanRasa = {"Pedas", "Sedang", "Tidak Pedas"};

                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMakanan.this);
                builder.setTitle("Pilih Tingkat Kepedasan");
                builder.setItems(pilihanRasa, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selected = pilihanRasa[which];
                        pilihanButton.setText(selected);
                        Toast.makeText(getApplicationContext(), "Pilihan: " + selected, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });
    }
}
