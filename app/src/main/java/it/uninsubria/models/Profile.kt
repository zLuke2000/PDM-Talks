package it.uninsubria.models

import android.os.Parcel
import android.os.Parcelable

class Profile (var nickname: String? = null,
               var name: String? = null,
               var surname: String? = null,
               var hasPicture: Boolean? = null,
               var email: String? = null) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nickname)
        parcel.writeString(name)
        parcel.writeString(surname)
        parcel.writeValue(hasPicture)
        parcel.writeString(email)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Profile> {
        override fun createFromParcel(parcel: Parcel): Profile {
            return Profile(parcel)
        }

        override fun newArray(size: Int): Array<Profile?> {
            return arrayOfNulls(size)
        }
    }
}