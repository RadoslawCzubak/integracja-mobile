package pl.rczubak.stripetest.ui.home

import androidx.lifecycle.viewModelScope
import pl.rczubak.stripetest.base.BaseViewModel
import pl.rczubak.stripetest.domain.usecase.CancelReservationUseCase
import pl.rczubak.stripetest.domain.usecase.GetTablesAvailabilityUseCase
import pl.rczubak.stripetest.domain.usecase.ReserveTableUseCase
import pl.rczubak.stripetest.ui.home.model.HomeContract
import java.time.LocalDateTime

class HomeViewModel(
    private val getTablesAvailabilityUseCase: GetTablesAvailabilityUseCase,
    private val reserveTableUseCase: ReserveTableUseCase,
    private val cancelReservationUseCase: CancelReservationUseCase,
) : BaseViewModel<HomeContract.HomeState, HomeContract.HomeEvent>() {
    override fun initialState(): HomeContract.HomeState = HomeContract.HomeState()

    override fun handleEvent(event: HomeContract.HomeEvent) {
        when (event) {
            is HomeContract.HomeEvent.ChooseTable -> setState { state -> state.copy(chosenTableId = event.tableId) }
            is HomeContract.HomeEvent.CheckTablesAvailability -> checkTablesAvailability(time = event.time)
            is HomeContract.HomeEvent.ReserveTable -> reserveChosenTable(event.time)
            is HomeContract.HomeEvent.CancelReservation -> cancelReservation()
        }
    }

    private fun reserveChosenTable(time: LocalDateTime) {
        uiState.value.chosenTableId?.let { chosenTableId ->
            reserveTableUseCase(
                params = ReserveTableUseCase.Params(tableId = chosenTableId, time),
                scope = viewModelScope,
                onResult = {
                    it.onSuccess {
                        setState { state -> state.copy(reservation = it) }
                    }
                    it.onFailure {
                        it.printStackTrace()
                    }
                }
            )
        }
    }

    private fun checkTablesAvailability(time: LocalDateTime) {
        setState { state -> state.copy(tableAvailabilityLoading = true) }
        getTablesAvailabilityUseCase.invoke(
            params = time,
            scope = viewModelScope,
            onResult = {
                setState { state -> state.copy(tableAvailabilityLoading = false) }
                it.onSuccess {
                    setState { state -> state.copy(availableTables = it) }
                }
                it.onFailure {
                    it.printStackTrace()
                }
            }
        )
    }

    private fun cancelReservation() {
        uiState.value.reservation?.let {
            cancelReservationUseCase(
                params = CancelReservationUseCase.Params(it.id),
                scope = viewModelScope,
                onResult = {
                    it.onSuccess {
                        if (it) {
                            setState { state -> state.copy(reservation = null) }
                        }
                    }
                }
            )
        }
    }
}