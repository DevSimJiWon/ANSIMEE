package com.example.ansimee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;


public class ShowActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show);  
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
        else {
            Log.e("token", FirebaseInstanceId.getInstance().getToken());

            Map<String, Object> fcm = new HashMap<>();
            fcm.put("fcm", FirebaseInstanceId.getInstance().getToken());

            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .set(fcm)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "DocumentSnapshot successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error updating document", e);
                        }
                    });
            findViewById(R.id.button8).setOnClickListener(onClickListener); //사용자조회/등록버튼
            findViewById(R.id.button9).setOnClickListener(onClickListener); //도난내역조회버튼
            findViewById(R.id.button10).setOnClickListener(onClickListener); //
            findViewById(R.id.button11).setOnClickListener(onClickListener); //로그아웃버튼

        }
    }
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.button8:
                    Intent intent = new Intent(getApplicationContext(), ImglistActivity.class);
                    startActivity(intent);
                    break;

                case R.id.button9:
                    Intent intent1 = new Intent(getApplicationContext(), RoblistActivity.class);
                    startActivity(intent1);
                    break;

                case R.id.button10:
                    Intent intent2 = new Intent(getApplicationContext(), SettingActivity.class);
                    startActivity(intent2);
                    break;

                case R.id.button11:
                    FirebaseAuth.getInstance().signOut();
                    Intent intent3 = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent3);
                    break;
            }
        }
    };


}
