import SwiftUI
import SafariServices
import ComposeApp

@main
struct iOSApp: SwiftUI.App {
    init() {
        MainViewControllerKt.doInitKoin()
    }
    @State private var launchUrl: String? = nil
    var body: some Scene {
        WindowGroup {
            ContentView(url: launchUrl ?? "")
                .onOpenURL { url in
                    (getVisibleViewController(UIApplication.shared.keyWindow?.rootViewController) as! SFSafariViewController).dismiss(animated: true)
                    
                    if url.scheme == "vpp" && url.host == "app" && url.pathComponents[1] == "auth" {
                        launchUrl = url.absoluteString
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

