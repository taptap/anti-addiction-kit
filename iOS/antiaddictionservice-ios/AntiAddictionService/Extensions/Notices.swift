
import Foundation

let kPaymentLimitAlertTitle: String = "健康消费提示"

public enum TipType {
    case timeLimitAlert
    case payLimitAlert
}

struct PlayTimeTipText {
    var title:String = "健康游戏提示"
    var description:String = ""
    
    init(tipTitle:String = "健康游戏提示",desc:String) {
        title = tipTitle
        description = desc
    }
}

struct PayTipText {
    var title:String = "健康消费提示"
    var description:String = ""
    init(tipTitle:String = "健康消费提示",desc:String) {
        title = tipTitle
        description = desc
    }
}

struct NoticeTemplate {
    
    static var childSinglePayLimitTip:PayTipText = PayTipText(desc: "当前账号未满 8 周岁，无法使用充值相关功能。")
    static var childMonthPayLimitTip:PayTipText = PayTipText(desc: "当前账号未满 8 周岁，无法使用充值相关功能。")
    
    static var juniorSinglePayLimitTip:PayTipText = PayTipText(desc: "当前账号未满 16 周岁，本次单笔付费金额超过规定上限 50 元，无法购买。")
    static var juniorMonthPayLimitTip:PayTipText = PayTipText(desc: "当前账号未满 16 周岁，购买此商品后，您当月交易的累计总额已达上限 200 元，无法购买。")
    
    static var seniorSinglePayLimitTip:PayTipText = PayTipText(desc: "当前账号未满 18 周岁，本次单笔付费金额超过规定上限 100 元，无法购买。")
    static var seniorMonthPayLimitTip:PayTipText = PayTipText(desc: "当前账号未满 18 周岁，购买此商品后，您当月交易的累计总额已达上限  400 元，无法购买。")
    
