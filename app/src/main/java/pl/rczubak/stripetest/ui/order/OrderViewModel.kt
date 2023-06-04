package pl.rczubak.stripetest.ui.order

import androidx.lifecycle.viewModelScope
import pl.rczubak.stripetest.base.BaseViewModel
import pl.rczubak.stripetest.domain.usecase.CreateOrderUseCase
import pl.rczubak.stripetest.domain.usecase.GetLoyaltyPointsUseCase
import pl.rczubak.stripetest.domain.usecase.GetMenuUseCase
import pl.rczubak.stripetest.domain.usecase.GetOrdersUseCase
import pl.rczubak.stripetest.ui.order.model.OrderEvent
import pl.rczubak.stripetest.ui.order.model.OrderState

class OrderViewModel(
    private val getMenuUseCase: GetMenuUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val getOrdersUseCase: GetOrdersUseCase,
    private val getLoyaltyPointsUseCase: GetLoyaltyPointsUseCase
) : BaseViewModel<OrderState, OrderEvent>() {
    override fun initialState(): OrderState = OrderState()

    override fun handleEvent(event: OrderEvent) {
        when (event) {
            is OrderEvent.ChooseMenuItem -> handleMenuChoose(event.id)
            OrderEvent.CreateOrder -> createOrder()
            is OrderEvent.PayBill -> {}
            OrderEvent.RefreshMenu -> handleMenuRefresh()
            OrderEvent.RefreshOrder -> getOrders()
            OrderEvent.RefreshLoyalty -> getLoyalty()
        }
    }

    private fun handleMenuRefresh() {
        setState { state -> state.copy(isMenuLoading = true) }
        getMenuUseCase.invoke(
            scope = viewModelScope,
            params = Unit,
            onResult = {
                setState { state -> state.copy(isMenuLoading = false) }
                it.onSuccess {
                    setState { state -> state.copy(menuItems = it) }
                }
            }
        )
    }

    private fun getLoyalty() {
        getLoyaltyPointsUseCase(
            scope = viewModelScope,
            params = Unit,
            onResult = {
                it.onSuccess {
                    setState { state -> state.copy(loyaltyPoints = it) }
                }
            }
        )
    }

    private fun createOrder() {
        createOrderUseCase(
            params = CreateOrderUseCase.Params(uiState.value.chosenMenuItemsIds),
            scope = viewModelScope,
            onResult = {
                it.onSuccess {
                    getOrders()
                }
                it.onFailure { it.printStackTrace() }
            }
        )
    }

    private fun getOrders() {
        setState { state -> state.copy(isOrderListLoading = true) }
        getOrdersUseCase(
            params = Unit,
            scope = viewModelScope,
            onResult = {
                setState { state -> state.copy(isOrderListLoading = false) }
                it.onSuccess {
                    setState { state ->
                        state.copy(orders = it.sortedByDescending {
                            it.id
                        })
                    }
                }
            }
        )
    }

    private fun handleMenuChoose(id: Int) {
        val isChosen = id in uiState.value.chosenMenuItemsIds
        val chosenOnes = uiState.value.chosenMenuItemsIds.toMutableList()
        if (isChosen) {
            chosenOnes.remove(id)
        } else {
            chosenOnes.add(id)
        }
        setState { state -> state.copy(chosenMenuItemsIds = chosenOnes) }
    }
}