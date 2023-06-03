package pl.rczubak.stripetest.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class FlowUseCase<out Type, in Params> {
    abstract suspend fun action(params: Params): Flow<Type>

    private var flowJob: Job? = null

    operator fun invoke(
        params: Params,
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        onResult: (Type) -> Unit
    ) {
        flowJob = scope.launch(dispatcher) {
            action(params).cancellable().collectLatest {
                onResult(it)
            }
        }
    }

    fun cancel(){
        flowJob?.cancel()
    }
}