    // 游客
    static var guestPayLimitTip:PayTipText = PayTipText(desc: "根据国家相关规定，当前您无法使用充值相关功能。")
    static var guestChatLimitTip:PlayTimeTipText = PlayTimeTipText(desc: "您当前未提交实名信息，根据相关规定，无法使用聊天功能")
    static var guestFirstLoginTip:PlayTimeTipText = PlayTimeTipText(desc: "您当前未提交实名信息，根据国家相关规定，享有##分钟游戏体验时间。登记实名信息后可深度体验。")
    static var guestLoginTip:PlayTimeTipText = PlayTimeTipText(desc: "您当前为游客账号，游戏体验时间还剩余 ## 分钟。登记实名信息后可深度体验。")
    static var guestRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "您的游戏体验时间还剩余##分钟，登记实名信息后可深度体验。")
    static var guestPopRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "您的游戏体验时间还剩余##分钟，登记实名信息后可深度体验。")
    static var guestLimitTip:PlayTimeTipText = PlayTimeTipText(desc: "您的游戏体验时长已达##分钟。登记实名信息后可深度体验。")
    // 宵禁
    static var guestNightStrictRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "距离健康保护时间还剩余 ## 分钟，请注意适当休息。")
    static var guestNightStrictPopRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "距离健康保护时间还剩余 ## 分钟，请注意适当休息。")
    static var guestNightStrictLimitTip:PlayTimeTipText = PlayTimeTipText(desc: "根据国家相关规定，每日 #22 点# - 次日 #8 点#为健康保护时段，当前无法进入游戏。")
    
    // 未成年
    static var childFirstLoginTip:PlayTimeTipText = PlayTimeTipText(desc: "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，周五、周六、周日及法定节假日 20 点 -  21 点之外为健康保护时段。您今日游戏时间还剩余 ## 分钟游戏时间，请注意适当休息。")
    static var childLoginTip:PlayTimeTipText = PlayTimeTipText(desc: "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，周五、周六、周日及法定节假日 20 点 -  21 点之外为健康保护时段。您今日游戏时间还剩余 ## 分钟，请注意适当休息。")
    static var childRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "您今日游戏时间还剩余 ## 分钟，请注意适当休息。")
    static var childPopRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "您今日游戏时间还剩余 ## 分钟，请注意适当休息。")
    static var childLimitTip:PlayTimeTipText = PlayTimeTipText(desc: "您今日游戏时间已达 ## 分钟。根据国家相关规定，今日无法再进行游戏。请注意适当休息。")
    // 宵禁
    static var childNightStrictRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "距离健康保护时间还剩余 ## 分钟，请注意适当休息。")
    static var childNightStrictPopRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "距离健康保护时间还剩余 ## 分钟，请注意适当休息。")
    static var childNightStrictLimitTip:PlayTimeTipText = PlayTimeTipText(desc: "根据国家相关规定，每日 #22 点# - 次日 #8 点#为健康保护时段，当前无法进入游戏。")
    
    // 未实名正式账号
    static var unknownAccountPayLimitTip:PayTipText = PayTipText(desc: "根据国家相关规定，当前您无法使用充值相关功能。")
    static var unknownAccountChatLimitTip:PlayTimeTipText = PlayTimeTipText(desc: "您当前未提交实名信息，根据相关规定，无法使用聊天功能")
    static var unknownAccountFirstLoginTip:PlayTimeTipText = PlayTimeTipText(desc: "您当前未提交实名信息，根据国家相关规定，享有 ## 分钟游戏体验时间。登记实名信息后可深度体验。")
    static var unknownAccountLoginTip:PlayTimeTipText = PlayTimeTipText(desc: "您当前未提交实名信息，已被纳入防沉迷系统。您享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间还剩余 ## 分钟。")
    static var unknownAccountRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "您的游戏体验时间还剩余 ## 分钟，登记实名信息后可深度体验。")
    static var unknownAccountPopRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "您的游戏体验时间还剩余 ## 分钟，登记实名信息后可深度体验。")
    static var unknownAccountLimitTip:PlayTimeTipText = PlayTimeTipText(desc: "您的游戏体验时长已达 ## 分钟。登记实名信息后可深度体验。")
    // 宵禁
    static var unknownAccountNightStrictRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "距离健康保护时间还剩余 ## 分钟，请注意适当休息。")
    static var unknownAccountNightStrictPopRemainTip:PlayTimeTipText = PlayTimeTipText(desc: "距离健康保护时间还剩余 ## 分钟，请注意适当休息。")
    static var unknownAccountNightStrictLimitTip:PlayTimeTipText = PlayTimeTipText(desc: "根据国家相关规定，每日 #22 点# - 次日 #8 点#为健康保护时段，当前无法进入游戏。")
    
 
    
    static func formateDescParams(description:String,params:String...) -> String {
        let pattern = "[#](\\S|\\s])*((?!#).)*(\\S|\\s])*[#]"
        let regex = try! NSRegularExpression(pattern: pattern, options:[])
        let matches = regex.matches(in: description, options: NSRegularExpression.MatchingOptions(rawValue: 0), range: NSMakeRange(0, description.count))
        
        var reversedParams = params
        var resultDesc = description
        matches.reversed().forEach { (checkResult:NSTextCheckingResult) in
            let param = reversedParams.popLast()
            resultDesc.replaceSubrange(Range(checkResult.range, in: resultDesc)!, with: param!)
        }
        
        return resultDesc
    }
    
    static func formattedCurfewNotice(accountType:AccountType) -> String {
        let curfewStart = AntiAddictionService.configuration.nightStrictStart
        let curfewEnd = AntiAddictionService.configuration.nightStrictEnd
        var resultString = ""
        switch accountType {
        case .unknown:
            resultString = NoticeTemplate.formateDescParams(description: NoticeTemplate.guestNightStrictLimitTip.description, params: "\(curfewStart)","\(curfewEnd)")
            break
        case .unknownAccount:
            resultString = NoticeTemplate.formateDescParams(description: NoticeTemplate.unknownAccountNightStrictLimitTip.description, params: "\(curfewStart)","\(curfewEnd)")
            break
        case .senior,.child,.junior:
            resultString = NoticeTemplate.formateDescParams(description: NoticeTemplate.childNightStrictLimitTip.description, params: "\(curfewStart)","\(curfewEnd)")
            break
        default:
            break
        }
        return resultString
    }
    
}

