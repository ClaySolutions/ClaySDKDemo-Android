package com.salto.claysdkdemo.application

import com.salto.claysdkdemo.modules.ActivitiesModule
import com.salto.claysdkdemo.modules.AppModule
import com.salto.claysdkdemo.modules.PresenterModule
import com.salto.claysdkdemo.modules.ServiceModule
import com.salto.claysdkdemo.widget.DemoWidgetProvider
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        ActivitiesModule::class,
        PresenterModule::class,
        ServiceModule::class
    ]
)

interface AppComponent {

    companion object {

        fun init(
                application: App
        ): AppComponent {
            return DaggerAppComponent.builder()
                .appModule(AppModule(application))
                .presenterModule(PresenterModule())
                .serviceModule(ServiceModule())
                .build()
        }
    }

    fun inject(app: App)
    fun inject(widgetProvider: DemoWidgetProvider)
}