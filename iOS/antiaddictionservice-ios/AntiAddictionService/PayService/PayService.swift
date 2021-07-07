
import Foundation

final class PayService {
    // MARK: - Public
    /// 查询能否购买道具，通过回调通知调用方
    /// - Parameter price: 道具价格
    public class func canPurchase(_ price: Int) {
        // 非大陆用户，不开启防沉迷系统
        if !RegionDetector.isMainlandUser {
            AntiAddictionService.sendCallback(result: .payLimitNone, extra: nil)
            return
        }
        
        if AntiAddictionService.configuration.useSdkPaymentLimit == false {
            let limitType = PayLimitType.unlimited
            limitType.notify()
            return
        }
        PayService.getPayLimitType(price).notify()
    }
    
    /// 成功购买道具
    /// - Parameter price: 道具价格
    public class func didPurchase(_ price: Int) {
        if User.shared == nil { return }
        User.shared!.paymentIncrease(price)
        UserService.store(User.shared!)
    }
}

extension PayService {
    
    // MARK: - Private
    private class func getPayLimitType(_ price: Int) -> PayLimitType {
        guard let user = User.shared else {
            return .unAuthed(price)
        }
        switch user.type {
        case .adult:
            return .unlimited
        case .child:
            return .tooYoung
        case .junior:
            //检测支付金额限制
            if (price > AntiAddictionService.configuration.singlePaymentAmountLimitJunior) {
                return .juniorSingleAmountLimit
            } else if (price + user.totalPaymentAmount) > AntiAddictionService.configuration.mouthTotalPaymentAmountLimitJunior {
                return .juniorMonthTotalAmountLimit
            } else {
                return .unlimited
            }
        case .senior:
             //检测支付金额限制
            if (price > AntiAddictionService.configuration.singlePaymentAmountLimitSenior) {
                return .seniorSingleAmountLimit
            } else if (price + user.totalPaymentAmount) > AntiAddictionService.configuration.mouthTotalPaymentAmountLimitSenior {
                return .seniorMonthTotalAmountLimit
            } else {
                return .unlimited
            }
        case .unknown:
            return .unAuthed(price)
        case .unknownAccount:
            return .unAuthed(price)
        }
    }
}

fileprivate enum PayLimitType {
    // price参数，方便用户实名成功后自动重新检测支付限制
    case unAuthed(_ price: Int) // 未实名
    case tooYoung // 0-8岁不能支付
    case juniorSingleAmountLimit //8-16 单次支付金额限制
    case seniorSingleAmountLimit //16-17 单次支付金额限制
    case juniorMonthTotalAmountLimit //8-16 每月支付金额限制
    case seniorMonthTotalAmountLimit //16-17 每月支付金额限制
    case unlimited //无限制
    
    func paymentLimitAlertTitle() -> String {
        switch self {
        case .unAuthed(_):
            return NoticeTemplate.guestPayLimitTip.title
        case .tooYoung:
            return NoticeTemplate.childSinglePayLimitTip.title
        case .juniorSingleAmountLimit:
            return NoticeTemplate.juniorSinglePayLimitTip.title
        case .seniorSingleAmountLimit:
            return NoticeTemplate.seniorSinglePayLimitTip.title
        case .juniorMonthTotalAmountLimit:
             return NoticeTemplate.juniorMonthPayLimitTip.title
        case .seniorMonthTotalAmountLimit:
             return NoticeTemplate.seniorMonthPayLimitTip.title
        case .unlimited:
            return kPaymentLimitAlertTitle
        }
    }
    
    func paymentLimitAlertBody() -> String {
        switch self {
        case .unAuthed(_):
            Logger.debug("用户没实名登记，无法充值")
            return NoticeTemplate.guestPayLimitTip.description
        case .tooYoung:
            Logger.debug("用户低于8岁，无法充值")
            return NoticeTemplate.childSinglePayLimitTip.description
        case .juniorSingleAmountLimit:
            Logger.debug("超过单价限制，无法充值")
            return NoticeTemplate.juniorSinglePayLimitTip.description
        case .seniorSingleAmountLimit:
            Logger.debug("超过单价限制，无法充值")
            return NoticeTemplate.seniorSinglePayLimitTip.description
        case .juniorMonthTotalAmountLimit:
             Logger.debug("超过总额限制，无法充值")
             return NoticeTemplate.juniorMonthPayLimitTip.description
        case .seniorMonthTotalAmountLimit:
             Logger.debug("超过总额限制，无法充值")
             return NoticeTemplate.seniorMonthPayLimitTip.description
        case .unlimited:
             Logger.debug("充值没限制")
            return "请适度娱乐，理性消费。"
        }
    }
    
    func notify() {
        let title = self.paymentLimitAlertTitle()
        let body = self.paymentLimitAlertBody()
        switch self {
        case .unlimited:
            AntiAddictionService.invokePayCallback(result: .payLimitNone, extra: nil)
        case .tooYoung, .juniorSingleAmountLimit,.seniorSingleAmountLimit, .juniorMonthTotalAmountLimit,.seniorMonthTotalAmountLimit:
            AntiAddictionService.invokePayCallback(result: .payLimitReachLimit, extra: AntiAddictionService.PayExtra(title:title, description: body,userType: .senior))
        case .unAuthed( _):
            //游客想付费，直接弹出实名登记页面
            AntiAddictionService.invokePayCallback(result: .payLimitReachLimit, extra: AntiAddictionService.PayExtra(userType: .guest, forceOpen: true, extraSource: .pay))
        }
    }
}

extension PayService {
    /// 查询能否购买道具，直接返回支付限制相关的回调类型 raw value, 特殊情况使用。
    /// - Parameter price: 道具价格
    public class func checkCurrentPayLimit(_ price: Int) -> Int {
        
        // 非大陆用户，不开启防沉迷系统
        if !RegionDetector.isMainlandUser {
            return AntiAddictionServiceResult.payLimitNone.rawValue
        }
        
        //如果未开启 付费限制，直接发送无限制回调
        if AntiAddictionService.configuration.useSdkPaymentLimit == false {
            return AntiAddictionServiceResult.payLimitNone.rawValue
        }
        
        if User.shared == nil { return AntiAddictionServiceResult.payLimitReachLimit.rawValue }
        let payLimitType = PayService.getPayLimitType(price)
        switch payLimitType {
            case .unlimited:
                //无限制
                return AntiAddictionServiceResult.payLimitNone.rawValue
            case .unAuthed(_):
                //未实名，有限制，打开实名窗口
                AntiAddictionService.invokePayCallback(result: .openRealName, extra: AntiAddictionService.PayExtra(description: "用户支付，请求实名"))

                return AntiAddictionServiceResult.payLimitReachLimit.rawValue

            case .tooYoung, .juniorSingleAmountLimit,.juniorMonthTotalAmountLimit,.seniorSingleAmountLimit,.seniorMonthTotalAmountLimit:
                return AntiAddictionServiceResult.payLimitReachLimit.rawValue
        }
    }

    
}


