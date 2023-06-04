package pl.rczubak.stripetest.domain.usecase

import pl.rczubak.stripetest.base.UseCase
import pl.rczubak.stripetest.data.CafeRepository

class GetLoyaltyPointsUseCase(
    private val repository: CafeRepository
) : UseCase<Int, Unit>() {
    override suspend fun action(params: Unit): Int {
        return repository.getLoyaltyPoints()
    }
}