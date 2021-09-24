# 如何部署到 TDS 的云引擎

TDS 云引擎是一个开放的容器云计算平台，它既可以被简单地用来托管静态网站，又可以接受任意程序语言的定制开发来动态处理外来请求，满足业务定制化需求。云引擎除了可以支持程序部署之外，还提供 Redis、MySQL、MongoDB、Elasticsearch 等常见开源数据库支持，供开发者按需购买使用，而且还提供有免费 HTTPS 证书，让开发者搭建 Web Server 更加便捷灵活。具体介绍可以参考文档：[云引擎功能介绍](https://developer.taptap.com/docs/sdk/engine/features/)。

本文档演示了如何利用 TDS 云引擎来部署防沉迷 Server，让我们不租用服务器也可以给游戏加上防沉迷功能。

## 1，服务端架构说明

TDS 开源的防沉迷解决方案，其服务端包含两部分：

- 实名认证 Server。它负责完成将实名信息上报到中宣部接口，代码在 Server/tds-registration-server 目录下。
- 防沉迷 Server。它负责对玩家的游戏时间与充值付费进行控制。

实名认证 Server 在运行时依赖 MySQL 数据库和 Redis，防沉迷 Server 也依赖 MySQL 数据库。

云引擎标准实例、MySQL 实例、Redis 实例均需付费购买，所以请提前充值，以免次日扣费后欠费停服。

## 2，部署准备
### 2.1 创建开发者账号和游戏
可参考文档：

- [开发者注册](https://developer.taptap.com/docs/store/store-register)
- [创建游戏](https://developer.taptap.com/docs/store/store-creategame/)

### 2.2 下载 TDS 云引擎命令行工具
可参考文档：[云引擎命令行工具使用指南](https://developer.taptap.com/docs/sdk/engine/guide/cli/)。

安装完成后在终端中输入 `tds --version` 命令，会看到：

```
$tds --version
tds version 0.25.0
```
这就说明云引擎命令行工具安装完成。

### 2.3 下载防沉迷服务端代码
使用 `git clone git@github.com:taptap/anti-addiction-kit.git` 将 TDS 开源防沉迷方案拷贝到本地，后续我们假设所有的操作都在当前代码目录下。

代码下载完成之后，我们可以在当前目录下，按照[文档的提示](https://developer.taptap.com/docs/sdk/engine/guide/cli/#%E7%99%BB%E5%BD%95)，完成命令行工具的登录环节。

### 2.4 购买、配置 MySQL 实例
由于防沉迷服务端需要 MySQL 数据库，所以我们先在云引擎中配置好实例。

方法如下：登录 TapTap 开发者中心，选中目标游戏，进入游戏服务 - 云服务 - 云引擎菜单，拖动右侧的滚动条，选择「MySQL」标签页，然后点击下面的「创建实例」按钮。
比如，创建一个名为 `antiMysql` 的实例。

我们按照自己的需求选择内存规格和存储空间大小之后，点击「确认并开始收费」，然后等待 MySQL 实例启动即可。

#### 创建数据库和用户
在数据库实例运行起来之后，我们可以在「MySQL」标签页下面看到实例状态为「运行中」，并且可以点击下面的「管理员面板」以 root 用户身份连接数据库来进行设置。

在管理员面板的右侧中，从最上面我们可以看到数据库的访问地址，类似于
`MySQL >> engine-stateful-local-proxy:27015` 这样的字符串，在下面还有「Create database」「Privileges」这样的操作链接：

- 点击「Create database」来创建数据库，假设命名为`tds`；

- 点击「Privileges - Create user」来创建用户，并授予它对 tds 数据库完全的访问权限。例如：

    - **Username** 填写 `tds`（这里假定我们创建一个名为 `tds` 的用户）
    - **Password** （密码）填写 `tdspass`。
    - **Privileges** 后的文本框中填写 `tds.*`，接着勾选下方的「All privileges」，授予对 tds 数据库的访问权限。
    - 最后点击页面底部的 **Save** 按钮保存用户。 

请记住这些信息，后面还会用到：

- 数据库主机：`engine-stateful-local-proxy`
- 数据库端口：`27015`
- 数据库：`tds`
- 登录用户名：`tds`
- 登录用户密码：`tdspass`

### 2.5 购买、配置 Redis 实例
由于防沉迷服务端需要 Redis Server，所以我们先在云引擎中配置好实例。

方法如下：登录 TapTap 开发者中心，选中目标游戏，进入游戏服务 - 云服务 - 云引擎菜单，选择「LeanCache（Redis）」标签页，然后点击下面的「创建实例」按钮。
比如，创建一个名为 `antiRedis` 的实例，删除策略选择默认的 `volatile-lru`，实例规格按照自己的需求选择，点击「确认并开始收费」，然后等待 Redis 实例启动即可。


### 2.6 在云引擎分组管理中增加实名认证和防沉迷分组
接下来我们要为实名认证 Server 和防沉迷 Server 分别创建一个云引擎分组。

方法如下：登录 TapTap 开发者中心，选中目标游戏，进入游戏服务 - 云服务 - 云引擎菜单，选择「组管理」标签页，然后点击下面的「创建分组」按钮。

这里假设我们创建的两个分组名为 `publicity_reporter` 和 `anti_addiction`。记住这两个名字，后面还会用到。

### 2.7 获取 Redis 的访问信息
在 2.5 步骤中，我们可以看到创建的 Redis 实例，下面有一行提示信息：`访问地址: REDIS_URL_antiRedis`

这里的 `REDIS_URL_antiRedis` 是一个环境变量，在程序中需要先获取到具体的值，然后在代码中进行动态设置。由于实名认证 Server 在实现上采用了 SpringBoot 框架，采用依赖注入的方式构造的 Java 对象，我们不好动态配置，所以我们需要用一种迂回的方式来获取 Redis 访问地址：

- 在 Chrome 浏览器中打开 TapTap 开发者中心，选中目标游戏，进入游戏服务 - 云服务 - 云引擎菜单，选中「LeanCache（Redis）」标签页。
- 点击右键菜单，打开「检查」窗口，切换到 Network 标签页。
- 刷新当前页面，从网络请求中寻找 `clusters` 的返回结果，从中找到 Redis 的访问信息，其 json 格式如下：

```
{
    "id": 88,
    "appId": "SJjoXHWujw",
    "name": "antiRedis",
    "runtime": "redis",
    "nodeQuota": "redis-128",
    "storageQuota": "redis-128",
    "dataNodes": 2,
    "status": "running",
    "proxyPort": 27014,
    "authUser": "",
    "authPassword": "BUDMFF0cZbFD",
    "createdAt": "2021-09-05T05:20:00.000Z",
    "updatedAt": "2021-09-05T05:20:23.000Z",
    "version": 18,
    "versionTag": "6.2.5",
    "proxyHost": "engine-stateful-local-proxy",
    "maxMemoryPolicy": "volatile-lru"
}
```
其中 `proxyHost`、`proxyPort`、`authPassword` 就是我们需要的访问信息（登录用户名会被固定为`default`，这里没有显示出来）。

当然，开发者也可以通过程序来获取环境变量的值，具体做法可以参考[文档](https://developer.taptap.com/docs/sdk/engine/guide/redis/#%E5%9C%A8%E4%BA%91%E5%BC%95%E6%93%8E%E4%B8%AD%E4%BD%BF%E7%94%A8%EF%BC%88nodejs-%E7%8E%AF%E5%A2%83%EF%BC%89)。

假设我们创建的 Redis 实例访问信息如下：

- Redis 主机：`engine-stateful-local-proxy`
- Redis 端口：`27014`
- 登录用户名：`default`
- 登录用户密码：`BUDMFF0cZbFD`


做好这些准备工作之后，我们就可以开始进行实际部署了。

## 3，中宣部实名认证服务部署说明
### 3.1 修改配置文件
将当前工作目录切换到 Server/tds-registration-server，将 src/main/resources 下的 application.engine.yml 改名为 application.yml，并对如下部分进行修改：

#### MySQL 配置信息。
确认 `spring.datasource` 中的 `url`、`username`、`password` 三项和前面的 MySQL 实例信息一致，特别注意替换 `url` 中的端口，例如：

- `spring.datasource.url`: `jdbc:mysql://engine-stateful-local-proxy:27015/tds?characterEncoding=utf-8&allowPublicKeyRetrieval=true&useSSL=false`
- `spring.datasource.username`: `tds`
- `spring.datasource.password`: `tdspass`

#### Redis 配置信息
使用前面的 Redis 实例信息，对 spring.redis 的信息进行如下修改：

- spring.redis.host: engine-stateful-local-proxy
- spring.redis.port: 27014
- spring.redis.username: default
- spring.redis.password: BUDMFF0cZbFD

#### 中宣部应用信息
使用中宣部登记信息，对 tds.publicity 的信息进行如下修改：

- tds.publicity.appId: {请使用中宣部系统发放的 appId 进行填写}
- tds.publicity.bizId: {与游戏备案识别码一致}
- tds.publicity.signKey: {接口调用签名，由中宣部系统生成}

#### JWS 配置
配置好 tds.push.jws 的值，注意这里的配置要和后面防沉迷服务的配置一致：

- tds.push.jws:F5AE49C8

### 3.2 修改云引擎实例分组的设置信息
登录 TapTap 开发者中心，选中目标游戏，进入游戏服务 - 云服务 - 云引擎菜单，进入 publicity_reporter 标签页，进入其下的「设置」标签页，绑定自己的域名，比如 `publicity.example.com` 和 `stg-publicity.example.com`（`stg-` 开头的域名会被绑定到预备环境）。

### 3.3 部署
在云引擎控制台 `publicity_reporter` 标签页下面，从上一步的「设置」切换到「资源」标签页，点击「升级」按钮，根据需要选择标准实例的规格。


然后再切换到「部署」标签页，选择「部署模式」为「部署项目」。
如果之前没有生成过 access token，需要先生成一个 access token。

将本地工作目录切换到 `Server/tds-registration-server`，然后根据控制台的提示通过命令行工具进行部署。

```sh
tds switch --group publicity_reporter XXX
tds deploy
```

### 确认正常工作
在本地浏览器输入你之前绑定到预备环境的自定义域名（比如 `https://stg-publicity.example.com`），如果页面显示

```json
{"code":200,"data":{"status":"running"}}
```

则说明部署成功。

部署成功后，运行 `tds publish` 可以发布到正式环境。
发布到正式环境后同样可以通过访问正式环境的自定义域名，比如 `https://publicity.example.com` 来验证发布成功。

## 4，防沉迷服务部署说明
### 4.1 修改配置文件
防沉迷服务主要通过环境变量进行配置，本地配置文件不需要修改。

### 4.2 修改云引擎实例分组的设置信息
登录 TapTap 开发者中心，选中目标游戏，进入游戏服务 - 云服务 - 云引擎菜单，进入 anti_addition 标签页，进入其下的「设置」标签页，
绑定自己的域名，比如 `anti-addiction.example.com` 和 `stg-anti-addiction.example.com`（`stg-` 开头的域名会被绑定到预备环境）。

然后，在自定义环境变量中，我们增加如下环境变量：

- DATASOURCE_HOST: engine-stateful-local-proxy
- DATASOURCE_DUANKOU: 27015
- DATASOURCE_USER: tds
- DATASOURCE_PW: tdspass
- DATASOURCE_SCHEMA: tds
- JWS: F5AE49C8

请用你自己的值替换上面环境变量的值，特别是 `DATASOURCE_DUANKOU`（MySQL 的端口） 和 `JWS`。
可以参考之前 `application.yml` 中的配置。

保存退出。

### 4.3 部署

同样，在云引擎控制台 `anti_addiction` 标签页下面，从上一步的「设置」切换到「资源」标签页，点击「升级」按钮，根据需要选择标准实例的规格。


然后再切换到「部署」标签页，选择「部署模式」为「部署项目」。
将本地工作目录切换到 Server/anti-addiction-server，然后根据控制台的提示通过命令行工具进行部署。

```sh
tds switch --group anti_addiction XXX
tds deploy
```

执行成功之后，如果输入 `tds info` 命令，会看到如下输出信息（注意用户、应用的名字和 id 并不会完全相同）：

```
$tds info
[INFO] Retrieving user info from region: cn-tds1
[INFO] Retrieving app info ...
[INFO] Current region:  cn-tds1 User: tds-client-118194293 (ask+tds118194293@leancloud.rocks)
[INFO] Current region: cn-tds1 App: 云引擎测试-SJjoXH (SJjoFEAFEHJg4Ojw)
[INFO] Current group: anti_addiction

```

初始化成功之后，就可以执行 `tds deploy` 命令进行部署了。


### 4.4 确认正常工作
在本地浏览器输入如下地址 `stg-anti-addiction-1.tds1.tdsapps.cn`，如果页面显示

```json
{"status": "running"}
```

则说明部署成功。

同样，部署成功后，运行 `tds publish` 可以发布到正式环境。
发布到正式环境后同样可以通过访问正式环境的自定义域名，比如 `https://anti-addiction.example.com` 来验证发布成功。


## 5，SDK 确认测试
我们可以将上面两个访问地址设置到 SDK 中，来测试完整流程能否跑通。具体的设置方法可以参考 SDK 说明文档。
