package com.example.animelibrary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AnimeViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AnimeDatabase.getDatabase(app).animeDao()

    private val _selected = MutableStateFlow(Category.WATCHING)
    val selected: StateFlow<Category> = _selected

    @OptIn(ExperimentalCoroutinesApi::class)
    val animeList: StateFlow<List<Anime>> = _selected
        .flatMapLatest { dao.observeByCategory(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun select(category: Category) {
        _selected.value = category
    }

    fun add(title: String, posterUrl: String, watchUrl: String, category: Category) {
        viewModelScope.launch {
            dao.insert(
                Anime(
                    title = title.trim(),
                    posterUrl = normalizeUrl(posterUrl),
                    watchUrl = normalizeUrl(watchUrl),
                    category = category
                )
            )
        }
    }

    fun move(anime: Anime, to: Category) {
        viewModelScope.launch { dao.update(anime.copy(category = to)) }
    }

    fun remove(anime: Anime) {
        viewModelScope.launch { dao.delete(anime) }
    }
}

/** Adds https:// when the link has no scheme, so "bilibili.com/..." still opens. */
fun normalizeUrl(raw: String): String {
    val t = raw.trim()
    if (t.isEmpty()) return t
    return if ("://" in t) t else "https://$t"
}
