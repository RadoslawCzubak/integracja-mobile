package pl.rczubak.stripetest.domain.usecase

import pl.rczubak.stripetest.base.UseCase
import pl.rczubak.stripetest.data.CafeRepository

class CancelReservationUseCase(
    private val repository: CafeRepository
) : UseCase<Boolean, CancelReservationUseCase.Params>() {

    data class Params(
        val reservationId: Int,
    )

    override suspend fun action(params: Params): Boolean {
        val response = repository.cancelReservation(
            params.reservationId
        )
        return response
    }
}