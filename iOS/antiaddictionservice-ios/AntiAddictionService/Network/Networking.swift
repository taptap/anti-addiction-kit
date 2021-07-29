import Foundation
import AntiAddictionAsyncHttp

/// 联网版 API Manager
struct Networking {
    // MARK: - Debug messages
    private static let networkRequestError: String = "网络请求失败"
    private static let dataFormatError: String = "接口数据解析失败"
    private static let networkRequestSuccess: String = "网络服务请求成功"
    
    // MARK: - API URLs
    private static var baseUrl: String = AntiAddictionService.configuration.host ?? ""
    private static var identifyBaseUrl: String = AntiAddictionService.configuration.identifyHost ?? ""
    
    private static let configUrl        = "/v3/fcm/get_config"      // GET
    private static let tokenUrl         = "/v3/fcm/authorizations"  // POST
    private static let serverTimeUrl    = "/v3/fcm/get_server_time" // GET
    private static let setPlayLogUrl    = "/v3/fcm/set_play_log"    // POST
    private static let setUserInfoUrl   = "/api/v1/identification"  // POST
    private static let checkRealnameUrl = "/api/v1/identification/info" // GET
    private static let checkPaymentUrl  = "/v3/fcm/check_pay"       // POST
    private static let setPaymentUrl    = "/v3/fcm/submit_pay"      // POST
    
    /**
     签名算法:
     1、 将业务参数，根据参数的 key 进行 字典排序，并按照 Key-Value 的格式拼接成一个字符串。将请求体中的参数 拼接在字符串最后。
     2、 将 secretKey 拼接在步骤 1 获得字符串最前面，得到待加密字符串
     即secretKey+query+body。 （e5d341b5aed6110da68f93e06aff47dbuser_id=sdafsdf）
     使用 SHA256 算法对待加密字符串进行计算，放入header ， key 为sign
     */
    private static let clientSecretKey  = "e5d341b5aed6110da68f93e06aff47db"
    
    /// 字典数组Array<Dictionary>序列化成JSON字符串
    private static func dictionaryArrayToJSONString(_ array: [[String: Any]]?) -> String {
        var jsonString: String = ""
        if let tryArray = array {
            do {
                let jsonData = try JSONSerialization.data(withJSONObject: tryArray, options: [])
                jsonString = String(data: jsonData, encoding: .utf8) ?? ""
            }
            catch { jsonString = "[]" }
        } else {
            jsonString = "[]"
        }
        return jsonString
    }
    
    private static func dataToDictionary(_ data:Data?) -> [String:Any]? {
        var jsonDic:[String:Any]?
        if let jsonData = data {
            do {
                jsonDic = try JSONSerialization.jsonObject(with: jsonData, options: .mutableContainers) as? Dictionary<String,Any>
            }catch{}
        }
        
        return jsonDic
    }
    
    /// 字典Dictionary序列化成JSON字符串
    private static func dictionaryToJSONString(_ dictionary: [String: Any]?) -> String {
        var jsonString: String = ""
        if let tryDictionary = dictionary {
            do {
                let jsonData = try JSONSerialization.data(withJSONObject: tryDictionary, options:.prettyPrinted)
                jsonString = String(data: jsonData, encoding: .utf8) ?? ""
            } catch {}
        }
        return jsonString
    }
    
    private static func antiaddictionCommonParams() -> Dictionary<String, Any> {
        var commonData = ["game":AntiAddictionService.configuration.bundleId,
                          "sdkVersion":AntiAddictionService.getSDKVersion()]
        if AntiAddictionService.configuration.gameIdentifier != nil,AntiAddictionService.configuration.gameIdentifier!.count > 0 {
            commonData["game"] = AntiAddictionService.configuration.gameIdentifier!
        }
        
        return commonData
    }
    
