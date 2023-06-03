package pl.rczubak.stripetest.data

import pl.rczubak.stripetest.data.model.ReservationRemote
import pl.rczubak.stripetest.data.model.ReservationResponse
import pl.rczubak.stripetest.data.model.TableAvailabilityItem
import pl.rczubak.stripetest.data.service.CafeAPI

class CafeRepository(
    private val cafeAPI: CafeAPI
) {
    suspend fun getTablesAvailability(isoTime: String): List<TableAvailabilityItem> {
        return cafeAPI.getTablesAvailability(isoTime = isoTime)
    }

    suspend fun reserveTable(isoTime: String, tableId: Int): ReservationResponse {
        return cafeAPI.postReservation(ReservationRemote(tableId, isoTime))
    }

    suspend fun cancelReservation(reservationId: Int): Boolean {
        return cafeAPI.deleteReservation(reservationId = reservationId).code() == 202
    }
}