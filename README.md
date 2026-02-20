# MySharedPreferences

Aplikasi Android sederhana untuk mendemonstrasikan penggunaan **SharedPreferences** dalam menyimpan data preferensi pengguna secara lokal.

## 📱 Deskripsi

MySharedPreferences adalah aplikasi contoh yang menunjukkan cara menyimpan dan mengambil data pengguna menggunakan SharedPreferences di Android. Aplikasi ini memungkinkan pengguna untuk:
- Menyimpan informasi profil (nama, email, umur, nomor telepon)
- Menyimpan preferensi boolean (apakah mencintai MU atau tidak)
- Mengedit data yang sudah tersimpan
- Menampilkan data yang tersimpan di layar utama

## 🏗️ Arsitektur Aplikasi

Aplikasi ini terdiri dari 4 komponen utama:

### 1. **UserModel.kt**
Model data yang merepresentasikan informasi pengguna.

**Konsep Utama:**
- Menggunakan `data class` untuk otomatis generate `equals()`, `hashCode()`, `toString()`
- Implementasi `Parcelable` dengan `@Parcelize` untuk transfer data antar Activity melalui Intent
- Properti nullable (`String?`) untuk menangani kasus data belum diisi
- Default values untuk mencegah null pointer exception

```kotlin
@Parcelize
data class UserModel(
    var name: String? = null,
    var email: String? = null,
    var age: Int = 0,
    var phoneNumber: String? = null,
    var isLove: Boolean = false
) : Parcelable
```

### 2. **UserPreference.kt**
Class helper untuk mengelola operasi SharedPreferences.

**Konsep Utama:**
- **Separation of Concerns**: Memisahkan logika penyimpanan data dari UI
- **Encapsulation**: Semua operasi SharedPreferences terpusat di satu class
- **Internal visibility**: Hanya bisa diakses dalam module yang sama untuk keamanan
- Menggunakan konstanta untuk key SharedPreferences (menghindari typo)
- Extension function `edit{}` untuk atomic transaction (semua perubahan disimpan sekaligus)
- Default values pada getter untuk menghindari null/crash

**Fungsi:**
- `setUser()`: Menyimpan semua data user ke SharedPreferences
- `getUser()`: Mengambil semua data user dari SharedPreferences

### 3. **MainActivity.kt**
Activity utama yang menampilkan data user yang tersimpan.

**Konsep Utama:**
- **Activity Result API**: Menggunakan `registerForActivityResult()` (bukan `startActivityForResult()` yang deprecated)
- **State Management**: Flag `isPreferenceEmpty` untuk mengontrol UI state
- **Backward Compatibility**: Handling berbeda untuk Android 13+ vs versi sebelumnya
- **View Binding**: Penggunaan binding untuk akses view yang type-safe
- **Edge-to-Edge Display**: Modern fullscreen UI

**Flow:**
1. Load data dari SharedPreferences saat onCreate
2. Tampilkan data ke UI (atau "Tidak Ada" jika kosong)
3. Tentukan label tombol ("Simpan" atau "Ubah")
4. Launch FormActivity dengan tipe yang sesuai
5. Terima hasil dari FormActivity dan update UI

### 4. **FormUserPreferenceActivity.kt**
Activity form untuk input/edit data user.

**Konsep Utama:**
- **Dual Mode**: Satu activity untuk dua keperluan (ADD & EDIT)
- **Input Validation**: Validasi komprehensif sebelum menyimpan
  - Empty check untuk semua field
  - Email format validation menggunakan `Patterns.EMAIL_ADDRESS`
  - Digits only check untuk nomor telepon
- **Early Return Pattern**: Keluar dari fungsi segera setelah validasi gagal
- **ActionBar Navigation**: Tombol back di ActionBar

**Flow:**
1. Terima tipe form (ADD/EDIT) dari Intent
2. Jika EDIT: populate form dengan data lama
3. User mengisi/edit form
4. Validasi input saat tombol save diklik
5. Simpan ke SharedPreferences
6. Kirim hasil ke MainActivity via Intent result

## 🔑 Konsep Penting

### SharedPreferences
- **Storage lokal key-value** untuk data kecil yang persisten
- Data bertahan meski aplikasi ditutup
- Cocok untuk: pengaturan, preferensi, data login
- Tidak cocok untuk: data kompleks/besar (gunakan Room/Database)

### Parcelable
- Mekanisme serialization Android untuk transfer objek antar komponen
- Lebih efisien daripada Serializable
- `@Parcelize` otomatis generate boilerplate code

### Activity Result API
- API modern untuk komunikasi antar Activity
- Lebih type-safe dan lifecycle-aware
- Menggantikan `startActivityForResult()` dan `onActivityResult()`

### View Binding
- Alternatif modern dari `findViewById()`
- Type-safe dan null-safe
- Compile-time checking

