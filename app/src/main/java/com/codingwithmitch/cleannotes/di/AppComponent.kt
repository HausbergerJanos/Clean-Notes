package com.codingwithmitch.cleannotes.di

import com.codingwithmitch.cleannotes.framework.presentation.BaseApplication
import com.codingwithmitch.cleannotes.framework.presentation.MainActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ProductionModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factorí {

        fun create(@BindsInstance app: BaseApplication): AppComponent
    }

    fun inject(mainActivity: MainActivity)
}