enum Notice {
    case guestFirstLogin
    case guestLogin(remainTime: Int)
    case guestRemain(remainTime: Int)
    case guestPopRemain(remainTime: Int)
    case guestLimit
    case guestNightStrictRemain(remainTime: Int)
    case guestNightStrictPopRemain(remainTime: Int)
    case guestNightStrictLimit
    
    case childFirstLogin(remainTime: Int)
    case childLogin(remainTime: Int)
    case childRemain(remainTime: Int)
    case childPopRemain(remainTime: Int)
    case childLimit(isHoliday: Bool)
    case childNightStrictRemain(remainTime: Int)
    case childNightStrictPopRemain(remainTime: Int)
    case childNightStrictLimit
    
    case unknownAccountFirstLogin
    case unknownAccountLogin(remainTime: Int)
    case unknownAccountRemain(remainTime: Int)
    case unknownAccountPopRemain(remainTime: Int)
    case unknownAccountLimit
    case unknownAccountNightStrictRemain(remainTime: Int)
    case unknownAccountNightStrictPopRemain(remainTime: Int)
    case unknownAccountNightStrictLimit
    
    var title: String {
        switch self {
        case .guestFirstLogin:
            return NoticeTemplate.guestFirstLoginTip.title
        case .guestLogin( _):
            return NoticeTemplate.guestLoginTip.title
        case .guestRemain( _):
            return NoticeTemplate.guestRemainTip.title
        case .guestPopRemain( _):
            return NoticeTemplate.guestPopRemainTip.title
        case .guestLimit:
            return NoticeTemplate.guestLimitTip.title
        case .guestNightStrictRemain( _):
            return NoticeTemplate.guestNightStrictRemainTip.title
        case .guestNightStrictPopRemain( _):
            return NoticeTemplate.guestNightStrictPopRemainTip.title
        case .guestNightStrictLimit:
            return NoticeTemplate.guestNightStrictLimitTip.title
            
        case .unknownAccountFirstLogin:
            return NoticeTemplate.unknownAccountFirstLoginTip.title
        case .unknownAccountLogin(_):
            return NoticeTemplate.unknownAccountLoginTip.title
        case .unknownAccountRemain( _):
            return NoticeTemplate.unknownAccountRemainTip.title
        case .unknownAccountPopRemain( _):
            return NoticeTemplate.unknownAccountPopRemainTip.title
        case .unknownAccountLimit:
            return NoticeTemplate.unknownAccountLimitTip.title
        case .unknownAccountNightStrictRemain( _):
            return NoticeTemplate.unknownAccountNightStrictRemainTip.title
        case .unknownAccountNightStrictPopRemain( _):
            return NoticeTemplate.unknownAccountNightStrictPopRemainTip.title
        case .unknownAccountNightStrictLimit:
            return NoticeTemplate.unknownAccountNightStrictLimitTip.title
            
        case .childFirstLogin( _):
            return NoticeTemplate.childFirstLoginTip.title
        case .childLogin( _):
            return NoticeTemplate.childLoginTip.title
        case .childRemain( _):
            return NoticeTemplate.childRemainTip.title
        case .childPopRemain( _):
            return NoticeTemplate.childPopRemainTip.title
        case .childLimit(let isHoliday):
            if isHoliday {
                return NoticeTemplate.childLimitTip.title
            } else {
                return NoticeTemplate.childLimitTip.title
            }
        case .childNightStrictRemain( _):
            return NoticeTemplate.childNightStrictRemainTip.title
        case .childNightStrictPopRemain( _):
            return NoticeTemplate.childNightStrictPopRemainTip.title
        case .childNightStrictLimit:
            return NoticeTemplate.childNightStrictLimitTip.title
        }
    }
    
