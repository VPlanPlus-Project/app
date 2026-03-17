import SwiftUI
import VPlanPlusShared

struct SubjectGroupRow: View {
    let canEdit: Bool
    let allowGroup: Bool
    let onClick: () -> Void
    let subject: String?
    let group: VPlanPlusShared.ModelGroup?

    var body: some View {
        MetadataRow {
            Text(allowGroup ? "Klasse/Fach" : "Fach")
            .font(tableNameStyle())
            .foregroundStyle(.gray)
        } value: {
            MetadataValueContainer(canEdit: canEdit, onClick: onClick) {
                HStack(spacing: 12) {
                    if let subject {
                        SubjectIcon(subject: subject)
                            .frame(width: 18, height: 18)
                        
                        Text(subject)
                            .font(tableValueStyle())
                            .foregroundStyle(Color(.secondaryLabel))
                    } else {
                        Text(allowGroup ? (group?.name ?? "Unbekannt") : "Kein Fach")
                            .font(tableValueStyle())
                            .foregroundStyle(Color(.secondaryLabel))
                    }
                }
                .animation(.default, value: subject)
            }
        }
    }
}
