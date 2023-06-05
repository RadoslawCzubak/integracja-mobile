package pl.rczubak.stripetest.ui.employee

import androidx.lifecycle.viewModelScope
import pl.rczubak.stripetest.base.BaseViewModel
import pl.rczubak.stripetest.domain.usecase.GetEmployeeOrderUseCase
import pl.rczubak.stripetest.domain.usecase.UpdateOrderUseCase

class EmployeeViewModel(
    private val getEmployeeOrderUseCase: GetEmployeeOrderUseCase,
    private val updateOrderUseCase: UpdateOrderUseCase
) : BaseViewModel<EmployeeState, EmployeeEvent>() {
    override fun initialState(): EmployeeState = EmployeeState()

    override fun handleEvent(event: EmployeeEvent) {
        when (event) {
            EmployeeEvent.RefreshOrders -> getOrders()
            is EmployeeEvent.ServeMeal -> serveMail(event.orderId)
        }
    }

    private fun serveMail(orderId: Int) {
        updateOrderUseCase(
            scope = viewModelScope,
            params = UpdateOrderUseCase.Params(orderId),
            onResult = {
                it.onSuccess {
                    getOrders()
                }
            }
        )
    }

    private fun getOrders() {
        setState { state -> state.copy(isOrderLoading = true) }
        getEmployeeOrderUseCase(
            scope = viewModelScope,
            params = Unit,
            onResult = {
                setState { state -> state.copy(isOrderLoading = false) }
                it.onSuccess {
                    setState { state -> state.copy(orders = it) }
                }
            }
        )
    }
}