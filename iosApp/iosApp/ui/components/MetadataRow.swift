import SwiftUI

// MARK: - Styles
func tableNameStyle() -> Font { .body }
func tableValueStyle() -> Font { .callout }

// MARK: - MetadataRow
struct MetadataRow<Key: View, Value: View>: View {
    let key: () -> Key
    let value: () -> Value

    var body: some View {
        HStack(alignment: .center) {
            key()
                .frame(maxWidth: .infinity, alignment: .leading)
            value()
                .frame(idealWidth: .infinity, maxWidth: .infinity, alignment: .leading)
        }
        .padding(.vertical, 2)
    }
}

// MARK: - MetadataValueContainer
struct MetadataValueContainer<Content: View>: View {
    var canEdit: Bool
    var editStyling: Bool = true
    var onClick: (() -> Void)?
    let content: () -> Content

    var body: some View {
        if canEdit {
            content()
                .frame(maxWidth: .infinity, minHeight: 32, alignment: .leading)
                .padding(editStyling ? 4 : 0)
                .background(editStyling ? Color(.secondarySystemFill) : .clear)
                .clipShape(onClick == nil ? AnyShape(Rectangle()) : AnyShape(RoundedRectangle(cornerRadius: 8)))
                .onTapGesture {
                    if let onClick = onClick {
                        onClick()
                    }
                }
        } else {
            content()
        }
    }
}

#Preview {
    VStack(spacing: 8) {
        MetadataRow {
            Text("Note")
                .font(tableNameStyle())
                .foregroundStyle(.gray)
        } value: {
            MetadataValueContainer(canEdit: true, onClick: {}) {
                Text("1,5")
                    .font(tableValueStyle())
            }
        }

        MetadataRow {
            Text("Fach")
                .font(tableNameStyle())
                .foregroundStyle(.gray)
        } value: {
            MetadataValueContainer(canEdit: false, onClick: {}) {
                Text("Mathematik")
                    .font(tableValueStyle())
            }
        }

        MetadataRow {
            Text("Editierbar")
                .font(tableNameStyle())
                .foregroundStyle(.gray)
        } value: {
            MetadataValueContainer(canEdit: true, editStyling: false, onClick: {}) {
                Text("Kein Styling")
                    .font(tableValueStyle())
            }
        }
    }
    .padding()
}
