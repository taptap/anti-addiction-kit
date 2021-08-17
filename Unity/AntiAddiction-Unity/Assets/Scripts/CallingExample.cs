using System.Collections;
using System.Collections.Generic;
using System;
using Plugins.AntiAddictionKit;
using UnityEngine;

public class CallingExample : MonoBehaviour
{

    private string loginStatus = "";

    private string userId = "792";

    private string payAmount = "100";

    private string userName = "张凯迪";

    private string userIDCard = "642221199804231424";

    private string userType = "用户类型:-1";

    private string userRemainTime = "剩余时长:-1";

    private string antiAddictionToken = "";

    private string logText = "";

    private string strictPromotion = "";

    public float timer = 2.0f;

    // Start is called before the first frame update
    void Start()
    {
        try
        {
            Debug.Log("CallingExample - started");

            var gameIdentifier = "demo";
            var useTimeLimit = true;
            var usePaymentLimit = true;

            AntiAddictionKit.Init(gameIdentifier, useTimeLimit, usePaymentLimit
                , (antiAddictionCallbackData) =>
                {
                    Debug.Log($"AntiAddictionCallback: code = {antiAddictionCallbackData.code},extras = {antiAddictionCallbackData.extras}");

                    if (antiAddictionCallbackData.code == 500)
                    {
                        antiAddictionToken = AntiAddictionKit.CurrentToken();
                        logText = "已登录";
                        loginStatus = "用户状态:已登录";
                        userType = "用户类型:" + AntiAddictionKit.CurrentUserType().ToString();
                        userRemainTime = "剩余时长:" + AntiAddictionKit.CurrentUserRemainTime().ToString();
                        Debug.Log("AntiAddictionCallback userType:" + userType);
                        Debug.Log("AntiAddictionCallback userRemainTime:" + userRemainTime);
                    }
                    else if (antiAddictionCallbackData.code == 1095)
                    {
                        // 消息提示
                        logText = antiAddictionCallbackData.extras.description;
                    }
                    else if (antiAddictionCallbackData.code == 1030)
                    {
                        strictPromotion = "时长已耗尽";
                    }
                    else if (antiAddictionCallbackData.code == 1050)
                    {
                        strictPromotion = "当前为宵禁时间，注意休息";
                    }
                }, (exception) =>
                {
                    logText = "AntiAddiction Callback Exception:" + exception;
                });
        }
        catch (Exception exception)
        {
            Debug.LogError(exception);
        }
    }

    // Update is called once per frame
    void Update()
    {
        timer -= Time.deltaTime;
        if (timer <= 0)
        {
            updateRemainTime();
            timer = 2.0f;
        }

    }

    private void updateRemainTime()
    {
        userRemainTime = "剩余时长:" + AntiAddictionKit.CurrentUserRemainTime().ToString();
    }

    private void ResetUI()
    {
        logText = "";
        loginStatus = "用户状态:未登录";
        userType = "用户类型:" + AntiAddictionKit.CurrentUserType().ToString();
        userRemainTime = "剩余时长:" + AntiAddictionKit.CurrentUserRemainTime().ToString();
        logText = "未登录";
        strictPromotion = "";
    }

