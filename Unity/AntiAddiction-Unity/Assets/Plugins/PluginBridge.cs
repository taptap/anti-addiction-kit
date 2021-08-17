using System;
using UnityEngine;

namespace Plugins.AntiAddictionKit
{
    [Serializable]
    public class CheckPayResult
    {
        public int status;
        public string title;
        public string description;
    }

    [Serializable]
    public class AntiAddictionCallbackOriginData
    {
        public int code;
        public string extras;
    }

    [Serializable]
    public class AntiAddictionCallbackData
    {
        public int code;
        public MsgExtraParams extras;
    }

    [Serializable]
    public class IdentificationInfo
    {
        public int authState;

        public string idCard;

        public string name;

        public string phoneNumber;

        public string antiAddictionToken;
    }

    [Serializable]
    public class IdentifyResult
    {
        public int identifyState;
    }

    [Serializable]
    public class MsgExtraParams
    {
        public string limit_tip_type;
        public string strict_type;
        public string description;
        public string title;
        public string remaining_time_str;
    }

    public static class AntiAddictionKit
    {
        // Game object is created to receive async messages
        private const string GAME_OBJECT_NAME = "PluginBridge";
        private static GameObject gameObject;

        // Android only variables
        private const string JAVA_OBJECT_NAME = "com.tapsdk.antiaddiction.NativeAntiAddictionKitPlugin";
        private static AndroidJavaObject androidJavaNativeAntiAddiction;

        // iOS only variables
#if UNITY_IOS
        [DllImport("__Internal")]
#endif
        private static extern string initSDK(string gameIdentifier, bool useTimeLimit, bool usePaymentLimit);
#if UNITY_IOS
        [DllImport("__Internal")]
#endif
        private static extern void login(string userId);

        private static Action<AntiAddictionCallbackData> handleAsyncAntiAddictionMsg;
        private static Action<string> handleAsyncAntiAddictionMsgException;

        private static Action<IdentificationInfo> handleAsyncFetchIdentificationInfo;
        private static Action<string> handleAsyncFetchIdentificationInfoException;

        private static Action<IdentifyResult> handleAsyncAuthIdentity;
        private static Action<string> handleAsyncAuthIdentityException;

        private static Action<CheckPayResult> handleCheckPayLimit;

        private static Action<string> handleCheckPayLimitException;

        private static Action handleSubmitPayResult;

        private static Action<string> handleSubmitPayResultException;


        private class PlatformNotSupportedException : Exception
        {
            public PlatformNotSupportedException() : base()
            {

            }
        }

        static AntiAddictionKit()
        {
            gameObject = new GameObject();
            gameObject.name = GAME_OBJECT_NAME;

            // attach this class to allow for handling of callbacks from Java or Objective c
            gameObject.AddComponent<NativeAntiAddictionCallbackHandler>();
            gameObject.AddComponent<NativeFetchIdentificationInfoHandler>();
            gameObject.AddComponent<NativeAuthIdentityHandler>();
            gameObject.AddComponent<NativeCheckPayLimitHandler>();
            gameObject.AddComponent<NativeSubmitPayResultHandler>();

            // Do not destroy when loading a new scene
            UnityEngine.Object.DontDestroyOnLoad(gameObject);

            switch (Application.platform)
            {
                case RuntimePlatform.Android:

                    // Initialize native Java object
                    androidJavaNativeAntiAddiction = new AndroidJavaObject(JAVA_OBJECT_NAME);

                    break;

                case RuntimePlatform.IPhonePlayer:
                    break;

                default:
                    throw new PlatformNotSupportedException();

            }
        }

        private class NativeAntiAddictionCallbackHandler : MonoBehaviour
        {
            private void HandleException(string exception)
            {
                handleAsyncAntiAddictionMsgException?.Invoke(exception);
            }

            private void HandleAntiAddictionCallbackMsg(string antiAddictionCallbackDataJSON)
            {
                Debug.Log("antiAddictionCallbackDataJSON:" + antiAddictionCallbackDataJSON);
                var antiAddictionCallbackOriginData = JsonUtility.FromJson<AntiAddictionCallbackOriginData>(antiAddictionCallbackDataJSON);

                var result = new AntiAddictionCallbackData();
                result.code = antiAddictionCallbackOriginData.code;
                result.extras = JsonUtility.FromJson<MsgExtraParams>(antiAddictionCallbackOriginData.extras);

                handleAsyncAntiAddictionMsg?.Invoke(result);
            }
        }

