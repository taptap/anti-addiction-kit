# AntiAddictionKit-Android

## 1.接入前准备

### 1.1 AntiAddictionKit 说明
是为了应对最新防沉迷政策而编写的一个集实名登记、防沉迷时长限制、付费限制三部分功能的组件，方便国内游戏团队快速接入游戏实现防沉迷功能从而符合政策规定。

**最低Android版本为4.4** .SDK编译环境为Android Studio。	
### 1.2 导入 AntiAddiction
- 将编译好的AntiAddiction_${AntiAddictionVersion}.aar拷贝到游戏目录下的libs目录中
- 在游戏目录下build.gradle文件中添加代码
```
// 在应用目录下的build.gradle中添加 
repositories{flatDir{dirs 'libs'}}

dependencies {
...
    implementation(name: "AntiAddiction_${AntiAddictionVersion}", ext: "aar")
...
}

```

## 2. AntiAddictionKit 使用说明

### 2.1 初始化
示例如下：
```
AntiAddiction.init(activity, gameIdentifier, antiAddictionFunctionConfig, antiAddictionCallback); 
```

其中 antiAddictionFunctionConfig` 为防沉迷配置（打开时长限制，打开付费限制）, `gameIdentifier ` 为该游戏唯一标识符，`antiAddictionCallback` 为游戏内接受回调的对象`。
回调中会返回对应的回调类型码 `code` 和相应信息 `message`。部分 `code` 为预留，游戏只需处理必要的回调即可。

<a name="回调类型"></a>

回调类型 | `code` |  触发逻辑 | `message`（仅供游戏接受回调时参考）
--- | --- | --- | ---
CALLBACK_CODE_TIME_LIMIT_NONE | 100 | 游戏时长无限制 | -
CALLBACK_CODE_LOGIN_SUCCESS | 500 | 游戏调用 login 后用户完成登录流程 | -
CALLBACK_CODE_LOGOUT | 1000 | 游戏调用 logout 登出账号 | -
CALLBACK_CODE_PAY_NO_LIMIT | 1020 | 付费不受限，sdk检查用户付费无限制时触发| -
CALLBACK_CODE_PAY_LIMIT | 1025 | 付费受限，付费受限触发,包括游客未实名或付费额达到限制等 | -
CALLBACK_CODE_TIME_LIMIT | 1030 | 时间受限，未成年人或游客游戏时长**接近或已达限制**，剩余时长请依据 remainTime 字段 | -
CALLBACK_CODE_OPEN_ALERT_TIP | 1095 | SDK 请求打开弹窗提示，具体内容解析 json 格式的 message | 
	
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

调用登录以后，会收到用户剩余游戏时长或者登录成功的回调，游戏可弹窗提醒用户，在游戏和用户做完所有登录时操作后（如提示弹窗关闭，用户协议关闭等），**调用以下接口开始防沉迷计时**。

```
AntiAddictionKit.enterGame()
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

