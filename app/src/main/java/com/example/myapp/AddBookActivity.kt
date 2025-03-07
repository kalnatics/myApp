package com.example.myapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.models.Book
import java.io.ByteArrayOutputStream
import android.app.AlertDialog
import android.content.ContentValues
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import java.util.*

class AddBookActivity : AppCompatActivity() {
    private lateinit var etNama: EditText
    private lateinit var etNamaPanggilan: EditText
    private lateinit var ivPhoto: ImageView
    private lateinit var btnPickImage: Button
    private lateinit var etEmail: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etTglLahir: EditText
    private lateinit var etNoHp: EditText
    private lateinit var btnSave: Button
    private lateinit var databaseHelper: DatabaseHelper
    private var imageUri: Uri? = null

    companion object {
        private const val GALLERY_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
        private const val FILE_AUTHORITY = "com.example.myapp.fileprovider"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        databaseHelper = DatabaseHelper(this)

        etNama = findViewById(R.id.etNama)
        etNamaPanggilan = findViewById(R.id.etNamaPanggilan)
        ivPhoto = findViewById(R.id.ivPhoto)
        btnPickImage = findViewById(R.id.btnPickImage)
        etEmail = findViewById(R.id.etEmail)
        etAlamat = findViewById(R.id.etAlamat)
        etTglLahir = findViewById(R.id.etTglLahir)
        etNoHp = findViewById(R.id.etNoHp)
        btnSave = findViewById(R.id.btnSave)

        btnPickImage.setOnClickListener { showImagePickerDialog() }
        etTglLahir.setOnClickListener { showDatePicker() }
        btnSave.setOnClickListener {
            saveBook()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Kamera", "Galeri")
        AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Foto Buku Baru")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Dari Kamera")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            etTglLahir.setText(formattedDate)
        }, year, month, day)
        datePickerDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    if (data != null) {
                        imageUri = data.data
                        loadImageWithPicasso(imageUri)
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    // Kamera sudah menyimpan ke imageUri yang kita tentukan
                    loadImageWithPicasso(imageUri)
                }
            }
        }
    }

    private fun loadImageWithPicasso(uri: Uri?) {
        if (uri != null) {
            Picasso.get()
                .load(uri)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(ivPhoto)
        }
    }

    private fun saveBook() {
        val nama = etNama.text.toString().trim()
        val namaPanggilan = etNamaPanggilan.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val tanggalLahir = etTglLahir.text.toString().trim()
        val noHp = etNoHp.text.toString().trim()

        if (nama.isEmpty() || namaPanggilan.isEmpty() || email.isEmpty() || tanggalLahir.isEmpty() || noHp.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val book = Book(
            nama = nama,
            namaPanggilan = namaPanggilan,
            photoUri = imageUri.toString(),
            email = email,
            alamat = if (alamat.isEmpty()) null else alamat,
            tanggalLahir = tanggalLahir,
            noHp = noHp
        )

        if (databaseHelper.addBook(book)) {
            Toast.makeText(this, "Buku berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            // Kirim data ke MainActivity untuk update RecyclerView
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            Toast.makeText(this, "Gagal menambahkan buku!", Toast.LENGTH_SHORT).show()
        }
    }
}

// EditBookActivity.kt with Camera Support & Fixed Image Loading
class EditBookActivity : AppCompatActivity() {
    private lateinit var etEditNama: EditText
    private lateinit var etEditNamaPanggilan: EditText
    private lateinit var ivEditPhoto: ImageView
    private lateinit var btnEditPickImage: Button
    private lateinit var etEditEmail: EditText
    private lateinit var etEditAlamat: EditText
    private lateinit var etEditTglLahir: EditText
    private lateinit var etEditNoHp: EditText
    private lateinit var btnUpdateBook: Button
    private lateinit var databaseHelper: DatabaseHelper
    private var imageUri: Uri? = null
    private var bookId: Int = -1

    companion object {
        private const val GALLERY_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_book)

        databaseHelper = DatabaseHelper(this)

        etEditNama = findViewById(R.id.etEditNama)
        etEditNamaPanggilan = findViewById(R.id.etEditNamaPanggilan)
        ivEditPhoto = findViewById(R.id.ivEditPhoto)
        btnEditPickImage = findViewById(R.id.btnEditPickImage)
        etEditEmail = findViewById(R.id.etEditEmail)
        etEditAlamat = findViewById(R.id.etEditAlamat)
        etEditTglLahir = findViewById(R.id.etEditTglLahir)
        etEditNoHp = findViewById(R.id.etEditNoHp)
        btnUpdateBook = findViewById(R.id.btnUpdateBook)

        bookId = intent.getIntExtra("BOOK_ID", -1)

        loadBookData()

        btnEditPickImage.setOnClickListener { showImagePickerDialog() }
        etEditTglLahir.setOnClickListener { showDatePicker() }
        btnUpdateBook.setOnClickListener {
            updateBook()
        }
    }

    private fun loadBookData() {
        val book = databaseHelper.getBookById(bookId)

        if (book != null) {
            etEditNama.setText(book.nama)
            etEditNamaPanggilan.setText(book.namaPanggilan)
            etEditEmail.setText(book.email)
            etEditAlamat.setText(book.alamat ?: "")
            etEditTglLahir.setText(book.tanggalLahir)
            etEditNoHp.setText(book.noHp)

            // Set URI dan load gambar
            imageUri = Uri.parse(book.photoUri)
            loadImageWithPicasso(imageUri)

            // Log untuk debugging
            Log.d("EditBookActivity", "Loaded book with ID: $bookId")
            Log.d("EditBookActivity", "Photo URI: ${book.photoUri}")
        } else {
            Toast.makeText(this, "Buku tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Kamera", "Galeri")
        AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Foto Buku Edit")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Dari Kamera")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            etEditTglLahir.setText(formattedDate)
        }, year, month, day)
        datePickerDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    if (data != null) {
                        imageUri = data.data
                        loadImageWithPicasso(imageUri)
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    // Kamera sudah menyimpan ke imageUri yang kita tentukan
                    loadImageWithPicasso(imageUri)
                }
            }
        }
    }

    private fun loadImageWithPicasso(uri: Uri?) {
        if (uri != null) {
            Picasso.get()
                .load(uri)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(ivEditPhoto)
        }
    }

    private fun updateBook() {
        val nama = etEditNama.text.toString().trim()
        val namaPanggilan = etEditNamaPanggilan.text.toString().trim()
        val email = etEditEmail.text.toString().trim()
        val alamat = etEditAlamat.text.toString().trim()
        val tanggalLahir = etEditTglLahir.text.toString().trim()
        val noHp = etEditNoHp.text.toString().trim()

        if (nama.isEmpty() || namaPanggilan.isEmpty() || email.isEmpty() || tanggalLahir.isEmpty() || noHp.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val book = Book(
            id = bookId,
            nama = nama,
            namaPanggilan = namaPanggilan,
            photoUri = imageUri.toString(),
            email = email,
            alamat = if (alamat.isEmpty()) null else alamat,
            tanggalLahir = tanggalLahir,
            noHp = noHp
        )

        // Setelah Toast untuk sukses atau gagal
        if (databaseHelper.updateBook(book)) {
            Toast.makeText(this, "Buku berhasil diperbarui!", Toast.LENGTH_SHORT).show()

            // Set result untuk memastikan daftar diperbarui
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            Toast.makeText(this, "Gagal memperbarui buku!", Toast.LENGTH_SHORT).show()
        }
    }
}

