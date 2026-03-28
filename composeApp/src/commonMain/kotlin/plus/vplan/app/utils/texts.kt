package plus.vplan.app.utils

import plus.vplan.app.feature.search.domain.model.SearchResult

fun SearchResult.Type.toName() = when (this) {
    SearchResult.Type.Group -> "Klassen"
    SearchResult.Type.Teacher -> "Lehrer"
    SearchResult.Type.Room -> "Räume"
    SearchResult.Type.Homework -> "Hausaufgaben"
    SearchResult.Type.Assessment -> "Leistungserhebungen"
    SearchResult.Type.Grade -> "Noten"
}