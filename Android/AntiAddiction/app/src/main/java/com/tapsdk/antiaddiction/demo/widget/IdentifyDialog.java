package com.tapsdk.antiaddiction.demo.widget;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tapsdk.antiaddiction.demo.R;
import com.tapsdk.antiaddiction.demo.models.IdentificationInfo;
import com.tapsdk.antiaddiction.demo.utils.ActivityUtils;
import com.tapsdk.antiaddiction.demo.utils.UIUtil;

public class IdentifyDialog extends AbstractAlertDialog {

    public static final String TAG = "IdentifyDialog";

    private static final String IDENTIFY_TITLE = "identify_title";

    private static final String USER_ID_EXTRA_PARAM = "user_id_extra_param";

    private static final String CONFIRM_NEGATIVE = "CONFIRM_NEGATIVE";

    private static final String CONFIRM_POSITIVE = "CONFIRM_POSITIVE";

    private AlertClickCallback mCallback;

    TextView userIdTextView;

    EditText userNameEditText;

    EditText idCardEditView;

    private Gson gson = new GsonBuilder().create();

    public static IdentifyDialog newInstance(
            String title,
            String userId,
            String negative,
            String positive,
            AlertClickCallback callback) {
        IdentifyDialog dialog = new IdentifyDialog();
        dialog.mCallback = callback;
        Bundle args = new Bundle();
        args.putString(IDENTIFY_TITLE, title);
        args.putString(USER_ID_EXTRA_PARAM, userId);
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
        View view = LayoutInflater.from(activity).inflate(R.layout.view_identify_content, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.height = UIUtil.dp2px(activity, 250);
        view.setLayoutParams(layoutParams);
        ((TextView) view.findViewById(R.id.tv_alert_confirm_title)).setText(args.getString(IDENTIFY_TITLE));
        userIdTextView = view.findViewById(R.id.userIdTextView);
        userIdTextView.setText("用户ID:" + args.getString(USER_ID_EXTRA_PARAM));
        userNameEditText = view.findViewById(R.id.userNameEditText);
        idCardEditView = view.findViewById(R.id.idCardEditText);

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
                        IdentificationInfo identificationInfo = new IdentificationInfo();
                        if (!TextUtils.isEmpty(userNameEditText.getText())) {
                            identificationInfo.userName = userNameEditText.getText().toString();
                        }

                        if (!TextUtils.isEmpty(idCardEditView.getText())) {
                            identificationInfo.idCard = idCardEditView.getText().toString();
                        }
                        mCallback.onRightClick(gson.toJson(identificationInfo));
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
