package com.github.kazukinr.android.signinwithapple.internal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ErrorInfo(
    val code: Int,
    val message: String
) : Parcelable {

    companion object {

        const val INVALID_REQUEST = 400
        const val NOT_FOUND = 404
        const val INTERNAL = 500
    }
}
