@file:Suppress("unused")

package plus.vplan.app.feature.grades.di

import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module
import plus.vplan.app.feature.grades.detail.ui.GradeDetailDrawerLauncher
import plus.vplan.app.feature.grades.detail.ui.GradeDetailViewModel

object KoinHelper : KoinComponent {
    fun getGradeDetailViewModel(): GradeDetailViewModel = get()

    fun install(
        into: KoinApplication,
        detailDrawerLauncher: GradeDetailDrawerLauncher
    ) {
        into.koin.loadModules(listOf(module {
            single<GradeDetailDrawerLauncher> { detailDrawerLauncher }
        }))
    }
}
