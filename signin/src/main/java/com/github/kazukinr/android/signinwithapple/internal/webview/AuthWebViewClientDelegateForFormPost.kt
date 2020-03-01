package com.github.kazukinr.android.signinwithapple.internal.webview

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.github.kazukinr.android.signinwithapple.SignInWithAppleRequest
import com.github.kazukinr.android.signinwithapple.SignInWithAppleResult
import com.github.kazukinr.android.signinwithapple.internal.ErrorInfo
import org.json.JSONException
import org.json.JSONObject

internal class AuthWebViewClientDelegateForFormPost(
    override val request: SignInWithAppleRequest
) : AuthWebViewClientDelegate {

    override var callback: AuthWebViewClientDelegate.Callback? = null

    private val jsCallback = JSCallback()

    override fun register(webView: WebView) {
        webView.addJavascriptInterface(jsCallback, "jsCallback")
    }

    override fun handleUrlLoading(webView: WebView, url: Uri): Boolean {
        return false
    }

    override fun handlePageStarted(webView: WebView, url: String, favicon: Bitmap?): Boolean {
        if (url.contains(request.redirectUri)) {
            // insert javascript into page to read post values
            buildString {
                append("javascript:jsCallback.onReadPostStarted();")
                append("var p=document.getElementsByTagName('input');")
                append("for(i=0;i < p.length;i++){")
                append("  jsCallback.onPostValueRead(p[i].getAttribute('name'),p[i].value);")
                append("}")
                append("jsCallback.onReadPostCompleted();")
            }.also {
                webView.loadUrl(it)
            }
            return true
        }

        return false
    }

    inner class JSCallback {

        private val postValues = mutableMapOf<String, String>()

        @JavascriptInterface
        fun onReadPostStarted() {
            postValues.clear()
        }

        @JavascriptInterface
        fun onPostValueRead(key: String, value: String) {
            postValues[key] = value
        }

        @JavascriptInterface
        fun onReadPostCompleted() {
            val code = postValues["code"]
            val idToken = postValues["id_token"]
            val state = postValues["state"]
            val userJson = postValues["user"]
            val error = postValues["error"]

            if (error == "user_cancelled_authorize") {
                callback?.onCancel()
                return
            }

            var result = SignInWithAppleResult()
            when (request.responseType) {
                SignInWithAppleRequest.ResponseType.CODE -> {
                    if (code == null) {
                        callback?.onError(ErrorInfo.NOT_FOUND, "code.not.found")
                        return
                    }
                    result = result.copy(code = code)
                }
                SignInWithAppleRequest.ResponseType.ID_TOKEN -> {
                    if (idToken == null) {
                        callback?.onError(ErrorInfo.NOT_FOUND, "id_token.not.found")
                        return
                    }
                    result = result.copy(idToken = idToken)
                }
                SignInWithAppleRequest.ResponseType.BOTH -> {
                    if (code == null) {
                        callback?.onError(ErrorInfo.NOT_FOUND, "code.not.found")
                        return
                    }
                    if (idToken == null) {
                        callback?.onError(ErrorInfo.NOT_FOUND, "id_token.not.found")
                        return
                    }
                    result = result.copy(code = code, idToken = idToken)
                }
            }

            if (request.scope != null) {
                if (userJson == null) {
                    callback?.onError(ErrorInfo.NOT_FOUND, "user.not.found")
                    return
                }

                try {
                    result = result.copy(user = parseUser(userJson))

                } catch (e: JSONException) {
                    callback?.onError(ErrorInfo.INTERNAL, "parse.user.failed")
                    return
                }
            }

            callback?.onSuccess(result)
        }

        @Throws(JSONException::class)
        private fun parseUser(userJson: String): SignInWithAppleResult.User {
            val json = JSONObject(userJson)

            var user = SignInWithAppleResult.User(email = json.getStringOrNull("email"))
            json.getJSONObjectOrNull("name")?.also {
                user = user.copy(
                    name = SignInWithAppleResult.User.Name(
                        firstName = it.getStringOrNull("firstName"),
                        lastName = it.getStringOrNull("lastName")
                    )
                )
            }
            return user
        }

        private fun JSONObject.getStringOrNull(name: String): String? =
            try {
                this.getString(name)
            } catch (e: JSONException) {
                null
            }

        private fun JSONObject.getJSONObjectOrNull(name: String): JSONObject? =
            try {
                this.getJSONObject(name)
            } catch (e: JSONException) {
                null
            }
    }
}