package com.fax.passyourpmpexam.core.ads

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val adsModule: Module = module {
    single { ConsentManager(androidContext()) }
}
