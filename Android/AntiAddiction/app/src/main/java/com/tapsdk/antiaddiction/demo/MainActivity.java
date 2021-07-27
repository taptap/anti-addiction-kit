package com.tapsdk.antiaddiction.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.tapsdk.antiaddiction.config.AntiAddictionFunctionConfig;
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
                , new FuncItemInfo("实际支付", new PayAction())
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
                            funcBaseList.clear();
                            funcBaseList.addAll(unLoginSupportFuncList);
                            funcItemAdapter.setFuncBaseList(funcBaseList);
                            funcItemAdapter.notifyDataSetChanged();
                            dashboardView.updateUserInfo(null, null);
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
                                        Toast.makeText(MainActivity.this, Toast.LENGTH_SHORT, Toast.LENGTH_SHORT).show();
                                    } else {
                                        dashboardView.updatePromptInfo(result.title, result.description);
                                    }
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    dashboardView.updatePromptInfo("", "");
                                }
                            });
                        } else if (funcAction instanceof PayAction) {
                            AntiAddictionKit.paySuccess(payAmount, new Callback<SubmitPayResult>() {

                                @Override
                                public void onSuccess(SubmitPayResult result) {

                                }

                                @Override
                                public void onError(Throwable throwable) {

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
                                    Log.d("hxh", "identifyInfo:" + result.toString());
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    Log.d("hxh", "identifyInfo:" + throwable.toString());
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
                        if (code == AntiAddictionKit.CALLBACK_CODE_LOGIN_SUCCESS) {
                            funcBaseList.clear();
                            funcBaseList.addAll(loginSupportFuncList);
                            funcItemAdapter.setFuncBaseList(funcBaseList);
                            funcItemAdapter.notifyDataSetChanged();
                        } else if (code == AntiAddictionKit.CALLBACK_CODE_OPEN_ALERT_TIP) {
                            funcBaseList.clear();
                            funcBaseList.addAll(loginSupportFuncList);
                            funcItemAdapter.setFuncBaseList(funcBaseList);
                            funcItemAdapter.notifyDataSetChanged();
                            dashboardView.updatePromptInfo(
                                    String.valueOf(msg.get("title"))
                                    , String.valueOf(msg.get("description")));
                        }
                    }
                }
        );


//        findViewById(R.id.fetchUserIdentifyInfo).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AntiAddictionKit.fetchUserIdentifyInfo("123", new Callback<IdentificationInfo>() {
//                    @Override
//                    public void onSuccess(IdentificationInfo result) {
//                        Toast.makeText(MainActivity.this
//                                , "fetchUserIdentifyInfo success:" + result.toString(), Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        Toast.makeText(MainActivity.this
//                                , "fetchUserIdentifyInfo fail", Toast.LENGTH_SHORT
//                        ).show();
//                    }
//                });
//            }
//        });
//
//        findViewById(R.id.identifyUser).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AntiAddictionKit.authIdentity("791"
//                        , "李四"
//                        , "310101200803070290"
//                        , "13580460916"
//                        , new Callback<IdentifyResult>() {
//                            @Override
//                            public void onSuccess(IdentifyResult result) {
//                                Toast.makeText(MainActivity.this
//                                        , "identifyUser success:" + result.toString(), Toast.LENGTH_SHORT).show();
//                                AntiAddictionLogger.d("identifyUser success:" + result.toString());
//                            }
//
//                            @Override
//                            public void onError(Throwable throwable) {
//                                Toast.makeText(MainActivity.this
//                                        , "identifyUser success:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                                AntiAddictionLogger.e(throwable.getMessage());
//                            }
//                        });
//            }
//        });
//
//        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AntiAddictionKit.login("791");
//            }
//        });
//
//        findViewById(R.id.startTimingButton).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AntiAddictionKit.enterGame();
//            }
//        });
//
//        findViewById(R.id.stopTimingButton).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AntiAddictionKit.leaveGame();
//            }
//        });
//
//        findViewById(R.id.testSubscribeOnTwice).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Observable.create(new Observable.OnSubscribe<Object>(){
//                    @Override
//                    public void call(Subscriber<? super Object> subscriber) {
//                        Object message = new Object();
//                        subscriber.onNext(message);
//                        subscriber.onCompleted();
//                    }
//                })
//                        .subscribeOn(Schedulers.newThread())
//                        .map(new Func1<Object, Object>() {
//                            @Override
//                            public Object call(Object o) {
//                                Log.d("test", "map1 emitting source from thread:" + Thread.currentThread());
//                                return o;
//                            }
//                        })
//                        .subscribeOn(Schedulers.io())
//                        .map(new Func1<Object, Object>() {
//                            @Override
//                            public Object call(Object o) {
//                                Log.d("test", "map2 emitting source from thread:" + Thread.currentThread());
//                                return null;
//                            }
//                        })
//                        .subscribe(new Subscriber<Object>() {
//
//                            @Override
//                            public void onCompleted() {
//
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//
//                            }
//
//                            @Override
//                            public void onNext(Object o) {
//                                Log.d("test", "oNext:" + Thread.currentThread());
//                            }
//                        });
//            }
//        });
    }

    private void bindView() {
        dashboardView = findViewById(R.id.dashboardView);
    }
}