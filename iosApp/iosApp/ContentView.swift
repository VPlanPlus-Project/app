import UIKit
import SwiftUI
import VPlanPlusShared

struct ComposeView: UIViewControllerRepresentable {
    let url: String
    let notificationTask: String?
    
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.mainViewController(url: url, notificationTask: notificationTask)
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        MainViewControllerKt.updateView(url: url, notificationTask: notificationTask)
    }
}

struct ContentView: View {
    let url: String
    let notificationTask: String?
    @ObservedObject var sheetCoordinator: IosDevInfoSheetCoordinator

    var body: some View {
        ComposeView(url: url, notificationTask: notificationTask)
            .ignoresSafeArea()
            .sheet(isPresented: $sheetCoordinator.isPresented, onDismiss: {
                sheetCoordinator.handleDismiss()
            }) {
                IosDevInfoSheet()
            }
    }
}
