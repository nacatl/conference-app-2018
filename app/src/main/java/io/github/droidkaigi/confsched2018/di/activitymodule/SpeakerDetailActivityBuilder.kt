package io.github.droidkaigi.confsched2018.di.activitymodule

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.github.droidkaigi.confsched2018.presentation.speaker.SpeakerDetailActivity

@Module interface SpeakerDetailActivityBuilder {
    @ContributesAndroidInjector(modules = [SpeakerDetailActivityModule::class])
    fun contributeSpeakerDetailActivity(): SpeakerDetailActivity
}
