package com.github.kazukinr.android.signinwithapple.internal.webview

import android.graphics.Bitmap
import android.webkit.*
import com.github.kazukinr.android.signinwithapple.SignInWithAppleRequest
import com.github.kazukinr.android.signinwithapple.SignInWithAppleResult
import com.github.kazukinr.android.signinwithapple.internal.ErrorInfo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SignInWithAppleWebViewClientForPost(
    override val request: SignInWithAppleRequest,
    override val callback: SignInWithAppleWebViewClient.Callback,
    webView: WebView
) : WebViewClient(), SignInWithAppleWebViewClient {

    private val recorder = PayloadRecorder()

    init {
        webView.addJavascriptInterface(recorder, "recorder")
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        if (url.contains("appleid.apple.com/auth/authorize")) {
            // inject javascript to intercept post payload
            view.context.resources.assets.open("intercept.js")
                .reader()
                .readText()
                .replace("\${redirect_uri}", request.redirectUri)
                .also {
                    view.evaluateJavascript(it, null)
                }
        }
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest)
            : WebResourceResponse? {
        if (request.url.toString().contains(this.request.redirectUri)) {
            handleRedirectParam()
            return WebResourceResponse(
                "text/html",
                "utf-8",
                view.context.resources.assets.open("complete.html")
            )
        }

        return super.shouldInterceptRequest(view, request)
    }

    private fun handleRedirectParam() {
        val payloadJson = recorder.redirectParams
        if (payloadJson == null) {
            callback.onError(ErrorInfo.NOT_FOUND, "redirected.params.not.found")
            return
        }

        try {
            val params = mutableMapOf<String, String?>()
            val jsonArray = JSONArray(payloadJson)
            for (i in 0 until jsonArray.length() - 1) {
                val data = jsonArray.getJSONObject(i)
                data.getStringOrNull("name")?.also { name ->
                    params[name] = data.getStringOrNull("value")
                }
            }

            params["error"]?.also {
                if (it == "user_cancelled_authorize") {
                    callback.onCancel()
                } else {
                    callback.onError(ErrorInfo.INTERNAL, it)
                }
                return
            }

            val code = params["code"]
            val idToken = params["id_token"]
            val state = params["state"]
            val user = params["user"]?.let { parseUser(it) }

            if (code == null && request.isCodeRequested) {
                callback.onError(ErrorInfo.NOT_FOUND, "code.not.found")
                return
            }
            if (idToken == null && request.isIdTokenRequested) {
                callback.onError(ErrorInfo.NOT_FOUND, "id_token.not.found")
                return
            }
            if (state != request.state) {
                callback.onError(ErrorInfo.INTERNAL, "state.not.matched")
                return
            }

            callback.onSuccess(
                SignInWithAppleResult(
                    code = code,
                    idToken = idToken,
                    user = user
                )
            )

        } catch (e: Exception) {
            callback.onError(ErrorInfo.INTERNAL, e.localizedMessage)
        }
    }

    inner class PayloadRecorder {

        var redirectParams: String? = null
            private set

        @JavascriptInterface
        fun recordPayload(method: String, url: String, payload: String) {
            if (url == request.redirectUri) {
                redirectParams = payload
            }
        }
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