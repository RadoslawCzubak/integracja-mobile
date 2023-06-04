package pl.rczubak.stripetest.domain.usecase

import pl.rczubak.stripetest.base.UseCase
import pl.rczubak.stripetest.data.CafeRepository
import pl.rczubak.stripetest.domain.model.Order
import pl.rczubak.stripetest.utils.statusToEnum

class CreateOrderUseCase(
    private val repository: CafeRepository
) : UseCase<Order, CreateOrderUseCase.Params>() {

    data class Params(
        val productIds: List<Int>
    )

    override suspend fun action(params: Params): Order {
        val response = repository.createOrder(params.productIds)
        return with(response) {
            Order(
                price = price,
                status = statusToEnum(status),
                userId = userId,
                id = orderId
            )
        }
    }
}