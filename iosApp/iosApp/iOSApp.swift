import SwiftUI
import SafariServices

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    print("Open URL")
                    print(url)
                    (getVisibleViewController(UIApplication.shared.keyWindow?.rootViewController) as! SFSafariViewController).dismiss(animated: true)
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