    /// 获取防沉迷相关配置
    static func getSdkConfig() {
        AntiAddictionAsyncHttp.httpGet(baseUrl + configUrl, requestParams: nil, customHeader: nil, params: antiaddictionCommonParams(), callBack: { (r) in
            guard let data = r?.data else {
                Logger.debug(baseUrl + configUrl+networkRequestError)
                return
            }
            
            do {
                let jsonString = String(data: data, encoding: String.Encoding(rawValue: String.Encoding.utf8.rawValue))
                if jsonString != nil {
                    AntiAddictionService.sendCallback(result: .updateConfig, extra: jsonString)
                }
                
                // 防沉迷配置
                let jsonDic = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? Dictionary<String,Any>
                let code = jsonDic?["code"] as? NSNumber
                let dataDic = jsonDic?["data"] as? Dictionary<String,Any>
                if (code != nil) ,code == 200,
                   let normalConfigDic = dataDic?["child_protected_config"] as? Dictionary<String,Any>,
                   let nightStrictStart = normalConfigDic["night_strict_start"] as? String, // 宵禁开始时间
                   let nightStrictEnd = normalConfigDic["night_strict_end"] as? String,     // 宵禁结束时间
                   let childCommonTime = normalConfigDic["child_common_time"] as? NSNumber, // 未成年游戏时间
                   let childHolidayTime = normalConfigDic["child_holiday_time"] as? NSNumber,// 未成年节假日游戏时间
                   let guestTotalTime = normalConfigDic["no_identify_time"] as? NSNumber    // 游客游戏时间
                {
                    AntiAddictionService.configuration.nightStrictStart = nightStrictStart
                    AntiAddictionService.configuration.nightStrictEnd = nightStrictEnd
                    AntiAddictionService.configuration.minorCommonDayTotalTime = Int(truncating: childCommonTime)
                    AntiAddictionService.configuration.minorHolidayTotalTime = Int(truncating: childHolidayTime)
                    AntiAddictionService.configuration.guestTotalTime = Int(truncating: guestTotalTime)
                    
                    if let uploadAll = normalConfigDic["upload_all_data"] as? NSNumber{
                        AntiAddictionService.configuration.needUploadAllTimeData = Bool(truncating: uploadAll)
                    }
                    Logger.debug(baseUrl+configUrl+networkRequestSuccess)
                }
                
                // 节假日
                if (code != nil) ,code == 200,
                   let holidays = dataDic?["holiday"] as? Array<String> {
                    if holidays.count > 0 {
                        var newHolidayConfig:[String] = []
                        holidays.forEach { (oneDay) in
                            newHolidayConfig.append(oneDay)
                        }
                        AntiAddictionService.configuration.holiday = newHolidayConfig
                    }
                }
                
                //获取文案
                if (code != nil) ,code == 200,
                   let uiConfigDic = dataDic?["ui_config"] as? Dictionary<String,Any>,
                   let payLimitTips = uiConfigDic["pay_limit_words"] as? Array<Dictionary<String,Any>>,
                   let onlineTimeLimitTips = uiConfigDic["health_reminder_words"] as? Array<Dictionary<String,Any>>
                {
                    payLimitTips.forEach { (oneTip) in
                        if let accountType = oneTip["account_type"] as? NSNumber,
                           let singleTitle = oneTip["single_title"] as? String,
                           let singleDescription = oneTip["single_description"] as? String,
                           let monthTitle = oneTip["month_title"] as? String,
                           let monthDescription = oneTip["month_description"] as? String,
                           let singleLimit = oneTip["single_limit"] as? NSNumber,
                           let monthLimit = oneTip["month_limit"] as? NSNumber
                        {
                            let account_type = Int(truncating: accountType)
                            switch (account_type) {
                            case AccountType.unknown.rawValue:
                                break
                            case AccountType.unknownAccount.rawValue:
                                break
                            case AccountType.child.rawValue:
                                NoticeTemplate.childSinglePayLimitTip = PayTipText(tipTitle: singleTitle, desc: singleDescription)
                                NoticeTemplate.childMonthPayLimitTip = PayTipText(tipTitle: monthTitle, desc: monthDescription)
                                break
                            case AccountType.junior.rawValue:
                                AntiAddictionService.configuration.singlePaymentAmountLimitJunior = Int(truncating: singleLimit)
                                AntiAddictionService.configuration.mouthTotalPaymentAmountLimitJunior = Int(truncating: monthLimit)
                                NoticeTemplate.juniorSinglePayLimitTip = PayTipText(tipTitle: singleTitle, desc: singleDescription)
                                NoticeTemplate.juniorMonthPayLimitTip = PayTipText(tipTitle: monthTitle, desc: monthDescription)
                                break
                            case AccountType.senior.rawValue:
                                AntiAddictionService.configuration.singlePaymentAmountLimitSenior = Int(truncating: singleLimit)
                                AntiAddictionService.configuration.mouthTotalPaymentAmountLimitSenior = Int(truncating: monthLimit)
                                NoticeTemplate.seniorSinglePayLimitTip = PayTipText(tipTitle: singleTitle, desc: singleDescription)
                                NoticeTemplate.seniorMonthPayLimitTip = PayTipText(tipTitle: monthTitle, desc: monthDescription)
                                break
                            case AccountType.adult.rawValue:
                                break
                            default:
                                break
                            }
                        }
                    }
                    
                    // accout_type:0,实名类型，0-未实名游客 1：8岁以下 ，2：8-15岁，  3：16-17岁， 4：18+ 5：未实名正式账号
                    /*
                     type:
                     1-宵禁剩余时间提示 2-时长剩余提示 3-单笔消费限制 4-月消费限制 5-已经处于宵禁提示 6-时长耗尽提示 7-首次登陆提示 8-非首次登录提示
                     */
                    onlineTimeLimitTips.forEach { (oneTip) in
                        if let accountType = oneTip["account_type"] as? NSNumber ,
                           let tips = oneTip["tips"] as? Array<Dictionary<String,Any>>
                        {
                            let account_type = Int(truncating: accountType)
                            if tips.count <= 0 {
                                return
                            }
                            tips.forEach { (curTip) in
                                if let type = curTip["type"] as? NSNumber,
                                   let title = curTip["title"] as? String,
                                   let description = curTip["description"] as? String {
                                    let tipText = PlayTimeTipText(tipTitle: title, desc: description)
                                    switch (account_type) {
                                    case AccountType.unknown.rawValue:
                                        switch type {
                                        case 1:
                                            NoticeTemplate.guestNightStrictRemainTip = tipText
                                        case 2:
                                            NoticeTemplate.guestRemainTip = tipText
                                        case 5:
                                            NoticeTemplate.guestNightStrictLimitTip = tipText
                                        case 6:
                                            NoticeTemplate.guestLimitTip = tipText
                                        case 7:
                                            NoticeTemplate.guestFirstLoginTip = tipText
                                        case 8:
                                            NoticeTemplate.guestLoginTip = tipText
                                        case 12:
                                            NoticeTemplate.guestPopRemainTip = tipText
                                        case 13:
                                            NoticeTemplate.guestNightStrictPopRemainTip = tipText
                                        default:
                                            break
                                        }
                                    case AccountType.unknownAccount.rawValue:
                                        switch type {
                                        case 1:
                                            NoticeTemplate.unknownAccountNightStrictRemainTip = tipText
                                        case 2:
                                            NoticeTemplate.unknownAccountRemainTip = tipText
                                        case 5:
                                            NoticeTemplate.unknownAccountNightStrictLimitTip = tipText
                                        case 6:
                                            NoticeTemplate.unknownAccountLimitTip = tipText
                                        case 7:
                                            NoticeTemplate.unknownAccountFirstLoginTip = tipText
                                        case 8:
                                            NoticeTemplate.unknownAccountLoginTip = tipText
                                        case 12:
                                            NoticeTemplate.unknownAccountPopRemainTip = tipText
                                        case 13:
                                            NoticeTemplate.unknownAccountNightStrictPopRemainTip = tipText
                                        default:
                                            break
                                        }
                                    case AccountType.child.rawValue,AccountType.junior.rawValue,AccountType.senior.rawValue:
                                        switch type {
                                        case 1:
                                            NoticeTemplate.childNightStrictRemainTip = tipText
                                        case 2:
                                            NoticeTemplate.childRemainTip = tipText
                                        case 5:
                                            NoticeTemplate.childNightStrictLimitTip = tipText
                                        case 6:
                                            NoticeTemplate.childLimitTip = tipText
                                        case 7:
                                            NoticeTemplate.childFirstLoginTip = tipText
                                        case 8:
                                            NoticeTemplate.childLoginTip = tipText
                                        case 12:
                                            NoticeTemplate.childPopRemainTip = tipText
                                        case 13:
                                            NoticeTemplate.childNightStrictPopRemainTip = tipText
                                        default:
                                            break
                                        }
                                    default:
                                        break
                                    }
                                }
                            }
                        }
                    }
                    
                }
                return
            } catch {}
            Logger.debug(baseUrl+configUrl+dataFormatError)
        }) { (r) in
            Logger.debug(baseUrl+configUrl+networkRequestError)
        }
    }
    
