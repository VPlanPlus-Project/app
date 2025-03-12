//
//  QuickLookImpl.swift
//  iosApp
//
//  Created by Julius Babies on 01.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp

class QuickLookImpl : OpenQuicklook {
    let onQuicklook: (String) -> ()
    
    init(onQuicklook: @escaping (String) -> Void) {
        self.onQuicklook = onQuicklook
    }
    
    func open(path: String) {
        onQuicklook(path)
    }
}
