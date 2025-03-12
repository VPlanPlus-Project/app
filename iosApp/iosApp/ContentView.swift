import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    let url: String
    let notificationTask: String?
    let onQuicklook: (String) -> ()
    
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.mainViewController(url: url, notificationTask: notificationTask, quicklookImpl: QuickLookImpl(onQuicklook: onQuicklook))
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        MainViewControllerKt.updateView(url: url, notificationTask: notificationTask)
    }
}

struct ContentView: View {
    let url: String
    let notificationTask: String?
    let onQuicklook: (String)->()
    var body: some View {
        return ComposeView(url: url, notificationTask: notificationTask, onQuicklook: onQuicklook).ignoresSafeArea()
    }
}
