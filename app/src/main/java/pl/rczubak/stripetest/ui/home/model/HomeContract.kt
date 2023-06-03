package pl.rczubak.stripetest.ui.home.model

import pl.rczubak.stripetest.base.Event
import pl.rczubak.stripetest.base.State
import pl.rczubak.stripetest.domain.model.Reservation
import pl.rczubak.stripetest.domain.model.Table
import java.time.LocalDateTime

class HomeContract {
    data class HomeState(
        val availableTables: List<Table>? = null,
        val chosenTableId: Int? = null,
        val tableAvailabilityLoading: Boolean = false,
        val reservation: Reservation? = null
    ) : State

    sealed interface HomeEvent : Event {
        data class ChooseTable(val tableId: Int) : HomeEvent
        data class CheckTablesAvailability(val time: LocalDateTime) : HomeEvent
        data class ReserveTable(val time: LocalDateTime) : HomeEvent
        object CancelReservation : HomeEvent
    }
}