# 中宣部接口服务

## 环境准备 

- JDK 1.8+ 
- MYSQL （由Liquibase自动建表）
- REDIS

### 概述
本服务基于spring boot 开发，对接中宣部实名认证、实名查询和游戏时长上报接口。

### 参数说明

```
spring.redis.host=  // redis地址

spring.datasource.url= //数据库连接地址

spring.datasource.username= //数据库用户名

spring.datasource.password= //数据库密码

tds.publicity.appId=  //中宣部接口调用唯一凭证

tds.publicity.bizId=  //游戏备案识别码

tds.publicity.signKey= //中宣部密钥

tds.publicity.requestSignKey= //接口签名密钥

tds.publicity.idCardSecretKey= //身份证存入数据库时的密钥（身份证应该加密存储）

tds.push.jws= //JWT的密钥，应该和anti-addiction-service一致
```


### 打包和启动
按SpringBoot启动方式启动即可

### 签名算法:

1. 将业务参数，根据参数的 key 进行 字典排序，并按照 Key-Value 的格式拼接成一个字符串。将请求体中的参数 拼接在字符串最后。

2. 将 requestSignKey 拼接在步骤 1 获得字符串最前面，得到待加密字符串
`requestSignKey+query+body` （`e5d341b5aed6110da68f93e06aff47dbuser_id=sdafsdf`）。
使用 SHA256 算法对待加密字符串进行计算，放入header，key 为 `sign`

postman 签名代码示例：  

GET

```js
var sdk = require('postman-collection')
var user_id = pm.request.url.query.get('user_id')
var requestSignKey = "wr060pqmt93zd07zh2pl2er9hg5sn0xq"
var signBase = requestSignKey + 'user_id' + user_id
var sign =  CryptoJS.SHA256(signBase).toString(CryptoJS.enc.Hex);
pm.globals.set("sign",sign);
```

POST

```js
var sdk = require('postman-collection')
var body = pm.request.body.raw
var requestSignKey = "wr060pqmt93zd07zh2pl2er9hg5sn0xq"
var signBase = requestSignKey + body
var sign =  CryptoJS.SHA256(signBase).toString(CryptoJS.enc.Hex);
pm.globals.set("sign",sign);
```

## 接口说明

### 实名认证接口

```
/api/v1/identification
```

**HTTP请求方式**

```
POST application/json
```

**请求数据**

字段             | 类型           | 说明
--------------- | ------------- | ------------
user_id         | string        | 用户id
id_card         | string        | 身份证号码
name            | string        | 身份证姓名

**返回数据**

字段             | 类型           | 说明
--------------- | ------------- | ------------
code            | int           | 状态码
msg             | string        | 提示信息
data            | string        | identify_state: 0/1/2 (成功/认证中/失败)，anti_addiction_token（调用防沉迷服务的token）。

示例：

入参：

```json
{
    "user_id":"76ea77f9d4e6d236f8e31d0777106023",
    "id_card": "xxxxxxxxxxxxxxxxx",
    "name":"test"
}
```

出参：

```json
{  
    "code": 200,
    "data": {
        "identify_state": 2,
        "anti_addiction_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJiaXJ0aGRheSI6IiIsInVuaXF1ZV9pZCI6IiIsInVzZXJfaWQiOiJhYm56amRqZCJ9.OyoqCbhfFu12uAuFQVejlkFry9v7vqFFA3t4Szjsezs"
    }
}
```

#### 实名查询接口

```
/api/v1/identification/info
```

**HTTP请求方式**

GET

**请求数据**

字段             | 类型           | 说明
--------------- | ------------- | ------------
user_id         | string        | 用户id

**返回数据**

字段             | 类型           | 说明
--------------- | ------------- | ------------
code            | int           | 状态码
data            | string        | identify_state: 0/1/2 (成功/认证中/失败)，user_id（用户id），id_card（身份证），name（姓名），anti_addiction_token（调用防沉迷服务的token）。

示例：

入参：

```
user_id=76ea77f9d4e6d236f8e31d0777106023
```

出参：

```json
{
    "code": 200,
    "msg": "",
    "data": {
       "identify_state" : 0/1/2,
       "user_id": "76ea77f9d4e6d236f8e31d0777106023",
       "id_card": "210203xxxxxxxx18",
       "name" : "test",
       "anti_addiction_token" : "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJiaXJ0aGRheSI6IjE5OTAtMDUtMDkiLCJ1bmlxdWVLZXkiOiIxNjQyZDlhMDBhMWM2YjA2NjY0ZjUzYjEwMjk0MTA5ZiIsInVzZXJJZCI6Ijc2ZWE3N2Y5ZDRlNmQyMzZmOGUzMWQwNzc3MTA2MDIzIn0.v8mgbrbx_hE-I5G4p0cA8_FiFdww0OzCVEL1EWHKzUY"
    }
}
```

#### WebSocket接口（行为上报）：

```
/ws/v1?Authorization=xxx
```

**请求数据**

字段             | 类型           | 说明
--------------- | ------------- | ------------
Authorization   | string        | anti_addiction_token

### 打包部署

#### 打包

```sh
cd tds-registration-server/src/main/resources
# 复制配置文件后，修改数据库、中宣部参数、加密密钥等参数
cp application-example.yml application-prod.yml
mvn clean package -Pprod
mkdir build # 初次执行，本地有了就不用执行
mv target/*.jar ./build/app.jar
```

生成的包在 `build` 目录下，把生成的包上传到服务器，执行下面的命令启动。

多环境配置：  

- `application-prod.yml` 生产环境 
- `application-test.yml` 测试环境
- `application-dev.yml`  开发环境，打包时不加-P，默认为开发环境。

#### Docker部署（推荐）

```sh
docker build -t tds-registration-server .
docker run -p 80:80 tds-registration-server
```

#### 直接部署

```sh
cd build
java -jar tds-registration-server-1.0.jar & # 仅为示例，可自行选择其他工具部署。
```

