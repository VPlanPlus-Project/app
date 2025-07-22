package plus.vplan.app.data.source.indiware.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("splan")
data class WplanBaseData(
    @SerialName("Kopf") val header: Header,
    @SerialName("Schulwochen") @XmlChildrenName("Sw") val schoolWeeks: List<SchoolWeek>
) {

    @Serializable
    @SerialName("Kopf")
    data class Header(
        @SerialName("schulname") val schoolName: SchoolName? = null
    ) {
        @SerialName("schulname")
        @Serializable
        data class SchoolName(
            @XmlValue val name: String
        )
    }

    @Serializable
    @SerialName("Sw")
    data class SchoolWeek(
        @SerialName("SwKw") val calendarWeek: Int,
        @SerialName("SwDatumVon") val start: String,
        @SerialName("SwDatumBis") val end: String,
        @SerialName("SwWo") val weekType: String,
        @XmlValue val weekIndex: Int
    )

}