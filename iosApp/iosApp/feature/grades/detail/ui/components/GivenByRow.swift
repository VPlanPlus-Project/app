import SwiftUI

struct GivenByRow: View {
    var teacherName: String

    var body: some View {
        MetadataRow {
            Text("Erteilt von")
                .font(tableNameStyle())
                .foregroundStyle(.gray)
        } value: {
            MetadataValueContainer(canEdit: false, onClick: {}) {
                Text(teacherName)
                    .font(tableValueStyle())
                    .foregroundStyle(Color(.secondaryLabel))
                    .lineLimit(1)
            }
        }
    }
}
