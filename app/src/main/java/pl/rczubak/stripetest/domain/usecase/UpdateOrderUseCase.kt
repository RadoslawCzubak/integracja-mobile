package pl.rczubak.stripetest.domain.usecase

import pl.rczubak.stripetest.base.UseCase
import pl.rczubak.stripetest.data.CafeRepository
import pl.rczubak.stripetest.domain.model.Order
import pl.rczubak.stripetest.utils.statusToEnum

class UpdateOrderUseCase(
    private val repository: CafeRepository
) : UseCase<Order, UpdateOrderUseCase.Params>() {

    data class Params(
        val orderId: Int
    )

    override suspend fun action(params: Params): Order {
        val response = repository.updateOrder(params.orderId)
        return with(response) {
            Order(
                userId,
                orderId,
                price,
                statusToEnum(status)
            )
        }
    }
}