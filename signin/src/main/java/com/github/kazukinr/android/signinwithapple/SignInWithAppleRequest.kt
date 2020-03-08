package com.github.kazukinr.android.signinwithapple

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SignInWithAppleRequest(
    val clientId: String,
    val redirectUri: String,
    val state: String,
    val responseType: ResponseType,
    val scope: Scope? = null
) : Parcelable {

    enum class ResponseType {
        CODE,
        ID_TOKEN,
        BOTH
    }

    enum class Scope {
        NAME,
        EMAIL,
        BOTH
    }

    val isQueryResponseType: Boolean = this.responseType == ResponseType.CODE && this.scope == null

    val isCodeRequested: Boolean = this.responseType == ResponseType.CODE || this.responseType == ResponseType.BOTH

    val isIdTokenRequested: Boolean = this.responseType == ResponseType.ID_TOKEN || this.responseType == ResponseType.BOTH
}
