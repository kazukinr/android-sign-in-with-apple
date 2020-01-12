package com.github.kazukinr.android.signin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.github.kazukinr.android.signin.databinding.SignInWithAppleActivityBinding
import java.util.*

@SuppressLint("SetJavaScriptEnabled")
class SignInWithAppleActivity : AppCompatActivity() {

    companion object {

        private const val KEY_EXTRA_REQUEST = "key_extra_request"
        private const val KEY_EXTRA_RESULT = "key_extra_result"
        private const val KEY_STATE_WEB_VIEW = "key_state_web_view"

        fun createIntent(
            context: Context,
            clientId: String,
            redirectUri: String,
            scope: String = "email"
        ): Intent =
            Intent(context, SignInWithAppleActivity::class.java).apply {
                val req = SignInWithAppleRequest(
                    clientId = clientId,
                    redirectUri = redirectUri,
                    state = UUID.randomUUID().toString()
                )
                putExtra(KEY_EXTRA_REQUEST, req)
            }
    }

    private lateinit var binding: SignInWithAppleActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.sign_in_with_apple_activity)

        binding.webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
        }

        val req = intent.getParcelableExtra<SignInWithAppleRequest>(KEY_EXTRA_REQUEST)
        binding.webView.webViewClient = SignInWithAppleWebViewClient(req) { result ->
            onSignInCallback(result)
        }

        if (savedInstanceState != null) {
            savedInstanceState.getBundle(KEY_STATE_WEB_VIEW)?.also {
                binding.webView.restoreState(it)
            }
        } else {
            binding.webView.loadUrl(buildAuthenticationUri(req).toString())
        }
    }

    /**
     * Build uri to authorize with apple.
     * See also https://developer.apple.com/documentation/signinwithapplejs/configuring_your_webpage_for_sign_in_with_apple
     */
    private fun buildAuthenticationUri(req: SignInWithAppleRequest): Uri =
        Uri.parse("https://appleid.apple.com/auth/authorize")
            .buildUpon()
            .apply {
                appendQueryParameter("response_type", "code")
                appendQueryParameter("v", "1.1.6")
                appendQueryParameter("client_id", req.clientId)
                appendQueryParameter("redirect_uri", req.redirectUri)
                appendQueryParameter("scope", "")
                appendQueryParameter("state", req.state)
            }
            .build()

    private fun onSignInCallback(result: SignInWithAppleResult) {
        val data = Intent().apply {
            putExtra(KEY_EXTRA_RESULT, result)
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(
            KEY_STATE_WEB_VIEW,
            Bundle().apply {
                binding.webView.saveState(this)
            }
        )
    }
}
