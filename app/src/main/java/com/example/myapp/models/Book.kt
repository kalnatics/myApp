package com.example.myapp.models

data class Book(
    val id: Int = 0, // Auto Increment di database
    val nama: String,
    val namaPanggilan: String,
    val photoUri: String, // URI gambar dari kamera/galeri
    val email: String,
    val alamat: String?,
    val tanggalLahir: String, // Disimpan sebagai format "yyyy-MM-dd"
    val noHp: String
)
