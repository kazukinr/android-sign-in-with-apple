package com.github.kazukinr.android.signinwithapple.internal.webview

import android.webkit.WebView
import com.github.kazukinr.android.signinwithapple.SignInWithAppleRequest
import com.github.kazukinr.android.signinwithapple.SignInWithAppleResult

interface SignInWithAppleWebViewClient {

    val request: SignInWithAppleRequest
    val callback: Callback

    interface Callback {

        fun onSuccess(result: SignInWithAppleResult)

        fun onError(code: Int, message: String)

        fun onCancel()
    }
}