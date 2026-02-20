package com.dicoding.mysharedpreferences

import android.content.Context
import androidx.core.content.edit

// Class ini bertanggung jawab untuk mengelola data user dengan SharedPreferences
// Internal class agar hanya bisa diakses dalam module yang sama
internal class UserPreference(context: Context) {
    companion object {
        // Konstanta untuk nama file SharedPreferences dan key-key penyimpanan
        // Menggunakan konstanta agar konsisten dan menghindari typo
        private const val PREFS_NAME = "user_pref"
        private const val NAME = "name"
        private const val EMAIL = "email"
        private const val AGE = "age"
        private const val PHONE_NUMBER = "phone"
        private const val LOVE_MU = "islove"
    }

    // Inisialisasi SharedPreferences dengan MODE_PRIVATE agar hanya aplikasi ini yang bisa akses
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Fungsi untuk menyimpan seluruh data user ke SharedPreferences
    fun setUser(value: UserModel) {
        // Extension function edit{} otomatis memanggil commit/apply di akhir
        // Lebih efisien karena semua perubahan disimpan sekaligus dalam satu transaksi
        preferences.edit {
            putString(NAME, value.name)
            putString(EMAIL, value.email)
            putInt(AGE, value.age)
            putString(PHONE_NUMBER, value.phoneNumber)
            putBoolean(LOVE_MU, value.isLove)
        }
    }

    // Fungsi untuk mengambil data user dari SharedPreferences
    fun getUser(): UserModel {
        val model = UserModel()
        // Parameter kedua adalah default value jika key tidak ditemukan
        // Ini penting untuk menghindari null atau crash saat data belum pernah disimpan
        model.name = preferences.getString(NAME, "")
        model.email = preferences.getString(EMAIL, "")
        model.age = preferences.getInt(AGE, 0)
        model.phoneNumber = preferences.getString(PHONE_NUMBER, "")
        model.isLove = preferences.getBoolean(LOVE_MU, false)

        return model
    }
}