# AntiAddictionService-iOS

## 1.接入前准备

### 1.1 AntiAddictionService 说明
是为了应对最新防沉迷政策而编写的一个集实名登记、防沉迷时长限制、付费限制三部分功能的组件，方便国内游戏团队快速接入游戏实现防沉迷功能从而符合政策规定。

**最低iOS版本为iOS 9** .SDK 编译环境为Xcode 12。	
### 1.2 添加 AntiAddictionService
说明：AntiAddictionService iOS 源码由 **Swift** 编写，但接口同时支持 Swift 或 Objective-C 使用，文档中代码示例同时包含 Swift 与 Objective-C 两种代码使用，可根据自身项目需要选择。


#### 1.2.1 添加 AntiAddictionService 文件
1. 打开AntiAddictionService.xcodeproj,选中Target为UniversalFramework工程得到 AntiAddictionKit.Framework 完整架构的动态库，引入现有游戏的 Xcode 工程
2. 修改 Xcode 工程的 BuildSettings 的 Always Embed Swift Standard Libraries 为 Yes，即始终引入 Swift 标准库，避免 App 启动时报错（无法找到 Swift标准库之类）
3. 代码引用

```
// Swift
import AntiAddictionService

// Objective-C
@import AntiAddictionService;
```
	

	
### 1.3 添加系统依赖库
请检查项目中是否已自动添加以下依赖项：

	libc++.tdb

	
若运行时遇到相关依赖库加载报错，可改为 Optional 尝试。

### 1.4 配置编译选项
**在 Build Setting 中的 Other Link Flag 中添加 -ObjC**

**在 Build Setting 中的 Always Embed Swift Standard Libraried 设置为 YES**



## 2. AntiAddictionService 使用说明

### 2.1 AntiAddictionService 配置（采用默认值可跳过）

#### 2.1.1 基础配置 

参数 | 类型 | 默认值 | 说明 
--- | --- | --- | ---
`useSdkPaymentLimit` | `Bool` | `true` | 是否使用 SDK 付费限制
`useSdkOnlineTimeLimit` | `Bool` | `true` | 是否使用 SDK 在线时长限制

使用示例：

```
// swift
AntiAddictionService.configuration.useSdkPaymentLimit = true
AntiAddictionService.configuration.useSdkOnlineTimeLimit = true
```

```
// Objective-C
AntiAddictionService.configuration.useSdkPaymentLimit = YES;
AntiAddictionService.configuration.useSdkOnlineTimeLimit = YES;
```

或直接传递对应参数（参数顺序）： `useSdkPaymentLimit`, `useSdkOnlineTimeLimit`)。

使用示例：

```
// swift
AntiAddictionService.setFunctionConfig(true, true)
```

```
// Objective-C
[AntiAddictionService setFunctionConfig:YES :YES];
```


### 2.2 初始化
示例如下：

```
// swift
AntiAddictionService.init(yourCallbackDelegate,gameIdentifier;
```

```
// Objective-C
[AntiAddictionService init: yourCallbackDelegate gameIdentifier:@"demo"];
```

其中 `yourCallbackDelegate` 为游戏内接受回调的对象,`gameIdentifier ` 为该游戏唯一标识符。
回调中会返回对应的回调类型码 `code` 和相应信息 `message`。部分 `code` 为预留，游戏只需处理必要的回调即可。

<a name="回调类型"></a>

回调类型 | `code` |  触发逻辑 | `message`（仅供游戏接受回调时参考）
--- | --- | --- | ---
playTimeLimitNone | 100 | 游戏时长无限制 | -
loginSuccess | 500 | 游戏调用 login 后用户完成登录流程 | -
logout | 1000 | 游戏调用 logout 登出账号 | -
payLimitNone | 1020 | 付费不受限，sdk检查用户付费无限制时触发| -
payLimitReachLimit | 1025 | 付费受限，付费受限触发,包括游客未实名或付费额达到限制等 | -
playTimeLimitNoTime | 1030 | 时间受限，未成年人或游客游戏时长**接近或已达限制**，剩余时长请依据 remainTime 字段 | -
openRealName | 1060 | SDK请求打开游戏的实名窗口，当游戏查询支付或聊天限制时触发 | -
noChatLimit | 1080 | 聊天无限制，用户已通过实名，可进行聊天 | 无
hasChatLimit | 1090 | 聊天限制，用户未通过实名，不可进行聊天 | 无
openAlertTip | 1095 | SDK 请求打开弹窗提示，具体内容解析 json 格式的 message | 
closeAlertTip | 1096 | SDK 请求关闭所有防沉迷弹窗 |
updateConfig | 1100 | SDK 配置更新，如相关提示语等 |


	
### 2.3 登录/登出
游戏只需在用户登录时调用登录接口，传入用户唯一标识符（如用户 ID等）,SDK 即会接管所有防沉迷逻辑。
在用户登出时调用登出接口，暂停防沉迷计时。

