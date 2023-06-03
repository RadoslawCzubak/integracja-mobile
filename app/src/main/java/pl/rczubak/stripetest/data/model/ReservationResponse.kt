package pl.rczubak.stripetest.data.model

import com.google.gson.annotations.SerializedName

data class ReservationResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("table_id") val tableId: Int,
    @SerializedName("reservation_time") val reservationTimeTimeStamp: String,
    @SerializedName("reserved_at") val reservedAtTimestamp: String,
    @SerializedName("user_id") val userId: String,
)