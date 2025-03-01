import SwiftUI
import SafariServices
import ComposeApp
import QuickLook
import BackgroundTasks

@main
struct iOSApp: SwiftUI.App {
    init() {
        MainViewControllerKt.doInitKoin()
    }
    
    @State private var launchUrl: String? = nil
    @State var quickLookUrl: URL? = nil
    
    @Environment(\.scenePhase) private var phase
    
    var body: some Scene {
        WindowGroup {
            ContentView(
                url: launchUrl ?? "",
                onQuicklook: { path in quickLookUrl = URL(string: path) }
            )
                .onOpenURL { url in
                    (getVisibleViewController(UIApplication.shared.keyWindow?.rootViewController) as! SFSafariViewController).dismiss(animated: true)
                    
                    if url.scheme == "vpp" && url.host == "app" && url.pathComponents[1] == "auth" {
                        launchUrl = url.absoluteString
                    }
                }
                .quickLookPreview($quickLookUrl)
        }.onChange(of: phase) { newPhase in
            switch newPhase {
            case .background: scheduleAppRefresh()
            default: break
            }
        }.backgroundTask(.appRefresh("plus.vplan.app.sync.task")) {
            print("Refresh")
            Task {
                do {
                    try await SyncHelperKt.sync()
                }
            }
        }
    }
}

func getVisibleViewController(_ rootViewController: UIViewController?) -> UIViewController? {
  var rootVC = rootViewController
  if rootVC == nil {
      rootVC = UIApplication.shared.keyWindow?.rootViewController
  }

  var presented = rootVC?.presentedViewController
  if rootVC?.presentedViewController == nil {
      if let isTab = rootVC?.isKind(of: UITabBarController.self), let isNav = rootVC?.isKind(of: UINavigationController.self) {
          if !isTab && !isNav {
              return rootVC
          }
          presented = rootVC
      } else {
          return rootVC
      }
  }

  if let presented = presented {
    if presented.isKind(of: UINavigationController.self) {
        if let navigationController = presented as? UINavigationController {
            return navigationController.viewControllers.last!
        }
     }

     if presented.isKind(of: UITabBarController.self) {
        if let tabBarController = presented as? UITabBarController {
            if let navigationController = tabBarController.selectedViewController! as? UINavigationController {
                 return navigationController.viewControllers.last!
             } else {
                 return tabBarController.selectedViewController!
             }
         }
     }

     return getVisibleViewController(presented)
  }
  return nil
}

public func scheduleAppRefresh() {
    let request = BGAppRefreshTaskRequest(identifier: "plus.vplan.app.sync.task") // Mark 1
    request.earliestBeginDate = Calendar.current.date(byAdding: .second, value: 30, to: Date()) // Mark 2
    do {
        try BGTaskScheduler.shared.submit(request) // Mark 3
        print("Background Task Scheduled!")
    } catch (let error) {
        print("Scheduling error: ", error)
    }
}
