import SwiftUI

struct OptionalRow: View {
    var isOptional: Bool

    var body: some View {
        MetadataRow {
            Text("Optional")
                .font(tableNameStyle())
                .foregroundStyle(.gray)
        } value: {
            MetadataValueContainer(canEdit: false, onClick: {}) {
                Text(isOptional ? "Ja" : "Nein")
                    .font(tableValueStyle())
                    .foregroundStyle(Color(.secondaryLabel))
                    .lineLimit(1)
            }
        }
    }
}
