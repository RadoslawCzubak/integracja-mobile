package pl.rczubak.stripetest.data.model

import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("user_id") val userId: String,
    @SerializedName("id") val orderId: Int,
    @SerializedName("price") val price: Float,
    @SerializedName("status") val status: String,
)