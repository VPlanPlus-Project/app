import SwiftUI
import SafariServices
import ComposeApp
import Onboarding
import QuickLook
import BackgroundTasks

@main
struct iOSApp: SwiftUI.App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    let quickLookImpl = QuickLookImpl()
    let sheetCoordinator = IosDevInfoSheetCoordinator()

    init() {
        MainViewControllerKt.doInitKoin(
            quicklookImpl: quickLookImpl,
            iosDevInfoSheetHandlerImpl: sheetCoordinator
        )
    }
    
    @State private var launchUrl: String? = nil
    @State var quickLookUrl: URL? = nil
    
    @ObservedObject var notificationManager = NotificationManager.shared
    
    @Environment(\.scenePhase) private var phase
    
    var body: some Scene {
        WindowGroup {
            VStack {
                let _ = { quickLookImpl.onQuicklook = { path in quickLookUrl = URL(string: path) } }()
                ContentView(
                    url: launchUrl ?? "",
                    notificationTask: notificationManager.notificationData,
                    sheetCoordinator: sheetCoordinator
                )
                .onOpenURL { url in
                    if let safariVC = getVisibleViewController(UIApplication.shared.keyWindow?.rootViewController) as? SFSafariViewController {
                        safariVC.dismiss(animated: true)
                    }
                    
                    if url.scheme == "vpp" && url.host == "app" && url.pathComponents[1] == "auth" {
                        launchUrl = url.absoluteString
                    }
                }
                .quickLookPreview($quickLookUrl)
            }
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
    let request = BGAppRefreshTaskRequest(identifier: "plus.vplan.app.sync.task")
    request.earliestBeginDate = Calendar.current.date(byAdding: .second, value: 30, to: Date())
    do {
        try BGTaskScheduler.shared.submit(request)
        print("Background Task Scheduled!")
    } catch (let error) {
        print("Scheduling error: ", error)
    }
}

class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .sound])
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        
        let userInfo = response.notification.request.content.userInfo
        
        if let notificationTask = userInfo["data"] as? String {
            DispatchQueue.main.async {
                NotificationManager.shared.notificationData = notificationTask
            }
        }
        
        completionHandler()
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    let notificationDelegate = NotificationDelegate()

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        UNUserNotificationCenter.current().delegate = notificationDelegate
        return true
    }
}

class NotificationManager: ObservableObject {
    static let shared = NotificationManager()
    @Published var notificationData: String?
}
