
import Foundation

struct LoginManager {
    
    /// 游戏主动登录用户
    static func login(user id: String, type: Int) {
        //登录前先退出(同时恢复某些 flag 登录前的初始值)
        LoginManager.logout()
        UserService.logout()
                
        // 非大陆用户，不开启防沉迷系统
        if !RegionDetector.isMainlandUser {
            Logger.info("非大陆地区不开启防沉迷")
            AntiAddictionService.invokePlayTimeCallback(result: .loginSuccess, extra: AntiAddictionService.PlayTimeExtra(description: "用户登录成功"))
            return
        }
        
        // 如果在线时长控制未开启，则直接登录成功
        if !AntiAddictionService.configuration.useSdkOnlineTimeLimit {
            AntiAddictionService.invokePlayTimeCallback(result: .loginSuccess, extra: AntiAddictionService.PlayTimeExtra(description: "用户登录成功"))
            return
        }
        
        setupAccount(id: id, type: type) { (account) in
            // 有 token 则进获取时间限制，保证最终发送`登录成功`回调
                   if let token = account.token {
                       //  拿到 token 后获取防沉迷限制
                       let ts: Int = Int(Date().timeIntervalSince1970)
                       
                       TimeManager.lastLocalTimestamp = ts
                       Networking.getServerTime { (serverTime) in
                           TimeManager.lastServerTimestamp = serverTime > 0 ? serverTime : ts
                           Networking.setPlayLog(token: token,
                                                             serverTime: (ts, ts),
                                                             localTime: (ts, ts),
                                                             isLogin: true,
                                                             successHandler: { (restrictType, remainTime, title, description) in
                                           
                                                               if remainTime >= 0 {
                                                                   TimeManager.currentRemainTime = remainTime
                                                               }
                                                               
                                                               //成年人 无限制
                                                               if account.type == .adult {
                                                                   AntiAddictionService.invokePlayTimeCallback(result: .loginSuccess, extra: AntiAddictionService.PlayTimeExtra(description: "用户登录成功"))
                                                                   TimeManager.activate(isLogin: true)
                                                                   return
                                                               }
                                                               
                                                               // 未成年人
                                                               if (account.type) == .child || (account.type == .junior) || (account.type == .senior) {
                                                                   //宵禁
                                                                   if restrictType == 1 {
                                                                       if remainTime > 0 {
                                                                        // 未成年允许游戏弹窗提醒
                                                                          let localTitle = Notice.childLogin(remainTime: remainTime).title
                                                                          let localDesc = Notice.childLogin(remainTime: remainTime).content
                                                                          AntiAddictionService.invokePlayTimeCallback(result: .openAlertTip, extra: AntiAddictionService.PlayTimeExtra(title: localTitle, description: localDesc, remainTime: remainTime, restrictType: .playTimeLimit, userType: .child, forceOpen: false, extraSource: .login))
                                                                           return
                                                                       } else {
                                                                        // 未成年不允许游戏弹窗提醒
                                                                           let localTitle = Notice.childNightStrictLimit.title
                                                                           let localDesc = Notice.childNightStrictLimit.content
                                                                           let limitTitle = getLimitTitle(title: title, localTitle: localTitle)
                                                                           let limitDesc = getLimitDesc(desc: description, localDesc: localDesc)
                                                                           
                                                                           AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: limitTitle, description: limitDesc, remainTime: remainTime, restrictType: .curfew, userType: .senior, forceOpen: false,extraSource: .login))
                            
                                                                           return
                                                                       }
                                                                       
                                                                   } else {
                                                                       //非宵禁 一般时长限制
                                                                       if restrictType == 0 {
                                                                           AntiAddictionService.invokePlayTimeCallback(result: .loginSuccess, extra: AntiAddictionService.PlayTimeExtra(description: "用户登录成功"))
                                                                           TimeManager.activate(isLogin: true)
                                                                           return
                                                                       } else {
                                                                           // 弹窗提醒
                                                                           let localTitle = remainTime > 0 ? Notice.childRemain(remainTime: remainTime).title : Notice.childLimit(isHoliday: DateHelper.nowIsHoliday()).title;
                                                                           let localDesc = remainTime > 0 ? Notice.childRemain(remainTime: remainTime).content : Notice.childLimit(isHoliday: DateHelper.nowIsHoliday()).content
                                                                           
                                                                           let limitTitle = getLimitTitle(title: title, localTitle: localTitle)
                                                                           let limitDesc = getLimitDesc(desc: description, localDesc: localDesc)
                                                                           // TODO
                           //                                                AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title: Notice.childFirstLogin(remainTime: remainTime).title, description: Notice.childFirstLogin(remainTime: remainTime).content, remainTime: remainTime, restrictType: .playTimeLimit, userType: .senior, forceOpen: false,extraSource: .login))
                                                                               AntiAddictionService.invokePlayTimeCallback(result: .playTimeLimitNoTime, extra: AntiAddictionService.PlayTimeExtra(title:limitTitle, description:limitDesc, remainTime: remainTime, restrictType: .playTimeLimit, userType: .senior, forceOpen: false,extraSource: .login))
                                                                           return
                                                                       }
                                                                   }
                                                               }
                                                               
                                                               // 游客 未实名
                                                               if account.type == .unknown {
                                                                   var guestLoginNotice: String
                                                                   var tipTitle :String
                                                                   if remainTime == AntiAddictionService.configuration.guestTotalTime {
                                                                       guestLoginNotice = Notice.guestFirstLogin.content
                                                                       tipTitle = Notice.guestFirstLogin.title
                                                                   } else if remainTime > 0 {
                                                                       guestLoginNotice = Notice.guestLogin(remainTime: remainTime).content
                                                                       tipTitle = Notice.guestLogin(remainTime: remainTime).title
                                                                   } else {
                                                                       guestLoginNotice = Notice.guestLimit.content
                                                                       tipTitle = Notice.guestLimit.title
                                                                   }
                                                                   let limitTitle = getLimitTitle(title: title, localTitle: tipTitle)
                                                                   let limitDesc = getLimitDesc(desc: description, localDesc: guestLoginNotice)
                                                                   // 游客登录时统一弹窗
                                                                   AntiAddictionService.invokePlayTimeCallback(result: .openAlertTip, extra: AntiAddictionService.PlayTimeExtra(title: limitTitle, description: limitDesc, remainTime: remainTime, restrictType: .playTimeLimit, userType: .guest,extraSource: .login))
                                                                   return
                                                               }
                                                                
                                                               if account.type == .unknownAccount {
                                                                   if restrictType == 1 && remainTime == 0{
                                                                       // 宵禁
                                                                       AntiAddictionService.invokePlayTimeCallback(result: .openAlertTip, extra: AntiAddictionService.PlayTimeExtra(title: Notice.unknownAccountNightStrictLimit.title, description: Notice.unknownAccountNightStrictLimit.content, remainTime: remainTime, restrictType: .curfew, userType: .unknownAccount,extraSource: .login))
                                                                   }else {
                                                                       var guestLoginNotice: String
                                                                       var tipTitle :String
                                                                       if remainTime == AntiAddictionService.configuration.guestTotalTime {
                                                                           guestLoginNotice = Notice.unknownAccountFirstLogin.content
                                                                           tipTitle = Notice.unknownAccountFirstLogin.title
                                                                       } else if remainTime > 0 {
                                                                           guestLoginNotice = Notice.unknownAccountLogin(remainTime: remainTime).content
                                                                           tipTitle = Notice.unknownAccountLogin(remainTime: remainTime).title
                                                                       } else {
                                                                           guestLoginNotice = Notice.unknownAccountLimit.content
                                                                           tipTitle = Notice.unknownAccountLimit.title
                                                                       }
                                                                       
                                                                       let limitTitle = getLimitTitle(title: title, localTitle: tipTitle)
                                                                       let limitDesc = getLimitDesc(desc: description, localDesc: guestLoginNotice)
                                                                       // 游客登录时统一弹窗
                                                                       AntiAddictionService.invokePlayTimeCallback(result: .openAlertTip, extra: AntiAddictionService.PlayTimeExtra(title: limitTitle, description: limitDesc, remainTime: remainTime, restrictType: .playTimeLimit, userType: .unknownAccount,extraSource: .login))
                                                                   }
                                                                   return
                                                               }
                                                               
                                                               //默认直接登录成功
                                                               AntiAddictionService.invokePlayTimeCallback(result: .loginSuccess, extra: AntiAddictionService.PlayTimeExtra(description: "用户登录成功"))
                                                               TimeManager.activate(isLogin: true)
                                                               return
                                                               
                                       }) {
                                           //获取账号的防沉迷限制失败
                                           AntiAddictionService.invokePlayTimeCallback(result: .loginSuccess, extra: AntiAddictionService.PlayTimeExtra(description: "用户登录成功"))
                                           Logger.info("获取用户防沉迷数据失败")
                                           return
                                       }
                       }
                       
                   }
                   else {
                       //如果拿不到token，则用户类型type设置为成年人
                       account.type = .adult
                       AccountManager.currentAccount = account
                       AntiAddictionService.invokePlayTimeCallback(result: .loginSuccess, extra: AntiAddictionService.PlayTimeExtra(description: "用户登录成功"))
                       Logger.info("获取服务器 Token 失败，防沉迷功能关闭！")
                       return
                   }
        }
    }
    
    static func getLimitTitle(title:String,localTitle:String) ->String {
        if title.count > 0 {
            return title
        }
        
        return localTitle
    }
    
    static func getLimitDesc(desc:String,localDesc:String) ->String {
        if desc.count > 0 {
            return desc
        }
        
        return localDesc
    }
    
    static func setupAccount (id: String, type: Int,completehandler: ((Account) -> Void)? = nil) {
        let account = Account(id: id, type: AccountType.type(rawValue: type))
        
        // 如果本地有数据，则上传给服务器后删除
        var allLocalUserInfo: [[String: Any]] = []
        let keys = Array(UserDefaults.standard.dictionaryRepresentation().keys)
        for key in keys {
            if let storedUser = UserService.fetch(key) {
                let userInfo: [String: Any] = ["userId": storedUser.id,
                                               "name": storedUser.realName?.decrypt() ?? "",
                                               "identify": storedUser.idCardNumber?.decrypt() ?? "",
                                               "phone": storedUser.phone?.decrypt() ?? "",
                                               "accountType": storedUser.type.rawValue]
                allLocalUserInfo.append(userInfo)
            }
        }
        
        Networking.checkRealnameState(token: id) { identifyState, userToken, idCard, name, antiAddictionToken in
            // 以 token 换服务端 `accessToken`
            Networking.authorize(token: antiAddictionToken, accountType: type, allLocalUserInfo: allLocalUserInfo, suceessHandler: { (accessToken, accountType, userId) in
                account.token = accessToken
                account.type = accountType
                
                // 尝试从硬盘中取出用户,本地用户
                let user = User(id: userId, type: UserType.typeByRawValue(accountType.rawValue))
                var theUser: User = user
                if var storedUser = UserService.fetch(theUser.id) {
                    storedUser.updateUserType(theUser.type)
                    storedUser.checkOutdateTimeStamps()
                    theUser = storedUser
                }
                // 更新当前用户
                User.shared = theUser
                
                // 删除本地数据
                for key in keys {
                    if let _ = UserService.fetch(key) {
                        UserService.delete(key)
                    }
                }

                // 设置当前已登录用户
                AccountManager.currentAccount = account
                completehandler?(account)
            }, failureHandler: {
                //失败则 默认成年人
                account.type = .adult

                // 设置当前已登录用户
                AccountManager.currentAccount = account
                completehandler?(account)
            })
        }failureHandler: { message in
            Logger.info(message)
            //失败则 默认成年人
            account.type = .adult

            // 设置当前已登录用户
            AccountManager.currentAccount = account
            completehandler?(account)
        }
    }
    
    static func updateCurAccount(token:String) {
        guard let account = AccountManager.currentAccount else {
            return
        }
        
        // 以 token 换服务端 `accessToken`
        Networking.authorize(token: token, accountType: account.type.rawValue, allLocalUserInfo: [], suceessHandler: { (accessToken, accountType, userId) in
            account.token = accessToken
            account.type = accountType
            
            // 尝试从硬盘中取出用户,本地用户
            let user = User(id: userId, type: UserType.typeByRawValue(accountType.rawValue))
            var theUser: User = user
            if var storedUser = UserService.fetch(theUser.id) {
                storedUser.updateUserType(theUser.type)
                storedUser.checkOutdateTimeStamps()
                theUser = storedUser
            }
            // 更新当前用户
            User.shared = theUser
            // 设置当前已登录用户
            AccountManager.currentAccount = account
        }, failureHandler: {
            //失败则不更新
        })

    }
    
    
    /// 游戏主动退出用户
    static func logout() {
        //关掉所有页面
        AntiAddictionService.invokePlayTimeCallback(result: .closeAlertTip, extra: nil)
        AntiAddictionService.invokePlayTimeCallback(result: .logout, extra: nil)
        AntiAddictionServiceManager.shared.stopAll()

        // 清除当前用户信息
        UserService.saveCurrentUserInfo()
        User.shared = nil
        AccountManager.currentAccount = nil
    }
}
