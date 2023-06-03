package pl.rczubak.stripetest.ui.order.model

import pl.rczubak.stripetest.base.Event
import pl.rczubak.stripetest.base.State


data class OrderState(
    val test: String = ""
) : State

sealed class OrderEvent : Event {

}