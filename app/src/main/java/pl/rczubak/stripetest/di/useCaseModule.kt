package pl.rczubak.stripetest.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import pl.rczubak.stripetest.domain.usecase.*

val useCaseModule = module {
    factory { GetTablesAvailabilityUseCase(get()) }
    factory { ReserveTableUseCase(get()) }
    factory { CancelReservationUseCase(get()) }
    factory { GetMenuUseCase(get()) }
    factory { GetOrdersUseCase(get()) }
    factory { CreateOrderUseCase(get()) }
    factoryOf(::GetMenuUseCase)
    factoryOf(::GetLoyaltyPointsUseCase)
    factoryOf(::GetEmployeeOrderUseCase)
    factoryOf(::UpdateOrderUseCase)
}