class BookDetailActivity : AppCompatActivity() {

    private lateinit var ivBookDetailPhoto: ImageView
    private lateinit var tvBookDetailNama: TextView
    private lateinit var tvBookDetailNamaPanggilan: TextView
    private lateinit var tvBookDetailEmail: TextView
    private lateinit var tvBookDetailAlamat: TextView
    private lateinit var tvBookDetailTglLahir: TextView
    private lateinit var tvBookDetailNoHp: TextView
    private lateinit var btnEditBook: Button
    private lateinit var btnDeleteBook: Button

    private lateinit var databaseHelper: DatabaseHelper
    private var bookId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        databaseHelper = DatabaseHelper(this)

        ivBookDetailPhoto = findViewById(R.id.ivBookDetailPhoto)
        tvBookDetailNama = findViewById(R.id.tvBookDetailNama)
        tvBookDetailNamaPanggilan = findViewById(R.id.tvBookDetailNamaPanggilan)
        tvBookDetailEmail = findViewById(R.id.tvBookDetailEmail)
        tvBookDetailAlamat = findViewById(R.id.tvBookDetailAlamat)
        tvBookDetailTglLahir = findViewById(R.id.tvBookDetailTglLahir)
        tvBookDetailNoHp = findViewById(R.id.tvBookDetailNoHp)
        btnEditBook = findViewById(R.id.btnEditBook)
        btnDeleteBook = findViewById(R.id.btnDeleteBook)

        bookId = intent.getIntExtra("BOOK_ID", -1)
        loadBookDetails()

        val book = databaseHelper.getAllBooks().find { it.id == bookId }
        if (book != null) {
            Picasso.get().load(Uri.parse(book.photoUri)).into(ivBookDetailPhoto)
        }

        if (book != null) {
            tvBookDetailNama.text = book.nama
            tvBookDetailNamaPanggilan.text = book.namaPanggilan
            tvBookDetailEmail.text = book.email
            tvBookDetailAlamat.text = book.alamat ?: "Alamat tidak tersedia"
            tvBookDetailTglLahir.text = book.tanggalLahir
            tvBookDetailNoHp.text = book.noHp

            Picasso.get().load(Uri.parse(book.photoUri)).into(ivBookDetailPhoto)
        }

        btnEditBook.setOnClickListener {
            val intent = Intent(this, EditBookActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivityForResult(intent, 300)
        }

        btnDeleteBook.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin menghapus buku ini?")
                .setPositiveButton("Ya") { _, _ ->
                    databaseHelper.deleteBook(bookId)
                    Toast.makeText(this, "Buku berhasil dihapus", Toast.LENGTH_SHORT).show()

                    // Kirim hasil ke MainActivity agar daftar diperbarui
                    val resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }
    private fun loadBookDetails() {
        val book = databaseHelper.getAllBooks().find { it.id == bookId }
        if (book != null) {
            tvBookDetailNama.text = book.nama
            tvBookDetailNamaPanggilan.text = book.namaPanggilan
            tvBookDetailEmail.text = book.email
            tvBookDetailAlamat.text = book.alamat ?: "Alamat tidak tersedia"
            tvBookDetailTglLahir.text = book.tanggalLahir
            tvBookDetailNoHp.text = book.noHp
            Picasso.get().load(Uri.parse(book.photoUri)).into(ivBookDetailPhoto)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 300 && resultCode == Activity.RESULT_OK) {
            loadBookDetails() // Refresh data setelah kembali dari EditBookActivity
        }
    }
}
