package com.salto.claysdkdemo.modules

import com.salto.claysdkdemo.access_code.AccessCodeActivity
import com.salto.claysdkdemo.guest_digital_key.GuestDigitalKeysListActivity
import com.salto.claysdkdemo.main.MainActivity
import com.salto.claysdkdemo.login.LoginActivity
import com.salto.claysdkdemo.send_dkey.SendDKeyActivity
import com.salto.claysdkdemo.widget.AppWidgetPermissionActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesModule {

    @ContributesAndroidInjector
    internal abstract fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun contributeSendDKeyActivity(): SendDKeyActivity

    @ContributesAndroidInjector
    internal abstract fun contributeGuestDigitalKeysListActivity(): GuestDigitalKeysListActivity

    @ContributesAndroidInjector
    internal abstract fun contributeAccessCodeActivity(): AccessCodeActivity

    @ContributesAndroidInjector
    internal abstract fun contributeAppWidgetPermissionActivity(): AppWidgetPermissionActivity
}