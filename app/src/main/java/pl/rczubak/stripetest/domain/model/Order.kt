package pl.rczubak.stripetest.domain.model

data class Order(
    val userId: String,
    val id: Int,
    val price: Float,
    val status: OrderStatus
)

enum class OrderStatus(val string: String) {
    PAID("paid"), ACCEPTED("accepted"), CANCELLED("cancelled"), DELIVERED("delivered")
}
