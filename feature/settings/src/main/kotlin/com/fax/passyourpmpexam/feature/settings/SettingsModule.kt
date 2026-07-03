package com.fax.passyourpmpexam.feature.settings

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val settingsModule: Module = module {
    viewModel { SettingsViewModel(get(), get()) }
}
