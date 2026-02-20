package com.dicoding.mysharedpreferences

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dicoding.mysharedpreferences.databinding.ActivityFormUserPreferenceBinding
import androidx.core.text.isDigitsOnly

class FormUserPreferenceActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        // Konstanta untuk komunikasi antar Activity via Intent
        const val EXTRA_TYPE_FORM = "extra_type_form"
        const val EXTRA_RESULT = "extra_result"
        const val RESULT_CODE = 101

        // Tipe form: TYPE_ADD untuk form tambah baru, TYPE_EDIT untuk form ubah data
        // Digunakan untuk menentukan perilaku dan tampilan form
        const val TYPE_ADD = 1
        const val TYPE_EDIT = 2

        // Konstanta pesan error untuk validasi input
        // Dipisahkan agar mudah diubah atau ditranslate ke bahasa lain
        private const val FIELD_REQUIRED = "Field tidak boleh kosong"
        private const val FIELD_DIGIT_ONLY = "Hanya boleh terisi numerik"
        private const val FIELD_IS_NOT_VALID = "Email tidak valid"
    }

    private lateinit var userModel: UserModel

    private lateinit var binding: ActivityFormUserPreferenceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFormUserPreferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnSave.setOnClickListener(this)

        // Ambil data UserModel dari Intent yang dikirim MainActivity
        // Menggunakan cara berbeda untuk Android 13+ dan versi sebelumnya
        userModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Untuk Android 13 ke atas menggunakan getParcelableExtra dengan parameter Class
            // Diperlukan karena perubahan API untuk keamanan tipe data
            intent.getParcelableExtra("USER", UserModel::class.java) as UserModel
        } else {
            // Untuk Android 12 ke bawah menggunakan cara lama
            @Suppress("DEPRECATION") intent.getParcelableExtra<UserModel>("USER") as UserModel
        }

        // Ambil tipe form dari Intent (ADD atau EDIT)
        val formType = intent.getIntExtra(EXTRA_TYPE_FORM, 0)

        var actionBarTitle = ""
        var btnTitle = ""

        // Sesuaikan tampilan berdasarkan tipe form
        when (formType) {
            TYPE_ADD -> {
                // Mode tambah baru: form kosong, tombol "Simpan"
                actionBarTitle = "Tambah Baru"
                btnTitle = "Simpan"
            }

            TYPE_EDIT -> {
                // Mode ubah: form terisi data lama, tombol "Update"
                actionBarTitle = "Ubah"
                btnTitle = "Update"
                // Tampilkan data yang sudah ada ke dalam form
                showPreferenceInForm()
            }
        }

        supportActionBar?.title = actionBarTitle
        // Aktifkan tombol back di ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSave.text = btnTitle
    }

    // Fungsi untuk menampilkan data user yang sudah ada ke dalam form EditText
    private fun showPreferenceInForm() {
        binding.edtName.setText(userModel.name)
        binding.edtEmail.setText(userModel.email)
        binding.edtAge.setText(userModel.age.toString())
        binding.edtPhone.setText(userModel.phoneNumber)

        // Set RadioButton sesuai preferensi isLove
        if (userModel.isLove) {
            binding.rbYes.isChecked = true
        } else {
            binding.rbNo.isChecked = true
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_save) {
            // Ambil input dari EditText dan trim whitespace untuk menghindari input spasi kosong
            val name = binding.edtName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val age = binding.edtAge.text.toString().trim()
            val phoneNo = binding.edtPhone.text.toString().trim()
            // Cek RadioButton mana yang dipilih untuk menentukan nilai boolean
            val isLoveMU = binding.rgLoveMu.checkedRadioButtonId == R.id.rb_yes

            // Validasi input: nama tidak boleh kosong
            if (name.isEmpty()) {
                binding.edtName.error = FIELD_REQUIRED
                return // Stop eksekusi jika validasi gagal
            }

            // Validasi input: email tidak boleh kosong
            if (email.isEmpty()) {
                binding.edtEmail.error = FIELD_REQUIRED
                return
            }

            // Validasi input: email harus format yang valid
            if (!isValidEmail(email)) {
                binding.edtEmail.error = FIELD_IS_NOT_VALID
                return
            }

            // Validasi input: umur tidak boleh kosong
            if (age.isEmpty()) {
                binding.edtAge.error = FIELD_REQUIRED
                return
            }

            // Validasi input: nomor telepon tidak boleh kosong
            if (phoneNo.isEmpty()) {
                binding.edtPhone.error = FIELD_REQUIRED
                return
            }

            // Validasi input: nomor telepon harus berisi angka saja
            if (!phoneNo.isDigitsOnly()) {
                binding.edtPhone.error = FIELD_DIGIT_ONLY
                return
            }

            // Jika semua validasi berhasil, simpan data ke SharedPreferences
            saveUser(name, email, age, phoneNo, isLoveMU)

            // Kirim data kembali ke MainActivity agar tampilan terupdate
            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_RESULT, userModel)
            setResult(RESULT_CODE, resultIntent)

            // Tutup activity dan kembali ke MainActivity
            finish()
        }
    }

    // Fungsi untuk menyimpan data user ke SharedPreferences
    private fun saveUser(name: String, email: String, age: String, phoneNo: String, isLoveMU: Boolean) {
        val userPreference = UserPreference(this)

        // Update userModel dengan data baru dari form
        userModel.name = name
        userModel.email = email
        userModel.age = Integer.parseInt(age)
        userModel.phoneNumber = phoneNo
        userModel.isLove = isLoveMU

        // Simpan ke SharedPreferences melalui UserPreference
        userPreference.setUser(userModel)
        Toast.makeText(this, "Data Tersimpan", Toast.LENGTH_SHORT).show()
    }

    // Fungsi untuk validasi format email menggunakan Android Patterns
    // Return true jika email valid, false jika tidak valid
    private fun isValidEmail(email: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Handle klik tombol back di ActionBar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Tutup activity saat tombol back ditekan
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}