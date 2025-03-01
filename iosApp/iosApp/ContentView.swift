import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    let url: String
    let onQuicklook: (String) -> ()
    
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.mainViewController(url: url, quicklookImpl: QuickLookImpl(onQuicklook: onQuicklook))
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Replace the view controller with a new one
        // This effectively removes and recreates the view controller
        if let parent = uiViewController.view.superview {
            uiViewController.removeFromParent()
            uiViewController.view.removeFromSuperview()
            
            let newController = MainViewControllerKt.mainViewController(url: url, quicklookImpl: QuickLookImpl(onQuicklook: onQuicklook))
            parent.addSubview(newController.view)
            newController.view.frame = parent.bounds
            parent.addSubview(newController.view)
        }
    }
}

struct ContentView: View {
    let url: String
    let onQuicklook: (String)->()
    var body: some View {
        return ComposeView(url: url, onQuicklook: onQuicklook).ignoresSafeArea()
    }
}
