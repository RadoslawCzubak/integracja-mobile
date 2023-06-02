package pl.rczubak.stripetest.data.service

import pl.rczubak.stripetest.data.model.*
import retrofit2.http.*

const val BASE_URL = "http://integracja.radoslav.pl"

interface CafeAPI {
    @POST("/reservation")
    fun postReservation(@Body reservationForm: ReservationRemote)

    @GET("/tables/availability")
    fun getTablesAvailability(@Query("time") isoTime: String): List<TableAvailabilityItem>

    @DELETE("/reservation/{reservation_id}")
    fun deleteReservation(@Path("reservation_id") reservationId: Int)

    @GET("/menu")
    fun getMenu(): List<MenuItemRemote>

    @POST("/order")
    fun createOrder(): OrderResponse

    @POST("/create-payment-intent/{order_id}")
    fun createPaymentIntent(@Path("order_id") orderId: Int): PaymentIntentMetadataResponse

}