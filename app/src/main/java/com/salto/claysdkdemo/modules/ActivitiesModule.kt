package com.salto.claysdkdemo.modules

import com.salto.claysdkdemo.main.MainActivity
import com.salto.claysdkdemo.login.LoginActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesModule {

    @ContributesAndroidInjector
    internal abstract fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity
}