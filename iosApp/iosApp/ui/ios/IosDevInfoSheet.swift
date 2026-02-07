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
        ModalItem(title: "Freiwillige Arbeit", description: "VPlanPlus wird von Freiwilligen als Hobbyprojekt entwickelt. Dabei ist die iOS-Version schon immer ein Ziel gewesen, um die App noch mehr Schülerinnen und Schülern zu Verfügung stellen zu können.", iconName: "heart.fill", iconColor: .red),
        ModalItem(title: "Wir brauchen Unterstützung", description: "Entwicklung für iOS ist für uns sehr teuer: Zum Einen wurde ein MacBook angeschaft, damit überhaupt für Apple-Geräte entwickelt werden kann. Eine Lizenz, mit welcher wir Apps in den AppStore einbringen dürfen, kostst uns circa 100 Euro pro Jahr. Zum besseren Testen wurde ein gebrauchtes iPhone beschafft. Daher freuen wir uns riesig über jede Spende, die diese Kosten für uns reduziert.", iconName: "wallet.bifold.fill", iconColor: .blue),
        ModalItem(title: "Spätere Updates", description: "Jedes Update wird von Apple überprüft. Dies kann erfahrungsgemäß sehr lange dauern. Daher kommen wichtige Bugfixes oder neue Funktionen unter Umständen erst später, als auf anderen Plattformen. Keine Sorge, wir haben euch nicht vergessen.", iconName: "arrow.trianglehead.2.clockwise.rotate.90", iconColor: .yellow),
        ModalItem(title: "Gib uns Feedback", description: "Es ist etwas komplexer, die iOS-Versionen gründlich zu testen. Daher freuen wir uns über jede Fehlerbenachrichtigung, um die Bugs besser beheben zu können.", iconName: "ellipsis.bubble", iconColor: .green),
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
                    Button("Schließen") {
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
