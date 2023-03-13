package com.example.todoapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todoapp.ui.theme.TodoAppTheme
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val lifecycle: LifecycleOwner = LocalLifecycleOwner.current
            CycleObserver(applicationContext, lifecycle)
            TodoAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoView()
                }
            }
        }
    }
}

@Composable
fun CycleObserver(ctx: Context, lifecycle: LifecycleOwner) {
    DisposableEffect(Unit) {
            val observer = LifecycleEventObserver { _, event ->
            when(event){
                Lifecycle.Event.ON_STOP  -> {
                    saveTodosToFile(ctx)
                }
                Lifecycle.Event.ON_CREATE -> {
                    getTodosFromFile(ctx)
                }
                else -> {
                    Log.d("Lifecycle", "Event: $event")
                }
            }
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose {
            lifecycle.lifecycle.removeObserver(observer)
        }
    }
}

var openAdd = mutableStateOf(false)
var openEdit = mutableStateOf(false)
var todoEdit = mutableStateOf(Todo(0, "", "", false))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoView() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAdd.value = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Filled.Add, "add")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Todo App")
                },
                colors = TopAppBarDefaults.smallTopAppBarColors (
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = { todos.clear() }
                    ) {
                        Icon(Icons.Filled.Delete, "delete", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) { p ->
        EditTodoDialog()
        AddTodoDialog()
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp, 70.dp, 10.dp, 0.dp)
        ) {
            items(todos.size) { index ->
                TodoItem(todos[index])
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItem(todo: Todo) {
    var text by rememberSaveable { mutableStateOf(
        if (todo.isDone) "Done" else "To do"
    ) }
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        onClick = {
            todoEdit.value = todo
            openEdit.value = true
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = todo.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 3.dp),
            )
            Text(
                text = todo.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 10.dp, 0.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        todo.isDone = !todo.isDone
                        text = if (todo.isDone) "Done" else "To do"
                    }
                ) {
                    Text(text = text)
                }
                IconButton(
                    onClick = { todos.remove(todo) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Filled.Delete, "delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoDialog() {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    if (openAdd.value) {
        AlertDialog(
            onDismissRequest = { openAdd.value = false },
            confirmButton = {
                Button (
                    onClick = {
                        openAdd.value = false
                        if (title.isNotEmpty() && description.isNotEmpty()) {
                            todos.add(Todo(
                                id = todos.size,
                                title = title,
                                description = description,
                                isDone = false
                            ))
                            title = ""
                            description = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(text = "Add")
                }
            },
            dismissButton = {
                Button(
                    onClick = { openAdd.value = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(text = "Cancel")
                }
            },
            title = {
                Text(text = "Add Todo")
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(text = "Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 0.dp, 8.dp)
                    )
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(text = "Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 0.dp, 8.dp)
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTodoDialog() {
    if (openEdit.value) {
        var title by rememberSaveable { mutableStateOf(todoEdit.value.title) }
        var description by rememberSaveable { mutableStateOf(todoEdit.value.description) }
        AlertDialog(
            onDismissRequest = { openEdit.value = false },
            confirmButton = {
                Button (
                    onClick = {
                        openEdit.value = false
                        if (title.isNotEmpty() && description.isNotEmpty()) {
                            todos[todoEdit.value.id] = Todo(
                                id = todoEdit.value.id,
                                title = title,
                                description = description,
                                isDone = todoEdit.value.isDone
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(text = "Add")
                }
            },
            dismissButton = {
                Button(
                    onClick = { openEdit.value = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(text = "Cancel")
                }
            },
            title = {
                Text(text = "Add Todo")
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(text = "Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 0.dp, 8.dp)
                    )
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(text = "Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 0.dp, 8.dp)
                    )
                }
            }
        )
    }
}
