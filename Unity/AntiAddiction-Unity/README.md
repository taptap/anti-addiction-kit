# 游戏防沉迷 AntiAddiction (Unity) 对接文档
AntiAddictionSDK 是为了遵循最新防沉迷政策而编写的一个集实名登记、防沉迷时长限制、付费限制三部分功能的组件，方便国内游戏团队快速接入游戏实现防沉迷功能从而符合政策规定。

# 说明
Unity 模块是通过引入 iOS 和 Android 模块后增加桥接文件打包出的 `.unitypackage`，方便以 Unity 开发的游戏直接引入。其他引擎/平台的游戏可以通过 iOS/Android 原生的方式接入，详见 iOS/Android 各模块接入文档。

## 1.接入SDK
Unity 开发环境:2018.4.36f1

导入 `AntiAddictionForUnity.unitypackage`

### 1.1 iOS
- iOS Deployment Target 最低支持 iOS 10.0
- Xcode 13.0 beta 5 编译 

>注意:  
>`unitypackge`中默认 iOS 平台 `AntiAddictionService.framework` 为真机设备架构，如需生成模拟器包进行测试。
>
> `.xcframework` 是 WWDC 2019 推出的 Framework 替代品，自带模拟器和真机架构，其使用方法与原`.framework` 基本相同
> `AntiWorkspace.xcworkspace` 同时包含 `i386`, `x86_64`,`armv7`, `armv7s`, `arm64` 等多种真机和模拟器架构
>

**检查 Unity 输出的 Xcode 工程**

1. 请确保设置 `Xcode` - `General` - `Frameworks, Libraries, and Embedded Content` 中的 `AntiAddictionService.framework` 为 `Do Not Embed`。
2. 如果编译报错找不到头文件或者模块，请确保 `Xcode`-`Build Settings` - `Framework Search Paths` 中的路径以保证 Xcode 正常编译。
3. 确保 Xcode 工程的 `Build Settings` 的 `Swift Compile Language/Swfit Language Version` 为 `Swift5`。
4. 添加依赖库 `libz.tbd`
5. 开始代码接入

> 请确保以上步骤正确执行。

### 1.2 Android
最低支持安卓版本 4.4。

## 2.接口文档
防沉迷需要游戏提供用于授权防沉迷的游戏唯一id（需要保证唯一即可，建议不要使用游戏中的用户id，如果一定要使用可以进行hash处理，客户端对长度无限制，服务端支持最长32位的字符）。

**以下使用需要SDK命名空间下**
```
using Plugins.AntiAddictionKit
```

### 2.1 初始化
初始化SDK并设置回调，初始化方法接收Action作为回调
- 参数介绍
- gameIdentifier 游戏名称标识（游戏自行定义）
- useTimeLimit 启用时长限制功能
- usePaymentLimit 启用付费限制功能
- antiServerUrl 防沉迷服务域名
- identifyServerUrl 实名服务域名
- departmentWebSocketUrl 中宣部长连服务域名
- antiSecretKey 防沉迷服务密钥
示例如下：

```
AntiAddictionKit.Init(gameIdentifier, useTimeLimit, usePaymentLimit,antiServerUrl,identifyServerUrl,departmentWebSocketUrl,antiSecretKey
                , (antiAddictionCallbackData) => // 防沉迷事件回调


```

回调中会返回对应的回调类型码 resultCode 和相应信息 message：

回调类型 | 参数值 |  触发条件 | 附带信息
--- | --- | --- | ---
CALLBACK\_CODE\_ENTER\_SUCCESS | 500 | 登录通过，当用户登录过程中通过防沉迷限制时会触发 | 无
CALLBACK\_CODE\_SWITCH_ACCOUNT | 1000 | 切换账号，当用户因防沉迷机制受限时，选择切换账号时会触发 | 无
CALLBACK\_CODE\_TIME\_LIMIT | 1030 | 时间受限，未成年人或游客游戏时长已达限制，通知游戏 | 给用户返回提示信息
CALLBACK\_CODE\_PAY\_NO\_LIMIT | 1020 | 付费不受限，sdk检查用户付费无限制时触发| 无                 （iOS专用）
CALLBACK\_CODE\_PAY\_LIMIT | 1025 | 付费受限，付费受限触发,包括游客未实名或付费额达到限制等 | 触发原因   (iOS专用)


#### 2.2 登录

登录接口应只在游戏登录过程中、登出后以及收到回调 ”SWITCH_ACCOUNT" 时调用。

调用示例：

