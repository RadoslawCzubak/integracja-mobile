package pl.rczubak.stripetest.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pl.rczubak.stripetest.data.CafeRepository
import pl.rczubak.stripetest.data.service.BASE_URL
import pl.rczubak.stripetest.data.service.CafeAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<CafeAPI> {
        val retrofit: Retrofit = get()
        retrofit.create(CafeAPI::class.java)
    }

    singleOf(::CafeRepository)

}