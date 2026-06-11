package com.lucasbueno.moises_challenge.di

import com.lucasbueno.moises_challenge.data.local.MusicLocalDataSource
import com.lucasbueno.moises_challenge.data.local.RoomMusicLocalDataSource
import com.lucasbueno.moises_challenge.data.remote.ITunesMusicRemoteDataSource
import com.lucasbueno.moises_challenge.data.remote.MusicRemoteDataSource
import com.lucasbueno.moises_challenge.data.repository.MusicRepositoryImpl
import com.lucasbueno.moises_challenge.domain.repository.MusicRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        implementation: MusicRepositoryImpl,
    ): MusicRepository

    @Binds
    @Singleton
    abstract fun bindMusicLocalDataSource(
        implementation: RoomMusicLocalDataSource,
    ): MusicLocalDataSource

    @Binds
    @Singleton
    abstract fun bindMusicRemoteDataSource(
        implementation: ITunesMusicRemoteDataSource,
    ): MusicRemoteDataSource
}
