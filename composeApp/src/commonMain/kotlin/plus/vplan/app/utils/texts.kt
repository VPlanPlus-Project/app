package plus.vplan.app.utils

import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.feature.search.domain.model.Result

fun Assessment.Type.toName() = when (this) {
    Assessment.Type.ORAL -> "Mündlich"
    Assessment.Type.PROJECT -> "Projekt"
    Assessment.Type.CLASS_TEST -> "Klassenarbeit/Klausur"
    Assessment.Type.SHORT_TEST -> "Leistungskontrolle"
    Assessment.Type.OTHER -> "Sonstiges"
}

fun Result.toName() = when (this) {
    Result.Group -> "Klassen"
    Result.Teacher -> "Lehrer"
    Result.Room -> "Räume"
    Result.Homework -> "Hausaufgaben"
    Result.Assessment -> "Leistungserhebungen"
    Result.Grade -> "Noten"
}