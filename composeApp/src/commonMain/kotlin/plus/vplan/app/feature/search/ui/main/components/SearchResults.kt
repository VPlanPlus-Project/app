package plus.vplan.app.feature.search.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.App
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.calendar.ui.components.agenda.GradeCard
import plus.vplan.app.feature.calendar.ui.components.calendar.CalendarView
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.utils.findCurrentLessons
import plus.vplan.app.utils.getLastLessonEnd
import plus.vplan.app.utils.getNextLessonStart
import plus.vplan.app.utils.now
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.regularTimeFormat
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.toName
import plus.vplan.app.utils.untilRelativeText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.book_open
import vplanplus.composeapp.generated.resources.door_closed
import vplanplus.composeapp.generated.resources.file_badge
import vplanplus.composeapp.generated.resources.notebook_text
import vplanplus.composeapp.generated.resources.search_x
import vplanplus.composeapp.generated.resources.square_user_round
import vplanplus.composeapp.generated.resources.users

@Composable
private fun sectionTitleFont() = MaterialTheme.typography.titleMedium

@Composable
fun SearchResults(
    profile: Profile,
    dayType: Day.DayType,
    date: LocalDate,
    results: Map<SearchResult.Type, List<SearchResult>>,
    onHomeworkClicked: (homeworkId: Int) -> Unit,
    onAssessmentClicked: (assessmentId: Int) -> Unit,
    onGradeClicked: (gradeId: Int) -> Unit
) {
    if (results.all { it.value.isEmpty() }) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Icon(
                painter = painterResource(Res.drawable.search_x),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Keine Ergebnisse gefunden",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Versuche es mit einem anderen Suchbegriff",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    var visibleResult by remember { mutableStateOf<SearchResult.SchoolEntity?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        results.toList()
            .sortedBy { (type, _) -> typeTypeSortings.indexOf(type).let { if (it == -1) typeTypeSortings.size + 1 else it } }
            .filter { it.second.isNotEmpty() }
            .forEach { (type, results) ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(when (type) {
                            SearchResult.Type.Group -> Res.drawable.users
                            SearchResult.Type.Teacher -> Res.drawable.square_user_round
                            SearchResult.Type.Room -> Res.drawable.door_closed
                            SearchResult.Type.Homework -> Res.drawable.book_open
                            SearchResult.Type.Assessment -> Res.drawable.notebook_text
                            SearchResult.Type.Grade -> Res.drawable.file_badge
                        }),
                        contentDescription = null,
                        modifier = Modifier.size(sectionTitleFont().lineHeight.toDp())
                    )
                    Row {
                        Text(
                            text = type.toName(),
                            style = sectionTitleFont(),
                            modifier = Modifier.alignByBaseline()
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = "(${results.size})",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }
                Spacer(Modifier.size(4.dp))
                when (type) {
                    SearchResult.Type.Group -> GroupResults(results.filterIsInstance<SearchResult.SchoolEntity.Group>()) { visibleResult = it }
                    SearchResult.Type.Room -> RoomResults(results.filterIsInstance<SearchResult.SchoolEntity.Room>()) { visibleResult = it }
                    SearchResult.Type.Teacher -> TeacherResults(results.filterIsInstance<SearchResult.SchoolEntity.Teacher>()) { visibleResult = it }
                    SearchResult.Type.Homework -> {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            results.filterIsInstance<SearchResult.Homework>().forEach { result ->
                                plus.vplan.app.feature.calendar.ui.components.agenda.HomeworkCard(
                                    homework = result.homework,
                                    profile = profile,
                                    onClick = { onHomeworkClicked(result.homework.id) }
                                )
                            }
                        }
                    }
                    SearchResult.Type.Assessment -> {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            results.filterIsInstance<SearchResult.Assessment>().forEach { result ->
                                plus.vplan.app.feature.calendar.ui.components.agenda.AssessmentCard(
                                    assessment = result.assessment,
                                    onClick = { onAssessmentClicked(result.assessment.id) }
                                )
                            }
                        }
                    }
                    SearchResult.Type.Grade -> {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            results.filterIsInstance<SearchResult.Grade>().forEach { result ->
                                GradeCard(
                                    grade = result.grade,
                                    onClick = { onGradeClicked(result.grade.id) }
                                )
                            }
                        }
                    }
                }
            }
    }

    visibleResult?.let {
        LessonsDrawer(
            date = date,
            lessons = it.lessons,
            dayType = dayType,
            type = it.type,
            name = it.name,
            onDismiss = { visibleResult = null }
        )
    }
}

