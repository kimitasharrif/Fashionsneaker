package com.example.sneakerhub.models

import android.os.Parcel
import android.os.Parcelable

data class shoes(
    var shoe_id: String = "",
    var category_id: String = "",
    var name: String = "",
    var price: String = "",
    var description: String = "",
    var brand_name: String = "",
    var quantity: String = "",
    var photo_url: String = "",
    var size: String = ""  // Add this field
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""  // Read the new field from the parcel
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(shoe_id)
        parcel.writeString(category_id)
        parcel.writeString(name)
        parcel.writeString(price)
        parcel.writeString(description)
        parcel.writeString(brand_name)
        parcel.writeString(quantity)
        parcel.writeString(photo_url)
        parcel.writeString(size)  // Write the new field to the parcel
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<shoes> {
        override fun createFromParcel(parcel: Parcel): shoes {
            return shoes(parcel)
        }

        override fun newArray(size: Int): Array<shoes?> {
            return arrayOfNulls(size)
        }
    }
}
