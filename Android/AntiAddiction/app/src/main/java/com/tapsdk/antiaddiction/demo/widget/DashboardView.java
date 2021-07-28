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
        ((TextView) findViewById(R.id.antiAddictionLimitInfoTextView)).setText("未登录防沉迷");
    }

    private List<String> getUserInfoList(UserInfo userInfo, IdentificationInfo identificationInfo) {
        List<String> userInfoList = new ArrayList<>();
        if (userInfo != null) {
            userInfoList.add("防沉迷用户ID:" + userInfo.userId);
            userInfoList.add("防沉迷用户类型:" + userInfo.accountType);
        } else {
            userInfoList.add("无用户信息");
            return userInfoList;
        }

        if (identificationInfo != null) {
            userInfoList.add("姓名:" + identificationInfo.name);
            userInfoList.add("身份证:" + identificationInfo.idCard);
            userInfoList.add("身份认证状态:" + identificationInfo.authState);
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
                ((TextView) findViewById(R.id.antiAddictionLimitInfoTextView)).setText("防沉迷授权成功，可以开始计时");
            } else {
                String prompt = "已启动防沉迷时长限制，原因:";
                if (strictType == StrictType.TIME_LIMIT) prompt += "时长限制";
                else prompt += "宵禁限制";
                ((TextView) findViewById(R.id.antiAddictionLimitInfoTextView)).setText(prompt);
            }
        } else {
            ((TextView) findViewById(R.id.antiAddictionLimitInfoTextView)).setText("未登录防沉迷");
        }
    }

    private List<String> getAntiAddictionInfoList(long serverTimeInSeconds, long remainTime, boolean playing) {

        List<String> antiAddictionInfoList = new ArrayList<>();
        if (playing) antiAddictionInfoList.add("状态:游戏中");

        if (serverTimeInSeconds != -1L) {
            antiAddictionInfoList.add("当前时间:" + TimeUtil.getFullTime(serverTimeInSeconds * 1000));
        }
        if (remainTime != -1L) {
            antiAddictionInfoList.add("剩余游戏时间:" + remainTime);
        }
        return antiAddictionInfoList;
    }

    public void updateAntiAddictionInfo(long serverTimeInSeconds, long remainTime, boolean playing) {
        antiAddictionInfoAdapter.reset(getAntiAddictionInfoList(serverTimeInSeconds, remainTime, playing));
        antiAddictionInfoAdapter.notifyDataSetChanged();
    }
}
