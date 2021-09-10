# AntiAddictionKit-Android

## 1.接入前准备

### 1.1 AntiAddictionKit 说明
是为了应对最新防沉迷政策而编写的一个集实名登记、防沉迷时长限制、付费限制三部分功能的组件，方便国内游戏团队快速接入游戏实现防沉迷功能从而符合政策规定。

**最低Android版本为4.4** .SDK编译环境为Android Studio。	

### 1.2 配置 AntiAddiction 的服务器参数
- 在`lib-antiaddiction`模块下的`build.gradle`文件下进行防沉迷参数的配置（详情请见防沉迷服务端说明）
```
Android {
...
    productFlavors {
        // 正式环境
        Publish {
            dimension = "staging"
            // 实名认证服务端配置的signKey
            buildConfigField("String", "IDENTIFICATION_SIGN_KEY", "\"e5d341b5aed6110da68f93e06aff47db\"")
            // 实名认证服务地址
            buildConfigField("String", "IDENTIFICATION_HOST", "\"http://172.19.101.76\"")
            // 防沉迷服务地址
            buildConfigField("String", "ANTI_ADDICTION_HOST", "\"http://172.19.56.86:7005\"")
            // 防沉迷对接中宣部服务长连接服务地址
            buildConfigField("String", "WEB_SOCKET_HOST", "\"ws://172.19.101.76/ws/v1\"")
        }
        // 测试环境
        Inhouse {
            dimension = "staging"
            buildConfigField("String", "IDENTIFICATION_SIGN_KEY", "\"e5d341b5aed6110da68f93e06aff47db\"")
            buildConfigField("String", "IDENTIFICATION_HOST", "\"http://172.19.101.76\"")
            buildConfigField("String", "ANTI_ADDICTION_HOST", "\"http://172.19.56.86:7005\"")
            buildConfigField("String", "WEB_SOCKET_HOST", "\"ws://172.19.101.76/ws/v1\"")
        }
    }
...
}
```

### 1.3 导入 AntiAddiction
- 将编译好的AntiAddiction_${AntiAddictionVersion}.aar拷贝到游戏目录下的src/main/libs目录中
- 将lib-antiaddiction/src/main/libs目录下的gson-2.8.6.jar拷贝到游戏目录下的src/main/libs目录中
- 在游戏目录下build.gradle文件中添加代码
```
// 在游戏目录下的build.gradle中添加 
repositories{flatDir{dirs 'src/main/libs'}}

dependencies {
...
    implementation(name: "AntiAddiction_${AntiAddictionVersion}", ext: "aar")
    implementation(name: "gson-2.8.6", ext: "jar")
...
}

```

## 2. AntiAddictionKit 使用说明
### 2.1 初始化
示例如下：
```
AntiAddictionFunctionConfig antiAddictionFunctionConfig = new AntiAddictionFunctionConfig.Builder()
                        .enablePaymentLimit(true)
                        .enableOnLineTimeLimit(true)
                        .withAntiAddictionServerUrl("${部署的防沉迷服务域名}")
                        .withIdentifyVerifiedServerUrl("${部署的实名认证服务域名}")
                        .withDepartmentSocketUrl("${部署的中宣部上报服务域名}")
                        .withAntiAddictionSecretKey("${实名认证的SecretKey}")
                        .build()
AntiAddictionKit.init(context, gameIdentifier
, antiAddictionFunctionConfig
, antiAddictionCallback); 
```

其中 context`android系统的上下文，这边可以传activity,也可以传application,`antiAddictionFunctionConfig` 为防沉迷配置（打开时长限制，打开付费限制）, `gameIdentifier ` 为该游戏唯一标识符，`antiAddictionCallback` 为游戏内接受回调的对象`。
回调中会返回对应的回调类型码 `code` 和相应信息 `message`。部分 `code` 为预留，游戏只需处理必要的回调即可。

<a name="回调类型"></a>

