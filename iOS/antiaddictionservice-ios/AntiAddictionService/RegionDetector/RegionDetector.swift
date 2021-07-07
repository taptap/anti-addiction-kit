
import Foundation
import CoreTelephony

internal class RegionDetector {
    
    // MARK: - Public
    
    static var isMainlandUser: Bool {
        // TODO
        return true
//        let key = Key<Bool>(RegionDetector.mainlandUserKey)
//        return Defaults.shared.get(for: key) ?? false
    }
    
    /// 是否检测过
    static var isDetected: Bool {
        let key = Key<Bool>(RegionDetector.detectMarkKey)
        return Defaults.shared.get(for: key) ?? false
    }
   
    /// 检测地区
    static func detect() {
        var isMainlandUser: Bool = false
        
        // 将检测结果保存在本地，保证最后执行
        defer {
            let userKey = Key<Bool>(self.mainlandUserKey)
            Defaults.shared.set(isMainlandUser, for: userKey)
            
            let markKey = Key<Bool>(self.detectMarkKey)
            Defaults.shared.set(true, for: markKey)
        }
        
        // 通过运营商和设备地区判断，有一个满足条件则判断为大陆用户
        isMainlandUser = (self.isMainlandCarrier || self.isMainlandDeviceLocale)
        return
    }
    
    // MARK: - Private
    
    private static let detectMarkKey: String = "detectMark"
    private static let mainlandUserKey: String = "isMainlandUser"
    
    /// 是否中国大陆运营商（判断条件 MobileCountryCode == 460/461）
    static var isMainlandCarrier: Bool {
        let telephony = CTTelephonyNetworkInfo()
        
        if #available(iOS 12.0, *) {
            if let carrierDictionary = telephony.serviceSubscriberCellularProviders {
                for (_, carrier) in carrierDictionary {
                    if let mobileCountryCode = carrier.mobileCountryCode, (mobileCountryCode == "460") || (mobileCountryCode == "461") {
                        return true
                    }
                }
            }
        } else {
            if let carrier = telephony.subscriberCellularProvider, let mobileCountryCode = carrier.mobileCountryCode {
                return ((mobileCountryCode == "460") || (mobileCountryCode == "461"))
            }
            
        }
        
        return false
    }
    
    /// 设备地区是否中国大陆（判断条件 regionCode == "CN"）
    private static var isMainlandDeviceLocale: Bool {
        let locale = Locale.current
        if let regionCode = locale.regionCode, regionCode == "CN" {
            return true
        }
        if String(locale.identifier.suffix(2)) == "CN" {
            return true
        }
        return false
    }
    
}
