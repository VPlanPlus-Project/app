package plus.vplan.app.data.source.indiware.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("VpMobil")
data class MobdatenClassData(
    @SerialName("FreieTage")
    @XmlChildrenName("ft")
    val holidays: List<String>,

    @SerialName("Klassen")
    @XmlChildrenName("Kl")
    val classes: List<Class>,

    @SerialName("Kopf")
    @Serializable
    val header: Header
) {

    @SerialName("Kopf")
    @Serializable
    data class Header(
        @SerialName("tageprowoche") val daysPerWeek: DaysPerWeek
    ) {
        @SerialName("tageprowoche")
        @Serializable
        data class DaysPerWeek(
            @XmlValue val daysPerWeek: Int
        )
    }

    @SerialName("Kl")
    @Serializable
    data class Class(
        @SerialName("Kurz") val name: ClassName,
    ) {

        @Serializable
        @SerialName("Kurz")
        data class ClassName(
            @XmlValue val name: String
        )
    }
}