package com.tapsdk.antiaddiction.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.tapsdk.antiaddiction.demo.models.FuncAction;
import com.tapsdk.antiaddiction.demo.models.FuncBase;
import com.tapsdk.antiaddiction.demo.models.FuncCategroyInfo;
import com.tapsdk.antiaddiction.demo.models.FuncItemInfo;
import com.tapsdk.antiaddiction.reactor.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;


public class FuncItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final static int TYPE_FUNC_CATEGORY = 0;
    public final static int TYPE_FUNC_ITEM = 1;

    public List<FuncBase> data;

    public PublishSubject<FuncAction> funcActionPublishSubject = PublishSubject.create();

    public FuncItemAdapter() {
        data = new ArrayList<>();
    }

    public void setFuncBaseList(List<FuncBase> funcBaseList) {
        data = funcBaseList;
    }

    @Override
    public int getItemViewType(int position) {
        FuncBase funcBase = data.get(position);
        if (funcBase instanceof FuncCategroyInfo) {
            return TYPE_FUNC_CATEGORY;
        } else if (funcBase instanceof FuncItemInfo) {
            return TYPE_FUNC_ITEM;
        } else {
            throw new RuntimeException("unknow type");
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FUNC_CATEGORY) {
            return new FuncCategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_func_category_info, parent, false));
        } else {
            return new FuncItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_func_item_info, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FuncBase funcBase = data.get(position);
        if (funcBase instanceof FuncCategroyInfo) {
            renderFuncCategory((FuncCategoryViewHolder) holder, (FuncCategroyInfo) funcBase);
        } else {
            renderFuncItem((FuncItemViewHolder) holder, (FuncItemInfo) funcBase);
        }
    }

    private void renderFuncCategory(FuncCategoryViewHolder holder, FuncCategroyInfo categoryInfo) {
        holder.funcCategroyTextView.setText(categoryInfo.categoryName);
    }

    private void renderFuncItem(FuncItemViewHolder holder, FuncItemInfo itemInfo) {
        holder.funcItemTextView.setText(itemInfo.funcName);
        holder.itemView.setOnClickListener(v -> funcActionPublishSubject.onNext(itemInfo.funcAction));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public FuncItemAdapter(List<FuncBase> funcInfoList) {
        this.data = funcInfoList;
    }

    static class FuncCategoryViewHolder extends RecyclerView.ViewHolder {

        TextView funcCategroyTextView;

        public FuncCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            funcCategroyTextView = itemView.findViewById(R.id.categoryNameTextView);
        }
    }

    static class FuncItemViewHolder extends RecyclerView.ViewHolder {
        TextView funcItemTextView;

        public FuncItemViewHolder(@NonNull View itemView) {
            super(itemView);
            funcItemTextView = itemView.findViewById(R.id.funcItemTextView);
        }
    }
}
