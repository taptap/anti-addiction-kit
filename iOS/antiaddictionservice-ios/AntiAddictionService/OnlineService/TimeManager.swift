
import Foundation

struct TimeManager {
    static var currentRemainTime: Int = Int.max
    
    static var lastLocalTimestamp: Int = Int(Date().timeIntervalSince1970)
    static var lastServerTimestamp: Int = Int(Date().timeIntervalSince1970)
    
    /// 启动时长统计服务
    static func activate(isLogin: Bool = false) {
        // 非大陆用户，不开启防沉迷系统
        if !RegionDetector.isMainlandUser {
            return
        }
        
        if AntiAddictionService.configuration.useSdkOnlineTimeLimit == false {
            Logger.debug("联网版未开启防沉迷时长统计")
            return
        }
        
        guard let account = AccountManager.currentAccount, let _ = account.token else {
            Logger.debug("联网版无用户 token，无法启动防沉迷时长统计")
            return
        }
                
        if account.type == .adult , !AntiAddictionService.configuration.needUploadAllTimeData{
            Logger.debug("联网版成年用户，无需统计时长")
            return
        }
        
        Logger.debug("联网版防沉迷时长统计开始")
        
        //常规计时 定时器
        commonTimer.start(fireOnceWhenStart: isLogin)
        timeAddTimer.start()

        //倒计时计时器
        countdownTimer?.start()
        fiftyMinutesCountdownTimer?.start()
    }
    
    /// 暂停时长统计服务
    static func inactivate() {
        commonTimer.suspend()
        countdownTimer?.suspend()
        fiftyMinutesCountdownTimer?.suspend()
        timeAddTimer.suspend()
        User.shared?.addOneLocalTimestamps(timeStamps: [lastLocalTimestamp,lastLocalTimestamp + timeCount])
        User.shared?.addOneServerTimestamps(timeStamps: [lastServerTimestamp,lastServerTimestamp + timeCount])
        UserService.saveCurrentUserInfo()
        
        updateTimestamps()
    }
    
    static func updateTimestamps() {
        lastServerTimestamp += timeCount
        lastLocalTimestamp += timeCount
        resetTimeCount()
    }
    
    private static let commonTimerInterval: Int = 120
    
