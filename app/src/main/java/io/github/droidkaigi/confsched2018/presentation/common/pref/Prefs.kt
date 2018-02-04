package io.github.droidkaigi.confsched2018.presentation.common.pref

import com.chibatching.kotpref.KotprefModel
import io.github.droidkaigi.confsched2018.R
import io.github.droidkaigi.confsched2018.util.ext.bool
import io.github.droidkaigi.confsched2018.util.ext.integer

object Prefs : KotprefModel() {
    public override val kotprefName: String = "droidkaigi_prefs"
    var enableLocalTime: Boolean by booleanPref(
            context.bool(R.bool.pref_default_value_enable_local_time),
            R.string.pref_key_enable_local_time
    )
    var enableNotification: Boolean by booleanPref(
            context.bool(R.bool.pref_default_value_enable_notification),
            R.string.pref_key_enable_notification
    )
    var enableHideBottomNavigationBar: Boolean by booleanPref(
            context.bool(R.bool.pref_default_value_enable_hide_bottom_navigation),
            R.string.pref_key_enable_hide_bottom_navigation
    )
    var enableReopenPreviousRoomSessions: Boolean by booleanPref(
            context.bool(R.bool.pref_default_value_enable_reopen_previous_room_sessions),
            R.string.pref_key_enable_reopen_previous_room_sessions
    )
    var previousSessionTabId: Int by intPref(
            context.integer(R.integer.pref_default_value_previous_session_tab_id),
            R.string.pref_key_previous_session_tab_id
    )
    var previousSessionScrollPosition: Int by intPref(
            context.integer(R.integer.pref_default_value_previous_session_scroll_position),
            R.string.pref_key_previous_session_scroll_position
    )
    var previousSessionScrollOffset: Int by intPref(
            context.integer(R.integer.pref_default_value_previous_session_scroll_offset),
            R.string.pref_key_previous_session_scroll_offset
    )
    var previousSessionTab: String by stringPref(
            context.getString(R.string.pref_default_value_previous_session_tab),
            R.string.pref_key_previous_session_tab
    )
}

fun initPreviousSessionPrefs() {
    Prefs.previousSessionTab = Prefs.context.getString(
            R.string.pref_default_value_previous_session_tab)
    Prefs.previousSessionTabId = Prefs.context.integer(
            R.integer.pref_default_value_previous_session_tab_id)
    Prefs.previousSessionScrollPosition = Prefs.context.integer(
            R.integer.pref_default_value_previous_session_scroll_position)
    Prefs.previousSessionScrollOffset = Prefs.context.integer(
            R.integer.pref_default_value_previous_session_scroll_offset)
}
