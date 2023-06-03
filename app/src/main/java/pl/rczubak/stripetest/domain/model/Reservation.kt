package pl.rczubak.stripetest.domain.model

import java.time.LocalDateTime

data class Reservation(
    val id: Int,
    val tableId: Int,
    val reservationTime: LocalDateTime,
    val reservedAt: LocalDateTime,
    val userId: String,
)