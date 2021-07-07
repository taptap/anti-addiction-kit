
import Foundation

public struct User: Codable {
    
    /// 用户 id
    var id: String
    
    /// 用户类型 默认0=未知
    var type: UserType
    
    /// 用户年龄 默认-1
    var age: Int
    
    /// 用户真实姓名，已加密，读取需解密
    var realName: Data?
    
    /// 用户身份证号，已加密，读取需解密
    var idCardNumber: Data?
    
    /// 用户手机号，已加密，读取需解密
    var phone: Data?
    
    /// 用户当日游戏时长
    var totalOnlineTime: Int
    
    /// 用户当日剩余时长
    var totalRemainTime:Int = -1
    
    /// 用户游戏时长时间戳，发给服务端计时
    var onlineServerTimeStamps: [[Int]]?
    var onlineLocalTimeStamps: [[Int]]?

    
    /// 用户当月总付费，单位分
    var totalPaymentAmount: Int
    
    /// 用户数据保存时的时机
    var timestamp: Date
    
    var lastCheckResult:String?
}
