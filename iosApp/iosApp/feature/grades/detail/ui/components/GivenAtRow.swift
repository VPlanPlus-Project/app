import SwiftUI
import VPlanPlusShared

struct GivenAtRow: View {
    var date: Date

    var body: some View {
        MetadataRow {
            Text("Erteilt am")
                .font(tableNameStyle())
                .foregroundStyle(.gray)
        } value: {
            MetadataValueContainer(canEdit: false, onClick: {}) {
                Text("\(date.formatted(.dateTime.day().month().year())), \(date.untilText(other: Date.now))")
                    .font(tableValueStyle())
                    .foregroundStyle(Color(.secondaryLabel))
                    .lineLimit(1)
            }
        }
    }
}
