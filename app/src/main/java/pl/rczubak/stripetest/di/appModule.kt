package pl.rczubak.stripetest.di

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pl.rczubak.stripetest.data.CafeRepository
import pl.rczubak.stripetest.data.TokenInterceptor
import pl.rczubak.stripetest.data.service.BASE_URL
import pl.rczubak.stripetest.data.service.CafeAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {

    single {
        val interceptor = HttpLoggingInterceptor()
        val tokenInterceptor = TokenInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder().addInterceptor(interceptor).addInterceptor(tokenInterceptor).build()
    }

    single {
        val okHttpClient: OkHttpClient = get()
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<CafeAPI> {
        val retrofit: Retrofit = get()
        retrofit.create(CafeAPI::class.java)
    }

    singleOf(::CafeRepository)

    single {
        Firebase.auth
    }
}