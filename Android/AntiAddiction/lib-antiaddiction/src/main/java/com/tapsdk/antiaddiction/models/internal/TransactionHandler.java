package com.tapsdk.antiaddiction.models.internal;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tapsdk.antiaddiction.entities.ThreeTuple;
import com.tapsdk.antiaddiction.utils.AntiAddictionLogger;

public class TransactionHandler extends Handler {

    public final static int MESSAGE_COUNT_TIME = 0;
    public final static int MESSAGE_COUNT_DOWN = 1;
    public final static int MESSAGE_SEND_TIME = 2;
    public final static int MESSAGE_STOP_COUNT_DOWN_TIME = 3;
    public final static int MESSAGE_CHILD_TIME_RUN_OUT = 4;
    public final static int MESSAGE_LOGOUT = 5;

    // timed task
//    public final static int COUNT_TIME_PERIOD = 2 * 60 * 1000;
     public final static int COUNT_TIME_PERIOD = 10 * 1000;
    public final static int COUNT_DOWN_PERIOD = 1000;

    private final InteractiveOperation operation;

    public interface InteractiveOperation {
        void countTime();

        boolean countDown(String title, String description, int restrictType);

        void updateServerTime();

        void stopCountDownTimerAndUpdateServerTime();

        void childTimeRunOut(int strictType) throws Throwable;

        void logout();
    }

    public TransactionHandler(Looper looper, InteractiveOperation operation) {
        super(looper);
        this.operation = operation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        try {
            switch (msg.what) {
                case MESSAGE_COUNT_TIME:
                    operation.countTime();
                    AntiAddictionLogger.d("MESSAGE_COUNT_TIME:" + COUNT_TIME_PERIOD);
                    sendMessageDelayed(Message.obtain(msg), COUNT_TIME_PERIOD);
                    break;
                case MESSAGE_COUNT_DOWN:
                    ThreeTuple<String, String, Integer> tuple = (ThreeTuple<String, String, Integer>) msg.obj;
                    boolean finished = operation.countDown(tuple.firstParam, tuple.secondParam, tuple.thirdParam);
                    if (!finished) sendMessageDelayed(Message.obtain(msg), COUNT_DOWN_PERIOD);
                    break;
                case MESSAGE_SEND_TIME:
                    operation.updateServerTime();
                    break;
                case MESSAGE_STOP_COUNT_DOWN_TIME:
                    operation.stopCountDownTimerAndUpdateServerTime();
                    break;
                case MESSAGE_CHILD_TIME_RUN_OUT:
                    int strictType = (int) msg.obj;
                    operation.childTimeRunOut(strictType);
                    break;
                case MESSAGE_LOGOUT:
                    operation.logout();
                default:
                    break;
            }
        } catch (Exception e) {
            AntiAddictionLogger.e("TransactionHandler handleMessage error");
            AntiAddictionLogger.printStackTrace(e);
        } catch (Throwable throwable) {
            AntiAddictionLogger.printStackTrace(throwable);
        }
    }
}
