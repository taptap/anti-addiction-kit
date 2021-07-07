
import Foundation


/// 每分钟60秒
let kSecondsPerMinute: Int = 60
/// Timer 执行间隔
fileprivate let kTimerInterval: Int = 1

final class TimeService {
    
    static var localTimeStart = 0
    static var serverTimeStart = 0
    
    /// 开始防沉迷时长统计服务
    class func start() {
        
        // 非大陆用户，不开启防沉迷系统
        if !RegionDetector.isMainlandUser {
            return
        }
        
        if AntiAddictionService.configuration.useSdkOnlineTimeLimit == false {
            return
        }
        
        guard User.shared != nil else {
            return
        }
                
        let limitLevel = TimeLimitLevel.limitLevelForUser(User.shared!)
        
        //成年人
        if limitLevel == .unlimited  {
            return
        }
        
        //如果最后一次存储的日期 与 现在不是同一天，则清空在线时长
        if DateHelper.isSameDay(User.shared!.timestamp, Date()) == false,!AntiAddictionServiceManager.shared.isServerEnable() {
            // 如果不是游客，才清空，游客同一设备时长固定，不刷新
            if User.shared!.type != .unknown ,User.shared!.type != .unknownAccount{
                User.shared!.clearOnlineTime()
            }
        }else {
            // TODO 联网更新时间
            DispatchQueue.global().async {
                Networking.getServerTime { (serverTime) in
                    if serverTime > 0 , DateHelper.isSameDay(User.shared!.timestamp, Date(timeIntervalSince1970: Double(serverTime))) == false {
                        if User.shared!.type != .unknown ,User.shared!.type != .unknownAccount{
                            User.shared!.clearOnlineTime()
                        }
                    }
                }
            }
        }
        
        Logger.info("单机版防沉迷时长统计开始")
        
        localTimeStart = TimeManager.lastLocalTimestamp
        serverTimeStart = TimeManager.lastServerTimestamp
        mainTimer.start()
    }
    
    /// 停止防沉迷时长统计服务
    class func stop() {
        if localTimeStart > 0 {
            User.shared?.addOneLocalTimestamps(timeStamps: [localTimeStart,TimeManager.lastLocalTimestamp])
            localTimeStart = 0
        }
        
        if serverTimeStart > 0 {
            User.shared?.addOneServerTimestamps(timeStamps: [serverTimeStart,TimeManager.lastServerTimestamp])
            serverTimeStart = 0
        }

        UserService.saveCurrentUserInfo()
        
        mainTimer.suspend()
    }
    
    /// 主 Timer
    private static var mainTimer: SwiftTimer = SwiftTimer(interval: .seconds(Int(kTimerInterval)), repeats: true, queue: .global()) { (mTimer) in
        
        guard User.shared != nil else {
            Logger.debug("单机版当前无登录用户")
            mTimer.suspend()
            return
        }
        
        let limitLevel = TimeLimitLevel.limitLevelForUser(User.shared!)
        
        //成年人
        if limitLevel == .unlimited  {
            mTimer.suspend()
            return
        }
        
        // 记录游戏时长时间戳
        TimeManager.lastLocalTimestamp += kTimerInterval;
        TimeManager.lastServerTimestamp += kTimerInterval;
        
        User.shared!.onlineTimeIncrease(kTimerInterval)
        #if DEBUG // 给demo发送时间
        postOnlineTimeNotification()
        #endif
        //游客
        if limitLevel == .guest {
        //游客不区分节假日
            let guestTotalTime: Int = AntiAddictionService.configuration.guestTotalTime
            let guestTotalMinutes: Int = max(1, guestTotalTime/kSecondsPerMinute)
            var remainSeconds: Int = max(0, guestTotalTime - User.shared!.totalOnlineTime)
            if User.shared!.totalRemainTime >= 0 {
                remainSeconds = User.shared!.totalRemainTime
            }
            
            assert(guestTotalTime >= 0, "游客设定总时长不能为负数！！！")
            
            var title = Notice.guestRemain(remainTime: remainSeconds).title
            var desc = Notice.guestRemain(remainTime: remainSeconds).content
            if remainSeconds <= 0 {
                mTimer.suspend()
                title = Notice.guestLimit.title
                desc = Notice.guestLimit.content
            }
            
            AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: title, description: desc, remainTime: remainSeconds, restrictType: .playTimeLimit, userType: .guest))
            

