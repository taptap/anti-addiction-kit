import Foundation
import SwiftWebSocket
import UIKit

class AntiAddictionSocket {
    public static let shared = AntiAddictionSocket()
    public  var isConnected: Bool = false
    private var sockect: WebSocket? = nil
    
    // 定时ping
    private var pingTimer:Timer? = nil
    
    // 进入后台时间
    private var lastBackgroundTime:Double = 0
    
    // 已经重连的次数，重连取消后置为1
    private var reConnectCount: Int = 1

    // 心跳超时定时器
    var disconnectTimer:DispatchSourceTimer?
    // 梯度重连定时器
    var reConnectTimer:DispatchSourceTimer?
    
    // 上次离线时间，获取离线消息用
    var offlineTimestamp:Double = 0
    
    init() {
        addNotificationListener()
    }

    public func connect() {
        disconnect()
        if let socketAddress = AntiAddictionService.configuration.websocketAddress,
           let token = AccountManager.currentAccount?.token,
           let gameIdentifier = AntiAddictionService.configuration.gameIdentifier,
           let urlRequest = urlRequest(from: "\(socketAddress)?Authorization=\(token)&client_id=\(gameIdentifier)") {
            sockect = WebSocket(request: urlRequest)
            sockect?.delegate = self;
            sockect?.open()
        } else {
            Logger.info("endpointUrl 无效，无法连接 WebSocket Server")
        }
    }
    
    public func disconnect() {
        stopPing()
    }
}

extension AntiAddictionSocket {
    func addNotificationListener () {
        NotificationCenter.default.addObserver(forName: UIApplication.didBecomeActiveNotification, object: nil, queue: nil) { (_) in
            guard User.shared != nil else {
                Logger.info("用户未登录")
                return
            }
            if Date().timeIntervalSince1970 - self.offlineTimestamp > 5 * 60 {
                // 断线超过5分钟,所有消息重新获取
                self.updateOfflineTime(timestamp: 0)
            }
            
            Logger.info("socket重连")
            self.connect()
        }
        
        NotificationCenter.default.addObserver(forName: UIApplication.willResignActiveNotification, object: nil, queue: nil) { (_) in
            self.disconnect()
            guard User.shared != nil else {
                Logger.info("用户未登录")
                return
            }
            self.updateOfflineTime(timestamp: Date().timeIntervalSince1970)
        }
    }
    
    func updateOfflineTime(timestamp:Double) {
        offlineTimestamp = timestamp
    }
}

extension AntiAddictionSocket {
    // handle error and ping pong
    public func startPing() {
        pingTimer = Timer(timeInterval: 50, target: self, selector: #selector(ping), userInfo: nil, repeats: true)
        pingTimer?.fire()
        RunLoop.current.add(pingTimer!, forMode: .common)
        isConnected = true
        resetReConnectTimer()
    }
    
    public func stopPing () {
        pingTimer?.invalidate()
        self.disconnectTimer?.cancel()
        if isConnected {
            sockect?.close()
        }
        isConnected = false
    }
    
    @objc public func ping() {
        sockect?.ping()
        Logger.info("Socket ping!")
        
        self.disconnectTimer = DispatchSource.makeTimerSource()
        // 45s超时收不到心跳断开连接
        self.disconnectTimer?.schedule(deadline: .now() + 45, repeating: 100)
        self.disconnectTimer?.setEventHandler(handler: {
            DispatchQueue.main.async {
                Logger.info("Socket 心跳断开，尝试重连")

                self.stopPing()
                self.reConnect()
            }
            
            self.disconnectTimer?.cancel()
        })
        self.disconnectTimer?.resume()
    }
    
    public func handlePong () {
        Logger.info("Socket pong!")
        self.disconnectTimer?.cancel()
    }
    
    public func reConnect() {
        // 正在重连
        if reConnectTimer != nil {
            return
        }
        let interval:TimeInterval = 5
        connect()
        
        reConnectTimer = DispatchSource.makeTimerSource()
        let curInterval = interval * Double(self.reConnectCount)
        reConnectTimer?.schedule(deadline: .now() + curInterval  + getRandom(interval: curInterval), repeating: 100)
        reConnectTimer?.setEventHandler(handler: {
            if self.isConnected || self.reConnectCount > 5 {
                self.resetReConnectTimer()
                self.reConnectCount = 1
                return
            }
            self.resetReConnectTimer()
            self.reConnect()
            // 重连之后再增加 count
            Logger.info("AntiAddictionSocket socket重连，第\(self.reConnectCount)次")
            self.reConnectCount += 1
        })
        reConnectTimer?.resume()
    }
    
    // 加上随机数，避免瞬时重连数过多
    func getRandom(interval:TimeInterval)->TimeInterval {
        return TimeInterval(arc4random() % UInt32(interval))
    }
    
    func resetReConnectTimer() {
        reConnectTimer?.cancel()
        reConnectTimer = nil
    }
}

extension AntiAddictionSocket {
    private func urlRequest(from string: String?) -> URLRequest? {
        guard let urlStr = string, let url = URL(string: urlStr) else {
            return nil
        }
        var urlRequest = URLRequest(url: url)
        guard (AccountManager.currentAccount?.token) != nil else {
            return urlRequest
        }
        urlRequest.setValue(AccountManager.currentAccount?.token, forHTTPHeaderField: "authorization")
        
        return urlRequest
    }
}

extension AntiAddictionSocket: WebSocketDelegate {
    func webSocketOpen () {
        Logger.info("AntiAddictionSocket 已成功连接")
        startPing()
    }
    
    func webSocketClose(_ code: Int, reason: String, wasClean: Bool) {
        Logger.info("AntiAddictionSocket 断开, reason=`\(reason)`, code=`\(code)`")
        stopPing()
    }
    
    func webSocketError(_ error: NSError) {
        Logger.info("AntiAddictionSocket 错误 \(String(describing: error))")
        updateOfflineTime(timestamp: Date().timeIntervalSince1970)
        stopPing()
        reConnect()
    }
    
    func webSocketPong() {
        handlePong()
    }
    
    func webSocketMessageText(_ text: String) {
        handleSocketMessage(message: text)
    }

    /// 处理socket消息
    /// - Parameters:
    ///   - message: 消息
    func handleSocketMessage(message: String) {
        if message.count <= 0 {
            return
        }
        
        Logger.info("AntiAddictionSocket 收到消息, text=`\(message)")
        
    }
    
    // 系统主动关闭，稍候重连
    func systemClose() {
        stopPing()
        reConnect()
    }
}
