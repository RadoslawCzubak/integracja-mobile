package pl.rczubak.stripetest.data.model

import com.google.gson.annotations.SerializedName

data class OrderForm(
    @SerializedName("product_ids") val productIds: List<Int>
)