    /// 获取服务器用户 token
    static func authorize(token: String,
                          accountType: Int = 0,
                          allLocalUserInfo: [[String: Any]] = [],
                          suceessHandler: ((_ accessToken: String, _ accountType: AccountType,_ userId:String) -> Void)? = nil,
                          failureHandler: (() -> Void)? = nil) {
        var form: [String: Any] = ["token": token,
                                   "accountType": accountType,
                                   "carrier":RegionDetector.isMainlandCarrier ? 1 : 0,
                                   "local_user_info": dictionaryArrayToJSONString(allLocalUserInfo)]
        for (commonKey,commonValue) in antiaddictionCommonParams() {
            form.updateValue(commonValue, forKey: commonKey)
        }
        
        AntiAddictionAsyncHttp.httpPost(baseUrl+tokenUrl, requestParams: nil, customHeader: nil, params: form, callBack: { (r) in
            guard let data = r?.data else {
                Logger.debug(baseUrl+tokenUrl+networkRequestError)
                failureHandler?()
                return
            }
            let jsonDic = dataToDictionary(data)
            if jsonDic != nil,
               let code = jsonDic?["code"] as? NSNumber,
               code == 200,
               let dataDic = jsonDic?["data"] as? Dictionary<String,Any>,
               let token = dataDic["access_token"] as? String,
               let type = dataDic["type"] as? NSNumber,
               let userId = dataDic["user_id"] as? String{
                Logger.debug(baseUrl+tokenUrl+networkRequestSuccess)
                suceessHandler?(token, AccountType.type(rawValue: Int(truncating: type)),userId)
                return
            }
            
            Logger.debug(baseUrl+tokenUrl+dataFormatError)
            failureHandler?()
        }) { (r) in
            Logger.debug(baseUrl+tokenUrl+dataFormatError)
            failureHandler?()
        }
        
    }
    
