package com.example.ansimee;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("IDService", "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {

    }
    public void getToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        ////////////////////토큰이 계속 초기화가 되기때문에 sharedPreferences로 저장하여 초기화 방지////////////////////
                        SharedPreferences sharedPreferences = getSharedPreferences("sFile1", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String token = task.getResult().getToken(); // 사용자가 입력한 저장할 데이터
                        editor.putString("Token1", token); // key, value를 이용하여 저장하는 형태
                        editor.commit();
                        ////////////////////토큰이 계속 초기화가 되기때문에 sharedPreferences로 저장하여 초기화 방지////////////////////
                    }
                });
    }
}