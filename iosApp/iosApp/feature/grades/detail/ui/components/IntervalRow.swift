import SwiftUI

struct IntervalRow: View {
    var schoolYearName: String
    var intervalName: String

    var body: some View {
        MetadataRow {
            Text("Schuljahr")
                .font(tableNameStyle())
                .foregroundStyle(.gray)
        } value: {
            MetadataValueContainer(canEdit: false, onClick: {}) {
                Text("\(schoolYearName), \(intervalName)")
                    .font(tableValueStyle())
                    .foregroundStyle(Color(.secondaryLabel))
                    .lineLimit(1)
            }
        }
    }
}
