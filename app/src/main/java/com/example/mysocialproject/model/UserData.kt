package com.example.mysocialproject.model

import android.os.Parcel
import android.os.Parcelable

data class UserData(
    var userId: String = "",
    val emailUser: String = "",
    var avatarUser: String = "",
    var nameUser: String = "",
    val passwordUser: String = "",
)
