package com.fax.passyourpmpexam.feature.quiz

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val quizModule: Module = module {
    viewModel { QuizViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