## 📂 Struktur File

```
app/src/main/java/com/dicoding/mysharedpreferences/
├── UserModel.kt                    # Model data user
├── UserPreference.kt               # Helper SharedPreferences
├── MainActivity.kt                 # Activity utama (tampil data)
└── FormUserPreferenceActivity.kt   # Activity form (input/edit)
```

## 🚀 Cara Kerja

### Flow Aplikasi

1. **Pertama Kali Buka Aplikasi**
   ```
   MainActivity onCreate
   → UserPreference.getUser()
   → SharedPreferences kosong, return UserModel dengan default values
   → Tampilkan "Tidak Ada" untuk semua field
   → Tombol menampilkan "Simpan"
   ```

2. **Klik Tombol Simpan (Data Kosong)**
   ```
   onClick → isPreferenceEmpty = true
   → Launch FormActivity dengan TYPE_ADD
   → Form kosong siap diisi
   ```

3. **Isi Form & Save**
   ```
   Input data → Validasi semua field
   → UserPreference.setUser(userModel)
   → SharedPreferences.edit{ putString/putInt/putBoolean }
   → Kirim result ke MainActivity
   → MainActivity update UI dengan data baru
   ```

4. **Buka Aplikasi Lagi (Data Sudah Ada)**
   ```
   MainActivity onCreate
   → UserPreference.getUser()
   → SharedPreferences ada data, return UserModel terisi
   → Tampilkan data user
   → Tombol menampilkan "Ubah"
   ```

5. **Klik Tombol Ubah**
   ```
   onClick → isPreferenceEmpty = false
   → Launch FormActivity dengan TYPE_EDIT
   → Form terisi dengan data lama
   → User edit data → Save → Update SharedPreferences
   ```

## 🔍 Validasi Input

Form melakukan validasi berikut:
- ✅ Nama: tidak boleh kosong
- ✅ Email: tidak boleh kosong & harus format valid
- ✅ Umur: tidak boleh kosong
- ✅ Nomor Telepon: tidak boleh kosong & hanya angka
- ✅ Preferensi MU: RadioButton (Ya/Tidak)

## 💾 Data yang Disimpan

SharedPreferences menyimpan data dengan key:
- `name` → String
- `email` → String
- `age` → Int
- `phone` → String
- `islove` → Boolean

File SharedPreferences: `user_pref.xml` di `/data/data/com.dicoding.mysharedpreferences/shared_prefs/`

## 🛠️ Teknologi & Library

- **Kotlin**: Bahasa pemrograman utama
- **View Binding**: Akses view yang type-safe
- **SharedPreferences**: Penyimpanan data lokal
- **Parcelize**: Serialization objek untuk Intent
- **Activity Result API**: Komunikasi antar Activity
- **Android Patterns**: Validasi format email

## 📱 Minimum Requirements

- Android SDK: 21 (Android 5.0 Lollipop)
- Target SDK: 34 (Android 14)
- Kotlin: 1.9+

## 🎯 Tujuan Pembelajaran

Proyek ini mengajarkan:
1. ✅ Cara menggunakan SharedPreferences untuk menyimpan data
2. ✅ Komunikasi antar Activity dengan Intent & Result API
3. ✅ Validasi input form
4. ✅ Parcelable untuk transfer objek
5. ✅ View Binding untuk akses view
6. ✅ Separation of concerns dengan helper class
7. ✅ Backward compatibility dengan version checking
8. ✅ State management dengan boolean flag

## 📝 Catatan Penting

### Kapan Menggunakan SharedPreferences?
**✅ Gunakan untuk:**
- Pengaturan aplikasi (dark mode, notifikasi, dll)
- Token autentikasi sederhana
- Preferensi user
- Data konfigurasi kecil
- Boolean flags

**❌ Jangan gunakan untuk:**
- Data kompleks/relasional (gunakan Room)
- Data besar (gunakan Database)
- Data sensitif tanpa enkripsi (gunakan EncryptedSharedPreferences)
- File atau gambar (gunakan Internal/External Storage)

### Best Practices
1. **Gunakan konstanta untuk key** (menghindari typo)
2. **Sediakan default values** (mencegah null)
3. **Buat helper class** (centralize logic)
4. **Gunakan apply() bukan commit()** (lebih cepat, asynchronous)
5. **Jangan simpan data sensitif** tanpa enkripsi

## 📚 Referensi

- [Android SharedPreferences Documentation](https://developer.android.com/reference/android/content/SharedPreferences)
- [Data and file storage overview](https://developer.android.com/training/data-storage)
- [Activity Result API](https://developer.android.com/training/basics/intents/result)
- [Parcelable and Bundle](https://developer.android.com/guide/components/activities/parcelables-and-bundles)

## 👨‍💻 Author

Dicoding Academy - Android Learning Path

---

**Happy Coding! 🚀**

