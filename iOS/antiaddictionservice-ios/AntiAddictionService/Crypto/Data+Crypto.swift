
import Foundation

extension Data {
    
    func decrypt() -> String {
        do {
            let aes = try AES(keyString: "FiugQTgPNwCWUY,VhfmM4cKXTLVFvHFe")
            let encryptedData: Data = self
            let decryptedString: String = try aes.decrypt(encryptedData)
            return decryptedString
        } catch {
            return ""
        }
    }
    
}