回调类型 | `code` |  触发逻辑 | `message`（仅供游戏接受回调时参考）
--- | --- | --- | ---
CALLBACK_CODE_LOGIN_SUCCESS | 500 | 游戏调用 login 后用户完成登录流程 | -
CALLBACK_CODE_LOGOUT | 1000 | 游戏调用 logout 登出账号 | -
CALLBACK_CODE_TIME_LIMIT | 1030 | 游客登录时会返回该消息，会返回游客无法进行游戏的消息
CALLBACK_CODE_NIGHT_STRICT | 1050 | 时间受限，未成年该时段无法进行游戏（根据国家新闻出版署针对未成年人发布的新规 **仅允许未成年人在周五、周六、周日和法定节假日的 20:00 至 21:00 游玩游戏**）
CALLBACK_CODE_OPEN_ALERT_TIP | 1095 | SDK 请求打开弹窗提示，具体内容解析 json 格式的 message | 1、防沉迷登录成功且当前时段未成年人可以进行游戏 2、游戏时长剩余15分钟 3、游戏剩余时长60秒
	
### 2.2 登录/登出
游戏只需在用户登录时调用登录接口，传入用户唯一标识符（如用户 ID等）,SDK 即会接管所有防沉迷逻辑。
在用户登出时调用登出接口，暂停防沉迷计时。

调用以下接口
	
```
// 登录
AntiAddictionKit.login(userId)
// 登出
AntiAddictionKit.logout()
```

如果步骤一配置的 `AntiAddictionFunctionConfig.useSdkOnlineTimeLimit` 值为 `true`，则 sdk 会根据当前政策主动限制游戏时长，反之不会限制用户游戏时长。

调用登录以后，会收到用户剩余游戏时长或者登录成功的回调，游戏可弹窗提醒用户，在游戏和用户做完所有登录时操作后（如提示弹窗关闭，用户协议关闭等），**调用以下接口开始防沉迷计时/停止计时**。

```
// 开始计时
AntiAddictionKit.enterGame()
// 停止计时
AntiAddictionKit.leaveGame()
```

### 2.3 付费检查
游戏在收到用户的付费请求后，调用 SDK 的对应接口来判断当前用户的付费行为是否被限制，示例如下：

```
/**
 * @param payAmount 付费的金额，单位为分（例如1元道具=100分）
 * @param callback 
 */
AntiAddictionKit.checkPayLimit(payAmount, new Callback<CheckPayResult>() {
    @Override
    public void onSuccess(CheckPayResult result) {
        // 可以购买
        if (result.status) {
        } 
        // 不可以购买
        else {
            // result.title返回失败提示标题, result.description返回失败提示描述
        }
    }

    @Override
    public void onError(Throwable throwable) {
        // 检查时出现异常（比如网络出错）游戏自行决定提示方式
    }
});
```

当用户完成付费行为时，游戏需要通知 SDK ，更新用户付费情况，示例如下：
```
/**
 * @param payAmount 付费的金额，单位为分（例如1元道具=100分）
 * @param callback 
 */
AntiAddictionKit.paySuccess(payAmount, new Callback<SubmitPayResult>() {

    @Override
    public void onSuccess(SubmitPayResult result) {
        // 提交成功
    }

    @Override
    public void onError(Throwable throwable) {
        // 提交失败
    }
});
```

### 2.4 实名
防沉迷 SDK 提供了实名接口，游戏可在用户输入身份证和姓名以后，调用实名接口进行认证，接收回调确认是否成功。
```
/**
 * @param userId 游戏中用户唯一标识
 * @param name 姓名
 * @param idCard 身份证
 * @param callback
 */
AntiAddictionKit.authIdentity(
    userId
    , name
    , idCard
    , phoneNumber // 预留字段
    , new Callback<IdentifyResult>() {
        @Override
        public void onSuccess(IdentifyResult result) {
           // result.identifyState 可以查看实名情况 0:实名成功 1:实名中 2:实名失败
        }

        @Override
        public void onError(Throwable throwable) {
            // 实名认证失败 （由于一些非预期的异常，比如网络出错）
        }
    }
);
```

可以调用以下接口查询用户实名信息。
```
AntiAddictionKit.fetchUserIdentifyInfo(currentUserId, new Callback<com.tapsdk.antiaddiction.entities.IdentificationInfo>() {
    @Override
    public void onSuccess(IdentificationInfo result) {
        // 获取实名信息成功
    }

    @Override
    public void onError(Throwable throwable) {
       // 获取实名信息失败（由于一些非预期的异常，比如网络出错）
    }
});
```

### 2.5 获取防沉迷token
```
// 如果已登录防沉迷的情况下，获取当前token，否则返回空字符串
String currentToken = AntiAddictionKit.currentToken();
```

