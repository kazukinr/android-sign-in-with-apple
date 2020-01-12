package com.github.kazukinr.android.signin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SignInWithAppleRequest(
    val clientId: String,
    val redirectUri: String,
    val state: String
) : Parcelable
