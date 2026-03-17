import SwiftUI

struct IsSelectedForFinalGradeRow: View {
    var useForFinalGrade: Bool
    var wasNotGiven: Bool
    var onToggle: () -> Void

    var body: some View {
        MetadataRow {
            Text("Im Durchschnitt berücksichtigen")
                .font(tableNameStyle())
                .foregroundStyle(.gray)
        } value: {
            MetadataValueContainer(canEdit: !wasNotGiven, editStyling: false, onClick: onToggle) {
                Toggle("", isOn: Binding(
                    get: { useForFinalGrade },
                    set: { _ in onToggle() }
                ))
                .disabled(wasNotGiven)
                .labelsHidden()
            }
        }
    }
}