```
AntiAddictionKit.Login("12345");
```
该接口中共有两个参数，第一个是用户的唯一标识，类型为字符串，第二个代表当前用户的类型.参考上表



#### 2.3 登出
当用户在游戏内点击登出或退出账号时调用该接口。

调用示例如下：

```
AntiAddictionKit.Logout();
```

#### 2.4 开始游戏
用户已登录情况下，并且开始游戏调用该接口。

调用示例如下:
```
AntiAddictionKit.EnterGame();
```

#### 2.4 停止游戏
用户已登录情况下，并且停止游戏调用该接口。

调用示例如下:
```
AntiAddictionKit.LeaveGame();
```

#### 2.5 付费
游戏在收到用户的付费请求后，调用 SDK 的对应接口来判断当前用户的付费行为是否被限制，示例如下：

```
AntiAddictionKit.CheckPayLimit(100, (checkPayResult) =>
            {
                logText = "status:" + checkPayResult.status + ", title:" + checkPayResult.title + ", description:" + checkPayResult.description;
            },
            (exception) =>
            {
                logText = "CheckPayLimit Exception:" + exception;
            });
```

接口参数1表示付费的金额，单位为分（例如1元道具=100分）, 参数2为检测是否可以支付回调(status为1时可以支付，否则提示用户失败信息)， 参数3为检测是否可以支付接口异常(提示用户失败信息)
```
AntiAddictionKit.SubmitPayResult(100, () =>
            {
                logText = "SubmitPayResult success";
            }, (exception) =>
            {
                logText = "SubmitPayResult Exception:" + exception;
            });
```
参数1为本次充值的金额，单位为分， 参数2为上报支付结果成功回调，参数3为上报支付结果失败回调。

##### 注意：如果用户在付费过程中需要打开第三方页面进行实名，实名完成后，游戏除了要调用 "setUser" [更新用户信息](#设置用户信息) , 还需再次调用 " checkPayLimit " 接口才能收到 [是否付费限制] (#回调类型) 的回调。

### 2.6. 时长统计
如果步骤一配置的 useSdkOnlineTimeLimit = true，则 sdk 会根据当前政策主动限制游戏时长，反之不会限制用户游戏时长。

安卓平台需要注意，在unity的OnApplicationPause调用onResume和onStop方法

示例如下：

```
void OnApplicationPause(bool pauseStatus){
	if (pauseStatus)
	{
		AntiAddictionKit.LeaveGame();

	}else
	{
		AntiAddictionKit.EnterGame();

	}
}
```

#### 2.7. 获取用户实名信息
SDK 初始化后，可以根据防沉迷登录用的游戏id（）获取该用户实名信息

```
AntiAddictionKit.FetchIdentificationInfo(userId, (identificationInfo) =>
            {
                Debug.Log("FetchIdentificationInfo callback");
                logText = "authState:" + identificationInfo.authState + ", name:" + identificationInfo.name + ", idCard:" + identificationInfo.idCard + ", antiaddiction_token:" + identificationInfo.antiAddictionToken;
            }, (exception) =>
            {
                logText = "FetchIdentificationInfo Exception:" + exception;
            });
```
参数是用户的唯一标识字符串，返回值参考
实名信息说明。

```
authState {0:认证成功,1:认证中,2:认证失败}
name {姓名，如果没有实名返回空}
idCard {身份证信息，如果没有实名返回空}
antiaddiction_token {用户防沉迷token，可以提供给游戏服务端查询用户剩余时长使用}
```

#### 2.8. 实名认证
如题
参数介绍
userId 游戏唯一id
useName 真实姓名
userIDCard 真实身份证号
```
 AntiAddictionKit.AuthIdentity(userId, userName, userIDCard, (identifyResult) =>
            {
                Debug.Log("AuthIdentity callback");
                logText = "实名结果[identifyState:" + identifyResult.identifyState + "]";
            }, (exception) =>
            {
                logText = "AuthIdentity Exception:" + exception;
            });
```
返回结果
```
authState {0:认证成功,1:认证中,2:认证失败}
```

#### 2.9 获取游戏剩余时长
游戏辅助使用（更新有延迟），获取成功返回剩余时长，获取失败返回-1

```
AntiAddictionKit.CurrentUserRemainTime()
```

#### 2.10 获取用户类型
如果已经登录返回用户类型，否则返回-1

```
AntiAddictionKit.CurrentUserType()
```

#### 2.11 获取防沉迷token
获取防沉迷token，获取失败返回空
```
AntiAddictionKit.CurrentToken()
```
