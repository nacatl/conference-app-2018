package io.github.droidkaigi.confsched2018.presentation.sessions

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.transition.TransitionInflater
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.github.droidkaigi.confsched2018.R
import io.github.droidkaigi.confsched2018.databinding.FragmentAllSessionsBinding
import io.github.droidkaigi.confsched2018.di.Injectable
import io.github.droidkaigi.confsched2018.model.Session
import io.github.droidkaigi.confsched2018.presentation.NavigationController
import io.github.droidkaigi.confsched2018.presentation.Result
import io.github.droidkaigi.confsched2018.presentation.common.pref.Prefs
import io.github.droidkaigi.confsched2018.presentation.common.pref.initPreviousRoomPrefs
import io.github.droidkaigi.confsched2018.presentation.sessions.SessionsFragment.CurrentSessionScroller
import io.github.droidkaigi.confsched2018.presentation.sessions.SessionsFragment.SaveClosedSessionScroller
import io.github.droidkaigi.confsched2018.presentation.sessions.item.DateSessionsSection
import io.github.droidkaigi.confsched2018.presentation.sessions.item.SpeechSessionItem
import io.github.droidkaigi.confsched2018.util.ProgressTimeLatch
import io.github.droidkaigi.confsched2018.util.SessionAlarm
import io.github.droidkaigi.confsched2018.util.ext.addOnScrollListener
import io.github.droidkaigi.confsched2018.util.ext.isGone
import io.github.droidkaigi.confsched2018.util.ext.observe
import io.github.droidkaigi.confsched2018.util.ext.setLinearDivider
import io.github.droidkaigi.confsched2018.util.ext.setTextIfChanged
import io.github.droidkaigi.confsched2018.util.ext.setVisible
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class AllSessionsFragment : Fragment(), Injectable,
        CurrentSessionScroller, SaveClosedSessionScroller {

    private var fireBaseAnalytics: FirebaseAnalytics? = null
    private lateinit var binding: FragmentAllSessionsBinding

    private val sessionsSection = DateSessionsSection()

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var navigationController: NavigationController
    @Inject lateinit var sessionAlarm: SessionAlarm

    private val sessionsViewModel: AllSessionsViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AllSessionsViewModel::class.java)
    }

    private val onFavoriteClickListener = { session: Session.SpeechSession ->
        sessionsViewModel.onFavoriteClick(session)
        sessionAlarm.toggleRegister(session)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentAllSessionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fireBaseAnalytics = FirebaseAnalytics.getInstance(context)
        setupRecyclerView()

        val progressTimeLatch = ProgressTimeLatch {
            binding.progress.visibility = if (it) View.VISIBLE else View.GONE
        }
        sessionsViewModel.sessions.observe(this, { result ->
            when (result) {
                is Result.Success -> {
                    val sessions = result.data
                    sessionsSection.updateSessions(sessions, onFavoriteClickListener, true)

                    sessionsViewModel.onSuccessFetchSessions()
                }
                is Result.Failure -> {
                    Timber.e(result.e)
                }
            }
        })
        sessionsViewModel.isLoading.observe(this, { isLoading ->
            progressTimeLatch.loading = isLoading ?: false
        })
        sessionsViewModel.refreshFocusCurrentSession.observe(this, {
            if (it != true) return@observe
            scrollToCurrentSession()
        })
        sessionsViewModel.reopenPreviousSession.observe(this, {
            if (it != true) return@observe
            scrollToPreviousSession()
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            fireBaseAnalytics?.setCurrentScreen(activity!!, null, this::class.java.simpleName)
        }
    }

    override fun scrollToCurrentSession() {
        val now = Date(ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS["JST"]))
                .toInstant().toEpochMilli())
        val currentSessionPosition = sessionsSection.getDateHeaderPositionByDate(now)
        binding.sessionsRecycler.scrollToPosition(currentSessionPosition)
    }

    override fun saveCurrentSession() {
        val savedState = (binding.sessionsRecycler.layoutManager as LinearLayoutManager)
                .onSaveInstanceState() as Parcelable
        val parcel = Parcel.obtain()
        savedState.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        parcel.setDataPosition(0)

        val anchorPosition = parcel.readInt()
        val anchorOffset = parcel.readInt()

        parcel.recycle()

        Prefs.previousRoomScrollPosition = anchorPosition
        Prefs.previousRoomScrollOffset = anchorOffset
    }

    override fun restorePreviousSession() {
        sessionsViewModel.enableRestoreScroller = true
    }

    private fun scrollToPreviousSession() {
        val linearLayoutManager = binding.sessionsRecycler.layoutManager as LinearLayoutManager
        val previousScrollPosition = Prefs.previousRoomScrollPosition
        val previousScrollOffset = Prefs.previousRoomScrollOffset

        if (previousScrollPosition < 0) return

        val parcel = Parcel.obtain()
        parcel.writeInt(previousScrollPosition)
        parcel.writeInt(previousScrollOffset)
        parcel.writeInt(0)
        parcel.setDataPosition(0)

        val savedState = LinearLayoutManager.SavedState.CREATOR.createFromParcel(parcel)

        linearLayoutManager.onRestoreInstanceState(savedState)
        parcel.recycle()

        initPreviousRoomPrefs()

        sessionsViewModel.enableRestoreScroller = false
    }

    private fun setupRecyclerView() {
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            add(sessionsSection)
            setOnItemClickListener({ item, _ ->
                val sessionItem = item as? SpeechSessionItem ?: return@setOnItemClickListener
                navigationController.navigateToSessionDetailActivity(sessionItem.session)
            })
        }
        binding.sessionsRecycler.apply {
            adapter = groupAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

            addOnScrollListener(
                    onScrollStateChanged = { _: RecyclerView?, newState: Int ->
                        if (binding.sessionsRecycler.isGone()) return@addOnScrollListener
                        setDayHeaderVisibility(newState != RecyclerView.SCROLL_STATE_IDLE)
                    },
                    onScrolled = { _, _, _ ->
                        val linearLayoutManager = layoutManager as LinearLayoutManager
                        val firstPosition = linearLayoutManager.findFirstVisibleItemPosition()
                        val dayNumber = sessionsSection.getDateNumberOrNull(firstPosition)
                        dayNumber ?: return@addOnScrollListener
                        val dayTitle = getString(R.string.session_day_title, dayNumber)
                        binding.dayHeader.setTextIfChanged(dayTitle)
                    })
            setLinearDivider(R.drawable.shape_divider_vertical_12dp,
                    layoutManager as LinearLayoutManager)
        }
    }

    private fun setDayHeaderVisibility(visibleDayHeader: Boolean) {
        val transition = TransitionInflater
                .from(context)
                .inflateTransition(R.transition.date_header_visibility)
        TransitionManager.beginDelayedTransition(binding.sessionsConstraintLayout, transition)
        binding.dayHeader.setVisible(visibleDayHeader)
    }

    companion object {
        fun newInstance(): AllSessionsFragment = AllSessionsFragment()
    }
}