    /// 获取服务器时间戳
    static func getServerTime(completehandler: ((Int) -> Void)? = nil) {
        let timestamp: Int = -1
        
        AntiAddictionAsyncHttp.httpGet(baseUrl+serverTimeUrl, requestParams: nil, customHeader: nil, params: antiaddictionCommonParams(), callBack: { (r) in
            guard let data = r?.data else {
                Logger.debug(baseUrl+serverTimeUrl+networkRequestError)
                completehandler?(timestamp)
                return
            }
            let jsonDic = dataToDictionary(data)
            if jsonDic != nil, let ts = jsonDic?["timestamp"] as? NSNumber {
                Logger.debug(baseUrl+serverTimeUrl+networkRequestSuccess)
                completehandler?(Int(truncating: ts))
                return
            }
            Logger.debug(baseUrl+serverTimeUrl+dataFormatError)
            completehandler?(timestamp)
        }) { (r) in
            completehandler?(timestamp)
        }
    }
    
    /// 上传游戏时间
    static func setPlayLog(token: String,
                           serverTime: (Int, Int),
                           localTime: (Int, Int),
                           isLogin:Bool = false,
                           successHandler: ((_ restrictType: Int, _ remainTime: Int, _ title: String, _ description: String) -> Void)? = nil,
                           failureHandler: (() -> Void)? = nil) {
        User.shared?.addOneServerTimestamps(timeStamps: [serverTime.0, serverTime.1])
        User.shared?.addOneLocalTimestamps(timeStamps: [localTime.0, localTime.1])
        let playLogs: [String: Any] = ["server_times": User.shared?.getServerTimestamps(),
                                       "local_times": User.shared?.getLocalTimestamps()]
        
        Logger.debug(playLogs)
        var formData: [String: Any] = ["play_logs": playLogs,"is_login":isLogin ? 1 : 0]
        let header: [String: String] = ["Authorization": "Bearer \(token)"]
        
        for (commonKey,commonValue) in antiaddictionCommonParams() {
            formData.updateValue(commonValue, forKey: commonKey)
        }
        
        //        let r = Just.post(baseUrl+setPlayLogUrl, data: formData, headers: header)
        AntiAddictionAsyncHttp.httpPost(baseUrl+setPlayLogUrl, requestParams: nil, customHeader: header, params: formData, callBack: { (r) in
            guard let data = r?.data else {
                Logger.debug(baseUrl+setPlayLogUrl+networkRequestError)
                failureHandler?()
                return
            }
            let jsonDic = dataToDictionary(data)
            let dataDic = jsonDic?["data"] as? Dictionary<String,Any>
            
            if let statu = dataDic?["status"] as? NSNumber,Bool(truncating: statu) {
                // clear local timestamps
                User.shared?.clearTimeStamps()
                successHandler?(0, Int.max, "", "")
                return
            }
            if  let code = jsonDic?["code"] as? NSNumber,
                code == 200,
                let restrictType = dataDic?["restrictType"] as? NSNumber,
                let remainTime = dataDic?["remainTime"] as? NSNumber,
                let title = dataDic?["title"] as? String,
                let description = dataDic?["description"] as? String,
                let costTime = dataDic?["costTime"] as? NSNumber
            {
                
                Logger.debug(baseUrl+setPlayLogUrl+networkRequestSuccess)
                // clear local timestamps
                User.shared?.clearTimeStamps()
                // update local online time
                User.shared?.resetOnlineTime(Int(truncating: costTime))
                // update local remain time
                User.shared?.resetRemainTime(Int(truncating: remainTime))
                //save check result
                User.shared?.lastCheckResult = dictionaryToJSONString(dataDic)
                Logger.debug("剩余时间：\(remainTime)")
                successHandler?(Int(truncating: restrictType), Int(truncating: remainTime), title, description)
                return
            }
            
            Logger.debug(baseUrl+setPlayLogUrl+dataFormatError)
            failureHandler?()
        }) { (r) in
            Logger.debug(baseUrl+setPlayLogUrl+dataFormatError)
            failureHandler?()
        }
    }
    
    
    /// 提交实名信息
    static func setUserInfo(token: String,
                            name: String,
                            identify: String,
                            successHandler: ((_ identifyState: AntiAddictionRealNameAuthState,_ antiAddictionToken:String) -> Void)? = nil,
                            failureHandler: ((_ message: String) -> Void)? = nil) {
        var identifyState = AntiAddictionRealNameAuthState.success
        var errorMsg = "实名出错，请稍候重试"
        let realnameUrl = identifyBaseUrl + setUserInfoUrl
        
        var userInfo: [String: Any] = ["name": name,
                                       "id_card": identify,
                                       "user_id":token]
        for (commonKey,commonValue) in antiaddictionCommonParams() {
            userInfo.updateValue(commonValue, forKey: commonKey)
        }
        
        let userInfoJson = dictionaryToJSONString(userInfo)
        let sign = clientSecretKey+userInfoJson
        let header: [String: String] = ["sign":sign.sha256()]
        
        AntiAddictionAsyncHttp.httpPost(realnameUrl, requestParams: nil, customHeader: header, params: userInfo,paramsJson: userInfoJson, callBack: { (r) in
            guard let data = r?.data else {
                Logger.debug(identifyBaseUrl+setUserInfoUrl+networkRequestError)
                failureHandler?(errorMsg)
                return
            }
            let jsonDic = dataToDictionary(data)
            let code = jsonDic?["code"] as? NSNumber
            let dataDic = jsonDic?["data"] as? Dictionary<String,Any>
            
            if (code != nil) ,code == 200,
               let state = dataDic?["identify_state"] as? NSNumber
            {
                identifyState = AntiAddictionRealNameAuthState.init(rawValue: state.intValue) ?? .success
                let antiToken = dataDic?["anti_addiction_token"] as? String
                Logger.debug(identifyBaseUrl+setUserInfoUrl+networkRequestSuccess)
                successHandler?(identifyState,antiToken ?? "")
                return
            }else {
                let msg = jsonDic?["msg"] as? String
                if msg != nil {
                    Logger.debug(identifyBaseUrl+setUserInfoUrl+msg!)
                    failureHandler?(msg!)
                    return
                }
            }
            
            Logger.debug(identifyBaseUrl+setUserInfoUrl+dataFormatError)
            failureHandler?(dataFormatError)
        }) { (httpResult) in
            Logger.debug(identifyBaseUrl+setUserInfoUrl+dataFormatError)
            guard let data = httpResult?.data else {
                Logger.debug(identifyBaseUrl+setUserInfoUrl+networkRequestError)
                failureHandler?(errorMsg)
                return
            }
            let jsonDic = dataToDictionary(data)
            if jsonDic != nil,
               let msg = jsonDic?["msg"] as? String
            {
                errorMsg = msg
            }
            failureHandler?(errorMsg)
        }
    }
    
