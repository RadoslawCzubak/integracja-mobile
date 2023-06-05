package pl.rczubak.stripetest.ui.employee

import pl.rczubak.stripetest.base.Event
import pl.rczubak.stripetest.base.State
import pl.rczubak.stripetest.domain.model.Order

data class EmployeeState(
    val orders: List<Order> = listOf(),
    val isOrderLoading: Boolean = false
) : State

sealed interface EmployeeEvent : Event {
    object RefreshOrders : EmployeeEvent
    data class ServeMeal(val orderId: Int) : EmployeeEvent
}