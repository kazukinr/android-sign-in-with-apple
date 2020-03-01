package com.github.kazukinr.android.signinwithapple.internal.webview

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebView
import com.github.kazukinr.android.signinwithapple.SignInWithAppleRequest
import com.github.kazukinr.android.signinwithapple.SignInWithAppleResult
import com.github.kazukinr.android.signinwithapple.internal.ErrorInfo

internal class AuthWebViewClientDelegateForQuery(
    override val request: SignInWithAppleRequest
) : AuthWebViewClientDelegate {

    override var callback: AuthWebViewClientDelegate.Callback? = null

    override fun register(webView: WebView) {
        //nop
    }

    override fun handleUrlLoading(webView: WebView, url: Uri): Boolean {
        return url.toString().let {
            when {
                it.contains("appleid.apple.com") -> false
                it.contains(request.redirectUri) -> {
                    val code = url.getQueryParameter("code")
                    val state = url.getQueryParameter("state")
                    val err = url.getQueryParameter("error")

                    when {
                        err == "user_cancelled_authorize" -> {
                            callback?.onCancel()
                        }
                        code == null -> {
                            callback?.onError(ErrorInfo.NOT_FOUND, "code.not.found")
                        }
                        state != request.state -> {
                            callback?.onError(ErrorInfo.INTERNAL, "state.not.matched")
                        }
                        else -> {
                            callback?.onSuccess(SignInWithAppleResult(code = code))
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun handlePageStarted(webView: WebView, url: String, favicon: Bitmap?): Boolean {
        return false
    }
}