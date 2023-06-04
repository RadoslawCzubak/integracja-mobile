package pl.rczubak.stripetest.data.service

import pl.rczubak.stripetest.data.model.*
import retrofit2.Response
import retrofit2.http.*

const val BASE_URL = "http://integracja.radoslav.pl"

interface CafeAPI {
    @POST("/reservation")
    suspend fun postReservation(@Body reservationForm: ReservationRemote): ReservationResponse

    @GET("/tables/availability")
    suspend fun getTablesAvailability(@Query("time") isoTime: String): List<TableAvailabilityItem>

    @DELETE("/reservation/{reservation_id}")
    suspend fun deleteReservation(@Path("reservation_id") reservationId: Int): Response<Unit>

    @GET("/menu")
    suspend fun getMenu(): List<MenuItemRemote>

    @POST("/order")
    suspend fun createOrder(@Body body: OrderForm): OrderResponse

    @GET("/order")
    suspend fun getOrders(): List<OrderResponse>

    @POST("/create-payment-intent/{order_id}")
    suspend fun createPaymentIntent(@Path("order_id") orderId: Int): PaymentIntentMetadataResponse

    @GET("/loyalty/points")
    suspend fun getLoyaltyPoints(): Int

}