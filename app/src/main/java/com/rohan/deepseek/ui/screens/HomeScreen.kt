package com.rohan.deepseek.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.rohan.deepseek.cache.CacheManager
import com.rohan.deepseek.viewmodel.AppViewModel

private const val DEEPSEEK_URL = "https://chat.deepseek.com"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HomeScreen(vm: AppViewModel) {
    val context        = LocalContext.current
    val triggerRefresh by vm.triggerRefresh.collectAsState()
    // isLoading starts false when returning to a cached WebView (page already loaded)
    var isLoading by remember { mutableStateOf(vm.webView == null) }

    LaunchedEffect(triggerRefresh) {
        if (triggerRefresh) {
            vm.webView?.reload()
            vm.onRefreshConsumed()
        }
    }

    BackHandler {
        vm.webView?.let { if (it.canGoBack()) it.goBack() }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory  = { ctx ->
                // Return cached WebView instance — detach from any previous parent first
                val cached = vm.webView
                if (cached != null) {
                    (cached.parent as? ViewGroup)?.removeView(cached)
                    cached
                } else {
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled                = true
                            domStorageEnabled                = true
                            databaseEnabled                  = true
                            loadWithOverviewMode             = true
                            useWideViewPort                  = true
                            builtInZoomControls              = false
                            displayZoomControls              = false
                            setSupportMultipleWindows(true)
                            mixedContentMode                 = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode                        = WebSettings.LOAD_CACHE_ELSE_NETWORK
                            userAgentString                  = CacheManager.CHROME_UA
                            allowContentAccess               = true
                            allowFileAccess                  = true
                            mediaPlaybackRequiresUserGesture = false

                            // Pass system dark-mode preference to the website so DeepSeek
                            // picks up the device theme via prefers-color-scheme
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                @Suppress("DEPRECATION")
                                forceDark = WebSettings.FORCE_DARK_AUTO
                            }
                        }
                        loadUrl(DEEPSEEK_URL)
                    }.also { vm.webView = it }
                }
            },
            // update runs on every recomposition — install a fresh WebViewClient so
            // the loading-state callbacks close over the current mutableState refs.
            update  = { wv ->
                wv.webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? = vm.cacheManager.intercept(request)

                    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                        isLoading = true
                    }

                    override fun onPageFinished(view: WebView, url: String?) {
                        isLoading = false
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val url = request.url.toString()
                        return if (url.startsWith("https://chat.deepseek.com") ||
                                   url.startsWith("https://deepseek.com")) {
                            false
                        } else {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                            true
                        }
                    }
                }
            }
        )

        // Slim 2dp progress bar at top — visible only while loading
        AnimatedVisibility(
            visible  = isLoading,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            LinearProgressIndicator(
                modifier   = Modifier.fillMaxWidth().height(2.dp),
                color      = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
