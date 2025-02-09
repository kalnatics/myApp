package com.example.myapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.api.RetrofitClient
import com.example.myapp.models.Note
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddNoteActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnSave = findViewById(R.id.btnSave)

        btnSave.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Judul dan konten tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.instance.addNote(title, content).enqueue(object : Callback<Note> {
            override fun onResponse(call: Call<Note>, response: Response<Note>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddNoteActivity, "Catatan berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke NotesActivity setelah sukses
                }
            }

            override fun onFailure(call: Call<Note>, t: Throwable) {
                Toast.makeText(this@AddNoteActivity, "Gagal menyimpan catatan", Toast.LENGTH_SHORT).show()
            }
        })
    }
}