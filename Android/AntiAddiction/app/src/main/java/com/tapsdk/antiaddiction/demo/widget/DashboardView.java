package com.tapsdk.antiaddiction.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tapsdk.antiaddiction.demo.FuncItemAdapter;
import com.tapsdk.antiaddiction.demo.R;
import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class DashboardView extends ConstraintLayout {

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
        RecyclerView recyclerView = view.findViewById(R.id.userInfoRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
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

        RecyclerView recyclerView = findViewById(R.id.userInfoRecyclerView);
        recyclerView.setAdapter(new SimpleAdapter(getUserInfoList(userInfo, identificationInfo)));


    }
}
