package com.tapsdk.antiaddiction.ws;

import android.text.TextUtils;

import com.tapsdk.antiaddiction.skynet.okhttp3.OkHttpClient;
import com.tapsdk.antiaddiction.skynet.okhttp3.Request;
import com.tapsdk.antiaddiction.skynet.okhttp3.Response;
import com.tapsdk.antiaddiction.skynet.okhttp3.WebSocket;
import com.tapsdk.antiaddiction.skynet.okhttp3.WebSocketListener;
import com.tapsdk.antiaddiction.skynet.okio.ByteString;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;

import java.util.concurrent.TimeUnit;


public final class WebSocketManager {
    private final static int MAX_NUM = 5;
    private final static int MILLIS = 5000;
//    private final static String WSURL = "wss://tds-tapsdk-ws.cn.tapapis.com/ws/xd/v1";
    private boolean connected = false;

    private static class Holder {
        static WebSocketManager INSTANCE = new WebSocketManager();
    }

    private IReceiveMessage iReceiveMessage;
    private WebSocket mWebSocket;
    private int connectNum = 0;
    private String antiToken = "";
    private String gameName = "";
    private String wsUrl = "";

    public static WebSocketManager getInstance() {
        return Holder.INSTANCE;
    }

    public void init(String antiToken, String gameName, IReceiveMessage iReceiveMessage, String wsUrl) {
        this.antiToken = antiToken;
        this.gameName = gameName;
        this.iReceiveMessage = iReceiveMessage;
        this.wsUrl = wsUrl;
        connectCentralDepartment(antiToken, gameName);
    }

    private void connectCentralDepartment(String antiToken, String gameName) {
        if (TextUtils.isEmpty(antiToken)) {
            AntiAddictionLogger.d("antiToken is empty");
            return;
        }
        if (TextUtils.isEmpty(gameName)) {
            AntiAddictionLogger.d("gameName is empty");
            return;
        }
        // xd后台配的游戏简称
        String url = wsUrl + "?client_id=" + gameName;
        OkHttpClient client = new OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", antiToken)
                .build();
        if (isConnect()) {
            AntiAddictionLogger.d("WebSocket connected");
            return;
        }

        client.newWebSocket(request, createListener());
    }

    /**
     * 重连
     */
    public void reconnect() {
        AntiAddictionLogger.d("WebSocket reconnect");
        if (connectNum <= MAX_NUM) {
            try {
                Thread.sleep(MILLIS);
                connectCentralDepartment(antiToken, gameName);
                connectNum++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            AntiAddictionLogger.d("reconnect over " + MAX_NUM + ",please check url or network");
        }
    }


    private WebSocketListener createListener() {
        return new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                AntiAddictionLogger.d("WebSocket onOpen:" + response.toString());
                mWebSocket = webSocket;
                connected = response.code() == 101;
                if (!connected) {
                    reconnect();
                } else {
                    AntiAddictionLogger.d("WebSocket connect success");
                    if (iReceiveMessage != null) {
                        iReceiveMessage.onConnectSuccess();
                    }
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                if (iReceiveMessage != null) {
                    iReceiveMessage.onMessage(text);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                AntiAddictionLogger.d("WebSocket onMessage(bytes):" + bytes.base64());
                if (iReceiveMessage != null) {
                    iReceiveMessage.onMessage(bytes.base64());
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                AntiAddictionLogger.d("WebSocket onClosing:");
                mWebSocket = null;
                connected = false;
                if (iReceiveMessage != null) {
                    iReceiveMessage.onClose();
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                AntiAddictionLogger.d("WebSocket onClosing:code" + code + ",reason=" + reason);
                mWebSocket = null;
                connected = false;
                if (iReceiveMessage != null) {
                    iReceiveMessage.onClose();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                if (response != null) {
                    AntiAddictionLogger.d("WebSocket 连接失败：" + response.message());
                }
                AntiAddictionLogger.d("WebSocket 连接失败异常原因：" + t.getMessage());
                connected = false;
                if (iReceiveMessage != null) {
                    iReceiveMessage.onConnectFailed();
                }
                if (t.getMessage() != null && !TextUtils.isEmpty(t.getMessage()) && !t.getMessage().equals("Socket closed")) {
                    reconnect();
                }
            }
        };
    }

    public void close() {
        AntiAddictionLogger.d("WebSocket close");
        if (isConnect()) {
            mWebSocket.cancel();
            // todo translate to english
            mWebSocket.close(1001, "client offer to close connection");
        }
    }

    public boolean isConnect() {
        return mWebSocket != null && connected;
    }
}
