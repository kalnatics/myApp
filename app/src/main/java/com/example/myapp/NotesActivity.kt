package com.example.myapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.adapters.NotesAdapter
import com.example.myapp.api.RetrofitClient
import com.example.myapp.models.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var notesAdapter: NotesAdapter
    private var notesList: MutableList<Note> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        recyclerView = findViewById(R.id.recyclerViewNotes)
        fabAddNote = findViewById(R.id.fabAddNote)

        setupRecyclerView() // Panggil setup di sini
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notesAdapter

        fabAddNote.setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java))
        }

        fetchNotes()
    }

    private fun fetchNotes() {
        RetrofitClient.instance.getNotes().enqueue(object : Callback<List<Note>> {
            override fun onResponse(call: Call<List<Note>>, response: Response<List<Note>>) {
                if (response.isSuccessful) {
                    notesList.clear()
                    response.body()?.let { notesList.addAll(it) }
                    notesAdapter.notifyDataSetChanged()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@NotesActivity,
                        "Error: ${response.code()} - $errorBody",
                        Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Note>>, t: Throwable) {
                Log.e("API_ERROR", "Error: ${t.message}", t)
                Toast.makeText(this@NotesActivity,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            notesList,
            onItemClick = { note ->
                // Buka DetailNoteActivity saat item diklik
                val intent = Intent(this, DetailNoteActivity::class.java).apply {
                    putExtra("note_id", note.id)
                    putExtra("note_title", note.title)
                    putExtra("note_content", note.content)
                }
                startActivity(intent)
            },
            onDeleteClick = { note ->
                showDeleteConfirmation(note)
            }
        )
    }
    fun showDeleteConfirmation(note: Note) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin menghapus catatan ini?")
            .setPositiveButton("Ya") { dialog, _ ->
                deleteNote(note)
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss() // Menutup dialog, tidak keluar aplikasi
            }
            .show()
    }

    private fun deleteNote(note: Note) {
        RetrofitClient.instance.deleteNote(note.id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    notesList.remove(note)
                    notesAdapter.notifyDataSetChanged()
                    Toast.makeText(this@NotesActivity,
                        "Catatan berhasil dihapus",
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@NotesActivity,
                    "Gagal menghapus catatan",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
    override fun onResume() {
        super.onResume()
        fetchNotes()
    }
}

class DetailNoteActivity : AppCompatActivity() {
    private lateinit var etDetailTitle: EditText
    private lateinit var etDetailContent: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private var noteId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_note)

        etDetailTitle = findViewById(R.id.etDetailTitle)
        etDetailContent = findViewById(R.id.etDetailContent)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)

        // Ambil data dari intent
        noteId = intent.getIntExtra("note_id", 0)
        etDetailTitle.setText(intent.getStringExtra("note_title"))
        etDetailContent.setText(intent.getStringExtra("note_content"))

        btnUpdate.setOnClickListener {
            updateNote()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun updateNote() {
        val title = etDetailTitle.text.toString().trim()
        val content = etDetailContent.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Judul dan konten tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.instance.updateNote(noteId, title, content)
            .enqueue(object : Callback<Note> {
                override fun onResponse(call: Call<Note>, response: Response<Note>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DetailNoteActivity,
                            "Catatan berhasil diupdate",
                            Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<Note>, t: Throwable) {
                    Toast.makeText(this@DetailNoteActivity,
                        "Gagal mengupdate catatan",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus catatan ini?")
            .setPositiveButton("Ya") { dialog, _ ->
                deleteNote()
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteNote() {
        RetrofitClient.instance.deleteNote(noteId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DetailNoteActivity,
                            "Catatan berhasil dihapus",
                            Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@DetailNoteActivity,
                        "Gagal menghapus catatan",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }
}