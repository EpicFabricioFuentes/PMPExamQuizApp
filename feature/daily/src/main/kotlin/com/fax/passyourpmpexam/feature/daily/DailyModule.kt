package com.fax.passyourpmpexam.feature.daily

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val dailyModule: Module = module {
    viewModel { DailyViewModel(get(), get(), get(), get(), get(), get()) }
}
