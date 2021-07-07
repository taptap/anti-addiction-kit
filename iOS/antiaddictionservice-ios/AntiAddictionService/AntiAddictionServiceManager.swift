//
//  AntiAddictionServiceManager.swift
//  AntiAddictionService
//
//  Created by JiangJiahao on 2020/7/7.
//  Copyright © 2020 JiangJiahao. All rights reserved.
//  单机联网切换

import Foundation
import UIKit

public struct PlayTimeCheckResult:Codable {
    var restrictType:Int?
    var remainTime:Int?
    var title:String?
    var description:String?
    var costTime:Int?
    var adultType:Int?
}

class AntiAddictionServiceManager {
    let reachability = try! Reachability()
    
    static var shared:AntiAddictionServiceManager = AntiAddictionServiceManager()
    // ????? 单机登录不用再联网检查
    var needServerCheck = true
    var playingGame = false
    
    init() {
        setupReachability()
        addNotificationListener()
        getConfig()
    }
    
    func startManager() {
        Logger.debug("开始防沉迷")
    }
    
    func getConfig () {
        // 如果Host已设置，则获取服务端配置
        if checkConnection(),AntiAddictionService.configuration.host != nil {
            DispatchQueue.global().async {
                Networking.getSdkConfig()
            }
        }
    }
    
    func enterGame () {
        // 登录成功，进入游戏开始计时
        if !playingGame {
            playingGame = true
            startTimeStatic(isLogin: true)
        }
    }
    
    func setupReachability() {
        reachability.whenReachable = { reachability in
            if reachability.connection == .wifi {
                print("Reachable via WiFi")
            } else {
                print("Reachable via Cellular")
            }
            DispatchQueue.global().async {
                if self.isServerEnable(),self.playingGame {
                    self.checkServerAccount()
                    self.startServerStatic()
                }
            }
        }
        reachability.whenUnreachable = { _ in
            if self.needServerCheck,self.playingGame {
                self.stopServerStatic()
            }
        }

        do {
            try reachability.startNotifier()
        } catch {
            print("Unable to start notifier")
        }
    }
    
    /// 当前是否有网络连接
    /// - Returns: 有/无
    func checkConnection() -> Bool {
        return reachability.connection != .unavailable
    }
    
    func checkServerAccount() {
        if checkConnection() {
            return
        }
        
        if User.shared == nil {
            Logger.info("用户无登录用户")
            return
        }
        
        if User.shared?.type.rawValue != AccountManager.currentAccount?.type.rawValue {
            AccountManager.updateAccountType(type: (User.shared?.type.rawValue)!)
        }
    }
    
    func stopServerStatic() {
        Logger.info("开始本地计时")
        TimeManager.inactivate()
        TimeService.start()
    }
    
    func startServerStatic() {
        Logger.info("开始网络计时")
        TimeService.stop()
        TimeManager.activate(isLogin: true)
    }
    
    func startTimeStatic(isLogin: Bool = false) {
        if !playingGame {
            return
        }
        if self.checkConnection(),isServerEnable() {
            Logger.info("开始网络计时")
            TimeManager.activate(isLogin: isLogin)
        }else {
            Logger.info("开始本地计时")
            TimeService.start()
        }
    }
    
    func checkPlayTimeResult(handler:@escaping (_ result:String?)->Void) -> String? {
        handler(User.shared?.lastCheckResult)
        return User.shared?.lastCheckResult
    }
    
    func stopAll() {
        //停止计时器相关（同时会保存当前用户信息）
        TimeService.stop()
        // -----网络版----- begin
        TimeManager.inactivate()
    }
    
    func isServerEnable() -> Bool {
        return (AntiAddictionService.configuration.host != nil && AntiAddictionServiceManager.shared.needServerCheck)
    }
}


