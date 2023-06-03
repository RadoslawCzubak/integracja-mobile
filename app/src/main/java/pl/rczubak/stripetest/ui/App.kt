package pl.rczubak.stripetest.ui

import android.app.Application
import com.stripe.android.PaymentConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import pl.rczubak.stripetest.di.appModule
import pl.rczubak.stripetest.di.useCaseModule
import pl.rczubak.stripetest.di.viewModelModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                listOf(appModule)
                    .plus(viewModelModule)
                    .plus(useCaseModule)
            )
        }
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51N7DrUHoWwrUTQHV88Ip45mOZD44CCNHD0Wbc5maOW5zv5gQ74H03x8WsYgtg17Th4qa2wEB2AD6wwZ5WaWGBJcO00ZGYMJJHb"
        )
    }
}