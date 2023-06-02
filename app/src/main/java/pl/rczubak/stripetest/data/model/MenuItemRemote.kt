package pl.rczubak.stripetest.data.model

import com.google.gson.annotations.SerializedName

data class MenuItemRemote(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Float,
)