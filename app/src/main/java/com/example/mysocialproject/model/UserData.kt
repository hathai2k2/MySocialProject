package com.example.mysocialproject.model

import android.os.Parcel
import android.os.Parcelable

data class UserData(
    var userId: String = "",
    val emailUser: String = "",
    var avatarUser: String = "",
    var nameUser: String = "",
    val passwordUser: String = "",
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(emailUser)
        parcel.writeString(avatarUser)
        parcel.writeString(nameUser)
        parcel.writeString(passwordUser)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserData> {
        override fun createFromParcel(parcel: Parcel): UserData {
            return UserData(parcel)
        }

        override fun newArray(size: Int): Array<UserData?> {
            return arrayOfNulls(size)
        }
    }
}
