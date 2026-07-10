# Buku Panduan Penggunaan & Konfigurasi eProject
**Aplikasi Jalan dan Jembatan**  
**eProject by Dendy Sofian ©2024**

---

## 1. Pendahuluan

**eProject** adalah aplikasi berbasis Android dan Website yang dirancang untuk membantu rekapitulasi volume pekerjaan (opname) jalan dan jembatan sesuai Spesifikasi Umum Bina Marga. Aplikasi ini mendukung penuh **mode offline** (dapat digunakan di lokasi tanpa sinyal) dan akan melakukan **sinkronisasi otomatis** saat terhubung ke internet.

### Hak Akses Pengguna
1. **Super Admin (`eproject.admin@gmail.com`)**:
   - Menyetujui pendaftaran akun Pengguna dan Admin.
   - Melihat seluruh daftar akun yang terdaftar beserta masa berlakunya.
   - Menghapus akun yang tidak aktif atau bermasalah.
2. **Admin (Mendaftar dengan email sendiri)**:
   - Melihat rincian kontrak dan rekap opname pengguna lain dengan memasukkan email pengguna tersebut.
   - Tidak dapat menginput/mengubah data (Hanya View).
3. **Pengguna / User (Mendaftar dengan email sendiri)**:
   - Mendapatkan Nomor Akun unik saat disetujui (berlaku selama 1 tahun).
   - Mengisi data kontrak kerja dan rincian item pekerjaan.
   - Melakukan input data opname lapangan lengkap dengan foto dokumentasi.

---

## 2. Panduan Fitur Aplikasi Android

### A. Registrasi dan Login (Halaman Pertama)
- Buka aplikasi, pilih tab **Daftar** untuk membuat akun baru.
- Isi **Nama Paket Pekerjaan**, **Email**, **Kata Sandi**, serta pilih mendaftar sebagai **Pengguna / User** atau **Admin**.
- Klik **Daftar Sekarang**. Status akun akan masuk dalam daftar tunggu persetujuan Super Admin.
- Setelah disetujui oleh Super Admin (`eproject.admin@gmail.com`), Anda dapat masuk ke aplikasi menggunakan tab **Masuk**.

### B. Menu Beranda (Home)
- Menampilkan nama paket pekerjaan yang sedang berjalan.
- **Progres Fisik Rata-rata** (persentase volume opname terhadap volume kontrak).
- **Rekapitulasi Volume Per Item**: Daftar rincian progres per item pekerjaan yang diurutkan rapi berdasarkan kode item Bina Marga.
- **Status Sinkronisasi**: Menampilkan badge `OFFLINE READY` serta keterangan apakah data lokal Anda sudah berhasil ter-upload ke server (Google Sheets / Firebase).

### C. Menu Kontrak
- **Input Data Kontrak**: Pilih divisi Bina Marga, pilih item pekerjaan, isi volume kontrak, dan harga satuan.
- Klik **Tambah Item Baru** jika ingin menambahkan kode item kustom di luar spesifikasi default.
- Klik **Tambah Satuan Baru** jika ingin menambahkan satuan volume baru.
- **Rincian Item Kontrak (Tabel)**: Geser tabel ke samping untuk melihat total harga per item, edit data volume/harga, atau menghapus item.

### D. Menu Input (Hanya untuk Pengguna)
- Digunakan untuk melaporkan data opname hasil pengukuran lapangan.
- Isi **Lokasi Pekerjaan** (contoh: *Sta 1+250*).
- Pilih **Sisi Jalan** (Kanan, Kiri, atau CL).
- Pilih atau tambahkan **Ruas Jalan** baru. Anda dapat mengedit dan menghapus daftar ruas jalan.
- Isi dimensi pengukuran: **Panjang (m)**, **Lebar (m)**, **Tinggi (m)**, **Tebal (cm)**, **Luasan (m2)**, **Jumlah**, serta **Berat Jenis (BJ)** (khusus untuk satuan ton).
- Aplikasi akan menampilkan formula dan hasil perhitungan volume total secara live sesuai satuan yang dipilih.
- Ambil foto dokumentasi melalui kamera atau galeri handphone.
- Klik **Simpan Data Pekerjaan (Offline)**. Data akan otomatis masuk ke tabel opname dan ter-upload ketika Anda mendapatkan jaringan internet.

