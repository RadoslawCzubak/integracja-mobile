package pl.rczubak.stripetest.data.model

import com.google.gson.annotations.SerializedName

data class ReservationRemote(
    @SerializedName("table_id") val tableId: Int,
    @SerializedName("reservation_time") val reservationTime: String,
)