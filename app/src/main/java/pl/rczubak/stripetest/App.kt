package pl.rczubak.stripetest

import android.app.Application
import com.stripe.android.PaymentConfiguration
import org.koin.core.context.GlobalContext.startKoin
import pl.rczubak.stripetest.di.appModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
//        startKoin{
//            modules(appModule)
//        }
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51N7DrUHoWwrUTQHV88Ip45mOZD44CCNHD0Wbc5maOW5zv5gQ74H03x8WsYgtg17Th4qa2wEB2AD6wwZ5WaWGBJcO00ZGYMJJHb"
        )
    }
}