            return
        }
        
        //剩下是未成年人
        if limitLevel == .minor {
            //判断当前时间与宵禁时间的距离是否==15分钟 或者 0分钟
            //如果是宵禁，无法游戏，给游戏发送无游戏时间通知
            if DateHelper.isCurfew(Date()) {
                //宵禁无法进入
                AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: Notice.childNightStrictLimit.title, description: Notice.childNightStrictLimit.content, remainTime: 0, restrictType: .curfew, userType: .senior))
                
                return
            }
            
            //距离宵禁的时间a
            let intervalForNextCurfew: Int = DateHelper.intervalForNextCurfew()
            
            //根据总时长限制计算出剩余时间
            let isHoliday = DateHelper.nowIsHoliday()
            let minorTotalTime: Int = isHoliday ? AntiAddictionService.configuration.minorHolidayTotalTime : AntiAddictionService.configuration.minorCommonDayTotalTime
            let minorTotalMinutes: Int = max(1, minorTotalTime/kSecondsPerMinute)
            var minorRemainSeconds: Int = Int(max(0, minorTotalTime - User.shared!.totalOnlineTime))
            if User.shared!.totalRemainTime >= 0 {
                minorRemainSeconds = User.shared!.totalRemainTime
            }
            
            //判断a，b哪个更小
            let isCurfew: Bool = intervalForNextCurfew < minorRemainSeconds
            let minimumRemainSeconds: Int = isCurfew ? intervalForNextCurfew : minorRemainSeconds
            
            
            assert(minorTotalTime >= 0, "未成年人设定总时长不能为负数！！！")
            assert(minorRemainSeconds >= 0, "用户剩余时间不能为负数！！！")
            assert(intervalForNextCurfew >= 0, "当前距宵禁时间不能为负数！！！")

            var title = isCurfew ? Notice.childNightStrictRemain(remainTime: minimumRemainSeconds).title : Notice.childRemain(remainTime: minimumRemainSeconds).title
            var desc = isCurfew ? Notice.childNightStrictRemain(remainTime: minimumRemainSeconds).content : Notice.childRemain(remainTime: minimumRemainSeconds).content
//            if minimumRemainSeconds > 0, minimumRemainSeconds <= AntiAddictionService.configuration.countdownAlertTipRemainTime {
//                title = isCurfew ? NoticeTemplate.childNightStrictRemainTip.title : NoticeTemplate.childRemainTip.title
//                desc = isCurfew ? NoticeTemplate.childNightStrictRemainTip.description.formattedAdjustAbleNotice(with: minimumRemainSeconds) : NoticeTemplate.childRemainTip.description.formattedAdjustAbleNotice(with: minimumRemainSeconds)
//            }
            
            if minimumRemainSeconds <= 0 {
                mTimer.suspend()
                title = isCurfew ? Notice.childNightStrictLimit.title : Notice.childLimit(isHoliday: isHoliday).title
                desc = isCurfew ? Notice.childNightStrictLimit.content : Notice.childLimit(isHoliday: isHoliday).content
                
            }
            AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: title, description: desc, remainTime: minimumRemainSeconds, restrictType:isCurfew ? .curfew : .playTimeLimit, userType: .senior))
        }
    }
    
}

enum TimeLimitLevel {
    case guest //游客限制
    case minor //未成年限制
    case unlimited //成年人无限制
    
    static func limitLevelForUser(_ user: User) -> TimeLimitLevel {
        switch user.type {
        case .unknown,.unknownAccount:
            return .guest
        case .child, .junior, .senior:
            return .minor
        case .adult:
            return .unlimited
        }
    }
}


/// Debug: - 给 DEMO 发送玩家当前游戏时间
internal func postOnlineTimeNotification() {
    #if DEBUG
    if let user = User.shared {
        NotificationCenter.default.post(name: NSNotification.Name("NSNotification.Name.totalOnlineTime"), object: nil, userInfo: ["userId": user.id, "totalOnlineTime": user.totalOnlineTime])
        return
    }
    
    if let account = AccountManager.currentAccount {
        NotificationCenter.default.post(name: NSNotification.Name("NSNotification.Name.totalOnlineTime"), object: nil, userInfo: ["userId": account.id, "totalOnlineTime": TimeManager.currentRemainTime])
        return
    }
    #endif
}
