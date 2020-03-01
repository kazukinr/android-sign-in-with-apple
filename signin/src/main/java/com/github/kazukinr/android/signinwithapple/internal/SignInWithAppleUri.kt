package com.github.kazukinr.android.signinwithapple.internal

import android.net.Uri
import com.github.kazukinr.android.signinwithapple.SignInWithAppleRequest

/**
 * Build uri to authorize with apple.
 *
 * See https://developer.apple.com/documentation/signinwithapplejs/incorporating_sign_in_with_apple_into_other_platforms
 */
internal data class SignInWithAppleUri(
    val request: SignInWithAppleRequest
) {

    fun authUri(): Uri =
        Uri.parse("https://appleid.apple.com/auth/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", request.clientId)
            .appendQueryParameter("redirect_uri", request.redirectUri)
            .appendQueryParameter("state", request.state)
            .also {
                val responseMode = if (request.isQueryResponseType) {
                    "query"
                } else {
                    "form_post"
                }
                val responseType = when (request.responseType) {
                    SignInWithAppleRequest.ResponseType.CODE -> "code"
                    SignInWithAppleRequest.ResponseType.ID_TOKEN -> "id_token"
                    SignInWithAppleRequest.ResponseType.BOTH -> "code id_token"
                }
                val scope = when (request.scope) {
                    SignInWithAppleRequest.Scope.NAME -> "name"
                    SignInWithAppleRequest.Scope.EMAIL -> "email"
                    SignInWithAppleRequest.Scope.BOTH -> "name email"
                    null -> null
                }

                it.appendQueryParameter("response_mode", responseMode)
                it.appendQueryParameter("response_type", responseType)
                scope?.also { s ->
                    it.appendQueryParameter("scope", s)
                }
            }
            .build()
}