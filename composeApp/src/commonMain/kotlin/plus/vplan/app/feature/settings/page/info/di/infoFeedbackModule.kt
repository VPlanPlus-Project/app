package plus.vplan.app.feature.settings.page.info.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.settings.page.info.domain.usecase.GetFeedbackMetadataUseCase
import plus.vplan.app.feature.settings.page.info.domain.usecase.SendFeedbackUseCase
import plus.vplan.app.feature.settings.page.info.ui.components.FeedbackDrawerViewModel

val infoFeedbackModule = module {
    singleOf(::GetFeedbackMetadataUseCase)
    singleOf(::SendFeedbackUseCase)

    viewModelOf(::FeedbackDrawerViewModel)
}