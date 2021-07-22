package com.example.ansimee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    ImageButton home;
    EditText account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        mAuth = FirebaseAuth.getInstance();
        home = findViewById(R.id.imageButton7);
        account = findViewById(R.id.editTextTextEmailAddress4);
        findViewById(R.id.button5).setOnClickListener(onClickListener);
        findViewById(R.id.button12).setOnClickListener(onClickListener);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FirebaseUser user = mAuth.getCurrentUser();
                String uid = user.getUid();
                String acc = (String)dataSnapshot.child("user").child(uid).child("email").getValue();
                account.setText(acc);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ShowActivity.class);
                startActivity(intent);
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.button5://회원정보 수정 버튼
                    Intent intent = new Intent(getApplicationContext(), ModuserActivity.class);
                    startActivity(intent);
                    break;


                case R.id.button12://로그아웃버튼
                    FirebaseAuth.getInstance().signOut();
                    Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent2);
                    break;
            }
        }
    };

}

