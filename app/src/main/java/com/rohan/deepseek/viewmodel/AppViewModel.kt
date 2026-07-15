package com.rohan.deepseek.viewmodel

import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rohan.deepseek.cache.CacheDatabase
import com.rohan.deepseek.cache.CacheEntry
import com.rohan.deepseek.cache.CacheManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db  = CacheDatabase.getInstance(application)
    private val dao = db.cacheDao()
    val cacheManager = CacheManager(application)

    /** Kept alive across navigation — prevents WebView reload when returning to Chat tab. */
    var webView: WebView? = null

    val cacheEntries: StateFlow<List<CacheEntry>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalCacheSize: StateFlow<Long> = dao.observeTotalSize()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val cacheCount: StateFlow<Int> = dao.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _triggerRefresh = MutableStateFlow(false)
    val triggerRefresh: StateFlow<Boolean> = _triggerRefresh

    fun deleteEntry(entry: CacheEntry) = viewModelScope.launch {
        cacheManager.deleteEntry(entry)
    }

    fun clearAll() = viewModelScope.launch {
        cacheManager.clearAll()
        _triggerRefresh.value = true
    }

    fun refreshCache() = viewModelScope.launch {
        cacheManager.clearAll()
        _triggerRefresh.value = true
    }

    fun onRefreshConsumed() {
        _triggerRefresh.value = false
    }

    override fun onCleared() {
        super.onCleared()
        webView?.destroy()
        webView = null
    }
}
