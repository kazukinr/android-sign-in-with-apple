package com.github.kazukinr.android.signin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class SignInWithAppleResult : Parcelable {

    @Parcelize
    data class Success(val authCode: String) : SignInWithAppleResult()

    @Parcelize
    data class Failure(val throwable: Throwable) : SignInWithAppleResult()
}