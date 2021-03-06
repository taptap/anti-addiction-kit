package com.tapsdk.antiaddiction.demo.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tapsdk.antiaddiction.demo.R;
import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.UserInfo;
import com.tapsdk.antiaddiction.models.StrictType;
import com.tapsdk.antiaddiction.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class DashboardView extends ConstraintLayout {

    private RecyclerView userInfoRecyclerView, antiAddictionInfoRecyclerView;
    private SimpleAdapter userInfoAdapter, antiAddictionInfoAdapter;

    public DashboardView(Context context) {
        super(context);
        initInnerChildLayout(context);
    }

    public DashboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initInnerChildLayout(context);
    }

    public DashboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initInnerChildLayout(context);
    }

    private void initInnerChildLayout(Context context) {
        View childView = LayoutInflater.from(context).inflate(R.layout.view_dashboard, this, true);
        bindView(childView);
    }

    private void bindView(View view) {
        userInfoRecyclerView = view.findViewById(R.id.userInfoRecyclerView);
        userInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        userInfoRecyclerView.addItemDecoration(new DividerItemDecoration(this.getContext(),
                LinearLayoutManager.VERTICAL));
        userInfoAdapter = new SimpleAdapter();
        userInfoRecyclerView.setAdapter(userInfoAdapter);

        antiAddictionInfoRecyclerView = view.findViewById(R.id.antiAddictionInfoRecyclerView);
        antiAddictionInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        antiAddictionInfoRecyclerView.addItemDecoration(new DividerItemDecoration(this.getContext(),
                LinearLayoutManager.VERTICAL));
        antiAddictionInfoAdapter = new SimpleAdapter();
        antiAddictionInfoRecyclerView.setAdapter(antiAddictionInfoAdapter);
        ((TextView) findViewById(R.id.antiAddictionLimitInfoTextView)).setText("??????????????????");
    }

    private List<String> getUserInfoList(UserInfo userInfo, IdentificationInfo identificationInfo) {
        List<String> userInfoList = new ArrayList<>();
        if (userInfo != null) {
            userInfoList.add("???????????????ID:" + userInfo.userId);
            userInfoList.add("?????????????????????:" + userInfo.accountType);
        } else {
            userInfoList.add("???????????????");
            return userInfoList;
        }

        if (identificationInfo != null) {
            userInfoList.add("??????:" + identificationInfo.name);
            userInfoList.add("?????????:" + identificationInfo.idCard);
            userInfoList.add("??????????????????:" + identificationInfo.authState);
        }
        return userInfoList;
    }

    public void updateUserInfo(UserInfo userInfo, IdentificationInfo identificationInfo) {
        userInfoAdapter.reset(getUserInfoList(userInfo, identificationInfo));
        userInfoAdapter.notifyDataSetChanged();
    }

    public void updatePromptInfo(String title, String description) {
        if (!TextUtils.isEmpty(title)) {
            ((TextView) findViewById(R.id.promptTitleTextView)).setText(title);
        } else {
            ((TextView) findViewById(R.id.promptTitleTextView)).setText("");
        }

        if (!TextUtils.isEmpty(description)) {
            ((TextView) findViewById(R.id.promptDescriptionTextView)).setText(description);
        } else {
            ((TextView) findViewById(R.id.promptDescriptionTextView)).setText("");
        }
    }

    public void updateAntiAddictionLimitInfo(boolean loggedIn, boolean canPlay, int strictType) {
        if (loggedIn) {
            if (canPlay) {
                ((TextView) findViewById(R.id.antiAddictionLimitInfoTextView)).setText("??????????????????????????????????????????");
            } else {
                String prompt = "???????????????????????????????????????:";
                if (strictType == StrictType.TIME_LIMIT) prompt += "????????????";
                else prompt += "????????????";
                ((TextView) findViewById(R.id.antiAddictionLimitInfoTextView)).setText(prompt);
            }
        } else {
            ((TextView) findViewById(R.id.antiAddictionLimitInfoTextView)).setText("??????????????????");
        }
    }

    private List<String> getAntiAddictionInfoList(long serverTimeInSeconds, long remainTime, boolean playing) {

        List<String> antiAddictionInfoList = new ArrayList<>();
        if (playing) antiAddictionInfoList.add("??????:?????????");

        if (serverTimeInSeconds != -1L) {
            antiAddictionInfoList.add("????????????:" + TimeUtil.getFullTime(serverTimeInSeconds * 1000));
        }
        if (remainTime != -1L) {
            antiAddictionInfoList.add("??????????????????:" + remainTime);
        }
        return antiAddictionInfoList;
    }

    public void updateAntiAddictionInfo(long serverTimeInSeconds, long remainTime, boolean playing) {
        antiAddictionInfoAdapter.reset(getAntiAddictionInfoList(serverTimeInSeconds, remainTime, playing));
        antiAddictionInfoAdapter.notifyDataSetChanged();
    }
}
