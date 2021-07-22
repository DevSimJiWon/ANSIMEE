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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class ModuserActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    Button moduserbutton;
    EditText phone, address;
    ImageButton home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moduser);
        phone = findViewById(R.id.editTextPhone);
        address = findViewById(R.id.editTextTextPostalAddress);
        moduserbutton = findViewById(R.id.button6);
        home = findViewById(R.id.imageButton3);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FirebaseUser user = mAuth.getCurrentUser();
                String uid = user.getUid();
                String phonenum = (String)dataSnapshot.child("user").child(uid).child("phone").getValue();
                String addr = (String)dataSnapshot.child("user").child(uid).child("address").getValue();
                phone.setText(phonenum);
                address.setText(addr);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        moduserbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moduser();
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

    private void moduser(){
        String password = ((EditText) findViewById(R.id.editTextTextPassword3)).getText().toString();
        String phonenum = ((EditText) findViewById(R.id.editTextPhone)).getText().toString();
        String addr = ((EditText) findViewById(R.id.editTextTextPostalAddress)).getText().toString();
        FirebaseUser user = mAuth.getCurrentUser();
        final String uid = user.getUid();
        user.updatePassword(password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        }
                    }
                });
        mDatabase.child("user").child(uid).child("phone").setValue(phonenum);
        mDatabase.child("user").child(uid).child("address").setValue(addr);
        Toast.makeText(ModuserActivity.this, "회원 정보 수정 완료", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
        startActivity(intent);
    }
}
