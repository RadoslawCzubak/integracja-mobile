package pl.rczubak.stripetest.ui.order.model

import pl.rczubak.stripetest.base.Event
import pl.rczubak.stripetest.base.State
import pl.rczubak.stripetest.domain.model.MenuItem
import pl.rczubak.stripetest.domain.model.Order


data class OrderState(
    val orders: List<Order> = listOf(),
    val menuItems: List<MenuItem> = listOf(),
    val chosenMenuItemsIds: List<Int> = listOf(),
    val isMenuLoading: Boolean = false,
    val isOrderListLoading: Boolean = false,
    val loyaltyPoints: Int = 0,
) : State

sealed interface OrderEvent : Event {
    object RefreshMenu : OrderEvent
    object RefreshOrder : OrderEvent
    object CreateOrder : OrderEvent
    object RefreshLoyalty : OrderEvent
    data class ChooseMenuItem(val id: Int) : OrderEvent
    data class PayBill(val id: Int) : OrderEvent
}