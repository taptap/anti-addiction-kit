package com.tapsdk.antiaddiction.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tapsdk.antiaddiction.AntiAddictionCallback;
import com.tapsdk.antiaddiction.AntiAddictionKit;
import com.tapsdk.antiaddiction.Callback;
import com.tapsdk.antiaddiction.annotation.IntDef;
import com.tapsdk.antiaddiction.config.AntiAddictionFunctionConfig;
import com.tapsdk.antiaddiction.constants.Constants;
import com.tapsdk.antiaddiction.demo.models.ChangePayAmountAction;
import com.tapsdk.antiaddiction.demo.models.CheckPayAction;
import com.tapsdk.antiaddiction.demo.models.EnterGameAction;
import com.tapsdk.antiaddiction.demo.models.FuncAction;
import com.tapsdk.antiaddiction.demo.models.FuncBase;
import com.tapsdk.antiaddiction.demo.models.FuncCategroyInfo;
import com.tapsdk.antiaddiction.demo.models.FuncItemInfo;
import com.tapsdk.antiaddiction.demo.models.GetIdentifyInfoAction;
import com.tapsdk.antiaddiction.demo.models.IdentificationInfo;
import com.tapsdk.antiaddiction.demo.models.IdentifyAction;
import com.tapsdk.antiaddiction.demo.models.LeaveGameAction;
import com.tapsdk.antiaddiction.demo.models.LoginAntiAddictionAction;
import com.tapsdk.antiaddiction.demo.models.LogoutAction;
import com.tapsdk.antiaddiction.demo.models.PayAction;
import com.tapsdk.antiaddiction.demo.models.SetUserIdAction;
import com.tapsdk.antiaddiction.demo.widget.AbstractAlertDialog;
import com.tapsdk.antiaddiction.demo.widget.DashboardView;
import com.tapsdk.antiaddiction.demo.widget.IdentifyDialog;
import com.tapsdk.antiaddiction.demo.widget.ModifyAttrsDialog;
import com.tapsdk.antiaddiction.entities.response.CheckPayResult;
import com.tapsdk.antiaddiction.entities.response.IdentifyResult;
import com.tapsdk.antiaddiction.entities.response.SubmitPayResult;
import com.tapsdk.antiaddiction.enums.AccountLimitTipEnum;
import com.tapsdk.antiaddiction.models.AntiAddictionLimitInfoAction;
import com.tapsdk.antiaddiction.models.UpdateAccountAction;
import com.tapsdk.antiaddiction.models.UpdateAntiAddictionInfoAction;
import com.tapsdk.antiaddiction.reactor.RxBus;
import com.tapsdk.antiaddiction.reactor.functions.Action1;
import com.tapsdk.antiaddiction.reactor.rxandroid.schedulers.AndroidSchedulers;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public static final String DEFAULT_USER_ID = "791";

    public static final long DEFAULT_PAY_AMOUNT = 100L;

    private String currentUserId = DEFAULT_USER_ID;

    private long payAmount = DEFAULT_PAY_AMOUNT;

    List<FuncBase> unLoginSupportFuncList;
    List<FuncBase> loginSupportFuncList;
    List<FuncBase> funcBaseList = new ArrayList<>();
    FuncItemAdapter funcItemAdapter;

    private DashboardView dashboardView;

    private final Gson gson = new GsonBuilder().create();

    private void initFuncItems() {
        unLoginSupportFuncList = Arrays.asList(
                new FuncCategroyInfo("设置")
                , new FuncItemInfo("设置用户ID", new SetUserIdAction())
                , new FuncItemInfo("查询当前用户实名情况", new GetIdentifyInfoAction())
                , new FuncItemInfo("对当前用户实名认证", new IdentifyAction())
                , new FuncCategroyInfo("防沉迷功能")
                , new FuncItemInfo("登录防沉迷用户", new LoginAntiAddictionAction())
        );

        loginSupportFuncList = Arrays.asList(
                new FuncCategroyInfo("防沉迷功能")
                , new FuncItemInfo("开始游戏", new EnterGameAction())
                , new FuncItemInfo("离开游戏", new LeaveGameAction())
                , new FuncItemInfo("修改支付金额", new ChangePayAmountAction())
                , new FuncItemInfo("检查付费", new CheckPayAction())
                , new FuncItemInfo("上报支付金额", new PayAction())
                , new FuncItemInfo("登出", new LogoutAction())
        );

        RecyclerView functionsRecyclerView = findViewById(R.id.functionsRecyclerView);
        funcBaseList.addAll(unLoginSupportFuncList);
        funcItemAdapter = new FuncItemAdapter(funcBaseList);
        functionsRecyclerView.setAdapter(funcItemAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        functionsRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                layoutManager.getOrientation());
        functionsRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void listenToFunctionListEvent() {
        funcItemAdapter.funcActionPublishSubject
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FuncAction>() {
                    @Override
                    public void call(FuncAction funcAction) {
                        if (funcAction instanceof LoginAntiAddictionAction) {
                            AntiAddictionKit.login(currentUserId);
                        } else if (funcAction instanceof LogoutAction) {
                            AntiAddictionKit.logout();
                        } else if (funcAction instanceof SetUserIdAction) {
                            ModifyAttrsDialog.newInstance("设置用户ID"
                                    , currentUserId
                                    , "取消"
                                    , "确定"
                                    , new AbstractAlertDialog.AlertClickCallback() {
                                        @Override
                                        public void onLeftClick(String extra) {

                                        }

                                        @Override
                                        public void onRightClick(String extra) {
                                            if (!TextUtils.isEmpty(extra)) currentUserId = extra;
                                            Toast.makeText(MainActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            ).show(MainActivity.this.getFragmentManager(), ModifyAttrsDialog.TAG);
                        } else if (funcAction instanceof IdentifyAction) {
                            IdentifyDialog.newInstance("身份验证"
                                    , currentUserId
                                    , "取消"
                                    , "确定"
                                    , new AbstractAlertDialog.AlertClickCallback() {
                                        @Override
                                        public void onLeftClick(String extra) {

                                        }

                                        @Override
                                        public void onRightClick(String extra) {
                                            if (!TextUtils.isEmpty(extra)) {
                                                IdentificationInfo identificationInfo = gson.fromJson(extra, IdentificationInfo.class);
                                                if (!TextUtils.isEmpty(identificationInfo.userName)
                                                        && !TextUtils.isEmpty(identificationInfo.idCard)) {
                                                    AntiAddictionKit.authIdentity(currentUserId
                                                            , identificationInfo.userName
                                                            , identificationInfo.idCard
                                                            , ""
                                                            , new Callback<IdentifyResult>() {
                                                                @Override
                                                                public void onSuccess(IdentifyResult result) {
                                                                    Toast.makeText(MainActivity.this, result.toString(), Toast.LENGTH_SHORT).show();
                                                                }

                                                                @Override
                                                                public void onError(Throwable throwable) {
                                                                    Toast.makeText(MainActivity.this, "identify error", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                    );
                                                }
                                            }
                                        }
                                    }
                            ).show(MainActivity.this.getFragmentManager(), ModifyAttrsDialog.TAG);
                        } else if (funcAction instanceof EnterGameAction) {
                            AntiAddictionKit.enterGame();
                        } else if (funcAction instanceof LeaveGameAction) {
                            AntiAddictionKit.leaveGame();
                        } else if (funcAction instanceof CheckPayAction) {
                            AntiAddictionKit.checkPayLimit(payAmount, new Callback<CheckPayResult>() {
                                @Override
                                public void onSuccess(CheckPayResult result) {
                                    if (result.status) {
                                        Toast.makeText(MainActivity.this, "付费检查通过，可以付费", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "付费检查未通过，详情请见右方提示", Toast.LENGTH_SHORT).show();
                                        dashboardView.updatePromptInfo(result.title, result.description);
                                    }
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    dashboardView.updatePromptInfo("", throwable.getMessage());
                                }
                            });
                        } else if (funcAction instanceof PayAction) {
                            AntiAddictionKit.paySuccess(payAmount, new Callback<SubmitPayResult>() {

                                @Override
                                public void onSuccess(SubmitPayResult result) {
                                    Toast.makeText(MainActivity.this, "提交付费信息成功", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    Toast.makeText(MainActivity.this, "提交付费信息失败" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else if (funcAction instanceof ChangePayAmountAction) {
                            ModifyAttrsDialog.newInstance("修改支付金额"
                                    , String.valueOf(payAmount)
                                    , "取消"
                                    , "确定"
                                    , new AbstractAlertDialog.AlertClickCallback() {
                                        @Override
                                        public void onLeftClick(String extra) {

                                        }

                                        @Override
                                        public void onRightClick(String extra) {
                                            if (!TextUtils.isEmpty(extra)) {
                                                try {
                                                    payAmount = Long.parseLong(extra);
                                                    Toast.makeText(MainActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                                } catch (Exception e) {
                                                    Toast.makeText(MainActivity.this, "金额格式不正确", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    }
                            ).show(MainActivity.this.getFragmentManager(), ModifyAttrsDialog.TAG);
                        } else if (funcAction instanceof GetIdentifyInfoAction) {
                            AntiAddictionKit.fetchUserIdentifyInfo(currentUserId, new Callback<com.tapsdk.antiaddiction.entities.IdentificationInfo>() {
                                @Override
                                public void onSuccess(com.tapsdk.antiaddiction.entities.IdentificationInfo result) {
                                    Toast.makeText(MainActivity.this
                                            ,"result" + result.toString(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    Toast.makeText(MainActivity.this
                                            ,"throwable:" + throwable.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void listenToDebugEvent() {
        RxBus.getInstance().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof UpdateAccountAction) {
                            dashboardView.updateUserInfo(((UpdateAccountAction) event).userInfo
                                    , ((UpdateAccountAction) event).identificationInfo);
                        } else if (event instanceof UpdateAntiAddictionInfoAction) {
                            dashboardView.updateAntiAddictionInfo(((UpdateAntiAddictionInfoAction) event).serverTimeInSeconds
                                    , ((UpdateAntiAddictionInfoAction) event).remainTime, ((UpdateAntiAddictionInfoAction) event).playing);
                        } else if (event instanceof AntiAddictionLimitInfoAction) {
                            dashboardView.updateAntiAddictionLimitInfo(((AntiAddictionLimitInfoAction) event).loggedIn
                                    , ((AntiAddictionLimitInfoAction) event).canPlay
                                    , ((AntiAddictionLimitInfoAction) event).strictType
                            );
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFuncItems();
        listenToFunctionListEvent();
        listenToDebugEvent();

        bindView();

        AntiAddictionKit.init(this, "demo"
                , new AntiAddictionFunctionConfig.Builder()
                        .enablePaymentLimit(true)
                        .enableOnLineTimeLimit(true)
                        .build()
                , new AntiAddictionCallback() {
                    @Override
                    public void onCallback(int code, Map<String, Object> msg) {
                        AntiAddictionLogger.d("result:(" + code + "," + msg + ")");
                        if (code == Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS) {
                            // 防沉迷登录成功
                            funcBaseList.clear();
                            funcBaseList.addAll(loginSupportFuncList);
                            funcItemAdapter.setFuncBaseList(funcBaseList);
                            funcItemAdapter.notifyDataSetChanged();

                            dashboardView.updateAntiAddictionLimitInfo(true, true, 0);
                            dashboardView.updatePromptInfo("", "");
                        } else if (code == Constants.ANTI_ADDICTION_CALLBACK_CODE.OPEN_ALERT_TIP) {
                            // 提示消息
                            String title = String.valueOf(msg.get(Constants.MsgExtraParams.TITLE));
                            String description = String.valueOf(msg.get(Constants.MsgExtraParams.DESCRIPTION));
                            String targetTitle = title;
                            // 如何处理气泡消息示例（游戏倒计时的ui逻辑需要游戏自己画，sdk会返回提示消息和剩余时间）
                            if (msg.containsKey(Constants.MsgExtraParams.LIMIT_TIP_TYPE)
                                && AccountLimitTipEnum.STATE_COUNT_DOWN_POPUP.equals(msg.get(Constants.MsgExtraParams.LIMIT_TIP_TYPE))
                            ) {
                                String remainingTimeStr = "";
                                if (msg.containsKey(Constants.MsgExtraParams.REMAINING_TIME_STR)) {
                                    try {
                                        remainingTimeStr = (String) msg.get(Constants.MsgExtraParams.REMAINING_TIME_STR);
                                    } catch (Exception e) {
                                    }
                                }
                                if (!TextUtils.isEmpty(remainingTimeStr)) {
                                    targetTitle = title.replace("${remaining}", remainingTimeStr);
                                }
                            }
                            dashboardView.updatePromptInfo(targetTitle
                                    , description);
                        } else if (code == Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGOUT) {
                            // 登出后逻辑
                            dashboardView.updateAntiAddictionLimitInfo(true, true, 0);
                            dashboardView.updatePromptInfo("", "");
                            funcBaseList.clear();
                            funcBaseList.addAll(unLoginSupportFuncList);
                            funcItemAdapter.setFuncBaseList(funcBaseList);
                            funcItemAdapter.notifyDataSetChanged();
                            dashboardView.updateUserInfo(null, null);
                        } else if (code == Constants.ANTI_ADDICTION_CALLBACK_CODE.TIME_LIMIT
                                || code  == Constants.ANTI_ADDICTION_CALLBACK_CODE.NIGHT_STRICT) {
                            // 防沉迷限制（获取该状态时游戏应该禁止玩家继续游戏）
                            if (msg != null) {
                                int strictType = 0;
                                try {
                                    if (msg.containsKey(Constants.MsgExtraParams.STRICT_TYPE)
                                        && msg.get(Constants.MsgExtraParams.STRICT_TYPE) instanceof Integer
                                    ) {
                                        strictType = (int) msg.get(Constants.MsgExtraParams.STRICT_TYPE);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (msg.containsKey(Constants.MsgExtraParams.LIMIT_TIP_TYPE)) {
                                    if (AccountLimitTipEnum.STATE_CHILD_ENTER_STRICT.equals(msg.get(Constants.MsgExtraParams.LIMIT_TIP_TYPE))
                                        || AccountLimitTipEnum.STATE_ENTER_LIMIT.equals(msg.get(Constants.MsgExtraParams.LIMIT_TIP_TYPE))
                                    ) {
                                        // 这边只是演示，登录时遇到宵禁或者时长耗尽，需要做什么处理
                                        funcBaseList.clear();
                                        funcBaseList.addAll(loginSupportFuncList);
                                        funcItemAdapter.setFuncBaseList(funcBaseList);
                                        funcItemAdapter.notifyDataSetChanged();
                                        dashboardView.updateAntiAddictionLimitInfo(true, false, strictType);
                                        dashboardView.updatePromptInfo("", "");
                                    }
                                }
                            }
                        }
                    }
                }
        );
    }

    private void bindView() {
        dashboardView = findViewById(R.id.dashboardView);
    }
}