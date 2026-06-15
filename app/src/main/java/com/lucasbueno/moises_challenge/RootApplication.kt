package com.lucasbueno.moises_challenge

import android.app.Application
import com.lucasbueno.moises_challenge.domain.cache.RecentlyPlayedCacheRecycler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RootApplication : Application() {
    @Inject
    lateinit var recentlyPlayedCacheRecycler: RecentlyPlayedCacheRecycler

    override fun onCreate() {
        super.onCreate()
        recentlyPlayedCacheRecycler.start()
    }
}
