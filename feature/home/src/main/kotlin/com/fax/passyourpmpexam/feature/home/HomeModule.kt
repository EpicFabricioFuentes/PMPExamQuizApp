package com.fax.passyourpmpexam.feature.home

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val homeModule: Module = module {
    viewModel { HomeViewModel(get(), get(), get()) }
}