    void OnGUI()
    {

        GUIStyle myButtonStyle = new GUIStyle(GUI.skin.button)
        {
            fontSize = 30
        };

        GUIStyle myLabelStyle = new GUIStyle(GUI.skin.label)
        {
            fontSize = 30
        };

        string antiAddictionToken = AntiAddictionKit.CurrentToken();

        int standardButtonWidth = 200;
        int standardButtonHeight = 80;

        int standardLabelWidth = 200;
        int standardLabelHeight = 80;

        int standardVerticalGap = 40;
        int standardHorizontalGap = 50;

        if (antiAddictionToken.Length == 0)
        {
            loginStatus = "用户状态:未登录";
        }
        else
        {
            loginStatus = "用户状态:已登录";
        }

        int xOffset = 50;
        int yOffset = 50;


        if (GUI.Button(new Rect(xOffset, yOffset, standardButtonWidth, standardButtonHeight), "登录", myButtonStyle))
        {
            // user has not logged in 
            if (antiAddictionToken.Length == 0)
            {
                AntiAddictionKit.Login(userId);
                logText = "登录中";
            }
        }
        yOffset += (standardLabelHeight + standardVerticalGap);

        if (GUI.Button(new Rect(xOffset, yOffset, standardButtonWidth, standardButtonHeight), "登出", myButtonStyle))
        {
            AntiAddictionKit.Logout();
            ResetUI();
            logText = "已登出";
        }
        yOffset += (standardLabelHeight + standardVerticalGap);

        if (GUI.Button(new Rect(xOffset, yOffset, standardButtonWidth, standardButtonHeight), "开始计时", myButtonStyle))
        {
            AntiAddictionKit.EnterGame();
        }
        yOffset += (standardLabelHeight + standardVerticalGap);

        if (GUI.Button(new Rect(xOffset, yOffset, standardButtonWidth, standardLabelHeight), "停止计时", myButtonStyle))
        {
            AntiAddictionKit.LeaveGame();
        }

        xOffset += (standardHorizontalGap + standardButtonWidth);
        yOffset = 50;

        GUI.Label(new Rect(xOffset, yOffset, standardLabelWidth, standardLabelHeight)
            , loginStatus, myLabelStyle);
        yOffset += (standardLabelHeight + standardVerticalGap);

        userId = GUI.TextArea(new Rect(xOffset, yOffset, standardLabelWidth, standardLabelHeight)
        , userId, myButtonStyle);
        yOffset += (standardLabelHeight + standardVerticalGap);

        GUI.Label(new Rect(xOffset, yOffset, standardLabelWidth, standardLabelHeight)
            , userType, myLabelStyle);
        yOffset += (standardLabelHeight + standardVerticalGap);

        GUI.Label(new Rect(xOffset, yOffset, standardLabelWidth, standardLabelHeight)
            , userRemainTime, myLabelStyle);

        xOffset += (standardHorizontalGap + standardButtonWidth);
        yOffset = 50;

        userName = GUI.TextArea(new Rect(xOffset, yOffset, standardLabelWidth, standardLabelHeight)
        , userName, myButtonStyle);
        yOffset += (standardLabelHeight + standardVerticalGap);

        userIDCard = GUI.TextArea(new Rect(xOffset, yOffset, standardLabelWidth, standardLabelHeight)
        , userIDCard, myButtonStyle);
        yOffset += (standardLabelHeight + standardVerticalGap);

        if (GUI.Button(new Rect(xOffset, yOffset, standardButtonWidth, standardLabelHeight), "实名", myButtonStyle))
        {
            AntiAddictionKit.AuthIdentity(userId, userName, userIDCard, (identifyResult) =>
            {
                Debug.Log("AuthIdentity callback");
                logText = "实名结果[identifyState:" + identifyResult.identifyState + "]";
            }, (exception) =>
            {
                logText = "AuthIdentity Exception:" + exception;
            });
        }
        yOffset += (standardLabelHeight + standardVerticalGap);
        if (GUI.Button(new Rect(xOffset, yOffset, standardButtonWidth, standardLabelHeight), "实名信息", myButtonStyle))
        {
            AntiAddictionKit.FetchIdentificationInfo(userId, (identificationInfo) =>
            {
                Debug.Log("FetchIdentificationInfo callback");
                logText = "authState:" + identificationInfo.authState + ", name:" + identificationInfo.name + ", idCard:" + identificationInfo.idCard + ", antiaddiction_token:" + identificationInfo.antiAddictionToken;
            }, (exception) =>
            {
                logText = "FetchIdentificationInfo Exception:" + exception;
            });
        }

        xOffset += (standardHorizontalGap + standardButtonWidth);
        yOffset = 50;

        payAmount = GUI.TextArea(new Rect(xOffset, yOffset, standardLabelWidth, standardLabelHeight)
            , payAmount, myButtonStyle);
        yOffset += (standardLabelHeight + standardVerticalGap);

        if (GUI.Button(new Rect(xOffset, yOffset, standardButtonWidth, standardButtonHeight), "检查支付", myButtonStyle))
        {
            var amount = long.Parse(payAmount);
            AntiAddictionKit.CheckPayLimit(amount, (checkPayResult) =>
            {
                logText = "status:" + checkPayResult.status + ", title:" + checkPayResult.title + ", description:" + checkPayResult.description;
            },
            (exception) =>
            {
                logText = "CheckPayLimit Exception:" + exception;
            });
        }
        yOffset += (standardLabelHeight + standardVerticalGap);

        if (GUI.Button(new Rect(xOffset, yOffset, standardButtonWidth, standardButtonHeight), "上报支付结果", myButtonStyle))
        {
            var amount = long.Parse(payAmount);
            AntiAddictionKit.SubmitPayResult(amount, () =>
            {
                logText = "SubmitPayResult success";
            }, (exception) =>
            {
                logText = "SubmitPayResult Exception:" + exception;
            });
        }

        // 提示消息框
        GUI.Label(new Rect(550, 600, 500, 300), logText, myLabelStyle);

        // 提示已经宵禁或者时长耗尽
        GUI.Label(new Rect(1150, 600, 500, 300), strictPromotion, myLabelStyle);

    }
}
