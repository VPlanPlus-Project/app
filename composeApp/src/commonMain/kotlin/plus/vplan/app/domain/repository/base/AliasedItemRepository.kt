package plus.vplan.app.domain.repository.base

import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasedItem
import kotlin.uuid.Uuid

interface AliasedItemRepository<DBDTO, I: AliasedItem<*>>: ItemRepository<Uuid, I> {

    /**
     * @return The ID for the inserted or updated entity.
     */
    suspend fun upsert(item: DBDTO): Uuid
    suspend fun resolveAliasToLocalId(alias: Alias): Uuid?

    /**
     * Resolves the first existing alias to a local ID.
     */
    suspend fun resolveAliasesToLocalId(aliases: List<Alias>): Uuid? {
        return aliases.firstNotNullOfOrNull { resolveAliasToLocalId(it) }
    }
}