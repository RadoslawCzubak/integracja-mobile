package pl.rczubak.stripetest.data.model

import com.google.gson.annotations.SerializedName

data class ReservationResponse(
    @SerializedName("id") val id: String,
    @SerializedName("table_id") val tableId: Int,
    @SerializedName("reservation_time") val reservationTime: String,
    @SerializedName("reserved_at") val reservedAt: String,
    @SerializedName("user_id") val userId: String,
)