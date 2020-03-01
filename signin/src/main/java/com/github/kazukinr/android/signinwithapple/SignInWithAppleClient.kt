package com.github.kazukinr.android.signinwithapple

import android.app.Activity
import android.content.Intent
import com.github.kazukinr.android.signinwithapple.internal.ErrorInfo
import com.github.kazukinr.android.signinwithapple.internal.SignInWithAppleActivity

class SignInWithAppleClient(
    private val activity: Activity,
    private val signInRequestCode: Int = 7919
) {

    private var callback: Callback? = null

    fun signIn(
        request: SignInWithAppleRequest,
        callback: Callback
    ) {
        if (request.clientId.isBlank()) {
            callback.onError(
                SignInWithAppleException(
                    ErrorInfo.INVALID_REQUEST,
                    "client_id.required"
                )
            )
            return
        }
        if (request.redirectUri.isBlank()) {
            callback.onError(
                SignInWithAppleException(
                    ErrorInfo.INVALID_REQUEST,
                    "redirect_uri.required"
                )
            )
            return
        }

        this.callback = callback
        activity.startActivityForResult(
            SignInWithAppleActivity.createIntent(activity, request),
            signInRequestCode
        )
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == signInRequestCode) {
            val callback = this.callback ?: return
            this.callback = null

            when (resultCode) {
                Activity.RESULT_CANCELED -> {
                    callback.onCancel()
                }
                else -> {
                    val error =
                        data?.getParcelableExtra<ErrorInfo>(SignInWithAppleActivity.KEY_EXTRA_ERROR)
                    if (error != null) {
                        callback.onError(SignInWithAppleException(error.code, error.message))
                        return
                    }

                    val result =
                        data?.getParcelableExtra<SignInWithAppleResult>(SignInWithAppleActivity.KEY_EXTRA_RESULT)
                    if (result == null) {
                        callback.onError(
                            SignInWithAppleException(
                                ErrorInfo.NOT_FOUND,
                                "result.not.found"
                            )
                        )
                        return
                    }

                    callback.onSuccess(result)
                }
            }
        }
    }

    interface Callback {

        fun onSuccess(result: SignInWithAppleResult)

        fun onError(throwable: Throwable)

        fun onCancel()
    }
}
