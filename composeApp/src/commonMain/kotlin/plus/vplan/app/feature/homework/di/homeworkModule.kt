package plus.vplan.app.feature.homework.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.homework.ui.components.NewHomeworkViewModel

val homeworkModule = module {
    viewModelOf(::NewHomeworkViewModel)
}