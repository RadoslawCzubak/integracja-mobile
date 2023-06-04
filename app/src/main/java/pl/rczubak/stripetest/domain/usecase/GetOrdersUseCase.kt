package pl.rczubak.stripetest.domain.usecase

import pl.rczubak.stripetest.base.UseCase
import pl.rczubak.stripetest.data.CafeRepository
import pl.rczubak.stripetest.domain.model.Order
import pl.rczubak.stripetest.utils.statusToEnum

class GetOrdersUseCase(
    private val repository: CafeRepository
) : UseCase<List<Order>, Unit>() {
    override suspend fun action(params: Unit): List<Order> {
        return repository.getOrders().map {
            with(it) {
                Order(
                    price = price,
                    status = statusToEnum(status),
                    userId = userId,
                    id = orderId
                )
            }
        }
    }
}