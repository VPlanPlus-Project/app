package plus.vplan.app.feature.search.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.App
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.calendar.ui.components.agenda.GradeCard
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.utils.findCurrentLessons
import plus.vplan.app.utils.getLastLessonEnd
import plus.vplan.app.utils.getNextLessonStart
import plus.vplan.app.utils.now
import plus.vplan.app.utils.regularTimeFormat
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.toName
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.book_open
import vplanplus.composeapp.generated.resources.door_closed
import vplanplus.composeapp.generated.resources.file_badge
import vplanplus.composeapp.generated.resources.notebook_text
import vplanplus.composeapp.generated.resources.square_user_round
import vplanplus.composeapp.generated.resources.users

@Composable
private fun sectionTitleFont() = MaterialTheme.typography.titleMedium

@Composable
fun SearchResults(
    profile: Profile,
    results: Map<SearchResult.Result, List<SearchResult>>,
    onHomeworkClicked: (homeworkId: Int) -> Unit,
    onAssessmentClicked: (assessmentId: Int) -> Unit,
    onGradeClicked: (gradeId: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        results.toList()
            .sortedBy { (type, _) -> resultTypeSorting.indexOf(type).let { if (it == -1) resultTypeSorting.size + 1 else it } }
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
                            SearchResult.Result.Group -> Res.drawable.users
                            SearchResult.Result.Teacher -> Res.drawable.square_user_round
                            SearchResult.Result.Room -> Res.drawable.door_closed
                            SearchResult.Result.Homework -> Res.drawable.book_open
                            SearchResult.Result.Assessment -> Res.drawable.notebook_text
                            SearchResult.Result.Grade -> Res.drawable.file_badge
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
                when (type) {
                    SearchResult.Result.Group -> GroupResults(results.filterIsInstance<SearchResult.SchoolEntity.Group>())
                    SearchResult.Result.Room -> RoomResults(results.filterIsInstance<SearchResult.SchoolEntity.Room>())
                    SearchResult.Result.Teacher -> TeacherResults(results.filterIsInstance<SearchResult.SchoolEntity.Teacher>())
                    SearchResult.Result.Homework -> {
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
                    SearchResult.Result.Assessment -> {
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
                    SearchResult.Result.Grade -> {
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
}

private val resultTypeSorting = listOf(
    SearchResult.Result.Group,
    SearchResult.Result.Teacher,
    SearchResult.Result.Room,
    SearchResult.Result.Homework,
    SearchResult.Result.Assessment,
    SearchResult.Result.Grade,
)

@Composable
private fun GroupResults(
    results: List<SearchResult.SchoolEntity.Group>
) {
    val localDensity = LocalDensity.current
    var groupNameWidth by remember { mutableStateOf(0.dp) }
    results.forEachIndexed { i, result ->
        key(result.group.id) {
            val currentLessons = remember { mutableStateListOf<Lesson>() }
            var end by remember { mutableStateOf<LocalTime?>(null) }
            var hasLessonsLoaded by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
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
                    .clickable {  }
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
                        val roomIds = currentLessons.mapNotNull { it.rooms }.flatten().distinct()
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
    results: List<SearchResult.SchoolEntity.Room>
) {
    val localDensity = LocalDensity.current
    var roomNameWidth by remember { mutableStateOf(0.dp) }
    results.forEachIndexed { i, result ->
        key(result.room.id) {
            val currentLessons = remember { mutableStateListOf<Lesson>() }
            var nextLesson by remember { mutableStateOf<LocalTime?>(null) }
            var hasLessonsLoaded by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
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
                    .clickable {  }
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
                        val groupIds = currentLessons.map { it.groups }.flatten().distinct()
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
    results: List<SearchResult.SchoolEntity.Teacher>
) {
    val localDensity = LocalDensity.current
    var teacherNameWidth by remember { mutableStateOf(0.dp) }
    results.forEachIndexed { i, result ->
        key(result.teacher.id) {
            val currentLessons = remember { mutableStateListOf<Lesson>() }
            var end by remember { mutableStateOf<LocalTime?>(null) }
            var hasLessonsLoaded by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
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
                    .clickable {  }
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
                        val roomIds = currentLessons.mapNotNull { it.rooms }.flatten().distinct()
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