调用以下接口
	
```
// swift
let userId = "xxxx"
// 登录
AntiAddictionService.login(userId)
// 登出
AntiAddictionService.logout
```

```
// Objective-C
// 登录
[AntiAddictionService login:@"xxxx"];
// 登出
[AntiAddictionService logout];
```
    
如果步骤一配置的 `useSdkOnlineTimeLimit` 值为 `true`，则 sdk 会根据当前政策主动限制游戏时长，反之不会限制用户游戏时长。

调用登录以后，会收到用户剩余游戏时长或者登录成功的回调，游戏可弹窗提醒用户，在游戏和用户做完所有登录时操作后（如提示弹窗关闭，用户协议关闭等），**调用以下接口开始防沉迷计时**。

```
// swift
AntiAddictionService.enterGame
```

```
// Objective-C
[AntiAddictionService enterGame];
```


### 2.4 付费检查
游戏在收到用户的付费请求后，调用 SDK 的对应接口来判断当前用户的付费行为是否被限制，示例如下：

```
// swift
AntiAddictionService.checkPayLimit(100)
```

```
// Objective-C
[AntiAddictionService checkPayLimit:100];
```

接口参数表示付费的金额，单位为分（例如1元道具=100分）。当用户可以发起付费时，SDK 会调用回调 [payLimitNone](#回调类型) 通知游戏,否则调用 [payLimitReachLimit](#回调类型) 回调;   
当用户完成付费行为时，游戏需要通知 SDK ，更新用户状态，示例如下：

```
// swift
 AntiAddictionService.paySuccess(100)
```

```
// Objective-C
[AntiAddictionService paySuccess:100];
```

参数为本次充值的金额，单位为分。

##### 注意：如果用户在付费过程中需要进行实名，会收到 [openRealName](#回调类型) 回调,实名完成后,需再次调用 [checkPayLimit] 接口，SDK 才能判断用户类型并发出 [是否付费限制] (#回调类型) 的回调。

### 2.4 聊天检查
游戏在需要聊天时，调用 SDK 接口判断当前用户是否实名，示例如下：

```
// swift
 AntiAddictionService.checkChatLimit()
```

```
// Objective-C
[AntiAddictionService checkChatLimit];
```

当用户可以聊天时， SDK 会通过聊天回调 [noChatLimit](#回调类型) 来通知游戏，否则就会直接让用户进行实名。如果此时需要打开第三方的实名页面，SDK 会调用 [openRealName](#回调类型) 回调。实名完成后，需再次调用 [checkChatLimit]() 接口，SDK 才能判断用户类型并发出 [是否聊天限制](#回调类型) 的回调。

### 2.5 实名
防沉迷 SDK 提供了实名接口，游戏可在用户输入身份证和姓名以后，调用实名接口进行认证，接收回调确认是否成功。

```
// swift
AntiAddictionService.realNameAuth(userToken: "testUserId", name: "name", idCard: "idNumber", phone: "phoneNumber") { identifyState, errorMessage in
    // handle result
}
```

```
// Objective-C
[AntiAddictionService realNameAuthWithUserToken:@"testUserId" name:@"name" idCard:@"idNumber" phone:@"phoneNumber" completion:^(enum AntiAddictionRealNameAuthState identifyState, NSString * _Nonnull errorMessage) {
	// handle result
}];
```

可以调用以下接口查询用户实名信息。

```
// swift
AntiAddictionService.checkRealnameState(userToken: "testUserId") { identifyState, userToken, idCardNumber, name in
    // handle result
} failureHandler: { errorMessage in
    // handle error
}
```

```
// Objective-C
[AntiAddictionService checkRealnameStateWithUserToken:testUserId completion:^(enum AntiAddictionRealNameAuthState state, NSString * _Nonnull userToken, NSString * _Nonnull idCard, NSString * _Nonnull name) {
	// handle result
} failureHandler:^(NSString * _Nonnull errorMsg) {
	// handle error
}];
```

实名状态说明。


```
public enum AntiAddictionRealNameAuthState:Int,Codable {
    case success = 0                     // 认证成功
    case verifying = 1                   // 认证中
    case fail = 2                        // 认证失败
}
```