package pl.rczubak.stripetest.data.model

import com.google.gson.annotations.SerializedName

data class PaymentIntentMetadataResponse(
    @SerializedName("clientSecret") val clientSecret: String
)