    /// 检查实名信息
    static func checkRealnameState(token: String,
                                   successHandler: ((_ state:AntiAddictionRealNameAuthState,_ userToken:String,_ idCard:String,_ name:String,_ antiAddictionToken:String) -> Void)? = nil,
                                   failureHandler: ((_ message:String) -> Void)? = nil) {
        var identifyState = AntiAddictionRealNameAuthState.success
        var userToken = token
        var name = ""
        var idCard = ""
        var antiAddictionToken = ""
        var errorMsg = "实名查询出错，请稍候重试"
        let checkUrl = identifyBaseUrl + checkRealnameUrl + "?user_id=\(token)"
        let sign = clientSecretKey + "user_id\(token)"
        
        AntiAddictionAsyncHttp.httpGet(checkUrl, requestParams: nil, customHeader: ["sign":sign.sha256()], params: nil) { httpResult in
            guard let data = httpResult?.data else {
                Logger.debug(identifyBaseUrl+checkRealnameUrl+networkRequestError)
                failureHandler?(errorMsg)
                return
            }
            
            let jsonDic = dataToDictionary(data)
            if jsonDic != nil,
               let code = jsonDic?["code"] as? NSNumber,
               code == 200,
               let dataDic = jsonDic?["data"] as? Dictionary<String, Any>,
               let state = dataDic["identify_state"] as? NSNumber,
               let userId = dataDic["user_id"] as? String,
               let userName = dataDic["name"] as? String,
               let userIdCard = dataDic["id_card"] as? String,
               let userAntiToken = dataDic["anti_addiction_token"] as? String
            {
                identifyState = AntiAddictionRealNameAuthState.init(rawValue: state.intValue) ?? .success
                userToken = userId
                name = userName
                idCard = userIdCard
                antiAddictionToken = userAntiToken
                
                Logger.debug(identifyBaseUrl+checkRealnameUrl+networkRequestSuccess)
                successHandler?(identifyState,userToken,idCard,name,antiAddictionToken)
                return
            }else {
                let msg = jsonDic?["msg"] as? String
                if msg != nil {
                    Logger.debug(identifyBaseUrl+checkRealnameUrl+msg!)
                    failureHandler?(msg!)
                    return
                }
            }
            
            Logger.debug(identifyBaseUrl+checkRealnameUrl+dataFormatError)
            failureHandler?(dataFormatError)
        } failedCallback: { httpResult in
            guard let data = httpResult?.data else {
                Logger.debug(identifyBaseUrl+checkRealnameUrl+networkRequestError)
                failureHandler?(errorMsg)
                return
            }
            
            let jsonDic = dataToDictionary(data)
            if jsonDic != nil,
               let msg = jsonDic?["msg"] as? String
            {
                errorMsg = msg
            }
            
            Logger.debug(identifyBaseUrl+checkRealnameUrl+networkRequestError)
            failureHandler?(errorMsg)
        }
        
    }
    /// 检查付费限制
    static func checkPayment(token: String,
                             amount: Int,
                             completionHandler: ((_ allow: Bool, _ title: String , _ description: String) -> Void)? = nil) {
        var allow: Bool = false
        var title: String = "健康消费提示"
        var description: String = "请适度娱乐，理性消费。"
        
        var formData: [String: Any] = ["amount": amount]
        let header: [String: String] = ["Authorization": "Bearer \(token)"]
        for (commonKey,commonValue) in antiaddictionCommonParams() {
            formData.updateValue(commonValue, forKey: commonKey)
        }
        AntiAddictionAsyncHttp.httpPost(baseUrl+checkPaymentUrl, requestParams: nil, customHeader: header, params: formData, callBack: { (r) in
            guard let data = r?.data else {
                Logger.debug(baseUrl+checkPaymentUrl+networkRequestError)
                completionHandler?(allow, title, description)
                return
            }
            
            let jsonDic = dataToDictionary(data)
            
            if jsonDic != nil,
               let code = jsonDic?["code"] as? NSNumber,
               code == 200,
               let dataDic = jsonDic?["data"] as? Dictionary<String, Any>
            {
                
                if let t = dataDic["title"] as? String {
                    title = t
                }
                
                if let d = dataDic["description"] as? String {
                    description = d
                }
                
                if let status = dataDic["status"] as? Bool {
                    allow = status
                } else {
                    allow = false
                }
                Logger.debug(baseUrl+checkPaymentUrl+networkRequestSuccess)
                
                completionHandler?(allow, title, description)
                
                return
            }
            
            Logger.debug(baseUrl+checkPaymentUrl+dataFormatError)
            completionHandler?(false, title, description)
            
        }) { (r) in
            Logger.debug(baseUrl+checkPaymentUrl+dataFormatError)
            completionHandler?(allow, title, description)
        }
    }
    
