import SwiftUI
import VPlanPlusShared

extension String? {
    func subjectIcon() -> String {
        let cleaned = (self ?? "").lowercased().replacingOccurrences(
            of: "\\d",
            with: "",
            options: .regularExpression
        )
        
        switch cleaned {
        case "ast", "astro", "astronomie":      return "telescope"
        case "bio", "bia", "biologie":          return "microscope"
        case "ch", "cha", "chemie":             return "flask_conical"
        case "daz", "de", "deu", "deutsch":     return "book_marked"
        case "en", "eng", "englisch":           return "union_jack"
        case "eth", "ethik":                    return "heart_handshake"
        case "fr", "fra", "französisch":        return "croissant"
        case "ge", "geschichte":                return "scroll_text"
        case "geo", "geographie", "geografie":  return "earth"
        case "grw", "wirtschaft":               return "scale"
        case "inf", "it", "informatik":         return "braces"
        case "ku", "kunst":                     return "brush"
        case "ma", "maa", "mathematik":         return "pi"
        case "mu", "musik":                     return "music"
        case "ph", "phy", "lzp", "pha", "physik": return "atom"
        case "re", "ree", "religion":           return "church"
        case "sp", "spo", "spm", "spw", "sport": return "dumbbell"
        default:                                return "graduation_cap"
        }
    }
}

struct SubjectIcon: View {
    
    @Environment(\.colorScheme) var colorScheme: ColorScheme
    
    var subject: String?
    var size: CGFloat
    
    init(subject: String?, size: CGFloat = 24) {
        self.subject = subject
        self.size = size
    }

    private var colors: (container: Color, content: Color) {
        let subjectColors = VPlanPlusShared.SubjectIconKt.subjectColor(subject)
        let colorFamily = colorScheme == .light ? subjectColors.light : subjectColors.dark
        
        return (
            container: Color(compose: colorFamily.container),
            content:   Color(compose: colorFamily.onContainer)
        )
    }
    
    var body: some View {
        let (containerColor, contentColor) = colors
        
        Image(self.subject.subjectIcon())
            .renderingMode(.template)
            .resizable()
            .scaledToFit()
            .foregroundStyle(contentColor)
            .padding(4)
            .frame(width: size, height: size)
            .background(containerColor)
            .clipShape(RoundedRectangle(cornerRadius: 2))
    }
}
