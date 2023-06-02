package pl.rczubak.stripetest.ui

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.stripe.android.PaymentConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import pl.rczubak.stripetest.di.appModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51N7DrUHoWwrUTQHV88Ip45mOZD44CCNHD0Wbc5maOW5zv5gQ74H03x8WsYgtg17Th4qa2wEB2AD6wwZ5WaWGBJcO00ZGYMJJHb"
        )
    }
}