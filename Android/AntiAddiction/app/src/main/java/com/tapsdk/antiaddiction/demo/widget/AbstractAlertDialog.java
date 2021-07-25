package com.tapsdk.antiaddiction.demo.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tapsdk.antiaddiction.demo.R;
import com.tapsdk.antiaddiction.demo.utils.UIUtil;

public abstract class AbstractAlertDialog extends SafeDialogFragment {

    protected Activity mActivity;

    public abstract View getContentView();

    public abstract Event leftEvent();

    public abstract Event rightEvent();

    public abstract int[] getLayoutParams();

    public interface AlertClickCallback {

        void onLeftClick(String extra);

        void onRightClick(String extra);

    }

    public static class Event {

        String text;
        View.OnClickListener listener;

        public Event(String text, View.OnClickListener listener) {
            this.text = text;
            this.listener = listener;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = getActivity();
        Dialog dialog = getDialog();
        if (dialog == null ||
                activity == null ||
                activity.getWindowManager() == null ||
                dialog.getWindow() == null) {
            return;
        }
        dialog.getWindow().setLayout(getLayoutParams()[0], WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_alert, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() == null) {
            dismiss();
            return;
        }
        initView(view);
    }

    private void initView(View view) {
        TextView mTvNegative = view.findViewById(R.id.tv_alert_negative);
        TextView mTvPositive = view.findViewById(R.id.tv_alert_positive);
        if (leftEvent() != null) {
            mTvNegative.setText(leftEvent().text);
            mTvNegative.setOnClickListener(leftEvent().listener);
            mTvNegative.setVisibility(View.VISIBLE);
        } else {
            mTvNegative.setVisibility(View.GONE);
        }
        if (rightEvent() != null) {
            mTvPositive.setText(rightEvent().text);
            mTvPositive.setOnClickListener(rightEvent().listener);
            mTvPositive.setVisibility(View.VISIBLE);
        } else {
            mTvPositive.setVisibility(View.GONE);
        }
        if (view instanceof LinearLayout && getContentView() != null) {
            ViewGroup.LayoutParams viewLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtil.dp2px(getActivity(), ViewGroup.LayoutParams.WRAP_CONTENT));
            ((LinearLayout) view).addView(getContentView(), 0, viewLayoutParams);
        }
    }
}
