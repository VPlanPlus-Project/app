import Foundation
import VPlanPlusShared

extension Date {
    func untilText(other: Date) -> String {
        let days = Calendar.current.dateComponents([.day], from: self, to: other).day ?? 0
        switch days {
        case -2: return "Vorgestern"
        case -1: return "Gestern"
        case 0:  return "Heute"
        case 1:  return "Morgen"
        case 2:  return "Übermorgen"
        default:
            return days > 0 ? "In \(days) Tagen" : "Vor \(abs(days)) Tagen"
        }
    }
}

extension Kotlinx_datetimeLocalDate {
    func toDate() -> Date {
        var components = DateComponents()
        components.year = Int(self.year)
        components.month = Int(self.month.ordinal)
        components.day = Int(self.day)
        components.hour = 0
        components.minute = 0
        components.second = 0
        return Calendar.current.date(from: components) ?? Date()
    }
}
