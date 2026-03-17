//
//  QuickLookImpl.swift
//  iosApp
//
//  Created by Julius Babies on 01.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import VPlanPlusShared

class QuickLookImpl : VPlanPlusShared.OpenQuicklook {
    var onQuicklook: ((String) -> ())?
    
    func open(path: String) {
        onQuicklook?(path)
    }
}
