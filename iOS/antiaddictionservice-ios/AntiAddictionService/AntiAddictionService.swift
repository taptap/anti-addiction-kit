//
//  AntiAddictionService.swift
//  AntiAddictionService
//
//  Created by JiangJiahao on 2020/7/7.
//  Copyright © 2020 JiangJiahao. All rights reserved.
//

import Foundation
import UIKit

@objc
public enum AntiAddictionServiceResult: Int {
    case playTimeLimitNone      = 100             // 游戏时长无限制
    
    case loginSuccess           = 500             // 用户登录成功
    case logout                 = 1000            // 用户登出
    
    case playTimeLimitNoTime    = 1030            // 游戏时长限制
    
    case payLimitNone           = 1020            // 付费无限制
    case payLimitReachLimit     = 1025            // 付费有限制
    
    case openRealName           = 1060            // 打开实名
    
    case noChatLimit            = 1080            // 用户已实名，可聊天
    case hasChatLimit           = 1090            // 用户未实名，无法聊天
    
    case openAlertTip           = 1095            // 打开弹窗提示
    case closeAlertTip          = 1096            // 关闭弹窗提示

    case updateConfig           = 1100            // 配置更新
}

enum AntiAddictionServiceUserType: Int,Codable {
    case guest = 0              // 游客
    case child = 1              // 0-7岁
    case junior = 2             // 8-15
    case senior = 3             // 16-17
    case adult = 4              // 18以上
    case unknownAccount = 5     // 未实名正式账号
}

enum AntiAddictionServiceRestrictType: Int,Codable {
    case none = 0                       // 无限制
    case curfew = 1                     // 宵禁
    case playTimeLimit = 2              // 时长
    case realnameRequired = 3           // 强制实名
    
    static func type(rawValue: Int) -> AntiAddictionServiceRestrictType {
        switch rawValue {
            case self.none.rawValue: return .none
            case self.curfew.rawValue: return .curfew
            case self.playTimeLimit.rawValue: return .playTimeLimit
            case self.realnameRequired.rawValue: return .realnameRequired
            default: return .none
        }
    }
}

enum AntiAddictionServiceSourceType: Int,Codable {
    case playTime = 0                   // 时长
    case pay = 1                        // 付费
    case chat = 2                       // 聊天
    case login = 3                      // 登录
}

@objc
public enum AntiAddictionRealNameAuthState:Int,Codable {
    case success = 0                     // 认证成功
    case verifying = 1                   // 认证中
    case fail = 2                        // 认证失败
}


@objc public protocol AntiAddictionServiceCallback:NSObjectProtocol {
    
    /// 回调
    /// - Parameters:
    ///   - code: 回调code
    ///   - extra: 回调信息
    func onCallback(code: Int, extra: String?)
}

@objcMembers
@objc(AntiAddictionService)
public final class AntiAddictionService:NSObject {
    
    static var sharedDelegate: AntiAddictionServiceCallback?
    
    public static var configuration: AntiAddictionConfiguration = AntiAddictionConfiguration()
    
    public class func getSDKVersion() -> String {
        return "1.0.0"
    }
    
    public class func enableLog(enable:Bool) {
        AntiAddictionService.configuration.enableLog = enable
    }
    
    public class func setHost(_ host: String) {
        AntiAddictionService.configuration.host = host

    }
    
    public class func setIdentifyHost(_ host: String) {
        AntiAddictionService.configuration.identifyHost = host
    }
    
    /// 设置长连接地址，设置以后，进入游戏将会连接，退出登录会断开
    /// - Parameter address: 长连接地址
    public class func setWebsocketAddress(_ address:String) {
        AntiAddictionService.configuration.websocketAddress = address
    }
    
    /// AAKit 配置方法
    /// - Parameters:
    ///   - useSdkRealName: 实名登记开关，默认值为 true
    ///   - useSdkPaymentLimit: 支付限制开关，默认值为 true
    ///   - useSdkOnlineTimeLimit: 在线时长限制开关，默认值为 true
    public class func setFunctionConfig(_ useSdkPaymentLimit: Bool = true, _ useSdkOnlineTimeLimit: Bool = true) {
        configuration.useSdkOnlineTimeLimit = useSdkOnlineTimeLimit
        configuration.useSdkPaymentLimit = useSdkPaymentLimit
    }
    
