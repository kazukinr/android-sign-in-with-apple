package com.github.kazukinr.android.signinwithapple

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

internal class SignInWithAppleWebViewClient(
    private val request: SignInWithAppleRequest,
    private val callback: (SignInWithAppleResult) -> Unit
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return isUrlOverridden(view, Uri.parse(url))
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return isUrlOverridden(view, request?.url)
    }

    private fun isUrlOverridden(view: WebView?, url: Uri?): Boolean =
        url?.toString()?.let {
            when {
                it.contains("appleid.apple.com") -> {
                    view?.loadUrl(it)
                    true
                }
                it.contains(request.redirectUri) -> {
                    val code = url.getQueryParameter("code")
                    val state = url.getQueryParameter("state")
                    val err = url.getQueryParameter("error")

                    when {
                        err == "user_cancelled_authorize" -> {
                            callback(SignInWithAppleResult.Cancel)
                        }
                        code == null -> {
                            callback(
                                SignInWithAppleResult.Failure(
                                    IllegalArgumentException("code is not returned")
                                )
                            )
                        }
                        state != request.state -> {
                            callback(
                                SignInWithAppleResult.Failure(
                                    IllegalArgumentException("state is not matched")
                                )
                            )
                        }
                        else -> {
                            callback(
                                SignInWithAppleResult.Success(
                                    code
                                )
                            )
                        }
                    }
                    true
                }
                else -> false
            }
        } ?: false
}
