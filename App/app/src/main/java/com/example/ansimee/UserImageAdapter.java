package com.example.ansimee;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class UserImageAdapter extends RecyclerView.Adapter<UserImageAdapter.ViewHolder> {
private final List <ImgData> data;
private final Context context;

// 아이템 뷰를 저장하는 뷰홀더 클래스.
public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView tv;
    ImageView iv;
    private final Button delButton;

    ViewHolder(View itemView) {
        super(itemView);
        // 뷰 객체에 대한 참조. (hold strong reference)
        tv = itemView.findViewById(R.id.textView280);
        iv = itemView.findViewById(R.id.imageView20);
        delButton = itemView.findViewById(R.id.button170);
    }
}
    // 생성자에서 데이터 리스트 객체를 전달받음.
    public UserImageAdapter(List<ImgData> data, Context context) {
        this.data = data;
        this.context = context;
    }

    //아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @NonNull
    @Override
    public UserImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false);
        return new UserImageAdapter.ViewHolder(view);
    }

    // position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(UserImageAdapter.ViewHolder holder, int position) {
        ImgData data = this.data.get(position);
        holder.tv.setText(data.name);
        Glide.with(context).load(data.imageUri).into(holder.iv);
        holder.delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("qwerty", data.name.toString());
                // click event
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                String uid = user.getUid();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference desertRef = storageRef.child("user_image").child(uid).child(data.name);
                // Delete the file
                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("qwerty", "deleted");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                        Log.d("qwerty", "error");
                    }
                });
            }
        });
    }

    // 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return data.size();
    }
}

