import SwiftUI
import ComposeApp

@main
struct iOSApp: SwiftUI.App {
    init() {
        KoinHelperKt.doInitKoin()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
