package com.dicoding.mysharedpreferences

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dicoding.mysharedpreferences.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mUserPreference: UserPreference

    // Flag untuk menandai apakah data preferensi masih kosong atau sudah terisi
    // Digunakan untuk menentukan apakah tombol akan menampilkan "Simpan" atau "Ubah"
    private var isPreferenceEmpty = false
    private lateinit var userModel: UserModel

    private lateinit var binding: ActivityMainBinding

    // Activity Result Launcher menggunakan API baru (menggantikan startActivityForResult yang deprecated)
    // Digunakan untuk menerima hasil dari FormUserPreferenceActivity
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        // Cek apakah ada data yang dikembalikan dan result code sesuai
        if (result.data != null && result.resultCode == FormUserPreferenceActivity.RESULT_CODE) {
            // Ambil data UserModel dari Intent result
            // Menggunakan cara berbeda tergantung versi Android untuk backward compatibility
            userModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Untuk Android 13+ menggunakan getParcelableExtra dengan parameter Class
                result.data?.getParcelableExtra(
                    FormUserPreferenceActivity.EXTRA_RESULT,
                    UserModel::class.java
                ) as UserModel
            } else {
                // Untuk Android 12 ke bawah menggunakan cara lama dengan @Suppress untuk warning
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra<UserModel>(FormUserPreferenceActivity.EXTRA_RESULT) as UserModel
            }

            // Update tampilan dengan data user terbaru
            populateView(userModel)
            // Perbarui status form (apakah tombol menampilkan "Simpan" atau "Ubah")
            checkForm(userModel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Aktifkan edge-to-edge display untuk tampilan fullscreen modern
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Set padding agar konten tidak tertutup status bar/navigation bar
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.title = "My User Preference"

        // Inisialisasi UserPreference untuk akses ke SharedPreferences
        mUserPreference = UserPreference(this)

        // Tampilkan data yang sudah tersimpan sebelumnya (jika ada)
        showExistingPreference()

        binding.btnSave.setOnClickListener(this)
    }

    private fun showExistingPreference() {
        // Ambil data user dari SharedPreferences
        userModel = mUserPreference.getUser()
        populateView(userModel)
        checkForm(userModel)
    }

    // Fungsi untuk menampilkan data user ke UI
    private fun populateView(userModel: UserModel) {
        // ifEmpty digunakan untuk menampilkan "Tidak Ada" jika data kosong
        // Penting untuk user experience agar tidak menampilkan TextView kosong
        binding.tvName.text = userModel.name.toString().ifEmpty { "Tidak Ada" }
        binding.tvAge.text = userModel.age.toString().ifEmpty { "Tidak Ada" }
        // Konversi boolean ke teks untuk ditampilkan
        binding.tvIsLoveMu.text = if (userModel.isLove) "Ya" else "Tidak"
        binding.tvEmail.text = userModel.email.toString().ifEmpty { "Tidak Ada" }
        binding.tvPhone.text =
            userModel.phoneNumber.toString().ifEmpty { "Tidak Ada" }
    }

    // Fungsi untuk mengecek apakah form sudah terisi atau masih kosong
    // Menentukan label tombol dan state aplikasi
    private fun checkForm(userModel: UserModel) {
        when {
            userModel.name.toString().isNotEmpty() -> {
                // Jika name tidak kosong, berarti data sudah ada, tombol jadi "Ubah"
                binding.btnSave.text = getString(R.string.change)
                isPreferenceEmpty = false
            }

            else -> {
                // Jika name kosong, berarti belum ada data, tombol jadi "Simpan"
                binding.btnSave.text = getString(R.string.save)
                isPreferenceEmpty = true
            }
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_save) {
            val intent = Intent(this@MainActivity, FormUserPreferenceActivity::class.java)
            when {
                isPreferenceEmpty -> {
                    // Jika data masih kosong, buka form dalam mode "Tambah Baru"
                    intent.putExtra(
                        FormUserPreferenceActivity.EXTRA_TYPE_FORM,
                        FormUserPreferenceActivity.TYPE_ADD
                    )
                    intent.putExtra("USER", userModel)
                }
                else -> {
                    // Jika data sudah ada, buka form dalam mode "Ubah"
                    // Form akan otomatis terisi dengan data yang sudah ada
                    intent.putExtra(
                        FormUserPreferenceActivity.EXTRA_TYPE_FORM,
                        FormUserPreferenceActivity.TYPE_EDIT
                    )
                    intent.putExtra("USER", userModel)
                }
            }
            // Launch activity dengan result launcher untuk menunggu hasil input user
            resultLauncher.launch(intent)
        }
    }
}