    /// 提交已付费金额
    static func setPayment(token: String, amount: Int) {
        var formData: [String: Any] = ["amount": amount]
        let header: [String: String] = ["Authorization": "Bearer \(token)"]
        for (commonKey,commonValue) in antiaddictionCommonParams() {
            formData.updateValue(commonValue, forKey: commonKey)
        }
        AntiAddictionAsyncHttp.httpPost(baseUrl+setPaymentUrl, requestParams: nil, customHeader: header, params: formData, callBack: { (r) in
            guard let data = r?.data else {
                Logger.debug("联网版付费金额保存失败")
                Logger.debug(baseUrl+setPaymentUrl+networkRequestError)
                return
            }
            
            let jsonDic = dataToDictionary(data)
            if jsonDic != nil,
               let code = jsonDic?["code"] as? NSNumber,
               code == 200 {
                //提交成功
                Logger.debug(baseUrl+setPaymentUrl+networkRequestSuccess)
                Logger.debug("联网版付费金额保存成功")
                return
            }
            
            Logger.debug("联网版付费金额保存失败")
            Logger.debug(baseUrl+setPaymentUrl+dataFormatError)
            
        }) { (r) in
            Logger.debug("联网版付费金额保存失败")
            Logger.debug(baseUrl+setPaymentUrl+dataFormatError)
        }
    }
}
