package com.example.ansimee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RobImageAdapter extends RecyclerView.Adapter<RobImageAdapter.ViewHolder> {
    private final List<RobData> data;
    private final Context context;

    // 아이템 뷰를 저장하는 뷰홀더 클래스
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ImageView iv;

        ViewHolder(View itemView) {
            super(itemView);
            // 뷰 객체에 대한 참조 (hold strong reference)
            tv = itemView.findViewById(R.id.textView28);
            iv = itemView.findViewById(R.id.imageView2);
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음
    public RobImageAdapter(List<RobData> data, Context context) {
        this.data = data;
        this.context = context;
    }

    //아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴
    @NonNull
    @Override
    public RobImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    //position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(RobImageAdapter.ViewHolder holder, int position) {
        RobData data = this.data.get(position);
        holder.tv.setText(data.name);
        Glide.with(context).load(data.imageUri).into(holder.iv);

    }

    //전체 데이터 갯수 리턴
    @Override
    public int getItemCount() {
        return data.size();
    }

    private Task<String> addMessage(String text) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("push", true);

        FirebaseFunctions mFunctions = null;
        return mFunctions
                .getHttpsCallable("addMessage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }
}
