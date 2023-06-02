package pl.rczubak.stripetest.data.model

import com.google.gson.annotations.SerializedName

data class TableAvailabilityItem(
    @SerializedName("nb_of_seats") val numberOfSeats: Int,
    @SerializedName("table_id") val tableId: Int
)