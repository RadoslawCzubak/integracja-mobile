package pl.rczubak.stripetest.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module
import pl.rczubak.stripetest.ui.home.HomeViewModel
import pl.rczubak.stripetest.ui.login.LoginViewModel
import pl.rczubak.stripetest.ui.order.OrderViewModel

val viewModelModule = module {
    viewModel {
        LoginViewModel(get())
    }
    viewModelOf(::HomeViewModel)
    viewModelOf(::OrderViewModel)
}
