package com.example.ansimee;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegimgActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    StorageReference storageReference =  FirebaseStorage.getInstance().getReference();
    Button gallery, camera, upload;
    ImageButton home;
    ImageView imageView;
    EditText name;
    private Uri ImageUri;
    final int permission_code = 0; // 퍼미션 코드 설정
    static final int FROM_CAMERA = 1;
    static final int FROM_GALLERY = 10;
    String[] permission = { // 퍼미션 종류
            Manifest.permission.CAMERA, // 카메라
            Manifest.permission.READ_EXTERNAL_STORAGE, // 스토리지 읽기
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // 스토리지 쓰기
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regimg);
        mAuth = FirebaseAuth.getInstance();
        imageView = findViewById(R.id.imageView);
        gallery = findViewById(R.id.button14);
        camera = findViewById(R.id.button13);
        upload = findViewById(R.id.button7);
        name = findViewById(R.id.editTextTextPersonName);
        home = findViewById(R.id.imageButton4);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ShowActivity.class);
                startActivity(intent);
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, FROM_CAMERA);
                }
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, FROM_GALLERY);
                }
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
                String name = ((EditText) findViewById(R.id.editTextTextPersonName)).getText().toString();
                Map<String, Object> username2 = new HashMap<>();
                username2.put("username2", name );

                db.collection("RaspberryPi").document("rob")
                        .set(username2, SetOptions.merge())
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
                DatabaseReference  mDatabase;
                mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("Userupdatebool").setValue("True");
                FirebaseUser user = mAuth.getCurrentUser();
                final String uid = user.getUid();
                mDatabase.child("username").setValue(uid);

            }
        });
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap bitmap = (Bitmap)extras.get("data");
                    imageView.setImageBitmap(bitmap);
                    ImageUri = getImageUri(getApplicationContext(), bitmap);
                }
                break;
            case FROM_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    ImageUri = data.getData();
                    try {
                        ImageUri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), ImageUri);
                        imageView.setImageBitmap(bitmap);
                    } catch (Exception e){}

                }
        }
    }


    private Uri getImageUri(Context context, Bitmap bitmap){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"title", null);
        return Uri.parse(path);
    }
    private void uploadImage(){
        FirebaseUser user = mAuth.getCurrentUser();
        final String uid = user.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user_image").child(uid);
        String name = ((EditText) findViewById(R.id.editTextTextPersonName)).getText().toString();
        if(ImageUri != null) {
            StorageReference ref = storageReference.child("user_image").child(uid).child(name);
            ref.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            databaseReference.push().setValue(uri.toString());
                            Toast.makeText(RegimgActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(),ImglistActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegimgActivity.this, "업로드 실패", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(RegimgActivity.this, "null", Toast.LENGTH_SHORT).show();
        }
    }
    public void checkPermission(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){ // 버전이 마시멜로보다 낮으면
            return; //권한 실행 제한 없이 그냥 실행함 안드로이드 6.0 버전
        }
        for(int i = 0; i<permission.length; i++){
            String checkPermission = permission[i];
            int chk = checkCallingOrSelfPermission(checkPermission);
            if (chk == PackageManager.PERMISSION_DENIED) {
                //사용자에게 권한 허용 여부를 확인하는 창을 띄움
                requestPermissions(permission, permission_code); //호출이 되면 자동으로 onRequestPermissionResult 함수가 호출 permission_code 퍼미션을 구분하는 int 값
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // 파라미터 값은 permission_code , 권한 종류 permission , 허용하는 권한
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i = 0; i < grantResults.length; i++){
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) { //권한 체크를 거부하면 자동으로 꺼짐
                finish();
                System.exit(0);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }


}
