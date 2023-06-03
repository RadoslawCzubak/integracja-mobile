package pl.rczubak.stripetest.ui.order

import pl.rczubak.stripetest.base.BaseViewModel
import pl.rczubak.stripetest.data.CafeRepository
import pl.rczubak.stripetest.ui.order.model.OrderEvent
import pl.rczubak.stripetest.ui.order.model.OrderState

class OrderViewModel(
    private val cafeRepository: CafeRepository
) : BaseViewModel<OrderState, OrderEvent>() {
    override fun initialState(): OrderState = OrderState()

    override fun handleEvent(event: OrderEvent) {
        when (event) {
            else -> {}
        }
    }
}