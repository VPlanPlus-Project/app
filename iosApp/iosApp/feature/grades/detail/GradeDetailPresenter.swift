import Foundation
import SwiftUI
import UIKit
import VPlanPlusShared

class SwiftGradeDetailDrawerLauncher: VPlanPlusShared.GradeDetailDrawerLauncher {
    private weak var currentHostingVC: UIHostingController<GradesDetailView>?

    func launch(gradeId: Int32, onDismiss: @escaping () -> Void) {
        DispatchQueue.main.async {
            let view = GradesDetailView(gradeId: Int(gradeId), onDismiss: {
                self.currentHostingVC?.dismiss(animated: true) {
                    onDismiss()
                }
            })

            let hostingVC = UIHostingController(rootView: view)
            self.currentHostingVC = hostingVC

            hostingVC.modalPresentationStyle = .pageSheet

            guard let rootVC = UIApplication.shared.connectedScenes
                .compactMap({ $0 as? UIWindowScene })
                .first?.windows.first?.rootViewController else {
                return
            }

            if let sheet = hostingVC.sheetPresentationController {
                sheet.detents = [.medium()]
                sheet.prefersGrabberVisible = true
            }

            rootVC.present(hostingVC, animated: true)
        }
    }

    func close() {
        DispatchQueue.main.async {
            self.currentHostingVC?.dismiss(animated: true)
            self.currentHostingVC = nil
        }
    }
}
