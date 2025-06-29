package se.umu.c22jwg.thirty.model

import android.os.Parcel
import android.os.Parcelable

class Die: Parcelable {
    var value: Int = (1..6).random();
    var selected = false;

    fun roll() {
        value = (1..6).random();
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(value)
        dest.writeByte(if (selected) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<Die> {
        override fun createFromParcel(parcel: Parcel): Die {
            val die = Die()
            die.value = parcel.readInt()
            die.selected = parcel.readByte().toInt() != 0
            return die
        }

        override fun newArray(size: Int): Array<Die?> = arrayOfNulls(size)
    }
}