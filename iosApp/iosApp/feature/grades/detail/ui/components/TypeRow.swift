import SwiftUI

struct TypeRow: View {
    var type: String

    var body: some View {
        MetadataRow {
            Text("Kategorie")
                .font(tableNameStyle())
                .foregroundStyle(.gray)
        } value: {
            MetadataValueContainer(canEdit: false, onClick: {}) {
                Text(type)
                    .font(tableValueStyle())
                    .foregroundStyle(Color(.secondaryLabel))
                    .lineLimit(1)
            }
        }
    }
}
