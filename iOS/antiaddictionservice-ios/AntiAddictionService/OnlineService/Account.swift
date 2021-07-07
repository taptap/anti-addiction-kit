
import Foundation

enum AccountType: Int {
    case unknown = 0 // 未知（未实名）游客
    case child = 1 // 0-7岁
    case junior = 2 // 8-15岁
    case senior = 3  // 16-17岁
    case adult = 4// 18岁+
    case unknownAccount = 5 // 未实名正式账号
    
    static func type(rawValue: Int) -> AccountType {
        switch rawValue {
        case self.unknown.rawValue: return .unknown
        case self.child.rawValue: return .child
        case self.junior.rawValue: return .junior
        case self.senior.rawValue: return .senior
        case self.adult.rawValue: return .adult
        case self.unknownAccount.rawValue: return .unknownAccount
        default: return .unknown
        }
    }
}

// 网络版 账号
final class Account {
    
    /// 账号唯一标识符
    var id: String
    
    /// 用户类型 默认0=未知
    var type: AccountType = .unknown
    
    /// 通过服务器以 id 获取的 token
    var token: String? = nil
    
    init(id: String) {
        self.id = id
    }
    
    init(id: String, type: AccountType) {
        self.id = id
        self.type = type
    }
    
}
