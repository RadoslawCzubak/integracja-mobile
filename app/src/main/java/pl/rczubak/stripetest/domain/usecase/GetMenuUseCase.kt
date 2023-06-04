package pl.rczubak.stripetest.domain.usecase

import pl.rczubak.stripetest.base.UseCase
import pl.rczubak.stripetest.data.CafeRepository
import pl.rczubak.stripetest.domain.model.MenuItem
import java.time.LocalDateTime

class GetMenuUseCase(
    private val repository: CafeRepository
) : UseCase<List<MenuItem>, Unit>() {
    override suspend fun action(params: Unit): List<MenuItem> {
        return repository.getMenu().map {
            MenuItem(
                it.id,
                it.name,
                it.price.toString()
            )
        }
    }
}