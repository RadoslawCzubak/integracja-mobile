package pl.rczubak.stripetest.data

import pl.rczubak.stripetest.data.model.*
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

    suspend fun getMenu(): List<MenuItemRemote> {
        return cafeAPI.getMenu()
    }

    suspend fun createOrder(productIds: List<Int>): OrderResponse {
        return cafeAPI.createOrder(OrderForm(productIds))
    }

    suspend fun getOrders(): List<OrderResponse> {
        return cafeAPI.getOrders()
    }

    suspend fun getPaymentInfo(paymentId: Int): PaymentIntentMetadataResponse {
        return cafeAPI.createPaymentIntent(paymentId)
    }

    suspend fun getLoyaltyPoints(): Int {
        return cafeAPI.getLoyaltyPoints()
    }
}