    class func sendCallback(result: AntiAddictionServiceResult, extra: String?) {
        DispatchQueue.main.async {
            AntiAddictionService.sharedDelegate?.onCallback(code:result.rawValue,extra: extra)
        }
    }
    
    class func invokePlayTimeCallback(result: AntiAddictionServiceResult, extra: PlayTimeExtra?) {
        do {
            let jsonData = try JSONEncoder().encode(extra)
            let jsonString = String(data: jsonData, encoding: .utf8)!
            sendCallback(result: result, extra: jsonString)
        } catch {
            print(error)
            sendCallback(result: result, extra: nil)
        }
    }
    
    class func invokePayCallback(result: AntiAddictionServiceResult, extra: PayExtra?) {
        do {
            let jsonData = try JSONEncoder().encode(extra)
            let jsonString = String(data: jsonData, encoding: .utf8)!
            sendCallback(result: result, extra: jsonString)
        } catch {
            print(error)
            sendCallback(result: result, extra: nil)
        }
    }
    
    
    /// 初始化 SDK
    /// - Parameters:
    ///   - delegate: 接收回调代理
    ///   - gameIdentifier: 游戏唯一标识符
    public class func `init`(_ delegate: AntiAddictionServiceCallback,gameIdentifier:String? = nil) {
        if (AntiAddictionService.sharedDelegate != nil) {
            Logger.info("请勿重复初始化！")
        } else {
            /// 只会在游戏安装后首次初始化时检测一次用户地区，之后按第一次检测的值判定用户地区，除非删包
            if !RegionDetector.isDetected {
                RegionDetector.detect()
            }
            
            AntiAddictionService.configuration.gameIdentifier = gameIdentifier
            AntiAddictionService.sharedDelegate = delegate
            
            AntiAddictionServiceManager.shared.startManager()
            Logger.info("初始化成功！")
        }
        
        // 如果非大陆用户，关闭所有防沉迷措施
        if !RegionDetector.isMainlandUser {
            AntiAddictionService.configuration.useSdkPaymentLimit = false
            AntiAddictionService.configuration.useSdkOnlineTimeLimit = false
        }
    }
    
    /// 登录用户
    /// - Parameters:
    ///   - userToken: 用户唯一标识符，如用户 ID 等，不能为空
    public class func login(_ userToken: String) {
        if !self.isKitInstalled() { return }
        AntiAddictionServiceManager.shared.login(userToken, AntiAddictionServiceUserType.guest.rawValue)
    }
    
    /// 进入游戏
    public class func enterGame() {
        AntiAddictionServiceManager.shared.enterGame()
        AntiAddictionSocket.shared.connect()
    }
    
    /// 离开游戏
    public class func leaveGame() {
        AntiAddictionServiceManager.shared.leaveGame()
        AntiAddictionSocket.shared.disconnect()
    }
    
    /// 退出用户登录
    public class func logout() {
        if !self.isKitInstalled() { return }
        AntiAddictionServiceManager.shared.logout()
        AntiAddictionSocket.shared.disconnect()
    }
    
    /// 获取用户类型
    /// - Parameter userId: 用户 id
    public class func getCurrentUserType() -> Int {
        if !self.isKitInstalled() { return -1 }
        
        return AntiAddictionServiceManager.shared.getCurrentUserType()
    }
    
    /// 查询能否支付，直接返回支付限制相关回调类型 raw value，特殊情况使用
    /// - Parameter amount: 支付金额，单位分
    public class func checkCurrentPayLimit(_ amount: Int) -> Int {
        AntiAddictionServiceManager.shared.checkCurrentPayLimit(amount)
    }
    
    /// 查询能否支付
    /// - Parameter amount: 支付金额，单位分
    public class func checkPayLimit(_ amount: Int) {
        if !self.isKitInstalled() { return }
        AntiAddictionServiceManager.shared.checkPayLimit(amount)
    }
    
