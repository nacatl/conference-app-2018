package io.github.droidkaigi.confsched2018.presentation.common.view

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import io.github.droidkaigi.confsched2018.presentation.common.pref.Prefs
import io.github.droidkaigi.confsched2018.presentation.common.pref.initPreviousRoomPrefs


class SessionsLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {

    fun saveScrollPositionToPrefs() {
        val savedState = onSaveInstanceState() as Parcelable
        val parcel = Parcel.obtain()
        savedState.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        parcel.setDataPosition(0)

        val anchorPosition = parcel.readInt()
        val anchorOffset = parcel.readInt()

        parcel.recycle()

        Prefs.previousRoomScrollPosition = anchorPosition
        Prefs.previousRoomScrollOffset = anchorOffset
    }

    fun restoreScrollPositionFromPrefs(){
        val previousScrollPosition = Prefs.previousRoomScrollPosition
        val previousScrollOffset = Prefs.previousRoomScrollOffset

        if (previousScrollPosition < 0) return

        val parcel = Parcel.obtain()
        parcel.writeInt(previousScrollPosition)
        parcel.writeInt(previousScrollOffset)
        parcel.writeInt(0)
        parcel.setDataPosition(0)

        val savedState = LinearLayoutManager.SavedState.CREATOR.createFromParcel(parcel)

        onRestoreInstanceState(savedState)
        parcel.recycle()

        initPreviousRoomPrefs()
    }

}