### E. Menu Opname (Rekap Data)
- Menampilkan tabel lengkap seluruh data opname yang sudah dimasukkan.
- Terdapat filter pencarian berdasarkan **Rentang Tanggal**, **Filter Item**, dan **Filter Ruas Jalan**.
- Geser tabel secara horizontal untuk melihat semua detail dimensi.
- Klik **Edit** atau **Hapus** langsung dari tabel untuk menyesuaikan data.
- Klik tombol **Print / Export ke Excel** untuk mengunduh rekapitulasi data dalam bentuk file Excel/CSV ke penyimpanan perangkat Anda.

### F. Menu Profil
- Mengubah foto profil dan nama paket pekerjaan.
- Menampilkan detail lisensi akun dan sisa masa aktif akun (1 tahun).
- Tombol **Keluar Aplikasi** (Logout).

---

## 3. Konfigurasi Google Firebase Console

Aplikasi eProject dirancang agar terhubung secara aman dengan Google Firebase untuk autentikasi dan database real-time. Ikuti langkah berikut untuk mengonfigurasinya:

1. Buka [Firebase Console](https://console.firebase.google.com/) dan masuk menggunakan akun Google Anda.
2. Klik **Add Project**, masukkan nama proyek `eProject Kaltara`, lalu klik **Continue**.
3. Di dashboard proyek Anda, klik ikon **Android** untuk mendaftarkan aplikasi:
   - Android Package Name: `com.example`
   - App Nickname: `eProject App`
   - Klik **Register App**.
4. Unduh file `google-services.json` dan letakkan di dalam folder `/app/` pada proyek Android Anda.
5. Pada Firebase Console sidebar, masuk ke menu **Build -> Firestore Database**:
   - Klik **Create Database**.
   - Pilih mode keamanan **Start in test mode** atau terapkan aturan keamanan (`firestore.rules`) yang sesuai agar aman.
6. Masuk ke menu **Build -> Authentication**:
   - Aktifkan metode login **Email/Password**.

---

## 4. Konfigurasi Google Sheets & Google Apps Script (code.gs)

Web Dashboard eProject ditenagai oleh Google Sheets sebagai database utama berbasis cloud untuk memudahkan ekspor data dinas.

1. Buat Spreadsheet baru di Google Drive Anda, beri nama `eProject_Database_BPJN_Kaltara`.
2. Di dalam Spreadsheet tersebut, buka menu **Extensions -> Apps Script** (Ekstensi -> Apps Script).
3. Hapus semua kode default di dalam editor Apps Script, lalu salin isi file `code.gs` yang disediakan oleh sistem eProject ke editor tersebut.
4. Buat file HTML baru di editor Apps Script dengan nama `index` (tanpa ekstensi `.html`), lalu salin seluruh isi file `index.html` yang disediakan sistem ke file tersebut.
5. Simpan proyek Apps Script Anda.
6. Klik tombol **Deploy -> New Deployment**:
   - Select type: **Web App**.
   - Description: `eProject API & Web v1`.
   - Execute as: **Me (email Anda)**.
   - Who has access: **Anyone** (agar aplikasi Android dapat mengirimkan data offline).
   - Klik **Deploy**.
7. Salin **Web App URL** yang dihasilkan. Paste URL tersebut ke dalam variable `url` di file `AppRepository.kt` baris 220 untuk menghubungkan sinkronisasi otomatis Android Anda ke cloud secara sempurna!

---
*Aplikasi ini dikembangkan untuk BPJN Kalimantan Utara. Seluruh hak cipta dilindungi.*
