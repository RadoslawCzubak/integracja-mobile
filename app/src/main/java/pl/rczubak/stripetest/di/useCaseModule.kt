package pl.rczubak.stripetest.di

import org.koin.dsl.module
import pl.rczubak.stripetest.domain.usecase.CancelReservationUseCase
import pl.rczubak.stripetest.domain.usecase.GetTablesAvailabilityUseCase
import pl.rczubak.stripetest.domain.usecase.ReserveTableUseCase

val useCaseModule = module {
    factory { GetTablesAvailabilityUseCase(get()) }
    factory { ReserveTableUseCase(get()) }
    factory { CancelReservationUseCase(get()) }
}
