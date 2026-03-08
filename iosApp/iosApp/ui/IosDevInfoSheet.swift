//
//  IosDevInfoSheet.swift
//  iosApp
//
//  Created by Julius Vincent Babies on 12.01.26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI


struct IosDevInfoSheet: View {
    @Environment(\.dismiss) private var dismiss
    
    let items = [
        ModalItem(
            title: "Ein Herzensprojekt",
            description: "VPlanPlus wird mit viel Leidenschaft von Freiwilligen in ihrer Freizeit entwickelt. Die iOS-Version war dabei von Anfang an ein wichtiges Ziel – damit noch mehr Schülerinnen und Schüler von der App profitieren können.",
            iconName: "heart.fill",
            iconColor: .red
        ),
        ModalItem(
            title: "Deine Unterstützung zählt",
            description: "iOS-Entwicklung ist für uns als Freiwillige mit echten Kosten verbunden: Ein MacBook, ein gebrauchtes iPhone zum Testen und die Apple-Entwicklerlizenz schlagen jährlich mit rund 100 € zu Buche. Jede Spende hilft uns direkt, diese Kosten zu stemmen – und die App am Leben zu halten.",
            iconName: "wallet.bifold.fill",
            iconColor: .blue
        ),
        ModalItem(
            title: "Updates kommen – versprochen",
            description: "Jede neue Version muss zunächst Apples Prüfprozess durchlaufen, was manchmal etwas Zeit in Anspruch nimmt. Bugfixes oder neue Features können dadurch etwas später erscheinen als auf anderen Plattformen. Wir arbeiten stets daran, die Wartezeit so kurz wie möglich zu halten.",
            iconName: "arrow.trianglehead.2.clockwise.rotate.90",
            iconColor: .yellow
        ),
        ModalItem(
            title: "Sag uns, was nicht stimmt",
            description: "iOS gründlich zu testen ist eine echte Herausforderung. Wenn dir ein Fehler auffällt, freuen wir uns sehr über dein Feedback – so können wir Bugs schneller finden und beheben.",
            iconName: "ellipsis.bubble",
            iconColor: .green
        ),
    ]
    ]
    
    func openDonatePage() {
        if let url = URL(string: "https://vplan.plus/donate?ref=ios_donate_sheet") {
            UIApplication.shared.open(url)
        }
    }
    
    var body: some View {
        VStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 25) {
                    Text("Hinweis zur iOS-Version")
                        .font(.largeTitle)
                        .bold()
                        .multilineTextAlignment(.center)
                        .padding(.top, 40)
                    ForEach(items) { feature in
                        HStack(alignment: .top, spacing: 15) {
                            Image(systemName: feature.iconName)
                                .font(.title)
                                .foregroundColor(feature.iconColor)
                                .frame(width: 45)

                            VStack(alignment: .leading, spacing: 2) {
                                Text(feature.title)
                                    .font(.headline)
                                Text(feature.description)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                    .padding(.bottom, 10)
                }
            }
            
            VStack(spacing: 12) {
                if #available(iOS 26.0, *) {
                    Button("Spenden") {
                        openDonatePage()
                        dismiss()
                    }
                    .buttonStyle(.borderedProminent)
                    .buttonSizing(.flexible)
                    .controlSize(.large)
                } else {
                    Button("Spenden") {
                        openDonatePage()
                        dismiss()
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.large)
                    .frame(maxWidth: .infinity)
                }

                if #available(iOS 26.0, *) {
                    Button("Schließen") {
                        dismiss()
                    }
                    .buttonStyle(.plain)
                    .foregroundColor(.secondary)
                } else {
                    Button("Weiter ohne Spende") {
                        dismiss()
                    }
                    .buttonStyle(.plain)
                    .foregroundColor(.secondary)
                    .controlSize(.large)
                    .frame(maxWidth: .infinity)
                }
            }
        }
        .padding()
        .interactiveDismissDisabled(true)
        .presentationDetents([.large])
        .presentationContentInteraction(.scrolls)
        .presentationCornerRadius(30)
    }
}


struct ModalItem: Identifiable {
    let id = UUID()
    let title: String
    let description: String
    let iconName: String
    let iconColor: Color
}

#Preview {
    IosDevInfoSheet()
}
