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

fun SearchResult.Result.toName() = when (this) {
    SearchResult.Result.Group -> "Klassen"
    SearchResult.Result.Teacher -> "Lehrer"
    SearchResult.Result.Room -> "Räume"
    SearchResult.Result.Homework -> "Hausaufgaben"
    SearchResult.Result.Assessment -> "Leistungserhebungen"
    SearchResult.Result.Grade -> "Noten"
}