        private class NativeFetchIdentificationInfoHandler : MonoBehaviour
        {
            private void HandleFetchIdentificationException(string exception)
            {
                handleAsyncFetchIdentificationInfoException?.Invoke(exception);
            }

            private void HandleFetchIdentificationInfo(string identifyResultJSON)
            {
                Debug.Log("identifycationInfoJSON:" + identifyResultJSON);
                var result = JsonUtility.FromJson<IdentificationInfo>(identifyResultJSON);
                handleAsyncFetchIdentificationInfo?.Invoke(result);
            }
        }

        private class NativeAuthIdentityHandler : MonoBehaviour
        {
            private void HandleAuthIdentityException(string exception)
            {
                //throw new Exception(exception);
                handleAsyncAuthIdentityException?.Invoke(exception);
            }

            private void HandleAuthIdentity(string identifyResultJSON)
            {
                Debug.Log("identifycationInfoJSON:" + identifyResultJSON);
                var result = JsonUtility.FromJson<IdentifyResult>(identifyResultJSON);
                handleAsyncAuthIdentity?.Invoke(result);
            }
        }

        private class NativeCheckPayLimitHandler : MonoBehaviour
        {
            private void HandleCheckPayLimitException(string exception)
            {
                handleCheckPayLimitException?.Invoke(exception);
            }

            private void HandleCheckPayLimit(string checkPayResultJSON)
            {
                var result = JsonUtility.FromJson<CheckPayResult>(checkPayResultJSON);
                handleCheckPayLimit?.Invoke(result);
            }
        }

        private class NativeSubmitPayResultHandler : MonoBehaviour
        {
            private void HandleSubmitPayResultException(string exception)
            {
                handleSubmitPayResultException?.Invoke(exception);
            }

            private void HandleSubmitPayResult()
            {
                handleSubmitPayResult?.Invoke();
            }
        }

