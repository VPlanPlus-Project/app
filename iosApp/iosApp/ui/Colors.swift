import SwiftUI

extension Color {
    init(compose value: UInt64) {
        let upper = value >> 32          // obere 32 bits = ARGB
        let argb  = UInt32(upper & 0xFFFF_FFFF)
        let a = Double((argb >> 24) & 0xFF) / 255.0
        let r = Double((argb >> 16) & 0xFF) / 255.0
        let g = Double((argb >> 8)  & 0xFF) / 255.0
        let b = Double( argb        & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b, opacity: a)
    }
}
