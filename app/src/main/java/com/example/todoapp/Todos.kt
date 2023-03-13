package com.example.todoapp

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.google.gson.Gson
import java.io.File

data class Todo(
    val id: Int,
    var title: String,
    var description: String,
    var isDone: Boolean,
)

val gson = Gson()
val todos = mutableStateListOf<Todo>()

fun saveTodosToFile(context: Context) {
    val json = gson.toJson(todos)
    val file = File(context.filesDir, "todos.json")
    file.delete()
    file.createNewFile()
    file.writeText(json)
}

fun getTodosFromFile(context: Context) {
    if (!File(context.filesDir, "todos.json").createNewFile()) {
        val json = File(context.filesDir, "todos.json").readText()
        todos.addAll(gson.fromJson(json, Array<Todo>::class.java).toList())
    }
}
