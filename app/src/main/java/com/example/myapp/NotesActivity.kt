package com.example.myapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.adapters.NotesAdapter
import com.example.myapp.models.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotesActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var notesList: MutableList<Note>
    private lateinit var arrayAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        listView = findViewById(R.id.listViewNotes)
        fabAddNote = findViewById(R.id.fabAddNote)
        dbHelper = DatabaseHelper(this)

        fabAddNote.setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java))
        }

        loadNotes()

        listView.setOnItemClickListener { _, _, position, _ ->
            val note = notesList[position]
            val intent = Intent(this, DetailNoteActivity::class.java).apply {
                putExtra("note_id", note.id)
                putExtra("note_title", note.title)
                putExtra("note_content", note.content)
            }
            startActivity(intent)
        }

        // Menambahkan LongPress Listener untuk hapus
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val note = notesList[position]
            showDeleteConfirmation(note)
            true // Return true to indicate that the long click was handled
        }
    }

    private fun loadNotes() {
        notesList = dbHelper.getAllNotes().toMutableList()
        val titles = notesList.map { it.title } // Hanya menampilkan judul di ListView
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, titles)
        listView.adapter = arrayAdapter
    }

    private fun showDeleteConfirmation(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus catatan '${note.title}'?")
            .setPositiveButton("Ya") { _, _ -> deleteNote(note.id) }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun deleteNote(noteId: Int) {
        val success = dbHelper.deleteNote(noteId)
        if (success) {
            Toast.makeText(this, "Catatan berhasil dihapus", Toast.LENGTH_SHORT).show()
            loadNotes() // Refresh list setelah penghapusan
        } else {
            Toast.makeText(this, "Gagal menghapus catatan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }
}


class DetailNoteActivity : AppCompatActivity() {
    private lateinit var etDetailTitle: EditText
    private lateinit var etDetailContent: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private var noteId: Int = 0
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_note)

        etDetailTitle = findViewById(R.id.etDetailTitle)
        etDetailContent = findViewById(R.id.etDetailContent)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        dbHelper = DatabaseHelper(this)

        // Ambil data dari intent
        noteId = intent.getIntExtra("note_id", 0)
        etDetailTitle.setText(intent.getStringExtra("note_title"))
        etDetailContent.setText(intent.getStringExtra("note_content"))

        btnUpdate.setOnClickListener { updateNote() }
        btnDelete.setOnClickListener { confirmDelete() }
    }

    private fun updateNote() {
        val title = etDetailTitle.text.toString().trim()
        val content = etDetailContent.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Judul dan isi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val note = Note(noteId, title, content)
        val success = dbHelper.updateNote(note)

        if (success) {
            Toast.makeText(this, "Catatan berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal memperbarui catatan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus catatan ini?")
            .setPositiveButton("Ya") { _, _ -> deleteNote() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun deleteNote() {
        val success = dbHelper.deleteNote(noteId)
        if (success) {
            Toast.makeText(this, "Catatan berhasil dihapus", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal menghapus catatan", Toast.LENGTH_SHORT).show()
        }
    }
}