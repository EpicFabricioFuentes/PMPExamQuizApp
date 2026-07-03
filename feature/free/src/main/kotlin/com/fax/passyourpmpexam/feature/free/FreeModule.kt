package com.fax.passyourpmpexam.feature.free

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val freeModule: Module = module {
    viewModel { FreeViewModel(get(), get(), get(), get(), get()) }
}
