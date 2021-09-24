# fcm-server

## 新版防沉迷规则
| 用户类型 | 触发器 | 文案反馈（线上版）|
|  ----  | ----  | ----  |
| 未实名  | 登录、游戏中 |title：健康游戏提示<br>description：您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。<br>button 1：去实名<br>button 
| 已实名且为未成年 | 游戏时段，剩余十五分钟通知（星期五、六、七及法定节假日20：00-21：00） |您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，周五、周六、周日及法定节假日 20 点 -  21 点之外为健康保护时段。您今日游戏时间还剩余 15 分钟，请注意适当休息。
| 已实名且为未成年 | 游戏时段（星期五、六、七及法定节假日20：00-21：00） |(不需要弹窗，不限制，可正常游戏）
| 已实名且为未成年 | 非游戏时段|title：健康游戏提示<br>description：您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，周五、周六、周日及法定节假日 20 点 -  21 点之外为健康保护时段。当前时间段无法游玩，请合理安排时间。<br>button 1：退出游戏<br>button 2：切换账号
  
### 充值触发
| 用户类型 | 触发流程 | 触发器 | 文案反馈（线上版）|文案反馈（版署版）|
|  ----  | ----  | ----  | ----  |----  | 
|已实名：8 岁以下|充值付费|点击商品|title：健康消费提示<br>description：根据国家相关规定，当前您无法使用充值相关功能。<br>button 1：返回游戏|title：健康消费提示<br>description：当前账号未满 8 周岁，无法使用充值相关功能。<br>根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。<br>button 1：返回游戏
|已实名：8 - 16岁以下|充值付费|点击商品<br>商品金额 > 50 元|title：健康消费提示<br>description：根据国家相关规定，您本次付费金额超过规定上限，无法购买。请适度娱乐，理性消费。<br>button 1：返回游戏|title：健康消费提示<br>description：当前账号未满 16 周岁，本次单笔付费金额超过规定上限 50 元，无法购买。<br>根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。<br>button 1：返回游戏
|已实名：8 - 16岁以下|充值付费|点击商品<br>月累计金额+当前选择的充值项金额 > 200 元|title：健康消费提示<br>description：根据国家相关规定，您当月的剩余可用充值额度不足，无法购买此商品。请适度娱乐，理性消费。<br>button 1：返回游戏|title：健康消费提示<br>description：当前账号未满 16 周岁，购买此商品后，您当月交易的累计总额已达上限  200 元，无法购买。<br>根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。<br>button 1：返回游戏
|已实名：16 - 18岁以下|充值付费|点击商品<br>商品金额 > 100 元|title：健康消费提示<br>description：根据国家相关规定，您本次付费金额超过规定上限，无法购买。请适度娱乐，理性消费。<br>button 1：返回游戏|title：健康消费提示<br>description：当前账号未满 18 周岁，本次单笔付费金额超过规定上限 100 元，无法购买。<br>根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。<br>button 1：返回游戏
|已实名：16 - 18岁以下|充值付费|点击商品<br>月累计金额+当前选择的充值项金额 > 400 元|title：健康消费提示<br>description：根据国家相关规定，您当月的剩余可用充值额度不足，无法购买此商品。请适度娱乐，理性消费。<br>button 1：返回游戏|title：健康消费提示<br>description：当前账号未满 18 周岁，购买此商品后，您当月交易的累计总额已达上限  400 元，无法购买。<br>根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。<br>button 1：返回游戏

## API
### 1.0获取防沉迷配置（因20210901防沉迷新规，改防沉迷配置时长限制部分已无效）

请求路由

```
GET /v3/fcm/get_config?game=ro
```


出参

```json
// accout_type:0,实名类型 1：8岁以下 ，2：8-15岁，  3：16-17岁， 4：18+ 5：未实名
// type:1-宵禁剩余时间提示 2-时长剩余提示 3-单笔消费限制 4-月消费限制 5-已经处于宵禁提示 6-时长耗尽提示 7-非宵禁时段第一次登陆 8-非宵禁时段非第一次登陆,剩余时长大于0，9-非宵禁时段非第一次登陆,剩余时长小于等于0，10-宵禁时段，登陆成功 11-非第一次登录，且剩余时长＞0，且＜20min时 12-气泡剩余时长 13-气泡距离宵禁时间

{
    "code": 200,
    "msg": "",
    "data": {
        "child_protected_config": {
            "share_time_switch": 1,
            "use_time_switch": 1,
            "no_identify_time": 3600,
            "charge_amount_switch": 1,
            "child_common_time": 5400,
            "child_holiday_time": 10800,
            "night_strict_start": "21:00",
            "night_strict_end": "20:00",
            "night_strict_warn": 1200,
            "remain_time_warn": 1200,
            "upload_all_data": 1
        },
        "ui_config": {
            "pay_limit_words": [
                {
                    "single_title": "健康消费提示",
                    "single_description": "当前账号未满 8 周岁，无法使用充值相关功能。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",
                    "single_limit": 0,
                    "account_type": 1,
                    "month_title": "健康消费提示",
                    "month_description": "当前账号未满 8 周岁，无法使用充值相关功能。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",
                    "month_limit": 0
                },
                {
                    "single_title": "健康消费提示",
                    "single_description": "当前账号未满 16 周岁，本次单笔付费金额超过规定上限 50 元，无法购买。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",
                    "single_limit": 5000,
                    "account_type": 2,
                    "month_title": "健康消费提示",
                    "month_description": "当前账号未满 16 周岁，购买此商品后，您当月交易的累计总额已达上限  200 元，无法购买。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",
                    "month_limit": 20000
                },
                {
                    "single_title": "健康消费提示",
                    "single_description": "当前账号未满 18 周岁，本次单笔付费金额超过规定上限 100 元，无法购买。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",
                    "single_limit": 10000,
                    "account_type": 3,
                    "month_title": "健康消费提示",
                    "month_description": "当前账号未满 18 周岁，购买此商品后，您当月交易的累计总额已达上限  400 元，无法购买。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",
                    "month_limit": 40000
                }
            ],
            "health_reminder_words": [
                {
                    "account_type": 0,
                    "tips": [
                        {
                            "type": 1,
                            "title": "健康游戏提示",
                            "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。"
                        },
                        {
                            "type": 5,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 2,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 6,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 7,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 8,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 9,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 10,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 12,
                            "title": "健康游戏提示",
                            "description": "您的游戏体验时间还剩余# ${remaining} #分钟，登记实名信息后可深度体验。"
                        },
                        {
                            "type": 13,
                            "title": "健康游戏提示",
                            "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。"
                        }
                    ]
                },
                {
                    "account_type": 1,
                    "tips": [
                        {
                            "type": 1,
                            "title": "健康游戏提示",
                            "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。"
                        },
                        {
                            "type": 5,
                            "title": "健康游戏提示",
                            "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，周五、周六、周日及法定节假日 20 点 -  21 点之外为健康保护时段。当前时间段无法游玩，请合理安排时间。"
                        },
                        {
                            "type": 2,
                            "title": "健康游戏提示",
                            "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，未成年账号非法定节假日每日游戏时长 1.5 小时，法定节假日每日游戏时长为 3 小时，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间还剩余# ${remaining} #分钟，请注意适度游戏。"
                        },
                        {
                            "type": 6,
                            "title": "健康游戏提示",
                            "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，未成年账号非法定节假日每日游戏时长 1.5 小时，法定节假日每日游戏时长为 3 小时，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间已达# ${remaining} #分钟上限，无法再进行游戏。"
                        },
                        {
                            "type": 7,
                            "title": "健康游戏提示",
                            "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，未成年账号非法定节假日每日游戏时长 1.5 小时，法定节假日每日游戏时长为 3 小时，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。您当前享有# ${remaining} #分钟游戏时间，请注意适度游戏。"
                        },
                        {
                            "type": 8,
                            "title": "健康游戏提示",
                            "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，未成年账号非法定节假日每日游戏时长 1.5 小时，法定节假日每日游戏时长为 3 小时，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间还剩余# ${remaining} #分钟，请注意适度游戏。"
                        },
                        {
                            "type": 9,
                            "title": "健康游戏提示",
                            "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，未成年账号非法定节假日每日游戏时长 1.5 小时，法定节假日每日游戏时长为 3 小时，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间已达# ${remaining} #分钟上限，无法再进行游戏。"
                        },
                        {
                            "type": 10,
                            "title": "健康游戏提示",
                            "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，每日 22 点 - 次日 8 点为健康保护时段，当前不可进入游戏。"
                        },
                        {
                            "type": 11,
                            "title": "健康游戏提示",
                            "description": "您今日游戏时间还剩余# ${remaining} #分钟，请注意适度游戏。"
                        },
                        {
                            "type": 12,
                            "title": "健康游戏提示",
                            "description": "您今日游戏时间还剩余# ${remaining} #分钟，请注意适度游戏。"
                        },
                        {
                            "type": 13,
                            "title": "健康游戏提示",
                            "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。"
                        }
                    ]
                },
                {
                    "account_type": 5,
                    "tips": [
                        {
                            "type": 1,
                            "title": "健康游戏提示",
                            "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。"
                        },
                        {
                            "type": 5,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 2,
                            "title": "健康游戏提示",
                            "description": "您当前未提交实名信息，已被纳入防沉迷系统。您享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间还剩余# ${remaining} #分钟。"
                        },
                        {
                            "type": 6,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 7,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 8,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 9,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 10,
                            "title": "健康游戏提示",
                            "description": "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
                        },
                        {
                            "type": 12,
                            "title": "健康游戏提示",
                            "description": "您的游戏体验时间还剩余# ${remaining} #分钟，登记实名信息后可深度体验。"
                        },
                        {
                            "type": 13,
                            "title": "健康游戏提示",
                            "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。"
                        }
                    ]
                }
            ]
        },
        "holiday": [
            "01.01",
            "02.12",
            "02.13",
            "02.14",
            "04.04",
            "05.01",
            "06.14",
            "09.21",
            "10.01",
            "10.02",
            "10.03"
        ]
    }
}
```

### 1.1授权

解密客户端传过来的 token，保存用户到数据库，生成一个包含防沉迷系统用户 id 的 wt token 返回给客户端。

请求路由

```
请求方式：POST application/json

/v3/fcm/authorizations
```

|参数|类型|含义|是否必须|
|  ----  | ----  | ----  | ----  |
|game|string|游戏名称|Y|
|token|string|包含身份信息的token|Y|


入参

```json
{"game":"ro","token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJiaXJ0aGRheSI6IjE5OTAtMDUtMDkiLCJ1bmlxdWVfaWQiOiIxNjQyZDlhMDBhMWM2YjA2NjY0ZjUzYjEwMjk0MTA5ZiIsInVzZXJfaWQiOiI3NmVhNzdmOWQ0ZTZkMjM2ZjhlMzFkMDc3NzEwNjAyMyJ9.EFxHmGiSTAn3ei1tCvaXw4Nfp51s_X0-NVRYX1lBtGw"}
```

出参

```json
{
    "code": 200,
    "msg": "授权成功",
    "data": {
        "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiNzZlYTc3ZjlkNGU2ZDIzNmY4ZTMxZDA3NzcxMDYwMjMiLCJiaXJ0aGRheSI6IjE5OTAtMDUtMDkiLCJhY2NvdW50VHlwZSI6NCwiaWF0IjoxNjIzOTk5NzY2fQ.SMBqG90kS5H52KsMVgENw8K6eyKAE_Wtyrdns2oOJ78",
        "user_id": "76ea77f9d4e6d236f8e31d0777106023",
        "type": 4
    }
}
```

### 1.2上传时长

请求路由

```
请求方式：POST application/json

/v3/fcm/set_play_log
```

|参数|类型|含义|是否必须|
|  ----  | ----  | ----  | ----  |
|game|string|游戏名称|Y|
|is_login|int|1:登录时调用  0：游戏中调用|Y|
|play_logs|json|时长json|Y|


HTTP Header  鉴权:

```
Authorization: Bearer <access_token>
```

入参

```json
{"play_logs":{"local_times":[],"server_times":[[1623749555,1623749655]]},"is_login":1,"game":"ro"}
```

出参

```json
{
    "code": 200,
    "msg": "上传时间成功",
    "data": {
        "remainTime": 300,//   防沉迷剩余时间，单位秒
        "costTime": 3300,//  今日游戏已玩时长，单位秒
        "restrictType": 1,//0-不限制1-宵禁 2-未实名
        "title": "健康游戏提示",
        "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，周五、周六、周日及法定节假日 20 点 -  21 点之外为健康保护时段。您今日游戏时间还剩余 6 分钟，请注意适当休息。"
    }
}
```

### 1.3充值检查

请求路由

```
请求方式：POST-application/json

/v3/fcm/check_pay
```

|参数|类型|含义|是否必须|
|  ----  | ----  | ----  | ----  |
|game|string|游戏名称|Y|
|amount|int|单位（分）|Y|

HTTP Header  鉴权

```
Authorization: Bearer <access_token>
```

入参

```json
{
    "game":"demo",
    "amount":4200
}
```

出参

```json
{
    "code": 200,
    "msg": "限额提示",
    "data": {"status":true}
}
```

或

```json
{
    "code": 9999,
    "msg": "限制消费",
    "data": {
        "title": "健康消费提醒",
        "description": "根据国家相关规定，您本次付费金额超过规定上限，无法购买。请适度娱乐，理性消费"
    }
}
```

### 1.4游戏服务端上报充值金额

请求路由

```
请求方式：POST-application/json

/v3/fcm/submit_pay
```

|参数|类型|含义|是否必须|
|  ----  | ----  | ----  | ----  |
|accessToken|string|附带充值记录的token|Y|

HTTP Header  鉴权

```
Authorization: Bearer <access_token>
```

入参

```json
{
    "game":"demo",
    "amount":4200
}
```

出参

```json
{
    "code": 200,
    "msg": "上传金额成功",
    "data": {}
}
```

## 部署

### 环境准备

- Node v10.x and later
- MySQL

### 生成环境变量和数据表

```bash
#配置数据库及jwt密钥
$ echo 'export DATASOURCE_HOST="127.0.0.1"' >> ~/.bash_profile
$ echo 'export DATASOURCE_PORT="3306"' >> ~/.bash_profile
$ echo 'export DATASOURCE_USER="root"' >> ~/.bash_profile
$ echo 'export DATASOURCE_PW="root"' >> ~/.bash_profile
$ echo 'export DATASOURCE_SCHEMA="fcm"' >> ~/.bash_profile
$ echo 'export JWS="!OKM9ijn"' >> ~/.bash_profile
$ source ~/.bash_profile
#进入主目录
$ cd anti-addiction-kit
#自动创建数据表
$ npx sequelize db:migrate --env "production"
```

### Docker
```
$ cd Server/anti-addiction-server/
$ docker build -t anti-addiction-server .
#输出环境变量到env.list
$ env > env.list
#启动镜像
$ docker run -p 7001:7001 --env-file env.list anti-addiction-server
```

### 命令行
```
$ cd Server/anti-addiction-server/
$ npm i
#启动项目
$ npm run start   
#关闭项目
$ npm run stop
```

### 业务配置

用途：配置每年的法定节假日（法定节假日180分钟，工作日90分钟）

操作示例：插入2022年法定节假日配置，sql如下

```sql
INSERT INTO `fcm_game_holiday_json` VALUES (2, 'common', 2022, '{\"202201\":{\"01\":\"2\"},\"202202\":{\"01\":\"2\",\"02\":\"02\",\"03\":\"2\",\"04\":\"2\",\"05\":\"2\",\"06\":\"2\",\"07\":\"2\"},\"202204\":{\"05\":\"2\"},\"202105\":{\"01\":\"2\"},\"202206\":{\"03\":\"2\"},\"202209\":{\"10\":\"2\"},\"202110\":{\"01\":\"2\",\"02\":\"2\",\"03\":\"2\",\"04\":\"2\",\"05\":\"2\",\"06\":\"2\",\"07\":\"2\"}}');
```
