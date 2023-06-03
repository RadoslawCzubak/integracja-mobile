package pl.rczubak.stripetest.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module
import pl.rczubak.stripetest.ui.home.HomeViewModel
import pl.rczubak.stripetest.ui.login.LoginViewModel

val viewModelModule = module {
    viewModel {
        LoginViewModel(get())
    }
    viewModelOf(::HomeViewModel)
}