    /// 设置已支付金额
    /// - Parameter amount: 支付金额，单位分
    public class func paySuccess(_ amount: Int) {
        if !self.isKitInstalled() { return }
        AntiAddictionServiceManager.shared.paySuccess(amount)
    }
    
    /// 查询当前用户能否聊天
    public class func checkChatLimit() {
        if !self.isKitInstalled() { return }
        
        AntiAddictionServiceManager.shared.checkChatLimit()
    }
    
    /// 打开实名窗口，实名结果通过回调接受
    public class func openRealName() {
        if !self.isKitInstalled() { return }

        if User.shared == nil {
            print("用户无登录用户")
            return
        }
        
        AntiAddictionService.invokePlayTimeCallback(result: .openRealName, extra: nil)
    }
    
    public class func isHoliday() -> Bool {
        return DateHelper.isHoliday(Date())
    }
    
    // Warning: - DEBUG 模式
    /// 生成身份证兑换码（有效期从生成起6个小时整以内）
    public class func generateIDCode() -> String {
        return AAKitIDNumberGenerator.generate()
    }
    
    public class func startTimeStatic(isLogin: Bool = false) {
        AntiAddictionServiceManager.shared.startTimeStatic(isLogin: isLogin)
    }
    
    public class func stopTimeStatic () {
        AntiAddictionServiceManager.shared.stopAll()
    }
    
    /// 实名认证
    /// - Parameters:
    ///   - userToken: 用户唯一标识，如用户 ID
    ///   - name: 姓名
    ///   - idCard: 身份证
    ///   - phone: 手机号
    ///   - completion: 结果回调
    public class func realNameAuth(userToken:String, name:String,idCard:String,phone:String,completion:@escaping (AntiAddictionRealNameAuthState,String)->()) {
        realName(userToken: userToken,name: name, idCard: idCard, phone: phone, completion: completion)
    }
    
    /// 检查实名状态
    /// - Parameters:
    ///   - userToken: 用户唯一标识，如用户 ID
    ///   - completion: 结果回调
    public class func checkRealnameState(userToken:String,completion:@escaping (_ identifyState : AntiAddictionRealNameAuthState,_ userToken:String,_ idCard:String ,_ name:String)->(),failureHandler:@escaping(_ errorMessage:String) -> ()) {
        checkRealName(userToken: userToken, completion: completion,failureHandler: failureHandler)
    }
    
    public class func checkPlayTimeResult(handler:@escaping (_ result:String?)->Void) -> String? {
        return AntiAddictionServiceManager.shared.checkPlayTimeResult(handler: handler)
    }
    
    //禁用初始化方法
    @available(*, unavailable)
    private override init() {
        fatalError("Class `AntiAddictionKit` init method is unavailable!")
    }
    
    internal struct PlayTimeExtra:Encodable {
        let title: String?
        let description: String?
        let remainTime: Int?
        let restrictType: AntiAddictionServiceRestrictType?
        let userType:AntiAddictionServiceUserType?                  // 用户类型 0： 游客 1：非游客
        let forceOpen:Bool?
        let extraSource:AntiAddictionServiceSourceType?             // 来源 0 时长 1 付费 2 聊天 3 登录

        init(description:String,userType:AntiAddictionServiceUserType? = .guest) {
            self.init(title: nil, description: description, remainTime: nil, restrictType: nil, userType: userType, forceOpen: nil, extraSource: nil)
        }

        init(
            title: String? = nil,
             description: String? = nil,
             remainTime: Int? = nil,
             restrictType: AntiAddictionServiceRestrictType? = nil,
             userType:AntiAddictionServiceUserType? = nil,
             forceOpen:Bool? = false,
             extraSource:AntiAddictionServiceSourceType? = .playTime)
             {
                self.title = title
                self.description = description
                self.remainTime = remainTime
                self.restrictType = restrictType
                self.userType = userType
                self.forceOpen = forceOpen
                self.extraSource = extraSource
        }
    }
    
