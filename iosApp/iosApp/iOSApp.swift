import SwiftUI
import SafariServices
import ComposeApp
import QuickLook

@main
struct iOSApp: SwiftUI.App {
    init() {
        MainViewControllerKt.doInitKoin()
        let fileManager = FileManager.default
        let documentsDirectory = try! fileManager.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false)
        let homeworkFileURL = documentsDirectory.appendingPathComponent("homework_files/30")
        if #available(iOS 16.0, *) {
            print(fileManager.fileExists(atPath: homeworkFileURL.path()))
        } else {
            // Fallback on earlier versions
        }
        print(homeworkFileURL.absoluteString)
        
        let a = Int("0")!
        //1/a

        let tmpDirectory = fileManager.temporaryDirectory
        let destinationURL = tmpDirectory.appendingPathComponent("image.jpg")

        do {
            // Check if the file exists at the source location
            if fileManager.fileExists(atPath: homeworkFileURL.path) {
                // Copy the file to the tmp directory with a new name
                try fileManager.copyItem(at: homeworkFileURL, to: destinationURL)
                print("File copied to temporary directory as image.jpg")
            } else {
                print("File does not exist at the specified location.")
            }
        } catch {
            print("An error occurred: \(error.localizedDescription)")
        }
        
        let viewer = UIDocumentInteractionController(url:destinationURL)
        viewer.presentPreview(animated: true)
    }
    
    @State private var launchUrl: String? = nil
    @State var quickLookUrl: URL? = nil
    
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

