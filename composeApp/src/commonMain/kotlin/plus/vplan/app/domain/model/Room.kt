package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.Item

data class Room(
    val id: Int,
    val school: Int,
    val name: String
) : Item {
    override fun getEntityId(): String = this.id.toString()
}