    internal struct PayExtra:Encodable {
        let title: String?
        let description: String?
        let userType: AntiAddictionServiceUserType?                      // 用户类型(年龄段)
        let forceOpen:Bool?
        let extraSource:AntiAddictionServiceSourceType?              // 实名来源 0 时长 1 付费 2 聊天 3 其他
        let amount:Int?

        init(description:String) {
            self.init(title: nil, description: description, userType: nil, forceOpen: nil, extraSource: nil,amount:nil)
        }

        init(
            title: String? = nil,
             description: String? = nil,
             userType: AntiAddictionServiceUserType? = nil,
             forceOpen:Bool? = false,
             extraSource:AntiAddictionServiceSourceType? = .playTime,
             amount:Int? = nil)
             {
                self.title = title
                self.description = description
                self.userType = userType
                self.forceOpen = forceOpen
                self.extraSource = extraSource
                self.amount = amount
            }
    }
}

enum RealNameFailedError: String {
    case unknownError                  = ""
    
    case localMissingUser              = "1"
    case localInvalidIDCard            = "2"
    
    case serverMissingUserToken        = "11"
    case serverIDCardRealNameFailed    = "12"
    case serverPromoCodeRealNameFailed = "13"
}

extension AntiAddictionService {
    private static var isServerEnabled: Bool {
        return (AntiAddictionService.configuration.host != nil && AntiAddictionServiceManager.shared.needServerCheck)
    }
    
    class func isKitInstalled() -> Bool {
        if (AntiAddictionService.sharedDelegate == nil) {
            Logger.info("请先初始化！")
            return false
        }
        return true
    }
    
    class func checkRealName(userToken:String,completion:@escaping (_ identifyState : AntiAddictionRealNameAuthState,_ userToken:String,_ idCard:String ,_ name:String)->(),failureHandler:@escaping(_ errorMessage:String) -> ()) {
        Networking.checkRealnameState(token: userToken) { state, userToken, idCard, name, antiAddictionToken in
            completion(state,userToken,idCard,name)
        }failureHandler: { message in
            failureHandler(message)
        }
    }
    
    class func realName(userToken:String, name:String,idCard:String,phone:String,completion:@escaping (AntiAddictionRealNameAuthState,String)->()) {
        let isGeneratedCode = AAKitIDNumberGenerator.isValid(with: idCard)
        // 联网版
        if isServerEnabled {
            // 判断用户是否有效
            if let account = AccountManager.currentAccount, let token = account.token {
                    // 身份证
                    Networking.setUserInfo(token: userToken, name: name, identify: idCard, successHandler: { (state,antiAddictionToken) in
                        if state == .success || state == .verifying {
                            AntiAddictionServiceManager.shared.updateCurAccount(token:token)
                        }
                        completion(state,"")
                    }) { message in
                        completion(.fail,message)
                    }
            } else {
                completion(.fail,RealNameFailedError.serverMissingUserToken.rawValue)
            }
            
            return
        }

        // 单机版
        // 判断用户是否有效
        if User.shared != nil {
            //判断身份证是不是兑换码
            if isGeneratedCode {
                //如果兑换码有效,直接更新用户为成人
                //单机版
                User.shared!.updateUserType(.adult)
                UserService.saveCurrentUserInfo()
                completion(.success,"0")
                return
            } else {
                //根据身份证生日更新用户信息
                if let yearStr = idCard.yyyyMMdd() {
                    let age = DateHelper.getAge(yearStr)
                    
                    if age < 0 {
                        completion(.fail,RealNameFailedError.localInvalidIDCard.rawValue)
                        return
                    }
                    //登记成功
                    let type = UserType.typeByAge(age)
                    
                    User.shared!.updateUserType(type)
                    User.shared!.updateUserRealName(name: name.encrypt(),
                                                    idCardNumber: idCard.encrypt(),
                                                    phone: phone.encrypt())
                    UserService.saveCurrentUserInfo()
                    completion(.success,"0")
                    return
                } else {
                    //身份证拿不到日期
                    completion(.fail,RealNameFailedError.localInvalidIDCard.rawValue)
                    return
                }
            }
            
        } else {
            completion(.fail,RealNameFailedError.localMissingUser.rawValue)
            return
        }
    }
}