    var content: String {
        switch self {
        case .guestFirstLogin:
            return NoticeTemplate.guestFirstLoginTip.description.formattedNotice(with: AntiAddictionService.configuration.guestTotalTime)
        case .guestLogin(let remainTime):
            return NoticeTemplate.guestLoginTip.description.formattedNotice(with: remainTime)
        case .guestRemain(let remainTime):
            return NoticeTemplate.guestRemainTip.description.formattedNotice(with: remainTime)
        case .guestPopRemain(let remainTime):
            return NoticeTemplate.guestPopRemainTip.description.formattedNotice(with: remainTime)
        case .guestLimit:
            return NoticeTemplate.guestLimitTip.description.formattedNotice(with: AntiAddictionService.configuration.guestTotalTime)
        case .guestNightStrictRemain(let remainTime):
            return NoticeTemplate.guestNightStrictRemainTip.description.formattedNotice(with: remainTime)
        case .guestNightStrictPopRemain(let remainTime):
            return NoticeTemplate.guestNightStrictPopRemainTip.description.formattedNotice(with: remainTime)
        case .guestNightStrictLimit:
            return NoticeTemplate.formattedCurfewNotice(accountType: .unknown)
            
        case .unknownAccountFirstLogin:
            return NoticeTemplate.unknownAccountFirstLoginTip.description.formattedNotice(with: AntiAddictionService.configuration.guestTotalTime)
        case .unknownAccountLogin(let remaintime):
            return NoticeTemplate.unknownAccountLoginTip.description.formattedNotice(with: remaintime)
        case .unknownAccountRemain(let remainTime):
            return NoticeTemplate.unknownAccountRemainTip.description.formattedNotice(with: remainTime)
        case .unknownAccountPopRemain(let remainTime):
            return NoticeTemplate.unknownAccountPopRemainTip.description.formattedNotice(with: remainTime)
        case .unknownAccountLimit:
            return NoticeTemplate.unknownAccountLimitTip.description.formattedNotice(with: AntiAddictionService.configuration.guestTotalTime)
        case .unknownAccountNightStrictRemain(let remainTime):
            return NoticeTemplate.unknownAccountNightStrictRemainTip.description.formattedNotice(with: remainTime)
        case .unknownAccountNightStrictPopRemain(let remainTime):
            return NoticeTemplate.unknownAccountNightStrictPopRemainTip.description.formattedNotice(with: remainTime)
        case .unknownAccountNightStrictLimit:
            return NoticeTemplate.formattedCurfewNotice(accountType: .unknownAccount)
            
        case .childFirstLogin(let remainTime):
//            return NoticeTemplate.childFirstLoginTip.description.formattedNotice(with: AntiAddictionService.configuration.minorCommonDayTotalTime)
            return NoticeTemplate.childFirstLoginTip.description.formattedNotice(with: remainTime)
        case .childLogin(let remainTime):
            return NoticeTemplate.childLoginTip.description.formattedNotice(with: remainTime)
        case .childRemain(let remainTime):
            return NoticeTemplate.childRemainTip.description.formattedNotice(with: remainTime)
        case .childPopRemain(let remainTime):
            return NoticeTemplate.childPopRemainTip.description.formattedNotice(with: remainTime)
        case .childLimit(let isHoliday):
            if isHoliday {
                return NoticeTemplate.childLimitTip.description.formattedNotice(with: AntiAddictionService.configuration.minorHolidayTotalTime)
            } else {
                return NoticeTemplate.childLimitTip.description.formattedNotice(with: AntiAddictionService.configuration.minorCommonDayTotalTime)
            }
        case .childNightStrictRemain(let remainTime):
            return NoticeTemplate.childNightStrictRemainTip.description.formattedNotice(with: remainTime)
        case .childNightStrictPopRemain(let remainTime):
            return NoticeTemplate.childNightStrictPopRemainTip.description.formattedNotice(with: remainTime)
        case .childNightStrictLimit:
            return NoticeTemplate.formattedCurfewNotice(accountType: .child)
        }
    }
    
}

