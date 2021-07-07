
import Foundation

struct ChatManager {
    
    /// 联网版
    /// 检测是否可以聊天
    static func check() {
        
        // 非大陆用户，不开启防沉迷系统
        if !RegionDetector.isMainlandUser {
            AntiAddictionService.invokePlayTimeCallback(result: .noChatLimit, extra: AntiAddictionService.PlayTimeExtra(description: "海外用户，不开启防沉迷系统"))
            return
        }
        
        if let _ = AntiAddictionService.configuration.host {
            guard let account = AccountManager.currentAccount, let _ = account.token else {
                AntiAddictionService.invokePlayTimeCallback(result: .hasChatLimit, extra: AntiAddictionService.PlayTimeExtra(description: "当前无已登录用户，无法聊天"))
                return
            }
            //检查是否实名
            if account.type == .unknown || account.type == .unknownAccount{
                AntiAddictionService.invokePayCallback(result: .hasChatLimit, extra: AntiAddictionService.PayExtra(title: nil, description: "用户未实名登记无法聊天", userType: .guest))
            } else {
                AntiAddictionService.invokePlayTimeCallback(result: .noChatLimit, extra: AntiAddictionService.PlayTimeExtra(description: "用户已实名，可以聊天"))
            }
            
        }
        
    }
    
}