extension AntiAddictionServiceManager {
    func addNotificationListener() {
        // MARK: - App 生命周期
        NotificationCenter.default.addObserver(forName: UIApplication.didBecomeActiveNotification, object: nil, queue: nil) { (notification) in
            Logger.info("游戏开始活跃")
            guard let _ = AntiAddictionService.sharedDelegate else { return }
            self.startTimeStatic()
        }
        NotificationCenter.default.addObserver(forName: UIApplication.willResignActiveNotification, object: nil, queue: nil) { (notification) in
            Logger.info("游戏开始不活跃")
            guard let _ = AntiAddictionService.sharedDelegate else { return }
            self.stopAll()
        }
        NotificationCenter.default.addObserver(forName: UIApplication.didEnterBackgroundNotification, object: nil, queue: nil) { (notification) in
            Logger.info("游戏进入后台")
//            guard let _ = AntiAddictionService.sharedDelegate else { return }
//            self.stopAll()
        }
        NotificationCenter.default.addObserver(forName: UIApplication.willTerminateNotification, object: nil, queue: nil) { (notification) in
            Logger.info("游戏即将关闭")
            guard let _ = AntiAddictionService.sharedDelegate else { return }
            self.stopAll()
        }
        
        // MARK: - 时长统计 主Timer通知倒计时timer启动 避免Timer Block 内容相互嵌套 导致线程任务互相等待造成阻塞。
        
        NotificationCenter.default.addObserver(forName: .startFiftyMinutesCountdownNotification, object: nil, queue: nil) { (notification) in
            if let userInfo = notification.userInfo, let isCurfew = userInfo["isCurfew"] as? Bool, let countdownBeginTime = userInfo["countdownBeginTime"] as? Int {
                Logger.debug("开始15分钟浮窗的倒计时")
                TimeManager.startFiftyMinutesCountdown(isCurfew: isCurfew, countdownBeginTime: countdownBeginTime)
            }
        }
        NotificationCenter.default.addObserver(forName: .startSixtySecondsCountdownNotification, object: nil, queue: nil) { (notification) in
            if let userInfo = notification.userInfo, let isCurfew = userInfo["isCurfew"] as? Bool {
                Logger.debug("开始1分钟浮窗的倒计时")
                TimeManager.startCountdown(isCurfew: isCurfew)
            }
            
        }

    }
}

extension Notification.Name {
    static let startSixtySecondsCountdownNotification: NSNotification.Name = NSNotification.Name("startSixtySecondsCountdownNotification")
    static let startFiftyMinutesCountdownNotification: NSNotification.Name = NSNotification.Name("startFiftyMinutesCountdownNotification")
}

/// MARK: 接口相关
extension AntiAddictionServiceManager {
    func login(_ userToken: String, _ userType: Int) {
        // 如果Host已设置，则使用在线方式获取token
        if AntiAddictionService.configuration.host != nil {
            DispatchQueue.global().async {
                LoginManager.login(user: userToken, type: userType)
            }
        } else {
            let user = User(id: userToken, type: UserType.typeByRawValue(userType))
            UserService.login(user)
        }
    }
    
    func logout() {
        //关掉所有页面
        AntiAddictionService.invokePlayTimeCallback(result: .closeAlertTip, extra: nil)
        AntiAddictionService.invokePlayTimeCallback(result: .logout, extra: nil)
        AntiAddictionServiceManager.shared.stopAll()

        // 清除当前用户信息
        User.shared?.lastCheckResult = nil
        UserService.saveCurrentUserInfo()
        User.shared = nil
        AccountManager.currentAccount = nil
        playingGame = false
    }
    
    func updateUserType( _ userType: Int) {
        AntiAddictionService.invokePlayTimeCallback(result: .closeAlertTip, extra: nil)
        if checkConnection(),isServerEnable() {
            DispatchQueue.global().async {
                AccountManager.updateAccountType(type: userType)
            }
        }
        
        UserService.updateUserType(UserType.typeByRawValue(userType))
    }
    
    func updateCurAccount(token:String) {
        if checkConnection(),isServerEnable() {
            DispatchQueue.global().async {
                LoginManager.updateCurAccount(token: token)
            }
        }
    }
    
    func getUserType(_ userId: String) -> Int {
        return UserService.getUserType(userId)
    }
    
    func getCurrentUserType() -> Int {
        return getUserType(User.shared!.id)
    }
    
    func checkChatLimit() {
        if checkConnection(),needServerCheck {
            ChatManager.check()
        } else {
            ChatService.checkChatLimit()
        }
    }
    
    func paySuccess(_ amount: Int) {
        if isServerEnable() {
            DispatchQueue.global().async {
                PaymentManager.submit(amount: amount)
            }
        }else {
            PayService.didPurchase(amount)
        }
    }
    
    func checkPayLimit(_ amount: Int) {
        if isServerEnable() {
            DispatchQueue.global().async {
                PaymentManager.check(amount: amount)
            }
        }else {
            PayService.canPurchase(amount)
        }
    }
    
    func checkCurrentPayLimit(_ amount: Int) -> Int {
        if isServerEnable() {
            return PaymentManager.checkCurrentPayLimit(amount)
        } else {
            return PayService.checkCurrentPayLimit(amount)
        }
    }
}

