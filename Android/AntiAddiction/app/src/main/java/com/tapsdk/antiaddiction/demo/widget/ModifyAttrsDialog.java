package com.tapsdk.antiaddiction.demo.widget;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tapsdk.antiaddiction.demo.R;
import com.tapsdk.antiaddiction.demo.utils.ActivityUtils;
import com.tapsdk.antiaddiction.demo.utils.UIUtil;

public class ModifyAttrsDialog extends AbstractAlertDialog {

    public static final String TAG = "ModifyAttrsDialog";

    private static final String MODIFY_TITLE = "modify_title";

    private static final String MODIFY_HINT = "modify_hint";

    private static final String CONFIRM_NEGATIVE = "CONFIRM_NEGATIVE";

    private static final String CONFIRM_POSITIVE = "CONFIRM_POSITIVE";

    private AlertClickCallback mCallback;

    EditText modifyAttrsEditText;

    public static ModifyAttrsDialog newInstance(
            String title,
            String hint,
            String negative,
            String positive,
            AlertClickCallback callback) {
        ModifyAttrsDialog dialog = new ModifyAttrsDialog();
        dialog.mCallback = callback;
        Bundle args = new Bundle();
        args.putString(MODIFY_TITLE, title);
        args.putString(MODIFY_HINT, hint);
        args.putString(CONFIRM_NEGATIVE, negative);
        args.putString(CONFIRM_POSITIVE, positive);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View getContentView() {
        Activity activity = getActivity();
        if (ActivityUtils.isActivityNotAlive(activity)) return null;
        Bundle args = getArguments();
        if (args == null) {
            dismiss();
            return null;
        }
        View view = LayoutInflater.from(activity).inflate(R.layout.view_change_attrs_content, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.height = UIUtil.dp2px(activity, 165);
        view.setLayoutParams(layoutParams);
        ((TextView) view.findViewById(R.id.tv_alert_confirm_title)).setText(args.getString(MODIFY_TITLE));
        modifyAttrsEditText = view.findViewById(R.id.modifyAttrsEditText);
        modifyAttrsEditText.setHint(args.getString(MODIFY_HINT));
        return view;
    }

    @Override
    public Event leftEvent() {
        return new Event(
                getArguments().getString(CONFIRM_NEGATIVE),
                view -> {
                    if (mCallback != null) {
                        mCallback.onLeftClick("");
                    }
                    dismiss();
                });
    }

    @Override
    public Event rightEvent() {
        return new Event(
                getArguments().getString(CONFIRM_POSITIVE),
                view -> {
                    if (mCallback != null) {
                        mCallback.onRightClick(modifyAttrsEditText.getText().toString());
                    }
                    dismiss();
                });
    }

    @Override
    public int[] getLayoutParams() {
        int[] params = new int[2];
        Activity activity = getActivity();
        if (ActivityUtils.isActivityNotAlive(activity)) return params;
        params[0] = UIUtil.dp2px(activity, 335);
        params[1] = UIUtil.dp2px(activity, 75);
        return params;
    }
}
