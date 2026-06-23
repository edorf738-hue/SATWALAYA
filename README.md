# Satwalaya

**Satwalaya** adalah aplikasi pet care berbasis Android yang memudahkan pemilik hewan untuk menitipkan, merawat, dan memantau hewan kesayangan mereka. Mulai dari booking penginapan, grooming, hingga update harian selama masa penitipan — semua dalam satu aplikasi.

## Fitur Utama

- **Booking Pet Hotel & Grooming** — dua layanan utama dengan paket Reguler/Premium (Hotel) dan Paket Fresh/Full Grooming (Grooming)
- **My Pet ID** — kelola data hewan peliharaan (nama, jenis, ras, umur, berat, alergi, jadwal makan) tanpa perlu input ulang setiap booking
- **Multi-pet booking** — booking lebih dari satu hewan dalam satu transaksi
- **Daily Updates** — pantau kondisi hewan secara real-time selama masa penitipan, lengkap dengan foto dari staf
- **Riwayat & Status Tracking** — lihat status booking dari menunggu verifikasi hingga selesai
- **Pembayaran** — mendukung COD dan transfer bank dengan upload bukti pembayaran
- **Ulasan & Rating** — beri rating dan review setelah booking selesai
- **Login Google & Email** — autentikasi fleksibel dengan opsi reset password

## Tech Stack

- **Bahasa:** Kotlin
- **Arsitektur:** MVVM (ViewModel + LiveData)
- **Database:** Firebase Firestore (realtime dengan `addSnapshotListener`)
- **Autentikasi:** Firebase Authentication (Email & Google Sign-In)
- **Storage:** Firebase Storage (foto profil, foto hewan, bukti pembayaran)
- **UI:** ViewBinding, Navigation Component, Material Components
- **Image Loading:** Glide

## Struktur Halaman

| Halaman | Deskripsi |
|---|---|
| Home | Ringkasan hewan, booking aktif, dan ulasan pelanggan |
| My Pet ID | Daftar dan kelola data hewan peliharaan |
| Booking | Pilih layanan Hotel/Grooming dan buat pesanan |
| Riwayat | Pantau status booking dan ajukan ulasan |
| Daily Updates | Update kondisi hewan selama penitipan |
| Profil | Kelola data akun, foto profil, dan keamanan |

## Tim Pengembang

| Nama | Peran |
|---|---|
| Edo Rizki Firnando | Backend Logic, Database Integration, Git/Repo Management |
| Alexander Saputra Nadeak | UI/UX & Frontend |
| Fazil Al-Falah | Analisis & Frontend |
