package com.dicoding.mysharedpreferences

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Anotasi @Parcelize untuk otomatis generate kode Parcelable
// Diperlukan agar objek UserModel bisa dikirim antar Activity melalui Intent
@Parcelize
data class UserModel(
    // Properti nullable (?) karena data awal mungkin kosong sebelum user mengisi form
    var name: String? = null,
    var email: String? = null,
    // Age menggunakan Int default 0 karena angka tidak bisa null
    var age: Int = 0,
    var phoneNumber: String? = null,
    // Boolean untuk menyimpan preferensi user, default false
    var isLove: Boolean = false
) : Parcelable // Implement Parcelable agar bisa di-serialize untuk Intent extras
