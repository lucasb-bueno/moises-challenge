package com.lucasbueno.moises_challenge.domain.cache

import com.lucasbueno.moises_challenge.di.ApplicationScope
import com.lucasbueno.moises_challenge.domain.model.RecentlyPlayedCachePolicy
import com.lucasbueno.moises_challenge.domain.repository.MusicRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Singleton
class RecentlyPlayedCacheRecycler @Inject constructor(
    private val repository: MusicRepository,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return

        job = applicationScope.launch {
            while (isActive) {
                repository.recycleRecentlyPlayedCache(
                    recentlyPlayedMaxAgeMillis = RecentlyPlayedCachePolicy.MAX_AGE_MILLIS,
                    recentlyPlayedMaxSize = RecentlyPlayedCachePolicy.MAX_SIZE,
                )
                delay(RecentlyPlayedCachePolicy.RECYCLE_INTERVAL_MILLIS)
            }
        }
    }
}
