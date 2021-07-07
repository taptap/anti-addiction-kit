
import Foundation

struct Logger {
    
    /// debug log
    static func debug(_ items: Any...) {
//        #if DEBUG
//        let s = items.reduce("") { result, next in
//            return result + String(describing: next)
//        }
//        Swift.print("[Debug] \(s)")
//        #endif
        info(items)
    }
    
    /// 业务流程 log
    static func info(_ items: Any...) {
        let enable = AntiAddictionService.configuration.enableLog
        if enable == false {
            return
        }
        let s = items.reduce("") { result, next in
            return result + String(describing: next)
        }
//        Swift.print("[AntiAddictionService] \(s)")
        NSLog("[AntiAddictionService] %@", s)
    }
    
}


