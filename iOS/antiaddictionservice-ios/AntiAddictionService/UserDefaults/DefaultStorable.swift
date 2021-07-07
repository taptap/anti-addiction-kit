
import Foundation

public protocol DefaultStorable {
    static var defaults: UserDefaults { get }
    static var defaultIdentifier: String { get }
    static var defaultValue: Self? { get }
    static func read(forKey key: String?) -> Self?
    func write(withKey key: String?)
    func clear(forKey key: String?)
    static func clear(forKey key: String?)
}

extension DefaultStorable where Self: Codable {
    public static var defaultIdentifier: String {
        return String(describing: type(of: self))
    }

    public static var defaults: UserDefaults {
        return UserDefaults.standard
    }

    public static var defaultValue: Self? {
        return nil
    }

    public func write(withKey key: String? = nil) {
        let key: String = key ?? Self.defaultIdentifier
        Self.defaults.df.store(self, forKey: key)
    }

    public static func read(forKey key: String? = nil) -> Self? {
        let key: String = key ?? defaultIdentifier
        return defaults.df.fetch(forKey: key, type: Self.self) ?? defaultValue
    }

    public func clear(forKey key: String? = nil) {
        type(of: self).clear(forKey: key)
    }

    public static func clear(forKey key: String? = nil) {
        let key: String = key ?? Self.defaultIdentifier
        return Self.defaults.removeObject(forKey: key)
    }
}
