package io.github.droidkaigi.confsched2018.data.db

import android.support.annotation.CheckResult
import io.github.droidkaigi.confsched2018.data.api.response.Response
import io.github.droidkaigi.confsched2018.data.db.entity.RoomEntity
import io.github.droidkaigi.confsched2018.data.db.entity.SessionFeedbackEntity
import io.github.droidkaigi.confsched2018.data.db.entity.SessionWithSpeakers
import io.github.droidkaigi.confsched2018.data.db.entity.SpeakerEntity
import io.github.droidkaigi.confsched2018.data.db.entity.TopicEntity
import io.github.droidkaigi.confsched2018.model.SessionFeedback
import io.reactivex.Flowable

interface SessionDatabase {
    @CheckResult fun getAllSessions(): Flowable<List<SessionWithSpeakers>>
    @CheckResult fun getAllSessionFeedback(): Flowable<List<SessionFeedbackEntity>>
    @CheckResult fun getAllSpeaker(): Flowable<List<SpeakerEntity>>
    @CheckResult fun getAllRoom(): Flowable<List<RoomEntity>>
    @CheckResult fun getAllTopic(): Flowable<List<TopicEntity>>
    fun save(response: Response)
    fun saveSessionFeedback(sessionFeedback: SessionFeedback)
}