        /*
         * ------------------
         * Interface Metthods
         * ------------------
         */
        public static void Init(string gameIdentifier, bool useTimeLimit, bool usePaymentLimit, Action<AntiAddictionCallbackData> handleAsyncAntiAddictionMsg
            , Action<string> handleAsyncAntiAddictionMsgException)
        {
            AntiAddictionKit.handleAsyncAntiAddictionMsg = handleAsyncAntiAddictionMsg;
            AntiAddictionKit.handleAsyncAntiAddictionMsgException = handleAsyncAntiAddictionMsgException;

            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    PerformAndroidInit(gameIdentifier, useTimeLimit, usePaymentLimit);
                    break;
                case RuntimePlatform.IPhonePlayer:
                    PerformIOSInit(gameIdentifier, useTimeLimit, usePaymentLimit);
                    break;
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        public static void Login(string userId)
        {
            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    Debug.Log("PerformSyncLogin calling");
                    PerformAndroidLogin(userId);
                    break;
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        public static void EnterGame()
        {
            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    PerformAndroidEnterGame();
                    break;
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        public static void LeaveGame()
        {
            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    PerformAndroidLeaveGame();
                    break;
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        public static string CurrentToken()
        {
            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    return PerformAndroidGetCurrentToken();
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        public static int CurrentUserType()
        {
            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    return PerformAndroidGetCurrentUserType();
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        public static int CurrentUserRemainTime()
        {
            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    return PerformAndroidGetCurrentUserRemainTime();
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        public static void Logout()
        {
            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    PerformAndroidLogout();
                    break;
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        public static void FetchIdentificationInfo(string userId, Action<IdentificationInfo> handleAsyncFetchIdentificationInfo
            , Action<string> handleAsyncFetchIdentificationInfoException)
        {
            AntiAddictionKit.handleAsyncFetchIdentificationInfo = handleAsyncFetchIdentificationInfo;
            AntiAddictionKit.handleAsyncFetchIdentificationInfoException = handleAsyncFetchIdentificationInfoException;

            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    PerformAndroidFetchIdentificationInfo(userId);
                    break;
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        public static void AuthIdentity(string userId, string name, string idCard
            , Action<IdentifyResult> handleAsyncAuthIdentity
            , Action<string> handleAsyncAuthIdentityException
            )
        {
            AntiAddictionKit.handleAsyncAuthIdentity = handleAsyncAuthIdentity;
            AntiAddictionKit.handleAsyncAuthIdentityException = handleAsyncAuthIdentityException;

            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    PerformAndroidAuthIdentity(userId, name, idCard);
                    break;
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        public static void CheckPayLimit(long amount
            , Action<CheckPayResult> handleCheckPayLimit
            , Action<string> handleCheckPayLimitException)
        {
            AntiAddictionKit.handleCheckPayLimit = handleCheckPayLimit;
            AntiAddictionKit.handleCheckPayLimitException = handleCheckPayLimitException;

            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    PerformAndroidCheckPayLimit(amount);
                    break;
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        public static void SubmitPayResult(long amount
            , Action handleSubmitPayResult
            , Action<string> handleSubmitPayResultException
            )
        {
            AntiAddictionKit.handleSubmitPayResult = handleSubmitPayResult;
            AntiAddictionKit.handleSubmitPayResultException = handleSubmitPayResultException;

            switch (Application.platform)
            {
                case RuntimePlatform.Android:
                    PerformAndroidSubmitPayResult(amount);
                    break;
                default:
                    throw new PlatformNotSupportedException();
            }
        }

        /*
         * ------------------
         * Internal Metthods(Android)
         * ------------------
         */
        private static void PerformAndroidInit(
            string gameIdentifier
            , bool useTimeLimit
            , bool usePaymentLimit)
        {
            Debug.Log("Android Init calling");
            AndroidJavaClass unityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            AndroidJavaObject unityActivity = unityClass.GetStatic<AndroidJavaObject>("currentActivity");
            androidJavaNativeAntiAddiction.Call("initSDK", unityActivity, gameIdentifier, useTimeLimit, usePaymentLimit);
        }

        private static void PerformAndroidLogin(string userId)
        {
            androidJavaNativeAntiAddiction.Call("login", userId);
        }

        private static void PerformAndroidEnterGame()
        {
            androidJavaNativeAntiAddiction.Call("enterGame");
        }

        private static void PerformAndroidLeaveGame()
        {
            androidJavaNativeAntiAddiction.Call("leaveGame");
        }

        private static string PerformAndroidGetCurrentToken()
        {
            return androidJavaNativeAntiAddiction.Call<string>("getCurrentToken");
        }

        private static int PerformAndroidGetCurrentUserType()
        {
            return androidJavaNativeAntiAddiction.Call<int>("getCurrentUserType");
        }

        private static int PerformAndroidGetCurrentUserRemainTime()
        {
            return androidJavaNativeAntiAddiction.Call<int>("getCurrentUserRemainTime");
        }

        private static void PerformAndroidLogout()
        {
            androidJavaNativeAntiAddiction.Call("logout");
        }

        private static void PerformAndroidFetchIdentificationInfo(string userId)
        {
            Debug.Log("Android fetchIdentificationInfo calling");
            androidJavaNativeAntiAddiction.Call("fetchIdentificationInfo", userId);
        }

        private static void PerformAndroidAuthIdentity(string userId, string name, string idCard)
        {
            Debug.Log("Android authIdentity calling");
            androidJavaNativeAntiAddiction.Call("authIdentity", userId, name, idCard);
        }

        private static void PerformAndroidCheckPayLimit(long amount)
        {
            Debug.Log("Android checkPayLimit calling");
            androidJavaNativeAntiAddiction.Call("checkPayLimit", amount);
        }

        private static void PerformAndroidSubmitPayResult(long amount)
        {
            Debug.Log("Android submitPayResult calling");
            androidJavaNativeAntiAddiction.Call("submitPayResult", amount);
        }

        /*
         * ------------------
         * Internal Methods(iOS)
         * ------------------
         */
        private static void PerformIOSInit(string gameIdentifier, bool useTimeLimit
            , bool usePaymentLimit)
        {
            initSDK(gameIdentifier, useTimeLimit, usePaymentLimit);
        }
    }
}