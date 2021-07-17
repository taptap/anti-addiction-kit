package com.tapsdk.antiaddiction.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.tapsdk.antiaddiction.AntiAddictionCallback;
import com.tapsdk.antiaddiction.AntiAddictionKit;
import com.tapsdk.antiaddiction.Callback;
import com.tapsdk.antiaddiction.config.AntiAddictionFunctionConfig;
import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.response.IdentifyResult;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AntiAddictionKit.init(this, "demo"
                , new AntiAddictionFunctionConfig.Builder()
                        .enablePaymentLimit(true)
                        .enableOnLineTimeLimit(true)
                        .build()
                , new AntiAddictionCallback() {
                    @Override
                    public void onCallback(int code, String msg) {
                        AntiAddictionLogger.d("result:(" + code + "," + msg + ")");
                    }
                }
        );

        findViewById(R.id.fetchUserIdentifyInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AntiAddictionKit.fetchUserIdentifyInfo("123", new Callback<IdentificationInfo>() {
                    @Override
                    public void onSuccess(IdentificationInfo result) {
                        Toast.makeText(MainActivity.this
                                , "fetchUserIdentifyInfo success:" + result.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Toast.makeText(MainActivity.this
                                , "fetchUserIdentifyInfo fail", Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });

        findViewById(R.id.identifyUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AntiAddictionKit.authIdentity("456"
                        , "何兴华"
                        , "310112198409285217"
                        , "15901963713"
                        , new Callback<IdentifyResult>() {
                            @Override
                            public void onSuccess(IdentifyResult result) {
                                Toast.makeText(MainActivity.this
                                        , "identifyUser success:" + result.toString(), Toast.LENGTH_SHORT).show();
                                AntiAddictionLogger.d("identifyUser success:" + result.toString());
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Toast.makeText(MainActivity.this
                                        , "identifyUser success:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                AntiAddictionLogger.e(throwable.getMessage());
                            }
                        });
            }
        });

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AntiAddictionKit.login("123");
            }
        });

    }
}