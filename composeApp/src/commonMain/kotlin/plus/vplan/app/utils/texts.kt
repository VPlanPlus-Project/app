package plus.vplan.app.utils

import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.feature.search.domain.model.SearchResult

fun Assessment.Type.toName() = when (this) {
    Assessment.Type.ORAL -> "Mündlich"
    Assessment.Type.PROJECT -> "Projekt"
    Assessment.Type.CLASS_TEST -> "Klassenarbeit/Klausur"
    Assessment.Type.SHORT_TEST -> "Leistungskontrolle"
    Assessment.Type.OTHER -> "Sonstiges"
}

fun SearchResult.Type.toName() = when (this) {
    SearchResult.Type.Group -> "Klassen"
    SearchResult.Type.Teacher -> "Lehrer"
    SearchResult.Type.Room -> "Räume"
    SearchResult.Type.Homework -> "Hausaufgaben"
    SearchResult.Type.Assessment -> "Leistungserhebungen"
    SearchResult.Type.Grade -> "Noten"
}