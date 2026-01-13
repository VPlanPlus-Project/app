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

class AppViewModel: ObservableObject {
    @Published var showIosDevInfoSheet = false
    
    init() {
        // Init iOS Sheet Bridge
        PostOnboardingBridge.shared.showSheetCallback = { [weak self] in
            DispatchQueue.main.async {
                self?.showIosDevInfoSheet = true
            }
        }
    }
}

struct ContentView: View {
    let url: String
    let notificationTask: String?
    let onQuicklook: (String)->()
    
    @StateObject private var viewModel = AppViewModel()
    @State private var hasBeenShown = false
    
    var body: some View {
        return ComposeView(url: url, notificationTask: notificationTask, onQuicklook: onQuicklook)
            .ignoresSafeArea()
            .sheet(isPresented: $viewModel.showIosDevInfoSheet) {
                IosDevInfoSheet()
            }
            .onChange(of: viewModel.showIosDevInfoSheet) { isPresented in
                if !isPresented && hasBeenShown {
                    PostOnboardingBridge.shared.onClosed()
                }
                
                if isPresented {
                    hasBeenShown = true
                }
            }
    }
}
