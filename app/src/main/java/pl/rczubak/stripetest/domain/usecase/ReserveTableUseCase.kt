package pl.rczubak.stripetest.domain.usecase

import pl.rczubak.stripetest.base.UseCase
import pl.rczubak.stripetest.data.CafeRepository
import pl.rczubak.stripetest.domain.model.Reservation
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class ReserveTableUseCase(
    private val repository: CafeRepository
) : UseCase<Reservation, ReserveTableUseCase.Params>() {

    data class Params(
        val tableId: Int, val time: LocalDateTime
    )

    override suspend fun action(params: Params): Reservation {
        val response = repository.reserveTable(
            params.time.format(DateTimeFormatter.ISO_DATE_TIME), tableId = params.tableId
        )
        return with(response) {
            Reservation(
                id, tableId, LocalDateTime.ofEpochSecond(
                    reservationTimeTimeStamp.toFloat().toLong(), 0, ZoneOffset.UTC
                ), LocalDateTime.ofEpochSecond(
                    reservedAtTimestamp.toFloat().toLong(), 0, ZoneOffset.UTC
                ), userId
            )
        }
    }

}