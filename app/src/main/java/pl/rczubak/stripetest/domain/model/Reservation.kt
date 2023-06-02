package pl.rczubak.stripetest.domain.model

import java.time.LocalDateTime

data class Reservation(
    val id: String,
    val tableId: Int,
    val reservationTime: LocalDateTime,
    val reservedAt: LocalDateTime,
    val userId: String,
)