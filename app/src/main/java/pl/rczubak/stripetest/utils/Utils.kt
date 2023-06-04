package pl.rczubak.stripetest.utils

import pl.rczubak.stripetest.domain.model.OrderStatus

fun statusToEnum(string: String): OrderStatus {
    return when (string) {
        OrderStatus.ACCEPTED.string -> OrderStatus.ACCEPTED
        OrderStatus.CANCELLED.string -> OrderStatus.ACCEPTED
        OrderStatus.DELIVERED.string -> OrderStatus.DELIVERED
        OrderStatus.PAID.string -> OrderStatus.PAID
        else -> OrderStatus.ACCEPTED
    }
}