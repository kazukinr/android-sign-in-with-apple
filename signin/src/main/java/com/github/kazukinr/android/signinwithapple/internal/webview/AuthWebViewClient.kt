package com.github.kazukinr.android.signinwithapple.internal.webview

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

internal class AuthWebViewClient(
    private val delegate: AuthWebViewClientDelegate
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
        return delegate.handleUrlLoading(webView, Uri.parse(url))
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(webView: WebView, request: WebResourceRequest): Boolean {
        return delegate.handleUrlLoading(webView, request.url)
    }

    override fun onPageStarted(webView: WebView, url: String, favicon: Bitmap?) {
        if (delegate.handlePageStarted(webView, url, favicon)) {
            return
        }
        super.onPageStarted(webView, url, favicon)
    }
}