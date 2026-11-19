@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.example.animelibrary

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimeLibraryScreen()
                }
            }
        }
    }
}

@Composable
fun AnimeLibraryScreen(vm: AnimeViewModel = viewModel()) {
    val context = LocalContext.current
    val selected by vm.selected.collectAsState()
    val animeList by vm.animeList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Mi") })
                TabRow(selectedTabIndex = selected.ordinal) {
                    Category.entries.forEach { category ->
                        Tab(
                            selected = category == selected,
                            onClick = { vm.select(category) },
                            text = { Text(category.label, maxLines = 1) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add anime")
            }
        }
    ) { padding ->
        if (animeList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nothing here yet. Tap + to add one.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(animeList, key = { it.id }) { anime ->
                    AnimePosterCard(
                        anime = anime,
                        onOpen = { openLink(context, anime.watchUrl) },
                        onMove = { vm.move(anime, it) },
                        onDelete = { vm.remove(anime) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddAnimeDialog(
            defaultCategory = selected,
            onDismiss = { showAddDialog = false },
            onSave = { title, poster, watch, category ->
                vm.add(title, poster, watch, category)
                showAddDialog = false
            }
        )
    }
}

fun openLink(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Couldn't open that link", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AnimePosterCard(
    anime: Anime,
    onOpen: () -> Unit,
    onMove: (Category) -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onOpen,
                    onLongClick = { menuOpen = true }
                ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column {
                // The title sits behind the image, so a blank or broken poster URL
                // still shows a readable placeholder.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = anime.title,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                    if (anime.posterUrl.isNotBlank()) {
                        AsyncImage(
                            model = anime.posterUrl,
                            contentDescription = anime.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Text(
                    text = anime.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            Category.entries.filter { it != anime.category }.forEach { target ->
                DropdownMenuItem(
                    text = { Text("Move to ${target.label}") },
                    onClick = {
                        menuOpen = false
                        onMove(target)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    menuOpen = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
fun AddAnimeDialog(
    defaultCategory: Category,
    onDismiss: () -> Unit,
    onSave: (title: String, posterUrl: String, watchUrl: String, category: Category) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var posterUrl by remember { mutableStateOf("") }
    var watchUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(defaultCategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add anime") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = posterUrl,
                    onValueChange = { posterUrl = it },
                    label = { Text("Poster image URL (optional)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = watchUrl,
                    onValueChange = { watchUrl = it },
                    label = { Text("Watch link (Bilibili, etc.)") },
                    singleLine = true
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Category.entries.forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c.label, maxLines = 1) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && watchUrl.isNotBlank()) {
                        onSave(title, posterUrl, watchUrl, category)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
