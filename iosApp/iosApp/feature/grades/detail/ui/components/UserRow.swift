import SwiftUI

struct UserRow: View {
    var username: String

    var body: some View {
        MetadataRow {
            Text("Für")
                .font(tableNameStyle())
                .foregroundStyle(.gray)
        } value: {
            MetadataValueContainer(canEdit: false, onClick: {}) {
                Text(username)
                    .font(tableValueStyle())
                    .foregroundStyle(Color(.secondaryLabel))
                    .lineLimit(1)
            }
        }
    }
}
