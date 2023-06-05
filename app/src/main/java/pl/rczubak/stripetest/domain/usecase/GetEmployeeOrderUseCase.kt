package pl.rczubak.stripetest.domain.usecase

import pl.rczubak.stripetest.base.UseCase
import pl.rczubak.stripetest.data.CafeRepository
import pl.rczubak.stripetest.domain.model.Order
import pl.rczubak.stripetest.utils.statusToEnum

class GetEmployeeOrderUseCase(
    private val repository: CafeRepository
) : UseCase<List<Order>, Unit>() {
    override suspend fun action(params: Unit): List<Order> {
        return repository.getEmployeeOrders()
            .map {
                Order(
                    it.userId,
                    it.orderId,
                    it.price,
                    statusToEnum(it.status)
                )
            }
    }
}