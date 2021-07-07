
import Foundation

final class ChatService {
    
    class func checkChatLimit() {
        
        // 非大陆用户，不开启防沉迷系统
        if !RegionDetector.isMainlandUser {
            AntiAddictionService.invokePlayTimeCallback(result: .noChatLimit, extra: AntiAddictionService.PlayTimeExtra(description: "海外用户，不开启防沉迷系统"))
            return
        }
        
        //单机版
        //检查是否实名
        guard let user = User.shared else {
            AntiAddictionService.invokePlayTimeCallback(result: .hasChatLimit, extra: AntiAddictionService.PlayTimeExtra(description: "当前无用户登录，无法聊天", userType: .adult))
            return
        }
        
        if user.type == .unknown || user.type == .unknownAccount {
            AntiAddictionService.invokePlayTimeCallback(result: .hasChatLimit, extra: AntiAddictionService.PlayTimeExtra(title:NoticeTemplate.guestChatLimitTip.title, description: NoticeTemplate.guestChatLimitTip.description, userType: .guest))
            
        } else {
            AntiAddictionService.invokePlayTimeCallback(result: .noChatLimit, extra: AntiAddictionService.PlayTimeExtra(description: "用户已实名，可以聊天"))
        }
    }
    
}
