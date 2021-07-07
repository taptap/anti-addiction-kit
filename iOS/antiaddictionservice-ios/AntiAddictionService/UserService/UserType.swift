
import Foundation

enum UserType: Int, Codable {
    case unknown = 0 // 未知（未实名）游客
    case child = 1 // 0-7岁
    case junior = 2 // 8-15岁
    case senior = 3  // 16-17岁
    case adult = 4// 18岁+
    case unknownAccount = 5 // 未实名正式账号

    
    static func typeByAge(_ age: Int) -> UserType {
        switch age {
        case 0...7: return .child
        case 8...15: return .junior
        case 16...17: return .senior
        case 18...Int.max: return .adult
        default: return .unknown
        }
    }
    
    static func typeByRawValue(_ rawValue: Int) -> UserType {
        switch rawValue {
        case UserType.unknown.rawValue: return UserType.unknown
        case UserType.child.rawValue: return UserType.child
        case UserType.junior.rawValue: return UserType.junior
        case UserType.senior.rawValue: return UserType.senior
        case UserType.adult.rawValue: return UserType.adult
        case UserType.unknownAccount.rawValue: return UserType.unknownAccount
        default: return UserType.unknown
        }
    }
}

extension User {
    
    private static var privateShared: User? = nil
    
    static var shared: User? {
        get {
            return privateShared
        }
        set(new) {
            privateShared = new
        }
    }
    
}

extension User {
    
    /// 传入一个相同 id 的 user 以更新自身状态
    /// - Parameter new: new.id == self.id
    mutating func update(with new: User) {
        //如果id不同，无法更新
        if (new.id != self.id) {
            return
        }
        
        self.updateUserType(new.type)
    }
    
    mutating func updateUserType(_ type: UserType) {
        if (type == .unknown && self.type != .unknown) {
            return
        }
        
        if (type == self.type) {
            return
        }
        
        self.resetUserInfoButId()
        
        self.type = type
    }
    
    mutating func updateUserRealName(name: Data?, idCardNumber: Data?, phone: Data?) {
        self.realName = name
        self.idCardNumber = idCardNumber
        self.phone = phone
    }
     
    mutating func resetOnlineTime(_ time: Int) {
        self.totalOnlineTime = time
        UserService.store(self)
    }
    
    mutating func resetRemainTime(_ time: Int) {
        self.totalRemainTime = time
//        UserService.store(self)
    }
    
    mutating func onlineTimeIncrease(_ addition: Int) {
        self.totalOnlineTime += addition
        if self.totalRemainTime > 0 {
            self.totalRemainTime -= addition
        }
        UserService.store(self)
    }
    
    mutating func clearOnlineTime() {
        self.totalOnlineTime = 0
    }
    
    mutating func paymentIncrease(_ addition: Int) {
        self.totalPaymentAmount += addition
    }
    
    mutating func clearPaymentAmount() {
        self.totalPaymentAmount = 0
    }
    
    mutating func updateTimestamp() {
        self.timestamp = Date()
    }
    
    mutating func getServerTimestamps() -> [[Int]] {
        if self.onlineServerTimeStamps == nil {
            self.onlineServerTimeStamps = [[Int]]()
        }
        
        return self.onlineServerTimeStamps!
    }
    
    mutating func getLocalTimestamps() -> [[Int]] {
        if self.onlineLocalTimeStamps == nil {
            self.onlineLocalTimeStamps = [[Int]]()
        }
        
        return self.onlineLocalTimeStamps!
    }
    
    mutating func addOneServerTimestamps(timeStamps:[Int]) {
        
        if self.onlineServerTimeStamps == nil {
            self.onlineServerTimeStamps = [[Int]]()
        }
        
        if timeStamps[0] == timeStamps[1] {
            return
        }
        
        if timeStamps[0] == 0 || timeStamps[1] == 0 {
            return
        }
        
        self.onlineServerTimeStamps?.append(timeStamps)
        Logger.debug("添加网络时间戳：\(timeStamps)")
    }
    
    mutating func addOneLocalTimestamps(timeStamps:[Int]) {
        if self.onlineLocalTimeStamps == nil {
            self.onlineLocalTimeStamps = [[Int]]()
        }
        
        if timeStamps[0] == timeStamps[1] {
            return
        }
        
        if timeStamps[0] == 0 || timeStamps[1] == 0 {
            return
        }
        
        self.onlineLocalTimeStamps?.append(timeStamps)
        Logger.debug("添加本地时间戳：\(timeStamps)")
    }
    
    mutating func clearTimeStamps() {
        onlineServerTimeStamps?.removeAll()
        onlineLocalTimeStamps?.removeAll()
    }
    
    mutating func checkOutdateTimeStamps() {
        if onlineLocalTimeStamps != nil,onlineLocalTimeStamps!.count > 0 {
            var index = -1
            let endTime = (onlineLocalTimeStamps?.last?.last)!
            for (idx,curTimestamps) in onlineLocalTimeStamps!.enumerated() {
                let curEndTime = curTimestamps[1]
                if endTime - curEndTime >= 24 * 60 * 60 {
                    index = idx
                }else {
                    break
                }
            }
            
            
            if index > 0 {
                onlineLocalTimeStamps?.removeSubrange(0...index)
            }
        }
        
        if onlineServerTimeStamps != nil,onlineServerTimeStamps!.count > 0 {
            var index = -1
            let endTime = (onlineServerTimeStamps?.last?.last)!
            for (idx,curTimestamps) in onlineServerTimeStamps!.enumerated() {
                let curEndTime = curTimestamps[1]
                if endTime - curEndTime >= 24 * 60 * 60 {
                    index = idx
                }else {
                    break
                }
            }
            
            
            if index > 0 {
                onlineServerTimeStamps?.removeSubrange(0...index)
            }
        }
    }
    
    mutating private func resetUserInfoButId() {
        self.type = .unknown
        self.age = -1
        self.idCardNumber = nil
        self.realName = nil
        self.phone = nil
        self.totalOnlineTime = 0
        self.totalPaymentAmount = 0
        self.timestamp = Date()
    }
}


extension User {
    init() {
        self.id = ""
        self.type = .unknown

        self.age = -1

        self.idCardNumber = nil
        self.realName = nil
        self.phone = nil

        self.totalOnlineTime = 0
        self.totalPaymentAmount = 0

        self.timestamp = Date()
    }
    
    init(id: String, type: UserType = .unknown) {
        self.init()
        
        self.id = id
        self.type = type
    }
    
//    init(id: String, type: UserType, age: Int, idCardNumber: String, realName: String, phone: String, totalPlayDuration: Int, totalPaymentAmount: Int, timestamp: Date) {
//        self.init()
//
//        self.id = id
//        self.type = type
//
//        self.age = age
//
//        self.idCardNumber = idCardNumber
//        self.realName = realName
//        self.phone = phone
//
//        self.totalPlayDuration = 0
//        self.totalPaymentAmount = 0
//
//        self.timestamp = timestamp
//    }
}
