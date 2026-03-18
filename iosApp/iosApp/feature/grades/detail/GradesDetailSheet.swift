import SwiftUI
import KMPObservableViewModelCore
import KMPObservableViewModelSwiftUI
import VPlanPlusShared

struct GradesDetailView: View {
    let gradeId: Int
    let onDismiss: () -> Void

    @StateViewModel var viewModel: VPlanPlusShared.GradeDetailViewModel = VPlanPlusShared.KoinHelper.shared.getGradeDetailViewModel()

    var body: some View {
        NavigationStack {
            ScrollView {
                GradeDetailPage(state: viewModel.state, onEvent: { viewModel.onEvent(event: $0) })
                    .padding(.top, 16)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button {
                        onDismiss()
                    } label: {
                        Image(systemName: "xmark")
                    }
                }

                ToolbarItem(placement: .principal) {
                    VStack(spacing: 2) {
                        Text(viewModel.state.title.isEmpty ? " " : viewModel.state.title)
                            .font(.headline)
                        Text(viewModel.state.subtitle ?? " ")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

                ToolbarItem(placement: .topBarTrailing) {
                    if viewModel.state.lockState == GradeLockState.unlocked {
                        Button {
                            viewModel.onEvent(event: GradeDetailEvent.LockGrades())
                        } label: {
                            Image(systemName: "lock")
                        }
                    }
                }

                ToolbarItem(placement: .primaryAction) {
                    Button {
                        viewModel.onEvent(event: GradeDetailEvent.Reload())
                    } label: {
                        Group {
                            if viewModel.state.reloadingState == ModelUnoptimisticTaskState.inprogress {
                                ProgressView()
                            } else {
                                let iconName = switch viewModel.state.reloadingState {
                                case ModelUnoptimisticTaskState.success: "checkmark"
                                case ModelUnoptimisticTaskState.error: "info.circle"
                                default: "arrow.clockwise"
                                }
                                Image(systemName: iconName)
                            }
                        }
                        .animation(.default.speed(5), value: viewModel.state.reloadingState)
                        .contentTransition(.symbolEffect(.replace.downUp.wholeSymbol))
                    }
                    .disabled(viewModel.state.reloadingState == ModelUnoptimisticTaskState.inprogress)
                }
            }
        }
        .onAppear {
            viewModel.doInit(gradeId: Int32(gradeId))
        }
    }
}

// MARK: - GradeDetailPage

struct GradeDetailPage: View {
    let state: GradeDetailState
    let onEvent: (GradeDetailEvent) -> Void

    var body: some View {
        if let grade = state.grade {
            gradeContent(grade: grade)
                .padding(.horizontal, 16)
        }
    }

    @ViewBuilder
    private func gradeContent(grade: ModelBesteSchuleGrade) -> some View {
        let vppId = state.gradeUser

        if state.lockState?.canAccess != true {
            VStack(alignment: .leading, spacing: 8) {
                Text("Noten entsperren")
                    .font(.largeTitle)
                    .lineLimit(1)
                    .truncationMode(.tail)

                Button(action: {
                    onEvent(GradeDetailEvent.RequestGradesUnlock())
                }) {
                    HStack(spacing: 8) {
                        Image(systemName: "lock.open")
                        Text("Entsperren")
                    }
                    .foregroundStyle(Color(.systemTeal))
                    .frame(maxWidth: .infinity)
                }
            }
        } else {
            VStack(alignment: .leading, spacing: 0) {
                SubjectGroupRow(
                    canEdit: false,
                    allowGroup: false,
                    onClick: {},
                    subject: grade.collection.subject.shortName,
                    group: nil
                )

                TypeRow(type: grade.collection.type)
                IntervalRow(
                    schoolYearName: grade.collection.interval.year.name,
                    intervalName: grade.collection.interval.name
                )
                GivenAtRow(date: grade.givenAt.toDate())
                GivenByRow(
                    teacherName: "\(grade.collection.teacher.forename) \(grade.collection.teacher.surname)"
                )

                if let vppId = vppId {
                    UserRow(username: vppId.name)
                }

                OptionalRow(isOptional: grade.isOptional)

                IsSelectedForFinalGradeRow(
                    useForFinalGrade: grade.isSelectedForFinalGrade,
                    wasNotGiven: grade.value == nil,
                    onToggle: {
                        onEvent(GradeDetailEvent.ToggleConsiderForFinalGrade())
                    }
                )

                Divider()
                    .padding(.vertical, 8)

                Text(grade.collection.name)
                    .font(tableValueStyle())
            }
        }
    }
}
