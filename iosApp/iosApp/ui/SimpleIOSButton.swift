//
//  SwiftUiView.swift
//  iosApp
//
//  Created by Julius Vincent Babies on 16.01.26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

class IOSNativeViewFactory: NativeViewFactory {
    static var shared = IOSNativeViewFactory()
    
    func nativeButton(text: String, onClick: @escaping () -> Void) -> UIViewController {
        let view = SimpleIOSButton(label: text, action: onClick)
        
        return UIHostingController(rootView: view)
    }
}

struct SimpleIOSButton: View {
    var label: String
    var action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.headline)
        }
    }
}
