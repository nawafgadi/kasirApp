# API Specification - Reports & Dashboard

Dokumentasi ini berisi spesifikasi API untuk modul Laporan (Dashboard) dan Riwayat Penjualan. API ini digunakan oleh Frontend / Mobile App untuk menampilkan ringkasan data, statistik, grafik penjualan, dan riwayat transaksi.

---

## 1. Get Dashboard Reports (Ringkasan Laporan)

Endpoint ini mengembalikan seluruh data ringkasan metrik dashboard, termasuk total pendapatan, rata-rata keranjang, tren penjualan, dan produk terlaris. Data dipisah berdasarkan periode (hari ini, minggu ini, bulan ini, tahun ini, dan sepanjang masa).

Endpoint ini juga menyediakan helper `grafik_data` khusus untuk mempermudah render *chart* (grafik) di Mobile/Frontend.

- **Method:** `GET`
- **Endpoint:** `/api/reports` *(Sesuaikan prefix route dengan `routes/api.php` Anda)*
- **Headers:**
    - `Authorization: Bearer {token}`
    - `Accept: application/json`

### Response Success (200 OK)

```json
{
  "message": "Report data retrieved successfully.",
  "data": {
    "hari_ini": {
      "total_pendapatan": 500000,
      "pendapatan_vs_sebelumnya": {
        "nilai_sebelumnya": 400000,
        "persentase_perubahan": 25.0
      },
      "total_transaksi": 10,
      "rata_rata_keranjang": 50000.0,
      "tren_penjualan": [
        {
          "date": "2023-10-31",
          "total": 500000.0
        }
      ],
      "produk_terlaris": [
        {
          "name": "Kopi Susu",
          "total_quantity": 20
        }
      ],
      "transaksi_terakhir": [
        {
          "id": 1,
          "trx_type": "SALE",
          "total_amount": 50000,
          "trx_date": "2023-10-31 10:00:00",
          "items": []
        }
      ]
    },
    "minggu_ini": {
        "...": "Sama dengan struktur hari_ini"
    },
    "bulan_ini": {
        "...": "Sama dengan struktur hari_ini"
    },
    "tahun_ini": {
        "...": "Sama dengan struktur hari_ini"
    },
    "sepanjang_masa": {
        "...": "Sama dengan struktur hari_ini"
    },
    "grafik_data": {
      "minggu_ini": {
        "labels": ["2023-10-25", "2023-10-26", "2023-10-27"],
        "values": [150000, 200000, 250000]
      },
      "bulan_ini": {
        "labels": ["2023-10-01", "2023-10-02"],
        "values": [500000, 600000]
      },
      "tahun_ini": {
        "labels": ["2023-01-01", "2023-02-01"],
        "values": [15000000, 20000000]
      }
    }
  }
}