    // 常规计时 定时器
    static var commonTimer = SwiftTimer(interval: .seconds(commonTimerInterval), repeats: true, queue: .global()) { (aTimer) in
        
        guard let account = AccountManager.currentAccount, let token = account.token else {
            Logger.debug("当前无登录用户，timer 停止")
            aTimer.suspend()
            return
        }
        
        if account.type == .adult, !AntiAddictionService.configuration.needUploadAllTimeData {
            Logger.debug("成年用户，无需统计时长，timer 停止")
            aTimer.suspend()
            return
        }
        
        let newLocalTimestamp = lastLocalTimestamp + timeCount
        let newServerTimestamp = lastServerTimestamp + timeCount
        
        Networking.setPlayLog(token: token,
                              serverTime: (lastServerTimestamp, newServerTimestamp),
                              localTime: (lastLocalTimestamp, newLocalTimestamp),
                              successHandler: { (restrictType, remainTime, title, description) in
                                let antiRestrictType = AntiAddictionServiceRestrictType.type(rawValue: restrictType)
                                //更新剩余时间
                                TimeManager.currentRemainTime = remainTime
                                
                                if account.type == .unknown {
                                    //游客
                                    AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: title, description: description ?? "游客用户游戏时长限制", remainTime: remainTime, restrictType: antiRestrictType, userType: .guest))

                                    // 游客剩余时长1分钟倒计时浮窗
                                    if remainTime <= countdownBeginSeconds {
                                        DispatchQueue.main.async {
                                            NotificationCenter.default.post(name: .startSixtySecondsCountdownNotification, object: nil, userInfo: ["isCurfew": false])
                                        }
                                        return
                                    }
                                    // 游客15分钟弹窗
                                    let firstAlertTipRemainTime = AntiAddictionService.configuration.firstAlertTipRemainTime
                                    if remainTime > firstAlertTipRemainTime && remainTime < firstAlertTipRemainTime + commonTimerInterval {
                                        DispatchQueue.main.async {
                                            NotificationCenter.default.post(name: .startFiftyMinutesCountdownNotification, object: nil, userInfo: ["isCurfew": false, "countdownBeginTime": remainTime])
                                        }
                                        return
                                    }
                                }
                                    
                                else if account.type == .unknownAccount {
                                    if restrictType == 1 {
                                        // 宵禁
                                        // 未实名正式账号
                                        AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: title, description: description ?? "未实名正式账号宵禁限制", remainTime: remainTime, restrictType: .curfew, userType: .unknownAccount))
                                
                                        
                                         // 距离宵禁倒计时
                                        if remainTime <= countdownBeginSeconds {
                                            DispatchQueue.main.async {
                                                NotificationCenter.default.post(name: .startSixtySecondsCountdownNotification, object: nil, userInfo: ["isCurfew": true])
                                            }
                                            return
                                        }
                                        
                                        // 距离宵禁15分钟浮窗提醒
                                        let firstAlertTipRemainTime = AntiAddictionService.configuration.firstAlertTipRemainTime
                                        if remainTime > firstAlertTipRemainTime && remainTime < firstAlertTipRemainTime + commonTimerInterval {
                                            DispatchQueue.main.async {
                                                NotificationCenter.default.post(name: .startFiftyMinutesCountdownNotification, object: nil, userInfo: ["isCurfew": true, "countdownBeginTime": remainTime])
                                            }
                                            return
                                        }
                                    }else {
                                        // 未实名正式账号
                                        AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: title, description: description ?? "游客用户游戏时长限制", remainTime: remainTime, restrictType: antiRestrictType, userType: .unknownAccount))

                                        // 游客剩余时长1分钟倒计时浮窗
                                        if remainTime <= countdownBeginSeconds {
                                            DispatchQueue.main.async {
                                                NotificationCenter.default.post(name: .startSixtySecondsCountdownNotification, object: nil, userInfo: ["isCurfew": false])
                                            }
                                            return
                                        }
                                        // 游客15分钟弹窗
                                        let firstAlertTipRemainTime = AntiAddictionService.configuration.firstAlertTipRemainTime
                                        if remainTime > firstAlertTipRemainTime && remainTime < firstAlertTipRemainTime + commonTimerInterval {
                                            DispatchQueue.main.async {
                                                NotificationCenter.default.post(name: .startFiftyMinutesCountdownNotification, object: nil, userInfo: ["isCurfew": false, "countdownBeginTime": remainTime])
                                            }
                                            return
                                        }
                                    }
                                }
                                    
                                else if account.type == .adult,!AntiAddictionService.configuration.needUploadAllTimeData {
                                    //成年人
                                    Logger.debug("成年用户，无需统计时长，timer 停止")
                                    aTimer.suspend()
                                    return
                                }
                                else if account.type == .child || account.type == .senior || account.type == .junior{
                                    //剩下即未成年人
                                    if restrictType == 1 {
                                        // 宵禁
                                        AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: title, description:description ?? "未成年人宵禁时间", remainTime: remainTime, restrictType: .curfew, userType: .senior))
                                         //未成年人距离宵禁倒计时
                                        if remainTime <= countdownBeginSeconds {
                                            DispatchQueue.main.async {
                                                NotificationCenter.default.post(name: .startSixtySecondsCountdownNotification, object: nil, userInfo: ["isCurfew": true])
                                            }
                                            return
                                        }
                                        
                                        //未成年人距离宵禁15分钟浮窗提醒
                                        let firstAlertTipRemainTime = AntiAddictionService.configuration.firstAlertTipRemainTime
                                        if remainTime > firstAlertTipRemainTime && remainTime < firstAlertTipRemainTime + commonTimerInterval {
                                            DispatchQueue.main.async {
                                                NotificationCenter.default.post(name: .startFiftyMinutesCountdownNotification, object: nil, userInfo: ["isCurfew": true, "countdownBeginTime": remainTime])
                                            }
                                            return
                                        }
                                    }
                                        
                                    else {
                                        // 非宵禁
                                        AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: title, description:description ?? "未成年人每日游戏时长限制", remainTime: remainTime, restrictType: antiRestrictType, userType: .senior))
                                        
                                        // 未成年人游戏剩余时长倒计时启动
                                        if remainTime <= countdownBeginSeconds {
                                            DispatchQueue.main.async {
                                                NotificationCenter.default.post(name: .startSixtySecondsCountdownNotification, object: nil, userInfo: ["isCurfew": false])
                                            }
                                            return
                                        }
                                        
                                        // 未成年人游戏剩余时长15分钟浮窗提醒
                                        let firstAlertTipRemainTime = AntiAddictionService.configuration.firstAlertTipRemainTime
                                        if remainTime > firstAlertTipRemainTime && remainTime < firstAlertTipRemainTime + commonTimerInterval {
                                            DispatchQueue.main.async {
                                                NotificationCenter.default.post(name: .startFiftyMinutesCountdownNotification, object: nil, userInfo: ["isCurfew": false, "countdownBeginTime": remainTime])
                                            }
                                            return
                                        }
                                    }
                                }
        },failureHandler: {
            // failed
        })
        
        lastLocalTimestamp = newLocalTimestamp
        lastServerTimestamp = newServerTimestamp
        resetTimeCount()
        
        #if DEBUG // 给demo发送时间
        postOnlineTimeNotification()
        #endif
    }
    
    private static var timeAddTimer:SwiftTimer = SwiftTimer(interval: .seconds(Int(1)), repeats: true, queue: .global()) { (mTimer) in
        timeCount += 1
    }
    private static var timeCount:Int = 0
    
    static func resetTimeCount() {
        timeCount = 0
    }

    // 启动倒计时的时间 (默认2m30s)
    private static var countdownInterval: Int = 1
    private static var countdownBeginSeconds: Int = AntiAddictionService.configuration.countdownAlertTipRemainTime + commonTimerInterval
    private static var countdownTimer: SwiftCountDownTimer? = nil
    private static var fiftyMinutesCountdownTimer: SwiftCountDownTimer? = nil
    
    static func notifyCountDown(restrictType:AntiAddictionServiceRestrictType,leftTimes:Int,serverTitle:String? = nil,serverDesc:String? = nil) {
        guard let account = AccountManager.currentAccount, let _ = account.token else { return }
        let noTime = leftTimes == 0;
        if account.type == .unknown {
            Logger.debug("游客倒计时提示")
            var title = noTime ? Notice.guestLimit.title :Notice.guestPopRemain(remainTime: leftTimes).title
            var desc = noTime ? Notice.guestLimit.content : NoticeTemplate.guestPopRemainTip.description.formattedAdjustAbleNotice(with: leftTimes)
            if restrictType == .curfew {
                title = noTime ? Notice.guestNightStrictLimit.title : Notice.guestNightStrictPopRemain(remainTime: leftTimes).title
                desc = noTime ? Notice.guestNightStrictLimit.content : NoticeTemplate.guestNightStrictPopRemainTip.description.formattedAdjustAbleNotice(with: leftTimes)
            }
            AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title:serverTitle ?? title,description:serverDesc ?? desc, remainTime: leftTimes,restrictType: restrictType,userType: .guest))
            return
        }
        if account.type == .unknownAccount {
            Logger.debug("未实名游客倒计时提示")
            var title = noTime ? Notice.unknownAccountLimit.title : Notice.unknownAccountPopRemain(remainTime: leftTimes).title
            var desc = noTime ? Notice.unknownAccountLimit.content : NoticeTemplate.unknownAccountPopRemainTip.description.formattedAdjustAbleNotice(with: leftTimes)
            if restrictType == .curfew {
                title = noTime ? Notice.unknownAccountNightStrictLimit.title : Notice.unknownAccountNightStrictPopRemain(remainTime: leftTimes).title
                desc = noTime ? Notice.unknownAccountNightStrictLimit.content : NoticeTemplate.unknownAccountNightStrictPopRemainTip.description.formattedAdjustAbleNotice(with: leftTimes)
            }
            AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title:serverTitle ?? title,description:serverDesc ?? desc, remainTime: leftTimes,restrictType: restrictType,userType: .unknownAccount))
            return
        }
        else if account.type == .adult {
            countdownTimer?.suspend()
        } else {
            Logger.debug("未成年倒计时提示")
            var title = noTime ? Notice.childLimit(isHoliday: DateHelper.nowIsHoliday()).title : Notice.childPopRemain(remainTime: leftTimes).title
            var desc = noTime ? Notice.childLimit(isHoliday: DateHelper.nowIsHoliday()).content : NoticeTemplate.childPopRemainTip.description.formattedAdjustAbleNotice(with: leftTimes)
            if restrictType == .curfew {
                title = noTime ? Notice.childNightStrictLimit.title : Notice.childNightStrictPopRemain(remainTime: leftTimes).title
                desc = noTime ? Notice.childNightStrictLimit.content : NoticeTemplate.childNightStrictPopRemainTip.description.formattedAdjustAbleNotice(with: leftTimes)
            }
            AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title:serverTitle ?? title,description:serverDesc ?? desc, remainTime: leftTimes,restrictType: restrictType,userType: .senior))
            return
        }
    }
    // 倒计时浮窗
    static func startCountdown(isCurfew: Bool) {
        //停止主要Timer
        commonTimer.suspend()
        countdownTimer?.suspend()
        
        let restrictType:AntiAddictionServiceRestrictType = isCurfew ? .curfew : .playTimeLimit

        notifyCountDown(restrictType: restrictType, leftTimes: TimeManager.currentRemainTime)
        //设置并执行倒计时Timer任务
        countdownTimer = SwiftCountDownTimer(interval: .seconds(countdownInterval), times: TimeManager.currentRemainTime, queue: .global()) { (cTimer, costTimes, leftTimes) in
            
            Logger.debug("准备60s浮窗的倒计时任务 执行一次,剩余时长：\(leftTimes)")
            
            //减少时间
            TimeManager.currentRemainTime -= 1
            User.shared!.onlineTimeIncrease(1)
            
            #if DEBUG // 给demo发送时间
            postOnlineTimeNotification()
            #endif
            
            /// 方便结束的那一次同步标记
            var isTimeSynchronized: Bool = false
            
            if leftTimes > 0 && leftTimes <= AntiAddictionService.configuration.countdownAlertTipRemainTime {
                guard let account = AccountManager.currentAccount, let token = account.token else { return }
                if leftTimes == AntiAddictionService.configuration.countdownAlertTipRemainTime {
                    let newServerTimestamp = lastServerTimestamp + costTimes
                    let newLocalTimestamp = lastLocalTimestamp + costTimes
                    Networking.setPlayLog(token: token,
                                          serverTime: (lastServerTimestamp, newServerTimestamp),
                                          localTime: (lastLocalTimestamp, newLocalTimestamp), successHandler: {
                                            (_, _, _, _) in
                                            
                    }, failureHandler: {

                    })
                    
                    lastLocalTimestamp = newLocalTimestamp
                    lastServerTimestamp = newServerTimestamp
                    resetTimeCount()
                    
                    isTimeSynchronized = true
                    notifyCountDown(restrictType: restrictType, leftTimes: leftTimes)
                    
                }
                
            }
            
            if leftTimes == 0 {
                guard let account = AccountManager.currentAccount, let token = account.token else { return }
                let LastSyncInterval: Int = isTimeSynchronized ? 60 : costTimes
                let newServerTimestamp = lastServerTimestamp + LastSyncInterval
                let newLocalTimestamp = lastLocalTimestamp + LastSyncInterval
                
                Networking.setPlayLog(token: token,
                                      serverTime: (lastServerTimestamp, newServerTimestamp),
                                      localTime: (lastLocalTimestamp, newLocalTimestamp), successHandler: {
                                        (_, remainTime, title, desc) in
                                        if remainTime > 0 {
                                            // 可能有误差，本地计时0 ，服务端未0
                                            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                                                NotificationCenter.default.post(name: .startSixtySecondsCountdownNotification, object: nil, userInfo: ["isCurfew": isCurfew])
                                            }

                                        }else {
                                            notifyCountDown(restrictType: restrictType, leftTimes: 0, serverTitle: title.count > 0 ? title : nil, serverDesc: desc.count > 0 ? desc : nil)
                                        }

                }, failureHandler: {
                    notifyCountDown(restrictType: restrictType, leftTimes: 0)
                })
                
                //更新时间戳
                lastLocalTimestamp = newLocalTimestamp
                lastServerTimestamp = newServerTimestamp
                resetTimeCount()
                
//                if account.type == .unknown {
//                    Logger.debug("游客时间结束弹窗")
//                    AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: Notice.guestLimit.title, description: Notice.guestLimit.content, remainTime: 0, restrictType: restrictType, userType: .guest, forceOpen: true))
//                    return
//                }
//                else if account.type == .unknownAccount {
//                    Logger.debug("未实名正式账号时间结束弹窗")
//                    AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: Notice.unknownAccountLimit.title, description: Notice.unknownAccountLimit.content, remainTime: 0, restrictType: restrictType, userType: .guest, forceOpen: true))
//                    return
//                }
//                else if account.type == .adult {
//                    countdownTimer?.suspend()
//                } else {
//                    Logger.debug("未成年时间结束弹窗")
//                    let body: String = isCurfew ? Notice.childNightStrictLimit.content : Notice.childLimit(isHoliday: DateHelper.isHoliday(Date())).content
//                    AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: Notice.childLimit(isHoliday: false).title, description: body, remainTime: 0, restrictType: restrictType, userType: .senior, forceOpen: true))
//                    return
//                }
                
            }
        }
        
        //开始执行
        countdownTimer?.start()
    }

    // 15分钟倒计时浮窗，在大于15分钟的时候开始倒计时，等于15分钟时显示一次
    static func startFiftyMinutesCountdown(isCurfew: Bool, countdownBeginTime: Int) {
        let restrictType:AntiAddictionServiceRestrictType = isCurfew ? .curfew : .playTimeLimit
        assert(countdownBeginTime >= AntiAddictionService.configuration.firstAlertTipRemainTime, "开始倒计时的时间必须大于等于需要首次展示浮窗的时间")
        Logger.debug("开始15分钟提示的倒计时")
        fiftyMinutesCountdownTimer = SwiftCountDownTimer(interval: .seconds(countdownInterval), times: countdownBeginTime, queue: .global()) { (fTimer, costTimes, leftTimes) in
            if leftTimes == AntiAddictionService.configuration.firstAlertTipRemainTime {
                commonTimer.fire()
                guard let account = AccountManager.currentAccount else {
                    fTimer.suspend()
                    return
                }
                
                if account.type == .unknown {
                    Logger.debug("游客15分钟提示")
                    AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title:Notice.guestPopRemain(remainTime: leftTimes).title,description: Notice.guestPopRemain(remainTime: leftTimes).content,remainTime: leftTimes,restrictType: restrictType, userType: .guest))
                    fTimer.suspend()
                    return
                }
                else if account.type == .unknownAccount {
                        Logger.debug("未实名账号15分钟提示")
                    var title = Notice.unknownAccountPopRemain(remainTime: leftTimes).title
                    var desc = Notice.unknownAccountPopRemain(remainTime: leftTimes).content
                    if restrictType == .curfew {
                        title = Notice.unknownAccountNightStrictPopRemain(remainTime: leftTimes).title
                        desc = Notice.unknownAccountNightStrictPopRemain(remainTime: leftTimes).content
                    }
                    AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title:title,description: desc,remainTime: leftTimes,restrictType: restrictType, userType: .unknownAccount))
                        fTimer.suspend()
                        return
                    }
                else if account.type == .adult {
                    fTimer.suspend()
                    return
                } else {
                    var title = Notice.childPopRemain(remainTime: leftTimes).title
                    var desc = Notice.childPopRemain(remainTime: leftTimes).content
                    if restrictType == .curfew {
                        title = Notice.childNightStrictPopRemain(remainTime: leftTimes).title
                        desc = Notice.childNightStrictPopRemain(remainTime: leftTimes).content
                    }
                    Logger.debug("未成年15分钟提示")
                    AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title:title,description: desc,remainTime: leftTimes,restrictType: restrictType, userType: .senior))
                    fTimer.suspend()
                    return
                }
                
            }
        }
        
        fiftyMinutesCountdownTimer?.start()
    }
}

