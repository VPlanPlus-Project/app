//
//  IosDevInfoSheetCoordinator.swift
//  iosApp
//

import SwiftUI
import ComposeApp
import Onboarding

/// Owns the presentation state for IosDevInfoSheet.
/// Conforms to both protocol representations of IosDevInfoSheetHandler:
///   - Onboarding.IosDevInfoSheetHandler  (swift_name of the Onboarding framework protocol)
///   - ComposeApp.OnboardingIosDevInfoSheetHandler  (swift_name used by doInitKoin's parameter type)
/// Both protocols have identical method signatures so one implementation satisfies both.
final class IosDevInfoSheetCoordinator: NSObject, ObservableObject, IosDevInfoSheetHandler, OnboardingIosDevInfoSheetHandler {
    @Published var isPresented = false
    private var onDismiss: (() -> Void)? = nil

    func showSheet(onDismiss: @escaping () -> Void) {
        DispatchQueue.main.async { [self] in
            self.onDismiss = onDismiss
            self.isPresented = true
        }
    }

    func handleDismiss() {
        onDismiss?()
        onDismiss = nil
    }
}
