package com.example.myapp.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.models.Book
import com.squareup.picasso.Picasso

class BookAdapter(
    private val books: MutableList<Book>,
    private val onItemClick: (Book) -> Unit,
    private val onDeleteClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookImage: ImageView = itemView.findViewById(R.id.bookImage)
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        val bookSubtitle: TextView = itemView.findViewById(R.id.bookSubtitle)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bookTitle.text = book.nama
        holder.bookSubtitle.text = book.namaPanggilan

        // 🔹 Debugging: Cek URI gambar yang dimuat
        Log.d("BookAdapter", "Memuat gambar: ${book.photoUri}")

        // Load gambar menggunakan Picasso
        Picasso.get()
            .load(Uri.parse(book.photoUri))
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.bookImage)

        holder.itemView.setOnClickListener {
            onItemClick(book)
        }

        holder.btnDelete.setOnClickListener { onDeleteClick(book) }
    }

    override fun getItemCount(): Int = books.size

    fun removeBook(book: Book) {
        val position = books.indexOf(book)
        if (position != -1) {
            books.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateBooks(newBooks: List<Book>) {
        books.clear()
        books.addAll(newBooks)
        notifyDataSetChanged()
    }

}
