package com.example.ansimee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResetpwActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Button gotologinbutton, sendemailbutton;
    ImageButton home;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpw);
        mAuth = FirebaseAuth.getInstance();
        home = findViewById(R.id.imageButton6);
        sendemailbutton = findViewById(R.id.button16);
        gotologinbutton = findViewById(R.id.button15);

        sendemailbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email();
            }
        });
        gotologinbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void email(){
        String emailAddress = ((EditText) findViewById(R.id.editTextTextEmailAddress3)).getText().toString();

        if (emailAddress.length() > 0 ) {
            mAuth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetpwActivity.this, "이메일을 전송했습니다", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(ResetpwActivity.this, "등록되지 않은 이메일이거나 잘못 입력되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else{
            Toast.makeText(ResetpwActivity.this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
        }

    }
}

