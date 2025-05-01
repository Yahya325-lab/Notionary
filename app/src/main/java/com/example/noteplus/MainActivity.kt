package com.example.noteplus

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var etNote: EditText
    private lateinit var btnSummarize: Button
    private lateinit var tvSummary: TextView
    private lateinit var btnSave: Button
    private lateinit var rvNotes: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private val notes = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etNote = findViewById(R.id.etNote)
        btnSummarize = findViewById(R.id.btnSummarize)
        tvSummary = findViewById(R.id.tvSummary)
        btnSave = findViewById(R.id.btnSave)
        rvNotes = findViewById(R.id.rvNotes)

        notes.addAll(loadNotes())
        noteAdapter = NoteAdapter(notes) { position -> deleteNote(position) }
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = noteAdapter

        btnSave.setOnClickListener {
            val noteText = etNote.text.toString()
            if (noteText.isNotEmpty()) {
                val newNote = Note(notes.size + 1, noteText)
                notes.add(newNote)
                noteAdapter.notifyItemInserted(notes.size - 1)
                saveNotes()
                etNote.text.clear()
            }
        }

        btnSummarize.setOnClickListener {
            val noteText = etNote.text.toString()
            if (noteText.isNotEmpty()) {
                tvSummary.text = "Meringkas..."
                AIHelper.summarize(noteText) { summary ->
                    runOnUiThread {
                        tvSummary.text = summary
                    }
                }
            } else {
                tvSummary.text = "Masukkan catatan terlebih dahulu."
            }
        }
    }

    private fun saveNotes() {
        val json = Gson().toJson(notes)
        val file = File(filesDir, "notes.json")
        file.writeText(json)
    }

    private fun loadNotes(): List<Note> {
        val file = File(filesDir, "notes.json")
        if (!file.exists()) return emptyList()

        val json = file.readText()
        val type = object : TypeToken<List<Note>>() {}.type
        return Gson().fromJson(json, type)
    }



    private fun deleteNote(position: Int) {
        notes.removeAt(position)
        noteAdapter.notifyItemRemoved(position)
        saveNotes()
    }
}
