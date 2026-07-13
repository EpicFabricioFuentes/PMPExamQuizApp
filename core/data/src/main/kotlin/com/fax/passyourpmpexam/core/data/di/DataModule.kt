package com.fax.passyourpmpexam.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.fax.passyourpmpexam.core.common.IdGenerator
import com.fax.passyourpmpexam.core.common.SystemTimeProvider
import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.common.UuidGenerator
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.fax.passyourpmpexam.core.data.content.AssetContentSource
import com.fax.passyourpmpexam.core.data.content.BankImporter
import com.fax.passyourpmpexam.core.data.local.PmpDatabase
import com.fax.passyourpmpexam.core.data.repository.AttemptRepositoryImpl
import com.fax.passyourpmpexam.core.data.repository.QuestionRepositoryImpl
import com.fax.passyourpmpexam.core.data.repository.QuizSessionRepositoryImpl
import com.fax.passyourpmpexam.core.data.repository.SettingsRepositoryImpl
import com.fax.passyourpmpexam.core.data.repository.StreakRepositoryImpl
import com.fax.passyourpmpexam.core.domain.repository.AttemptRepository
import com.fax.passyourpmpexam.core.domain.repository.ContentSource
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.QuizSessionRepository
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
import com.fax.passyourpmpexam.core.domain.repository.StreakRepository
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

private const val SETTINGS_STORE_NAME = "pmp_settings"

val dataModule: Module = module {
    single { Json { ignoreUnknownKeys = true } }
    single<TimeProvider> { SystemTimeProvider() }
    single<IdGenerator> { UuidGenerator() }

    single { provideDatabase(androidContext()) }
    single { get<PmpDatabase>().questionDao() }
    single { get<PmpDatabase>().quizSessionDao() }
    single { get<PmpDatabase>().sessionQuestionDao() }
    single { get<PmpDatabase>().attemptDao() }

    single { provideSettingsDataStore(androidContext()) }

    single<QuestionRepository> { QuestionRepositoryImpl(get()) }
    single<AttemptRepository> { AttemptRepositoryImpl(get()) }
    single<QuizSessionRepository> { QuizSessionRepositoryImpl(get(), get(), get(), get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<StreakRepository> { StreakRepositoryImpl(get()) }

    single<ContentSource> { AssetContentSource(androidContext(), get()) }
    single { BankImporter(get(), get(), get()) }
}

private fun provideDatabase(context: Context): PmpDatabase =
    Room.databaseBuilder(context, PmpDatabase::class.java, PmpDatabase.NAME).build()

private fun provideSettingsDataStore(context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile(SETTINGS_STORE_NAME)
    }