extension String {
    fileprivate func formattedNotice(with seconds: Int) -> String {
        let minute: Int = seconds <= 60 ? seconds : Int(ceilf(Float(seconds)/Float(60)))
        let minuteString = " \(minute) "
        var notice = NoticeTemplate.formateDescParams(description: self, params: minuteString)
        if seconds <= 60 {
            notice = notice.replacingOccurrences(of: "分钟", with: "秒")
            notice = notice.replacingOccurrences(of: "miniute", with: "second")
        }
        return notice
    }
    
    // 需要动态更改时间的提示，60秒倒计时
    func formattedAdjustAbleNotice(with seconds: Int) -> String {
        let formatString = " %ld "
        var notice = NoticeTemplate.formateDescParams(description: self, params: formatString)
        if seconds <= 60 {
            notice = notice.replacingOccurrences(of: "分钟", with: "秒")
            notice = notice.replacingOccurrences(of: "miniute", with: "second")
        }
        return notice
    }
}

/// 弹窗类型
public enum AlertType {
    /// 无限制
//    case unlimited
    /// 游戏时长限制
    case timeLimitAlert
    /// 支付限制
    case payLimitAlert
}

extension AlertType {
    enum TimeLimitAlertContent {
        case guestLogin(seconds: Int, isFirstLogin: Bool)
        case guestGameOver(seconds: Int)
        case unknownAccountLogin(seconds: Int, isFirstLogin: Bool)
        case unknownAccountGameOver(seconds: Int)
        case minorGameOver(seconds: Int = 0, isCurfew: Bool)
        
        var title: String {
            switch self {
            case .guestLogin(_, let isFirstLogin):
                if isFirstLogin {
                    return NoticeTemplate.guestFirstLoginTip.title
                } else {
                    return NoticeTemplate.guestRemainTip.title
                }
            case .guestGameOver(_):
                return NoticeTemplate.guestLimitTip.title
            case .unknownAccountLogin(_, let isFirstLogin):
                if isFirstLogin {
                    return NoticeTemplate.unknownAccountFirstLoginTip.title
                } else {
                    return NoticeTemplate.unknownAccountRemainTip.title
                }
            case .unknownAccountGameOver(_):
                return NoticeTemplate.unknownAccountLimitTip.title
            case .minorGameOver(_, let isCurfew):
                if isCurfew {
                    return NoticeTemplate.childNightStrictLimitTip.title
                } else {
                    return NoticeTemplate.childLimitTip.title
                }
            }
                    }
        
        var body: String {
            switch self {
            case .guestLogin(let seconds, let isFirstLogin):
                if isFirstLogin {
                    return NoticeTemplate.guestFirstLoginTip.description.formattedNotice(with: seconds)
                } else {
                    return NoticeTemplate.guestRemainTip.description.formattedNotice(with: seconds)
                }
            case .guestGameOver(let seconds):
                return NoticeTemplate.guestLimitTip.description.formattedNotice(with: seconds)
            case .unknownAccountLogin(let seconds, let isFirstLogin):
                if isFirstLogin {
                    return NoticeTemplate.unknownAccountFirstLoginTip.description.formattedNotice(with: seconds)
                } else {
                    return NoticeTemplate.unknownAccountRemainTip.description.formattedNotice(with: seconds)
                }
            case .unknownAccountGameOver(let seconds):
                return NoticeTemplate.unknownAccountLimitTip.description.formattedNotice(with: seconds)
            case .minorGameOver(let seconds, let isCurfew):
                if isCurfew {
                    return NoticeTemplate.formattedCurfewNotice(accountType: .child)
                } else {
                    return NoticeTemplate.childLimitTip.description.formattedNotice(with: seconds)
                }
            }
        }
        
    }
}


