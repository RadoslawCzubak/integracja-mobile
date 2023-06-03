package pl.rczubak.stripetest.domain.usecase

import pl.rczubak.stripetest.base.UseCase
import pl.rczubak.stripetest.data.CafeRepository
import pl.rczubak.stripetest.domain.model.Table
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GetTablesAvailabilityUseCase(
    private val repository: CafeRepository
) : UseCase<List<Table>, LocalDateTime>() {
    override suspend fun action(params: LocalDateTime): List<Table> {
        return repository.getTablesAvailability(params.format(DateTimeFormatter.ISO_DATE_TIME))
            .map {
                Table(it.tableId, it.numberOfSeats)
            }
    }
}