private val typeTypeSortings = listOf(
    SearchResult.Type.Group,
    SearchResult.Type.Teacher,
    SearchResult.Type.Room,
    SearchResult.Type.Homework,
    SearchResult.Type.Assessment,
    SearchResult.Type.Grade,
)

@Composable
private fun GroupResults(
    results: List<SearchResult.SchoolEntity.Group>,
    onClick: (result: SearchResult.SchoolEntity.Group) -> Unit
) {
    val localDensity = LocalDensity.current
    var groupNameWidth by remember { mutableStateOf(0.dp) }
    results.forEachIndexed { i, result ->
        key(result.group.id) {
            val currentLessons = remember { mutableStateListOf<Lesson>() }
            var end by remember { mutableStateOf<LocalTime?>(null) }
            var hasLessonsLoaded by remember { mutableStateOf(false) }
            LaunchedEffect(result.lessons) {
                currentLessons.clear()
                currentLessons.addAll(result.lessons.findCurrentLessons(LocalTime.now()).toMutableStateList())
                if (result.lessons.isNotEmpty()) end = result.lessons.getLastLessonEnd()
                hasLessonsLoaded = true
            }
            if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick(result) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = groupNameWidth, maxWidth = 72.dp)
                        .onSizeChanged { with(localDensity) { it.width.toDp().let { width -> if (width > groupNameWidth) groupNameWidth = width } } }
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = result.group.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column {
                    if (result.lessons.isEmpty()) Text(
                        text = "Heute keine Stunden",
                        style = MaterialTheme.typography.bodySmall
                    )
                    else if (currentLessons.isEmpty() && !hasLessonsLoaded) LineShimmer()
                    else if (currentLessons.isEmpty() && hasLessonsLoaded) Text(
                        text = "Momentan keine Stunden",
                        style = MaterialTheme.typography.bodySmall
                    )
                    else {
                        val roomIds = currentLessons.mapNotNull { it.roomIds }.flatten().distinct()
                        Text(
                            text = if (roomIds.isEmpty()) "Momentan nicht im Haus"
                            else {
                                val rooms = roomIds.map { App.roomSource.getById(it) }.collectAsResultingFlow().value
                                val until = currentLessons.map { it.lessonTime }.collectAsResultingFlow().value.maxOfOrNull { it.end }
                                buildString {
                                    append("Momentan in ${rooms.map { it.name }.sorted().joinToString()}")
                                    if (until != null) append(" (bis $until)")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (end == null && !hasLessonsLoaded) LineShimmer()
                    else if (hasLessonsLoaded && end != null) Text(
                        text = "Schulschluss um ${end!!.format(regularTimeFormat)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomResults(
    results: List<SearchResult.SchoolEntity.Room>,
    onClick: (result: SearchResult.SchoolEntity.Room) -> Unit
) {
    val localDensity = LocalDensity.current
    var roomNameWidth by remember { mutableStateOf(0.dp) }
    results.forEachIndexed { i, result ->
        key(result.room.id) {
            val currentLessons = remember { mutableStateListOf<Lesson>() }
            var nextLesson by remember { mutableStateOf<LocalTime?>(null) }
            var hasLessonsLoaded by remember { mutableStateOf(false) }
            LaunchedEffect(result.lessons) {
                currentLessons.clear()
                currentLessons.addAll(result.lessons.findCurrentLessons(LocalTime.now()).toMutableStateList())
                nextLesson = result.lessons.getNextLessonStart(LocalTime.now())
                hasLessonsLoaded = true
            }
            if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick(result) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = roomNameWidth, maxWidth = 72.dp)
                        .onSizeChanged { with(localDensity) { it.width.toDp().let { width -> if (width > roomNameWidth) roomNameWidth = width } } }
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = result.room.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column {
                    if (result.lessons.isEmpty()) Text(
                        text = "Heute nicht belegt",
                        style = MaterialTheme.typography.bodySmall
                    )
                    else if (currentLessons.isEmpty() && !hasLessonsLoaded) LineShimmer()
                    else if (currentLessons.isEmpty() && hasLessonsLoaded) Text(
                        text = "Momentan nicht belegt",
                        style = MaterialTheme.typography.bodySmall
                    )
                    else {
                        val groupIds = currentLessons.map { it.groupIds }.flatten().distinct()
                        Text(
                            text = if (groupIds.isEmpty()) "Momentan nicht belegt (Keine Gruppen zugeteilt)"
                            else {
                                val groups = groupIds.map { App.groupSource.getById(it) }.collectAsResultingFlow().value
                                "Momentan belegt von ${groups.map { it.name }.sorted().joinToString()}"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (nextLesson == null && !hasLessonsLoaded) LineShimmer()
                    else if (currentLessons.isEmpty() && hasLessonsLoaded && nextLesson != null) Text(
                        text = "Nächster Unterricht ab ${nextLesson!!.format(regularTimeFormat)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TeacherResults(
    results: List<SearchResult.SchoolEntity.Teacher>,
    onClick: (result: SearchResult.SchoolEntity.Teacher) -> Unit
) {
    val localDensity = LocalDensity.current
    var teacherNameWidth by remember { mutableStateOf(0.dp) }
    results.forEachIndexed { i, result ->
        key(result.teacher.id) {
            val currentLessons = remember { mutableStateListOf<Lesson>() }
            var end by remember { mutableStateOf<LocalTime?>(null) }
            var hasLessonsLoaded by remember { mutableStateOf(false) }
            LaunchedEffect(result.lessons) {
                currentLessons.clear()
                currentLessons.addAll(result.lessons.findCurrentLessons(LocalTime(8, 30)).toMutableStateList())
                if (result.lessons.isNotEmpty()) end = result.lessons.getLastLessonEnd()
                hasLessonsLoaded = true
            }
            if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick(result) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = teacherNameWidth, maxWidth = 72.dp)
                        .onSizeChanged { with(localDensity) { it.width.toDp().let { width -> if (width > teacherNameWidth) teacherNameWidth = width } } }
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = result.teacher.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column {
                    if (result.lessons.isEmpty()) Text(
                        text = "Heute keine Stunden",
                        style = MaterialTheme.typography.bodySmall
                    )
                    else if (currentLessons.isEmpty() && !hasLessonsLoaded) LineShimmer()
                    else if (currentLessons.isEmpty() && hasLessonsLoaded) Text(
                        text = "Momentan keine Stunden",
                        style = MaterialTheme.typography.bodySmall
                    )
                    else {
                        val roomIds = currentLessons.mapNotNull { it.roomIds }.flatten().distinct()
                        Text(
                            text = if (roomIds.isEmpty()) "Momentan nicht im Haus"
                            else {
                                val rooms = roomIds.map { App.roomSource.getById(it) }.collectAsResultingFlow().value
                                val until = currentLessons.map { it.lessonTime }.collectAsResultingFlow().value.maxOfOrNull { it.end }
                                buildString {
                                    append("Momentan in ${rooms.map { it.name }.sorted().joinToString()}")
                                    if (until != null) append(" (bis $until)")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (end == null && !hasLessonsLoaded) LineShimmer()
                    else if (hasLessonsLoaded && end != null) Text(
                        text = "Letzte Stunde endet ${end!!.format(regularTimeFormat)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonsDrawer(
    date: LocalDate,
    dayType: Day.DayType,
    lessons: List<Lesson>,
    type: SearchResult.Type,
    name: String,
    onDismiss: () -> Unit
)  {
    val sheetState = rememberModalBottomSheetState(true)

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp))
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = buildString {
                        append(when (type) {
                            SearchResult.Type.Group -> "Klasse"
                            SearchResult.Type.Teacher -> "Lehrer"
                            SearchResult.Type.Room -> "Raum"
                            else -> "Anderes"
                        })
                        append(" ")
                        append(name)
                    },
                    style = MaterialTheme.typography.headlineLarge,
                )

                Text(
                    text = buildString {
                        append(when (lessons.size) {
                            0 -> "Keine Stunden"
                            1 -> "Eine Stunde"
                            else -> "${lessons.size} Stunden"
                        })
                        append(" für ")
                        append((LocalDate.now() untilRelativeText date) ?: date.format(regularDateFormat))
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                CalendarView(
                    profile = null,
                    dayType = dayType,
                    date = date,
                    lessons = lessons,
                    assessments = emptyList(),
                    homework = emptyList(),
                    autoLimitTimeSpanToLessons = true,
                    info = null,
                    contentScrollState = null,
                    onHomeworkClicked = {},
                    onAssessmentClicked = {}
                )
            }
        }
    }
}

@Composable
private fun LineShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.typography.bodyMedium.lineHeight.toDp()),
        contentAlignment = Alignment.CenterStart
    ) {
        ShimmerLoader(
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.typography.bodyMedium.fontSize.toDp())
                .clip(RoundedCornerShape(8.dp))
        )
    }
}