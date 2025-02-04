package plus.vplan.app.utils

import plus.vplan.app.domain.model.Assessment

fun Assessment.Type.toName() = when (this) {
    Assessment.Type.ORAL -> "Mündlich"
    Assessment.Type.PROJECT -> "Projekt"
    Assessment.Type.CLASS_TEST -> "Klassenarbeit/Klausur"
    Assessment.Type.SHORT_TEST -> "Leistungskontrolle"
    Assessment.Type.OTHER -> "Sonstiges"
}