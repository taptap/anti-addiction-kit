
import Foundation

// 网络版 付费管理
struct PaymentManager {
    
    static func check(amount: Int) {
        
        // 非大陆用户，不开启防沉迷系统
        if !RegionDetector.isMainlandUser {
            AntiAddictionService.sendCallback(result: .payLimitNone, extra: nil)
            return
        }
        
        //如果未开启 付费限制，直接发送无限制回调
        if AntiAddictionService.configuration.useSdkPaymentLimit == false {
            AntiAddictionService.sendCallback(result: .payLimitNone, extra: nil)
            return
        }
        
        if let account = AccountManager.currentAccount, let token = account.token {
            Networking.checkPayment(token: token, amount: amount) { (allow, title, description) in
                if allow {
                    AntiAddictionService.sendCallback(result: .payLimitNone, extra: nil)
                    return
                } else {
                    if account.type == AccountType.unknown || account.type == AccountType.unknownAccount {
                        AntiAddictionService.invokePayCallback(result: .payLimitReachLimit, extra: AntiAddictionService.PayExtra(title: nil, description: description, userType: .guest, forceOpen: true, extraSource: .pay,amount: amount))
                    } else {
                        AntiAddictionService.invokePayCallback(result: .payLimitReachLimit, extra: AntiAddictionService.PayExtra(description: description,amount: amount))
                    }
                }
            }
        } else {
            AntiAddictionService.sendCallback(result: .payLimitNone, extra: nil)
        }
        
    }
    
    static func submit(amount: Int) {
        if let account = AccountManager.currentAccount, let token = account.token {
            Networking.setPayment(token: token, amount: amount)
        }
        Logger.debug("联网版无token，无法提交付费金额。")
    }
    
}

extension PaymentManager {
    
    /// 查询能否购买道具，直接返回支付限制相关的回调类型 raw value, 特殊情况使用。
    /// - Parameter price: 道具价格
    public static func checkCurrentPayLimit(_ amount: Int) -> Int {
        // TODO
        // 非大陆用户，不开启防沉迷系统
        if !RegionDetector.isMainlandUser {
            return AntiAddictionServiceResult.payLimitNone.rawValue
        }
        
        //如果未开启 付费限制，直接发送无限制回调
        if AntiAddictionService.configuration.useSdkPaymentLimit == false {
            return AntiAddictionServiceResult.payLimitNone.rawValue
        }
        
        if let account = AccountManager.currentAccount, let token = account.token {
            
            var limitIntValue: Int = AntiAddictionServiceResult.payLimitNone.rawValue
            
            Networking.checkPayment(token: token, amount: amount) { (allow, title, description) in
                if allow {
                    limitIntValue = AntiAddictionServiceResult.payLimitNone.rawValue
                } else {
                    limitIntValue = AntiAddictionServiceResult.payLimitReachLimit.rawValue

                    if account.type == AccountType.unknown || account.type == AccountType.unknownAccount {
                        AntiAddictionService.invokePayCallback(result: .openRealName, extra: AntiAddictionService.PayExtra(title: nil, description: "用户支付，请求实名", userType: .guest,forceOpen: true,amount: amount))
                    } else {
                        limitIntValue = AntiAddictionServiceResult.payLimitReachLimit.rawValue
                        AntiAddictionService.invokePayCallback(result: .payLimitReachLimit, extra: AntiAddictionService.PayExtra(title: title, description: description, userType: .senior,amount: amount))
                    }
                }
            }
            
            return limitIntValue
            
        } else {
            return AntiAddictionServiceResult.payLimitReachLimit.rawValue
        }
        
    }

    
}
