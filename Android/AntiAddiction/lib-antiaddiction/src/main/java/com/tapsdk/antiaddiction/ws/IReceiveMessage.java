package com.tapsdk.antiaddiction.ws;

public interface IReceiveMessage {

    void onConnectSuccess();

    void onConnectFailed();

    void onClose();

    void onMessage(String text);
}
