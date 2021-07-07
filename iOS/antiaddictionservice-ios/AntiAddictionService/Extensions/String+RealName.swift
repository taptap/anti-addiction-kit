
import Foundation


/// 年龄相关
extension String {
    /// 从身份证中拿出日期 yyyyMMdd 20200101
    func yyyyMMdd() -> String? {
        if (count != 15 && count != 18) {
            return nil
        }
        
        if (count == 15) {
            let dateStart = index(startIndex, offsetBy: 6)
            let dateEnd = index(startIndex, offsetBy: 13)
            let date = "19" + String(self[dateStart...dateEnd])
            return (date.count == 8 ? date : nil)
        }
        
        if (count == 18) {
            let dateStart = index(startIndex, offsetBy: 6)
            let dateEnd = index(startIndex, offsetBy: 13)
            let date = String(self[dateStart...dateEnd])
            return (date.count == 8 ? date : nil)
        }
        
        return nil
    }
}
