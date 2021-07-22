package com.example.ansimee;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingIDService extends FirebaseMessagingService {
    /**
     * 구글 토큰을 얻는 값, 아래 토큰은 앱이 설치된 디바이스에 대한 고유값으로 푸시를 보낼때 사용
     **/

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("Firebase", "FirebaseInstanceIDService : " + s);
    }

    /**
     * 메세지를 받았을 경우 그 메세지에 대하여 구현
     **/
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage != null && remoteMessage.getData().size() > 0) {
            sendNotification(remoteMessage);
        }
    }

    /**
     * remoteMessage 메세지 안애 getData와 getNotification 있음
     **/
    private void sendNotification(RemoteMessage remoteMessage) {

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");

    }
}
