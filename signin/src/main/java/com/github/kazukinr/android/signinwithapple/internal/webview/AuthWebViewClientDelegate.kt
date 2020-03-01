package com.github.kazukinr.android.signinwithapple.internal.webview

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebView
import com.github.kazukinr.android.signinwithapple.SignInWithAppleRequest
import com.github.kazukinr.android.signinwithapple.SignInWithAppleResult

internal interface AuthWebViewClientDelegate {

    val request: SignInWithAppleRequest

    var callback: Callback?

    fun register(webView: WebView)

    fun handleUrlLoading(webView: WebView, url: Uri): Boolean

    fun handlePageStarted(webView: WebView, url: String, favicon: Bitmap?): Boolean


    interface Callback {

        fun onSuccess(result: SignInWithAppleResult)

        fun onError(code: Int, message: String)

        fun onCancel()
    }

    companion object {

        fun create(request: SignInWithAppleRequest): AuthWebViewClientDelegate =
            if (request.isQueryResponseType) {
                AuthWebViewClientDelegateForQuery(request)
            } else {
                AuthWebViewClientDelegateForFormPost(request)
            }
    }
}
