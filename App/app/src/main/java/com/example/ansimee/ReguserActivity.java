package com.example.ansimee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReguserActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    ImageButton home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reguser);
        findViewById(R.id.button4).setOnClickListener(onClickListener);
        home = findViewById(R.id.imageButton5);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button4:
                    regUser();
                    break;
                case R.id.home:
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
            }
        }
    };

    private void regUser() {
        DatabaseReference  mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String email = ((EditText) findViewById(R.id.editTextTextEmailAddress2)).getText().toString();
        String password = ((EditText) findViewById(R.id.editTextTextPassword2)).getText().toString();
        String phone = ((EditText) findViewById(R.id.editTextPhone2)).getText().toString();
        String address = ((EditText) findViewById(R.id.editTextTextPostalAddress2)).getText().toString();

        if (email.length() > 0 && password.length() > 0) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                final String uid = user.getUid();
                                mDatabase.child("user").child(uid).child("email").setValue(email);
                                mDatabase.child("user").child(uid).child("phone").setValue(phone);
                                mDatabase.child("user").child(uid).child("address").setValue(address);

                                Toast.makeText(ReguserActivity.this, "회원 가입 성공", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(ReguserActivity.this, "회원 가입 실패", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }

        else{
            Toast.makeText(ReguserActivity.this, "이메일/패스워드를 입력하세요", Toast.LENGTH_SHORT).show();
        }
    }

}
